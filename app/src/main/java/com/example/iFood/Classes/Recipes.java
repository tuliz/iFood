package com.example.iFood.Classes;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

/**

 * This class is the Recipes with all the needed information regarding the Recipe itself
 */
public class Recipes {
    public String id;
    public String recipeName;
    public String recipeIngredients;
    public String recipeMethodTitle;
    public String recipe;
    public String recipePicture;
    public String addedBy;
    public String type;
    public String feature;
    private boolean approved;
    public Object timestamp;

    Recipes(){}


    public Recipes(String recipeName, String recipeIngredients, String recipeMethodTitle, String recipe, String recipePicture, String id, String addedBy, String type, String feature) {
        this.recipeName = recipeName;
        this.recipeIngredients = recipeIngredients;
        this.recipeMethodTitle = recipeMethodTitle;
        this.recipe = recipe;
        this.recipePicture = recipePicture;
        this.id = id;
        this.addedBy = addedBy;
        this.type = type;
        this.feature = feature;
        timestamp = ServerValue.TIMESTAMP;

    }

    public String getAddedBy() {
        return addedBy;
    }

    public String getId(){
        return id;
    }
    public String getRecipeName() {
        return recipeName;
    }

    public String getRecipeIngredients() {
        return recipeIngredients;
    }

    public String getRecipeMethodTitle() {
        return recipeMethodTitle;
    }

    public String getRecipe() {
        return recipe;
    }

    public String getRecipePicture() {
        return recipePicture;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public void setRecipeIngredients(String recipeIngredients) {
        this.recipeIngredients = recipeIngredients;
    }

    public void setRecipeMethodTitle(String recipeMethodTitle) {
        this.recipeMethodTitle = recipeMethodTitle;
    }

    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    public void setRecipePicture(String recipePicture) {
        this.recipePicture = recipePicture;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public Object getTimestamp() {
        return timestamp;
    }
    @Exclude
    public long timestamp() {
        return (long) timestamp;
    }
}
