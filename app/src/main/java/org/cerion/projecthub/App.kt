package org.cerion.projecthub

import android.app.Application
import org.cerion.projecthub.database.AppDatabase
import org.cerion.projecthub.github.getGraphQLClient
import org.cerion.projecthub.repository.*
import org.cerion.projecthub.ui.ProjectBrowserViewModel
import org.cerion.projecthub.ui.ProjectListViewModel
import org.cerion.projecthub.ui.project.IssueViewModel
import org.cerion.projecthub.ui.project.ProjectHomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module

const val USE_MOCK_DATA = false

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@App)

            modules(listOf(
                networkModule,
                databaseModule,
                repositoryModule,
                viewModelModule
            ))
        }
    }
}

val networkModule = module {
    single { getGraphQLClient(androidContext()) }
}

val databaseModule = module {
    // TODO is this correct way to get each one?
    single { AppDatabase.getInstance(androidContext()).projectDao() }
}

val repositoryModule = module {
    single { ProjectRepository(get(), get()) }
    single { CardRepository(get()) }
    single { ColumnRepository(get()) }
}

val viewModelModule = module {
    viewModel { IssueViewModel(get()) }
    viewModel { ProjectBrowserViewModel(get()) }
    viewModel { ProjectListViewModel(get()) }
    viewModel { ProjectHomeViewModel(get(), get(), get()) }
}