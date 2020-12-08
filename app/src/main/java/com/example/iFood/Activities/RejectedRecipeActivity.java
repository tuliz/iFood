package com.example.iFood.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.iFood.Classes.Recipes;
import com.example.iFood.Classes.RejectedRecipe;
import com.example.iFood.R;
import com.example.iFood.Utils.ConnectionBCR;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import ru.embersoft.expandabletextview.ExpandableTextView;

public class RejectedRecipeActivity extends AppCompatActivity {
    // Broadcast Receiver
    ConnectionBCR bcr = new ConnectionBCR();
    // Recipe Details
    long time;
    TextView mRecipeName, mRecipeMethodTitle, rejectDate, rejectReasons, rejectedBy;
    ExpandableTextView mRecipeIngredients, mRecipe;
    // Buttons
    FloatingActionButton btnApprove,btnDismiss;
    // Intent Information
    String userName,userRole,addedBy,userRejected,recipeName,recipeIngredients,recipeMethodTitle,recipeContent,recipeID,recipeImage,removeDate,rejectReason;
    // Database
    DatabaseReference deleted_list = FirebaseDatabase.getInstance().getReference().child("Deleted List");
    DatabaseReference recipesRef = FirebaseDatabase.getInstance().getReference().child("Recipes");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rejected_recipe);


        ///////////////
        pullIntentInformation();
        setVars();
        setVarsDate();


        ///////////// Listeners
        btnDismiss.setOnClickListener(v -> {
            // If Admin clicked dismiss remove record from DB completely
                        String storageUrl = recipeImage;
                        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(storageUrl);
                        storageReference.delete().addOnSuccessListener(aVoid -> {
                            // File deleted successfully so now remove from DB
                               deleted_list.child(userRejected).child(recipeID).removeValue();
                        }).addOnFailureListener(exception -> {
                            // Uh-oh, an error occurred! toast message to user
                            Toast.makeText(RejectedRecipeActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                            Log.w("TAG","Error: "+exception.getMessage());
                        });

        });

        btnApprove.setOnClickListener(v -> {
            // If Admin clicked Approve, add to general recipe list and set to approved
            Recipes rec;
            rec = new Recipes(recipeName,recipeIngredients,recipeMethodTitle,recipeContent,recipeImage,recipeID,addedBy);
            rec.setApproved(true);
            recipesRef.child(addedBy).child(recipeID).setValue(rec);
        });
        ///////////////
    } // onCreate Ends

    private void pullIntentInformation() {
        final Intent intent = getIntent();
        userName = intent.getStringExtra("username");
        userRole = intent.getStringExtra("userRole");
        addedBy = intent.getStringExtra("addedBy");
        recipeName = intent.getStringExtra("RecipeName");
        recipeIngredients = intent.getStringExtra("RecipeIngredients");
        recipeMethodTitle = intent.getStringExtra("RecipeMethodTitle");
        recipeContent = intent.getStringExtra("Recipe");
        recipeID = intent.getStringExtra("id");
        recipeImage = intent.getStringExtra("Thumbnail");
        removeDate = intent.getStringExtra("removeDate");
        rejectReason = intent.getStringExtra("rejectReasons");
        userRejected = intent.getStringExtra("rejectedBy");
        time = intent.getLongExtra("time",0);


    }

    private void setVars() {
        //Textview
        mRecipeName = findViewById(R.id.text_recipe);
        mRecipeIngredients = findViewById(R.id.ingredients);
        mRecipeMethodTitle = findViewById(R.id.method);
        mRecipe = findViewById(R.id.recipe);
        rejectDate = findViewById(R.id.tv_removeDate);
        rejectReasons = findViewById(R.id.tv_removeReason);
        rejectedBy = findViewById(R.id.tv_removedBy);
        //Buttons
        btnApprove = findViewById(R.id.btnApprove);
        btnDismiss = findViewById(R.id.btnDismiss);
    }


    private void setVarsDate() {

        mRecipe.setText(recipeName);
        mRecipeIngredients.setText(recipeIngredients);
        mRecipeMethodTitle.setText(recipeMethodTitle);
        mRecipe.setText(recipeContent);
        rejectDate.setText(removeDate);
        rejectReasons.setText(rejectReason);
        rejectedBy.setText(userRejected);
        //Setting the ExpandableText closed until user open it
        mRecipeIngredients.resetState(true);
        mRecipe.resetState(true);

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