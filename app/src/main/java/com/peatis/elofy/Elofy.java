package com.peatis.elofy;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Elofy {
    public static final String KEY_DOMAIN = "domain";
    public static final String KEY_AUTH_TOKEN = "auth_token";
    public static final String KEY_USER_ID = "id";
    public static final String KEY_USER_NAME = "name";
    public static final String KEY_USER_EMAIL = "email";
    public static final String KEY_USER_AVATAR = "avatar";
    public static final String KEY_USER_AVATAR_XS = "avatar_xs";
    public static final String KEY_USER_AVATAR_MD = "avatar_md";
    public static final String KEY_REMEMBER_ME = "remember_me";
    public static final String KEY_FIREBASE_TOKEN = "firebase_token";
    public static final String KEY_NOTIFIED = "notified";

    public static final String BROADCAST_ACTIVITY_UPDATED = "activity_updated";
    public static final String BROADCAST_KEY_VALUE_ADDED = "key_value_added";
    public static final String BROADCAST_SURVEY_FINISHED = "survey_finished";

    private static final String TAG = "com.sitaep.elofy";

    public static String string(Context context, String key) {
        return context.getApplicationContext().getSharedPreferences(TAG, 0).getString(key, null);
    }

    public static boolean Bool(Context context, String key) {
        return context.getApplicationContext().getSharedPreferences(TAG, 0).getBoolean(key, false);
    }

    public static int Int(Context context, String key) {
        return context.getApplicationContext().getSharedPreferences(TAG, 0).getInt(key, 0);
    }

    public static void string(Context context, String key, String val) {
        context.getApplicationContext().getSharedPreferences(TAG, 0).edit().putString(key, val).apply();
    }

    public static void Bool(Context context, String key, boolean val) {
        context.getApplicationContext().getSharedPreferences(TAG, 0).edit().putBoolean(key, val).apply();
    }

    public static void Int(Context context, String key, int val) {
        context.getApplicationContext().getSharedPreferences(TAG, 0).edit().putInt(key, val).apply();
    }

    public static void clear(Context context) {
        context.getApplicationContext().getSharedPreferences(TAG, 0).edit().clear().apply();
    }

    public static String string(JsonObject json, String key) {
        return !json.has(key) || json.get(key).isJsonNull() ? "" : json.get(key).getAsString();
    }

    public static boolean Bool(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() && json.get(key).getAsBoolean();
    }

    public static double Double(JsonObject json, String key) {
        return !json.has(key) || json.get(key).isJsonNull() ? 0 : json.get(key).getAsDouble();
    }

    public static int Int(JsonObject json, String key) {
        return !json.has(key) || json.get(key).isJsonNull() ? 0 : json.get(key).getAsInt();
    }

    public static long Long(JsonObject json, String key) {
        return !json.has(key) || json.get(key).isJsonNull() ? 0 : json.get(key).getAsLong();
    }

    public static String dateString(String date, String source, String destination) {
        try {
            SimpleDateFormat sd = new SimpleDateFormat(source, Locale.getDefault());
            Date d = sd.parse(date);
            SimpleDateFormat sd1 = new SimpleDateFormat(destination, Locale.getDefault());
            return sd1.format(d);
        } catch (Exception e) {
            return date;
        }
    }

    public static String dateString(String format, int year, int month, int day) {
        String m = "0" + (month + 1);
        if (m.length() > 2) m = m.substring(1);

        String d = "0" + day;
        if (d.length() > 2) d = d.substring(1);

        return format.replace("yyyy", year + "").replace("MM", m).replace("dd", d);
    }

    public static String dateDiff(long diff) {
        final long hour = 60;
        final long day = hour * 24;
        final long week = day * 7;
        final long month = day * 30;
        final long year = day * 365;

        if (diff / year > 0) {
            return (diff / year) + " " + (diff / year == 1 ? "year ago" : "years ago");
        } else if (diff / month > 0) {
            return (diff / month) + " " + (diff / month == 1 ? "month ago" : "months ago");
        } else if (diff / week > 0) {
            return (diff / week) + " " + (diff / week == 1 ? "week ago" : "weeks ago");
        } else if (diff / day > 0) {
            return (diff / day) + " " + (diff / day == 1 ? "day ago" : "days ago");
        } else if (diff / hour > 0) {
            return (diff / hour) + " " + (diff / hour == 1 ? "hour ago" : "hours ago");
        } else if (diff > 0) {
            return (diff) + " " + (diff == 1 ? "minute ago" : "minutes ago");
        } else {
            return "Just ago";
        }
    }

    public static int color(Context context, int res) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getResources().getColor(res, context.getTheme());
        } else {
            return context.getResources().getColor(res);
        }
    }

    public static Drawable drawable(Context context, int res) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getResources().getDrawable(res, context.getTheme());
        } else {
            return context.getResources().getDrawable(res);
        }
    }

    public static int pixel(Context context, int dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.getResources().getDisplayMetrics());
    }

    public static int dip(Context context, int pixel) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(pixel / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
