package io.github.johannrosenberg.sample.ui.screens.test

import androidx.lifecycle.ViewModel

class TestViewModel: ViewModel() {
    var screenId: Int = (0..1_000_000).random()
}