# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/BogerChan/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.java\script.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-printmapping build/outputs/mapping/release/mapping.txt

-dontwarn okio.**
-dontwarn javax.annotation.**
# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions
-keep class sun.misc.Unsafe { *; }
-keep class geographic.boger.me.nationalgeographic.biz.selectdate.SelectDateData { *; }
-keep class geographic.boger.me.nationalgeographic.biz.selectdate.SelectDateAlbumData { *; }
-keep class geographic.boger.me.nationalgeographic.biz.ngdetail.NGDetailData { *; }
-keep class geographic.boger.me.nationalgeographic.biz.ngdetail.NGDetailPictureData { *; }
-keep class app.dinus.com.loadingdrawable.** { *; }

-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}
-keep class android.support.**{*;}