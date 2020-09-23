package com.example.iFood.Activities.Inbox;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iFood.Classes.Message;
import com.example.iFood.R;

import java.util.ArrayList;


public class Inbox_New_Messages extends Fragment {
    public static RecyclerView unReadList;
    public static ArrayList<Message> unReadmsg = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_inbox__new__messages, container, false);

       unReadList = view.findViewById(R.id.unread_message);
       if(unReadmsg.size() < 1){

       }

        return view;
    }
}