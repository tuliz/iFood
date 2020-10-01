package com.example.iFood.Classes;

public class RejectedRecipe {

    public String recipeID;
    public String Title;
    public String Content;
    public String recipeMethodTitle;
    public String Ingredients;
    public String recipeUrl;
    public String rejectReasons;
    public String addedBy;
    private boolean approved;
    public String rejectDate;
    public Object timestamp;


    RejectedRecipe(){}

    public RejectedRecipe(String recipeID, String title, String content, String recipeMethodTitle, String ingredients, String recipeUrl, String rejectReasons, String addedBy, boolean approved,String rejectDate ,Object timestamp) {
        this.recipeID = recipeID;
        Title = title;
        Content = content;
        this.recipeMethodTitle = recipeMethodTitle;
        Ingredients = ingredients;
        this.recipeUrl = recipeUrl;
        this.rejectReasons = rejectReasons;
        this.addedBy = addedBy;
        this.approved = approved;
        this.timestamp = timestamp;
        this.rejectDate = rejectDate;
    }

    public String getRejectDate() {
        return rejectDate;
    }

    public void setRejectDate(String rejectDate) {
        this.rejectDate = rejectDate;
    }

    public String getRecipeID() {
        return recipeID;
    }

    public void setRecipeID(String recipeID) {
        this.recipeID = recipeID;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

    public String getRecipeMethodTitle() {
        return recipeMethodTitle;
    }

    public void setRecipeMethodTitle(String recipeMethodTitle) {
        this.recipeMethodTitle = recipeMethodTitle;
    }

    public String getIngredients() {
        return Ingredients;
    }

    public void setIngredients(String ingredients) {
        Ingredients = ingredients;
    }

    public String getRecipeUrl() {
        return recipeUrl;
    }

    public void setRecipeUrl(String recipeUrl) {
        this.recipeUrl = recipeUrl;
    }

    public String getRejectReasons() {
        return rejectReasons;
    }

    public void setRejectReasons(String rejectReasons) {
        this.rejectReasons = rejectReasons;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }
}
