package com.example.memorygdynia

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.memorygdynia.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.startButton.setOnClickListener {
            val nickname = binding.nicknameInput.text.toString()
            if (nickname.isBlank()) {
                binding.nicknameInput.error = "Please enter your nickname"
                return@setOnClickListener
            }

            val intent = Intent(requireActivity(), GameActivity::class.java)
            intent.putExtra("nickname", nickname)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateTopScoresList()
    }

    private fun updateTopScoresList() {
        val db = ScoresDatabase(requireContext())
        val topScores = db.getTopScores()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, topScores.map { "${it.nickname}: ${it.moves} moves" })
        binding.topScoresList.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
