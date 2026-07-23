# Add project specific ProGuard rules here.
# Keep Room Entities and DAOs
-keep class com.example.data.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.paging.**

# Keep Kotlin Serialization and Moshi Data Models
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,LineNumberTable
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}

# Keep WorkManager and Worker Classes
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Keep Firebase and Gemini AI
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.ai.client.generativeai.** { *; }

# Obfuscate private classes and helper logic while preserving entry points
-repackageclasses 'a'
-allowaccessmodification
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

