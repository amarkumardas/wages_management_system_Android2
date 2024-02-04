package amar.das.acbook.adapters;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaRecorder;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import amar.das.acbook.Database;
import amar.das.acbook.R;
import amar.das.acbook.activity.CustomizeLayoutOrDepositAmount;
import amar.das.acbook.activity.IndividualPersonDetailActivity;
import amar.das.acbook.model.WagesDetailsModel;
import amar.das.acbook.utility.MyUtility;
import amar.das.acbook.voicerecording.VoiceRecorder;

public class WagesDetailsAdapter extends RecyclerView.Adapter<WagesDetailsAdapter.ViewHolder> {
    Context context;
    ArrayList<WagesDetailsModel> arrayList;
    Database db;
    byte indicator;
   // boolean bool;
    int []arr=new int[6];
    String []previousDataHold=new String[7];
   // String fromIntentPersonId;
    //for recording variable declaration
    MediaRecorder mediaRecorder;
    String audioPath;
    int cYear;
    byte cMonth,cDayOfMonth;
    boolean toggleToStartRecording=false;
     //String currentDate =MyUtility.getOnlyCurrentDate();
    public WagesDetailsAdapter(Context context, ArrayList<WagesDetailsModel> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.single_wages_row,parent,false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
       WagesDetailsModel data=arrayList.get(position);

       holder.date.setText(data.getUserGivenDate());
       if(!data.getIsdeposited()) {//if wages is there and deposit not there then set wages
           if (data.getWagesOrDeposit() != 0){
               holder.wages.setText(MyUtility.convertToIndianNumberSystem(data.getWagesOrDeposit()));
            }else{
               holder.wages.setText("");
           }
           holder.wages.setTextColor(Color.BLACK);
                 //if we put data.getIsDeposited().equals("1") then unwanted 0 will be set if not data is present so not using it.
      // } else if( data.getDeposit() != 0 && data.getWages() == 0  && data.getP1() == 0){//if wages is not there and deposit there then set wages and color to green
       }else if(data.getIsdeposited()){//if wages is not there and deposit there then set wages and color to green
           if (data.getWagesOrDeposit() != 0){
               holder.wages.setText(MyUtility.convertToIndianNumberSystem(data.getWagesOrDeposit()));//while entering deposit then there will be no p1 or p2p3p4 so checking data.getP1() == 0
           }else{
               holder.wages.setText("");
           }
           holder.wages.setTextColor(context.getColor(R.color.green));

       }
//       else/*if we don't put else statement then default value will be set*/
//           holder.wages.setText("");

       //*************************************Audio and mic*********************************************************
//        if((data.getRemarks() != null) || data.getMicPath() !=null) {//if audio or description is present then set min icon to green
//           holder.spinnerRemarksAudioIcon.setBackgroundResource(R.drawable.ic_green_sharp_mic_20);
//            holder.spinnerRemarksAudioIcon.setEnabled(true);//if there is data then enable
//           bool=true;//means data is present
//       }else {
//            holder.spinnerRemarksAudioIcon.setBackgroundResource(R.drawable.black_sharp_mic_24);
//            holder.spinnerRemarksAudioIcon.setEnabled(false);//if there is no data then disable
//            bool=false;//means data is not present
//        }
//
//        //if(bool != false) {
//       if(bool){//means data is present so it will be clickable so we will set adapter otherwise not
//           String[] audioAndDescription = context.getResources().getStringArray(R.array.audioRemarksShare);
//           ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.select_dialog_item, audioAndDescription);
//           holder.spinnerRemarksAudioIcon.setAdapter(adapter);//adapter set
//       }
        if(data.getMicPath() !=null) {//if only audio  is present then set min icon to green
            holder.spinnerRemarksAudioIcon.setBackgroundResource(R.drawable.ic_green_sharp_mic_20);
        }else {//if no audio present then set min icon to green
            holder.spinnerRemarksAudioIcon.setBackgroundResource(R.drawable.black_sharp_mic_24);
//            holder.spinnerRemarksAudioIcon.setEnabled(false);//if there is no data then disable
//            bool=false;//means data is not present
        }

        //if(bool != false) {
       // if(bool){//means data is present so it will be clickable so we will set adapter otherwise not
           // String[] audioAndDescription = context.getResources().getStringArray(R.array.audioRemarksShare);
           // ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.select_dialog_item,context.getResources().getStringArray(R.array.audioRemarksShare));
            holder.spinnerRemarksAudioIcon.setAdapter(new ArrayAdapter<>(context, android.R.layout.select_dialog_item,context.getResources().getStringArray(R.array.audioRemarksShare)));//adapter set
       // }
        holder.spinnerRemarksAudioIcon.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {//this will only execute when there is data
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                MyUtility.spinnerAudioRemarksShare(adapterView,pos,view,data.getMicPath(),data.getRemarks(),context,data);
                //after selecting second time remarks data is not shown so 0 is set so that when second time click it will show data
               //int initialPosition = holder.spinnerDescAudioIcon.getSelectedItemPosition();
                holder.spinnerRemarksAudioIcon.setSelection(0, false);//clearing auto selected or if we remove this line then only one time we would be able to select audio and remarks which we don't want
              }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
       //*************************************done audio and mic*********************************************************
        indicator=MyUtility.get_indicator(context,data.getId());  //showing data of p1 p2p3p4 if data is present
        holder.p1.setText("");//initially all will be blank
        holder.p2.setText("");
        holder.p3.setText("");
        holder.p4.setText("");
        if(data.getP1() != 0) {//default skill will be common to all
            holder.p1.setText(String.valueOf(data.getP1()));
        }
         if(indicator==2){
            if(data.getP2() != 0) {
                holder.p2.setText(String.valueOf(data.getP2()));
            }
            
        }else if(indicator==3){
            if(data.getP2() != 0) {
                holder.p2.setText(String.valueOf(data.getP2()));
            }

            if(data.getP3() != 0) {
                holder.p3.setText(String.valueOf(data.getP3()));
            }

        }else if(indicator==4){
             if(data.getP2() != 0) {
                 holder.p2.setText(String.valueOf(data.getP2()));
             }

             if(data.getP3() != 0) {
                 holder.p3.setText(String.valueOf(data.getP3()));
             }

            if(data.getP4() != 0) {
                holder.p4.setText(String.valueOf(data.getP4()));
            }
        }
         //************************SETTING BACKGROUND COLOR ACCORDING TO PREVIOUS AND TODAY'S DATE*******************************
        if(MyUtility.getDate(data.getSystemDateAndTime()).equals(getPreviousDateFromCurrentDate((byte)1)))//if data has entered yesterday then set background to gray
            holder.itemView.setBackgroundColor(context.getColor(R.color.background));
        else if(getPreviousDateFromCurrentDate((byte) 0).equals(MyUtility.getDate(data.getSystemDateAndTime())))//if data has entered today then set background to yellow
        holder.itemView.setBackgroundColor(context.getColor(R.color.yellow));
        else//if we don't put else statement then other layout also color get change so else is important
        holder.itemView.setBackgroundColor(context.getColor(R.color.wagesSingleRecord));
        //************************ DONE SETTING BACKGROUND COLOR ACCORDING TO PREVIOUS AND TODAY'S DATE*******************************

        //for updating
        holder.itemView.setOnLongClickListener(view -> {
            AlertDialog.Builder updateCustomDialog = new AlertDialog.Builder(view.getContext());
            LayoutInflater inflater = LayoutInflater.from(view.getContext());

            View myView = inflater.inflate(R.layout.input_data_to_recycler, null);//myView contain all layout view ids
            updateCustomDialog.setView(myView);//set custom layout to alert dialog
            updateCustomDialog.setCancelable(false);//if user touch to other place then dialog will be close

            final AlertDialog dialog = updateCustomDialog.create();//myCustomDialog variable cannot be use in inner class so creating another final variable  to use in inner class

            mediaRecorder=null;//so that it not take previous VALUE
            audioPath=null;//so that it not take previous VALUE

            TextView deposit_btn_tv = myView.findViewById(R.id.to_deposit_tv);
            Spinner customSpinnerRemoveOrAddMlg=myView.findViewById(R.id.info_spinner_add_lmg_or_remove);
            deposit_btn_tv.setVisibility(View.GONE);//initially no deposit button because we are updating only wages p1,p2...etc
            customSpinnerRemoveOrAddMlg.setVisibility(View.GONE);
            TextView hardcodedP1 = myView.findViewById(R.id.hardcoded_p1_tv);
            TextView hardcodedP2 = myView.findViewById(R.id.hardcoded_p2_tv);
            TextView hardcodedP3 = myView.findViewById(R.id.hardcoded_p3_tv);
            TextView hardcodedP4 = myView.findViewById(R.id.hardcoded_p4_tv);
            TextView micIcon = myView.findViewById(R.id.mic_tv);
            TextView inputDate = myView.findViewById(R.id.input_date_tv);
            TextView saveAudio = myView.findViewById(R.id.save_audio_tv);

            Chronometer playAudioChronometer = myView.findViewById(R.id.chronometer);

            EditText inputP1 = myView.findViewById(R.id.input_p1_et);
            TextView runtimeSuggestionAmountToGive = myView.findViewById(R.id.work_amount_tv);
            EditText inputP2 = myView.findViewById(R.id.input_p2_et);
            EditText inputP3 = myView.findViewById(R.id.input_p3_et);
            EditText inputP4 = myView.findViewById(R.id.input_p4_et);
            EditText toGive_Amount = myView.findViewById(R.id.wages_et);
            EditText description = myView.findViewById(R.id.enter_description_et);
            TextView message_tv = myView.findViewById(R.id.only_used_while_updating);
            Button save = myView.findViewById(R.id.save_btn);
            Button cancel = myView.findViewById(R.id.cancel_btn);
            cancel.setOnClickListener(view13 -> {
                VoiceRecorder.stopAudioPlayer();//when audio is playing and   user clock  cancel then stop audio also
                MyUtility.deletePdfOrRecordingUsingPathFromDevice(audioPath);//delete Audio If Not user Saved
                dialog.dismiss();
            });

            if (!data.getIsdeposited()) {// 0 means not deposited it is important because it will open window to enter deposit or wages.wrong window should not be opened.
            message_tv.setText(view.getContext().getResources().getString(R.string.you_are_updating));
            save.setText(view.getContext().getResources().getString(R.string.long_press_to_update));

            db = new Database(holder.wages.getContext());//we can take any field context

                Cursor cursorData = db.getWagesForUpdate(data.getId(),data.getSystemDateAndTime());
                cursorData.moveToFirst();//this cursor is not closed

//********************************CUSTOMIZATION*******************************************************************************************
            //initially every field will be invisible based on indicator others fields will be visible
            hardcodedP2.setVisibility(View.GONE);
            inputP2.setVisibility(View.GONE);
            hardcodedP3.setVisibility(View.GONE);
            inputP3.setVisibility(View.GONE);
            hardcodedP4.setVisibility(View.GONE);
            inputP4.setVisibility(View.GONE);

            //CUSTOMIZATION: initially in person skill or type is M,L or G then according to that layout will be customised
            //hardcodedP1,inputP1 by default visible so no need to mention if(indicator == 1) {
            Cursor cursorDefault = db.getData("SELECT "+Database.COL_8_MAINSKILL1 +" FROM " + Database.TABLE_NAME1 + " WHERE "+Database.COL_1_ID+"= '" + data.getId() + "'");//for sure it will return type or skill
            cursorDefault.moveToFirst();//no need to check  cursorDefault !=null because for sure TYPE data is present
            hardcodedP1.setText(cursorDefault.getString(0));
            previousDataHold[0] = cursorDefault.getString(0) + ": " + (cursorData.getString(4)!= null?cursorData.getString(4):0);//to write previous record in description
            toGive_Amount.setText(String.valueOf(cursorData.getInt(3)));//setting wages
            inputP1.setText(String.valueOf(cursorData.getInt(4)));//setting same data to p1
            cursorDefault.close();

            Cursor skillsCursor = db.getData("SELECT "+Database.COL_36_SKILL2 +" , "+Database.COL_37_SKILL3 +" , "+Database.COL_38_SKILL4 +" FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE "+Database.COL_31_ID+"= '" + data.getId() + "'");
            if (skillsCursor != null) {
                skillsCursor.moveToFirst();
                if (indicator == 2) {//two person
                    // hardcodedP1,inputP1 by default visible so no need to mention
                    hardcodedP2.setVisibility(View.VISIBLE);
                    inputP2.setVisibility(View.VISIBLE);
                    hardcodedP2.setText(skillsCursor.getString(0));
                    previousDataHold[1] = skillsCursor.getString(0) + ": " + (cursorData.getString(5)!= null?cursorData.getString(5):0);//to write previous record in description
                    inputP2.setText(String.valueOf(cursorData.getInt(5)));//setting same data to p2
                } else if (indicator == 3) {//three person
                    //hardcodedP1,inputP1 by default visible so no need to mention
                    hardcodedP2.setVisibility(View.VISIBLE);
                    inputP2.setVisibility(View.VISIBLE);
                    hardcodedP2.setText(skillsCursor.getString(0));
                    previousDataHold[1] = skillsCursor.getString(0) + ": " + (cursorData.getString(5)!= null?cursorData.getString(5):0);//to write previous record in description
                    inputP2.setText(String.valueOf(cursorData.getInt(5)));//setting same data to p2

                    hardcodedP3.setVisibility(View.VISIBLE);
                    hardcodedP3.setText(skillsCursor.getString(1));
                    previousDataHold[2] = skillsCursor.getString(1) + ": " + (cursorData.getString(6)!= null?cursorData.getString(6):0);//to write previous record in description
                    inputP3.setText(String.valueOf(cursorData.getInt(6)));//setting same data to p3
                    inputP3.setVisibility(View.VISIBLE);
                } else if (indicator == 4) {////two person
                    //hardcodedP1,inputP1 by default visible so no need to mention
                    hardcodedP2.setVisibility(View.VISIBLE);
                    inputP2.setVisibility(View.VISIBLE);
                    hardcodedP2.setText(skillsCursor.getString(0));
                    previousDataHold[1] = skillsCursor.getString(0) + ": " + (cursorData.getString(5)!= null?cursorData.getString(5):0);//to write previous record in description
                    inputP2.setText(String.valueOf(cursorData.getInt(5)));//setting same data to p2

                    hardcodedP3.setVisibility(View.VISIBLE);
                    hardcodedP3.setText(skillsCursor.getString(1));
                    previousDataHold[2] = skillsCursor.getString(1) + ": " + (cursorData.getString(6)!= null?cursorData.getString(6):0);//to write previous record in description
                    inputP3.setVisibility(View.VISIBLE);
                    inputP3.setText(String.valueOf(cursorData.getInt(6)));//setting same data to p3

                    hardcodedP4.setVisibility(View.VISIBLE);
                    hardcodedP4.setText(skillsCursor.getString(2));
                    previousDataHold[3] = skillsCursor.getString(2) + ": " + (cursorData.getString(7)!= null?cursorData.getString(7):0);//to write previous record in description
                    inputP4.setText(String.valueOf(cursorData.getInt(7)));//setting same data to p3
                    inputP4.setVisibility(View.VISIBLE);
                }
            } else {
                Toast.makeText(context, "NO DATA IN CURSOR", Toast.LENGTH_LONG).show();
            }
                if(skillsCursor != null) {
                    skillsCursor.close();
                }
//********************************Done CUSTOMIZATION*******************************************************************************************
            previousDataHold[4] = "WAGES: " + (cursorData.getString(3)!=null?MyUtility.convertToIndianNumberSystem(Long.parseLong(cursorData.getString(3))):0);//wages to write previous record in description
            previousDataHold[5] = "DATE: " + cursorData.getString(1)+" ("+MyUtility.getTime12hr(cursorData.getString(9))+")";//date to write previous record in description
            previousDataHold[6] = "REMARKS: " + cursorData.getString(2);//description or remarks

                String [] dateArray =cursorData.getString(1).split("-");
                cursorData.close();
                cDayOfMonth= (byte) Integer.parseInt(dateArray[0]);
                cMonth= (byte) Integer.parseInt(dateArray[1]);
                cYear=Integer.parseInt(dateArray[2]);
                inputDate.setText(Integer.parseInt(dateArray[0]) + "-" + (Integer.parseInt(dateArray[1])) + "-" + Integer.parseInt(dateArray[2]));
                inputDate.setOnClickListener(view1 -> {
                    DatePickerDialog datePickerDialog = new DatePickerDialog(myView.getContext(), (datePicker, year, month, dayOfMonth) -> { //To show calendar dialog
                        inputDate.setText(dayOfMonth + "-" + (month + 1) + "-" + year);//month start from 0 so 1 is added to get right month like 12
                        cDayOfMonth= (byte) dayOfMonth;
                        cMonth= (byte) (month+1);
                        cYear=year;
                    }, cYear, (cMonth-1), cDayOfMonth);//This variable should be ordered this variable will set date day month to calendar to datePickerDialog so passing it
                    datePickerDialog.show();
                });

            //description.setText(cursorData.getString(3));//don't set previous data because if the data is more then update button or cancel button will hide due to large data
            dialog.show();
            //*************************************SAVING*****************************************************************************************************
            save.setOnLongClickListener(view12 -> {
                save.setVisibility(View.GONE);//to avoid when user click multiple times
                //*********************************common to all indicator 1,2,3,4*******************
                VoiceRecorder.stopAudioPlayer();//if user playing audio and click save button then stop audio
                VoiceRecorder.stopRecording();//if user don't click tick button to save recording then while saving all data recording will also get saved automatically.so  VoiceRecorder.stopRecording()  method should be called then only file will be saved

                int p1, p2, p3, p4;//this default value is taken when user do enter date to filed
                p1 = p2 = p3 = p4 = 0;
                int wages = 0;

                String micPath=data.getMicPath();//default value if we don't fetch previous data then null will be inserted and previous voice will be deleted when we try to update only wages so it is important

                String onlyTime =MyUtility.getOnlyTime();

                //if user don't enter remarks or description then it is sure that previous data will be entered so no need to check null pointer exception
               // String remarks = "[" + onlyTime +view.getContext().getString(R.string.hyphen_edited)+"\n\n"+description.getText().toString().trim()+"\n\n"+view12.getContext().getString(R.string.previous_details_were_hyphen)+"\n" + previousDataHold[5] + "  " + previousDataHold[6] + "\n" + previousDataHold[0] + " " + previousDataHold[1] + " " + previousDataHold[2] + " " + previousDataHold[3] + "\n" + previousDataHold[4] + "\n" + previousDataHold[7];//onlyTime is set automatically to remarks if user enter any remarks;
                String remarks =getUpdatedRemarks(indicator,onlyTime,view12,description.getText().toString().trim());
                arr[5] = 1;//this is important because when user do not enter any data while updating then at least 1 field should be filled with data so this field will sure be filled automatically so this is important.

                String userDate = inputDate.getText().toString();//date will be inserted automatically

                if(audioPath !=null){//if file is not null then only it execute otherwise nothing will be inserted
                    micPath= audioPath;
                 }

                boolean success, isWrongData, isDataPresent;
                isWrongData = MyUtility.isEnterDataIsWrong(arr);
                isDataPresent = MyUtility.isDataPresent(arr);
                if (isDataPresent == true && isWrongData == false) {//means if data is present then check is it right data or not .if condition is false then default value will be taken
                    if (toGive_Amount.getText().toString().length() >= 1) {//to prevent null pointer exception
                        wages = Integer.parseInt(toGive_Amount.getText().toString().trim());
                    }
                    //>= if user enter only one digit then >= is important otherwise default value will be set
                    if (inputP1.getText().toString().length() >= 1) {//to prevent null pointer exception
                        p1 = Integer.parseInt(inputP1.getText().toString().trim());//converted to float and stored
                    }
                }else
                    Toast.makeText(context, context.getResources().getString(R.string.correct_the_data_or_cancel_and_enter_again), Toast.LENGTH_LONG).show();

                //*********************************all the upper code are common to all indicator 1,2,3,4*******************

                if (indicator == 1) {
                    if (isDataPresent && isWrongData == false) {//it is important means if data is present then check is it right data or not.if condition is false then this message will be displayed "Correct the Data or Cancel and Enter again"
                        //UPDATE to database
                         if(micPath != null){//if it is not null then update micPath
                             // success = db.updateTable("UPDATE " + db.TABLE_NAME2 + " SET DATE='" + date + "',TIME='" + onlyTime + "',DESCRIPTION='" + remarks +"',MICPATH='"+micPath+ "',WAGES='" + wages + "',P1='" + p1 + "'" + " WHERE ID= '" + data.getId() + "'" + " AND DATE= '" + data.getDate() + "'" + " AND TIME='" + data.getTime() + "'");
                             //success=db.update_1_TABLE_NAME2(date,onlyTime,remarks,micPath,wages,p1,data.getId(),data.getDate(),data.getTime());
                             success=db.updateWagesOrDepositOnlyToActiveTableAndHistoryTableTransaction(userDate,MyUtility.systemCurrentDate24hrTime(),onlyTime,remarks,micPath,wages,p1,0,0,0,data.getId(),data.getSystemDateAndTime());
                        }else {//if micPath == null then we are not updating because null in text will be set to micPath and give wrong result like it will indicate that audio is present but actually audio is not present
                            // success = db.updateTable("UPDATE " + db.TABLE_NAME2 + " SET DATE='" + date + "',TIME='" + onlyTime + "',DESCRIPTION='" + remarks + "',WAGES='" + wages + "',P1='" + p1 + "'" + " WHERE ID= '" + data.getId() + "'" + " AND DATE= '" + data.getDate() + "'" + " AND TIME='" + data.getTime() + "'");
                            // success=db.update_1_TABLE_NAME2(date,onlyTime,remarks,null,wages,p1,data.getId(),data.getDate(),data.getTime());
                             success=db.updateWagesOrDepositOnlyToActiveTableAndHistoryTableTransaction(userDate,MyUtility.systemCurrentDate24hrTime(),onlyTime,remarks,null,wages,p1,0,0,0,data.getId(),data.getSystemDateAndTime());

                         }
                         if(!success){
                             Toast.makeText(context, context.getResources().getString(R.string.failed_to_update), Toast.LENGTH_LONG).show();
                         }
                         refreshCurrentActivity(data.getId());
                         dialog.dismiss();//dialog will be dismiss after saved automatically

//                         if (success) {
//                            displayResultAndRefresh(wages + "          " + p1, "\nDATE- " + date + "\n\n" + "REMARKS- " + remarks+"\n\n"+"MICPATH- "+micPath);
//                            fromIntentPersonId = data.getId();//update to send to other intent for refresh
//                            dialog.dismiss();//dialog will be dismiss after saved automatically
//                        } else
//                            Toast.makeText(context, "FAILED TO UPDATE", Toast.LENGTH_LONG).show();
                    } else//once user enter wrong data and left blank then user wound be able to save because array value would not be change it will be 2 so  user have to "Cancel and enter again" if use don't leave blank then it will save successfully
                        MyUtility.snackBar(view12,context.getResources().getString(R.string.correct_the_data_or_cancel_and_enter_again));

                } else if (indicator == 2) {
                    //p1 is automatically added
                    if (isDataPresent && isWrongData == false) {
                        if (inputP2.getText().toString().length() >= 1) {//to prevent null pointer exception
                            p2 = Integer.parseInt(inputP2.getText().toString().trim());//converted to float and stored
                        }
                        //UPDATE to database
                        if(micPath != null){//if it is not null then update micPath
                           // success = db.updateTable("UPDATE " + db.TABLE_NAME2 + " SET DATE='" + date + "',TIME='" + onlyTime + "',DESCRIPTION='" + remarks + "',MICPATH='"+micPath+"',WAGES='" + wages + "',P1='" + p1 + "'" + ",P2='" + p2 + "'  WHERE ID= '" + data.getId() + "'" + " AND DATE= '" + data.getDate() + "'" + " AND TIME='" + data.getTime() + "'");
                            //success=db.update_2_TABLE_NAME2(date,onlyTime,remarks,micPath,wages,p1,p2,data.getId(),data.getDate(),data.getTime());
                            success=db.updateWagesOrDepositOnlyToActiveTableAndHistoryTableTransaction(userDate,MyUtility.systemCurrentDate24hrTime(),onlyTime,remarks,micPath,wages,p1,p2,0,0,data.getId(),data.getSystemDateAndTime());

                        }else {//if micPath == null then we are not updating because null in text will be set to micPath and give wrong result like it will indicate that audio is present but actually audio is not present
                            //success = db.updateTable("UPDATE " + db.TABLE_NAME2 + " SET DATE='" + date + "',TIME='" + onlyTime + "',DESCRIPTION='" + remarks + "',WAGES='" + wages + "',P1='" + p1 + "'" + ",P2='" + p2 + "'  WHERE ID= '" + data.getId() + "'" + " AND DATE= '" + data.getDate() + "'" + " AND TIME='" + data.getTime() + "'");
                            //success=db.update_2_TABLE_NAME2(date,onlyTime,remarks,null,wages,p1,p2,data.getId(),data.getDate(),data.getTime());
                            success=db.updateWagesOrDepositOnlyToActiveTableAndHistoryTableTransaction(userDate,MyUtility.systemCurrentDate24hrTime(),onlyTime,remarks,null,wages,p1,p2,0,0,data.getId(),data.getSystemDateAndTime());
                        }
                        if(!success){
                            Toast.makeText(context, context.getResources().getString(R.string.failed_to_update), Toast.LENGTH_LONG).show();
                        }
                        refreshCurrentActivity(data.getId());
                        dialog.dismiss();//dialog will be dismiss after saved automatically
//                        if (success) {
//                            displayResultAndRefresh(wages + "          " + p1 + "     " + p2, "\nDATE- " + date + "\n\n" + "REMARKS- " + remarks+"\n\n"+"MICPATH- "+micPath);
//                            fromIntentPersonId = data.getId();//update to send to other intent for refresh
//                            dialog.dismiss();//dialog will be dismiss after saved automatically
//                        } else
//                            Toast.makeText(context, "FAILED TO UPDATE", Toast.LENGTH_LONG).show();
                    }else
                        MyUtility.snackBar(view12,context.getResources().getString(R.string.correct_the_data_or_cancel_and_enter_again));

                } else if (indicator == 3) {
                    if (isDataPresent && isWrongData == false) {
                        if (inputP2.getText().toString().length() >= 1) {//to prevent null pointer exception
                            p2 = Integer.parseInt(inputP2.getText().toString().trim());//converted to float and stored
                        }
                        if (inputP3.getText().toString().length() >= 1) {//to prevent null pointer exception
                            p3 = Integer.parseInt(inputP3.getText().toString().trim());//converted to float and stored
                        }
                        //UPDATE to database
                        if(micPath != null){//if it is not null then update micPath
                           // success = db.updateTable("UPDATE " + db.TABLE_NAME2 + " SET DATE='" + date + "',TIME='" + onlyTime + "',DESCRIPTION='" + remarks +"',MICPATH='"+micPath+ "',WAGES='" + wages + "',P1='" + p1 + "'" + ",P2='" + p2 + "'" + ",P3='" + p3 + "' WHERE ID= '" + data.getId() + "'" + " AND DATE= '" + data.getDate() + "'" + " AND TIME='" + data.getTime() + "'");
                           // success=db.update_3_TABLE_NAME2(date,onlyTime,remarks,micPath,wages,p1,p2,p3,data.getId(),data.getDate(),data.getTime());
                            success=db.updateWagesOrDepositOnlyToActiveTableAndHistoryTableTransaction(userDate,MyUtility.systemCurrentDate24hrTime(),onlyTime,remarks,micPath,wages,p1,p2,p3,0,data.getId(),data.getSystemDateAndTime());

                        }else {//if micPath == null then we are not updating because null in text will be set to micPath and give wrong result like it will indicate that audio is present but actually audio is not present
                           // success = db.updateTable("UPDATE " + db.TABLE_NAME2 + " SET DATE='" + date + "',TIME='" + onlyTime + "',DESCRIPTION='" + remarks + "',WAGES='" + wages + "',P1='" + p1 + "'" + ",P2='" + p2 + "'" + ",P3='" + p3 + "' WHERE ID= '" + data.getId() + "'" + " AND DATE= '" + data.getDate() + "'" + " AND TIME='" + data.getTime() + "'");
                            //success=db.update_3_TABLE_NAME2(date,onlyTime,remarks,null,wages,p1,p2,p3,data.getId(),data.getDate(),data.getTime());
                            success=db.updateWagesOrDepositOnlyToActiveTableAndHistoryTableTransaction(userDate,MyUtility.systemCurrentDate24hrTime(),onlyTime,remarks,null,wages,p1,p2,p3,0,data.getId(),data.getSystemDateAndTime());
                        }

                        if(!success){
                            Toast.makeText(context, context.getResources().getString(R.string.failed_to_update), Toast.LENGTH_LONG).show();
                        }
                        refreshCurrentActivity(data.getId());
                        dialog.dismiss();//dialog will be dismiss after saved automatically
//                        if (success) {
//                            displayResultAndRefresh(wages + "          " + p1 + "     " + p2 + "     " + p3, "\nDATE- " + date + "\n\n" + "REMARKS- " + remarks+"\n\n"+"MICPATH- "+micPath);
//                            fromIntentPersonId = data.getId();//update to send to other intent for refresh
//                            dialog.dismiss();//dialog will be dismiss after saved automatically
//                        } else
//                            Toast.makeText(context, "FAILED TO UPDATE", Toast.LENGTH_LONG).show();
                    } else
                        MyUtility.snackBar(view12,context.getResources().getString(R.string.correct_the_data_or_cancel_and_enter_again));

                } else if (indicator == 4) {
                    if (isDataPresent && isWrongData == false) {
                        if (inputP2.getText().toString().length() >= 1) {//to prevent null pointer exception.If user do not enter any data then that onlyTime it will save from crashing app.So due to this condition if field is empty then default value will be taken
                            p2 = Integer.parseInt(inputP2.getText().toString().trim());//converted to INT and stored
                        }
                        if (inputP3.getText().toString().length() >= 1) {//to prevent null pointer exception
                            p3 = Integer.parseInt(inputP3.getText().toString().trim());//converted to INT and stored
                        }
                        if (inputP4.getText().toString().length() >= 1) {//to prevent null pointer exception
                            p4 = Integer.parseInt(inputP4.getText().toString().trim());//converted to INT and stored
                        }
                        //UPDATE to database
                        if(micPath != null){//if it is not null then update micPath
                           // success = db.updateTable("UPDATE " + db.TABLE_NAME2 + " SET DATE='" + date + "',TIME='" + onlyTime + "',DESCRIPTION='" + remarks +"',MICPATH='"+micPath+ "',WAGES='" + wages + "',P1='" + p1 + "'" + ",P2='" + p2 + "'" + ",P3='" + p3 + "',P4='" + p4 + "' WHERE ID= '" + data.getId() + "'" + " AND DATE= '" + data.getDate() + "'" + " AND TIME='" + data.getTime() + "'");
                           // success=db.update_4_TABLE_NAME2(date,onlyTime,remarks,micPath,wages,p1,p2,p3,p4,data.getId(),data.getDate(),data.getTime());
                            success=db.updateWagesOrDepositOnlyToActiveTableAndHistoryTableTransaction(userDate,MyUtility.systemCurrentDate24hrTime(),onlyTime,remarks,micPath,wages,p1,p2,p3,p4,data.getId(),data.getSystemDateAndTime());

                        }else {//if micPath == null then we are not updating because null in text will be set to micPath and give wrong result like it will indicate that audio is present but actually audio is not present
                            //success = db.updateTable("UPDATE " + db.TABLE_NAME2 + " SET DATE='" + date + "',TIME='" + onlyTime + "',DESCRIPTION='" + remarks + "',WAGES='" + wages + "',P1='" + p1 + "'" + ",P2='" + p2 + "'" + ",P3='" + p3 + "',P4='" + p4 + "' WHERE ID= '" + data.getId() + "'" + " AND DATE= '" + data.getDate() + "'" + " AND TIME='" + data.getTime() + "'");
                           // success=db.update_4_TABLE_NAME2(date,onlyTime,remarks,null,wages,p1,p2,p3,p4,data.getId(),data.getDate(),data.getTime());
                            success=db.updateWagesOrDepositOnlyToActiveTableAndHistoryTableTransaction(userDate,MyUtility.systemCurrentDate24hrTime(),onlyTime,remarks,null,wages,p1,p2,p3,p4,data.getId(),data.getSystemDateAndTime());
                        }

                        if(!success){
                            Toast.makeText(context, context.getResources().getString(R.string.failed_to_update), Toast.LENGTH_LONG).show();
                        }
                        refreshCurrentActivity(data.getId());
                        dialog.dismiss();//dialog will be dismiss after saved automatically
//                            if (success) {
//                                displayResultAndRefresh(wages + "          " + p1 + "     " + p2 + "     " + p3 + "     " + p4, "\nDATE- " + date + "\n\n" + "REMARKS- " + remarks+"\n\n"+"MICPATH- "+micPath);
//                                fromIntentPersonId = data.getId();//update to send to other intent for refresh
//                                dialog.dismiss();//dialog will be dismiss after saved automatically
//                            } else
//                                Toast.makeText(context, "FAILED TO UPDATE", Toast.LENGTH_LONG).show();
                    } else
                        MyUtility.snackBar(view12,context.getResources().getString(R.string.correct_the_data_or_cancel_and_enter_again));
                 }

                audioPath =null;//since audio is saved then make this variable null otherwise audio will be deleted ON CANCEL OR ON DESTROY only if user don't enter save button

                return false;
            });
            //*************************************DONE SAVING*****************************************************************************************************
            toGive_Amount.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    String amount = toGive_Amount.getText().toString().trim();
                    toGive_Amount.setTextColor(Color.BLACK);
                    arr[4] = 1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data
                    if(!MyUtility.isEnterDataIsWrong(arr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                        save.setVisibility(View.VISIBLE);
                    }
                    if (!amount.matches("[0-9]+")) {//no space or . or ,
                       // Toast.makeText(context, "NOT ALLOWED(space  .  ,  -)\nPLEASE CORRECT", Toast.LENGTH_LONG).show();
                        toGive_Amount.setTextColor(Color.RED);
                        save.setVisibility(View.GONE);
                        arr[4] = 2;//means wrong data
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
            inputP1.addTextChangedListener(new TextWatcher() {
                Cursor result = db.getData("SELECT  "+Database.COL_32_R1+" , "+Database.COL_33_R2+" , "+Database.COL_34_R3+" , "+Database.COL_35_R4+"  FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE "+Database.COL_31_ID+"= '" + data.getId() + "'");

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    String p11 = inputP1.getText().toString().trim();
                    inputP1.setTextColor(Color.BLACK);
                    arr[0] = 1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data
                    if(!MyUtility.isEnterDataIsWrong(arr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                        save.setVisibility(View.VISIBLE);
                    }
                    if (!p11.matches("[0-9]+")) {//"[.]?[0-9]+[.]?[0-9]*" for float
                        inputP1.setTextColor(Color.RED);
                        save.setVisibility(View.GONE);
                        arr[0] = 2;//means wrong data
                        //Toast.makeText(context, "NOT ALLOWED(space  .  ,  -)\nPLEASE CORRECT", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {//after text changed for suggestion calculate based on previous rate
                    result.moveToFirst();
                    p1_p2_p3_p4_Change_Tracker(result, inputP1, inputP2, inputP3, inputP4, runtimeSuggestionAmountToGive);
                }
            });
            inputP2.addTextChangedListener(new TextWatcher() {
               // Cursor result = db.getData("SELECT  R1,R2,R3,R4  FROM " + db.TABLE_NAME3 + " WHERE ID= '" + data.getId() + "'");
                Cursor result = db.getData("SELECT  "+Database.COL_32_R1+" , "+Database.COL_33_R2+" , "+Database.COL_34_R3+" , "+Database.COL_35_R4+"  FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE "+Database.COL_31_ID+"= '" + data.getId() + "'");

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    String p11 = inputP2.getText().toString().trim();
                    inputP2.setTextColor(Color.BLACK);
                    arr[1] = 1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data
                    if(!MyUtility.isEnterDataIsWrong(arr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                        save.setVisibility(View.VISIBLE);
                    }
                    if (!p11.matches("[0-9]+")) {// "[.]?[0-9]+[.]?[0-9]*"
                        inputP2.setTextColor(Color.RED);
                        save.setVisibility(View.GONE);
                        arr[1] = 2;//means wrong data
                       // Toast.makeText(context, "NOT ALLOWED(space  .  ,  -)\nPLEASE CORRECT", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    result.moveToFirst();
                    p1_p2_p3_p4_Change_Tracker(result, inputP1, inputP2, inputP3, inputP4, runtimeSuggestionAmountToGive);
                }
            });
            inputP3.addTextChangedListener(new TextWatcher() {
                //Cursor result = db.getData("SELECT  R1,R2,R3,R4  FROM " + db.TABLE_NAME3 + " WHERE ID= '" + data.getId() + "'");
                Cursor result = db.getData("SELECT  "+Database.COL_32_R1+" , "+Database.COL_33_R2+" , "+Database.COL_34_R3+" , "+Database.COL_35_R4+"  FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE "+Database.COL_31_ID+"= '" + data.getId() + "'");

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    String p11 = inputP3.getText().toString().trim();
                    inputP3.setTextColor(Color.BLACK);
                    arr[2] = 1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data
                    if(!MyUtility.isEnterDataIsWrong(arr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                        save.setVisibility(View.VISIBLE);
                    }
                    if (!p11.matches("[0-9]+")) {//space or , or - is restricted
                        inputP3.setTextColor(Color.RED);
                        save.setVisibility(View.GONE);
                        arr[2] = 2;//means wrong data
                       // Toast.makeText(context, "NOT ALLOWED(space  .  ,  -)\nPLEASE CORRECT", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    result.moveToFirst();
                    p1_p2_p3_p4_Change_Tracker(result, inputP1, inputP2, inputP3, inputP4, runtimeSuggestionAmountToGive);
                }
            });
            inputP4.addTextChangedListener(new TextWatcher() {
               // Cursor result = db.getData("SELECT  R1,R2,R3,R4  FROM " + db.TABLE_NAME3 + " WHERE ID= '" + data.getId() + "'");
                Cursor result = db.getData("SELECT  "+Database.COL_32_R1+" , "+Database.COL_33_R2+" , "+Database.COL_34_R3+" , "+Database.COL_35_R4+"  FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE "+Database.COL_31_ID+"= '" + data.getId() + "'");

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    String p11 = inputP4.getText().toString().trim();
                    inputP4.setTextColor(Color.BLACK);
                    arr[3] = 1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data

                    if(!MyUtility.isEnterDataIsWrong(arr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                        save.setVisibility(View.VISIBLE);
                    }
                    if (!p11.matches("[0-9]+")) {//space or , or - is restricted
                        inputP4.setTextColor(Color.RED);
                        save.setVisibility(View.GONE);
                        arr[3] = 2;//means wrong data
                        //Toast.makeText(context, "NOT ALLOWED(space  .  ,  -)\nPLEASE CORRECT", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    result.moveToFirst();
                    p1_p2_p3_p4_Change_Tracker(result, inputP1, inputP2, inputP3, inputP4, runtimeSuggestionAmountToGive);
                }
            });

            micIcon.setOnClickListener(view15 -> {
                if(MyUtility.checkPermissionAudioAndExternal(context)){ //checking for permission
                    if (toggleToStartRecording) {//initially false
                        save.setVisibility(View.GONE);

                        playAudioChronometer.setEnabled(false);//when user press save button then set to true playAudioChronometer.setEnabled(true);
                        saveAudio.setBackgroundResource(R.drawable.ic_green_sharp_done_sharp_tick_20);//changing tick color to green so that user can feel to press to save
                        micIcon.setEnabled(false);
                        micIcon.setBackgroundResource(R.drawable.baseline_record_voice_over_24);//change color when user click

                        VoiceRecorder voiceRecorder=new VoiceRecorder(data.getId(),context.getExternalFilesDir(null).toString());
                        if(voiceRecorder.startRecording()) {
                            playAudioChronometer.setBase(SystemClock.elapsedRealtime());//In Android, Chronometer is a class that implements a simple timer. Chronometer is a subclass of TextView. This class helps us to add a timer in our app.
                            playAudioChronometer.start();
                            Toast.makeText(context, view15.getContext().getResources().getString(R.string.recording_started), Toast.LENGTH_LONG).show();

                            audioPath=voiceRecorder.getAudioAbsolutePath();//updating audioPath for further use otherwise it will be null
                            mediaRecorder=voiceRecorder.getMediaRecorder();//updating mediaRecorder for further use  otherwise it will be null
                        }else{
                            Toast.makeText(context,context.getResources().getString(R.string.failed_to_start_recording), Toast.LENGTH_SHORT).show();
                        }

                        ((Activity)context).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//while the user is recording screen should be on. it should not close

                    }else {//if recording is not started then stop
                        Toast.makeText(context,view15.getContext().getResources().getString(R.string.again_tab_on_mic_to_start_recording),Toast.LENGTH_SHORT).show();
                    }
                    toggleToStartRecording = !toggleToStartRecording;//so that user should click 2 times to start recording

                }else {//request for permission
                    Toast.makeText(context,context.getResources().getString(R.string.audio_permission_required), Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 21);
                }
            });

                if(data.getMicPath() != null) {//if there is audio then set to color  green
                    micIcon.setVisibility(View.GONE);//user wound be able to save voice for second time if there is already voice because we want to keep previous voice save we don't want to delete previous voice
                    saveAudio.setVisibility(View.GONE);

                    playAudioChronometer.setTextColor(context.getColor(R.color.green));
                    playAudioChronometer.setText(view.getContext().getString(R.string.play));
                    playAudioChronometer.setGravity(Gravity.RIGHT);//setting text to right
                    playAudioChronometer.setTypeface(null,Typeface.BOLD);//changing text to bold
                }
                playAudioChronometer.setOnClickListener(view16 -> {
                    if (data.getMicPath() != null || audioPath != null) {//checking audio is present or not

                            if(audioPath != null){//if new audio is set then file will contain audio and data.getMicPath() will contain null
                                if(VoiceRecorder.audioPlayer(audioPath)){
                                    Toast.makeText(view16.getContext(), view16.getContext().getResources().getString(R.string.audio_playing), Toast.LENGTH_LONG).show();
                                }else{
                                    Toast.makeText(view16.getContext(), view16.getResources().getString(R.string.audio_not_found_may_be_deleted), Toast.LENGTH_LONG).show();
                                }
                            }else {
                                if(VoiceRecorder.audioPlayer(data.getMicPath())){
                                    Toast.makeText(view16.getContext(), view16.getContext().getResources().getString(R.string.audio_playing), Toast.LENGTH_LONG).show();
                                }else{
                                    Toast.makeText(view16.getContext(), view16.getResources().getString(R.string.audio_not_found_may_be_deleted), Toast.LENGTH_LONG).show();
                                }
                            }
                    }else
                        Toast.makeText(context, view16.getContext().getResources().getString(R.string.tab_on_mic_to_start_recording), Toast.LENGTH_SHORT).show();
                });
                saveAudio.setOnClickListener(view17 -> { //after clicking save audio then setEnabled to true so that user can enter data to fields
                    if(mediaRecorder !=null){

                        if(!MyUtility.isEnterDataIsWrong(arr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                            save.setVisibility(View.VISIBLE);
                         }

                        playAudioChronometer.setTextColor(context.getColor(R.color.green));//changing text color to green to give feel that is saved
                        micIcon.setBackgroundResource(R.drawable.ic_green_sharp_mic_20);//set background image to cancel
                        VoiceRecorder.stopRecording();//to save correct audio file in device stopRecording() method should be called then only file will ve saved
                        playAudioChronometer.stop();//stopping chronometer
                        micIcon.setEnabled(false);//so that user cannot press again this button
                        saveAudio.setEnabled(false);//even this button user should not click again
                        playAudioChronometer.setEnabled(true);//when audio is save then user will be able to play
                    }else
                        Toast.makeText(context, view17.getContext().getResources().getString(R.string.tab_on_mic_to_start_recording), Toast.LENGTH_SHORT).show();
                });

        }else if(data.getIsdeposited()){//this will execute only user want to update only deposit
                description.setVisibility(View.GONE);
                save.setVisibility(View.GONE);
                LinearLayout l1=myView.findViewById(R.id.hardcode_layout);
                LinearLayout l2=myView.findViewById(R.id.input_layout);
                LinearLayout l3=myView.findViewById(R.id.suggestion_layout);
                LinearLayout l4=myView.findViewById(R.id.this_week_work_amount_layout);
                LinearLayout l5=myView.findViewById(R.id.date_mic_chronometer_layout);
                l1.setVisibility(View.GONE);
                l2.setVisibility(View.GONE);
                l3.setVisibility(View.GONE);
                l4.setVisibility(View.GONE);
                l5.setVisibility(View.GONE);

                 deposit_btn_tv.setVisibility(View.VISIBLE);
                 deposit_btn_tv.setText(view.getContext().getString(R.string.to_update));
                //not opening directly because user sometime click on wages instead of deposit so to let user think he has click on right filed then only he can update
                deposit_btn_tv.setOnClickListener(view14 -> {//sending date,time and id to update deposit
                    Intent intent=new Intent(context,CustomizeLayoutOrDepositAmount.class);
                    intent.putExtra("ID",data.getId());
                    intent.putExtra("SYSTEM_DATETIME",data.getSystemDateAndTime());
//                    intent.putExtra("DATE",data.getUserGivenDate());
//                    intent.putExtra("TIME",data.getTime());
                    dialog.dismiss();//while going to other activity dismiss dialog otherwise window leak
                    ((Activity)context).finish();//while going to other activity so destroy  this current activity so that while coming back we will see refresh activity
                   context.startActivity(intent);
                });
                dialog.show();
            }
            return false;
        });
    }

    private String getUpdatedRemarks(byte indicator, String onlyTime, View view12,String description) {
        switch (indicator){
            case 1: return  "[" + onlyTime +view12.getContext().getString(R.string.hyphen_edited)+"\n\n"+description+"\n\n"+view12.getContext().getString(R.string.previous_details_were_colon)+"\n" + previousDataHold[5] +"\n" + previousDataHold[0] +"\n" + previousDataHold[4] + "\n" + previousDataHold[6];//onlyTime is set automatically to remarks if user enter any remarks;
            case 2: return  "[" + onlyTime +view12.getContext().getString(R.string.hyphen_edited)+"\n\n"+description+"\n\n"+view12.getContext().getString(R.string.previous_details_were_colon)+"\n" + previousDataHold[5] +"\n" + previousDataHold[0] + " " + previousDataHold[1] + "\n" + previousDataHold[4] + "\n" + previousDataHold[6];//onlyTime is set automatically to remarks if user enter any remarks;
            case 3: return  "[" + onlyTime +view12.getContext().getString(R.string.hyphen_edited)+"\n\n"+description+"\n\n"+view12.getContext().getString(R.string.previous_details_were_colon)+"\n" + previousDataHold[5] +"\n" + previousDataHold[0] + " " + previousDataHold[1] + " " + previousDataHold[2] +"\n" + previousDataHold[4] + "\n" + previousDataHold[6];//onlyTime is set automatically to remarks if user enter any remarks;
            case 4: return  "[" + onlyTime +view12.getContext().getString(R.string.hyphen_edited)+"\n\n"+description+"\n\n"+view12.getContext().getString(R.string.previous_details_were_colon)+"\n" + previousDataHold[5] +"\n" + previousDataHold[0] + " " + previousDataHold[1] + " " + previousDataHold[2] + " " + previousDataHold[3] + "\n" + previousDataHold[4] + "\n" + previousDataHold[6];//onlyTime is set automatically to remarks if user enter any remarks;
        }
        return "error";
    }

    public void refreshCurrentActivity(String id) {
        Intent intent1=new Intent(context,IndividualPersonDetailActivity.class);
        intent1.putExtra("ID",id);
        ((Activity)context).finish();//destroying this current activity typecast to activity
        context.startActivity(intent1);
    }
    public void p1_p2_p3_p4_Change_Tracker(Cursor result, EditText inputP1, EditText inputP2, EditText inputP3, EditText inputP4, TextView runtimeSuggestionAmountToGive) {
        String p1,p2,p3,p4;
        p1 = inputP1.getText().toString().trim();
        //all 15 combination
        //only p1
        if (arr[0] == 1 && arr[1] != 1 && arr[2] != 1 && arr[3] != 1) {
            runtimeSuggestionAmountToGive.setText(String.valueOf (result.getInt(0) * Integer.parseInt(p1)));
        }
        //only p1 p2
        else if (arr[0] == 1 && arr[1] == 1 && arr[2] != 1 && arr[3] != 1) {
            p2 = inputP2.getText().toString().trim();
            runtimeSuggestionAmountToGive.setText(String.valueOf(((result.getInt(0) * Integer.parseInt(p1)) + (result.getInt(1) * Integer.parseInt(p2)))));
        }
        //only p1 p2,p3
        else if (arr[0] == 1 && arr[1] == 1 && arr[2] == 1 && arr[3] != 1) {
            p2 = inputP2.getText().toString().trim();
            p3 = inputP3.getText().toString().trim();
            runtimeSuggestionAmountToGive.setText(String.valueOf (((result.getInt(0) * Integer.parseInt(p1)) + (result.getInt(1) * Integer.parseInt(p2)) + (result.getInt(2) * Integer.parseInt(p3)))));
        }
        //only p1 p2,p3,p4
        else if (arr[0] == 1 && arr[1] == 1 && arr[2] == 1 && arr[3] == 1) {
            p2 = inputP2.getText().toString().trim();
            p3 = inputP3.getText().toString().trim();
            p4 = inputP4.getText().toString().trim();
            runtimeSuggestionAmountToGive.setText(String.valueOf (((result.getInt(0) * Integer.parseInt(p1)) + (result.getInt(1) * Integer.parseInt(p2)) + (result.getInt(2) * Integer.parseInt(p3)) + (result.getInt(3) * Integer.parseInt(p4)))));
        }
        //only p1 p3 p4
        else if (arr[0] == 1 && arr[1] != 1 && arr[2] == 1 && arr[3] == 1) {
            p3 = inputP3.getText().toString().trim();
            p4 = inputP4.getText().toString().trim();
            runtimeSuggestionAmountToGive.setText(String.valueOf(((result.getInt(0) * Integer.parseInt(p1)) + (result.getInt(2) * Integer.parseInt(p3)) + (result.getInt(3) * Integer.parseInt(p4)))));
        }
        //only p1 p2 p4
        else if (arr[0] == 1 && arr[1] == 1 && arr[2] != 1 && arr[3] == 1) {
            p2 = inputP2.getText().toString().trim();
            p4 = inputP4.getText().toString().trim();
            runtimeSuggestionAmountToGive.setText(String.valueOf(((result.getInt(0) * Integer.parseInt(p1)) + (result.getInt(1) * Integer.parseInt(p2)) + (result.getInt(3) * Integer.parseInt(p4)))));
        }
        //only p2 p3 p4
        else if (arr[0] != 1 && arr[1] == 1 && arr[2] == 1 && arr[3] == 1) {
            p2 = inputP2.getText().toString().trim();
            p3 = inputP3.getText().toString().trim();
            p4 = inputP4.getText().toString().trim();
            runtimeSuggestionAmountToGive.setText(String.valueOf (((result.getInt(1) * Integer.parseInt(p2)) + (result.getInt(2) * Integer.parseInt(p3)) + (result.getInt(3) * Integer.parseInt(p4)))));
        }
        //only p1 P4
        else if (arr[0] == 1 && arr[1] != 1 && arr[2] != 1 && arr[3] == 1) {
            p4 = inputP4.getText().toString().trim();
            runtimeSuggestionAmountToGive.setText(String.valueOf (((result.getInt(0) * Integer.parseInt(p1)) + (result.getInt(3) * Integer.parseInt(p4)))));
        }
        //only p1 P3
        else if (arr[0] == 1 && arr[1] != 1 && arr[2] == 1 && arr[3] != 1) {
            p3 = inputP3.getText().toString().trim();
            runtimeSuggestionAmountToGive.setText(String.valueOf (((result.getInt(0) * Integer.parseInt(p1)) + (result.getInt(2) * Integer.parseInt(p3)))));
        }
        //Only p3,p4
        else if (arr[0] != 1 && arr[1] != 1 && arr[2] == 1 && arr[3] == 1) {
            p3 = inputP3.getText().toString().trim();
            p4 = inputP4.getText().toString().trim();
            runtimeSuggestionAmountToGive.setText(String.valueOf (((result.getInt(2) * Integer.parseInt(p3)) + (result.getInt(3) * Integer.parseInt(p4)))));
        }
        //Only p2,p4
        else if (arr[0] != 1 && arr[1] == 1 && arr[2] != 1 && arr[3] == 1) {
            p2 = inputP2.getText().toString().trim();
            p4 = inputP4.getText().toString().trim();
            runtimeSuggestionAmountToGive.setText(String.valueOf (((result.getInt(1) * Integer.parseInt(p2)) + (result.getInt(3) * Integer.parseInt(p4)))));
        }
        //Only p2,p3
        else if (arr[0] != 1 && arr[1] == 1 && arr[2] == 1 && arr[3] != 1) {
            p2 = inputP2.getText().toString().trim();
            p3 = inputP3.getText().toString().trim();
            runtimeSuggestionAmountToGive.setText(String.valueOf (((result.getInt(1) * Integer.parseInt(p2)) + (result.getInt(2) * Integer.parseInt(p3)))));
        }
        //only p2
        else if (arr[0] != 1 && arr[1] == 1 && arr[2] != 1 && arr[3] != 1) {
            p2 = inputP2.getText().toString().trim();
            runtimeSuggestionAmountToGive.setText(String.valueOf (((result.getInt(1) * Integer.parseInt(p2)))));
        }
        //only p3
        else if (arr[0] != 1 && arr[1] != 1 && arr[2] == 1 && arr[3] != 1) {
            p3 = inputP3.getText().toString().trim();
            runtimeSuggestionAmountToGive.setText(String.valueOf (((result.getInt(2) * Integer.parseInt(p3)))));
        }
        //only p4
        else if (arr[0] != 1 && arr[1] != 1 && arr[2] != 1 && arr[3] == 1) {
            p4 = inputP4.getText().toString().trim();
            runtimeSuggestionAmountToGive.setText(String.valueOf(((result.getInt(3) * Integer.parseInt(p4)))));
        }
        //if any wrong data then this will execute
        if(arr[0] == 2 || arr[1] == 2 || arr[2] == 2 || arr[3] == 2) {
            runtimeSuggestionAmountToGive.setText("0");
            // Toast.makeText(this, "ENTER 0 DON'T LEFT M L G EMPTY", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public int getItemCount() {
        return arrayList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView date,wages,p1,p2,p3,p4;
       public Spinner spinnerRemarksAudioIcon;
       // LinearLayout singleRecordLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date=itemView.findViewById(R.id.date_in_recycler_tv);
            wages=itemView.findViewById(R.id.wages_in_recycler_tv);
            p1=itemView.findViewById(R.id.p1_in_recycler_tv);
            p2=itemView.findViewById(R.id.p2_in_recycler_tv);
            p3=itemView.findViewById(R.id.p3_in_recycler_tv);
            p4=itemView.findViewById(R.id.p4_in_recycler_tv);
            spinnerRemarksAudioIcon =itemView.findViewById(R.id.spinner_in_recycler_tv);
            //singleRecordLayout=itemView.findViewById(R.id.single_record_layout);
        }
    }
    public String getPreviousDateFromCurrentDate(byte days){//if error return null
    try {//if 0 is passed then current date is return
        LocalDate currentDate = LocalDate.now();
        LocalDate  resultDate = currentDate.minusDays(days);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return resultDate.format(formatter);
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
  }
}
