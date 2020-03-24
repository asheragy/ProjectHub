package org.cerion.projecthub.github

import android.content.Context
import androidx.preference.PreferenceManager
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.*
import java.util.concurrent.TimeUnit

data class GitHubColumn(val id: Int, val name: String)
data class GitHubProject(val id: Int, val name: String, val state: String, val updated_at: Date)
data class GitHubCard(val id: Int, val note: String?, val content_url: String?)
data class GitHubIssue(val id: Int, val title: String, val state: String, val url: String)

interface GitHubService {
    @GET("users/asheragy/projects")
    fun getProjectsAsync(): Deferred<List<GitHubProject>>

    @GET("projects/{id}/columns")
    fun getProjectColumns(@Path("id")id: Int): Deferred<List<GitHubColumn>>

    @GET("projects/columns/{id}/cards")
    fun getCardsForColumn(@Path("id")id: Int): Deferred<List<GitHubCard>>

    @GET("repos/{user}/{repo}/issues?state=all&per_page=100")
    fun getIssuesForRepo(@Path("user") user: String, @Path("repo")repo: String): Deferred<List<GitHubIssue>>
}

private const val BASE_URL = "https://api.github.com/"

fun getService(context: Context): GitHubService {
    val token =
        PreferenceManager.getDefaultSharedPreferences(context).getString("access_token", null)
            ?: throw Exception("access token not found")

    return getService(token)
}

fun getService(accessToken: String): GitHubService {

    val client = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "token $accessToken")
                .addHeader("Accept", "application/vnd.github.inertia-preview+json")
                .build()
            chain.proceed(request)
        }
        //.proxy(Proxy.NO_PROXY)
        .build()

    val retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .baseUrl(BASE_URL)
        .client(client)
        .build()

    return retrofit.create(GitHubService::class.java)
}

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .add(Date::class.java, Rfc3339DateJsonAdapter())
    .build()


