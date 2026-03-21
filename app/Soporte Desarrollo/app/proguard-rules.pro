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

# Preserve line numbers in stack traces for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---------------------------------------------------------------------------
# Clover SDK — uses reflection heavily; keep all public/protected members
# ---------------------------------------------------------------------------
-keep class com.clover.sdk.** { *; }

# ---------------------------------------------------------------------------
# Gson — keep model classes that are serialized/deserialized
# ---------------------------------------------------------------------------
-keep class com.aleph.nudge.model.** { *; }

# ---------------------------------------------------------------------------
# OkHttp
# ---------------------------------------------------------------------------
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# ---------------------------------------------------------------------------
# BuildConfig
# ---------------------------------------------------------------------------
-keep class com.aleph.nudge.BuildConfig { *; }

# ---------------------------------------------------------------------------
# UpsellHistoryManager inner class (accessed reflectively via Gson)
# ---------------------------------------------------------------------------
-keep class com.aleph.nudge.data.UpsellHistoryManager$PairStats { *; }

# ---------------------------------------------------------------------------
# Sentry crash reporting
# ---------------------------------------------------------------------------
-keep class io.sentry.** { *; }