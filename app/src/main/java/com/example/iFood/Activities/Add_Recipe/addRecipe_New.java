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
import android.util.Log;
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

import com.example.iFood.Activities.AdminActivity;
import com.example.iFood.Activities.Inbox.Inbox_new;
import com.example.iFood.Activities.MainActivity;
import com.example.iFood.Activities.MyRecipes;
import com.example.iFood.Activities.ProfileActivity;
import com.example.iFood.Activities.SearchRecipe;

import com.example.iFood.Classes.Recipes;
import com.example.iFood.Classes.Users;
import com.example.iFood.Notification.APIService;
import com.example.iFood.Notification.Client;
import com.example.iFood.Notification.Data;
import com.example.iFood.Notification.MyResponse;
import com.example.iFood.Notification.NotificationSender;
import com.example.iFood.R;
import com.example.iFood.Utils.ConnectionBCR;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class addRecipe_New extends AppCompatActivity {

    // Public Variables ( shared with fragments )
    public static String recipeName="",recipeIngredients="",recipeInstructions="",recipeImage="",recipe_Type,recipeFeature;
    public static Bitmap bitmapImage=null;
    public static List<String> featureList;
    public static List<String> recipeType;

    // Connect to DB & Storage
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference DB = database.getReference();
    StorageReference mStorage = FirebaseStorage.getInstance().getReference();

    // Local variables
    String userName,userRole;
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
     APIService apiService;

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
       userRole = getIntent().getStringExtra("userRole");
       userName = getIntent().getStringExtra("username");
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

           if(stepPosition==0){
              this.finish();
           }
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
                 Snackbar.make(v,"Recipe name / Ingredients cannot be empty.", Snackbar.LENGTH_SHORT).setAction("Action",null).show();
              }
              else if(recipeInstructions.isEmpty()){
                  stepPosition=1;
                  viewPager.setCurrentItem(stepPosition);
                  btnNext.setVisibility(View.VISIBLE);
                  btnConfirm.setVisibility(View.GONE);
                  Snackbar.make(v,"Please explain how to prepare this recipe.", Snackbar.LENGTH_SHORT).setAction("Action",null).show();
              }else if(recipe_Type.isEmpty() || recipeFeature.isEmpty()){
                  stepPosition=1;
                  viewPager.setCurrentItem(stepPosition);
                  btnNext.setVisibility(View.VISIBLE);
                  btnConfirm.setVisibility(View.GONE);
                  Snackbar.make(v,"Please enter the type and features of this recipe.", Snackbar.LENGTH_SHORT).setAction("Action",null).show();
              }
              else if(bitmapImage == null)
              {
                    Snackbar.make(v,"Please upload a picture of your recipe", Snackbar.LENGTH_SHORT).setAction("Action",null).show();
              }
              else
                  {
                  // create our recipe with the information we need from our global variables
                  progressDialog.setMessage("Creating Recipe");
                  progressDialog.setCanceledOnTouchOutside(false);
                  progressDialog.show();
                  createRecipe();

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
        int itemId = item.getItemId();
        if(itemId ==  R.id.menu_Exit) {
            final Dialog myDialog = new Dialog(addRecipe_New.this);
            myDialog.setContentView(R.layout.dialog);
            btnDismiss = myDialog.findViewById(R.id.btnDismiss);
            btnOk = myDialog.findViewById(R.id.btnOk);

            // if pressed Ok will close the App
            btnOk.setOnClickListener(v -> {

                SharedPreferences.Editor delData = getSharedPreferences("userData", MODE_PRIVATE).edit();
                delData.clear();
                delData.apply();
                finishAffinity();
            });
            // if pressed Dismiss will stay in the App
            btnDismiss.setOnClickListener(v -> myDialog.dismiss());
            myDialog.show();
        }

        else if(itemId == R.id.menuProfile) {
            Intent profile = new Intent(addRecipe_New.this, ProfileActivity.class);
            profile.putExtra("username", userName);
            profile.putExtra("userRole", userRole);
            startActivity(profile);
            finish();
        }
        else if(itemId == R.id.menu_MyRecepies){
            Intent myRecipes = new Intent(addRecipe_New.this, MyRecipes.class);
            myRecipes.putExtra("username",userName);
            myRecipes.putExtra("userRole", userRole);
            startActivity(myRecipes);
        }
        else if(itemId == R.id.menu_SearchRecepie) {
            Intent search = new Intent(addRecipe_New.this, SearchRecipe.class);
            search.putExtra("username", userName);
            search.putExtra("userRole", userRole);
            startActivity(search);
        }
        else if(itemId == R.id.menuInbox) {
            Intent inbox = new Intent(addRecipe_New.this, Inbox_new.class);
            inbox.putExtra("username", userName);
            inbox.putExtra("userRole", userRole);
            startActivity(inbox);
        }
        else if(itemId ==R.id.menuHome) {
            Intent main = new Intent(addRecipe_New.this, MainActivity.class);
            main.putExtra("username", userName);
            main.putExtra("userRole", userRole);
            startActivity(main);
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

                        // Generate random unique key in the DB
                        id = String.valueOf(DB.child("Recipes").push().getKey());
                        // Declare the recipe class.
                        Recipes rec;
                        // Assign values to constructor
                        rec = new Recipes(recipeName,recipeIngredients,getResources().getString(R.string.method),recipeInstructions,recipeImage,id,userName,recipe_Type,recipeFeature);
                        // Set it as new recipe that waiting for approval.
                        rec.setApproved(false);

                        // Adding the recipe with all the above to DB.

                        DB.child("Recipes").child(id).child(userName).setValue(rec);
                        // Toast the User a message process is finished.
                        Toast.makeText(addRecipe_New.this,"Recipe added successfully",Toast.LENGTH_SHORT).show();
                        // Reset all the variables to empty.
                        resetRecipe();
                        sendModNotification();

                        // Dismiss Dialog.
                        progressDialog.dismiss();
                        Toast.makeText(addRecipe_New.this,"A moderator will review your recipe as soon as possible, thank you.",Toast.LENGTH_LONG).show();


                    });
                }
            }
        }); // close OnSuccessListener
    }

    /**
     * Sending notification to mod/admin users for each new recipe added in the app to approve it.
     */
    private void sendModNotification() {
        DatabaseReference modUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        new Thread(() -> modUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot outerData : snapshot.getChildren()) {
                    Users u = outerData.getValue(Users.class);
                    assert u != null;
                    if(!u.getUsername().equals(userName)) {
                        if (u.userRole.equals("mod") || u.userRole.equals("admin")) {
                            Log.d("TAG", "User data:" + u.toString());
                            apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
                            FirebaseDatabase.getInstance().getReference().child("Tokens").child(u.uid).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getValue(String.class) != null) {
                                        String userToken = snapshot.getValue(String.class);
                                        //Log.w("TAG","Token:"+userToken);
                                        sendNotifications(userToken, "New Recipe", "A Recipe is waiting for your approval, check it out!");
                                        // Log.w("TAG", "Sent notification.");
                                    } else {
                                        Log.w("TAG", "Token not found.");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.w("TAG", "Error:" + error.getMessage());
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        })).start();

    }


    /**
     * Function deliver the information to send to the API class
     * @param usertoken user device token.
     * @param title notification title.
     * @param message notification message.
     */
    public void sendNotifications(String usertoken, String title, String message) {
        Data data = new Data(title, message);
        NotificationSender sender = new NotificationSender(data, usertoken);
        apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                if (response.code() == 200) {
                    //Log.w("TAG","code:"+response.code());
                    //Log.w("TAG","body:"+response.body().success);
                    assert response.body() != null;
                    if (response.body().success != 1) {
                       Log.d("Error",response.message());
                    }
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {
                Log.w("TAG","Error:"+t.getMessage());
            }
        });
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