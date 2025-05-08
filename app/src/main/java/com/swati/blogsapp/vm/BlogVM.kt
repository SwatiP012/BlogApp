package com.swati.blogsapp.vm

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swati.blogsapp.data.BlogDatabase
import com.swati.blogsapp.data.BlogPost
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class BlogVM(
    application: Application,
) : ViewModel() {

    private val api: BlogApi
    private val blogDatabase = BlogDatabase.getDatabase(application)
    private val blogDao = blogDatabase.blogDao()
    private val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private var currentPage = 1
    private var _blogs = MutableStateFlow<List<BlogPost>>(emptyList())

    val blogs: StateFlow<List<BlogPost>> get() = _blogs

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://blog.vrid.in/wp-json/wp/v2/") // Base URL for the API
            .addConverterFactory(GsonConverterFactory.create()) // Converts JSON response into Kotlin objects
            .build()

        api = retrofit.create(BlogApi::class.java)

        // Check if we have blog posts saved locally and load them if available
        viewModelScope.launch {
            val savedBlogs = blogDao.getBlogPosts()
            _blogs.value = savedBlogs

            // If no blogs are saved locally, load blogs from the network
            if (_blogs.value.isEmpty()) {
                loadMoreBlogs()
            }
        }
    }

    // Check if network is available before making API calls
    private fun isNetworkAvailable(): Boolean {
        return connectivityManager.activeNetwork != null
    }

    // Load more blogs from the API
    fun loadMoreBlogs() {
        viewModelScope.launch {
            if (!isNetworkAvailable()) return@launch

            try {
                // Fetch blogs from API and append to the current list
                val newBlogs = api.getPosts(perPage = 10, page = currentPage)
                _blogs.value = _blogs.value + newBlogs

                // Increment the currentPage for next API call
                currentPage++

                // Save new blogs into local database for offline use
                for (blog in newBlogs) {
                    blogDao.insertBlogPost(blog)
                }
            } catch (e: Exception) {
                // Handle error if API request fails
                // You might want to log this error or notify the user
                e.printStackTrace()
            }
        }
    }
}

// Retrofit API interface to fetch blog posts
interface BlogApi {
    @GET("posts")
    suspend fun getPosts(
        @Query("per_page") perPage: Int,
        @Query("page") page: Int
    ): List<BlogPost>
}
