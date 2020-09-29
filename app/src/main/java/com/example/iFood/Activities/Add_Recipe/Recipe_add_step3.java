package com.example.iFood.Activities.Add_Recipe;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.iFood.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.example.iFood.Activities.Add_Recipe.addRecipe_New.bitmapImage;

/**
 * Recipe step 3, adding a photo to the recipe itself.
 */

public class Recipe_add_step3 extends Fragment {


    // Camera Handling
    Button btnCamera;
    ImageView ivRecipeImage;


    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_step3, container, false);



        btnCamera = view.findViewById(R.id.btnCamera);
        ivRecipeImage = view.findViewById(R.id.ivRecipeImage);
        btnCamera = view.findViewById(R.id.btnCamera);
        ivRecipeImage.setDrawingCacheEnabled(true);
        ivRecipeImage.setOnClickListener(v -> selectImage(getContext()));
        btnCamera.setOnClickListener(v -> selectImage(getContext()));

        return view;
    }


    private void selectImage(Context context) {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose your picture");

        builder.setItems(options, (dialog, item) -> {
           if(hasCameraPermission()){
            if (options[item].equals("Take Photo")) {

                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, 0);

            }else if (options[item].equals("Choose from Gallery")) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , 1);

            }else if (options[item].equals("Cancel")) {
                dialog.dismiss();
            }
        }
        });
        builder.show();
    }
    private boolean hasCameraPermission() {
        Context mContext = Objects.requireNonNull(getContext()).getApplicationContext();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, 1);
                return false;
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, 1);
                return false;
            }
        } else return true;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        bitmapImage = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                        compressImage();
                        ivRecipeImage.setImageBitmap(bitmapImage);
                    }
                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage =  data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = Objects.requireNonNull(getActivity()).getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                bitmapImage = BitmapFactory.decodeFile(picturePath);
                                try {
                                    ExifInterface exifInterface = new ExifInterface(picturePath);
                                    int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                                    switch (orientation) {
                                        case ExifInterface.ORIENTATION_ROTATE_90:
                                            bitmapImage = rotateImage(bitmapImage, 90);
                                            break;
                                        case ExifInterface.ORIENTATION_ROTATE_180:
                                            bitmapImage = rotateImage(bitmapImage, 180);
                                            break;
                                        case ExifInterface.ORIENTATION_ROTATE_270:
                                            bitmapImage = rotateImage(bitmapImage, 270);
                                            break;
                                        case ExifInterface.ORIENTATION_NORMAL:
                                            // do nothing
                                            break;
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                compressImage();
                                ivRecipeImage.setImageBitmap(bitmapImage);
                                cursor.close();
                            }
                        }

                    }
                    break;
            }
        }
    }
    public Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
    private void compressImage() {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);//Compression quality, here 100 means no compression, the storage of compressed data to baos
        int options = 90;
        while (baos.toByteArray().length / 1024 > 400) {  //Loop if compressed picture is greater than 400kb, than to compression
            baos.reset();//Reset baos is empty baos
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, options, baos);//The compression options%, storing the compressed data to the baos
            options -= 10;//Every time reduced by 10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//The storage of compressed data in the baos to ByteArrayInputStream
        bitmapImage = BitmapFactory.decodeStream(isBm, null, null);//The ByteArrayInputStream data generation
    }

    public void clearImage() {
        ivRecipeImage.setImageResource(R.drawable.no_image);
        bitmapImage=null;
    }
}
