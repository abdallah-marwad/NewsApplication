package com.androiddevs.mvvmnewsapp.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.androiddevs.mvvmnewsapp.R
import com.androiddevs.mvvmnewsapp.databinding.ItemArticlePreviewBinding
import com.androiddevs.mvvmnewsapp.models.Article
import com.bumptech.glide.Glide

class NewsAdapter : RecyclerView.Adapter<NewsAdapter.ArticleViewHolder>() {
    var list: MutableList<Article>? = null
    var useList: Boolean = false

    inner class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemArticlePreviewBinding

        init {
            binding = ItemArticlePreviewBinding.bind(itemView)
        }
    }

    private val differCallBack =
        object : DiffUtil.ItemCallback<Article>() {
            override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
                return oldItem.url == newItem.url
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
                return oldItem == newItem
            }
        }
    val differ = AsyncListDiffer(this, differCallBack)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_article_preview,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return if (!useList) {
            differ.currentList.size
        } else {
            list?.size ?: 0
        }
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        var article: Article?
        if (useList) {
            article = list?.get(position)

        } else {
            article = differ.currentList[position]
        }
        holder.binding.apply {
            Glide.with(root).load(article?.urlToImage).into(ivArticleImage)
            tvSource.text = article?.source?.name
            title.text = article?.title
            tvDescription.text = article?.description
            tvPublishedAt.text = article?.publishedAt
        }
        holder.binding.root.setOnClickListener {
            onItemClickListener?.let {
                if (article != null) {
                    it(article)
                }
            }
        }
    }

    // Lambda For Listener
    private var onItemClickListener: ((Article) -> Unit)? = null
    fun setOnClickListener(listener: (Article) -> Unit) {
        onItemClickListener = listener
    }

}