package com.example.iFood.Utils;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.iFood.Activities.Add_Recipe.addRecipe_New;
import com.example.iFood.Activities.EditRecipeActivity;
import com.example.iFood.Activities.ProfileActivity;
import com.example.iFood.Activities.SignUpActivity;

import com.example.iFood.BuildConfig;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditItemImage {

    public static final int TAKE_PICTURE = 1;
    public static final int PICK_IMAGE = 2;

    public static String mPath;
    private File tempFile;

    private Context mContext;

    public EditItemImage(Context context) {
        mContext = context;
    }



    public void openDialog() {
        String[] imageOptions;
        if (mPath != null)
            imageOptions = new String[]{"Camera", "Gallery", "Remove image"};
        else
            imageOptions = new String[]{"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Select an image");
        builder.setItems(imageOptions, (dialog, which) -> {
            switch (which) {
                case 0:
                    openCamera();
                    break;
                case 1:
                    openGallery();
                    break;
                case 2:
                    clearImage();
                    break;
            }
        }).show();
    }

    private boolean hasCameraPermission() {
        AppCompatActivity activity = ((AppCompatActivity) mContext);
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, TAKE_PICTURE);
                return false;
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, TAKE_PICTURE);
                return false;
            }
        } else return true;
    }

    private boolean hasReadExternalStoragePermission() {
        AppCompatActivity activity = ((AppCompatActivity) mContext);
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE);
                return false;
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE);
                return false;
            }
        } else return true;
    }

    private boolean hasWriteStoragePermission() {
        AppCompatActivity activity = ((AppCompatActivity) mContext);
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, TAKE_PICTURE);
                return false;
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, TAKE_PICTURE);
                return false;
            }
        } else return true;
    }

    public void openCamera() {
        String n = mContext.getClass().getSimpleName();
        if (hasCameraPermission() && hasWriteStoragePermission()) {

            File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Camera");

            if (!folder.exists())
                folder.mkdirs();

            String timeStamp = new SimpleDateFormat("yyyyMMdd_kkmmss", Locale.getDefault()).format(Calendar.getInstance().getTime());

            tempFile = null;
            tempFile = new File(String.format("%s/%s%s", folder, timeStamp, ".jpg"));

            mPath = tempFile.getAbsolutePath();

            Uri uri = FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider", tempFile);
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
                try {
                    if(n.equals("ProfileActivity")) {
                        ((ProfileActivity) mContext).startActivityForResult(takePictureIntent, TAKE_PICTURE);
                    }
                    if(n.equals("EditRecipeActivity")){
                        ((EditRecipeActivity) mContext).startActivityForResult(takePictureIntent, TAKE_PICTURE);
                    }
                    if(n.equals("SignUpActivity")){
                        ((SignUpActivity) mContext).startActivityForResult(takePictureIntent, TAKE_PICTURE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void openGallery() {
        String n = mContext.getClass().getSimpleName();
        if (hasReadExternalStoragePermission()) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            try {
                if(n.equals("ProfileActivity")) {
                    ((ProfileActivity) mContext).startActivityForResult(Intent.createChooser(intent, ""), PICK_IMAGE);
                }
                if(n.equals("EditRecipeActivity")){
                    ((EditRecipeActivity) mContext).startActivityForResult(Intent.createChooser(intent, ""), PICK_IMAGE);
                }
                if(n.equals("SignUpActivity")){
                    ((SignUpActivity) mContext).startActivityForResult(Intent.createChooser(intent, ""), PICK_IMAGE);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void clearImage() {
        String n = mContext.getClass().getSimpleName();
        mPath = null;
        tempFile = null;
        if(n.equals("ProfileActivity")) {
            ((ProfileActivity) mContext).clearImage();
        }
        if(n.equals("SignUpActivity")){
            ((SignUpActivity) mContext).clearImage();
        }
        if(n.equals("EditRecipeActivity")){
            ((EditRecipeActivity) mContext).clearImage();
        }
      }


}