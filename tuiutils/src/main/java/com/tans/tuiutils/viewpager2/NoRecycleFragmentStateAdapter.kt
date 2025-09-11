/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tans.tuiutils.viewpager2

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import androidx.annotation.OptIn
import androidx.annotation.RequiresOptIn
import androidx.collection.ArraySet
import androidx.collection.LongSparseArray
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.tans.tuiutils.viewpager2.FragmentViewHolder.Companion.create
import java.util.Collections
import java.util.concurrent.CopyOnWriteArrayList


/**
 * Similar in behavior to [ FragmentStatePagerAdapter][androidx.fragment.app.FragmentStatePagerAdapter]
 *
 *
 * Lifecycle within [RecyclerView]:
 *
 *  * [RecyclerView.ViewHolder] initially an empty [FrameLayout], serves as a
 * re-usable container for a [Fragment] in later stages.
 *  * [RecyclerView.Adapter.onBindViewHolder] we ask for a [Fragment] for the
 * position. If we already have the fragment, or have previously saved its state, we use those.
 *  *  RecyclerView.Adapter#onAttachedToWindow we attach the [Fragment] to a
 * container.
 *  * [RecyclerView.Adapter.onViewRecycled] we remove, save state, destroy the
 * [Fragment].
 *
 */
abstract class NoRecycleFragmentStateAdapter(// to avoid creation of a synthetic accessor
    val mFragmentManager: FragmentManager,
    // to avoid creation of a synthetic accessor
    val mLifecycle: Lifecycle
) :
    RecyclerView.Adapter<FragmentViewHolder>() {
    // Fragment bookkeeping
    // to avoid creation of a synthetic accessor
    val mFragments: LongSparseArray<Fragment> = LongSparseArray()

    // private final LongSparseArray<Fragment.SavedState> mSavedStates = new LongSparseArray<>();
    private val mItemIdToViewHolder = LongSparseArray<Int>()

    private var mFragmentMaxLifecycleEnforcer: FragmentMaxLifecycleEnforcer? = null

    var mFragmentEventDispatcher:  // to avoid creation of a synthetic accessor
            FragmentEventDispatcher = FragmentEventDispatcher()

    // Fragment GC
    var mIsInGracePeriod:  // to avoid creation of a synthetic accessor
            Boolean = false
    private var mHasStaleFragments = false

    /**
     * @param fragmentActivity if the [ViewPager2] lives directly in a
     * [FragmentActivity] subclass.
     *
     * @see NoRecycleFragmentStateAdapter.NoRecycleFragmentStateAdapter
     * @see NoRecycleFragmentStateAdapter.NoRecycleFragmentStateAdapter
     */
    constructor(fragmentActivity: FragmentActivity) : this(
        fragmentActivity.supportFragmentManager,
        fragmentActivity.lifecycle
    )

    /**
     * @param fragment if the [ViewPager2] lives directly in a [Fragment] subclass.
     *
     * @see NoRecycleFragmentStateAdapter.NoRecycleFragmentStateAdapter
     * @see NoRecycleFragmentStateAdapter.NoRecycleFragmentStateAdapter
     */
    constructor(fragment: Fragment) : this(fragment.childFragmentManager, fragment.lifecycle)

    /**
     * @param fragmentManager of [ViewPager2]'s host
     * @param lifecycle of [ViewPager2]'s host
     *
     * @see NoRecycleFragmentStateAdapter.NoRecycleFragmentStateAdapter
     * @see NoRecycleFragmentStateAdapter.NoRecycleFragmentStateAdapter
     */
    init {
        // mLifecycle = mLifecycle
        super.setHasStableIds(true)
    }

    @CallSuper
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        // checkArgument(mFragmentMaxLifecycleEnforcer == null);
        mFragmentMaxLifecycleEnforcer = FragmentMaxLifecycleEnforcer()
        mFragmentMaxLifecycleEnforcer!!.register(recyclerView)
    }

    @CallSuper
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        mFragmentMaxLifecycleEnforcer!!.unregister(recyclerView)
        mFragmentMaxLifecycleEnforcer = null
    }

    /**
     * Provide a new Fragment associated with the specified position.
     *
     *
     * The adapter will be responsible for the Fragment lifecycle:
     *
     *  * The Fragment will be used to display an item.
     *  * The Fragment will be destroyed when it gets too far from the viewport, and its state
     * will be saved. When the item is close to the viewport again, a new Fragment will be
     * requested, and a previously saved state will be used to initialize it.
     *
     * @see ViewPager2.setOffscreenPageLimit
     */
    abstract fun createFragment(position: Int): Fragment

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FragmentViewHolder {
        return create(parent)
    }

    override fun onBindViewHolder(holder: FragmentViewHolder, position: Int) {
        // Data id
        val itemId = holder.itemId
        // ContainerView id
        val viewHolderId = holder.container.id
        // 当前绑定到 ViewHolder 的 item id.
        val boundItemId = itemForViewHolder(viewHolderId) // item currently bound to the VH
        if (boundItemId != null && boundItemId != itemId) {
            // ViewHolder 中当前的 Fragment 和新添加的 Fragment 发生改变，移除旧的 Fragment
            removeFragment(boundItemId)
            mItemIdToViewHolder.remove(boundItemId)
        }

        mItemIdToViewHolder.put(itemId, viewHolderId) // this might overwrite an existing entry
        ensureFragment(position) // 确保当前位置的 Fragment 已经创建

        /* Special case when {@link RecyclerView} decides to keep the {@link container}
         * attached to the window, resulting in no {@link `onViewAttachedToWindow} callback later */
        val container = holder.container
        // 如果 container 已经 attach，将 Fragment 中的 View 添加到 Container 中。
        // 如果 container 没有被 attach，在 onViewAttachedToWindow() 回调后再做这个操作。
        if (container.isAttachedToWindow) {
            placeFragmentInViewHolder(holder)
        }

        // 清除已经移除的 Fragments
        gcFragments()
    }

    fun gcFragments() {
        if (!mHasStaleFragments || shouldDelayFragmentTransactions()) {
            return
        }

        // Remove Fragments for items that are no longer part of the data-set
        val toRemove: MutableSet<Long> = ArraySet()
        for (ix in 0 until mFragments.size()) { // 移除已经不存在的 Fragment
            val itemId = mFragments.keyAt(ix)
            if (!containsItem(itemId)) {
                toRemove.add(itemId)
                mItemIdToViewHolder.remove(itemId) // in case they're still bound
            }
        }

        // Remove Fragments that are not bound anywhere -- pending a grace period
        if (!mIsInGracePeriod) {
            mHasStaleFragments = false // we've executed all GC checks

            for (ix in 0 until mFragments.size()) { // 移除已经不在 ViewHolder 中的 Fragment.
                val itemId = mFragments.keyAt(ix)
                if (!isFragmentViewBound(itemId)) {
                    toRemove.add(itemId)
                }
            }
        }

        for (itemId in toRemove) {
            removeFragment(itemId)
        }
    }

    private fun isFragmentViewBound(itemId: Long): Boolean {
        if (mItemIdToViewHolder.containsKey(itemId)) {
            return true
        }

        val fragment = mFragments[itemId] ?: return false

        val view = fragment.view ?: return false

        return view.parent != null
    }

    private fun itemForViewHolder(viewHolderId: Int): Long? {
        var boundItemId: Long? = null
        for (ix in 0 until mItemIdToViewHolder.size()) {
            if (mItemIdToViewHolder.valueAt(ix) == viewHolderId) {
                check(boundItemId == null) {
                    ("Design assumption violated: "
                            + "a ViewHolder can only be bound to one item at a time.")
                }
                boundItemId = mItemIdToViewHolder.keyAt(ix)
            }
        }
        return boundItemId
    }

    private fun ensureFragment(position: Int) {
        val itemId = getItemId(position)
        if (!mFragments.containsKey(itemId)) {
            // TODO(133419201): check if a Fragment provided here is a new Fragment
            val newFragment = createFragment(position)
            // 移除保存的状态代码
            // newFragment.setInitialSavedState(mSavedStates.get(itemId));

            if (newFragment.isAdded) { // 如果当前 fragment 已经添加，移除它
                val tag = newFragment.tag
                val id = newFragment.id
                val idFragment = mFragmentManager.findFragmentById(id)
                val tagFragment = mFragmentManager.findFragmentByTag(tag)
                if (idFragment != null || tagFragment != null) { // 如果在当前的 fragment manager 中，先移除它
                    val tc = mFragmentManager.beginTransaction()
                    if (idFragment != null) {
                        tc.remove(idFragment)
                    }
                    if (tagFragment != null && tagFragment !== idFragment) {
                        tc.remove(tagFragment)
                    }
                    tc.commitNowAllowingStateLoss()
                }

                // 重新初始化状态
                try {
                    val initMethod = Fragment::class.java.getDeclaredMethod("initState")
                    if (initMethod != null) {
                        initMethod.isAccessible = true
                        initMethod.invoke(newFragment)
                    }
                } catch (_: Throwable) {

                }
            }

            mFragments.put(itemId, newFragment)
        }
    }

    override fun onViewAttachedToWindow(holder: FragmentViewHolder) {
        // 如果 onBindViewHolder 执行时 view 还没有 attach，attach 后在这个回调方法中继续执行后续的操作.
        placeFragmentInViewHolder(holder)
        gcFragments()
    }

    /**
     * @param holder that has been bound to a Fragment in the [.onBindViewHolder] stage.
     */
    fun placeFragmentInViewHolder(holder: FragmentViewHolder) {
        val fragment = mFragments[holder.itemId]
        checkNotNull(fragment) { "Design assumption violated." }
        val container = holder.container
        val view = fragment.view

        /*
        possible states:
        - fragment: { added, notAdded }
        - view: { created, notCreated }
        - view: { attached, notAttached }

        combinations:
        - { f:added, v:created, v:attached } -> check if attached to the right container
        - { f:added, v:created, v:notAttached} -> attach view to container
        - { f:added, v:notCreated, v:attached } -> impossible
        - { f:added, v:notCreated, v:notAttached} -> schedule callback for when created
        - { f:notAdded, v:created, v:attached } -> illegal state
        - { f:notAdded, v:created, v:notAttached } -> illegal state
        - { f:notAdded, v:notCreated, v:attached } -> impossible
        - { f:notAdded, v:notCreated, v:notAttached } -> add, create, attach
         */

        // { f:notAdded, v:created, v:attached } -> illegal state
        // { f:notAdded, v:created, v:notAttached } -> illegal state
        check(!(!fragment.isAdded && view != null)) { "Design assumption violated." }

        // { f:added, v:notCreated, v:notAttached} -> schedule callback for when created
        if (fragment.isAdded && view == null) { // Fragment 已经添加，但是对应的 view 还没有创建
            // 等待 Fragment 的 View 创建后添加到 container 中
            scheduleViewAttach(fragment, container)
            return
        }

        // { f:added, v:created, v:attached } -> check if attached to the right container
        if (fragment.isAdded && view!!.parent != null) { // Fragment 已经添加，但是对应的 view 的 parent 已经不为空
            if (view.parent !== container) { // 如果 parent 不是当前 container，需要重新添加到当前 container.
                addViewToContainer(view, container)
            }
            return
        }

        // { f:added, v:created, v:notAttached} -> attach view to container
        if (fragment.isAdded) { // Fragment 已经添加，对应的 view 已经创建，但是还没有添加 container
            addViewToContainer(view!!, container)
            return
        }

        // 后续就是 Fragment 没有添加的逻辑了。

        // { f:notAdded, v:notCreated, v:notAttached } -> add, create, attach
        if (!shouldDelayFragmentTransactions()) { // 不延迟处理
            // 等待 Fragment 中的 View 完成创建
            scheduleViewAttach(fragment, container)
            val onPost =
                mFragmentEventDispatcher.dispatchPreAdded(fragment)
            try {
                fragment.setMenuVisibility(false) // appropriate for maxLifecycle == STARTED
                // 触发 Fragment 生命周期
                mFragmentManager.beginTransaction()
                    .add(fragment, "f" + holder.itemId)
                    .setMaxLifecycle(fragment, Lifecycle.State.STARTED)
                    .commitNowAllowingStateLoss()
                mFragmentMaxLifecycleEnforcer!!.updateFragmentMaxLifecycle(false)
            } finally {
                mFragmentEventDispatcher.dispatchPostEvents(onPost)
            }
        } else { // 延迟处理，其实就是 FragmentManager 已经执行完 onSaveState
            if (mFragmentManager.isDestroyed) {
                return  // nothing we can do
            }
            mLifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(
                    source: LifecycleOwner,
                    event: Lifecycle.Event
                ) {
                    if (shouldDelayFragmentTransactions()) {
                        return
                    }
                    source.lifecycle.removeObserver(this)
                    if (holder.container.isAttachedToWindow) {
                        placeFragmentInViewHolder(holder)
                    }
                }
            })
        }
    }

    /**
     * 等待 Fragment 中的 View 完成创建后，添加到 Container 中。
     * @param fragment
     * @param container
     */
    private fun scheduleViewAttach(fragment: Fragment, container: FrameLayout) {
        // After a config change, Fragments that were in FragmentManager will be recreated. Since
        // ViewHolder container ids are dynamically generated, we opted to manually handle
        // attaching Fragment views to containers. For consistency, we use the same mechanism for
        // all Fragment views.
        mFragmentManager.registerFragmentLifecycleCallbacks(
            object : FragmentManager.FragmentLifecycleCallbacks() {
                // TODO(b/141956012): Suppressed during upgrade to AGP 3.6.
                override fun onFragmentViewCreated(
                    fm: FragmentManager,
                    f: Fragment, v: View,
                    savedInstanceState: Bundle?
                ) {
                    if (f === fragment) {
                        fm.unregisterFragmentLifecycleCallbacks(this)
                        addViewToContainer(v, container)
                    }
                }
            }, false
        )
    }

    fun addViewToContainer(v: View, container: FrameLayout) {
        check(container.childCount <= 1) { "Design assumption violated." }

        if (v.parent === container) {
            return
        }

        if (container.childCount > 0) {
            container.removeAllViews()
        }

        if (v.parent != null) {
            (v.parent as ViewGroup).removeView(v)
        }

        container.addView(v)

        // TODO: 不知道为什么，有时会出现 container view 没有 layout 的问题.
        container.postDelayed({
            container.requestLayout()
        }, 20)
    }

    /**
     * ViewHolder 被回收
     * @param holder The ViewHolder for the view being recycled
     */
    override fun onViewRecycled(holder: FragmentViewHolder) {
        val viewHolderId = holder.container.id
        val boundItemId = itemForViewHolder(viewHolderId) // item currently bound to the VH
        if (boundItemId != null) {
            stopFragment(boundItemId)
            mItemIdToViewHolder.remove(boundItemId)
        }
    }

    override fun onFailedToRecycleView(holder: FragmentViewHolder): Boolean {
        /*
         This happens when a ViewHolder is in a transient state (e.g. during an
         animation).

         Our ViewHolders are effectively just FrameLayout instances in which we put Fragment
         Views, so it's safe to force recycle them. This is because:
         - FrameLayout instances are not to be directly manipulated, so no animations are
         expected to be running directly on them.
         - Fragment Views are not reused between position (one Fragment = one page). Animation
         running in one of the Fragment Views won't affect another Fragment View.
         - If a user chooses to violate these assumptions, they are also in the position to
         correct the state in their code.
        */
        return true
    }

    private fun removeFragment(itemId: Long) {
        val fragment = mFragments[itemId] ?: return

        if (fragment.view != null) {
            val viewParent = fragment.requireView().parent
            if (viewParent != null) {
                (viewParent as FrameLayout).removeAllViews()
            }
        }

        //        if (!containsItem(itemId)) {
//            mSavedStates.remove(itemId);
//        }
        if (!fragment.isAdded) {
            mFragments.remove(itemId)
            return
        }

        if (shouldDelayFragmentTransactions()) {
            mHasStaleFragments = true
            return
        }

        if (fragment.isAdded && containsItem(itemId)) {
            // 保存当前 Fragment 的状态，删除这部分代码.
//            List<OnPostEventListener> onPost =
//                    mFragmentEventDispatcher.dispatchPreSavedInstanceState(fragment);
//            Fragment.SavedState savedState = mFragmentManager.saveFragmentInstanceState(fragment);
//            mFragmentEventDispatcher.dispatchPostEvents(onPost);
//
//            mSavedStates.put(itemId, savedState);
        }
        val onPost =
            mFragmentEventDispatcher.dispatchPreRemoved(fragment)

        // 移除 Fragment.
        try {
            mFragmentManager.beginTransaction().remove(fragment).commitNowAllowingStateLoss()
            mFragments.remove(itemId)
        } finally {
            mFragmentEventDispatcher.dispatchPostEvents(onPost)
        }
    }

    private fun stopFragment(itemId: Long) {
        val fragment = mFragments[itemId] ?: return

        if (fragment.view != null) {
            val viewParent = fragment.requireView().parent
            if (viewParent != null) {
                (viewParent as FrameLayout).removeAllViews()
            }
        }

        if (!fragment.isAdded) {
            mFragments.remove(itemId)
            return
        }

        if (shouldDelayFragmentTransactions()) {
            mHasStaleFragments = true
            return
        }

        mFragmentManager.beginTransaction()
            .setMaxLifecycle(fragment, Lifecycle.State.CREATED)
            .commitNowAllowingStateLoss()
    }

    fun shouldDelayFragmentTransactions(): Boolean {
        return mFragmentManager.isStateSaved
    }

    /**
     * Default implementation works for collections that don't add, move, remove items.
     *
     *
     * When overriding, also override [.containsItem].
     *
     *
     * If the item is not a part of the collection, return [RecyclerView.NO_ID].
     *
     * @param position Adapter position
     * @return stable item id [RecyclerView.Adapter.hasStableIds]
     */
    // TODO(b/122670460): add lint rule
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /**
     * Default implementation works for collections that don't add, move, remove items.
     *
     *
     * When overriding, also override [.getItemId]
     */
    // TODO(b/122670460): add lint rule
    fun containsItem(itemId: Long): Boolean {
        return itemId >= 0 && itemId < itemCount
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        throw UnsupportedOperationException(
            "Stable Ids are required for the adapter to function properly, and the adapter "
                    + "takes care of setting the flag."
        )
    }

    // 移除数据保存和恢复
    //    @Override
    //    public final @NonNull Parcelable saveState() {
    //        /* TODO(b/122670461): use custom {@link Parcelable} instead of Bundle to save space */
    //        Bundle savedState = new Bundle(mFragments.size() + mSavedStates.size());
    //
    //        /* save references to active fragments */
    //        for (int ix = 0; ix < mFragments.size(); ix++) {
    //            long itemId = mFragments.keyAt(ix);
    //            Fragment fragment = mFragments.get(itemId);
    //            if (fragment != null && fragment.isAdded()) {
    //                String key = createKey(KEY_PREFIX_FRAGMENT, itemId);
    //                mFragmentManager.putFragment(savedState, key, fragment);
    //            }
    //        }
    //
    //        /* Write {@link mSavedStates) into a {@link Parcelable} */
    //        for (int ix = 0; ix < mSavedStates.size(); ix++) {
    //            long itemId = mSavedStates.keyAt(ix);
    //            if (containsItem(itemId)) {
    //                String key = createKey(KEY_PREFIX_STATE, itemId);
    //                savedState.putParcelable(key, mSavedStates.get(itemId));
    //            }
    //        }
    //
    //        return savedState;
    //    }
    //
    //    @Override
    //    @SuppressWarnings("deprecation")
    //    public final void restoreState(@NonNull Parcelable savedState) {
    //        if (!mSavedStates.isEmpty() || !mFragments.isEmpty()) {
    //            throw new IllegalStateException(
    //                    "Expected the adapter to be 'fresh' while restoring state.");
    //        }
    //
    //        Bundle bundle = (Bundle) savedState;
    //        if (bundle.getClassLoader() == null) {
    //            /* TODO(b/133752041): pass the class loader from {@link ViewPager2.SavedState } */
    //            bundle.setClassLoader(getClass().getClassLoader());
    //        }
    //
    //        for (String key : bundle.keySet()) {
    //            if (isValidKey(key, KEY_PREFIX_FRAGMENT)) {
    //                long itemId = parseIdFromKey(key, KEY_PREFIX_FRAGMENT);
    //                Fragment fragment = mFragmentManager.getFragment(bundle, key);
    //                mFragments.put(itemId, fragment);
    //                continue;
    //            }
    //
    //            if (isValidKey(key, KEY_PREFIX_STATE)) {
    //                long itemId = parseIdFromKey(key, KEY_PREFIX_STATE);
    //                Fragment.SavedState state = bundle.getParcelable(key);
    //                if (containsItem(itemId)) {
    //                    mSavedStates.put(itemId, state);
    //                }
    //                continue;
    //            }
    //
    //            throw new IllegalArgumentException("Unexpected key in savedState: " + key);
    //        }
    //
    //        if (!mFragments.isEmpty()) {
    //            mHasStaleFragments = true;
    //            mIsInGracePeriod = true;
    //            gcFragments();
    //            scheduleGracePeriodEnd();
    //        }
    //    }
    //
    //    private void scheduleGracePeriodEnd() {
    //        final Handler handler = new Handler(Looper.getMainLooper());
    //        final Runnable runnable = new Runnable() {
    //            @Override
    //            public void run() {
    //                mIsInGracePeriod = false;
    //                gcFragments(); // good opportunity to GC
    //            }
    //        };
    //
    //        mLifecycle.addObserver(new LifecycleEventObserver() {
    //            @Override
    //            public void onStateChanged(@NonNull LifecycleOwner source,
    //                                       @NonNull Lifecycle.Event event) {
    //                if (event == Lifecycle.Event.ON_DESTROY) {
    //                    handler.removeCallbacks(runnable);
    //                    source.getLifecycle().removeObserver(this);
    //                }
    //            }
    //        });
    //
    //        handler.postDelayed(runnable, GRACE_WINDOW_TIME_MS);
    //    }
    //    // Helper function for dealing with save / restore state
    //    private static @NonNull String createKey(@NonNull String prefix, long id) {
    //        return prefix + id;
    //    }
    //
    //    // Helper function for dealing with save / restore state
    //    private static boolean isValidKey(@NonNull String key, @NonNull String prefix) {
    //        return key.startsWith(prefix) && key.length() > prefix.length();
    //    }
    //
    //    // Helper function for dealing with save / restore state
    //    private static long parseIdFromKey(@NonNull String key, @NonNull String prefix) {
    //        return Long.parseLong(key.substring(prefix.length()));
    //    }
    /**
     * Pauses (STARTED) all Fragments that are attached and not a primary item.
     * Keeps primary item Fragment RESUMED.
     */
    internal inner class FragmentMaxLifecycleEnforcer {
        private var mPageChangeCallback: OnPageChangeCallback? = null
        private var mDataObserver: RecyclerView.AdapterDataObserver? = null
        private var mLifecycleObserver: LifecycleEventObserver? = null
        private var mViewPager: ViewPager2? = null

        // 选中的 item
        private var mPrimaryItemId = RecyclerView.NO_ID

        fun register(recyclerView: RecyclerView) {
            mViewPager = inferViewPager(recyclerView)

            // signal 1 of 3: current item has changed
            // ViewPager 滑动
            val pageChangeCallback = object : OnPageChangeCallback() {
                override fun onPageScrollStateChanged(state: Int) {
                    updateFragmentMaxLifecycle(false)
                }

                override fun onPageSelected(position: Int) {
                    updateFragmentMaxLifecycle(false)
                }
            }
            mPageChangeCallback = pageChangeCallback
            mViewPager!!.registerOnPageChangeCallback(pageChangeCallback)

            // signal 2 of 3: underlying data-set has been updated
            // Adapter 数据改变
            val dataObserver = object : DataSetChangeObserver() {
                override fun onChanged() {
                    updateFragmentMaxLifecycle(true)
                }
            }
            mDataObserver = dataObserver
            registerAdapterDataObserver(dataObserver)

            // signal 3 of 3: we may have to catch-up after being in a lifecycle state that
            // prevented us to perform transactions
            // LifecycleOwner 的生命周期发生改变
            val lifecycleObserver =
                LifecycleEventObserver { source, event -> updateFragmentMaxLifecycle(false) }
            mLifecycleObserver = lifecycleObserver
            mLifecycle.addObserver(lifecycleObserver)
        }

        fun unregister(recyclerView: RecyclerView) {
            val viewPager = inferViewPager(recyclerView)
            viewPager.unregisterOnPageChangeCallback(mPageChangeCallback!!)
            unregisterAdapterDataObserver(mDataObserver!!)
            mLifecycle.removeObserver(mLifecycleObserver!!)
            mViewPager = null
        }

        // 更新 Fragment 生命周期
        fun updateFragmentMaxLifecycle(dataSetChanged: Boolean) {
            if (shouldDelayFragmentTransactions()) {
                return  /* recovery step via {@link #mLifecycleObserver} */
            }

            // 只有静止状态才处理
            if (mViewPager!!.scrollState != ViewPager2.SCROLL_STATE_IDLE) {
                return  // do not update while not idle to avoid jitter
            }

            if (mFragments.isEmpty() || itemCount == 0) {
                return  // nothing to do
            }

            val currentItem = mViewPager!!.currentItem
            if (currentItem >= itemCount) {
                /* current item is yet to be updated; it is guaranteed to change, so we will be
                 * notified via {@link ViewPager2.OnPageChangeCallback#onPageSelected(int)}  */
                return
            }

            // 当前选中的 ItemId
            val currentItemId = getItemId(currentItem)
            if (currentItemId == mPrimaryItemId && !dataSetChanged) { // 如果选中的 Item 没有改变，同时数据也没有改变，直接返回。
                return  // nothing to do
            }

            val currentItemFragment = mFragments[currentItemId]
            if (currentItemFragment == null || !currentItemFragment.isAdded) { // 当前选中的 Fragment 为空或者没有被添加到 FragmentManager 中，直接返回。
                return
            }

            mPrimaryItemId = currentItemId
            val transaction = mFragmentManager.beginTransaction()

            var toResume: Fragment? = null
            val onPost: MutableList<List<FragmentTransactionCallback.OnPostEventListener>> =
                ArrayList()
            for (ix in 0 until mFragments.size()) {
                val itemId = mFragments.keyAt(ix)
                val fragment = mFragments.valueAt(ix)

                if (!fragment.isAdded) { // 已经添加到 FragmentManager 的 Fragment 跳过
                    continue
                }

                if (itemId != mPrimaryItemId) {
                    // 没有被选中的 Fragment 生命周期为 STARTED
                    transaction.setMaxLifecycle(fragment, Lifecycle.State.STARTED)
                    onPost.add(
                        mFragmentEventDispatcher.dispatchMaxLifecyclePreUpdated(
                            fragment,
                            Lifecycle.State.STARTED
                        )
                    )
                } else {
                    toResume = fragment // itemId map key, so only one can match the predicate
                }

                // 选中的 Fragment 的 Menu 可见
                fragment.setMenuVisibility(itemId == mPrimaryItemId)
            }
            if (toResume != null) { // in case the Fragment wasn't added yet
                // 设置选中的 Fragment 生命周期为 RESUMED
                transaction.setMaxLifecycle(toResume, Lifecycle.State.RESUMED)
                onPost.add(
                    mFragmentEventDispatcher.dispatchMaxLifecyclePreUpdated(
                        toResume,
                        Lifecycle.State.RESUMED
                    )
                )
            }

            if (!transaction.isEmpty) {
                // 提交
                transaction.commitNowAllowingStateLoss()
                Collections.reverse(onPost) // to assure 'nesting' of events
                for (event in onPost) {
                    mFragmentEventDispatcher.dispatchPostEvents(event)
                }
            }
        }

        private fun inferViewPager(recyclerView: RecyclerView): ViewPager2 {
            val parent = recyclerView.parent
            if (parent is ViewPager2) {
                return parent
            }
            throw IllegalStateException("Expected ViewPager2 instance. Got: $parent")
        }
    }

    /**
     * Simplified [RecyclerView.AdapterDataObserver] for clients interested in any data-set
     * changes regardless of their nature.
     */
    private abstract class DataSetChangeObserver : RecyclerView.AdapterDataObserver() {
        abstract override fun onChanged()

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            onChanged()
        }

        override fun onItemRangeChanged(
            positionStart: Int, itemCount: Int,
            payload: Any?
        ) {
            onChanged()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            onChanged()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            onChanged()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            onChanged()
        }
    }

    class FragmentEventDispatcher {
        private val mCallbacks: MutableList<FragmentTransactionCallback> = CopyOnWriteArrayList()

        fun registerCallback(callback: FragmentTransactionCallback) {
            mCallbacks.add(callback)
        }

        fun unregisterCallback(callback: FragmentTransactionCallback) {
            mCallbacks.remove(callback)
        }

        fun dispatchMaxLifecyclePreUpdated(
            fragment: Fragment,
            maxState: Lifecycle.State
        ): List<FragmentTransactionCallback.OnPostEventListener> {
            val result: MutableList<FragmentTransactionCallback.OnPostEventListener> = ArrayList()
            for (callback in mCallbacks) {
                result.add(callback.onFragmentMaxLifecyclePreUpdated(fragment, maxState))
            }
            return result
        }

        fun dispatchPostEvents(entries: List<FragmentTransactionCallback.OnPostEventListener>) {
            for (entry in entries) {
                entry.onPost()
            }
        }

        fun dispatchPreAdded(fragment: Fragment): List<FragmentTransactionCallback.OnPostEventListener> {
            val result: MutableList<FragmentTransactionCallback.OnPostEventListener> = ArrayList()
            for (callback in mCallbacks) {
                result.add(callback.onFragmentPreAdded(fragment))
            }
            return result
        }

        @OptIn(markerClass = [ExperimentalFragmentStateAdapterApi::class])
        fun dispatchPreSavedInstanceState(fragment: Fragment): List<FragmentTransactionCallback.OnPostEventListener> {
            val result: MutableList<FragmentTransactionCallback.OnPostEventListener> = ArrayList()
            for (callback in mCallbacks) {
                result.add(callback.onFragmentPreSavedInstanceState(fragment))
            }
            return result
        }

        fun dispatchPreRemoved(fragment: Fragment): List<FragmentTransactionCallback.OnPostEventListener> {
            val result: MutableList<FragmentTransactionCallback.OnPostEventListener> = ArrayList()
            for (callback in mCallbacks) {
                result.add(callback.onFragmentPreRemoved(fragment))
            }
            return result
        }
    }

    /**
     * Callback interface for listening to fragment lifecycle changes that happen
     * inside the adapter.
     */
    abstract class FragmentTransactionCallback {
        /**
         * Called right before the Fragment is added to adapter's FragmentManager.
         *
         * @param fragment Fragment changing state
         * @return Listener called after the operation
         */
        fun onFragmentPreAdded(fragment: Fragment): OnPostEventListener {
            return NO_OP
        }

        /**
         * Called right before Fragment's state is being saved through a
         * [FragmentManager.saveFragmentInstanceState] call.
         *
         * @param fragment Fragment which state is being saved
         * @return Listener called after the operation
         */
        @ExperimentalFragmentStateAdapterApi // Experimental in v1.1.*. To become stable in v1.2.*.
        fun onFragmentPreSavedInstanceState(fragment: Fragment): OnPostEventListener {
            return NO_OP
        }

        /**
         * Called right before the Fragment is removed from adapter's FragmentManager.
         *
         * @param fragment Fragment changing state
         * @return Listener called after the operation
         */
        fun onFragmentPreRemoved(fragment: Fragment): OnPostEventListener {
            return NO_OP
        }

        /**
         * Called right before Fragment's maximum state is capped via
         * [FragmentTransaction.setMaxLifecycle].
         *
         * @param fragment Fragment to have its state capped
         * @param maxLifecycleState Ceiling state for the fragment
         * @return Listener called after the operation
         */
        fun onFragmentMaxLifecyclePreUpdated(
            fragment: Fragment,
            maxLifecycleState: Lifecycle.State
        ): OnPostEventListener {
            return NO_OP
        }

        /**
         * Callback returned by [.onFragmentPreAdded], [.onFragmentPreRemoved],
         * [.onFragmentMaxLifecyclePreUpdated] called after the operation ends.
         */
        interface OnPostEventListener {
            /** Called after the operation is ends.  */
            fun onPost()
        }

        companion object {
            private val NO_OP: OnPostEventListener = object : OnPostEventListener {
                override fun onPost() {
                    // do nothing
                }
            }
        }
    }

    /**
     * Registers a [FragmentTransactionCallback] to listen to fragment lifecycle changes
     * that happen inside the adapter.
     *
     * @param callback Callback to register
     */
    fun registerFragmentTransactionCallback(callback: FragmentTransactionCallback) {
        mFragmentEventDispatcher.registerCallback(callback)
    }

    /**
     * Unregisters a [FragmentTransactionCallback].
     *
     * @param callback Callback to unregister
     * @see .registerFragmentTransactionCallback
     */
    fun unregisterFragmentTransactionCallback(
        callback: FragmentTransactionCallback
    ) {
        mFragmentEventDispatcher.unregisterCallback(callback)
    }

    @RequiresOptIn(level = RequiresOptIn.Level.WARNING)
    annotation class ExperimentalFragmentStateAdapterApi
    companion object {
        // State saving config
        private const val KEY_PREFIX_FRAGMENT = "f#"
        private const val KEY_PREFIX_STATE = "s#"

        // Fragment GC config
        private const val GRACE_WINDOW_TIME_MS: Long = 10000 // 10 seconds
    }
}
