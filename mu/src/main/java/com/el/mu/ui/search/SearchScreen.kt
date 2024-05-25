package com.el.mu.ui.search

import android.annotation.SuppressLint
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.el.mu.PlayerViewModel
import com.el.mu.R
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
@Preview
@Composable
fun SearchScreen(
    playerViewModel:PlayerViewModel = hiltViewModel<PlayerViewModel>(LocalView.current.findViewTreeViewModelStoreOwner()!!),
    searchViewModel: SearchViewModel = hiltViewModel()
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val searchItems = searchViewModel.list.collectAsState().value
    val historyItems =searchViewModel.historyList.collectAsState().value
    val sParam = searchViewModel.sParams.collectAsState().value
    val sugg = searchViewModel.sug.collectAsState().value

    val coroutineScope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val queryText = remember {
        mutableStateOf("")
    }
    val sug = remember {
        mutableStateOf(coroutineScope.launch {  })
    }
    var showMore by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var showSugg by remember { mutableStateOf(false) }
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
                showSugg = false
                showResult = true
            },
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        /*SearchBar(
            query = query,
            onQueryChange = {
                query = it
            }, onSearch = {
                history.add(it)
                active = false
                coroutineScope.launch {
                    searchViewModel.searchAndPlay(it)
                }
            }, active = active,
            onActiveChange = {
                active = it
            },

            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            placeholder = { Text(text = "Search") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon")
            },trailingIcon = {
                if(active) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        tint = Color.White,
                        contentDescription = "Send query",
                        modifier = Modifier.clickable {
                            if(query.isNotEmpty()) {
                                query = ""
                            } else {
                                active = false
                            }
                        }
                    )
                }
            },
        ) {
            history.forEach {item->
                Row(modifier = Modifier.padding(14.dp)){
                    Icon(imageVector = Icons.Default.Star, contentDescription = "history")
                    Text(text = item, color = Color.White)
                }

            }
        }*/

        EditableTextFieldWithButton(queryText, {
            coroutineScope.launch {
                searchViewModel.cleanSug()
                searchViewModel.search(it)
                showResult = true
            }
            focusManager.clearFocus()
            keyboardController?.hide()
        }, {input->
            if (input.isBlank()) {
                if(sugg.isNotEmpty()) {
                    showSugg = false
                    searchViewModel.cleanSug()
                }
                return@EditableTextFieldWithButton
            }
            if(!sug.value.isCompleted) {
                sug.value.cancel()
            }

            sug.value = coroutineScope.launch {
                searchViewModel.searchSuggestions(input)
            }
        }, {
            showSugg = true
            showResult = false
        })

        AnimatedVisibility(visible = queryText.value.isEmpty()) {
            HistoryList(
                historyItems,
                { query ->
                    if (queryText.value != query) {
                        queryText.value = query
                        coroutineScope.launch {
                            searchViewModel.search(query)
                            showResult = true
                        }
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                }
            ) { idx, _ ->
                searchViewModel.deleteRecordByIdx(idx)
            }
        }

        AnimatedVisibility(visible = queryText.value.isNotEmpty()) {
            SuggestionList(
                sugg.filter{ sugStr-> sugStr != queryText.value }, // same query text won't start  http. 1st request won't show anything
                { query ->
                    if (queryText.value != query) {
                        queryText.value = query
                        coroutineScope.launch {
                            searchViewModel.cleanSug()
                            searchViewModel.search(query)
                            showResult = true
                        }
                    } else {
                        searchViewModel.cleanSug()
                        showResult = true
                    }
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            ) { idx, _ ->
                //earchViewModel.deleteRecordByIdx(idx)
            }
        }

        AnimatedVisibility(visible = showResult && queryText.value.isNotBlank() && sParam.isNotBlank(),
            exit = scaleOut() + shrinkVertically(shrinkTowards = Alignment.CenterVertically)) {
            Button(onClick = {
                coroutineScope.launch {
                    searchViewModel.search(queryText.value, sParam)
                }
            }) {
                Text(text = "More Songs", color = Color.White)
            }
        }

        AnimatedVisibility(visible = showResult) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent),
                contentPadding = PaddingValues(bottom = 150.dp),
            ) {
                items(searchItems.size) { idx ->
                    val item = searchItems.elementAt(idx)
                    ImageTitleRow(
                        title = item.title,
                        subTitle = item.subTitle,
                        imageUrl = item.image,
                        onClick = {
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
                    // Separator
                    Divider(
                        color = Color.Gray,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("InvalidColorHexValue")
@Composable
fun EditableTextFieldWithButton(
    input: MutableState<String>,
    onButtonClick: (String) -> Unit,
    onLunched: (String) -> Unit,
    onFocused: ()-> Unit = {}) {
    var text by remember { input }

    // This is used to get a reference to the software keyboard controller
    val keyboardController = LocalSoftwareKeyboardController.current

    // This is used to get a reference to the text input service
    val textInputService = LocalTextInputService.current
    val enabled by remember {
        mutableStateOf(true)
    }

    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        var focused by remember {
            mutableStateOf(false)
        }
        // Editable Text Field
        LaunchedEffect(key1 = text) {
            if(focused) {
                delay(200)
                onLunched(text)
            }
        }
        BasicTextField(
            value = text,
            onValueChange = {
                text = it
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    // Trigger the button click action when Done button on the keyboard is pressed
                    onButtonClick(text)
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent)
                .onFocusChanged { fs ->
                    focused = fs.isFocused
                    if (fs.isFocused) onFocused()
                }
                .padding(15.dp),
            //.background(Color(0xD74BB1B1))

            textStyle = TextStyle(color = Color.White),
            singleLine = true,
            enabled = enabled,
            interactionSource = interactionSource,

            decorationBox = { innerTextField ->
                TextFieldDefaults.DecorationBox(
                    value = text,
                    innerTextField = innerTextField,
                    singleLine = true,
                    enabled = enabled,
                    //label = { Text("Search") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon"
                        )
                    },
                    trailingIcon = {
                        if (text.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Send query",
                                modifier = Modifier.clickable {
                                    if (text.isNotEmpty()) {
                                        text = " "
                                    }
                                }
                            )
                        }
                    },
                    placeholder = { Text("Search artists, songs") },
                    visualTransformation = VisualTransformation.None,
                    interactionSource = interactionSource,
                    colors = TextFieldDefaults.colors(
                        unfocusedPlaceholderColor = Color.Gray,
                        unfocusedLeadingIconColor = Color.Gray,
                        focusedContainerColor = Color.Transparent,

                        unfocusedContainerColor = Color.Transparent,
                        //unfocusedBorderColor = Color.Transparent,
                    ),

                    )
            }
        )
    }
}

@Composable
fun HistoryList(
    historyItems: List<String>,
    onClick: (String) -> Unit,
    onRemove: (Int, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    ) {
        items(historyItems.size) { idx ->
            val item = historyItems.elementAt(idx)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onClick(item)
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.history_24px),
                        contentDescription = "S",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = item,
                        color = Color.White
                    )
                }

                Icon(
                    modifier = Modifier.clickable {
                        onRemove(idx, item)
                    },
                    imageVector = Icons.Default.Close,
                    contentDescription = "S",
                    tint = Color.White
                )
            }
        }

    }
}

@Composable
fun SuggestionList(
    historyItems: List<String>,
    onClick: (String) -> Unit,
    onRemove: (Int, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    ) {
        items(historyItems.size) { idx ->
            val item = historyItems.elementAt(idx)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onClick(item)
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "S",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = item,
                        color = Color.White
                    )
                }

                Icon(
                    modifier = Modifier.clickable {
                        onRemove(idx, item)
                    },
                    imageVector = Icons.Default.Close,
                    contentDescription = "S",
                    tint = Color.White
                )
            }
        }

    }
}

@Composable
fun ImageTitleRow(
    imageUrl: Uri,
    title: String,
    subTitle: String,
    onClick: () -> Unit,
    onMixClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,

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

        // Title
        Column(
            modifier = Modifier
                //.background(color = Color.Green)
                .padding(start = 16.dp, end = 16.dp)
                .weight(1F)
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subTitle,
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }


        // Icon at the rightmost
        Icon(
            painter = painterResource(id = R.drawable.playlist_play_24),
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = "Mix",

            modifier = Modifier
                .clickable { onMixClick() }
                .size(30.dp)
        )

    }
}
