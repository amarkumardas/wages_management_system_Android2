package amar.das.acbook.sharedpreferences;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {

    public enum Keys { // Enum declaration inside the class for keys
        HISTORY_KEEPING_DAYS,
        BUSINESS_NAME,WHATSAPP_NUMBER,PHONE_NUMBER,EMAIL,GST_NUMBER,ADDRESS,GOOGLE_SIGNIN_EMAIL,
        BACKUP_FILE_SHARE_OR_DOWNLOAD, BACKUP_EACH_FILE_USER_SELECTED_FORMAT,LAST_BACKUP_EACH_FILE,SINGLE_BACKUP_FILE_USER_SELECTED_FORMAT,LAST_SINGLE_BACKUP_FILE;

    }
    //Common types of data that can be stored include boolean, int, long, float, and String.
    //SharedPreferences in Android is a mechanism to store and retrieve simple data in key-value pairs persistently. It's often used to store small pieces of information such as user preferences, settings, and other application-specific configurations. SharedPreferences are private to the application, meaning that other applications cannot access or modify the preferences of your app.
    private static final String PREFERENCE_NAME = "MyPreferences";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public static void setString(Context context, String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(Context context, String key, String defaultValue) {
        return getSharedPreferences(context).getString(key, defaultValue);
    }

    public static void setInt(Context context, String key, int value) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static int getInt(Context context, String key, int defaultValue) {
        return getSharedPreferences(context).getInt(key, defaultValue);
    }
    public static void setBoolean(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        return getSharedPreferences(context).getBoolean(key, defaultValue);
    }
    public static void clearSharedPreferences(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.clear();
        editor.apply();
    }
}

