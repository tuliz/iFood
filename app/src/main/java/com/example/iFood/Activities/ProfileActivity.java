package com.example.iFood.Activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.DigitsKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.iFood.Classes.Users;
import com.example.iFood.R;
import com.example.iFood.Utils.ConnectionBCR;
import com.example.iFood.Utils.EditItemImage;
import com.example.iFood.Utils.FileUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class ProfileActivity extends AppCompatActivity {
    // Patterns for inputs
    public final Pattern textPattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$");
    public final Pattern namePattern = Pattern.compile("[a-zA-Z ]+");
    ProgressDialog progressDialog;
    Dialog myDialog;
    FloatingActionButton btnRefresh,btnBack;
    TextView firstName,lastName,phone,email,tvUsername;
    ImageView userProfileImage,editPass,editFname,editLname,editPhone;
    // Camera Handling
    private EditItemImage mEditItemImage;
    // Broadcast Receiver
    ConnectionBCR bcr = new ConnectionBCR();

    Bitmap imageBitmap;
    // User Info related
    String imageURL;
    String userName,userRole;
    Users u;
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
    StorageReference mStorage = FirebaseStorage.getInstance().getReference();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // hide top bar
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }



        // Variables
        mEditItemImage = new EditItemImage(ProfileActivity.this);
        tvUsername = findViewById(R.id.tvUsr);
        userName = getIntent().getStringExtra("username");
        userRole = getIntent().getStringExtra("userRole");
        firstName = findViewById(R.id.tvFirstName);
        lastName = findViewById(R.id.tvLastName);
        phone = findViewById(R.id.tvUsrPhone);
        email = findViewById(R.id.tvUsrEmail);
        userProfileImage = findViewById(R.id.userProfileImage);
        // buttons
        btnBack = findViewById(R.id.btnBack);
        btnRefresh = findViewById(R.id.btnResetProfile);
        // clickable images for edit

        editFname = findViewById(R.id.editFname);
        editLname = findViewById(R.id.editLname);
        editPhone = findViewById(R.id.editPhone);
        editPass = findViewById(R.id.editPass);

        // progress dialog
        progressDialog = new ProgressDialog(ProfileActivity.this);
        // function
        pullUserData();
        // Listeners
        btnBack.setOnClickListener(v -> finish());
        btnRefresh.setOnClickListener(v -> refreshPage());
        editFname.setOnClickListener(v -> {
            final AlertDialog.Builder alert = new AlertDialog.Builder(ProfileActivity.this);
            final EditText edittext = new EditText(ProfileActivity.this);
            alert.setTitle("Enter your first name:");
            alert.setIcon(R.drawable.ic_edit_black);
            edittext.setText(firstName.getText().toString());
            alert.setView(edittext);
            alert.setPositiveButton(R.string.submit, (dialog, whichButton) -> {
                String fName = edittext.getText().toString();
                if(!namePattern.matcher(fName).matches())
                    Toast.makeText(ProfileActivity.this, "Must contain only letters!", Toast.LENGTH_SHORT).show();
                else {

                    ref.child("Users").child(userName).child("Fname").setValue(fName);

                }
            });
            alert.setNegativeButton(R.string.cancel, (dialog, whichButton) -> {

            });
            alert.show();
        });
        editLname.setOnClickListener(v -> {
            final AlertDialog.Builder alert = new AlertDialog.Builder(ProfileActivity.this);
            final EditText edittext = new EditText(ProfileActivity.this);
            alert.setTitle("Enter your last name:");
            alert.setIcon(R.drawable.ic_edit_black);
            edittext.setText(lastName.getText().toString());
            alert.setView(edittext);
            alert.setPositiveButton("Submit", (dialog, whichButton) -> {
                String lName = edittext.getText().toString();
                if(!namePattern.matcher(lName).matches())
                    Toast.makeText(ProfileActivity.this, "Must contain only letters!", Toast.LENGTH_SHORT).show();
                 else {

                    ref.child("Users").child(userName).child("Lname").setValue(lName);
                }
            });
            alert.setNegativeButton("Cancel", (dialog, whichButton) -> {

            });
            alert.show();
        });
        editPhone.setOnClickListener(v -> {
            final AlertDialog.Builder alert = new AlertDialog.Builder(ProfileActivity.this);
            final EditText edittext = new EditText(ProfileActivity.this);
            edittext.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
            alert.setTitle("Enter your phone:");
            edittext.setText(phone.getText().toString());
            alert.setIcon(R.drawable.ic_edit_black);
            alert.setView(edittext);

            alert.setPositiveButton("Submit", (dialog, whichButton) -> {
                String number =edittext.getText().toString();
                ref.child("Users").child(userName).child("Phone").setValue(number);
                });
            alert.setNegativeButton("Cancel", (dialog, whichButton) -> {

            });

            alert.show();
        });
        editPass.setOnClickListener(v -> {
            Button confirm,cancel;
            TextView userEmail;

            myDialog = new Dialog(ProfileActivity.this);
            myDialog.setContentView(R.layout.reset_password_dialog);
            myDialog.setTitle("Reset Password");
            confirm = myDialog.findViewById(R.id.btnConfirm);
            cancel = myDialog.findViewById(R.id.btnCancel);
            userEmail = myDialog.findViewById(R.id.userEmailtoSend);
            userEmail.setText(email.getText().toString());


            confirm.setOnClickListener(v1 -> FirebaseAuth.getInstance().sendPasswordResetEmail(email.getText().toString())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                           // Log.d("TAG", "Email Sent.");
                            Toast.makeText(ProfileActivity.this, R.string.psd_link_sent,Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(ProfileActivity.this, R.string.cant_send_email,Toast.LENGTH_SHORT).show();
                        }
                    }));
         cancel.setOnClickListener(v12 -> myDialog.dismiss());
         myDialog.show();
        });
        userProfileImage.setOnClickListener(v -> mEditItemImage.openDialog());
    }

    private void pullUserData() {

        progressDialog.setMessage("Loading User data..");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            // Delay for 2seconds
            Query q = ref.child("Users").orderByValue();
            q.addValueEventListener(new ValueEventListener() {

                @SuppressLint("DefaultLocale")
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                    for (DataSnapshot dbAnswer : dataSnapshot.getChildren()) {
                        if (Objects.equals(dbAnswer.getKey(), userName)) {
                            u = dbAnswer.getValue(Users.class);
                            assert u != null;
                            tvUsername.setText(String.format("%s", u.getUsername()));
                            firstName.setText(String.format("%s", u.Fname));
                            lastName.setText(String.format("%s", u.Lname));
                            phone.setText(u.Phone);
                            email.setText(String.format("%s", u.Email));
                            Picasso.get().load(u.getPic_url()).into(userProfileImage);
                            break;


                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }, 2000);

        progressDialog.dismiss();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onResume() {
        // if the image is not null upload it to firebase
        if(imageBitmap!=null && !Objects.equals(userProfileImage.getDrawable().getConstantState(), getResources().getDrawable(R.drawable.no_image).getConstantState())){
            progressDialog.setMessage("Uploading Photo");
            progressDialog.show();
            updatePhoto();
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ProfileActivity.this);

        builder.setMessage("Are you sure you want to Exit?");
        builder.setTitle("Exit Application");
        builder.setPositiveButton(R.string.yes, (dialog, which) -> finishAffinity());
        builder.setNegativeButton(R.string.no, (dialog, which) -> dialog.cancel());

        final android.app.AlertDialog alertExit = builder.create();
        alertExit.setOnShowListener(dialog -> {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(20,0,0,0);
            Button button = alertExit.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
            button.setLayoutParams(params);
        });
        alertExit.show();

    }
    /**
     * When user click the refresh button, pull the data again from DB.
     */
    private void refreshPage(){
        pullUserData();
    }

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
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                userProfileImage.setImageBitmap(imageBitmap);

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
                        compressImage();
                        userProfileImage.setImageBitmap(imageBitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    /**
     * @param source Source Image to rotate
     * @param angle the angle to rotate the Image
     * @return Rotated Image
     */
    public Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
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
     * Clear the image was taken by the User and replace it with noImage.png
     */
    public void clearImage() {
        userProfileImage.setImageResource(R.drawable.no_image);
    }

    /**
     * Function that send the new photo to our Firebase Storage
     */
    private void updatePhoto() {

        new Thread(() -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            final byte[] data = baos.toByteArray();
            final UploadTask uploadTask = mStorage.child("Users_Profiles").child(userName).putBytes(data);
            uploadTask.addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(ProfileActivity.this,"Photo upload failed, please try again.",Toast.LENGTH_SHORT).show();
            }).addOnSuccessListener(taskSnapshot -> {
                if (taskSnapshot.getMetadata() != null) {
                    if (taskSnapshot.getMetadata().getReference() != null) {
                        Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                        result.addOnSuccessListener(uri -> {
                            imageURL = uri.toString();
                            ref.child("Users").child(userName).child("pic_url").setValue(imageURL);
                            progressDialog.dismiss();
                        });
                    }
                }
            });
        }).start();

    }
    /**
     * Register our Broadcast Receiver when opening the app.
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
}
