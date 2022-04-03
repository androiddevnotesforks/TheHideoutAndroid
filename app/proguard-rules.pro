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
-keepattributes Signature
-keepattributes *Annotation*

-keep class com.austinhodak.thehideout.firebase.** { *; }
-keep class com.austinhodak.thehideout.calculator.models.** { *; }
-keep class com.austinhodak.thehideout.WeaponBuild { *; }
-keep class com.austinhodak.thehideout.WeaponBuild$BuildMod { *; }
-keep class com.austinhodak.thehideout.calculator.views.HealthBar$Health { *; }
-keep class com.austinhodak.thehideout.flea_market.detail.FleaItemDetail$NavItem { *; }
-keep class com.austinhodak.thehideout.map.models.** { *; }
-keep class com.austinhodak.thehideout.news.models.** { *; }

-keep class com.austinhodak.tarkovapi.models.** { *; }
-keep class com.austinhodak.tarkovapi.room.models.** { *; }
-keep class com.austinhodak.tarkovapi.tarkovtracker.models.** { *; }


-keepclassmembers class **.R$* {
    public static <fields>;
}

-keep class **.R$*

-keep class com.austinhodak.thehideout.crafts.CraftDetailActivity$ItemSubtitle { *; }
-keep class com.austinhodak.thehideout.barters.BarterDetailActivity$ItemSubtitle { *; }

-keepclassmembers class com.google.firebase.database.GenericTypeIndicator {
  *;
}

-keep class com.adapty.** { *; }
-keep class com.austinhodak.thehideout.billing.** { *; }

-keepattributes SourceFile,LineNumberTable        # Keep file names and line numbers.
-keep public class * extends java.lang.Exception  # Optional: Keep custom exceptions.

# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken