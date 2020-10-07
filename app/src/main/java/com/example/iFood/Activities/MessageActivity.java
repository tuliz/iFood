package com.example.iFood.Activities;


import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.iFood.R;
import com.example.iFood.Utils.ConnectionBCR;


/**

 * Message Activity that allow the user to view the message he received.
 */
public class MessageActivity extends AppCompatActivity {
    ConnectionBCR bcr = new ConnectionBCR();
    TextView fromUser,msgTitle,msgContent,msgDate;
    String userName,frmUser,dateMsg,contentMsg,titleMsg,activity,userRole,msgID;
    Button btnReplay,btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        /////// Get data from previous activity
        activity = getIntent().getStringExtra("activity");
        userName = getIntent().getStringExtra("username");
        userRole = getIntent().getStringExtra("userRole");
        frmUser = getIntent().getStringExtra("From User");
        dateMsg = getIntent().getStringExtra("Message Date");
        contentMsg = getIntent().getStringExtra("Message Content");
        titleMsg= getIntent().getStringExtra("Message Title");
        msgID = getIntent().getStringExtra("msgID");

        ///////
        fromUser = findViewById(R.id.tvFrom);
        msgTitle = findViewById(R.id.tvMessageTitle);
        msgContent = findViewById(R.id.tvMessageContent);
        msgDate = findViewById(R.id.tvMessageDate);

        btnClose = findViewById(R.id.btnCloseMessage);
        btnReplay = findViewById(R.id.btnReplayMessage);

        btnReplay.setOnClickListener(v -> {
            Intent sendMsg = new Intent(MessageActivity.this, SendMessage.class);
            sendMsg.putExtra("username",userName);
            sendMsg.putExtra("userRole",userRole);
            sendMsg.putExtra("activity",activity);
            sendMsg.putExtra("fromUser",fromUser.getText().toString());
            sendMsg.putExtra("To User",fromUser.getText().toString());
            sendMsg.putExtra("msgTitle",msgTitle.getText().toString());
            startActivity(sendMsg);
            finish();
        });
        btnClose.setOnClickListener(v -> finish());
        fromUser.setText(frmUser);
        msgContent.setText(contentMsg);
        msgDate.setText(dateMsg);
        msgTitle.setText(titleMsg);

    } // onCreate ends

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
