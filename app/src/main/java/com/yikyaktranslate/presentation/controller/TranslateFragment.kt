package com.yikyaktranslate.presentation.controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yikyaktranslate.presentation.theme.YikYakTranslateTheme
import com.yikyaktranslate.presentation.view.TranslateView
import com.yikyaktranslate.presentation.viewmodel.TranslateViewModel

class TranslateFragment : Fragment() {

    private val translateViewModel: TranslateViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                YikYakTranslateTheme {
                    // Observe fields from view model
                    val inputText by translateViewModel.textToTranslate.observeAsState(
                        TextFieldValue("")
                    )
                    val languages by translateViewModel.languagesToDisplay.observeAsState(initial = listOf())
                    val targetLanguageIndex by translateViewModel.targetLanguageIndex
                    val translatedText by translateViewModel.translation.observeAsState(initial = "")
                    val detectedLanguage by translateViewModel.detectedLanguage.observeAsState(initial = "")

                    Scaffold(topBar = {AppBar()}) {

                        Surface(modifier = Modifier.fillMaxSize()) {

                            // Create Compose view
                            TranslateView(
                                inputText = inputText,
                                onInputChange = translateViewModel::onInputTextChange,
                                languages = languages,
                                targetLanguageIndex = targetLanguageIndex,
                                onTargetLanguageSelected = translateViewModel::onTargetLanguageChange,
                                onTranslateClick = {
                                    translateViewModel.translateText()
                                },
                                translatedText = translatedText,
                                detectedLanguage = detectedLanguage
                            )
                            
                        }



                    }
                }
            }
        }
    }

}

@Composable
fun AppBar() {
    TopAppBar(
        navigationIcon = {
            Icon(imageVector = Icons.Default.Home,
                contentDescription = "Home Icon",
                Modifier.padding(horizontal = 12.dp))
                         },
        title = { Text("Yik Yak Translate") },
        contentColor = Color.Black
    )
}