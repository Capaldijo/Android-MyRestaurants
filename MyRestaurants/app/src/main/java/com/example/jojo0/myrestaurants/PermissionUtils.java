package com.example.jojo0.myrestaurants;

import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

/**
 * Utility class for access to runtime permissions.
 */
public abstract class PermissionUtils {

    /**
     * Requests the coarse location, call phone, write to external storage and contact permissions.
     * If a rationale with an additional explanation should
     * be shown to the user, displays a dialog that triggers the request.
     */
    public static void requestPermission(AppCompatActivity activity, int requestId,
                                         String permission) {
            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestId);
    }

    /**
     * Return True or False depending the user's answer for the asked permissions
     */
    public static boolean isPermissionGranted(String[] grantPermissions, int[] grantResults,
                                              String permission) {
        if(grantResults.length>0)
            for (int i = 0; i < grantPermissions.length; i++) {
                if (permission.equals(grantPermissions[i]))
                    return grantResults[i] == PackageManager.PERMISSION_GRANTED;
            }
        return false;
    }
}