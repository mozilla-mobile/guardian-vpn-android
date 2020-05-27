package org.mozilla.firefox.vpn.main.vpn

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import androidx.cardview.widget.CardView
import androidx.core.graphics.drawable.DrawableCompat
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.databinding.ViewConnectionStateBinding
import org.mozilla.firefox.vpn.main.vpn.VpnViewModel.UIModel
import org.mozilla.firefox.vpn.util.color
import org.mozilla.firefox.vpn.util.tint

class ConnectionStateView : CardView {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var onSwitchListener: ((Boolean) -> Unit)? = null

    private val binding: ViewConnectionStateBinding = ViewConnectionStateBinding.inflate(LayoutInflater.from(context), this, true)

    private val onCheckedChangedListener =
        CompoundButton.OnCheckedChangeListener { button, isChecked ->
            // every time the onRestoreInstanceState() is called, onCheckedChangedListener will be
            // triggered, button.isPressed here help to check whether the change is initiated by the user
            if (button.isPressed) {
                onSwitchListener?.invoke(isChecked)
                button.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            }
        }

    private var currentModel: UIModel = UIModel.Disconnected()

    init {
        radius = context.resources.getDimensionPixelSize(R.dimen.vpn_state_card_radius).toFloat()
        binding.switchBtn.setOnCheckedChangeListener(onCheckedChangedListener)
        binding.ripple.frame = 0
        binding.warningIcon.setImageDrawable(DrawableCompat.wrap(binding.warningIcon.drawable).mutate())
    }

    fun applyUiModel(model: UIModel) {
        initGlobeAnimation(currentModel, model)
        initRippleAnimation(currentModel, model)
        initHapticFeedback(currentModel, model)

        binding.title.text = model.title.resolve(context)

        binding.description.text = when (model) {
            is UIModel.Connected -> model.description.resolve(context) +
                    " ${context.getString(R.string.vpn_state_separator)} "
            is UIModel.Switching -> model.description.resolve(context)
            is UIModel.NoSignal,
            is UIModel.Unstable -> " ${context.getString(R.string.vpn_state_separator)} " +
                    model.description.resolve(context)
            else -> model.description.resolve(context)
        }

        when (model) {
            is UIModel.WarningState -> {
                val color = context.color(model.stateColorId)
                binding.warningIcon.apply {
                    visibility = View.VISIBLE
                    drawable.tint(color)
                }
                binding.warningText.apply {
                    visibility = View.VISIBLE
                    text = model.stateText.resolve(context)
                    setTextColor(color)
                }
            }
            else -> {
                binding.warningIcon.visibility = View.GONE
                binding.warningText.visibility = View.GONE
            }
        }

        binding.duration.visibility = if (model is UIModel.Connected) { View.VISIBLE } else { View.GONE }

        binding.switchBtn.isEnabled = when (model) {
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
        binding.duration.text = duration
    }

    private fun initGlobeAnimation(oldModel: UIModel, newModel: UIModel) {
        val fromAnyConnectedState = oldModel is UIModel.Connected ||
                oldModel is UIModel.Unstable ||
                oldModel is UIModel.NoSignal

        if (oldModel is UIModel.Disconnected && newModel is UIModel.Connecting) {
            binding.globe.playOnce(0, 14)
        } else if (oldModel is UIModel.Connecting && newModel is UIModel.Connected) {
            binding.globe.playOnce(15, 29)
        } else if (oldModel is UIModel.Connected && newModel is UIModel.Switching) {
            binding.globe.playOnce(30, 44)
        } else if (oldModel is UIModel.Switching && newModel is UIModel.Connected) {
            binding.globe.playOnce(45, 59)
        } else if (fromAnyConnectedState && newModel is UIModel.Disconnecting) {
            binding.globe.playOnce(60, 74)
        } else if (oldModel is UIModel.Disconnecting && newModel is UIModel.Disconnected) {
            binding.globe.playOnce(75, 89)
        } else if (fromAnyConnectedState && newModel is UIModel.Switching) {
            binding.globe.playOnce(30, 44)
        } else {
            val frame = when (newModel) {
                is UIModel.Disconnected -> 0
                is UIModel.Connecting -> 15
                is UIModel.Unstable,
                is UIModel.NoSignal,
                is UIModel.WarningState,
                is UIModel.Connected -> 30
                is UIModel.Switching -> 30
                is UIModel.Disconnecting -> 75
            }
            binding.globe.fixAtFrame(frame)
        }
    }

    private fun initRippleAnimation(oldModel: UIModel, newModel: UIModel) {
        when (newModel) {
            is UIModel.Connecting,
            is UIModel.Disconnected,
            is UIModel.NoSignal -> {
                binding.ripple.fixAtFrame(0)
            }
        }

        val fromInsecure = !oldModel.isSecure()

        val unstableToSwitching = oldModel.isBadSignal() && newModel is UIModel.Switching
        val insecureToConnected = fromInsecure && newModel is UIModel.Connected

        val playEnterAnimation = insecureToConnected || unstableToSwitching
        val playEndAnimation = oldModel is UIModel.Connected &&
                (newModel is UIModel.Disconnecting || newModel is UIModel.Unstable)

        if (playEnterAnimation) {
            binding.ripple.playOnce(0, 74).then { loop(75, 120) }
        } else if (playEndAnimation) {
            binding.ripple.playOnce(binding.ripple.frame, 210)
        }
    }

    private fun UIModel.isBadSignal() = this is UIModel.NoSignal || this is UIModel.Unstable

    private fun UIModel.isSecure() = this.isBadSignal() ||
            this is UIModel.Connected ||
            this is UIModel.Switching

    private fun initHapticFeedback(oldModel: UIModel, newModel: UIModel) {
        when {
            oldModel is UIModel.Connecting && newModel is UIModel.Connected ||
            oldModel is UIModel.Disconnecting && newModel is UIModel.Disconnected -> {
                performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            }
        }
    }

    private fun applyStyle(style: UIModel.Styles) {
        binding.container.setBackgroundColor(context.color(style.bkgColorId))
        binding.title.setTextColor(context.color(style.titleColorId))
        binding.description.setTextColor(context.color(style.descriptionColorId))
        binding.duration.setTextColor(context.color(style.descriptionColorId))

        binding.switchBtn.alpha = style.switchAlpha

        elevation = context.resources.getDimensionPixelSize(style.bkgElevation).toFloat()
    }

    private fun switchSilently(isChecked: Boolean) {
        binding.switchBtn.setOnCheckedChangeListener(null)
        binding.switchBtn.isChecked = isChecked
        binding.switchBtn.setOnCheckedChangeListener(onCheckedChangedListener)
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

fun LottieAnimationView.fixAtFrame(frame: Int) {
    pauseAnimation()
    setMinAndMaxFrame(frame, frame)
    progress = 0f
}

fun LottieAnimationView.then(block: LottieAnimationView.() -> Unit) {
    addAnimatorListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
            removeAnimatorListener(this)
            block(this@then)
        }
    })
}
