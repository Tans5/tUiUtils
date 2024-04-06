## 0 Summary  

Android UI Utils: Enhance your app with easy-to-use utilities for managing UI elements. This library streamlines status bar customization, permission handling, activity results, media retrieval, and RecyclerView adapter creation.

## 1 Usage Guide

### 1.0 Add Dependence

To use the TUIUtils library, add the following dependency to your `build.gradle` file:

```Groovy
// ...
dependencies {
    implementation "io.github.tans5:tuiutils:1.0.0"
}
```

### 1.1 Custom System Bar

#### 1.1.0 Initialization

If you’re using annotations to customize the system bar, include the following code when your app launches:

```Kotlin
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AutoApplySystemBarAnnotation.init(this)
    }
}
```

#### 1.1.1 Change Status and Navigation Bar Color

Add the `@SystemBarStyle` annotation to your Activity class for default transparent status and navigation bars.  

Alternatively, use the following code when your activity is created:

```Kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // Customize system bar color
    systemBarColor()

    // Make status bar and navigation bar light.
    systemBarThemeStyle(
        statusBarThemeStyle = SystemBarThemeStyle.Light,
        navigationThemeStyle = SystemBarThemeStyle.Light
    )
}
```

#### 1.1.2 Make Activity Full Screen

Add the @FullScreenStyle annotation to your Activity class.

Or use the following code when your activity is created:

```Kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_fit_system_window)
    
    // make activity full screen.
    this.fullScreenStyle()
}
```

### 1.2 Permission Request

#### 1.2.0 Use Callback

```Kotlin

permissionsRequest(
    permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 
    error = {
        
    }, 
    callback = { granted: Set<String>, denied: Set<String> ->
        
    })

permissionsRequestSimplify(
    permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
    error = {
        
    },
    callback = { isAllGranted ->
        
    }
)
```

#### 1.2.1 Use Coroutines

```Kotlin
launch {
    runCatching {
        permissionsRequestSuspend(Manifest.permission.ACCESS_FINE_LOCATION)
    }.onSuccess { (granted, denied) -> 
        // TODO:
    }.onFailure { 
        // TODO:
    }
    
    runCatching { 
        permissionsRequestSimplifySuspend(Manifest.permission.ACCESS_FINE_LOCATION)
    }.onSuccess { isAllGranted -> 
        // TODO:
    }.onFailure { 
        // TODO:
    }
}
```

#### 1.2.2 Use RxJava3

```Kotlin
permissionsRequestRx3(Manifest.permission.ACCESS_FINE_LOCATION)
    .subscribe({ (granted, denied) ->
        // TODO: 
    }, { t: Throwable ->
        // TODO:
    }
    )
permissionsRequestSimplifyRx3(Manifest.permission.ACCESS_FINE_LOCATION)
    .subscribe({ isGrantAll ->
        // TODO
    }, { t: Throwable ->
        // TODO:
    })
    
```

### 1.3 Start Activity Result

#### 1.3.0 Use Callback

```Kotlin
startActivityResult(
    targetActivityIntent = Intent(this, MainActivity::class.java),
    error = {
        // TODO:
    },
    callback = { resultCode, resultData ->  
        // TODO:
    }
)
```

#### 1.3.1 Use Coroutines

```Kotlin
 launch {
     runCatching {
         startActivityResultSuspend(Intent(this@MainActivity, MainActivity::class.java))
     }.onSuccess { (resultCode, resultData) ->
         // TODO:
     }.onFailure { 
         // TODO:
     }
 }
```

#### 1.3.2 Use RxJava3

```Kotlin
startActivityResultRx(Intent(this@MainActivity, MainActivity::class.java))
    .subscribe({ (resultCode, resultData) ->
        // TODO:
    }, { e: Throwable ->
        // TODO:
    })
```

### 1.3 Get Media Data

### 1.3.0 Query MediaData From MediaStore

After `Android 13` need permissions `READ_MEDIA_AUDIO`, `READ_MEDIA_VIDEO` and `READ_MEDIA_IMAGE`, Before `Android 13`, need permission `READ_EXTERNAL_STORAGE`.  

```Kotlin
val audios = queryAudioFromMediaStore()
val images = queryImageFromMediaStore()
val videos = queryVideoFromMediaStore()
```

### 1.3.1 Take a Photo

#### 1.3.1.0 Use Callback

```Kotlin
takeAPhoto(
    outputFileUri = outputUri,
    error = { error -> 
            // TODO:
    }, 
    callback = { isSuccess ->
           // TODO:
    })
```

#### 1.3.1.1 Use Coroutines

```Kotlin
runCatching {
    takeAPhotoSuspend(outputUri)
}.onSuccess { isSuccess ->
   // TODO:
}.onFailure { error ->
    // TODO:
}
```

#### 1.3.1.2 Use RxJava3

```Kotlin
 takeAPhotoRx(outputUri)
     .subscribe(
         { isSuccess ->
             // TODO:
         }, 
         { error ->
             // TODO:
         })
```

### 1.3.2 Pick a Image

#### 1.3.2.0 Use Callback

```Kotlin
pickImage(
    error = { error ->
            
        // TODO:
    },
    callback = { imageUri ->
        //TODO:
    })
```

#### 1.3.2.1 Use Coroutines

```Kotlin
launch {
    runCatching { pickImageSuspend() }
        .onSuccess { imageUri ->
            // TODO: 
        }
        .onFailure {  error ->
            // TODO:
        }
}
```

#### 1.3.2.2 Use RxJava3

```Kotlin
pickImageRx()
    .subscribe(
        { uri -> 
            // TODO:
        },
        {error -> 
            // TODO:
        }
    )
```

### 1.4 View

Base on Kotlin Coroutines.

#### 1.4.0 View Clicks

```Kotlin
viewBinding.fragmentActBt.clicks(
    coroutineScope = this,
    minInterval = 300L,
    clickWorkOn = Dispatchers.IO) {
    // TODO
}
```

#### 1.4.1 SwipeRefreshLayout Refresh

```Kotlin
viewBinding.swipeRefresh.refreshes(
    coroutineScope = this, 
    refreshWorkOn = Dispatchers.IO) {
    // TODO
}
```

### 1.5 RecyclerView Adapter Creation

#### 1.5.0 Simple Adapter Builder

```Kotlin
val adapterBuilder = SimpleAdapterBuilderImpl<MediaStoreImage>(
    itemViewCreator = SingleItemViewCreatorImpl(R.layout.image_item_layout),
    dataSource = FlowDataSourceImpl(stateFlow.map { it.images }),
    dataBinder = DataBinderImpl { data, view, _ ->
        val itemViewBinding = ImageItemLayoutBinding.bind(view)
        Glide.with(view)
            .load(data.uri)
            .into(itemViewBinding.imageIv)
    }
)
viewBinding.imagesRv.adapter = adapterBuilder.build()
```

#### 1.5.1 Combine Multiple Adapter Builder

```Kotlin
// Header
val headerAdapterBuilder = SimpleAdapterBuilderImpl<Unit>(
    itemViewCreator = SingleItemViewCreatorImpl(R.layout.header_footer_item_layout),
    dataSource = FlowDataSourceImpl(headerFooterDataFlow),
    dataBinder = DataBinderImpl { _, view, _ ->
        HeaderFooterItemLayoutBinding.bind(view).let { binding ->
            binding.msgTv.text = "Header"
        }
    }
)
// Audios Content
val audiosAdapterBuilder = SimpleAdapterBuilderImpl<MediaStoreAudio>(
    itemViewCreator = SingleItemViewCreatorImpl(R.layout.audio_item_layout),
    dataSource = FlowDataSourceImpl(stateFlow.map { it.audios }),
    dataBinder = DataBinderImpl { data, view, _ ->
        val itemViewBinding = AudioItemLayoutBinding.bind(view)
        itemViewBinding.musicTitleTv.text = data.title
        itemViewBinding.artistAlbumTv.text = "${data.artist}-${data.album}"
        view.clicks(this) {
            Toast.makeText(this@AudiosFragment.requireContext(), data.title, Toast.LENGTH_SHORT).show()
        }
    }
)
// Footer
val footerAdapterBuilder = SimpleAdapterBuilderImpl<Unit>(
    itemViewCreator = SingleItemViewCreatorImpl(R.layout.header_footer_item_layout),
    dataSource = FlowDataSourceImpl(headerFooterDataFlow),
    dataBinder = DataBinderImpl { _, view, _ ->
        HeaderFooterItemLayoutBinding.bind(view).let { binding ->
            binding.msgTv.text = "Footer"
        }
    }
)
// Empty
val emptyAdapterBuilder = SimpleAdapterBuilderImpl<Unit>(
    itemViewCreator = SingleItemViewCreatorImpl(R.layout.empty_content_layout),
    dataSource = FlowDataSourceImpl(stateFlow.map { if (it.audios.isEmpty() && it.hasLoadFirstData) listOf(Unit) else emptyList() }),
    dataBinder = DataBinderImpl { _, view, _ ->
        EmptyContentLayoutBinding.bind(view).let { binding ->
            binding.msgTv.text = "No Audio."
        }
    }
)
viewBinding.audiosRv.adapter = (headerAdapterBuilder + audiosAdapterBuilder + footerAdapterBuilder + emptyAdapterBuilder).build()
```



