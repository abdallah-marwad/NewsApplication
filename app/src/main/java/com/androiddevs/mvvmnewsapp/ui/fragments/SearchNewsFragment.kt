package com.androiddevs.mvvmnewsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androiddevs.mvvmnewsapp.R
import com.androiddevs.mvvmnewsapp.adapters.NewsAdapter
import com.androiddevs.mvvmnewsapp.databinding.FragmentBreakingNewsBinding
import com.androiddevs.mvvmnewsapp.databinding.FragmentSearchNewsBinding
import com.androiddevs.mvvmnewsapp.ui.NewsActivity
import com.androiddevs.mvvmnewsapp.ui.viewmodel.NewsViewModel
import com.androiddevs.mvvmnewsapp.utils.Constants
import com.androiddevs.mvvmnewsapp.utils.Resource
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*

class SearchNewsFragment : Fragment(R.layout.fragment_search_news) {
    private lateinit var viewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var binding: FragmentSearchNewsBinding
    private  var changeSearchList: MutableLiveData<Boolean> =  MutableLiveData<Boolean>(false)
    val TAG = "Searching News Fragment"
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchNewsBinding.bind(view)
        viewModel = (activity as NewsActivity).viewModel
        setUpRecyclerView()
        viewModelObserving()
        searchWithQuery()
        onItemClick()

    }


    private fun onItemClick() {
        newsAdapter.setOnClickListener {

            if (findNavController().currentDestination?.id == R.id.searchNewsFragment) {
                findNavController().navigate(
                    SearchNewsFragmentDirections.actionSearchNewsFragmentToArticleFragment(it)
                )
            }
        }

    }

    private fun searchWithQuery() {
        var job: Job? = null
        binding.etSearch.addTextChangedListener {
            job?.cancel()
            job = MainScope().launch {
                delay(800L)
                it?.let {
                    if (it.toString().isNotEmpty()) {
                        viewModel.getSearchingNews(it.toString())
                        changeSearchList.postValue(true)
                    }
                }
            }
        }
    }
    var isLoading = false
    var isScrolling = false
    var isLastPage = false

    private val scrollListener = object : RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL ){
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            // error could be in  and
            val isNotLoadingAndNotLastPage = !isLoading and !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >=0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage and isAtLastItem and isNotAtBeginning and isTotalMoreThanVisible and isScrolling
            if(shouldPaginate){
                viewModel.getSearchingNews(binding.etSearch.text.toString())
                isScrolling = false
            }

        }
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading =true

    }

    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading =false

    }

    private fun viewModelObserving() {
        viewModel.searchingNews.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Error -> {
                    hideProgressBar()
                    it.message?.let { message ->
                        Snackbar.make(binding.root, "Error : $message" , Snackbar.LENGTH_SHORT).show()                    }

                }
                is Resource.Loading ->
                    showProgressBar()
                is Resource.Success -> {
                    hideProgressBar()
                    it.data?.let { newsResponse ->
                        changeSearchList.observe(viewLifecycleOwner){

                            if(it){
                                newsAdapter.useList = true
                                newsAdapter.list = newsResponse.articles
                                val totalPages = newsResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
                                isLastPage = viewModel.searchingNewsPage == totalPages
                                if(isLastPage){
                                    binding.rvSearchNews.setPadding(0,0,0,0)
                                }
                            }else{
                            newsAdapter.differ.submitList(newsResponse.articles.toList())
                            val totalPages = newsResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
                            isLastPage = viewModel.searchingNewsPage == totalPages
                            if(isLastPage){
                                binding.rvSearchNews.setPadding(0,0,0,0)
                            }
                        }
                        }

                        }

                }
            }
        })
    }

    private fun setUpRecyclerView() {
        newsAdapter = NewsAdapter()
        binding.rvSearchNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@SearchNewsFragment.scrollListener)
        }
    }
}