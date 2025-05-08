package com.swati.blogsapp

import com.swati.blogsapp.vm.BlogVM
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val blogModules = module {
    viewModel { BlogVM(get()) }
}