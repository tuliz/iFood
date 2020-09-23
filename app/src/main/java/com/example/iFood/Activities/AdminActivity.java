package com.example.iFood.Activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Range;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.iFood.Classes.Recipes;
import com.example.iFood.Classes.Users;
import com.example.iFood.MenuFragments.AddDrawFragment;
import com.example.iFood.MenuFragments.NavDrawFragment;
import com.example.iFood.R;
import com.example.iFood.Utils.ConnectionBCR;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

public class AdminActivity extends AppCompatActivity implements View.OnClickListener {
    public static String TAG = "AdminActivity";
    // DB Connection / related
    DatabaseReference refRecipes = FirebaseDatabase.getInstance().getReference().child("Recipes");
    DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference().child("Users");
    int userCount =0, recipeCount =0,userTotalCount=0,recipeTotalCount=0;
    // Bottom Bar
    BottomAppBar bottomAppBar;
    FloatingActionButton addIcon;
    // Button
    Button btnSearch;
    // Date Variables
    Date to,from;
    long userTime,recipeTime;
    int mYear, mMonth, mDay;
    // PieChart
    PieChartView usersChart,recipesChart;
    List<SliceValue> usersPieData = new ArrayList<>();
    List<SliceValue> recipesPieData = new ArrayList<>();
    PieChartData usersPieChart,recipesPieChart;
    // TextView
    TextView fromDate,toDate;
    // Broadcast
    ConnectionBCR bcr = new ConnectionBCR();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // hide top bar
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        setVariables();
        setCurrentDateonOpen();
        getDB_Data();



        // Bottom App bar
        bottomAppBar.setNavigationOnClickListener(v -> {
            NavDrawFragment bottomNavFrag = new NavDrawFragment();
            Bundle bundle = new Bundle();
            bundle.putString("username",getIntent().getStringExtra("username"));
            bundle.putString("userRole",getIntent().getStringExtra("userRole"));
            bottomNavFrag.setArguments(bundle);
            bottomNavFrag.show(getSupportFragmentManager(),"TAG");

        });
        bottomAppBar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if(id == R.id.bottomAbout){
                Intent about = new Intent(AdminActivity.this, About.class);
                startActivity(about);
            }
            return false;
        });


        addIcon.setOnClickListener(v -> {
            AddDrawFragment addIcon = new AddDrawFragment();
            Bundle bundle = new Bundle();
            bundle.putString("username",getIntent().getStringExtra("username"));
            bundle.putString("userRole",getIntent().getStringExtra("userRole"));
            addIcon.setArguments(bundle);
            addIcon.show(getSupportFragmentManager(),"TAG");
        });
        // onClick Listeners
        btnSearch.setOnClickListener(v -> {
            if(to==null || from==null){

                @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat(getString(R.string.date_format));
                try {
                    if(to==null) to = formatter.parse(toDate.getText().toString());
                    if(from==null)from = formatter.parse(toDate.getText().toString());
                    else{
                        from = formatter.parse(toDate.getText().toString());
                        to = formatter.parse(toDate.getText().toString());
                    }

                  //  Log.d(TAG, "onClick: toDate:"+to.getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
            else if (to.getTime() < from.getTime()) {
                Toast.makeText(AdminActivity.this,"Search credentials are not valid",Toast.LENGTH_SHORT).show();
            }
            else {
                refUsers.orderByKey();
                refUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for(DataSnapshot ds : snapshot.getChildren()){
                          Users u = ds.getValue(Users.class);
                          assert u != null;
                          userTime = (Long)u.timestamp;
                         //   @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.date_format));
                         //   String c = simpleDateFormat.format(userTime);
                            //Log.d("TAG","user date: "+c);
                          Range<Long> timeRange = Range.create(from.getTime(),
                                  to.getTime());
                          if(timeRange.contains(userTime)){

                           //   c = simpleDateFormat.format(userTime);
                              //Log.d("TAG","Found user match to date: "+c);
                              userCount++;
                              Log.d("TAG","User count is:"+userCount);
                          }
                      }
                        if(userCount>0) {
                            usersPieData.add(new SliceValue(userCount, Color.GREEN).setLabel("Users :" + userCount));
                            usersPieData.add(new SliceValue(userTotalCount, Color.parseColor("#FF5252")).setLabel("Total Users :"+userTotalCount));
                            userCount=0;
                        }else{
                            usersPieData.add(new SliceValue(userTotalCount, Color.parseColor("#FF5252")).setLabel("Total Users :"+userTotalCount));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                refRecipes.orderByKey();
                refRecipes.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for(DataSnapshot ds : snapshot.getChildren()) {
                            for (DataSnapshot dsResult : ds.getChildren()) {
                                Recipes rec = dsResult.getValue(Recipes.class);
                                assert rec != null;

                                recipeTime = (Long) rec.timestamp;
                              //  @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.date_format));
                                //String c = simpleDateFormat.format(recipeTime);
                               // Log.d("TAG"," recipe date: "+c);
                                Range<Long> recipeTimeRange = Range.create(from.getTime(),
                                        to.getTime());
                                if (recipeTimeRange.contains(recipeTime)) {

                                 //  c = simpleDateFormat.format(recipeTime);
                                  //  Log.d("TAG","Found recipe match to date: "+c);
                                    recipeCount++;
                                   Log.d("TAG","Recipe count is:"+recipeCount);
                                }
                            }

                        }
                        if(recipeCount>0) {
                            recipesPieData.add(new SliceValue(recipeCount, Color.RED).setLabel("Recipes :" + recipeCount));
                            recipesPieData.add(new SliceValue(recipeTotalCount, Color.parseColor("#3F51B5")).setLabel("Total Recipes :"+recipeTotalCount));
                            recipeCount=0;
                        }else{
                            recipesPieData.add(new SliceValue(recipeTotalCount, Color.parseColor("#3F51B5")).setLabel("Total Recipes :"+recipeTotalCount));
                        }
                        setChart();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

        });
        // more onClick listeners with over ride their onClick method
        fromDate.setOnClickListener(this);
        toDate.setOnClickListener(this);
    } // onCreate ends

    private void setChart(){
        recipesPieChart = new PieChartData(recipesPieData);
        usersPieChart = new PieChartData(usersPieData);

        //
        usersPieChart.setHasLabels(true).setValueLabelTextSize(10);
        usersPieChart.setValueLabelsTextColor(Color.parseColor("#FFFFFF"));
        //usersPieChart.setHasLabelsOutside(true);
        //
        recipesPieChart.setHasLabels(true).setValueLabelTextSize(10);
        recipesPieChart.setValueLabelsTextColor(Color.parseColor("#FFFFFF"));
        //recipesPieChart.setHasLabelsOutside(true);
        //
        recipesChart.setPieChartData(recipesPieChart);
        usersChart.setPieChartData(usersPieChart);
    }
    private void setVariables() {
        usersChart = findViewById(R.id.chartUsers);
        recipesChart = findViewById(R.id.chartRecipe);
        fromDate = findViewById(R.id.fromDate);
        toDate = findViewById(R.id.toDate);
        bottomAppBar = findViewById(R.id.bottomAppBar);
        addIcon = findViewById(R.id.bottomAddIcon);
       // usersAmount = findViewById(R.id.usersAmount);
      //  recipeAmount = findViewById(R.id.recipeAmount);
        btnSearch = findViewById(R.id.btnSearchAdmin);

    }


    private void getDB_Data(){
        Query dbQuery = refRecipes.orderByKey();
        dbQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    recipeTotalCount = Integer.parseInt(String.valueOf(snapshot.getChildrenCount()));
                    //recipeAmount.setText(String.valueOf(snapshot.getChildrenCount()));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        Query dbUsersQuery = refUsers.orderByKey();
        dbUsersQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    userTotalCount= Integer.parseInt(String.valueOf(snapshot.getChildrenCount()));
                   // usersAmount.setText(String.valueOf(snapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    /**
     * Convert Date to Text.
     * @param v gets the onClickListner that was clicked
     */
    @Override
    public void onClick(View v) {

        if (v == toDate) {
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);

            @SuppressLint("SetTextI18n") DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, monthOfYear, dayOfMonth) -> {

                        toDate.setText( dayOfMonth+ "-" + (monthOfYear + 1) + "-" + year);
                        @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat(getString(R.string.date_format));
                        try {
                            to = formatter.parse(toDate.getText().toString());
                            Log.d(TAG, "onClick: toDate:"+to.getTime());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    }, mYear, mMonth, mDay);
            datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
            datePickerDialog.show();
        }
        if(v == fromDate){
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);


            @SuppressLint("SetTextI18n") DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, monthOfYear, dayOfMonth) -> {

                        fromDate.setText( dayOfMonth+ "-" + (monthOfYear + 1) + "-" + year);
                        @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat(getString(R.string.date_format));
                        try {
                           from = formatter.parse(fromDate.getText().toString());
                            Log.d(TAG, "onClick: fromDate:"+from.getTime());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    }, mYear, mMonth, mDay);
            datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
            datePickerDialog.show();
    }


    }
    /**
     * Register our Broadcast Receiver when opening the app.
     */
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(bcr,filter);
    }

    /**
     * Stop our Broadcast Receiver when the app is closed.
     */
    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(bcr);
    }

    private void setCurrentDateonOpen(){
        Calendar c = Calendar.getInstance();
        int currentYear = c.get(Calendar.YEAR);
        int currentMonth = c.get(Calendar.MONTH);
        int currentDay = c.get(Calendar.DAY_OF_MONTH);

        String date =  currentDay +"-"+ (currentMonth+1) +"-"+ currentYear;
        toDate.setText(date);
        fromDate.setText(date);
    }
}