package com.example.iFood.Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iFood.Adapters.RecipeAdapter;
import com.example.iFood.Classes.Recipes;
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


/**

 * This is our Main screen where users can view the last 25 added recipes pulled
 * from the Database.
 * The Main screen holds a menu and adding recipe button.
 */
public class ModActivity extends AppCompatActivity {
    ConnectionBCR bcr = new ConnectionBCR();
    BottomAppBar bottomAppBar;
    FloatingActionButton addIcon;
    RecyclerView modList;
    RecipeAdapter myAdapter;
    List<Recipes> unApprovedList = new ArrayList<>();
    String activity = this.getClass().getName();
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Recipes");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mod);
        modList = findViewById(R.id.modList);

        bottomAppBar = findViewById(R.id.bottomAppBar);
        addIcon = findViewById(R.id.bottomAddIcon);


        getRecipeList();

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
                Intent about = new Intent(ModActivity.this, About.class);
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

    } // onCreate Ends

    /**
     * This function responsible for retrieve the last 25 added recipes from the Database
     * and called "refresh_lv" to refresh the List view on each result.
     */
    private void getRecipeList(){
        // enter all recipes fetched from DB to arrayList that are not approved by mod/admin

        new Thread(() -> {
            Query dbQuery = ref.orderByKey();
            dbQuery.addValueEventListener(new ValueEventListener() {
                @SuppressLint("UseCompatLoadingForDrawables")
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    unApprovedList.clear();
                    for(DataSnapshot dst : dataSnapshot.getChildren()) {
                        for(DataSnapshot dst2 : dst.getChildren()) {
                            if (dst2.exists()) {

                                String check = String.valueOf(dst2.child("approved").getValue());
                                if(check.equals("false")){
                                    Recipes rec = dst2.getValue(Recipes.class);
                                    unApprovedList.add(rec);
                                    // Call function to post all the recipes
                                    refresh_lv();
                                }

                            }
                        }
                    }
                    if(unApprovedList.size() < 1){
                        refresh_lv();
                        CoordinatorLayout coordinatorLayout = findViewById(R.id.mainLayoutMod);
                        coordinatorLayout.setBackground(getDrawable(R.drawable.all_clear_background));
                        bottomAppBar.performShow();
                    }else{
                        CoordinatorLayout coordinatorLayout = findViewById(R.id.mainLayoutMod);
                        coordinatorLayout.setBackground(getDrawable(R.drawable.background3));
                        bottomAppBar.performShow();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }).start();

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ModActivity.this);

        builder.setMessage("Are you sure you want to Exit?");
        builder.setTitle("Exit Application");
        builder.setPositiveButton(R.string.yes, (dialog, which) -> finishAffinity());
        builder.setNegativeButton(R.string.no, (dialog, which) -> dialog.cancel());

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
     * This function is responsible for refreshing our Listview with our customer Adapter.
     * spanCount controls on the amount of items on each row.
     */
    private void refresh_lv(){
        myAdapter = new RecipeAdapter(this,unApprovedList,activity);

        modList.setLayoutManager(new GridLayoutManager(this,1));

        modList.setAdapter(myAdapter);
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

