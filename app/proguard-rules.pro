# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in E:\Applications\ADT\adt-bundle-windows-x86_64-20140702\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keep class com.flurry.** { *; }
-dontwarn com.flurry.**
-keepattributes *Annotation*,EnclosingMethod
-keepclasseswithmembers class * {
public <init>(android.content.Context, android.util.AttributeSet, int); }
# Preserve Flurry mediation classes for DFP/AdMob Ads ­keep public class com.google.ads.mediation.flurry.**
# Google Play Services library
-keep class * extends java.util.ListResourceBundle {
protected Object[][] getContents(); }

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
public static final *** NULL; }
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
@com.google.android.gms.common.annotation.KeepName *; }
-keepnames class * implements android.os.Parcelable { public static final ** CREATOR;
}