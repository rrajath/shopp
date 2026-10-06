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

# Keep Glance widget classes
-keep class com.rrajath.shopp.widget.ShoppWidgetReceiver
-keep class com.rrajath.shopp.widget.ShoppWidget

# Glance instantiates ActionCallbacks reflectively via their no-arg
# constructor. Its consumer rule keeps the class but not the constructor, so
# R8 full mode strips it and widget item taps silently fail in release.
-keep class * extends androidx.glance.appwidget.action.ActionCallback {
    <init>();
}