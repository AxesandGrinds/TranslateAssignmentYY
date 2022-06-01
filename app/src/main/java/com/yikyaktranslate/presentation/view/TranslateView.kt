package com.yikyaktranslate.presentation.view

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.yikyaktranslate.R

/**
 * Composable views that create primary translation screen
 */

@Composable
fun TranslateView(
    inputText: TextFieldValue,
    onInputChange: (TextFieldValue) -> Unit,
    languages: List<String>?,
    targetLanguageIndex: Int,
    onTargetLanguageSelected: (Int) -> Unit,
    onTranslateClick: () -> Unit, // TODO: implement
    translatedText: String, // TODO: implement
    detectedLanguage: String = "",
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // User inputs text to translate here
            TextField(
                modifier = Modifier.size(150.dp, 150.dp),
                value = inputText,
                onValueChange = onInputChange,
                placeholder = {
                    Text("Input text to translate")
                }
            )

            // Translated text response should show up here
            Text(
                modifier = Modifier
                    .size(150.dp, 150.dp)
                    .border(width = 2.dp, color = MaterialTheme.colors.primary)
                    .padding(5.dp),
                text = translatedText
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            // "Translate to: " prompt label
            Text(stringResource(R.string.language_selection_prompt))

            Spacer(Modifier.size(5.dp))

            if (languages.isNullOrEmpty()) {
                // Placeholder text if we don't have languages for the dropdown
                Text(stringResource(R.string.language_selection_placeholder))
            } else {
                // Creates the dropdown list of languages to select from
                LanguageDropDown(
                    languages = languages,
                    targetLanguageIndex = targetLanguageIndex,
                    onTargetLanguageSelected = onTargetLanguageSelected
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            // "Translate to: " prompt label
            Text(stringResource(R.string.language_detected_prompt))

            Spacer(Modifier.size(5.dp))

            if (detectedLanguage.isEmpty()) {
                // Placeholder text if we don't language is not detected
                Text(stringResource(R.string.language_detected_placeholder))
            } else {
                // Shows detected language
                Text(detectedLanguage)
            }
        }

        // Button to execute the translation
        Button(onClick = onTranslateClick) {
            Text(stringResource(R.string.translate_button))
        }
    }
}

@Composable
fun LanguageDropDown(
    languages: List<String>,
    targetLanguageIndex: Int,
    onTargetLanguageSelected: (Int) -> Unit
) {
    // Keeps track of whether or not the list of languages is expanded
    var expandLanguageList by remember { mutableStateOf(false) }

    Box {
        // Shows currently selected language and opens dropdown menu
        Text(
            modifier = Modifier.clickable { expandLanguageList = true },
            text = languages[targetLanguageIndex]
        )

        // Dropdown menu to select a language to translate to
        DropdownMenu(
            expanded = expandLanguageList,
            onDismissRequest = { expandLanguageList = false }
        ) {
            // Creates a DropdownMenuItem for each language
            languages.forEachIndexed { index, language ->
                DropdownMenuItem(
                    onClick = {
                        onTargetLanguageSelected(index)
                        expandLanguageList = false
                    }
                ) {
                    Text(text = language)
                }
            }
        }
    }
}
