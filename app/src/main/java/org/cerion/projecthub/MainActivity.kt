package org.cerion.projecthub

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager


class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val graphInflater = navHostFragment.navController.navInflater
        val navGraph = graphInflater.inflate(R.navigation.nav_graph)

        val token = PreferenceManager.getDefaultSharedPreferences(this).getString("access_token", null)
        navGraph.setStartDestination(if (token == null) R.id.loginFragment else R.id.projectListFragment)
        navController.graph = navGraph

        NavigationUI.setupActionBarWithNavController(this, navController)
        //setupActionBarWithNavController(navController)
        //setupActionBarWithNavController(navController)
        //supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        navController.navigateUp()
        return super.onSupportNavigateUp()
    }

    //override fun onSupportNavigateUp(): Boolean {
    //    return true
    //}
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
