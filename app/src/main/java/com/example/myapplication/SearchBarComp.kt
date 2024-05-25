package com.example.myapplication

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarComp() {

    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    var history = remember {
        mutableListOf("")
    }
    SearchBar(
        query = query,
        onQueryChange = {query = it},
        onSearch = {
            history.add(query)
            active = false
            println("Query: $it")
        },
        active = active,
        onActiveChange = { active = it },
        placeholder = { Text(text = "Search")},
        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription =  "Search" ) },
        trailingIcon = {
            if (active) {
                Icon(
                    modifier = Modifier.clickable {
                        if(query.isNotEmpty()){
                            query = ""
                        } else {
                            active = false
                        }
                    },
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Icon")
            }
        }
    ) {

    }
}