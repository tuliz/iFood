package com.example.iFood.Activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.iFood.Classes.Recipes;
import com.example.iFood.Classes.RejectedRecipe;
import com.example.iFood.R;
import com.example.iFood.Utils.ConnectionBCR;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import ru.embersoft.expandabletextview.ExpandableTextView;

/**

 * This activity handles all the recipes information and allow the user to watch the recipe
 * information with the ability to either add to his favorite list or send the recipe creator a message
 * regarding the recipe.
  */
public class RecipeActivity extends AppCompatActivity {
    ConnectionBCR bcr = new ConnectionBCR();
    private boolean isExists = false;
    long time;
    Dialog myDialog;
    Bitmap image = null;
    Uri imageToSend;
    FloatingActionButton btnMsg,btnFav,btnShare,btnReport;
    String addedBy,userName,activity,Title,Ingredients,MethodTitle,Recipe,recipeID,recipeImage,userRole,approved,reason="",recipe_Type,recipeFeature;
    TextView mRecipeName,mRecipeMethodTitle;
    Button confirm, cancel;
    TextView option1,option2,option3,option4,option5,option6,recipeFeatureSelection,recipeType;
    EditText otherReason;
    ExpandableTextView mRecipeIngredients,mRecipe;
    ProgressDialog progressDialog;
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Favorites");
    DatabaseReference recipesRef = FirebaseDatabase.getInstance().getReference().child("Recipes");
    DatabaseReference deleted_list = FirebaseDatabase.getInstance().getReference().child("Deleted List");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        // get information from intent

        approved = getIntent().getStringExtra("approved");
        userRole = getIntent().getStringExtra("userRole");
        userName = getIntent().getStringExtra("username");
        activity = getIntent().getStringExtra("activity");
        addedBy = getIntent().getExtras().getString("addedBy");
        recipe_Type = getIntent().getExtras().getString("recipeType");
        recipeFeature = getIntent().getExtras().getString("recipeFeature");
        Title = getIntent().getExtras().getString("RecipeName");
        Ingredients = getIntent().getExtras().getString("RecipeIngredients");
        MethodTitle = getIntent().getExtras().getString("RecipeMethodTitle");
        Recipe = getIntent().getExtras().getString("Recipe");
        recipeID = getIntent().getStringExtra("id");
        recipeImage = getIntent().getStringExtra("Thumbnail");
        time = getIntent().getLongExtra("time",0);


        //////////////
        progressDialog = new ProgressDialog(this);
        btnFav = findViewById(R.id.btnFav);
        btnMsg = findViewById(R.id.btnSendMessage);
        btnShare = findViewById(R.id.btnShare);
        btnReport = findViewById(R.id.btnReport);

        //////////////
        mRecipeName = findViewById(R.id.text_recipe);
        mRecipeIngredients = findViewById(R.id.ingredients);
        mRecipeMethodTitle = findViewById(R.id.method);
        mRecipe = findViewById(R.id.recipe);
        recipeType = findViewById(R.id.recipeType);
        recipeFeatureSelection = findViewById(R.id.recipeFeatureSelection);
        ///////////////



        mRecipeName.setText(Title);
        mRecipeIngredients.setText(Ingredients);
        mRecipeMethodTitle.setText(MethodTitle);
        mRecipe.setText(Recipe);
        recipeFeatureSelection.setText(recipeFeature);
        recipeType.setText(recipe_Type);

        //Setting the ExpandableText closed until user open it
        mRecipeIngredients.resetState(true);
        mRecipe.resetState(true);

        // Listeners
        mRecipeIngredients.setOnClickListener(v -> mRecipeIngredients.animate());
        mRecipe.setOnClickListener(v -> mRecipe.animate());

        btnMsg.setOnClickListener(v -> {
            Intent newMsg = new Intent(RecipeActivity.this, SendMessage.class);
            newMsg.putExtra("username",userName);
            newMsg.putExtra("activity",activity);
            newMsg.putExtra("To User",addedBy);
            newMsg.putExtra("msgTitle",Title);
            startActivity(newMsg);
        });
        btnFav.setOnClickListener(v -> {
            // Add to fav in DB
            addToFav();

        });

        btnReport.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("plain/text");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "ifoodspprt@gmail.com" });
            intent.putExtra(Intent.EXTRA_SUBJECT, "Recipe Report: "+Title);
            intent.putExtra(Intent.EXTRA_TEXT, "Hi, I would like to report recipe id ("+recipeID+") because...");
            startActivity(Intent.createChooser(intent, ""));
        });


        btnShare.setOnClickListener(v -> new Thread(() -> {

            Intent shareRecipe = new Intent(Intent.ACTION_SEND);
            shareRecipe.setType("image/*");
            image = getUrltoBitMap(recipeImage);
            imageToSend  = getLocalBitmapUri(image);
            File file = new File(Objects.requireNonNull(imageToSend.getPath()));
            Uri uriToSend = FileProvider.getUriForFile(RecipeActivity.this,getApplicationContext().getPackageName()+".provider",file);
            String ingredients = mRecipeIngredients.getTextContent().toString();
            String recipeContent = mRecipe.getTextContent().toString();
            shareRecipe.putExtra(Intent.EXTRA_SUBJECT,mRecipeName.getText().toString());
            String text = getResources().getString(R.string.ingredients)+
                    System.getProperty("line.separator")+
                    System.getProperty("line.separator")+
                    ingredients+
                    System.getProperty("line.separator")+
                    System.getProperty("line.separator")+
                    getResources().getString(R.string.method)+
                    System.getProperty("line.separator")+
                    System.getProperty("line.separator")+
                    recipeContent+
                    System.getProperty("line.separator")+
                    System.getProperty("line.separator")+
                    "Shared from iFood app, look for the app on the Play Store!";
            shareRecipe.putExtra(Intent.EXTRA_TEXT,text);
            shareRecipe.putExtra(Intent.EXTRA_STREAM,uriToSend);
            shareRecipe.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            shareRecipe.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareRecipe,"Share this Recipe via"));

        }).start());

        handleButtons();
    } // onCreate ends
    /**
     * This function is responsible for either hiding not needed buttons in the screen "My Recipes" and admin buttons
     * Prevents from users to add their own recipes to their favorite list.
     * Prevents from users to send messages to themselves.
     * Enables users to share the recipe with other people.
     */
    public void handleButtons() {

            if (activity.contains("MyRecipes") || userName.equals(getIntent().getStringExtra("addedBy"))) {
                btnMsg.setVisibility(View.GONE);
                btnFav.setVisibility(View.GONE);
            }
            if (!activity.contains("MyRecipes") && approved.equals("false") && userRole.equals("admin") || userRole.equals("mod")) {
                if (activity.contains("ModActivity")) {
                    btnShare.setVisibility(View.GONE);
                    btnFav.setVisibility(View.GONE);
                    btnMsg.setVisibility(View.GONE);
                    FloatingActionButton btnApprove = new FloatingActionButton(this);
                    btnApprove.setId(View.generateViewId());
                    btnApprove.setImageResource(R.drawable.ic_baseline_check);
                    btnApprove.setSize(FloatingActionButton.SIZE_NORMAL);
                    btnApprove.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                    btnApprove.setOnClickListener(v -> {
                        recipesRef.child(recipeID).child(addedBy).child("approved").setValue(true);
                        finish();
                    });
                    FloatingActionButton btnDel = new FloatingActionButton(this);
                    btnDel.setId(View.generateViewId());
                    btnDel.setImageResource(R.drawable.ic_exit);
                    btnDel.setSize(FloatingActionButton.SIZE_NORMAL);
                    btnDel.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                    btnDel.setOnClickListener(v -> {
                        myDialog = new Dialog(RecipeActivity.this);
                        // Layout
                        myDialog.setContentView(R.layout.delete_reason_dialog);

                        // Textview options
                        option1 = myDialog.findViewById(R.id.tv1);
                        option2 = myDialog.findViewById(R.id.tv2);
                        option3 = myDialog.findViewById(R.id.tv3);
                        option4 = myDialog.findViewById(R.id.tv4);
                        option5 = myDialog.findViewById(R.id.tv5);
                        option6 = myDialog.findViewById(R.id.tv6);

                        // Checkbox
                        CheckBox reason1 = myDialog.findViewById(R.id.reason1);
                        CheckBox reason2 = myDialog.findViewById(R.id.reason2);
                        CheckBox reason3 = myDialog.findViewById(R.id.reason3);
                        CheckBox reason4 = myDialog.findViewById(R.id.reason4);
                        CheckBox reason5 = myDialog.findViewById(R.id.reason5);
                        CheckBox reason6 = myDialog.findViewById(R.id.reason6);
                        // Edittext
                        otherReason = myDialog.findViewById(R.id.etOtherReason);

                        // Buttons
                        confirm = myDialog.findViewById(R.id.btnReasonConfirm);
                        cancel = myDialog.findViewById(R.id.btnReasonCancel);


                        // Title
                        myDialog.setTitle("Rejection Reason");

                        // Listeners
                        confirm.setOnClickListener(v1 -> {
                            if (reason1.isChecked()) {
                                if (reason.isEmpty()) reason += option1.getText().toString();
                                else reason += "," + option1.getText().toString();

                            }
                            if (reason2.isChecked()) {
                                if (reason.isEmpty()) reason += option2.getText().toString();
                                else reason += "," + option2.getText().toString();

                            }
                            if (reason3.isChecked()) {
                                if (reason.isEmpty()) reason += option3.getText().toString();
                                else reason += "," + option3.getText().toString();

                            }
                            if (reason4.isChecked()) {
                                if (reason.isEmpty()) reason += option4.getText().toString();
                                else reason += "," + option4.getText().toString();

                            }
                            if (reason5.isChecked()) {
                                if (reason.isEmpty()) reason += option5.getText().toString();
                                else reason += "," + option5.getText().toString();

                            }
                            if (reason6.isChecked()) {
                                if (reason.isEmpty()) reason += option6.getText().toString();
                                else reason += "," + option6.getText().toString();

                            }
                            if (!otherReason.getText().toString().isEmpty()) {
                                if (reason.isEmpty()) reason += otherReason.getText().toString();
                                else reason += "," + otherReason.getText().toString();
                            }
                            // Log.w("TAG","Reason: "+reason);
                            myDialog.dismiss();
                            if (!reason.isEmpty()) {

                                // Get the time of declined recipe
                                Date date = Calendar.getInstance().getTime();
                                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                                String formattedDate = df.format(date);

                                // Get the recipe details for future review later on
                                RejectedRecipe rejectedRecipe;
                                rejectedRecipe = new RejectedRecipe(recipeID, Title, Recipe, MethodTitle
                                        , Ingredients, recipeImage, reason, addedBy,recipe_Type,recipeFeature, userName, false,
                                        formattedDate, time);

                                //deleted_list.child(userName).child(recipeID).setValue(rejectedRecipe);

                                deleted_list.child(userName).child(recipeID).setValue(rejectedRecipe).addOnSuccessListener(aVoid -> {
                                    // Remove the recipe from waiting list
                                    Log.w("TAG", "Recipe added to rejected list");
                                    recipesRef.child(recipeID).removeValue();
                                    finish();
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(RecipeActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                                    Log.w("TAG", "Error:" + e.getMessage());
                                });
                            } else {
                                Toast.makeText(RecipeActivity.this, "Please choose at least 1 option.", Toast.LENGTH_SHORT).show();
                            }

                        });
                        cancel.setOnClickListener(v12 -> myDialog.dismiss());
                        myDialog.show();

                    });
                    LinearLayout linearLayout = findViewById(R.id.bottomLayout);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    linearLayout.setGravity(Gravity.CENTER);
                    params.setMarginStart(15);
                    btnApprove.setLayoutParams(params);
                    btnDel.setLayoutParams(params);
                    linearLayout.addView(btnApprove);
                    linearLayout.addView(btnDel);
                }


            }
        }


    /**
     * This function responsible for converting given string to Bitmap, pulling data from our DB as image url and convert it to actual
     * Bitmap object to insert the image view
     * @param string gets a string parameter to convert to Bitmap object
     * @return Bitmap Object
     */
    public Bitmap getUrltoBitMap(String string){
        try {
            URL url = new URL(string);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This function responsible from converting Bitmap object into Uri when a user decides to share the recipe
     * @param bmp represents the actual Bitmap object in the Recipe itself
     * @return Returns Uri that we can actually send via Intent using our share button.
     */
    public Uri getLocalBitmapUri(@NonNull Bitmap bmp) {
        Uri bmpUri = null;
        try {
            File file =  new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }
    /**
     * This function is responsible for adding a recipe to a user favorite list.
     * Double click on the favorite button will remove it from his favorite list.
     */
 private void addToFav(){


     ref.child(userName).addListenerForSingleValueEvent(new ValueEventListener() {
         @Override
         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
             for(DataSnapshot dst : dataSnapshot.getChildren()) {
              //   Log.w("TAG", "key(dst):" + dst.getKey());
                         if (Objects.equals(dst.getKey(), recipeID)) {
                             // remove from fav list is clicked on fav the second time
                             ref.child(userName).child(recipeID).removeValue();
                             isExists = true;
                            // Log.w("TAG", "isExists2:" + isExists);
                             Toast.makeText(RecipeActivity.this, "Recipe removed from favorites.", Toast.LENGTH_SHORT).show();
                             break;
                         }


             }
             //Log.w("TAG","isExists3:"+isExists);
             if(!isExists){
                 // didn't found the ID meaning not in the list so adding to user fav list
                 Recipes r;
                 r= new Recipes(Title,Ingredients,MethodTitle,Recipe,recipeImage,recipeID,addedBy,recipe_Type,recipeFeature);
                 ref.child(userName).child(recipeID).setValue(r);
                 Toast.makeText(RecipeActivity.this,"Added to favorites!",Toast.LENGTH_SHORT).show();

             }
         }


         @Override
         public void onCancelled(@NonNull DatabaseError databaseError) {

         }
     });

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


}
