# Nyxtra ProGuard rules
#
# The app does not use Room; the previous androidx.room keep rule was dead
# configuration. These rules protect what actually matters: the gomobile/JNI
# bridge, which resolves classes and methods by name at runtime.

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses

# gomobile generated bindings: called reflectively from Go.
-keep class go.** { *; }
-keep class io.nekohasekai.libbox.** { *; }
-keepclassmembers class io.nekohasekai.libbox.** { *; }

# Keep native method declarations and anything implementing a JNI interface.
-keepclasseswithmembernames class * {
    native <methods>;
}

# Gson serialises the persisted models by field name; renaming breaks stored
# profiles, engine config and per-app selection after an update.
-keep class com.nyxtra.vpn.data.model.** { *; }
-keepclassmembers class com.nyxtra.vpn.data.model.** {
    <fields>;
    <init>(...);
}

# ZXing embedded scanner is launched via reflection by the library.
-keep class com.journeyapps.barcodescanner.** { *; }
-keep class com.google.zxing.** { *; }
