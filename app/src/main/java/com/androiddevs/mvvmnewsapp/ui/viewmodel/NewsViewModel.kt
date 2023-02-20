package com.androiddevs.mvvmnewsapp.ui.viewmodel

import android.app.Application
import android.app.DownloadManager.Query
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.*
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddevs.mvvmnewsapp.NewsApplication
import com.androiddevs.mvvmnewsapp.models.Article
import com.androiddevs.mvvmnewsapp.models.NewsResponse
import com.androiddevs.mvvmnewsapp.repository.NewsRepository
import com.androiddevs.mvvmnewsapp.utils.Resource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import retrofit2.Response
import java.io.IOException

class NewsViewModel(
    private val newsRepository: NewsRepository,
    application: Application
) : AndroidViewModel(application) {


    init {
        getBreakingNews()
    }


    // Breaking News Handling
    val breakingNews = MutableLiveData<Resource<NewsResponse>>()
    var breakingNewsPage = 1
    var breakingNewsResponse : NewsResponse? = null


     fun getBreakingNews() =
        viewModelScope.launch(Dispatchers.IO)
        {
            safeBreakingNewsCall()
        }

    /**
    * @param response the async Api Call by ViewModelScope
    * @return Resource that carry the state with the data
    * */
    private fun handleBreakingNewResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let {
                breakingNewsPage++
                if(breakingNewsResponse == null){
                    breakingNewsResponse = it
                }
                else{
                    val oldArticle = breakingNewsResponse?.articles
                    val newArticle = it.articles
                    oldArticle?.addAll(newArticle)

                }
                return Resource.Success(breakingNewsResponse ?: it)
            }
        }
        return Resource.Error(response.message())
    }

    private suspend fun safeBreakingNewsCall() {
        breakingNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = newsRepository.getBreakingNews(breakingNewsPage)
                breakingNews.postValue(handleBreakingNewResponse(response))
            } else {
                breakingNews.postValue(Resource.Error("No Internet Connection"))

            }
        } catch (t: Throwable) {
            when(t){
                is IOException -> breakingNews.postValue(Resource.Error("Network Failure"))
                else -> breakingNews.postValue(Resource.Error("Conversion Error"))
            }
        }


    }


    // Search News Handling
    val searchingNews = MutableLiveData<Resource<NewsResponse>>()
    var searchingNewsPage = 1
    var searchNewsResponse : NewsResponse? = null

    fun getSearchingNews(searchQuery: String) = viewModelScope.launch(Dispatchers.IO) {
        safeSearchingNewsCall(searchQuery)

    }
    private suspend fun safeSearchingNewsCall(searchQuery: String) {
        searchingNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = newsRepository.getSearchingNews(searchQuery, searchingNewsPage)
                searchingNews.postValue(handleSearchingNewsResponse(response))
            } else {
                searchingNews.postValue(Resource.Error("No Internet Connection"))

            }
        } catch (t: Throwable) {
            when(t){
                is IOException -> searchingNews.postValue(Resource.Error("Network Failure"))
                else -> searchingNews.postValue(Resource.Error("Conversion Error"))
            }
        }


    }

    private fun handleSearchingNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let {
                searchingNewsPage++
                if (searchNewsResponse == null) {
                    searchNewsResponse = it
                } else {
                    val oldArticle = searchNewsResponse?.articles
                    val newArticle = it.articles
                    oldArticle?.addAll(newArticle)

                }
                return Resource.Success(searchNewsResponse ?: it)
            }
        }
        return Resource.Error(response.message())
    }

    //Room Operations
    val addStateFlow = MutableStateFlow<Long>(0)
    fun saveArticle(article: Article) =
        viewModelScope.launch(Dispatchers.IO) {

            addStateFlow.emit(newsRepository.upsert(article))

        }

    fun deleteArticle(article: Article) = viewModelScope.launch(Dispatchers.IO) {
        newsRepository.delete(article)
    }

    fun getSavedNews() = newsRepository.getSavedNews()


    // Check Internet

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<NewsApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capability = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capability.hasTransport(TRANSPORT_WIFI) -> true
            capability.hasTransport(TRANSPORT_CELLULAR) -> true
            capability.hasTransport(TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }


}