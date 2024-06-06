package com.example.langsync.Preguntas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.langsync.databinding.FragmentHomeBinding
import com.google.android.material.tabs.TabLayoutMediator

class PreguntasFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val rootView = binding.root

        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout

        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 3

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> PreguntasNativoFragment()
                    1 -> PreguntasInteresFragment()
                    else -> PreguntasUsuarioFragment()
                }
            }
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Idioma Nativo"
                1 -> "Idiomas de Interés"
                else -> "Mis Preguntas"
            }
        }.attach()

        binding.fabAddpregunta.setOnClickListener {
            val intent = Intent(activity, AnadirPregunta::class.java)
            startActivity(intent)
        }

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


