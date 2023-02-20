package com.androiddevs.mvvmnewsapp.ui.fragments

import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.androiddevs.mvvmnewsapp.R
import com.androiddevs.mvvmnewsapp.databinding.FragmentArticleBinding
import com.androiddevs.mvvmnewsapp.databinding.FragmentSearchNewsBinding
import com.androiddevs.mvvmnewsapp.ui.NewsActivity
import com.androiddevs.mvvmnewsapp.ui.viewmodel.NewsViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ArticleFragment : Fragment(R.layout.fragment_article) {
    lateinit var viewModel: NewsViewModel
    lateinit var binding: FragmentArticleBinding
    val args by navArgs<ArticleFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentArticleBinding.bind(view)
        viewModel = (activity as NewsActivity).viewModel
        showWebView()
        saveArticle()

    }
    // SHow Article In WebView

    private fun showWebView() {
        val article = args.articleParcelable
        binding.webView.apply {
            webViewClient = WebViewClient()
            article?.url?.let { loadUrl(it) }
        }
    }


    //add article to room

    private fun saveArticle() {
        binding.fab.setOnClickListener { view ->
            viewModel.saveArticle(args.articleParcelable)
            lifecycleScope.launchWhenStarted {
                viewModel.addStateFlow.collect {
                    Snackbar.make(view, "Successfully Saved ", Snackbar.LENGTH_SHORT)
                        .show()

                }
            }
        }

        }
    }

