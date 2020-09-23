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


public class Inbox_Old_Messages extends Fragment {
    public static RecyclerView readList;
    public static ArrayList<Message> readMsg = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_inbox__old__messages, container, false);

        readList = view.findViewById(R.id.opened_message);


        return view;
    }
}