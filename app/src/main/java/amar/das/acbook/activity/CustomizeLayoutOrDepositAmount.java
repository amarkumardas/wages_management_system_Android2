package amar.das.acbook.activity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;

import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;


import android.widget.Toast;

import java.util.Calendar;

import amar.das.acbook.Database;
import amar.das.acbook.R;
import amar.das.acbook.databinding.ActivityCustomizeLayoutOrDepositAmountBinding;
import amar.das.acbook.utility.MyUtility;
import amar.das.acbook.voicerecording.VoiceRecorder;

public class CustomizeLayoutOrDepositAmount extends AppCompatActivity {
     ActivityCustomizeLayoutOrDepositAmountBinding binding;
    Database db;
    private  String fromIntentPersonId;
    //for recording variable declaration
    MediaRecorder mediaRecorder;
    String audioPath;
    boolean toggleToStartRecording =false;

    int []arr=new int[3];//to give information which field is empty or contain data
    String []previousDataHold=new String[4];
    int cYear,cMonth,cDayOfMonth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0); //we have used overridePendingTransition(), it is used to remove activity create animation while re-creating activity.
        binding = ActivityCustomizeLayoutOrDepositAmountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        fromIntentPersonId = getIntent().getStringExtra("ID");//getting id from intent IT IS important because we have to pass while cancelling

        binding.customCancelBtn.setOnClickListener(view -> {
            VoiceRecorder.stopAudioPlayer();//when audio is playing and   user click  cancel then stop audio also
            MyUtility.deletePdfOrRecordingUsingPathFromDevice(audioPath);//delete Audio If Not user Saved

            goBackToIndividualPersonActivity(fromIntentPersonId);
//            finish();//destroy current activity
//            Intent intent=new Intent(CustomizeLayoutOrDepositAmount.this,IndividualPersonDetailActivity.class);
//            intent.putExtra("ID",fromIntentPersonId);
//            startActivity(intent);//while cancelling we will go back to previous Activity with updated activity so passing id to get particular person detail
        });
        binding.gobackDeposit.setOnClickListener(view -> {
            VoiceRecorder.stopAudioPlayer();//when audio is playing and   user click  cancel then stop audio also
            MyUtility.deletePdfOrRecordingUsingPathFromDevice(audioPath);//delete Audio If Not user Saved
            goBackToIndividualPersonActivity(fromIntentPersonId);
//            finish();//destroy current activity
//            Intent intent=new Intent(CustomizeLayoutOrDepositAmount.this,IndividualPersonDetailActivity.class);
//            intent.putExtra("ID",fromIntentPersonId);
//            startActivity(intent);// go back to previous Activity with updated activity so passing id to get particular person detail
        });
        binding.customDepositEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String p11 = binding.customDepositEt.getText().toString().trim();
                //binding.customDepositEt.setTextColor(getResources().getColor(R.color.green));
                binding.customDepositEt.setTextColor(getColor(R.color.green));
                arr[0]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data
                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(arr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    binding.customSaveBtn.setVisibility(View.VISIBLE);
                }
                if (!p11.matches("[0-9]+")) {//space or , or - is restricted"[.]?[0-9]+[.]?[0-9]*"
                    binding.customDepositEt.setTextColor(Color.RED);
                    binding.customSaveBtn.setVisibility(View.GONE);
                    arr[0]=2;//means data is inserted wrong
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
        binding.customMicIconTv.setOnClickListener(view -> {
            if(MyUtility.checkPermissionAudioAndExternal(getApplicationContext())){//checking for permission
                if (toggleToStartRecording) {//initially false
                    binding.customSaveBtn.setVisibility(View.GONE);

                    binding.customChronometer.setEnabled(false);//when user press save button then set to true playAudioChronometer.setEnabled(true);
                    binding.customSaveAudioIconTv.setBackgroundResource(R.drawable.ic_green_sharp_done_sharp_tick_20);//changing tick color to green so that user can feel to press to save
                    binding.customMicIconTv.setEnabled(false);
                    binding.customMicIconTv.setBackgroundResource(R.drawable.black_sharp_mic_24);//change color when user click

                    VoiceRecorder voiceRecorder=new VoiceRecorder(fromIntentPersonId,getExternalFilesDir(null).toString());//getExternalFilesDir(null) is a method in Android Studio that returns the path of the directory holding application files on external storage
                    if(voiceRecorder.startRecording()){
                        binding.customChronometer.setBase(SystemClock.elapsedRealtime());//In Android, Chronometer is a class that implements a simple timer. Chronometer is a subclass of TextView. This class helps us to add a timer in our app.
                        binding.customChronometer.start();
                        Toast.makeText(CustomizeLayoutOrDepositAmount.this, getResources().getString(R.string.recording_started), Toast.LENGTH_LONG).show();
                        audioPath=voiceRecorder.getAudioAbsolutePath();//updating audioPath for further use otherwise it will be null
                        mediaRecorder=voiceRecorder.getMediaRecorder();//updating mediaRecorder for further use  otherwise it will be null
                    }else{
                        Toast.makeText(CustomizeLayoutOrDepositAmount.this, getResources().getString(R.string.failed_to_start_recording), Toast.LENGTH_SHORT).show();
                    }
                    CustomizeLayoutOrDepositAmount.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //while the user is recording screen should be on. it should not close

                }else{//if recording is not started then stop
                    Toast.makeText(CustomizeLayoutOrDepositAmount.this, getResources().getString(R.string.again_tab_on_mic_to_start_recording), Toast.LENGTH_SHORT).show();
                }
                toggleToStartRecording = !toggleToStartRecording;//so that user should click 2 times to start recording

            }else {//request for permission
                Toast.makeText(CustomizeLayoutOrDepositAmount.this, getResources().getString(R.string.audio_permission_required), Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(CustomizeLayoutOrDepositAmount.this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 21);
            }
        });

        binding.customSaveAudioIconTv.setOnClickListener(view -> {
            if(mediaRecorder !=null){
                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(arr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    binding.customSaveBtn.setVisibility(View.VISIBLE);
                }
                binding.customChronometer.setTextColor(getColor(R.color.green));//changing text color to green to give feel that is saved
                binding.customMicIconTv.setBackgroundResource(R.drawable.ic_green_sharp_mic_20);//set background image to cancel
                VoiceRecorder.stopRecording();//to save correct audio file in device stopRecording() method should be called then only file will ve saved
                binding.customChronometer.stop();//stopping chronometer
                binding.customMicIconTv.setEnabled(false);//so that user cannot press again this button
                binding.customSaveAudioIconTv.setEnabled(false);//even this button user should not click again
                binding.customChronometer.setEnabled(true);//when audio is save then user will be able to play
            }else {
                Toast.makeText(CustomizeLayoutOrDepositAmount.this, getResources().getString(R.string.tab_on_mic_to_start_recording), Toast.LENGTH_SHORT).show();
            }
        });

          final Calendar current=Calendar.getInstance();//to get current date and time
          cYear=current.get(Calendar.YEAR);
          cMonth=current.get(Calendar.MONTH);
          cDayOfMonth=current.get(Calendar.DAY_OF_MONTH);
        binding.customDateTv.setOnClickListener(view -> {//to automatically set date to textView
            //To show calendar dialog
            DatePickerDialog datePickerDialog=new DatePickerDialog(CustomizeLayoutOrDepositAmount.this, (datePicker, year, month, dayOfMonth) -> {
                binding.customDateTv.setText(dayOfMonth+"-"+(month+1)+"-"+year);//month start from 0 so 1 is added to get right month like 12
            },cYear,cMonth,cDayOfMonth);//This variable should be ordered this variable will set date day month to calendar to datePickerDialog so passing it
            datePickerDialog.show();
        });

        if (getIntent().hasExtra("ID") && !getIntent().hasExtra("DATE") &&  !getIntent().hasExtra("TIME")) {//if id present than only operation will be performed
            db = new Database(this);//on start only database should be create
            binding.customDateTv.setText(cDayOfMonth+"-"+(cMonth+1)+"-"+cYear);
            binding.customSaveBtn.setOnClickListener(view -> {
                binding.customSaveBtn.setVisibility(View.GONE);//to avoid when user click multiple times
                VoiceRecorder.stopAudioPlayer();//if user playing audio and click save button then stop audio
                VoiceRecorder.stopRecording();//if user don't click tick button to save recording then while saving all data recording will also get saved automatically.so  VoiceRecorder.stopRecording()  method should be called then only file will be saved

                int depositAmount=0;
                String remarks;
                String micPath=null;

                String onlyTime = MyUtility.getOnlyTime();
                //binding.customTimeTv.setText(onlyTime);//setting time to take time and store in db

               // db.updateTable("UPDATE " + Database.TABLE_NAME1 + " SET  "+Database.COL_15_LATESTDATE+"='" + MyUtility.getOnlyCurrentDate() + "' , "+Database.COL_16_TIME+"= '"+ onlyTime  +"' WHERE "+Database.COL_1_ID+"='" + fromIntentPersonId + "'");//when ever user insert its  deposit then latest date AND TIME  will be updated to current date AND TIME not user entered date

                if(audioPath !=null){//if file is not null then only it execute otherwise nothing will be inserted
                    micPath= audioPath;
                    arr[1]=1;
                }
                else
                    arr[1]=0;

                if(binding.customDescriptionEt.getText().toString().length() >=1){//to prevent null pointer exception.it execute when user enter date
                    remarks="["+onlyTime+getResources().getString(R.string.hyphen_entered)+"\n\n"+getResources().getString(R.string.deposited_with_hyphen)+binding.customDescriptionEt.getText().toString().trim();//time is set automatically to remarks if user enter any remarks
                    //arr[2]=1;
                }
                else{
                    remarks="["+onlyTime+getResources().getString(R.string.hyphen_automatic_entered)+"\n\n"+getResources().getString(R.string.deposited);//adding default deposit message so that when user don't enter remarks this remarks will be added
                  // arr[2]=1;
                }
                arr[2]=1;//for description

                boolean isWrongData,isDataPresent,success;
                  isWrongData=MyUtility.isEnterDataIsWrong(arr);//it should be here to get updated result
                  isDataPresent= MyUtility.isDataPresent(arr);
                if(isDataPresent==true && isWrongData==false ) {  //means if data is present then check is it right data or not
                    //db.updateTable("UPDATE " + Database.TABLE_NAME1 + " SET "+Database.COL_12_ACTIVE+"='" + 1 + "'" +" WHERE "+Database.COL_1_ID+"='" + fromIntentPersonId + "'");//when ever user update then that person will become active
//                    if(!db.activateIdWithLatestDate(fromIntentPersonId,onlyTime)){
//                        Toast.makeText(this, "FAILED TO MAKE ID ACTIVE", Toast.LENGTH_LONG).show();
//                    }

                    if(binding.customDepositEt.getText().toString().trim().length() >= 1) {
                        depositAmount = Integer.parseInt(binding.customDepositEt.getText().toString().trim());
                    }

                    //success = db.insert_Deposit_Table2(fromIntentPersonId,binding.customDateTv.getText().toString(),binding.customTimeTv.getText().toString(),micPath,remarks,depositAmount,"1");
                    success=db.insertWagesOrDepositOnlyToActiveTableTransaction(fromIntentPersonId,binding.customDateTv.getText().toString(),onlyTime,micPath,remarks,0,0,0,0,0,depositAmount,"1");
                    if(!success){
                        Toast.makeText(CustomizeLayoutOrDepositAmount.this, getResources().getString(R.string.failed_to_insert), Toast.LENGTH_LONG).show();
                    }
                    goBackToIndividualPersonActivity(fromIntentPersonId);
//                    if (success){
//                        showResult("DEPOSIT - "+depositAmount,"\nDATE-  "+binding.customDateTv.getText().toString()+"\n\nREMARKS- "+remarks+"\n\nMICPATH- "+micPath);
//                    } else
//                        Toast.makeText(CustomizeLayoutOrDepositAmount.this, "FAILED TO INSERT", Toast.LENGTH_LONG).show();

                }else
                    Toast.makeText(CustomizeLayoutOrDepositAmount.this, "CORRECT THE DATA or CANCEL AND ENTER AGAIN", Toast.LENGTH_LONG).show();

                audioPath =null;//since audio is saved then make this variable null otherwise audio will be deleted ON CANCEL OR ON DESTROY only if user don't enter save button
            });
            binding.customChronometer.setOnClickListener(view -> {
                if(audioPath != null){//checking for null pointer Exception
                    if(VoiceRecorder.audioPlayer(audioPath)){
                        Toast.makeText(CustomizeLayoutOrDepositAmount.this,getResources().getString(R.string.audio_playing),Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(CustomizeLayoutOrDepositAmount.this,getResources().getString(R.string.failed_to_play_audio),Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(CustomizeLayoutOrDepositAmount.this, getResources().getString(R.string.tab_on_mic_to_start_recording), Toast.LENGTH_SHORT).show();
                }
            });

            //while updating this will execute
        }else if(getIntent().hasExtra("ID") &&  getIntent().hasExtra("DATE") &&  getIntent().hasExtra("TIME")){

            binding.customDepositAmountTv.setText(getResources().getString(R.string.update_deposit_amount));
            binding.customSaveBtn.setText(getResources().getString(R.string.long_press_to_update));

            String recycleDate= getIntent().getStringExtra("DATE");
              cDayOfMonth = extractData(0,recycleDate);
              cMonth = extractData(1, recycleDate);
              cYear = extractData(2, recycleDate);
            binding.customDateTv.setText(cDayOfMonth+"-"+(cMonth+1)+"-"+cYear);//date set

            db = new Database(CustomizeLayoutOrDepositAmount.this);//we can take any field context
            //Cursor cursorData = db.getData("SELECT  "+Database.COL_25_DESCRIPTION+","+Database.COL_27_DEPOSIT+","+Database.COL_24_MICPATH+" FROM " + Database.TABLE_NAME2 + " WHERE "+Database.COL_21_ID+"= '" + getIntent().getStringExtra("ID") + "'" + " AND "+Database.COL_22_DATE+"= '" + getIntent().getStringExtra("DATE") + "'" + " AND "+Database.COL_23_TIME+"='" + getIntent().getStringExtra("TIME") + "'");
            Cursor cursorData = db.getDepositForUpdate(getIntent().getStringExtra("ID"),getIntent().getStringExtra("DATE"),getIntent().getStringExtra("TIME"));
            cursorData.moveToFirst();//this cursor is not closed
            String cDescription,cDeposit,cMicPath;
            cDescription=cursorData.getString(0);
            cDeposit=cursorData.getString(1);
            cMicPath=cursorData.getString(2);
            cursorData.close();

            binding.customDepositEt.setText(cDeposit);//fetching deposit

             previousDataHold[0]="DATE- "+getIntent().getStringExtra("DATE");
             previousDataHold[1]="TIME- "+getIntent().getStringExtra("TIME");
             previousDataHold[2]="DEPOSIT- "+cDeposit;
             previousDataHold[3]="REMARKS- "+cDescription;

            if(cMicPath != null) {//if there is audio then set to color  green
                binding.customMicIconTv.setVisibility(View.GONE);//user wound be able to save voice for second time if there is already voice because we want to keep previous voice save we don't want to delete previous voice
                binding.customSaveAudioIconTv.setVisibility(View.GONE);

                binding.customChronometer.setTextColor(getColor(R.color.green));
                binding.customChronometer.setText(getResources().getString(R.string.play));
                binding.customChronometer.setGravity(Gravity.RIGHT);//setting text to right
                binding.customChronometer.setTypeface(null, Typeface.BOLD);//changing text to bold
            }

            binding.customChronometer.setOnClickListener(view ->{
                if (cMicPath != null || audioPath != null){//checking audio is present or not

                    if(audioPath != null){//if new audio is set then file will contain audio and data.getMicPath() will contain null
                        if(VoiceRecorder.audioPlayer(audioPath)){
                            Toast.makeText(view.getContext(), view.getContext().getResources().getString(R.string.audio_playing), Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(view.getContext(), view.getResources().getString(R.string.audio_not_found_may_be_deleted), Toast.LENGTH_LONG).show();
                        }
                    }else {
                        if(VoiceRecorder.audioPlayer(cMicPath)){
                            Toast.makeText(view.getContext(), view.getContext().getResources().getString(R.string.audio_playing), Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(view.getContext(), view.getResources().getString(R.string.audio_not_found_may_be_deleted), Toast.LENGTH_LONG).show();
                        }
                    }
                }else {
                    Toast.makeText(view.getContext(), view.getContext().getResources().getString(R.string.tab_on_mic_to_start_recording), Toast.LENGTH_SHORT).show();
                }
            });
            binding.customSaveBtn.setOnLongClickListener(view -> {
                binding.customSaveBtn.setVisibility(View.GONE);//to avoid when user click multiple times

                VoiceRecorder.stopAudioPlayer();//if user playing audio and click save button then stop audio
                VoiceRecorder.stopRecording();//if user don't click tick button to save recording then while saving all data recording will also get saved automatically.so  VoiceRecorder.stopRecording()  method should be called then only file will be saved

                int depositAmount=0;
                String micPath=cMicPath;//default value if we don't fetch previous data then null will be inserted and previous voice will be deleted when we try to update only deposit so it is important
                String remarks;

                //To get exact onlyTime so write code in save button
//                Date d=Calendar.getInstance().getTime();
//                SimpleDateFormat sdf=new SimpleDateFormat("hh:mm:ss a");//a stands for AM or PM

                //String onlyTime = MyUtility.getOnlyTime();
               // binding.customTimeTv.setText(onlyTime);//setting onlyTime to take onlyTime and store in db
               // String onlyTime=binding.customTimeTv.getText().toString();
                String onlyTime=MyUtility.getOnlyTime();
                String date=binding.customDateTv.getText().toString();

                //this will store latest date by taking current date
//                final Calendar current1 =Calendar.getInstance();//to get current date
//                String curreentDate = current1.get(Calendar.DAY_OF_MONTH)+"-"+(current1.get(Calendar.MONTH)+1)+"-"+ current1.get(Calendar.YEAR);

//                if(date.equals(currentDate)) {//if it is true then store
//                    db.updateTable("UPDATE " + db.TABLE_NAME1 + " SET  LATESTDATE='" +date + "'" +" WHERE ID='" + getIntent().getStringExtra("ID") + "'");
//                }


                //db.updateTable("UPDATE " + Database.TABLE_NAME1 + " SET  "+Database.COL_15_LATESTDATE+"='" + MyUtility.getOnlyCurrentDate() + "' , "+Database.COL_16_TIME+"= '"+ onlyTime  +"' WHERE "+Database.COL_1_ID+"='" + fromIntentPersonId + "'");//when ever user insert its  deposit then latest date AND TIME  will be updated to current date AND TIME not user entered date

                if(audioPath !=null){//if file is not null then only it execute otherwise nothing will be inserted
                    micPath= audioPath ;
                 }

                //if user don't enter remarks or description then it is sure that previous data will be entered so no need to check null pointer exception
                remarks = "[" + onlyTime + getResources().getString(R.string.hyphen_edited)+"\n\n"+getResources().getString(R.string.deposited_with_hyphen)+binding.customDescriptionEt.getText().toString().trim()+"\n\n"+getResources().getString(R.string.previous_details_were_hyphen)+"\n" + previousDataHold[0] + "  " + previousDataHold[1] + "\n" + previousDataHold[2] + "\n" + previousDataHold[3] ;//onlyTime is set automatically to remarks if user enter any remarks;
                arr[1] = 1;//this is important because when user do not enter any data while updating then least 1 field should be filled with data so this field will sure be filled automatically so this is important.

                boolean isWrongData,isDataPresent,success;
                  isWrongData=MyUtility.isEnterDataIsWrong(arr);//it should be here to get updated result
                  isDataPresent=MyUtility.isDataPresent(arr);
                if(isDataPresent==true && isWrongData==false ) {  //means if data is present then check is it right data or not
                    //  db.updateTable("UPDATE " + Database.TABLE_NAME1 + " SET "+Database.COL_12_ACTIVE+"='" + 1 + "'" + " WHERE "+Database.COL_1_ID+"='" + getIntent().getStringExtra("ID") + "'");//when ever user update then that person will become active
//                    if(!db.activateIdWithLatestDate(getIntent().getStringExtra("ID"),onlyTime)){
//                        Toast.makeText(this, "FAILED TO MAKE ID ACTIVE", Toast.LENGTH_LONG).show();
//                    }

                    if(binding.customDepositEt.getText().toString().trim().length() >= 1) {
                        depositAmount = Integer.parseInt(binding.customDepositEt.getText().toString().trim());
                    }

                    if(micPath != null) {//if it is not null then update mic-path
                      //  success = db.updateTable("UPDATE " + db.TABLE_NAME2 + " SET DATE='" + date + "',TIME='" + onlyTime + "',DESCRIPTION='" + remarks + "',MICPATH='" + micPath + "',DEPOSIT='" + depositAmount + "' WHERE ID= '" + getIntent().getStringExtra("ID") + "'" + " AND DATE= '" + getIntent().getStringExtra("DATE") + "'" + " AND TIME='" + getIntent().getStringExtra("TIME") + "'");
                       // success=db.update_Deposit_TABLE_NAME2(date,onlyTime,micPath,remarks,depositAmount,getIntent().getStringExtra("ID"),getIntent().getStringExtra("DATE"),getIntent().getStringExtra("TIME"));
                        success=db.updateWagesOrDepositOnlyToActiveTable(date,onlyTime,remarks,micPath,0,depositAmount,0,0,0,0,getIntent().getStringExtra("ID"),getIntent().getStringExtra("DATE"),getIntent().getStringExtra("TIME"));

                    } else {//if micPath == null then we are not updating because null in text will be set to mic-path and give wrong result like it will indicate that audio is present but actually audio is not present
                       // success = db.updateTable("UPDATE " + db.TABLE_NAME2 + " SET DATE='" + date + "',TIME='" + onlyTime + "',DESCRIPTION='" + remarks + "',DEPOSIT='" + depositAmount + "' WHERE ID= '" + getIntent().getStringExtra("ID") + "'" + " AND DATE= '" + getIntent().getStringExtra("DATE") + "'" + " AND TIME='" + getIntent().getStringExtra("TIME") + "'");
                        //success=db.update_Deposit_TABLE_NAME2(date,onlyTime,null,remarks,depositAmount,getIntent().getStringExtra("ID"),getIntent().getStringExtra("DATE"),getIntent().getStringExtra("TIME"));
                        success=db.updateWagesOrDepositOnlyToActiveTable(date,onlyTime,remarks,null,0,depositAmount,0,0,0,0,getIntent().getStringExtra("ID"),getIntent().getStringExtra("DATE"),getIntent().getStringExtra("TIME"));
                    }
                    if(!success){
                        Toast.makeText(CustomizeLayoutOrDepositAmount.this, getResources().getString(R.string.failed_to_update), Toast.LENGTH_LONG).show();
                    }
                    goBackToIndividualPersonActivity(fromIntentPersonId);
//                    if (success) {
//                        showResult("DEPOSIT - "+depositAmount,"\nDATE-  "+date+"\n\nREMARKS- "+remarks+"\n\nMICPATH- "+micPath);
//                    } else
//                        Toast.makeText(CustomizeLayoutOrDepositAmount.this, "FAILED TO UPDATE", Toast.LENGTH_LONG).show();

                }else {
                    Toast.makeText(CustomizeLayoutOrDepositAmount.this, "CORRECT THE DATA or CANCEL AND ENTER AGAIN", Toast.LENGTH_LONG).show();
                }

                audioPath =null;//since audio is saved then make this variable null otherwise audio will be deleted ON CANCEL OR ON DESTROY only if user don't enter save button
                return false;
            });
        }else
            Toast.makeText(this, "No ID from other Intent", Toast.LENGTH_SHORT).show();
    }
    private int extractData(int i,String date) {
        int data=0;
        String[] str=date.split("-");//converting using String.split() method with "-" as a delimiter
        if(i==0){//0 is for day of month
            return Integer.parseInt(str[0]);
        }else if(i==1){//1 is for month
            return (Integer.parseInt(str[1])-1);//month-1 to get right result
        }else if(i==2){//2 is for year
            return Integer.parseInt(str[2]);
        }
        return data;
    }

//    private void showResult(String title, String message) {
//        AlertDialog.Builder showDataFromDataBase=new AlertDialog.Builder(CustomizeLayoutOrDepositAmount.this);
//        showDataFromDataBase.setCancelable(false);
//        showDataFromDataBase.setTitle(title);
//        showDataFromDataBase.setMessage(message);
//        showDataFromDataBase.setPositiveButton("OK", (dialogInterface, i) -> {
//            dialogInterface.dismiss();
//            //after data entered successfully
//            finish();//destroy current activity
//            Intent intent=new Intent(CustomizeLayoutOrDepositAmount.this,IndividualPersonDetailActivity.class);
//            intent.putExtra("ID",fromIntentPersonId);
//            startActivity(intent);//while cancelling we will go back to previous Activity with updated activity so passing id to get particular person detail
//        });
//        showDataFromDataBase.create().show();
//    }
    private void goBackToIndividualPersonActivity(String id){
        finish();//destroy current activity
        Intent intent=new Intent(CustomizeLayoutOrDepositAmount.this,IndividualPersonDetailActivity.class);
        intent.putExtra("ID",id);
        startActivity(intent);//while cancelling we will go back to previous Activity with updated activity so passing id to get particular person detail
    }
    @Override
    public void onBackPressed() {//on back press button
        super.onBackPressed();
        MyUtility.deletePdfOrRecordingUsingPathFromDevice(audioPath);//delete Audio If Not user Saved
        finish();//destroy current activity
        Intent intent=new Intent(CustomizeLayoutOrDepositAmount.this,IndividualPersonDetailActivity.class);
        intent.putExtra("ID",fromIntentPersonId);
        startActivity(intent);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyUtility.deletePdfOrRecordingUsingPathFromDevice(audioPath);//delete Audio If Not user Saved
    }
}