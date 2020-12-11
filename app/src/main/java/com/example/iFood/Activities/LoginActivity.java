package com.example.iFood.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.iFood.Classes.Users;
import com.example.iFood.Notification.MyFireBaseMessagingService;
import com.example.iFood.R;
import com.example.iFood.Utils.ConnectionBCR;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

/**
 *
 * This is our first screen that responsible of the users Login.
 * This screen also provides the client the ability to create himself a user by pressing the "Sign-up" button.
 */
public class LoginActivity extends AppCompatActivity {
    ConnectionBCR bcr = new ConnectionBCR();
    SharedPreferences pref;
    String userRole;
    Button btnSignIn;
    ProgressDialog progressDialog;
    Dialog myDialog;
    TextView reset_password,resend_authEmail,signupText;
    SwitchCompat rmbMe;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    Dialog sendEmailDialog;
    EditText etUser,etPass;
    Users u;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final Intent intent = new Intent(this, MyFireBaseMessagingService.class);
        startService(intent);

        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        // Declare variables ( function at the end )
        setVars();

        // Check if the user did asked to be remembered last time he logged in the Application
        pref = getSharedPreferences("userData",MODE_PRIVATE);
        if(pref.contains("username")&&pref.contains("userRole")){

            Intent main = new Intent(LoginActivity.this, MainActivity.class);
            main.putExtra("username", pref.getString("username",null));
            main.putExtra("userRole",pref.getString("userRole",null));
          //  Log.i("userRole","role in shared pref is:"+pref.getString("userRole",null));
            startActivity(main);
            finish();
        }


        signupText.setOnClickListener(v -> {
            Intent signUp = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(signUp);
        });
        resend_authEmail.setOnClickListener(v -> {
            // Getting user from Firebase
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            // If user is null meaning he never signed up before in this device
            if(user!= null) {
                Log.w("TAG","User verified:"+user.isEmailVerified());
                if (!user.isEmailVerified()) {
                    Toast.makeText(LoginActivity.this, "Email Sent", Toast.LENGTH_SHORT).show();
                    user.sendEmailVerification();
                }
                else {
                    Toast.makeText(LoginActivity.this, R.string.already_verified, Toast.LENGTH_SHORT).show();
                }
            }else{
                // Bring up a dialog to the user to enter his details
                // Declare variables
                Button input_ok,input_cancel;
                TextView emailInput,pwdInput;
                sendEmailDialog = new Dialog(LoginActivity.this);
                // Set Layout to Dialog
                sendEmailDialog.setContentView(R.layout.resend_email_dialog);
                // Set Title to Dialog
                sendEmailDialog.setTitle("verification email");
                // Set Variables
                input_cancel = sendEmailDialog.findViewById(R.id.input_cancel);
                input_ok = sendEmailDialog.findViewById(R.id.input_ok);
                emailInput = sendEmailDialog.findViewById(R.id.email_input);
                pwdInput = sendEmailDialog.findViewById(R.id.pwd_input);
                // OnClickListeners
                input_cancel.setOnClickListener(v13 -> sendEmailDialog.dismiss());
                input_ok.setOnClickListener(v14 -> {
                    // if one of the fields is empty, promote a toast to the user
                    if(emailInput.getText().toString().isEmpty() || pwdInput.getText().toString().isEmpty()){
                        Toast.makeText(LoginActivity.this, "Please enter your email and password", Toast.LENGTH_SHORT).show();
                    }
                    // Check if the give email is actually registered in our DB
                   else {
                        mAuth.fetchSignInMethodsForEmail(emailInput.getText().toString()).addOnCompleteListener(task -> {
                            // if there is values, mean user registered this email to our DB before, so we could make a sign in and send a verification email
                            if (!Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getSignInMethods()).isEmpty()) {
                                mAuth.signInWithEmailAndPassword(emailInput.getText().toString(), pwdInput.getText().toString()).addOnCompleteListener(taskSignIn -> {
                                    AuthResult authResult = taskSignIn.getResult();
                                    assert authResult != null;
                                    FirebaseUser firebaseUser = authResult.getUser();
                                    assert firebaseUser != null;
                                    firebaseUser.sendEmailVerification();
                                    //Log.w("TAG", "Email sent to:" + emailInput.getText().toString());
                                    sendEmailDialog.dismiss();
                                }).addOnFailureListener(e ->
                                        //Log.w("TAG", "Exception:" + e.getMessage()));
                                Toast.makeText(LoginActivity.this, "Wrong Email/Password", Toast.LENGTH_SHORT).show());
                            } else {
                                Toast.makeText(LoginActivity.this, "No user found with this email address", Toast.LENGTH_SHORT).show();
                                sendEmailDialog.dismiss();
                            }
                        }).addOnFailureListener(e -> {
                            //Log.w("TAG", "Error:" + e.getMessage());
                        });

                    }
                });
                sendEmailDialog.show();
                // Expand the width of dialog to maximum screen width
                Window window = sendEmailDialog.getWindow();
                // Make sure the given window is not null
                assert window != null;
                window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            }
        });
        reset_password.setOnClickListener(v -> {

            if (etUser.getText().toString().isEmpty() || !isValidEmail(etUser.getText().toString())) {
                    etUser.setError("Must enter a valid email address");
                    etUser.requestFocus();
            } else{
                // Declaring Dialog variables
            Button confirm, cancel;
            TextView userEmail;
            // Attaching variables to XML layout
            myDialog = new Dialog(LoginActivity.this);
            myDialog.setContentView(R.layout.reset_password_dialog);
            myDialog.setTitle(R.string.reset_password);
            confirm = myDialog.findViewById(R.id.btnConfirm);
            cancel = myDialog.findViewById(R.id.btnCancel);
            userEmail = myDialog.findViewById(R.id.userEmailtoSend);
            userEmail.setText(etUser.getText().toString());
            // onClickLisnters
            confirm.setOnClickListener(v1 -> {
                if (!userEmail.getText().toString().isEmpty() && isValidEmail(userEmail.getText().toString())) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(userEmail.getText().toString())
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    //Log.d("TAG", "Email Sent.");
                                    Toast.makeText(LoginActivity.this, R.string.psd_link_sent, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(LoginActivity.this, R.string.cant_send_email, Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(LoginActivity.this, "Please enter a valid Email address",
                            Toast.LENGTH_SHORT).show();

                }
            });
            cancel.setOnClickListener(v12 -> myDialog.dismiss());
            myDialog.show();

            // Expand the width of dialog to maximum screen width
            Window window = myDialog.getWindow();
            // Make sure the given window is not null
            assert window != null;
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }

        });
        btnSignIn.setOnClickListener(v -> {

            String email = etUser.getText().toString();
            String password = etPass.getText().toString();
            if(email.isEmpty()||password.isEmpty()){
                Toast.makeText(LoginActivity.this, R.string.enter_emailpassword,Toast.LENGTH_SHORT).show();
            }else {
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage(getString(R.string.connecting_login));
                progressDialog.show();
                progressDialog.setCanceledOnTouchOutside(false);
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                //Log.d("TAG", "signInWithEmail:success");
                                final FirebaseUser user = mAuth.getCurrentUser();
                                assert user != null;
                                if (user.isEmailVerified()) {
                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                                    ref.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            boolean foundUser = false;
                                            for (DataSnapshot dbAnswer : snapshot.getChildren()) {
                                                u = dbAnswer.getValue(Users.class);
                                                assert u != null;
                                                if (u.Email.equals(user.getEmail())) {
                                                    foundUser = true;
                                                    userRole = u.userRole;
                                                    break;
                                                }
                                            }
                                            if (foundUser) {

                                                if (rmbMe.isChecked()) {
                                                    saveData();
                                                    Intent main = new Intent(LoginActivity.this, MainActivity.class);
                                                    main.putExtra("username", u.getUsername());
                                                    main.putExtra("userRole", u.userRole);
                                                    startActivity(main);

                                                } else {
                                                    Intent main = new Intent(LoginActivity.this, MainActivity.class);
                                                    main.putExtra("username", u.getUsername());
                                                    main.putExtra("userRole", userRole);
                                                    startActivity(main);

                                                }
                                                progressDialog.dismiss();
                                                finish();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                } else {
                                    progressDialog.dismiss();
                                    // If sign in fails, display a message to the user.
                                    //Log.w("TAG", "signInWithEmail:failure", task.getException());
                                    Toast.makeText(LoginActivity.this, R.string.email_verify,
                                            Toast.LENGTH_SHORT).show();

                                    // ...
                                }
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, R.string.incorrect_emailpassword,
                                        Toast.LENGTH_SHORT).show();
                            }

                            // ...
                        });
            }
        });
    } // onCreate ends

    /**
     * Declaring variables and connecting them to the XML layout related to LoginActivity.
     */
    private void setVars() {
        signupText = findViewById(R.id.tv2);
        btnSignIn = findViewById(R.id.btnSign);
        etUser = findViewById(R.id.etUser);
        etPass = findViewById(R.id.etPass);
        rmbMe = findViewById(R.id.btnRemember);
        reset_password = findViewById(R.id.reset_password);
        resend_authEmail = findViewById(R.id.resendVerificationEmail);
    }


    /**
     * This function responsible for saving the user data if he wish to so he won't need to reenter his login
     * information next time he open the App
     */
    public void saveData(){
        SharedPreferences.Editor saveInfo = getSharedPreferences("userData",MODE_PRIVATE).edit();
        saveInfo.putString("username",u.getUsername());
        saveInfo.putString("userRole",userRole);
        saveInfo.apply();
        saveInfo.commit();
    }

     public void onBackPressed(){
         //Log.d("TAG", "onBackPressed");
         AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
         builder.setMessage("Are you sure you want to Exit?");
         builder.setNegativeButton(R.string.no, (dialog, which) -> dialog.cancel());
         builder.setPositiveButton(R.string.yes, (dialog, which) -> finish());

         final AlertDialog alertExit = builder.create();
         alertExit.setOnShowListener(dialog -> {
             LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                     LinearLayout.LayoutParams.WRAP_CONTENT,
                     LinearLayout.LayoutParams.WRAP_CONTENT
             );
             params.setMargins(20,0,0,0);
             Button button = alertExit.getButton(AlertDialog.BUTTON_POSITIVE);
             button.setLayoutParams(params);
         });

         alertExit.show();
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

    protected void onStop() {
        super.onStop();
        unregisterReceiver(bcr);
    }

    /**
     * Check if the Email address is valid with the expression of example@.something.com
     * @param target Gets Email string.
     * @return if Email address is valid or not.
     */
    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}
