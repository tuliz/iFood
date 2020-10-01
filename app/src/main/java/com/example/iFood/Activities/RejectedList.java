package com.example.iFood.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.example.iFood.Adapters.RecipeAdapter;
import com.example.iFood.Adapters.RejectAdapter;
import com.example.iFood.Classes.Recipes;
import com.example.iFood.Classes.RejectedRecipe;
import com.example.iFood.MenuFragments.AddDrawFragment;
import com.example.iFood.MenuFragments.NavDrawFragment;
import com.example.iFood.R;
import com.example.iFood.Utils.ConnectionBCR;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class RejectedList extends AppCompatActivity {
    ConnectionBCR bcr = new ConnectionBCR();
    BottomAppBar bottomAppBar;
    FloatingActionButton addIcon;
    RecyclerView rejectedList;
    RejectAdapter myAdapter;
    List<RejectedRecipe> rejectedRecipeList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rejected_list);


        rejectedList = findViewById(R.id.rejectList);
        bottomAppBar = findViewById(R.id.bottomAppBar);
        addIcon = findViewById(R.id.bottomAddIcon);


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
                Intent about = new Intent(RejectedList.this, About.class);
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

    }
}