-keepattributes Signature, *Annotation*, SourceFile, LineNumberTable

# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keep @kotlinx.serialization.Serializable class com.zoti321.c2cmarket.data.remote.dto.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class com.zoti321.c2cmarket.data.local.entity.** { *; }

# Hilt Workers
-keep class * extends androidx.work.Worker
-keep @androidx.hilt.work.HiltWorker class * { *; }

# Coil
-dontwarn coil3.**

# Credential Manager + Google Sign-In
-dontwarn androidx.credentials.**
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
