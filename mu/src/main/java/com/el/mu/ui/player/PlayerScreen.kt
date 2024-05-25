package com.el.mu.ui.player

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.el.mu.PlayerViewModel
import com.el.mu.R

@Composable
fun PlayerScreen(
    navController: NavController,
    playerViewModel:PlayerViewModel = hiltViewModel<PlayerViewModel>(LocalView.current.findViewTreeViewModelStoreOwner()!!),
) {
    val playList by remember {
        mutableStateOf(playerViewModel.getPlaylist())
    }
    //val playList = playerViewModel.playList.collectAsState().value
    val curTitle = playerViewModel.currentTitle.collectAsState().value
    LaunchedEffect(key1 = playList) {

    }
    Column(
        modifier = Modifier
            //.padding(bottom = 200.dp)
            //.background(color = Color.Gray)
            .fillMaxWidth()
            .fillMaxHeight()
    )
    {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 25.dp)

        ) {
            items(playList.size) { idx ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            playerViewModel.playAt(idx)
                        },
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if(curTitle == playList.elementAt(idx)) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = "", tint = Color.White)
                    }
                    Text(
                        text = playList.elementAt(idx),
                        color = Color.White,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.clickable {
                            playerViewModel.removeAt(idx)
                        }
                    )


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

@Composable
fun PlayList() {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    ) {

    }
}

@Composable
fun PlayItem(
    imageUrl: Uri,
    title: String,
    onClick: () -> Unit,
    onMixClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        // Asynchronous Image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            //placeholder = painterResource(R.drawable.ic_baseline_shopping_cart_24),
            contentDescription = stringResource(R.string.app_name),
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .size(56.dp)
                .clip(MaterialTheme.shapes.medium)
            //colorFilter = ColorFilter.tint(Color.Blue)
        )

        // Spacer
        Spacer(modifier = Modifier.width(16.dp))

        // Title
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        // Icon at the rightmost
        Icon(
            painter = painterResource(id = R.drawable.playlist_play_24),
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = "Mix",
            modifier = Modifier.clickable { onMixClick() }
        )

    }
}

