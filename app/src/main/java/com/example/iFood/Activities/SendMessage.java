package com.example.iFood.Activities;

import android.app.ProgressDialog;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.iFood.Classes.Message;
import com.example.iFood.Classes.Users;
import com.example.iFood.Notification.APIService;
import com.example.iFood.Notification.Client;
import com.example.iFood.Notification.Data;
import com.example.iFood.Notification.MyResponse;
import com.example.iFood.Notification.NotificationSender;
import com.example.iFood.Notification.Token;
import com.example.iFood.R;
import com.example.iFood.Utils.ConnectionBCR;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**

 * This Activity is for sending message for other users regarding their recipes.
 *
 */
public class SendMessage extends AppCompatActivity {
    private ConnectionBCR bcr = new ConnectionBCR();
    String formattedDate,userName, uniqueID,toUser,url,activity;
    TextView etFrom,etTo;
    private EditText etTitle,etContent;
    Button btnSend,btnClose;
    private ProgressDialog progressDialog;
    //
    // Connect to DB
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Messages");
    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users");
    // Storage
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    // Service
    String uid="";
    APIService apiService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);
        // Get information from intent
        activity = getIntent().getStringExtra("activity");
        userName = getIntent().getStringExtra("username");
        toUser = getIntent().getStringExtra("To User");

        // get current Date for the message
        formattedDate = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());

        // Declare Variables and connect to layout
        etTo = findViewById(R.id.tvTo);
        etFrom = findViewById(R.id.tvFrom);
        etTitle = findViewById(R.id.tvMessageTitle);
        etContent = findViewById(R.id.tvMessageContent);
        btnSend = findViewById(R.id.btnReplayMessage);
        btnClose = findViewById(R.id.btnCloseMessage);

        // Progress bar
        progressDialog = new ProgressDialog(SendMessage.this);
        progressDialog.setCanceledOnTouchOutside(false);
        // Call function to get the user image.
        getUserURL();
        getUserID();
        // Setting variable with related text
        etFrom.setText(String.format("%s", userName));
        etTo.setText(String.format("%s",toUser));
        etTitle.setText(String.format("%s",getIntent().getStringExtra("msgTitle")));

        // Listeners
        btnClose.setOnClickListener(v -> {
            // go back to previous screen
            finish();
        });
        btnSend.setOnClickListener(v -> {
            if(!etTitle.getText().toString().isEmpty() || !etContent.getText().toString().isEmpty()){
                if(etContent.getText().toString().length() > 10) {
                    // Make sure there is a title + content and the call function here to compose the message
                    progressDialog.setMessage("Sending message, please wait.");
                    progressDialog.show();
                    composeMsg();
                }else{
                    Toast.makeText(SendMessage.this,"Message content too short.",Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(SendMessage.this,"One or more required fields are empty.",Toast.LENGTH_SHORT).show();
                if(etTitle.getText().toString().isEmpty())
                    etTitle.hasFocus();
                if(etContent.getText().toString().isEmpty())
                    etContent.hasFocus();
            }
        });
        UpdateToken();
    } // onCreate ends

    private void getUserID() {
        DatabaseReference user_uid = FirebaseDatabase.getInstance().getReference().child("Users").child(toUser);
        user_uid.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users user = snapshot.getValue(Users.class);
                assert user != null;
                //Log.d("TAG","User:"+user.toString());
                uid = user.getUid();
               // Log.d("TAG","uid:"+uid);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    /**
     * Function is responsible on fetching the user image from the user details.
     * if not found, get the noImage file from the Database
     */
    public void getUserURL(){

        userRef.child(userName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                   // Log.i("message","key is:"+dataSnapshot.getKey());
                Users u = dataSnapshot.getValue(Users.class);
                assert u != null;
                url = u.getPic_url();
                 //   Log.d("TAG","URL is:"+u.getPic_url());
                if(url==null){
                       storageRef.child("Photos").child("noImage").getDownloadUrl().addOnSuccessListener(uri -> {
                       Log.d("TAG","url:"+uri.toString());
                       url = uri.toString();
                       userRef.child(userName).child("pic_url").setValue(url);
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    private void UpdateToken(){
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        String refreshToken= FirebaseInstanceId.getInstance().getToken();
        Token token= new Token(refreshToken);
        FirebaseDatabase.getInstance().getReference("Tokens").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).setValue(token);
    }
    /**
     * Function is responsible for saving the messages and the information in the Database.
     */
    public void composeMsg(){

        final String title,userImageUrl,message,toUser;
        final String shortMsg;


        title = etTitle.getText().toString();
        message = etContent.getText().toString();
        if(etContent.getText().toString().length()>65){
            shortMsg = etContent.getText().toString().substring(0,50);
        }else{
            shortMsg = etContent.getText().toString();
        }
        toUser = etTo.getText().toString();
        userImageUrl = url;
        Message msg;
        uniqueID = String.valueOf(ref.push().getKey());
        msg = new Message(title,userImageUrl,message,toUser,formattedDate,userName, uniqueID,"false");
        Log.w("TAG","Message: "+msg.toString());
        ref.child(toUser).child(uniqueID).setValue(msg);
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

            FirebaseDatabase.getInstance().getReference().child("Tokens").child(uid).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue(String.class) != null) {
                        String userToken = snapshot.getValue(String.class);
                        //Log.w("TAG","Token:"+userToken);
                        sendNotifications(userToken, title, shortMsg);
                       // Log.w("TAG", "Sent notification.");
                    } else {
                        Log.w("TAG", "Token not found.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                              Log.w("TAG","Error:"+error.getMessage());
                }
            });

        progressDialog.dismiss();
        finish();
    }
    public void sendNotifications(String usertoken, String title, String message) {
        Data data = new Data(title, message);
        NotificationSender sender = new NotificationSender(data, usertoken);
        apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                if (response.code() == 200) {
                    //Log.w("TAG","code:"+response.code());
                    //Log.w("TAG","body:"+response.body().success);
                    assert response.body() != null;
                    if (response.body().success != 1) {
                        Toast.makeText(SendMessage.this, "Failed ", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {
                     Log.w("TAG","Error:"+t.getMessage());
            }
        });
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
