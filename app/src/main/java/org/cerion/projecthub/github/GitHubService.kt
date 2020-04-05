package org.cerion.projecthub.github

import android.content.Context
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.*
import java.util.concurrent.TimeUnit

//data class GitHubColumn(val id: Int, val name: String)
data class GitHubProject(val id: Int, val name: String, val state: String, val updated_at: Date)
data class GitHubCard(val id: Int, val note: String?, val content_url: String?)
data class GitHubIssue(val id: Int, val title: String, val state: String, val url: String)

// position values: top / bottom / after:<card_id>
data class MoveCardParams(val column_id: Int, val position: String = "bottom")

data class UpdateCardParams(val note: String, val archived: Boolean = false)
data class CreateCardParams(val note: String)

interface GitHubService {
    @GET("users/asheragy/projects")
    fun getProjectsAsync(): Deferred<List<GitHubProject>>

    @POST("projects/columns/cards/{card_id}/moves")
    fun moveCard(@Path("card_id")cardId: Int, @Body params: MoveCardParams): Deferred<ResponseBody>

    @PATCH("projects/columns/cards/{card_id}")
    fun updateCard(@Path("card_id")cardId: Int, @Body params: UpdateCardParams): Deferred<ResponseBody>

    @POST("projects/columns/{column_id}/cards")
    fun createCard(@Path("column_id")columnId: Int, @Body params: CreateCardParams): Deferred<ResponseBody>

    //@GET("projects/{id}/columns")
    //fun getProjectColumns(@Path("id")id: Int): Deferred<List<GitHubColumn>>

    @GET("projects/columns/{id}/cards")
    fun getCardsForColumn(@Path("id")id: Int): Deferred<List<GitHubCard>>

    @GET("repos/{user}/{repo}/issues?state=all&per_page=100")
    fun getIssuesForRepo(@Path("user") user: String, @Path("repo")repo: String): Deferred<List<GitHubIssue>>
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