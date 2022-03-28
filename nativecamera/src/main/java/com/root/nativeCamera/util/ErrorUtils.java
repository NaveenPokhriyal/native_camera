package com.root.nativeCamera.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by 123 on 04-Jan-18.
 */

public class ErrorUtils {

    public static void showError(Exception e) {
        String errorMsg = !TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : null;
        if (errorMsg != null) {

        }
        e.printStackTrace();
    }

    public static void showError(String error) {
        
    }

    public static void logError(String tag, Exception e) {
        String errorMsg = !TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : "";
        Log.e(tag, errorMsg);
    }

    public static void logError(String tag, String msg) {
        String errorMsg = !TextUtils.isEmpty(msg) ? msg : "";
        Log.e(tag, msg);
    }

    public static void logError(Exception e) {
        e.printStackTrace();
    }

    public static void logError(Throwable e) {
        e.printStackTrace();
    }

    public static Toast getErrorToast(Context context, String error) {
        return Toast.makeText(context, error, Toast.LENGTH_LONG);
    }
}
