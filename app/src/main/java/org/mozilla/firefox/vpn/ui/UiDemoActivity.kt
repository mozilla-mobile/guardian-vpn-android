package org.mozilla.firefox.vpn.ui

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import org.mozilla.firefox.vpn.databinding.ActivityUiDemoBinding
import org.mozilla.firefox.vpn.util.StringResource

class UiDemoActivity : Activity() {

    private lateinit var binding: ActivityUiDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUiDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.container.addView(InAppNotificationView.inflate(
            this,
            InAppNotificationView.Config.warning(StringResource("Red"))
        ))

        addDivider()

        binding.container.addView(
            InAppNotificationView.inflate(
                this,
                InAppNotificationView.Config
                    .warning(StringResource("Red"))
                    .action(StringResource("action")) {
                        Toast.makeText(this, "Action", Toast.LENGTH_SHORT).show()
                    }
                    .close {
                        Toast.makeText(this, "Close", Toast.LENGTH_SHORT).show()
                    }
            ))

        addDivider()

        binding.container.addView(
            InAppNotificationView.inflate(
                this,
                InAppNotificationView.Config(InAppNotificationView.Style.Green, StringResource("Green"))
            ))

        addDivider()

        binding.container.addView(
            InAppNotificationView.inflate(
            this,
            InAppNotificationView.Config(InAppNotificationView.Style.Green, StringResource("Green"))
                .action(StringResource("action")) {
                    Toast.makeText(this, "Action", Toast.LENGTH_SHORT).show()
                }
                .close {
                    Toast.makeText(this, "Close", Toast.LENGTH_SHORT).show()
                }
        ))

        addDivider()

        binding.container.addView(InAppNotificationView.inflate(
            this,
            InAppNotificationView.Config(InAppNotificationView.Style.Blue, StringResource("Blue"))
        ))

        addDivider()

        binding.container.addView(InAppNotificationView.inflate(
            this,
            InAppNotificationView.Config(InAppNotificationView.Style.Blue, StringResource("Blue"))
                .action(StringResource("action")) {
                    Toast.makeText(this, "Action", Toast.LENGTH_SHORT).show()
                }
                .close {
                    Toast.makeText(this, "Close", Toast.LENGTH_SHORT).show()
                }
        ))
    }

    private fun addDivider() {
        binding.container.addView(
            View(this),
            ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2).apply {
                topMargin = 10
                bottomMargin = 10
            }
        )
    }
}
