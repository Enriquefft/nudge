package com.aleph.nudge.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.aleph.nudge.R
import com.aleph.nudge.model.Suggestion

class SuggestionCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val tvItemName: TextView
    private val tvItemPrice: TextView
    private val tvReason: TextView
    private val btnAdd: Button
    private val btnDismiss: Button

    private val mainHandler = Handler(Looper.getMainLooper())
    private var autoDismissRunnable: Runnable? = null
    private var isDismissing = false

    init {
        LayoutInflater.from(context).inflate(R.layout.view_suggestion_card, this, true)
        tvItemName = findViewById(R.id.tv_item_name)
        tvItemPrice = findViewById(R.id.tv_item_price)
        tvReason = findViewById(R.id.tv_reason)
        btnAdd = findViewById(R.id.btn_add)
        btnDismiss = findViewById(R.id.btn_dismiss)
    }

    fun show(suggestion: Suggestion) {
        tvItemName.text = suggestion.itemName
        tvItemPrice.text = suggestion.priceFormatted
        tvReason.text = "\"${suggestion.reason}\""

        val slideIn = AnimationUtils.loadAnimation(context, R.anim.slide_in_bottom)
        startAnimation(slideIn)

        cancelAutoDismiss()
        val runnable = Runnable { dismiss() }
        autoDismissRunnable = runnable
        mainHandler.postDelayed(runnable, 15000L)
    }

    fun dismiss() {
        if (isDismissing) return
        isDismissing = true
        cancelAutoDismiss()

        btnAdd.isEnabled = false
        btnDismiss.isEnabled = false

        val slideOut = AnimationUtils.loadAnimation(context, R.anim.slide_out_bottom)
        slideOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                mainHandler.post {
                    val parent = parent
                    if (parent is ViewGroup) {
                        parent.removeView(this@SuggestionCardView)
                    }
                }
            }
        })
        startAnimation(slideOut)
    }

    fun showAddedConfirmation() {
        btnAdd.text = context.getString(R.string.added_confirmation)
        btnAdd.setBackgroundColor(ContextCompat.getColor(context, R.color.semantic_success))
        btnAdd.setTextColor(ContextCompat.getColor(context, R.color.text_on_brand))
        btnDismiss.visibility = View.GONE
    }

    fun setOnAddClickListener(listener: (onResult: (Boolean) -> Unit) -> Unit) {
        btnAdd.setOnClickListener {
            if (!isDismissing) {
                btnAdd.isEnabled = false
                listener { success ->
                    mainHandler.post {
                        if (success) {
                            showAddedConfirmation()
                            mainHandler.postDelayed({ dismiss() }, 600)
                        } else {
                            showErrorFeedback()
                        }
                    }
                }
            }
        }
    }

    fun showErrorFeedback() {
        btnAdd.text = context.getString(R.string.add_failed)
        btnAdd.setBackgroundColor(ContextCompat.getColor(context, R.color.semantic_error))
        btnAdd.setTextColor(ContextCompat.getColor(context, R.color.text_on_brand))
        mainHandler.postDelayed({ resetButton() }, 1500)
    }

    fun resetButton() {
        btnAdd.text = context.getString(R.string.add_to_order)
        btnAdd.setBackgroundResource(R.drawable.btn_primary_bg)
        btnAdd.setTextColor(ContextCompat.getColor(context, R.color.text_on_accent))
        btnAdd.isEnabled = true
    }

    fun setOnDismissClickListener(listener: () -> Unit) {
        btnDismiss.setOnClickListener { if (!isDismissing) listener() }
    }

    private fun cancelAutoDismiss() {
        autoDismissRunnable?.let { mainHandler.removeCallbacks(it) }
        autoDismissRunnable = null
    }
}
