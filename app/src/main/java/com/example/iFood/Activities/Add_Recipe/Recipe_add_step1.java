package com.example.iFood.Activities.Add_Recipe;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.example.iFood.R;

import static com.example.iFood.Activities.Add_Recipe.addRecipe_New.recipeIngredients;
import static com.example.iFood.Activities.Add_Recipe.addRecipe_New.recipeName;


public class Recipe_add_step1 extends Fragment {
    EditText etRecipeName,etRecipeIngredients;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_step1, container, false);

        etRecipeIngredients = view.findViewById(R.id.etRecipeIngredients);
        etRecipeName = view.findViewById(R.id.etRecipeName);

        etRecipeName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    recipeName = etRecipeName.getText().toString();
                }
            });
        etRecipeIngredients.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                recipeIngredients = etRecipeIngredients.getText().toString();
            }
        });



        return view;
    }
}