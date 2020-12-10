package com.example.iFood.Adapters;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iFood.Activities.MessageActivity;
import com.example.iFood.Classes.Message;
import com.example.iFood.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import static com.example.iFood.Activities.Inbox.Inbox_new.checkDelList;
import static com.example.iFood.Activities.Inbox.Inbox_new.msgList;

/**

 * This adapter hold all the information related to the message
 * and allow the user to move to Message Activity to view all the information regarding the message.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyHolder> {
    String userName,userRole,msgID;
    DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference().child("Messages");
    private final Context mContext;
    private final List<Message> mData;


    public MessageAdapter(Context mContext, List<Message> mData){
        this.mContext = mContext;
        this.mData = mData;

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view ;
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        view = layoutInflater.inflate(R.layout.message_view,viewGroup,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder myHolder, final int i) {


        Intent  intent = ((Activity) mContext).getIntent();
        userName = intent.getStringExtra("username");
        userRole = intent.getStringExtra("userRole");
        msgID = mData.get(i).getMsgID() ;
        //////
        myHolder.msgTitle.setText(mData.get(i).getTitle());
        myHolder.userName.setText(mData.get(i).getFromUser());
        Picasso.get().load(mData.get(i).getUserImageUrl()).into(myHolder.userImg);




        myHolder.msgView.setOnClickListener(v -> {
            Intent intent1 = new Intent(mContext, MessageActivity.class);
            changeMsgState();
            intent1.putExtra("username",userName);
            intent1.putExtra("userRole",userRole);
            intent1.putExtra("Message Title",mData.get(i).getTitle());
            intent1.putExtra("Message Content",mData.get(i).getMessage());
            intent1.putExtra("From User",mData.get(i).getFromUser());
            intent1.putExtra("Message Date",mData.get(i).getSentDate());
            intent1.putExtra("To User",mData.get(i).getToUser());
            intent1.putExtra("msgID",msgID);
            mContext.startActivity(intent1);
        });

        myHolder.userImg.setOnClickListener(v -> {
            if(!mData.get(i).isMarked) {
               // Toast.makeText(mContext, "Clicked on" + mData.get(i).title, Toast.LENGTH_SHORT).show();
                Drawable d = AppCompatResources.getDrawable(mContext, R.drawable.ic_done);
                myHolder.userImg.setImageDrawable(d);

                mData.get(i).isMarked = true;
                msgList.add(mData.get(i).getMsgID());

              //Log.i("tag","MsgID added:"+mData.get(i).msgID+", msgList size now is:"+msgList.size());
            }else{
                Picasso.get().load(mData.get(i).getUserImageUrl()).into(myHolder.userImg);
                mData.get(i).isMarked = false;
                msgList.remove(mData.get(i).msgID);
           //   Log.i("tag","MsgID remove:"+mData.get(i).msgID+", msgList size now is:"+msgList.size());
            }
            checkDelList();

        });

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {

        public TextView msgTitle,userName;
        public CardView msgView;
        public  ImageView userImg;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            msgTitle = itemView.findViewById(R.id.tvMessageTitle);
            userName = itemView.findViewById(R.id.tvFullUserName);
            userImg = itemView.findViewById(R.id.messageIcon);
            msgView = itemView.findViewById(R.id.message_id);




        }
    }

    /**
     * This function responsible for changing the state of the message from read=false to read=true
     * after the user viewed the message itself.
     */
    public void changeMsgState(){
        Log.w("TAG","Before messageRef");
        messagesRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.w("in func","Changed");
                messagesRef.child(userName).child(msgID).child("isRead").setValue("true");
                Log.w("TAG2","isRead:"+messagesRef.child(userName).child(msgID).child("read").toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    /*
    public Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }*/

}
