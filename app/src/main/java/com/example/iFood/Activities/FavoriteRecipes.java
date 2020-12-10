package com.example.iFood.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This screen is responsible for showing the user his favorite lists.
 */
public class FavoriteRecipes extends AppCompatActivity {
    ConnectionBCR bcr = new ConnectionBCR();
    String userName,userRole;
    String activity = this.getClass().getName();
    BottomAppBar bottomAppBar;
    FloatingActionButton addIcon;
    TextView tvMyRecipesCount;
    RecyclerView favViewList;
    RecipeAdapter myAdapter;
    List<Recipes> myFavList = new ArrayList<>();

    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Favorites");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_recipes);

        /// get userName from intent data
        userName = getIntent().getStringExtra("username");
        userRole = getIntent().getStringExtra("userRole");

        /////
        setVariables();

        bottomAppBar.setNavigationOnClickListener(v -> {
            NavDrawFragment bottomNavFrag = new NavDrawFragment();
            Bundle bundle = new Bundle();
            bundle.putString("username",getIntent().getStringExtra("username"));
            bundle.putString("userRole",getIntent().getStringExtra("userRole"));
            bottomNavFrag.setArguments(bundle);
            bottomNavFrag.show(getSupportFragmentManager(),"bottomNav");

        });
        ///////////////////////////////
        bottomAppBar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if(id == R.id.bottomAbout){
                Intent about = new Intent(FavoriteRecipes.this, About.class);
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
            addIcon.show(getSupportFragmentManager(),"addIconNav");
        });

        getFavListbyUser();



        // Check if user have nothing in his favorites, if so, pop up Dialog to move him to search.


    } // onCreate ends

    /**
     * Declaring variables.
     */
    private void setVariables() {
        favViewList = findViewById(R.id.favList);
        bottomAppBar = findViewById(R.id.bottomAppBar);
        addIcon = findViewById(R.id.bottomAddIcon);
        tvMyRecipesCount = findViewById(R.id.tvMyRecipesCount);
    }


    /**
     * This function is responsible for refreshing our ListView with our customer Adapter.
     * spanCount controls on the amount of items on each row.
     */
    private void refresh_lv(){

        myAdapter = new RecipeAdapter(this,myFavList,activity);

        favViewList.setLayoutManager(new GridLayoutManager(this,1));

        favViewList.setAdapter(myAdapter);
    }

    /**
     * This function is responsible for retrieving all the recipes the user liked from the Database.
     */
    private void getFavListbyUser(){
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myFavList.clear();
                for(DataSnapshot dst : dataSnapshot.getChildren()){
                      if(Objects.equals(dst.getKey(), userName))
                        for(DataSnapshot userRecipes : dst.getChildren()){
                           Recipes results = userRecipes.getValue(Recipes.class);
                           myFavList.add(results);
                            refresh_lv();

                   }

                }
                if(myFavList.size()<1)
                   favSize();
                tvMyRecipesCount.setText(String.valueOf(myFavList.size()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });





    }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(FavoriteRecipes.this);

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
     * Register our Broadcast Receiver when opening the app.
     */
    @Override
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
     * On resuming ( if coming back from Recipe Activity ), to refresh the list if user removed a recipe
     * from his favorite list.
     */
    @Override
    protected void onResume() {
        super.onResume();
        refresh_lv();

    }
    /**
     * This function responsible for promoting a user a Dialog to ask if he wants to search for a Recipe
     * if his list is empty.
     */
    private void favSize(){

        if(myFavList.size() < 1){

            AlertDialog.Builder builder = new AlertDialog.Builder(FavoriteRecipes.this);
            builder.setMessage(R.string.NoFavFound);
            builder.setTitle(R.string.FavRecipes);
            builder.setNegativeButton(R.string.no, (dialog, which) -> dialog.cancel());
            builder.setPositiveButton(R.string.yes, (dialog, which) -> {
                Intent moveToSearch = new Intent(FavoriteRecipes.this, SearchRecipe.class);
                moveToSearch.putExtra("username",userName);
                moveToSearch.putExtra("userRole",userRole);
                startActivity(moveToSearch);
                finishAffinity();
            });
            final AlertDialog alertFav = builder.create();
            alertFav.setOnShowListener(dialog -> {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(20,0,0,0);
                Button button = alertFav.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setLayoutParams(params);
            });
            alertFav.show();
        }
    }
}
