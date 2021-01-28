package com.example.iFood.Classes;

public class RejectedRecipe {

    public String recipeID;
    public String title;
    public String content;
    public String recipeMethodTitle;
    public String ingredients;
    public String recipeUrl;
    public String rejectReasons;
    public String addedBy;
    public String type;
    public String feature;
    public String rejectedBy;
    private boolean approved;
    public String rejectDate;
    public long timestamp;


    RejectedRecipe(){}

    public RejectedRecipe(String recipeID, String title, String content, String recipeMethodTitle, String ingredients, String recipeUrl, String rejectReasons, String addedBy,String type, String feature,String rejectedBy, boolean approved,String rejectDate ,long timestamp) {
        this.recipeID = recipeID;
        this.title = title;
        this.content = content;
        this.recipeMethodTitle = recipeMethodTitle;
        this.ingredients = ingredients;
        this.recipeUrl = recipeUrl;
        this.rejectReasons = rejectReasons;
        this.addedBy = addedBy;
        this.type = type;
        this.feature = feature;
        this.approved = approved;
        this.rejectedBy = rejectedBy;
        this.timestamp = timestamp;
        this.rejectDate = rejectDate;
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

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getRecipeID() {
        return recipeID;
    }

    public void setRecipeID(String recipeID) {
        this.recipeID = recipeID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRecipeMethodTitle() {
        return recipeMethodTitle;
    }

    public void setRecipeMethodTitle(String recipeMethodTitle) {
        this.recipeMethodTitle = recipeMethodTitle;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
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

    public String getRejectedBy() {
        return rejectedBy;
    }

    public void setRejectedBy(String rejectedBy) {
        this.rejectedBy = rejectedBy;
    }

    public String getRejectDate() {
        return rejectDate;
    }

    public void setRejectDate(String rejectDate) {
        this.rejectDate = rejectDate;
    }

    @Override
    public String toString() {
        return "RejectedRecipe{" +
                "recipeID='" + recipeID + '\'' +
                ", Title='" + title + '\'' +
                ", Content='" + content + '\'' +
                ", recipeMethodTitle='" + recipeMethodTitle + '\'' +
                ", Ingredients='" + ingredients + '\'' +
                ", recipeUrl='" + recipeUrl + '\'' +
                ", rejectReasons='" + rejectReasons + '\'' +
                ", addedBy='" + addedBy + '\'' +
                ", rejectedBy='" + rejectedBy + '\'' +
                ", approved=" + approved +
                ", rejectDate='" + rejectDate + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
