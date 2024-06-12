package com.example.memorygdynia

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.memorygdynia.databinding.FragmentGameBinding
import kotlin.math.abs

class GameFragment : Fragment() {

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!

    private val imageViews: MutableList<ImageView> = ArrayList()
    private val imageResources: MutableList<Int> = ArrayList()
    private val flippedCards: MutableList<ImageView> = ArrayList()
    private val flippedCardTags: MutableList<Int> = ArrayList()
    private var moves = 0

    private var originalX = 0f
    private var originalY = 0f
    private var originalWidth = 0
    private var originalHeight = 0

    private lateinit var nickname: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nickname = requireActivity().intent.getStringExtra("nickname") ?: ""

        initializeGame()

        binding.resetButton.setOnClickListener { resetGame() }
    }

    private fun initializeGame() {
        loadImages()
        imageResources.shuffle() // Shuffle the images

        binding.gridLayout.removeAllViews()
        imageViews.clear()
        flippedCards.clear()
        flippedCardTags.clear()

        for (i in 0 until NUM_CARDS) {
            val imageView = ImageView(requireContext())
            imageView.setImageResource(R.drawable.pic_back)
            imageView.tag = imageResources[i]

            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = 0
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(8, 8, 8, 8)
            }
            imageView.layoutParams = params

            imageView.scaleType = ImageView.ScaleType.FIT_CENTER

            imageView.setOnClickListener { onCardClicked(imageView) }
            binding.gridLayout.addView(imageView)
            imageViews.add(imageView)
        }
    }

    private fun loadImages() {
        imageResources.clear()
        imageResources.add(R.drawable.pic_101)
        imageResources.add(R.drawable.pic_201)
        imageResources.add(R.drawable.pic_102)
        imageResources.add(R.drawable.pic_202)
        imageResources.add(R.drawable.pic_103)
        imageResources.add(R.drawable.pic_203)
        imageResources.add(R.drawable.pic_104)
        imageResources.add(R.drawable.pic_204)
        imageResources.add(R.drawable.pic_105)
        imageResources.add(R.drawable.pic_205)
        imageResources.add(R.drawable.pic_106)
        imageResources.add(R.drawable.pic_206)
        imageResources.add(R.drawable.pic_107)
        imageResources.add(R.drawable.pic_207)
        imageResources.add(R.drawable.pic_108)
        imageResources.add(R.drawable.pic_208)
    }

    private fun onCardClicked(imageView: ImageView) {
        if (flippedCards.size == 2) return

        val imageId = imageView.tag as Int
        imageView.setImageResource(imageId)

        flippedCards.add(imageView)
        flippedCardTags.add(imageId)

        if (flippedCards.size == 2) {
            moves++
            updateMoveCounter()

            if (isMatch(flippedCardTags[0], flippedCardTags[1])) {
                flippedCards.clear()
                flippedCardTags.clear()
                Toast.makeText(requireContext(), "Match found!", Toast.LENGTH_SHORT).show()

                if (imageViews.all { it.drawable.constantState != resources.getDrawable(R.drawable.pic_back).constantState }) {
                    val db = ScoresDatabase(requireContext())
                    db.insertScore(nickname, moves)
                    Toast.makeText(requireContext(), "Congratulations! You've completed the game.", Toast.LENGTH_SHORT).show()
                    // Return to WelcomeFragment
                    requireActivity().finish()
                }
            } else {
                Handler(Looper.getMainLooper()).postDelayed({
                    for (card in flippedCards) {
                        card.setImageResource(R.drawable.pic_back)
                    }
                    flippedCards.clear()
                    flippedCardTags.clear()
                }, DELAY_DURATION)
            }
        }

        animateCard(imageView, imageId)
    }

    private fun isMatch(firstTag: Int, secondTag: Int): Boolean {
        return abs(firstTag - secondTag) == 8
    }

    private fun animateCard(imageView: ImageView, imageId: Int) {
        val location = IntArray(2)
        imageView.getLocationOnScreen(location)
        originalX = location[0].toFloat()
        originalY = location[1].toFloat()
        originalWidth = imageView.width
        originalHeight = imageView.height

        binding.overlayImageView.setImageResource(imageId)
        binding.overlayImageView.visibility = View.VISIBLE
        binding.overlayImageView.x = originalX
        binding.overlayImageView.y = originalY
        val layoutParams = binding.overlayImageView.layoutParams
        layoutParams.width = originalWidth
        layoutParams.height = originalHeight
        binding.overlayImageView.layoutParams = layoutParams
        binding.overlayImageView.scaleType = ImageView.ScaleType.FIT_CENTER

        binding.overlayImageView.post {
            val rootView = requireActivity().findViewById<ViewGroup>(android.R.id.content).rootView
            val centerX = (rootView.width - binding.overlayImageView.width) / 2
            val centerY = (rootView.height - binding.overlayImageView.height) / 2

            val scaleX = ObjectAnimator.ofFloat(binding.overlayImageView, "scaleX", 1f, 4f)
            val scaleY = ObjectAnimator.ofFloat(binding.overlayImageView, "scaleY", 1f, 4f)
            val translateX = ObjectAnimator.ofFloat(binding.overlayImageView, "x", centerX.toFloat())
            val translateY = ObjectAnimator.ofFloat(binding.overlayImageView, "y", centerY.toFloat())

            val animatorSet = AnimatorSet().apply {
                interpolator = AccelerateDecelerateInterpolator()
                duration = ANIMATION_DURATION
                playTogether(scaleX, scaleY, translateX, translateY)
            }

            animatorSet.addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    binding.overlayImageView.postDelayed({
                        val scaleX = ObjectAnimator.ofFloat(binding.overlayImageView, "scaleX", 4f, 1f)
                        val scaleY = ObjectAnimator.ofFloat(binding.overlayImageView, "scaleY", 4f, 1f)
                        val translateX = ObjectAnimator.ofFloat(binding.overlayImageView, "x", originalX)
                        val translateY = ObjectAnimator.ofFloat(binding.overlayImageView, "y", originalY)

                        val returnAnimatorSet = AnimatorSet().apply {
                            interpolator = AccelerateDecelerateInterpolator()
                            duration = ANIMATION_DURATION
                            playTogether(scaleX, scaleY, translateX, translateY)
                        }
                        returnAnimatorSet.start()

                        returnAnimatorSet.addListener(object : android.animation.AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: android.animation.Animator) {
                                binding.overlayImageView.visibility = View.GONE
                            }
                        })
                    }, 500)
                }
            })
            animatorSet.start()
        }
    }

    private fun updateMoveCounter() {
        binding.moveCounter.text = "Moves: $moves"
    }

    private fun resetGame() {
        moves = 0
        updateMoveCounter()
        initializeGame()
    }

    companion object {
        private const val NUM_CARDS = 16
        private const val ANIMATION_DURATION = 1000L
        private const val DELAY_DURATION = 2000L
    }
}
