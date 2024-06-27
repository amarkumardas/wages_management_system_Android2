package amar.das.acbook.activity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import amar.das.acbook.Database;
//import amar.das.acbook.GoogleDrive.DriveQuickstart;
import amar.das.acbook.R;
import amar.das.acbook.adapters.WagesDetailsAdapter;
import amar.das.acbook.customdialog.Dialog;
import amar.das.acbook.databinding.ActivityIndividualPersonDetailBinding;
import amar.das.acbook.globalenum.GlobalConstants;
import amar.das.acbook.model.WagesDetailsModel;
import amar.das.acbook.progressdialog.ProgressDialogHelper;
import amar.das.acbook.utility.BackupDataUtility;
import amar.das.acbook.voicerecording.VoiceRecorder;
import amar.das.acbook.utility.MyUtility;

public class IndividualPersonDetailActivity extends AppCompatActivity {
    ActivityIndividualPersonDetailBinding binding;
    MediaRecorder mediaRecorder;
    public static String audioPath;//it is made static so that in adapter class if user during updating data if audio is saved or not saved and user suddenly closed the app then on destroy method that audio should be deleted from device.so on destroy method code is there to delete that audio path form device
    public static android.app.AlertDialog adapterDialog;//made it static so that we can close adapter dialog in activity on destroy method
    boolean toggleToStartRecording=false;
    private String fromIntentPersonId;
    Database db;
    int cYear;
    byte cMonth,cDayOfMonth;
    int [] correctInputArray =new int[7];
    String active =GlobalConstants.INACTIVE_PEOPLE.getValue();
    byte redIndicatorToLeave=21;//if person will leave in 50 days so when 21 days 3 weeks left to leave then their name back ground color will change to red which indicate person is about to leave in 21 days so that wages can be given according to that
    ArrayList<WagesDetailsModel> dataList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0); //we have used overridePendingTransition(), it is used to remove activity create animation while re-creating activity.
        binding = ActivityIndividualPersonDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getIntent().hasExtra("ID")) {//every operation will be perform based on id
             db=Database.getInstance(this);
            fromIntentPersonId = getIntent().getStringExtra("ID");//getting data from intent

            //***********setting skill top of layout**********************************************
            Cursor defaultSkillCursor=db.getData("SELECT "+Database.COL_8_MAINSKILL1 +" FROM " + Database.PERSON_REGISTERED_TABLE + " WHERE "+Database.COL_1_ID+"= '" + fromIntentPersonId +"'");//for sure it will return type or skill
               defaultSkillCursor.moveToFirst();
               binding.defaultHardcodedTv.setText(defaultSkillCursor.getString(0));
               binding.defaultSkillTextTv.setText(defaultSkillCursor.getString(0) +"  =");//default calculation skill
               defaultSkillCursor.close();

             //Cursor sumData=db.getData("SELECT SUM("+Database.COL_26_WAGES+"),SUM("+Database.COL_28_P1+"),SUM("+Database.COL_29_P2+"),SUM("+Database.COL_291_P3+"),SUM("+Database.COL_292_P4+"),SUM("+Database.COL_27_DEPOSIT+") FROM "+Database.TABLE_NAME2+" WHERE "+Database.COL_21_ID+"= '"+fromIntentPersonId +"'");
             Integer[] sumData=db.getSumOfWagesP1P2P3P4Deposit(fromIntentPersonId);

             if(sumData[0] != null && sumData[0] < 0)//if total wages amount cross the  range of int the this message will be shown
                 Toast.makeText(this, getResources().getString(R.string.value_out_of_range_please_check_total_wages), Toast.LENGTH_LONG).show();

             binding.blueTotalWagesTv.setText(MyUtility.convertToIndianNumberSystem(sumData[0]));
             binding.blueTotalp1Tv.setText(sumData[1]+"");
             binding.totalP1CountTv.setText(sumData[1]+"");
                    //sum deposit
             if(sumData[5] != null && sumData[5] != 0) {//if there is deposit then set visibility visible or else layout visibility GONE
                 binding.totalDepositAmountTv.setText("= " + MyUtility.convertToIndianNumberSystem(sumData[5]));
             }else
                 binding.totalDepositAmountLayout.setVisibility(View.GONE);

            Cursor skillNRateCursor=db.getData("SELECT "+Database.COL_36_SKILL2 +","+Database.COL_37_SKILL3 +","+Database.COL_38_SKILL4 +","+Database.COL_32_R1+","+Database.COL_33_R2+","+Database.COL_34_R3+","+Database.COL_35_R4+" FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE "+Database.COL_31_ID+"= '" + fromIntentPersonId +"'");
            if(skillNRateCursor != null && skillNRateCursor.moveToFirst()) {

                int indicate = MyUtility.get_indicator(getBaseContext(),fromIntentPersonId);

                if(skillNRateCursor.getInt(3) != 0) {

                    binding.p1RateTv.setText(skillNRateCursor.getString(3));//default skill
                    //    R1 * p1
                    binding.totalP1AmountTv.setText("= "+MyUtility.convertToIndianNumberSystem(skillNRateCursor.getInt(3)*sumData[1]));//default skill
                }else {
                    binding.totalP1AmountTv.setText(getResources().getString(R.string.equal_new_person_provide_rate));//default skill
                }
                               //total wages
                if(sumData[0] !=null){//if total wages is not null then set total wages
                    binding.wagesTotalAmountTv.setText(MyUtility.convertToIndianNumberSystem(sumData[0]));//total wages set
                }
                   //by default= deposit,totalP2CountTv,defaultHardcodedTv,defaultSkillTextTv,p1RateTv,totalP1AmountTv is set automatically
                     if(indicate==1) {
                         indicator1234CalculateAndUpdate(sumData,skillNRateCursor.getInt(3) * sumData[1],0,0,0,indicate,fromIntentPersonId);
                     }

                binding.p2Layout.setVisibility(View.GONE);//initially invisible according to indicator it will customize
                binding.p3Layout.setVisibility(View.GONE);//initially invisible according to indicator it will customize
                binding.p4Layout.setVisibility(View.GONE);//initially invisible according to indicator it will customize

                if(indicate == 2) {
                    if(skillNRateCursor.getInt(4) != 0) {
                        //R1
                        binding.p2RateTv.setText(skillNRateCursor.getString(4));
                        //    R2 * p2
                        binding.totalP2AmountTv.setText("= "+MyUtility.convertToIndianNumberSystem(skillNRateCursor.getInt(4)*sumData[2]));
                    }else {
                        binding.totalP2AmountTv.setText(getResources().getString(R.string.equal_new_person_provide_rate));
                        //Toast.makeText(this, "Long press FINAL TOTAL button to  provide rate", Toast.LENGTH_LONG).show();
                    }

                    binding.totalP2CountTv.setText(sumData[2]+"");//total p2 count
                    binding.skill1TextTv.setText(skillNRateCursor.getString(0)+"  =");//setting skill 1
                    binding.hardcoded1Tv.setText(skillNRateCursor.getString(0));
                    binding.blueTotalp2Tv.setText(sumData[2]+"");
                    binding.p2Layout.setVisibility(View.VISIBLE);

                    indicator1234CalculateAndUpdate(sumData,skillNRateCursor.getInt(3) * sumData[1],skillNRateCursor.getInt(4) * sumData[2],0,0,indicate,fromIntentPersonId);

                } else if (indicate == 3) {
                    if(skillNRateCursor.getInt(4) != 0) {
                        binding.p2RateTv.setText(skillNRateCursor.getString(4));
                                                                                //    R2 * p2
                        binding.totalP2AmountTv.setText("= "+MyUtility.convertToIndianNumberSystem(skillNRateCursor.getInt(4)*sumData[2]));
                    }else {
                        binding.totalP2AmountTv.setText(getResources().getString(R.string.equal_new_person_provide_rate));
                     }

                    if(skillNRateCursor.getInt(5) != 0) {
                        binding.p3RateTv.setText(skillNRateCursor.getString(5));
                                                                                 //    R3 * p3
                        binding.totalP3AmountTv.setText("= "+MyUtility.convertToIndianNumberSystem(skillNRateCursor.getInt(5)*sumData[3]));
                    }else {
                        binding.totalP3AmountTv.setText(getResources().getString(R.string.equal_new_person_provide_rate));
                        //Toast.makeText(this, "Long press FINAL TOTAL button to  provide rate", Toast.LENGTH_LONG).show();
                    }
                    binding.totalP2CountTv.setText(sumData[2]+"");//total p2 count
                    binding.totalP3CountTv.setText(sumData[3]+"");//total p3 count
                    binding.skill1TextTv.setText(skillNRateCursor.getString(0)+"  =");//setting skill 1
                    binding.skill2TextTv.setText(skillNRateCursor.getString(1)+"  =");//setting skill 2
                    binding.hardcoded1Tv.setText(skillNRateCursor.getString(0));
                    binding.blueTotalp2Tv.setText(sumData[2]+"");
                    binding.hardcoded2Tv.setText(skillNRateCursor.getString(1));
                    binding.blueTotalp3Tv.setText(sumData[3]+"");
                    binding.p2Layout.setVisibility(View.VISIBLE);
                    binding.p3Layout.setVisibility(View.VISIBLE);

                    indicator1234CalculateAndUpdate(sumData,skillNRateCursor.getInt(3) * sumData[1],skillNRateCursor.getInt(4) * sumData[2],skillNRateCursor.getInt(5) * sumData[3],0,indicate,fromIntentPersonId);

                }else if(indicate == 4) {
                    if(skillNRateCursor.getInt(4) != 0) {
                        binding.p2RateTv.setText(skillNRateCursor.getString(4));
                        //    R2 * p2
                        binding.totalP2AmountTv.setText("= "+MyUtility.convertToIndianNumberSystem(skillNRateCursor.getInt(4)*sumData[2]));
                    }else{
                        binding.totalP2AmountTv.setText(getResources().getString(R.string.equal_new_person_provide_rate));
                     }

                    if(skillNRateCursor.getInt(5) != 0) {
                        binding.p3RateTv.setText(skillNRateCursor.getString(5));
                        //    R3 * p3
                        binding.totalP3AmountTv.setText("= "+MyUtility.convertToIndianNumberSystem(skillNRateCursor.getInt(5)*sumData[3]));
                    }else{
                        binding.totalP3AmountTv.setText(getResources().getString(R.string.equal_new_person_provide_rate));
                    }

                    if(skillNRateCursor.getInt(6) != 0) {
                        binding.p4RateTv.setText(skillNRateCursor.getString(6));
                        //    R4 * p4
                        binding.totalP4AmountTv.setText("= "+MyUtility.convertToIndianNumberSystem(skillNRateCursor.getInt(6)*sumData[4]));
                    }else{
                        binding.totalP4AmountTv.setText(getResources().getString(R.string.equal_new_person_provide_rate));
                    }

                    binding.totalP2CountTv.setText(sumData[2]+"");//total p2 count
                    binding.totalP3CountTv.setText(sumData[3]+"");//total p3 count
                    binding.totalP4CountTv.setText(sumData[4]+"");//total p4 count
                    binding.skill1TextTv.setText(skillNRateCursor.getString(0)+"  =");//setting skill 1
                    binding.skill2TextTv.setText(skillNRateCursor.getString(1)+"  =");//setting skill 2
                    binding.skill3TextTv.setText(skillNRateCursor.getString(2)+"  =");//setting skill 3
                    binding.hardcoded1Tv.setText(skillNRateCursor.getString(0));
                    binding.blueTotalp2Tv.setText(sumData[2]+"");
                    binding.hardcoded2Tv.setText(skillNRateCursor.getString(1));
                    binding.blueTotalp3Tv.setText(sumData[3]+"");
                    binding.hardcoded3Tv.setText(skillNRateCursor.getString(2));
                    binding.blueTotalp4Tv.setText(sumData[4]+"");
                    binding.p2Layout.setVisibility(View.VISIBLE);
                    binding.p3Layout.setVisibility(View.VISIBLE);
                    binding.p4Layout.setVisibility(View.VISIBLE);
                    indicator1234CalculateAndUpdate(sumData,skillNRateCursor.getInt(3) * sumData[1],skillNRateCursor.getInt(4) * sumData[2],skillNRateCursor.getInt(5) * sumData[3],skillNRateCursor.getInt(6) * sumData[4],indicate,fromIntentPersonId);
                }
            }
            if (skillNRateCursor != null) {
                skillNRateCursor.close();
            }

            //***********Done setting skill***********************************************
            //*******************Recycler view********************************************
             // Cursor allDataCursor=db.getData("SELECT "+Database.COL_22_DATE+","+Database.COL_24_MICPATH+","+Database.COL_25_DESCRIPTION+","+Database.COL_26_WAGES+","+Database.COL_27_DEPOSIT+","+Database.COL_28_P1+","+Database.COL_29_P2+","+Database.COL_291_P3+","+Database.COL_292_P4+","+Database.COL_21_ID+","+Database.COL_23_TIME+","+Database.COL_293_ISDEPOSITED+" FROM "+Database.TABLE_NAME2+" WHERE "+Database.COL_21_ID+"='"+fromIntentPersonId+"'");
             Cursor allDataCursor=db.getWagesDepositDataForRecyclerView(fromIntentPersonId);
              dataList=new ArrayList<>();
              if(allDataCursor!=null){
                  while (allDataCursor.moveToNext()) {
                      WagesDetailsModel model = new WagesDetailsModel();
                      model.setUserGivenDate(allDataCursor.getString(0));
                      model.setMicPath(allDataCursor.getString(1));
                      model.setRemarks(allDataCursor.getString(2));
                      model.setWagesOrDeposit(allDataCursor.getInt(3));
                      model.setP1(allDataCursor.getShort(4));
                      model.setP2(allDataCursor.getShort(5));
                      model.setP3(allDataCursor.getShort(6));
                      model.setP4(allDataCursor.getShort(7));
                      model.setId(allDataCursor.getString(8));
                      model.setIsdeposited((allDataCursor.getString(9).equals("1")));
                      model.setSystemDateAndTime(allDataCursor.getString(10));
                      dataList.add(model);
                  }
                  allDataCursor.close();
                  WagesDetailsAdapter wagesDetailsAdapter = new WagesDetailsAdapter(this, dataList);

                  binding.singleRecordRecy.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
                  binding.singleRecordRecy.setAdapter(wagesDetailsAdapter);
                  binding.singleRecordRecy.scrollToPosition(dataList.size() - 1);//this will scroll recycler view to last position automatically
              }else {
                  Toast.makeText(this, "NO DATA IN CURSOR", Toast.LENGTH_LONG).show();
              }
            //*******************done Recycler view********************************************

             if(!setStarAndLeavingDate()){
                Toast.makeText(this, "Exception occurred in fetching star and leaving date", Toast.LENGTH_LONG).show();
              }
             if(!setNameImageIdPhoneAadhaar()){
                 Toast.makeText(this, "Exception occurred in fetching person details", Toast.LENGTH_SHORT).show();
             }
            binding.starRatingTv.setOnClickListener(view ->{//it will work when only few days left to leave less than 21 days
                try(Cursor cursor2 = db.getData("SELECT "+Database.COL_392_LEAVINGDATE+" FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE "+Database.COL_31_ID+"='" + fromIntentPersonId + "'")){
                    cursor2.moveToFirst();
                    if (cursor2.getString(0) != null){

                            LocalDate dbDate, todayDate = LocalDate.now();//current date; return 2022-05-01
                            String[] dateArray = cursor2.getString(0).split("-");

                            dbDate = LocalDate.of(Integer.parseInt(dateArray[2]), Integer.parseInt(dateArray[1]), Integer.parseInt(dateArray[0]));//it convert 2022-05-01 it add 0 automatically

                            if(ChronoUnit.DAYS.between(todayDate, dbDate) <= redIndicatorToLeave){//if true then update text
                                MyUtility.snackBar(view,getString(R.string.person_will_leave_on_date_colon)+" "+cursor2.getString(0));
                             }
                    }
                }catch (Exception x){
                    x.printStackTrace();
                    Toast.makeText(this, "Exception occurred while fetching skill and leaving date", Toast.LENGTH_LONG).show();
                }
            });
            binding.advanceOrBalanceTv.setOnClickListener(view -> {
                if(!isAllRateSet(MyUtility.getRateArray(fromIntentPersonId,getBaseContext()),MyUtility.get_indicator(getBaseContext(),fromIntentPersonId))){//when there is no rate
                    Dialog dialog = new Dialog(this, fromIntentPersonId);
                    dialog.openUpdateRatesDialogSaveAndRefresh(true);
                }
            });
            binding.p1RateTv.setOnClickListener(view -> {
                Dialog dialog=new Dialog(this,fromIntentPersonId);
                dialog.openUpdateRatesDialogSaveAndRefresh(true);
            });
            binding.p2RateTv.setOnClickListener(view -> {
                Dialog dialog=new Dialog(this,fromIntentPersonId);
                dialog.openUpdateRatesDialogSaveAndRefresh(true);
            });
            binding.p3RateTv.setOnClickListener(view -> {
                Dialog dialog=new Dialog(this,fromIntentPersonId);
                dialog.openUpdateRatesDialogSaveAndRefresh(true);
            });
            binding.p4RateTv.setOnClickListener(view -> {
                Dialog dialog=new Dialog(this,fromIntentPersonId);
                dialog.openUpdateRatesDialogSaveAndRefresh(true);
            });
            //Meta data
            binding.infoTv.setOnClickListener(view ->{
                final boolean[] editOrNot = {false,false};
                AlertDialog.Builder myCustomDialog=new AlertDialog.Builder(IndividualPersonDetailActivity.this);
                LayoutInflater inflater=LayoutInflater.from(IndividualPersonDetailActivity.this);

                View myView=inflater.inflate(R.layout.person_meta_data,null);//myView contain all layout view ids
                myCustomDialog.setView(myView);//set custom layout to alert dialog
                myCustomDialog.setCancelable(false);//if user touch to other place then dialog will not be close

                final AlertDialog dialog=myCustomDialog.create();//myCustomDialog variable cannot be use in inner class so creating another final variable  to use in inner class
                HashSet<String> locationHashSet,religionHashSet;
                //ids
                RadioButton activeRadio=myView.findViewById(R.id.active_metadata);
                RadioGroup radioGroup=myView.findViewById(R.id.skill_radiogp_metadata);

                TextView hardcodedP1Tv=myView.findViewById(R.id.hardcoded_p1_tv_meta);//don't remove
                EditText inputP1Et=myView.findViewById(R.id.input_p1_et_meta);
                TextView hardcodedP2Tv=myView.findViewById(R.id.hardcoded_p2_tv_meta);
                EditText inputP2Et=myView.findViewById(R.id.input_p2_et_meta);
                TextView hardcodedP3Tv=myView.findViewById(R.id.hardcoded_p3_tv_meta);
                EditText inputP3Et=myView.findViewById(R.id.input_p3_et_meta);
                TextView hardcodedP4Tv=myView.findViewById(R.id.hardcoded_p4_tv_meta);
                EditText inputP4Et=myView.findViewById(R.id.input_p4_et_meta);
                TextView returningDate=myView.findViewById(R.id.returning_dateTv_metadata);
                TextView eraseReturningDate=myView.findViewById(R.id.eraseDate_tv_metadata);
                Spinner customSpinnerRemoveOrAddMlg=myView.findViewById(R.id.custom_spinner_remove_or_add_mlg);
                TextView totalWorkDaysMetadata=myView.findViewById(R.id.total_work_days_metadata);
                AutoCompleteTextView locationAutoComplete=myView.findViewById(R.id.location_autocomplete_tv_meta);
                AutoCompleteTextView religionAutoComplete=myView.findViewById(R.id.religion_autoComplte_tv_meta);

                Spinner starSpinner= myView.findViewById(R.id.starSpinner_metadata);
                TextView leavingDateTv=myView.findViewById(R.id.leaving_dateTv_metadata);
                EditText remarksMetaData=myView.findViewById(R.id.refferal_metadata);
                Button infoSave=myView.findViewById(R.id.save_btn_info);
                Button  cancel=myView.findViewById(R.id.cancel_btn_metadata);
                cancel.setOnClickListener(view12 -> dialog.dismiss());

                radioGroup.getChildAt(0).setEnabled(false);
                inputP1Et.setEnabled(false);
                inputP2Et.setEnabled(false);
                inputP3Et.setEnabled(false);
                inputP4Et.setEnabled(false);
                returningDate.setEnabled(false);
                customSpinnerRemoveOrAddMlg.setEnabled(false);
                locationAutoComplete.setEnabled(false);
                religionAutoComplete.setEnabled(false);
                starSpinner.setEnabled(false);
                leavingDateTv.setEnabled(false);
                eraseReturningDate.setEnabled(false);
                remarksMetaData.setEnabled(false);
                //-------------------------------------------------------------------------------------------------------------------------
                int indicator=MyUtility.get_indicator(getBaseContext(),fromIntentPersonId);
                setRateComponentAccordingToId(hardcodedP1Tv,inputP1Et,hardcodedP2Tv,inputP2Et,hardcodedP3Tv,inputP3Et,hardcodedP4Tv,inputP4Et,infoSave,new int[indicator],new int[indicator],indicator,fromIntentPersonId);
                //--------------------------------------------------------------------------------------------------------------------------
                Cursor cursor1 = db.getData("SELECT "+Database.COL_12_ACTIVE+" FROM " + Database.PERSON_REGISTERED_TABLE + " WHERE "+Database.COL_1_ID+"='" + fromIntentPersonId + "'");
                cursor1.moveToFirst();
                if(cursor1.getString(0).equals(GlobalConstants.ACTIVE_PEOPLE.getValue()))
                    activeRadio.setVisibility(View.GONE);//when it is active then don't show to activate
                else if(cursor1.getString(0).equals(GlobalConstants.INACTIVE_PEOPLE.getValue()))
                    activeRadio.setChecked(false);

                if(cursor1 != null){
                    cursor1.close();
                }
                //this should not be use in other class   other wise it will not be called when user change radio button
                radioGroup.setOnCheckedChangeListener((radioGroup1, checkedIdOfRadioBtn) -> {
                    if (checkedIdOfRadioBtn == R.id.active_metadata) {
                        active = GlobalConstants.ACTIVE_PEOPLE.getValue();//updating active variable
                    }
                });
                //-----------------------------------------to remove returning date automatically------returning date remove manually by admin so to call the worker untill they come---------------------------------------------------
                //should be done first before cursor21 query to get right result.updating returning date before to get FRESH result. if days is 0 between two date then update SET returning="+null+
//                cursor1 = db.getData("SELECT "+Database.COL_398_RETURNINGDATE+" FROM " + Database.TABLE_NAME3 + " WHERE "+Database.COL_31_ID+"='" + fromIntentPersonId + "'");
//                cursor1.moveToFirst();
//                LocalDate dbDate, todayDate = LocalDate.now();//current date; return 2022-05-01
//                String []dateArray;
//                if (cursor1.getString(0) != null) {
//                    dateArray = cursor1.getString(0).split("-");
//                    dbDate = LocalDate.of(Integer.parseInt(dateArray[2]), Integer.parseInt(dateArray[1]), Integer.parseInt(dateArray[0]));//it convert 2022-05-01 it add 0 automatically
//                    //between (2022-05-01,2022-05-01) like
//                    if (ChronoUnit.DAYS.between(dbDate, todayDate) >= 0) {//if days between returning date and today date is 0 then returning date will set null automatically
//                        db.updateTable("UPDATE " + Database.TABLE_NAME3 + " SET "+Database.COL_398_RETURNINGDATE+"=" + null + " WHERE "+Database.COL_31_ID+"='" + fromIntentPersonId + "'");
//                    }
//                }
//                cursor1.close();
                //---------------------------------------------------------------------------------------------------------------------------
                try(Cursor cursor21 = db.getData("SELECT "+Database.COL_391_STAR +","+Database.COL_392_LEAVINGDATE+","+Database.COL_393_PERSON_REMARKS +" , "+Database.COL_397_TOTAL_WORKED_DAYS+" , "+Database.COL_398_RETURNINGDATE+" FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE "+Database.COL_31_ID+"='" + fromIntentPersonId + "'")) {
                    cursor21.moveToFirst();

                        totalWorkDaysMetadata.setText(cursor21.getString(3));  //total worked days

                        String[] ratingStar = getResources().getStringArray(R.array.star);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(IndividualPersonDetailActivity.this, android.R.layout.simple_list_item_1, ratingStar);
                        starSpinner.setAdapter(adapter);
                        if (cursor21.getString(0) != null) {//rating star
                            int spinnerPosition = adapter.getPosition(cursor21.getString(0));
                            starSpinner.setSelection(spinnerPosition);
                        } else if (cursor21.getString(0) == null) {
                            int spinnerPosition = adapter.getPosition("1");//1 star by default
                            starSpinner.setSelection(spinnerPosition);
                        }

                        if (cursor21.getString(1) != null) {//leaving date
                            leavingDateTv.setText(cursor21.getString(1));
                        } else if (cursor21.getString(1) == null) {
                            leavingDateTv.setText("");
                        }
                        if (cursor21.getString(2) != null) {//remarksMetaData
                            remarksMetaData.setText(cursor21.getString(2));
                        }

                        if (cursor21.getString(4) != null) {//leaving date
                            returningDate.setText(cursor21.getString(4));
                        } else if (cursor21.getString(4) == null) {
                            returningDate.setText("");
                        }
                    }catch (Exception x){
                     x.printStackTrace();
                     Toast.makeText(this, "Exception occurred failed to fetch data", Toast.LENGTH_LONG).show();
                    }

                final Calendar current=Calendar.getInstance();//to get current date and time
                leavingDateTv.setOnClickListener(view13 -> {//leaving date
                    DatePickerDialog datePickerDialog=new DatePickerDialog(IndividualPersonDetailActivity.this, (datePicker, year, month, dayOfMonth) -> {
                        leavingDateTv.setText(dayOfMonth+"-"+(month+1)+"-"+year);//month start from 0 so 1 is added to get right month like 12
                    },current.get(Calendar.YEAR),current.get(Calendar.MONTH),current.get(Calendar.DAY_OF_MONTH));//This variable should be ordered this variable will set date day month to calendar to datePickerDialog so passing it
                    datePickerDialog.show();
                });

                returningDate.setOnClickListener(view16 ->{//returning date
                    DatePickerDialog datePickerDialog=new DatePickerDialog(IndividualPersonDetailActivity.this, (datePicker, year, month, dayOfMonth) -> {
                        returningDate.setText(dayOfMonth+"-"+(month+1)+"-"+year);//month start from 0 so 1 is added to get right month like 12
                    },current.get(Calendar.YEAR),current.get(Calendar.MONTH),current.get(Calendar.DAY_OF_MONTH));//This variable should be ordered this variable will set date day month to calendar to datePickerDialog so passing it
                    datePickerDialog.show();
                });

                eraseReturningDate.setOnClickListener(view17 -> {
                    returningDate.setText("");//set nothing empty
                });
                //------------------------------------------------------------------------------------
                try(Cursor locationAndReligionCursor=db.getData("SELECT "+Database.COL_17_LOCATION+","+Database.COL_18_RELIGION+" FROM "+Database.PERSON_REGISTERED_TABLE +" WHERE "+Database.COL_1_ID+"='"+fromIntentPersonId+"'")) {//to close cursor automatically
                    locationAndReligionCursor.moveToFirst();//because we get only 1 row
                    locationAutoComplete.setText((locationAndReligionCursor.getString(0)!=null?locationAndReligionCursor.getString(0):""));//set data
                    religionAutoComplete.setText((locationAndReligionCursor.getString(1)!=null?locationAndReligionCursor.getString(1):""));//set data
                }catch (Exception x){
                    x.printStackTrace();
                    Toast.makeText(this, "Exception occurred in fetching location and religion", Toast.LENGTH_LONG).show();
                }
                //location
                locationHashSet=new HashSet<>(Arrays.asList(MyUtility.getLocationFromDb(getBaseContext())));//hashset is taken to insert only unique data in table
                ArrayAdapter<String> locationAdapter=new ArrayAdapter<>(IndividualPersonDetailActivity.this, android.R.layout.simple_list_item_1, locationHashSet.toArray(new String[locationHashSet.size()]));
                locationAutoComplete.setAdapter(locationAdapter);
                //religion
                religionHashSet=new HashSet<>(Arrays.asList(MyUtility.getReligionFromDb(getBaseContext()))); //hashset is taken to insert only unique data in table
                ArrayAdapter<String> religionAdapter=new ArrayAdapter<>(IndividualPersonDetailActivity.this, android.R.layout.simple_list_item_1, religionHashSet.toArray(new String[religionHashSet.size()]));
                religionAutoComplete.setAdapter(religionAdapter);
                //-------------------------------------------------------------------------------------

                //****************************************************setting adapter for addOrRemoveMLG spinner*****************************************
                String[] addOrRemoveMLG = getResources().getStringArray(R.array.addOrRemoveMlG);
                ArrayAdapter<String> addOrRemoveMlGAdapter = new ArrayAdapter<>(IndividualPersonDetailActivity.this, android.R.layout.simple_list_item_1, addOrRemoveMLG);
                customSpinnerRemoveOrAddMlg.setAdapter(addOrRemoveMlGAdapter);
                // when activity is loaded spinner item is selected automatically so to avoid this we are using customSpinnerSetting.setSelection(initialPosition, false);
//            int initialposition = binding.customSpinnerSetting.getSelectedItemPosition();
//            binding.customSpinnerSetting.setSelection(initialposition, false);//clearing auto selected item
                customSpinnerRemoveOrAddMlg.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {//**Spinner OnItemSelectedListener event will execute twice:1.Spinner initializationUser 2.selected manually Try to differentiate those two by using flag variable.that's the reason boolean array is used
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                        String data = adapterView.getItemAtPosition(pos).toString();
                        try(Database db=new Database(getBaseContext());
                            Cursor cursorx =db.getData("SELECT "+Database.COL_36_SKILL2 +","+Database.COL_37_SKILL3 +","+Database.COL_38_SKILL4 +" FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE "+Database.COL_31_ID+"= '" + fromIntentPersonId +"'")){
                            cursorx.moveToFirst();//skill which is null there skill is updated
                            switch (data) {
                                case "ADD L": //adding L means p2
                                {
                                    editOrNot[1] = true;//indicate user has selected option

                                    if (cursorx.getString(0) == null) {
                                        showDialogAsMessage("UPDATE " + Database.TABLE_NAME_RATE_SKILL + " SET " + Database.COL_36_SKILL2 + "='" + getResources().getString(R.string.laber) + "' , " + Database.COL_39_INDICATOR + "=" + 2 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'", getResources().getString(R.string.successfully_added_l), getResources().getString(R.string.status_colon_success), getResources().getString(R.string.failed_to_add_l), getResources().getString(R.string.status_colon_failed));

                                    } else if (cursorx.getString(1) == null) {
                                        showDialogAsMessage("UPDATE " + Database.TABLE_NAME_RATE_SKILL + " SET " + Database.COL_37_SKILL3 + "='" + getResources().getString(R.string.laber) + "' , " + Database.COL_39_INDICATOR + "=" + 3 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'", getResources().getString(R.string.successfully_added_l), getResources().getString(R.string.status_colon_success), getResources().getString(R.string.failed_to_add_l), getResources().getString(R.string.status_colon_failed));

                                    } else if (cursorx.getString(2) == null) {
                                        showDialogAsMessage("UPDATE " + Database.TABLE_NAME_RATE_SKILL + " SET " + Database.COL_38_SKILL4 + "='" + getResources().getString(R.string.laber) + "' , " + Database.COL_39_INDICATOR + "=" + 4 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'", getResources().getString(R.string.successfully_added_l), getResources().getString(R.string.status_colon_success), getResources().getString(R.string.failed_to_add_l), getResources().getString(R.string.status_colon_failed));

                                    } else
                                        displayResultAndRefresh(getResources().getString(R.string.only_4_person_allowed_to_add), getResources().getString(R.string.status_cant_add_more) + getResources().getString(R.string.laber));
                                }
                                break;
                                case "ADD M": //adding M p3
                                {
                                    editOrNot[1] = true;//indicate user has selected option
                                    if (cursorx.getString(0) == null) {
                                        showDialogAsMessage("UPDATE " + Database.TABLE_NAME_RATE_SKILL + " SET " + Database.COL_36_SKILL2 + "='" + getResources().getString(R.string.mestre) + "' , " + Database.COL_39_INDICATOR + "=" + 2 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'", getResources().getString(R.string.successfully_added_m), getResources().getString(R.string.status_colon_success), getResources().getString(R.string.failed_to_add_m), getResources().getString(R.string.status_colon_failed));

                                    } else if (cursorx.getString(1) == null) {
                                        showDialogAsMessage("UPDATE " + Database.TABLE_NAME_RATE_SKILL + " SET " + Database.COL_37_SKILL3 + "='" + getResources().getString(R.string.mestre) + "' , " + Database.COL_39_INDICATOR + "=" + 3 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'", getResources().getString(R.string.successfully_added_m), getResources().getString(R.string.status_colon_success), getResources().getString(R.string.failed_to_add_m), getResources().getString(R.string.status_colon_failed));

                                    } else if (cursorx.getString(2) == null) {
                                        showDialogAsMessage("UPDATE " + Database.TABLE_NAME_RATE_SKILL + " SET " + Database.COL_38_SKILL4 + "='" + getResources().getString(R.string.mestre) + "' , " + Database.COL_39_INDICATOR + "=" + 4 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'", getResources().getString(R.string.successfully_added_m), getResources().getString(R.string.status_colon_success), getResources().getString(R.string.failed_to_add_m), getResources().getString(R.string.status_colon_failed));

                                    } else
                                        displayResultAndRefresh(getResources().getString(R.string.only_4_person_allowed_to_add), getResources().getString(R.string.status_cant_add_more) + getResources().getString(R.string.mestre));
                                }
                                break;
                                case "ADD G": //adding G p4
                                {
                                    editOrNot[1] = true;//indicate user has selected option

                                    if (cursorx.getString(0) == null) {
                                        showDialogAsMessage("UPDATE " + Database.TABLE_NAME_RATE_SKILL + " SET " + Database.COL_36_SKILL2 + "='" + getResources().getString(R.string.women_laber) + "' , " + Database.COL_39_INDICATOR + "=" + 2 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'", getResources().getString(R.string.successfully_added_g), getResources().getString(R.string.status_colon_success), getResources().getString(R.string.failed_to_add_g), getResources().getString(R.string.status_colon_failed));

                                    } else if (cursorx.getString(1) == null) {
                                        showDialogAsMessage("UPDATE " + Database.TABLE_NAME_RATE_SKILL + " SET " + Database.COL_37_SKILL3 + "='" + getResources().getString(R.string.women_laber) + "' , " + Database.COL_39_INDICATOR + "=" + 3 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'", getResources().getString(R.string.successfully_added_g), getResources().getString(R.string.status_colon_success), getResources().getString(R.string.failed_to_add_g), getResources().getString(R.string.status_colon_failed));

                                    } else if (cursorx.getString(2) == null) {
                                        showDialogAsMessage("UPDATE " + Database.TABLE_NAME_RATE_SKILL + " SET " + Database.COL_38_SKILL4 + "='" + getResources().getString(R.string.women_laber) + "' , " + Database.COL_39_INDICATOR + "=" + 4 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'", getResources().getString(R.string.successfully_added_g), getResources().getString(R.string.status_colon_success), getResources().getString(R.string.failed_to_add_g), getResources().getString(R.string.status_colon_failed));

                                    } else
                                        displayResultAndRefresh(getResources().getString(R.string.only_4_person_allowed_to_add), getResources().getString(R.string.status_cant_add_more) + getResources().getString(R.string.women_laber));
                                }
                                break;
                                case "REMOVE M/L/G": //removing
                                {
                                    try (Cursor cursorIndicator = db.getData("SELECT " + Database.COL_39_INDICATOR + " FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'")){//First getting indicator to decide whether delete or not.if indicator is null then cant delete because by default M or L or G present.If indicator is 2,3,4 then checking data is present or not if present then don't delete else delete
                                        editOrNot[1] = true;//indicate user has selected option
                                        if (cursorIndicator != null && cursorIndicator.moveToFirst()) {

                                            if (cursorIndicator.getString(0) == null) {//person1
                                                displayResultAndRefresh(getResources().getString(R.string.cant_remove_main_skill), getResources().getString(R.string.status_colon_failed));//default M or L or G

                                            } else if (cursorIndicator.getString(0).equals("2")) {//person2
                                                // Cursor result = db.getData("SELECT SUM(" + Database.COL_99_P2 + ") FROM " + Database.TABLE_NAME2 + " WHERE " + Database.COL_11_ID + "= '" + fromIntentPersonId + "'");
                                                Cursor result = db.getData("SELECT SUM(" + db.getColumnNameOutOf4Table(fromIntentPersonId, (byte) 7) + ") FROM " + db.tableNameOutOf4Table(fromIntentPersonId) + " WHERE " + db.getColumnNameOutOf4Table(fromIntentPersonId, (byte) 1) + "= '" + fromIntentPersonId + "'");
                                                result.moveToFirst();
                                                if (result.getInt(0) == 0) {//Means no data IN P2 so set null
                                                    if (db.updateTable("UPDATE " + Database.TABLE_NAME_RATE_SKILL + " SET " + Database.COL_36_SKILL2 + "= NULL , " + Database.COL_33_R2 + "= NULL  , " + Database.COL_39_INDICATOR + "=" + 1 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'")) {
                                                        displayResultAndRefresh(getResources().getString(R.string.no_data_present_so_removed), getResources().getString(R.string.status_colon_success));
                                                    } else {
                                                        Toast.makeText(IndividualPersonDetailActivity.this, getResources().getString(R.string.failed_to_update), Toast.LENGTH_LONG).show();
                                                    }
                                                } else if (result.getInt(0) >= 1) {
                                                    displayResultAndRefresh(getResources().getString(R.string.cant_remove), getResources().getString(R.string.because_data_is_present_newline_total_sum) + result.getInt(0));
                                                }
                                                if (result != null) result.close();

                                            } else if (cursorIndicator.getString(0).equals("3")) {//person3
                                                // Cursor result = db.getData("SELECT SUM(" + Database.COL_100_P3 + ") FROM " + Database.TABLE_NAME2 + " WHERE " + Database.COL_11_ID + "= '" + fromIntentPersonId + "'");
                                                Cursor result = db.getData("SELECT SUM(" + db.getColumnNameOutOf4Table(fromIntentPersonId, (byte) 8) + ") FROM " + db.tableNameOutOf4Table(fromIntentPersonId) + " WHERE " + db.getColumnNameOutOf4Table(fromIntentPersonId, (byte) 1) + "= '" + fromIntentPersonId + "'");
                                                result.moveToFirst();
                                                if (result.getInt(0) == 0) {//Means no data IN P2                                                                                          //decreasing indicator from 3 to 2
                                                    if (db.updateTable("UPDATE " + Database.TABLE_NAME_RATE_SKILL + " SET " + Database.COL_37_SKILL3 + "= NULL , " + Database.COL_34_R3 + "= NULL  , " + Database.COL_39_INDICATOR + "=" + 2 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'")) {
                                                        displayResultAndRefresh(getResources().getString(R.string.no_data_present_so_removed), getResources().getString(R.string.status_colon_success));
                                                    } else {
                                                        Toast.makeText(IndividualPersonDetailActivity.this, getResources().getString(R.string.failed_to_update), Toast.LENGTH_LONG).show();
                                                    }
                                                } else if (result.getInt(0) >= 1) {
                                                    displayResultAndRefresh(getResources().getString(R.string.cant_remove), getResources().getString(R.string.because_data_is_present_newline_total_sum) + result.getInt(0));
                                                }
                                                if (result != null) result.close();

                                            } else if (cursorIndicator.getString(0).equals("4")) {//person4
                                                // Cursor result = db.getData("SELECT SUM(" + Database.COL_1111_P4 + ") FROM " + Database.TABLE_NAME2 + " WHERE " + Database.COL_11_ID + "= '" + fromIntentPersonId + "'");
                                                Cursor result = db.getData("SELECT SUM(" + db.getColumnNameOutOf4Table(fromIntentPersonId, (byte) 9) + ") FROM " + db.tableNameOutOf4Table(fromIntentPersonId) + " WHERE " + db.getColumnNameOutOf4Table(fromIntentPersonId, (byte) 1) + "= '" + fromIntentPersonId + "'");
                                                result.moveToFirst();
                                                if (result.getInt(0) == 0) {//Means no data IN P2
                                                    if (db.updateTable("UPDATE " + Database.TABLE_NAME_RATE_SKILL + " SET " + Database.COL_38_SKILL4 + "= NULL , " + Database.COL_35_R4 + "= NULL , " + Database.COL_39_INDICATOR + "=" + 3 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'")) {
                                                        displayResultAndRefresh(getResources().getString(R.string.no_data_present_so_removed), getResources().getString(R.string.status_colon_success));
                                                    } else {
                                                        Toast.makeText(IndividualPersonDetailActivity.this, getResources().getString(R.string.failed_to_update), Toast.LENGTH_LONG).show();
                                                    }
                                                } else if (result.getInt(0) >= 1) {
                                                    displayResultAndRefresh(getResources().getString(R.string.cant_remove), getResources().getString(R.string.because_data_is_present_newline_total_sum) + result.getInt(0));
                                                }
                                                if (result != null) result.close();

                                            } else
                                                displayResultAndRefresh(getResources().getString(R.string.cant_remove_main_skill), getResources().getString(R.string.status_colon_failed));
                                        } else
                                            Toast.makeText(IndividualPersonDetailActivity.this, "NO DATA IN CURSOR", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                }
                            }
                        }catch (Exception x){
                            x.printStackTrace();
                            Toast.makeText(IndividualPersonDetailActivity.this, "Exception occurred", Toast.LENGTH_LONG).show();
                        }
                        if(editOrNot[1]) {
                            dialog.dismiss();//closing dialog to prevent window leak.whenever user select any option then editOrNot[1]=true; will be set.so if it is true then dismiss dialog before going to IndividualPersonDetailActivity.java from displayResult method
                        }
                     }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) { }
                });
                //****************************************************DONE setting adapter for addOrRemoveMLG spinner*****************************************
                infoSave.setOnClickListener(view1 ->{
                    if(editOrNot[0] ==false) {//while editing this will execute
                        radioGroup.getChildAt(0).setEnabled(true);
                        inputP1Et.setEnabled(true);
                        inputP2Et.setEnabled(true);
                        inputP3Et.setEnabled(true);
                        inputP4Et.setEnabled(true);
                        returningDate.setEnabled(true);
                        customSpinnerRemoveOrAddMlg.setEnabled(true);
                        locationAutoComplete.setEnabled(true);
                        religionAutoComplete.setEnabled(true);
                        starSpinner.setEnabled(true);
                        leavingDateTv.setEnabled(true);
                        eraseReturningDate.setEnabled(true);
                        remarksMetaData.setEnabled(true);
                        infoSave.setBackgroundResource(R.drawable.green_color_bg);//changing background
                        infoSave.setText(getResources().getString(R.string.save));
                        editOrNot[0] =true;
                    }else{//while saving this will execute
                        boolean updateRateSuccess,locationReligionSuccess;
                        String star;
                        if (active.equals(GlobalConstants.ACTIVE_PEOPLE.getValue())) {//if user has pressed radio button then only it will execute

                            if(!db.makeIdActive(fromIntentPersonId)){
                                Toast.makeText(IndividualPersonDetailActivity.this, "FAILED TO MAKE ID ACTIVE", Toast.LENGTH_LONG).show();
                            }
                        }
                        if (starSpinner.getSelectedItem().toString().equals("SELECT"))//if user BY mistake click on SELECT then by default start set to 1
                            star = "1";//default value
                        else
                            star = starSpinner.getSelectedItem().toString();

                        String leaveDate=null,returnDate=null;//taken this variable so that default value will be stored
                        int p1Rate=0,p2Rate=0,p3Rate=0,p4Rate=0;//taken this variable so that default value will be stored
                        if(!TextUtils.isEmpty(leavingDateTv.getText().toString().trim())){//TextUtils.isEmpty() checks for null or ""
                            leaveDate=leavingDateTv.getText().toString().trim();
                        }
                        if(!TextUtils.isEmpty(returningDate.getText().toString().trim())){//TextUtils.isEmpty() checks for null or ""
                            returnDate=returningDate.getText().toString().trim();
                        }
                        //rates--------------------------------
                        if(!TextUtils.isEmpty(inputP1Et.getText().toString().trim())){//TextUtils.isEmpty() checks for null or ""
                            p1Rate= Integer.parseInt(inputP1Et.getText().toString().trim());
                        }
                        if(!TextUtils.isEmpty(inputP2Et.getText().toString().trim())){//TextUtils.isEmpty() checks for null or ""
                            p2Rate= Integer.parseInt(inputP2Et.getText().toString().trim());
                        }
                        if(!TextUtils.isEmpty(inputP3Et.getText().toString().trim())){//TextUtils.isEmpty() checks for null or ""
                            p3Rate= Integer.parseInt(inputP3Et.getText().toString().trim());
                        }
                        if(!TextUtils.isEmpty(inputP4Et.getText().toString().trim())){//TextUtils.isEmpty() checks for null or ""
                            p4Rate= Integer.parseInt(inputP4Et.getText().toString().trim());
                        }

                        updateRateSuccess =db.update_Rating_TABLE_NAME3(star,(TextUtils.isEmpty(remarksMetaData.getText().toString().trim())? null : remarksMetaData.getText().toString().trim()),leaveDate,returnDate,(p1Rate!=0?String.valueOf(p1Rate):null),(p2Rate!=0?String.valueOf(p2Rate):null),(p3Rate!=0?String.valueOf(p3Rate):null),(p4Rate!=0?String.valueOf(p4Rate):null),fromIntentPersonId,indicator,false);

                        if(!MyUtility.updateLocationReligionToTableIfValueIsUnique(locationHashSet,locationAutoComplete.getText().toString().trim(),religionHashSet,religionAutoComplete.getText().toString().trim(),getBaseContext())){//UPDATING location and religion TO table
                            Toast.makeText(IndividualPersonDetailActivity.this, "NOT UPDATED", Toast.LENGTH_LONG).show();
                        }
                        locationReligionSuccess=db.updateTable("UPDATE " + Database.PERSON_REGISTERED_TABLE + " SET "+Database.COL_18_RELIGION+"='" + religionAutoComplete.getText().toString().trim() + "', "+Database.COL_17_LOCATION+"='"+ locationAutoComplete.getText().toString().trim() +"' WHERE "+Database.COL_1_ID+"='" + fromIntentPersonId + "'");

                        dialog.dismiss();//dismiss current dialog because new dialog will be open when display result()

                        if(updateRateSuccess || locationReligionSuccess){
                             refreshCurrentActivity(fromIntentPersonId);
//                            displayResult("SAVED SUCCESSFULLY",generateMessageAccordingToIndicator(star,leavingDateTv.getText().toString().trim(),returningDate.getText().toString().trim(),locationAutoComplete.getText().toString().trim(),
//                                    religionAutoComplete.getText().toString().trim(),remarksMetaData.getText().toString().trim(),indicator,hardcodedP1Tv.getText().toString().trim(),
//                                    p1Rate,hardcodedP2Tv.getText().toString().trim(),p2Rate,hardcodedP3Tv.getText().toString().trim(),p3Rate,hardcodedP4Tv.getText().toString().trim(),p4Rate));
                        }else{
                            displayResultAndRefresh("FAILED TO SAVE!!!", "DATA NOT UPDATED- UPDATE QUERY FAILED- PLEASE TRY AGAIN");
                        }
                    }
                });
                dialog.show();
            });
            binding.pdfShareTv.setOnClickListener(view -> {
                try {//to view pdf
                    finish();//while going to other activity so destroy this current activity(individualPersonDetailActivity) so that while coming back we will see refresh activity
                    Intent intent=new Intent(IndividualPersonDetailActivity.this, PdfViewerOperationActivity.class);
                    intent.putExtra("pdf1_or_2_or_3_for_blank_4",(byte)4);
                    intent.putExtra("ID",fromIntentPersonId);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplication().startActivity(intent);
                }catch(Exception e){
                    e.printStackTrace();
                }
            });
            binding.finalCalculationBtn.setOnClickListener(new View.OnClickListener() {
                TextView mainSkillTextTv,totalP1CountTv,workTotalAmountTv,totalP1AmountTv,advanceOrBalanceTv,totalDepositAmountTv,wagesTotalAmountTv,skill1TextTv,totalP2CountTv,totalP2AmountTv,skill2TextTv,totalP3CountTv,totalP3AmountTv,skill3TextTv,totalP4CountTv,totalP4AmountTv;
                LinearLayout p2Layout,p3Layout,p4Layout,totalDepositAmountLayout;
                EditText p1RateTv,p2RateTv,p3RateTv,p4RateTv;
                Button longPressToSaveAndCreatePdf,cancel;
                int []innerArray=new int[4];
                int totalDeposit=0,totalWages=0,p1=0,p2=0,p3=0,p4=0,r1=0,r2=0,r3=0,r4=0;//while saving this variable required
                byte indicate =MyUtility.get_indicator(getBaseContext(),fromIntentPersonId);
                @Override
                public void onClick(View view){
                    AlertDialog.Builder myCustomDialog=new AlertDialog.Builder(IndividualPersonDetailActivity.this);
                    LayoutInflater inflater=LayoutInflater.from(IndividualPersonDetailActivity.this);
                    View myView=inflater.inflate(R.layout.final_calculation_layout,null);//myView contain all layout view ids
                    myCustomDialog.setView(myView);//set custom layout to alert dialog
                    myCustomDialog.setCancelable(false);//if user touch to other place then dialog will not be close
                    final AlertDialog finalDialog=myCustomDialog.create();//myCustomDialog variable cannot be use in inner class so creating another final varialbe  to use in inner class
                    initialiseIDs(myView);//ids
                    cancel.setOnClickListener(view15 -> finalDialog.dismiss());

                    //Cursor defaultSkillCursor=db.getData("SELECT "+Database.COL_8_MAINSKILL1 +" FROM " + Database.TABLE_NAME1 + " WHERE "+Database.COL_1_ID+"= '" + fromIntentPersonId +"'");//for sure it will return type or skill
                    //defaultSkillCursor.moveToFirst();
                    //mainSkillTextTv.setText(defaultSkillCursor.getString(0)+" =");//default calculation skill
                    //mainSkillTextTv.setText(db.getOnlyMainSkill(fromIntentPersonId)+" =");//default calculation skill

                    String mainSkill = db.getOnlyMainSkill(fromIntentPersonId);
                    if (mainSkill != null) {
                        mainSkillTextTv.setText(mainSkill + " =");
                    }else{
                        mainSkillTextTv.setText("ERROR");
                    }

                   // defaultSkillCursor.close();
                    Integer[] sumArr=db.getSumOfWagesP1P2P3P4Deposit(fromIntentPersonId);

                    //initializing this variable to take during saving
                    p1=sumArr[1];
                    p2=sumArr[2];
                    p3=sumArr[3];
                    p4=sumArr[4];
                    totalWages=sumArr[0];

                    totalP1CountTv.setText(sumArr[1]+"");//default skill
                    //sum deposit
                    if(sumArr[5] != null && sumData[5] != 0) {//if there is deposit then set visibility visible or else layout visibility GONE
                         totalDepositAmountTv.setText("= " + MyUtility.convertToIndianNumberSystem(sumArr[5]));
                         totalDeposit=sumArr[5];//updating totalDeposit to take during save
                    }else {
                        totalDepositAmountLayout.setVisibility(View.GONE);
                    }

                    Cursor skillNRateCursor=db.getData("SELECT "+Database.COL_36_SKILL2 +","+Database.COL_37_SKILL3 +","+Database.COL_38_SKILL4 +","+Database.COL_32_R1+","+Database.COL_33_R2+","+Database.COL_34_R3+","+Database.COL_35_R4+" FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE "+Database.COL_31_ID+"= '" + fromIntentPersonId +"'");
                    if(skillNRateCursor != null) {
                        skillNRateCursor.moveToFirst();
                       //initializing this variables to take during saving
                        r1=skillNRateCursor.getInt(3);
                        r2=skillNRateCursor.getInt(4);
                        r3=skillNRateCursor.getInt(5);
                        r4=skillNRateCursor.getInt(6);

                        //if both wages and total work amount is less then 0 then both message have to show so if statement two times
                        if(sumArr[0] != null && sumArr[0] < 0 ) {//if total wages amount cross the  range of int the this message will be shown.its important
                            Toast.makeText(IndividualPersonDetailActivity.this,getResources().getString(R.string.value_out_of_range_please_check_total_wages), Toast.LENGTH_LONG).show();
                            longPressToSaveAndCreatePdf.setVisibility(View.GONE);
                        }
                        if ((totalDeposit + ((p1 * r1) + (p2 * r2) + (p3 * r3) + (p4 * r4))) < 0) {//user cant enter negative number so when (totalDeposit + (totalr1r2r3r4sum1sum2sum3sum4)) is negative that means int range is exceeds so wrong result will be shown
                            Toast.makeText(IndividualPersonDetailActivity.this, getResources().getString(R.string.value_out_of_range_please_check_total_work_amount), Toast.LENGTH_LONG).show();
                            longPressToSaveAndCreatePdf.setVisibility(View.GONE);//its important otherwise save option will be unable when user enter rate
                        }
                        //R1
                        if (skillNRateCursor.getInt(3) != 0) {
                            //R1
                             p1RateTv.setText(skillNRateCursor.getString(3));//default skill
                            //    R1 * p1
                            totalP1AmountTv.setText("= " + MyUtility.convertToIndianNumberSystem(skillNRateCursor.getInt(3) * sumArr[1]));//default skill
                        } else {
                            longPressToSaveAndCreatePdf.setVisibility(View.GONE);
                            totalP1AmountTv.setText(getResources().getString(R.string.equal_provide_rate));//default skill
                        }
                        //total wages
                        if (sumArr[0] != null) {//if total wages is not null then set total wages
                             wagesTotalAmountTv.setText(MyUtility.convertToIndianNumberSystem(sumArr[0]));//total wages set
                        }
                        //by default= deposit,totalP2CountTv,defaultHardcodedTv,defaultSkillTextTv,p1RateTv,totalP1AmountTv is set automatically
                        if (indicate == 1) {
                            indicator1234CalculateButDoNotUpdateToDBFinal(sumArr,skillNRateCursor.getInt(3) * sumArr[1],0,0,0);
                        }

                        p2Layout.setVisibility(View.GONE);//initially invisible according to indicator it will customize
                        p3Layout.setVisibility(View.GONE);//initially invisible according to indicator it will customize
                        p4Layout.setVisibility(View.GONE);//initially invisible according to indicator it will customize

                        if(indicate == 2) {
                            if(skillNRateCursor.getInt(4) != 0) {
                                //R1
                                p2RateTv.setText(skillNRateCursor.getString(4));
                                //    R2 * p2
                                totalP2AmountTv.setText("= "+MyUtility.convertToIndianNumberSystem(skillNRateCursor.getInt(4)*sumArr[2]));
                            }else {
                                longPressToSaveAndCreatePdf.setVisibility(View.GONE);
                                totalP2AmountTv.setText(getResources().getString(R.string.equal_provide_rate));
                            }

                             totalP2CountTv.setText(sumArr[2]+"");//total p2 count
                             skill1TextTv.setText(skillNRateCursor.getString(0)+" =");//setting skill 1
                             p2Layout.setVisibility(View.VISIBLE);
                             indicator1234CalculateButDoNotUpdateToDBFinal(sumArr,skillNRateCursor.getInt(3) * sumArr[1],skillNRateCursor.getInt(4) * sumArr[2],0,0);
                        } else if (indicate == 3) {
                            if(skillNRateCursor.getInt(4) != 0) {
                                 p2RateTv.setText(skillNRateCursor.getString(4));
                                //    R2 * p2
                                 totalP2AmountTv.setText("= "+MyUtility.convertToIndianNumberSystem(skillNRateCursor.getInt(4)*sumArr[2]));
                            }else {
                                longPressToSaveAndCreatePdf.setVisibility(View.GONE);
                                totalP2AmountTv.setText(getResources().getString(R.string.equal_provide_rate));
                            }
                            if(skillNRateCursor.getInt(5) != 0) {
                                 p3RateTv.setText(skillNRateCursor.getString(5));
                                //    R3 * p3
                                totalP3AmountTv.setText("= "+MyUtility.convertToIndianNumberSystem(skillNRateCursor.getInt(5)*sumArr[3]));
                            }else {
                                longPressToSaveAndCreatePdf.setVisibility(View.GONE);
                                totalP3AmountTv.setText(getResources().getString(R.string.equal_provide_rate));
                            }
                             totalP2CountTv.setText(sumArr[2]+"");//total p2 count
                             totalP3CountTv.setText(sumArr[3]+"");//total p3 count
                             skill1TextTv.setText(skillNRateCursor.getString(0)+" =");//setting skill 1
                             skill2TextTv.setText(skillNRateCursor.getString(1)+" =");//setting skill 2
                             p2Layout.setVisibility(View.VISIBLE);
                             p3Layout.setVisibility(View.VISIBLE);
                            indicator1234CalculateButDoNotUpdateToDBFinal(sumArr,skillNRateCursor.getInt(3) * sumArr[1],skillNRateCursor.getInt(4) * sumArr[2],skillNRateCursor.getInt(5) * sumArr[3],0);

                        } else if (indicate == 4) {
                            if(skillNRateCursor.getInt(4) != 0) {
                                 p2RateTv.setText(skillNRateCursor.getString(4));
                                //    R2 * p2
                                 totalP2AmountTv.setText("= "+MyUtility.convertToIndianNumberSystem(skillNRateCursor.getInt(4)*sumArr[2]));
                            }else {
                                longPressToSaveAndCreatePdf.setVisibility(View.GONE);
                                totalP2AmountTv.setText(getResources().getString(R.string.equal_provide_rate));
                            }

                            if(skillNRateCursor.getInt(5) != 0) {
                                p3RateTv.setText(skillNRateCursor.getString(5));
                                //    R3 * p3
                                 totalP3AmountTv.setText("= "+MyUtility.convertToIndianNumberSystem(skillNRateCursor.getInt(5)*sumArr[3]));
                            }else {
                                longPressToSaveAndCreatePdf.setVisibility(View.GONE);
                                totalP3AmountTv.setText(getResources().getString(R.string.equal_provide_rate));
                            }

                            if(skillNRateCursor.getInt(6) != 0) {
                                 p4RateTv.setText(skillNRateCursor.getString(6));
                                //    R4 * p4
                                 totalP4AmountTv.setText("= "+MyUtility.convertToIndianNumberSystem(skillNRateCursor.getInt(6)*sumArr[4]));
                            }else {
                                longPressToSaveAndCreatePdf.setVisibility(View.GONE);
                                totalP4AmountTv.setText(getResources().getString(R.string.equal_provide_rate));
                            }
                             totalP2CountTv.setText(sumArr[2]+"");//total p2 count
                             totalP3CountTv.setText(sumArr[3]+"");//total p3 count
                             totalP4CountTv.setText(sumArr[4]+"");//total p4 count
                             skill1TextTv.setText(skillNRateCursor.getString(0)+" =");//setting skill 1
                             skill2TextTv.setText(skillNRateCursor.getString(1)+" =");//setting skill 2
                             skill3TextTv.setText(skillNRateCursor.getString(2)+" =");//setting skill 3
                             p2Layout.setVisibility(View.VISIBLE);
                             p3Layout.setVisibility(View.VISIBLE);
                             p4Layout.setVisibility(View.VISIBLE);
                            indicator1234CalculateButDoNotUpdateToDBFinal(sumArr,skillNRateCursor.getInt(3) * sumArr[1],skillNRateCursor.getInt(4) * sumArr[2],skillNRateCursor.getInt(5) * sumArr[3],skillNRateCursor.getInt(6) * sumArr[4]);
                        }
                    }
                    //if rate 1 is changed
                    p1RateTv.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                         }
                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            String p11 =p1RateTv.getText().toString().trim();
                            p1RateTv.setTextColor(getColor(R.color.purple_700));
                            innerArray[0]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data
                            //this will check if other data is right or wrong
                            if(!MyUtility.isEnterDataIsWrong(innerArray)) {//this is important if in field data is wrong then save button will not enabled until data is right.
                                longPressToSaveAndCreatePdf.setVisibility(View.VISIBLE);
                            }
                            if (!p11.matches("[0-9]+")) {//space or , or - is restricted"[.]?[0-9]+[.]?[0-9]*"
                                p1RateTv.setTextColor(Color.RED);
                                longPressToSaveAndCreatePdf.setVisibility(View.GONE);
                                innerArray[0]=2;//means data is inserted wrong
                              //  Toast.makeText(IndividualPersonDetailActivity.this, "NOT ALLOWED(space  .  ,  -)\nPLEASE CORRECT", Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void afterTextChanged(Editable editable) {
                            try{
                                r1 = Integer.parseInt(p1RateTv.getText().toString().trim());//updating r1 variable to take during saving
                                totalP1AmountTv.setText("= " + MyUtility.convertToIndianNumberSystem(r1 * p1));
                                updateTotalWorkAmountAndAdvanceOrBalanceTv();

                                if(MyUtility.isp1p2p3p4PresentAndRateNotPresent(r1,r2,r3,r4,p1,p2,p3,p4,indicate)){
                                    longPressToSaveAndCreatePdf.setVisibility(View.GONE);
                                    advanceOrBalanceTv.setText("");
                                    workTotalAmountTv.setText(" - 0");
                                }

                            }catch(Exception e){
                                totalP1AmountTv.setText("= 0");
                                advanceOrBalanceTv.setText("");
                                workTotalAmountTv.setText(" - 0");
                            }
                        }
                    });
                    //if   rate 2 is changed
                    p2RateTv.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }
                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            String p11 =p2RateTv.getText().toString().trim();
                            p2RateTv.setTextColor(getColor(R.color.purple_700));
                            innerArray[1]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data
                            //this will check if other data is right or wrong
                            if(!MyUtility.isEnterDataIsWrong(innerArray)) {//this is important if in field data is wrong then save button will not enabled until data is right.
                                longPressToSaveAndCreatePdf.setVisibility(View.VISIBLE);

                            }
                            if (!p11.matches("[0-9]+")) {//space or , or - is restricted"[.]?[0-9]+[.]?[0-9]*"
                                p2RateTv.setTextColor(Color.RED);
                                longPressToSaveAndCreatePdf.setVisibility(View.GONE);
                                innerArray[1]=2;//means data is inserted wrong
                               // Toast.makeText(IndividualPersonDetailActivity.this, "NOT ALLOWED(space  .  ,  -)\nPLEASE CORRECT", Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void afterTextChanged(Editable editable) {
                            try{
                                r2 = Integer.parseInt(p2RateTv.getText().toString().trim());//updating r1 variable to take during saving
                                totalP2AmountTv.setText("= " + MyUtility.convertToIndianNumberSystem(r2 * p2));
                                updateTotalWorkAmountAndAdvanceOrBalanceTv();

                                if(MyUtility.isp1p2p3p4PresentAndRateNotPresent(r1,r2,r3,r4,p1,p2,p3,p4,indicate)){
                                    longPressToSaveAndCreatePdf.setVisibility(View.GONE);
                                    advanceOrBalanceTv.setText("");
                                    workTotalAmountTv.setText(" - 0");
                                }
                            }catch(Exception e){
                                totalP2AmountTv.setText("= 0");
                                advanceOrBalanceTv.setText("");
                                workTotalAmountTv.setText(" - 0");
                            }
                        }
                    });
                    //if   rate 3 is changed
                    p3RateTv.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }
                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            String p11 =p3RateTv.getText().toString().trim();
                            p3RateTv.setTextColor(getColor(R.color.purple_700));
                            innerArray[2]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data
                            //this will check if other data is right or wrong
                            if(!MyUtility.isEnterDataIsWrong(innerArray)) {//this is important if in field data is wrong then save button will not enabled until data is right.
                                longPressToSaveAndCreatePdf.setVisibility(View.VISIBLE);
                            }
                            if (!p11.matches("[0-9]+")) {//space or , or - is restricted"[.]?[0-9]+[.]?[0-9]*"
                                p3RateTv.setTextColor(Color.RED);
                                longPressToSaveAndCreatePdf.setVisibility(View.GONE);
                                innerArray[2]=2;//means data is inserted wrong
                                //Toast.makeText(IndividualPersonDetailActivity.this, "NOT ALLOWED(space  .  ,  -)\nPLEASE CORRECT", Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void afterTextChanged(Editable editable) {
                            try {
                                r3 = Integer.parseInt(p3RateTv.getText().toString().trim());//updating r1 variable to take during saving
                                totalP3AmountTv.setText("= " + MyUtility.convertToIndianNumberSystem(r3 * p3));
                                updateTotalWorkAmountAndAdvanceOrBalanceTv();

                                if(MyUtility.isp1p2p3p4PresentAndRateNotPresent(r1,r2,r3,r4,p1,p2,p3,p4,indicate)){
                                    longPressToSaveAndCreatePdf.setVisibility(View.GONE);
                                    advanceOrBalanceTv.setText("");
                                    workTotalAmountTv.setText(" - 0");
                                }
                            }catch (Exception e){
                                totalP3AmountTv.setText("= 0");
                                advanceOrBalanceTv.setText("");
                                workTotalAmountTv.setText(" - 0");
                            }
                        }
                    });
                    //if   rate 4 is changed
                    p4RateTv.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        }
                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            String p11 =p4RateTv.getText().toString().trim();
                            p4RateTv.setTextColor(getColor(R.color.purple_700));
                            innerArray[3]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data
                            //this will check if other data is right or wrong
                            if(!MyUtility.isEnterDataIsWrong(innerArray)) {//this is important if in field data is wrong then save button will not enabled until data is right.
                                longPressToSaveAndCreatePdf.setVisibility(View.VISIBLE);
                            }
                            if (!p11.matches("[0-9]+")) {//space or , or - is restricted"[.]?[0-9]+[.]?[0-9]*"
                                p4RateTv.setTextColor(Color.RED);
                                longPressToSaveAndCreatePdf.setVisibility(View.GONE);
                                innerArray[3]=2;//means data is inserted wrong
                            }
                        }
                        @Override
                        public void afterTextChanged(Editable editable) {
                            try {
                                r4 = Integer.parseInt(p4RateTv.getText().toString().trim());//updating r1 variable to take during saving
                                totalP4AmountTv.setText("= " + MyUtility.convertToIndianNumberSystem(r4 * p4));
                                updateTotalWorkAmountAndAdvanceOrBalanceTv();

                                if(MyUtility.isp1p2p3p4PresentAndRateNotPresent(r1,r2,r3,r4,p1,p2,p3,p4,indicate)){
                                    longPressToSaveAndCreatePdf.setVisibility(View.GONE);
                                    advanceOrBalanceTv.setText("");
                                    workTotalAmountTv.setText(" - 0");
                                }
                            }catch(Exception e){
                                totalP4AmountTv.setText("= 0");
                                advanceOrBalanceTv.setText("");
                                workTotalAmountTv.setText(" - 0");
                            }
                        }
                    });
                    if (skillNRateCursor != null) skillNRateCursor.close();

                    longPressToSaveAndCreatePdf.setOnLongClickListener(view14 -> {
                        longPressToSaveAndCreatePdf.setVisibility(View.GONE);//to avoid when user click button multiple times

                        if(!((BackupDataUtility.checkDeviceInternalStorageAvailabilityInMB(getBaseContext())) >= 30)) {//(checkInternalStorageAvailability()*1000) converted to MB so if it is greater or equal to 50 MB then true
                         Toast.makeText(IndividualPersonDetailActivity.this, "LESS STORAGE SPACE TO CREATE PDF", Toast.LENGTH_LONG).show();
                         return false;
                        }

                         if(!MyUtility.checkPermissionForReadAndWriteToExternalStorage(getBaseContext())) {//Take permission
                           Toast.makeText(IndividualPersonDetailActivity.this, "READ,WRITE EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show();
                             //to read and write own app specific directory from minsdk 29 to 33+ we don't require READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE due to scope storage after android 10
                            // ActivityCompat.requestPermissions(IndividualPersonDetailActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 20);
                           return false;
                         }

                         ProgressDialogHelper progressBar = new ProgressDialogHelper(IndividualPersonDetailActivity.this);
                         ExecutorService backgroundTask = Executors.newSingleThreadExecutor(); // Create an ExecutorService
                         backgroundTask.execute(() -> {
                                                       //on pre execute
                          runOnUiThread(() -> {//first close the dialog then showing progress bar on main thread
                          if (finalDialog != null && finalDialog.isShowing()) {//dismiss dialog before going to pdf-viewer activity
                           finalDialog.dismiss();
                           }
                           progressBar.showProgressBar();
                           });
                         boolean success=dataBaseDeleteAndCreatePdfOperation();//background task

                          //on post execute method.will execute after all operation completed
                         runOnUiThread(() ->{ progressBar.hideProgressBar();
                                               if(success){
                                                   if(!viewPDFFromDb((byte) 2, fromIntentPersonId)) {//column name should be correct Viewing pdf2
                                                       Toast.makeText(IndividualPersonDetailActivity.this, "FAILED TO VIEW PDF", Toast.LENGTH_LONG).show();
                                                      }
                                               }else{viewPDFFromDb((byte) 4, fromIntentPersonId);//view blank pdf 4
                                                    Toast.makeText(IndividualPersonDetailActivity.this, "FAILED TO CREATE PDF", Toast.LENGTH_LONG).show();
                                               }
                            });

                        });backgroundTask.shutdown();//it will shutdown when all task is completed.The backgroundTask.shutdown() method is executed when you want to stop the ExecutorService. This can be useful for a number of reasons, such as when you are finished using the ExecutorService or when you want to conserve resources.When you call the backgroundTask.shutdown() method, the ExecutorService will stop accepting new tasks. However, any tasks that are already in progress will continue to run until they are finished.Once all of the tasks in the ExecutorService have finished running, the ExecutorService will be shutdown. This means that all of the threads in the pool will be cleaned up and the ExecutorService will no longer be usable.It is important to note that the backgroundTask.shutdown() method will not block your app. This means that you can continue to use your app after calling the backgroundTask.shutdown() method.

                   return false;
                    });

                    //this code should  be here because ontextchange listerner is called before this code.so if condition is true it will override that value
                    if(!isAllRateSet(MyUtility.getRateArray(fromIntentPersonId,getBaseContext()),indicate)){//when there is no rate
                        advanceOrBalanceTv.setTextColor(getColor(R.color.purple_700));
                        advanceOrBalanceTv.setText("="+getString(R.string.set_rate));//when there is no rate
                    }
                    finalDialog.show();
                }
                private boolean dataBaseDeleteAndCreatePdfOperation() {
                    try {
                        Database dB = Database.getInstance(getBaseContext());
                        if (!dB.deleteAudioFirstThenWagesAndDepositThenAddFinalMessageThenUpdatePdfSequence(fromIntentPersonId, totalDeposit, totalWages, p1, p2, p3, p4, r1, r2, r3, r4, indicate, innerArray)) {
                            return false;
                        }
                    }catch (Exception ex) {
                        ex.printStackTrace();
                        return false;
                    }finally {
                        Database.closeDatabase();//closing connection pool
                    }
                    return true;
                }
                private boolean viewPDFFromDb(byte whichPdf,String fromIntentPersonId) {
                    try {//to view pdf
                        finish();//while going to other activity so destroy this current activity(individualPersonDetailActivity) so that while coming back we will see refresh activity
                        Intent intent=new Intent(IndividualPersonDetailActivity.this, PdfViewerOperationActivity.class);
                        intent.putExtra("pdf1_or_2_or_3_for_blank_4",whichPdf);
                        intent.putExtra("ID",fromIntentPersonId);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplication().startActivity(intent);
                        return true;
                    }catch(Exception e){
                        e.printStackTrace();
                        return false;
                    }
                }
//                private void displayFinalResult(String title,String message) {
//                    AlertDialog.Builder showDataFromDataBase=new AlertDialog.Builder(IndividualPersonDetailActivity.this);
//                    showDataFromDataBase.setCancelable(false);
//                    showDataFromDataBase.setTitle(title);
//                    showDataFromDataBase.setMessage(message);
//                    showDataFromDataBase.setPositiveButton("OK", (dialogInterface, i) -> {//REFRESHING
//                        dialogInterface.dismiss();//close current dialog
//                        Intent intent=new Intent(IndividualPersonDetailActivity.this,IndividualPersonDetailActivity.class);
//                        intent.putExtra("ID",fromIntentPersonId);
//                        finish();//while going to other activity so destroy  this current activity so that while coming back we will see refresh activity
//                        startActivity(intent);
//                    });
//                    showDataFromDataBase.create().show();
//                }
//                private float checkInternalStorageAvailabilityInMB(){
//                    try {
//                        File path = Environment.getDataDirectory();//Return the user data directory.return type FILE and Environment class Provides access to environment variables.
//                        StatFs stat = new StatFs(path.getPath());//Construct a new StatFs for looking at the stats of the filesystem at path.
//                        long blockSize = stat.getBlockSizeLong();//The size, in bytes, of a block on the file system. This corresponds to the Unix statvfs.f_frsize field.
//                        long availableBlocks = stat.getAvailableBlocksLong();//The number of bytes that are free on the file system and available to applications.
//                        String format = Formatter.formatFileSize(IndividualPersonDetailActivity.this, availableBlocks * blockSize);//return available internal storage memory like 9.66 GB
//                        format = format.trim();//for safer side
//
//                        StringBuilder stringBuilder = new StringBuilder();
//                        for (int i = 0; i < format.length(); i++) {
//                            if (format.charAt(i) == ' ' || Character.isAlphabetic(format.charAt(i)))
//                                break;
//                            stringBuilder.append(format.charAt(i));
//                        }
//                        return Float.parseFloat(stringBuilder.toString())*1024;//converted to mb
//                    }catch (Exception x){
//                        x.printStackTrace();
//                    }
//                    return 0;
//                }
                private void updateTotalWorkAmountAndAdvanceOrBalanceTv() {
                    if(!MyUtility.isEnterDataIsWrong(innerArray)) {//if data is right then only change fields
                        workTotalAmountTv.setText(" - " + MyUtility.convertToIndianNumberSystem((p1 * r1) + (p2 * r2) + (p3 * r3) + (p4 * r4) + totalDeposit));

                        //if both wages and total work amount is less then 0 then both message have to show so if statement two times
                        if ((totalDeposit + ((p1 * r1) + (p2 * r2) + (p3 * r3) + (p4 * r4))) < 0) {//user cant enter negative number so when (totalDeposit + (totalr1r2r3r4sum1sum2sum3sum4)) is negative that means int range is exceeds so wrong result will be shown
                            Toast.makeText(IndividualPersonDetailActivity.this, getResources().getString(R.string.value_out_of_range_please_check_total_work_amount), Toast.LENGTH_LONG).show();
                            longPressToSaveAndCreatePdf.setVisibility(View.GONE);//its important otherwise save option will be unable when user enter rate
                        }
                        if(totalWages < 0){//its important otherwise save option will be unable when user enter rate
                            Toast.makeText(IndividualPersonDetailActivity.this, getResources().getString(R.string.value_out_of_range_please_check_total_wages), Toast.LENGTH_LONG).show();
                            longPressToSaveAndCreatePdf.setVisibility(View.GONE);
                        }

                        if ((totalDeposit + ((p1 * r1) + (p2 * r2) + (p3 * r3) + (p4 * r4))) < totalWages) {
                            advanceOrBalanceTv.setTextColor(Color.RED);
                            //                                        total wages -   totalDeposit+(R1*SUMP1)+(R2*SUMP2)+(R3*SUMP3)+(R4*SUMP4)
                            advanceOrBalanceTv.setText("= "+MyUtility.convertToIndianNumberSystem((totalWages - (totalDeposit + ((p1 * r1) + (p2 * r2) + (p3 * r3) + (p4 * r4))))));
                            //       totalDeposit+(R1*SUMP1)+(R2*SUMP2)+(R3*SUMP3)+(R4*SUMP4) >= totalWages
                        } else if ((totalDeposit + ((p1 * r1) + (p2 * r2) + (p3 * r3) + (p4 * r4))) >= totalWages) {//>= is given because of green color
                            advanceOrBalanceTv.setTextColor(getColor(R.color.green));
                            //                                           totalDeposit+(R1*SUMP1)+(R2*SUMP2)+(R3*SUMP3)+(R4*SUMP4) -total wages
                            advanceOrBalanceTv.setText("= "+MyUtility.convertToIndianNumberSystem(((totalDeposit + ((p1 * r1) + (p2 * r2) + (p3 * r3) + (p4 * r4))) - totalWages)));
                        }
                    }else{
                        advanceOrBalanceTv.setText("= 0");
                        advanceOrBalanceTv.setTextColor(getColor(R.color.green));

                        workTotalAmountTv.setText(" - 0");
                    }
                }
                private void indicator1234CalculateButDoNotUpdateToDBFinal(Integer[] sumCursor, int rate1IntoSump1, int rate2IntoSump2, int rate3IntoSump3, int rate4IntoSump4) {
                    int  totalDeposit,totalWages;
                    int totalr1r2r3r4sum1sum2sum3sum4=rate1IntoSump1+rate2IntoSump2+rate3IntoSump3+rate4IntoSump4;
                    totalWages=sumCursor[0];
                    totalDeposit=sumCursor[5];

                    if(((totalDeposit + totalr1r2r3r4sum1sum2sum3sum4) < 0) || (totalr1r2r3r4sum1sum2sum3sum4 < 0) || (totalDeposit < 0)) //user cant enter negative number so when (totalDeposit + (totalr1r2r3r4sum1sum2sum3sum4)) is negative that means int range is exceeds so wrong result will be shown
                        Toast.makeText(IndividualPersonDetailActivity.this, getResources().getString(R.string.value_out_of_range_please_check_total_work_amount), Toast.LENGTH_LONG).show();

                    workTotalAmountTv.setText(" - " + MyUtility.convertToIndianNumberSystem(totalDeposit + (totalr1r2r3r4sum1sum2sum3sum4)));
                    //    totalDeposit+(R1*SUMP1)+(R2*SUMP2)+(R3*SUMP3)+(R4*SUMP4)
                    if ((totalDeposit + (totalr1r2r3r4sum1sum2sum3sum4)) < totalWages) {
                        advanceOrBalanceTv.setTextColor(Color.RED);
                        //                                total wages -   totalDeposit+(R1*SUMP1)+(R2*SUMP2)+(R3*SUMP3)+(R4*SUMP4)
                        advanceOrBalanceTv.setText("= " + MyUtility.convertToIndianNumberSystem(totalWages - (totalDeposit + (totalr1r2r3r4sum1sum2sum3sum4))));

                        //totalDeposit+(R1*SUMP1)+(R2*SUMP2)+(R3*SUMP3)+(R4*SUMP4) >= totalWages
                    }else if((totalDeposit + (totalr1r2r3r4sum1sum2sum3sum4)) >= totalWages) {//>= is given because of green color and when calculation is 0
                        advanceOrBalanceTv.setTextColor(getColor(R.color.green));
                        //                                   totalDeposit+(R1*SUMP1)+(R2*SUMP2)+(R3*SUMP3)+(R4*SUMP4) -total wages
                        advanceOrBalanceTv.setText("= " + MyUtility.convertToIndianNumberSystem((totalDeposit + (totalr1r2r3r4sum1sum2sum3sum4)) -totalWages));
                    }
                }
                private void initialiseIDs(View myView) {
                      mainSkillTextTv =myView.findViewById(R.id.default_skill_text_tv_final);
                      totalP1CountTv=myView.findViewById(R.id.total_p1_count_tv_final);
                      p1RateTv=myView.findViewById(R.id.p1_rate_tv_final);
                      totalP1AmountTv=myView.findViewById(R.id.total_p1_amount_tv_final);

                      skill1TextTv=myView.findViewById(R.id.skill1_text_tv_final);
                      totalP2CountTv=myView.findViewById(R.id.total_p2_count_tv_final);
                      p2RateTv=myView.findViewById(R.id.p2_rate_tv_final);
                      totalP2AmountTv=myView.findViewById(R.id.total_p2_amount_tv_final);

                      skill2TextTv=myView.findViewById(R.id.skill2_text_tv_final);
                      totalP3CountTv=myView.findViewById(R.id.total_p3_count_tv_final);
                      p3RateTv=myView.findViewById(R.id.p3_rate_tv_final);
                      totalP3AmountTv=myView.findViewById(R.id.total_p3_amount_tv_final);

                      skill3TextTv=myView.findViewById(R.id.skill3_text_tv_final);
                      totalP4CountTv=myView.findViewById(R.id.total_p4_count_tv_final);
                      p4RateTv=myView.findViewById(R.id.p4_rate_tv_final);
                      totalP4AmountTv=myView.findViewById(R.id.total_p4_amount_tv_final);

                      totalDepositAmountTv=myView.findViewById(R.id.total_deposit_amount_tv_final);
                      wagesTotalAmountTv=myView.findViewById(R.id.wages_total_amount_tv_final);
                      workTotalAmountTv=myView.findViewById(R.id.work_total_amount_tv_final);
                      advanceOrBalanceTv=myView.findViewById(R.id.advance_or_balance_tv_final);

                      p2Layout=myView.findViewById(R.id.p2_layout_final);
                      p3Layout=myView.findViewById(R.id.p3_layout_final);
                      p4Layout=myView.findViewById(R.id.p4_layout_final);
                      totalDepositAmountLayout=myView.findViewById(R.id.total_deposit_amount_layout_final);

                      longPressToSaveAndCreatePdf =myView.findViewById(R.id.save_btn_final);
                      cancel=myView.findViewById(R.id.cancel_btn_final);
                }
            });
            binding.callTv.setOnClickListener(view -> {//it will call to first active number if not available then make call to second active number
                if (MyUtility.getActiveOrBothPhoneNumber(fromIntentPersonId,getBaseContext(),true) != null) {
                    Intent callingIntent = new Intent(Intent.ACTION_DIAL);
                    callingIntent.setData(Uri.parse("tel:+91" + MyUtility.getActiveOrBothPhoneNumber(fromIntentPersonId,getBaseContext(),true)));
                    startActivity(callingIntent);
                } else
                    Toast.makeText(IndividualPersonDetailActivity.this, getResources().getString(R.string.no_phone_number), Toast.LENGTH_SHORT).show();
            });
            binding.editTv.setOnClickListener(view -> {
                Intent intent = new Intent(getBaseContext(), RegisterPersonDetailsActivity.class);
                intent.putExtra("ID", fromIntentPersonId);
                startActivity(intent);
                finish();//while going to other activity so destroy  this current activity so that while coming back we will see refresh activity
            });
            binding.gobackIndividualPersonDetails.setOnClickListener(view -> {
                finish();
                  //super.onBackPressed();// This calls finish() on this activity and pops the back stack.
                    });
        } else
            Toast.makeText(this, "NO ID FROM OTHER INTENT", Toast.LENGTH_LONG).show();
        //to insert data in recyclerview
         binding.fab.setOnClickListener(view -> {
            correctInputArray =new int[7];//so that when again enter data new array will be created with new values
            insertDataToRecyclerView_AlertDialogBox(MyUtility.get_indicator(getBaseContext(),fromIntentPersonId));
        });
    }
    private boolean setNameImageIdPhoneAadhaar() {
        try(Cursor cursor = db.getData("SELECT "+Database.COL_2_NAME+","+Database.COL_3_BANKAC+","+Database.COL_6_AADHAAR_NUMBER+","+Database.COL_7_MAIN_ACTIVE_PHONE1 +","+Database.COL_10_IMAGE_PATH +","+Database.COL_11_ACTIVE_PHONE2+","+Database.COL_1_ID+" FROM " + Database.PERSON_REGISTERED_TABLE + " WHERE "+Database.COL_1_ID+"='" + fromIntentPersonId + "'")) {
            if (cursor != null && cursor.moveToFirst()) {

                binding.nameTv.setText(cursor.getString(0));
                binding.accountTv.setText(HtmlCompat.fromHtml("A/C:  " + "<b>" + (cursor.getString(1) != null ? cursor.getString(1) : "") + "</b>", HtmlCompat.FROM_HTML_MODE_LEGACY));
                binding.aadharTv.setText(HtmlCompat.fromHtml("AADHAAR CARD:  " + "<b>" + (cursor.getString(2) != null ? cursor.getString(2) : "") + "</b>", HtmlCompat.FROM_HTML_MODE_LEGACY));
                binding.activePhone1Tv.setText(HtmlCompat.fromHtml("ACTIVE PHONE1:  " + "<b>" + (cursor.getString(3) != null ? cursor.getString(3) : "") + "</b>", HtmlCompat.FROM_HTML_MODE_LEGACY));

                if (MyUtility.getActiveOrBothPhoneNumber(fromIntentPersonId, getBaseContext(), true) != null) {//if there is no phone number then show default icon color black else green icon
                    binding.callTv.setBackgroundResource(R.drawable.ic_outline_call_24);
                }

//                byte[] image = cursor.getBlob(4);//getting image from db as blob
//                if (image != null) {
//                    //getting bytearray image from DB and converting  to bitmap to set in imageview
//                    Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
//                    binding.imageImg.setImageBitmap(bitmap);
//                }

                String imagePath=cursor.getString(4);//getting image path from db
                if(imagePath!=null){
                    Bitmap bitmap = MyUtility.getBitmapFromPath(imagePath);//converting image path to bitmap
                    if(bitmap != null){
                        binding.imageImg.setImageBitmap(bitmap);
                    }//default image will be shown
//                    else{//default image will be shown
//                        Toast.makeText(this, getString(R.string.no_image), Toast.LENGTH_LONG).show();
//                    }
                }//else default image will be shown

                binding.activePhone2Tv.setText(HtmlCompat.fromHtml("PHONE2: " + "<b>" + (cursor.getString(5) != null ? cursor.getString(5) : "") + "</b>", HtmlCompat.FROM_HTML_MODE_LEGACY));
                binding.idTv.setText("ID " + cursor.getString(6));
            }
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }
        return true;
    }
    private boolean setStarAndLeavingDate() {
        try(Cursor cursor2 = db.getData("SELECT "+Database.COL_391_STAR +","+Database.COL_392_LEAVINGDATE+" FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE "+Database.COL_31_ID+"='" + fromIntentPersonId +"'")){
            cursor2.moveToFirst();
            if(cursor2.getString(0) != null || cursor2.getString(1) != null){

                binding.starRatingTv.setText((cursor2.getString(0) != null) ? (cursor2.getString(0) + " *") : "0 *");//when leaving date is more the 21 days then show star

                if (cursor2.getString(1) != null) {//when there is leaving date https://www.youtube.com/watch?v=VmhcvoenUl0
                    LocalDate dbDate, todayDate = LocalDate.now();//current date; return 2022-05-01
                    String[] dateArray = cursor2.getString(1).split("-");

                    dbDate = LocalDate.of(Integer.parseInt(dateArray[2]), Integer.parseInt(dateArray[1]), Integer.parseInt(dateArray[0]));//it convert 2022-05-01 it add 0 automatically
                    //between (2022-05-01,2022-05-01) like
                    // Toast.makeText(contex, ""+ ChronoUnit.DAYS.between(todayDate,dbDate)+" DAYS LEFT TO LEAVE", Toast.LENGTH_SHORT).show();//HERE dbDate will always be higher then todayDate because user will leave in forward date so in method Chrono Unit todayDate is written first and second dbDate to get right days
                    //between (2022-05-01,2022-05-01) like

                    //to show how many days left to leave
                    // binding.starRatingTv.setText(ChronoUnit.DAYS.between(todayDate, dbDate) + " " + getResources().getString(R.string.days_left));//HERE dbDate will always be higher then todayDate because user will leave in forward date so in method Chrono Unit todayDate is written first and second dbDate to get right days

                    if (ChronoUnit.DAYS.between(todayDate, dbDate) <= redIndicatorToLeave) {//if true then update text
                        binding.leavingOrNotColorIndicationLayout.setBackgroundColor(Color.RED);//red color indicate person going to leave within 3 weeks
                        binding.starRatingTv.setText(ChronoUnit.DAYS.between(todayDate, dbDate) + " " + getResources().getString(R.string.days_to_leave));//HERE dbDate will always be higher then todayDate because user will leave in forward date so in method Chrono Unit todayDate is written first and second dbDate to get right days
                    }
                }
            }else{
                binding.starRatingTv.setText("0 *");//if user has never press save button on Meta data then by default 0* will be shown
            }
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }
        return true;
    }

    //    public  String generateMessageAccordingToIndicator(String star,String leavingDate,String returningDate,String locationAutoComplete,String religionAutoComplete,String remarksMetaData,int indicator,String skill1,int p1Rate,String skill2,int p2Rate,String skill3,int p3Rate,String skill4,int p4Rate){
//        StringBuilder sb=new StringBuilder();
//        switch (indicator){
//            case 1:{
//                sb.append(skill1).append(" : "+p1Rate);
//            }break;
//            case 2:{
//                sb.append(skill1).append(" : "+p1Rate).append("  "+skill2).append(" : "+p2Rate);
//            }break;
//            case 3:{
//                sb.append(skill1).append(" : "+p1Rate).append("  "+skill2).append(" : "+p2Rate).append("  "+skill3).append(" : "+p3Rate);
//            }break;
//            case 4:{
//                sb.append(skill1).append(" : "+p1Rate).append("  "+skill2).append(" : "+p2Rate).append("  "+skill3).append(" : "+p3Rate).append("  "+skill4).append(" : "+p4Rate);
//            }break;
//        }
//        sb.append("\n\nSTAR:  " + star)
//                .append("\nLEAVING :     "+leavingDate)
//                .append("\nRETURNING : "+returningDate)
//                .append("\nLOCATION:  "+locationAutoComplete)
//                .append("\nRELIGION:    "+religionAutoComplete)
//                .append( "\n\nREMARKS: "+remarksMetaData);
//        return sb.toString();
//    }
    private void setRateComponentAccordingToId(TextView hardcodedP1Tv, EditText inputP1Rate, TextView hardcodedP2Tv, EditText inputP2Rate, TextView hardcodedP3Tv, EditText inputP3Rate, TextView hardcodedP4Tv, EditText inputP4Rate, Button saveButton, int checkCorrectionArray[], int userInputRateArray[], int indicator, String id) {
        try(Database db=new Database(getBaseContext());
            Cursor rateCursor1 = db.getData("SELECT " + Database.COL_32_R1 + " FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE " + Database.COL_31_ID + "= '" + id + "'")){
            if(rateCursor1 != null) rateCursor1.moveToFirst();
            String mainSkill=db.getOnlyMainSkill(id);
            if(mainSkill==null){
               mainSkill="error";
            }

            Cursor skillNRateCursor=null;
            if(indicator > 1) {//if indicator more than 1 then get all skill and rate
                  skillNRateCursor = db.getData("SELECT " + Database.COL_36_SKILL2 + "," + Database.COL_37_SKILL3 + "," + Database.COL_38_SKILL4 + " , "+ Database.COL_33_R2 + "," + Database.COL_34_R3 + "," + Database.COL_35_R4 + " FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE " + Database.COL_31_ID + "= '" + id + "'");
                if (skillNRateCursor != null) skillNRateCursor.moveToFirst();
            }
            //initially hide
            hardcodedP2Tv.setVisibility(View.GONE);
            inputP2Rate.setVisibility(View.GONE);
            hardcodedP3Tv.setVisibility(View.GONE);
            inputP3Rate.setVisibility(View.GONE);
            hardcodedP4Tv.setVisibility(View.GONE);
            inputP4Rate.setVisibility(View.GONE);

            switch (indicator){
                case 1: {
                    hardcodedP1Tv.setText(mainSkill);
                    inputP1Rate.setText(rateCursor1.getString(0));
                    rate1Et(inputP1Rate, saveButton, checkCorrectionArray, userInputRateArray);//input rate array is updated
                }
                break;
                case 2: {
                    hardcodedP2Tv.setVisibility(View.VISIBLE);
                    inputP2Rate.setVisibility(View.VISIBLE);

                    hardcodedP1Tv.setText(mainSkill);
                    inputP1Rate.setText(rateCursor1.getString(0));
                    rate1Et(inputP1Rate, saveButton, checkCorrectionArray, userInputRateArray);//input rate array is updated

                    hardcodedP2Tv.setText(skillNRateCursor.getString(0));
                    inputP2Rate.setText(skillNRateCursor.getString(3));
                    rate2Et(inputP2Rate, saveButton, checkCorrectionArray, userInputRateArray);//input rate array is updated
                }
                break;
                case 3: {
                    hardcodedP2Tv.setVisibility(View.VISIBLE);
                    inputP2Rate.setVisibility(View.VISIBLE);

                    hardcodedP3Tv.setVisibility(View.VISIBLE);
                    inputP3Rate.setVisibility(View.VISIBLE);

                    hardcodedP1Tv.setText(mainSkill);
                    inputP1Rate.setText(rateCursor1.getString(0));
                    rate1Et(inputP1Rate, saveButton, checkCorrectionArray, userInputRateArray);//input rate array is updated

                    hardcodedP2Tv.setText(skillNRateCursor.getString(0));
                    inputP2Rate.setText(skillNRateCursor.getString(3));
                    rate2Et(inputP2Rate, saveButton, checkCorrectionArray, userInputRateArray);//input rate array is updated

                    hardcodedP3Tv.setText(skillNRateCursor.getString(1));
                    inputP3Rate.setText(skillNRateCursor.getString(4));
                    rate3Et(inputP3Rate, saveButton, checkCorrectionArray, userInputRateArray);//input rate array is updated
                }
                break;
                case 4: {
                    hardcodedP2Tv.setVisibility(View.VISIBLE);
                    inputP2Rate.setVisibility(View.VISIBLE);

                    hardcodedP3Tv.setVisibility(View.VISIBLE);
                    inputP3Rate.setVisibility(View.VISIBLE);

                    hardcodedP4Tv.setVisibility(View.VISIBLE);
                    inputP4Rate.setVisibility(View.VISIBLE);

                    hardcodedP1Tv.setText(mainSkill);
                    inputP1Rate.setText(rateCursor1.getString(0));
                    rate1Et(inputP1Rate, saveButton, checkCorrectionArray, userInputRateArray);//input rate array is updated

                    hardcodedP2Tv.setText(skillNRateCursor.getString(0));
                    inputP2Rate.setText(skillNRateCursor.getString(3));
                    rate2Et(inputP2Rate, saveButton, checkCorrectionArray, userInputRateArray);//input rate array is updated

                    hardcodedP3Tv.setText(skillNRateCursor.getString(1));
                    inputP3Rate.setText(skillNRateCursor.getString(4));
                    rate3Et(inputP3Rate, saveButton, checkCorrectionArray, userInputRateArray);//input rate array is updated

                    hardcodedP4Tv.setText(skillNRateCursor.getString(2));
                    inputP4Rate.setText(skillNRateCursor.getString(5));
                    rate4Et(inputP4Rate, saveButton, checkCorrectionArray, userInputRateArray);//input rate array is updated
                }break;
            }
            if(skillNRateCursor != null) {
                skillNRateCursor.close();
            }
        }catch (Exception x){
            x.printStackTrace();
            Toast.makeText(this, "error occurred to show visibility", Toast.LENGTH_LONG).show();
        }
    }
    private void rate1Et(EditText inputP1Rate, Button  saveButton, int checkCorrectionArray[], int userInputRateArray[]) {
        inputP1Rate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String p1 =inputP1Rate.getText().toString().trim();
                inputP1Rate.setTextColor(getColor(R.color.black));
                checkCorrectionArray[0]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data
                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(checkCorrectionArray)) {//this is important if in field data is wrong then save button will not enabled until data is right.
                     saveButton.setVisibility(View.VISIBLE);
                }
                if (!(p1.matches("[0-9]+") || TextUtils.isEmpty(p1))) {//space or , or - is restricted"[.]?[0-9]+[.]?[0-9]*"
                    inputP1Rate.setTextColor(Color.RED);
                     saveButton.setVisibility(View.GONE);
                    checkCorrectionArray[0]=2;//means data is inserted wrong
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                try{
                    if(!TextUtils.isEmpty(inputP1Rate.getText().toString().trim())) {//
                        userInputRateArray[0] = Integer.parseInt(inputP1Rate.getText().toString().trim());
                    }
                }catch(Exception e){//when user enter wrong input
                    e.printStackTrace();
                   // Log.d(this.getClass().getSimpleName(), "method afterTextChanged: wrong input");
                }
            }
        });
    }
    private void rate2Et(EditText inputP2Rate, Button saveButton, int[] checkCorrectionArray, int[] userInputRateArray) {
        inputP2Rate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String p1 =inputP2Rate.getText().toString().trim();
                inputP2Rate.setTextColor(getColor(R.color.black));
                checkCorrectionArray[1]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data
                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(checkCorrectionArray)) {//this is important if in field data is wrong then save button will not enabled until data is right.
                    saveButton.setVisibility(View.VISIBLE);

                }
                if (!(p1.matches("[0-9]+") || TextUtils.isEmpty(p1))) {//space or , or - is restricted"[.]?[0-9]+[.]?[0-9]*"
                    inputP2Rate.setTextColor(Color.RED);
                    saveButton.setVisibility(View.GONE);
                    checkCorrectionArray[1]=2;//means data is inserted wrong
                    // Toast.makeText(IndividualPersonDetailActivity.this, "NOT ALLOWED(space  .  ,  -)\nPLEASE CORRECT", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                try{
                    if(!TextUtils.isEmpty(inputP2Rate.getText().toString().trim())) {
                        userInputRateArray[1] = Integer.parseInt(inputP2Rate.getText().toString().trim());
                    }

                }catch(Exception x){
                    x.printStackTrace();
                    //Log.d(this.getClass().getSimpleName(), "method afterTextChanged: wrong input");
                }
            }
        });
    }
    private void rate3Et(EditText inputP3Rate, Button saveButton, int[] checkCorrectionArray, int[] userInputRateArray) {
        inputP3Rate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String p1 =inputP3Rate.getText().toString().trim();
                inputP3Rate.setTextColor(getColor(R.color.black));
                checkCorrectionArray[2]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data
                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(checkCorrectionArray)) {//this is important if in field data is wrong then save button will not enabled until data is right.
                    saveButton.setVisibility(View.VISIBLE);
                }
                if (!(p1.matches("[0-9]+") || TextUtils.isEmpty(p1))){//space or , or - is restricted"[.]?[0-9]+[.]?[0-9]*"
                    inputP3Rate.setTextColor(Color.RED);
                    saveButton.setVisibility(View.GONE);
                    checkCorrectionArray[2]=2;//means data is inserted wrong
                    //Toast.makeText(IndividualPersonDetailActivity.this, "NOT ALLOWED(space  .  ,  -)\nPLEASE CORRECT", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    if(!TextUtils.isEmpty(inputP3Rate.getText().toString().trim())) {
                        userInputRateArray[2] = Integer.parseInt(inputP3Rate.getText().toString().trim());
                    }
                }catch (Exception e){
                    e.printStackTrace();
                   // Log.d(this.getClass().getSimpleName(), "method afterTextChanged: wrong input");
                }
            }
        });
    }
    private void rate4Et(EditText inputP4Rate, Button saveButton, int[] checkCorrectionArray, int[] userInputRateArray) {
        inputP4Rate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String p1 =inputP4Rate.getText().toString().trim();
                inputP4Rate.setTextColor(getColor(R.color.black));
                checkCorrectionArray[3]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data
                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(checkCorrectionArray)) {//this is important if in field data is wrong then save button will not enabled until data is right.
                    saveButton.setVisibility(View.VISIBLE);
                }
                if (!(p1.matches("[0-9]+") || TextUtils.isEmpty(p1))){//space or , or - is restricted"[.]?[0-9]+[.]?[0-9]*"
                    inputP4Rate.setTextColor(Color.RED);
                    saveButton.setVisibility(View.GONE);
                    checkCorrectionArray[3]=2;//means data is inserted wrong
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    if(!TextUtils.isEmpty(inputP4Rate.getText().toString().trim())) {
                        userInputRateArray[3] = Integer.parseInt(inputP4Rate.getText().toString().trim());
                    }
                }catch(Exception e){
                    e.printStackTrace();
                   // Log.d(this.getClass().getSimpleName(), "method afterTextChanged: wrong input");
                }
            }
        });
    }
    private void indicator1234CalculateAndUpdate(Integer[] sumCursor, int rate1IntoSump1, int rate2IntoSump2, int rate3IntoSump3, int rate4IntoSump4,int indicator,String id) {
//        boolean bool;
        int  totalDeposit,totalWages;
        int totalr1r2r3r4sum1sum2sum3sum4=rate1IntoSump1+rate2IntoSump2+rate3IntoSump3+rate4IntoSump4;
        totalWages=sumCursor[0];
        totalDeposit=sumCursor[5];
        if(totalDeposit==0){
            binding.textWorkDepositTv.setText(getResources().getString(R.string.total_work_amount));//if no deposit
        }else{
            binding.textWorkDepositTv.setText(getResources().getString(R.string.total_work_deposit_plus_work_amount));
        }

        if(((totalDeposit + totalr1r2r3r4sum1sum2sum3sum4) < 0) || (totalr1r2r3r4sum1sum2sum3sum4 < 0) || (totalDeposit < 0)) //user cant enter negative number so when (totalDeposit + (totalr1r2r3r4sum1sum2sum3sum4)) is negative that means int range is exceeds so wrong result will be shown
            Toast.makeText(this,getResources().getString(R.string.value_out_of_range_please_check_total_work_amount), Toast.LENGTH_LONG).show();

        binding.workAndDepositTotalAmountTv.setText(MyUtility.convertToIndianNumberSystem(totalDeposit + (totalr1r2r3r4sum1sum2sum3sum4)));
        //    totalDeposit+(R1*SUMP1)+(R2*SUMP2)+(R3*SUMP3)+(R4*SUMP4)
        if (((totalDeposit + (totalr1r2r3r4sum1sum2sum3sum4)) < totalWages) && isAllRateSet(MyUtility.getRateArray(id,getBaseContext()),indicator)) {//if all rate is there than only show output

            binding.textAdvanceOrBalanceTv.setText(getResources().getString(R.string.advance_due));
            binding.advanceOrBalanceTv.setTextColor(Color.RED);
            //                                        total wages -   totalDeposit+(R1*SUMP1)+(R2*SUMP2)+(R3*SUMP3)+(R4*SUMP4)
            binding.advanceOrBalanceTv.setText(MyUtility.convertToIndianNumberSystem(totalWages - (totalDeposit + (totalr1r2r3r4sum1sum2sum3sum4))));

            //updating Advance to db                                                    total wages -   totalDeposit+(R1*SUMP1)+(R2*SUMP2)+(R3*SUMP3)+(R4*SUMP4)
            if(!(db.updateTable("UPDATE " + Database.PERSON_REGISTERED_TABLE + " SET "+Database.COL_13_ADVANCE+"='" + (totalWages - (totalDeposit + (totalr1r2r3r4sum1sum2sum3sum4))) + "'" + "WHERE "+Database.COL_1_ID+"='" + fromIntentPersonId + "'") && db.updateTable("UPDATE " + Database.PERSON_REGISTERED_TABLE + " SET "+Database.COL_14_BALANCE+"='" + 0 + "'" + "WHERE "+Database.COL_1_ID+"='" + fromIntentPersonId + "'"))){/*Situation when user first enter jama /totalDeposit amount then wages amount which is greater then jama amount then balance column should be updated otherwise advance column will have amount and balance column will also have amount so when there is advance then balance should be 0.*/
                Toast.makeText(this, "BALANCE AND ADVANCE AMOUNT NOT UPDATED TO DATABASE", Toast.LENGTH_LONG).show();
//                bool = db.updateTable("UPDATE " + Database.TABLE_NAME1 + " SET "+Database.COL_14_BALANCE+"='" + 0 + "'" + "WHERE "+Database.COL_1_ID+"='" + fromIntentPersonId + "'");
//                if (bool == false)
//                    Toast.makeText(this, "BALANCE AMOUNT NOT UPDATED TO DATABASE", Toast.LENGTH_LONG).show();
            }
//            else{
//                Toast.makeText(this, "ADVANCE AMOUNT NOT UPDATED TO DATABASE", Toast.LENGTH_LONG).show();
//            }
            //totalDeposit+(R1*SUMP1)+(R2*SUMP2)+(R3*SUMP3)+(R4*SUMP4) >= totalWages
        }else if(((totalDeposit + (totalr1r2r3r4sum1sum2sum3sum4)) >= totalWages) && isAllRateSet(MyUtility.getRateArray(id,getBaseContext()),indicator)) {//>= is given because of green color and when calculation is 0
            binding.textAdvanceOrBalanceTv.setText(getResources().getString(R.string.balance));
            binding.advanceOrBalanceTv.setTextColor(getColor(R.color.green));
            //                                           totalDeposit+(R1*SUMP1)+(R2*SUMP2)+(R3*SUMP3)+(R4*SUMP4) -total wages
            binding.advanceOrBalanceTv.setText(MyUtility.convertToIndianNumberSystem((totalDeposit + (totalr1r2r3r4sum1sum2sum3sum4)) - totalWages));

            //updating balance to db if greater then or equal to 0
           // bool = db.updateTable("UPDATE " + Database.TABLE_NAME1 + " SET "+Database.COL_14_BALANCE+"='" + ((totalDeposit + (totalr1r2r3r4sum1sum2sum3sum4)) -totalWages) + "'" + "WHERE "+Database.COL_1_ID+"='" + fromIntentPersonId + "'");
            if(!(db.updateTable("UPDATE " + Database.PERSON_REGISTERED_TABLE + " SET "+Database.COL_14_BALANCE+"='" + ((totalDeposit + (totalr1r2r3r4sum1sum2sum3sum4)) -totalWages) + "'" + "WHERE "+Database.COL_1_ID+"='" + fromIntentPersonId + "'") && db.updateTable("UPDATE " + Database.PERSON_REGISTERED_TABLE + " SET "+Database.COL_13_ADVANCE+"='" + 0 + "'" + "WHERE "+Database.COL_1_ID+"='" + fromIntentPersonId + "'"))){ //if there is balance then update advance column should be 0
                Toast.makeText(this, "BALANCE AND ADVANCE AMOUNT NOT UPDATED TO DATABASE", Toast.LENGTH_LONG).show();
//                bool = db.updateTable("UPDATE " + Database.TABLE_NAME1 + " SET "+Database.COL_13_ADVANCE+"='" + 0 + "'" + "WHERE "+Database.COL_1_ID+"='" + fromIntentPersonId + "'");
//                if (bool == false)
//                    Toast.makeText(this, "ADVANCE AMOUNT NOT UPDATED TO DATABASE", Toast.LENGTH_LONG).show();
            }
//            else{
//                Toast.makeText(this, "BALANCE AMOUNT NOT UPDATED TO DATABASE", Toast.LENGTH_LONG).show();
//            }
        }else{
            binding.advanceOrBalanceTv.setTextColor(getColor(R.color.purple_700));
            binding.advanceOrBalanceTv.setText(getString(R.string.set_rate));
        }
    }
    private void insertDataToRecyclerView_AlertDialogBox(int indicator) {
        final boolean[] editOrNot = {false,false};//for spinner to close dialogbox
        AlertDialog.Builder myCustomDialog=new AlertDialog.Builder(IndividualPersonDetailActivity.this);
        LayoutInflater inflater=LayoutInflater.from(IndividualPersonDetailActivity.this);

        View myView=inflater.inflate(R.layout.input_data_to_recycler,null);//myView contain all layout view ids
        myCustomDialog.setView(myView);//set custom layout to alert dialog
        myCustomDialog.setCancelable(false);//if user touch to other place then dialog will not be close

        final AlertDialog customDialog=myCustomDialog.create();//myCustomDialog variable cannot be use in inner class so creating another final variable  to use in inner class

        mediaRecorder=null;//so that it not take previous VALUE
        audioPath=null;//so that it not take previous VALUE

        Spinner customSpinnerRemoveOrAddMlg=myView.findViewById(R.id.info_spinner_add_lmg_or_remove);
        TextView deposit_btn_tv=myView.findViewById(R.id.to_deposit_tv);
        TextView hardcodedP1=myView.findViewById(R.id.hardcoded_p1_tv);
        TextView hardcodedP2=myView.findViewById(R.id.hardcoded_p2_tv);
        TextView hardcodedP3=myView.findViewById(R.id.hardcoded_p3_tv);
        TextView hardcodedP4=myView.findViewById(R.id.hardcoded_p4_tv);
        TextView micIcon=myView.findViewById(R.id.mic_tv);
        TextView advanceOrBalanceWarring=myView.findViewById(R.id.advance_or_balance_amount_warring_tv);
        TextView noOfDaysToWork=myView.findViewById(R.id.no_of_days);
        TextView inputDate=myView.findViewById(R.id.input_date_tv);
        TextView saveAudio=myView.findViewById(R.id.save_audio_tv);
        TextView advanceOrBalanceText=myView.findViewById(R.id.wages_input_text_advance_or_balance_tv);
        Chronometer playAudioChronometer =myView.findViewById(R.id.chronometer);

        EditText inputP1=myView.findViewById(R.id.input_p1_et);
        //to open keyboard automatically
        Window window = customDialog.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        TextView runtimeSuggestionAmountToGive=myView.findViewById(R.id.work_amount_tv);
        EditText inputP2=myView.findViewById(R.id.input_p2_et);
        EditText inputP3=myView.findViewById(R.id.input_p3_et);
        EditText inputP4=myView.findViewById(R.id.input_p4_et);
        EditText toGiveWages=myView.findViewById(R.id.wages_et);
        EditText description=myView.findViewById(R.id.enter_description_et);
        Button save=myView.findViewById(R.id.save_btn);
        save.setVisibility(View.GONE);//initially save button is disabled it is enabled when user enter any data and its important otherwise app crash
        Button cancel=myView.findViewById(R.id.cancel_btn);

        //****************************************************setting adapter for addOrRemoveMLG spinner*****************************************
        String[] addOrRemoveMLG = getResources().getStringArray(R.array.addOrRemoveMlG);
        addOrRemoveMLG[0]="ADD/REMOVE";//changing the value of array to show to user
        customSpinnerRemoveOrAddMlg.setAdapter(new ArrayAdapter<>(IndividualPersonDetailActivity.this, android.R.layout.simple_list_item_1, addOrRemoveMLG));
        // when activity is loaded spinner item is selected automatically so to avoid this we are using customSpinnerSetting.setSelection(initialPosition, false);
//            int initialposition = binding.customSpinnerSetting.getSelectedItemPosition();
//            binding.customSpinnerSetting.setSelection(initialposition, false);//clearing auto selected item
        customSpinnerRemoveOrAddMlg.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {//**Spinner OnItemSelectedListener event will execute twice:1.Spinner initializationUser 2.selected manually Try to differentiate those two by using flag variable.that's the reason boolean array is used
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                String data = adapterView.getItemAtPosition(pos).toString();
                try(Database db=new Database(getBaseContext());
                    Cursor cursor =db.getData("SELECT "+Database.COL_36_SKILL2 +","+Database.COL_37_SKILL3 +","+Database.COL_38_SKILL4 +" FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE "+Database.COL_31_ID+"= '" + fromIntentPersonId +"'")) {
                    cursor.moveToFirst();//skill which is null there skill is updated
                    switch (data) {
                        case "ADD L": //adding L means p2
                        {
                            editOrNot[1] = true;//indicate user has selected option

                            if (cursor.getString(0) == null) {
                                showDialogAsMessage("UPDATE " + Database.TABLE_NAME_RATE_SKILL + " SET " + Database.COL_36_SKILL2 + "='" + getResources().getString(R.string.laber) + "' , " + Database.COL_39_INDICATOR + "=" + 2 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'", getResources().getString(R.string.successfully_added_l), getResources().getString(R.string.status_colon_success), getResources().getString(R.string.failed_to_add_l), getResources().getString(R.string.status_colon_failed));

                            } else if (cursor.getString(1) == null) {
                                showDialogAsMessage("UPDATE " + Database.TABLE_NAME_RATE_SKILL + " SET " + Database.COL_37_SKILL3 + "='" + getResources().getString(R.string.laber) + "' , " + Database.COL_39_INDICATOR + "=" + 3 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'", getResources().getString(R.string.successfully_added_l), getResources().getString(R.string.status_colon_success), getResources().getString(R.string.failed_to_add_l), getResources().getString(R.string.status_colon_failed));

                            } else if (cursor.getString(2) == null) {
                                showDialogAsMessage("UPDATE " + Database.TABLE_NAME_RATE_SKILL + " SET " + Database.COL_38_SKILL4 + "='" + getResources().getString(R.string.laber) + "' , " + Database.COL_39_INDICATOR + "=" + 4 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'", getResources().getString(R.string.successfully_added_l), getResources().getString(R.string.status_colon_success), getResources().getString(R.string.failed_to_add_l), getResources().getString(R.string.status_colon_failed));

                            } else
                                displayResultAndRefresh(getResources().getString(R.string.only_4_person_allowed_to_add), getResources().getString(R.string.status_cant_add_more) + getResources().getString(R.string.laber));
                        }break;
                        case "ADD M": //adding M p3
                        {
                            editOrNot[1] = true;//indicate user has selected option
                            if (cursor.getString(0) == null) {
                                showDialogAsMessage("UPDATE " + Database.TABLE_NAME_RATE_SKILL + " SET " + Database.COL_36_SKILL2 + "='" + getResources().getString(R.string.mestre) + "' , " + Database.COL_39_INDICATOR + "=" + 2 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'", getResources().getString(R.string.successfully_added_m), getResources().getString(R.string.status_colon_success), getResources().getString(R.string.failed_to_add_m), getResources().getString(R.string.status_colon_failed));

                            } else if (cursor.getString(1) == null) {
                                showDialogAsMessage("UPDATE " + Database.TABLE_NAME_RATE_SKILL + " SET " + Database.COL_37_SKILL3 + "='" + getResources().getString(R.string.mestre) + "' , " + Database.COL_39_INDICATOR + "=" + 3 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'", getResources().getString(R.string.successfully_added_m), getResources().getString(R.string.status_colon_success), getResources().getString(R.string.failed_to_add_m), getResources().getString(R.string.status_colon_failed));

                            } else if (cursor.getString(2) == null) {
                                showDialogAsMessage("UPDATE " + Database.TABLE_NAME_RATE_SKILL + " SET " + Database.COL_38_SKILL4 + "='" + getResources().getString(R.string.mestre) + "' , " + Database.COL_39_INDICATOR + "=" + 4 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'", getResources().getString(R.string.successfully_added_m), getResources().getString(R.string.status_colon_success), getResources().getString(R.string.failed_to_add_m), getResources().getString(R.string.status_colon_failed));

                            } else
                                displayResultAndRefresh(getResources().getString(R.string.only_4_person_allowed_to_add), getResources().getString(R.string.status_cant_add_more) + getResources().getString(R.string.mestre));
                        }break;
                        case "ADD G": //adding G p4
                        {
                            editOrNot[1] = true;//indicate user has selected option

                            if (cursor.getString(0) == null) {
                                showDialogAsMessage("UPDATE " + Database.TABLE_NAME_RATE_SKILL + " SET " + Database.COL_36_SKILL2 + "='" + getResources().getString(R.string.women_laber) + "' , " + Database.COL_39_INDICATOR + "=" + 2 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'", getResources().getString(R.string.successfully_added_g), getResources().getString(R.string.status_colon_success), getResources().getString(R.string.failed_to_add_g), getResources().getString(R.string.status_colon_failed));

                            } else if (cursor.getString(1) == null) {
                                showDialogAsMessage("UPDATE " + Database.TABLE_NAME_RATE_SKILL + " SET " + Database.COL_37_SKILL3 + "='" + getResources().getString(R.string.women_laber) + "' , " + Database.COL_39_INDICATOR + "=" + 3 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'", getResources().getString(R.string.successfully_added_g), getResources().getString(R.string.status_colon_success), getResources().getString(R.string.failed_to_add_g), getResources().getString(R.string.status_colon_failed));

                            } else if (cursor.getString(2) == null) {
                                showDialogAsMessage("UPDATE " + Database.TABLE_NAME_RATE_SKILL + " SET " + Database.COL_38_SKILL4 + "='" + getResources().getString(R.string.women_laber) + "' , " + Database.COL_39_INDICATOR + "=" + 4 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'", getResources().getString(R.string.successfully_added_g), getResources().getString(R.string.status_colon_success), getResources().getString(R.string.failed_to_add_g), getResources().getString(R.string.status_colon_failed));

                            } else
                                displayResultAndRefresh(getResources().getString(R.string.only_4_person_allowed_to_add), getResources().getString(R.string.status_cant_add_more) + getResources().getString(R.string.women_laber));
                        }break;
                        case "REMOVE M/L/G": //removing
                        {
                            try (Cursor cursorIndicator = db.getData("SELECT " + Database.COL_39_INDICATOR + " FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'")){//First getting indicator to decide whether delete or not.if indicator is null then cant delete because by default M or L or G present.If indicator is 2,3,4 then checking data is present or not if present then don't delete else delete
                                editOrNot[1] = true;//indicate user has selected option
                                if (cursorIndicator != null && cursorIndicator.moveToFirst()) {

                                    if (cursorIndicator.getString(0) == null) {//null or 1 person1
                                        displayResultAndRefresh(getResources().getString(R.string.cant_remove_main_skill), getResources().getString(R.string.status_colon_failed));//default M or L or G

                                    } else if (cursorIndicator.getString(0).equals("2")) {//person2
                                        // Cursor result = db.getData("SELECT SUM(" + Database.COL_99_P2 + ") FROM " + Database.TABLE_NAME2 + " WHERE " + Database.COL_11_ID + "= '" + fromIntentPersonId + "'");
                                        Cursor result = db.getData("SELECT SUM(" + db.getColumnNameOutOf4Table(fromIntentPersonId, (byte) 7) + ") FROM " + db.tableNameOutOf4Table(fromIntentPersonId) + " WHERE " + db.getColumnNameOutOf4Table(fromIntentPersonId, (byte) 1) + "= '" + fromIntentPersonId + "'");
                                        result.moveToFirst();
                                        if (result.getInt(0) == 0) {//Means no data IN P2 so set null
                                            if (db.updateTable("UPDATE " + Database.TABLE_NAME_RATE_SKILL + " SET " + Database.COL_36_SKILL2 + "= NULL , " + Database.COL_33_R2 + "= NULL  , " + Database.COL_39_INDICATOR + "=" + 1 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'")) {
                                                displayResultAndRefresh(getResources().getString(R.string.no_data_present_so_removed), getResources().getString(R.string.status_colon_success));
                                            } else {
                                                Toast.makeText(IndividualPersonDetailActivity.this, getResources().getString(R.string.failed_to_update), Toast.LENGTH_LONG).show();
                                            }
                                        } else if (result.getInt(0) >= 1) {
                                            displayResultAndRefresh(getResources().getString(R.string.cant_remove), getResources().getString(R.string.because_data_is_present_newline_total_sum) + result.getInt(0));
                                        }
                                        if (result != null) result.close();

                                    } else if (cursorIndicator.getString(0).equals("3")) {//person3
                                        // Cursor result = db.getData("SELECT SUM(" + Database.COL_100_P3 + ") FROM " + Database.TABLE_NAME2 + " WHERE " + Database.COL_11_ID + "= '" + fromIntentPersonId + "'");
                                        Cursor result = db.getData("SELECT SUM(" + db.getColumnNameOutOf4Table(fromIntentPersonId, (byte) 8) + ") FROM " + db.tableNameOutOf4Table(fromIntentPersonId) + " WHERE " + db.getColumnNameOutOf4Table(fromIntentPersonId, (byte) 1) + "= '" + fromIntentPersonId + "'");
                                        result.moveToFirst();
                                        if (result.getInt(0) == 0) {//Means no data IN P2                                                                                          //decreasing indicator from 3 to 2
                                            if (db.updateTable("UPDATE " + Database.TABLE_NAME_RATE_SKILL + " SET " + Database.COL_37_SKILL3 + "= NULL , " + Database.COL_34_R3 + "= NULL  , " + Database.COL_39_INDICATOR + "=" + 2 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'")) {
                                                displayResultAndRefresh(getResources().getString(R.string.no_data_present_so_removed), getResources().getString(R.string.status_colon_success));
                                            } else {
                                                Toast.makeText(IndividualPersonDetailActivity.this, getResources().getString(R.string.failed_to_update), Toast.LENGTH_LONG).show();
                                            }
                                        } else if (result.getInt(0) >= 1) {
                                            displayResultAndRefresh(getResources().getString(R.string.cant_remove), getResources().getString(R.string.because_data_is_present_newline_total_sum) + result.getInt(0));
                                        }
                                        if (result != null) result.close();

                                    } else if (cursorIndicator.getString(0).equals("4")) {//person4
                                        // Cursor result = db.getData("SELECT SUM(" + Database.COL_1111_P4 + ") FROM " + Database.TABLE_NAME2 + " WHERE " + Database.COL_11_ID + "= '" + fromIntentPersonId + "'");
                                        Cursor result = db.getData("SELECT SUM(" + db.getColumnNameOutOf4Table(fromIntentPersonId, (byte) 9) + ") FROM " + db.tableNameOutOf4Table(fromIntentPersonId) + " WHERE " + db.getColumnNameOutOf4Table(fromIntentPersonId, (byte) 1) + "= '" + fromIntentPersonId + "'");
                                        result.moveToFirst();
                                        if (result.getInt(0) == 0) {//Means no data IN P2
                                            if (db.updateTable("UPDATE " + Database.TABLE_NAME_RATE_SKILL + " SET " + Database.COL_38_SKILL4 + "= NULL , " + Database.COL_35_R4 + "= NULL , " + Database.COL_39_INDICATOR + "=" + 3 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'")) {
                                                displayResultAndRefresh(getResources().getString(R.string.no_data_present_so_removed), getResources().getString(R.string.status_colon_success));
                                            } else {
                                                Toast.makeText(IndividualPersonDetailActivity.this, getResources().getString(R.string.failed_to_update), Toast.LENGTH_LONG).show();
                                            }
                                        } else if (result.getInt(0) >= 1) {
                                            displayResultAndRefresh(getResources().getString(R.string.cant_remove), getResources().getString(R.string.because_data_is_present_newline_total_sum) + result.getInt(0));
                                        }
                                        if (result != null) result.close();

                                    } else
                                        displayResultAndRefresh(getResources().getString(R.string.cant_remove_main_skill), getResources().getString(R.string.status_colon_failed));
                                } else
                                    Toast.makeText(IndividualPersonDetailActivity.this, "NO DATA IN CURSOR", Toast.LENGTH_SHORT).show();
                            }break;
                        }
                    }
                }catch (Exception x){
                    x.printStackTrace();
                }
                if(editOrNot[1]) {
                    customDialog.dismiss();//closing dialog to prevent window leak.whenever user select any option then editOrNot[1]=true; will be set.so if it is true then dismiss dialog before going to IndividualPersonDetailActivity.java from displayResult method
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
        //****************************************************done setting adapter for addOrRemoveMLG spinner*****************************************

        //***********************setting no of days and warning Total advance amount********************************************
        int rateArray[]=MyUtility.getRateArray(fromIntentPersonId,getBaseContext());//it should be here
        Cursor  advanceAmountCursor=db.getData("SELECT "+Database.COL_13_ADVANCE+" , "+Database.COL_14_BALANCE+" FROM " + Database.PERSON_REGISTERED_TABLE + " WHERE "+Database.COL_1_ID+"= '" + fromIntentPersonId +"'");
        advanceAmountCursor.moveToFirst();
        if(advanceAmountCursor.getInt(0) > 0) {//advance
            if(isAllRateSet(rateArray,indicator)){
                advanceOrBalanceText.setText(getString(R.string.advance_due));
                advanceOrBalanceWarring.setTextColor(Color.RED);
                advanceOrBalanceWarring.setText(advanceAmountCursor.getString(0));
                numberOfDaysToWorkSuggestion(indicator, noOfDaysToWork, advanceAmountCursor.getInt(0), rateArray);
            }else{//if there is no rate
                advanceOrBalanceWarring.setText(getString(R.string.set_rate));//if there is no rate than dont show advance or balance
                noOfDaysToWork.setText(getString(R.string.set_rate));//if there is no rate than dont show no. of days to work
            }

        }else if(advanceAmountCursor.getInt(1) >= 0 ){//balance
            noOfDaysToWork.setText("0");//if balance then show 0 days
            if(isAllRateSet(rateArray,indicator)) {
                advanceOrBalanceText.setText(getString(R.string.balance));
                advanceOrBalanceWarring.setTextColor(getColor(R.color.green));
                advanceOrBalanceWarring.setText(advanceAmountCursor.getString(1));
            }else{//if there is no rate
                advanceOrBalanceWarring.setText(getString(R.string.set_rate));//if there is no rate than dont show advance or balance
                noOfDaysToWork.setText(getString(R.string.set_rate));//if there is no rate than dont show no. of days to work
            }
        }
        advanceAmountCursor.close();
        //***********************done setting no of days and warring Total advance amount********************************************

        deposit_btn_tv.setOnClickListener(view -> {
            MyUtility.deletePdfOrRecordingUsingPathFromAppStorage(audioPath);//before going to other activity .delete Audio If Not user Saved
            Intent intent=new Intent(IndividualPersonDetailActivity.this,CustomizeLayoutOrDepositAmount.class);
            intent.putExtra("ID",fromIntentPersonId);
            customDialog.dismiss();//while going to other activity dismiss dialog otherwise window leak
            finish();//while going to other activity so destroy  this current activity so that while coming back we will see refresh activity
            startActivity(intent);
        });

        //to automatically set date to textView
        final Calendar current=Calendar.getInstance();//to get current date and time
          cYear=current.get(Calendar.YEAR);
          cMonth= (byte) current.get(Calendar.MONTH);
          cDayOfMonth= (byte) current.get(Calendar.DAY_OF_MONTH);
        inputDate.setText(current.get(Calendar.DAY_OF_MONTH)+"-"+(current.get(Calendar.MONTH)+1)+"-"+current.get(Calendar.YEAR));
        inputDate.setOnClickListener(view -> {
            //To show calendar dialog
            DatePickerDialog datePickerDialog=new DatePickerDialog(IndividualPersonDetailActivity.this, (datePicker, year, month, dayOfMonth) -> {
                inputDate.setText(dayOfMonth+"-"+(month+1)+"-"+year);//month start from 0 so 1 is added to get right month like 12
                cYear=year;
                cMonth= (byte) month;
                cDayOfMonth= (byte) dayOfMonth;
            },cYear,cMonth,cDayOfMonth);//This variable should be ordered this variable will set date day month to calendar to datePickerDialog so passing it
            datePickerDialog.show();
        });

        //initially every field will be invisible based on indicator others fields will be visible
        hardcodedP2.setVisibility(View.GONE);
        inputP2.setVisibility(View.GONE);
        hardcodedP3.setVisibility(View.GONE);
        inputP3.setVisibility(View.GONE);
        hardcodedP4.setVisibility(View.GONE);
        inputP4.setVisibility(View.GONE);

        //**************************************Setting skills*******************************************
        //CUSTOMIZATION: initially in person skill or type is M,L or G then according to that layout will be customised
        //hardcodedP1,inputP1 by default visible so no need to mention if(indicator == 1) {
        Cursor cursorDefault=db.getData("SELECT "+Database.COL_8_MAINSKILL1 +" FROM " + Database.PERSON_REGISTERED_TABLE + " WHERE "+Database.COL_1_ID+"= '" + fromIntentPersonId +"'");//for sure it will return type or skill
        cursorDefault.moveToFirst();//no need to check  cursorDefault !=null because for sure TYPE data is present
        hardcodedP1.setText(cursorDefault.getString(0));
        cursorDefault.close();

        Cursor skillsCursor=db.getData("SELECT "+Database.COL_36_SKILL2 +","+Database.COL_37_SKILL3 +","+Database.COL_38_SKILL4 +" FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE "+Database.COL_31_ID+"= '" + fromIntentPersonId +"'");
        if(skillsCursor != null) {
            skillsCursor.moveToFirst();
            if (indicator == 2) {//two person
                // hardcodedP1,inputP1 by default visible so no need to mention
                hardcodedP2.setVisibility(View.VISIBLE);
                inputP2.setVisibility(View.VISIBLE);
                hardcodedP2.setText(skillsCursor.getString(0));
            } else if (indicator == 3) {//three person
                //hardcodedP1,inputP1 by default visible so no need to mention
                hardcodedP2.setVisibility(View.VISIBLE);
                inputP2.setVisibility(View.VISIBLE);
                hardcodedP2.setText(skillsCursor.getString(0));

                hardcodedP3.setVisibility(View.VISIBLE);
                hardcodedP3.setText(skillsCursor.getString(1));
                inputP3.setVisibility(View.VISIBLE);
            } else if (indicator == 4) {////two person
                //hardcodedP1,inputP1 by default visible so no need to mention
                hardcodedP2.setVisibility(View.VISIBLE);
                inputP2.setVisibility(View.VISIBLE);
                hardcodedP2.setText(skillsCursor.getString(0));

                hardcodedP3.setVisibility(View.VISIBLE);
                hardcodedP3.setText(skillsCursor.getString(1));
                inputP3.setVisibility(View.VISIBLE);

                hardcodedP4.setVisibility(View.VISIBLE);
                hardcodedP4.setText(skillsCursor.getString(2));
                inputP4.setVisibility(View.VISIBLE);
            }
        }
        if (skillsCursor != null) {
            skillsCursor.close();
        }
        //**************************************done setting skills*******************************************
        save.setOnClickListener(view -> {
            save.setVisibility(View.GONE);//to avoid when user click multiple times
            //*********************************common to all indicator 1,2,3,4*******************
            VoiceRecorder.stopAudioPlayer();//if user playing audio and click save button then stop audio
            VoiceRecorder.stopRecording();//if user don't click tick button to save recording then while saving all data recording will also get saved automatically.so  VoiceRecorder.stopRecording()  method should be called then only file will be saved

            int p1,p2,p3,p4;//this default value is taken when user do enter date to filed
            p1=p2=p3=p4=0;
            int wages=0;
            String remarks=null;
            String micPath=null;
            String date=inputDate.getText().toString();//date will be inserted automatically

            //To get exact time so write code in save button
            String time = MyUtility.getOnlyTime();

            if(audioPath != null){//if file is not null then only it execute otherwise nothing will be inserted
                micPath=audioPath;
                correctInputArray[5]=1;//1 means data present
             }else
                correctInputArray[5]=0;// 0 means data not present

            if(!TextUtils.isEmpty(description.getText().toString().trim())){//to prevent null pointer exception
                remarks="["+time+getResources().getString(R.string.hyphen_entered)+"\n\n"+description.getText().toString().trim();//time is set automatically to remarks if user enter any remarks
                correctInputArray[6]=1;//means data present
            } else {//if user don't enter anything then time will set automatically
                remarks="["+time+getResources().getString(R.string.hyphen_entered);
                correctInputArray[6] = 0;
            }
            boolean isWrongData, isDataPresent;
              isWrongData= MyUtility.isEnterDataIsWrong(correctInputArray);
              isDataPresent= MyUtility.isDataPresent(correctInputArray);
            if(isDataPresent==true && isWrongData==false ) {//means if data is present then check is it right data or not .if condition is false then default value will be taken
                if (!TextUtils.isEmpty(toGiveWages.getText().toString().trim())) {//to prevent null pointer exception
                    wages = Integer.parseInt(toGiveWages.getText().toString().trim());
                }

                if(!TextUtils.isEmpty(inputP1.getText().toString().trim())) {//to prevent null pointer exception
                    p1 = Integer.parseInt(inputP1.getText().toString().trim());//converted to float and stored
                }
            }
            //*********************************  all the upper code are common to all indicator 1,2,3,4*******************
             if(indicator==1){
                if (isDataPresent == true && isWrongData == false) {//it is important means if data is present then check is it right data or not.if condition is false then this message will be displayed "Correct the Data or Cancel and Enter again"
                    //insert to database
                    if(!db.insertWagesOrDepositOnlyToActiveTableAndHistoryTableTransaction(fromIntentPersonId,MyUtility.systemCurrentDate24hrTime(),date, time, micPath, remarks, wages, p1, 0, 0, 0,GlobalConstants.WAGES_CODE.getValue())) {
                           Toast.makeText(IndividualPersonDetailActivity.this, getResources().getString(R.string.failed_to_insert), Toast.LENGTH_LONG).show();
                     }
                    refreshCurrentActivity(fromIntentPersonId);
                    customDialog.dismiss();

//                    if (success) {
//                        displayResult(wages + "          " + p1, "\nDATE- " + date + "\n\n" + "REMARKS- " + remarks + "\n\nMICPATH- " + micPath);
//                        customDialog.dismiss();//dialog will be dismiss after saved automatically
//                    }else
//                        Toast.makeText(IndividualPersonDetailActivity.this, "FAILED TO INSERT", Toast.LENGTH_LONG).show();
                } else//once user enter wrong data and left blank then user wound be able to save because array value would not be change it will be 2 so  user have to "Cancel and enter again" if use don't leave blank then it will save successfully
                    Toast.makeText(IndividualPersonDetailActivity.this, "CORRECT THE DATA or CANCEL AND ENTER AGAIN", Toast.LENGTH_LONG).show();

            }else if(indicator==2){
                //p1 is automatically added
                if(isDataPresent==true && isWrongData==false ) {
                    if (!TextUtils.isEmpty(inputP2.getText().toString().trim())) {//to prevent null pointer exception
                        p2 = Integer.parseInt(inputP2.getText().toString().trim());//converted to float and stored
                    }
                    //insert to database
                     if(!db.insertWagesOrDepositOnlyToActiveTableAndHistoryTableTransaction(fromIntentPersonId,MyUtility.systemCurrentDate24hrTime(), date, time, micPath, remarks, wages, p1, p2, 0, 0,  GlobalConstants.WAGES_CODE.getValue())) {
                            Toast.makeText(IndividualPersonDetailActivity.this, getResources().getString(R.string.failed_to_insert), Toast.LENGTH_LONG).show();
                       }
                    refreshCurrentActivity(fromIntentPersonId);
                    customDialog.dismiss();
//                    if (success) {
//                        displayResult(wages+"          "+p1+"     "+p2,"\nDATE- "+date+"\n\n"+"REMARKS- "+remarks+"\n\nMICPATH- "+micPath);
//                        customDialog.dismiss();//dialog will be dismiss after saved automatically
//                    } else
//                        Toast.makeText(IndividualPersonDetailActivity.this, "FAILED TO INSERT", Toast.LENGTH_LONG).show();
                }else
                    Toast.makeText(IndividualPersonDetailActivity.this, "CORRECT THE DATA or CANCEL AND ENTER AGAIN", Toast.LENGTH_LONG).show();

            }else if(indicator==3){
                if(isDataPresent==true && isWrongData==false ){
                    if (!TextUtils.isEmpty(inputP2.getText().toString().trim())) {//to prevent null pointer exception
                        p2 = Integer.parseInt(inputP2.getText().toString().trim());//converted to float and stored
                    }
                    if (!TextUtils.isEmpty(inputP3.getText().toString().trim())) {//to prevent null pointer exception
                        p3 = Integer.parseInt(inputP3.getText().toString().trim());//converted to float and stored
                    }
                    //insert to database
                    if(!db.insertWagesOrDepositOnlyToActiveTableAndHistoryTableTransaction(fromIntentPersonId,MyUtility.systemCurrentDate24hrTime(),date, time, micPath, remarks, wages, p1, p2, p3, 0,GlobalConstants.WAGES_CODE.getValue())) {
                            Toast.makeText(IndividualPersonDetailActivity.this, getResources().getString(R.string.failed_to_insert), Toast.LENGTH_LONG).show();
                     }
                    refreshCurrentActivity(fromIntentPersonId);
                    customDialog.dismiss();
//                    if (success) {
//                        displayResult(wages+"          "+p1+"     "+p2+"     "+p3,"\nDATE- "+date+"\n\n"+"REMARKS- "+remarks+"\n\nMICPATH- "+micPath);
//                        customDialog.dismiss();//dialog will be dismiss after saved automatically
//                    } else
//                        Toast.makeText(IndividualPersonDetailActivity.this, "FAILED TO INSERT", Toast.LENGTH_LONG).show();
                }else
                    Toast.makeText(IndividualPersonDetailActivity.this, "CORRECT THE DATA or CANCEL AND ENTER AGAIN", Toast.LENGTH_LONG).show();

            }else if(indicator==4) {
                if (isDataPresent == true && isWrongData == false) {
                    if (!TextUtils.isEmpty(inputP2.getText().toString().trim())) {//to prevent null pointer exception
                        p2 = Integer.parseInt(inputP2.getText().toString().trim());//converted to float and stored
                    }
                    if (!TextUtils.isEmpty(inputP3.getText().toString().trim())) {//to prevent null pointer exception
                        p3 = Integer.parseInt(inputP3.getText().toString().trim());//converted to float and stored
                    }
                    if (!TextUtils.isEmpty(inputP4.getText().toString().trim())) {//to prevent null pointer exception
                        p4 = Integer.parseInt(inputP4.getText().toString().trim());//converted to float and stored
                    }
                    //insert to database
                    if(!db.insertWagesOrDepositOnlyToActiveTableAndHistoryTableTransaction(fromIntentPersonId,MyUtility.systemCurrentDate24hrTime(),date, time, micPath, remarks, wages, p1, p2, p3, p4,GlobalConstants.WAGES_CODE.getValue())) {
                        Toast.makeText(IndividualPersonDetailActivity.this, getResources().getString(R.string.failed_to_insert), Toast.LENGTH_LONG).show();
                     }
                    refreshCurrentActivity(fromIntentPersonId);
                    customDialog.dismiss();
//                    if (success) {
//                        displayResult(wages+"          "+p1+"     "+p2+"     "+p3+"     "+p4,"\nDATE- "+date+"\n\n"+"REMARKS- "+remarks+"\n\nMICPATH- "+micPath);
//                        customDialog.dismiss();//dialog will be dismiss after saved automatically
//                    }else {
//                        Toast.makeText(IndividualPersonDetailActivity.this, "FAILED TO INSERT", Toast.LENGTH_LONG).show();
//                    }
                }else
                    Toast.makeText(IndividualPersonDetailActivity.this, "CORRECT THE DATA or CANCEL AND ENTER AGAIN", Toast.LENGTH_LONG).show();
            }
          audioPath =null;//since audio is saved then make this variable null otherwise audio will be deleted ON CANCEL OR ON DESTROY only if user don't enter save button
        });
        micIcon.setOnClickListener(view -> {
            if(MyUtility.checkAudioPermission(getBaseContext())){//checking for permission
                if (toggleToStartRecording) {//initially false

                    save.setVisibility(View.GONE);
                    deposit_btn_tv.setEnabled(false);

                    playAudioChronometer.setEnabled(false);//when user press save button then set to true playAudioChronometer.setEnabled(true);
                    saveAudio.setBackgroundResource(R.drawable.ic_green_sharp_done_sharp_tick_20);//changing tick color to green so that user can feel to press to save
                    micIcon.setEnabled(false);
                    micIcon.setBackgroundResource(R.drawable.baseline_record_voice_over_24);//change color when user click

                    VoiceRecorder voiceRecorder=new VoiceRecorder(fromIntentPersonId,getExternalFilesDir(null).toString());

                    if(voiceRecorder.startRecording()) {
                        playAudioChronometer.setBase(SystemClock.elapsedRealtime());//In Android, Chronometer is a class that implements a simple timer. Chronometer is a subclass of TextView. This class helps us to add a timer in our app.
                        playAudioChronometer.start();
                        Toast.makeText(IndividualPersonDetailActivity.this, getResources().getString(R.string.recording_started), Toast.LENGTH_LONG).show();
                         audioPath=voiceRecorder.getAudioAbsolutePath();//updating audioPath for further use otherwise it will be null
                        mediaRecorder=voiceRecorder.getMediaRecorder();//updating mediaRecorder for further use  otherwise it will be null
                    }else{
                        Toast.makeText(IndividualPersonDetailActivity.this, getResources().getString(R.string.failed_to_start_recording), Toast.LENGTH_SHORT).show();
                    }

                    IndividualPersonDetailActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //while the user is recording screen should be on. it should not close

                }else {//if recording is not started then stop
                    Toast.makeText(IndividualPersonDetailActivity.this, getResources().getString(R.string.again_tab_on_mic_to_start_recording), Toast.LENGTH_SHORT).show();
                }
                toggleToStartRecording = !toggleToStartRecording;//so that user should click 2 times to start recording
            }else {//request for permission
                Toast.makeText(IndividualPersonDetailActivity.this, getResources().getString(R.string.enable_audio_permission), Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(IndividualPersonDetailActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 21);
            }
        });
        playAudioChronometer.setOnClickListener(view -> {
            if(audioPath != null){//checking for null pointer Exception
                if(VoiceRecorder.audioPlayer(audioPath)){
                    Toast.makeText(IndividualPersonDetailActivity.this,getResources().getString(R.string.audio_playing),Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(IndividualPersonDetailActivity.this,getResources().getString(R.string.failed_to_play_audio),Toast.LENGTH_LONG).show();
                }
            }else
                Toast.makeText(IndividualPersonDetailActivity.this, getResources().getString(R.string.tab_on_mic_to_start_recording),Toast.LENGTH_SHORT).show();
        });
        saveAudio.setOnClickListener(view -> {
            if(mediaRecorder !=null ){
                if(!MyUtility.isEnterDataIsWrong(correctInputArray)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    save.setVisibility(View.VISIBLE);
                 }

                deposit_btn_tv.setEnabled(true);
                playAudioChronometer.setTextColor(getColor(R.color.green));//changing text color to green to give feel that is saved
                micIcon.setBackgroundResource(R.drawable.ic_green_sharp_mic_20);//set background image to cancel
                VoiceRecorder.stopRecording();//to save correct audio file in device stopRecording() method should be called then only file will ve saved
                playAudioChronometer.stop();//stopping chronometer
                micIcon.setEnabled(false);//so that user cannot press again this button
                saveAudio.setEnabled(false);//even this button user should not click again
                playAudioChronometer.setEnabled(true);//when audio is save then user will be able to play
            }else
                Toast.makeText(IndividualPersonDetailActivity.this, getResources().getString(R.string.tab_on_mic_to_start_recording), Toast.LENGTH_SHORT).show();
        });
        cancel.setOnClickListener(view -> {
            VoiceRecorder.stopAudioPlayer();//when audio is playing and   user click  cancel then stop audio also
            MyUtility.deletePdfOrRecordingUsingPathFromAppStorage(audioPath);//delete Audio If Not user Saved
            customDialog.dismiss();
        });
        customDialog.show();
        toGiveWages.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String amount=toGiveWages.getText().toString().trim();
                toGiveWages.setTextColor(Color.BLACK);
                correctInputArray[4]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data

                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(correctInputArray)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    save.setVisibility(View.VISIBLE);
                 }

                if(!(amount.matches("[0-9]+") || TextUtils.isEmpty(amount))){//no space or . or ,
                    //Toast.makeText(IndividualPersonDetailActivity.this, "NOT ALLOWED(space  .  ,  -)\nPlease Correct", Toast.LENGTH_LONG).show();
                    toGiveWages.setTextColor(Color.RED);
                    save.setVisibility(View.GONE);
                    correctInputArray[4]=2;//means wrong data
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
        inputP1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String p11= inputP1.getText().toString().trim();

                inputP1.setTextColor(Color.BLACK);
                correctInputArray[0]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data

                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(correctInputArray)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    save.setVisibility(View.VISIBLE);
                }
                if(!(p11.matches("[0-9]+") || TextUtils.isEmpty(p11))){//"[.]?[0-9]+[.]?[0-9]*" for float
                    inputP1.setTextColor(Color.RED);
                    save.setVisibility(View.GONE);
                    correctInputArray[0]=2;//means wrong data
                     //Toast.makeText(IndividualPersonDetailActivity.this, "NOT ALLOWED(space  .  ,  -)\nPLEASE CORRECT", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {//after text changed for suggestion calculate based on previous rate
                if(isAllRateSet(rateArray,indicator)){
                    MyUtility.p1_p2_p3_p4_Change_Tracker(correctInputArray, rateArray, inputP1, inputP2, inputP3, inputP4, runtimeSuggestionAmountToGive);
                }else runtimeSuggestionAmountToGive.setText(getString(R.string.set_rate));//if there is no rate then don't show suggestion
            }
        });
        inputP2.addTextChangedListener(new TextWatcher() {
           @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String p11= inputP2.getText().toString().trim();
                inputP2.setTextColor(Color.BLACK);
                correctInputArray[1]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data

                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(correctInputArray)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    save.setVisibility(View.VISIBLE);
                }

                if(!(p11.matches("[0-9]+") || TextUtils.isEmpty(p11))){// "[.]?[0-9]+[.]?[0-9]*"
                    inputP2.setTextColor(Color.RED);
                    save.setVisibility(View.GONE);
                    correctInputArray[1]=2;//means wrong data
                   // Toast.makeText(IndividualPersonDetailActivity.this, "NOT ALLOWED(space  .  ,  -)\nPLEASE CORRECT", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                if(isAllRateSet(rateArray,indicator)){
                    MyUtility.p1_p2_p3_p4_Change_Tracker(correctInputArray, rateArray, inputP1, inputP2, inputP3, inputP4, runtimeSuggestionAmountToGive);
                }else runtimeSuggestionAmountToGive.setText(getString(R.string.set_rate));//if there is no rate then don't show suggestion
            }
        });
        inputP3.addTextChangedListener(new TextWatcher() {
             @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String p11= inputP3.getText().toString().trim();
                inputP3.setTextColor(Color.BLACK);
                correctInputArray[2]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data


                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(correctInputArray)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    save.setVisibility(View.VISIBLE);
                }

                if(!(p11.matches("[0-9]+") || TextUtils.isEmpty(p11))){//space or , or - is restricted
                    inputP3.setTextColor(Color.RED);
                    save.setVisibility(View.GONE);
                    correctInputArray[2]=2;//means wrong data
                   // Toast.makeText(IndividualPersonDetailActivity.this, "NOT ALLOWED(space  .  ,  -)\nPLEASE CORRECT", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                if(isAllRateSet(rateArray,indicator)){
                    MyUtility.p1_p2_p3_p4_Change_Tracker(correctInputArray, rateArray, inputP1, inputP2, inputP3, inputP4, runtimeSuggestionAmountToGive);
                }else runtimeSuggestionAmountToGive.setText(getString(R.string.set_rate));//if there is no rate then don't show suggestion
            }
        });
        inputP4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String p11= inputP4.getText().toString().trim();
                inputP4.setTextColor(Color.BLACK);
                correctInputArray[3]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data
                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(correctInputArray)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    save.setVisibility(View.VISIBLE);
                }
                if(!(p11.matches("[0-9]+") || TextUtils.isEmpty(p11))){//space or , or - is restricted
                    inputP4.setTextColor(Color.RED);
                    save.setVisibility(View.GONE);
                    correctInputArray[3]=2;//means wrong data
                    //Toast.makeText(IndividualPersonDetailActivity.this, "NOT ALLOWED(space  .  ,  -)\nPLEASE CORRECT", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                if(isAllRateSet(rateArray,indicator)){
                    MyUtility.p1_p2_p3_p4_Change_Tracker(correctInputArray, rateArray, inputP1, inputP2, inputP3, inputP4, runtimeSuggestionAmountToGive);
                }else runtimeSuggestionAmountToGive.setText(getString(R.string.set_rate));//if there is no rate then don't show suggestion
            }
        });
    }
    private boolean isAllRateSet(int[] rateArray, int indicator) {
        if((indicator >=1 && indicator<=4) && rateArray != null){
            if((indicator == 1 && rateArray[0] != 0) || (indicator == 2 && rateArray[0] != 0 && rateArray[1] != 0) || (indicator == 3 && rateArray[0] != 0 && rateArray[1] != 0 && rateArray[2] != 0) || (indicator == 4 && rateArray[0] != 0 && rateArray[1] != 0 && rateArray[2] != 0 && rateArray[3] != 0)){
                return true;//if all rate is set
            }
        }
       return false;//if there is no rate and incorrect parameters
    }

    private void numberOfDaysToWorkSuggestion(int indicator, TextView noOfDaysToWork,int advance,int []rateArray) {
        if(indicator==1) {
            if(rateArray[0] !=0) {//to avoid arithmetic exception 1/0
                //total advance / r1
                noOfDaysToWork.setText(String.valueOf((advance / rateArray[0])+1));//1 is added to get right days because we are not considering point values when dividing
            }
        }else if(indicator==2){//to avoid arithmetic exception 1/0
            if(rateArray[0]+rateArray[1] != 0) {
                //                                         total advance/(R1+R1)
                noOfDaysToWork.setText(String.valueOf((advance / (rateArray[0]+rateArray[1]))+1));//1 is added to get right days because we are not considering point values when dividing
            }
        }else if(indicator==3){//to avoid arithmetic exception 1/0
            if(rateArray[0]+rateArray[1]+rateArray[2] != 0) {
                //                                               total advance/(R1+R2+R3)
                noOfDaysToWork.setText(String.valueOf((advance / (rateArray[0]+rateArray[1]+rateArray[2]))+1));//1 is added to get right days because we are not considering point values when dividing
            }
        }else if(indicator==4){//to avoid arithmetic exception 1/0
            if(rateArray[0]+rateArray[1]+rateArray[2]+rateArray[3] != 0) {
                //                                                total advance/(R1+R2+R3+R4)
                noOfDaysToWork.setText(String.valueOf((advance / (rateArray[0]+rateArray[1]+rateArray[2]+rateArray[3]))+1));//1 is added to get right days because we are not considering point values when dividing
            }
        }
    }
    public void refreshCurrentActivity(String id) {
        Intent intent=new Intent(IndividualPersonDetailActivity.this,IndividualPersonDetailActivity.class);
        intent.putExtra("ID",id);
        finish();//while going to other activity so destroy  this current activity so that while coming back we will see refresh activity
        startActivity(intent);
    }
    public void showDialogAsMessage( String query,String ifTitle,String ifMessage,String elseTitle,String elseMessage){
        if(db.updateTable(query)){
            displayResultAndRefresh(ifTitle,ifMessage);
        }else{
            displayResultAndRefresh(elseTitle, elseMessage);
        }
    }
    private void displayResultAndRefresh(String title, String message)  {
        AlertDialog.Builder showDataFromDataBase=new AlertDialog.Builder(IndividualPersonDetailActivity.this);
        showDataFromDataBase.setCancelable(false);
        showDataFromDataBase.setTitle(title);
        showDataFromDataBase.setMessage(message);
        showDataFromDataBase.setPositiveButton(getResources().getString(R.string.ok),(dialogInterface, i) -> {
            dialogInterface.dismiss();//close current dialog
            refreshCurrentActivity(fromIntentPersonId);
//            Intent intent=new Intent(IndividualPersonDetailActivity.this,IndividualPersonDetailActivity.class);
//            intent.putExtra("ID",fromIntentPersonId);
//            finish();//while going to other activity so destroy  this current activity so that while coming back we will see refresh activity
//            startActivity(intent);
        });
        showDataFromDataBase.create().show();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(adapterDialog!=null){
            adapterDialog.dismiss();//dialog will be close when adapter is destroyed
            adapterDialog=null;
        }
        MyUtility.deletePdfOrRecordingUsingPathFromAppStorage(audioPath);//delete Audio If Not user Saved
        Database.closeDatabase();
    }
}