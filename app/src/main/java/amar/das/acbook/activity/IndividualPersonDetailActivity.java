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
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.util.Log;
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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;

import amar.das.acbook.Database;
import amar.das.acbook.R;
import amar.das.acbook.adapters.WagesDetailsAdapter;
import amar.das.acbook.databinding.ActivityIndividualPersonDetailBinding;
import amar.das.acbook.model.WagesDetailsModel;
import amar.das.acbook.pdfgenerator.MakePdf;
import amar.das.acbook.voicerecording.VoiceRecorder;
import amar.das.acbook.utility.MyUtility;

public class IndividualPersonDetailActivity extends AppCompatActivity {
     ActivityIndividualPersonDetailBinding binding;
    MediaRecorder mediaRecorder;
    String audioPath;
    boolean toggleToStartRecording=false;
    private String fromIntentPersonId;
    Database db;
    int []arr=new int[7];
    String active ="0";
    byte redIndicatorToLeave=21;//if person will leave in 50 days so when 21 days 3 weeks left to leave then their name back ground color will change to red which indicate person is about to leave in 21 days so that wages can be given according to that
    ArrayList<WagesDetailsModel> dataList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0); //we have used overridePendingTransition(), it is used to remove activity create animation while re-creating activity.
        binding = ActivityIndividualPersonDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getIntent().hasExtra("ID")) {//every operation will be perform based on id
            db = new Database(this);//on start only database should be create
            fromIntentPersonId = getIntent().getStringExtra("ID");//getting data from intent

            //***********setting skill top of layout**********************************************
            Cursor defaultSkillCursor=db.getData("SELECT "+Database.COL_8_MAINSKILL1 +" FROM " + Database.TABLE_NAME1 + " WHERE "+Database.COL_1_ID+"= '" + fromIntentPersonId +"'");//for sure it will return type or skill
               defaultSkillCursor.moveToFirst();
               binding.defaultHardcodedTv.setText(defaultSkillCursor.getString(0));
               binding.defaultSkillTextTv.setText(defaultSkillCursor.getString(0) +"  =");//default calculation skill
               defaultSkillCursor.close();

             //Cursor sumCursor=db.getData("SELECT SUM("+Database.COL_26_WAGES+"),SUM("+Database.COL_28_P1+"),SUM("+Database.COL_29_P2+"),SUM("+Database.COL_291_P3+"),SUM("+Database.COL_292_P4+"),SUM("+Database.COL_27_DEPOSIT+") FROM "+Database.TABLE_NAME2+" WHERE "+Database.COL_21_ID+"= '"+fromIntentPersonId +"'");
             Cursor sumCursor=db.getSumOfWagesP1P2P3P4Deposit(fromIntentPersonId);
             sumCursor.moveToFirst();

             if(sumCursor.getInt(0) < 0)//if total wages amount cross the  range of int the this message will be shown
                 Toast.makeText(this, getResources().getString(R.string.value_out_of_range_please_check_total_wages), Toast.LENGTH_LONG).show();

             binding.blueTotalWagesTv.setText(MyUtility.convertToIndianNumberSystem(sumCursor.getLong(0)));
             binding.blueTotalp1Tv.setText(sumCursor.getString(1));
             binding.totalP1CountTv.setText(sumCursor.getString(1));
                    //sum deposit
             if(sumCursor.getString(5) != null) {//if there is deposit then set visibility visible or else layout visibility GONE
                 binding.totalDepositAmountTv.setText("= " + MyUtility.convertToIndianNumberSystem(sumCursor.getLong(5)));
             }else
                 binding.totalDepositAmountLayout.setVisibility(View.GONE);

            Cursor skillNRateCursor=db.getData("SELECT "+Database.COL_36_SKILL2 +","+Database.COL_37_SKILL3 +","+Database.COL_38_SKILL4 +","+Database.COL_32_R1+","+Database.COL_33_R2+","+Database.COL_34_R3+","+Database.COL_35_R4+" FROM " + Database.TABLE_NAME3 + " WHERE "+Database.COL_31_ID+"= '" + fromIntentPersonId +"'");
            if(skillNRateCursor != null) {
                skillNRateCursor.moveToFirst();
                int indicate = MyUtility.get_indicator(getBaseContext(),fromIntentPersonId);
                                //R1
                if(skillNRateCursor.getInt(3) != 0) {
                                                    //R1
                    binding.p1RateTv.setText(skillNRateCursor.getString(3));//default skill
                                                                       //    R1 * p1
                    binding.totalP1AmountTv.setText("= "+MyUtility.convertToIndianNumberSystem(skillNRateCursor.getInt(3)*sumCursor.getInt(1)));//default skill
                }else {
                    binding.totalP1AmountTv.setText(getResources().getString(R.string.equal_new_person_provide_rate));//default skill
                }
                               //total wages
                if(sumCursor.getString(0) !=null){//if total wages is not null then set total wages
                    binding.wagesTotalAmountTv.setText(MyUtility.convertToIndianNumberSystem(sumCursor.getLong(0)));//total wages set
                }
                   //by default= deposit,totalP2CountTv,defaultHardcodedTv,defaultSkillTextTv,p1RateTv,totalP1AmountTv is set automatically
                     if(indicate==1) {
                         indicator1234CalculateAndUpdate(sumCursor,skillNRateCursor.getInt(3) * sumCursor.getInt(1),0,0,0);
                     }

                binding.p2Layout.setVisibility(View.GONE);//initially invisible according to indicator it will customize
                binding.p3Layout.setVisibility(View.GONE);//initially invisible according to indicator it will customize
                binding.p4Layout.setVisibility(View.GONE);//initially invisible according to indicator it will customize

                if(indicate == 2) {
                    if(skillNRateCursor.getInt(4) != 0) {
                        //R1
                        binding.p2RateTv.setText(skillNRateCursor.getString(4));
                        //    R2 * p2
                        binding.totalP2AmountTv.setText("= "+MyUtility.convertToIndianNumberSystem(skillNRateCursor.getInt(4)*sumCursor.getInt(2)));
                    }else {
                        binding.totalP2AmountTv.setText(getResources().getString(R.string.equal_new_person_provide_rate));
                        //Toast.makeText(this, "Long press FINAL TOTAL button to  provide rate", Toast.LENGTH_LONG).show();
                    }

                    binding.totalP2CountTv.setText(sumCursor.getString(2));//total p2 count
                    binding.skill1TextTv.setText(skillNRateCursor.getString(0)+"  =");//setting skill 1
                    binding.hardcoded1Tv.setText(skillNRateCursor.getString(0));
                    binding.blueTotalp2Tv.setText(sumCursor.getString(2));
                    binding.p2Layout.setVisibility(View.VISIBLE);

                    indicator1234CalculateAndUpdate(sumCursor,skillNRateCursor.getInt(3) * sumCursor.getInt(1),skillNRateCursor.getInt(4) * sumCursor.getInt(2),0,0);

                } else if (indicate == 3) {
                    if(skillNRateCursor.getInt(4) != 0) {
                        binding.p2RateTv.setText(skillNRateCursor.getString(4));
                                                                                //    R2 * p2
                        binding.totalP2AmountTv.setText("= "+MyUtility.convertToIndianNumberSystem(skillNRateCursor.getInt(4)*sumCursor.getInt(2)));
                    }else {
                        binding.totalP2AmountTv.setText(getResources().getString(R.string.equal_new_person_provide_rate));
                     }

                    if(skillNRateCursor.getInt(5) != 0) {
                        binding.p3RateTv.setText(skillNRateCursor.getString(5));
                                                                                 //    R3 * p3
                        binding.totalP3AmountTv.setText("= "+MyUtility.convertToIndianNumberSystem(skillNRateCursor.getInt(5)*sumCursor.getInt(3)));
                    }else {
                        binding.totalP3AmountTv.setText(getResources().getString(R.string.equal_new_person_provide_rate));
                        //Toast.makeText(this, "Long press FINAL TOTAL button to  provide rate", Toast.LENGTH_LONG).show();
                    }
                    binding.totalP2CountTv.setText(sumCursor.getString(2));//total p2 count
                    binding.totalP3CountTv.setText(sumCursor.getString(3));//total p3 count
                    binding.skill1TextTv.setText(skillNRateCursor.getString(0)+"  =");//setting skill 1
                    binding.skill2TextTv.setText(skillNRateCursor.getString(1)+"  =");//setting skill 2
                    binding.hardcoded1Tv.setText(skillNRateCursor.getString(0));
                    binding.blueTotalp2Tv.setText(sumCursor.getString(2));
                    binding.hardcoded2Tv.setText(skillNRateCursor.getString(1));
                    binding.blueTotalp3Tv.setText(sumCursor.getString(3));
                    binding.p2Layout.setVisibility(View.VISIBLE);
                    binding.p3Layout.setVisibility(View.VISIBLE);

                    indicator1234CalculateAndUpdate(sumCursor,skillNRateCursor.getInt(3) * sumCursor.getInt(1),skillNRateCursor.getInt(4) * sumCursor.getInt(2),skillNRateCursor.getInt(5) * sumCursor.getInt(3),0);

                }else if(indicate == 4) {
                    if(skillNRateCursor.getInt(4) != 0) {
                        binding.p2RateTv.setText(skillNRateCursor.getString(4));
                        //    R2 * p2
                        binding.totalP2AmountTv.setText("= "+MyUtility.convertToIndianNumberSystem(skillNRateCursor.getInt(4)*sumCursor.getInt(2)));
                    }else{
                        binding.totalP2AmountTv.setText(getResources().getString(R.string.equal_new_person_provide_rate));
                     }

                    if(skillNRateCursor.getInt(5) != 0) {
                        binding.p3RateTv.setText(skillNRateCursor.getString(5));
                        //    R3 * p3
                        binding.totalP3AmountTv.setText("= "+MyUtility.convertToIndianNumberSystem(skillNRateCursor.getInt(5)*sumCursor.getInt(3)));
                    }else{
                        binding.totalP3AmountTv.setText(getResources().getString(R.string.equal_new_person_provide_rate));
                    }

                    if(skillNRateCursor.getInt(6) != 0) {
                        binding.p4RateTv.setText(skillNRateCursor.getString(6));
                        //    R4 * p4
                        binding.totalP4AmountTv.setText("= "+MyUtility.convertToIndianNumberSystem(skillNRateCursor.getInt(6)*sumCursor.getInt(4)));
                    }else{
                        binding.totalP4AmountTv.setText(getResources().getString(R.string.equal_new_person_provide_rate));
                    }

                    binding.totalP2CountTv.setText(sumCursor.getString(2));//total p2 count
                    binding.totalP3CountTv.setText(sumCursor.getString(3));//total p3 count
                    binding.totalP4CountTv.setText(sumCursor.getString(4));//total p4 count
                    binding.skill1TextTv.setText(skillNRateCursor.getString(0)+"  =");//setting skill 1
                    binding.skill2TextTv.setText(skillNRateCursor.getString(1)+"  =");//setting skill 2
                    binding.skill3TextTv.setText(skillNRateCursor.getString(2)+"  =");//setting skill 3
                    binding.hardcoded1Tv.setText(skillNRateCursor.getString(0));
                    binding.blueTotalp2Tv.setText(sumCursor.getString(2));
                    binding.hardcoded2Tv.setText(skillNRateCursor.getString(1));
                    binding.blueTotalp3Tv.setText(sumCursor.getString(3));
                    binding.hardcoded3Tv.setText(skillNRateCursor.getString(2));
                    binding.blueTotalp4Tv.setText(sumCursor.getString(4));
                    binding.p2Layout.setVisibility(View.VISIBLE);
                    binding.p3Layout.setVisibility(View.VISIBLE);
                    binding.p4Layout.setVisibility(View.VISIBLE);
                    indicator1234CalculateAndUpdate(sumCursor,skillNRateCursor.getInt(3) * sumCursor.getInt(1),skillNRateCursor.getInt(4) * sumCursor.getInt(2),skillNRateCursor.getInt(5) * sumCursor.getInt(3),skillNRateCursor.getInt(6) * sumCursor.getInt(4));
                }
            }
            if (skillNRateCursor != null) {
                skillNRateCursor.close();
            }
            sumCursor.close();

            //***********Done setting skill***********************************************
            //*******************Recycler view********************************************
             // Cursor allDataCursor=db.getData("SELECT "+Database.COL_22_DATE+","+Database.COL_24_MICPATH+","+Database.COL_25_DESCRIPTION+","+Database.COL_26_WAGES+","+Database.COL_27_DEPOSIT+","+Database.COL_28_P1+","+Database.COL_29_P2+","+Database.COL_291_P3+","+Database.COL_292_P4+","+Database.COL_21_ID+","+Database.COL_23_TIME+","+Database.COL_293_ISDEPOSITED+" FROM "+Database.TABLE_NAME2+" WHERE "+Database.COL_21_ID+"='"+fromIntentPersonId+"'");
             Cursor allDataCursor=db.getWagesDepositDataForRecyclerView(fromIntentPersonId);
              dataList=new ArrayList<>();
              if(allDataCursor!=null) {
                  while (allDataCursor.moveToNext()) {
                      WagesDetailsModel model = new WagesDetailsModel();
                      model.setDate(allDataCursor.getString(0));
                      model.setMicPath(allDataCursor.getString(1));
                      model.setDescription(allDataCursor.getString(2));
                      model.setWages(allDataCursor.getInt(3));
                      model.setDeposit(allDataCursor.getInt(4));
                      model.setP1(allDataCursor.getInt(5));
                      model.setP2(allDataCursor.getInt(6));
                      model.setP3(allDataCursor.getInt(7));
                      model.setP4(allDataCursor.getInt(8));
                      model.setId(allDataCursor.getString(9));
                      model.setTime(allDataCursor.getString(10));
                      model.setIsdeposited((allDataCursor.getString(11)));
                      dataList.add(model);
                  }
                  allDataCursor.close();
                  WagesDetailsAdapter wagesDetailsAdapter = new WagesDetailsAdapter(this, dataList);

                  binding.singleRecordRecy.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
                  binding.singleRecordRecy.setAdapter(wagesDetailsAdapter);
                  binding.singleRecordRecy.scrollToPosition(dataList.size() - 1);//this will scroll recycler view to last position automatically
              }else {
                  Toast.makeText(this, "NO DATA IN allDataCursor CURSOR", Toast.LENGTH_LONG).show();
              }
            //*******************done Recycler view********************************************
            //retrieving data from db
            Cursor cursor = db.getData("SELECT "+Database.COL_2_NAME+","+Database.COL_3_BANKAC+","+Database.COL_4_IFSCCODE+","+Database.COL_5_BANKNAME+","+Database.COL_6_AADHAAR_NUMBER+","+Database.COL_7_ACTIVE_PHONE1+","+Database.COL_9_ACCOUNT_HOLDER_NAME+","+Database.COL_10_IMAGE+","+Database.COL_11_ACTIVE_PHONE2+","+Database.COL_1_ID+" FROM " + Database.TABLE_NAME1 + " WHERE "+Database.COL_1_ID+"='" + fromIntentPersonId + "'");
            if (cursor != null) {
                cursor.moveToFirst();
                binding.nameTv.setText(cursor.getString(0));
                binding.accountTv.setText(HtmlCompat.fromHtml("A/C-  " + "<b>" + cursor.getString(1) + "</b>",HtmlCompat.FROM_HTML_MODE_LEGACY));
                binding.ifscCodeTv.setText("IFSC-  " + cursor.getString(2));
                binding.bankNameTv.setText("BANK- " + cursor.getString(3));
                binding.aadharTv.setText(HtmlCompat.fromHtml("AADHAAR CARD-  " + "<b>" + cursor.getString(4) + "</b>",HtmlCompat.FROM_HTML_MODE_LEGACY));
                binding.phoneTv.setText("ACTIVE PHONE1-  " + cursor.getString(5));
                binding.acHolderNameTv.setText("A/C HOLDER- " + cursor.getString(6));

                if (cursor.getString(5).length() == 10 || MyUtility.getActivePhoneNumbersFromDb(fromIntentPersonId,getBaseContext()) != null) {//if there is no phone number then show default icon color black else green icon
                    binding.callTv.setBackgroundResource(R.drawable.ic_outline_call_24);
                }

                byte[] image = cursor.getBlob(7);//getting image from db as blob
                //getting bytearray image from DB and converting  to bitmap to set in imageview
                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                binding.imageImg.setImageBitmap(bitmap);

                binding.acHolderTv.setText("PHONE2- " + cursor.getString(8));
                binding.idTv.setText("ID- " + cursor.getString(9));
            } else {
                Toast.makeText(this, "NO DATA IN CURSOR", Toast.LENGTH_LONG).show();
            }
          //setting star rating
            Cursor cursor2 = db.getData("SELECT "+Database.COL_391_STAR +","+Database.COL_392_LEAVINGDATE+" FROM " + Database.TABLE_NAME3 + " WHERE "+Database.COL_31_ID+"='" + fromIntentPersonId + "'");
            cursor2.moveToFirst();
            if(cursor2.getString(0) != null || cursor2.getString(1) != null) {

                if(cursor2.getString(1) != null){//https://www.youtube.com/watch?v=VmhcvoenUl0
                    LocalDate dbDate, todayDate = LocalDate.now();//current date; return 2022-05-01
                    String[] dateArray = cursor2.getString(1).split("-");
//                    d = Integer.parseInt(dateArray[0]);
//                    m = Integer.parseInt(dateArray[1]);
//                    y = Integer.parseInt(dateArray[2]);//dbDate is leaving date
                    dbDate = LocalDate.of(Integer.parseInt(dateArray[2]),Integer.parseInt(dateArray[1]),Integer.parseInt(dateArray[0]));//it convert 2022-05-01 it add 0 automatically
                    //between (2022-05-01,2022-05-01) like
                   // Toast.makeText(contex, ""+ ChronoUnit.DAYS.between(todayDate,dbDate)+" DAYS LEFT TO LEAVE", Toast.LENGTH_SHORT).show();//HERE dbDate will always be higher then todayDate because user will leave in forward date so in method Chrono Unit todayDate is written first and second dbDate to get right days
                                                     //between (2022-05-01,2022-05-01) like
                    binding.starRatingTv.setText(ChronoUnit.DAYS.between(todayDate,dbDate)+" "+getResources().getString(R.string.days_left));//HERE dbDate will always be higher then todayDate because user will leave in forward date so in method Chrono Unit todayDate is written first and second dbDate to get right days

                    if(ChronoUnit.DAYS.between(todayDate,dbDate) <= redIndicatorToLeave){
                       binding.leavingOrNotColorIndicationLayout.setBackgroundColor(Color.RED);//red color indicate person going to leave within 3 weeks
                   }
                }else{
                    binding.starRatingTv.setText(cursor2.getString(0) + " *");
                }

            }else {
                binding.starRatingTv.setText("0 *");//if user has never press save button on Meta data then by default 0* will be shown
            }
            cursor2.close();
            //Meta data
            binding.infoTv.setOnClickListener(view ->{
                final boolean[] editOrNot = {false,false};
                AlertDialog.Builder myCustomDialog=new AlertDialog.Builder(IndividualPersonDetailActivity.this);
                LayoutInflater inflater=LayoutInflater.from(IndividualPersonDetailActivity.this);

                View myView=inflater.inflate(R.layout.meta_data,null);//myView contain all layout view ids
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
                int []checkCorrectionArray = new int[indicator];
                int []userInputRateArray=new int[indicator];
                rateUpdateManually(hardcodedP1Tv,inputP1Et,hardcodedP2Tv,inputP2Et,hardcodedP3Tv,inputP3Et,hardcodedP4Tv,inputP4Et,infoSave,checkCorrectionArray,userInputRateArray,indicator,fromIntentPersonId);
                //--------------------------------------------------------------------------------------------------------------------------
                Cursor cursor1 = db.getData("SELECT "+Database.COL_12_ACTIVE+" FROM " + Database.TABLE_NAME1 + " WHERE "+Database.COL_1_ID+"='" + fromIntentPersonId + "'");
                cursor1.moveToFirst();
                if(cursor1.getString(0).equals("1"))
                    activeRadio.setVisibility(View.GONE);//when it is active then don't show to activate
                else if(cursor1.getString(0).equals("0"))
                    activeRadio.setChecked(false);

                //this should not be use in other class   other wise it will not be called when user change radio button
                radioGroup.setOnCheckedChangeListener((radioGroup1, checkedIdOfRadioBtn) -> {
                    if (checkedIdOfRadioBtn == R.id.active_metadata) {
                        active = "1";//updating active variable
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
                Cursor cursor21 = db.getData("SELECT "+Database.COL_391_STAR +","+Database.COL_392_LEAVINGDATE+","+Database.COL_393_REFFERAL_REMARKS+" , "+Database.COL_397_TOTAL_WORKED_DAYS+" , "+Database.COL_398_RETURNINGDATE+" FROM " + Database.TABLE_NAME3 + " WHERE "+Database.COL_31_ID+"='" + fromIntentPersonId + "'");
                cursor21.moveToFirst();

                //total worked days
                totalWorkDaysMetadata.setText(cursor21.getString(3));

                String[] ratingStar =getResources().getStringArray(R.array.star);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(IndividualPersonDetailActivity.this, android.R.layout.simple_list_item_1,ratingStar);
                starSpinner.setAdapter(adapter);
                if(cursor21.getString(0) != null){//rating star
                    int spinnerPosition = adapter.getPosition(cursor21.getString(0));
                    starSpinner.setSelection(spinnerPosition);
                }else if(cursor21.getString(0) == null){
                    int spinnerPosition = adapter.getPosition("1");//1 star by default
                    starSpinner.setSelection(spinnerPosition);
                }

                if(cursor21.getString(1) != null){//leaving date
                    leavingDateTv.setText(cursor21.getString(1));
                }else if(cursor21.getString(1) == null) {
                    leavingDateTv.setText("");
                }

                if(cursor21.getString(4) != null){//leaving date
                    returningDate.setText(cursor21.getString(4));
                }else if(cursor21.getString(4) == null) {
                    returningDate.setText("");
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
                try(Cursor locationAndReligionCursor=db.getData("SELECT "+Database.COL_17_LOCATION+","+Database.COL_18_RELIGION+" FROM "+Database.TABLE_NAME1 +" WHERE "+Database.COL_1_ID+"='"+fromIntentPersonId+"'")) {//to close cursor automatically
                    locationAndReligionCursor.moveToFirst();//because we get only 1 row
                    locationAutoComplete.setText(locationAndReligionCursor.getString(0));//set data
                    religionAutoComplete.setText(locationAndReligionCursor.getString(1));//set data
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

                if(cursor21.getString(2) != null){//remarksMetaData
                    remarksMetaData.setText(cursor21.getString(2));
                }
                cursor21.close();

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
                        Cursor cursor1 =db.getData("SELECT "+Database.COL_36_SKILL2 +","+Database.COL_37_SKILL3 +","+Database.COL_38_SKILL4 +" FROM " + Database.TABLE_NAME3 + " WHERE "+Database.COL_31_ID+"= '" + fromIntentPersonId +"'")) {
                            cursor1.moveToFirst();//skill which is null there skill is updated
                            switch (data) {
                                case "ADD L": //adding L means p2
                                {
                                    editOrNot[1] = true;//indicate user has selected option

                                    if (cursor1.getString(0) == null) {
                                        showDialogAsMessage("UPDATE " + Database.TABLE_NAME3 + " SET " + Database.COL_36_SKILL2 + "='" + getResources().getString(R.string.laber) + "' , " + Database.COL_39_INDICATOR + "=" + 2 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'", "SUCCESSFULLY  ADDED L", "STATUS: SUCCESS", "FAILED TO ADD L", "STATUS: FAILED");

                                    } else if (cursor1.getString(1) == null) {
                                        showDialogAsMessage("UPDATE " + Database.TABLE_NAME3 + " SET " + Database.COL_37_SKILL3 + "='" + getResources().getString(R.string.laber) + "' , " + Database.COL_39_INDICATOR + "=" + 3 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'", "SUCCESSFULLY ADDED L", "STATUS: SUCCESS", "FAILED TO ADD L", "STATUS: FAILED");

                                    } else if (cursor1.getString(2) == null) {
                                        showDialogAsMessage("UPDATE " + Database.TABLE_NAME3 + " SET " + Database.COL_38_SKILL4 + "='" + getResources().getString(R.string.laber) + "' , " + Database.COL_39_INDICATOR + "=" + 4 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'", "SUCCESSFULLY  ADDED L", "STATUS: SUCCESS", "FAILED TO  ADD L", "STATUS: FAILED");

                                    } else
                                        displayResult(getResources().getString(R.string.only_4_person_allowed_to_add), getResources().getString(R.string.status_cant_add_more) + getResources().getString(R.string.laber));
                                }
                                break;
                                case "ADD M": //adding M p3
                                {
                                    editOrNot[1] = true;//indicate user has selected option
                                    if (cursor1.getString(0) == null) {
                                        showDialogAsMessage("UPDATE " + Database.TABLE_NAME3 + " SET " + Database.COL_36_SKILL2 + "='" + getResources().getString(R.string.mestre) + "' , " + Database.COL_39_INDICATOR + "=" + 2 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'", "SUCCESSFULLY  ADDED M", "STATUS: SUCCESS", "FAILED TO ADD M", "STATUS: FAILED");

                                    } else if (cursor1.getString(1) == null) {
                                        showDialogAsMessage("UPDATE " + Database.TABLE_NAME3 + " SET " + Database.COL_37_SKILL3 + "='" + getResources().getString(R.string.mestre) + "' , " + Database.COL_39_INDICATOR + "=" + 3 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'", "SUCCESSFULLY ADDED M", "STATUS: SUCCESS", "FAILED TO ADD M", "STATUS: FAILED");

                                    } else if (cursor1.getString(2) == null) {
                                        showDialogAsMessage("UPDATE " + Database.TABLE_NAME3 + " SET " + Database.COL_38_SKILL4 + "='" + getResources().getString(R.string.mestre) + "' , " + Database.COL_39_INDICATOR + "=" + 4 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'", "SUCCESSFULLY  ADDED M", "STATUS: SUCCESS", "SUCCESSFULLY  ADD M", "STATUS: SUCCESS");

                                    } else
                                        displayResult(getResources().getString(R.string.only_4_person_allowed_to_add), getResources().getString(R.string.status_cant_add_more) + getResources().getString(R.string.mestre));
                                }
                                break;
                                case "ADD G": //adding G p4
                                {
                                    editOrNot[1] = true;//indicate user has selected option

                                    if (cursor1.getString(0) == null) {
                                        showDialogAsMessage("UPDATE " + Database.TABLE_NAME3 + " SET " + Database.COL_36_SKILL2 + "='" + getResources().getString(R.string.women_laber) + "' , " + Database.COL_39_INDICATOR + "=" + 2 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'", "SUCCESSFULLY  ADDED G", "STATUS: SUCCESS", "FAILED TO ADD G", "STATUS: FAILED");

                                    } else if (cursor1.getString(1) == null) {
                                        showDialogAsMessage("UPDATE " + Database.TABLE_NAME3 + " SET " + Database.COL_37_SKILL3 + "='" + getResources().getString(R.string.women_laber) + "' , " + Database.COL_39_INDICATOR + "=" + 3 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'", "SUCCESSFULLY ADDED G", "STATUS: SUCCESS", "FAILED TO ADD G", "STATUS: FAILED");

                                    } else if (cursor1.getString(2) == null) {
                                        showDialogAsMessage("UPDATE " + Database.TABLE_NAME3 + " SET " + Database.COL_38_SKILL4 + "='" + getResources().getString(R.string.women_laber) + "' , " + Database.COL_39_INDICATOR + "=" + 4 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'", "SUCCESSFULLY  ADDED G", "STATUS: SUCCESS", "FAILED TO  ADD G", "STATUS: FAILED");

                                    } else
                                        displayResult(getResources().getString(R.string.only_4_person_allowed_to_add), getResources().getString(R.string.status_cant_add_more) + getResources().getString(R.string.women_laber));
                                }
                                break;
                                case "REMOVE M/L/G": //removing
                                {
                                    editOrNot[1] = true;//indicate user has selected option
                                    //First getting indicator to decide whether delete or not.if indicator is null then cant delete because by default M or L or G present.If indicator is 2,3,4 then checking data is present or not if present then don't delete else delete
                                    Cursor cursorIndicator = db.getData("SELECT " + Database.COL_39_INDICATOR + " FROM " + Database.TABLE_NAME3 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'");
                                    if (cursorIndicator != null) {
                                        cursorIndicator.moveToFirst();
                                        if (cursorIndicator.getString(0) == null) {//person1
                                            displayResult(getResources().getString(R.string.cant_remove_default_skill),getResources().getString(R.string.status_colon_failed));//default M or L or G

                                        } else if (cursorIndicator.getString(0).equals("2")) {//person2
                                           // Cursor result = db.getData("SELECT SUM(" + Database.COL_99_P2 + ") FROM " + Database.TABLE_NAME2 + " WHERE " + Database.COL_11_ID + "= '" + fromIntentPersonId + "'");
                                            Cursor result = db.getData("SELECT SUM(" + db.columnNameOutOf4Table(fromIntentPersonId, (byte) 9) + ") FROM " + db.tableNameOutOf4Table(fromIntentPersonId) + " WHERE " + db.columnNameOutOf4Table(fromIntentPersonId, (byte) 1) + "= '" + fromIntentPersonId + "'");
                                            result.moveToFirst();
                                            if (result.getInt(0) == 0) {//Means no data IN P2 so set null
                                                db.updateTable("UPDATE " + Database.TABLE_NAME3 + " SET " + Database.COL_36_SKILL2 + "= " + null + " , " + Database.COL_33_R2 + "=0  , " + Database.COL_39_INDICATOR + "=" + 1 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'");
                                                displayResult(getResources().getString(R.string.no_data_present_so_removed), getResources().getString(R.string.status_colon_success));
                                            } else if (result.getInt(0) >= 1) {
                                                displayResult(getResources().getString(R.string.cant_remove), getResources().getString(R.string.because_data_is_present_newline_total_sum_equal) + result.getInt(0));
                                            }

                                        } else if (cursorIndicator.getString(0).equals("3")) {//person3
                                           // Cursor result = db.getData("SELECT SUM(" + Database.COL_100_P3 + ") FROM " + Database.TABLE_NAME2 + " WHERE " + Database.COL_11_ID + "= '" + fromIntentPersonId + "'");
                                            Cursor result = db.getData("SELECT SUM(" + db.columnNameOutOf4Table(fromIntentPersonId, (byte) 10) + ") FROM " + db.tableNameOutOf4Table(fromIntentPersonId) + " WHERE " + db.columnNameOutOf4Table(fromIntentPersonId, (byte) 1) + "= '" + fromIntentPersonId + "'");
                                            result.moveToFirst();
                                            if (result.getInt(0) == 0) {//Means no data IN P2                                                                                          //decreasing indicator from 3 to 2
                                                db.updateTable("UPDATE " + Database.TABLE_NAME3 + " SET " + Database.COL_37_SKILL3 + "= " + null + " , " + Database.COL_34_R3 + "=0  , " + Database.COL_39_INDICATOR + "=" + 2 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'");
                                                displayResult(getResources().getString(R.string.no_data_present_so_removed), getResources().getString(R.string.status_colon_success));
                                            } else if (result.getInt(0) >= 1) {
                                                displayResult(getResources().getString(R.string.cant_remove), getResources().getString(R.string.because_data_is_present_newline_total_sum_equal) + result.getInt(0));
                                            }
                                        } else if (cursorIndicator.getString(0).equals("4")) {//person4
                                           // Cursor result = db.getData("SELECT SUM(" + Database.COL_1111_P4 + ") FROM " + Database.TABLE_NAME2 + " WHERE " + Database.COL_11_ID + "= '" + fromIntentPersonId + "'");
                                            Cursor result = db.getData("SELECT SUM(" + db.columnNameOutOf4Table(fromIntentPersonId, (byte) 11) + ") FROM " + db.tableNameOutOf4Table(fromIntentPersonId) + " WHERE " + db.columnNameOutOf4Table(fromIntentPersonId, (byte) 1) + "= '" + fromIntentPersonId + "'");
                                            result.moveToFirst();
                                            if (result.getInt(0) == 0) {//Means no data IN P2
                                                db.updateTable("UPDATE " + Database.TABLE_NAME3 + " SET " + Database.COL_38_SKILL4 + "= " + null + " , " + Database.COL_35_R4 + "=0 , " + Database.COL_39_INDICATOR + "=" + 3 + " WHERE " + Database.COL_31_ID + "= '" + fromIntentPersonId + "'");
                                                displayResult(getResources().getString(R.string.no_data_present_so_removed), getResources().getString(R.string.status_colon_success));
                                            } else if (result.getInt(0) >= 1) {
                                                displayResult(getResources().getString(R.string.cant_remove), getResources().getString(R.string.because_data_is_present_newline_total_sum_equal) + result.getInt(0));
                                            }
                                        } else
                                            displayResult(getResources().getString(R.string.cant_remove_default_skill), getResources().getString(R.string.status_colon_failed));
                                    } else
                                        Toast.makeText(IndividualPersonDetailActivity.this, "NO DATA IN CURSOR", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            }
                        }catch (Exception x){
                            x.printStackTrace();
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
                        boolean updateRaingsuccess,locationReligionSuccess;
                        String star;
                        if (active.equals("1")) {//if user has pressed radio button then only it will execute

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

                        updateRaingsuccess =db.update_Rating_TABLE_NAME3(star,remarksMetaData.getText().toString().trim(),leaveDate,returnDate,p1Rate,p2Rate,p3Rate,p4Rate,fromIntentPersonId,indicator);

                        if(!MyUtility.updateLocationReligionToTableIf(locationHashSet,locationAutoComplete.getText().toString().trim(),religionHashSet,religionAutoComplete.getText().toString().trim(),getBaseContext())){//UPDATING location and religion TO table
                            Toast.makeText(IndividualPersonDetailActivity.this, "NOT UPDATED", Toast.LENGTH_LONG).show();
                        }
                        locationReligionSuccess=db.updateTable("UPDATE " + Database.TABLE_NAME1 + " SET "+Database.COL_18_RELIGION+"='" + religionAutoComplete.getText().toString().trim() + "', "+Database.COL_17_LOCATION+"='"+ locationAutoComplete.getText().toString().trim() +"' WHERE "+Database.COL_1_ID+"='" + fromIntentPersonId + "'");

                        dialog.dismiss();//dismiss current dialog because new dialog will be open when display result()

                        if(updateRaingsuccess || locationReligionSuccess){
                            displayResult("SAVED SUCCESSFULLY",generateMessageAccordingToIndicator(star,leavingDateTv.getText().toString().trim(),returningDate.getText().toString().trim(),locationAutoComplete.getText().toString().trim(),
                                    religionAutoComplete.getText().toString().trim(),remarksMetaData.getText().toString().trim(),indicator,hardcodedP1Tv.getText().toString().trim(),
                                    p1Rate,hardcodedP2Tv.getText().toString().trim(),p2Rate,hardcodedP3Tv.getText().toString().trim(),p3Rate,hardcodedP4Tv.getText().toString().trim(),p4Rate));
                        }else{
                            displayResult("FAILED TO SAVE!!!", "DATA NOT UPDATED- UPDATE QUERY FAILED- PLEASE TRY AGAIN");
                        }
                    }
                });
                dialog.show();
            });
            binding.pdfShareTv.setOnClickListener(view -> {
                try {//to view pdf
                    finish();//while going to other activity so destroy this current activity(individualPersonDetailActivity) so that while coming back we will see refresh activity
                    Intent intent=new Intent(IndividualPersonDetailActivity.this, PdfViewerOperation.class);
                    intent.putExtra("pdf1_or_2_or_3_for_blank_4",(byte)4);
                    intent.putExtra("ID",fromIntentPersonId);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplication().startActivity(intent);
                }catch(Exception e){
                    e.printStackTrace();
                }
            });
            binding.finalCalculationBtn.setOnLongClickListener(new View.OnLongClickListener() {
                TextView defaultSkillTextTv,totalP1CountTv,workTotalAmountTv,totalP1AmountTv,advanceOrBalanceTv,totalDepositAmountTv,wagesTotalAmountTv,skill1TextTv,totalP2CountTv,totalP2AmountTv,skill2TextTv,totalP3CountTv,totalP3AmountTv,skill3TextTv,totalP4CountTv,totalP4AmountTv;
                LinearLayout p2Layout,p3Layout,p4Layout,totalDepositAmountLayout;
                EditText p1RateTv,p2RateTv,p3RateTv,p4RateTv;
                Button longPressToSaveAndCreatePdf,cancel;
                int []innerArray=new int[4];
                int totalDeposit=0,totalWages=0,p1=0,p2=0,p3=0,p4=0,r1=0,r2=0,r3=0,r4=0,indicate=0;//while saving this variable required
                @Override
                public boolean onLongClick(View view){
                    AlertDialog.Builder myCustomDialog=new AlertDialog.Builder(IndividualPersonDetailActivity.this);
                    LayoutInflater inflater=LayoutInflater.from(IndividualPersonDetailActivity.this);
                    View myView=inflater.inflate(R.layout.final_calculation_layout,null);//myView contain all layout view ids
                    myCustomDialog.setView(myView);//set custom layout to alert dialog
                    myCustomDialog.setCancelable(false);//if user touch to other place then dialog will not be close
                    final AlertDialog finalDialog=myCustomDialog.create();//myCustomDialog variable cannot be use in inner class so creating another final varialbe  to use in inner class
                    initialiseIDs(myView);//ids
                    cancel.setOnClickListener(view15 -> finalDialog.dismiss());

                    Cursor defaultSkillCursor=db.getData("SELECT "+Database.COL_8_MAINSKILL1 +" FROM " + Database.TABLE_NAME1 + " WHERE "+Database.COL_1_ID+"= '" + fromIntentPersonId +"'");//for sure it will return type or skill
                    defaultSkillCursor.moveToFirst();
                    defaultSkillTextTv.setText(defaultSkillCursor.getString(0)+" =");//default calculation skill
                    defaultSkillCursor.close();

                   // Cursor sumCursor=db.getData("SELECT SUM("+Database.COL_26_WAGES+"),SUM("+Database.COL_28_P1+"),SUM("+Database.COL_29_P2+"),SUM("+Database.COL_291_P3+"),SUM("+Database.COL_292_P4+"),SUM("+Database.COL_27_DEPOSIT+") FROM "+Database.TABLE_NAME2+" WHERE "+Database.COL_21_ID+"= '"+fromIntentPersonId +"'");
                    Cursor sumCursor=db.getSumOfWagesP1P2P3P4Deposit(fromIntentPersonId);
                    sumCursor.moveToFirst();
                    //initializing this variable to take during saving
                    p1=sumCursor.getInt(1);
                    p2=sumCursor.getInt(2);
                    p3=sumCursor.getInt(3);
                    p4=sumCursor.getInt(4);
                    totalWages=sumCursor.getInt(0);

                    totalP1CountTv.setText(sumCursor.getString(1));//default skill
                    //sum deposit
                    if(sumCursor.getString(5) != null) {//if there is deposit then set visibility visible or else layout visibility GONE
                         totalDepositAmountTv.setText("= " + MyUtility.convertToIndianNumberSystem(sumCursor.getLong(5)));
                         totalDeposit=sumCursor.getInt(5);//updating totalDeposit to take during save
                    }else {
                        totalDepositAmountLayout.setVisibility(View.GONE);
                    }

                    Cursor skillNRateCursor=db.getData("SELECT "+Database.COL_36_SKILL2 +","+Database.COL_37_SKILL3 +","+Database.COL_38_SKILL4 +","+Database.COL_32_R1+","+Database.COL_33_R2+","+Database.COL_34_R3+","+Database.COL_35_R4+" FROM " + Database.TABLE_NAME3 + " WHERE "+Database.COL_31_ID+"= '" + fromIntentPersonId +"'");
                    if(skillNRateCursor != null) {
                        skillNRateCursor.moveToFirst();
                       //initializing this variables to take during saving
                        r1=skillNRateCursor.getInt(3);
                        r2=skillNRateCursor.getInt(4);
                        r3=skillNRateCursor.getInt(5);
                        r4=skillNRateCursor.getInt(6);

                        //if both wages and total work amount is less then 0 then both message have to show so if statement two times
                        if(sumCursor.getInt(0) < 0 ) {//if total wages amount cross the  range of int the this message will be shown.its important
                            Toast.makeText(IndividualPersonDetailActivity.this,getResources().getString(R.string.value_out_of_range_please_check_total_wages), Toast.LENGTH_LONG).show();
                            longPressToSaveAndCreatePdf.setVisibility(View.GONE);
                        }
                        if ((totalDeposit + ((p1 * r1) + (p2 * r2) + (p3 * r3) + (p4 * r4))) < 0) {//user cant enter negative number so when (totalDeposit + (totalr1r2r3r4sum1sum2sum3sum4)) is negative that means int range is exceeds so wrong result will be shown
                            Toast.makeText(IndividualPersonDetailActivity.this, getResources().getString(R.string.value_out_of_range_please_check_total_work_amount), Toast.LENGTH_LONG).show();
                            longPressToSaveAndCreatePdf.setVisibility(View.GONE);//its important otherwise save option will be unable when user enter rate
                        }

                        indicate = MyUtility.get_indicator(getBaseContext(),fromIntentPersonId);
                        //R1
                        if (skillNRateCursor.getInt(3) != 0) {
                            //R1
                             p1RateTv.setText(skillNRateCursor.getString(3));//default skill
                            //    R1 * p1
                            totalP1AmountTv.setText("= " + MyUtility.convertToIndianNumberSystem(skillNRateCursor.getInt(3) * sumCursor.getInt(1)));//default skill
                        } else {
                            longPressToSaveAndCreatePdf.setVisibility(View.GONE);
                            totalP1AmountTv.setText(getResources().getString(R.string.equal_provide_rate));//default skill
                        }
                        //total wages
                        if (sumCursor.getString(0) != null) {//if total wages is not null then set total wages
                             wagesTotalAmountTv.setText(MyUtility.convertToIndianNumberSystem(sumCursor.getLong(0)));//total wages set
                        }
                        //by default= deposit,totalP2CountTv,defaultHardcodedTv,defaultSkillTextTv,p1RateTv,totalP1AmountTv is set automatically
                        if (indicate == 1) {
                            indicator1234CalculateButDoNotUpdateToDBFinal(sumCursor,skillNRateCursor.getInt(3) * sumCursor.getInt(1),0,0,0);
                        }

                        p2Layout.setVisibility(View.GONE);//initially invisible according to indicator it will customize
                        p3Layout.setVisibility(View.GONE);//initially invisible according to indicator it will customize
                        p4Layout.setVisibility(View.GONE);//initially invisible according to indicator it will customize

                        if(indicate == 2) {
                            if(skillNRateCursor.getInt(4) != 0) {
                                //R1
                                p2RateTv.setText(skillNRateCursor.getString(4));
                                //    R2 * p2
                                totalP2AmountTv.setText("= "+MyUtility.convertToIndianNumberSystem(skillNRateCursor.getInt(4)*sumCursor.getInt(2)));
                            }else {
                                longPressToSaveAndCreatePdf.setVisibility(View.GONE);
                                totalP2AmountTv.setText(getResources().getString(R.string.equal_provide_rate));
                            }

                             totalP2CountTv.setText(sumCursor.getString(2));//total p2 count
                             skill1TextTv.setText(skillNRateCursor.getString(0)+" =");//setting skill 1
                             p2Layout.setVisibility(View.VISIBLE);
                             indicator1234CalculateButDoNotUpdateToDBFinal(sumCursor,skillNRateCursor.getInt(3) * sumCursor.getInt(1),skillNRateCursor.getInt(4) * sumCursor.getInt(2),0,0);
                        } else if (indicate == 3) {
                            if(skillNRateCursor.getInt(4) != 0) {
                                 p2RateTv.setText(skillNRateCursor.getString(4));
                                //    R2 * p2
                                 totalP2AmountTv.setText("= "+MyUtility.convertToIndianNumberSystem(skillNRateCursor.getInt(4)*sumCursor.getInt(2)));
                            }else {
                                longPressToSaveAndCreatePdf.setVisibility(View.GONE);
                                totalP2AmountTv.setText(getResources().getString(R.string.equal_provide_rate));
                            }
                            if(skillNRateCursor.getInt(5) != 0) {
                                 p3RateTv.setText(skillNRateCursor.getString(5));
                                //    R3 * p3
                                totalP3AmountTv.setText("= "+MyUtility.convertToIndianNumberSystem(skillNRateCursor.getInt(5)*sumCursor.getInt(3)));
                            }else {
                                longPressToSaveAndCreatePdf.setVisibility(View.GONE);
                                totalP3AmountTv.setText(getResources().getString(R.string.equal_provide_rate));
                            }
                             totalP2CountTv.setText(sumCursor.getString(2));//total p2 count
                             totalP3CountTv.setText(sumCursor.getString(3));//total p3 count
                             skill1TextTv.setText(skillNRateCursor.getString(0)+" =");//setting skill 1
                             skill2TextTv.setText(skillNRateCursor.getString(1)+" =");//setting skill 2
                             p2Layout.setVisibility(View.VISIBLE);
                             p3Layout.setVisibility(View.VISIBLE);
                            indicator1234CalculateButDoNotUpdateToDBFinal(sumCursor,skillNRateCursor.getInt(3) * sumCursor.getInt(1),skillNRateCursor.getInt(4) * sumCursor.getInt(2),skillNRateCursor.getInt(5) * sumCursor.getInt(3),0);

                        } else if (indicate == 4) {
                            if(skillNRateCursor.getInt(4) != 0) {
                                 p2RateTv.setText(skillNRateCursor.getString(4));
                                //    R2 * p2
                                 totalP2AmountTv.setText("= "+MyUtility.convertToIndianNumberSystem(skillNRateCursor.getInt(4)*sumCursor.getInt(2)));
                            }else {
                                longPressToSaveAndCreatePdf.setVisibility(View.GONE);
                                totalP2AmountTv.setText(getResources().getString(R.string.equal_provide_rate));
                            }

                            if(skillNRateCursor.getInt(5) != 0) {
                                p3RateTv.setText(skillNRateCursor.getString(5));
                                //    R3 * p3
                                 totalP3AmountTv.setText("= "+MyUtility.convertToIndianNumberSystem(skillNRateCursor.getInt(5)*sumCursor.getInt(3)));
                            }else {
                                longPressToSaveAndCreatePdf.setVisibility(View.GONE);
                                totalP3AmountTv.setText(getResources().getString(R.string.equal_provide_rate));
                            }

                            if(skillNRateCursor.getInt(6) != 0) {
                                 p4RateTv.setText(skillNRateCursor.getString(6));
                                //    R4 * p4
                                 totalP4AmountTv.setText("= "+MyUtility.convertToIndianNumberSystem(skillNRateCursor.getInt(6)*sumCursor.getInt(4)));
                            }else {
                                longPressToSaveAndCreatePdf.setVisibility(View.GONE);
                                totalP4AmountTv.setText(getResources().getString(R.string.equal_provide_rate));
                            }
                             totalP2CountTv.setText(sumCursor.getString(2));//total p2 count
                             totalP3CountTv.setText(sumCursor.getString(3));//total p3 count
                             totalP4CountTv.setText(sumCursor.getString(4));//total p4 count
                             skill1TextTv.setText(skillNRateCursor.getString(0)+" =");//setting skill 1
                             skill2TextTv.setText(skillNRateCursor.getString(1)+" =");//setting skill 2
                             skill3TextTv.setText(skillNRateCursor.getString(2)+" =");//setting skill 3
                             p2Layout.setVisibility(View.VISIBLE);
                             p3Layout.setVisibility(View.VISIBLE);
                             p4Layout.setVisibility(View.VISIBLE);
                            indicator1234CalculateButDoNotUpdateToDBFinal(sumCursor,skillNRateCursor.getInt(3) * sumCursor.getInt(1),skillNRateCursor.getInt(4) * sumCursor.getInt(2),skillNRateCursor.getInt(5) * sumCursor.getInt(3),skillNRateCursor.getInt(6) * sumCursor.getInt(4));
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

                                if(isp1p2p3p4PresentAndRateNotPresent(r1,r2,r3,r4,p1,p2,p3,p4,indicate)){
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

                                if(isp1p2p3p4PresentAndRateNotPresent(r1,r2,r3,r4,p1,p2,p3,p4,indicate)){
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

                                if(isp1p2p3p4PresentAndRateNotPresent(r1,r2,r3,r4,p1,p2,p3,p4,indicate)){
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

                                if(isp1p2p3p4PresentAndRateNotPresent(r1,r2,r3,r4,p1,p2,p3,p4,indicate)){
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
                    if (skillNRateCursor != null) {
                        skillNRateCursor.close();
                    }
                    sumCursor.close();
                    longPressToSaveAndCreatePdf.setOnLongClickListener(view14 -> {
                        longPressToSaveAndCreatePdf.setVisibility(View.GONE);//to avoid when user click multiple times

                        //if((checkInternalStorageAvailability()*1000) >= 50){//(checkInternalStorageAvailability()*1000) converted to MB so if it is greater or equal to 50 MB then true
                        if (MyUtility.checkPermissionForReadAndWriteToExternalStorage(getApplicationContext())) {//Take permission
                            if (updateRateTotalDaysWorkedTotalAdvanceOrBalanceToDatabase()) {//this method updateRateTotalAdvanceOrBalanceToDatabase() calculate first so that other method could access db and get balance or advance
                                if (MyUtility.createTextFileInvoice(fromIntentPersonId,getBaseContext(),getExternalFilesDir(null).toString())){
                                    if (savePdfToDatabase(generatePDFAndReturnFileAbsolutePath(fromIntentPersonId))) {//first pdf is generated then saved in db in bytes
                                        if (finalDialog != null && finalDialog.isShowing()) {//dismiss dialog before going to pdf-viewer activity
                                            finalDialog.dismiss();
                                        }
                                        if (viewPDFFromDb((byte) 2, fromIntentPersonId)) {//column name should be correct Viewing pdf2
                                            if (deleteDataFromDB()) {
                                                Toast.makeText(IndividualPersonDetailActivity.this, "successfully created", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(IndividualPersonDetailActivity.this, "check remarks\n in recyclerview", Toast.LENGTH_LONG).show();
                                            }
                                        } else {
                                            Toast.makeText(IndividualPersonDetailActivity.this, "FAILED TO VIEW PDF\n(NOTHING LOST)", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(IndividualPersonDetailActivity.this, "FAILED TO SAVE PDF IN DB\n(NOTHING LOST)", Toast.LENGTH_LONG).show();
                                    }
                            }else{
                                 Toast.makeText(IndividualPersonDetailActivity.this, "FAILED TO CREATE TEXT FILE IN DEVICE\n(NOTHING LOST)", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(IndividualPersonDetailActivity.this, "FAILED TO UPDATE ADVANCE OR BALANCE TO DB\n(NOTHING LOST)", Toast.LENGTH_LONG).show();
                            }
                        }else {//request for permission
                            Toast.makeText(IndividualPersonDetailActivity.this, "READ,WRITE EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show();
                            ActivityCompat.requestPermissions(IndividualPersonDetailActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 20);
                        }
                        return false;
                    });
                    finalDialog.show();
                    return false;
                }
                private boolean deleteDataFromDB() {
                    try(Database personDb=new Database(getApplicationContext())){//so that object close automatically
                         boolean success;
                            //this function will be written when existing from pdf viewer
//                                boolean deleted = deletePdfFromDevice(pdfPath);
//                                if (!deleted) {
//                                    Toast.makeText(IndividualPersonDetailActivity.this, "FAILED TO DELETE PDF FILE FROM DEVICE", Toast.LENGTH_SHORT).show();
//                                }
                       // if(updateInvoiceNumberBy1ToDb(fromIntentPersonId)){//updating invoice number by 1
                           // if (deleteAudios(fromIntentPersonId)) {
                                if (deleteWagesAndAudiosFromDBorRecyclerView(fromIntentPersonId)){//delete records from recycle view this should be perform first so that update will be visible else update message will also be deleted //if this failed then recycler view still contain previous data

                                    if (!addMessageAfterFinalCalculationToRecyclerview(fromIntentPersonId)){ //update balance or advance to db.this code is not in else block because if data is not deleted from db then this code should not be executed
                                        Toast.makeText(IndividualPersonDetailActivity.this, "FAILED TO ADD MESSAGE IN RECYCLER VIEW AFTER FINAL CALCULATION .\nCHECK PREVIOUS INVOICE2 TO KNOW ABOUT PREVIOUS CALCULATION\n\n\ncheck remarks in recyclerview", Toast.LENGTH_LONG).show();
                                       // success = personDb.insert_1_Person_WithWagesTable2(fromIntentPersonId, "0-0-0", "0:0:0:0", null, "[AUTOMATIC ENTERED-CHECK INVOICE2 TO KNOW ABOUT PREVIOUS CALCULATION AND SINCE CALCULATION IS DONE ADD DATA TO RECYCLERVIEW (LIKE HOW YOU ADD WAGES) WHATEVER TOTAL ADVANCE OR BALANCE IS.ITS MANDATORY TO GET CORRECT CALCULATION]", 0, 0, "0");
                                       /**change this insertWagesOrDepositOnlyToActiveTableTransaction method*/
                                        success = personDb.insertWagesOrDepositOnlyToActiveTableTransaction(fromIntentPersonId,"0-0-0", "0:0:0:0",null,"[AUTOMATIC ENTERED-CHECK INVOICE2 TO KNOW ABOUT PREVIOUS CALCULATION AND SINCE CALCULATION IS DONE ADD DATA TO RECYCLERVIEW (LIKE HOW YOU ADD WAGES) WHATEVER TOTAL ADVANCE OR BALANCE IS.ITS MANDATORY TO GET CORRECT CALCULATION]",0,0,0,0,0,0,"0");
                                        if (!success)
                                            Toast.makeText(IndividualPersonDetailActivity.this, "UPDATE RECYCLERVIEW \nCHECK INVOICE2 TO KNOW ABOUT PREVIOUS CALCULATION", Toast.LENGTH_LONG).show();
                                    }

                                    if(!updateInvoiceNumberBy1ToDb(fromIntentPersonId)) {//updating invoice number by 1
                                        Toast.makeText(IndividualPersonDetailActivity.this, "FAILED TO UPDATE INVOICE NUMBER IN DATABASE NOTHING TO DO JUST REMEMBER FROM NOW INVOICE NUMBER WOULD NOT BE CORRECT FOR THIS ID: "+fromIntentPersonId+"\n\n\ncheck remarks in recyclerview", Toast.LENGTH_LONG).show();
                                        //success = personDb.insert_1_Person_WithWagesTable2(fromIntentPersonId, "0-0-0", "0:0:0:0", null, "[AUTOMATIC ENTERED-FAILED TO UPDATE INVOICE NUMBER IN DATABASE NOTHING TO DO JUST REMEMBER FROM NOW INVOICE NUMBER WOULD NOT BE CORRECT FOR THIS ID: "+fromIntentPersonId+" BECAUSE PDF/INVOICE IS GENERATED BUT INVOICE NUMBER NOT UPDATED IN DATABASE]", 0, 0, "0");
                                        success = personDb.insertWagesOrDepositOnlyToActiveTableTransaction(fromIntentPersonId,"0-0-0", "0:0:0:0",null,"[AUTOMATIC ENTERED-FAILED TO UPDATE INVOICE NUMBER IN DATABASE NOTHING TO DO JUST REMEMBER FROM NOW INVOICE NUMBER WOULD NOT BE CORRECT FOR THIS ID: "+fromIntentPersonId+" BECAUSE PDF/INVOICE IS GENERATED BUT INVOICE NUMBER NOT UPDATED IN DATABASE]",0,0,0,0,0,0,"0");

                                        if (!success)
                                            Toast.makeText(IndividualPersonDetailActivity.this, "NOTHING TO DO JUST REMEMBER FROM NOW INVOICE NUMBER \nWOULD NOT BE CORRECT FOR THIS ID: "+fromIntentPersonId, Toast.LENGTH_LONG).show();
                                    }

//                                    if (!beforeDeletingWagesAudiosShouldBeDeletedFirst(fromIntentPersonId)){//deleting audio
//                                        Toast.makeText(IndividualPersonDetailActivity.this, "FAILED TO DELETE AUDIOS SO MANUALLY DELETE BY YOURSELF FROM YOUR DEVICE AUDIO ID: "+fromIntentPersonId+"\n\n\ncheck remarks in recycler view", Toast.LENGTH_LONG).show();
//                                        success = personDb.insert_1_Person_WithWagesTable2(fromIntentPersonId, "0-0-0", "0:0:0:0", null, "[AUTOMATIC ENTERED-DELETE AUDIOS MANUALLY HAVING ID:"+fromIntentPersonId+" (IF NOT DELETED THEN IT WILL BE IN DEVICE)", 0, 0, "0");
//                                        if (!success)
//                                            Toast.makeText(IndividualPersonDetailActivity.this, "OPTIONAL TO DO \nMANUALLY DELETE BY YOURSELF FROM YOUR DEVICE AUDIO ID: "+fromIntentPersonId, Toast.LENGTH_LONG).show();
//                                    }
                                }else{//add this message to recycle view

                                    if(!updateInvoiceNumberBy1ToDb(fromIntentPersonId)) {//updating invoice number by 1 CANT REVERSE
                                        Toast.makeText(IndividualPersonDetailActivity.this, "FAILED TO UPDATE INVOICE NUMBER IN DATABASE NOTHING TO DO JUST REMEMBER FROM NOW INVOICE NUMBER WOULD NOT BE CORRECT FOR THIS ID: "+fromIntentPersonId+"\n\n\ncheck remarks in recyclerview", Toast.LENGTH_LONG).show();
                                       // success = personDb.insert_1_Person_WithWagesTable2(fromIntentPersonId, "0-0-0", "0:0:0:0", null, "[AUTOMATIC ENTERED-FAILED TO UPDATE INVOICE NUMBER IN DATABASE NOTHING TO DO JUST REMEMBER FROM NOW INVOICE NUMBER WOULD NOT BE CORRECT FOR THIS ID: "+fromIntentPersonId+" BECAUSE PDF/INVOICE IS GENERATED BUT INVOICE NUMBER NOT UPDATED IN DATABASE]", 0, 0, "0");
                                        success = personDb.insertWagesOrDepositOnlyToActiveTableTransaction(fromIntentPersonId,"0-0-0", "0:0:0:0",null,"[AUTOMATIC ENTERED-FAILED TO UPDATE INVOICE NUMBER IN DATABASE NOTHING TO DO JUST REMEMBER FROM NOW INVOICE NUMBER WOULD NOT BE CORRECT FOR THIS ID: "+fromIntentPersonId+" BECAUSE PDF/INVOICE IS GENERATED BUT INVOICE NUMBER NOT UPDATED IN DATABASE]",0,0,0,0,0,0,"0");

                                        if (!success)
                                            Toast.makeText(IndividualPersonDetailActivity.this, "NOTHING TO DO JUST REMEMBER FROM NOW INVOICE NUMBER \nWOULD NOT BE CORRECT FOR THIS ID: "+fromIntentPersonId, Toast.LENGTH_LONG).show();
                                    }
//                                    if (!beforeDeletingWagesAudiosShouldBeDeletedFirst(fromIntentPersonId)){//deleting audio
//                                        Toast.makeText(IndividualPersonDetailActivity.this, "FAILED TO DELETE AUDIOS SO MANUALLY DELETE BY YOURSELF FROM YOUR DEVICE AUDIO ID: "+fromIntentPersonId+"\n\n\ncheck remarks in recycler view", Toast.LENGTH_LONG).show();
//                                        success = personDb.insert_1_Person_WithWagesTable2(fromIntentPersonId, "0-0-0", "0:0:0:0", null, "[AUTOMATIC ENTERED-DELETE AUDIOS MANUALLY HAVING ID:"+fromIntentPersonId+" (IF NOT DELETED THEN IT WILL BE IN DEVICE)", 0, 0, "0");
//                                        if (!success)
//                                            Toast.makeText(IndividualPersonDetailActivity.this, "OPTIONAL TO DO \nMANUALLY DELETE BY YOURSELF FROM YOUR DEVICE AUDIO ID: "+fromIntentPersonId, Toast.LENGTH_LONG).show();
//                                    }

                                        String  message=  "[AUTOMATIC ENTERED-FAILED TO DELETE RECORD FROM DATABASE. CURRENT DATA IS SAVED TO PREVIOUS INVOICE2. ACTION TO PERFORM BY YOURSELF SEQUENTIALLY(strictly) \n\n " +
                                                "1.MANUALLY EDIT ALL WAGES DATA TO 0 ie.set all wages and work days to 0 IN RECYCLER VIEW (IF NOT DONE THEN PREVIOUS DATA WILL BE THERE AND GIVE INCORRECT CALCULATION ITS MANDATORY) AFTER THAT\n\n " +
                                                "2.CHECK INVOICE2 TO KNOW ABOUT PREVIOUS CALCULATION AND SINCE CALCULATION IS DONE SO ADD DATA TO RECYCLERVIEW (LIKE HOW YOU ADD WAGES) WHATEVER TOTAL ADVANCE OR BALANCE IS.ITS MANDATORY TO GET CORRECT CALCULATION]";
                                                //"3.DELETE AUDIOS MANUALLY HAVING ID:"+fromIntentPersonId+" (IF NOT DELETED THEN IT WILL BE IN DEVICE)\n\n" +
                                                //"*JUST REMEMBER FROM NOW INVOICE NUMBER WOULD NOT BE CORRECT FOR THIS ID:"+fromIntentPersonId+" BECAUSE PDF/INVOICE IS GENERATED BUT INVOICE NUMBER NOT UPDATED IN DATABASE";

                                        Toast.makeText(IndividualPersonDetailActivity.this, message, Toast.LENGTH_LONG).show();
                                    //success = personDb.insert_1_Person_WithWagesTable2(fromIntentPersonId, "0-0-0", "0:0:0:0", null, message, 0, 0, "0");
                                    success = personDb.insertWagesOrDepositOnlyToActiveTableTransaction(fromIntentPersonId,"0-0-0", "0:0:0:0",null, message,0,0,0,0,0,0,"0");

                                    if (success) {
                                            Toast.makeText(IndividualPersonDetailActivity.this, "CHECK RECYCLER VIEW REMARKS TO KNOW", Toast.LENGTH_LONG).show();//because data is deleted so set all data to 0
                                        }else{//it will execute when message is not set//data is save to pdf ie.invoice2
                                            Toast.makeText(IndividualPersonDetailActivity.this,"ATTENTION \nWRITE ALL DATA BY HAND\n IN PAPER MANUALLY", Toast.LENGTH_LONG).show();
                                        }
                                            return false;
                                    }
                    }catch(Exception ex){
                        Toast.makeText(IndividualPersonDetailActivity.this, "File not Found Exception", Toast.LENGTH_LONG).show();
                        ex.printStackTrace();
                        return false;
                    }
                      return true;
                }
                private boolean updateInvoiceNumberBy1ToDb(String id) {
                    try(Database db=new Database(getApplicationContext())){
                        Cursor cursor = db.getData("SELECT "+Database.COL_396_PDFSEQUENCE +" FROM " + Database.TABLE_NAME3 + " WHERE "+Database.COL_31_ID+"= '" + id + "'");
                        cursor.moveToFirst();//means only one row is returned
                       if(!db.updateTable("UPDATE " + Database.TABLE_NAME3 + " SET  "+Database.COL_396_PDFSEQUENCE +" ='" + (cursor.getInt(0)+1) +"' WHERE "+Database.COL_31_ID+"='" + id + "'")){
                          return false;
                       }
                        cursor.close();
                    }catch (Exception ex){
                        ex.printStackTrace();
                        Toast.makeText(IndividualPersonDetailActivity.this, "INVOICE NUMBER NOT UPDATED TO DB ERROR OCCURRED", Toast.LENGTH_LONG).show();
                        return false;
                    }
                    return true;
                }
                private boolean beforeDeletingWagesAudiosShouldBeDeletedFirst(String id) {
                    try(Database personDb=new Database(getApplicationContext());
                        Cursor cursor = personDb.getData("SELECT "+Database.COL_4__MICPATH +" FROM " + Database.TABLE_NAME2 + " WHERE "+Database.COL_1__ID +"= '" + id + "'")){//so that object close automatically
                         while(cursor.moveToNext()){
                             if(cursor.getString(0) != null) {//checking path may be null
                                 if (!MyUtility.deletePdfOrRecordingFromDevice(cursor.getString(0))) {
                                      return false;
                                 }
                             }
                         }
                        return true;
                    }
                }
                private boolean deleteWagesAndAudiosFromDBorRecyclerView(String fromIntentPersonId) {
                    try(Database personDb=new Database(getApplicationContext());
                        Cursor cursor = personDb.getData("SELECT "+Database.COL_1__ID +" FROM " + Database.TABLE_NAME2 + " WHERE "+Database.COL_1__ID +"= '" + fromIntentPersonId + "'")){//so that object close automatically
                        cursor.moveToFirst();
                        if(cursor.getCount()==0){//if already record not present then return true
                            return true;
                        }
                        if(!beforeDeletingWagesAudiosShouldBeDeletedFirst(fromIntentPersonId)){//deleting audio first before deleting wages otherwise audio will not be deleted because deleting wages row first will delete path on mic so it is important to delete audio first
                                        Toast.makeText(IndividualPersonDetailActivity.this, "FAILED TO DELETE AUDIOS SO MANUALLY DELETE BY YOURSELF FROM YOUR DEVICE AUDIO ID: "+fromIntentPersonId+"\n\n\ncheck remarks in recycler view", Toast.LENGTH_LONG).show();
                                        //boolean success = personDb.insert_1_Person_WithWagesTable2(fromIntentPersonId, "0-0-0", "0:0:0:0", null, "[AUTOMATIC ENTERED-DELETE AUDIOS MANUALLY HAVING ID:"+fromIntentPersonId+" (IF NOT DELETED THEN IT WILL BE IN DEVICE)", 0, 0, "0");
                                        boolean success = personDb.insertWagesOrDepositOnlyToActiveTableTransaction(fromIntentPersonId,"0-0-0", "0:0:0:0",null,"[AUTOMATIC ENTERED-DELETE AUDIOS MANUALLY HAVING ID:"+fromIntentPersonId+" (IF NOT DELETED THEN IT WILL BE IN DEVICE)",0,0,0,0,0,0,"0");
                                        if (!success)
                                            Toast.makeText(IndividualPersonDetailActivity.this, "OPTIONAL TO DO \nMANUALLY DELETE BY YOURSELF FROM YOUR DEVICE AUDIO ID: "+fromIntentPersonId, Toast.LENGTH_LONG).show();
                          return false;//because audio failed to delete
                         }//else not required because below statement should be executed

                        if(!personDb.deleteAllRowsTransaction(fromIntentPersonId,Database.TABLE_NAME2)) {
                            return false;//if failed to deleted
                        }
                        return true;
                    }
                }
                private boolean addMessageAfterFinalCalculationToRecyclerview(String fromIntentPersonId) {
                    //PersonRecordDatabase db=null;
                   // Cursor cursor=null;
                    try(Database db = new Database(getApplicationContext());
                        Cursor cursor=db.getData("SELECT "+Database.COL_13_ADVANCE+","+Database.COL_14_BALANCE+" FROM " + Database.TABLE_NAME1 + " WHERE "+Database.COL_1_ID+"= '" + fromIntentPersonId + "'"))
                    {
                       // db = new PersonRecordDatabase(getApplicationContext());//so db close automatically
                        int amount=0;
                        boolean success;
//                        final Calendar current = Calendar.getInstance();//to get current date
//                        String date = current.get(Calendar.DAY_OF_MONTH) + "-" + (current.get(Calendar.MONTH) + 1) + "-" + current.get(Calendar.YEAR);
                         String date=MyUtility.getOnlyCurrentDate();
//                        Date d = Calendar.getInstance().getTime(); //To get exact onlyTime so write code in save button
//                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");//a stands for is AM or PM
//                        String onlyTime = sdf.format(d);
                        String onlyTime = MyUtility.getOnlyTime();
                       // cursor = db.getData("SELECT ADVANCE,BALANCE FROM " + db.TABLE_NAME1 + " WHERE ID= '" + fromIntentPersonId + "'");
                        cursor.moveToFirst();//means only one row is returned
                        if (cursor.getInt(0) != 0 && cursor.getInt(1) == 0) {
                            amount = cursor.getInt(0);
                            //insert to database taking just first person                                                      //remarks
                           // success = db.insert_1_Person_WithWagesTable2(fromIntentPersonId, date, onlyTime, null, "[" + onlyTime +getResources().getString(R.string.hyphen_automatic_entered)+"\n\n" + "[After calculation advance Rs. " + amount+" ]", amount, 0, "0");
                            success = db.insertWagesOrDepositOnlyToActiveTableTransaction(fromIntentPersonId,date, onlyTime,null, "[" + onlyTime +getResources().getString(R.string.hyphen_automatic_entered)+"\n\n" + "[After calculation advance Rs. " + amount+" ]",amount,0,0,0,0,0,"0");
                            if (!success)
                                return false;
                        }else if (cursor.getInt(0) == 0 && cursor.getInt(1) != 0) {
                            amount = cursor.getInt(1);
                            //insert to database taking just first person                                                      //remarks
                            //success = db.insert_Deposit_Table2(fromIntentPersonId, date, onlyTime, null, "[" +onlyTime +getResources().getString(R.string.hyphen_automatic_entered)+"\n\n" + "[After calculation balance Rs. " + amount+" ]", amount, "1");
                            success=db.insertWagesOrDepositOnlyToActiveTableTransaction(fromIntentPersonId,date,onlyTime,null, "[" +onlyTime +getResources().getString(R.string.hyphen_automatic_entered)+"\n\n" + "[After calculation balance Rs. " + amount+" ]",0,0,0,0,0,amount,"1");

                            if (!success)
                                return false;
                        }else if(cursor.getInt(0) == 0 && cursor.getInt(1) == 0){
                            //insert to database taking just first person                                                      //remarks
                            //success = db.insert_1_Person_WithWagesTable2(fromIntentPersonId, date, onlyTime, null, "[" + onlyTime +getResources().getString(R.string.hyphen_automatic_entered)+"\n\n" + "[After calculation all cleared  Rs. " + amount+" ]", amount, 0, "0");
                            success = db.insertWagesOrDepositOnlyToActiveTableTransaction(fromIntentPersonId,date, onlyTime,null, "[" + onlyTime +getResources().getString(R.string.hyphen_automatic_entered)+"\n\n" + "[After calculation all cleared  Rs. " + amount+" ]",amount,0,0,0,0,0,"0");
                            if (!success)
                                return false;
                        }
                        return true;

                    }catch (Exception ex){
                        ex.printStackTrace();
                        return false;
                    }
                }
                private boolean savePdfToDatabase(String pdfAbsolutePath) {
                    if(pdfAbsolutePath != null) {
                        try (Database db = new Database(getApplicationContext());
                             Cursor cursor = db.getData("SELECT "+Database.COL_395_INVOICE2+" FROM " + Database.TABLE_NAME3 + " WHERE "+Database.COL_31_ID+"= '" + fromIntentPersonId + "'")){//so that object close automatically
                             cursor.moveToFirst();
                            byte[] newPDF = Files.readAllBytes(Paths.get(pdfAbsolutePath));//CONVERTED pdf file to byte array if path is not found then catch block execute

                            if (cursor.getBlob(0) == null) {//if pdf2 is null then store in pdf2
                                // Toast.makeText(IndividualPersonDetailActivity.this, "pdf not there", Toast.LENGTH_LONG).show();
                                return db.insertPdf(fromIntentPersonId, newPDF, 2);
                            }
                            //if pdf1 is not null then store in pdf 2
                            if (db.insertPdf(fromIntentPersonId, cursor.getBlob(0), 1)) {//store pdf2 in pdf1
                                return db.insertPdf(fromIntentPersonId, newPDF, 2);//store new pdf in pdf2
                            }
                            return false;
                        }catch (Exception ex){
                            Toast.makeText(IndividualPersonDetailActivity.this, "File not Found Exception", Toast.LENGTH_LONG).show();
                            ex.printStackTrace();
                            return false;
                        }finally{
                            MyUtility.deletePdfOrRecordingFromDevice(pdfAbsolutePath);//after saving created pdf in db and device then delete that pdf from device.not returning true or false because it is not important.but if we return then it will override return value of try or catch block
                        }
                    }else return false;
                }
                private boolean viewPDFFromDb(byte whichPdf,String fromIntentPersonId) {
                    try {//to view pdf
                        finish();//while going to other activity so destroy this current activity(individualPersonDetailActivity) so that while coming back we will see refresh activity
                        Intent intent=new Intent(IndividualPersonDetailActivity.this, PdfViewerOperation.class);
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
                public String generatePDFAndReturnFileAbsolutePath(String id) {//if error return null otherwise file path
                    try{
                        String fileAbsolutePath;
                        MakePdf makePdf = new MakePdf(); //create PDF
                       if(!makePdf.createPage1(MakePdf.defaultPageWidth, MakePdf.defaultPageHeight, 1)) return null;//created page 1

                       if(!fetchOrganizationDetailsAndWriteToPDF(makePdf)) return null;//org details

                       if(!fetchPersonDetailAndWriteToPDF(id,makePdf))return null;//personal detail

                      if(!fetchWorkDetailsCalculationAndWriteToPDF(id,makePdf)) return null;//calculation

                        if(!makePdf.createdPageFinish2()) return null;

                        fileAbsolutePath =makePdf.createFileToSavePdfDocumentAndReturnFileAbsolutePath3(getExternalFilesDir(null).toString(),MyUtility.generateUniqueFileName(getBaseContext(),id));

                        if(!makePdf.closeDocumentLastOperation4())return null;

                       if(fileAbsolutePath !=null){
                           return fileAbsolutePath;//fileNameAbsolutePath will be used to get file from device and convert to byteArray and store in db
                       }else return null;

                    }catch (Exception ex){
                        Toast.makeText(IndividualPersonDetailActivity.this, "PDF GENERATION ERROR", Toast.LENGTH_LONG).show();
                        ex.printStackTrace();
                        return null;
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
                private float checkInternalStorageAvailability(){
                    File path = Environment.getDataDirectory();//Return the user data directory.return type FILE and Environment class Provides access to environment variables.
                    StatFs stat = new StatFs(path.getPath());//Construct a new StatFs for looking at the stats of the filesystem at path.
                    long blockSize = stat.getBlockSizeLong();//The size, in bytes, of a block on the file system. This corresponds to the Unix statvfs.f_frsize field.
                    long availableBlocks = stat.getAvailableBlocksLong();//The number of bytes that are free on the file system and available to applications.
                    String format = Formatter.formatFileSize(IndividualPersonDetailActivity.this, availableBlocks * blockSize);//return available internal storage memory like 9.66 GB
                    format=format.trim();//for safer side

                    StringBuilder stringBuilder=new StringBuilder();
                    for(int i=0;i<format.length();i++){
                        if(format.charAt(i) == ' ' || Character.isAlphabetic(format.charAt(i)))
                            break;
                        stringBuilder.append(format.charAt(i));
                    }
                    return  Float.parseFloat(stringBuilder.toString());
                }
                private boolean updateRateTotalDaysWorkedTotalAdvanceOrBalanceToDatabase(){
                        Cursor cursor = db.getData("SELECT "+Database.COL_397_TOTAL_WORKED_DAYS +" FROM " + Database.TABLE_NAME3 + " WHERE "+Database.COL_31_ID+"= '" + fromIntentPersonId + "'");
                        cursor.moveToFirst();//means only one row is returned

                    //updating rate and total worked days
                    boolean success = db.updateTable("UPDATE " + Database.TABLE_NAME3 + " SET "+Database.COL_32_R1+"='"+r1+"' , "+Database.COL_33_R2+"='"+r2+"' , "+Database.COL_34_R3+"='"+r3+"' , "+Database.COL_35_R4+"='"+r4+"' , "+Database.COL_397_TOTAL_WORKED_DAYS+" ='" +(cursor.getInt(0)+p1)+"' WHERE "+Database.COL_31_ID+"='" + fromIntentPersonId + "'");
                    cursor.close();

                    if(success){//if rate is updated then proceed
                       if (!MyUtility.isEnterDataIsWrong(innerArray)) {//if data is right then only change fields.This condition is already checked but checking again
                           if (!isp1p2p3p4PresentAndRateNotPresent(r1, r2, r3, r4, p1, p2, p3, p4, indicate)) {//This condition is already checked but checking again
                               //if both wages and total work amount is less then 0 then don't save.This condition already checked but checking again

                               if(((totalDeposit + ((p1 * r1) + (p2 * r2) + (p3 * r3) + (p4 * r4))) < 0) || (totalWages < 0)){//user cant enter negative number so when (totalDeposit + (totalr1r2r3r4sum1sum2sum3sum4)) is negative that means int range is exceeds so wrong result will be shown
                                   Toast.makeText(IndividualPersonDetailActivity.this, "FAILED TO SAVE DUE TO WRONG DATA", Toast.LENGTH_LONG).show();
                                   return false;
                               }else if((totalDeposit + ((p1 * r1) + (p2 * r2) + (p3 * r3) + (p4 * r4))) < totalWages){
                                   //updating Advance to db
                                   success = db.updateTable("UPDATE " + Database.TABLE_NAME1 + " SET "+Database.COL_13_ADVANCE+"='" + (totalWages - (totalDeposit + ((p1 * r1) + (p2 * r2) + (p3 * r3) + (p4 * r4)))) + "'" + "WHERE "+Database.COL_1_ID+"='" + fromIntentPersonId + "'");
                                   if(!success){
                                       Toast.makeText(IndividualPersonDetailActivity.this, "FAILED TO UPDATE ADVANCE AMOUNT TO DB", Toast.LENGTH_LONG).show();
                                       return false;
                                   }
                                   //if there is advance then balance  column should be 0
                                   success = db.updateTable("UPDATE " + Database.TABLE_NAME1 + " SET "+Database.COL_14_BALANCE+"='" + 0 + "'" + "WHERE "+Database.COL_1_ID+"='" + fromIntentPersonId + "'");
                                   if (!success) {
                                       Toast.makeText(IndividualPersonDetailActivity.this, "FAILED TO UPDATE BALANCE AMOUNT TO DB", Toast.LENGTH_LONG).show();
                                       return false;
                                      }
                               }else if ((totalDeposit + ((p1 * r1) + (p2 * r2) + (p3 * r3) + (p4 * r4))) >= totalWages) {//>= is given because when totalWages and total work is same then this condition will be executed to set balance 0

                                   //updating balance to db if greater then 0
                                   success = db.updateTable("UPDATE " + Database.TABLE_NAME1 + " SET "+Database.COL_14_BALANCE+"='" + ((totalDeposit + ((p1 * r1) + (p2 * r2) + (p3 * r3) + (p4 * r4))) - totalWages) + "'" + "WHERE "+Database.COL_1_ID+"='" + fromIntentPersonId + "'");
                                   if (!success) {
                                       Toast.makeText(IndividualPersonDetailActivity.this, "FAILED TO UPDATE BALANCE AMOUNT TO DB", Toast.LENGTH_LONG).show();
                                       return false;
                                   }
                                   //if there is balance then update advance column should be 0
                                   success = db.updateTable("UPDATE " + Database.TABLE_NAME1 + " SET "+Database.COL_13_ADVANCE+"='" + 0 + "'" + "WHERE "+Database.COL_1_ID+"='" + fromIntentPersonId + "'");
                                   if (!success) {
                                       Toast.makeText(IndividualPersonDetailActivity.this, "FAILED TO UPDATE ADVANCE AMOUNT TO DB", Toast.LENGTH_LONG).show();
                                       return false;
                                   }
                               }
                           }else{
                               Toast.makeText(IndividualPersonDetailActivity.this, "FAILED TO SAVE DUE TO RATE NOT PROVIDED", Toast.LENGTH_LONG).show();
                               return false;
                           }
                       }else{
                           Toast.makeText(IndividualPersonDetailActivity.this, "FAILED TO SAVE DUE TO WRONG DATA", Toast.LENGTH_LONG).show();
                           return false;
                       }
                   }else{
                        Toast.makeText(IndividualPersonDetailActivity.this, "FAILED TO UPDATE RATE AND TOTAL WORKED DAYS", Toast.LENGTH_LONG).show();
                        return false;
                    }
                    return true;
                }
                public boolean isp1p2p3p4PresentAndRateNotPresent(int r1,int r2,int r3,int r4,int p1,int p2,int p3,int p4,int indicator){
                    if(indicator==1 && (p1 !=0 && r1==0)){
                        return true;
                    }else if(indicator==2 && ((p1 !=0 && r1==0) || (p2 !=0 && r2==0))){
                        return true;
                    }else if(indicator==3 && ((p1 !=0 && r1==0) || (p2 !=0 && r2==0) || (p3 !=0 && r3==0))){
                        return true;
                    }else if(indicator==4 && ((p1 !=0 && r1==0) || (p2 !=0 && r2==0) || (p3 !=0 && r3==0) || (p4 !=0 && r4==0))){
                        return true;
                    }
                    return false;
                }
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
                private void indicator1234CalculateButDoNotUpdateToDBFinal(Cursor sumCursor, int rate1IntoSump1, int rate2IntoSump2, int rate3IntoSump3, int rate4IntoSump4) {
                    int  totalDeposit,totalWages;
                    int totalr1r2r3r4sum1sum2sum3sum4=rate1IntoSump1+rate2IntoSump2+rate3IntoSump3+rate4IntoSump4;
                    totalWages=sumCursor.getInt(0);
                    totalDeposit=sumCursor.getInt(5);

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
                      defaultSkillTextTv=myView.findViewById(R.id.default_skill_text_tv_final);
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
                if (MyUtility.getActivePhoneNumbersFromDb(fromIntentPersonId,getBaseContext()) != null) {
                    Intent callingIntent = new Intent(Intent.ACTION_DIAL);
                    callingIntent.setData(Uri.parse("tel:+91" + MyUtility.getActivePhoneNumbersFromDb(fromIntentPersonId,getBaseContext())));
                    startActivity(callingIntent);
                } else
                    Toast.makeText(IndividualPersonDetailActivity.this, getResources().getString(R.string.no_phone_number), Toast.LENGTH_SHORT).show();
            });
            binding.editTv.setOnClickListener(view -> {
                Intent intent = new Intent(getBaseContext(), InsertPersonDetailsActivity.class);
                intent.putExtra("ID", fromIntentPersonId);
                startActivity(intent);
                finish();//while going to other activity so destroy  this current activity so that while coming back we will see refresh activity
            });
            if (cursor != null) {
                cursor.close();
            }
            binding.gobackIndividualPersonDetails.setOnClickListener(view -> {
//                        if (getIntent().hasExtra("FromMesterLaberGAdapter")) {
//                            finish();//first destroy current activity then go back
//                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//                            transaction.replace(R.id.individual_person_details_activity, new SearchFragment()).commit();
//                            Toast.makeText(this, "if", Toast.LENGTH_SHORT).show();
//                           // getSupportFragmentManager().beginTransaction().detach(new ActiveLGFragment()).attach(new ActiveLGFragment()).commit();
//                        } else {
//                            Toast.makeText(this, "else", Toast.LENGTH_SHORT).show();
//                            super.onBackPressed();// This calls finish() on this activity and pops the back stack.
//                        }
                        super.onBackPressed();// This calls finish() on this activity and pops the back stack.
                    });
        } else
            Toast.makeText(this, "NO ID FROM OTHER INTENT", Toast.LENGTH_SHORT).show();
        //to insert data in recyclerview
        binding.fab.setOnClickListener(view -> {
            arr=new int[7];//so that when again enter data fresh array will be created
            insertDataToRecyclerView_AlertDialogBox(MyUtility.get_indicator(getBaseContext(),fromIntentPersonId));
        });
    }

    public  String generateMessageAccordingToIndicator(String star,String leavingDate,String returningDate,String locationAutoComplete,String religionAutoComplete,String remarksMetaData,int indicator,String skill1,int p1,String skill2,int p2,String skill3,int p3,String skill4,int p4){
        StringBuilder sb=new StringBuilder();
        switch (indicator){
            case 1:{
                sb.append(skill1).append(" - "+p1);
            }break;
            case 2:{
                sb.append(skill1).append(" - "+p1).append("  "+skill2).append(" - "+p2);
            }break;
            case 3:{
                sb.append(skill1).append(" - "+p1).append("  "+skill2).append(" - "+p2).append("  "+skill3).append(" - "+p3);
            }break;
            case 4:{
                sb.append(skill1).append(" - "+p1).append("  "+skill2).append(" - "+p2).append("  "+skill3).append(" - "+p3).append("  "+skill4).append(" - "+p4);
            }break;
        }
        sb.append("\n\nSTAR-  " + star)
                .append("\nLEAVING -     "+leavingDate)
                .append("\nRETURNING - "+returningDate)
                .append("\nLOCATION-  "+locationAutoComplete)
                .append("\nRELIGION-    "+religionAutoComplete)
                .append( "\n\nREMARKS- "+remarksMetaData);
        return sb.toString();
    }
    private void rateUpdateManually(TextView hardcodedP1Tv, EditText inputP1Rate, TextView hardcodedP2Tv, EditText inputP2Rate, TextView hardcodedP3Tv, EditText inputP3Rate, TextView hardcodedP4Tv, EditText inputP4Rate, Button saveButton, int checkCorrectionArray[], int userInputRateArray[], int indicator, String id) {
        try(Database db=new Database(getBaseContext());
            Cursor skillCursor1=db.getData("SELECT "+Database.COL_8_MAINSKILL1 +" FROM " + Database.TABLE_NAME1 + " WHERE "+Database.COL_1_ID+"= '" + id +"'")) {
           if(skillCursor1 != null) skillCursor1.moveToFirst();

            Cursor rateCursor1 = db.getData("SELECT " + Database.COL_32_R1 + " FROM " + Database.TABLE_NAME3 + " WHERE " + Database.COL_1_ID + "= '" + id + "'");
            if(rateCursor1 != null) rateCursor1.moveToFirst();

            Cursor skillNRateCursor=null;
            if(indicator > 1) {//if more than 1 then get all skill and rate
                  skillNRateCursor = db.getData("SELECT " + Database.COL_36_SKILL2 + "," + Database.COL_37_SKILL3 + "," + Database.COL_38_SKILL4 + " , "+ Database.COL_33_R2 + "," + Database.COL_34_R3 + "," + Database.COL_35_R4 + " FROM " + Database.TABLE_NAME3 + " WHERE " + Database.COL_31_ID + "= '" + id + "'");
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
                    hardcodedP1Tv.setText(skillCursor1.getString(0));
                    inputP1Rate.setText(rateCursor1.getString(0));
                    rate1Et(inputP1Rate, saveButton, checkCorrectionArray, userInputRateArray);//input rate array is updated
                }
                break;
                case 2: {
                    hardcodedP2Tv.setVisibility(View.VISIBLE);
                    inputP2Rate.setVisibility(View.VISIBLE);

                    hardcodedP1Tv.setText(skillCursor1.getString(0));
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

                    hardcodedP1Tv.setText(skillCursor1.getString(0));
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

                    hardcodedP1Tv.setText(skillCursor1.getString(0));
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
            Toast.makeText(this, "error occurred", Toast.LENGTH_SHORT).show();
        }
    }
    public void rate1Et(EditText inputP1Rate, Button  saveButton, int checkCorrectionArray[], int userInputRateArray[]) {
        inputP1Rate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String p1 =inputP1Rate.getText().toString().trim();
                inputP1Rate.setTextColor(getColor(R.color.purple_700));
                checkCorrectionArray[0]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data
                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(checkCorrectionArray)) {//this is important if in field data is wrong then save button will not enabled until data is right.
                     saveButton.setVisibility(View.VISIBLE);
                }
                if (!p1.matches("[0-9]+")) {//space or , or - is restricted"[.]?[0-9]+[.]?[0-9]*"
                    inputP1Rate.setTextColor(Color.RED);
                     saveButton.setVisibility(View.GONE);
                    checkCorrectionArray[0]=2;//means data is inserted wrong
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                try{
                    userInputRateArray[0] = Integer.parseInt(inputP1Rate.getText().toString().trim());

                }catch(Exception e){
                    Log.d(this.getClass().getSimpleName(), "method afterTextChanged: wrong input");
                }
            }
        });
    }
    public void rate2Et(EditText inputP2Rate, Button saveButton, int[] checkCorrectionArray, int[] userInputRateArray) {
        inputP2Rate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String p11 =inputP2Rate.getText().toString().trim();
                inputP2Rate.setTextColor(getColor(R.color.purple_700));
                checkCorrectionArray[1]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data
                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(checkCorrectionArray)) {//this is important if in field data is wrong then save button will not enabled until data is right.
                    saveButton.setVisibility(View.VISIBLE);

                }
                if (!p11.matches("[0-9]+")) {//space or , or - is restricted"[.]?[0-9]+[.]?[0-9]*"
                    inputP2Rate.setTextColor(Color.RED);
                    saveButton.setVisibility(View.GONE);
                    checkCorrectionArray[1]=2;//means data is inserted wrong
                    // Toast.makeText(IndividualPersonDetailActivity.this, "NOT ALLOWED(space  .  ,  -)\nPLEASE CORRECT", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                try{
                    userInputRateArray[1]  = Integer.parseInt(inputP2Rate.getText().toString().trim());

                }catch(Exception x){
                    Log.d(this.getClass().getSimpleName(), "method afterTextChanged: wrong input");
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
                String p11 =inputP3Rate.getText().toString().trim();
                inputP3Rate.setTextColor(getColor(R.color.purple_700));
                checkCorrectionArray[2]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data
                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(checkCorrectionArray)) {//this is important if in field data is wrong then save button will not enabled until data is right.
                    saveButton.setVisibility(View.VISIBLE);
                }
                if (!p11.matches("[0-9]+")) {//space or , or - is restricted"[.]?[0-9]+[.]?[0-9]*"
                    inputP3Rate.setTextColor(Color.RED);
                    saveButton.setVisibility(View.GONE);
                    checkCorrectionArray[2]=2;//means data is inserted wrong
                    //Toast.makeText(IndividualPersonDetailActivity.this, "NOT ALLOWED(space  .  ,  -)\nPLEASE CORRECT", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    userInputRateArray[2]= Integer.parseInt(inputP3Rate.getText().toString().trim());
                }catch (Exception e){
                    Log.d(this.getClass().getSimpleName(), "method afterTextChanged: wrong input");
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
                String p11 =inputP4Rate.getText().toString().trim();
                inputP4Rate.setTextColor(getColor(R.color.purple_700));
                checkCorrectionArray[3]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data
                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(checkCorrectionArray)) {//this is important if in field data is wrong then save button will not enabled until data is right.
                    saveButton.setVisibility(View.VISIBLE);
                }
                if (!p11.matches("[0-9]+")) {//space or , or - is restricted"[.]?[0-9]+[.]?[0-9]*"
                    inputP4Rate.setTextColor(Color.RED);
                    saveButton.setVisibility(View.GONE);
                    checkCorrectionArray[3]=2;//means data is inserted wrong
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    userInputRateArray[3] = Integer.parseInt(inputP4Rate.getText().toString().trim());
                }catch(Exception e){
                    Log.d(this.getClass().getSimpleName(), "method afterTextChanged: wrong input");
                }
            }
        });
    }
    public boolean fetchWorkDetailsCalculationAndWriteToPDF(String id, MakePdf makePdf) {
        try{
            byte indicator= (byte) MyUtility.get_indicator(getBaseContext(),id);
            boolean[] errorDetection={false};//when ever exception occur it will be updated to true in method so it indicate error occurred or not
            String[] skillHeader = MyUtility.getWagesHeadersFromDbBasedOnIndicator(getBaseContext(),id, indicator, errorDetection);//THIS SHOULD BE TOP at arrayOfTotalWagesDepositRateAccordingToIndicator   TO AVOID INDEX EXCEPTION
            float[] columnWidth=getColumnWidthBasedOnIndicator(indicator,errorDetection);
            int[] arrayOfTotalWagesDepositRateAccordingToIndicator= MyUtility.getSumOfTotalWagesDepositRateDaysWorkedBasedOnIndicator(getBaseContext(),id,indicator,errorDetection);//if error cause errorDetection will be set true
            String[][] recyclerViewWagesData = MyUtility.getAllWagesDetailsFromDbBasedOnIndicator(getBaseContext(),id, indicator, errorDetection);//it amy return null   when no data
            String[][] recyclerViewDepositData = MyUtility.getAllDepositFromDb(getBaseContext(),id,errorDetection);//it amy return null   when no data
            if(errorDetection[0]==false){
                if(!makeSummaryAndWriteToPDFBasedOnIndicator(indicator,id,makePdf,arrayOfTotalWagesDepositRateAccordingToIndicator)) return false;//summary
                if(!makePdf.writeSentenceWithoutLines(new String[]{""},new float[]{100f},true, (byte) 50,(byte)50)) return false;//just for 1 space

                           if (recyclerViewWagesData != null){//null means data not present
                                if(makePdf.makeTable(skillHeader, recyclerViewWagesData,columnWidth, 9, false)){
                                                               //getTotalOfWagesAndWorkingDaysFromDbBasedOnIndicator should be use after all wages displayed
                                    if(!makePdf.singleCustomRow(getTotalOfWagesAndWorkingDaysFromDbBasedOnIndicator(indicator, errorDetection, arrayOfTotalWagesDepositRateAccordingToIndicator), columnWidth, 0, Color.rgb(221, 133, 3), 0, 0, true, (byte) 0, (byte) 0)){
                                        return false;
                                    }
                                }else return false;
                             }

                if (recyclerViewDepositData != null) {//if deposit there then draw in pdf
                    if(makePdf.makeTable(new String[]{"DATE", "DEPOSIT", "REMARKS"}, recyclerViewDepositData, new float[]{12f, 12f, 76f}, 9, false)) {//[indicator + 1] is index of deposit
                        if(!makePdf.singleCustomRow(new String[]{"+", MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[indicator + 1]), "****TOTAL DEPOSIT****"}, new float[]{12f, 12f, 76f}, 0, 0, 0, 0, true, (byte) 0, (byte) 0)) {
                            return false;
                        }
                    }else return false;
                }
                if(!addWorkAmountAndDepositBasedOnIndicatorAndWriteToPDF(indicator, arrayOfTotalWagesDepositRateAccordingToIndicator, makePdf, skillHeader)) {return false;}
            }else return false;//means error has occurred

            return true;
        }catch(Exception ex){
            ex.printStackTrace();
             return false;
        }
    }
    public float[] getColumnWidthBasedOnIndicator(byte indicator,boolean[] errorDetection) {
        try{
            switch (indicator) {
                case 1: return new float[]{12f, 12f, 5f, 71f};

                case 2: return new float[]{12f, 12f, 5f, 5f, 66f};

                case 3: return new float[]{12f, 12f, 5f, 5f, 5f, 61f};

                case 4: return new float[]{12f, 12f, 5f, 5f, 5f, 5f, 56f};
            }
            return new float[]{1f,1f,1f};//this code will not execute due to return in switch block just using to avoid error
        }catch (Exception ex){
            ex.printStackTrace();
            Log.d(this.getClass().getSimpleName(),"exception occurred in method "+Thread.currentThread().getStackTrace()[2].getMethodName());
            errorDetection[0]=true;//indicate error has occur
            return new float[]{1f,1f,1f};//to avoid error
        }
    }
    public boolean makeSummaryAndWriteToPDFBasedOnIndicator(byte indicator,String id,MakePdf makePdf, int[] arrayOfTotalWagesDepositRateAccordingToIndicator) {
        try(Database db = new Database(getApplicationContext());
            Cursor cursor=db.getData("SELECT "+Database.COL_13_ADVANCE+" ,"+Database.COL_14_BALANCE+" FROM " + Database.TABLE_NAME1 + " WHERE "+Database.COL_1_ID+"= '" + id + "'"))
         {
             if(!makePdf.writeSentenceWithoutLines(new String[]{"SUMMARY","",""},new float[]{12f, 50f, 38f},false,(byte)50,(byte)50)) return false;

            cursor.moveToFirst();//means only one row is returned
            if (cursor.getInt(0) != 0 && cursor.getInt(1) == 0) {

               if(!makePdf.singleCustomRow(MyUtility.headersForSummaryBasedOnIndicator(indicator,arrayOfTotalWagesDepositRateAccordingToIndicator,"ADVANCE"),new float[]{25f, 50f, 25f},0,0,0,0,true,(byte)50,(byte)50)) return false;
                                                                                                                                                                                                                                          //yellow                               green
                if(!makePdf.singleCustomRow(MyUtility.totalWagesWorkAmountDepositAdvanceOrBalanceForSummaryBasedOnIndicator(indicator,arrayOfTotalWagesDepositRateAccordingToIndicator,cursor.getInt(0)),new float[]{25f, 50f, 25f},Color.rgb(221, 133, 3),Color.rgb(26,145,12) ,Color.RED,0,true,(byte)50,(byte)50)) return false;
                if(!makePdf.singleCustomRow(new String[]{ " *After calculation advance Rs. " + MyUtility.convertToIndianNumberSystem(cursor.getInt(0))},new float[]{100f},0,0 ,0,0,true,(byte)50,(byte)50)) return false;

            }else if (cursor.getInt(0) == 0 && cursor.getInt(1) != 0) {

                if(!makePdf.singleCustomRow(MyUtility.headersForSummaryBasedOnIndicator(indicator,arrayOfTotalWagesDepositRateAccordingToIndicator,"BALANCE"),new float[]{25f, 50f, 25f},0,0,0,0,true,(byte)50,(byte)50))return false;
                //                                                                                                                                                                                                                      yellow                               green                                    green
                if(!makePdf.singleCustomRow(MyUtility.totalWagesWorkAmountDepositAdvanceOrBalanceForSummaryBasedOnIndicator(indicator,arrayOfTotalWagesDepositRateAccordingToIndicator,cursor.getInt(1)),new float[]{25f, 50f, 25f},Color.rgb(221, 133, 3),Color.rgb(26,145,12) ,Color.rgb(26,145,12),0,true,(byte)50,(byte)50))return false;
                if(!makePdf.singleCustomRow(new String[]{ " *After calculation balance Rs. " +MyUtility.convertToIndianNumberSystem(cursor.getInt(1))},new float[]{100f},0,0 ,0,0,true,(byte)50,(byte)50))return false;

            }else if(cursor.getInt(0) == 0 && cursor.getInt(1) == 0){
                if(!makePdf.singleCustomRow(MyUtility.headersForSummaryBasedOnIndicator(indicator,arrayOfTotalWagesDepositRateAccordingToIndicator,"ALL CLEARED"),new float[]{25f, 50f, 25f},0,0,0,0,true,(byte)50,(byte)50))return false;
                                                                                                                                                                                                                                            // yellow                               green                                green
                if(!makePdf.singleCustomRow(MyUtility.totalWagesWorkAmountDepositAdvanceOrBalanceForSummaryBasedOnIndicator(indicator,arrayOfTotalWagesDepositRateAccordingToIndicator,0),new float[]{25f, 50f, 25f},Color.rgb(221, 133, 3),Color.rgb(26,145,12),Color.rgb(26,145,12),0,true,(byte)50,(byte)50))return false;
                if(!makePdf.singleCustomRow(new String[]{ " * After calculation all cleared Rs. 0"},new float[]{100f},0,0 ,0,0,true,(byte)50,(byte)50))return false;

            }
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
             return false;
        }
    }
    public boolean addWorkAmountAndDepositBasedOnIndicatorAndWriteToPDF(byte indicator,int[] sumArrayAccordingToIndicator, MakePdf makePdf,String[] skillAccordingToindicator) {
        try{
            switch(indicator){
                case 1: {
                    makePdf.singleCustomRow(new String[]{skillAccordingToindicator[2] + " =", sumArrayAccordingToIndicator[1] + "", "X", "RATE", MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[3])}, new float[]{15f, 20f, 12f, 20f, 33f}, 0, 0, 0, 0, false, (byte) 88, (byte) 88);
                    if (sumArrayAccordingToIndicator[2] == 0) {//DEPOSIT AMOUNT checking there or not or can be use (indicator+1) to get index of deposit
                        makePdf.singleCustomRow(new String[]{"TOTAL WORK AMOUNT =",  MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[3])}, new float[]{67f, 33f}, 0, Color.rgb(26,145,12), 0, 0, true, (byte) 88, (byte) 88);
                    } else {//when there is deposit then add deposit
                        makePdf.singleCustomRow(new String[]{"TOTAL DEPOSIT =", MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[2])}, new float[]{67f, 33f}, 0, 0, 0, 0, true, (byte) 88, (byte) 88);
                         //                                                                                                                                                                                                                                                                           green color
                        makePdf.singleCustomRow(new String[]{"TOTAL DEPOSIT+WORK AMOUNT=",  MyUtility.convertToIndianNumberSystem((sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[3]) + sumArrayAccordingToIndicator[2])}, new float[]{67f, 33f}, 0, Color.rgb(26,145,12), 0, 0, true, (byte) 88, (byte) 88);
                    }
                }break;
                case 2: {
                    makePdf.singleCustomRow(new String[]{skillAccordingToindicator[2] + " =", sumArrayAccordingToIndicator[1] + "", "X", "RATE",MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[4])}, new float[]{15f, 20f, 12f, 20f, 33f}, 0, 0, 0, 0, false,(byte) 88, (byte) 88);
                    makePdf.singleCustomRow(new String[]{skillAccordingToindicator[3] + " =", sumArrayAccordingToIndicator[2] + "", "X", "RATE",MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[2] * sumArrayAccordingToIndicator[5])}, new float[]{15f, 20f, 12f, 20f, 33f}, 0, 0, 0, 0, true, (byte) 88, (byte) 88);
                    if (sumArrayAccordingToIndicator[3] == 0) {//DEPOSIT AMOUNT checking there or not
                        //                                                                                                                                      P1*R1                              +                             P2*R2
                        makePdf.singleCustomRow(new String[]{"TOTAL WORK AMOUNT =", MyUtility.convertToIndianNumberSystem((sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[4]) + (sumArrayAccordingToIndicator[2] * sumArrayAccordingToIndicator[5]))}, new float[]{67f, 33f}, 0, Color.rgb(26,145,12), 0, 0, true, (byte) 88, (byte) 88);
                    }else{//when there is deposit then add deposit
                        makePdf.singleCustomRow(new String[]{"TOTAL DEPOSIT =", MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[3])}, new float[]{67f, 33f}, 0, 0, 0, 0, true, (byte) 88, (byte) 88);
                        //                       P1*R1                             +                             P2*R2                             +  DEPOSIT
                        makePdf.singleCustomRow(new String[]{"TOTAL DEPOSIT+WORK AMOUNT=",MyUtility.convertToIndianNumberSystem((sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[4]) + (sumArrayAccordingToIndicator[2] * sumArrayAccordingToIndicator[5]) + sumArrayAccordingToIndicator[3])}, new float[]{67f, 33f}, 0, Color.rgb(26,145,12), 0, 0, true, (byte) 88, (byte) 88);
                    }
                }break;
                case 3:{
                    makePdf.singleCustomRow(new String[]{skillAccordingToindicator[2] + " =", sumArrayAccordingToIndicator[1] + "", "X", "RATE", MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[5])}, new float[]{15f, 20f, 12f, 20f, 33f}, 0, 0, 0, 0, false,(byte) 88, (byte) 88);
                    makePdf.singleCustomRow(new String[]{skillAccordingToindicator[3] + " =", sumArrayAccordingToIndicator[2] + "", "X", "RATE", MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[2] * sumArrayAccordingToIndicator[6])}, new float[]{15f, 20f, 12f, 20f, 33f}, 0, 0, 0, 0, true, (byte) 88, (byte) 88);
                    makePdf.singleCustomRow(new String[]{skillAccordingToindicator[4] + " =", sumArrayAccordingToIndicator[3] + "", "X", "RATE", MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[3] * sumArrayAccordingToIndicator[7])}, new float[]{15f, 20f, 12f, 20f, 33f}, 0, 0, 0, 0, true, (byte) 88, (byte) 88);

                    if (sumArrayAccordingToIndicator[4] == 0) {//DEPOSIT AMOUNT checking there or not
                        makePdf.singleCustomRow(new String[]{"TOTAL WORK AMOUNT =",MyUtility.convertToIndianNumberSystem((sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[5]) + (sumArrayAccordingToIndicator[2] * sumArrayAccordingToIndicator[6]) + (sumArrayAccordingToIndicator[3] * sumArrayAccordingToIndicator[7]))}, new float[]{67f, 33f}, 0, Color.rgb(26,145,12), 0, 0, true, (byte) 88, (byte) 88);
                    }else{ //when there is deposit then add deposit
                        makePdf.singleCustomRow(new String[]{"TOTAL DEPOSIT =", MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[4])}, new float[]{67f, 33f}, 0, 0, 0, 0, true, (byte) 88, (byte) 88);
                        //                                                                                P1*R1                             +                                P2*R2                                                           P3*R3                                              +  DEPOSIT
                        makePdf.singleCustomRow(new String[]{"TOTAL DEPOSIT+WORK AMOUNT=",MyUtility.convertToIndianNumberSystem((sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[5]) + (sumArrayAccordingToIndicator[2] * sumArrayAccordingToIndicator[6] + (sumArrayAccordingToIndicator[3] * sumArrayAccordingToIndicator[7])) + sumArrayAccordingToIndicator[4])}, new float[]{67f, 33f}, 0,Color.rgb(26,145,12), 0, 0, true, (byte) 88, (byte) 88);
                    }
                }break;
                case 4:{
                    makePdf.singleCustomRow(new String[]{skillAccordingToindicator[2] + " =", sumArrayAccordingToIndicator[1] + "", "X", "RATE",MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[6])}, new float[]{15f, 20f, 12f, 20f, 33f}, 0, 0, 0, 0, false,(byte) 88, (byte) 88);
                    makePdf.singleCustomRow(new String[]{skillAccordingToindicator[3] + " =", sumArrayAccordingToIndicator[2] + "", "X", "RATE",MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[2] * sumArrayAccordingToIndicator[7])}, new float[]{15f, 20f, 12f, 20f, 33f}, 0, 0, 0, 0, true, (byte) 88, (byte) 88);
                    makePdf.singleCustomRow(new String[]{skillAccordingToindicator[4] + " =", sumArrayAccordingToIndicator[3] + "", "X", "RATE",MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[3] * sumArrayAccordingToIndicator[8])}, new float[]{15f, 20f, 12f, 20f, 33f}, 0, 0, 0, 0, true, (byte) 88, (byte) 88);
                    makePdf.singleCustomRow(new String[]{skillAccordingToindicator[5] + " =", sumArrayAccordingToIndicator[4] + "", "X", "RATE",MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[4] * sumArrayAccordingToIndicator[9])}, new float[]{15f, 20f, 12f, 20f, 33f}, 0, 0, 0, 0, true, (byte) 88, (byte) 88);

                    if(sumArrayAccordingToIndicator[5] == 0) {//DEPOSIT AMOUNT checking there or not
                        makePdf.singleCustomRow(new String[]{"TOTAL WORK AMOUNT =",MyUtility.convertToIndianNumberSystem((sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[6]) + (sumArrayAccordingToIndicator[2] * sumArrayAccordingToIndicator[7]) + (sumArrayAccordingToIndicator[3] * sumArrayAccordingToIndicator[8]) + (sumArrayAccordingToIndicator[4] * sumArrayAccordingToIndicator[9]))}, new float[]{67f, 33f}, 0,Color.rgb(26,145,12), 0, 0, true, (byte) 88, (byte) 88);
                    }else{//when there is deposit then add deposit
                        makePdf.singleCustomRow(new String[]{"TOTAL DEPOSIT =",MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[5])}, new float[]{67f, 33f}, 0, 0, 0, 0, true, (byte) 88, (byte) 88 );
                        //                                                                                P1*R1                             +                                P2*R2                                                           P3*R3                                                                           P4*R4                                  +  DEPOSIT
                        makePdf.singleCustomRow(new String[]{"TOTAL DEPOSIT+WORK AMOUNT=",MyUtility.convertToIndianNumberSystem((sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[6]) + (sumArrayAccordingToIndicator[2] * sumArrayAccordingToIndicator[7] + (sumArrayAccordingToIndicator[3] * sumArrayAccordingToIndicator[8]) + (sumArrayAccordingToIndicator[4] * sumArrayAccordingToIndicator[9])) + sumArrayAccordingToIndicator[5])}, new float[]{67f, 33f}, 0,Color.rgb(26,145,12), 0, 0, true, (byte) 88, (byte) 88);
                    }
                }break;
            }
            return true;
    }catch(Exception ex){
        ex.printStackTrace();
        return false;
    }
    }
    public String[] getTotalOfWagesAndWorkingDaysFromDbBasedOnIndicator(byte indicator, boolean[] errorDetection,int[] arrayOfTotalWagesDepositRateAccordingToIndicator) {// when no data and if error errorDetection will be set to true
        try{
                switch (indicator) {
                    case 1: return new String[]{"+",MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[0]), arrayOfTotalWagesDepositRateAccordingToIndicator[1]+"" ,"****TOTAL****"};

                    case 2: return new String[]{"+", MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[0]), arrayOfTotalWagesDepositRateAccordingToIndicator[1]+"",arrayOfTotalWagesDepositRateAccordingToIndicator[2]+"" ,"****TOTAL****"};

                    case 3: return new String[]{"+", MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[0]), arrayOfTotalWagesDepositRateAccordingToIndicator[1]+"",arrayOfTotalWagesDepositRateAccordingToIndicator[2]+"",arrayOfTotalWagesDepositRateAccordingToIndicator[3]+"" ,"****TOTAL****"};

                    case 4: return new String[]{"+", MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[0]), arrayOfTotalWagesDepositRateAccordingToIndicator[1]+"",arrayOfTotalWagesDepositRateAccordingToIndicator[2]+"",arrayOfTotalWagesDepositRateAccordingToIndicator[3]+"",arrayOfTotalWagesDepositRateAccordingToIndicator[4]+"" ,"****TOTAL****"};
                }
                return new String[]{"no indicator"};//this code will not execute due to return in switch block just using to avoid error
        }catch (Exception ex){
            ex.printStackTrace();
            System.out.println("error occurred in getTotalOfWagesAndWorkingDaysFromDbBasedOnIndicator method**************************");
            errorDetection[0]=true;//indicate error has occur
            return new String[]{"error occurred"};//to avoid error
        }
    }
    public boolean fetchOrganizationDetailsAndWriteToPDF(MakePdf makePdf) {
        try{
            makePdf.makeTopHeaderOrganizationDetails("RRD Construction Work","GSTIN-123456789123456789", "9436018408", "7005422684", "rrdconstructionbench@gmail.com",false);
            return true;
        }catch(Exception ex){
            ex.printStackTrace();
           return false;
        }
    }
    public boolean fetchPersonDetailAndWriteToPDF(String id, MakePdf makePdf) {
        try (Database db=new Database(getApplicationContext());
             Cursor cursor1 = db.getData("SELECT " + Database.COL_2_NAME + " , " + Database.COL_3_BANKAC + " , " + Database.COL_6_AADHAAR_NUMBER + " , " + Database.COL_10_IMAGE + " FROM " + Database.TABLE_NAME1 + " WHERE "+Database.COL_1_ID+"='" + id + "'");
             Cursor cursor2 = db.getData("SELECT " + Database.COL_396_PDFSEQUENCE + " FROM " + Database.TABLE_NAME3 + " WHERE "+Database.COL_31_ID+"= '" + id + "'")){
            if (cursor1 != null){
                cursor1.moveToFirst();
                String bankAccount, aadhaar;
                int pdfSequenceNo;

                if (cursor1.getString(1).length() > 4) {
                    bankAccount = cursor1.getString(1).substring(cursor1.getString(1).length() - 4);
                } else {
                    bankAccount = "";
                }
                if (cursor1.getString(2).length() > 5) {
                    aadhaar = cursor1.getString(2).substring(cursor1.getString(2).length() - 5);
                } else {
                    aadhaar = "";
                }

                if (cursor2 != null) {//this make filename unique
                    cursor2.moveToFirst();
                    pdfSequenceNo = (cursor2.getInt(0) + 1); /*pdf sequence in db is updated when pdf is generated successfully so for now increasing manually NOT UPDATING so that if pdf generation is failed sequence should not be updated in db*/
                } else {
                    pdfSequenceNo = -1;//if error
                }

                String activePhoneNumber=MyUtility.getActivePhoneNumbersFromDb(id,getApplicationContext());
                if(activePhoneNumber != null){
                    activePhoneNumber= activePhoneNumber.substring(activePhoneNumber.length() - 6);//phone number
                }else{
                    activePhoneNumber="";
                }
                makePdf.makePersonImageDetails(cursor1.getString(0), id, bankAccount, aadhaar, cursor1.getBlob(3), String.valueOf(pdfSequenceNo),activePhoneNumber, false);
            }else{
                Toast.makeText(IndividualPersonDetailActivity.this, "NO DATA IN CURSOR", Toast.LENGTH_LONG).show();
                makePdf.makePersonImageDetails("[NULL NO DATA IN CURSOR]", "[NULL NO DATA IN CURSOR]", "[NULL NO DATA IN CURSOR]", "[NULL NO DATA IN CURSOR]", null, "[NULL]","[NULL NO DATA IN CURSOR]",false);
            }
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
             return false;
        }
    }
    private void indicator1234CalculateAndUpdate(Cursor sumCursor, int rate1IntoSump1, int rate2IntoSump2, int rate3IntoSump3, int rate4IntoSump4) {
        boolean bool;
        int  totalDeposit,totalWages;
        int totalr1r2r3r4sum1sum2sum3sum4=rate1IntoSump1+rate2IntoSump2+rate3IntoSump3+rate4IntoSump4;
        totalWages=sumCursor.getInt(0);
        totalDeposit=sumCursor.getInt(5);

        if(((totalDeposit + totalr1r2r3r4sum1sum2sum3sum4) < 0) || (totalr1r2r3r4sum1sum2sum3sum4 < 0) || (totalDeposit < 0)) //user cant enter negative number so when (totalDeposit + (totalr1r2r3r4sum1sum2sum3sum4)) is negative that means int range is exceeds so wrong result will be shown
            Toast.makeText(this,getResources().getString(R.string.value_out_of_range_please_check_total_work_amount), Toast.LENGTH_LONG).show();

        binding.workTotalAmountTv.setText(" - " + MyUtility.convertToIndianNumberSystem(totalDeposit + (totalr1r2r3r4sum1sum2sum3sum4)));
        //    totalDeposit+(R1*SUMP1)+(R2*SUMP2)+(R3*SUMP3)+(R4*SUMP4)
        if ((totalDeposit + (totalr1r2r3r4sum1sum2sum3sum4)) < totalWages) {
            binding.advanceOrBalanceTv.setTextColor(Color.RED);
            //                                        total wages -   totalDeposit+(R1*SUMP1)+(R2*SUMP2)+(R3*SUMP3)+(R4*SUMP4)
            binding.advanceOrBalanceTv.setText("= " + MyUtility.convertToIndianNumberSystem(totalWages - (totalDeposit + (totalr1r2r3r4sum1sum2sum3sum4))));

            //updating Advance to db                                                    total wages -   totalDeposit+(R1*SUMP1)+(R2*SUMP2)+(R3*SUMP3)+(R4*SUMP4)
            bool = db.updateTable("UPDATE " + Database.TABLE_NAME1 + " SET "+Database.COL_13_ADVANCE+"='" + (totalWages - (totalDeposit + (totalr1r2r3r4sum1sum2sum3sum4))) + "'" + "WHERE "+Database.COL_1_ID+"='" + fromIntentPersonId + "'");
            if(bool){
                /*Situation when user first enter jama /totalDeposit amount then wages amount which is greater then jama amount then balance column should be updated otherwise advance column will have amount and balance column will also have amount so when there is advance then balance should be 0.*/
                bool = db.updateTable("UPDATE " + Database.TABLE_NAME1 + " SET "+Database.COL_14_BALANCE+"='" + 0 + "'" + "WHERE "+Database.COL_1_ID+"='" + fromIntentPersonId + "'");
                if (bool == false)
                    Toast.makeText(this, "BALANCE AMOUNT NOT UPDATED TO DATABASE", Toast.LENGTH_LONG).show();
            }
            //else if (bool == false)
            else {
                Toast.makeText(this, "ADVANCE AMOUNT NOT UPDATED TO DATABASE", Toast.LENGTH_LONG).show();
            }
            //totalDeposit+(R1*SUMP1)+(R2*SUMP2)+(R3*SUMP3)+(R4*SUMP4) >= totalWages
        }else if((totalDeposit + (totalr1r2r3r4sum1sum2sum3sum4)) >= totalWages) {//>= is given because of green color and when calculation is 0
            binding.advanceOrBalanceTv.setTextColor(getColor(R.color.green));
            //                                           totalDeposit+(R1*SUMP1)+(R2*SUMP2)+(R3*SUMP3)+(R4*SUMP4) -total wages
            binding.advanceOrBalanceTv.setText("= " + MyUtility.convertToIndianNumberSystem((totalDeposit + (totalr1r2r3r4sum1sum2sum3sum4)) - totalWages));

            //updating balance to db if greater then or equal to 0
            bool = db.updateTable("UPDATE " + Database.TABLE_NAME1 + " SET "+Database.COL_14_BALANCE+"='" + ((totalDeposit + (totalr1r2r3r4sum1sum2sum3sum4)) -totalWages) + "'" + "WHERE "+Database.COL_1_ID+"='" + fromIntentPersonId + "'");
            if(bool){
                //if there is balance then update advance column should be 0
                bool = db.updateTable("UPDATE " + Database.TABLE_NAME1 + " SET "+Database.COL_13_ADVANCE+"='" + 0 + "'" + "WHERE "+Database.COL_1_ID+"='" + fromIntentPersonId + "'");
                if (bool == false)
                    Toast.makeText(this, "ADVANCE AMOUNT NOT UPDATED TO DATABASE", Toast.LENGTH_LONG).show();
            }
            //else if(bool == false)
            else {
                Toast.makeText(this, "BALANCE AMOUNT NOT UPDATED TO DATABASE", Toast.LENGTH_LONG).show();
            }
        }
    }
    private void insertDataToRecyclerView_AlertDialogBox(int indicator) {
        AlertDialog.Builder myCustomDialog=new AlertDialog.Builder(IndividualPersonDetailActivity.this);
        LayoutInflater inflater=LayoutInflater.from(IndividualPersonDetailActivity.this);

        View myView=inflater.inflate(R.layout.input_data_to_recycler,null);//myView contain all layout view ids
        myCustomDialog.setView(myView);//set custom layout to alert dialog
        myCustomDialog.setCancelable(false);//if user touch to other place then dialog will not be close

        final AlertDialog customDialog=myCustomDialog.create();//myCustomDialog variable cannot be use in inner class so creating another final variable  to use in inner class

        mediaRecorder=null;//so that it not take previous VALUE
        audioPath=null;//so that it not take previous VALUE

        TextView deposit_btn_tv=myView.findViewById(R.id.to_deposit_tv);
        TextView hardcodedP1=myView.findViewById(R.id.hardcoded_p1_tv);
        TextView hardcodedP2=myView.findViewById(R.id.hardcoded_p2_tv);
        TextView hardcodedP3=myView.findViewById(R.id.hardcoded_p3_tv);
        TextView hardcodedP4=myView.findViewById(R.id.hardcoded_p4_tv);
        TextView micIcon=myView.findViewById(R.id.mic_tv);
       // TextView dateIcon=myView.findViewById(R.id.date_icon_tv);
        TextView advanceOrBalanceWarring=myView.findViewById(R.id.advance_or_balance_amount_warring_tv);
        TextView noOfDaysToWork=myView.findViewById(R.id.no_of_days);
        TextView inputDate=myView.findViewById(R.id.input_date_tv);
       // TextView inputTime=myView.findViewById(R.id.input_time_tv);
        TextView saveAudio=myView.findViewById(R.id.save_audio_tv);

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
        EditText toGive_Amount=myView.findViewById(R.id.wages_et);
        EditText description=myView.findViewById(R.id.enter_description_et);
        Button save=myView.findViewById(R.id.save_btn);
        save.setVisibility(View.GONE);//initially save button is disabled it is enabled when user enter any data and its important otherwise app crash
        Button cancel=myView.findViewById(R.id.cancel_btn);

        //***********************setting no of days and warning Total advance amount********************************************
        Cursor  advanceAmountCursor=db.getData("SELECT "+Database.COL_13_ADVANCE+" , "+Database.COL_14_BALANCE+" FROM " + Database.TABLE_NAME1 + " WHERE "+Database.COL_1_ID+"= '" + fromIntentPersonId +"'");
        advanceAmountCursor.moveToFirst();
        if(advanceAmountCursor.getInt(0) > 0) {//advance
            advanceOrBalanceWarring.setTextColor(Color.RED);
            advanceOrBalanceWarring.setText(advanceAmountCursor.getString(0));

            Cursor sum1DayAmountCursor=db.getData("SELECT  "+Database.COL_32_R1+","+Database.COL_33_R2+","+Database.COL_34_R3+","+Database.COL_35_R4+" FROM " + Database.TABLE_NAME3 + " WHERE "+Database.COL_31_ID+"= '" + fromIntentPersonId +"'");
                   sum1DayAmountCursor.moveToFirst();
           // int howManyPerson=get_indicator(fromIntentPersonId);
            int howManyPerson=MyUtility.get_indicator(getBaseContext(),fromIntentPersonId);
             if(howManyPerson==1) {
                 if(sum1DayAmountCursor.getInt(0) !=0) {//to avoid arithmetic exception 1/0
                                                                   //total advance / r1
                     noOfDaysToWork.setText(String.valueOf(advanceAmountCursor.getInt(0) / sum1DayAmountCursor.getInt(0)));
                 }
             }else if(howManyPerson==2){//to avoid arithmetic exception 1/0
                 if(sum1DayAmountCursor.getInt(0)+sum1DayAmountCursor.getInt(1) != 0) {
                           //                                         total advance/(R1+R1)
                     noOfDaysToWork.setText(String.valueOf(advanceAmountCursor.getInt(0) / (sum1DayAmountCursor.getInt(0) + sum1DayAmountCursor.getInt(1))));
                 }
             }else if(howManyPerson==3){//to avoid arithmetic exception 1/0
                 if(sum1DayAmountCursor.getInt(0)+sum1DayAmountCursor.getInt(1)+sum1DayAmountCursor.getInt(2) != 0) {
                     //                                               total advance/(R1+R2+R3)
                     noOfDaysToWork.setText(String.valueOf(advanceAmountCursor.getInt(0) / (sum1DayAmountCursor.getInt(0) + sum1DayAmountCursor.getInt(1) + sum1DayAmountCursor.getInt(2))));
                 }
             }else if(howManyPerson==4){//to avoid arithmetic exception 1/0
                 if(sum1DayAmountCursor.getInt(0)+sum1DayAmountCursor.getInt(1)+sum1DayAmountCursor.getInt(2)+sum1DayAmountCursor.getInt(3) != 0) {
                     //                                                total advance/(R1+R2+R3+R4)
                     noOfDaysToWork.setText(String.valueOf(advanceAmountCursor.getInt(0) / (sum1DayAmountCursor.getInt(0) + sum1DayAmountCursor.getInt(1) + sum1DayAmountCursor.getInt(2) + sum1DayAmountCursor.getInt(3))));
                 }
             }
            sum1DayAmountCursor.close();
        }else if(advanceAmountCursor.getInt(1) >= 0 ){//balance
            advanceOrBalanceWarring.setTextColor(getColor(R.color.green));
            advanceOrBalanceWarring.setText(advanceAmountCursor.getString(1));
        }
        advanceAmountCursor.close();
        //***********************done setting no of days and warring Total advance amount********************************************

        deposit_btn_tv.setOnLongClickListener(view -> {
            Intent intent=new Intent(IndividualPersonDetailActivity.this,CustomizeLayoutOrDepositAmount.class);
            intent.putExtra("ID",fromIntentPersonId);
            customDialog.dismiss();//while going to other activity dismiss dialog otherwise window leak
            finish();//while going to other activity so destroy  this current activity so that while coming back we will see refresh activity
            startActivity(intent);
            return false;
        });

        //to automatically set date to textView
        final Calendar current=Calendar.getInstance();//to get current date and time
        //int cYear=current.get(Calendar.YEAR);
       // int cMonth=current.get(Calendar.MONTH);
       // int cDayOfMonth=current.get(Calendar.DAY_OF_MONTH);
        inputDate.setText(current.get(Calendar.DAY_OF_MONTH)+"-"+(current.get(Calendar.MONTH)+1)+"-"+current.get(Calendar.YEAR));
        inputDate.setOnClickListener(view -> {
            //To show calendar dialog
            DatePickerDialog datePickerDialog=new DatePickerDialog(IndividualPersonDetailActivity.this, (datePicker, year, month, dayOfMonth) -> {
                inputDate.setText(dayOfMonth+"-"+(month+1)+"-"+year);//month start from 0 so 1 is added to get right month like 12
            },current.get(Calendar.YEAR),current.get(Calendar.MONTH),current.get(Calendar.DAY_OF_MONTH));//This variable should be ordered this variable will set date day month to calendar to datePickerDialog so passing it
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
        Cursor cursorDefault=db.getData("SELECT "+Database.COL_8_MAINSKILL1 +" FROM " + Database.TABLE_NAME1 + " WHERE "+Database.COL_1_ID+"= '" + fromIntentPersonId +"'");//for sure it will return type or skill
        cursorDefault.moveToFirst();//no need to check  cursorDefault !=null because for sure TYPE data is present
        hardcodedP1.setText(cursorDefault.getString(0));
        cursorDefault.close();

        Cursor skillsCursor=db.getData("SELECT "+Database.COL_36_SKILL2 +","+Database.COL_37_SKILL3 +","+Database.COL_38_SKILL4 +" FROM " + Database.TABLE_NAME3 + " WHERE "+Database.COL_31_ID+"= '" + fromIntentPersonId +"'");
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
//            Date d=Calendar.getInstance().getTime();
//            SimpleDateFormat sdf=new SimpleDateFormat("hh:mm:ss a");//a stands for is AM or PM
//            String onlyTime = sdf.format(d);

//            String onlyTime = MyUtility.getOnlyTime();
//            inputTime.setText(onlyTime);//setting time to take time and store in db
//            String time=inputTime.getText().toString();//time will be inserted automatically

            String time = MyUtility.getOnlyTime();

           // final Calendar current=Calendar.getInstance();//to get current date
           // String currentDate =current.get(Calendar.DAY_OF_MONTH)+"-"+(current.get(Calendar.MONTH)+1)+"-"+current.get(Calendar.YEAR);
            //db.updateTable("UPDATE " + db.TABLE_NAME1 + " SET  LATESTDATE='" + currentDate + "'" +" WHERE ID='" + fromIntentPersonId + "'");////when ever user insert its wages or deposit or update then latest date will be updated to current date not user entered date
           // db.updateTable("UPDATE " + Database.TABLE_NAME1 + " SET "+Database.COL_12_ACTIVE+"='" + 1 + "'"+" , "+Database.COL_15_LATESTDATE+"='" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+"-"+(current.get(Calendar.MONTH)+1)+"-"+current.get(Calendar.YEAR) + "' , "+Database.COL_16_TIME+"='"+onlyTime+"' WHERE "+Database.COL_1_ID+"='" + fromIntentPersonId + "'");////when ever user insert its wages or deposit or update then latest date will be updated to current date

           // db.updateTable("UPDATE " + Database.TABLE_NAME1 + " SET "+Database.COL_12_ACTIVE+"='" + 1 + "'"+" , "+Database.COL_15_LATESTDATE+"='" + MyUtility.getOnlyCurrentDate() + "' , "+Database.COL_16_TIME+"='"+time+"' WHERE "+Database.COL_1_ID+"='" + fromIntentPersonId + "'");////when ever user insert its wages or deposit or update then latest date will be updated to current date


//            if(!db.activateIdWithLatestDate(fromIntentPersonId,time)){
//                Toast.makeText(this, "FAILED TO MAKE ID ACTIVE", Toast.LENGTH_LONG).show();
//            }

            if(audioPath !=null){//if file is not null then only it execute otherwise nothing will be inserted
                micPath=audioPath;
                arr[5]=1;//1 means data present
             }
            else
                arr[5]=0;// 0 means data not present

            if(description.getText().toString().length() >=1){//to prevent null pointer exception
                remarks="["+time+getResources().getString(R.string.hyphen_entered)+"\n\n"+description.getText().toString().trim();//time is set automatically to remarks if user enter any remarks
                arr[6]=1;//means data present
            }
            else {//if user don't enter anything then time will set automatically
                remarks="["+time+getResources().getString(R.string.hyphen_automatic_entered);
                arr[6] = 0;
            }
            boolean isWrongData, isDataPresent;
              isWrongData= MyUtility.isEnterDataIsWrong(arr);
              isDataPresent= MyUtility.isDataPresent(arr);
            if(isDataPresent==true && isWrongData==false ) {//means if data is present then check is it right data or not .if condition is false then default value will be taken
                if (toGive_Amount.getText().toString().length() >= 1) {//to prevent null pointer exception
                    wages = Integer.parseInt(toGive_Amount.getText().toString().trim());
                }
                //>= if user enter only one digit then >= is important otherwise default value will be set
                if(inputP1.getText().toString().length() >=1) {//to prevent null pointer exception
                    p1 = Integer.parseInt(inputP1.getText().toString().trim());//converted to float and stored
                }
            }
            //*********************************  all the upper code are common to all indicator 1,2,3,4*******************
           //  db.updateTable("UPDATE " + db.TABLE_NAME1 + " SET ACTIVE='" + 1 + "'" +" WHERE ID='" + fromIntentPersonId + "'");//when ever user insert data then that person will become active.It will work for all
            if(indicator==1){
                if (isDataPresent == true && isWrongData == false) {//it is important means if data is present then check is it right data or not.if condition is false then this message will be displayed "Correct the Data or Cancel and Enter again"
                    //insert to database
                      //success = db.insert_1_Person_WithWagesTable2(fromIntentPersonId, date,time, micPath, remarks, wages, p1, "0");
                    //success=db.insertWagesOrDepositOnlyToActiveTableTransaction(fromIntentPersonId,date,time,micPath,remarks,wages,p1,0,0,0,0,"0");
                    if(!db.insertWagesOrDepositOnlyToActiveTableTransaction(fromIntentPersonId,date,time,micPath,remarks,wages,p1,0,0,0,0,"0")){
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
                    if (inputP2.getText().toString().length() >= 1) {//to prevent null pointer exception
                        p2 = Integer.parseInt(inputP2.getText().toString().trim());//converted to float and stored
                    }
                    //insert to database
                     // success = db.insert_2_Person_WithWagesTable2(fromIntentPersonId, date,time, micPath, remarks, wages, p1, p2, "0");
                   // success=db.insertWagesOrDepositOnlyToActiveTableTransaction(fromIntentPersonId,date,time,micPath,remarks,wages,p1,p2,0,0,0,"0");
                    if(!db.insertWagesOrDepositOnlyToActiveTableTransaction(fromIntentPersonId,date,time,micPath,remarks,wages,p1,p2,0,0,0,"0")){
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
                    if (inputP2.getText().toString().length() >= 1) {//to prevent null pointer exception
                        p2 = Integer.parseInt(inputP2.getText().toString().trim());//converted to float and stored
                    }
                    if (inputP3.getText().toString().length() >= 1) {//to prevent null pointer exception
                        p3 = Integer.parseInt(inputP3.getText().toString().trim());//converted to float and stored
                    }
                    //insert to database
                      //success = db.insert_3_Person_WithWagesTable2(fromIntentPersonId, date,time, micPath, remarks, wages, p1, p2, p3, "0");
                   // success=db.insertWagesOrDepositOnlyToActiveTableTransaction(fromIntentPersonId,date,time,micPath,remarks,wages,p1,p2,p3,0,0,"0");
                    if(!db.insertWagesOrDepositOnlyToActiveTableTransaction(fromIntentPersonId,date,time,micPath,remarks,wages,p1,p2,p3,0,0,"0")){
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
                    if (inputP2.getText().toString().length() >= 1) {//to prevent null pointer exception.If user do not enter any data then that time it will save from crashing app.So due to this condition if field is empty then default value will be taken
                        p2 = Integer.parseInt(inputP2.getText().toString().trim());//converted to float and stored
                    }
                    if (inputP3.getText().toString().length() >= 1) {//to prevent null pointer exception
                        p3 = Integer.parseInt(inputP3.getText().toString().trim());//converted to float and stored
                    }
                    if (inputP4.getText().toString().length() >= 1) {//to prevent null pointer exception
                        p4 = Integer.parseInt(inputP4.getText().toString().trim());//converted to float and stored
                    }
                    //insert to database
                        //success = db.insert_4_Person_WithWagesTable2(fromIntentPersonId, date, time, micPath, remarks, wages, p1, p2, p3, p4, "0");

                   // success = db.insertWagesOrDepositOnlyToActiveTableTransaction(fromIntentPersonId, date, time, micPath, remarks, wages, p1, p2, p3, p4, 0, "0");

                    if(!db.insertWagesOrDepositOnlyToActiveTableTransaction(fromIntentPersonId, date, time, micPath, remarks, wages, p1, p2, p3, p4, 0, "0")){
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
            if(MyUtility.checkPermissionAudioAndExternal(getBaseContext())){//checking for permission
                if (toggleToStartRecording) {//initially false

                    save.setVisibility(View.GONE);
                    deposit_btn_tv.setEnabled(false);

                    playAudioChronometer.setEnabled(false);//when user press save button then set to true playAudioChronometer.setEnabled(true);
                    saveAudio.setBackgroundResource(R.drawable.ic_green_sharp_done_sharp_tick_20);//changing tick color to green so that user can feel to press to save
                    micIcon.setEnabled(false);
                    micIcon.setBackgroundResource(R.drawable.black_sharp_mic_24);//change color when user click

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
                Toast.makeText(IndividualPersonDetailActivity.this, getResources().getString(R.string.audio_permission_required), Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(IndividualPersonDetailActivity.this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 21);
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
                if(!MyUtility.isEnterDataIsWrong(arr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
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
            MyUtility.deletePdfOrRecordingFromDevice(audioPath);//delete Audio If Not user Saved
            customDialog.dismiss();
        });
        customDialog.show();
        toGive_Amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String amount=toGive_Amount.getText().toString().trim();
                toGive_Amount.setTextColor(Color.BLACK);
                arr[4]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data

                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(arr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    save.setVisibility(View.VISIBLE);
                 }

                if(!amount.matches("[0-9]+")){//no space or . or ,
                    //Toast.makeText(IndividualPersonDetailActivity.this, "NOT ALLOWED(space  .  ,  -)\nPlease Correct", Toast.LENGTH_LONG).show();
                    toGive_Amount.setTextColor(Color.RED);
                    save.setVisibility(View.GONE);
                    arr[4]=2;//means wrong data
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
        inputP1.addTextChangedListener(new TextWatcher() {
            Cursor result =db.getData("SELECT  "+Database.COL_32_R1+","+Database.COL_33_R2+","+Database.COL_34_R3+","+Database.COL_35_R4+"  FROM " + Database.TABLE_NAME3 + " WHERE "+Database.COL_31_ID+"= '" + fromIntentPersonId +"'");
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String p11= inputP1.getText().toString().trim();
                inputP1.setTextColor(Color.BLACK);
                arr[0]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data

                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(arr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    save.setVisibility(View.VISIBLE);
                }
                if(!p11.matches("[0-9]+")){//"[.]?[0-9]+[.]?[0-9]*" for float
                    inputP1.setTextColor(Color.RED);
                    save.setVisibility(View.GONE);
                    arr[0]=2;//means wrong data
                     //Toast.makeText(IndividualPersonDetailActivity.this, "NOT ALLOWED(space  .  ,  -)\nPLEASE CORRECT", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {//after text changed for suggestion calculate based on previous rate
                result.moveToFirst();
                p1_p2_p3_p4_Change_Tracker(result,inputP1,inputP2,inputP3,inputP4,runtimeSuggestionAmountToGive);
            }
        });
        inputP2.addTextChangedListener(new TextWatcher() {
            Cursor result=db.getData("SELECT  "+Database.COL_32_R1+","+Database.COL_33_R2+","+Database.COL_34_R3+","+Database.COL_35_R4+"  FROM " + Database.TABLE_NAME3 + " WHERE "+Database.COL_31_ID+"= '" + fromIntentPersonId +"'");
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String p11= inputP2.getText().toString().trim();
                inputP2.setTextColor(Color.BLACK);
                arr[1]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data

                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(arr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    save.setVisibility(View.VISIBLE);
                }

                if(!p11.matches("[0-9]+")){// "[.]?[0-9]+[.]?[0-9]*"
                    inputP2.setTextColor(Color.RED);
                    save.setVisibility(View.GONE);
                    arr[1]=2;//means wrong data
                   // Toast.makeText(IndividualPersonDetailActivity.this, "NOT ALLOWED(space  .  ,  -)\nPLEASE CORRECT", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                result.moveToFirst();
                p1_p2_p3_p4_Change_Tracker(result,inputP1,inputP2,inputP3,inputP4,runtimeSuggestionAmountToGive);
            }
        });
        inputP3.addTextChangedListener(new TextWatcher() {
            Cursor result=db.getData("SELECT  "+Database.COL_32_R1+","+Database.COL_33_R2+","+Database.COL_34_R3+","+Database.COL_35_R4+"  FROM " + Database.TABLE_NAME3 + " WHERE "+Database.COL_31_ID+"= '" + fromIntentPersonId +"'");
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String p11= inputP3.getText().toString().trim();
                inputP3.setTextColor(Color.BLACK);
                arr[2]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data


                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(arr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    save.setVisibility(View.VISIBLE);
                }

                if(!p11.matches("[0-9]+")){//space or , or - is restricted
                    inputP3.setTextColor(Color.RED);
                    save.setVisibility(View.GONE);
                    arr[2]=2;//means wrong data
                   // Toast.makeText(IndividualPersonDetailActivity.this, "NOT ALLOWED(space  .  ,  -)\nPLEASE CORRECT", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                result.moveToFirst();
                p1_p2_p3_p4_Change_Tracker(result,inputP1,inputP2,inputP3,inputP4,runtimeSuggestionAmountToGive);
            }
        });
        inputP4.addTextChangedListener(new TextWatcher() {
            Cursor result=db.getData("SELECT  "+Database.COL_32_R1+","+Database.COL_33_R2+","+Database.COL_34_R3+","+Database.COL_35_R4+"  FROM " + Database.TABLE_NAME3 + " WHERE "+Database.COL_31_ID+"= '" + fromIntentPersonId +"'");
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String p11= inputP4.getText().toString().trim();
                inputP4.setTextColor(Color.BLACK);
                arr[3]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data
                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(arr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    save.setVisibility(View.VISIBLE);
                }
                if(!p11.matches("[0-9]+")){//space or , or - is restricted
                    inputP4.setTextColor(Color.RED);
                    save.setVisibility(View.GONE);
                    arr[3]=2;//means wrong data
                    //Toast.makeText(IndividualPersonDetailActivity.this, "NOT ALLOWED(space  .  ,  -)\nPLEASE CORRECT", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                result.moveToFirst();
                p1_p2_p3_p4_Change_Tracker(result,inputP1,inputP2,inputP3,inputP4,runtimeSuggestionAmountToGive);
            }
        });
    }

    public void refreshCurrentActivity(String id) {
        Intent intent=new Intent(IndividualPersonDetailActivity.this,IndividualPersonDetailActivity.class);
        intent.putExtra("ID",id);
        finish();//while going to other activity so destroy  this current activity so that while coming back we will see refresh activity
        startActivity(intent);
    }

    private void p1_p2_p3_p4_Change_Tracker(Cursor result, EditText inputP1, EditText inputP2, EditText inputP3, EditText inputP4, TextView runtimeSuggestionAmountToGive) {
        String p1,p2,p3,p4;
        p1 = inputP1.getText().toString().trim();
        //all 15 combination
        //only p1
        if (arr[0] == 1 && arr[1] != 1 && arr[2] != 1 && arr[3] != 1) {
            runtimeSuggestionAmountToGive.setText(String.valueOf(result.getInt(0) * Integer.parseInt(p1)));
        }
        //only p1 p2
        else if (arr[0] == 1 && arr[1] == 1 && arr[2] != 1 && arr[3] != 1) {
            p2 = inputP2.getText().toString().trim();
            runtimeSuggestionAmountToGive.setText(String.valueOf((result.getInt(0) * Integer.parseInt(p1)) + (result.getInt(1) * Integer.parseInt(p2))));
        }
        //only p1 p2,p3
        else if (arr[0] == 1 && arr[1] == 1 && arr[2] == 1 && arr[3] != 1) {
            p2 = inputP2.getText().toString().trim();
            p3 = inputP3.getText().toString().trim();
            runtimeSuggestionAmountToGive.setText(String.valueOf((result.getInt(0) * Integer.parseInt(p1)) + (result.getInt(1) * Integer.parseInt(p2)) + (result.getInt(2) * Integer.parseInt(p3))));
        }
        //only p1 p2,p3,p4
        else if (arr[0] == 1 && arr[1] == 1 && arr[2] == 1 && arr[3] == 1) {
            p2 = inputP2.getText().toString().trim();
            p3 = inputP3.getText().toString().trim();
            p4 = inputP4.getText().toString().trim();
            runtimeSuggestionAmountToGive.setText(String.valueOf((result.getInt(0) * Integer.parseInt(p1)) + (result.getInt(1) * Integer.parseInt(p2)) + (result.getInt(2) * Integer.parseInt(p3)) + (result.getInt(3) * Integer.parseInt(p4))));
        }
        //only p1 p3 p4
        else if (arr[0] == 1 && arr[1] != 1 && arr[2] == 1 && arr[3] == 1) {
            p3 = inputP3.getText().toString().trim();
            p4 = inputP4.getText().toString().trim();
            runtimeSuggestionAmountToGive.setText(String.valueOf((result.getInt(0) * Integer.parseInt(p1)) + (result.getInt(2) * Integer.parseInt(p3)) + (result.getInt(3) * Integer.parseInt(p4))));
        }
        //only p1 p2 p4
        else if (arr[0] == 1 && arr[1] == 1 && arr[2] != 1 && arr[3] == 1) {
            p2 = inputP2.getText().toString().trim();
            p4 = inputP4.getText().toString().trim();
            runtimeSuggestionAmountToGive.setText(String.valueOf((result.getInt(0) * Integer.parseInt(p1)) + (result.getInt(1) * Integer.parseInt(p2)) + (result.getInt(3) * Integer.parseInt(p4))));
        }
        //only p2 p3 p4
        else if (arr[0] != 1 && arr[1] == 1 && arr[2] == 1 && arr[3] == 1) {
            p2 = inputP2.getText().toString().trim();
            p3 = inputP3.getText().toString().trim();
            p4 = inputP4.getText().toString().trim();
            runtimeSuggestionAmountToGive.setText(String.valueOf((result.getInt(1) * Integer.parseInt(p2)) + (result.getInt(2) * Integer.parseInt(p3)) + (result.getInt(3) * Integer.parseInt(p4))));
        }
        //only p1 P4
        else if (arr[0] == 1 && arr[1] != 1 && arr[2] != 1 && arr[3] == 1) {
            p4 = inputP4.getText().toString().trim();
            runtimeSuggestionAmountToGive.setText(String.valueOf((result.getInt(0) * Integer.parseInt(p1)) + (result.getInt(3) * Integer.parseInt(p4))));
        }
        //only p1 P3
        else if (arr[0] == 1 && arr[1] != 1 && arr[2] == 1 && arr[3] != 1) {
            p3 = inputP3.getText().toString().trim();
            runtimeSuggestionAmountToGive.setText(String.valueOf((result.getInt(0) * Integer.parseInt(p1)) + (result.getInt(2) * Integer.parseInt(p3))));
        }
        //Only p3,p4
        else if (arr[0] != 1 && arr[1] != 1 && arr[2] == 1 && arr[3] == 1) {
            p3 = inputP3.getText().toString().trim();
            p4 = inputP4.getText().toString().trim();
            runtimeSuggestionAmountToGive.setText(String.valueOf((result.getInt(2) * Integer.parseInt(p3)) + (result.getInt(3) * Integer.parseInt(p4))));
        }
        //Only p2,p4
        else if (arr[0] != 1 && arr[1] == 1 && arr[2] != 1 && arr[3] == 1) {
            p2 = inputP2.getText().toString().trim();
            p4 = inputP4.getText().toString().trim();
            runtimeSuggestionAmountToGive.setText(String.valueOf((result.getInt(1) * Integer.parseInt(p2)) + (result.getInt(3) * Integer.parseInt(p4))));
        }
        //Only p2,p3
        else if (arr[0] != 1 && arr[1] == 1 && arr[2] == 1 && arr[3] != 1) {
            p2 = inputP2.getText().toString().trim();
            p3 = inputP3.getText().toString().trim();
            runtimeSuggestionAmountToGive.setText(String.valueOf ((result.getInt(1) * Integer.parseInt(p2)) + (result.getInt(2) * Integer.parseInt(p3))));
        }
        //only p2
        else if (arr[0] != 1 && arr[1] == 1 && arr[2] != 1 && arr[3] != 1) {
            p2 = inputP2.getText().toString().trim();
            runtimeSuggestionAmountToGive.setText(String.valueOf(result.getInt(1) * Integer.parseInt(p2)));
        }
        //only p3
        else if (arr[0] != 1 && arr[1] != 1 && arr[2] == 1 && arr[3] != 1) {
            p3 = inputP3.getText().toString().trim();
            runtimeSuggestionAmountToGive.setText(String.valueOf(result.getInt(2) * Integer.parseInt(p3)));
        }
        //only p4
        else if (arr[0] != 1 && arr[1] != 1 && arr[2] != 1 && arr[3] == 1) {
            p4 = inputP4.getText().toString().trim();
            runtimeSuggestionAmountToGive.setText(String.valueOf(result.getInt(3) * Integer.parseInt(p4)));
        }
        //if any wrong data then this will execute
        if(arr[0] == 2 || arr[1] == 2 || arr[2] == 2 || arr[3] == 2) {
            runtimeSuggestionAmountToGive.setText("0");
           // Toast.makeText(this, "ENTER 0 DON'T LEFT M L G EMPTY", Toast.LENGTH_SHORT).show();
        }
    }
    public void showDialogAsMessage( String query,String ifTitle,String ifMessage,String elseTitle,String elseMessage){
        if(db.updateTable(query)){
            displayResult(ifTitle,ifMessage);
        }else{
            displayResult(elseTitle, elseMessage);
        }
    }
    private void displayResult(String title, String message)  {
        AlertDialog.Builder showDataFromDataBase=new AlertDialog.Builder(IndividualPersonDetailActivity.this);
        showDataFromDataBase.setCancelable(false);
        showDataFromDataBase.setTitle(title);
        showDataFromDataBase.setMessage(message);
        showDataFromDataBase.setPositiveButton(getResources().getString(R.string.ok),(dialogInterface, i) -> {
            dialogInterface.dismiss();//close current dialog
            Intent intent=new Intent(IndividualPersonDetailActivity.this,IndividualPersonDetailActivity.class);
            intent.putExtra("ID",fromIntentPersonId);
            finish();//while going to other activity so destroy  this current activity so that while coming back we will see refresh activity
            startActivity(intent);
        });
        showDataFromDataBase.create().show();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyUtility.deletePdfOrRecordingFromDevice(audioPath);//delete Audio If Not user Saved
    }
}