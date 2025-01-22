package com.el.mu.ui.multitab

import com.el.mu.ui.local.AudioFilesScreen
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.el.mu.ui.search.SearchScreen

@Composable
fun MultiTab() {
    var tabIndex by remember { mutableStateOf(0) }

    val tabs = listOf("Online", "Local")

    Column(modifier = Modifier.fillMaxWidth()) {
        TabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(text = { Text(title) },
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                    icon = {
                        when (index) {
                            0 -> Icon(imageVector = Icons.Default.Home, contentDescription = null)
                            1 -> Icon(imageVector = Icons.Default.Place, contentDescription = null)
                        }
                    }
                )
            }
        }
        when (tabIndex) {
            0 -> SearchScreen()
            1 -> AudioFilesScreen()
        }
    }
}