package com.androiddevs.mvvmnewsapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.androiddevs.mvvmnewsapp.NewsApplication
import com.androiddevs.mvvmnewsapp.repository.NewsRepository

class NewsViewModelProviderFactory(
    val newsRepository : NewsRepository,
    val app : NewsApplication
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NewsViewModel(newsRepository ,app) as T

    }
}