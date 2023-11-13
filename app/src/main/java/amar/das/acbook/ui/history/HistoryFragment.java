package amar.das.acbook.ui.history;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import amar.das.acbook.Database;
import amar.das.acbook.R;
import amar.das.acbook.activity.IndividualPersonDetailActivity;
import amar.das.acbook.adapters.HistoryAdapter;
import amar.das.acbook.databinding.FragmentHistoryTabBinding;
import amar.das.acbook.model.HistoryModel;
import amar.das.acbook.utility.MyUtility;


public class HistoryFragment extends Fragment {
    public static String sameDayinserted ="1";//means inserted on same day
    public static String sameDayUpdated ="2";//means inserted and updated on same day
    public static String previousRecordUpdated ="3";//means updated previous record
    public static String automaticInserted="4";//means automatic inserted by application

    LocalDate now=LocalDate.now();
    int year=now.getYear();
    byte dayOfMonth= (byte) now.getDayOfMonth(),month= (byte) now.getMonthValue(),plus1,minus1;
    private FragmentHistoryTabBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoryTabBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.historyDateViewTv.setText(dayOfMonth+"-"+(month+1)+"-"+year+" , "+getDayName(this.year,this.month,this.dayOfMonth));//initially set this
        fetchData(container);
        binding.historyDateViewTv.setOnClickListener(view -> {
            DatePickerDialog datePickerDialog=new DatePickerDialog(getContext(), (datePicker, year, month, dayOfMonth) -> { //To show calendar dialog
                this.dayOfMonth=(byte)dayOfMonth;
                this.month=(byte)(month+1);
                this.year=year;
                minus1=plus1=0;
                binding.historyDateViewTv.setText(dayOfMonth+"-"+(month+1)+"-"+year+" , "+getDayName(this.year,this.month,this.dayOfMonth));//month start from 0 so 1 is added to get right month like 12
            },year,month-1,dayOfMonth);//This variable should be ordered this variable will set date day month to calendar to datePickerDialog so passing it
            datePickerDialog.show();
        });
        binding.historyDateViewTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                fetchData(container);
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //LocalDate currentDate = LocalDate.of();
        //Increase the date by one day
//        LocalDate increasedDate = currentDate.plusDays(1);
//        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("dd-MM-yyyy");
//        increasedDate.format(formatters);
//
//        //Decrease the date by one day
//        LocalDate decreasedDate = currentDate.minusDays(1);
//
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
//        decreasedDate.format(formatter);

        return root;
    }
    String getDayName(int year,byte month,byte dayOfMonth){
        LocalDate currentDate = LocalDate.of(year,month,dayOfMonth);
        return currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()).toUpperCase();
    }
    boolean fetchData(ViewGroup container){
        Database db=Database.getInstance(getContext());
        try(Cursor cursor = db.getSpecificDateHistory(year,month,dayOfMonth)){//data is sorted in desc order
            ArrayList<HistoryModel> historyData = new ArrayList<>();
            while (cursor.moveToNext()){
                HistoryModel model = new HistoryModel();
                model.setId(cursor.getString(0));
                model.setUserDate(cursor.getString(1));
                model.setRemarks(cursor.getString(2));
                model.setWagesOrDeposit(cursor.getInt(3));
                model.setP1Work((short) cursor.getInt(4));
                model.setP2Work((short) cursor.getInt(5));
                model.setP3Work((short) cursor.getInt(6));
                model.setP4Work((short) cursor.getInt(7));
                model.setIsDeposit((cursor.getString(8).equals("1"))? true:false);
                model.setSystemTimeDate(cursor.getString(9));
                model.setP1Skill( cursor.getString(10) );
                model.setP2Skill( cursor.getString(11) );
                model.setP3Skill(  cursor.getString(12) );
                model.setP4Skill( cursor.getString(13) );
                model.setShared((cursor.getString(14) != null)?true:false);
                model.setStatus(cursor.getString(15));
                model.setSubtractedAdvanceOrBal( cursor.getInt(16));
                model.setName(cursor.getString(17));
                historyData.add(model);
            }
            if(historyData.size()==0){
                MyUtility.snackBar(container,getResources().getString(R.string.history_not_available));
            }
            HistoryAdapter historyAdapter = new HistoryAdapter(getContext(), historyData);
            binding.historyRecyclerview.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            binding.historyRecyclerview.setHasFixedSize(true);
            binding.historyRecyclerview.setAdapter(historyAdapter);
        }catch(Exception x){
            x.printStackTrace();
            return false;
        }finally{
            if(db!=null) db.close();
        }
        return true;
    }
    public static HashMap<String,String> stringToHashMap(String string) {//1.if key is there and value not there then get() method will return null.2.if key is not there but we try to retrieve value then get() method will alse return null.
        HashMap<String, String> hashMap = new HashMap<>();
        string = string.replaceAll("[{}]", "");// Remove curly brackets using a single replace call.eg {WAGES=5000, P1_SKILL=0, P1_WORK=6, NAME=AMAR KUMAR DAS}
        String[] keyValuePairs = string.split(",");
        for (String keyValuePair : keyValuePairs) {
            String[] keyValueArray = keyValuePair.split("=");
            if (keyValueArray.length == 2) {//Added a check to ensure that a valid key-value pair is added to the HashMap. This check verifies that there are exactly two parts when splitting by "=", avoiding potential ArrayIndexOutOfBoundsException errors.
                String key = keyValueArray[0].trim();
                String value = keyValueArray[1].trim();
                hashMap.put(key, value);
            }
        }
        return hashMap;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}