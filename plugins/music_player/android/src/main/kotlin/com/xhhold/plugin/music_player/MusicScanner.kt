package com.xhhold.plugin.music_player

import android.content.ContentUris
import android.provider.MediaStore
import android.util.Log
import com.xhhold.plugin.music_player.ext.openFileDescriptorRead
import com.xhhold.plugin.music_player.manager.FileManager
import kotlinx.coroutines.runBlocking

object MusicScanner {

    private const val TAG = "MusicScanner"

    fun scan(service: MusicService) {
        Log.i(TAG, "scan start")
        val contentResolver = service.contentResolver
        val sources = arrayListOf<String>()

        contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.Media._ID),
            null,
            null,
            null
        )?.apply {
            val columnIndex = getColumnIndex(MediaStore.Audio.Media._ID)
            while (moveToNext()) {
                val uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    getLong(columnIndex)
                )
                sources.add(uri.toString())
            }
            close()
        }

        sources.forEachIndexed { index: Int, source: String ->
            runBlocking {
                Log.i(TAG, "${index + 1}/${sources.size}: $source")
                val fileDescriptor = source.openFileDescriptorRead(contentResolver)
                    ?: return@runBlocking
                val music = MusicFactory.decodeFromPath(
                    source,
                    fileDescriptor.fileDescriptor
                ) { md5: String, bytes: ByteArray? ->
                    if (bytes == null) {
                        return@decodeFromPath null
                    }
                    FileManager.saveAlbumImageCache(md5, bytes)?.toString()
                }
                fileDescriptor.close()
                if (music != null) {
                    service.musicRepository.addMusic(music)
                }
            }
        }
    }
}