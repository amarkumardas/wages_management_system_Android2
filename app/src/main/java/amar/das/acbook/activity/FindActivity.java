package amar.das.acbook.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Comparator;

import amar.das.acbook.Database;
import amar.das.acbook.R;
import amar.das.acbook.adapters.SeparateAllMLGRecordAdapter;
import amar.das.acbook.adapters.SearchAdapter;
import amar.das.acbook.globalenum.GlobalConstants;
import amar.das.acbook.model.MLGAllRecordModel;
import amar.das.acbook.model.SearchModel;
import amar.das.acbook.ui.ml.MLFragment;
import amar.das.acbook.utility.MyUtility;

public class FindActivity extends AppCompatActivity {
SearchView searchView;
RecyclerView searchRecycler;
ArrayList<SearchModel> dataList;
ArrayList<MLGAllRecordModel> allMLGList;
Database db;
Button  btn1,btn2,btn3;
boolean bool=false;
TextView searchHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0); //we have used overridePendingTransition(), it is used to remove activity create animation while re-creating activity.This can be applied only on activity
        setContentView(R.layout.activity_find);

        db=Database.getInstance(this);//on start only database should be create
        //ids
        searchView=findViewById(R.id.serach_view);
        searchRecycler=findViewById(R.id.search_recyclerview);
        btn1=findViewById(R.id.mestre_btn);
        btn2=findViewById(R.id.laber_btn);
        btn3=findViewById(R.id.g_btn);
        searchHint=findViewById(R.id.search_hint);


        searchHint.setOnClickListener(view -> {//searchHint.setTooltipText("may name is amar\n kumar \n das");
            MyUtility.showResult(getResources().getString(R.string.searching_tips),getResources().getString(R.string.searching_tips_info),view.getContext());
         });
        searchRecycler.setHasFixedSize(true);

        //getting all data
        Cursor cursor=db.getData("SELECT "+Database.COL_1_ID+" , "+Database.COL_2_NAME+" , "+Database.COL_3_BANKAC+" , "+Database.COL_6_AADHAAR_NUMBER+" , "+Database.COL_8_MAINSKILL1+" , "+Database.COL_12_ACTIVE+" FROM "+Database.TABLE_NAME1 +" WHERE "+Database.COL_12_ACTIVE+"='1' OR "+Database.COL_12_ACTIVE+"='0'");
        dataList =new ArrayList<>();

        while(cursor.moveToNext()){
            SearchModel model=new SearchModel();
            model.setPhoneNumber(MyUtility.getActivePhoneNumbersFromDb(cursor.getString(0),getBaseContext()));
            model.setId(cursor.getString(0));
            model.setName(cursor.getString(1));
            model.setAccount(cursor.getString(2));
            model.setAadhaar(cursor.getString(3));
            model.setSkill(cursor.getString(4));
            model.setActive(cursor.getString(5).equals(GlobalConstants.ACTIVE.getValue())?true:false);
            dataList.add(model);
        }
        cursor.close();
        SearchAdapter searchAdapter=new SearchAdapter(this, dataList);

        searchRecycler.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        searchRecycler.setAdapter(searchAdapter);
        //db.close();//closing database to prevent data leak
        Database.closeDatabase();

       // searchView.setQuery("I",true); //to set default text to search box

        //when Find Activity open then automatically keyboard should open.this is manual way of opening keyboard
        //showSoftKeyboard(searchView);//to show keyboard code added to manifest file

        /* To open keyboard in Dialog box automatically
        Window window = dialog.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);*/

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                btn3.setBackgroundResource(R.drawable.white_detailsbg);
                btn2.setBackgroundResource(R.drawable.white_detailsbg);
                btn1.setBackgroundResource(R.drawable.white_detailsbg);//making background white so that if user pressed button then search then button background should be white
                if(bool) {//this will set adapter to recycler view while switching from button M L G
                    searchRecycler.setAdapter(searchAdapter);
                }

               searchAdapter.getFilter().filter(newText);
                return false;
            }
        });


    }
//    public void showSoftKeyboard(View searchView) {//code link https://developer.android.com/training/keyboard-input/visibility#java
//        if (searchView.requestFocus()) {
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT);
//        }
//    }
     public void invoiceBackupButton(View view){
         Intent intent = new Intent(view.getContext(), BackupCalculatedInvoicesActivity.class);
         startActivity(intent);
     }
    public void mestreButton(View view) {
        //setting back ground color
//        view.setBackgroundColor(getColor(R.color.background));
//        btn2.setBackgroundColor(Color.WHITE);
//        btn3.setBackgroundColor(Color.WHITE);

        //setting background custom image of theres button
        btn1.setBackgroundResource(R.drawable.graycolor_bg);
        btn3.setBackgroundResource(R.drawable.white_detailsbg);
        btn2.setBackgroundResource(R.drawable.white_detailsbg);
        btnData("SELECT "+Database.COL_1_ID+" , "+Database.COL_2_NAME+" , "+Database.COL_12_ACTIVE+" , "+Database.COL_15_LATESTDATE+" FROM "+Database.TABLE_NAME1 +" WHERE "+Database.COL_8_MAINSKILL1 +"='"+getResources().getString(R.string.mestre)+"'");
    }
    public void maleLaberButton(View view) {
        //setting back ground color
//        view.setBackgroundColor(getColor(R.color.background));
//        btn1.setBackgroundColor(Color.WHITE);
//        btn3.setBackgroundColor(Color.WHITE);

        //setting background custom image of theres button
        btn2.setBackgroundResource(R.drawable.graycolor_bg);
        btn3.setBackgroundResource(R.drawable.white_detailsbg);
        btn1.setBackgroundResource(R.drawable.white_detailsbg);
        btnData("SELECT "+Database.COL_1_ID+" , "+Database.COL_2_NAME+" , "+Database.COL_12_ACTIVE+" , "+Database.COL_15_LATESTDATE+" FROM "+Database.TABLE_NAME1 +" WHERE "+Database.COL_8_MAINSKILL1 +"='"+getResources().getString(R.string.laber)+"'");
    }
    public void womenLaberButton(View view) {
        //setting back ground color
//        view.setBackgroundColor(getColor(R.color.background));
//        btn1.setBackgroundColor(Color.WHITE);
//        btn2.setBackgroundColor(Color.WHITE);

        //setting background custom image of theres button
        btn3.setBackgroundResource(R.drawable.graycolor_bg);
        btn1.setBackgroundResource(R.drawable.white_detailsbg);
        btn2.setBackgroundResource(R.drawable.white_detailsbg);
        btnData("SELECT "+Database.COL_1_ID+" , "+Database.COL_2_NAME+" , "+Database.COL_12_ACTIVE+" , "+Database.COL_15_LATESTDATE+" FROM "+Database.TABLE_NAME1 +" WHERE "+Database.COL_8_MAINSKILL1 +"='"+getResources().getString(R.string.women_laber)+"'");
    }

    public void btnData(String query){
        //fetching data
        Cursor cursor2=db.getData(query);
        allMLGList=new ArrayList<>();

        while(cursor2.moveToNext()){
            MLGAllRecordModel model=new MLGAllRecordModel();
            model.setId(cursor2.getString(0));
            model.setName(cursor2.getString(1));
            model.setActive(cursor2.getString(2).equals(GlobalConstants.ACTIVE.getValue())?true:false);//to set view red if inactive
            model.setLatestDate(cursor2.getString(3));//to display inactive duration
            allMLGList.add(model);
        }
        cursor2.close();
        //sorting according to name IN accenting order by default or natural sorting order
        //anonymous inner class Lambda expression can be used
        allMLGList.sort(Comparator.comparing(MLGAllRecordModel::getName));

        SeparateAllMLGRecordAdapter allMLGRecordAdapter=new SeparateAllMLGRecordAdapter(this,allMLGList);
        searchRecycler.setHasFixedSize(true);
        searchRecycler.setAdapter(allMLGRecordAdapter);
        bool=true;//to set adapter recycler view on onQueryTextChange method
        Database.closeDatabase();
    }

    public void goto_back(View view) {
        finish();//first destroy then go back
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.find_layout, new MLFragment()).commit();
    }
    /*In some situations, we need to recall activity again from onCreate(). This example demonstrates how to reload activity
    whenever we return back to this activity we will always get refreshed activity
    Disadvantage is whenever we press back button then it will load all data eg:50000 then it will take time to return back because we are refreshing
    This feature is added to see only update of Name,Aadhaar card and Account number but this situation is very rare because people name,dharma,account would rarely change 1 time so removing this feature*/
//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        //we have used Intent to recreate an activity as shown below -
//        Intent i = new Intent(FindActivity.this, FindActivity.class);
//        finish();//destroying current/same activity
//        overridePendingTransition(0, 0);
//        startActivity(i);//starting same/current activity ie. refreshed activity
//        overridePendingTransition(0, 0);
//        //we have used overridePendingTransition(), it is used to remove activity create animation while re-creating activity.This can be done only in activity
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Database.closeDatabase();
    }
}