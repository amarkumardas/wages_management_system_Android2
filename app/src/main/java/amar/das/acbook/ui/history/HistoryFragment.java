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
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Locale;

import amar.das.acbook.Database;
import amar.das.acbook.R;
import amar.das.acbook.adapters.HistoryAdapter;
import amar.das.acbook.databinding.FragmentHistoryTabBinding;
import amar.das.acbook.model.HistoryModel;
import amar.das.acbook.utility.MyUtility;


public class HistoryFragment extends Fragment {
    public static String sameDayinserted ="1";//means inserted on same day
    public static String sameDayUpdated ="2";//means inserted and updated on same day
    public static String previousRecordUpdated ="3";//means updated previous day record
    public static String automaticInserted="4";//means automatic inserted by application
    public static boolean shareingToggle=false;
    LocalDate now=LocalDate.now();
    int year=now.getYear();
    byte dayOfMonth= (byte) now.getDayOfMonth(),month= (byte) now.getMonthValue();
    private FragmentHistoryTabBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState){
        binding = FragmentHistoryTabBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.historyDateViewTv.setText(dayOfMonth+"-"+(month)+"-"+year+" , "+getDayName(this.year,this.month,this.dayOfMonth));//initially set this

        binding.historyTotalPayment.setText(MyUtility.convertToIndianNumberSystem(getTotalPayment(year,month,dayOfMonth)));
        binding.historyTotalAmountReceived.setText(MyUtility.convertToIndianNumberSystem(getTotalReceivedPayment(year,month,dayOfMonth)));
        fetchData(container);//initially fetch today's date data

        binding.historyDateViewTv.setOnClickListener(view -> {
            DatePickerDialog datePickerDialog=new DatePickerDialog(getContext(), (datePicker, year, month, dayOfMonth) -> { //To show calendar dialog
                this.dayOfMonth=(byte)dayOfMonth;
                this.month=(byte)(month+1);//because her month value start from 0 so adding 1 to month
                this.year=year;
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
                binding.historyTotalPayment.setText(MyUtility.convertToIndianNumberSystem(getTotalPayment(year,month,dayOfMonth)));
                binding.historyTotalAmountReceived.setText(MyUtility.convertToIndianNumberSystem(getTotalReceivedPayment(year,month,dayOfMonth)));
                fetchData(container);
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        binding.historyPlusOneTv.setOnClickListener(view -> {
            setDate((byte)1);
        });
        binding.historyMinusOneTv.setOnClickListener(view -> {
           setDate((byte)-1);
       });

        binding.historyToggleToShare.setChecked((shareingToggle)?true:false);//due to static variable
        binding.historyToggleToShare.setOnClickListener(view -> {
               if(binding.historyToggleToShare.isChecked()){
                   shareingToggle=true;
                   MyUtility.snackBar(view,getResources().getString(R.string.sharing_to_active_phone_number_enabled));
               }else{
                   shareingToggle=false;
                   MyUtility.snackBar(view,getResources().getString(R.string.sharing_to_whatsapp_enabled));
               }
        });

        binding.historySharePdfOrTextfileIcon.setOnClickListener(view -> {
//            SELECT *
//                    FROM history_table
//            WHERE system_datetime BETWEEN '2023-12-16 00:00:00' AND '2023-12-17 23:59:59';
        });
        return root;
    }
    private long getTotalPayment(int year,byte month,byte dayOfMonth){
        try(Database db=Database.getInstance(getContext())){
           return Integer.parseInt(db.getTotalPaymentHistory(year,month,dayOfMonth));
        }catch(Exception x){
            x.printStackTrace();
            return 0;
        }
    }
    private long getTotalReceivedPayment(int year,byte month,byte dayOfMonth){
        try(Database db=Database.getInstance(getContext())){
            return Integer.parseInt(db.getTotalReceivedPaymentHistory(year,month,dayOfMonth));
        }catch(Exception x){
            x.printStackTrace();
            return 0;
        }
    }
    private void setDate(byte days){
        int date[]= getBeforeOrForwardDate(days,this.year,this.month,this.dayOfMonth);
        if(date != null){//updating
            this.year = date[0];
            this.month = (byte) date[1];
            this.dayOfMonth = (byte) date[2];
            binding.historyDateViewTv.setText(dayOfMonth+"-"+(month)+"-"+year+" , "+getDayName(this.year,this.month,this.dayOfMonth));//month start from 0 so 1 is added to get right month like 12
        }else{
            binding.historyDateViewTv.setText("ERROR");
        }
    }
    String getDayName(int year,byte month,byte dayOfMonth){
        LocalDate currentDate = LocalDate.of(year,month,dayOfMonth);
        return currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()).toUpperCase();
    }
    boolean fetchData(ViewGroup container){
        Database db=Database.getInstance(getContext());
        try(Cursor cursor = db.getSpecificDateHistory(year,month,dayOfMonth)){//data is sorted in desc order
            ArrayList<HistoryModel> historyData = new ArrayList<>();
            while(cursor.moveToNext()){
                HistoryModel model = new HistoryModel();
                model.setId(cursor.getString(0));
                model.setUserDate(cursor.getString(1));
                model.setRemarks(cursor.getString(2));
                model.setWagesOrDeposit(cursor.getInt(3));
                model.setP1Work(cursor.getShort(4));
                model.setP2Work(cursor.getShort(5));
                model.setP3Work(cursor.getShort(6));
                model.setP4Work(cursor.getShort(7));
                model.setIsDeposit(cursor.getString(8).equals("1"));
                model.setSystemTimeDate(cursor.getString(9));
                model.setP1Skill( cursor.getString(10) );
                model.setP2Skill( cursor.getString(11) );
                model.setP3Skill(  cursor.getString(12) );
                model.setP4Skill( cursor.getString(13) );
                model.setShared( cursor.getString(14) != null);
                model.setStatus(cursor.getString(15));
                model.setSubtractedAdvanceOrBal(cursor.getString(16));
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
    public int[] getBeforeOrForwardDate(byte forForwardDaysPlusAndPreviousDayMinus, int year, byte month, byte daysOfMonth){//if error return null
        try {//if 0 is passed then current date is return.if -1 then previous day +1 forward days
            int []date=new int[3];
            LocalDate currentDate = LocalDate.of(year,month,daysOfMonth);
            LocalDate  resultDate=null;

            if(forForwardDaysPlusAndPreviousDayMinus >= 0){
                resultDate = currentDate.plusDays(forForwardDaysPlusAndPreviousDayMinus);// Calculate a future date
            }else{
                resultDate = currentDate.minusDays(Math.abs(forForwardDaysPlusAndPreviousDayMinus));  // Calculate a past date
            }
            date[0]= resultDate.getYear();
            date[1]=resultDate.getMonthValue();
            date[2]=resultDate.getDayOfMonth();
            return date;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
//    public static HashMap<String,String> stringToHashMap(String string) {//1.if key is there and value not there then get() method will return null.2.if key is not there but we try to retrieve value then get() method will alse return null.
//        HashMap<String, String> hashMap = new HashMap<>();
//        string = string.replaceAll("[{}]", "");// Remove curly brackets using a single replace call.eg {WAGES=5000, P1_SKILL=0, P1_WORK=6, NAME=AMAR KUMAR DAS}
//        String[] keyValuePairs = string.split(",");
//        for (String keyValuePair : keyValuePairs) {
//            String[] keyValueArray = keyValuePair.split("=");
//            if (keyValueArray.length == 2) {//Added a check to ensure that a valid key-value pair is added to the HashMap. This check verifies that there are exactly two parts when splitting by "=", avoiding potential ArrayIndexOutOfBoundsException errors.
//                String key = keyValueArray[0].trim();
//                String value = keyValueArray[1].trim();
//                hashMap.put(key, value);
//            }
//        }
//        return hashMap;
//    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}