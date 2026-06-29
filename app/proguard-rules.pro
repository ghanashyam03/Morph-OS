# JNI Keep Rules for AI Module (llama.cpp JNI wrappers)
-keep class com.morphos.app.core.ai.** { *; }
-keepclasseswithmembernames class com.morphos.app.core.ai.** {
    native <methods>;
}

# ONNX Runtime keep rules
-keep class ai.onnxruntime.** { *; }

# Room Keep Rules
-keep class * extends androidx.room.RoomDatabase
-keep class * implements androidx.room.RoomDatabase$Callback
-keep @androidx.room.Entity class * { *; }
-keep interface * extends androidx.room.RoomDatabase$* { *; }
-keep class * extends androidx.room.SharedSQLiteStatement { *; }

# kotlinx.serialization
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keep class kotlinx.serialization.json.** { *; }

# Hilt Keep Rules
-keep public class * extends android.app.Service
-keep public class * extends android.app.Application
-keep public class * extends android.app.Activity
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# DataStore Protobuf
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }

# OkHttp & Retrofit
-keepattributes Signature, InnerClasses, AnnotationDefault, EnclosingMethod
-keepclassmembers class * {
    @retrofit2.http.** <methods>;
}
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# Glance AppWidget rules
-keep class * extends androidx.glance.appwidget.GlanceAppWidget { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }
