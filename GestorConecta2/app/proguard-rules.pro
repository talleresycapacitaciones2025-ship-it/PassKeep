# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /sdk/tools/proguard/proguard-android.txt

# Keep Room entities
-keep class com.gestorconecta2.domain.model.** { *; }

# Keep SQLCipher
-dontwarn net.sqlcipher.**
-keep class net.sqlcipher.** { *; }
-keepclassmembers class net.sqlcipher.** { *; }
