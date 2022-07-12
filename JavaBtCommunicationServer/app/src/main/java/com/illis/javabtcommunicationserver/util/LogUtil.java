package com.illis.javabtcommunicationserver.util;

import android.util.Log;

public class LogUtil
{
    private static final String TAG = "OneTouch";
    private static final boolean IS_TOAST = false;

    public static void d(String msg)
    {
        String tag = "";
        String temp = new Throwable().getStackTrace()[1].getClassName();
        if (temp != null) {
            int lastDotPos = temp.lastIndexOf(".");
            tag = temp.substring(lastDotPos+1);
        }

        String methodName = new Throwable().getStackTrace()[1].getMethodName();
        int lineNumber = new Throwable().getStackTrace()[1].getLineNumber();

        Log.d(TAG, "["+tag+"] "+methodName+"()"+"["+lineNumber+"]"+" >> "+msg);
    }

    public static void w(String msg)
    {
        String tag = "";
        String temp = new Throwable().getStackTrace()[1].getClassName();
        if (temp != null) {
            int lastDotPos = temp.lastIndexOf(".");
            tag = temp.substring(lastDotPos+1);
        }

        String methodName = new Throwable().getStackTrace()[1].getMethodName();
        int lineNumber = new Throwable().getStackTrace()[1].getLineNumber();

        Log.w(TAG, "["+tag+"] "+methodName+"()"+"["+lineNumber+"]"+" >> "+msg);
    }

    public static void e(String msg)
    {
        String tag = "";
        String temp = new Throwable().getStackTrace()[1].getClassName();
        if (temp != null) {
            int lastDotPos = temp.lastIndexOf(".");
            tag = temp.substring(lastDotPos+1);
        }

        String methodName = new Throwable().getStackTrace()[1].getMethodName();
        int lineNumber = new Throwable().getStackTrace()[1].getLineNumber();

        Log.e(TAG, "["+tag+"] "+methodName+"()"+"["+lineNumber+"]"+" >> "+msg);
    }
}
