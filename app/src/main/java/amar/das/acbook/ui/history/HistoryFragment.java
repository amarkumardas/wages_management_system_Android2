package amar.das.acbook.ui.history;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import amar.das.acbook.Database;
import amar.das.acbook.R;
import amar.das.acbook.activity.PdfViewerOperationActivity;
import amar.das.acbook.adapters.HistoryAdapter;
import amar.das.acbook.databinding.FragmentHistoryTabBinding;
import amar.das.acbook.model.HistoryModel;
import amar.das.acbook.pdfgenerator.MakePdf;

import amar.das.acbook.progressdialog.ProgressDialogHelper;
import amar.das.acbook.sharedpreferences.SharedPreferencesHelper;
import amar.das.acbook.utility.MyUtility;

public class HistoryFragment extends Fragment {
    public static final String sameDayInserted ="1";//means inserted on same day or same day payment
    public static final String sameDayUpdated ="2";//means inserted and updated on same day or same day updated payment
    public static final String previousRecordUpdated ="3";//means updated previous day record or previous day payment updated
    public static final String automaticInserted="4";//means automatic inserted by application
    public static boolean shareingToggle=false;
    private LocalDate currentDate=LocalDate.now();
    int year=currentDate.getYear();//for date change by 1
    byte dayOfMonth= (byte)currentDate.getDayOfMonth(),month= (byte)currentDate.getMonthValue();//for date change by 1
    int startYear=currentDate.getYear(),endYear=currentDate.getYear();//for pdf generation use in lamda expression because local variable need to be final but class variable need not to be final
    byte startDayOfMonth=(byte)currentDate.getDayOfMonth(),startMonth=(byte)currentDate.getMonthValue(),endDayOfMonth=(byte)currentDate.getDayOfMonth(),endMonth=(byte)currentDate.getMonthValue();//for pdf generation use in lamda expression because local variable need to be final but class variable need not to be final

    int currentItem1, totalItem1, scrollOutItems1, totalNumberOfLoadedData, totalRecord;
    boolean isScrolling1 =false,loadOrNot=true;
    LinearLayoutManager layoutManager;
    HistoryAdapter historyAdapter;
    ArrayList<HistoryModel> historyArraylist;
    byte initialDataToLoad=15;//rows byte range from -128 to 127
    final byte eachTimeDataToLoad=40;//recycler view will load data when initial data is finish
    ProgressDialogHelper progressDialogHelper;
    final byte defaultHistoryStoringForOneWeek =7,twoWeek=14,threeWeek=21;
    private FragmentHistoryTabBinding binding;
    byte whichOneTwoFetch=0;//0,1,2,3
    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState){
        binding = FragmentHistoryTabBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        progressDialogHelper=new ProgressDialogHelper(getContext());
        binding.historyTotalWorkedPeople.setText(getTotalWorkedPeopleMessage(year,month,dayOfMonth));//initially message of total worked people
        binding.historyDateViewTv.setText(getDateAndDayName(year,month,dayOfMonth));//initially set this
        binding.historyTotalPayment.setText(MyUtility.convertToIndianNumberSystem(getTotalPayment(year,month,dayOfMonth)));//initially
        binding.historyTotalAmountReceived.setText(MyUtility.convertToIndianNumberSystem(getTotalReceivedPayment(year,month,dayOfMonth)));//initially
        deleteHistoryPreviousData(); //initially set values and perform operations

        binding.historyTotalPayment.setOnClickListener(view -> {
            loadTotalPaymentDataInitially(year,month,dayOfMonth,initialDataToLoad,container);//initially fetch today's date data
        });
        binding.historyTotalAmountReceived.setOnClickListener(view -> {
            loadTotalPaymentReceivedDataInitially(year,month,dayOfMonth,initialDataToLoad,container);
        });
        loadALLMixedHistoryDataInitially(year,month,dayOfMonth,initialDataToLoad,container);//initially fetch today's date data
        binding.historyDateViewTv.setOnClickListener(view -> {
            DatePickerDialog datePickerDialog=new DatePickerDialog(getContext(), (datePicker, year, month, dayOfMonth) -> { //To show calendar dialog
                this.dayOfMonth=(byte)dayOfMonth;
                this.month=(byte)(month+1);//because her month value start from 0 so adding 1 to month
                this.year=year;
                binding.historyDateViewTv.setText(getDateAndDayName(this.year,this.month,this.dayOfMonth));//month start from 0 so 1 is added to get right month like 12
            },year,month-1,dayOfMonth);//This variable should be ordered this variable will set date day month to calendar to datePickerDialog so passing it
            datePickerDialog.show();
        });
        binding.historyDateViewTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.historyTotalWorkedPeople.setText(getTotalWorkedPeopleMessage(year,month,dayOfMonth));//message of total worked people
                binding.historyTotalPayment.setText(MyUtility.convertToIndianNumberSystem(getTotalPayment(year,month,dayOfMonth)));
                binding.historyTotalAmountReceived.setText(MyUtility.convertToIndianNumberSystem(getTotalReceivedPayment(year,month,dayOfMonth)));
                binding.historyTotalAmountReceived.setBackgroundResource(R.drawable.grey_line_bg_forlayout);
                binding.historyTotalPayment.setBackgroundResource(R.drawable.grey_line_bg_forlayout);
                loadALLMixedHistoryDataInitially(year,month,dayOfMonth,initialDataToLoad,container);
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        binding.historyPlusOneTv.setOnClickListener(view -> setDate((byte)1));
        binding.historyMinusOneTv.setOnClickListener(view -> setDate((byte)-1));
        binding.historyToggleToShare.setChecked(shareingToggle);//due to static variable
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
            AlertDialog.Builder myCustomDialog=new AlertDialog.Builder(getContext());
            LayoutInflater inflate=LayoutInflater.from(getContext());

            View myView=inflate.inflate(R.layout.history_pdf_generator,null);//myView contain all layout view ids
            myCustomDialog.setView(myView);//set custom layout to alert dialog
            myCustomDialog.setCancelable(true);//if user touch to other place then dialog will not be close

            final AlertDialog customDialog=myCustomDialog.create();//myCustomDialog variable cannot be use in inner class so creating another final variable  to use in inner class

            TextView startDate=myView.findViewById(R.id.history_from_date);
            startDate.setText(currentDate.getDayOfMonth()+"-"+currentDate.getMonthValue()+"-"+currentDate.getYear());

            TextView endDate=myView.findViewById(R.id.history_end_date);
            endDate.setText(currentDate.getDayOfMonth()+"-"+currentDate.getMonthValue()+"-"+currentDate.getYear());

            TextView message=myView.findViewById(R.id.history_keeping_date);
            Button sharePdf=myView.findViewById(R.id.history_generate_pdf);
            Button cancelHistoryDialog=myView.findViewById(R.id.history_pdf_cancel);
            cancelHistoryDialog.setOnClickListener(view1 -> customDialog.dismiss());
            message.setText(historyKeepingRangeMessage());

            startDate.setOnClickListener(view13 -> {
                DatePickerDialog datePickerDialog=new DatePickerDialog(getContext(), (datePicker, year, month, dayOfMonth) -> { //To show calendar dialog
                    startDayOfMonth=(byte)dayOfMonth;
                    startMonth=(byte)(month+1);//because here month value start from 0 so adding 1 to month
                    startYear=year;
                    startDate.setText(dayOfMonth+"-"+(month+1)+"-"+year);//month start from 0 so 1 is added to get right month like 12
                },startYear,startMonth-1,startDayOfMonth);//This variable should be ordered this variable will set date day month to calendar to datePickerDialog so passing it
                datePickerDialog.show();
            });

            endDate.setOnClickListener(view14 -> {
                DatePickerDialog datePickerDialog=new DatePickerDialog(getContext(), (datePicker, year, month, dayOfMonth) -> { //To show calendar dialog
                    endDayOfMonth=(byte)dayOfMonth;
                    endMonth=(byte)(month+1);//because here month value start from 0 so adding 1 to month
                    endYear=year;
                    endDate.setText(dayOfMonth+"-"+(month+1)+"-"+year);//month start from 0 so 1 is added to get right month like 12
                },endYear,endMonth-1,endDayOfMonth);//This variable should be ordered this variable will set date day month to calendar to datePickerDialog so passing it
                datePickerDialog.show();
            });

            sharePdf.setOnClickListener(view12 ->{

                String userStartDateDayOfMonthMonthYear=startDate.getText().toString().trim();
                String userEndDateDayOfMonthMonthYear=endDate.getText().toString().trim();
               if(userStartDateDayOfMonthMonthYear.isEmpty() || userEndDateDayOfMonthMonthYear.isEmpty()){
                    MyUtility.snackBar(view12, getResources().getString(R.string.enter_date));
                    return;
                }

               if(!isStartDateLessThenEndDate(userStartDateDayOfMonthMonthYear, userEndDateDayOfMonthMonthYear)){
                    MyUtility.snackBar(view12,getResources().getString(R.string.ensure_the_start_date_is_before_the_end_date));
                    return;
                }

               if(!userDateRangeChecker(userStartDateDayOfMonthMonthYear,userEndDateDayOfMonthMonthYear)){
                   MyUtility.snackBar(view12,getResources().getString(R.string.given_date_is_out_of_history_date_range));
                   return;
               }

               if(userStartDateDayOfMonthMonthYear.equals(userEndDateDayOfMonthMonthYear)){//if start and end date is equal then we would check that date data is present or not.if present do nothing else display message to user and return
                   if(!isDataPresentInHistoryTable(userStartDateDayOfMonthMonthYear)){
                       MyUtility.snackBar(view12,getResources().getString(R.string.history_not_available));
                       return;
                   }
               }

               String pdfAndReturnAbsolutePath=generateHistoryPdfAndReturnAbsolutePath(userStartDateDayOfMonthMonthYear,userEndDateDayOfMonthMonthYear);
               if(pdfAndReturnAbsolutePath !=null){

                   ExecutorService backgroundTask= Executors.newSingleThreadExecutor();
                   backgroundTask.execute(()->{
                    //background task
                     /*note:using whatsapp we cannot send pdf directly to whatsapp phone number like message for that we required approval so not using that feature*/
                     if(!MyUtility.shareFileToAnyApp(new File(pdfAndReturnAbsolutePath),"application/pdf",historyFileName(userStartDateDayOfMonthMonthYear,userEndDateDayOfMonthMonthYear),getContext())) {//open intent to share
                         MyUtility.snackBar(view12,getResources().getString(R.string.failed_to_create_pdf));//this will not create error even though we dont use runOnUiThread() method
                     }

                   });backgroundTask.shutdown();//when all task completed then only shutdown

               }else{
                   MyUtility.snackBar(view12,getResources().getString(R.string.failed_to_create_pdf));
               }

            });

            customDialog.show();
        });
        binding.historyRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                    totalItem1 = historyAdapter.getItemCount();// totalItem=manager.getItemCount();
                    scrollOutItems1 = layoutManager.findFirstVisibleItemPosition();

                    if (isScrolling1 && ((currentItem1 + scrollOutItems1) == totalItem1)) {
                        isScrolling1 = false;
                       // progressDialogHelper.showProgressBar();

                     fetchDataAccoprdingToIndicator(historyArraylist,totalNumberOfLoadedData,eachTimeDataToLoad);//dont send cursor to this function otherwise it will give error connection closed.because this function will create new thread and in new thread we have to create new connection


                        totalNumberOfLoadedData = totalNumberOfLoadedData + eachTimeDataToLoad;//eachTimeDataToLoad eg. value is 40 then data will be loaded and this variable represents total data already loaded
                        if (totalNumberOfLoadedData >= totalRecord) {//when all record loaded then remove scroll listener
                            //inactiveRecyclerView.clearOnScrollListeners();//this will remove scrollListener so we wont be able to scroll after loading all data and finished scrolling to last
                            loadOrNot = false;//alternative way to remove inactiveRecyclerView.clearOnScrollListeners()
                        }
                    }else{                    //29 > 23
                        if(totalNumberOfLoadedData >= totalRecord) {//when data is very less then totalNumberOfLoadedData it should not load and progress should not be visible
                            loadOrNot = false;
                           // progressDialogHelper.hideProgressBar();
                        }
                    }

                }
            }
        });

        String[] historyKeepingOptions = getResources().getStringArray(R.array.historyKeepingWeeks);
        ArrayAdapter<String> shareOptionsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, historyKeepingOptions);
        binding.historySettingSpinner.setAdapter(shareOptionsAdapter);
        binding.historySettingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                   String itemName = adapterView.getItemAtPosition(i).toString();
                   //dynamic code so not used switch case
                   if(itemName.equals(historyKeepingOptions[1])){
                       setValuesToSharedPreferences(defaultHistoryStoringForOneWeek, itemName, view);
                   } else if (itemName.equals(historyKeepingOptions[2])) {
                       setValuesToSharedPreferences(twoWeek, itemName, view);
                   } else if (itemName.equals(historyKeepingOptions[3])) {
                       setValuesToSharedPreferences(threeWeek, itemName, view);
                   }
                    //after selecting second time any option data is not shown so 0 is set so that when second time click it will show data
                    //int initialposition = holder.spinnerdescAudioIcon.getSelectedItemPosition();
                    binding.historySettingSpinner.setSelection(0, false);//clearing auto selected or if we remove this line then only one time we would be able to select
            }
            private void setValuesToSharedPreferences(byte historyKeepingDays, String itemName, View view) {
                SharedPreferencesHelper.setInt(getContext(), String.valueOf(SharedPreferencesHelper.Keys.HISTORY_KEEPING_DAYS), historyKeepingDays);
                MyUtility.snackBar(view, "HISTORY WILL BE STORED " + itemName);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return root;
    }
    private boolean isDataPresentInHistoryTable(String userStartDateDayOfMonthMonthYear){
      Database db=Database.getInstance(getContext());
      int[] date=convertStringDateToDayOfMonthMonthYear(userStartDateDayOfMonthMonthYear);
      return db.isDataOfDatePresentInHistoryTable(date[2],(byte) date[1], (byte) date[0]);
    }
    private void deleteHistoryPreviousData() {//delete the data which is not in between given date
        int[]  dateArray =getBeforeOrForwardDateYearMonthDayOfMonth((byte)-SharedPreferencesHelper.getInt(getContext(),String.valueOf(SharedPreferencesHelper.Keys.HISTORY_KEEPING_DAYS), defaultHistoryStoringForOneWeek),LocalDate.now().getYear(),(byte)LocalDate.now().getMonthValue(),(byte)LocalDate.now().getDayOfMonth());//START DATE is calculated using now date
        Database db=Database.getInstance(getContext());
        if(!db.deleteHistoryRecord(dateArray[0], (byte) dateArray[1], (byte) dateArray[2])){
            Toast.makeText(getContext(), "FAILED TO DELETE HISTORY", Toast.LENGTH_LONG).show();
        }
    }
    public void loadALLMixedHistoryDataInitially(int year, byte month, byte dayOfMonth, int loadDataInitially, ViewGroup container) {
        whichOneTwoFetch=1;
        totalNumberOfLoadedData=loadDataInitially;//act like limit
        loadOrNot=true;
        Database db=Database.getInstance(getContext());
        totalRecord =db.getRowsCountOfSpecificDateHistory(year,month,dayOfMonth);
       // Toast.makeText(getContext(), "total data: "+totalRecord, Toast.LENGTH_SHORT).show();
        historyArraylist = geHistoryDataAccordingToIndicator(year,month,dayOfMonth,0,totalNumberOfLoadedData, historyArraylist,container,whichOneTwoFetch);//updating inactive arraylist otherwise NPE don't know its referenced is passed but still not updated in method
        historyAdapter =new HistoryAdapter(getContext(), historyArraylist);//this common code should be there otherwise adapter will not be updated
        binding.historyRecyclerview.setAdapter(historyAdapter);
        layoutManager=new LinearLayoutManager( getContext(), LinearLayoutManager.VERTICAL, false);
        binding.historyRecyclerview.setLayoutManager(layoutManager);
        binding.historyRecyclerview.setHasFixedSize(true);//telling to recycler view that don't calculate item size every time when added and remove from recyclerview
    }
    public void loadTotalPaymentDataInitially(int year, byte month, byte dayOfMonth, int loadDataInitially, ViewGroup container) {
        binding.historyTotalPayment.setBackgroundResource(R.drawable.graycolor_bg);
        binding.historyTotalAmountReceived.setBackgroundResource(R.drawable.white_detailsbg);
        whichOneTwoFetch=2;
        totalNumberOfLoadedData=loadDataInitially;//act like limit
        loadOrNot=true;
        Database db=Database.getInstance(getContext());
        totalRecord = db.getRowsCountOfOnlyTotalPaymentHistory(year,month,dayOfMonth);
        Toast.makeText(getContext(), "total data: "+totalRecord, Toast.LENGTH_SHORT).show();
        historyArraylist = geHistoryDataAccordingToIndicator(year,month,dayOfMonth,0,totalNumberOfLoadedData, historyArraylist,container,whichOneTwoFetch);//updating inactive arraylist otherwise NPE don't know its referenced is passed but still not updated in method
        historyAdapter =new HistoryAdapter(getContext(), historyArraylist);//this common code should be there otherwise adapter will not be updated
        binding.historyRecyclerview.setAdapter(historyAdapter);
        layoutManager=new LinearLayoutManager( getContext(), LinearLayoutManager.VERTICAL, false);
        binding.historyRecyclerview.setLayoutManager(layoutManager);
        binding.historyRecyclerview.setHasFixedSize(true);//telling to recycler view that don't calculate item size every time when added and remove from recyclerview
    }
    public void loadTotalPaymentReceivedDataInitially(int year, byte month, byte dayOfMonth, int loadDataInitially, ViewGroup container) {
        binding.historyTotalAmountReceived.setBackgroundResource(R.drawable.graycolor_bg);
        binding.historyTotalPayment.setBackgroundResource(R.drawable.white_detailsbg);
        whichOneTwoFetch=3;
        totalNumberOfLoadedData=loadDataInitially;//act like limit
        loadOrNot=true;
        Database db=Database.getInstance(getContext());
        totalRecord = db.getRowsCountOfOnlyTotalPaymentReceivedHistory(year,month,dayOfMonth);
       // Toast.makeText(getContext(), "total data: "+totalRecord, Toast.LENGTH_SHORT).show();
        historyArraylist = geHistoryDataAccordingToIndicator(year,month,dayOfMonth,0,totalNumberOfLoadedData, historyArraylist,container,whichOneTwoFetch);//updating inactive arraylist otherwise NPE don't know its referenced is passed but still not updated in method
        historyAdapter =new HistoryAdapter(getContext(), historyArraylist);//this common code should be there otherwise adapter will not be updated
        binding.historyRecyclerview.setAdapter(historyAdapter);
        layoutManager=new LinearLayoutManager( getContext(), LinearLayoutManager.VERTICAL, false);
        binding.historyRecyclerview.setLayoutManager(layoutManager);
        binding.historyRecyclerview.setHasFixedSize(true);//telling to recycler view that don't calculate item size every time when added and remove from recyclerview
    }
    private ArrayList<HistoryModel> geHistoryDataAccordingToIndicator(int year, byte month, byte dayOfMonth, int skip, int fetch, ArrayList<HistoryModel> arraylist, ViewGroup container, byte whichOneTwoFetch) {
        Database db=Database.getInstance(getContext());
        Cursor cursor=null;
        if(whichOneTwoFetch==1){
            cursor = db.getALLDataHistoryForRecyclerView(year,month,dayOfMonth,skip,fetch);
       }else if (whichOneTwoFetch ==2) {
            cursor=db.getOnlyTotalPaymentHistoryForRecyclerView(year,month,dayOfMonth,skip,fetch);
       }else if (whichOneTwoFetch ==3) {
            cursor=db.getOnlyTotalReceivedPaymentHistoryForRecyclerView(year,month,dayOfMonth,skip,fetch);
       }
        arraylist =new ArrayList<>(150);//capacity is 150 because when arraylist size become greater then 100 then arraylist will be cleared.extra 50 is kept because we don't know arraylist size become greater then 100 is exactly how much

         if(cursor == null) return arraylist;//cursor may be null if error occur when fetching data from db so return empty arraylist

            while (cursor.moveToNext()) {
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
                model.setP1Skill(cursor.getString(10));
                model.setP2Skill(cursor.getString(11));
                model.setP3Skill(cursor.getString(12));
                model.setP4Skill(cursor.getString(13));
                model.setShared(cursor.getString(14) != null);
                model.setStatus(cursor.getString(15));
                model.setSubtractedAdvanceOrBal(cursor.getString(16));
                model.setName(cursor.getString(17));
                arraylist.add(model);
            }
            if(arraylist.size()==0){
                MyUtility.snackBar(container,getResources().getString(R.string.history_not_available));
            }
            arraylist.trimToSize();//to free space
            cursor.close();
            return arraylist;
    }
    private String historyKeepingRangeMessage() {
        int[] settingStartDateArray =getBeforeOrForwardDateYearMonthDayOfMonth((byte)-SharedPreferencesHelper.getInt(getContext(),String.valueOf(SharedPreferencesHelper.Keys.HISTORY_KEEPING_DAYS), defaultHistoryStoringForOneWeek),LocalDate.now().getYear(),(byte)LocalDate.now().getMonthValue(),(byte)LocalDate.now().getDayOfMonth());//START DATE is calculated using now date
        byte week=0;
        int days=SharedPreferencesHelper.getInt(getContext(),String.valueOf(SharedPreferencesHelper.Keys.HISTORY_KEEPING_DAYS), defaultHistoryStoringForOneWeek);
        week= (byte) ((days== defaultHistoryStoringForOneWeek)?1:(days==twoWeek)? 2: (days==threeWeek)? 3:0);
        StringBuilder sb=new StringBuilder(50).append( week+" WEEK HISTORY PRESENT FROM DATE  "+settingStartDateArray[2]+"-"+settingStartDateArray[1]+"-"+settingStartDateArray[0]+"  TO  "+LocalDate.now().getDayOfMonth()+"-"+LocalDate.now().getMonthValue()+"-"+LocalDate.now().getYear());//actually it store extra 1 day and it is important
        return sb.toString();
    }
    private boolean userDateRangeChecker(String userStartDateDayOfMonthMonthYear, String userEndDateDayOfMonthMonthYear) {
        int[] startDate =convertStringDateToDayOfMonthMonthYear(userStartDateDayOfMonthMonthYear);
        int[] endDate =convertStringDateToDayOfMonthMonthYear(userEndDateDayOfMonthMonthYear);

        LocalDate userStartDate=LocalDate.of(startDate[2],startDate[1],startDate[0]);//this is better for taking date instead of using date pattern like dd-mm--yy this give erro when 0 is not present
        LocalDate userEndDate=LocalDate.of(endDate[2],endDate[1],endDate[0]);

        int startDateArray[]= getBeforeOrForwardDateYearMonthDayOfMonth((byte)-SharedPreferencesHelper.getInt(getContext(),String.valueOf(SharedPreferencesHelper.Keys.HISTORY_KEEPING_DAYS), defaultHistoryStoringForOneWeek),LocalDate.now().getYear(),(byte)LocalDate.now().getMonthValue(),(byte)LocalDate.now().getDayOfMonth());//START DATE is calculated using now date

        LocalDate settingStartDate=LocalDate.of(startDateArray[0],startDateArray[1],startDateArray[2]);
        LocalDate settingEndDate=LocalDate.now();

        //checking user and setting date range
        return (userStartDate.isEqual(settingStartDate) || userStartDate.isAfter(settingStartDate))
                && (userEndDate.isEqual(settingEndDate) || userEndDate.isBefore(settingEndDate));
    }
    private String generateHistoryPdfAndReturnAbsolutePath(String startDate, String endDate) {//if error return null
        try{
            String fileAbsolutePath;
            MakePdf makePdf = new MakePdf();
            if(!makePdf.createPage1(450, MakePdf.defaultPageHeight, 1)) return null;//created page 1

            if(!fetchDateAndCreateHistoryPdf(startDate,endDate,makePdf)) return null;

            if(!makePdf.createdPageFinish2()) return null;

            fileAbsolutePath=makePdf.createFileToSavePdfDocumentAndReturnFile(getContext().getExternalFilesDir(null).toString(),historyFileName(startDate,endDate)).getAbsolutePath();

            if(!makePdf.closeDocumentLastOperation4())return null;

            return fileAbsolutePath;//fileNameAbsolutePath will be used to get file from device and if needed then convert to byteArray to store in db

        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
    private String historyFileName(String startDate, String endDate) {
        return "HISTORY "+startDate+" TO "+endDate;
    }
    private boolean fetchDateAndCreateHistoryPdf(String startDayOfMonthMonthYear, String endDayOfMonthMonthYear, MakePdf makePdf) {
        try(Database db = Database.getInstance(getContext())){
            int startDayOfMonthMonthYearArr[]=convertStringDateToDayOfMonthMonthYear(startDayOfMonthMonthYear);
            int endDayOfMonthMonthYearArr[]=convertStringDateToDayOfMonthMonthYear(endDayOfMonthMonthYear);

            LocalDate startDate =LocalDate.of(startDayOfMonthMonthYearArr[2],startDayOfMonthMonthYearArr[1],startDayOfMonthMonthYearArr[0]);
            LocalDate endDate   =LocalDate.of(endDayOfMonthMonthYearArr[2],endDayOfMonthMonthYearArr[1],endDayOfMonthMonthYearArr[0]).plusDays(1);//incremented 1 day to iterate from exactly start to end

            if (!makePdf.writeSentenceWithoutLines(new String[]{""}, new float[]{100f}, false, (byte) 0, (byte) 0,true)) return false;//just for space

            if (!makePdf.writeSentenceWithoutLines(getHistoryCreatedDetails(startDayOfMonthMonthYear,endDayOfMonthMonthYear), new float[]{60f,40f}, true, (byte) 0, (byte) 0,true))
                return false;

           // if (!makePdf.writeSentenceWithoutLines(new String[]{"History Instructions."} , new float[]{100f}, false, (byte) 0, (byte) 0,false)) return false;//just for space
            if (!makePdf.writeSentenceWithoutLines(new String[]{getResources().getString(R.string.history_details_are_in_sequential_order)} , new float[]{100f}, true, (byte) 0, (byte) 0,false)) return false;//just for space
            if (!makePdf.writeSentenceWithoutLines(new String[]{getResources().getString(R.string.if_status_is_colon)} , new float[]{100f}, true, (byte) 0, (byte) 0,false)) return false;//just for space
            if (!makePdf.writeSentenceWithoutLines(new String[]{getResources().getString(R.string.inserted_colon_user_has_inserted_data_on_same_day)} , new float[]{100f}, true, (byte) 0, (byte) 0,false)) return false;//just for space
            if (!makePdf.writeSentenceWithoutLines(new String[]{getResources().getString(R.string.inserted_plus_user_has_inserted_and_updated_data_on_same_day)} , new float[]{100f}, true, (byte) 0, (byte) 0,false)) return false;//just for space
            if (!makePdf.writeSentenceWithoutLines(new String[]{getResources().getString(R.string.updated_user_has_updated_previous_day_data)} , new float[]{100f}, true, (byte) 0, (byte) 0,false)) return false;//just for space
            if (!makePdf.writeSentenceWithoutLines(new String[]{getResources().getString(R.string.calculated_user_has_calculated_all_wages_and_data_is_inserted_automatically)} , new float[]{100f}, true, (byte) 0, (byte) 0,false)) return false;//just for space

            // Iterate over the range of dates
                LocalDate startingDate = startDate;
                while (startingDate.isBefore(endDate)){//The loop condition startingDate.isBefore(endDate) allows the loop body to execute as long as startingDate is before endDate.When startDate is equal to endDate-1, then loop stop but we want to execute from` startDate to equal endDate so thats why we have incremented 1 day to endDate to iterate from start to end date

                    String[][] historyOriginalData=db.getSpecificDataHistoryForPdf(startingDate.getYear(),(byte)startingDate.getMonthValue(),(byte)startingDate.getDayOfMonth());
                                                                                                                            //  //Color.rgb(221, 133, 3) yellow
                    if(!makePdf.singleCustomRow(getHeaderOfSpecificHistoryDate(startingDate,historyOriginalData),new float[]{100f},0,0,0,0,false,(byte)0,(byte)0)) return false;

                    if(!createSpecifiedDateHistoryPdf(makePdf,historyOriginalData)) return false;

                    if (!makePdf.writeSentenceWithoutLines(new String[]{""}, new float[]{100f}, false, (byte) 0, (byte) 0,true))
                        return false;//just for space

                    startingDate = startingDate.plusDays(1); // Move to the next date
                }
         return true;
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }
    }
    private String[] getHeaderOfSpecificHistoryDate(LocalDate startsDate,String[][] origionalHistoryData) {
        if(origionalHistoryData.length == 1 && origionalHistoryData[0].length == 1){//if no history then length would be 1 so return no history available
             return  new String[]{"HISTORY DATE: "+startsDate.getDayOfMonth()+"-"+startsDate.getMonthValue()+"-"+startsDate.getYear()+" , "+startsDate.getDayOfWeek().name()+" - "+origionalHistoryData[0][0]};
        }
        return  new String[]{"HISTORY DATE: "+startsDate.getDayOfMonth()+"-"+startsDate.getMonthValue()+"-"+startsDate.getYear()+" , "+startsDate.getDayOfWeek().name()+" -> "+getHistorySummary(startsDate)};
    }
    private String[] getHistoryCreatedDetails(String startDayOfMonthMonthYear, String endDayOfMonthMonthYear) {
        return new String[]{"HISTORY FROM DATE:  "+startDayOfMonthMonthYear+"  TO  "+endDayOfMonthMonthYear,"CREATED ON: "+MyUtility.get12hrCurrentTimeAndDate()};
    }
    private boolean createSpecifiedDateHistoryPdf(MakePdf makePdf, String origionalHistoryData[][]) {
        if(makePdf == null || origionalHistoryData==null) return false;

        if(origionalHistoryData.length == 1 && origionalHistoryData[0].length == 1){//if no history then return true because message no history available is already set in history header.so no need to create header
          // if(!makePdf.writeSentenceWithoutLines(new String[]{origionalHistoryData[0][0]},new float[]{100f}, true, (byte) 0, (byte) 0)) return false;
           return true;
        }

        String formattedHistoryData[][]=formateHistoryDataAccordingToColumn(origionalHistoryData);//formatted data-"STATUS","DATE","ID","NAME","WAGES","DEPOSIT","WORKING DAYS","REMARKS","UPDATED PAYMENT","UPDATED PAYMENT RECEIVED"
        if(formattedHistoryData==null) return false;

        if(!makePdf.makeTable(new String[]{"STATUS","DATE","ID","NAME","WAGES","DEPOSIT","WORKED DAYS","PAYMENT","RECEIVED"},new String[][]{{}},getColumnWidth(), 9, true)) {
            return false;
        }

        for (int i=0;i<formattedHistoryData.length;i++){//writing data

            String[] rowData = {
                    formattedHistoryData[i][0], formattedHistoryData[i][1], formattedHistoryData[i][2],
                    formattedHistoryData[i][3], (formattedHistoryData[i][4] !=null)?MyUtility.convertToIndianNumberSystem(Integer.parseInt(formattedHistoryData[i][4])) : null,
                    (formattedHistoryData[i][5] !=null)?MyUtility.convertToIndianNumberSystem(Integer.parseInt(formattedHistoryData[i][5])) : null,
                    formattedHistoryData[i][6], (formattedHistoryData[i][8] !=null)?MyUtility.convertToIndianNumberSystem(Math.abs(Integer.parseInt(formattedHistoryData[i][8]))) : null,
                    (formattedHistoryData[i][9] !=null)?MyUtility.convertToIndianNumberSystem(Integer.parseInt(formattedHistoryData[i][9])) : null
            };

                if(!makePdf.singleCustomRow(rowData,getColumnWidth(),0,0,0,0,true, (byte) 0, (byte) 0))return false;
                if(!makePdf.writeSentenceWithoutLines(new String[]{"REMARKS: "+formattedHistoryData[i][7]},new float[]{100f}, true, (byte) 0, (byte) 0,true)) return false;//FOR REMARKS
                if(!makePdf.writeSentenceWithoutLines(new String[]{""}, new float[]{100f}, true, (byte) 0, (byte) 0,true) )return false;//just for space
           }
//        if(!makePdf.writeSentenceWithoutLines(new String[]{getHistorySummary(date)}, new float[]{100f}, false, (byte) 0, (byte) 0,true) )return false;//summary
//        if(!makePdf.writeSentenceWithoutLines(new String[]{""}, new float[]{100f}, true, (byte) 0, (byte) 0,true) )return false;//just for space

        return true;
    }
    private String getHistorySummary(LocalDate date) {
        StringBuilder sb=new StringBuilder(200)
                .append(getResources().getString(R.string.total_payment)).append(" ").append(MyUtility.convertToIndianNumberSystem(getTotalPayment(date.getYear(), (byte) date.getMonthValue(), (byte) date.getDayOfMonth()))).append("   , ")
                .append(getResources().getString(R.string.total_amount_received)).append(" ").append(MyUtility.convertToIndianNumberSystem(getTotalReceivedPayment(date.getYear(), (byte) date.getMonthValue(), (byte) date.getDayOfMonth()))).append("   , ")
                .append(getTotalWorkedPeopleMessage(date.getYear(), (byte) date.getMonthValue(), (byte) date.getDayOfMonth()));
        return sb.toString();
    }
    private float[] getColumnWidth() {
        return new float[]{11f,10f,7f,28f,10f,6f,14f,7f,7f};
    }
    private String[][] formateHistoryDataAccordingToColumn(String[][] origionalData){//if error return null
        //column original Data received in this order-"STATUS","DATE","ID","NAME","2000","0","M","L","S3","S4","6",null,"P3","P4","REMARKS","100"
        try{
            String formattedData[][]=new String[origionalData.length][10];//10 cloumns

            for (int i = 0; i < origionalData.length; i++) {
                int formattedCol = 0;
                for (int j = 0; j <  origionalData[0].length; j++) {
                    String modifiedData = origionalData[i][j];

                     if(j==4 || (j>=7 && j<=13)) continue;//skip unnecessary column
                    if(j==0 && origionalData[i][j] != null){//process cloumn 0 STATUS

                        if(origionalData[i][j].equals(HistoryFragment.sameDayInserted)){
                            modifiedData=getResources().getString(R.string.inserted);
                        }else if (origionalData[i][j].equals(HistoryFragment.sameDayUpdated)) {
                            modifiedData=getResources().getString(R.string.inserted_plus);
                        } else if (origionalData[i][j].equals(HistoryFragment.previousRecordUpdated)) {
                            modifiedData=getResources().getString(R.string.updated);
                        }else if (origionalData[i][j].equals(HistoryFragment.automaticInserted)) {
                            modifiedData=getResources().getString(R.string.calculated);
                        }

                    }else if (j == 5 && origionalData[i][j] != null) {//process cloumn 5 ISDEPOSIT

                        if(origionalData[i][j].equals("0")){//at this index column 4 and 5 should be updated
                            formattedData[i][4] = origionalData[i][j-1];
                        }

                        if(origionalData[i][j].equals("1")){
                            formattedData[i][5] = origionalData[i][j-1];
                        }
                        formattedCol=6;//because column 4 & 5 is done formattedCol should be updated to 6
                        continue;
                    }else if (j == 6 && origionalData[i][j] != null) {//process cloumn 6 WORKING DAYS

                        StringBuilder sb=new StringBuilder();
                        sb.append(origionalData[i][j] + ":" + (origionalData[i][10] != null?origionalData[i][10]:0) + "  ");

                        if (origionalData[i][7] != null) {
                            sb.append(origionalData[i][7] + ":" + (origionalData[i][11] != null?origionalData[i][11]:0) + " ");

                            if (origionalData[i][8] != null) {
                                sb.append(origionalData[i][8] + ":" + (origionalData[i][12] != null?origionalData[i][12]:0) + " ");

                                if (origionalData[i][9] != null) {
                                    sb.append( origionalData[i][9] + ":" + (origionalData[i][13] != null?origionalData[i][13]:0) + " ");
                                }
                            }
                        }
                        modifiedData=sb.toString().trim();
                        //  j = 13;//no need to traverse index 6 to 13 .and j is set to 13 so that it start from 14 because j++ is there in for loop

                    }else if (j==15 && origionalData[i][j] !=null) {//process cloumn 15 subtracted amount

                        if(Integer.parseInt(origionalData[i][j]) <= 0){//means amount payment
                            formattedData[i][8] =  modifiedData;
                        }
                        if (Integer.parseInt(origionalData[i][j]) >= 0){//means payment received
                            formattedData[i][9] = modifiedData;
                        }
                        break;//due to last operation
                    }
                    formattedData[i][formattedCol++] = modifiedData;
                }
            }
            return formattedData;
        }catch (Exception x){
            x.printStackTrace();
            return null;
        }
    }
    private String getTotalWorkedPeopleMessage(int year, byte month, byte day){
        Database db=Database.getInstance(getContext());
        char M=getResources().getString(R.string.mestre).charAt(0),L=getResources().getString(R.string.laber).charAt(0),G=getResources().getString(R.string.women_laber).charAt(0);
        int sumM=0,sumL=0,sumG=0;

        HashMap<Character,Integer> skill1data=db.getTotalPeopleWorked(year,month,day,Database.COL_14_P1_SKILL_H,Database.COL_8_P1_H);
        if(skill1data == null) return "error in getting total worked people";

            if(skill1data.containsKey(G)){
                 sumG=sumG+skill1data.get(G);
            }
            if(skill1data.containsKey(L)){
                sumL=sumL+skill1data.get(L);
            }
            if(skill1data.containsKey(M)){
                sumM=sumM+skill1data.get(M);
            }

        HashMap<Character,Integer> skill2data=db.getTotalPeopleWorked(year,month,day,Database.COL_15_P2_SKILL_H,Database.COL_9_P2_H);
        if(skill2data == null) return "error in getting total worked people";
            if(skill2data.containsKey(G)){
                sumG=sumG+skill2data.get(G);
            }
            if(skill2data.containsKey(L)){
                sumL=sumL+skill2data.get(L);
            }
            if(skill2data.containsKey(M)){
                sumM=sumM+skill2data.get(M);
            }

        HashMap<Character,Integer> skill3data=db.getTotalPeopleWorked(year,month,day,Database.COL_16_P3_SKILL_H,Database.COL_10_P3_H);
        if(skill3data == null) return "error in getting total worked people";
            if(skill3data.containsKey(G)){
                sumG=sumG+skill3data.get(G);
            }
            if(skill3data.containsKey(L)){
                sumL=sumL+skill3data.get(L);
            }
            if(skill3data.containsKey(M)){
                sumM=sumM+skill3data.get(M);
            }

        HashMap<Character,Integer> skill4data=db.getTotalPeopleWorked(year,month,day,Database.COL_17_P4_SKILL_H,Database.COL_11_P4_H);
        if(skill4data == null) return "error in getting total worked people";
            if(skill4data.containsKey(G)){
                sumG=sumG+skill4data.get(G);
            }
            if(skill4data.containsKey(L)){
                sumL=sumL+skill4data.get(L);
            }
            if(skill4data.containsKey(M)){
                sumM=sumM+skill4data.get(M);
            }

         return new StringBuilder().append("TOTAL  ").append(sumM+sumL+sumG).append("  PEOPLE  WORKED").append("  ").append(M).append(": ").append(sumM).append("   ").append(L).append(": ").append(sumL).append("   ").append(G).append(": ").append(sumG).toString();
    }
    private boolean isStartDateLessThenEndDate(String startDayOfMonthMonthYear,String endDayOfMonthMonthYear) {
        if(startDayOfMonthMonthYear==null || endDayOfMonthMonthYear==null) return false;
        if(startDayOfMonthMonthYear.equals(endDayOfMonthMonthYear)) return true;

        int startDate[]=convertStringDateToDayOfMonthMonthYear(startDayOfMonthMonthYear);
        int endDate[]=convertStringDateToDayOfMonthMonthYear(endDayOfMonthMonthYear);

//        LocalDate startDate = LocalDate.parse(startDayOfMonthMonthYear, DateTimeFormatter.ofPattern("dd-MM-yyyy"));//if date is 1-1-2023 the exception due to 0 not there
//        LocalDate endDate = LocalDate.parse(endDayOfMonthMonthYear, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        LocalDate startDate1=LocalDate.of(startDate[2],startDate[1],startDate[0]);
        LocalDate endDate1=LocalDate.of(endDate[2],endDate[1],endDate[0]);
        return startDate1.isBefore(endDate1)?true:false;
    }
    private int[] convertStringDateToDayOfMonthMonthYear(String dayOfMonthMonthYear) {//return 31-2-2023
        String[] date = dayOfMonthMonthYear.split("-");
        return new int[]{
                Integer.parseInt(date[0]),//day
                Integer.parseInt(date[1]),//month
                Integer.parseInt(date[2])//year
        };
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
        int date[]= getBeforeOrForwardDateYearMonthDayOfMonth(days,this.year,this.month,this.dayOfMonth);
        if(date != null){//updating
            this.year = date[0];
            this.month = (byte) date[1];
            this.dayOfMonth = (byte) date[2];
            binding.historyDateViewTv.setText(getDateAndDayName(this.year,this.month,this.dayOfMonth));//month start from 0 so 1 is added to get right month like 12
        }else{
            binding.historyDateViewTv.setText("ERROR");
        }
    }
    String getDateAndDayName(int year, byte month, byte dayOfMonth){
        LocalDate currentDate = LocalDate.of(year,month,dayOfMonth);
        StringBuilder sb=new StringBuilder(25).append(dayOfMonth+"-"+(month)+"-"+year+" , "+currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()).toUpperCase());
        return sb.toString();
    }
    private void fetchDataAccoprdingToIndicator(ArrayList<HistoryModel> arraylist, int skip, int fetch) {
        new Handler().postDelayed(() -> {
            dataLoad(arraylist,skip,fetch);
            //progressDialogHelper.hideProgressBar();//after data loading progressbar disabled
        }, 1000);//wait for 3 seconds
    }
    private void dataLoad(ArrayList<HistoryModel> arraylist,int skip,int fetch){//dynamically
       //it is running another thread that y connection pool closed errro showing so get here new db connection pool
        Database db=Database.getInstance(getContext());
        Cursor dataCursor=null;
        if(whichOneTwoFetch==1) {//for showing all mixed data ie,received payment and payment given
            dataCursor = db.getALLDataHistoryForRecyclerView(year, month, dayOfMonth, skip, fetch);

        }else if (whichOneTwoFetch==2) {
              dataCursor = db.getOnlyTotalPaymentHistoryForRecyclerView(year,month,dayOfMonth,skip,fetch);

        }else if (whichOneTwoFetch ==3) {
            dataCursor=db.getOnlyTotalReceivedPaymentHistoryForRecyclerView(year,month,dayOfMonth,skip,fetch);
        }

        if(arraylist.size()>=100){//when arraylist size is greater then 100 then free space but ensure capacity will be as mention during declaration ie.150
            arraylist.clear();
        }
        while(dataCursor.moveToNext()){
            HistoryModel model = new HistoryModel();
            model.setId(dataCursor.getString(0));
            model.setUserDate(dataCursor.getString(1));
            model.setRemarks(dataCursor.getString(2));
            model.setWagesOrDeposit(dataCursor.getInt(3));
            model.setP1Work(dataCursor.getShort(4));
            model.setP2Work(dataCursor.getShort(5));
            model.setP3Work(dataCursor.getShort(6));
            model.setP4Work(dataCursor.getShort(7));
            model.setIsDeposit(dataCursor.getString(8).equals("1"));
            model.setSystemTimeDate(dataCursor.getString(9));
            model.setP1Skill( dataCursor.getString(10) );
            model.setP2Skill( dataCursor.getString(11) );
            model.setP3Skill(  dataCursor.getString(12) );
            model.setP4Skill( dataCursor.getString(13) );
            model.setShared( dataCursor.getString(14) != null);
            model.setStatus(dataCursor.getString(15));
            model.setSubtractedAdvanceOrBal(dataCursor.getString(16));
            model.setName(dataCursor.getString(17));
            arraylist.add(model);
        }
        historyAdapter.notifyDataSetChanged();//Use the notifyDataSetChanged() every time the list is updated,or inserted or deleted
        arraylist.trimToSize();//If the size of the ArrayList is increased, the ensureCapacity() method will not have any effect. The ensureCapacity() method is used to ensure that the ArrayList has enough room to store the specified number of elements. If the size of the ArrayList is increased, the ensureCapacity() method will not be triggered.
        dataCursor.close();
    }
    public int[] getBeforeOrForwardDateYearMonthDayOfMonth(byte forForwardDaysPlusAndPreviousDayMinus, int year, byte month, byte daysOfMonth){//if error return null
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
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(!MyUtility.deleteFolderAllFiles(PdfViewerOperationActivity.pdfFolderName,true,getContext())){//delete pdf folder all files
            Toast.makeText(getContext(), "FAILED TO DELETE FILE FROM DEVICE", Toast.LENGTH_LONG).show();
        }
        binding = null;
    }
}