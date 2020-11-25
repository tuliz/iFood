package com.example.iFood.MenuFragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.iFood.Activities.Add_Recipe.addRecipe_New;
import com.example.iFood.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;

/**
 * This fragment handles the add button in our menu.
 */
public class AddDrawFragment extends BottomSheetDialogFragment {
    String username,userRole;
    private Context mContext;


    public AddDrawFragment() {
        // Required empty public constructor
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_add_draw, container, false);
        NavigationView navigationView = view.findViewById(R.id.navAdd);
        navigationView.setNavigationItemSelectedListener(item -> {

            assert getArguments() != null;
            username = getArguments().getString("username");
            userRole = getArguments().getString("userRole");
            if (item.getItemId() == R.id.menu_add) {
                Intent main = new Intent(mContext.getApplicationContext(), addRecipe_New.class);
                main.putExtra("username", username);
                main.putExtra("userRole",userRole);
                startActivity(main);
            }
            return false;
        });
        return view;
    }

}
