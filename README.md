<img width="1600" height="900" alt="Architecture" src="https://github.com/user-attachments/assets/c53c3cbf-ba46-4bbc-8ef1-f85c07e39751" />
<img width="1400" height="900" alt="Customer Home" src="https://github.com/user-attachments/assets/8e9ca38f-711f-4721-b9e1-3dce39571db0" />
<img width="1400" height="900" alt="Customer Home" src="https://github.com/user-attachments/assets/d8010cc7-1c44-43ad-9325-fa603b2c60bd" />
1. Product Vision
In many small towns, finding local household help is still handled informally through word of mouth, neighbors, or phone contacts. This creates friction for both sides:

workers struggle to be discovered consistently
residents do not know who is available nearby right now
there is little transparency around trust, profile quality, or daily availability
digital products often ignore Kannada-first and low-end device users
Mane-Kelsa solves this by creating a simple, accessible, and trustworthy local discovery system where:

workers can mark themselves available every day
customers can find nearby workers instantly
communication happens through familiar channels like phone and lightweight chat
trust signals such as verification, ratings, and moderation can scale over time
2. Key Objectives
Build a modern Android application from scratch using current Android best practices
Support three roles: Customer, Worker, and backend-managed Admin
Enable real-time worker discovery with Firebase-backed availability
Keep the UI simple, bold, accessible, and Kannada-first
Design the codebase for production growth, not just demo output
Support local development even when Firebase secrets are not yet configured
3. Core User Roles
Customer
discover nearby workers
search by skill, area, and availability
call or chat with workers
view worker details and trust signals
receive notifications and alerts
save preferences and manage profile settings
Worker
log in using mobile OTP
complete guided onboarding
upload profile and service details
mark daily availability
update profile and work areas
receive customer contact through call or chat
track approval and trust state
Admin
not exposed as a consumer Android UI in v1
supported through Firebase-first backend operations
verify workers
moderate reports
block abusive accounts
manage trust, review, and platform safety data
4. Major Features
Authentication and session
phone number login
OTP verification
session persistence
role selection
auto routing by session state
blocked-user restriction flow
Worker onboarding
step-based registration
name, skills, rate, and area setup
review-based worker visibility
approval state tracking
Customer marketplace
nearby worker listing
area-based discovery
availability-first home feed
worker detail page
call and chat entry points
trust and rating surfaces
Realtime and offline
Firestore for listing and structured app data
Realtime Database for presence and fast availability signaling
Room for local cache
DataStore for app/session preferences
local mock mode for development without Firebase config
Reliability and safety
centralized app errors
startup bootstrap checks
typed navigation model
role-aware route handling
Firebase security rules
App Check-ready initialization path
5. Technology Stack
Android
Kotlin
Jetpack Compose
Material 3
MVVM
Clean Architecture
Navigation Compose
Hilt
Coroutines + Flow
Room
DataStore
WorkManager
Paging 3
Coil
Timber
Backend and cloud
Firebase Authentication
Firebase Firestore
Firebase Realtime Database
Firebase Storage
Firebase Analytics
Firebase Crashlytics
Firebase Performance Monitoring
Firebase App Check
Firebase Cloud Messaging
6. Architecture Overview
Mane-Kelsa follows a modular Clean Architecture approach to keep the app scalable, testable, and easy to maintain.

Architectural layers
Presentation Layer
Compose screens
route composables
ViewModels
immutable UI state
UI effects such as navigation and external actions
Domain Layer
business rules
role-aware routing decisions
use cases
validation logic
ranking/trust abstractions
Data Layer
Firebase data sources
Room cache
DataStore preferences
local mock data support
repository implementations
Architectural principles
screens stay declarative
ViewModels own state
repositories own data orchestration
navigation is typed and centralized
side effects are routed through coordinators, not embedded directly in UI widgets
7. Module Structure
app
core:common
core:designsystem
core:ui
core:navigation
core:network
core:database
core:datastore
core:testing
feature:auth
feature:onboarding
feature:customer-home
feature:search
feature:worker-detail
feature:worker-profile
feature:customer-profile
feature:notifications
feature:ratings
feature:chat
Module responsibilities
app
App entry point, bootstrap, top-level wiring, runtime environment handling

core:common
Shared models, session routing, result wrappers, error model, environment definitions

core:designsystem
Color palette, typography, theme system, design tokens

core:ui
Reusable scaffolds, loading patterns, shared UI building blocks

core:navigation
Typed destinations, app graph, route guards, bootstrap routing

core:network
Connectivity monitoring and future remote infrastructure

core:database
Room cache and local persistence

core:datastore
Session and lightweight preference persistence

core:testing
Shared test dependencies and test utilities

feature:*
User-facing feature flows and screen logic

8. UX and Accessibility Strategy
Mane-Kelsa is intentionally designed for:

semi-literate users
elderly users
low-end Android devices
Kannada-first audiences
UX principles
large tap targets
high contrast
minimal clutter
simple top-level navigation
icon-assisted understanding
clear CTA hierarchy
predictable back behavior
explicit loading, empty, and error states
Accessibility principles
readable typography
localized labels
touch-friendly controls
support for font scaling
role-safe routing
persistent offline messaging
error copy that explains what happened and what to do next
9. Routing and App Flow
The application uses a bootstrap-first navigation model instead of direct startup side effects.

Startup flow
App opens into a bootstrap/splash route
Session state is restored
Network and environment state are checked
Firebase availability is evaluated
Role and account status are resolved
User is routed once to the correct graph
Route outcomes
no session -> Welcome
logged in but no role -> Role Selection
worker without completed profile -> Worker Onboarding
approved or pending worker -> Worker Profile
blocked user -> Unauthorized
customer -> Customer Home
Route quality improvements included
typed destinations instead of raw route strings
worker detail routes accept workerId
chat and ratings accept workerId
navigation guards for worker-only surfaces
safer back stack behavior after role selection and bootstrap
10. Error Handling Strategy
The project includes a structured application error model instead of ad hoc failures.

Current error categories
network unavailable
Firebase config missing
unauthorized
blocked account
OTP expired
not found
rate limited
validation error
unknown error
Error handling goals
consistent error contracts from repositories to UI
retry-friendly design where appropriate
stable error codes
user-readable messages
future analytics classification support
UX error patterns
inline field validation for login and OTP
scaffold-level status banners
clear local-mode messaging when Firebase config is absent
ability to extend into top-level snackbar/effect mediation
11. Local Development and Environment Modes
Mane-Kelsa is designed to support more than one runtime mode.

Supported modes
localMock
no google-services.json required
Firebase plugins are not forced
suitable for UI and architecture development
can run with local placeholder/mock data
debugFirebase
uses debug Firebase config
suitable for integration testing and feature validation
release
production configuration
Firebase services enabled
App Check and monitoring expected
This setup prevents local development from failing immediately just because cloud secrets are not yet available.

12. Firebase Integration
The app is structured for Firebase-backed production deployment with safe local fallback.

Services expected
Authentication
Firestore
Realtime Database
Storage
Messaging
Analytics
Crashlytics
Performance Monitoring
App Check
Important configuration files
firestore.rules
storage.rules
database.rules.json
firebase.json
.firebaserc
Security goals
ownership-based write access
admin-only moderation paths
separated public and sensitive storage buckets
restricted blocked-account behavior
role-aware access rules
13. Current Stabilization Improvements Added
This repository has already been improved beyond the initial scaffold in several critical ways:

local debug no longer strictly depends on google-services.json
Firebase initialization is guarded
navigation was upgraded to typed destinations
bootstrap routing replaced startup navigate(...) side effects
route contracts now support argument-based navigation
localization files were added for major user flows
corrupted hardcoded text was replaced with string resources
direct UI-triggered dial logic was moved out of pure screen rendering
shared error structures were expanded for better future handling
Gradle stabilization flags were added to reduce noisy environment issues during recovery
14. Setup Guide
Prerequisites
Android Studio
Android SDK 35
JDK 17
Gradle wrapper
Firebase project if using cloud-backed mode
Local setup
Clone the repository
Open the project in Android Studio
Ensure JDK 17 is configured
Sync Gradle
Run local mock mode even if Firebase config is absent
Firebase-enabled setup
Create or use a Firebase project
Register Android application ID
Add SHA-1 and SHA-256 keys
Download google-services.json
Place it in:
app/src/debug/google-services.json for debug Firebase
app/src/release/google-services.json for release
Enable Firebase services used by the app
Deploy security rules
15. Build and Run
Debug build
./gradlew :app:assembleDebug
Clean build
./gradlew clean :app:assembleDebug
Unit tests
./gradlew test
Notes
local mock mode is intended to help the app compile and run without production secrets
full Firebase-backed behavior requires proper network access and project configuration
16. Testing Strategy
The app is intended to be validated across multiple levels:

Unit tests
session route resolution
validation logic
bootstrap decisions
error mapping
UI tests
splash routing
login flow
OTP validation
role selection
onboarding navigation
customer home rendering
Integration tests
Firebase-backed auth
listing sync
presence handling
role guards
notification triggers
Manual QA priorities
Kannada text rendering
low-end device performance
offline mode messaging
back stack behavior
blocked-account routing
17. CI/CD Readiness
The repository includes CI/CD groundwork with GitHub Actions support.

Recommended pipeline responsibilities:

Gradle wrapper validation
debug assembly
unit test execution
lint and static checks
staging/release automation
18. Known Constraints
At the current stage, the codebase is a strong architecture and product scaffold, but some flows are still intentionally incomplete.

Present limitations
several feature screens still use placeholder content
repository-backed marketplace data is not fully wired yet
chat, search, and ratings are structurally routed but not production-complete
real Firebase feature behavior depends on environment configuration
build verification may fail in restricted environments without Maven/Google repository access
These are normal at this stage and do not reduce the value of the architecture foundation.

19. Future Roadmap
Near-term
connect feature screens to repositories
complete OTP integration
implement live availability synchronization
add proper paging and search queries
finish notification token registration
expand UI tests and repository tests
Mid-term
worker verification pipeline
report abuse flows
ranking logic
offline queue sync
app analytics dashboards
Long-term
advanced moderation tools
web admin panel
deeper trust scoring
richer worker profile experience
district/town level scale-out
20. Why This Project Stands Out
Mane-Kelsa is not positioned as a generic demo app. It is designed as a serious, socially relevant, and technically scalable product with:

meaningful local problem solving
accessible Kannada-first product thinking
modular Android engineering
real-time marketplace foundations
production-minded security and architecture choices
support for both developer-friendly local mode and Firebase-backed deployment
It combines product design, architecture, accessibility, and real-world platform thinking in a single Android system.

21. Project Status
Status: Active architecture foundation and stabilization complete
Platform: Android
Primary language: Kotlin
UI framework: Jetpack Compose
Architecture: MVVM + Clean Architecture + Modularization
Backend: Firebase + local mock fallback

22. Authoring Standard
This repository is intended to be presented as a professional-grade Android project. The codebase and documentation are being shaped to reflect:

engineering clarity
product maturity
accessible UX thinking
modular architecture discipline
production-readiness direction
If evaluated as a capstone, portfolio, client demo, or architecture submission, the project should communicate both technical competence and user-centered design quality.<img width="1600" height="900" alt="Hero" src="https://github.com/user-attachments/assets/990668dd-b8f0-4608-8201-5eb557c7f05c" />


