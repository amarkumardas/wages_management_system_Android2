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

-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** {*;}


#androidx.navigation.**: This refers to the entire androidx.navigation package and any sub-packages it might have (represented by the double asterisk **).
#{ *; }: The curly braces with an asterisk inside indicate that Proguard should keep all members (methods, fields) within these classes.
-keep class androidx.navigation.** { *; }

# Keep all classes from Apache POI
-keep class org.apache.poi.** { *; } #without this not working
-keep class org.openxmlformats.schemas.** { *; }#without this not working
#-keep class com.microsoft.schemas.** { *; }#working
# Keep Log4j classes
-keep class org.apache.logging.log4j.** { *; }
-keep class org.apache.logging.log4j.spi.CopyOnWriteSortedArrayThreadContextMap { *; }
# Keep Apache Commons Compress classes
-keep class org.apache.commons.compress.** { *; }

# Keep classes used for reflection
#-keepclassmembers class * {#working
#    public static final org.apache.xmlbeans.SchemaType type;
#}

# Keep XMLBeans classes
#-keep class org.apache.xmlbeans.** { *; }working
#-keep class org.w3c.dom.** { *; }working
#-keep class org.xml.sax.** { *; } working
#-keep class javax.xml.** { *; } working


# Don't warn about missing classes
-dontwarn org.apache.poi.**
-dontwarn org.openxmlformats.schemas.**
-dontwarn com.microsoft.schemas.**
-dontwarn org.apache.xmlbeans.**
-dontwarn org.w3c.dom.**
-dontwarn org.xml.sax.**
-dontwarn javax.xml.**
-dontwarn org.apache.logging.log4j.**
-dontwarn org.apache.commons.compress.**
-dontwarn java.awt.Shape

