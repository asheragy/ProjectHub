package org.cerion.projecthub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.net.HttpURLConnection


class LoginFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        view.findViewById<Button>(R.id.login).setOnClickListener {
            val url = "https://github.com/login/oauth/authorize?client_id=${BuildConfig.CLIENT_ID}&redirect_uri=${BuildConfig.CALLBACK_URL}&scope=repo"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        return view
    }

    override fun onResume() {
        super.onResume()

        val uri = requireActivity().intent.data
        if (uri != null) {
            Toast.makeText(requireContext(), "Authorizing", Toast.LENGTH_SHORT).show()
            Log.w("TEST", "clearing intent")
            requireActivity().intent.data = null
            onAuthCodeResponse(uri)
        }
    }

    private fun onAuthCodeResponse(responseUri: Uri) {
        if(responseUri.toString().startsWith(BuildConfig.CALLBACK_URL)) {
            val code = responseUri.getQueryParameter("code")
            if (code != null) {

                lifecycleScope.launch {
                    val token = getAccessToken(code)
                    Log.e(TAG, "token = $token")
                    PreferenceManager.getDefaultSharedPreferences(requireContext()).edit().putString("access_token", token).apply()
                    // TODO need to restart backstack
                    findNavController().navigate(R.id.homeFragment)
                }
            }

            // TODO handle error cases
        }
    }

    private suspend fun getAccessToken(code: String): String {
        return withContext(Dispatchers.IO) {
            val url = "https://github.com/login/oauth/access_token"

            val postBody: RequestBody = FormBody.Builder()
                .add("client_id", BuildConfig.CLIENT_ID)
                .add("client_secret", BuildConfig.CLIENT_SECRET)
                .add("code", code)
                .add("redirect_uri", BuildConfig.CALLBACK_URL)
                .build()

            val request = Request.Builder()
                .url(url)
                .post(postBody)
                .build()

            val response = OkHttpClient().newCall(request).execute()
            if (response.code != HttpURLConnection.HTTP_OK)
                throw Exception("Response code ${response.code}")

            val body = response.body?.string()
            val uri = Uri.parse("$url?$body")

            uri.getQueryParameter("access_token")!!
        }
    }
}
