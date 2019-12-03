package org.mozilla.firefox.vpn.main.vpn

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.CompoundButton
import androidx.cardview.widget.CardView
import androidx.core.graphics.drawable.DrawableCompat
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import kotlinx.android.synthetic.main.view_connection_state.view.*
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.main.vpn.VpnViewModel.UIModel
import org.mozilla.firefox.vpn.util.color
import org.mozilla.firefox.vpn.util.tint

class ConnectionStateView : CardView {

    var onSwitchListener: ((Boolean) -> Unit)? = null
    private var currentModel: UIModel = UIModel.Disconnected()

    private val onCheckedChangedListener =
        CompoundButton.OnCheckedChangeListener { _, isChecked -> onSwitchListener?.invoke(isChecked) }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        radius = context.resources.getDimensionPixelSize(R.dimen.vpn_state_card_radius).toFloat()
        inflate(context, R.layout.view_connection_state, this)
        switch_btn.setOnCheckedChangeListener(onCheckedChangedListener)
        ripple.frame = 0
        warning_icon.setImageDrawable(DrawableCompat.wrap(warning_icon.drawable).mutate())
    }

    fun applyUiModel(model: UIModel) {
        initGlobeAnimation(currentModel, model)
        initRippleAnimation(currentModel, model)

        title.text = context.getString(model.titleId)

        description.text = when (model) {
            is UIModel.Connected -> context.getString(model.descriptionId) +
                    " ${context.getString(R.string.vpn_state_separator)} "
            is UIModel.Switching -> context.getString(model.descriptionId, model.from, model.to)
            is UIModel.NoSignal,
            is UIModel.Unstable -> " ${context.getString(R.string.vpn_state_separator)} " +
                    context.getString(model.descriptionId)
            else -> context.getString(model.descriptionId)
        }

        when (model) {
            is UIModel.WarningState -> {
                val color = context.color(model.stateColorId)
                warning_icon.apply {
                    visibility = View.VISIBLE
                    drawable.tint(color)
                }
                warning_text.apply {
                    visibility = View.VISIBLE
                    text = context.getString(model.stateTextId)
                    setTextColor(color)
                }
            }
            else -> {
                warning_icon.visibility = View.GONE
                warning_text.visibility = View.GONE
            }
        }

        duration.visibility = if (model is UIModel.Connected) { View.VISIBLE } else { View.GONE }

        switch_btn.isEnabled = when (model) {
            is UIModel.Connecting,
            is UIModel.Disconnecting,
            is UIModel.Switching -> false
            else -> true
        }

        switchSilently(model.switchOn)

        applyStyle(model.style)
        currentModel = model
    }

    fun setDuration(duration: String) {
        this.duration.text = duration
    }

    private fun initGlobeAnimation(oldModel: UIModel, newModel: UIModel) {
        globe.frame = when (newModel) {
            is UIModel.Disconnected -> 0
            is UIModel.Connecting -> 15
            is UIModel.Unstable,
            is UIModel.NoSignal,
            is UIModel.WarningState,
            is UIModel.Connected -> 30
            is UIModel.Switching -> 45
            is UIModel.Disconnecting -> 75
        }

        if (oldModel is UIModel.Disconnected && newModel is UIModel.Connecting) {
            globe.playOnce(0, 14)
        } else if (oldModel is UIModel.Connecting && newModel is UIModel.Connected) {
            globe.playOnce(15, 29)
        } else if (oldModel is UIModel.Connected && newModel is UIModel.Switching) {
            globe.playOnce(30, 44)
        } else if (oldModel is UIModel.Switching && newModel is UIModel.Connected) {
            globe.playOnce(45, 59)
        } else if (oldModel is UIModel.Connected && newModel is UIModel.Disconnecting) {
            globe.playOnce(60, 74)
        } else if (oldModel is UIModel.Disconnecting && newModel is UIModel.Disconnected) {
            globe.playOnce(75, 89)
        }
    }

    private fun initRippleAnimation(oldModel: UIModel, newModel: UIModel) {
        when (newModel) {
            is UIModel.Connecting,
            is UIModel.Disconnected,
            is UIModel.NoSignal -> {
                ripple.pauseAnimation()
                ripple.setMinAndMaxFrame(0, 0)
                ripple.progress = 0f
            }
        }

        val playEnterAnimation = oldModel !is UIModel.Connected && newModel is UIModel.Connected
        val playEndAnimation = oldModel is UIModel.Connected &&
                (newModel is UIModel.Disconnecting || newModel is UIModel.Unstable)

        if (playEnterAnimation) {
            ripple.playOnce(0, 74).then { loop(75, 120) }
        } else if (playEndAnimation) {
            ripple.playOnce(ripple.frame, 210)
        }
    }

    private fun applyStyle(style: UIModel.Styles) {
        container.setBackgroundColor(context.color(style.bkgColorId))
        title.setTextColor(context.color(style.titleColorId))
        description.setTextColor(context.color(style.descriptionColorId))
        duration.setTextColor(context.color(style.descriptionColorId))

        switch_btn.alpha = style.switchAlpha

        elevation = context.resources.getDimensionPixelSize(style.bkgElevation).toFloat()
    }

    private fun switchSilently(isChecked: Boolean) {
        switch_btn.setOnCheckedChangeListener(null)
        switch_btn.isChecked = isChecked
        switch_btn.setOnCheckedChangeListener(onCheckedChangedListener)
    }
}

fun LottieAnimationView.loop(min: Int, max: Int, mode: Int = LottieDrawable.RESTART): LottieAnimationView {
    repeatCount = LottieDrawable.INFINITE
    repeatMode = mode
    setMinAndMaxFrame(min, max)
    playAnimation()
    return this
}

fun LottieAnimationView.playOnce(min: Int, max: Int): LottieAnimationView {
    repeatCount = 0
    setMinAndMaxFrame(min, max)
    playAnimation()
    return this
}

fun LottieAnimationView.then(block: LottieAnimationView.() -> Unit) {
    addAnimatorListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
            removeAnimatorListener(this)
            block(this@then)
        }
    })
}


