package org.cerion.projecthub

import android.app.Application
import org.cerion.projecthub.github.getGraphQLClient
import org.cerion.projecthub.github.getService
import org.cerion.projecthub.repository.IssueRepository
import org.cerion.projecthub.repository.ProjectRepository
import org.cerion.projecthub.ui.ProjectBrowserViewModel
import org.cerion.projecthub.ui.project.IssueViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@App)

            modules(listOf(
                networkModule,
                repositoryModule,
                viewModelModule
            ))
        }
    }
}

val networkModule = module {
    single { getService(androidContext()) }
    single { getGraphQLClient(androidContext()) }
}

val repositoryModule = module {
    single { IssueRepository(get()) }
    single { ProjectRepository(get()) }
}

val viewModelModule = module {
    viewModel { IssueViewModel(get()) }
    viewModel { ProjectBrowserViewModel(get()) }
}