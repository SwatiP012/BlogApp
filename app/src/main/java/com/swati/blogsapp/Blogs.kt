package com.swati.blogsapp

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.swati.blogsapp.presentation.ui.Blog
import com.swati.blogsapp.presentation.ui.BlogsList
import com.swati.blogsapp.vm.BlogVM
import kotlinx.coroutines.flow.collect
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.*
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun Blogs(vm: BlogVM = koinViewModel()) {
    val navController = rememberNavController()
    val blogs by vm.blogs.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "blog_list"
    ) {

         composable("blog_list") {
            BlogsList(
                blogs = blogs,
                onRefresh = { vm.loadMoreBlogs() },
                onClick = { link ->
                    // URL encode the link before navigation
                    val encodedLink = URLEncoder.encode(link, "UTF-8")
                    Log.d("Navigation", "Navigating to: blog_screen/$encodedLink")
                    navController.navigate("blog_screen/$encodedLink") {
                        // Optional: Add navigation options
                        launchSingleTop = true
                    }
                }
            )
        }

         composable(
            route = "blog_screen/{link}",
            deepLinks = listOf(navDeepLink {
                uriPattern = "android-app://androidx.navigation/blog_screen/{link}"
            }),
            arguments = listOf(navArgument("link") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            // URL decode the link before use
            val encodedLink = backStackEntry.arguments?.getString("link") ?: ""
            val decodedLink = URLDecoder.decode(encodedLink, "UTF-8")
            Blog(decodedLink)
        }
    }
}