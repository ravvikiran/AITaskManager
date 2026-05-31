# Google Play Store Publishing Guide — SmartTask AI

A complete checklist and best practices guide for publishing your Android app on the Google Play Store for the first time.

---

## Table of Contents

1. [Pre-Requisites (Before You Start)](#1-pre-requisites)
2. [Google Play Developer Account Setup](#2-developer-account-setup)
3. [App Preparation (Code & Build)](#3-app-preparation)
4. [Store Listing Assets](#4-store-listing-assets)
5. [Content Rating & Compliance](#5-content-rating--compliance)
6. [Pricing & Distribution](#6-pricing--distribution)
7. [In-App Purchases Setup](#7-in-app-purchases-setup)
8. [Testing Before Release](#8-testing-before-release)
9. [Release Process](#9-release-process)
10. [Post-Launch Checklist](#10-post-launch-checklist)
11. [Common Rejection Reasons & How to Avoid Them](#11-common-rejection-reasons)

---

## 1. Pre-Requisites

### What You Need
- [ ] A Google account (personal or business)
- [ ] A one-time registration fee of **$25 USD** for Google Play Developer account
- [ ] A valid credit/debit card for the registration fee
- [ ] A computer with Android Studio installed
- [ ] Your app's source code (ready to build a release APK/AAB)
- [ ] A privacy policy URL (hosted on a website — required for all apps)

### Recommended
- [ ] A business email (looks more professional than gmail)
- [ ] A simple website or landing page for your app
- [ ] A D-U-N-S number (if publishing as an organization — required since 2023)

---

## 2. Developer Account Setup

### Steps
1. Go to [Google Play Console](https://play.google.com/console)
2. Sign in with your Google account
3. Pay the one-time $25 registration fee
4. Fill in your developer profile:
   - Developer name (this is public — choose carefully)
   - Contact email (public)
   - Phone number
   - Website (optional but recommended)

### Identity Verification (New in 2023)
- **Personal accounts**: Government-issued ID verification required
- **Organization accounts**: D-U-N-S number + organization documents required
- Verification can take **3-7 business days**
- You cannot publish until verification is complete

### Important Notes
- Your developer name is permanent and public — pick something professional
- You need to verify your identity before your first app can go live
- Google requires a physical address for organization accounts

---

## 3. App Preparation

### Build Configuration

#### Signing Your App
```
Every app on Google Play MUST be signed with a release key.
```

1. **Generate a release keystore** (do this ONCE and keep it safe forever):
   ```
   keytool -genkey -v -keystore smarttaskai-release.keystore -alias smarttaskai -keyalg RSA -keysize 2048 -validity 10000
   ```

2. **Configure signing in `app/build.gradle.kts`**:
   ```kotlin
   android {
       signingConfigs {
           create("release") {
               storeFile = file("path/to/smarttaskai-release.keystore")
               storePassword = "your_store_password"
               keyAlias = "smarttaskai"
               keyPassword = "your_key_password"
           }
       }
       buildTypes {
           release {
               signingConfig = signingConfigs.getByName("release")
           }
       }
   }
   ```

3. **CRITICAL: Back up your keystore file and passwords**
   - If you lose your keystore, you can NEVER update your app again
   - Store it in a secure location (password manager, encrypted drive)
   - Consider using Google Play App Signing (recommended — Google holds a backup)

#### Build the Release Bundle
- Always use **AAB (Android App Bundle)** format, not APK
- Google Play requires AAB since August 2021
- Build via: `./gradlew bundleRelease`
- Output: `app/build/outputs/bundle/release/app-release.aab`

#### Version Management
```kotlin
defaultConfig {
    versionCode = 1        // Increment this for EVERY upload (integer)
    versionName = "1.0.0"  // User-facing version string
}
```
- `versionCode` must increase with every update — Google rejects same or lower
- Use semantic versioning for `versionName`: MAJOR.MINOR.PATCH

### Code Checklist
- [ ] Remove all debug logs (`Log.d`, `println`)
- [ ] Remove any hardcoded test data
- [ ] Ensure `isMinifyEnabled = true` in release build type
- [ ] Ensure `isShrinkResources = true` in release build type
- [ ] Test the release build on a real device (not just emulator)
- [ ] Verify ProGuard/R8 rules don't break anything
- [ ] Remove any test API keys or credentials
- [ ] Set `debuggable = false` (default for release builds)

---

## 4. Store Listing Assets

### Required Assets

| Asset | Specification | Notes |
|-------|--------------|-------|
| App Icon | 512 x 512 px, PNG, 32-bit | High-res, no transparency |
| Feature Graphic | 1024 x 500 px, PNG/JPEG | Shown at top of store listing |
| Screenshots (Phone) | Min 2, max 8. Min 320px, max 3840px | 16:9 or 9:16 ratio |
| Screenshots (Tablet) | Optional but recommended | 7" and 10" tablets |
| Short Description | Max 80 characters | Shown in search results |
| Full Description | Max 4000 characters | Detailed app description |

### Writing Your Store Listing

#### App Title (max 30 characters)
```
SmartTask AI - Productivity
```

#### Short Description (max 80 characters)
```
AI-powered task manager with smart scheduling, habits & focus timer.
```

#### Full Description Template
```
🧠 SmartTask AI — Your Intelligent Productivity Partner

Manage tasks, build habits, and stay focused with on-device AI that learns 
YOUR patterns. No cloud. No subscriptions required for core features. 
100% private.

✨ KEY FEATURES:

📋 SMART TASK MANAGEMENT
• Create tasks with priorities, categories, and sub-task checklists
• AI predicts how long tasks will take based on your history
• Auto-generated daily schedule optimized for your energy levels

🔥 HABIT TRACKING
• Build daily and weekly habits with visual streaks
• Get gentle reminders to stay consistent
• Watch your productivity score grow

⏱️ FOCUS MODE
• Built-in Pomodoro timer (15/25/45/60 min sessions)
• Runs in background — works even when you leave the app
• Track completed focus sessions

📊 ANALYTICS & INSIGHTS
• See your most productive hours
• Track completion rates and trends
• AI-generated productivity insights

🏆 SHARE YOUR SCORE
• Productivity scored 0-100 based on your activity
• Share with friends via email or social media
• Track your progress over time

🔒 PRIVACY FIRST
• All AI runs on-device — your data never leaves your phone
• No account required
• No internet needed for core features

⭐ PREMIUM (Optional)
• Unlimited habits
• Advanced AI scheduling
• Detailed analytics
• Custom focus intervals

Download now and take control of your productivity!
```

### Screenshot Tips
- Show the app in action (real content, not empty states)
- Add captions/text overlays explaining each feature
- Use a consistent style/branding across all screenshots
- First 2-3 screenshots are most important (shown in search)
- Consider using a tool like Figma, Canva, or AppMockUp

---

## 5. Content Rating & Compliance

### Content Rating Questionnaire
- Google requires you to fill out an IARC rating questionnaire
- Answer honestly — misrepresentation can get your app removed
- For SmartTask AI, the expected rating is: **Everyone (E)**
- Questions cover: violence, sexual content, language, substances, gambling

### Privacy Policy (REQUIRED)
- You MUST have a privacy policy URL
- Host it on your website, GitHub Pages, or a free hosting service
- Must cover:
  - What data you collect
  - How you use it
  - Whether you share it with third parties
  - How users can delete their data
  - Contact information

#### Privacy Policy Key Points for SmartTask AI:
```
- All data stored locally on device
- No data transmitted to external servers
- No third-party analytics SDKs
- Google Play Billing data handled by Google
- Users can delete all data by uninstalling the app
```

### Data Safety Section
Google Play requires a "Data Safety" declaration:
- [ ] Does your app collect user data? → Minimal (task data stored locally)
- [ ] Is data encrypted? → Yes (Room database on encrypted storage)
- [ ] Can users request data deletion? → Yes (uninstall or clear app data)
- [ ] Do you share data with third parties? → No (except Google Play Billing)

### Permissions Declaration
Explain why each permission is needed:
| Permission | Justification |
|-----------|---------------|
| POST_NOTIFICATIONS | Task reminders and habit nudges |
| FOREGROUND_SERVICE | Focus timer runs in background |
| VIBRATE | Notification alerts |
| RECEIVE_BOOT_COMPLETED | Reschedule reminders after device restart |

---

## 6. Pricing & Distribution

### Pricing Model
- **Free** with in-app purchases (freemium)
- You cannot change a free app to paid later
- In-app purchases require additional setup (see Section 7)

### Distribution
- [ ] Select countries (recommend: All countries initially)
- [ ] Select "This app is free" with in-app purchases
- [ ] Opt into Google Play App Signing (recommended)

### Target Audience
- [ ] Declare target age group (for SmartTask AI: 13+ or All ages)
- [ ] If targeting children under 13: additional requirements apply (COPPA)
- [ ] For SmartTask AI: declare "Not designed for children"

---

## 7. In-App Purchases Setup

### Google Play Billing Setup
1. In Play Console → Your App → Monetize → Products → Subscriptions
2. Create subscription products:
   - Product ID: `premium_monthly` (must match code exactly)
   - Price: $4.99/month
   - Product ID: `premium_yearly`
   - Price: $39.99/year

### Subscription Configuration
- Set a free trial period (optional, recommended: 7 days)
- Set grace period (recommended: 7 days for payment issues)
- Configure account hold (recommended: 30 days)

### Testing In-App Purchases
- Add test accounts in Play Console → Settings → License Testing
- Test accounts can make purchases without being charged
- ALWAYS test the full purchase flow before going live

---

## 8. Testing Before Release

### Internal Testing Track (Recommended First Step)
1. Play Console → Release → Testing → Internal testing
2. Upload your AAB
3. Add up to 100 testers by email
4. Testers get a private link to install
5. No review required — available within minutes

### Closed Testing (Alpha/Beta)
- Up to 2000 testers
- Requires Google review (1-3 days)
- Good for wider beta testing

### Open Testing
- Available to anyone who finds the listing
- Requires Google review
- Good for final validation before production

### Testing Checklist
- [ ] Test on at least 3 different devices/screen sizes
- [ ] Test on Android 8 (API 26) — your minimum SDK
- [ ] Test on latest Android version
- [ ] Test with no internet connection
- [ ] Test the full purchase flow with test accounts
- [ ] Test notifications (task reminders, habit nudges)
- [ ] Test the focus timer running in background
- [ ] Test app behavior after device restart
- [ ] Test with large amounts of data (50+ tasks, 20+ habits)
- [ ] Run the app through Android Vitals (no ANRs, no crashes)

---

## 9. Release Process

### Step-by-Step Production Release

1. **Create your app in Play Console**
   - Play Console → All apps → Create app
   - Fill in app name, language, app/game, free/paid

2. **Complete the Store Listing**
   - Main store listing (title, descriptions, screenshots)
   - Categorization (Category: Productivity, Tags: task manager, habits)

3. **Complete Content Rating**
   - Fill out the IARC questionnaire

4. **Complete Data Safety**
   - Declare what data your app collects and how it's used

5. **Set up Pricing & Distribution**
   - Countries, pricing model

6. **Upload your AAB to Production track**
   - Release → Production → Create new release
   - Upload the signed AAB file
   - Add release notes

7. **Submit for Review**
   - First review typically takes **3-7 days**
   - Subsequent updates: **1-3 days**

### Release Notes Template
```
Version 1.0.0 — Initial Release

🎉 Welcome to SmartTask AI!

• Smart task management with priorities and categories
• AI-powered duration prediction and daily scheduling
• Habit tracking with visual streaks
• Focus mode with Pomodoro timer
• Productivity analytics and insights
• Share your productivity score with friends

All AI features run on-device — your data stays private.
```

---

## 10. Post-Launch Checklist

### First Week
- [ ] Monitor Android Vitals (crash rate, ANR rate)
- [ ] Respond to user reviews (especially negative ones)
- [ ] Check install/uninstall metrics
- [ ] Verify in-app purchases are working in production
- [ ] Monitor revenue reports

### Ongoing
- [ ] Keep crash rate below 1.09% (Google's threshold)
- [ ] Keep ANR rate below 0.47%
- [ ] Respond to reviews within 24-48 hours
- [ ] Release updates regularly (at least monthly)
- [ ] Update target SDK when Google requires it (annual requirement)
- [ ] Monitor policy emails from Google — respond promptly

### ASO (App Store Optimization)
- [ ] Research keywords competitors use
- [ ] A/B test your store listing (Play Console has built-in experiments)
- [ ] Update screenshots seasonally
- [ ] Encourage happy users to leave reviews (but don't incentivize)
- [ ] Localize your listing for top markets

---

## 11. Common Rejection Reasons & How to Avoid Them

| Rejection Reason | How to Avoid |
|-----------------|--------------|
| Missing privacy policy | Host a privacy policy and link it in Play Console AND in-app |
| Misleading metadata | Don't use competitor names, don't exaggerate features |
| Broken functionality | Test thoroughly on multiple devices before submitting |
| Deceptive ads | If you add ads later, don't make them look like content |
| Insufficient permissions justification | Only request permissions you actually use |
| Impersonation | Don't use icons/names similar to popular apps |
| Repetitive content | Make sure your app offers unique value |
| Data safety mismatch | Ensure your data safety declaration matches actual behavior |
| Target API level too low | Must target the latest required API level |
| Missing content rating | Complete the IARC questionnaire |

### Google's Review Timeline
- First submission: 3-7 days (can be longer for new accounts)
- Updates: 1-3 days typically
- If rejected: Fix the issue and resubmit (goes back in queue)
- Appeals: Use the Play Console appeal form if you disagree

---

## Quick Reference: File Checklist

Before uploading, make sure you have:

```
✅ Signed release AAB file (app-release.aab)
✅ App icon (512x512 PNG)
✅ Feature graphic (1024x500 PNG)
✅ At least 2 phone screenshots
✅ Short description (80 chars)
✅ Full description (4000 chars)
✅ Privacy policy URL
✅ Content rating questionnaire completed
✅ Data safety form completed
✅ In-app products created (if applicable)
✅ Release notes written
```

---

## Useful Links

- [Google Play Console](https://play.google.com/console)
- [Play Console Help](https://support.google.com/googleplay/android-developer)
- [Developer Policy Center](https://play.google.com/about/developer-content-policy/)
- [App Signing by Google Play](https://developer.android.com/studio/publish/app-signing)
- [Android App Bundle Guide](https://developer.android.com/guide/app-bundle)
- [Store Listing Best Practices](https://developer.android.com/distribute/best-practices/launch/store-listing)
- [Pre-launch Report](https://developer.android.com/distribute/best-practices/launch/pre-launch-report)

---

## Timeline Estimate (First-Time Publisher)

| Step | Duration |
|------|----------|
| Developer account setup + verification | 3-7 days |
| Prepare store assets (screenshots, descriptions) | 1-2 days |
| Build & test release AAB | 1 day |
| Fill out Play Console forms | 1-2 hours |
| Internal testing | 2-3 days |
| Submit for production review | 3-7 days |
| **Total (first time)** | **~2-3 weeks** |

---

*Good luck with your launch! 🚀*
