package com.example.iFood.Activities.Inbox;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.viewpager.widget.ViewPager;
import com.example.iFood.Activities.About;
import com.example.iFood.Adapters.MessageAdapter;
import com.example.iFood.Classes.Message;
import com.example.iFood.MenuFragments.AddDrawFragment;
import com.example.iFood.MenuFragments.NavDrawFragment;
import com.example.iFood.R;
import com.example.iFood.Utils.ConnectionBCR;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.example.iFood.Activities.Inbox.Inbox_New_Messages.unReadList;
import static com.example.iFood.Activities.Inbox.Inbox_New_Messages.unReadmsg;
import static com.example.iFood.Activities.Inbox.Inbox_Old_Messages.readList;
import static com.example.iFood.Activities.Inbox.Inbox_Old_Messages.readMsg;

/**
 * This screen responsible on showing the user messages.
 */
public class Inbox_new extends AppCompatActivity {
    ConnectionBCR bcr = new ConnectionBCR();
    String userName,userRole;

    public static BottomAppBar bottomAppBar;
    public static FloatingActionButton addIcon,delIcon;
    public static ArrayList<String> msgList = new ArrayList<>();
    Toolbar toolbar;
    ViewPager viewPager;
    TabLayout tabLayout;

    MessageAdapter adapterUnread,adapterRead;
    DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference().child("Messages");

    Inbox_New_Messages inboxNewMessages;
    Inbox_Old_Messages inboxOldMessages;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox_new);
        // reset delete list size
        msgList.clear();
       // set items from layout
        setItems();
        // get username and role from previous Intent
        getName_Role();
        // set Tab items
        setFragItems();
        // Get Messages list from DB
        getMessages();
        ///////////////////////////////
        toolbar = findViewById(R.id.inboxToolBar);
        setSupportActionBar(toolbar);

        tabLayout.setupWithViewPager(viewPager);

        //
        bottomAppBar.setNavigationOnClickListener(v -> {
            NavDrawFragment bottomNavFrag = new NavDrawFragment();
            Bundle bundle = new Bundle();
            bundle.putString("username",userName);
            bundle.putString("userRole",userRole);
            bottomNavFrag.setArguments(bundle);
            bottomNavFrag.show(getSupportFragmentManager(),"TAG");

        });

       ///////////////////////////////
        bottomAppBar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if(id == R.id.bottomAbout){
                Intent about = new Intent(Inbox_new.this, About.class);
                startActivity(about);
            }
            return false;
        });
        ///////////////////////////////
        addIcon.setOnClickListener(v -> {
            AddDrawFragment addIcon = new AddDrawFragment();
            Bundle bundle = new Bundle();
            bundle.putString("username",userName);
            bundle.putString("userRole",userRole);
            addIcon.setArguments(bundle);
            addIcon.show(getSupportFragmentManager(),"TAG");
        });
        delIcon.setOnClickListener(v -> {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("Deleting messages..");
            progressDialog.show();
            for(int i =0 ; msgList.size()>i;i++) {
                messagesRef.child(userName).child(msgList.get(i)).removeValue();
                refresh_lvRead();
                refresh_lvNotRead();
            }
            delIcon.hide();
            msgList.clear();
            progressDialog.dismiss();

        });
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.w("TAG","Tab selected:"+tab.getText());
                msgList.clear();
                checkDelList();
                refresh_lvRead();
                refresh_lvNotRead();

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {


            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        checkDelList();

    } // onCreate ends

    private void setFragItems() {
        inboxNewMessages = new Inbox_New_Messages();
        inboxOldMessages = new Inbox_Old_Messages();
        viewPagerAdapter viewPagerAdapter = new viewPagerAdapter(getSupportFragmentManager(), 0);
        viewPagerAdapter.addFragment(inboxNewMessages,"New Messages");
        viewPagerAdapter.addFragment(inboxOldMessages,"Old Messages");

        viewPager.setAdapter(viewPagerAdapter);

    }

    private void getName_Role() {
        userName = getIntent().getStringExtra("username");
        userRole = getIntent().getStringExtra("userRole");
    }

    /**
     * Refresh the list for readList that hold all the messages that been read by the user
     */
    private void refresh_lvRead(){

        adapterRead = new MessageAdapter(Inbox_new.this, readMsg);

        readList.setLayoutManager(new GridLayoutManager(this,1));

        readList.setAdapter(adapterRead);
    }
    /**
     * Refresh the list for runRadList that hold all the messages that have not been read by the user
     */
    private void refresh_lvNotRead(){

        adapterUnread = new MessageAdapter(Inbox_new.this, unReadmsg);

        unReadList.setLayoutManager(new GridLayoutManager(this,1));

        unReadList.setAdapter(adapterUnread);
    }

    /**
     * This function responsible on fetching information regarding the user messages
     */
    private void getMessages(){
        new Thread(() -> messagesRef.orderByKey().addValueEventListener(new ValueEventListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                unReadmsg.clear();
                readMsg.clear();
                for(DataSnapshot dst : dataSnapshot.getChildren()) {
                    if (Objects.equals(dst.getKey(), userName)) {
                        // Log.i("message","Message Key: "+dst.getKey());
                        for (DataSnapshot dst2 : dst.getChildren()) {
                            Message m = dst2.getValue(Message.class);
                            assert m != null;
                            if (m.isRead.equals("true")) {
                                readMsg.add(m);
                                refresh_lvRead();
                            } else {
                                unReadmsg.add(m);
                                refresh_lvNotRead();
                            }
                        }
                    }
                }
                 if(unReadmsg.size()<1){
                     Objects.requireNonNull(inboxNewMessages.getView()).setBackground(getDrawable(R.drawable.all_clear_background));
                 }else{
                     Objects.requireNonNull(inboxNewMessages.getView()).setBackground(getDrawable(R.drawable.background3));
                 }
                 if(readMsg.size()<1){
                     Objects.requireNonNull(inboxOldMessages.getView()).setBackground(getDrawable(R.drawable.all_clear_background));
                 }else{
                     Objects.requireNonNull(inboxOldMessages.getView()).setBackground(getDrawable(R.drawable.background3));
                 }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("TAG","Error:"+databaseError.getMessage());

            }
        })).start();


    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    /**
     * Check the actual list for delete
     */
    public static void checkDelList(){
        if(msgList.size() >= 1){
            delIcon.show();
         }else{
            delIcon.hide();
}
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

    private static class viewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> fragments = new ArrayList<>();
        private final List<String> fragmentsTitle = new ArrayList<>();



        public viewPagerAdapter(@NonNull FragmentManager fm, int behavior) {

            super(fm, behavior);

        }

        public void addFragment(Fragment fragment, String title){
            fragments.add(fragment);
            fragmentsTitle.add(title);
        }


        @NonNull
        @Override
        public Fragment getItem(int position) {

            return fragments.get(position);
        }

        @Override
        public int getCount() {

            return fragments.size();
        }

        public CharSequence getPageTitle(int position){
            return fragmentsTitle.get(position);
        }
    }

    /**
     * Variables deceleration and connect them with the layout.
     */
    public void setItems(){

        bottomAppBar = findViewById(R.id.bottomAppBar);
        addIcon = findViewById(R.id.bottomAddIcon);
        delIcon = findViewById(R.id.bottomDelIcon);
        viewPager = findViewById(R.id.inboxViewPager);
        tabLayout = findViewById(R.id.inboxTabLayout);
    }
}