## Application
application-name = Firefox Private Network
application-description = 
   Ein schnelles, sicheres und praktisches VPN
   (Virtuelles Privates Netzwerk).

## Navigation
navigation-close = Close
navigation-back = Back
navigation-open-link
navigation-open-contact-link
navigation-open-support-link
navigation-open-terms-of-service
navigation-open-privacy-policy
navigation-open-debug
navigation-open-log

## Pages
connection-page-title = Verbindungen
devices-page-title = Meine Geräte
devices-page-subtitle = {$numUserDevices} von {$numMaxDevices}
settings-page-title = Einstellungen
get-help-page-title = Hilfe
about-page-title = Über
feedback-page-title = Feedback
language-page-title = Sprache
notifications-page-title = Mitteilungen

## Urls
terms-service-url-title = Nutzungsbedingungen
privacy-policy-url-title = Datenschutz
contact-url-title = Kontakt
help-support-url-title = Hilfe & Support
subscribe-url-title = Jetzt anmelden

## Main View
hero-text-vpn-off = VPN ist aus
hero-text-vpn-on = VPN ist an
hero-text-connecting = Verbinden...
hero-text-disconnecting = Trennen...
hero-text-switching = Wechselt...
hero-image-vpn-status
dev-mode-on
toggle-vpn
decoration-image
shadow-image
connection-navigation-button
vpn-status-image

hero-subtext-unstable = Instabil
hero-subtext-no-signal = Kein Signal
hero-subtext-turn-on = Anschalten um das gesamte Gerät zu schützen
hero-subtext-secure-protected = Sicher und geschützt
hero-subtext-check-connection = Verbindung überprüfen
hero-subtext-protected-shortly = Du bist bald geschützt
hero-subtext-disconnected-shortly = Die Verbindung wird unterbrochen
hero-subtext-server-switch = Von {$currentServer} zu {$switchServer}
hero-subtext-server-switch-from = Von
hero-subtext-server-switch-to = zu

## Avatar Menu
avatar-menu-settings = {settings-page-title}
avatar-menu-manage-account = Account verwalten
avatar-menu-view-log = Log anzeigen
avatar-menu-feedback = Feedback geben
avatar-menu-debug = Debug
avatar-menu-sign-out = Abmelden

## Tray
tray-connected = Verbunden
tray-disconnected = Getrennt
tray-unstable = Instabil
tray-no-signal = {hero-subtext-no-signal}

## Tray menu
tray-menu-exit = Be_enden
tray-menu-show = _Anzeigen
tray-menu-hide = A_usblenden

## Settings
settings-auto-launch = VPN beim Hochfahren des Computers starten
settings-unsecured-network-title = Alarm für unsichere Verbindungen
settings-unsecured-network-content = Benachrichtigungen bei unsicheren Wlan-Verbindungen
settings-local-device-access-title = Zugriff zum lokalen Netzwerk erlauben
settings-local-device-access-content = Zugriff auf Drucker, Streaming und alle anderen Geräte im lokalen Netzwerk
settings-local-device-access-checked-disabled-message = VPN muss vor der Deaktivierung ausgeschaltet werden
settings-local-device-access-unchecked-disabled-message = VPN muss vor der Aktivierung ausgeschaltet werden
settings-captive-portal-title = Guest Wi-Fi portal alert
settings-captive-portal-content = Get notified if a guest Wi-Fi portal is blocked due to VPN connection

## About
about-version-title = Version

## Language
language-default-title = Standard
language-additional-title = Zusätzlich

## Connection
connection-connecting = {hero-text-connecting}
connection-disconnecting = {hero-text-disconnecting}
connection-switching = {hero-text-switching}

## Devices
devices-current-device = Aktuelles Gerät
devices-add-date-days = {$numDays ->
    [0] Heute hinzugefügt
	[1] Gestern hinzugefügt
   *[other] Vor {$numDays} Tagen hinzugefügt
}
devices-add-date-months = {$numMonths ->
    [1] Vor {$numMonths} Monat hinzugefügt
   *[other] Vor {$numMonths} Monaten hinzugefügt
}
devices-add-date-years = {$numYears ->
    [1] Vor {$numYears} Jahr hinzugefügt
   *[other] Vor {$numYears} Jahren hinzugefügt
}

devices-of = von
devices-page-description = Geräte mit {application-name} installiert benutzen deinen Account. Verbinde bis zu {$numMaxDevices} Geräte.
devices-limit-reached-title = Gerätelimit erreicht
devices-limit-reached-content = Limit erreicht. Um den VPN auf diesem Gerät zu installieren, entferne zunächst ein andere.
devices-remove-popup-title = Gerät entfernen?
devices-remove-popup-content = Bitte bestätige das Entfernen von {$deviceName}.
devices-remove = Gerät entfernen

## Popup
popup-cancel-button-text = Abbrechen
popup-remove-button-text = Entfernen
popup-ok-button-text = Ok

## Beta Announcement
beta-announcement-title = Die erste VPN Verbindung wird hergestellt
beta-announcement-content-1 = Es kann einige Minuted dauern, um eine Verbindung des weltweiten Netzwerks unserer {$numServers}+ Server herzustellen.  Zukünftige Verbindungen werden schneller sein.
beta-announcement-content-2 = Wenn die Verbindung nach 5 Minuten noch nicht hergestellt wurde, bitte abbrechen und erneut versuchen.
beta-announcement-ok-button-text = Okay

## Landing
landing-title = {application-name}
landing-content = {application-description}
landing-signin-button-text = Anmelden
landing-not-subscriber-text = Noch nicht angemeldet?

## Verify Account
verify-account-title = Die Anmeldung wird bestätigt...
verify-account-something-wrong = Irgendetwas falsch?
verify-account-try-again = Abbrechen und nochmal versuchen

## Update
update-title = Sicherheitsupdate
update-content-1 = Update {application-name}, um es weiter zu verwenden
update-content-2 = Die Verbindung ist während des Updateprozesses nicht sicher.
update-update-button-text = Update starten
update-update-failed-georestricted = Updates are only available for users located in the US
update-update-failed = Update fehler
update-update-started = Update starten

## New User Experience
nux-title-1 = Kein Aktivitätsprotokoll vorhanden
nux-content-1 = Wir sind Mozilla. Wir speichern deine Aktivitäten nie.
nux-title-2 = Verschlüsselung auf Geräteebene
nux-content-2 = Niemand wird Ihren Standort oder Ihre Aktivität sehen, auch nicht in unsicheren Wi-Fi-Netzwerken.
nux-title-3 = Server in 39 Ländern
nux-content-3 =  Schützen Sie Ihren Zugang zum Internet, indem Sie sich gegen Datenkraken wehren.
nux-title-4 = Bis zu 5 Geräte verbinden
nux-content-4 = Streaming, Downloads und Spiele. Wir werden die Bandbreite nicht beschränken.
nux-title-5 = Schnellzugriff
nux-content-5-1 = Du kannst auf {application-name} schnell von der Taskleiste zugreifen
nux-content-5-2 = Neben der Uhr unten rechts auf Ihrem Bildschirm.
nux-learn-more = Weitere Informationen
nux-skip = Überspringen
nux-continue = Weiter
nux-next = Weiter

## Feedback
feedback-upset = Ich bin enttäuscht
feedback-confused = Ich bin verwirrt
feedback-happy = Ich bin zufrieden
feedback-note-placeholder = Nachricht schreiben...
feedback-submit-button-text = Absenden

## Windows notifications
windows-notification-error-title = {application-name} hat einen Fehler verursacht
windows-notification-notice-title = Meldung
windows-notification-warning-title = Warnung
windows-notification-vpn-on-title = {hero-text-vpn-on}
windows-notification-vpn-on-content = Sicher und geschützt
windows-notification-vpn-off-title = {hero-text-vpn-off}
windows-notification-vpn-off-content = Verbindung getrennt
windows-notification-vpn-switch-title = Von {$currentServer} zu {$switchServer} gewechselt
windows-notification-vpn-switch-content = {windows-notification-vpn-on-title}
windows-notification-vpn-turn-on-title = VPN anschalten?
windows-notification-vpn-turn-on-content = Wir verbinden dich mit deinem letzten Standort: {$serverCity}
windows-notification-unsecure-network-title = Unsicheres Netzwerk erkannt
windows-notification-unsecure-network-content = "{$wifiName}" ist nicht sicher. Du solltest Dein VPN einschalten.
windows-notification-captive-portal-blocked-title = Guest Wi-Fi portal blocked
windows-notification-captive-portal-blocked-content = The guest Wi-Fi network you're connected to requires action. Click to turn off VPN to see the portal.
windows-notification-captive-portal-detected-title = Guest Wi-Fi portal detected
windows-notification-captive-portal-detected-content = {$networkName} may not be secure. Click to turn on VPN to secure your device.

## In-app toasts
toast-debug-export-error = Wir sind beim Export Ihrer Debug-Informationen auf einen Fehler gestoßen.
toast-add-device-error = Neues Gerät konnte nicht hinzugefügt werden
toast-remove-device-error = Gerät konnte nicht entfernt werden
toast-remove-current-device-error = Gerät konnte nicht entfernt werden - es wird gerade verwendet.
toast-remove-device-success = Gerät erfolgreich entfernt
toast-login-url-retrieval-error = Login-URLs konnten nicht abgerufen werden.
toast-user-acess-control-error = Du musst zulassen, dass {application-name} fortgesetzt wird, indem Du auf "Ja" klickst.
toast-vpn-launch-error = Wir konnten den VPN nicht starten, versuche es in wenigen Augenblicken erneut.
toast-vpn-start-error = VPN konnte nicht gestartet werden
toast-driver-missing-error = Treiber fehlt, Verbindung fehlgeschlagen
toast-firewall-rules-error = Fehler bei der Erstellung von Firewall-Regeln
toast-load-configuration-error = Fehler beim Laden der Konfigurationsdatei für VPN
toast-open-log-file-error = Die Protokolldatei hat keine Schreibberechtigung.
toast-update-version-message-1 = Neue Funktionen sind verfügbar!{" "}
toast-update-version-message-2 = {update-update-button-text}
toast-no-subscription = Kein aktives Abonnement.{" "}
toast-unable-to-connect = Verbindung kann nicht hergestellt werden.{" "}
toast-try-again = Nochmal versuchen
toast-feedback-submitted = Feedback abgeschickt!{" "}
toast-feedback-undo = Rückgängig
toast-service-communication-error = Unable to communicate with the {application-name} background service. Click to restart.
toast-service-restart-error = We couldn't restart the background service. Please repair your installation of {application-name}.
toast-service-restart-success = The {application-name} background service has been restored.

## ViewLog window
viewlog-save-button = Speichern
viewlog-timestamp-header = Uhrzeit
viewlog-message-header = Nachricht
viewlog-copy-menuitem = Kopieren
viewlog-select-all-menuitem = Alles auswählen
viewlog-save-to-file-menuitem = In Datei speichern
viewlog-save-dialog-title = Protokoll speichern
viewlog-save-dialog-filter = Textprotokolldateien
viewlog-window-title = Protokolldatei
viewlog-save-error = Es gab ein Problem beim Speichern der Protokolldatei
