package org.cerion.projecthub

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val graphInflater = navHostFragment.navController.navInflater
        val navGraph = graphInflater.inflate(R.navigation.nav_graph)

        val token = PreferenceManager.getDefaultSharedPreferences(this).getString("access_token", null)
        navGraph.startDestination = if (token == null) R.id.loginFragment else R.id.projectListFragment
        navController.graph = navGraph
    }
}

fun Fragment.logout() {
    PreferenceManager.getDefaultSharedPreferences(requireContext()).edit().remove("access_token").apply()
    requireActivity().finish()
    startActivity(requireActivity().intent)

}

val Any.TAG: String
    get() {
        val tag = javaClass.simpleName
        return if (tag.length <= 23) tag else tag.substring(0, 23)
    }
