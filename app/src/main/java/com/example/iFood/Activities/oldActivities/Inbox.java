package com.example.iFood.Activities.oldActivities;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iFood.Activities.About;
import com.example.iFood.Adapters.MessageAdapter;
import com.example.iFood.Classes.Message;
import com.example.iFood.MenuFragments.AddDrawFragment;
import com.example.iFood.MenuFragments.NavDrawFragment;
import com.example.iFood.R;
import com.example.iFood.Utils.ConnectionBCR;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**

 * This screen responsible on showing the user messages.
 */

public class Inbox extends AppCompatActivity {
    ConnectionBCR bcr = new ConnectionBCR();
    String userName;
    BottomAppBar bottomAppBar;
    FloatingActionButton addIcon;
    MessageAdapter adapterUnread,adapterRead;
    RecyclerView unReadList,readList;
    ArrayList<Message> unReadmsg = new ArrayList<>();
    ArrayList<Message> readMsg = new ArrayList<>();
    DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference().child("Messages");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        bottomAppBar = findViewById(R.id.bottomAppBar);
        addIcon = findViewById(R.id.bottomAddIcon);

        unReadList = findViewById(R.id.unread_message);
        readList = findViewById(R.id.opened_message);

        userName = getIntent().getStringExtra("username");

        ///////////////////////////////
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        bottomAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavDrawFragment bottomNavFrag = new NavDrawFragment();
                Bundle bundle = new Bundle();
                bundle.putString("username",userName);
                bundle.putString("userRole",getIntent().getStringExtra("userRole"));
                bottomNavFrag.setArguments(bundle);
                bottomNavFrag.show(getSupportFragmentManager(),"TAG");

            }
        });
        // get the message list from DB
        getMessages();
        ///////////////////////////////
        bottomAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if(id == R.id.bottomAbout){
                    Intent about = new Intent(Inbox.this, About.class);
                    startActivity(about);
                }
                return false;
            }

        });
        ///////////////////////////////
        addIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddDrawFragment addIcon = new AddDrawFragment();
                Bundle bundle = new Bundle();
                bundle.putString("username",userName);
                bundle.putString("userRole",getIntent().getStringExtra("userRole"));
                addIcon.setArguments(bundle);
                addIcon.show(getSupportFragmentManager(),"TAG");
            }
        });


    } // onCreate ends

    /**
     * This function responsible on fetching information regarding the user messages
     */
    private void getMessages(){
        messagesRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                unReadmsg.clear();
                readMsg.clear();
                for(DataSnapshot dst : dataSnapshot.getChildren()) {
                    if (dst.getKey().equals(userName)) {
                       // Log.i("message","Message Key: "+dst.getKey());
                          for (DataSnapshot dst2 : dst.getChildren()) {
                              Message m = dst2.getValue(Message.class);
                              assert m != null;
                              if (m.isRead.equals("true")) {
                                    readMsg.add(m);
                                    Log.i("key","message key is:"+m.msgID);
                                    //Log.i("value","added value to readMsg, size:"+readMsg.size());
                                    refresh_lvRead();
                                } else {
                                    unReadmsg.add(m);
                                    Log.i("value","added value to unReadmsg, size:"+unReadmsg.size());
                                    refresh_lvNotRead();
                                }
                            }
                        }
                    }
                }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    /**
     * Refresh the list for readList that hold all the messages that been read by the user
     */
    private void refresh_lvRead(){

        adapterRead = new MessageAdapter(Inbox.this, readMsg);

        readList.setLayoutManager(new GridLayoutManager(this,1));

        readList.setAdapter(adapterRead);
    }
    /**
     * Refresh the list for runRadList that hold all the messages that have not been read by the user
     */
    private void refresh_lvNotRead(){

        adapterUnread = new MessageAdapter(Inbox.this, unReadmsg);

        unReadList.setLayoutManager(new GridLayoutManager(this,1));

        unReadList.setAdapter(adapterUnread);
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
}
