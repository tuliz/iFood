package com.example.iFood.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;

import com.example.iFood.Adapters.RejectAdapter;
import com.example.iFood.Classes.RejectedRecipe;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RejectedListActivity extends AppCompatActivity {
    ConnectionBCR bcr = new ConnectionBCR();
    BottomAppBar bottomAppBar;
    ProgressDialog progressDialog;
    FloatingActionButton addIcon;
    RecyclerView rejectedList;
    RejectAdapter myAdapter;
    List<RejectedRecipe> rejectedRecipeList = new ArrayList<>();
    DatabaseReference deleted_list = FirebaseDatabase.getInstance().getReference().child("Deleted List");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rejected_list);


        rejectedList = findViewById(R.id.rejectList);
        bottomAppBar = findViewById(R.id.bottomAppBar);
        addIcon = findViewById(R.id.bottomAddIcon);

        //////////

        getRejectedList();

        ///////////////////////////////
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        bottomAppBar.setNavigationOnClickListener(v -> {
            NavDrawFragment bottomNavFrag = new NavDrawFragment();
            Bundle bundle = new Bundle();
            bundle.putString("username",getIntent().getStringExtra("username"));
            bundle.putString("userRole",getIntent().getStringExtra("userRole"));
            bottomNavFrag.setArguments(bundle);
            bottomNavFrag.show(getSupportFragmentManager(),"TAG");

        });
        ///////////////////////////////
        bottomAppBar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if(id == R.id.bottomAbout){
                Intent about = new Intent(RejectedListActivity.this, About.class);
                startActivity(about);
            }
            return false;
        });
        ///////////////////////////////
        addIcon.setOnClickListener(v -> {
            AddDrawFragment addIcon = new AddDrawFragment();
            Bundle bundle = new Bundle();
            bundle.putString("username",getIntent().getStringExtra("username"));
            bundle.putString("userRole",getIntent().getStringExtra("userRole"));
            addIcon.setArguments(bundle);
            addIcon.show(getSupportFragmentManager(),"TAG");
        });

    } // onCreate ends


    private void getRejectedList(){
        // enter all recipes fetched from DB to arrayList that are not approved by mod/admin
        progressDialog = new ProgressDialog(RejectedListActivity.this);
        progressDialog.setMessage("Fetching information");
        progressDialog.show();
        new Thread(() -> {
            Query dbQuery = deleted_list.orderByKey();
            dbQuery.addValueEventListener(new ValueEventListener() {
                @SuppressLint("UseCompatLoadingForDrawables")
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    rejectedRecipeList.clear();
                    for(DataSnapshot dst : dataSnapshot.getChildren()) {
                        for(DataSnapshot dst2 : dst.getChildren()) {
                            if (dst2.exists()) {

                                RejectedRecipe rec = dst2.getValue(RejectedRecipe.class);
                                rejectedRecipeList.add(rec);
                                Log.d("TAG","Value:"+rec);
                                // Call function to post all the recipes
                                refresh_lv();


                            }
                        }
                    }
                    if(rejectedRecipeList.size() < 1){
                        refresh_lv();
                        CoordinatorLayout coordinatorLayout = findViewById(R.id.mainLayoutReject);
                        coordinatorLayout.setBackground(getDrawable(R.drawable.all_clear_background));

                    }else{
                        CoordinatorLayout coordinatorLayout = findViewById(R.id.mainLayoutReject);
                        coordinatorLayout.setBackground(getDrawable(R.drawable.background3));

                    }
                    progressDialog.dismiss();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }).start();

    }


    /**
     * This function is responsible for refreshing our Listview with our customer Adapter.
     * spanCount controls on the amount of items on each row.
     */
    private void refresh_lv(){
        myAdapter = new RejectAdapter(this,rejectedRecipeList);

        rejectedList.setLayoutManager(new GridLayoutManager(this,1));

        rejectedList.setAdapter(myAdapter);
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