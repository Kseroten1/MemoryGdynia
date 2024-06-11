package com.example.memorygdynia

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    companion object {
        const val NUM_CARDS = 16
        const val MATCH_DIFFERENCE = 8
        const val ANIMATION_DURATION = 1000L
        const val DELAY_DURATION = 2000L
    }

    private lateinit var welcomeScreen: LinearLayout
    private lateinit var gameScreen: LinearLayout
    private lateinit var nicknameInput: EditText
    private lateinit var topScoresList: ListView
    private lateinit var moveCounter: TextView
    private lateinit var gridLayout: GridLayout
    private lateinit var overlayImageView: ImageView

    private val imageViews: MutableList<ImageView> = ArrayList()
    private val imageResources: MutableList<Int> = ArrayList()
    private val flippedCards: MutableList<ImageView> = ArrayList()
    private val flippedCardTags: MutableList<Int> = ArrayList()
    private val topScores: MutableList<Pair<String, Int>> = mutableListOf()
    private var moves = 0

    private var originalX = 0f
    private var originalY = 0f
    private var originalWidth = 0
    private var originalHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        welcomeScreen = findViewById(R.id.welcomeScreen)
        gameScreen = findViewById(R.id.gameScreen)
        nicknameInput = findViewById(R.id.nicknameInput)
        topScoresList = findViewById(R.id.topScoresList)
        moveCounter = findViewById(R.id.moveCounter)
        gridLayout = findViewById(R.id.gridLayout)
        overlayImageView = findViewById(R.id.overlayImageView)

        findViewById<Button>(R.id.startButton).setOnClickListener { startGame() }
        findViewById<Button>(R.id.resetButton).setOnClickListener { resetGame() }

        // Initialize top scores list
        updateTopScoresList()
    }

    private fun startGame() {
        val nickname = nicknameInput.text.toString()
        if (nickname.isBlank()) {
            Toast.makeText(this, "Please enter your nickname", Toast.LENGTH_SHORT).show()
            return
        }

        welcomeScreen.visibility = View.GONE
        gameScreen.visibility = View.VISIBLE

        moves = 0
        updateMoveCounter()
        initializeGame()
    }

    private fun initializeGame() {
        loadImages()
        val cards = (imageResources + imageResources).shuffled() // Create pairs and shuffle them

        gridLayout.removeAllViews()
        imageViews.clear()
        flippedCards.clear()
        flippedCardTags.clear()

        for (i in 0 until NUM_CARDS) {
            val imageView = createImageView(cards[i])
            gridLayout.addView(imageView)
            imageViews.add(imageView)
        }
    }

    private fun createImageView(imageResource: Int): ImageView {
        return ImageView(this).apply {
            setImageResource(R.drawable.pic_back)
            tag = imageResource

            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = 0
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(8, 8, 8, 8)
            }

            scaleType = ImageView.ScaleType.FIT_CENTER

            setOnClickListener { onCardClicked(this) }
        }
    }

    private fun loadImages() {
        imageResources.clear()
        imageResources.addAll(listOf(
            R.drawable.pic_101, R.drawable.pic_102,
            R.drawable.pic_103, R.drawable.pic_104,
            R.drawable.pic_105, R.drawable.pic_106,
            R.drawable.pic_107, R.drawable.pic_108
        ))
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

            if (abs(flippedCardTags[0] - flippedCardTags[1]) == MATCH_DIFFERENCE) {
                flippedCards.clear()
                flippedCardTags.clear()
                Toast.makeText(this, "Match found!", Toast.LENGTH_SHORT).show()

                if (imageViews.all { it.drawable.constantState != resources.getDrawable(R.drawable.pic_back).constantState }) {
                    val nickname = nicknameInput.text.toString()
                    topScores.add(Pair(nickname, moves))
                    topScores.sortBy { it.second }
                    updateTopScoresList()
                    Toast.makeText(this, "Congratulations! You've completed the game.", Toast.LENGTH_SHORT).show()
                    resetGame()
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

    private fun animateCard(imageView: ImageView, imageId: Int) {
        val location = IntArray(2)
        imageView.getLocationOnScreen(location)
        originalX = location[0].toFloat()
        originalY = location[1].toFloat()
        originalWidth = imageView.width
        originalHeight = imageView.height

        overlayImageView.setImageResource(imageId)
        overlayImageView.visibility = View.VISIBLE
        overlayImageView.x = originalX
        overlayImageView.y = originalY
        val layoutParams = overlayImageView.layoutParams
        layoutParams.width = originalWidth
        layoutParams.height = originalHeight
        overlayImageView.layoutParams = layoutParams
        overlayImageView.scaleType = ImageView.ScaleType.FIT_CENTER

        overlayImageView.post {
            val rootView = findViewById<ViewGroup>(android.R.id.content).rootView
            val centerX = (rootView.width - overlayImageView.width) / 2
            val centerY = (rootView.height - overlayImageView.height) / 2

            val scaleX = ObjectAnimator.ofFloat(overlayImageView, "scaleX", 1f, 4f)
            val scaleY = ObjectAnimator.ofFloat(overlayImageView, "scaleY", 1f, 4f)
            val translateX = ObjectAnimator.ofFloat(overlayImageView, "x", centerX.toFloat())
            val translateY = ObjectAnimator.ofFloat(overlayImageView, "y", centerY.toFloat())

            val animatorSet = AnimatorSet().apply {
                interpolator = AccelerateDecelerateInterpolator()
                duration = ANIMATION_DURATION
                playTogether(scaleX, scaleY, translateX, translateY)
            }

            animatorSet.addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    overlayImageView.postDelayed({
                        val scaleX = ObjectAnimator.ofFloat(overlayImageView, "scaleX", 4f, 1f)
                        val scaleY = ObjectAnimator.ofFloat(overlayImageView, "scaleY", 4f, 1f)
                        val translateX = ObjectAnimator.ofFloat(overlayImageView, "x", originalX)
                        val translateY = ObjectAnimator.ofFloat(overlayImageView, "y", originalY)

                        val returnAnimatorSet = AnimatorSet().apply {
                            interpolator = AccelerateDecelerateInterpolator()
                            duration = ANIMATION_DURATION
                            playTogether(scaleX, scaleY, translateX, translateY)
                        }
                        returnAnimatorSet.start()

                        returnAnimatorSet.addListener(object : android.animation.AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: android.animation.Animator) {
                                overlayImageView.visibility = View.GONE
                            }
                        })
                    }, 500)
                }
            })
            animatorSet.start()
        }
    }

    private fun updateMoveCounter() {
        moveCounter.text = "Moves: $moves"
    }

    private fun updateTopScoresList() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, topScores.map { "${it.first}: ${it.second} moves" })
        topScoresList.adapter = adapter
    }

    private fun resetGame() {
        welcomeScreen.visibility = View.VISIBLE
        gameScreen.visibility = View.GONE

        gridLayout.removeAllViews()
        imageViews.clear()
        flippedCards.clear()
        flippedCardTags.clear()
    }
}