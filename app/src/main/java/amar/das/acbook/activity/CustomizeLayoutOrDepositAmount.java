package amar.das.acbook.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;

import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import amar.das.acbook.PersonRecordDatabase;
import amar.das.acbook.R;
import amar.das.acbook.databinding.ActivityCustomizeLayoutOrDepositAmountBinding;

public class CustomizeLayoutOrDepositAmount extends AppCompatActivity {
    ActivityCustomizeLayoutOrDepositAmountBinding binding;
    PersonRecordDatabase db;
    private  String fromIntentPersonId;
    //for recording variable declaration
    MediaRecorder mediaRecorder;
    long mstartingTimeMillis=0;
    long mElapsedMillis=0;
    File file;
    String fileName;
    MediaPlayer mediaPlayer;
    Boolean mStartRecording =false;

    int arr[]=new int[3];//to give information which field is empty or contain data
    String previousDataHold[]=new String[4];
    int cYear,cMonth,cDayOfMonth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0); //we have used overridePendingTransition(), it is used to remove activity create animation while re-creating activity.
        binding = ActivityCustomizeLayoutOrDepositAmountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        fromIntentPersonId = getIntent().getStringExtra("ID");//getting id from intent IT IS important because we have to pass while cancelling

        binding.customCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();//destroy current activity
                Intent intent=new Intent(CustomizeLayoutOrDepositAmount.this,IndividualPersonDetailActivity.class);
                intent.putExtra("ID",fromIntentPersonId);
                startActivity(intent);//while cancelling we will go back to previous Activity with updated activity so passing id to get particular person detail
            }
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
                if(!isEnterDataIsWrong(arr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    binding.customSaveBtn.setVisibility(View.VISIBLE);
                }
                if (!p11.matches("[0-9]+")) {//space or , or - is restricted"[.]?[0-9]+[.]?[0-9]*"
                    binding.customDepositEt.setTextColor(Color.RED);
                    binding.customSaveBtn.setVisibility(View.GONE);
                    arr[0]=2;//means data is inserted wrong
                   // Toast.makeText(CustomizeLayoutOrDepositAmount.this, "NOT ALLOWED(space  .  ,  -)\nPLEASE CORRECT", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
        binding.customMicIconTv.setOnClickListener(view -> {
            //checking for permission
            if(checkPermission()){
                if (mStartRecording) {//initially false
                    //while recording user should not perform other task like entering date while recording because app will crash so set all field to setEnabled(false);
                    binding.customDescriptionEt.setEnabled(false);
                    binding.customDepositEt.setEnabled(false);
                    binding.customDateTv.setEnabled(false);
                    binding.customSaveBtn.setVisibility(View.GONE);
                    binding.customCancelBtn.setEnabled(false);


                    binding.customChronometer.setBase(SystemClock.elapsedRealtime());//In Android, Chronometer is a class that implements a simple timer. Chronometer is a subclass of TextView. This class helps us to add a timer in our app.
                    binding.customChronometer.start();
                    binding.customChronometer.setEnabled(false);//when user press save button then set to true playAudioChronometer.setEnabled(true);
                    binding.customSaveAudioIconTv.setBackgroundResource(R.drawable.ic_green_sharp_done_sharp_tick_20);//changing tick color to green so that user can feel to press to save
                    binding.customMicIconTv.setEnabled(false);
                    binding.customMicIconTv.setBackgroundResource(R.drawable.black_sharp_mic_24);//change color when user click

                    Toast.makeText(CustomizeLayoutOrDepositAmount.this, "RECORDING STARTED", Toast.LENGTH_SHORT).show();

                    //be carefull take only getExternalFilesDir( null ) https://stackoverflow.com/questions/59017202/mediarecorder-stop-failed
                    File folder = new File(getExternalFilesDir(null) + "/acBookMicRecording");//Creating File directory in phone

                    if (!folder.exists()) {//if folder not exist
                        Toast.makeText(CustomizeLayoutOrDepositAmount.this, "Creating acBookMicRecord folder to store audios", Toast.LENGTH_LONG).show();
                        folder.mkdir();//create folder
                    }

                    startRecordingVoice();
                    CustomizeLayoutOrDepositAmount.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //while the user is recording screen should be on. it should not close

                } else {//if recording is not started then stop
                    Toast.makeText(CustomizeLayoutOrDepositAmount.this, "AGAIN TAB ON MIC TO START RECORDING", Toast.LENGTH_SHORT).show();
                }
                mStartRecording = !mStartRecording;//so that user should click 2 times to start recording

            }else {//request for permission
                Toast.makeText(CustomizeLayoutOrDepositAmount.this, "AUDIO PERMISSION REQUIRED", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(CustomizeLayoutOrDepositAmount.this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 21);
            }
        });
        binding.customSaveAudioIconTv.setOnClickListener(view -> {
            if(mediaRecorder !=null){
                //after clicking save audion then setEnabled to true so that user can enter data to fields

                binding.customDepositEt.setEnabled(true);
                binding.customDescriptionEt.setEnabled(true);
                binding.customDateTv.setEnabled(true);

                //this will check if other data is right or wrong
                if(!isEnterDataIsWrong(arr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    binding.customSaveBtn.setVisibility(View.VISIBLE);
                }

                binding.customCancelBtn.setEnabled(true);


                binding.customChronometer.setTextColor(getColor(R.color.green));//changind text color to green to give feel that is saved
                binding.customMicIconTv.setBackgroundResource(R.drawable.ic_green_sharp_mic_20);//set background image to cancel
                stopAndSaveRecordingPathToDB();
                binding.customChronometer.stop();//stopping chronometer
                binding.customMicIconTv.setEnabled(false);//so that user cannot press again this button
                binding.customSaveAudioIconTv.setEnabled(false);//even this button user should not click again
                binding.customChronometer.setEnabled(true);//when audio is save then user will be able to play
            }else
                Toast.makeText(CustomizeLayoutOrDepositAmount.this, "TAB ON MIC TO START RECORDING", Toast.LENGTH_SHORT).show();
        });

        //to automatically set date to textView
          final Calendar current=Calendar.getInstance();//to get current date and time
          cYear=current.get(Calendar.YEAR);
          cMonth=current.get(Calendar.MONTH);
          cDayOfMonth=current.get(Calendar.DAY_OF_MONTH);
        binding.customDateTv.setOnClickListener(view -> {
            //To show calendar dialog
            DatePickerDialog datePickerDialog=new DatePickerDialog(CustomizeLayoutOrDepositAmount.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                    binding.customDateTv.setText(dayOfMonth+"-"+(month+1)+"-"+year);//month start from 0 so 1 is added to get right month like 12
                }
            },cYear,cMonth,cDayOfMonth);//This variable should be ordered this variable will set date day month to calendar to datePickerDialog so passing it
            datePickerDialog.show();
        });

        if (getIntent().hasExtra("ID") && !getIntent().hasExtra("DATE") &&  !getIntent().hasExtra("TIME")) {//if id present than only operation will be performed
            db = new PersonRecordDatabase(this);//on start only database should be create
            binding.customDateTv.setText(cDayOfMonth+"-"+(cMonth+1)+"-"+cYear);
            binding.customSaveBtn.setOnClickListener(view -> {
                int depositAmount=0;
                String remarks=null;
                String micPath=null;

                //To get exact time so written code in save button
                Date d=Calendar.getInstance().getTime();
                SimpleDateFormat sdf=new SimpleDateFormat("hh:mm:ss a");//a stands for is AM or PM
                String onlyTime = sdf.format(d);
                binding.customTimeTv.setText(onlyTime);//setting time to take time and store in db

                //this will store latest date by taking current date
                final Calendar current12 =Calendar.getInstance();//to get current date
                String currentDate = current12.get(Calendar.DAY_OF_MONTH)+"-"+(current12.get(Calendar.MONTH)+1)+"-"+ current12.get(Calendar.YEAR);
//                    if(binding.customDateTv.getText().toString().equals(currentDate)) {//if it is true then store
//                        db.updateTable("UPDATE " + db.TABLE_NAME1 + " SET  LATESTDATE='" + binding.customDateTv.getText().toString() + "'" +" WHERE ID='" + fromIntentPersonId + "'");
//                    }
                                                                                                              // To get exact time
                db.updateTable("UPDATE " + db.TABLE_NAME1 + " SET  LATESTDATE='" + currentDate + "' , TIME= '"+ onlyTime  +"' WHERE ID='" + fromIntentPersonId + "'");//when ever user insert its wages or deposit then latest date AND TIME  will be updated to current date AND TIME not user entered date

                if(file !=null){//if file is not null then only it execute otherwise nothing will be inserted
                    micPath=file.getAbsolutePath();
                    arr[1]=1;
                }
                else
                    arr[1]=0;

                if(binding.customDescriptionEt.getText().toString().length() >=1){//to prevent nullpointer exception.it execute when user enter date
                    remarks="["+binding.customTimeTv.getText().toString()+"-ENTERED]\n\n"+"[DEPOSITED]-"+binding.customDescriptionEt.getText().toString().trim();//time is set automatically to remarks if user enter any remarks
                    arr[2]=1;
                }
                else{
                    remarks="["+binding.customTimeTv.getText().toString()+"-ENTERED]\n\n"+"[DEPOSITED]";//adding default deposit message so that when user dont enter remarks this remarks will be added
                    arr[2]=1;
                }


                  boolean isWrongData,isDataPresent,success;
                  isWrongData= isEnterDataIsWrong(arr);//it should be here to get updated result
                  isDataPresent= isDataPresent(arr);
                if(isDataPresent==true && isWrongData==false ) {  //means if data is present then check is it right data or not
                    if(binding.customDepositEt.getText().toString().trim().length() >= 1) {
                        depositAmount = Integer.parseInt(binding.customDepositEt.getText().toString().trim());
                    }

                    db.updateTable("UPDATE " + db.TABLE_NAME1 + " SET ACTIVE='" + 1 + "'" +" WHERE ID='" + fromIntentPersonId + "'");//when ever user update then that person will become active
                    success = db.insert_Deposit_Table2(fromIntentPersonId,binding.customDateTv.getText().toString(),binding.customTimeTv.getText().toString(),micPath,remarks,depositAmount,"1");
                    if (success == true) {
                        displResult("DEPOSIT - "+depositAmount,"\nDATE-  "+binding.customDateTv.getText().toString()+"\n\nREMARKS- "+remarks+"\n\nMICPATH- "+micPath);
                    } else
                        Toast.makeText(CustomizeLayoutOrDepositAmount.this, "FAILED TO INSERT", Toast.LENGTH_LONG).show();

                }else
                    Toast.makeText(CustomizeLayoutOrDepositAmount.this, "CORRECT THE DATA or CANCEL AND ENTER AGAIN", Toast.LENGTH_LONG).show();
            });
            binding.customChronometer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(file != null) {//checking for null pointer Exception
                        Toast.makeText(CustomizeLayoutOrDepositAmount.this, "AUDIO PLAYING", Toast.LENGTH_SHORT).show();
                        mediaPlayer = new MediaPlayer();
                        try {
                            mediaPlayer.setDataSource(file.getAbsolutePath());//passing the path where this audio is saved
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            Toast.makeText(CustomizeLayoutOrDepositAmount.this, "AUDIO PLAYING", Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else
                        Toast.makeText(CustomizeLayoutOrDepositAmount.this, "TAB ON MIC TO START RECORDING", Toast.LENGTH_SHORT).show();
                }
            });

            //while updating this will execute
        }else if( getIntent().hasExtra("ID") &&  getIntent().hasExtra("DATE") &&  getIntent().hasExtra("TIME") ){

            binding.customDepositAmountTv.setText("UPDATE DEPOSIT AMOUNT=");
            binding.customSaveBtn.setText("LONG PRESS TO UPDATE");

            String recycleDate= getIntent().getStringExtra("DATE");
              cDayOfMonth = extractData(0,recycleDate);
              cMonth = extractData(1, recycleDate);
              cYear = extractData(2, recycleDate);
            binding.customDateTv.setText(cDayOfMonth+"-"+(cMonth+1)+"-"+cYear);//date set

            db = new PersonRecordDatabase(CustomizeLayoutOrDepositAmount.this);//we can take any field context
            Cursor cursorData = db.getData("SELECT  DESCRIPTION,DEPOSIT,MICPATH FROM " + db.TABLE_NAME2 + " WHERE ID= '" + getIntent().getStringExtra("ID") + "'" + " AND DATE= '" + getIntent().getStringExtra("DATE") + "'" + " AND TIME='" + getIntent().getStringExtra("TIME") + "'");
            cursorData.moveToFirst();//this cursor is not closed
            String cdescription,cdeposit,cmicpath;
            cdescription=cursorData.getString(0);
            cdeposit=cursorData.getString(1);
            cmicpath=cursorData.getString(2);
            cursorData.close();

            binding.customDepositEt.setText(cdeposit);//fetching deposit

             previousDataHold[0]="DATE- "+getIntent().getStringExtra("DATE");
             previousDataHold[1]="TIME- "+getIntent().getStringExtra("TIME");
             previousDataHold[2]="DEPOSIT- "+cdeposit;
             previousDataHold[3]="REMARKS- "+cdescription;

            if(cmicpath != null) {//if there is audio then set to color  green
                binding.customMicIconTv.setVisibility(View.GONE);//user wound be able to save voice for second time if there is already voice because we want to keep previous voice save we dont want to delete previous voice
                binding.customSaveAudioIconTv.setVisibility(View.GONE);

                binding.customChronometer.setTextColor(getColor(R.color.green));
                binding.customChronometer.setText("PLAY");
                binding.customChronometer.setGravity(Gravity.RIGHT);//setting text to right
                binding.customChronometer.setTypeface(null, Typeface.BOLD);//changing text to bold
            }

            binding.customChronometer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (cmicpath != null || file != null) {//checking audio is present or not
                        MediaPlayer mediaPlayer = new MediaPlayer();
                        try {

                            if(file != null){//if new audio is set then file will contain audio and data.getMicPath() will contain null
                                mediaPlayer.setDataSource(file.getAbsolutePath());
                            }else
                                mediaPlayer.setDataSource(cmicpath);//passing the path where this audio is saved

                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            Toast.makeText(view.getContext(), "AUDIO PLAYING", Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else
                        Toast.makeText(CustomizeLayoutOrDepositAmount.this, "TAB ON MIC TO START RECORDING", Toast.LENGTH_SHORT).show();
                }
            });
            binding.customSaveBtn.setOnLongClickListener(view -> {
                int depositAmount=0;
                String micPath=cmicpath;//default value if we dont fetch previous data then null will be inserted and previous voice will be deleted when we try to update only deposit so it is important
                String remarks;

                //To get exact time so write code in save button
                Date d=Calendar.getInstance().getTime();
                SimpleDateFormat sdf=new SimpleDateFormat("hh:mm:ss a");//a stands for is AM or PM
                String onlyTime = sdf.format(d);
                binding.customTimeTv.setText(onlyTime);//setting time to take time and store in db
                String time=binding.customTimeTv.getText().toString();
                String date=binding.customDateTv.getText().toString();

                //this will store latest date by taking current date
                final Calendar current1 =Calendar.getInstance();//to get current date
                String currentDate = current1.get(Calendar.DAY_OF_MONTH)+"-"+(current1.get(Calendar.MONTH)+1)+"-"+ current1.get(Calendar.YEAR);
//                if(date.equals(currentDate)) {//if it is true then store
//                    db.updateTable("UPDATE " + db.TABLE_NAME1 + " SET  LATESTDATE='" +date + "'" +" WHERE ID='" + getIntent().getStringExtra("ID") + "'");
//                }
                db.updateTable("UPDATE " + db.TABLE_NAME1 + " SET  LATESTDATE='" + currentDate + "' , TIME= '"+ onlyTime  +"' WHERE ID='" + fromIntentPersonId + "'");//when ever user insert its wages or deposit then latest date AND TIME  will be updated to current date AND TIME not user entered date



                if(file !=null){//if file is not null then only it execute otherwise nothing will be inserted
                    micPath=file.getAbsolutePath();
                 }

                //if user dont enter remarks or description then it is sure that previous data will be entered so no need to check null pointer exception
                remarks = "[" + time + "-EDITED]\n\n"+"[DEPOSITED]-"+binding.customDescriptionEt.getText().toString().trim()+"\n\n*****PREVIOUS DATA WAS*****\n" + previousDataHold[0] + "  " + previousDataHold[1] + "\n" + previousDataHold[2] + "\n" + previousDataHold[3] ;//time is set automatically to remarks if user enter any remarks;
                arr[1] = 1;//this is important because when user do not enter any data while updating then atleast 1 field should be filled with data so this field will sure be filled automatically so this is important.

                boolean isWrongData,isDataPresent,success;
                  isWrongData= isEnterDataIsWrong(arr);//it should be here to get updated result
                  isDataPresent= isDataPresent(arr);
                if(isDataPresent==true && isWrongData==false ) {  //means if data is present then check is it right data or not
                    if(binding.customDepositEt.getText().toString().trim().length() >= 1) {
                        depositAmount = Integer.parseInt(binding.customDepositEt.getText().toString().trim());
                    }

                    db.updateTable("UPDATE " + db.TABLE_NAME1 + " SET ACTIVE='" + 1 + "'" + " WHERE ID='" + getIntent().getStringExtra("ID") + "'");//when ever user update then that person will become active

                    if(micPath != null) {//if it is not null then update micpath
                      //  success = db.updateTable("UPDATE " + db.TABLE_NAME2 + " SET DATE='" + date + "',TIME='" + time + "',DESCRIPTION='" + remarks + "',MICPATH='" + micPath + "',DEPOSIT='" + depositAmount + "' WHERE ID= '" + getIntent().getStringExtra("ID") + "'" + " AND DATE= '" + getIntent().getStringExtra("DATE") + "'" + " AND TIME='" + getIntent().getStringExtra("TIME") + "'");
                        success=db.update_Deposit_TABLE_NAME2(date,time,micPath,remarks,depositAmount,getIntent().getStringExtra("ID"),getIntent().getStringExtra("DATE"),getIntent().getStringExtra("TIME"));
                    } else {//if micPath == null then we are not updating because null in text will be set to micpath and give wroing result like it will indicate that audio is present but actually audio is not present
                       // success = db.updateTable("UPDATE " + db.TABLE_NAME2 + " SET DATE='" + date + "',TIME='" + time + "',DESCRIPTION='" + remarks + "',DEPOSIT='" + depositAmount + "' WHERE ID= '" + getIntent().getStringExtra("ID") + "'" + " AND DATE= '" + getIntent().getStringExtra("DATE") + "'" + " AND TIME='" + getIntent().getStringExtra("TIME") + "'");
                        success=db.update_Deposit_TABLE_NAME2(date,time,null,remarks,depositAmount,getIntent().getStringExtra("ID"),getIntent().getStringExtra("DATE"),getIntent().getStringExtra("TIME"));
                    }
                    if (success == true) {
                        displResult("DEPOSIT - "+depositAmount,"\nDATE-  "+date+"\n\nREMARKS- "+remarks+"\n\nMICPATH- "+micPath);
                    } else
                        Toast.makeText(CustomizeLayoutOrDepositAmount.this, "FAILED TO INSERT", Toast.LENGTH_LONG).show();

                }else
                    Toast.makeText(CustomizeLayoutOrDepositAmount.this, "CORRECT THE DATA or CANCEL AND ENTER AGAIN", Toast.LENGTH_LONG).show();

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

    private void startRecordingVoice() {
        Long  tsLong=System.currentTimeMillis()/1000;//folder name should be unique so taking time as name of mic record so every record name will be different
        String ts=tsLong.toString();
        fileName="audio_"+ts;//file name
        file=new File(getExternalFilesDir( null )+"/acBookMicRecording/"+fileName+".mp3");//fileName.mp3 fill will be stored in folder acBookMicRecordingpath

        //https://developer.android.com/reference/android/media/MediaRecorder
        mediaRecorder=new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//Sets the number of audio channels for recording.
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);//Sets the format of the output file produced during recording
        mediaRecorder.setOutputFile(file.getAbsolutePath());//giving file path where fill will be stored
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setAudioChannels(1);//Sets the number of audio channels for recording here setting to 1.

        try{//to start mediaRecorder should be in try catch block
            mediaRecorder.prepare();//first prepare then start
            mediaRecorder.start();
            mstartingTimeMillis=System.currentTimeMillis();
        }catch (IOException e){
            e.printStackTrace();
        }
        Toast.makeText(CustomizeLayoutOrDepositAmount.this, "RECORDING", Toast.LENGTH_SHORT).show();
    }
    private  void stopAndSaveRecordingPathToDB(){
        mediaRecorder.stop();
        mElapsedMillis=(System.currentTimeMillis()-mstartingTimeMillis);
        mediaRecorder.release();
        mediaRecorder=null;
       // Toast.makeText(this, "Recording SAVED "+file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
    }
    private boolean isEnterDataIsWrong(int[] arr) {
        boolean bool=true;
        int two=0;
        for(int i=0 ;i <arr.length;i++) {
            if (arr[i] == 2)
                two++;
        }
        if(two >=1)//data is wrong
            bool=true;
        else
            bool=false;//data is right
        return bool;
    }
    private void displResult(String title,String message) {
        AlertDialog.Builder showDataFromDataBase=new AlertDialog.Builder(CustomizeLayoutOrDepositAmount.this);
        showDataFromDataBase.setCancelable(false);
        showDataFromDataBase.setTitle(title);
        showDataFromDataBase.setMessage(message);
        showDataFromDataBase.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                //after data entered successfully
                finish();//destroy current activity
                Intent intent=new Intent(CustomizeLayoutOrDepositAmount.this,IndividualPersonDetailActivity.class);
                intent.putExtra("ID",fromIntentPersonId);
                startActivity(intent);//while cancelling we will go back to previous Activity with updated activity so passing id to get particular person detail
            }
        });
        showDataFromDataBase.create().show();
    }
    private boolean isDataPresent(int[] arr){
        boolean bool=true;
        int sum,one;
        sum=one=0;

        for(int i=0 ;i <arr.length;i++){

            if(arr[i]== 1)
                one++;

            sum=sum+arr[i];
        }
        if(sum == 0)//data is not present
            bool= false;
        else if((one >= 1))//data is present
            bool= true;
        return bool;
    }
    private boolean checkPermission() {//checking for permission of mic and external storage
        if( (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO)== PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED)) {
            return true;
        }else {
            return false;
        }
    }
}