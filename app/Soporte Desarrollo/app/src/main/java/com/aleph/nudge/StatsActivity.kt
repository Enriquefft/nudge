package com.aleph.nudge

import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import android.widget.TextView
import com.aleph.nudge.data.StatsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class StatsActivity : android.app.Activity() {

    private lateinit var tvRevenue: TextView
    private lateinit var tvShown: TextView
    private lateinit var tvAccepted: TextView
    private lateinit var tvRate: TextView
    private lateinit var tvNoData: TextView
    private lateinit var btnBack: ImageButton

    private lateinit var revenueCard: View
    private lateinit var shownCard: View
    private lateinit var acceptedCard: View
    private lateinit var rateCard: View

    private lateinit var statsManager: StatsManager
    private val activityScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        tvRevenue = findViewById(R.id.tv_revenue)
        tvShown = findViewById(R.id.tv_shown)
        tvAccepted = findViewById(R.id.tv_accepted)
        tvRate = findViewById(R.id.tv_rate)
        tvNoData = findViewById(R.id.tv_no_data)
        btnBack = findViewById(R.id.btn_back)

        revenueCard = findViewById(R.id.card_revenue)
        shownCard = findViewById(R.id.card_shown)
        acceptedCard = findViewById(R.id.card_accepted)
        rateCard = findViewById(R.id.card_rate)

        statsManager = (application as NudgeApplication).statsManager

        btnBack.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        refreshStats()
        animateStatsIn()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onDestroy() {
        activityScope.cancel()
        super.onDestroy()
    }

    private fun animateStatsIn() {
        val cards = listOf(revenueCard, shownCard, acceptedCard, rateCard)
        val offsetPx = 20f * resources.displayMetrics.density
        cards.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = offsetPx
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(250)
                .setStartDelay((index * 100).toLong())
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun refreshStats() {
        activityScope.launch {
            val shown = statsManager.getTodayShown()
            val accepted = statsManager.getTodayAccepted()
            val rate = statsManager.getAcceptanceRate()
            val revenue = statsManager.getTodayRevenueFormatted()

            if (shown == 0) {
                tvNoData.visibility = View.VISIBLE
                tvRevenue.text = "$0.00"
                tvShown.text = "0"
                tvAccepted.text = "0"
                tvRate.text = "0%"
            } else {
                tvNoData.visibility = View.GONE
                tvRevenue.text = revenue
                tvShown.text = shown.toString()
                tvAccepted.text = accepted.toString()
                tvRate.text = "${rate.toInt()}%"
            }
        }
    }
}
