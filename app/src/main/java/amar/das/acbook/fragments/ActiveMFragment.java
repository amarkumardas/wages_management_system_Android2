package amar.das.acbook.fragments;

import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.util.ArrayList;


import amar.das.acbook.adapters.MestreLaberGAdapter;
import amar.das.acbook.model.MestreLaberGModel;
import amar.das.acbook.Database;
import amar.das.acbook.R;
import amar.das.acbook.databinding.FragmentActiveMBinding;
import amar.das.acbook.utility.MyUtility;


public class ActiveMFragment extends Fragment {

    private FragmentActiveMBinding binding;
    ArrayList<MestreLaberGModel> mestreActiveArrayList;
    RecyclerView mestreRecyclerView;
    MestreLaberGAdapter mestreLaberGAdapter;
    TextView advance,balance;
    Database db;

   // Boolean isScrolling1 =false,loadOrNot=true;
    LinearLayoutManager layoutManager;
    //int currentItem1, totalItem1, scrollOutItems1;
    ProgressBar progressBar;
    //SwipeRefreshLayout swipeRefreshLayout;
    //static Integer initialLoadDataFotActiveMAndL =200;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         binding=FragmentActiveMBinding.inflate(inflater,container,false);
         View root=binding.getRoot();
         db=Database.getInstance(getContext());
         //ids
         mestreRecyclerView=root.findViewById(R.id.recycle_active_mestre);
         progressBar=binding.progressBarActiveM;
         progressBar.setVisibility(View.GONE);//initially visibility will be not there only when data is loading then visibility set visible
//       swipeRefreshLayout=binding.pullToRefresh;
//        swipeRefreshLayout.setOnRefreshListener(() -> {
//            fetchData("SELECT IMAGE,ID,NAME,ADVANCE,BALANCE,LATESTDATE,TIME FROM " + db.TABLE_NAME1 + " WHERE TYPE='M' AND ACTIVE='1' AND LATESTDATE IS NOT NULL ORDER BY LATESTDATE DESC ", mestreactiveArrayList);
//            mestreLaberGAdapter.notifyDataSetChanged();
//            swipeRefreshLayout.setRefreshing(false);
//        });
         advance=root.findViewById(R.id.active_m_advance);
         balance=root.findViewById(R.id.active_m_balance);

         Cursor advanceBalanceCursor=db.getData("SELECT SUM("+Database.COL_13_ADVANCE+"),SUM("+Database.COL_14_BALANCE+") FROM "+Database.TABLE_NAME1+" WHERE "+Database.COL_8_MAINSKILL1 +"='"+getResources().getString(R.string.mestre)+"' AND "+Database.COL_12_ACTIVE+"='1'");
         advanceBalanceCursor.moveToFirst();
         advance.setText(HtmlCompat.fromHtml("ADVANCE: "+"<b>"+advanceBalanceCursor.getLong(0)+"</b>",HtmlCompat.FROM_HTML_MODE_LEGACY));
         balance.setText(HtmlCompat.fromHtml("BALANCE: "+"<b>"+advanceBalanceCursor.getLong(1)+"</b>",HtmlCompat.FROM_HTML_MODE_LEGACY));
         advanceBalanceCursor.close();

//        LocalDate todayDate = LocalDate.now();//current date; return 2022-05-01
//        String currentDateDBPattern =""+ todayDate.getDayOfMonth()+"-"+ todayDate.getMonthValue()+"-"+ todayDate.getYear();//converted to 1-5-2022

       // int totalRecord=getCountOfTotalRecordFromDb();//this will execute when refreshed ie.when new person is added and it will be help full to know exactly how much the record is there to load

        Cursor cursorMestre;
        mestreActiveArrayList =new ArrayList<>(80);//insuring initial capacity to (10+initialDataToLoad)  10 is extra for  if new person present
        //**if latest date is null then first it will be top of arraylist that's why two WHILE LOOP is used
        cursorMestre=db.getData("SELECT "+Database.COL_10_IMAGE+","+Database.COL_1_ID+","+Database.COL_2_NAME+","+Database.COL_13_ADVANCE+","+Database.COL_14_BALANCE+","+Database.COL_15_LATESTDATE+","+Database.COL_16_TIME+" FROM "+Database.TABLE_NAME1 +" WHERE "+Database.COL_8_MAINSKILL1 +"='"+getResources().getString(R.string.mestre)+"' AND "+Database.COL_12_ACTIVE+"='1' AND "+Database.COL_15_LATESTDATE+" IS NULL");
        while(cursorMestre.moveToNext()){//if cursor has 0 record then cursorMestre.moveToNext() return false
            MestreLaberGModel data=new MestreLaberGModel();
            data.setName(cursorMestre.getString(2));
            data.setPerson_img(cursorMestre.getBlob(0));
            data.setId(cursorMestre.getString(1));
            data.setAdvanceAmount(cursorMestre.getInt(3));
            data.setBalanceAmount(cursorMestre.getInt(4));
            data.setLatestDate(cursorMestre.getString(5));
            data.setTime(cursorMestre.getString(6));
            mestreActiveArrayList.add(data);
        }
       // cursorMestre=db.getData("SELECT IMAGE,ID,NAME,ADVANCE,BALANCE,LATESTDATE,TIME FROM "+db.TABLE_NAME1 +" WHERE TYPE='M' AND ACTIVE='1' AND LATESTDATE IS NOT NULL ORDER BY LATESTDATE DESC LIMIT "+ initialLoadDataFotActiveMAndL);
        cursorMestre=db.getData("SELECT "+Database.COL_10_IMAGE+","+Database.COL_1_ID+","+Database.COL_2_NAME+","+Database.COL_13_ADVANCE+","+Database.COL_14_BALANCE+","+Database.COL_15_LATESTDATE+","+Database.COL_16_TIME+" FROM "+Database.TABLE_NAME1 +" WHERE "+Database.COL_8_MAINSKILL1 +"='"+getResources().getString(R.string.mestre)+"' AND "+Database.COL_12_ACTIVE+"='1' AND "+Database.COL_15_LATESTDATE+" IS NOT NULL ORDER BY "+Database.COL_15_LATESTDATE+" DESC");
        while(cursorMestre.moveToNext()){
            MestreLaberGModel data=new MestreLaberGModel();
            data.setName(cursorMestre.getString(2));
            data.setPerson_img(cursorMestre.getBlob(0));
            data.setId(cursorMestre.getString(1));
            data.setAdvanceAmount(cursorMestre.getInt(3));
            data.setBalanceAmount(cursorMestre.getInt(4));
            data.setLatestDate(cursorMestre.getString(5));
            data.setTime(cursorMestre.getString(6));
            mestreActiveArrayList.add(data);
        }

        mestreActiveArrayList.trimToSize();//to release free space
        MyUtility.sortArrayList(mestreActiveArrayList);//if list latest date is null then at index 0 ,1..should contain null object then non null object that y two while loop is taken
        cursorMestre.close();//closing cursor after finish
        //db.close();//closing database to prevent data leak
        Database.closeDatabase();
        mestreLaberGAdapter =new MestreLaberGAdapter(getContext(), mestreActiveArrayList);
        mestreRecyclerView.setAdapter(mestreLaberGAdapter);
        mestreRecyclerView.setHasFixedSize(true);//telling to recycler view that don't calculate item size every time when added and remove from recyclerview
        layoutManager=new GridLayoutManager(getContext(),4);//span-count is number of rows
        mestreRecyclerView.setLayoutManager(layoutManager);

        //scroll listener removing this scrolling is smooth and fast
//        mestreRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//        //int hold=40;
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {//Callback method to be invoked when RecyclerView's scroll state changes.
//               if(loadOrNot) {
//                   super.onScrollStateChanged(recyclerView, newState);
//                   //this will tell the state of scrolling if user is scrolling then isScrolling variable will become true
//                   if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
//                       isScrolling1 = true;//when user start to scroll then this varilable will be true
//                   }
//               }
//            }
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {//Callback method to be invoked when the RecyclerView has been scrolled. This will be called after the scroll has completed.This callback will also be called if visible item range changes after a layout calculation. In that case, dx and dy will be 0.
//                if (loadOrNot){
//                    super.onScrolled(recyclerView, dx, dy);
//                currentItem1 = layoutManager.getChildCount();
//                totalItem1 = mestreLaberGAdapter.getItemCount();// totalItem=manager.getItemCount();
//                scrollOutItems1 = layoutManager.findFirstVisibleItemPosition();
//
//                if (isScrolling1 && ((currentItem1 + scrollOutItems1) == totalItem1)) {
//                    isScrolling1 = false;
//                    progressBar.setVisibility(View.VISIBLE);//progressbar
//                    Toast.makeText(getContext(), "PLEASE WAIT LOADING", Toast.LENGTH_SHORT).show();
//                    fetchData("SELECT IMAGE,ID,NAME,ADVANCE,BALANCE,LATESTDATE,TIME FROM " + db.TABLE_NAME1 + " WHERE TYPE='M' AND ACTIVE='1' AND LATESTDATE IS NOT NULL ORDER BY LATESTDATE DESC ", mestreactiveArrayList);
//                    //fetchData("SELECT IMAGE,ID,NAME,ADVANCE,BALANCE,LATESTDATE,TIME FROM "+db.TABLE_NAME1 +" WHERE TYPE='M' AND ACTIVE='1' AND LATESTDATE IS NOT NULL ORDER BY LATESTDATE DESC LIMIT "+(hold)+","+40,mestreactiveArrayList);
//                    // hold=hold+40;
//                    // mestreRecyclerView.scrollToPosition(0);
//                    // if((hold) > totalRecord){
//                    // Toast.makeText(getContext(), "hold-"+hold+"-total record "+totalRecord, Toast.LENGTH_SHORT).show();
//                   // mestreRecyclerView.clearOnScrollListeners();//this will remove scrollListener so we wont be able to scroll after loading all data and finished scrolling to last
//                    // }
//                    loadOrNot = false;
//                }
//            }
//            }
//        });
        return root;
    }

    public int getCountOfTotalRecordFromDb() {
        int count;
        // Database db=new Database(getContext());
        try(Database db = Database.getInstance(getContext())){
        Cursor cursor;
        cursor = db.getData("SELECT COUNT() FROM " + Database.TABLE_NAME1 + " WHERE " + Database.COL_8_MAINSKILL1 + "='" + getResources().getString(R.string.mestre) + "' AND " + Database.COL_12_ACTIVE + "='1' AND " + Database.COL_15_LATESTDATE + " IS NULL");
        cursor.moveToFirst();
        count = cursor.getInt(0);
        cursor = db.getData("SELECT COUNT() FROM " + Database.TABLE_NAME1 + " WHERE " + Database.COL_8_MAINSKILL1 + "='" + getResources().getString(R.string.mestre) + "' AND " + Database.COL_12_ACTIVE + "='1' AND " + Database.COL_15_LATESTDATE + " IS NOT NULL");
        cursor.moveToFirst();
        count = count + cursor.getInt(0);
        return count;
        //db.close();
        }catch (Exception x){
            x.printStackTrace();
            return  0;
        }

    }

    private void fetchData(String query, ArrayList<MestreLaberGModel> arraylist) {
        new Handler().postDelayed(() -> {
            loadData(query,arraylist);
            progressBar.setVisibility(View.GONE);//after data loading progressbar disabled
         }, 1000);
    }

    private void loadData(String querys, ArrayList<MestreLaberGModel> arraylist) {
        db=Database.getInstance(getContext());//this should be first statement to load data from db
        Cursor cursorMestre;
        arraylist.clear();//clearing the previous object which is there
        arraylist.ensureCapacity(getCountOfTotalRecordFromDb());//to get exact arraylist storage to store exact record

        //**if latest date is null then first it will be top of arraylist that's why two while Loop is used
        cursorMestre=db.getData("SELECT "+Database.COL_10_IMAGE+","+Database.COL_1_ID+","+Database.COL_2_NAME+","+Database.COL_13_ADVANCE+","+Database.COL_14_BALANCE+","+Database.COL_15_LATESTDATE+","+Database.COL_16_TIME+" FROM "+Database.TABLE_NAME1 +" WHERE "+Database.COL_8_MAINSKILL1 +"='"+getResources().getString(R.string.mestre)+"' AND "+Database.COL_12_ACTIVE+"='1' AND "+Database.COL_15_LATESTDATE+" IS NULL");
        while(cursorMestre.moveToNext()){//if cursor has 0 record then cursorMestre.moveToNext() return false
            MestreLaberGModel data=new MestreLaberGModel();
            data.setName(cursorMestre.getString(2));
            data.setPerson_img(cursorMestre.getBlob(0));
            data.setId(cursorMestre.getString(1));
            data.setAdvanceAmount(cursorMestre.getInt(3));
            data.setBalanceAmount(cursorMestre.getInt(4));
            data.setLatestDate(cursorMestre.getString(5));
            data.setTime(cursorMestre.getString(6));
            arraylist.add(data);//adding data to mestre arraylist
        }
       // mestreLaberGAdapter.notifyDataSetChanged();//Use the notifyDataSetChanged() when the list is updated,or inserted or deleted


        cursorMestre = db.getData(querys);//getting data from db
        while (cursorMestre.moveToNext()) {
            MestreLaberGModel data = new MestreLaberGModel();
            data.setName(cursorMestre.getString(2));
            data.setPerson_img(cursorMestre.getBlob(0));
            data.setId(cursorMestre.getString(1));
            data.setAdvanceAmount(cursorMestre.getInt(3));
            data.setBalanceAmount(cursorMestre.getInt(4));
            data.setLatestDate(cursorMestre.getString(5));
            data.setTime(cursorMestre.getString(6));
            arraylist.add(data);
        }
        mestreLaberGAdapter.notifyDataSetChanged();//Use the notifyDataSetChanged() when the list is updated,or inserted or deleted

        arraylist.trimToSize();//to release free space
        MyUtility.sortArrayList(arraylist);
        cursorMestre.close();
        //db.close();//closing database
        Database.closeDatabase();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Database.closeDatabase();
    }
}