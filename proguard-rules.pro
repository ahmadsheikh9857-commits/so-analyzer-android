# Keep all public classes and methods
-keep public class * {
    public *;
}

# Keep Kotlin metadata
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# Keep AndroidX
-keep class androidx.** { *; }

# Keep Material Design
-keep class com.google.android.material.** { *; }

# Keep our app classes
-keep class com.example.soanalyzer.** { *; }

# Optimization
-optimizationpasses 5
-dontusemixedcaseclassnames
