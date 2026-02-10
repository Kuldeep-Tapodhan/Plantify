# 🌿 Plantify - Plant Disease Detection App

**Plantify** is an Android mobile application that uses **Machine Learning (TensorFlow Lite)** to detect plant diseases from leaf images. The app helps farmers and gardening enthusiasts identify diseases early and provides treatment recommendations to maintain healthy crops.

---

## 📋 Table of Contents

- [Problem Statement](#-problem-statement)
- [Project Goals](#-project-goals)
- [Key Features](#-key-features)
- [Technology Stack](#-technology-stack)
- [Project Architecture](#-project-architecture)
- [System Workflow](#-system-workflow)
- [Folder Structure](#-folder-structure)
- [Prerequisites](#-prerequisites)
- [Installation & Setup](#-installation--setup)
- [Configuration](#-configuration)
- [How to Run](#-how-to-run)
- [Usage Instructions](#-usage-instructions)
- [Future Improvements](#-future-improvements)
- [Contributing](#-contributing)

---

## 🎯 Problem Statement

Plant diseases cause significant crop losses worldwide, affecting food security and farmer livelihoods. Early detection is crucial but often requires expert knowledge that may not be readily available to all farmers. Manual inspection is time-consuming and prone to errors.

**Plantify** addresses this challenge by providing an accessible, AI-powered solution that enables anyone to quickly identify plant diseases using just their smartphone camera.

---

## 🚀 Project Goals

- **Early Disease Detection**: Enable users to identify plant diseases at early stages
- **Accessibility**: Provide an easy-to-use mobile interface for farmers and gardeners
- **Treatment Guidance**: Offer actionable treatment recommendations for detected diseases
- **History Tracking**: Maintain scan history for monitoring plant health over time
- **Weather Integration**: Provide weather information to help users understand environmental factors
- **Multi-language Support**: Support multiple languages (English, Hindi, Gujarati, French)

---

## ✨ Key Features

### 🔍 Disease Detection
- **AI-Powered Classification**: Uses TensorFlow Lite model to identify 38 different plant diseases
- **High Accuracy**: Trained on a comprehensive dataset of plant leaf images
- **Confidence Scoring**: Displays confidence percentage for each detection

### 📸 Image Capture Options
- **Camera Integration**: Capture plant images directly using device camera
- **Gallery Upload**: Upload existing images from device gallery
- **Secure File Handling**: Uses FileProvider for secure image storage

### 💊 Treatment Recommendations
- **Disease-Specific Guidance**: Provides pesticide recommendations and treatment steps
- **Step-by-Step Instructions**: Clear, actionable treatment guidelines
- **Default Fallback**: General treatment advice for unrecognized diseases

### 📊 Scan History
- **Firebase Integration**: Stores scan history in Firebase Realtime Database
- **Recent Scans Display**: Shows last 3 scans on home screen
- **Full History Access**: View complete scan history with timestamps

### 🌤️ Weather Information
- **Location-Based Weather**: Fetches current weather using device location
- **OpenWeather API**: Real-time temperature and weather conditions
- **Environmental Context**: Helps understand disease-favorable conditions

### 👤 User Management
- **Firebase Authentication**: Secure email/password authentication
- **User Profiles**: Editable user profiles with profile pictures
- **Firebase Storage**: Cloud storage for user profile images

### 🌐 Multi-language Support
- English (default)
- Hindi (हिंदी)
- Gujarati (ગુજરાતી)
- French (Français)

---

## 🛠️ Technology Stack

### **Frontend**
- **Language**: Kotlin
- **UI Framework**: Android XML Layouts with View Binding
- **Navigation**: Android Navigation Component
- **Material Design**: Material Design Components

### **Backend & Services**
- **Authentication**: Firebase Authentication
- **Database**: Firebase Realtime Database
- **Storage**: Firebase Cloud Storage
- **Weather API**: OpenWeather API

### **Machine Learning**
- **Framework**: TensorFlow Lite
- **Model**: Custom-trained plant disease classification model
- **Input Size**: 224x224 pixels
- **Classes**: 38 plant disease categories

### **Networking**
- **HTTP Client**: Retrofit 2
- **JSON Parsing**: Gson Converter
- **Image Loading**: Glide

### **Location Services**
- Google Play Services Location

### **Build System**
- Gradle (Kotlin DSL)
- Minimum SDK: 26 (Android 8.0)
- Target SDK: 34 (Android 14)

---

## 🏗️ Project Architecture

Plantify follows a **modular architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────────┐
│           Presentation Layer                │
│  (Activities, Fragments, Adapters)          │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│              Data Layer                     │
│  (Models, Repositories, Network Services)   │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│          External Services                  │
│  (Firebase, TensorFlow Lite, Weather API)   │
└─────────────────────────────────────────────┘
```

### **Architecture Components**

1. **UI Layer** (`ui/`)
   - **Fragments**: Home, Profile, History, Result
   - **Activities**: Login, Signup, EditProfile, MainActivity
   - **Adapters**: RecyclerView adapters for lists

2. **Data Layer** (`data/`)
   - **Models**: Data classes for Scan, HistoryItem, WeatherResponse
   - **Repositories**: TreatmentRepository for disease treatment data
   - **Network**: Retrofit service interfaces

3. **ML Layer** (`ml/`)
   - **DiseaseClassifier**: TensorFlow Lite model wrapper
   - **Model Assets**: tflite_model.tflite, labels.txt

4. **Utilities** (`utils/`)
   - Helper classes and extension functions

---

## 🔄 System Workflow

### **1. User Authentication Flow**
```
┌──────────┐     ┌──────────┐     ┌──────────┐
│  Login   │────▶│ Firebase │────▶│   Main   │
│ Activity │     │   Auth   │     │ Activity │
└──────────┘     └──────────┘     └──────────┘
     │
     ▼
┌──────────┐
│  Signup  │
│ Activity │
└──────────┘
```

### **2. Disease Detection Flow**
```
┌────────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│   Home     │────▶│  Camera/ │────▶│   ML     │────▶│  Result  │
│  Fragment  │     │  Gallery │     │ Classify │     │ Fragment │
└────────────┘     └──────────┘     └──────────┘     └──────────┘
                                          │
                                          ▼
                                    ┌──────────┐
                                    │ Firebase │
                                    │ Database │
                                    └──────────┘
```

### **3. Detailed Detection Process**

1. **Image Capture**
   - User taps "Scan" button on Home screen
   - Chooses between Camera or Gallery
   - Image is captured/selected

2. **Preprocessing**
   - Image is resized to 224x224 pixels
   - Normalized to [-1, 1] range
   - Converted to TensorFlow Lite format

3. **Classification**
   - TensorFlow Lite model processes the image
   - Returns probability distribution across 38 classes
   - Highest probability class is selected

4. **Result Display**
   - Disease name and confidence percentage shown
   - Treatment recommendations fetched from TreatmentRepository
   - Scan saved to Firebase with timestamp

5. **History Update**
   - Scan added to user's history in Firebase
   - Recent scans updated on Home screen

---

## 📁 Folder Structure

```
Plantify-master/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/plantify/
│   │   │   │   ├── ui/                          # UI Components
│   │   │   │   │   ├── home/
│   │   │   │   │   │   └── HomeFragment.kt      # Main screen with scan & weather
│   │   │   │   │   ├── result/
│   │   │   │   │   │   └── ResultFragment.kt    # Disease detection results
│   │   │   │   │   ├── history/
│   │   │   │   │   │   └── HistoryFragment.kt   # Scan history list
│   │   │   │   │   ├── profile/
│   │   │   │   │   │   └── ProfileFragment.kt   # User profile
│   │   │   │   │   └── adapters/
│   │   │   │   │       └── HistoryAdapter.kt    # RecyclerView adapter
│   │   │   │   │
│   │   │   │   ├── data/                        # Data Layer
│   │   │   │   │   ├── network/
│   │   │   │   │   │   ├── WeatherApiService.kt # Weather API interface
│   │   │   │   │   │   └── RetrofitInstance.kt  # Retrofit configuration
│   │   │   │   │   ├── TreatmentData.kt         # Disease treatment repository
│   │   │   │   │   ├── HistoryItem.kt           # Scan history model
│   │   │   │   │   ├── Scan.kt                  # Scan data model
│   │   │   │   │   └── WeatherResponse.kt       # Weather API response model
│   │   │   │   │
│   │   │   │   ├── ml/                          # Machine Learning
│   │   │   │   │   └── DiseaseClassifier.kt     # TensorFlow Lite wrapper
│   │   │   │   │
│   │   │   │   ├── utils/                       # Utilities
│   │   │   │   │
│   │   │   │   ├── MainActivity.kt              # Main container activity
│   │   │   │   ├── LoginActivity.kt             # Login screen
│   │   │   │   ├── SignupActivity.kt            # Registration screen
│   │   │   │   └── EditProfileActivity.kt       # Profile editing
│   │   │   │
│   │   │   ├── res/                             # Resources
│   │   │   │   ├── layout/                      # XML layouts
│   │   │   │   ├── drawable/                    # Images & icons
│   │   │   │   ├── navigation/                  # Navigation graph
│   │   │   │   ├── menu/                        # Bottom navigation menu
│   │   │   │   ├── values/                      # Strings, colors, themes
│   │   │   │   ├── values-hi/                   # Hindi translations
│   │   │   │   ├── values-gu/                   # Gujarati translations
│   │   │   │   └── values-fr/                   # French translations
│   │   │   │
│   │   │   ├── assets/                          # ML Model Assets
│   │   │   │   ├── tflite_model.tflite          # TensorFlow Lite model
│   │   │   │   └── labels.txt                   # Disease class labels
│   │   │   │
│   │   │   └── AndroidManifest.xml              # App configuration
│   │   │
│   │   ├── androidTest/                         # Instrumented tests
│   │   └── test/                                # Unit tests
│   │
│   ├── build.gradle.kts                         # App-level Gradle config
│   ├── google-services.json                     # Firebase configuration
│   └── proguard-rules.pro                       # ProGuard rules
│
├── gradle/                                      # Gradle wrapper
├── build.gradle.kts                             # Project-level Gradle config
├── settings.gradle.kts                          # Gradle settings
├── gradle.properties                            # Gradle properties
└── README.md                                    # This file
```

### **Key Files Explained**

| File/Folder | Purpose |
|------------|---------|
| `DiseaseClassifier.kt` | Loads and runs TensorFlow Lite model for disease detection |
| `HomeFragment.kt` | Main screen with scan button, weather info, and recent scans |
| `ResultFragment.kt` | Displays detection results and treatment recommendations |
| `TreatmentData.kt` | Contains disease treatment information repository |
| `WeatherApiService.kt` | Retrofit interface for OpenWeather API |
| `tflite_model.tflite` | Trained TensorFlow Lite model (not included in repo) |
| `labels.txt` | List of 38 plant disease classes |
| `google-services.json` | Firebase project configuration |

---

## 📋 Prerequisites

Before setting up Plantify, ensure you have:

### **Development Environment**
- **Android Studio**: Arctic Fox (2020.3.1) or later
- **JDK**: Java Development Kit 8 or higher
- **Gradle**: 7.0+ (included with Android Studio)

### **Android Device/Emulator**
- **Minimum SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 14 (API 34)
- **Camera**: Required for image capture
- **Internet**: Required for Firebase and Weather API

### **External Services**
- **Firebase Account**: For authentication, database, and storage
- **OpenWeather API Key**: For weather data (free tier available)

### **Machine Learning Model**
- **TensorFlow Lite Model**: `tflite_model.tflite` (needs to be added to `app/src/main/assets/`)

---

## 🔧 Installation & Setup

### **Step 1: Clone the Repository**

```bash
git clone https://github.com/yourusername/Plantify.git
cd Plantify-master
```

### **Step 2: Set Up Firebase**

1. **Create a Firebase Project**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Click "Add Project" and follow the setup wizard

2. **Add Android App to Firebase**
   - Click "Add App" → Select Android
   - Package name: `com.example.plantify`
   - Download `google-services.json`
   - Place it in `app/` directory

3. **Enable Firebase Services**
   - **Authentication**: Enable Email/Password sign-in
   - **Realtime Database**: Create database in test mode
   - **Storage**: Enable Firebase Storage

4. **Configure Database Rules** (for development)
   ```json
   {
     "rules": {
       "users": {
         "$uid": {
           ".read": "$uid === auth.uid",
           ".write": "$uid === auth.uid"
         }
       },
       "history": {
         "$uid": {
           ".read": "$uid === auth.uid",
           ".write": "$uid === auth.uid"
         }
       }
     }
   }
   ```

### **Step 3: Get OpenWeather API Key**

1. Sign up at [OpenWeather](https://openweathermap.org/api)
2. Get your free API key
3. Open `app/src/main/java/com/example/plantify/ui/home/HomeFragment.kt`
4. Replace the API key on line 50:
   ```kotlin
   private val WEATHER_API_KEY = "YOUR_API_KEY_HERE"
   ```

### **Step 4: Add TensorFlow Lite Model**

1. **Option A**: Train your own model
   - Use PlantVillage dataset or similar
   - Train using TensorFlow/Keras
   - Convert to TensorFlow Lite format

2. **Option B**: Use a pre-trained model
   - Download a compatible plant disease detection model
   - Ensure it outputs 38 classes matching `labels.txt`

3. Place `tflite_model.tflite` in `app/src/main/assets/`

### **Step 5: Install Dependencies**

Open the project in Android Studio and sync Gradle:

```bash
# Android Studio will automatically download dependencies
# Or run from terminal:
./gradlew build
```

---

## ⚙️ Configuration

### **Firebase Configuration**

Ensure `google-services.json` is properly placed in the `app/` directory. The file should contain:
- Project ID
- Application ID: `com.example.plantify`
- API keys for Firebase services

### **Weather API Configuration**

Update the API key in `HomeFragment.kt`:

```kotlin
private val WEATHER_API_KEY = "your_openweather_api_key"
```

### **Permissions**

The app requires the following permissions (already configured in `AndroidManifest.xml`):

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

### **Build Configuration**

Key configurations in `app/build.gradle.kts`:

```kotlin
android {
    namespace = "com.example.plantify"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.example.plantify"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    
    buildFeatures {
        viewBinding = true
    }
}
```

---

## ▶️ How to Run

### **Using Android Studio**

1. **Open Project**
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to `Plantify-master` folder

2. **Sync Gradle**
   - Wait for Gradle sync to complete
   - Resolve any dependency issues

3. **Connect Device or Start Emulator**
   - **Physical Device**: Enable USB debugging and connect
   - **Emulator**: Create/start an AVD (Android Virtual Device)

4. **Run the App**
   - Click the "Run" button (▶️) or press `Shift + F10`
   - Select your target device
   - Wait for build and installation

### **Using Command Line**

```bash
# Build the APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Build and run
./gradlew installDebug
adb shell am start -n com.example.plantify/.LoginActivity
```

### **Generate Release APK**

```bash
# Build release APK
./gradlew assembleRelease

# APK location: app/build/outputs/apk/release/app-release-unsigned.apk
```

---

## 📱 Usage Instructions

### **1. First-Time Setup**

1. **Launch the App**
   - App opens to Login screen

2. **Create an Account**
   - Tap "Sign Up"
   - Enter name, email, and password (min 6 characters)
   - Tap "Sign Up" button
   - Account is created and you're logged in

### **2. Scanning a Plant**

1. **Navigate to Home**
   - After login, you're on the Home screen
   - See weather information and recent scans

2. **Start a Scan**
   - Tap the "Scan" button (camera icon)
   - Choose image source:
     - **Use Camera**: Take a photo of the plant leaf
     - **Upload from Gallery**: Select existing image

3. **Capture Image**
   - For camera: Point at leaf and capture
   - Ensure good lighting and clear focus
   - Leaf should fill most of the frame

4. **View Results**
   - App processes image (takes 1-3 seconds)
   - Result screen shows:
     - Disease name
     - Confidence percentage
     - Recommended pesticide
     - Treatment steps

5. **Save to History**
   - Scan is automatically saved to your history
   - View recent scans on Home screen

### **3. Viewing History**

1. **Navigate to History Tab**
   - Tap "History" in bottom navigation

2. **Browse Scans**
   - See all previous scans with timestamps
   - Tap any scan to view details

### **4. Managing Profile**

1. **Navigate to Profile Tab**
   - Tap "Profile" in bottom navigation

2. **Edit Profile**
   - Tap "Edit Profile" button
   - Update name or profile picture
   - Save changes

3. **Logout**
   - Tap "Logout" button
   - Returns to Login screen

### **5. Understanding Results**

**Confidence Score**:
- **>80%**: High confidence, result is very reliable
- **60-80%**: Moderate confidence, result is likely correct
- **<60%**: Low confidence, consider retaking image

**Treatment Recommendations**:
- Follow the step-by-step guide
- Use recommended pesticides as directed
- Consult agricultural expert for severe cases

---

## 🔮 Future Improvements

### **Planned Features**

- [ ] **Offline Mode**: Cache model and enable offline disease detection
- [ ] **Community Forum**: Allow users to share experiences and tips
- [ ] **Expert Consultation**: Connect with agricultural experts
- [ ] **Disease Progression Tracking**: Monitor plant health over time
- [ ] **Crop Calendar**: Planting and harvesting reminders
- [ ] **Fertilizer Recommendations**: Soil-based fertilizer suggestions
- [ ] **Pest Detection**: Expand to detect pests in addition to diseases
- [ ] **AR Visualization**: Augmented reality for treatment instructions
- [ ] **Voice Commands**: Voice-based navigation and scanning
- [ ] **Multi-Plant Support**: Detect multiple plants in one image

### **Technical Enhancements**

- [ ] **Model Optimization**: Reduce model size and improve inference speed
- [ ] **MVVM Architecture**: Migrate to ViewModel and LiveData
- [ ] **Jetpack Compose**: Modernize UI with Compose
- [ ] **Room Database**: Add local caching for offline support
- [ ] **Dependency Injection**: Implement Hilt/Dagger
- [ ] **Unit Tests**: Comprehensive test coverage
- [ ] **CI/CD Pipeline**: Automated testing and deployment
- [ ] **Analytics**: Firebase Analytics for user behavior insights
- [ ] **Crash Reporting**: Firebase Crashlytics integration
- [ ] **Performance Monitoring**: Optimize app performance

### **User Experience**

- [ ] **Onboarding Tutorial**: Guide new users through features
- [ ] **Dark Mode**: Complete dark theme support
- [ ] **Accessibility**: Screen reader and accessibility improvements
- [ ] **More Languages**: Add support for regional languages
- [ ] **Push Notifications**: Reminders and disease alerts
- [ ] **Social Sharing**: Share results on social media

---

## 🤝 Contributing

We welcome contributions to Plantify! Here's how you can help:

### **How to Contribute**

1. **Fork the Repository**
   ```bash
   git clone https://github.com/yourusername/Plantify.git
   cd Plantify-master
   git checkout -b feature/your-feature-name
   ```

2. **Make Your Changes**
   - Follow Kotlin coding conventions
   - Add comments for complex logic
   - Update documentation if needed

3. **Test Your Changes**
   - Test on multiple devices/emulators
   - Ensure no regressions
   - Add unit tests if applicable

4. **Submit a Pull Request**
   - Push your branch to GitHub
   - Create a Pull Request with clear description
   - Reference any related issues

### **Contribution Guidelines**

- **Code Style**: Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- **Commit Messages**: Use clear, descriptive commit messages
- **Documentation**: Update README and code comments
- **Testing**: Ensure all tests pass before submitting
- **Issues**: Check existing issues before creating new ones

### **Areas for Contribution**

- 🐛 Bug fixes
- ✨ New features
- 📝 Documentation improvements
- 🌐 Translations
- 🎨 UI/UX enhancements
- 🧪 Test coverage
- ♿ Accessibility improvements

---

## 🙏 Acknowledgments

- **PlantVillage Dataset**: For plant disease images
- **TensorFlow Team**: For TensorFlow Lite framework
- **Firebase**: For backend services
- **OpenWeather**: For weather API
- **Material Design**: For UI components
- **Android Community**: For libraries and support

---

