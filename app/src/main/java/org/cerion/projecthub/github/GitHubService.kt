package org.cerion.projecthub.github

import android.content.Context
import android.util.Log
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okio.Buffer
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.*
import java.util.concurrent.TimeUnit

// TODO all ids might be longs
//data class GitHubColumn(val id: Int, val name: String)
data class GitHubProject(val id: Int, val name: String, val state: String, val updated_at: Date)
//data class GitHubCard(val id: Int, val note: String?, val content_url: String?)
data class GitHubIssue(val id: Int, val title: String, val body: String?, val state: String, val url: String, val number: Int, val labels: List<GitHubLabel>)
data class GitHubLabel(val id: Long, val name: String, val description: String?, val color: String)

data class ArchiveCardParams(val archived: Boolean)
data class UpdateCardParams(val note: String, val archived: Boolean = false)
data class CreateCardParams(val note: String)
data class CreateIssueCardParams(val content_id: Int, val content_type: String = "Issue")
data class CreateIssueParams(val title: String, val body: String)
data class UpdateIssueParams(val title: String, val body: String)
data class UpdateIssueState(val state: String)

interface GitHubService {
    @GET("users/asheragy/projects")
    fun getProjectsAsync(): Deferred<List<GitHubProject>>

    //@GET("projects/{id}/columns")
    //fun getProjectColumns(@Path("id")id: Int): Deferred<List<GitHubColumn>>

    //@GET("projects/columns/{id}/cards")
    //fun getCardsForColumn(@Path("id")id: Int): Deferred<List<GitHubCard>>

    //@GET("repos/{user}/{repo}/issues?state=all&per_page=100")
    //fun getIssuesForRepo(@Path("user") user: String, @Path("repo")repo: String): Deferred<List<GitHubIssue>>

    //region Cards
    @PATCH("projects/columns/cards/{card_id}")
    fun updateCard(@Path("card_id")cardId: Int, @Body params: UpdateCardParams): Deferred<ResponseBody>

    @PATCH("projects/columns/cards/{card_id}")
    fun archiveCard(@Path("card_id")cardId: Int, @Body params: ArchiveCardParams): Deferred<ResponseBody>

    @POST("projects/columns/{column_id}/cards")
    fun createCard(@Path("column_id")columnId: Int, @Body params: CreateCardParams): Deferred<ResponseBody>

    @POST("projects/columns/{column_id}/cards")
    fun createCard(@Path("column_id")columnId: Int, @Body params: CreateIssueCardParams): Deferred<ResponseBody>

    @DELETE("projects/columns/cards/{card_id}")
    fun deleteCard(@Path("card_id")id: Int): Call<ResponseBody>

    //endregion

    //region Issues

    @POST("repos/{owner}/{repo}/issues")
    fun createIssue(@Path("owner")owner: String, @Path("repo")repo: String, @Body params: CreateIssueParams): Deferred<GitHubIssue>

    @POST("repos/{owner}/{repo}/issues/{number}")
    fun getIssue(@Path("owner")owner: String, @Path("repo")repo: String, @Path("number")number: Int): Deferred<GitHubIssue>

    @PATCH("repos/{owner}/{repo}/issues/{number}")
    fun updateIssue(@Path("owner")owner: String, @Path("repo")repo: String, @Path("number")number: Int, @Body params: UpdateIssueParams): Deferred<GitHubIssue>

    @PUT("repos/{owner}/{repo}/issues/{number}/labels")
    fun updateIssueLabels(@Path("owner")owner: String, @Path("repo")repo: String, @Path("number")number: Int, @Body labels: List<String>): Deferred<ResponseBody>

    @PATCH("repos/{owner}/{repo}/issues/{number}")
    fun updateIssueState(@Path("owner")owner: String, @Path("repo")repo: String, @Path("number")number: Int, @Body state: UpdateIssueState): Deferred<GitHubIssue>

    //endregion

    @GET("repos/{owner}/{repo}/labels")
    fun getLabelsAsync(@Path("owner")owner: String, @Path("repo")repo: String): Deferred<List<GitHubLabel>>
}

fun getService(context: Context): GitHubService {
    val accessToken = getAccessToken(context)

    val client = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "token $accessToken")
                .addHeader("Accept", "application/vnd.github.inertia-preview+json")
                .build()

            val buffer = Buffer()
            request.body?.writeTo(buffer)
            Log.i("OkHttp", "Request: ${request.url}")
            Log.i("OkHttp", "Body: ${buffer.readUtf8()}")
            chain.proceed(request)
        }
        //.proxy(Proxy.NO_PROXY)
        .build()

    val retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .baseUrl("https://api.github.com/")
        .client(client)
        .build()

    return retrofit.create(GitHubService::class.java)
}

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .add(Date::class.java, Rfc3339DateJsonAdapter())
    .build()