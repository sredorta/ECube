package com.ecube.solutions.ecube.authentication.profile.dao;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.util.Log;
import com.ecube.solutions.ecube.R;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;


/**
 * Created by sredorta on 2/20/2017.
 */
public class Avatar implements Serializable {
    //Logs
    private static final String TAG = Avatar.class.getSimpleName();
    private static final boolean DEBUG = true;

    private static final int AVATAR_SIZE = 100;
    private Bitmap mBitmap;                     //Stores the bitmap of the avatar
    private File mPhotoFile;                    //Stores a photo of avatar if we take photo
    private Context mContext;


    public File getPhotoFile() {
        return mPhotoFile;
    }

    private void setPhotoFile() {
        File externalFilesDir = mContext.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (externalFilesDir == null) {
            mPhotoFile = null;
        } else {
            mPhotoFile = new File(externalFilesDir, "IMG_profile.jpg");
        }
    }


    public Bitmap getBitmap() {
        return mBitmap;
    }
    public void setBitmap(@Nullable  Bitmap bitmap) {
        if (bitmap == null) {
            mBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.profile_user_default);
            Log.i(TAG, "Setting default bitmap as it was null !!!!");
        } else {
            mBitmap = bitmap;
        }
    }

    public Avatar(Context context, Bitmap bitmap) {
        mContext = context;
        this.setBitmap(bitmap);
        this.setPhotoFile();
    }

    //Constructor with default bitmap creation
    public Avatar(Context context) {
        mContext = context;
        this.setBitmap(null);
        this.setPhotoFile();
    }

    //Set the avatar bitmap from Uri with correct crop and rotation
    public void getAvatarFromUri(Uri uri) {
        Bitmap myBitmap = null;
        try {
            Bitmap selectedBitmap = this.getBitmapFromUri(uri);
            myBitmap = ThumbnailUtils.extractThumbnail(selectedBitmap, AVATAR_SIZE, AVATAR_SIZE);
            myBitmap = this.rotateImage(myBitmap,uri);
        } catch (IOException e) {
            Log.i(TAG, "Caught exception: " + e);
        }
        Log.i(TAG, "Bitmap size: " + myBitmap.getByteCount());
        this.setBitmap(myBitmap);
    }

    //Set the avatar bitmap from file with correct crop and rotation
    public void getAvatarFromFile(File photoFile) {
        Bitmap myBitmap = null;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        myBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), bmOptions);
        myBitmap = ThumbnailUtils.extractThumbnail(myBitmap, AVATAR_SIZE, AVATAR_SIZE);         // Crop image to 300x300
        myBitmap = this.rotateImage(myBitmap, photoFile.getAbsolutePath());
        Log.i(TAG, "Bitmap size: " + myBitmap.getByteCount());
        this.setBitmap(myBitmap);
    }


    //From an uri we return a bitmap
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        Bitmap image = null;
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    mContext.getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
        } catch (NullPointerException e) {
            Log.i(TAG, "Caught exception : e");
        }
        return image;
    }


    public static void saveFile(Bitmap bitmap, String file_name,Context context ) {
        OutputStream fOut = null;
        File externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File myFile = new File(externalFilesDir, file_name);
        Log.i(TAG, "Saving file: " +myFile.getAbsolutePath());
        try {
            fOut = new FileOutputStream(myFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
            fOut.flush(); // Not really required
            fOut.close(); // do not forget to close the stream
        } catch (Exception e) {
            Log.i(TAG, "Caught exception :" + e);
        }

    }
    public static void saveFile(Uri sourceUri, File destination,Context context) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            ContentResolver content = context.getContentResolver();
            inputStream = content.openInputStream(sourceUri);

            outputStream = new FileOutputStream( destination); // filename.png, .mp3, .mp4 ...
            if(outputStream != null){
                Log.i(TAG, "Output Stream Opened successfully");
            }

            byte[] buffer = new byte[1000];
            int bytesRead = 0;
            while ( ( bytesRead = inputStream.read( buffer, 0, buffer.length ) ) >= 0 ) {
                outputStream.write( buffer, 0, buffer.length );
            }
        } catch (Exception e) {
            Log.i(TAG, "Caught exception : " + e);
        }
    }

    private Bitmap rotateImage(Bitmap bitmap, Uri contentURI) {
        File externalFilesDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File myFile = new File(externalFilesDir, "tmp.jpg");
        Avatar.saveFile(contentURI, myFile,mContext);
        Log.i(TAG, "myFile :" + myFile.getAbsolutePath());
        Bitmap result = this.rotateImage(bitmap,myFile.getAbsolutePath());
        myFile.delete();
        return result;
    }


    private Bitmap rotateImage(Bitmap bitmap, String filePath) {
        Bitmap resultBitmap = bitmap;

        try {
            ExifInterface exifInterface = new ExifInterface(filePath);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.i(TAG, "Orientation is: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                matrix.postRotate(90);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                matrix.postRotate(180);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                matrix.postRotate(270);
            } else {
                matrix.postRotate(0);
            }
            // Rotate the bitmap
            resultBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        catch (Exception exception) {
            Log.i(TAG, "Could not rotate the image");
        }
        return resultBitmap;
    }


}
