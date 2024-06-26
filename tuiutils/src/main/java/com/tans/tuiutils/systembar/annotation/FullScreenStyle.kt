package com.tans.tuiutils.systembar.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class FullScreenStyle(val sticky: Boolean = true, val ignoreCutoutArea: Boolean = true)
