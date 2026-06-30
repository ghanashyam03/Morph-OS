package com.morphos.app.initializer

import android.content.Context
import androidx.startup.Initializer
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache

import okio.Path.Companion.toPath

class CoilInitializer : Initializer<ImageLoader> {
    override fun create(context: Context): ImageLoader {
        val loader = ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.15)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("coil_cache").absolutePath.toPath())
                    .maxSizeBytes(50L * 1024 * 1024)
                    .build()
            }
            .build()

        SingletonImageLoader.setSafe { loader }
        return loader
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
