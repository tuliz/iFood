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

import com.example.iFood.Activities.RecipeActivity;
import com.example.iFood.Classes.Recipes;
import com.example.iFood.Classes.RejectedRecipe;
import com.example.iFood.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RejectAdapter extends RecyclerView.Adapter<RejectAdapter.MyHolder> {
    String userName,userRole,check;
    private Context mContext;
    private List<RejectedRecipe> mData;


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
        check = String.valueOf(mData.get(position).isApproved());
        if(mData.get(position).getRecipeUrl()!=null && !mData.get(position).getRecipeUrl().isEmpty()) {
            Picasso.get().load(mData.get(position).getRecipeUrl()).into(holder.img_recipe);

        }
        holder.recipeTitle.setText(mData.get(position).getTitle());
        holder.cardView.setOnClickListener(v -> {
            Intent intent1 = new Intent(mContext, RecipeActivity.class);





            mContext.startActivity(intent1);
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {

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
