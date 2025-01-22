package com.el.mu.ui.local

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import com.el.mu.PlayerViewModel
import java.io.File

@Composable
fun AudioFilesScreen(
    playerViewModel: PlayerViewModel = hiltViewModel<PlayerViewModel>(LocalView.current.findViewTreeViewModelStoreOwner()!!),
) {
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    val ctx = LocalContext.current
    // Launcher for the file picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                val fileName = getFileNameFromUri(it, ctx)
                selectedUri = it
                selectedFileName = fileName
                Log.d("FilePicker", "Selected file: $fileName")
            }
        }
    )


    val folderName = "WeChat" // Specify the subfolder in Downloads
    val downloadsDir =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    val targetFolder = File(downloadsDir, folderName)
    val audioFiles = remember { getAudioFilesFromMediaStore(ctx, targetFolder.toUri()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(onClick = { filePickerLauncher.launch("audio/*") }) {
            BasicText("Pick an Audio File")
        }

        selectedFileName?.let {
            Text(text = "Selected file: $it", color = Color.White)
        } ?: Text("No file selected", color = Color.White)
        selectedUri?.let {
            Button(onClick = { playerViewModel.playLocal(selectedUri!!) })
            { Text(text = "Play selected audios", color = Color.White) }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent),
            contentPadding = PaddingValues(bottom = 150.dp),
        ) {
            items(audioFiles.size) { idx ->
                var item = audioFiles.elementAt(idx)
                // Display each audio file in the folder
                Button(onClick = { playerViewModel.playLocal(item) }) {
                    Text(text = item.name, color = Color.White)
                }
                Divider(
                    color = Color.Gray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

fun getFileNameFromUri(uri: Uri, context: Context): String? {
    val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
    return cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex != -1 && it.moveToFirst()) {
            it.getString(nameIndex)
        } else {
            null
        }
    }
}

fun getAudioFilesFromFolder(folderName: String): List<File> {
    val downloadsDir =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    Log.d("FilePicker", "Searching")

    val targetFolder = File(downloadsDir, folderName)
    return if (targetFolder.exists() && targetFolder.isDirectory) {
        Log.d("FilePicker", "Selected dir: ${targetFolder.absolutePath}")
        downloadsDir.listFiles()?.forEach {
            Log.d("FilePicker", "Found: $it")

        }
        targetFolder.listFiles()?.forEach {
            Log.d("FilePicker", "Found: $it")

        }
        targetFolder.listFiles()!!.toList()
    } else {
        if (targetFolder.exists()) {
            Log.d("FilePicker", "Selected dir: ${targetFolder.absolutePath}")
        } else {
            Log.d("FilePicker", "Selected dir missing: ${downloadsDir.absolutePath}")

        }
        emptyList()
    }
}


// Get audio files from MediaStore
fun getAudioFilesFromMediaStore(context: Context, folderUri: Uri): List<LocalAudio> {
    val contentResolver = context.contentResolver
    val projection = arrayOf(
        MediaStore.Audio.Media._ID,           // ID for the audio item
        MediaStore.Audio.Media.DISPLAY_NAME,  // Name of the file
        MediaStore.Audio.Media.ARTIST,        // Artist name
        MediaStore.Audio.Media.DATA,           // Path to the file (file URI)
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ALBUM_ID      // Album ID for artwork

    )
    val selection = "${MediaStore.Audio.Media.DATA} LIKE ?"
    val selectionArgs = arrayOf("%${folderUri.path}%") // Match files in the folder path
    Log.d("FilePicker", "Selected dir: ${folderUri.path}")

    val cursor: Cursor? = contentResolver.query(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        null
    )
    if (cursor == null) {
        Log.d("FilePicker", "null: ${folderUri.path}")

    } else {
        Log.d("FilePicker", "use cursor: ${folderUri.path}")

    }

    val files = mutableListOf<LocalAudio>()
    cursor?.use {
        val idColumn = it.getColumnIndex(MediaStore.Audio.Media._ID)
        val nameColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
        val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val pathUri = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        val albumIdColumn = it.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
        while (it.moveToNext()) {
            val id = if (idColumn != -1) it.getLong(idColumn) else -1L
            val fileName = if (nameColumn != -1) it.getString(nameColumn) else "Unknown"
            val artist = if (artistColumn != -1) it.getString(artistColumn) else "Unknown Artist"
            val filePath = if (pathUri != -1) it.getString(pathUri) else "Unknown Path"
            val albumId = if (albumIdColumn != -1) it.getLong(albumIdColumn) else -1L
            val artworkUri: Uri? = if (albumId != -1L) ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    albumId
                ) else null


            // Log the information for debugging
            Log.d("FilePicker", "File Name: $fileName, Artist: $artist, File Path: $filePath")

            val fileUri =
                Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toString())
            files.add(LocalAudio(fileName, fileUri, artist, artworkUri))
        }
    }
    return files
}
