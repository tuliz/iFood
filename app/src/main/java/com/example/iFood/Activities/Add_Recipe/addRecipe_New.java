package com.example.iFood.Activities.Add_Recipe;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.iFood.Activities.MainActivity;
import com.example.iFood.Activities.MyRecipes;
import com.example.iFood.Activities.ProfileActivity;
import com.example.iFood.Activities.SearchRecipe;
import com.example.iFood.Activities.oldActivities.Inbox;
import com.example.iFood.Classes.Recipes;
import com.example.iFood.R;
import com.example.iFood.Utils.ConnectionBCR;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


public class addRecipe_New extends AppCompatActivity {

    // Public Variables ( shared with fragments )
    public static String recipeName="",recipeIngredients="",recipeInstructions="",recipeImage="";
    public static Bitmap bitmapImage=null;

    // Connect to DB & Storage
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference DB = database.getReference();
    StorageReference mStorage = FirebaseStorage.getInstance().getReference();

    // Local variables
    ProgressDialog progressDialog;
    String id;
    int stepPosition = 0;
    Toolbar toolbar;
    ViewPager viewPager;
    TabLayout tabLayout;
    Button btnNext,btnPrevious,btnConfirm;
    Button btnOk,btnDismiss;
    Recipe_add_step1 step1;
    Recipe_add_step2 step2;
    Recipe_add_step3 step3;
    // AppService


    // Broadcast Receiver
    ConnectionBCR bcr = new ConnectionBCR();



    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe_new);


       toolbar = findViewById(R.id.toolBarID);
       setSupportActionBar(toolbar);


        // Progress Dialog
        progressDialog = new ProgressDialog(this);

       // Variable from Layout

       viewPager = findViewById(R.id.viewPager);
       tabLayout = findViewById(R.id.tab_layout);
       btnNext = findViewById(R.id.btnNext);
       btnPrevious = findViewById(R.id.btnPrevious);
       btnConfirm = findViewById(R.id.btnConfirm);

      // Define each step
       step1 = new Recipe_add_step1();
       step2 = new Recipe_add_step2();
       step3 = new Recipe_add_step3();


       tabLayout.setupWithViewPager(viewPager);
       btnPrevious.setOnClickListener(v -> {

           if(stepPosition>0){

               stepPosition--;
               viewPager.setCurrentItem(stepPosition);
           }
           if(stepPosition<=2){
               btnNext.setVisibility(View.VISIBLE);
               btnConfirm.setVisibility(View.GONE);
           }

       });
       btnNext.setOnClickListener(v -> {

              if(stepPosition<3) {
                    stepPosition++;
                    viewPager.setCurrentItem(stepPosition);
              }
              if(stepPosition==2){
                    btnNext.setVisibility(View.GONE);
                    btnConfirm.setVisibility(View.VISIBLE);
              }

       });

       btnConfirm.setOnClickListener(v -> {
              if(recipeName.isEmpty() || recipeIngredients.isEmpty())
              {
                  stepPosition = 0;
                  viewPager.setCurrentItem(stepPosition);
                  btnNext.setVisibility(View.VISIBLE);
                  btnConfirm.setVisibility(View.GONE);
                 Snackbar.make(v,"Recipe name / Ingredients cannot be empty", Snackbar.LENGTH_SHORT).setAction("Action",null).show();
              }
              else if(recipeInstructions.isEmpty()){
                  stepPosition=1;
                  viewPager.setCurrentItem(stepPosition);
                  btnNext.setVisibility(View.VISIBLE);
                  btnConfirm.setVisibility(View.GONE);
                  Snackbar.make(v,"Please explain how to prepare this recipe", Snackbar.LENGTH_SHORT).setAction("Action",null).show();
              }
              else if(bitmapImage == null)
              {
                    Snackbar.make(v,"Please upload a picture of your recipe", Snackbar.LENGTH_SHORT).setAction("Action",null).show();
              }
              else
                  {
                  // create our recipe with the information we need from our global variables
                  progressDialog.setMessage("Creating Recipe");
                  progressDialog.show();
                  createRecipe();
                  //Log.i("image2","Image is:"+bitmapImage);
              }

       });

        LinearLayout tabStrip = ((LinearLayout)tabLayout.getChildAt(0));
        for(int i = 0; i < tabStrip.getChildCount(); i++) {
            tabStrip.getChildAt(i).setOnTouchListener((v, event) -> true);
        }


       viewPagerAdapter viewPagerAdapter = new viewPagerAdapter(getSupportFragmentManager(), 0);
       viewPagerAdapter.addFragment(step1,"Step 1");
       viewPagerAdapter.addFragment(step2,"Step 2");
       viewPagerAdapter.addFragment(step3,"Step 3");
       viewPager.setAdapter(viewPagerAdapter);



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

    /**
     * Adds menu to the top bar.
     */
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_layout,menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Manage user selection on the Menu.
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menuProfile:
                Intent profile = new Intent(addRecipe_New.this, ProfileActivity.class);
                profile.putExtra("username",getIntent().getStringExtra("username"));
                profile.putExtra("userRole",getIntent().getStringExtra("userRole"));
                startActivity(profile);
                finish();
                break;
            case R.id.menu_Exit:
                final Dialog myDialog = new Dialog(addRecipe_New.this);
                myDialog.setContentView(R.layout.dialog);
                btnDismiss = myDialog.findViewById(R.id.btnDismiss);
                btnOk =  myDialog.findViewById(R.id.btnOk);

               btnOk.setOnClickListener(v -> {
                   SharedPreferences.Editor delData = getSharedPreferences("userData",MODE_PRIVATE).edit();
                   delData.clear();
                   delData.apply();
                   finish();
               });
                btnDismiss.setOnClickListener(v -> myDialog.dismiss());
                myDialog.show();
                break;
            case R.id.menu_MyRecepies:
                Intent myRecipes = new Intent(addRecipe_New.this, MyRecipes.class);
                myRecipes.putExtra("username",getIntent().getStringExtra("username"));
                myRecipes.putExtra("userRole",getIntent().getStringExtra("userRole"));
                startActivity(myRecipes);
                finish();
                break;

            case R.id.menu_SearchRecepie:
                Intent search = new Intent(addRecipe_New.this, SearchRecipe.class);
                search.putExtra("username",getIntent().getStringExtra("username"));
                search.putExtra("userRole",getIntent().getStringExtra("userRole"));
                startActivity(search);
                finish();
                break;
            case R.id.menuInbox:
                Intent inbox = new Intent(addRecipe_New.this, Inbox.class);
                inbox.putExtra("username",getIntent().getStringExtra("username"));
                inbox.putExtra("userRole",getIntent().getStringExtra("userRole"));
                startActivity(inbox);
                finish();
                break;
            case R.id.menuHome:
                Intent main = new Intent(addRecipe_New.this, MainActivity.class);
                main.putExtra("username",getIntent().getStringExtra("username"));
                main.putExtra("userRole",getIntent().getStringExtra("userRole"));
                startActivity(main);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This function responsible for creating the recipe in the DB with all the
     * information the user put in the different steps.
     */
    public void createRecipe(){

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        final byte[] data = baos.toByteArray();
        final UploadTask uploadTask = mStorage.child("Photos/"+ UUID.randomUUID().toString()).putBytes(data);
        // close onSuccess method
        uploadTask.addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
        }).addOnSuccessListener(taskSnapshot -> {
            if (taskSnapshot.getMetadata() != null) {
                if (taskSnapshot.getMetadata().getReference() != null) {
                    Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                    result.addOnSuccessListener(uri -> {
                        // Get the URL string from the Storage of the Image that was just uploaded
                        recipeImage = uri.toString();
                        // Get the username from the Intent
                        String addBy = getIntent().getStringExtra("username");
                        // Generate random unique key in the DB
                        id = String.valueOf(DB.child("Recipes").push().getKey());
                        // Declare the recipe class.
                        Recipes rec;
                        // Assign values to constructor
                        rec = new Recipes(recipeName,recipeIngredients,getResources().getString(R.string.method),recipeInstructions,recipeImage,id,addBy);
                        // Set it as new recipe that waiting for approval.
                        rec.setApproved(false);
                        // Adding the recipe with all the above to DB.
                        DB.child("Recipes").child(id).child(Objects.requireNonNull(addRecipe_New.this.getIntent().getStringExtra("username"))).setValue(rec);
                        // Toast the User a message process is finished.
                        Toast.makeText(addRecipe_New.this,"Recipe added successfully",Toast.LENGTH_SHORT).show();
                        // Reset all the variables to empty.
                        resetRecipe();
                        // Dismiss Dialog.
                        progressDialog.dismiss();
                        Toast.makeText(addRecipe_New.this,"A moderator will review your recipe as soon as possible, thank you.",Toast.LENGTH_LONG).show();
                    });
                }
            }
        }); // close OnSuccessListener
    }

    /**
     * This function responsible for Camera and Storage permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    /**
     * Reset recipes information.
     */
    private void resetRecipe(){
       Intent addRecipe = new Intent(addRecipe_New.this,addRecipe_New.class);
        addRecipe.putExtra("username",getIntent().getStringExtra("username"));
        addRecipe.putExtra("userRole",getIntent().getStringExtra("userRole"));
        startActivity(addRecipe);
        finish();

    }

    /**
     * Inner class related to the fragments and page viewer ( to switch between the fragments layout )
     */
    private static class viewPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragments = new ArrayList<>();
        private List<String> fragmentsTitle = new ArrayList<>();



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

}