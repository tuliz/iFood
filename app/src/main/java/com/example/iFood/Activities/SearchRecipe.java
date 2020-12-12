package com.example.iFood.Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

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
            if(!et_search.getText().toString().isEmpty()){
                searchRecipe();
            }else{
                Toast.makeText(SearchRecipe.this,"Please enter ingredients",Toast.LENGTH_SHORT).show();
                et_search.requestFocus();
            }

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
            bottomNavFrag.show(getSupportFragmentManager(),"bottomNav");

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
            addIcon.show(getSupportFragmentManager(),"addIconNav");
        });





    } // onCreate ends

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SearchRecipe.this);

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
     * This function divides the user input into multiple variables to be used in search
     * and translate the input to English to bring up results from the Database.
     */
    private void getInput(){
        userInput = et_search.getText().toString().split("\n");

        /*
        Log.w("TAG","userInput length:"+userInput.length);
             for(i=0;i<userInput.length;i++){
            Log.w("TAG","input:"+userInput[i]);
        }

        for(i=0;i<userInput.length;i++) {
            TranslateAPI translateAPI = new TranslateAPI(
                    Language.AUTO_DETECT,
                    Language.ENGLISH,
                    userInput[i]);

            translateAPI.setTranslateListener(new TranslateAPI.TranslateListener() {
                @Override
                public void onSuccess(String translatedText) {
                    Log.w("TAG", "Translated:" + translatedText);
                    userInput[i]=translatedText;
                    Log.w("TAG","userInput in pos: "+i+" value now is:"+userInput[i]);

                }

                @Override
                public void onFailure(String s) {

                }
            });
        }*/
    }
    /**
     * This function is called each time a user hits the "Search" button.
     * This function responsible to retrieve information from Database depends on
     * users input.
     * Will also called "refresh_lv" on each result.
     */
    public void searchRecipe(){
        ProgressDialog progressDialog = new ProgressDialog(SearchRecipe.this);
        progressDialog.setMessage("Searching for your recipe..");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        Query dbQuery = ref.orderByKey();
        dbQuery.addValueEventListener(new ValueEventListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                searchResultArray.clear();
                boolean allMatch = false;
                int count = 0,inputSize = userInput.length;
               // Log.w("Start","count is:"+count+",inputSize:"+inputSize);
                for(DataSnapshot dst : dataSnapshot.getChildren()){
                    for(DataSnapshot searchedResults : dst.getChildren()){
                        Recipes results = searchedResults.getValue(Recipes.class);
                        assert results != null;
                        for (String s : userInput) {
                            //Log.w("TAG","Rec name: "+results.recipeName+", isApproved:"+results.isApproved());
                           // Log.w("userInput","Input:"+s);
                           // Log.w("recipe","recipe:"+results.getRecipeIngredients());
                           // allMatch = results.getRecipeIngredients().toLowerCase().contains(s.toLowerCase()) && results.isApproved();
                            if(results.getRecipeIngredients().toLowerCase().contains(s.toLowerCase()) && results.isApproved()){
                                count+=1;
                             //   Log.w("TAG","Found!,count is:"+count);
                            }

                        }
                       // Log.w("TAG2","Outside for loop,count is:"+count+",inputSize:"+inputSize+",recipe name:"+results.getRecipeName());
                        if(count==inputSize){
                            searchResultArray.add(results);
                            refresh_lv();

                        }
                        count=0;
                        }
                    }
                progressDialog.dismiss();
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

        //Log.w("tag","activity:"+activity);
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
