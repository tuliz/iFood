package com.example.iFood.Activities.oldActivities;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.iFood.Activities.MainActivity;
import com.example.iFood.Activities.MyRecipes;
import com.example.iFood.Activities.SearchRecipe;
import com.example.iFood.Classes.Recipes;
import com.example.iFood.R;
import com.example.iFood.Utils.ConnectionBCR;
import com.example.iFood.Utils.EditItemImage;
import com.example.iFood.Utils.FileUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**

 * This screen responsible for adding recipes to the database
 * Each user can create as many as he wants.
 * Must have all the information before actual create a recipe.
 */
public class AddRecipe extends AppCompatActivity {
    // Declare Variables
    ConnectionBCR bcr = new ConnectionBCR();
    FloatingActionButton btnAddRecipe,btnReset;
    Button btnOk,btnDismiss;
    EditText etRecipeName,etRecipeIngredients,etRecipeInstructions;
    String name,ingre,instru,recipeImage,id;
    ImageView ivImage;
    ProgressDialog progressDialog;
    long time;
    // Connect to DB & Storage
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference DB = database.getReference();
    StorageReference mStorage = FirebaseStorage.getInstance().getReference();

    // Camera Handling
    private EditItemImage mEditItemImage;

    Bitmap imageBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        mEditItemImage = new EditItemImage(AddRecipe.this);

        // Buttons Declarations
        btnAddRecipe = findViewById(R.id.btnSaveRecipe);
        btnReset = findViewById(R.id.btnResetRecipe);

        // EditText Declarations
        etRecipeName = findViewById(R.id.etRecipeName);
        etRecipeIngredients = findViewById(R.id.etRecipeIngredients);
        etRecipeInstructions = findViewById(R.id.etRecipeMethod);

        // Image View
        ivImage = findViewById(R.id.ivRecipeImage);
        ivImage.setDrawingCacheEnabled(true);

        // Progress Dialog
        progressDialog = new ProgressDialog(this);

        // Listeners
        ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditItemImage.openDialog();
            }
        });
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reset all fields in the screen
                resetRecipe();
            }
        });
        btnAddRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(imageBitmap!=null && !Objects.equals(ivImage.getDrawable().getConstantState(), getResources().getDrawable(R.drawable.no_image).getConstantState()) &&
                   !etRecipeName.getText().toString().isEmpty() &&
                   !etRecipeIngredients.getText().toString().isEmpty() &&
                   !etRecipeInstructions.getText().toString().isEmpty())
                {
                    progressDialog.setMessage("Creating Recipe");
                    progressDialog.show();
                    // Call function to create the recipe
                    createRecipe();

                }else{
                    Toast.makeText(AddRecipe.this,"One or more of required fields are empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

     //   Log.i("role","Role is:"+getIntent().getStringExtra("userRole"));

    } // onCreate End

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("RESULT_OK","RESULT_OK:"+RESULT_OK);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                default:
                case EditItemImage.TAKE_PICTURE:
                case EditItemImage.PICK_IMAGE:
                    setImage(requestCode, data);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    public void setImage(int requestCode, Intent data) {
        String mPath = EditItemImage.mPath;
        switch (requestCode) {
            case EditItemImage.TAKE_PICTURE:
                FileUtils.addMediaToGallery(this, mPath);
                imageBitmap = BitmapFactory.decodeFile(mPath);

                try {
                    ExifInterface exifInterface = new ExifInterface(mPath);
                    int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                    switch (orientation) {

                        case ExifInterface.ORIENTATION_ROTATE_90:
                            imageBitmap = rotateImage(imageBitmap, 90);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_180:
                            imageBitmap = rotateImage(imageBitmap, 180);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_270:
                            imageBitmap = rotateImage(imageBitmap, 270);
                            break;

                        case ExifInterface.ORIENTATION_NORMAL:
                            // do nothing
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                compressImage();
                ivImage.setImageBitmap(imageBitmap);

                break;

            case EditItemImage.PICK_IMAGE:
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    try {
                        EditItemImage.mPath = FileUtils.getPath(this, selectedImage);
                        int orientation = FileUtils.getOrientation(this, selectedImage);
                        InputStream inputStream = getContentResolver().openInputStream(selectedImage);
                        imageBitmap = BitmapFactory.decodeStream(inputStream);
                        imageBitmap = rotateImage(imageBitmap, orientation);
                        ivImage.setImageBitmap(imageBitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    /**
     *
     * @param source the given Image
     * @param angle the current image angle
     * @return rotated image
     */
    public Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
    /**
     * Function checking for permission and if there is none, asking for them ( camera related )
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case EditItemImage.TAKE_PICTURE:
                    mEditItemImage.openCamera();
                    break;
                case EditItemImage.PICK_IMAGE:
                    mEditItemImage.openGallery();
                    break;
            }
        }
    }
    /**
     * This function is responsible to reduce file size of image taken ( when taken pictures with phone, the image size could be up tp 8MB and we want to reduced
     * file size to reduce loading time / uploading time.
     */
    private void compressImage() {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);//Compression quality, here 100 means no compression, the storage of compressed data to baos
        int options = 90;
        while (baos.toByteArray().length / 1024 > 400) {  //Loop if compressed picture is greater than 400kb, than to compression
            baos.reset();//Reset baos is empty baos
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);//The compression options%, storing the compressed data to the baos
            options -= 10;//Every time reduced by 10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//The storage of compressed data in the baos to ByteArrayInputStream
        imageBitmap = BitmapFactory.decodeStream(isBm, null, null);//The ByteArrayInputStream data generation
    }
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_layout,menu);
        return super.onCreateOptionsMenu(menu);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {


            case R.id.menu_Exit:
                final Dialog myDialog = new Dialog(AddRecipe.this);
                myDialog.setContentView(R.layout.dialog);
                btnDismiss = myDialog.findViewById(R.id.btnDismiss);
                btnOk =  myDialog.findViewById(R.id.btnOk);

                // if pressed Ok will close the App
                btnOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        SharedPreferences.Editor delData = getSharedPreferences("userData",MODE_PRIVATE).edit();
                        delData.clear();
                        delData.apply();
                        finish();
                    }
                });
                // if pressed Dismiss will stay in the App
                btnDismiss.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myDialog.dismiss();
                    }
                });
                myDialog.show();
                break;
            case R.id.menu_MyRecepies:
                Intent myRecipes = new Intent(AddRecipe.this, MyRecipes.class);
                myRecipes.putExtra("username",getIntent().getStringExtra("username"));
                myRecipes.putExtra("userRole",getIntent().getStringExtra("userRole"));
                startActivity(myRecipes);
                break;

            case R.id.menu_SearchRecepie:
                Intent search = new Intent(AddRecipe.this, SearchRecipe.class);
                search.putExtra("username",getIntent().getStringExtra("username"));
                search.putExtra("userRole",getIntent().getStringExtra("userRole"));
                startActivity(search);
                break;
            case R.id.menuInbox:
                Intent inbox = new Intent(AddRecipe.this, Inbox.class);
                inbox.putExtra("username",getIntent().getStringExtra("username"));
                inbox.putExtra("userRole",getIntent().getStringExtra("userRole"));
                startActivity(inbox);
                break;
            case R.id.menuHome:
                Intent main = new Intent(AddRecipe.this, MainActivity.class);
                main.putExtra("username",getIntent().getStringExtra("username"));
                main.putExtra("userRole",getIntent().getStringExtra("userRole"));
                startActivity(main);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * Reset all the variables in the screen after a recipe was added or user clicked on "Reset".
     */
    public void resetRecipe(){
     etRecipeInstructions.setText("");
     etRecipeIngredients.setText("");
     etRecipeName.setText("");
     imageBitmap = BitmapFactory.decodeResource(getResources(),
             R.drawable.no_image);
     ivImage.setImageBitmap(imageBitmap);
 }
    /**
     * Function called when user press Submit and create the recipe itself.
     */
    public void createRecipe(){

     name = etRecipeName.getText().toString();
     ingre = etRecipeIngredients.getText().toString();
     instru = etRecipeInstructions.getText().toString();
     ByteArrayOutputStream baos = new ByteArrayOutputStream();
     imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
     final byte[] data = baos.toByteArray();
     final UploadTask uploadTask = mStorage.child("Photos/"+ UUID.randomUUID().toString()).putBytes(data);
     uploadTask.addOnFailureListener(new OnFailureListener() {
         @Override
         public void onFailure(@NonNull Exception exception) {
             // Handle unsuccessful uploads
         }
     }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
         @Override
         public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
             if (taskSnapshot.getMetadata() != null) {
                 if (taskSnapshot.getMetadata().getReference() != null) {
                     Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                     result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                         @Override
                         public void onSuccess(Uri uri) {

                             recipeImage = uri.toString();
                             String addBy = getIntent().getStringExtra("username");
                             id = String.valueOf(DB.child("Recipes").push().getKey());
                             Recipes rec;

                             rec = new Recipes(name,ingre,getResources().getString(R.string.method),instru,recipeImage,id,addBy);
                             rec.setApproved(false);
                             DB.child("Recipes").child(id).child(Objects.requireNonNull(AddRecipe.this.getIntent().getStringExtra("username"))).setValue(rec);
                             progressDialog.dismiss();


                             resetRecipe();
                             // Log.i("URL", "Image URL:" + picUrl);
                         }
                     });
                 } // close 2nd if
             } // close 1st if
         } // close onSuccess method
     }); // close OnSuccessListener
 }

        /**
         * Function called when user choose to clear image when promopted on clicking the image itself
         */
    public void clearImage() {
        ivImage.setImageResource(R.drawable.no_image);
    }
    /**
     * Register our Broadcast Receiver when opening the app.
     *
     */
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(bcr,filter);
    }
    /**
     * Stop our Broadcast Receiver when the app is closed.
     */
    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(bcr);
    }
} // class end
