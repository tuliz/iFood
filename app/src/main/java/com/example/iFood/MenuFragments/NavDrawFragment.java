package com.example.iFood.MenuFragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.example.iFood.Activities.Add_Recipe.addRecipe_New;
import com.example.iFood.Activities.AdminActivity;
import com.example.iFood.Activities.FavoriteRecipes;
import com.example.iFood.Activities.Inbox.Inbox_new;
import com.example.iFood.Activities.MainActivity;
import com.example.iFood.Activities.ModActivity;
import com.example.iFood.Activities.MyRecipes;
import com.example.iFood.Activities.ProfileActivity;
import com.example.iFood.Activities.RejectedListActivity;
import com.example.iFood.Activities.SearchRecipe;
import com.example.iFood.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

/**

 * This fragments is responsible for our menu
 */
public class NavDrawFragment extends BottomSheetDialogFragment {

    DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference().child("Messages");
    String username,userRole;
    SharedPreferences pref;
    int count=0;
    private Context mContext;

    public NavDrawFragment() {
        // Required empty public constructor
    }
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_nav_draw, container, false);
        assert getArguments() != null;

        username = getArguments().getString("username");
        pref = Objects.requireNonNull(getActivity()).getSharedPreferences("userData",MODE_PRIVATE);
        if(pref.contains("userRole")){
            userRole = pref.getString("userRole",null);
        }
        userRole = getArguments().getString("userRole");
        NavigationView navigationView = view.findViewById(R.id.navView);

        Menu nav_Menu = navigationView.getMenu();
        if(userRole.equals("admin") || userRole.equals("mod")){
            //    Log.i("role","Found Admin/Mod, role:"+userRole);
            nav_Menu.findItem(R.id.modMenu).setVisible(true);
                nav_Menu.findItem(R.id.menu_mod).setVisible(true);
                if(userRole.equals("admin")) {
                   // Log.i("role","Found Admin, role:"+userRole);
                    nav_Menu.findItem(R.id.menu_admin).setVisible(true);
                    nav_Menu.findItem(R.id.menu_rejectList).setVisible(true);
                }
        }else{
         //   Log.i("role","Normal member? role:"+userRole);
            nav_Menu.findItem(R.id.modMenu).setVisible(false);
            nav_Menu.findItem(R.id.menu_mod).setVisible(false);
            nav_Menu.findItem(R.id.menu_admin).setVisible(false);
        }

        navigationView.setNavigationItemSelectedListener(item -> {

            int itemId = item.getItemId();
            if (itemId == R.id.menu_admin) {
                Intent admin = new Intent(mContext.getApplicationContext(), AdminActivity.class);
                admin.putExtra("username", username);
                admin.putExtra("userRole", userRole);
                startActivity(admin);
                Objects.requireNonNull(getActivity()).finishAffinity();
            } else if (itemId == R.id.menu_mod) {
                Intent mod = new Intent(mContext.getApplicationContext(), ModActivity.class);
                mod.putExtra("username", username);
                mod.putExtra("userRole", userRole);
                startActivity(mod);
                Objects.requireNonNull(getActivity()).finishAffinity();
            } else if (itemId == R.id.menuProfile) {
                Intent profile = new Intent(mContext.getApplicationContext(), ProfileActivity.class);
                profile.putExtra("username", username);
                profile.putExtra("userRole", userRole);
                startActivity(profile);

            } else if (itemId == R.id.menu_rejectList) {
                Intent rejectList = new Intent(mContext.getApplicationContext(), RejectedListActivity.class);
                rejectList.putExtra("username", username);
                rejectList.putExtra("userRole", userRole);
                startActivity(rejectList);
                Objects.requireNonNull(getActivity()).finishAffinity();
            } else if (itemId == R.id.menuHome) {
                Intent home = new Intent(mContext.getApplicationContext(), MainActivity.class);
                home.putExtra("username", username);
                home.putExtra("userRole", userRole);
                startActivity(home);
                Objects.requireNonNull(getActivity()).finishAffinity();
            } else if (itemId == R.id.menu_Exit) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Exit Application");
                builder.setMessage("Are you sure you want to Exit?");
                builder.setNegativeButton(R.string.no, (dialog, which) -> dialog.cancel());
                builder.setPositiveButton(R.string.yes, (dialog, which) -> {

                    //mContext.stopService(new Intent(mContext.getApplicationContext(),AppService.class));
                    SharedPreferences.Editor delData = mContext.getSharedPreferences("userData", MODE_PRIVATE).edit();
                    delData.clear();
                    delData.apply();
                    Objects.requireNonNull(getActivity()).finishAffinity();
                });
                final AlertDialog alertExit = builder.create();
                alertExit.setOnShowListener(dialog -> {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(20, 0, 0, 0);
                    Button button = alertExit.getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setLayoutParams(params);
                });

                alertExit.show();
            } else if (itemId == R.id.menu_AddRecepie) {
                Intent main = new Intent(mContext.getApplicationContext(), addRecipe_New.class);
                main.putExtra("username", username);
                main.putExtra("userRole", userRole);
                startActivity(main);
                Objects.requireNonNull(getActivity()).finishAffinity();
            } else if (itemId == R.id.menu_MyRecepies) {
                Intent myRecipes = new Intent(mContext.getApplicationContext(), MyRecipes.class);
                myRecipes.putExtra("username", username);
                myRecipes.putExtra("userRole", userRole);
                startActivity(myRecipes);
                Objects.requireNonNull(getActivity()).finishAffinity();
            } else if (itemId == R.id.menu_SearchRecepie) {
                Intent search = new Intent(mContext.getApplicationContext(), SearchRecipe.class);
                search.putExtra("username", username);
                search.putExtra("userRole", userRole);
                startActivity(search);
                Objects.requireNonNull(getActivity()).finishAffinity();
            } else if (itemId == R.id.menuInbox) {
                Intent inbox = new Intent(mContext.getApplicationContext(), Inbox_new.class);
                inbox.putExtra("username", username);
                inbox.putExtra("userRole", userRole);
                startActivity(inbox);
                Objects.requireNonNull(getActivity()).finishAffinity();
            } else if (itemId == R.id.menu_favRecipe) {
                Intent fav = new Intent(mContext.getApplicationContext(), FavoriteRecipes.class);
                fav.putExtra("username", username);
                fav.putExtra("userRole", userRole);
                startActivity(fav);
                Objects.requireNonNull(getActivity()).finishAffinity();
            }
            return false;
        });

        return view;
    }

}
