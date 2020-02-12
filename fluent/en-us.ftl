## Application
application-name = Firefox Private Network
application-description = 
   A fast, secure and easy to use VPN 
   (Virtual Private Network).

## Navigation
navigation-close = Close
navigation-back = Back
navigation-open-link = Open link
navigation-open-contact-link = Open Contact us link
navigation-open-support-link = Open Help & Support link
navigation-open-terms-of-service = Open Terms of Service link
navigation-open-privacy-policy = Open Privacy Policy link
navigation-open-debug = Open Debug link
navigation-open-log = Open View log link

## Pages
connection-page-title = Connection
devices-page-title = My devices
devices-page-subtitle = {$numUserDevices} of {$numMaxDevices}
settings-page-title = Settings
get-help-page-title = Get help
about-page-title = About
feedback-page-title = Give feedback
language-page-title = Language
notifications-page-title = Notifications

## Urls
terms-service-url-title = Terms of Service
privacy-policy-url-title = Privacy Policy
contact-url-title = Contact us
help-support-url-title = Help & Support
subscribe-url-title = Subscribe now

## Main View
hero-text-vpn-off = VPN is off
hero-text-vpn-on = VPN is on
hero-text-connecting = Connecting...
hero-text-disconnecting = Disconnecting...
hero-text-switching = Switching...
hero-image-vpn-status = VPN status
dev-mode-on = Dev mode on
toggle-vpn = Toggle VPN
decoration-image = Decoration
shadow-image = Shadow
connection-navigation-button = Connection navigation
vpn-status-image = VPN status

hero-subtext-unstable = Unstable
hero-subtext-no-signal = No signal
hero-subtext-turn-on = Turn it on to protect your entire device
hero-subtext-secure-protected = Secure and protected
hero-subtext-check-connection = Check connection
hero-subtext-protected-shortly = You will be protected shortly
hero-subtext-disconnected-shortly = You will be disconnected shortly
hero-subtext-server-switch = From {$currentServer} to {$switchServer}
hero-subtext-server-switch-from = From
hero-subtext-server-switch-to = to

## Avatar Menu
avatar-menu-settings = {settings-page-title}
avatar-menu-manage-account = Manage account
avatar-menu-view-log = View Log
avatar-menu-feedback = Give feedback
avatar-menu-debug = Debug
avatar-menu-sign-out = Sign out

## Tray
tray-connected = Connected
tray-disconnected = Disconnected
tray-unstable = Unstable
tray-no-signal = {hero-subtext-no-signal}

## Tray menu
tray-menu-exit = E_xit
tray-menu-show = _Show
tray-menu-hide = _Hide

## Settings
settings-auto-launch = Launch VPN app on computer startup
settings-unsecured-network-title = Unsecured network alert
settings-unsecured-network-content = Get notified if you connect to an unsecured Wi-Fi network
settings-local-device-access-title = Allow access to your local network
settings-local-device-access-content = Access printers, streaming sticks and all other devices on your local network
settings-local-device-access-checked-disabled-message = VPN must be off before disabling
settings-local-device-access-unchecked-disabled-message = VPN must be off before enabling
settings-captive-portal-title = Guest Wi-Fi portal alert
settings-captive-portal-content = Get notified if a guest Wi-Fi portal is blocked due to VPN connection

## About
about-version-title = Release version

## Language
language-default-title = Default
language-additional-title = Additional

## Connection
connection-connecting = {hero-text-connecting}
connection-disconnecting = {hero-text-disconnecting}
connection-switching = {hero-text-switching}

## Devices
devices-current-device = Current device
devices-add-date-days = {$numDays ->
    [0] Added today
	[1] Added yesterday
   *[other] Added {$numDays} days ago
}
devices-add-date-months = {$numMonths ->
    [1] Added {$numMonths} month ago
   *[other] Added {$numMonths} months ago
}
devices-add-date-years = {$numYears ->
    [1] Added {$numYears} year ago
   *[other] Added {$numYears} year ago
}

devices-of = of
devices-page-description = Devices with {application-name} installed using your account. Connect up to {$numMaxDevices} devices.
devices-limit-reached-title = Remove a device
devices-limit-reached-content = You've reached your limit. To install the VPN on this device, you'll need to remove one.
devices-remove-popup-title = Remove device?
devices-remove-popup-content = Please confirm you would like to remove {$deviceName}.
devices-remove = Remove device

## Popup
popup-cancel-button-text = Cancel
popup-remove-button-text = Remove
popup-ok-button-text = Ok

## Beta Announcement
beta-announcement-title = Hang tight for initial VPN connection
beta-announcement-content-1 = It may take a few minutes to connect you to our global network of {$numServers}+ servers.  Future connections will be much faster.
beta-announcement-content-2 = If you're still not connected after 5 minutes, disconnect and try again.
beta-announcement-ok-button-text = Okay, got it

## Landing
landing-title = {application-name}
landing-content = {application-description}
landing-signin-button-text = Get started
landing-not-subscriber-text = Not a subscriber?

## Verify Account
verify-account-title = Waiting for sign in and subscription confirmation...
verify-account-something-wrong = Something wrong?
verify-account-try-again = Cancel and try again

## Update
update-title = Security Patch
update-content-1 = You must update your software to continue using {application-name}
update-content-2 = Your connection will not be secure while you update.
update-update-button-text = Update now
update-update-failed = Update failed
update-update-started = Update started
update-update-failed-georestricted = Updates are only available for users located in the US

## New User Experience
nux-title-1 = No activity logs
nux-content-1 = We're Mozilla. We don't log your activity and we're always on your side.
nux-title-2 = Device level encryption
nux-content-2 = No one will see your location or activity, even on unsecure Wi-Fi networks.
nux-title-3 = Servers in 39 countries
nux-content-3 = Stand up to tech bullies and protect your access to the web.
nux-title-4 = Connect up to 5 devices
nux-content-4 = Stream, download and game. We won't restrict your bandwidth.
nux-title-5 = Quick access
nux-content-5-1 = You can quickly access {application-name} from your taskbar tray
nux-content-5-2 = Located next to the clock at the bottom right of your screen
nux-learn-more = Learn more
nux-skip = Skip
nux-continue = Continue
nux-next = Next

## Feedback
feedback-upset = I'm upset
feedback-confused = I'm confused
feedback-happy = I'm happy
feedback-note-placeholder = Write a note...
feedback-submit-button-text = Submit feedback

## Windows notifications
windows-notification-error-title = {application-name} ran into an error
windows-notification-notice-title = Notice
windows-notification-warning-title = Warning
windows-notification-vpn-on-title = {hero-text-vpn-on}
windows-notification-vpn-on-content = You're secure and protected.
windows-notification-vpn-off-title = {hero-text-vpn-off}
windows-notification-vpn-off-content = You disconnected.
windows-notification-vpn-switch-title = From {$currentServer} to {$switchServer}
windows-notification-vpn-switch-content = You switched servers.
windows-notification-vpn-turn-on-title = Turn on VPN?
windows-notification-vpn-turn-on-content = Connect to your last location: {$serverCity}.
windows-notification-unsecure-network-title = Unsecured Wi-Fi network detected
windows-notification-unsecure-network-content = "{$wifiName}" is not secure. Turn on VPN to secure your device.
windows-notification-captive-portal-blocked-title = Guest Wi-Fi portal blocked
windows-notification-captive-portal-blocked-content = The guest Wi-Fi network you're connected to requires action. Click to turn off VPN to see the portal.
windows-notification-captive-portal-detected-title = Guest Wi-Fi portal detected
windows-notification-captive-portal-detected-content = "{$wifiName}" may not be secure. Click to turn on VPN to secure your device.

## In-app toasts
toast-debug-export-error = We ran into an error while exporting your debug information.
toast-add-device-error = Could not add new device
toast-remove-device-error = Could not remove device
toast-remove-current-device-error = Could not remove remove device - it is currently in use
toast-remove-device-success = Successfully removed device
toast-login-url-retrieval-error = Could not retrieve login URLs
toast-user-acess-control-error = You need to allow {application-name} to continue by clicking "Yes"
toast-vpn-launch-error = We couldn't launch your VPN right now, try again in a few moments
toast-vpn-start-error = Couldn't start VPN
toast-driver-missing-error = Driver is missing, connection failed
toast-firewall-rules-error = Error creating firewall rules
toast-load-configuration-error = Error loading configuration file for VPN
toast-open-log-file-error = Can't open log file for writing
toast-update-version-message-1 = New features are available!{" "}
toast-update-version-message-2 = {update-update-button-text}
toast-no-subscription = No active subscription.{" "}
toast-unable-to-connect = Unable to connect.{" "}
toast-try-again = Try again
toast-feedback-submitted = Feedback submitted!{" "}
toast-feedback-undo = Undo
toast-service-communication-error = Unable to communicate with the {application-name} background service. Click to restart.
toast-service-restart-error = We couldn't restart the background service. Please repair your installation of {application-name}.
toast-service-restart-success = The {application-name} background service has been restored.

## ViewLog window
viewlog-save-button = Save
viewlog-timestamp-header = Timestamp
viewlog-message-header = Message
viewlog-copy-menuitem = Copy
viewlog-select-all-menuitem = Select all
viewlog-save-to-file-menuitem = Save to file
viewlog-save-dialog-title = Save log
viewlog-save-dialog-filter = Text Log Files
viewlog-window-title = Log
viewlog-save-error = There was a problem saving the log file
