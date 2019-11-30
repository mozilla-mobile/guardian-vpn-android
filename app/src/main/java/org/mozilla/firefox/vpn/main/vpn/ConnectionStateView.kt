package org.mozilla.firefox.vpn.main.vpn

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.CompoundButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import kotlinx.android.synthetic.main.view_connection_state.view.*
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.main.vpn.VpnViewModel.UIModel

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
                warning_text.visibility = View.VISIBLE
                warning_icon.visibility = View.VISIBLE
                val color = ContextCompat.getColor(context, model.stateColorId)
                DrawableCompat.setTint(warning_icon.drawable, color)
                warning_text.setTextColor(color)
                val state = context.getString(model.stateTextId)
                warning_text.text = state
            }
            else -> {
                warning_icon.visibility = View.GONE
                warning_text.visibility = View.GONE
            }
        }

        duration.visibility = if (model is UIModel.Connected) { View.VISIBLE } else { View.GONE }

        switchSilently(model.switchOn)

        applyStyle(model.style)
        currentModel = model
    }

    fun setDuration(duration: String) {
        this.duration.text = duration
    }

    private fun initGlobeAnimation(oldModel: UIModel, newModel: UIModel) {
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
        if (oldModel !is UIModel.Connected && newModel is UIModel.Connected) {
            ripple.playOnce(0, 74).then { loop(75, 120) }
        } else if (oldModel !is UIModel.Disconnecting && newModel is UIModel.Disconnecting) {
            ripple.playOnce(ripple.frame, 210)
        }
    }

    private fun applyStyle(style: UIModel.Styles) {
        container.setBackgroundColor(ContextCompat.getColor(context, style.bkgColorId))
        title.setTextColor(ContextCompat.getColor(context, style.titleColorId))
        description.setTextColor(ContextCompat.getColor(context, style.descriptionColorId))
        duration.setTextColor(ContextCompat.getColor(context, style.descriptionColorId))

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


