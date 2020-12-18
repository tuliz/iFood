package com.example.iFood.Activities;

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
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.iFood.Classes.Users;
import com.example.iFood.R;
import com.example.iFood.Utils.ConnectionBCR;
import com.example.iFood.Utils.EditItemImage;
import com.example.iFood.Utils.FileUtils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import java.util.regex.Pattern;


/**

 * Sign-up screen that allow clients to sign up for using this application.
 */
public class SignUpActivity extends AppCompatActivity {

    // Camera Handling
    private EditItemImage mEditItemImage;
   // Password Security
    public final Pattern textPattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$");
    // Variables
    boolean tookPic=false;
    ImageView ivUserPic;
    Bitmap imageBitmap;
    Button btnSignup, btnBack, btnUserPic;
    EditText etFname, etLname, etPhone, etUsername, etPassword, etEmail;
    String Fname, Password, Username, Lname, Email, picUrl,uid,Phone;

    ConnectionBCR bcr = new ConnectionBCR();
    ProgressDialog progressDialog;

    // Connect to DB
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference DB = database.getReference();
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
    // Storage
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initData();
        btnBack.setOnClickListener(v -> {
            Intent back = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(back);
        });
        btnUserPic.setOnClickListener(v -> mEditItemImage.openDialog());
        btnSignup.setOnClickListener(v -> {
            // get values the user entered
            initUiViews();

            // check if mandatory fields are filled
            if (!tookPic) {
                imageBitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.no_image);
            }
            if (!Username.isEmpty() && !Password.isEmpty() && !Fname.isEmpty() && !Email.isEmpty() && !Phone.isEmpty())
            {
                if (!isValidEmail(Email)) {
                        Toast.makeText(SignUpActivity.this, "Email is not illegal,please enter valid Email address.", Toast.LENGTH_SHORT).show();
                        etEmail.setText("");
                        etPassword.setText("");
                        etEmail.requestFocus();
                    }
                else if(!validateTelAndMobileNo(Phone)){
                    Toast.makeText(SignUpActivity.this, "Phone must be minimum of 8 numbers", Toast.LENGTH_SHORT).show();
                    etPhone.setText("");
                    etPassword.setText("");
                    etPhone.requestFocus();

                }
                else {
                    Log.w("TAG","Email:"+isValidEmail(Email)+",Phone:"+validateTelAndMobileNo(Phone));
                        if (checkPassword()) {
                            {
                                ref.child(Username).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            // User name already exists.
                                            Toast.makeText(SignUpActivity.this, "Username already found in DB, try different Username", Toast.LENGTH_SHORT).show();
                                            etUsername.setText("");
                                            etPassword.setText("");
                                            etUsername.requestFocus();
                                        } else {
                                            final String email = etEmail.getText().toString();
                                            String password = etPassword.getText().toString();
                                            mAuth.createUserWithEmailAndPassword(email, password)
                                                    .addOnCompleteListener(task -> {
                                                        if (task.isSuccessful()) {
                                                            uid = Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getUser()).getUid();
                                                            // Sign in success, update UI with the signed-in user's information
                                                            //Log.d("TAG", "createUserWithEmail:success");
                                                            progressDialog.setMessage("Registering..");
                                                            progressDialog.setCanceledOnTouchOutside(false);
                                                            progressDialog.show();
                                                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                                            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                                            final byte[] data2 = baos.toByteArray();
                                                            final UploadTask uploadTask = storageRef.child("Users_Profiles").child(Username).putBytes(data2);
                                                            // close onSuccess mehtod
                                                            uploadTask.addOnFailureListener(exception -> {
                                                                // Handle unsuccessful uploads
                                                            }).addOnSuccessListener(taskSnapshot -> {
                                                                if (taskSnapshot.getMetadata() != null) {
                                                                    if (taskSnapshot.getMetadata().getReference() != null) {
                                                                        final Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                                                        result.addOnSuccessListener(uri -> {
                                                                            picUrl = uri.toString();
                                                                            String password1 = etPassword.getText().toString();
                                                                            addUser(Username, Fname, Phone, Lname, Email);
                                                                            mAuth.signInWithEmailAndPassword(Email, password1).addOnCompleteListener(task1 -> {
                                                                               AuthResult authResult = task1.getResult();
                                                                                assert authResult != null;
                                                                                FirebaseUser firebaseUser = authResult.getUser();
                                                                                assert firebaseUser != null;
                                                                                firebaseUser.sendEmailVerification();
                                                                            });
                                                                            // Log.i("URL", "Image URL:" + picUrl);
                                                                        });
                                                                    } // close 2nd if
                                                                } // close 1st if
                                                            }); // close OnSuccessListener
                                                            //updateUI(user);
                                                        } else {
                                                            // If sign in fails, display a message to the user.
                                                            Log.w("TAG", "createUserWithEmail:failure", task.getException());
                                                            Toast.makeText(SignUpActivity.this, "User registration failed, check Email.",
                                                                    Toast.LENGTH_SHORT).show();

                                                        }

                                                        // ...
                                                    });
                                            /*
                                            ref.orderByChild("Email").equalTo(Email).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.exists()) {
                                                        // Email already exists
                                                        Toast.makeText(SignUpActivity.this, "Email already found in DB, try different Email", Toast.LENGTH_SHORT).show();
                                                        etEmail.setText("");
                                                        etPassword.setText("");
                                                        etEmail.hasFocus();
                                                    } else {
                                                        progressDialog.setMessage("Registering..");
                                                        progressDialog.show();
                                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                                        final byte[] data2 = baos.toByteArray();
                                                        final UploadTask uploadTask = storageRef.child("Users_Profiles").child(Username).putBytes(data2);
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
                                                                                picUrl = uri.toString();
                                                                                Password = checkPass();
                                                                                addUser(Username, Password, Fname, Phone, Lname, Email);
                                                                                // Log.i("URL", "Image URL:" + picUrl);
                                                                            }
                                                                        });
                                                                    } // close 2nd if
                                                                } // close 1st if
                                                            } // close onSuccess mehtod
                                                        }); // close OnSuccessListener
                                                    } // close else onDataChange
                                                } // close  inner onDataChange

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                }
                                            });*/
                                        }
                                    } // close onDataChange

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });
                            }
                        }else{
                            if(Password.length()>=8){
                                Toast.makeText(SignUpActivity.this,"Password must contain Uppercase and numbers",Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(SignUpActivity.this,"Password must be at least 8 chars",Toast.LENGTH_SHORT).show();
                            }
                            etPassword.setText("");
                            etPassword.requestFocus();
                        }
                    }
                }
            else{
                    Toast.makeText(SignUpActivity.this, "One or more of required fields are empty", Toast.LENGTH_SHORT).show();
                    // Put focus on the empty variable
                    if (Username.isEmpty()) {
                        etUsername.requestFocus();
                        etPassword.setText("");
                    }
                    if (Password.isEmpty()) {
                        etPassword.requestFocus();
                    }
                    if (Fname.isEmpty()) {
                        etFname.requestFocus();
                        etPassword.setText("");
                    }
                    if (Email.isEmpty()) {
                        etEmail.requestFocus();
                        etPassword.setText("");
                    }
                    if(Phone.isEmpty()){
                        etPhone.requestFocus();
                        etPassword.setText("");

                    }
                }
            });


    } // OnCreate ends

    private void initUiViews() {
        Fname = etFname.getText().toString();
        Password = etPassword.getText().toString();
        Username = etUsername.getText().toString();
        // isValidEmail(Email);
        Lname = etLname.getText().toString();
        Email = etEmail.getText().toString();
        Phone = etPhone.getText().toString();
    }

    private void initData() {

        mEditItemImage = new EditItemImage(SignUpActivity.this);
        progressDialog = new ProgressDialog(SignUpActivity.this);

        ivUserPic = findViewById(R.id.ivUserPic);

        btnUserPic = findViewById(R.id.btnUserPic);
        btnSignup = findViewById(R.id.btn);
        btnBack = findViewById(R.id.btnBack);

        etFname = findViewById(R.id.etFname);
        etLname = findViewById(R.id.etLname);
        etPhone = findViewById(R.id.etPhone);
        etUsername = findViewById(R.id.etUser);
        etPassword = findViewById(R.id.etPass);
        etEmail = findViewById(R.id.etEmail);
    }

    /**
     * This function make sure Password not short ( for security )
     * @return true if password length is greater then 8 and match the pattern ( at lease 1 upper case,1 loser case and 1 number )
     */
    private boolean checkPassword() {
        if(Password.length()>=8){
            return textPattern.matcher(Password).matches();
        }
        return false;
    }
    /**
     * Function responsible to add new user to that Database
     * @param user Username entered by the client
     * @param Fname Client First Name
     * @param phone Client Phone number
     * @param Lname Client Last Name
     * @param Email Client Email
     */
    private void addUser(String user,String Fname, String phone, String Lname, String Email) {
        // add to DB

        Users newUser = new Users(user, Email, phone, Fname, Lname, picUrl,"member",uid);
        newUser.pic_url = picUrl;
        DB.child("Users").child(newUser.getUsername()).setValue(newUser);
        progressDialog.dismiss();
        Toast.makeText(SignUpActivity.this, "Sign-up success,check your Email for verification.. ", Toast.LENGTH_SHORT).show();
        Intent goBack = new Intent(SignUpActivity.this, LoginActivity.class);
        startActivity(goBack);
        finish();
    }
    /**
     * This is the camera handling function that responsible for the image
     *
     */
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
                ivUserPic.setImageBitmap(imageBitmap);

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
                        ivUserPic.setImageBitmap(imageBitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
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
    private void compressImage() {
        imageBitmap = scaleDown(imageBitmap,150,true);
       // Log.d("TAG","End function,Image width is:"+imageBitmap.getWidth()+",Image height is:"+imageBitmap.getHeight());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);//Compression quality, here 100 means no compression, the storage of compressed data to baos
        int options = 90;
        while (baos.toByteArray().length / 1024 > 150) {  //Loop if compressed picture is greater than 400kb, than do compression
            baos.reset();//Reset baos is empty baos
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);//The compression options%, storing the compressed data to the baos
            options -= 10;//Every time reduced by 10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//The storage of compressed data in the baos to ByteArrayInputStream
        imageBitmap = BitmapFactory.decodeStream(isBm, null, null);//The ByteArrayInputStream data generation
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

    /**
     * Function to scale down Image size for better loading.
     * @param givenImage Image provided from user camera.
     * @param maxImageSize Maximum Image size by pixel.
     * @return scaled Image.
     */
    public static Bitmap scaleDown(Bitmap givenImage, float maxImageSize,
                                   boolean filter) {


        float ratio = Math.min(
                 maxImageSize / givenImage.getWidth(),
                 maxImageSize / givenImage.getHeight());
        int width = Math.round( ratio * givenImage.getWidth());
        int height = Math.round( ratio * givenImage.getHeight());
        if (ratio >= 1.0){

            return givenImage;

        }

        return Bitmap.createScaledBitmap(givenImage, width,height, filter);
    }
    /**
     * This function is validate the user input in the Email EditText to make sure its an actual Email.
     * @param target is the Email the client entered
     * @return return true if the Email is valid and false if not
     */
    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static boolean validateTelAndMobileNo(String mobileNo){
        return mobileNo.matches("^[+]?[0-9]{8,15}$");
    }

    /**
     * Reset the bitmap and load a default image in the Imageview.
     */
    public void clearImage() {
        ivUserPic.setImageResource(R.drawable.no_image);
    }
    }




