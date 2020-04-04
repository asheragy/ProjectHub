package org.cerion.projecthub


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        view.findViewById<Button>(R.id.test).setOnClickListener {
            val token = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("access_token", null)
            if (token == null) {
                Toast.makeText(requireContext(), "Not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                testApi(token)
                Toast.makeText(requireContext(), "Done", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<Button>(R.id.logout).setOnClickListener {
            logout()
        }

        return view
    }

    private suspend fun testApi(accessToken: String) {
        withContext(Dispatchers.IO) {
        }
    }

}
