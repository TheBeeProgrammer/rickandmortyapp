package com.renato.rickandmorty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import com.renato.rickandmorty.ui.characters.CharactersScreen
import com.renato.rickandmorty.ui.theme.RickandmortyTheme
import com.renato.rickandmorty.viewmodel.CharactersViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RickandmortyTheme {
                val viewModel: CharactersViewModel = hiltViewModel()
                CharactersScreen(viewModel = viewModel)
            }
        }
    }
}