package com.example.iFood.Activities.Add_Recipe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.iFood.R;

import java.util.ArrayList;

import static com.example.iFood.Activities.Add_Recipe.addRecipe_New.featureList;
import static com.example.iFood.Activities.Add_Recipe.addRecipe_New.recipeFeature;
import static com.example.iFood.Activities.Add_Recipe.addRecipe_New.recipeInstructions;
import static com.example.iFood.Activities.Add_Recipe.addRecipe_New.recipeType;
import static com.example.iFood.Activities.Add_Recipe.addRecipe_New.recipe_Type;

/**
 * Step 2 of adding a recipe, Explaining how to prepare the recipe.
 */
public class Recipe_add_step2 extends Fragment implements View.OnClickListener{
    private TextView tvRecipe_features,tvRecipe_type;
    private EditText etRecipeMethod;
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_add_step2, container, false);
        etRecipeMethod = view.findViewById(R.id.etRecipeMethod);
        tvRecipe_features = view.findViewById(R.id.Recipe_features);
        tvRecipe_type = view.findViewById(R.id.Recipe_type);

        etRecipeMethod.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                recipeInstructions = etRecipeMethod.getText().toString();
            }
        });

        tvRecipe_features.setOnClickListener(this);
        tvRecipe_type.setOnClickListener(this);
        return view;
    }

    public void onClick(View v) {

        if(v == tvRecipe_features){
            featureList = new ArrayList<>();
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Select Recipe Features:");
            builder.setMultiChoiceItems(R.array.recipeFeatures, null, (dialog, which, isChecked) -> {

                String[] arr =getResources().getStringArray(R.array.recipeFeatures);
                       if(isChecked){
                           featureList.add(arr[which]);
                       }else if(featureList.contains(arr[which])){
                           featureList.remove(arr[which]);
                       }
            });
            builder.setPositiveButton("Submit", (dialog, which) -> {
               String data="";
                int i = 0;
                 for(String item:featureList){
                     if(i++ == featureList.size() - 1){
                         data = data + item;
                     }else {
                         data = data + item + ",";
                     }

                 }
                 recipeFeature = data;
                 tvRecipe_features.setText(data);
                 featureList.clear();
               });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.create();
            builder.show();
        }
        if(v == tvRecipe_type){
            recipeType = new ArrayList<>();
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Select Recipe Type:");
            builder.setMultiChoiceItems(R.array.recipeType, null, (dialog, which, isChecked) -> {


                String[] arr = getResources().getStringArray(R.array.recipeType);
                if(isChecked){
                    recipeType.add(arr[which]);
                }else if(recipeType.contains(arr[which])){
                         recipeType.remove(arr[which]);
                }
            });
            builder.setPositiveButton("Submit", (dialog, which) -> {
                String data="";
                int i = 0;
                for(String item:recipeType){
                    if(i++ == recipeType.size() - 1){
                        data = data + item;
                    }else {
                        data = data + item + ",";
                    }

                }
                recipe_Type = data;
                tvRecipe_type.setText(data);
                recipeType.clear();
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.create();
            builder.show();


        }
    }

}