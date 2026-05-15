
<img width="1600" height="900" alt="Hero" src="https://github.com/user-attachments/assets/990668dd-b8f0-4608-8201-5eb557c7f05c" />
# Mane-Kelsa

**Mane-Kelsa** is a Kannada-first Android app for small towns and tier-3 cities that helps residents find nearby household workers like cleaners, gardeners, and helpers, while enabling workers to mark daily availability and get discovered in real time.
<img width="1600" height="900" alt="Architecture" src="https://github.com/user-attachments/assets/c53c3cbf-ba46-4bbc-8ef1-f85c07e39751" />
## Features

- OTP-based mobile login
- Worker and customer role flows
- Nearby worker discovery
- Daily availability updates
- Call and lightweight chat flow
- Offline-first local cache
- Firebase-ready backend integration
- Kannada-first accessible UI

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- MVVM + Clean Architecture
- Hilt
- Coroutines + Flow
- Navigation Compose
- Room
- DataStore
- WorkManager
- Paging 3
- Firebase Auth
- Firestore
- Realtime Database
- Firebase Storage
- Crashlytics
- Analytics
- FCM
<img width="1400" height="900" alt="Customer Home" src="https://github.com/user-attachments/assets/8e9ca38f-711f-4721-b9e1-3dce39571db0" />
## Architecture

The app follows a **modular Clean Architecture** structure:

- `app`
- `core:common`
- `core:designsystem`
- `core:ui`
- `core:navigation`
- `core:network`
- `core:database`
- `core:datastore`
- `core:testing`
- `feature:auth`
- `feature:onboarding`
- `feature:customer-home`
- `feature:search`
- `feature:worker-detail`
- `feature:worker-profile`
- `feature:customer-profile`
- `feature:notifications`
- `feature:ratings`
- `feature:chat`
<img width="1400" height="900" alt="Customer Home" src="https://github.com/user-attachments/assets/d8010cc7-1c44-43ad-9325-fa603b2c60bd" />
## UX Focus

Designed for:

- Kannada-first users
- Semi-literate users
- Elderly users
- Low-end Android devices

## Build

```bash
./gradlew :app:assembleDebug
./gradlew test
<img width="1600" height="900" alt="Hero" src="https://github.com/user-attachments/assets/990668dd-b8f0-4608-8201-5eb557c7f05c" />


