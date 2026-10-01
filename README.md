# Wi-Fi RSSI Localization

Android teaching app using three Wi-Fi access points for RSSI-based localization.

## Features
- Reads Android Wi-Fi RSSI in dBm
- Matches three anchors by BSSID
- Converts RSSI to estimated distance
- 2D trilateration
- Displays anchor range circles and estimated phone position
- GitHub Actions builds a downloadable debug APK

## Anchor coordinates
Coordinates are physical positions in **metres** relative to an origin chosen for the room.

Example (Configured for Robotics Lab):
- AP1 = (0, 0)
- AP2 = (3, 0)
- AP3 = (0, 3)

Edit the anchors in `MainActivity.kt`.

## APK
Open **Actions → Build Android APK**, run the workflow, then download the `wifi-localization-apk` artifact.

## Calibration
The log-distance model is `d = 10^((A-RSSI)/(10n))`. Calibrate RSSI at 1 metre (A) and path-loss exponent (n) for the actual environment.
