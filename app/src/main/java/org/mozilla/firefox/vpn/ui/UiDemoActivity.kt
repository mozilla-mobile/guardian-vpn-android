package org.mozilla.firefox.vpn.ui;

import android.app.Activity;
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_ui_demo.*

import org.mozilla.firefox.vpn.R;
import org.mozilla.firefox.vpn.util.StringResource

class UiDemoActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ui_demo)

        container.addView(InAppNotificationView.inflate(
            this,
            InAppNotificationView.Config.warning(StringResource("Red"))
        ))

        addDivider()

        container.addView(
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

        container.addView(
            InAppNotificationView.inflate(
                this,
                InAppNotificationView.Config(InAppNotificationView.Style.Green, StringResource("Green"))
            ))

        addDivider()

        container.addView(
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

        container.addView(InAppNotificationView.inflate(
            this,
            InAppNotificationView.Config(InAppNotificationView.Style.Blue, StringResource("Blue"))
        ))

        addDivider()

        container.addView(InAppNotificationView.inflate(
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
        container.addView(
            View(this),
            ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2).apply {
                topMargin = 10
                bottomMargin = 10
            }
        )
    }
}
