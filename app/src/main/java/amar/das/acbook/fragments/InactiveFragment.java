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
import android.widget.AbsListView;
import android.widget.ProgressBar;

import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import amar.das.acbook.adapters.InactiveAdapter;
import amar.das.acbook.model.MestreLaberGModel;
import amar.das.acbook.Database;
import amar.das.acbook.R;
import amar.das.acbook.databinding.FragmentInactiveBinding;



public class InactiveFragment extends Fragment {
    private FragmentInactiveBinding binding;
    ArrayList<MestreLaberGModel> inactiveArraylist;
    RecyclerView inactiveRecyclerView;
    InactiveAdapter inactiveAdapter;
    Database db;
    Boolean isScrolling1 =false,loadOrNot=true;
    TextView advance,balance;

    LinearLayoutManager layoutManager;
    int currentItem1, totalItem1, scrollOutItems1, totalNumberOfLoadedData,totalSpecificInactiveRecord;
    //char skillIndicator='M';//for loading initial data
    String skillIndicator;//for not loading initial data
    ProgressBar progressBar;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding= FragmentInactiveBinding.inflate(inflater, container, false);
         View root=binding.getRoot();
        //ids
        inactiveRecyclerView =root.findViewById(R.id.recycleview_inactive);
        progressBar=binding.progressBarInactive;
        progressBar.setVisibility(View.GONE);//initially visibility will be not there only when data is loading then visibility set visible

        advance=root.findViewById(R.id.inactive_advance);//binding not taken BECAUSE we are passing in method
        balance=root.findViewById(R.id.inactive_balance);

        //to load initial inactive data
       // binding.mestreTotalInactiveRadioGroup.setChecked(true);//mestre radio button BY DEFAULT SHOW mestre record
       // loadDataByTakingSkillAndDefaultNoOfDataToLoadInitially('M',29);

        binding.inactiveRadiogroup.setOnCheckedChangeListener((radioGroup1, checkedIdOfRadioBtn) -> {
            if(checkedIdOfRadioBtn==R.id.mestre_total_inactive_radiogroup){
                loadDataByTakingSkillAndDefaultNoOfDataToLoadInitially(getResources().getString(R.string.mestre),29);
            } else if (checkedIdOfRadioBtn==R.id.laber_total_inactive_radiogroup) {
                loadDataByTakingSkillAndDefaultNoOfDataToLoadInitially(getResources().getString(R.string.laber),29);
            } else if (checkedIdOfRadioBtn==R.id.g_total_inactive_radiogroup) {
                loadDataByTakingSkillAndDefaultNoOfDataToLoadInitially(getResources().getString(R.string.women_laber),29);
            }

           // switch(checkedIdOfRadioBtn){
//                case R.id.mestre_total_inactive_radiogroup:{//M
//                    loadDataByTakingSkillAndDefaultNoOfDataToLoadInitially(getResources().getString(R.string.mestre),29);
//                    break;
//                }
//                case R.id.laber_total_inactive_radiogroup:{//L
//                    loadDataByTakingSkillAndDefaultNoOfDataToLoadInitially(getResources().getString(R.string.laber),29);
//                    break;
//                }
//                case R.id.g_total_inactive_radiogroup:{//G
//                    loadDataByTakingSkillAndDefaultNoOfDataToLoadInitially(getResources().getString(R.string.women_laber),29);
//                    break;
//                }
//            }
        });
        inactiveRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override//this method is called when we start scrolling recyclerview
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if(loadOrNot) {
                    super.onScrollStateChanged(recyclerView, newState);
                    //this will tell the state of scrolling if user is scrolling then isScrolling variable will become true
                    if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL && loadOrNot) {
                        isScrolling1 = true;//when user start to scroll then this variable will be true
                    }
                }
            }
            @Override//after scrolling finished then this method will be called
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (loadOrNot){//when all data is loaded then don't load anything
                    super.onScrolled(recyclerView, dx, dy);
                    currentItem1 = layoutManager.getChildCount();
                    totalItem1 = inactiveAdapter.getItemCount();// totalItem=manager.getItemCount();
                    scrollOutItems1 = layoutManager.findFirstVisibleItemPosition();

                    if (isScrolling1 && ((currentItem1 + scrollOutItems1) == totalItem1)) {
                        isScrolling1 = false;
                        progressBar.setVisibility(View.VISIBLE);//progressbar

                        Toast.makeText(getContext(), getResources().getString(R.string.please_wait_loading), Toast.LENGTH_SHORT).show();
                        fetchData("SELECT "+Database.COL_10_IMAGE+","+Database.COL_1_ID+","+Database.COL_13_ADVANCE+","+Database.COL_14_BALANCE+" FROM " + Database.TABLE_NAME1 + " WHERE "+Database.COL_8_SKILL+"='"+skillIndicator+"' AND "+Database.COL_12_ACTIVE+"='0' ORDER BY "+Database.COL_13_ADVANCE+" DESC LIMIT " + totalNumberOfLoadedData + "," + 40, inactiveArraylist);

                        totalNumberOfLoadedData = totalNumberOfLoadedData + 40;//40 data will be loaded and this variable represents total data already loaded
                        if (totalNumberOfLoadedData >= totalSpecificInactiveRecord) {//when all record loaded then remove scroll listener
                            //inactiveRecyclerView.clearOnScrollListeners();//this will remove scrollListener so we wont be able to scroll after loading all data and finished scrolling to last
                            loadOrNot = false;//alternative way to remove inactiveRecyclerView.clearOnScrollListeners()
                        }

                    }else{                    //29 > 23
                        if(totalNumberOfLoadedData >= totalSpecificInactiveRecord) {//when data is very less then totalNumberOfLoadedData it should not load and progress should not be visible
                            loadOrNot = false;
                            progressBar.setVisibility(View.GONE);//progressbar not visible
                        }
                    }
                }
            }
        });

        return root;
    }

    public ArrayList<MestreLaberGModel> loadInitialDataHavingTotalAdvanceNBalanceOfInactive(TextView advance,TextView balance,String skill,int limit,ArrayList<MestreLaberGModel> arraylist) {
        db=new Database(getContext());//on start only database should be create

        Cursor advanceBalanceCursor=db.getData("SELECT SUM("+Database.COL_13_ADVANCE+"),SUM("+Database.COL_14_BALANCE+") FROM "+Database.TABLE_NAME1+" WHERE +"+Database.COL_8_SKILL+"='"+skill+"' AND ("+Database.COL_12_ACTIVE+"='0')");
        advanceBalanceCursor.moveToFirst();
        advance.setText(HtmlCompat.fromHtml("ADVANCE: "+"<b>"+advanceBalanceCursor.getLong(0)+"</b>",HtmlCompat.FROM_HTML_MODE_LEGACY));
        balance.setText(HtmlCompat.fromHtml("BALANCE: "+"<b>"+advanceBalanceCursor.getLong(1)+"</b>",HtmlCompat.FROM_HTML_MODE_LEGACY));
        advanceBalanceCursor.close();

        Cursor dataCursorMLG=db.getData("SELECT "+Database.COL_10_IMAGE+","+Database.COL_1_ID+","+Database.COL_13_ADVANCE+","+Database.COL_14_BALANCE+" FROM "+Database.TABLE_NAME1 +" WHERE "+Database.COL_8_SKILL+"='"+skill+"' AND "+Database.COL_12_ACTIVE+"='0' ORDER BY "+Database.COL_13_ADVANCE+" DESC LIMIT "+limit);
        arraylist =new ArrayList<>(150);//capacity is 150 because when arraylist size become greater then 100 then arraylist will be cleared.extra 50 is kept because we don't know arraylist size become greater then 100 is exactly how much
        while(dataCursorMLG.moveToNext()){
            MestreLaberGModel data=new MestreLaberGModel();
            data.setPerson_img(dataCursorMLG.getBlob(0));
            data.setId(dataCursorMLG.getString(1));
            data.setAdvanceAmount(dataCursorMLG.getInt(2));
            data.setBalanceAmount(dataCursorMLG.getInt(3));
            arraylist.add(data);
        }
        arraylist.trimToSize();//to free space
        dataCursorMLG.close();
        db.close();
        return arraylist;
    }

    public void loadDataByTakingSkillAndDefaultNoOfDataToLoadInitially(String skill, int loadDataInitially) {
        skillIndicator=skill;
        totalNumberOfLoadedData=loadDataInitially;//default no of data to load initially
        loadOrNot=true;
        totalSpecificInactiveRecord= getCountOfSpecificInactiveTotalRecord(skill);
        inactiveArraylist=loadInitialDataHavingTotalAdvanceNBalanceOfInactive(advance,balance,skill, totalNumberOfLoadedData,inactiveArraylist);//updating inactive arraylist otherwise NPE don't know its referenced is passed but still not updated in method

        inactiveAdapter =new InactiveAdapter(getContext(), inactiveArraylist);//this common code should be there otherwise adapter will not be updated
        inactiveRecyclerView.setAdapter(inactiveAdapter);
       //binding.recycleViewInactive.scrollToPosition(0);//not working
        layoutManager =new GridLayoutManager(getContext(),4);//grid layout
        inactiveRecyclerView.setLayoutManager(layoutManager);
        inactiveRecyclerView.setHasFixedSize(true);//telling to recycler view that don't calculate item size every time when added and remove from recyclerview
    }

    public int getCountOfSpecificInactiveTotalRecord(String skill) {
        Database db=new Database(getContext());
        Cursor  cursor=db.getData("SELECT COUNT() FROM "+Database.TABLE_NAME1 +" WHERE "+Database.COL_8_SKILL+"='"+skill+"' AND "+Database.COL_12_ACTIVE+"='0'");
        cursor.moveToFirst();
        int count=cursor.getInt(0);
        cursor.close();
        db.close();
        return count;
    }

    private void fetchData(String query, ArrayList<MestreLaberGModel> arraylist) {
        new Handler().postDelayed(() -> {
            dataLoad(query,arraylist);
            progressBar.setVisibility(View.GONE);//after data loading progressbar disabled
         }, 2000);//wait for 3 seconds
    }

    private void dataLoad(String querys,ArrayList<MestreLaberGModel> arraylist){
        db=new Database(getContext());
        Cursor cursorMestre = db.getData(querys);//getting image from database
        if(arraylist.size()>=100){//when arraylist size is greater then 100 then free space but ensure capacity will be as mention during declaration ie.150
            arraylist.clear();
        }
        while (cursorMestre.moveToNext()) {
            MestreLaberGModel data = new MestreLaberGModel();
             data.setPerson_img(cursorMestre.getBlob(0));
             data.setAdvanceAmount(cursorMestre.getInt(2));
             data.setBalanceAmount(cursorMestre.getInt(3));
             data.setId(cursorMestre.getString(1));
             arraylist.add(data);
        }
        inactiveAdapter.notifyDataSetChanged();//Use the notifyDataSetChanged() every time the list is updated,or inserted or deleted

        arraylist.trimToSize();//If the size of the ArrayList is increased, the ensureCapacity() method will not have any effect. The ensureCapacity() method is used to ensure that the ArrayList has enough room to store the specified number of elements. If the size of the ArrayList is increased, the ensureCapacity() method will not be triggered.
        cursorMestre.close();
        db.close();//closing database
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}