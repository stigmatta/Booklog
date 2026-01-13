# 1. Keep your Data Models and Entities strictly
# This prevents R8 from stripping constructor parameters needed by Room/GSON/Firestore
-keep @androidx.room.Entity class *
-keep class site.odintsov.booklog.data.** { *; }

# 2. Critical for Firestore Mapping
# Firestore needs the setters/getters and the empty constructor to remain intact
-keepattributes Signature, Exceptions, *Annotation*
-keepclassmembers class site.odintsov.booklog.data.** {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
    public <init>(...);
}

# 3. Retrofit / GSON specifics
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# 4. Retrofit & GSON: Ensure JSON mapping doesn't break
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# 5. General Kotlin: Handle the 'no members match' issue by being less specific
-keepnames class kotlinx.coroutines.** { *; }