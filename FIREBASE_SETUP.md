# Firebase Authentication Setup Guide for Quick Loan App

This document guides you through setting up real Firebase Authentication in the Google Firebase Console for the Quick Loan Android application (`com.aistudio.quickloan.qklx`).

---

## 1. Firebase Project Setup

1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Click **Add project** (or select an existing Google Cloud project).
3. Name your project (e.g. `quick-loan-prod`).
4. (Optional) Enable Google Analytics and click **Create Project**.

---

## 2. Register Android Application

1. In the Project Overview, click the **Android icon** to add an Android app.
2. Enter the **Android package name**:
   ```
   com.aistudio.quickloan.qklx
   ```
   *(Note: This matches the `applicationId` defined in `app/build.gradle.kts`)*.
3. App nickname (optional): `Quick Loan Android`
4. **Debug signing certificate SHA-1**:
   - For Phone Authentication & SafetyNet/reCAPTCHA verification, add your SHA-1 fingerprint.
   - You can get the SHA-1 of the debug keystore using:
     ```bash
     keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
     ```
5. Click **Register app**.
6. Download the generated `google-services.json` file.
7. Replace the template file at `/app/google-services.json` with your real downloaded file.

---

## 3. Enable Sign-In Providers

In the Firebase Console sidebar, go to **Build** > **Authentication** > **Sign-in method**:

### A. Phone Number Authentication (Customer Login)
1. Click **Phone** in the sign-in providers list.
2. Toggle **Enable**.
3. Under **Phone numbers for testing (optional)**, add test numbers for automated testing and emulator usage without using SMS quotas:
   - Phone number: `+91 9876543210`
   - Verification code: `123456`
   - Phone number: `+91 9831122334` (Agent Rahul Sharma test number)
   - Verification code: `123456`
4. Click **Save**.

### B. Email/Password Provider (Admin & Agent Login)
1. In the Sign-in method list, click **Email/Password**.
2. Toggle **Email/Password** to **Enable**.
3. Click **Save**.

---

## 4. Setting up Roles & Permissions

The app implements strict **Role-Based Access Control (RBAC)** across three roles:

### 1. Operations Admin (`ADMIN`)
- **No hardcoded password in the codebase.**
- Go to Firebase Console > **Authentication** > **Users** tab.
- Click **Add user**.
- Add the Admin email:
  - **Email**: `admin@quickloan.in` (or your organizational admin email)
  - **Password**: Create a strong password (minimum 8 characters with symbols and digits).
- The app checks for authorized Admin email addresses or Firebase custom claims (`role == "admin"`). Normal customer users or agent accounts are blocked with an "Access Denied" barrier if they attempt to access admin endpoints.

### 2. Loan Assistance Agents (`AGENT`)
- Register agent accounts in Firebase Auth Users:
  - Example: `rahul@quickloan.in`, `priya@quickloan.in`.
- Agents can also be registered directly by the Admin in the Admin Console ("Agents" tab).
- Once logged in, an agent **only sees and manages applications assigned directly to their `agentId`**.

### 3. Customers (`CUSTOMER`)
- Log in seamlessly via **Phone Number + OTP** or optional **Email Login**.
- Customer data is strictly isolated to their own mobile number / UID.

---

## 5. Testing the App

- **Live Firebase Mode**: If a valid `google-services.json` is present and phone/email is registered in Firebase, the app uses real Firebase Authentication.
- **Demo Mode**: The app includes a built-in Demo Mode toggle with pre-configured accounts (Customer, Agent, Admin) so you can test all features and workflows without requiring external network connectivity or burning SMS credits during review.
