package com.example.iFood.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iFood.Activities.Add_Recipe.addRecipe_New;
import com.example.iFood.Activities.oldActivities.AddRecipe;
import com.example.iFood.Adapters.MyRecipesAdapter;
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
import java.util.Objects;

/**

 * This screen is the screen where every user can watch his own recipes.
 * The user can remove his recipe or view it, depends on his choice.
 * Screen have menu, add recipe button and Listview - keep simple.
 */

public class MyRecipes extends AppCompatActivity {
    ConnectionBCR bcr = new ConnectionBCR();
    BottomAppBar bottomAppBar;
    FloatingActionButton addIcon;
    String activity = this.getClass().getName();
    String userName,userRole;
    TextView totalCount,notApprovedCount;
    MyRecipesAdapter myAdapter;
    RecyclerView myrecyclerView;
    private final List<Recipes> myRecipes = new ArrayList<>();
    private final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Recipes");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_recipes);
        myrecyclerView = findViewById(R.id.MyRecipes_recycler_view);

        bottomAppBar = findViewById(R.id.bottomAppBar);
        addIcon = findViewById(R.id.bottomAddIcon);

        userRole = getIntent().getStringExtra("userRole");
        userName = getIntent().getStringExtra("username");
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
                Intent about = new Intent(MyRecipes.this, About.class);
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

        totalCount = findViewById(R.id.tvMyRecipesCount);
        notApprovedCount = findViewById(R.id.tvNotApprovedCount);

         getList();


    } // onCreate Ends

    /**
     * This function called every time user wants to looks for his recipes
     * Doing a Query on the Database that matches the user name and retrieve it
     * Refresh the Listview by calling "refresh_lv" .
     */
    private void getList(){

        Query qb = ref.orderByKey();
        qb.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myRecipes.clear();
                int i=0;
                for(DataSnapshot dst : dataSnapshot.getChildren()){
                    for(DataSnapshot userRecipes : dst.getChildren())
                            // check  the user name and add to his list
                        if (Objects.equals(userRecipes.getKey(), userName)) {
                            Recipes userRec = userRecipes.getValue(Recipes.class);
                            myRecipes.add(userRec);
                            assert userRec != null;
                            if(!userRec.isApproved()){
                                i++;
                            }
                            refresh_lv();

                        }
                }
                if(myRecipes.size()<1) {
                    myRecipesSize();
                    totalCount.setText(String.valueOf(0));
                }else{
                    totalCount.setText(String.valueOf(myRecipes.size()));
                    String notApproved = "("+i+")";
                    notApprovedCount.setText(notApproved);
                    notApprovedCount.setTextColor(Color.RED);
                    notApprovedCount.setOnClickListener(v -> Toast.makeText(MyRecipes.this,"Number of recipes waiting for review",Toast.LENGTH_SHORT)
                            .show());

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MyRecipes.this);

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
    private void refresh_lv() {
        myAdapter = new MyRecipesAdapter(this, myRecipes,activity);

        myrecyclerView.setLayoutManager(new GridLayoutManager(this, 1));

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

    /**
     * On resuming ( if coming back from Recipe Activity ), to refresh the list if user removed a recipe
     * from his own recipes
     */
    @Override
    protected void onResume() {
        super.onResume();
        refresh_lv();
    }

    /**
     * This function responsible for promoting a user a Dialog to ask if he wants to add a Recipe
     * if his list is empty
     */
    private void myRecipesSize(){
        //Check if user have anything in his recipes list, if not, promote him to add one and move him to AddRecipe Screen.
        if(myRecipes.size()<1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MyRecipes.this);

            builder.setMessage(R.string.NoRecipesFound);
            builder.setTitle(R.string.myRecipes);
            builder.setNegativeButton(R.string.no, (dialog, which) -> dialog.cancel());
            builder.setPositiveButton(R.string.yes, (dialog, which) -> {
                Intent moveToAdd = new Intent(MyRecipes.this, addRecipe_New.class);
                moveToAdd.putExtra("username", userName);
                moveToAdd.putExtra("userRole",userRole);
                startActivity(moveToAdd);
            });
            final AlertDialog alertMyRecipes = builder.create();
            alertMyRecipes.setOnShowListener(dialog -> {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(20,0,0,0);
                Button button = alertMyRecipes.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setLayoutParams(params);
            });
            alertMyRecipes.show();
        }
    }
} // Class ends
