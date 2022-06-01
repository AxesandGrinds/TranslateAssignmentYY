# yikyak-translate - Android Take Home Project

## Before you start

- Make sure you have the latest version of Android Studio and have a way to connect to a GitHub repository. You should be ready to run code from a repo on a physical device or emulator.

## Overview

You have been asked to create a translation app! You’ve been given a version that is already partially implemented, but you must finish it off. Currently, the app loads a list of possible translation languages from an API, allows the user to select one, and takes in user text input. There is also a button that says “Translate” and a text box that should display the translated result, but both currently do nothing. The app uses the LibreTranslate API. The documentation for the API is here: [https://libretranslate.com/docs/](https://libretranslate.com/docs/)

![yikyak translate start.png](https://github.com/Yik-Yak/yikyak-translate/blob/main/yikyak%20translate%20start.png)

Your tasks are as follows:

- Implement the translation functionality. The app currently uses Retrofit2 to make API calls, and the UI components are already in place in `TranslateView.kt`. This should involve:
    - Creating a POST request to get the translation [https://libretranslate.com/docs/#/translate/post_translate](https://libretranslate.com/docs/#/translate/post_translate)
    - Connecting the service to the UI to show the result
- Update the API calls to be asynchronous, using Kotlin coroutines and flows
- Leave the code better than you found it - if there are places within the code that you feel could be improved, whether it’s a performance issue, architecture issue, or something else entirely, feel free to change it.
