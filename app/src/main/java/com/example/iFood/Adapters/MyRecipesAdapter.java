package com.example.iFood.Adapters;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iFood.Activities.RecipeActivity;
import com.example.iFood.Classes.Recipes;
import com.example.iFood.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

/**

 * This adapter hold all the information related to the user recipes.
 * and allow the user to move to Recipe Activity to view all the information regarding the recipe.
 */
public class MyRecipesAdapter extends RecyclerView.Adapter<MyRecipesAdapter.MyHolder> {
    Button btnView,btnDelete;
    Dialog myDialog;
    String id,userName,userRole,check;
    private String activity;
    ProgressDialog progressDialog;
    private Context mContext;
    private List<Recipes> mData;
    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("Recipes");

    public MyRecipesAdapter(Context mContext, List<Recipes> mData,String activity){
        this.mContext = mContext;
        this.mData = mData;
        this.activity = activity;
    }



    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view ;
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        view = layoutInflater.inflate(R.layout.cardview_recipe,viewGroup,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder myHolder, final int i) {
        Intent  intent = ((Activity) mContext).getIntent();
        userName = intent.getStringExtra("username");
        userRole = intent.getStringExtra("userRole");
        check = String.valueOf(mData.get(i).isApproved());
        if(mData.get(i).getRecipePicture()!=null && !mData.get(i).getRecipePicture().isEmpty()) {
            Picasso.get().load(mData.get(i).getRecipePicture()).into(myHolder.img_recipe);

        }

        myHolder.recipeTitle.setText(mData.get(i).getRecipeName());
        myHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(mContext);
                myDialog = new Dialog(mContext);
                myDialog.setContentView(R.layout.myrecipes_dialog);
                myDialog.setTitle("View or Delete?");
                btnDelete = myDialog.findViewById(R.id.btnDelete);
                btnView = myDialog.findViewById(R.id.btnView);
                btnView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, RecipeActivity.class);
                        intent.putExtra("activity",activity);
                        intent.putExtra("RecipeName",mData.get(i).getRecipeName());
                        intent.putExtra("RecipeIngredients",mData.get(i).getRecipeIngredients());
                        intent.putExtra("RecipeMethodTitle",mData.get(i).getRecipeMethodTitle());
                        intent.putExtra("Recipe",mData.get(i).getRecipe());
                        intent.putExtra("Thumbnail",mData.get(i).getRecipePicture());
                        intent.putExtra("approved",check);
                        intent.putExtra("userRole",userRole);
                        myDialog.cancel();
                        mContext.startActivity(intent);
                    }
                });
                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mData.size()>0){
                            progressDialog.setMessage("Delete in progress");
                            progressDialog.show();
                            Query dbQuery = myRef.orderByKey();
                            id = mData.get(i).getId();
                            dbQuery.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for(DataSnapshot dst : dataSnapshot.getChildren()){
                                        Log.i("Title","Value: "+dst.getKey()+ " and id is: "+id);
                                        if(Objects.equals(dst.getKey(), id)){
                                            mData.remove(i);
                                            myRef.child(id).removeValue();
                                            progressDialog.dismiss();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            // Log.i("Title","ID is:"+mData.get(i).getId());
                            progressDialog.dismiss();
                            myDialog.cancel();
                        }
                            }

                });
                myDialog.show();
            }
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


