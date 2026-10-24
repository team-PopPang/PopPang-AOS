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

# Gson reads API DTO fields through reflection. Preserve their wire names and
# generic types instead of keeping the entire app or third-party SDKs.
-keepattributes Signature
-keep class com.poppang.PopPang.model.** { *; }

# Keep the encrypted session JSON compatible across optimized app updates.
-keep class com.poppang.PopPang.network.StoredSession { *; }
