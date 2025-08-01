package org.cerion.projecthub.github

import android.content.Context
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.okHttpClient
import okhttp3.OkHttpClient

fun getGraphQLClient(context: Context): ApolloClient {
    val accessToken = getAccessToken(context)

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "token $accessToken")
                .addHeader("Accept", "application/vnd.github.inertia-preview+json")
                .build()
            chain.proceed(request)
        }
        .build()

    return ApolloClient.Builder()
        .serverUrl("https://api.github.com/graphql")
        .okHttpClient(okHttpClient)
        .build()
}