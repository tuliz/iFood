package com.example.iFood.Adapters;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

import com.example.iFood.Activities.EditRecipeActivity;
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
    private Button btnView,btnDelete,btnEdit;
    private Dialog myDialog;
    private TextView dialogMessage;
    private String id,userName,userRole,check;
    private final String activity;
    private ProgressDialog progressDialog;
    private final Context mContext;
    private final List<Recipes> mData;
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
        if(check.equals("false")){
            myHolder.recipeTitle.setTextColor(Color.RED);
        }
        if(mData.get(i).getRecipePicture()!=null && !mData.get(i).getRecipePicture().isEmpty()) {
            Picasso.get().load(mData.get(i).getRecipePicture()).into(myHolder.img_recipe);

        }

        myHolder.recipeTitle.setText(mData.get(i).getRecipeName());
        myHolder.cardView.setOnClickListener(v -> {
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setCanceledOnTouchOutside(false);
            myDialog = new Dialog(mContext);
            myDialog.setContentView(R.layout.myrecipes_dialog);
            myDialog.setTitle("View or Delete?");
            dialogMessage = myDialog.findViewById(R.id.textView2);
            btnDelete = myDialog.findViewById(R.id.btnDelete);
            btnView = myDialog.findViewById(R.id.btnView);
            btnEdit = myDialog.findViewById(R.id.btnEdit);
            if(!mData.get(i).isApproved()){
               //Log.w("TAG","Inside if");

               dialogMessage.setText(R.string.edit_delete_view_my_recipes);
               btnEdit.setVisibility(View.VISIBLE);
                btnEdit.setOnClickListener(v13 -> {
                    Intent edit = new Intent(mContext, EditRecipeActivity.class);
                    edit.putExtra("RecipeName",mData.get(i).getRecipeName());
                    edit.putExtra("RecipeIngredients",mData.get(i).getRecipeIngredients());
                    edit.putExtra("Recipe",mData.get(i).getRecipe());
                    edit.putExtra("Thumbnail",mData.get(i).getRecipePicture());
                    edit.putExtra("recipeID",mData.get(i).getId());
                    edit.putExtra("recipeType",mData.get(i).getType());
                    edit.putExtra("recipeFeature",mData.get(i).getFeature());
                    edit.putExtra("userName",userName);
                    edit.putExtra("userRole",userRole);
                    myDialog.dismiss();
                    mContext.startActivity(edit);
                });
            }
            btnView.setOnClickListener(v1 -> {
                Intent intent1 = new Intent(mContext, RecipeActivity.class);
                intent1.putExtra("activity",activity);
                intent1.putExtra("RecipeName",mData.get(i).getRecipeName());
                intent1.putExtra("RecipeIngredients",mData.get(i).getRecipeIngredients());
                intent1.putExtra("RecipeMethodTitle",mData.get(i).getRecipeMethodTitle());
                intent1.putExtra("Recipe",mData.get(i).getRecipe());
                intent1.putExtra("recipeType",mData.get(i).getType());
                intent1.putExtra("recipeFeature",mData.get(i).getFeature());
                intent1.putExtra("Thumbnail",mData.get(i).getRecipePicture());
                intent1.putExtra("approved",check);
                intent1.putExtra("userRole",userRole);
                myDialog.cancel();
                mContext.startActivity(intent1);
            });
            btnDelete.setOnClickListener(v12 -> {
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

                    progressDialog.dismiss();
                    myDialog.cancel();
                }
                    });


            myDialog.show();
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


