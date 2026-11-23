# ProGuard and R8 rules for SafeGuard / Smart SOS App

# Keep data models serialized to JSON (PreferencesManager)
-keep class com.example.myapplication.model.** { *; }
