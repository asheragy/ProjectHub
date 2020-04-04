package org.cerion.projecthub.github

import android.content.Context
import androidx.preference.PreferenceManager

internal fun getAccessToken(context: Context): String {
    return PreferenceManager.getDefaultSharedPreferences(context).getString("access_token", null)
        ?: throw Exception("access token not found")
}