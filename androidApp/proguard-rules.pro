# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep WorkManager classes
-keep class androidx.work.** { *; }
-keep class androidx.work.impl.** { *; }
-keep class androidx.work.impl.background.systemjob.** { *; }
-keep class androidx.work.impl.background.systemalarm.** { *; }

# Keep WorkDatabase and related classes
-keep class androidx.work.impl.model.** { *; }
-keep class androidx.room.** { *; }

# Keep all WorkManager initializers
-keep class androidx.work.WorkManagerInitializer { *; }

# Keep WorkManager's database schemas
-keep class * extends androidx.room.RoomDatabase { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static final class *;
}

# Keep Glance state and serialization
-keep class androidx.glance.** { *; }
-keep class androidx.glance.appwidget.** { *; }
-keepclassmembers class * extends androidx.glance.GlanceAppWidget { *; }
-keepclassmembers class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }