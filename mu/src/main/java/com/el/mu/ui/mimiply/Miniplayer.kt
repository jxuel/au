package com.el.mu.ui.mimiply


import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.media3.common.Player
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.el.mu.PlayerViewModel
import com.el.mu.R

@OptIn(ExperimentalFoundationApi::class)
@Preview
@SuppressLint("SuspiciousIndentation")
@Composable
fun Miniplayer(
    playerViewModel: PlayerViewModel = hiltViewModel<PlayerViewModel>(LocalView.current.findViewTreeViewModelStoreOwner()!!),
) {
    val mediaImage = playerViewModel.currentImage.collectAsState().value
    val mediaTitle = playerViewModel.currentTitle.collectAsState().value
    val isPlaying = playerViewModel.isPlaying.collectAsState().value
    val repeatMode = playerViewModel.repeatMode.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.Center)
    ) {
        Box(
            modifier = Modifier
                .height(150.dp)
                .fillMaxWidth()
                .padding(10.dp)
                .clip(RectangleShape)
            //.background(Color.Gray)
        ) {
            ConstraintLayout {
                val (bg, image, controller) = createRefs()
                var size by remember { mutableStateOf(IntSize.Zero) }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(size = 15.dp))
                        .background(Color(0xffe5eae7))
                        .fillMaxWidth()
                        .constrainAs(bg) {
                            top.linkTo(parent.top, margin = 25.dp)
                            start.linkTo(parent.start, margin = 5.dp)
                            end.linkTo(parent.end, margin = 5.dp)
                            //width = Dimension.matchParent
                            height = Dimension.matchParent
                        }
                        .onSizeChanged {
                            size = it
                        }
                ) {
                }
                val edgeWidth = 32.dp
                fun ContentDrawScope.drawFadedEdge(leftEdge: Boolean) {
                    val edgeWidthPx = edgeWidth.toPx()
                    drawRect(
                        topLeft = Offset(if (leftEdge) 0f else size.width - edgeWidthPx, 0f),
                        size = Size(edgeWidthPx, size.height.toFloat()),
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                            startX = if (leftEdge) 0f else size.width.toFloat(),
                            endX = if (leftEdge) edgeWidthPx else size.width - edgeWidthPx
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
                Box(
                    modifier = Modifier
                        .background(Color.Transparent)
                        //.background(Color(0xffe5eae7))
                        .constrainAs(controller) {
                            top.linkTo(bg.top, margin = 25.dp)
                            bottom.linkTo(bg.bottom)
                            //start.linkTo(image.start)
                            end.linkTo(bg.end, margin = 25.dp)
                            translationX = 11.dp
                            width = Dimension.wrapContent
                            height = Dimension.matchParent
                        },
                    contentAlignment = Alignment.CenterEnd
                    //.shadow(10.dp, CircleShape, spotColor = Color.Black)
                ) {
                    Column(
                        //modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = mediaTitle,
                            maxLines = 1,
                            modifier = Modifier
                                .widthIn(max = edgeWidth * 4)
                                // Rendering to an offscreen buffer is required to get the faded edges' alpha to be
                                // applied only to the text, and not whatever is drawn below this composable (e.g. the
                                // window).
                                .graphicsLayer {
                                    compositingStrategy = CompositingStrategy.Offscreen
                                }
                                .drawWithContent {
                                    drawContent()
                                    drawFadedEdge(leftEdge = true)
                                    drawFadedEdge(leftEdge = false)
                                }
                                .basicMarquee(
                                    // Animate forever.
                                    iterations = Int.MAX_VALUE,
                                    spacing = MarqueeSpacing(0.dp)
                                )
                                .padding(start = edgeWidth)
                        )
                        PlayControlsRow(repeatMode, isPlaying, {
                            playerViewModel.previous()
                        }, {
                            playerViewModel.togglePlayer()
                        }, {
                            playerViewModel.next()
                        }, {
                            playerViewModel.toggleRepeatMode()
                        })
                    }

                }

                Box(
                    modifier = Modifier

                        //.padding(end = 4.dp)
                        //.background(Color.Magenta)
                        //.padding(bottom = 5.dp, start = 8.dp, end = 16.dp, top = 4.dp)
                        .constrainAs(image) {
                            //top.linkTo(title.top, margin = 30.dp)
                            start.linkTo(parent.start, margin = 30.dp)
                            width = Dimension.value(100.dp)
                            height = Dimension.value(100.dp)
                        }
                        .background(Color.Transparent)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(mediaImage)
                            .crossfade(true)
                            .build(),
                        //placeholder = painterResource(R.drawable.ic_baseline_shopping_cart_24),
                        contentDescription = stringResource(R.string.app_name),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .shadow(
                                elevation = 10.dp,
                                shape = RoundedCornerShape(15.dp)
                            )
                            .background(Color(0xffe5eae7))
                            .size(100.dp)

                        //colorFilter = ColorFilter.tint(Color.Blue)
                    )
                }


            }

        }

    }
}

@Composable
fun PlayControlsRow(
    repeat: Int,
    isPlaying: Boolean,
    onPreviousClick: () -> Unit,
    onPlayClick: () -> Unit,
    onNextClick: () -> Unit,
    onRepeatClick: () -> Unit
) {
    Row(
        modifier = Modifier.padding(16.dp)
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        val repeatIcon =
            when (repeat) {
                Player.REPEAT_MODE_OFF -> painterResource(id = R.drawable.arrow_right_alt)
                Player.REPEAT_MODE_ONE -> painterResource(id = R.drawable.repeat_one)
                Player.REPEAT_MODE_ALL -> painterResource(id = R.drawable.repeat_all)
                else -> painterResource(id = R.drawable.arrow_right_alt)
            }
        Icon(painter = repeatIcon,
            contentDescription = null,
            modifier = Modifier
                .clickable { onRepeatClick() }
                .size(30.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.skip_previous),
            contentDescription = null,
            modifier = Modifier
                .clickable { onPreviousClick() }
                .size(30.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        val playPauseIcon =
            if (isPlaying) painterResource(id = R.drawable.pause_circle) else painterResource(id = R.drawable.play_circle)
        Icon(painter = playPauseIcon,
            contentDescription = null,
            modifier = Modifier
                .clickable {
                    onPlayClick()
                }
                .size(30.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Icon(
            painter = painterResource(id = R.drawable.skip_next),
            contentDescription = null,
            modifier = Modifier
                .clickable { onNextClick() }
                .size(30.dp)

        )
    }
}