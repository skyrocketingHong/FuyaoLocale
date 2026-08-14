-dontobfuscate

# Shizuku starts this service by the class name carried in UserServiceArgs.
# It is not declared in the manifest, so R8 cannot infer the reflective entry point.
-keep class ing.fuyaoskyrocket.applocale.service.UserService {
    <init>();
    *;
}

# Keep the AIDL contract used across the Shizuku user-service Binder boundary.
-keep class ing.fuyaoskyrocket.applocale.IUserService { *; }
-keep class ing.fuyaoskyrocket.applocale.IUserService$* { *; }
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
