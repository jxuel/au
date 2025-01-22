package com.el.mu

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.window.layout.DisplayFeature
import com.el.mu.ui.home.HomeScreen
import com.el.mu.ui.mimiply.Miniplayer
import com.el.mu.ui.multitab.MultiTab
import com.el.mu.ui.player.PlayerScreen

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MusicApp(
    windowSizeClass: WindowSizeClass,
    displayFeatures: List<DisplayFeature>,
    appState: MusicAppState = rememberMusicAppState()
) {// Get local density from composable

    val localDensity = LocalDensity.current
    val bgColor = Color(0xE6070707)

    // Create element height in pixel state
    var columnHeightPx by remember {
        mutableStateOf(0f)
    }

    var visableMiniply by remember {
        mutableStateOf(true)
    }

    // Create element height in dp state
    var columnHeightDp by remember {
        mutableStateOf(0.dp)
    }

    var bEntry: NavBackStackEntry? = null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(bgColor),
    ) {
        //val (content, playbox) = createRefs()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .navigationBarsPadding()
                //.padding(bottom = columnHeightDp / 2)
                //.fillMaxHeight()

                //.height(200.dp)
                //.background(Color(0xE6E5EAE7))
                //.background(Color(0xff0c0d11))
                .background(Color.Transparent)
            /*.constrainAs(content) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(playbox.top)

                //baseline.linkTo(playbox.baseline, margin = 5.dp)
                width = Dimension.matchParent
            }*/,
            contentAlignment = Alignment.Center

        ) {
            NavHost(
                navController = appState.navController,
                startDestination = Screen.Home.route
            ) {
                composable(Screen.Home.route) {backStackEntry->
                    bEntry = backStackEntry
                    visableMiniply = true
                    HomeScreen(appState.navController)
                }
                composable(Screen.Search.route) {backStackEntry->
                    bEntry = backStackEntry
                    visableMiniply = true
                    MultiTab()
                }
                composable(Screen.Player.route) {
                    visableMiniply = false
                    PlayerScreen(appState.navController)
                }
            }
            if(visableMiniply) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .align(Alignment.BottomCenter)
                        //.background(Color(0xE6E5EAE7))
                        //.background(Color(0xff0c0d11))
                        //.background(Color.Red)
                        /*.constrainAs(playbox) {
                        //top.linkTo(content.bottom )
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.matchParent
                    }*/
                        .onGloballyPositioned { coordinates ->
                            // Set column height using the LayoutCoordinates
                            columnHeightPx = coordinates.size.height.toFloat()
                            columnHeightDp = with(localDensity) { coordinates.size.height.toDp() }
                        }
                        .clickable {
                            bEntry?.let {
                                appState.navigateToPlayer("DD", it)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Miniplayer()
                }
            }
        }
        /*val ctx = LocalContext.current
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(),
            onClick = {
                val mediaController = playerViewModel.mctl
                if (mediaController == null) {
                    Toast.makeText(ctx, "Player is not ready", Toast.LENGTH_SHORT).show()
                } else {
                    mediaController.run {
                        when (this.isPlaying) {
                            true -> this.pause()
                            else -> {
                                val mediaItem = MediaItem.Builder()
                                    .setMediaId("z9VMaLxg9Ok")
                                    .setUri("https://rr1---sn-p5qlsndr.googlevideo.com/videoplayback?expire=1703320825&ei=mUiGZYC5OI2EzLUP_pa3kAM&ip=73.212.187.42&id=o-ABLwYQlanYLJa-yEKsXYs4YnIXl4_deeQwDegctFWzyZ&itag=139&source=youtube&requiressl=yes&xpc=EgVo2aDSNQ%3D%3D&mh=L8&mm=31%2C29&mn=sn-p5qlsndr%2Csn-p5qddn7d&ms=au%2Crdu&mv=m&mvi=1&pl=15&gcr=us&initcwndbps=1395000&vprv=1&mime=audio%2Fmp4&gir=yes&clen=963202&dur=157.559&lmt=1603797423730063&mt=1703298793&fvip=4&keepalive=yes&fexp=24007246&c=ANDROID_MUSIC&txp=5531432&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cxpc%2Cgcr%2Cvprv%2Cmime%2Cgir%2Cclen%2Cdur%2Clmt&sig=AJfQdSswRgIhAJX_qFtlzxvFPrmqcQU4JhGbAJtnsu4z-vMWFu1QZjxzAiEArz1D-VLsSL1NjpbPvZGJ35c1XEbRtsbYMxoN0VIiHU4%3D&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AAO5W4owRQIgIq20ehKU1W22sdb5GsWMmkHzYqe7_L_jk18uL2fygsYCIQCsvgDpkmcalg0S47GA8O5beJVE28GG_c2wTiQ55o0tfg%3D%3D")
                                    .setMediaMetadata(
                                        MediaMetadata.Builder()
                                            .setArtist("Post Malone")
                                            .setTitle("SunFlower")
                                            .setArtworkUri(Uri.parse("https://lh3.googleusercontent.com/YoQ-A-GOpgeE8tgdF3Rcf5z9V8NIIKjLH6_7X3QphIQUwVHioLu7Ik2wQzU0oCkyNm1TeLDLDYvomJ8=w120-h120-l90-rj"))
                                            .build()
                                    ).build()
                                this.addMediaItem(mediaItem)
                                this.prepare()
                                this.play()
                            }
                        }

                    }
                }

            }) {
            Text(text = "Start")
        }*/

    }


}