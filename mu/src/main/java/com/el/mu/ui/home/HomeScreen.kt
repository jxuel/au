package com.el.mu.ui.home

import android.annotation.SuppressLint
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.expandVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.el.mu.PlayerViewModel
import com.el.mu.R
import com.el.mu.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("SuspiciousIndentation", "UnusedBoxWithConstraintsScope")
@Composable
fun HomeScreen(
    navController: NavController,
    playerViewModel:PlayerViewModel = hiltViewModel<PlayerViewModel>(LocalView.current.findViewTreeViewModelStoreOwner()!!),
    viewModel: HomeViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val recentItems = viewModel.list.collectAsState().value
    val ctx = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val horizontalLazyGridItemWidthFactor = 0.475f

    val lazyGridState = rememberLazyGridState()
    val snappingLayout = remember(lazyGridState) {
        SnapLayoutInfoProvider(
            lazyGridState = lazyGridState,
            positionInLayout = { layoutSize, itemSize, _ ->
                ((layoutSize * horizontalLazyGridItemWidthFactor / 2f - itemSize / 2f).toInt())
            }
        )
    }

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            //.padding(bottom = 200.dp)
            //.background(color = Color.Gray)
            .fillMaxWidth()
            .fillMaxHeight()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
                keyboardController?.hide()
            }

    ) {
        Row(
            modifier = Modifier
                .wrapContentSize()
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                text = "Music App",
                fontSize = 30.sp,
                color = Color.White, modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.Search,
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = "Mix",
                modifier = Modifier
                    .size(30.dp)
                    .clickable {
                        navController.navigate(Screen.Search.route)
                    }
            )
        }

        Row(
            modifier = Modifier
                .wrapContentSize()
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                text = "Recent",
                fontSize = 25.sp,
                color = Color.White,
            )
        }

        Row(modifier = Modifier
            .height(150.dp)
        ) {
            LazyRow(
                modifier = Modifier
                    .background(Color.Transparent)
            ) {
                items(recentItems.size) { idx ->
                    val item = recentItems.elementAt(idx)
                    RecentTitleRow(
                        title = item.title,
                        imageUrl = item.image,
                        onClick = {
                            //println("ASTY ADD")
                            coroutineScope.launch {
                                playerViewModel.playByVideoId(
                                    item.videoId,
                                ).note?.let {
                                    Toast.makeText(ctx, it, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onMixClick = {
                            if (item.playListId.isNotEmpty()) {
                                coroutineScope.launch {
                                    playerViewModel.playByPlayList(
                                        item.videoId,
                                        item.playListId
                                    )
                                }
                            } else {
                                coroutineScope.launch {
                                    val playlistId = playerViewModel.getPlayListId(
                                        item.videoId,
                                    )
                                    playerViewModel.playByPlayList(
                                        item.videoId,
                                        playlistId
                                    )
                                }
                            }
                        })
                }
            }
        }
        BoxWithConstraints(modifier = Modifier.height(200.dp)) {
            LazyHorizontalGrid(
                rows = GridCells.Fixed(4),
                modifier = Modifier.fillMaxWidth(),
                state = lazyGridState,
                flingBehavior = rememberSnapFlingBehavior(snappingLayout),
                contentPadding = WindowInsets
                    .systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(),
            ) {
                items(recentItems.size) { idx ->
                    val item = recentItems.elementAt(idx)
                    HorizontalItem(
                        maxWidth,
                        horizontalLazyGridItemWidthFactor,
                        item,
                        onClick = {
                            //println("ASTY ADDS")
                            coroutineScope.launch {
                                playerViewModel.playByVideoId(
                                    item.videoId,
                                ).note?.let {
                                    Toast.makeText(ctx, it, Toast.LENGTH_SHORT).show()
                                }
                            }
                            if (item.playListId.isNotEmpty()) {
                                coroutineScope.launch {
                                    playerViewModel.playByPlayList(
                                        item.videoId,
                                        item.playListId
                                    )
                                }
                            } else {
                                coroutineScope.launch {
                                    val playlistId = playerViewModel.getPlayListId(
                                        item.videoId,
                                    )
                                    playerViewModel.playByPlayList(
                                        item.videoId,
                                        playlistId
                                    )
                                }
                            }
                        },)
                }
            }
        }

        Button(onClick = {
            ctx.applicationContext.deleteDatabase("test.db")
        }) {
            Text(text = "Clean DB")
        }

    }
}

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnrememberedMutableState")
@Composable
fun RecentTitleRow(
    imageUrl: Uri,
    title: String,
    onClick: () -> Unit,
    onMixClick: () -> Unit
) {
    val width = 100.dp
    val space = 5.dp
    val padding = 10.dp
    val playerControlOffset by derivedStateOf {
        val cubicBezierEasing = CubicBezierEasing(
            a = 0.25f,
            b = -2 / 5f,
            c = 0.5f,
            d = -10f
        )
        cubicBezierEasing.transform(1/2f) + 11
    }
    var longPress by remember {
        mutableStateOf(false)
    }
    val haptics = LocalHapticFeedback.current

   Box {
        Column(
            modifier = Modifier
                //.background(Color.Green)
                .width(width)
                .combinedClickable(
                    onClick = { onClick() },
                    onLongClick = {
                        //haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        longPress = !longPress
                    },
                    onLongClickLabel = stringResource(R.string.app_name)
                )
                .padding(bottom = padding, start = padding, end = padding)
                .then(
                    if (longPress)
                        Modifier.blur(10.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                    else Modifier
                ),
        ) {
            // Asynchronous Image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                //placeholder = painterResource(R.drawable.ic_baseline_shopping_cart_24),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    //.background(Color.Red)
                    .size(width)
                    .clip(MaterialTheme.shapes.medium)
                //colorFilter = ColorFilter.tint(Color.Blue)
            )
            Spacer(modifier = Modifier.height(space))

            Box(
                modifier = Modifier
                    .width(width)
                //.background(Color.Blue)
            ) {
                // Title
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
       AnimatedVisibility(
           visible = longPress,
           enter = scaleIn() + expandVertically(expandFrom = Alignment.CenterVertically),
           exit = scaleOut() + shrinkVertically(shrinkTowards = Alignment.CenterVertically)
       ) {
           Box(modifier = Modifier
               .width(width)
               .fillMaxHeight()
               .padding(bottom = padding, start = padding, end = padding)
               .clickable {
                   longPress = !longPress
               }
               //.background(Color.Transparent, shape = RoundedCornerShape(20.dp))
               //.blur(5.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
           ) {
               Box(modifier = Modifier
                   .fillMaxWidth()
                   .background(Color.Transparent)
                   .align(Alignment.Center)
                   .border(2.dp, Color.Gray)
                   .clip(MaterialTheme.shapes.medium)
                   ) {
                   Text(
                       text = "Mix Play",
                       color = Color.White,
                       fontWeight = FontWeight.Bold,
                       modifier = Modifier
                           .align(Alignment.Center)
                           .clickable {
                               onMixClick()
                               longPress = !longPress
                           }
                   )
               }

           }
       }
    }


}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalItem(
    maxWidth: Dp,
    horizontalLazyGridItemWidthFactor: Float,
    item: SearchRenderItem,
    onClick: () -> Unit
) {
    val space = 5.dp

    Box(
        modifier = Modifier
            .width(maxWidth * horizontalLazyGridItemWidthFactor)
            .padding(2.dp)
            .background(color = Color.Gray)
            //.border(1.dp, Color.Red)
        ,
        contentAlignment = Alignment.CenterStart
    ) {
        Row(modifier = Modifier
            .combinedClickable(
            onClick = { onClick() },
            onLongClickLabel = stringResource(R.string.app_name)
        ),
            verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.image)
                    .crossfade(true)
                    .build()
                ,
                //placeholder = painterResource(R.drawable.ic_baseline_shopping_cart_24),
                contentDescription = stringResource(R.string.app_name),

                modifier = Modifier
                    //.background(Color.Red)
                    .fillMaxHeight()
                    .aspectRatio(1F, true)
                    .clip(MaterialTheme.shapes.medium)
                //colorFilter = ColorFilter.tint(Color.Blue)
            )
            Spacer(modifier = Modifier.width(space))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = item.title,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

    }
}