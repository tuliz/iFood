package com.example.iFood.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.iFood.Activities.RejectedRecipeActivity;
import com.example.iFood.Classes.RejectedRecipe;
import com.example.iFood.R;
import com.squareup.picasso.Picasso;
import java.util.List;

public class RejectAdapter extends RecyclerView.Adapter<RejectAdapter.MyHolder> {
    String userName,userRole;
    private final Context mContext;
    private final List<RejectedRecipe> mData;


    public RejectAdapter(Context mContext, List<RejectedRecipe> mData){
        this.mContext = mContext;
        this.mData = mData;

    }
    @NonNull
    @Override
    public RejectAdapter.MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view ;
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        view = layoutInflater.inflate(R.layout.cardview_recipe,viewGroup,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        Intent intent = ((Activity) mContext).getIntent();
        userName = intent.getStringExtra("username");
        userRole = intent.getStringExtra("userRole");

        if(mData.get(position).getRecipeUrl()!=null && !mData.get(position).getRecipeUrl().isEmpty()) {
            Picasso.get().load(mData.get(position).getRecipeUrl()).into(holder.img_recipe);

        }
        holder.recipeTitle.setText(mData.get(position).getTitle());
        holder.cardView.setOnClickListener(v -> {
            Intent rejectIntent = new Intent(mContext, RejectedRecipeActivity.class);

            rejectIntent.putExtra("addedBy",mData.get(position).getAddedBy());
            rejectIntent.putExtra("username",userName);
            rejectIntent.putExtra("userRole",userRole);
            rejectIntent.putExtra("RecipeName",mData.get(position).getTitle());
            rejectIntent.putExtra("RecipeIngredients",mData.get(position).getIngredients());
            rejectIntent.putExtra("RecipeMethodTitle",mData.get(position).getRecipeMethodTitle());
            rejectIntent.putExtra("recipeType",mData.get(position).getType());
            rejectIntent.putExtra("recipeFeature",mData.get(position).getFeature());
            rejectIntent.putExtra("Recipe",mData.get(position).getContent());
            rejectIntent.putExtra("id",mData.get(position).getRecipeID());
            rejectIntent.putExtra("Thumbnail",mData.get(position).getRecipeUrl());
            rejectIntent.putExtra("removeDate",mData.get(position).getRejectDate());
            rejectIntent.putExtra("rejectReasons",mData.get(position).getRejectReasons());
            rejectIntent.putExtra("time",mData.get(position).getTimestamp().toString());
            rejectIntent.putExtra("rejectedBy",mData.get(position).getRejectedBy());




            mContext.startActivity(rejectIntent);
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {

        TextView recipeTitle;
        CardView cardView;
        ImageView img_recipe;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            recipeTitle = itemView.findViewById(R.id.recipe_text);
            img_recipe = itemView.findViewById(R.id.recipe_img_id);
            cardView = itemView.findViewById(R.id.cardview_id);


        }
    }
}
