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
import org.cerion.projecthub.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentHomeBinding.inflate(layoutInflater, container, false)

        binding.test.setOnClickListener {
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

        binding.logout.setOnClickListener {
            logout()
        }

        return binding.root
    }

    private suspend fun testApi(accessToken: String) {
        withContext(Dispatchers.IO) {
        }
    }

}
