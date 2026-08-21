package com.slowbuild.storyverse.core.storage

private var androidStorageDir: String? = null

fun setAndroidStorageDirectory(path: String) {
    androidStorageDir = path
}

actual fun getAppStorageDirectory(): String {
    return androidStorageDir ?: System.getProperty("java.io.tmpdir") ?: "/tmp"
}
