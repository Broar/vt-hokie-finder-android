package com.parseapp.vthokiefinder.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

/**
 * A utility class to handle retrieving file paths on all API levels
 *
 * http://stackoverflow.com/questions/28452591/android-get-exif-rotation-from-uri
 */
public class FileUtility {

    /**
     * Gets the real path from file
     *
     * @param context the context of the activity
     * @param contentUri the uri pointing to the file location
     * @return the absolute path to the file
     */
    public static String getRealPathFromURI(Context context, Uri contentUri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && DocumentsContract.isDocumentUri(context, contentUri)) {
            return getPathForV19AndUp(context, contentUri);
        }

        else {
            return getPathForPreV19(context, contentUri);
        }
    }

    /**
     * Handles pre V19 uri's
     *
     * @param context the context of the activity
     * @param contentUri the uri pointing to the file location
     * @return the absolute path to the file
     */
    public static String getPathForPreV19(Context context, Uri contentUri) {
        String res = null;

        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);

        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }

        cursor.close();

        return res;
    }

    /**
     * Handles V19 and up uri's
     *
     * @param context the context of the activity
     * @param contentUri the uri pointing to the file location
     * @return the absolute path to the file
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPathForV19AndUp(Context context, Uri contentUri) {
        String wholeID = DocumentsContract.getDocumentId(contentUri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];
        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";
        Cursor cursor = context.getContentResolver().
                query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        column, sel, new String[]{ id }, null);

        String filePath = "";
        int columnIndex = cursor.getColumnIndex(column[0]);
        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }

        cursor.close();
        return filePath;
    }
}
