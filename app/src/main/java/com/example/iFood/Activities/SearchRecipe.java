package com.example.iFood.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**

 * This screen have menu, add recipes button and search option
 * This screen allows users to search for recipes depends on the ingredients they have.
 */
public class SearchRecipe extends AppCompatActivity {
    ConnectionBCR bcr = new ConnectionBCR();
    Button btnSearch,btnReset;
    BottomAppBar bottomAppBar;
    FloatingActionButton addIcon;
    EditText et_search;
    String activity = this.getClass().getName();
    String[] userInput = {};
    List<Recipes> searchResultArray = new ArrayList<>();
    RecyclerView myrecyclerView;
    RecipeAdapter myAdapter;
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Recipes");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_recipe);

        // hide the top bar
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        // Buttons
        btnSearch = findViewById(R.id.search_Recipe);
        btnReset = findViewById(R.id.search_resetField);
        bottomAppBar = findViewById(R.id.bottomAppBar);
        addIcon = findViewById(R.id.bottomAddIcon);

        // EditText
        et_search = findViewById(R.id.etSearch);

        // ListView
        myrecyclerView = findViewById(R.id.searchRecipeResultsLV);


        // Listeners
        btnSearch.setOnClickListener(v -> {
            // call a function to search for what user entered
            getInput();
            searchRecipe();
        });
        btnReset.setOnClickListener(v -> {
            // reset everything in screen
            et_search.setText("");
            searchResultArray.clear();
            refresh_lv();
            LinearLayout linearLayout = findViewById(R.id.resultsLayout);
            myrecyclerView.setVisibility(View.VISIBLE);
            linearLayout.setBackground(null);
            bottomAppBar.performShow();

        });

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
                Intent about = new Intent(SearchRecipe.this, About.class);
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

    /**
     * This function divides the user input into multiple variables to be used in search.
     */
    private void getInput(){
        userInput = et_search.getText().toString().split("\n");
    }
    /**
     * This function is called each time a user hits the "Search" button.
     * This function responsible to retrieve information from Database depends on
     * users input.
     * Will also called "refresh_lv" on each result.
     */
    public void searchRecipe(){
        Query dbQuery = ref.orderByKey();
        dbQuery.addValueEventListener(new ValueEventListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                searchResultArray.clear();
                boolean allMatch = false;
                for(DataSnapshot dst : dataSnapshot.getChildren()){
                    for(DataSnapshot searchedResults : dst.getChildren()){
                          Recipes results = searchedResults.getValue(Recipes.class);
                        for (String s : userInput) {
                            assert results != null;
                            if (results.getRecipeIngredients().toLowerCase().contains(s.toLowerCase()) && results.isApproved()) {
                                allMatch=true;
                            }
                        }
                        if(allMatch){
                            searchResultArray.add(results);
                            refresh_lv();
                        }
                        }
                    }
                if(searchResultArray.size()<1)
                {
                    LinearLayout linearLayout = findViewById(R.id.resultsLayout);
                    myrecyclerView.setVisibility(View.GONE);
                    linearLayout.setBackground(getDrawable(R.drawable.no_results));
                }
                }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    /**
     * This function is responsible for refreshing our Listview with our customer Adapter.
     * spanCount controls on the amount of items on each row.
     */
    private void refresh_lv(){

        myAdapter = new RecipeAdapter(this,searchResultArray,activity);

        myrecyclerView.setLayoutManager(new GridLayoutManager(this,3));

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
} // class ends
