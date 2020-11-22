package com.example.iFood.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iFood.Adapters.RecipeAdapter;
import com.example.iFood.Classes.Recipes;
import com.example.iFood.MenuFragments.AddDrawFragment;
import com.example.iFood.MenuFragments.NavDrawFragment;
import com.example.iFood.Notification.Token;
import com.example.iFood.R;
import com.example.iFood.Utils.ConnectionBCR;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**

 * This is our Main screen where users can view the last 25 added recipes pulled
 * from the Database.
 * The Main screen holds a menu and adding recipe button.
 */
public class MainActivity extends AppCompatActivity {
    ConnectionBCR bcr = new ConnectionBCR();
    BottomAppBar bottomAppBar;
    FloatingActionButton addIcon;
    RecyclerView myrecyclerView;
    RecipeAdapter myAdapter;
    SharedPreferences pref;
    List<Recipes> recipes1 = new ArrayList<>();
    String activity = this.getClass().getName(),userRole,userName;
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Recipes");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myrecyclerView = findViewById(R.id.recyclerView_id);
        myrecyclerView.setAdapter(myAdapter);
        bottomAppBar = findViewById(R.id.bottomAppBar);
        addIcon = findViewById(R.id.bottomAddIcon);


        getRecipeList();
        pref = getSharedPreferences("userData",MODE_PRIVATE);
        if(pref.contains("userRole")){
            userRole = pref.getString("userRole",null);
            userName = pref.getString("username",null);
        }else{
            userRole = getIntent().getStringExtra("userRole");
            userName = getIntent().getStringExtra("username");
        }

        ///////////////////////////////
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        bottomAppBar.setNavigationOnClickListener(v -> {
            NavDrawFragment bottomNavFrag = new NavDrawFragment();
            Bundle bundle = new Bundle();
            bundle.putString("username",userName);
            bundle.putString("userRole",userRole);
            bottomNavFrag.setArguments(bundle);
            bottomNavFrag.show(getSupportFragmentManager(),"bottomNav");

        });
        ///////////////////////////////
        bottomAppBar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if(id == R.id.bottomAbout){
                Intent about = new Intent(MainActivity.this, About.class);
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
            addIcon.show(getSupportFragmentManager(),"addIconNav");
        });


        UpdateToken();
    } // onCreate Ends

    private void UpdateToken(){
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        String refreshToken= FirebaseInstanceId.getInstance().getToken();
        Token token= new Token(refreshToken);
       // ref.child("Users").child(userName).child("token").setValue(token);
        FirebaseDatabase.getInstance().getReference("Tokens").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).setValue(token);
    }

    /**
     * This function responsible for retrieve the last 25 added recipes from the Database
     * and called "refresh_lv" to refresh the List view on each result.
     */
    private void getRecipeList(){
        // enter all recipes fetched from DB to arrayList
        new Thread(() -> {

            Query dbQuery = ref.orderByKey().limitToLast(100);
            dbQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    recipes1.clear();
                    for(DataSnapshot dst : dataSnapshot.getChildren()) {
                        for(DataSnapshot dst2 : dst.getChildren()) {
                            if (dst2.exists()) {
                                //Log.i("approved","Approve:"+dst2.child("approved").getValue());
                                String check = String.valueOf(dst2.child("approved").getValue());
                                if(check.equals("true") && recipes1.size() <= 25){
                                    Recipes rec = dst2.getValue(Recipes.class);
                                    recipes1.add(rec);
                                    // Call function to post all the recipes
                                    refresh_lv();
                                }

                            }
                        }
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
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

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

    @Override
    protected void onResume() {
        super.onResume();
        getRecipeList();
    }

    /**
     * This function is responsible for refreshing our Listview with our customer Adapter.
     * spanCount controls on the amount of items on each row.
     */
    private void refresh_lv(){

        myAdapter = new RecipeAdapter(this,recipes1,activity);

        myrecyclerView.setLayoutManager(new GridLayoutManager(this,1));

        myrecyclerView.setAdapter(myAdapter);
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

