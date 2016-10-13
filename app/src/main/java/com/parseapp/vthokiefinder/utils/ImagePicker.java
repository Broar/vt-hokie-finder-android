package com.parseapp.vthokiefinder.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * A utility class enabling users to choose images from an application of their choice.
 *
 * @author Steven Briggs
 * @version 2015.10.22
 */
public class ImagePicker {

    public static final int PICK_IMAGE = 1;

    /**
     * Get an Intent that allows the user to pick an image from an external application
     *
     * @return an Intent that lets the user pick an image
     */
    public static Intent getIntent() {
        Intent intent = new Intent();
        intent.setType("image/*").setAction(Intent.ACTION_GET_CONTENT);
        return Intent.createChooser(intent, "Select a picture");
    }

    /**
     * Get the Bitmap for the image that the specified Uri points to
     *
     * @param context the context of the activity
     * @param uri the Uri pointing to the image to retrieve a Bitmap from
     * @return the Bitmap from the image that the Uri points to
     */
    public static Bitmap getBitmapFromUri(Context context, Uri uri) throws IOException {
        InputStream stream = context.getContentResolver().openInputStream(uri);
        String path = FileUtility.getRealPathFromURI(context, uri);
        return fixBitmapOrientation(path, BitmapFactory.decodeStream(stream));
    }

    /**
     * Correct the orientation of the specified Bitmap
     *
     * @param imagePath the absolute path to the image
     * @return a Bitmap matching the original but with correct orientation
     */
    private static Bitmap fixBitmapOrientation(String imagePath, Bitmap bm) {
        Matrix matrix = new Matrix();
        matrix.postRotate(getImageOrientation(imagePath));
        return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
    }

    /**
     * Get the orientation (in degrees) of the image located at imagePath
     *
     * http://stackoverflow.com/questions/19511610/camera-intent-auto-rotate-to-90-degree
     *
     * @param imagePath the absolute path to the image
     * @return the orientation of the image in degrees
     */
    private static int getImageOrientation(String imagePath){
        int rotate = 0;

        try {

            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());

            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            Log.d("ImagePicker", "" + orientation);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        }

        catch (IOException e) {
            e.printStackTrace();
        }

        return rotate;
    }
}
