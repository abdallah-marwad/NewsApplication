package com.androiddevs.mvvmnewsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import androidx.fragment.app.Fragment

import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androiddevs.mvvmnewsapp.R
import com.androiddevs.mvvmnewsapp.adapters.NewsAdapter
import com.androiddevs.mvvmnewsapp.databinding.FragmentBreakingNewsBinding
import com.androiddevs.mvvmnewsapp.ui.NewsActivity
import com.androiddevs.mvvmnewsapp.ui.viewmodel.NewsViewModel
import com.androiddevs.mvvmnewsapp.utils.Constants.Companion.QUERY_PAGE_SIZE
import com.androiddevs.mvvmnewsapp.utils.Resource
import com.google.android.material.snackbar.Snackbar

class BreakingNewsFragment : Fragment(R.layout.fragment_breaking_news) {
    private lateinit var viewModel: NewsViewModel
    private lateinit var binding: FragmentBreakingNewsBinding
    private lateinit var newsAdapter: NewsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBreakingNewsBinding.bind(view)
        viewModel = (activity as NewsActivity).viewModel
        setUpRecyclerView()
        viewModelObserving()
        onItemClick()
    }

    private fun onItemClick() {
        newsAdapter.setOnClickListener {

                if (findNavController().currentDestination?.id == R.id.breakingNewsFragment) {
                findNavController().navigate(
                    BreakingNewsFragmentDirections.actionBreakingNewsFragmentToArticleFragment(it)
                )
            }
        }
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true

    }

    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun viewModelObserving() {
        viewModel.breakingNews.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> {
                    hideProgressBar()
                    it.message?.let { message ->
                        Snackbar.make(binding.root, "Error : $message", Snackbar.LENGTH_SHORT)
                            .show()
                    }
                }
                is Resource.Loading -> showProgressBar()
                is Resource.Success -> {
                    hideProgressBar()
                    it.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        Log.d("test", "submitList to RecyclerView")

                        val totalPages = newsResponse.totalResults / QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.breakingNewsPage == totalPages
                        if (isLastPage) {
                            binding.rvBreakingNews.setPadding(0, 0, 0, 0)
                        }
                    }
                }
            }
        }
    }

    var isLoading = false
    var isScrolling = false
    var isLastPage = false

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
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
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate =
                isNotLoadingAndNotLastPage and isAtLastItem and isNotAtBeginning and isTotalMoreThanVisible and isScrolling
            if (shouldPaginate) {
                viewModel.getBreakingNews()
                isScrolling = false
            }

        }
    }

    private fun setUpRecyclerView() {
        newsAdapter = NewsAdapter()
        binding.rvBreakingNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@BreakingNewsFragment.scrollListener)
            Log.d("test", "setUpRecyclerView")
        }
    }
}