package com.example.iFood.Adapters;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iFood.Activities.RecipeActivity;
import com.example.iFood.Activities.SendMessage;
import com.example.iFood.Classes.Recipes;
import com.example.iFood.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Objects;


/**

 * This adapter hold all the information related to the recipe
 * and allow the user to move to Recipe Activity to view all the information regarding the recipe.
 */
public class MainRecipeAdapter extends RecyclerView.Adapter<MainRecipeAdapter.MyHolder> {

    private String userName,userRole,check;
    private Bitmap image;
    private Uri imageToSend;
    private boolean isExists;
    private long time;
    private final Context mContext;
    private final List<Recipes> mData;
    private final String activity;

    public MainRecipeAdapter(Context mContext, List<Recipes> mData,String activity){
        this.mContext = mContext;
        this.mData = mData;
        this.activity = activity;
    }



    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view ;
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        view = layoutInflater.inflate(R.layout.card_view_main,viewGroup,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder myHolder, final int i) {
        Intent  intent = ((Activity) mContext).getIntent();
        userName = intent.getStringExtra("username");
        userRole = intent.getStringExtra("userRole");
        time = (Long) mData.get(i).getTimestamp();
        //   Log.w("TAG", "onBindViewHolder: time is:"+time );
        check = String.valueOf(mData.get(i).isApproved());
        if(mData.get(i).getRecipePicture()!=null && !mData.get(i).getRecipePicture().isEmpty()) {
            Picasso.get().load(mData.get(i).getRecipePicture()).into(myHolder.img_recipe);

        }

        myHolder.recipeTitle.setText(mData.get(i).getRecipeName());
        myHolder.cardView.setOnClickListener(v -> {
            Intent intent1 = new Intent(mContext, RecipeActivity.class);
            intent1.putExtra("activity",activity);
            intent1.putExtra("addedBy",mData.get(i).getAddedBy());
            intent1.putExtra("approved",check);
            intent1.putExtra("username",userName);
            intent1.putExtra("userRole",userRole);
            intent1.putExtra("RecipeName",mData.get(i).getRecipeName());
            intent1.putExtra("RecipeIngredients",mData.get(i).getRecipeIngredients());
            intent1.putExtra("RecipeMethodTitle",mData.get(i).getRecipeMethodTitle());
            intent1.putExtra("Recipe",mData.get(i).getRecipe());
            intent1.putExtra("recipeType",mData.get(i).getType());
            intent1.putExtra("recipeFeature",mData.get(i).getFeature());
            intent1.putExtra("id",mData.get(i).getId());
            intent1.putExtra("Thumbnail",mData.get(i).getRecipePicture());
            intent1.putExtra("time",time);
            mContext.startActivity(intent1);
        });

        myHolder.card_msg.setOnClickListener(v -> {
            Intent newMsg = new Intent(((Activity) mContext), SendMessage.class);
            newMsg.putExtra("username",userName);
            newMsg.putExtra("activity",activity);
            newMsg.putExtra("To User",mData.get(i).getAddedBy());
            newMsg.putExtra("msgTitle",mData.get(i).getRecipeName());
            mContext.startActivity(newMsg);
        });

        myHolder.card_share.setOnClickListener(v -> {

            new Thread(() -> {

                Intent shareRecipe = new Intent(Intent.ACTION_SEND);
                shareRecipe.setType("image/*");
                image = getUrltoBitMap(mData.get(i).getRecipePicture());
                imageToSend  = getLocalBitmapUri(image);
                File file = new File(Objects.requireNonNull(imageToSend.getPath()));
                Uri uriToSend = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName()+".provider",file);
                String ingredients = mData.get(i).getRecipeIngredients();
                String recipeContent = mData.get(i).getRecipe();
                shareRecipe.putExtra(Intent.EXTRA_SUBJECT,mData.get(i).getRecipeName());
                String text =  mContext.getResources().getString(R.string.ingredients)+
                        System.getProperty("line.separator")+
                        System.getProperty("line.separator")+
                        ingredients+
                        System.getProperty("line.separator")+
                        System.getProperty("line.separator")+
                        mContext.getResources().getString(R.string.method)+
                        System.getProperty("line.separator")+
                        System.getProperty("line.separator")+
                        recipeContent+
                        System.getProperty("line.separator")+
                        System.getProperty("line.separator")+
                        "Shared from iFood app, look for the app on the Play Store!";
                shareRecipe.putExtra(Intent.EXTRA_TEXT,text);
                shareRecipe.putExtra(Intent.EXTRA_STREAM,uriToSend);
                shareRecipe.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                shareRecipe.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                mContext.startActivity(Intent.createChooser(shareRecipe,"Share this Recipe via"));

            }).start();

        });

        myHolder.card_like.setOnClickListener(v -> {
            addFav(userName,mData.get(i));

            int color = myHolder.card_like.getCurrentTextColor();
            if(color == Color.RED){
                myHolder.card_like.setTextColor(Color.GRAY);
            }else{
                myHolder.card_like.setTextColor(Color.RED);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
    private void addFav(String userName,Recipes recipe){

        DatabaseReference Fav_ref = FirebaseDatabase.getInstance().getReference().child("Favorites");
        Fav_ref.child(userName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dst : dataSnapshot.getChildren()) {
                    //   Log.w("TAG", "key(dst):" + dst.getKey());
                    if (Objects.equals(dst.getKey(), recipe.getId())) {
                        // remove from fav list - clicked on like the second time
                        Fav_ref.child(userName).child(recipe.getId()).removeValue();
                        isExists = true;
                         //Log.w("TAG", "isExists:" + isExists);
                        Toast.makeText(((Activity) mContext), "Recipe removed from favorites.", Toast.LENGTH_SHORT).show();
                        break;
                    }


                }
                //Log.w("TAG","isExists3:"+isExists);
                if(!isExists){
                    // didn't found the ID meaning not in the list so adding to user fav list
                    //Log.w("TAG","Going to add to fav!");
                    Fav_ref.child(userName).child(recipe.getId()).setValue(recipe);

                    Toast.makeText(((Activity) mContext),"Added to favorites!",Toast.LENGTH_SHORT).show();

                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    public Bitmap getUrltoBitMap(String string){
        try {
            URL url = new URL(string);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Uri getLocalBitmapUri(@NonNull Bitmap bmp) {
        Uri bmpUri = null;
        try {
            File file =  new File(mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    public static class MyHolder extends RecyclerView.ViewHolder {

        TextView recipeTitle,card_like,card_share,card_msg;
        CardView cardView;
        ImageView img_recipe;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            recipeTitle = itemView.findViewById(R.id.recipe_text);
            img_recipe = itemView.findViewById(R.id.recipe_img_id);
            cardView = itemView.findViewById(R.id.cardview_id);

            card_like = itemView.findViewById(R.id.card_like);
            card_msg = itemView.findViewById(R.id.card_msg);
            card_share = itemView.findViewById(R.id.card_share);



        }
    }



}
