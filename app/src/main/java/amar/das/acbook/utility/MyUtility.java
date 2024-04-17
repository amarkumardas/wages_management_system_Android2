package amar.das.acbook.utility;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import amar.das.acbook.Database;
import amar.das.acbook.R;

import amar.das.acbook.globalenum.GlobalConstants;
import amar.das.acbook.model.MestreLaberGModel;
import amar.das.acbook.model.WagesDetailsModel;
import amar.das.acbook.textfilegenerator.TextFile;
import amar.das.acbook.voicerecording.VoiceRecorder;

public class MyUtility {
    public static String systemCurrentDate24hrTime(){//example output 2023-10-23 10:08:08
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
    public static String backupDateTime(){
       return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd_MM_yyyy_'at'_h_mm_ss_a_"));
    }
    public static String getTime12hr(String systemDateTime) {//if error return null
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = formatter.parse(systemDateTime);
            SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm:ss a");
            return timeFormatter.format(date);
        }catch (Exception x){
            x.printStackTrace();
            return null;
        }
    }
    public static String getDate(String systemDateTime) {//if error return null
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = formatter.parse(systemDateTime);
            SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyy-MM-dd");
            return timeFormatter.format(date);
        }catch (Exception x){
            x.printStackTrace();
            return null;
        }
    }
    public static int get24hrCurrentTimeRemoveColon() {//unique time
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");//capital HH stands for 24hr time format
       return  Integer.parseInt(sdf.format(new Date()).replaceAll("[:]", ""));//convert 01:30:55 to 13055 by parsing to INTEGER initial 0 is removed
    }
    public static String get12hrCurrentTimeAndDate(){
//        final Calendar current = Calendar.getInstance();//to get current date and time
//        Date d = Calendar.getInstance().getTime();//To get time
//        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:a");//a stands for am or pm here mm is lowercase to represent minute
//        return current.get(Calendar.DAY_OF_MONTH) + "-" + (current.get(Calendar.MONTH) + 1) + "-" + current.get(Calendar.YEAR) + "at" + sdf.format(d);

        SimpleDateFormat formatter = new SimpleDateFormat("dd - MM - yyyy (hh:mm a)");//MM is capital to represent month
        return formatter.format(new Date());
    }
    public static String getOnlyTime(){
        try{
//            Date d=Calendar.getInstance().getTime();
//            SimpleDateFormat sdf=new SimpleDateFormat("hh:mm:ss a");//a stands for AM or PM
//            return sdf.format(d);
            return new SimpleDateFormat("hh:mm:ss a").format(Calendar.getInstance().getTime());
        }catch (Exception  x){
            x.printStackTrace();
            return "00:00:00: pm";//error
        }
    }
    public static String getOnlyCurrentDate(){//latest date not contain 0
        try{
            Calendar currentDate =Calendar.getInstance();//to get current date like 1-2-2021 or 11-22-2023 no zero
            return currentDate.get(Calendar.DAY_OF_MONTH)+"-"+(currentDate.get(Calendar.MONTH)+1)+"-"+ currentDate.get(Calendar.YEAR);
        }catch (Exception x){
            x.printStackTrace();
            return "0-0-0000";//error
        }
    }
    public static boolean isp1p2p3p4PresentAndRateNotPresent(int r1,int r2,int r3,int r4,int p1,int p2,int p3,int p4,byte indicator){
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
    public static boolean updateLeavingDate(String id,Context context,LocalDate todayDate){
      //  try(Database db=new Database (context);
          try(Database db=Database.getInstance(context);
            Cursor cursor2 = db.getData("SELECT " + Database.COL_392_LEAVINGDATE + " FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE " + Database.COL_31_ID + "='" + id + "'")){
            if (cursor2.getCount() == 0) { return false; }// or throw an exception this will occur when there is no id in db or no data in db or table

            cursor2.moveToFirst();

            if (cursor2.getString(0) != null){
                String [] dateArray = cursor2.getString(0).split("-");
                LocalDate dbDate = LocalDate.of(Integer.parseInt(dateArray[2]), Integer.parseInt(dateArray[1]), Integer.parseInt(dateArray[0]));//it convert 2022-05-01it add 0 automatically
                //between (2022-05-01,2022-05-01) like
                if(ChronoUnit.DAYS.between(dbDate, todayDate) >= 0){//if days between leaving date and today date is 0 then leaving date will set null automatically
                   return db.updateTable("UPDATE " + Database.TABLE_NAME_RATE_SKILL + " SET " + Database.COL_392_LEAVINGDATE + "=" + null + " WHERE " + Database.COL_31_ID + "='" + id + "'");
                }
            }
            return true;
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }
    }
    public static String generateUniqueFileNameByTakingDateTime(String id,String fileName) {//file name will always be unique
        try {
            final Calendar current = Calendar.getInstance();//to get current date and time
            Date d = Calendar.getInstance().getTime();//To get time
            SimpleDateFormat sdf = new SimpleDateFormat("hhmmssa");//a stands for is AM or PM.example which make file unique 091659am which is unique
            return "id" + id + "date" + current.get(Calendar.DAY_OF_MONTH) + "_" + (current.get(Calendar.MONTH) + 1) + "_" + current.get(Calendar.YEAR)+fileName+ "At" + sdf.format(d);
        }catch (Exception x){
            x.printStackTrace();
            return "error";
        }
    }
    public static boolean isEnterDataIsWrong(int[] arr) {
        int wrongData=0;
        for(int i=0 ;i <arr.length;i++) {
            if (arr[i] == 2) {//value 2 represents wrong data
                wrongData++;
            }
        }
        return wrongData >= 1;
    }
    public static boolean isDataPresent(int[] arr){
        boolean bool=true;
        int sum=0,one=0;

        for(int i=0 ;i <arr.length;i++){

            if(arr[i]== 1){//value 1 represents data is present
                one++;
            }
            sum=sum+arr[i];
        }
        if(sum == 0)//data is not present
            bool= false;
        else if(one >= 1)//data is present
            bool= true;
        return bool;
    }
    public static boolean deletePdfOrRecordingUsingPathFromDevice(String pdfPath){
        if(pdfPath != null){
            try {
                File filePath = new File(pdfPath);//file to be delete
                if (filePath.exists()) {//checks file is present in device  or not
                    return filePath.delete();//only this can return false when not deleted else return true
                }
            }catch (Exception ex) {
                ex.printStackTrace();
                 return false;
            }
        }
        return true;//if user deleted file from device ie. file not exist in device so return true
    }
    public static String[] getReligionFromDb(Context context) {
        String [] religion;
        try(//Database db=new Database(context);
            Database db=Database.getInstance(context);
            Cursor religionCursor=db.getData("SELECT "+Database.COL_51_RELIGION+" FROM "+Database.TABLE_NAME_RELIGION)){
            religion=new String[religionCursor.getCount()];
            int i=0;
            while(religionCursor.moveToNext()){
                religion[i++]=religionCursor.getString(0);
            }
            return religion;
        }catch (Exception x){
            x.printStackTrace();
            return  new String[]{"error"};
        }
    }
    public static String[] getLocationFromDb(Context context) {
        String [] location=null;
        try(//Database db=new Database(context);
            Database db=  Database.getInstance(context);
            Cursor locationCursor=db.getData("SELECT "+Database.COL_41_LOCATION+" FROM "+Database.TABLE_NAME_LOCATION)){
            location=new String[locationCursor.getCount()];
            int i=0;
            while(locationCursor.moveToNext()){
                location[i++]=locationCursor.getString(0);
            }
            return location;
        }catch (Exception x){
            x.printStackTrace();
            return  new String[]{"error"};
        }
    }
    public static boolean updateLocationReligionToTableIf(HashSet<String> locationHashSet, String location, HashSet<String> religionHashSet, String religion, Context context) {
        /*The isEmpty() method checks whether a String is empty, meaning it has a length of 0, and returns true if it is empty. It does not consider whitespace characters as part of the string content.On the other hand, the isBlank() method was introduced in Java 11. It checks whether a String is empty or contains only whitespace characters, and returns true in such cases*/

        try(Database db=new Database(context))
        {
            if ( !TextUtils.isEmpty(location) && locationHashSet.add(location)) {//if false that means data is duplicate
                if(!db.updateTable("INSERT INTO "+Database.TABLE_NAME_LOCATION +" ( "+Database.COL_41_LOCATION+" ) VALUES ( '"+location+"' );")){
                   return false;//means error
                }
            }
            if (!TextUtils.isEmpty(religion) && religionHashSet.add(religion)) {//if false that means data is duplicate
                if(!db.updateTable("INSERT INTO "+Database.TABLE_NAME_RELIGION +" ( "+Database.COL_51_RELIGION+" ) VALUES ( '"+religion+"' );")){
                    return false;//means error
                }
            }
            return true;
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }
    }
    public static int getPdfSequence(String id,Context context){//if error return -1
        if(id==null){
            return -1;
        }
        Database database=Database.getInstance(context);
        try(Cursor cursor = database.getData("SELECT " + Database.COL_396_PDFSEQUENCE + " FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE "+Database.COL_31_ID+"= '" + id + "'")){
              cursor.moveToFirst();
            return cursor.getInt(0);

        }catch(Exception x){
            x.printStackTrace();
            return -1;
        }
    }
    public static boolean createTextFileInvoice(String id,Context context,String externalFileDir){
        try{
            TextFile textFile=new TextFile();
            StringBuilder sb=new StringBuilder();

            String[] personDetail=fetchPersonDetailOfPDF(id,context);//person detail
            sb.append("NAME: ").append(personDetail[0]).append("\n").
               append("ID: ").append(personDetail[1]).append(" A/C: ").append(personDetail[2]).append(" AADHAAR: ").append(personDetail[3]).append(" PHONE: ").append(personDetail[4]).append("\n").
               append("CREATED ON: ").append(personDetail[5]).append("\nINVOICE No. ").append(personDetail[6]).append("\n");

            sb.append("------------SUMMARY-------------\n");
            //summary
            byte indicator=get_indicator(context,id);
            if(!(indicator >= 1 && indicator <=4)) return false;//if indicator is not in range 1 to 4 then return false ie.error
            boolean[] errorDetection={false};//when ever exception occur it will be updated to true in method so it indicate error occurred or not
            int[] arrayOfTotalWagesDepositRate = getSumOfTotalWagesDepositRateDaysWorkedBasedOnIndicator(context,id,indicator,errorDetection);//if error cause errorDetection will be set true
            String[] skillHeader = getWagesHeadersFromDbBasedOnIndicator(context,id, indicator, errorDetection);//THIS SHOULD BE TOP at arrayOfTotalWagesDepositRateAccordingToIndicator   TO AVOID INDEX EXCEPTION
            if(errorDetection[0]==false) {
                String[][] summary = makeSummaryAndWriteToPDFBasedOnIndicator(context,indicator,id,arrayOfTotalWagesDepositRate);
                for (int j = 0; j < summary[0].length; j++) {//iterate only two row
                        sb.append(summary[0][j]).append(": ").append(summary[1][j]).append("\n");
                    }
                 sb.append(summary[2][0]).append("\n");//last row ie.message

                //deposit and wages
                sb.append(getAllDepositAndWagesDetailsAsText(context,id));

                //work amount
                sb.append("----------WORK AMOUNT-----------\n");
               String[][] workAmount= addWorkAmountAndDepositBasedOnIndicator(indicator,arrayOfTotalWagesDepositRate,skillHeader);
               for(int i = 0; i < workAmount.length; i++) {
                    for (int j = 0; j < workAmount[i].length; j++) {
                       sb.append(workAmount[i][j]).append(" ");
                    }
                    sb.append("\n");
                }
                sb.append("\n------------FINISH--------------");
            }else return false;

            textFile.appendText(sb.toString());
          return textFile.createTextFile(externalFileDir,GlobalConstants.TEXT_FILE_FOLDER_NAME.getValue(),generateUniqueFileName(context,id));
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }
    }
    public static String generateUniqueFileName(Context context,String id) {
        Database db=Database.getInstance(context);
            try{
            StringBuilder fileName = new StringBuilder();
            fileName.append("id").append(id);
            Cursor cursor = db.getData("SELECT "+Database.COL_396_PDFSEQUENCE+" FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE "+Database.COL_31_ID+"= '" + id + "'");
            cursor.moveToFirst();//means only one row is returned
            fileName.append("invoice").append(cursor.getInt(0) + 1); /*pdf sequence in db is updated when pdf is generated successfully so for now increasing manually so that if pdf generation is failed sequence should not be updated in db*/

            cursor =db.getData("SELECT "+Database.COL_3_BANKAC+" , "+Database.COL_6_AADHAAR_NUMBER+" FROM " + Database.TABLE_NAME1 + " WHERE "+Database.COL_1_ID+"= '" + id + "'");
            cursor.moveToFirst();
            if(cursor.getString(0)!=null && cursor.getString(0).length()>4) {
                fileName.append("ac").append(cursor.getString(0).substring(cursor.getString(0).length() - 4));//account
            }else{
                fileName.append("acnull");
            }

            if(cursor.getString(1)!=null && cursor.getString(1).length()>5){
                fileName.append("ad").append(cursor.getString(1).substring(cursor.getString(1).length() - 5));//aadhaar
            }
            else{
                fileName.append("adnull");
            }
            cursor.close();

            String activePhoneNumber=MyUtility.getActiveOrBothPhoneNumber(id,context,true);
            if(activePhoneNumber != null){
                fileName.append("phone").append(activePhoneNumber.substring(activePhoneNumber.length() - 6));//phone number
            }else{
                fileName.append("phonenull");
            }

            return fileName.toString();
        }catch (Exception ex){
            ex.printStackTrace();
             return "errorOccurredInvoiceNameNull";
        }
    }
    public static String[][] addWorkAmountAndDepositBasedOnIndicator(byte indicator, int[] sumArrayAccordingToIndicator, String[] skillAccordingToIndicator) {
        if(!(indicator >= 1 && indicator <=4))return new String[][]{{"error"},{"error"},{"error"},{"error"}};//if indicator not in range 1 to 4 return error

        try{
            ArrayList<String> al = new ArrayList<>();
            switch(indicator){
                case 1: {
                      al.add(Arrays.toString(new String[]{skillAccordingToIndicator[2] + " =", sumArrayAccordingToIndicator[1] + "", "X","RATE ="+sumArrayAccordingToIndicator[3]+" = ", MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[3])}));

                     if (sumArrayAccordingToIndicator[2] == 0) {//DEPOSIT AMOUNT checking there or not or can be use (indicator+1) to get index of deposit
                        al.add(Arrays.toString(new String[]{"TOTAL WORK AMOUNT =",  MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[3])}));

                     }else {//when there is deposit then add deposit
                        al.add(Arrays.toString(new String[]{"TOTAL DEPOSIT =", MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[2])}));

                         //                                                                                                                                                                                                                                                                           green color
                        al.add(Arrays.toString(new String[]{"TOTAL DEPOSIT+WORK AMOUNT=",  MyUtility.convertToIndianNumberSystem((sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[3]) + sumArrayAccordingToIndicator[2])}));

                     }
                }break;
                case 2: {
                    al.add(Arrays.toString(new String[]{skillAccordingToIndicator[2] + " =", sumArrayAccordingToIndicator[1] + "", "X","RATE ="+sumArrayAccordingToIndicator[4]+" = ",MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[4])}));

                    al.add(Arrays.toString(new String[]{skillAccordingToIndicator[3] + " =", sumArrayAccordingToIndicator[2] + "", "X","RATE ="+sumArrayAccordingToIndicator[5]+" = ",MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[2] * sumArrayAccordingToIndicator[5])}));
                     if (sumArrayAccordingToIndicator[3] == 0) {//DEPOSIT AMOUNT checking there or not
                        al.add(Arrays.toString(new String[]{"TOTAL WORK AMOUNT =", MyUtility.convertToIndianNumberSystem((sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[4]) + (sumArrayAccordingToIndicator[2] * sumArrayAccordingToIndicator[5]))}));
                        //                                                                                                                                      P1*R1                              +                             P2*R2
                     }else{//when there is deposit then add deposit
                        al.add(Arrays.toString(new String[]{"TOTAL DEPOSIT =", MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[3])}));

                        al.add(Arrays.toString(new String[]{"TOTAL DEPOSIT+WORK AMOUNT=",MyUtility.convertToIndianNumberSystem((sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[4]) + (sumArrayAccordingToIndicator[2] * sumArrayAccordingToIndicator[5]) + sumArrayAccordingToIndicator[3])}));
                        //                       P1*R1                             +                             P2*R2                             +  DEPOSIT
                     }
                }break;
                case 3:{
                    al.add(Arrays.toString(new String[]{skillAccordingToIndicator[2] + " =", sumArrayAccordingToIndicator[1] + "", "X","RATE ="+sumArrayAccordingToIndicator[5]+" = ", MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[5])}));

                    al.add(Arrays.toString(new String[]{skillAccordingToIndicator[3] + " =", sumArrayAccordingToIndicator[2] + "", "X","RATE ="+sumArrayAccordingToIndicator[6]+" = ", MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[2] * sumArrayAccordingToIndicator[6])}));

                    al.add(Arrays.toString(new String[]{skillAccordingToIndicator[4] + " =", sumArrayAccordingToIndicator[3] + "", "X","RATE ="+sumArrayAccordingToIndicator[7]+" = ", MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[3] * sumArrayAccordingToIndicator[7])}));

                    if (sumArrayAccordingToIndicator[4] == 0) {//DEPOSIT AMOUNT checking there or not
                        al.add(Arrays.toString(new String[]{"TOTAL WORK AMOUNT =",MyUtility.convertToIndianNumberSystem((sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[5]) + (sumArrayAccordingToIndicator[2] * sumArrayAccordingToIndicator[6]) + (sumArrayAccordingToIndicator[3] * sumArrayAccordingToIndicator[7]))}));
                     }else{ //when there is deposit then add deposit
                        al.add(Arrays.toString(new String[]{"TOTAL DEPOSIT =", MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[4])}));

                        al.add(Arrays.toString(new String[]{"TOTAL DEPOSIT+WORK AMOUNT=",MyUtility.convertToIndianNumberSystem((sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[5]) + (sumArrayAccordingToIndicator[2] * sumArrayAccordingToIndicator[6] + (sumArrayAccordingToIndicator[3] * sumArrayAccordingToIndicator[7])) + sumArrayAccordingToIndicator[4])}));
                        //                                                                                P1*R1                             +                                P2*R2                                                           P3*R3                                              +  DEPOSIT
                     }
                }break;
                case 4:{
                    al.add(Arrays.toString(new String[]{skillAccordingToIndicator[2] + " =", sumArrayAccordingToIndicator[1] + "", "X", "RATE ="+sumArrayAccordingToIndicator[6]+" = ",MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[6])}));

                    al.add(Arrays.toString(new String[]{skillAccordingToIndicator[3] + " =", sumArrayAccordingToIndicator[2] + "", "X", "RATE ="+sumArrayAccordingToIndicator[7]+" = ",MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[2] * sumArrayAccordingToIndicator[7])}));

                    al.add(Arrays.toString(new String[]{skillAccordingToIndicator[4] + " =", sumArrayAccordingToIndicator[3] + "", "X", "RATE ="+sumArrayAccordingToIndicator[8]+" = ",MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[3] * sumArrayAccordingToIndicator[8])}));

                    al.add(Arrays.toString(new String[]{skillAccordingToIndicator[5] + " =", sumArrayAccordingToIndicator[4] + "", "X", "RATE ="+sumArrayAccordingToIndicator[9]+" = ",MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[4] * sumArrayAccordingToIndicator[9])}));

                    if(sumArrayAccordingToIndicator[5] == 0) {//DEPOSIT AMOUNT checking there or not
                        al.add(Arrays.toString(new String[]{"TOTAL WORK AMOUNT =",MyUtility.convertToIndianNumberSystem((sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[6]) + (sumArrayAccordingToIndicator[2] * sumArrayAccordingToIndicator[7]) + (sumArrayAccordingToIndicator[3] * sumArrayAccordingToIndicator[8]) + (sumArrayAccordingToIndicator[4] * sumArrayAccordingToIndicator[9]))}));
                     }else{//when there is deposit then add deposit
                        al.add(Arrays.toString(new String[]{"TOTAL DEPOSIT =",MyUtility.convertToIndianNumberSystem(sumArrayAccordingToIndicator[5])}));

                        al.add(Arrays.toString(new String[]{"TOTAL DEPOSIT+WORK AMOUNT=",MyUtility.convertToIndianNumberSystem((sumArrayAccordingToIndicator[1] * sumArrayAccordingToIndicator[6]) + (sumArrayAccordingToIndicator[2] * sumArrayAccordingToIndicator[7] + (sumArrayAccordingToIndicator[3] * sumArrayAccordingToIndicator[8]) + (sumArrayAccordingToIndicator[4] * sumArrayAccordingToIndicator[9])) + sumArrayAccordingToIndicator[5])}));
                        //                                                                                P1*R1                             +                                P2*R2                                                           P3*R3                                                                           P4*R4                                  +  DEPOSIT
                     }
                }break;
            }
            int size=al.size();
            String[][] str=new String[size][];
            for (int i = 0; i < al.size(); i++) {
                str[i]=al.get(i).replace("[","").replace("]","").split(", ");
            }
            return str;
        }catch(Exception ex){
            ex.printStackTrace();
            return new String[][]{{"error"},{"error"},{"error"},{"error"}};
        }
    }
    public static String getAllDepositAndWagesDetailsAsText(Context context,String id){
        StringBuilder sb=new StringBuilder();
        try{
            byte indicator=get_indicator(context,id);
            if(!(indicator >= 1 && indicator <=4)) return "error";//if indicator is not in range 1 to 4 then return error

            boolean[] errorDetection={false};//when ever exception occur it will be updated to true in method so it indicate error occurred or not
             String[][] recyclerViewWagesData = getAllWagesDetailsFromDbBasedOnIndicator(context,id, indicator, errorDetection);//it amy return null   when no data
             String[][] recyclerViewDepositData = getAllDepositFromDb(context,id, errorDetection);//it amy return null   when no data

            if(errorDetection[0]==false){

                if (recyclerViewDepositData != null) {//null means data not present so don't add deposit in text
                    int rowLength=recyclerViewDepositData.length;
                    int columnLength=recyclerViewDepositData[0].length;

                    sb.append("\n-------------------------------");
                    sb.append("\n*TOTAL N0. OF DEPOSIT ENTRIES: ").append(rowLength).append("\n\n");

                    for (int row = 0; row < rowLength; row++) {
                        sb.append(row + 1).append("-> ");
                        for (int col = 0; col < columnLength; col++) {

                            if((columnLength-1)!=col)
                                sb.append(recyclerViewDepositData[row][col]).append("  ");
                            else sb.append("\n").append(recyclerViewDepositData[row][col]);//GIVING SPACE TO EACH COLUMN AND AT LAST COLUMN GIVING NEXT-LINE TO SHOW REMARKS
                        }
                        sb.append("\n\n");
                    }
                }
                if (recyclerViewWagesData != null) {//null means data not present
                    int rowLength=recyclerViewWagesData.length;
                    int columnLength=recyclerViewWagesData[0].length;

                    if(recyclerViewDepositData == null){
                        sb.append("\n-------------------------------");//add space when deposit is null for better readability.because space is not added when recyclerViewDepositData is null
                    }else{
                        sb.append("-------------------------------");
                    }
                    sb.append("\n*TOTAL N0. OF WAGES ENTRIES: ").append(rowLength).append("\n\n");

                    for(int row = 0; row < rowLength; row++){
                        sb.append(row + 1).append("-> ");
                        for (int col = 0; col < columnLength; col++){

                            if((columnLength-1)!=col) sb.append((recyclerViewWagesData[row][col]!=null?recyclerViewWagesData[row][col]:0)).append("  "); else sb.append("\n").append(recyclerViewWagesData[row][col]);

                        }
                        sb.append("\n\n");
                    }
                }
            }else{
                sb.append("error");
            }
            return sb.toString();
        }catch (Exception x){
            x.printStackTrace();
            return "error";
        }
    }
    public static String getMessageOnlyTotalWagesAndDeposit(String id,Context context){
        if(id==null){
            return "id null";
        }
        try{
            StringBuilder sb=new StringBuilder();
            byte indicator=MyUtility.get_indicator(context,id);
            boolean[] errorDetection={false};//when ever exception occur it will be updated to true in method so it indicate error occurred or not
            String[] header = MyUtility.getWagesHeadersFromDbBasedOnIndicator(context,id, indicator, errorDetection);//THIS SHOULD BE TOP at arrayOfTotalWagesDepositRateBasedOnIndicator   TO AVOID INDEX EXCEPTION
            String[][] recyclerViewDepositData =MyUtility.getAllDepositFromDb(context,id, errorDetection);//it amy return null   when no data
            int[] arrayOfTotalWagesDepositRateBasedOnIndicator= MyUtility.getSumOfTotalWagesDepositRateDaysWorkedBasedOnIndicator(context,id,indicator,errorDetection);//if error cause errorDetection will be set true

            if(errorDetection[0]==false){
                 sb.append(getTotalWagesDepositAndWorkingAccordingToIndicator(indicator, header, arrayOfTotalWagesDepositRateBasedOnIndicator, recyclerViewDepositData != null));
            }else{
                sb.append("error");
            }
            return sb.toString();
        }catch (Exception x){
            x.printStackTrace();
            return "error";
        }
    }
    public static String getTotalWagesDepositAndWorkingAccordingToIndicator(byte indicator, String[] headerBasedOnIndicator, int[] arrayOfTotalWagesDepositRateAccordingToIndicator, boolean isDepositPresent) {
        try{
            /*
             * return message like:
             * TOTAL WAGES: 23,000
             * TOTAL M: 59
             * TOTAL DEPOSIT: 55,000*/
            StringBuilder sb=new StringBuilder();
            switch (indicator) {//based on indicator generate message
                case 1:{sb.append("\nTOTAL ").append(headerBasedOnIndicator[1]).append(": ").append(MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[0])).append("\nTOTAL ").append(headerBasedOnIndicator[2]).append(": ").append(arrayOfTotalWagesDepositRateAccordingToIndicator[1]);}break;
                case 2:{sb.append("\nTOTAL ").append(headerBasedOnIndicator[1]).append(": ").append(MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[0])).append("\nTOTAL ").append(headerBasedOnIndicator[2]).append(": ").append(arrayOfTotalWagesDepositRateAccordingToIndicator[1]).append("\nTOTAL ").append(headerBasedOnIndicator[3]).append(": ").append(arrayOfTotalWagesDepositRateAccordingToIndicator[2]);}break;
                case 3:{sb.append("\nTOTAL ").append(headerBasedOnIndicator[1]).append(": ").append(MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[0])).append("\nTOTAL ").append(headerBasedOnIndicator[2]).append(": ").append(arrayOfTotalWagesDepositRateAccordingToIndicator[1]).append("\nTOTAL ").append(headerBasedOnIndicator[3]).append(": ").append(arrayOfTotalWagesDepositRateAccordingToIndicator[2]).append("\nTOTAL ").append(headerBasedOnIndicator[4]).append(": ").append(arrayOfTotalWagesDepositRateAccordingToIndicator[3]);}break;
                case 4:{sb.append("\nTOTAL ").append(headerBasedOnIndicator[1]).append(": ").append(MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[0])).append("\nTOTAL ").append(headerBasedOnIndicator[2]).append(": ").append(arrayOfTotalWagesDepositRateAccordingToIndicator[1]).append("\nTOTAL ").append(headerBasedOnIndicator[3]).append(": ").append(arrayOfTotalWagesDepositRateAccordingToIndicator[2]).append("\nTOTAL ").append(headerBasedOnIndicator[4]).append(": ").append(arrayOfTotalWagesDepositRateAccordingToIndicator[3]).append("\nTOTAL ").append(headerBasedOnIndicator[5]).append(": ").append(arrayOfTotalWagesDepositRateAccordingToIndicator[4]);}break;
            }

            if(isDepositPresent){//if deposit present then only add
                sb.append("\nTOTAL DEPOSIT: ").append(MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[indicator + 1]));//[indicator + 1] it is index of deposit
            }
            return sb.toString();

        }catch (Exception x){
            x.printStackTrace();
            return "ERROR OCCURRED";
        }
    }
    public static String[][] getAllDepositFromDb(Context context,String id, boolean[] errorDetection) {//return null when no data and if error errorDetection will be set to true
        Database db = Database.getInstance(context);
        try(
            //Cursor depositCursor=db.getData("SELECT "+Database.COL_2__DATE +" ,"+Database.COL_7__DEPOSIT +" ,"+Database.COL_5__DESCRIPTION +" FROM " + Database.TABLE_NAME2 + " WHERE "+Database.COL_1__ID +"='" + id + "'" + " AND "+Database.COL_12__ISDEPOSITED +"='1'"))
            Cursor depositCursor=db.getData("SELECT "+db.getColumnNameOutOf4Table(id, (byte) 2) +" ,"+ db.getColumnNameOutOf4Table(id, (byte) 5) +" ,"+ db.getColumnNameOutOf4Table(id, (byte) 4) +" FROM " + db.tableNameOutOf4Table(id) + " WHERE "+ db.getColumnNameOutOf4Table(id, (byte) 1) +"='" + id + "'" + " AND "+ db.getColumnNameOutOf4Table(id, (byte) 10) +"='1'")){
            String[][] recyclerViewDepositData =null;
            if(depositCursor!= null && depositCursor.getCount()!=0){
                recyclerViewDepositData= new String[depositCursor.getCount()][depositCursor.getColumnCount()];
                int row = 0;
                while (depositCursor.moveToNext()) {
                    for (int col = 0; col < depositCursor.getColumnCount(); col++) {
                        if(col!=1) {
                            recyclerViewDepositData[row][col] = depositCursor.getString(col);//storing all data in 2d string
                        }else{
                            recyclerViewDepositData[row][col] = MyUtility.convertToIndianNumberSystem(depositCursor.getLong(col));//if column is 1 then convert to indian number system
                        }
                    }
                    row++;
                }
            }
            return recyclerViewDepositData;//when no data return null
        }catch (Exception ex){
            ex.printStackTrace();
            Log.d(MyUtility.class.getSimpleName(),"exception occurred in method "+Thread.currentThread().getStackTrace()[2].getMethodName());
            errorDetection[0]=true;
            return new String[][]{{"error"}};//to avoid error
        }
    }
    public static String[][] getAllWagesDetailsFromDbBasedOnIndicator(Context context,String id, byte indicator, boolean[] errorDetection) {//return null when no data and if error errorDetection will be set to true
        Database db = Database.getInstance(context);
        try{
            Cursor wagesCursor = null;
            switch(indicator){
                case 1:wagesCursor=db.getData("SELECT "+db.getColumnNameOutOf4Table(id, (byte) 2) +" ,"+db.getColumnNameOutOf4Table(id, (byte) 5) +" ,"+db.getColumnNameOutOf4Table(id, (byte) 6) +" ,"+db.getColumnNameOutOf4Table(id, (byte) 4) +" FROM " + db.tableNameOutOf4Table(id) + " WHERE "+db.getColumnNameOutOf4Table(id, (byte) 1) +"='" + id + "'" + " AND "+db.getColumnNameOutOf4Table(id, (byte) 10) +"='0'");break;
                case 2:wagesCursor=db.getData("SELECT "+db.getColumnNameOutOf4Table(id, (byte) 2) +" ,"+db.getColumnNameOutOf4Table(id, (byte) 5) +" ,"+db.getColumnNameOutOf4Table(id, (byte) 6) +" ,"+db.getColumnNameOutOf4Table(id, (byte) 7) +" ,"+db.getColumnNameOutOf4Table(id, (byte) 4) +" FROM " + db.tableNameOutOf4Table(id) + " WHERE "+db.getColumnNameOutOf4Table(id, (byte) 1) +"='" + id + "'" + " AND "+db.getColumnNameOutOf4Table(id, (byte) 10) +"='0'");break;
                case 3:wagesCursor=db.getData("SELECT "+db.getColumnNameOutOf4Table(id, (byte) 2) +" ,"+db.getColumnNameOutOf4Table(id, (byte) 5) +" ,"+db.getColumnNameOutOf4Table(id, (byte) 6) +" ,"+db.getColumnNameOutOf4Table(id, (byte) 7) +" ,"+db.getColumnNameOutOf4Table(id, (byte) 8) +" ,"+db.getColumnNameOutOf4Table(id, (byte) 4) +" FROM " + db.tableNameOutOf4Table(id) + " WHERE "+db.getColumnNameOutOf4Table(id, (byte) 1) +"='" + id + "'" + " AND "+db.getColumnNameOutOf4Table(id, (byte) 10) +"='0'");break;
                case 4:wagesCursor=db.getData("SELECT "+db.getColumnNameOutOf4Table(id, (byte) 2) +" ,"+db.getColumnNameOutOf4Table(id, (byte) 5) +" ,"+db.getColumnNameOutOf4Table(id, (byte) 6) +" ,"+db.getColumnNameOutOf4Table(id, (byte) 7) +" ,"+db.getColumnNameOutOf4Table(id, (byte) 8) +" ,"+db.getColumnNameOutOf4Table(id, (byte) 9) +" ,"+db.getColumnNameOutOf4Table(id, (byte) 4) +" FROM " + db.tableNameOutOf4Table(id) + " WHERE "+db.getColumnNameOutOf4Table(id, (byte) 1) +"='" + id + "'" + " AND "+db.getColumnNameOutOf4Table(id, (byte) 10) +"='0'");break;
            }
            String[][] recyclerViewWagesData =null;
            if(wagesCursor!=null&&wagesCursor.getCount()!= 0) {
                recyclerViewWagesData = new String[wagesCursor.getCount()][wagesCursor.getColumnCount()];
                int row = 0;
                while (wagesCursor.moveToNext()) {
                    for (int col = 0; col < wagesCursor.getColumnCount(); col++) {
                        if(col !=1) {
                            recyclerViewWagesData[row][col] = wagesCursor.getString(col);//storing all data in 2d string
                        }else{//when col is 1 then convert wages to number system
                            recyclerViewWagesData[row][col]= MyUtility.convertToIndianNumberSystem(wagesCursor.getLong(col));
                        }
                    }
                    row++;
                }
            }
            if(wagesCursor!=null) wagesCursor.close();

            return recyclerViewWagesData;//when no data return null
        }catch (Exception ex){
            ex.printStackTrace();
            Log.d(MyUtility.class.getSimpleName(),"exception occurred in method "+Thread.currentThread().getStackTrace()[2].getMethodName());
            errorDetection[0]=true;
            return new String[][]{{"error"}};//to avoid error
        }
    }
    public static String[] getWagesHeadersFromDbBasedOnIndicator(Context context,String id, byte indicator, boolean[] errorDetection) {//  if error errorDetection will be set to true
        if(!(indicator >= 1 && indicator <=4)) return new String[]{"error"};//indicator is not in range 1 to 4 then return to avoid error

        Cursor cursor2=null;//returnOnlySkill will return only string of array
        Database db=Database.getInstance(context);
        try(Cursor cursor1=db.getData("SELECT "+Database.COL_8_MAINSKILL1 +" FROM " +Database.TABLE_NAME1+ " WHERE "+Database.COL_1_ID+"= '" + id +"'")){
            cursor1.moveToFirst();
            switch (indicator) {
                case 1: {return new String[]{"DATE", "WAGES", cursor1.getString(0), "REMARKS"};}
                case 2: {
                    cursor2 = db.getData("SELECT " + Database.COL_36_SKILL2 + " FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE "+Database.COL_31_ID+"='" + id + "'");
                    cursor2.moveToFirst();
                    return new String[]{"DATE", "WAGES", cursor1.getString(0), cursor2.getString(0), "REMARKS"};
                }
                case 3: { cursor2 = db.getData("SELECT " + Database.COL_36_SKILL2 + " ," + Database.COL_37_SKILL3 + " FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE "+Database.COL_31_ID+"='" + id + "'");
                    cursor2.moveToFirst();
                    return new String[]{"DATE", "WAGES", cursor1.getString(0), cursor2.getString(0), cursor2.getString(1), "REMARKS"};
                }
                case 4: { cursor2 = db.getData("SELECT " + Database.COL_36_SKILL2 + " ," + Database.COL_37_SKILL3 + " ," + Database.COL_38_SKILL4 + " FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE "+Database.COL_31_ID+"='" + id + "'");
                    cursor2.moveToFirst();
                    return new String[]{"DATE", "WAGES", cursor1.getString(0), cursor2.getString(0), cursor2.getString(1), cursor2.getString(2), "REMARKS"};
                }
            }
            return new String[]{"no indicator", "no indicator", "no indicator", "no indicator"};//this statement will not execute due to return statement in switch just to remove error used
        }catch (Exception ex){
            ex.printStackTrace();
            Log.d(MyUtility.class.getSimpleName(),"exception occurred in method "+Thread.currentThread().getStackTrace()[2].getMethodName());
            errorDetection[0]=true;
            return new String[]{"error"};//to avoid error
        }finally {//since there is return statement in try and catch block so finally needed
            if(cursor2!=null) {
                cursor2.close();
            }
        }
    }
    public static int[] getSumOfTotalWagesDepositRateDaysWorkedBasedOnIndicator(Context context,String id, byte indicator, boolean[] errorDetection) {//important method return arr with value but if error return arr with 0 value and errorDetection set to true;.it index value is sensitive.according to indicator it store date in particular index
        if(!(indicator >= 1 && indicator <=4)) return new int[2*(indicator+1)];//if indicator is not in range 1 to 4 then return exception all value will be 0

        Cursor rateCursor=null;//return data in format [wages,p1,p2,p3,p4,deposit,r1,r2,r3,r4]
        int sumDepositWages[]=null;
        Database db = Database.getInstance(context);
        try{
            switch(indicator){
                case 1:{ sumDepositWages=getSumDepositWages(db,id,indicator,context);
                    rateCursor=db.getData("SELECT  "+Database.COL_32_R1+" FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE "+Database.COL_31_ID+"= '" + id +"'");
                }break;
                case 2:{ sumDepositWages=getSumDepositWages(db,id,indicator,context);
                       rateCursor=db.getData("SELECT  "+Database.COL_32_R1+", "+Database.COL_33_R2+" FROM "+ Database.TABLE_NAME_RATE_SKILL +" WHERE "+Database.COL_31_ID+"= '" + id +"'");
                }break;
                case 3:{ sumDepositWages=getSumDepositWages(db,id,indicator,context);
                    rateCursor=db.getData("SELECT  "+Database.COL_32_R1+", "+Database.COL_33_R2+", "+Database.COL_34_R3+" FROM "+ Database.TABLE_NAME_RATE_SKILL +" WHERE "+Database.COL_31_ID+"= '" + id +"'");
                }break;
                case 4:{ sumDepositWages=getSumDepositWages(db,id,indicator,context);
                    rateCursor=db.getData("SELECT  "+Database.COL_32_R1+", "+Database.COL_33_R2+", "+Database.COL_34_R3+", "+Database.COL_35_R4+" FROM "+ Database.TABLE_NAME_RATE_SKILL +" WHERE "+Database.COL_31_ID+"= '" + id +"'");
                }break;
            }
            int[] arr=new int[2*(indicator+1)];//size will change according to indicator to get exact size.like indicator 1 need 4 space in array so formula is [2*(indicator+1)]
            int col=0;
//            if (sumDepositWagesCursor !=null && sumDepositWagesCursor.getCount()!=0) {
//                sumDepositWagesCursor.moveToFirst();
//                for (int i = 0; i < sumDepositWagesCursor.getColumnCount(); i++) {//retrieving data from cursor
//                    arr[col++]=sumDepositWagesCursor.getInt(i);
//                }
//            }
            if (sumDepositWages !=null) {
                for (int i = 0; i < sumDepositWages.length; i++) {//retrieving data from cursor
                    arr[col++]=sumDepositWages[i];
                }
            }

            if (rateCursor !=null && rateCursor.getCount()!=0){
                rateCursor.moveToFirst();
                for (int i = 0; i < rateCursor.getColumnCount(); i++){//retrieving data from cursor
                    arr[col++]=rateCursor.getInt(i);
                }
            }
            return arr;
        }catch (Exception ex){
            ex.printStackTrace();
            errorDetection[0]=true;//indicate error has occur
            return new int[2*(indicator+1)];//if exception occur 0 value will be return
        }finally {//since there is return statement in try and catch block so finally needed
            if( rateCursor !=null) {
                rateCursor.close();
            }
        }
    }
    private static int[] getSumDepositWages(Database db, String id, byte indicator,Context context){//return array in format [wages,p1,p2,p3,p4,deposit,r1,r2,r3,r4]
        if(!(indicator >= 1 && indicator <=4)) return new int[2*(indicator+1)];//if indicator is not in range 1 to 4 then return exception all value will be 0
        Cursor sumDepositWagesCursor=null;
        ArrayList<Integer> result=new ArrayList<>(10);
       try {
           switch (indicator) {

               case 1: {
                   sumDepositWagesCursor = db.getData("SELECT SUM(" + db.getColumnNameOutOf4Table(id, (byte) 6) +") FROM " + db.tableNameOutOf4Table(id) + " WHERE " + db.getColumnNameOutOf4Table(id, (byte) 1) + "= '" + id + "'");
                   sumDepositWagesCursor.moveToFirst();
                   result.add(getTotalWagesAmount(id,context));
                   result.add(sumDepositWagesCursor.getInt(0));
                   result.add(getTotalDepositAmount(id,context));
               }
               break;
               case 2: {
                   sumDepositWagesCursor = db.getData("SELECT  SUM(" + db.getColumnNameOutOf4Table(id, (byte) 6) + "),SUM(" + db.getColumnNameOutOf4Table(id, (byte) 7) +") FROM " + db.tableNameOutOf4Table(id) + " WHERE " + db.getColumnNameOutOf4Table(id, (byte) 1) + "= '" + id + "'");
                   sumDepositWagesCursor.moveToFirst();
                   result.add(getTotalWagesAmount(id,context));
                   result.add(sumDepositWagesCursor.getInt(0));
                   result.add(sumDepositWagesCursor.getInt(1));
                   result.add(getTotalDepositAmount(id,context));
               }
               break;
               case 3: {
                   sumDepositWagesCursor = db.getData("SELECT SUM(" + db.getColumnNameOutOf4Table(id, (byte) 6) + "),SUM(" + db.getColumnNameOutOf4Table(id, (byte) 7) + "),SUM(" + db.getColumnNameOutOf4Table(id, (byte) 8) +") FROM " + db.tableNameOutOf4Table(id) + " WHERE " + db.getColumnNameOutOf4Table(id, (byte) 1) + "= '" + id + "'");
                   sumDepositWagesCursor.moveToFirst();
                   result.add(getTotalWagesAmount(id,context));
                   result.add(sumDepositWagesCursor.getInt(0));
                   result.add(sumDepositWagesCursor.getInt(1));
                   result.add(sumDepositWagesCursor.getInt(2));
                   result.add(getTotalDepositAmount(id,context));
               }
               break;
               case 4: {
                   sumDepositWagesCursor = db.getData("SELECT SUM(" + db.getColumnNameOutOf4Table(id, (byte) 6) + "),SUM(" + db.getColumnNameOutOf4Table(id, (byte) 7) + "),SUM(" + db.getColumnNameOutOf4Table(id, (byte) 8) + "),SUM(" + db.getColumnNameOutOf4Table(id, (byte) 9) +")  FROM " + db.tableNameOutOf4Table(id) + " WHERE " + db.getColumnNameOutOf4Table(id, (byte) 1) + "= '" + id + "'");
                   sumDepositWagesCursor.moveToFirst();
                   result.add(getTotalWagesAmount(id,context));
                   result.add(sumDepositWagesCursor.getInt(0));
                   result.add(sumDepositWagesCursor.getInt(1));
                   result.add(sumDepositWagesCursor.getInt(2));
                   result.add(sumDepositWagesCursor.getInt(3));
                   result.add(getTotalDepositAmount(id,context));
               }
               break;
           }
//           int[] intArray = new int[result.size()];
//           for (int i = 0; i < result.size(); i++) {
//               intArray[i] = result.get(i);
//           }
          // return  result.stream().mapToInt(i -> i).toArray();
           return result.stream().mapToInt(Integer::intValue).toArray();//Convert the ArrayList to an int array

       }catch (Exception x){
           x.printStackTrace();
           return null;//returning null is important coz it indicate error
       }finally {
           if (sumDepositWagesCursor != null) {
               sumDepositWagesCursor.close();
           }
       }
    }
    public static Integer getTotalDepositAmount(String id, Context context){//return null if error
        Database db = Database.getInstance(context);
        try {
           Cursor cursor=db.getData("SELECT SUM("+ db.getColumnNameOutOf4Table(id, (byte) 5)+") FROM " + db.tableNameOutOf4Table(id) + " WHERE " +db.getColumnNameOutOf4Table(id, (byte) 10) + "='1' AND "+ db.getColumnNameOutOf4Table(id, (byte) 1) + "= '"+id+"'");
                  if(cursor.getCount()==0) return null;
                  cursor.moveToFirst();
                  return cursor.getInt(0);
        }catch (Exception x){
            x.printStackTrace();
            return null;
        }
    }
    public static Integer getTotalWagesAmount(String id, Context context){//return null if error
        Database db = Database.getInstance(context);
        try {
            Cursor cursor=db.getData("SELECT SUM("+ db.getColumnNameOutOf4Table(id, (byte) 5)+") FROM " + db.tableNameOutOf4Table(id) + " WHERE " +db.getColumnNameOutOf4Table(id, (byte) 10) + "='0' AND "+ db.getColumnNameOutOf4Table(id, (byte) 1) + "= '"+id+"'");
            if(cursor.getCount()==0) return null;
            cursor.moveToFirst();
            return cursor.getInt(0);
        }catch (Exception x){
            x.printStackTrace();
            return null;
        }
    }
    public static byte get_indicator(Context context,String PersonId){//indicator value start from 1.in db table there is no indicator 1. instead null value is there. but we require indicator as 1 when indicator is null so by default we are sending value 1 as default.
        Database db=Database.getInstance(context);
        try(Cursor cursor = db.getData("SELECT "+Database.COL_39_INDICATOR+" FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE "+Database.COL_31_ID+"= '" + PersonId + "'")) {//for sure it will return  skill
            if (cursor != null && cursor.moveToFirst()){

                if (cursor.getString(0) == null) {//initially if indicator is null then  it is considered as 1
                    return 1;
                } else {
                    return (byte) cursor.getShort(0);
                }
            }else
                Log.d(MyUtility.class.getSimpleName(), "NO DATA IN CURSOR" + Thread.currentThread().getStackTrace()[2].getMethodName());

        }catch(Exception ex){
            ex.printStackTrace();
            return -1;//potential may cause error
        }
        return -1;//by default -1
    }
    public static String[][] makeSummaryAndWriteToPDFBasedOnIndicator(Context context, byte indicator, String id, int[] arrayOfTotalWagesDepositRateAccordingToIndicator) {
        if(!(indicator >= 1 && indicator <=4)) return new String[][]{new String[]{"error"},new String[]{"error"},new String[]{"error"}};//if indicator is not in range 1 to 4 then return
        Database db=Database.getInstance(context);
        try(
            Cursor cursor=db.getData("SELECT "+Database.COL_13_ADVANCE+" ,"+Database.COL_14_BALANCE+" FROM " + Database.TABLE_NAME1 + " WHERE "+Database.COL_1_ID+"= '" + id + "'")) {
            cursor.moveToFirst();//means only one row is returned
            String[] header =null,totalCalculationForSummary =null,finalMessage =null;

            if (cursor.getInt(0) != 0 && cursor.getInt(1) == 0) {
                header=headersForSummaryBasedOnIndicator(indicator,arrayOfTotalWagesDepositRateAccordingToIndicator,context.getResources().getString(R.string.advance_due));
                totalCalculationForSummary=totalWagesWorkAmountDepositAdvanceOrBalanceForSummaryBasedOnIndicator(indicator,arrayOfTotalWagesDepositRateAccordingToIndicator,cursor.getInt(0));
                finalMessage=new String[]{ context.getResources().getString(R.string.star_after_calculation_advance_rs_dot) + MyUtility.convertToIndianNumberSystem(cursor.getInt(0))};

            }else if (cursor.getInt(0) == 0 && cursor.getInt(1) != 0) {
                header=headersForSummaryBasedOnIndicator(indicator,arrayOfTotalWagesDepositRateAccordingToIndicator,context.getResources().getString(R.string.balance));
                totalCalculationForSummary=totalWagesWorkAmountDepositAdvanceOrBalanceForSummaryBasedOnIndicator(indicator,arrayOfTotalWagesDepositRateAccordingToIndicator,cursor.getInt(1));
                finalMessage=new String[]{context.getResources().getString(R.string.star_after_calculation_balance_rs_dot) + MyUtility.convertToIndianNumberSystem(cursor.getInt(1))};

            }else if(cursor.getInt(0) == 0 && cursor.getInt(1) == 0){
                header=headersForSummaryBasedOnIndicator(indicator,arrayOfTotalWagesDepositRateAccordingToIndicator,context.getResources().getString(R.string.all_cleared));
                totalCalculationForSummary=totalWagesWorkAmountDepositAdvanceOrBalanceForSummaryBasedOnIndicator(indicator,arrayOfTotalWagesDepositRateAccordingToIndicator,0);
                finalMessage=new String[]{context.getResources().getString(R.string.star_after_calculation_no_dues_dot)};
             }
            return new String[][]{header,totalCalculationForSummary,finalMessage};
        }catch (Exception ex){
            ex.printStackTrace();
            return new String[][]{new String[]{"error"},new String[]{"error"},new String[]{"error"}};
        }
    }
    public static String[] totalWagesWorkAmountDepositAdvanceOrBalanceForSummaryBasedOnIndicator(byte indicator, int[] arrayOfTotalWagesDepositRateAccordingToIndicator, int advanceOrBalanceAmount){
        try{
            switch(indicator){
                case 1: {if(arrayOfTotalWagesDepositRateAccordingToIndicator[indicator+1] == 0) {//if no deposit
//                                                                                                        wages                                                                                                                            p1*r1
                    return new String[]{MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[0]), MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[1] * arrayOfTotalWagesDepositRateAccordingToIndicator[3]), MyUtility.convertToIndianNumberSystem(advanceOrBalanceAmount)};
                }else{//if deposit then add deposit
                    //                                                    wages                                                                                             deposit                                                                                                                   p1*r1
                    return new String[]{MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[0]), MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[2]+(arrayOfTotalWagesDepositRateAccordingToIndicator[1] * arrayOfTotalWagesDepositRateAccordingToIndicator[3])), MyUtility.convertToIndianNumberSystem(advanceOrBalanceAmount)};
                }
                }
                case 2: {if(arrayOfTotalWagesDepositRateAccordingToIndicator[indicator+1] == 0){//if no deposit
                    return new String[]{MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[0]),MyUtility.convertToIndianNumberSystem((arrayOfTotalWagesDepositRateAccordingToIndicator[1] * arrayOfTotalWagesDepositRateAccordingToIndicator[4])+(arrayOfTotalWagesDepositRateAccordingToIndicator[2] * arrayOfTotalWagesDepositRateAccordingToIndicator[5])),MyUtility.convertToIndianNumberSystem(advanceOrBalanceAmount)};
                }else{//if deposit then add deposit
                    return new String[]{MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[0]),MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[3]+(arrayOfTotalWagesDepositRateAccordingToIndicator[1] * arrayOfTotalWagesDepositRateAccordingToIndicator[4])+(arrayOfTotalWagesDepositRateAccordingToIndicator[2] * arrayOfTotalWagesDepositRateAccordingToIndicator[5])),MyUtility.convertToIndianNumberSystem(advanceOrBalanceAmount)};
                }
                }
                case 3: {if(arrayOfTotalWagesDepositRateAccordingToIndicator[indicator+1] == 0){//if no deposit
                    return new String[]{MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[0]),MyUtility.convertToIndianNumberSystem((arrayOfTotalWagesDepositRateAccordingToIndicator[1] * arrayOfTotalWagesDepositRateAccordingToIndicator[5])+(arrayOfTotalWagesDepositRateAccordingToIndicator[2] * arrayOfTotalWagesDepositRateAccordingToIndicator[6])+(arrayOfTotalWagesDepositRateAccordingToIndicator[3] * arrayOfTotalWagesDepositRateAccordingToIndicator[7])),MyUtility.convertToIndianNumberSystem(advanceOrBalanceAmount)};
                }else{//if deposit then add deposit
                    return new String[]{MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[0]),MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[4]+(arrayOfTotalWagesDepositRateAccordingToIndicator[1] * arrayOfTotalWagesDepositRateAccordingToIndicator[5])+(arrayOfTotalWagesDepositRateAccordingToIndicator[2] * arrayOfTotalWagesDepositRateAccordingToIndicator[6])+(arrayOfTotalWagesDepositRateAccordingToIndicator[3] * arrayOfTotalWagesDepositRateAccordingToIndicator[7])),MyUtility.convertToIndianNumberSystem(advanceOrBalanceAmount)};
                }
                }
                case 4: {if(arrayOfTotalWagesDepositRateAccordingToIndicator[indicator+1] == 0){//if no deposit
                    return new String[]{MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[0]),MyUtility.convertToIndianNumberSystem((arrayOfTotalWagesDepositRateAccordingToIndicator[1] * arrayOfTotalWagesDepositRateAccordingToIndicator[6])+(arrayOfTotalWagesDepositRateAccordingToIndicator[2] * arrayOfTotalWagesDepositRateAccordingToIndicator[7])+(arrayOfTotalWagesDepositRateAccordingToIndicator[3] * arrayOfTotalWagesDepositRateAccordingToIndicator[8])+(arrayOfTotalWagesDepositRateAccordingToIndicator[4] * arrayOfTotalWagesDepositRateAccordingToIndicator[9])),MyUtility.convertToIndianNumberSystem(advanceOrBalanceAmount)};
                }else{//if deposit then add deposit
                    return new String[]{MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[0]),MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[5]+(arrayOfTotalWagesDepositRateAccordingToIndicator[1] * arrayOfTotalWagesDepositRateAccordingToIndicator[6])+(arrayOfTotalWagesDepositRateAccordingToIndicator[2] * arrayOfTotalWagesDepositRateAccordingToIndicator[7])+(arrayOfTotalWagesDepositRateAccordingToIndicator[3] * arrayOfTotalWagesDepositRateAccordingToIndicator[8])+(arrayOfTotalWagesDepositRateAccordingToIndicator[4] * arrayOfTotalWagesDepositRateAccordingToIndicator[9])),MyUtility.convertToIndianNumberSystem(advanceOrBalanceAmount)};
                }
                }
            }
            return new String[]{"no indicator","no indicator","no indicator"};
        }catch(Exception ex){
            ex.printStackTrace();
            return new String[]{"error","error","error"};
        }
    }
    public static String[] headersForSummaryBasedOnIndicator(byte indicator, int[] arrayOfTotalWagesDepositRateAccordingToIndicator, String advanceOrBalanceString) {
        try{
            if(arrayOfTotalWagesDepositRateAccordingToIndicator[indicator+1] == 0){//indicator+1 is index of deposit in array of arrayOfTotalWagesDepositRateAccordingToIndicator
                return new String[]{"TOTAL WAGES","TOTAL WORK AMOUNT",advanceOrBalanceString};
            }else{//when deposit present
                return new String[]{"TOTAL WAGES","TOTAL DEPOSIT + WORK AMOUNT",advanceOrBalanceString};
            }
        }catch(Exception ex){
            ex.printStackTrace();
            return new String[]{"error","error","error"};//when exception occur it will set as header
        }
    }
    public static String[] fetchPersonDetailOfPDF(String id, Context context){
        Database db=Database.getInstance(context);
          try(
             Cursor cursor1 = db.getData("SELECT " + Database.COL_2_NAME + " , " + Database.COL_3_BANKAC + " , " + Database.COL_6_AADHAAR_NUMBER + " , " + Database.COL_10_IMAGE + " FROM " + Database.TABLE_NAME1 + " WHERE "+Database.COL_1_ID+"='" + id + "'");
             Cursor cursor2 = db.getData("SELECT " + Database.COL_396_PDFSEQUENCE + " FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE "+Database.COL_31_ID+"= '" + id + "'")){
            if (cursor1 != null){
                cursor1.moveToFirst();
                String bankAccount, aadhaar;
                int pdfSequenceNo;

                if (cursor1.getString(1)!=null && cursor1.getString(1).length() > 4) {
                    bankAccount = cursor1.getString(1).substring(cursor1.getString(1).length() - 4);
                } else {
                    bankAccount = "";
                }
                if (cursor1.getString(2)!=null && cursor1.getString(2).length() > 5) {
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

                String activePhoneNumber=MyUtility.getActiveOrBothPhoneNumber(id,context,true);
                if(activePhoneNumber != null){
                    activePhoneNumber= activePhoneNumber.substring(activePhoneNumber.length() - 6);//phone number
                }else{
                    activePhoneNumber="";
                }
                 return new String[]{cursor1.getString(0),id,bankAccount,aadhaar,activePhoneNumber,MyUtility.get12hrCurrentTimeAndDate(), String.valueOf(pdfSequenceNo)};
             }else{
                return new String[]{"[NULL NO DATA IN CURSOR]","[NULL NO DATA IN CURSOR]","[NULL NO DATA IN CURSOR]","[NULL NO DATA IN CURSOR]","[NULL NO DATA IN CURSOR]","[NULL NO DATA IN CURSOR]","[NULL NO DATA IN CURSOR]"};
             }
        }catch (Exception ex){
            ex.printStackTrace();
            return new String[]{"[ERROR]","[ERROR]","[ERROR]","[ERROR]","[ERROR]","[ERROR]","[ERROR]"};
        }
    }
    public static boolean checkPermissionAudioAndExternal(Context context) {//checking for permission of mic and external storage
        try {
            return (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) &&
                    (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }
    }
    public static boolean isFolderExistIfNotExistCreateIt(String externalFileDir, String folderName){
         //be carefully take only getExternalFilesDir( null ) https://stackoverflow.com/questions/59017202/mediarecorder-stop-failed
        try{
            File folder = new File( externalFileDir + "/"+folderName);//File folder = new File( externalFileDir + "/acBookPDF");   //https://stackoverflow.com/questions/65125446/cannot-resolve-method-getexternalfilesdir
            if (!folder.exists()){//if folder not exist then create folder
                 return folder.mkdir();//File createNewFile() method returns true if new file is created and false if file already exists.
            }
            return true;
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }
    }
    public static boolean checkPermissionForReadAndWriteToExternalStorage(Context context) {
        try {
            return (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                    (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }
    }
//    public static boolean requestReadWriteExternalStoragePermission(Context context) {//Casting Context to Activity: In your code, you are casting the Context to an Activity with (Activity)context before making the permission request. This may lead to a ClassCastException if the provided context is not an Activity. You should ensure that the context passed to this method is indeed an instance of an Activity. If it's not an Activity, you should handle this case to prevent a runtime exception.
//        if (context instanceof Activity) {
//            Toast.makeText(context, "READ, WRITE EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show();
//            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 20);
//            return true;
//        } else {
//            // Handle the case where the provided context is not an Activity
//            // You might display an error message or log a message.
//            Log.e("PermissionRequest", "Context is not an Activity");
//            Toast.makeText(context, "context is not an activity", Toast.LENGTH_LONG).show();
//            return false;
//        }
//    }
//    public static String getActiveOrBothPhoneNumber(String id, Context context, boolean forOnlyOneActiveNumberTrue){//if no data return null..it return first phone number if first not available then send second phone number will be return
//        Database db = Database.getInstance(context);
//        try (Cursor cursor = db.getData("SELECT " +Database.COL_7_ACTIVE_PHONE1+" , "+Database.COL_11_ACTIVE_PHONE2 + " FROM " + Database.TABLE_NAME1 + " WHERE "+Database.COL_1_ID+"='" + id + "'")){
//            if (cursor != null &&  cursor.moveToFirst()) {//which ever phone is available that phone will be send
//
//                if (forOnlyOneActiveNumberTrue){
//                    if (cursor.getString(0) != null && cursor.getString(0).length() == 10) {
//                        return cursor.getString(0);
//                    }
//                if(cursor.getString(1) != null && cursor.getString(1).length() == 10) {
//                    return cursor.getString(1);
//                }
//               }else{
//                    StringBuilder sb = new StringBuilder();
//                    if (cursor.getString(0) != null && cursor.getString(0).length() == 10) {
//                        sb.append(cursor.getString(0));
//                    }
//
//                    if(cursor.getString(1) != null && cursor.getString(1).length() == 10) {
//                        if(sb.length()>0){//add comma when first number is there
//                            sb.append(", "+cursor.getString(1));
//                        }
//                        sb.append(cursor.getString(1));
//                    }
//                    return sb.toString();//return both contact number
//                }
//            }
//        }catch(Exception ex){
//            ex.printStackTrace();
//            return null;
//        }
//        return null;
//    }
    public static String getActiveOrBothPhoneNumber(String id, Context context, boolean forOnlyOneActiveNumberTrue) {//if no data return null..if forOnlyOneActiveNumberTrue is true it return first phone number if first not available then send second phone number will be return.if false return both number
        Database db = Database.getInstance(context);
        String activePhone1 = null;
        String phone2 = null;

        try (Cursor cursor = db.getData("SELECT " + Database.COL_7_ACTIVE_PHONE1 + " , " + Database.COL_11_ACTIVE_PHONE2 + " FROM " + Database.TABLE_NAME1 + " WHERE " + Database.COL_1_ID + "='" + id + "'")) {
            if (cursor != null && cursor.moveToFirst()) {
                activePhone1 = cursor.getString(0);
                phone2 = cursor.getString(1);
                if(activePhone1 == null && phone2==null) return null;//no data
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        if (forOnlyOneActiveNumberTrue) {
            if (activePhone1 != null && activePhone1.length() == 10) {
                return activePhone1;
            } else if (phone2 != null && phone2.length() == 10) {
                return phone2;
            }
        } else {
            StringBuilder sb = new StringBuilder();
            if (activePhone1 != null && activePhone1.length() == 10) {
                sb.append(activePhone1);
            }
            if (phone2 != null && phone2.length() == 10) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(phone2);
            }
            return sb.toString(); // Return both contact numbers
        }
        return null;
    }
    public static String convertToReadableNumber(String number){//In this optimized code, we create a char array arr with an initial capacity equal to the length of the input string str, plus an extra capacity for the spaces that will be inserted (i.e., str.length() + str.length() / 4). We then loop through each character of the input string str, appending each character to arr using arr[j++] = str.charAt(i).After every fourth character (except for the last character in the string), we append a space character to arr using arr[j++] = ' '. We use an integer variable j to keep track of the next index in arr where a character should be inserted.
        if(number==null){
            return "null";
        }
        char[] arr=new char[number.length()+number.length()/4];
        int j=0;
        for (int i = 0; i < number.length(); i++) {
            arr[j++]=number.charAt(i);
            if((i+1)%4==0 && i!=number.length()-1){
                arr[j++]=' ';
            }
        }
        return new String(arr).trim();
    }
    public static String convertToIndianNumberSystem(long number) {//https://www.geeksforgeeks.org/convert-the-number-from-international-system-to-indian-system/
        String inputString = String.valueOf(number);//converting integer to string
        StringBuilder result = new StringBuilder();
        //when length is odd then place , after 2 digit and when length is even then place , after 1 digit
        for (int i = 0; i < inputString.length(); i++) {
            if (inputString.length() > 3 && i < (inputString.length() - 3)) {//i < (inputString.length() - 3) TO PREVENT last 3 digit to add , because last 3 digit don't contain comma
                if (inputString.length() % 2 == 0) {//if length is even
                    if (i % 2 == 0) {
                        result.append(inputString.charAt(i));
                        result.append(",");
                    } else {
                        result.append(inputString.charAt(i));
                    }
                } else if (inputString.length() % 2 != 0) {//if length is odd
                    if (i != 0 && i % 2 != 0) {//to prevent when i=0
                        result.append(inputString.charAt(i));
                        result.append(",");
                    } else {
                        result.append(inputString.charAt(i));
                    }
                } else {//else is important to add
                    result.append(inputString.charAt(i));
                }
            } else {
                result.append(inputString.charAt(i));
            }
        }
        return result.toString();
    }
    public static void sortArrayList(ArrayList<MestreLaberGModel> arrayListShouldMayOrMayNotContainNullAtFirstHalfAndSecondHalfNotNull) {
        /*
         *This function will sort Arraylist in such a way that all LatestDate which is null will be at top.
         * and based on that it will sort the remaining record*/

        int[] nullCountInArraylist = countNullAndTodayLatestDateAndBringAllLatestDateWhichIsNullAtTop(arrayListShouldMayOrMayNotContainNullAtFirstHalfAndSecondHalfNotNull);//it will return null count and latest date count and swap all latest date which is null and bring at top of arrayList

        if(nullCountInArraylist[0]== 0 && nullCountInArraylist[1] == 0){//if today Latest date is not there and new person not there then execute this
            Collections.sort(arrayListShouldMayOrMayNotContainNullAtFirstHalfAndSecondHalfNotNull.subList(0, arrayListShouldMayOrMayNotContainNullAtFirstHalfAndSecondHalfNotNull.size()));//natural sorting based on latest date desc comparator implemented

         }else {//sorting to original array in asc order by taking latest date
            Collections.sort(arrayListShouldMayOrMayNotContainNullAtFirstHalfAndSecondHalfNotNull.subList(nullCountInArraylist[0], arrayListShouldMayOrMayNotContainNullAtFirstHalfAndSecondHalfNotNull.size()), new Comparator<>() {
                DateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                @Override
                public int compare(MestreLaberGModel obj1, MestreLaberGModel obj2) {
                    try {
                        return sdf.parse(obj1.getLatestDate()).compareTo(sdf.parse(obj2.getLatestDate()));//will return o when two dates is same
                    } catch (ParseException e) {
                        throw new IllegalArgumentException(e);
                    }catch (Exception x){
                        x.printStackTrace();
                        return 0;//means equal string
                    }
                }
            });
            //arraylist Manipulation
            if (nullCountInArraylist[1] != 0) { //if there is today latestDate then sort last part using time desc order
                Collections.sort(arrayListShouldMayOrMayNotContainNullAtFirstHalfAndSecondHalfNotNull.subList(arrayListShouldMayOrMayNotContainNullAtFirstHalfAndSecondHalfNotNull.size() - nullCountInArraylist[1], arrayListShouldMayOrMayNotContainNullAtFirstHalfAndSecondHalfNotNull.size()), (obj1, obj2) -> {
                    Date obj1Date = null, obj2Date = null;
                    SimpleDateFormat format24hrs = new SimpleDateFormat("HH:mm:ss aa");//24 hrs format
                    SimpleDateFormat format12hrs = new SimpleDateFormat("hh:mm:ss aa");//12 hrs format
                    try{
                        obj1Date = format12hrs.parse(obj1.getTime());
                        obj2Date = format12hrs.parse(obj2.getTime());
                    }catch (ParseException e) {
                        e.printStackTrace();
                    }catch (Exception x){
                        x.printStackTrace();
                    }
//                    String obj1StringDate=format24hrs.format(obj1Date);
//                    String obj2StringDate=format24hrs.format(obj2Date);
                    return Integer.parseInt(format24hrs.format(obj2Date).replaceAll("[:]", "").substring(0, 6)) - Integer.parseInt(format24hrs.format(obj1Date).replaceAll("[:]", "").substring(0, 6));
                });//first making "00:59:30 PM" to 5930 .removing start 0 and :,AM,PM.PARSING TO INTEGER so that start 0 will remove.//sort   time in desc order.index start from 0 n-1.this will keep today's time on top so that search would be easy.arraylist is already sorted so we are sorting only last half obj which has today's time
            }
            //since array list already sorted in asc order so just reversing to get desc order
            reverseArrayListUsingIndex(arrayListShouldMayOrMayNotContainNullAtFirstHalfAndSecondHalfNotNull,nullCountInArraylist[0], arrayListShouldMayOrMayNotContainNullAtFirstHalfAndSecondHalfNotNull.size() - nullCountInArraylist[1]);
        }//else
    }
    public static void snackBar(View view,String message){
        try {
            Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
        }catch (Exception x){
            x.printStackTrace();
        }
    }
    public static int[] countNullAndTodayLatestDateAndBringAllLatestDateWhichIsNullAtTop(ArrayList<MestreLaberGModel> al) {
        int[] arr =new int[2];
        int nullIndex=0;
        // LocalDate todayDate = LocalDate.now();//current date; return 2022-05-01
        // String currentDateDBPattern =""+ todayDate.getDayOfMonth()+"-"+ todayDate.getMonthValue()+"-"+ todayDate.getYear();//converted to 1-5-2022
        for (int i = 0; i < al.size(); i++) {
            if(al.get(i).getLatestDate()== null){
                swapAndBringNullObjAtTopOfArrayList(al,nullIndex,i);
                nullIndex++;
                arr[0]++;//nullCount
            }                                                    //today date
            else if(al.get(i).getLatestDate().equals(""+ LocalDate.now().getDayOfMonth()+"-"+ LocalDate.now().getMonthValue()+"-"+ LocalDate.now().getYear())){
                arr[1]++;//todayLatestDateCount
            }
        }
        return arr;
    }
    public static void swapAndBringNullObjAtTopOfArrayList(ArrayList<MestreLaberGModel> al, int nullIndex, int i) {
        MestreLaberGModel nullObj=al.get(i);
        MestreLaberGModel nonNullObj=al.get(nullIndex);
        al.set(i,nonNullObj);
        al.set(nullIndex,nullObj);
    }
    public static <T> void reverseArrayListUsingIndex(List<T> al, int start, int end){
        while (start < end) {
            T a=al.get(start);
            T b=al.get(end-1);
            al.set(start,b);
            al.set(end-1,a);
            start++;
            end--;
        }
    }
    public static void showDefaultDialog(String title, String message, Context context) {
        AlertDialog.Builder showDataFromDataBase = new AlertDialog.Builder(context);
        showDataFromDataBase.setCancelable(false);
        showDataFromDataBase.setTitle(title);
        showDataFromDataBase.setMessage(message);
        showDataFromDataBase.setPositiveButton(context.getResources().getString(R.string.ok), (dialogInterface, i) -> {
            dialogInterface.dismiss();//while here refreshing getting error cursor out of bound exception if we put code of refresh then when user want to see remarks then also it will refresh so no needed
        });
        showDataFromDataBase.create().show();
    }
    public static void spinnerAudioRemarksShare(AdapterView<?> adapterView, int pos, View view, String micPath, String remarks, Context context, WagesDetailsModel data){
        String a = adapterView.getItemAtPosition(pos).toString();//get adapter position
        switch(a){
            case "AUDIO": {
                if (micPath != null) {//checking audi is present or not
                    if(VoiceRecorder.audioPlayer(micPath)){
                        MyUtility.snackBar(view,view.getContext().getResources().getString(R.string.audio_playing));
                    }else{
                        MyUtility.snackBar(view,view.getContext().getResources().getString(R.string.audio_not_found_may_be_deleted));
                    }
                } else{
                    MyUtility.snackBar(view,view.getContext().getResources().getString(R.string.no_audio) );
                }
            }break;
            case "REMARKS":{
                if(remarks != null) {//checking remarks is present or not
                    MyUtility.showDefaultDialog(view.getContext().getResources().getString(R.string.remarks),remarks,context);
                }else{
                    MyUtility.snackBar(view,view.getResources().getString(R.string.no_remarks));
                }
            }break;
            case "SHARE":{
                 if(data !=null){//best code
                     if(!MyUtility.checkPermissionForReadAndWriteToExternalStorage(view.getContext())) {
                         Toast.makeText(view.getContext(), "EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show();
                         ActivityCompat.requestPermissions((Activity) view.getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 41);//in thread cannot cast this view
                         return;
                     }
                     try(Database db=Database.getInstance(context)){//share to whats app if not contact open any app to share
                         String phoneNumber = MyUtility.getActiveOrBothPhoneNumber(data.getId(), context,true);
                        String[] skillArr=db.getAllSkill(data.getId());
                         if (phoneNumber != null) {

                             if (MyUtility.shareMessageDirectlyToWhatsApp(generateRecordMessageToSend(data.getId(),data.getUserGivenDate(),data.getWagesOrDeposit(),data.getIsdeposited(),skillArr[0],data.getP1(),skillArr[1],data.getP2(),skillArr[2],data.getP3(),skillArr[3],data.getP4(),data.getRemarks()), phoneNumber, context)) {//if false then open any app

                                 db.updateAsSharedToHistory(data.getId(), data.getSystemDateAndTime());//update table as shared.if data send to contact number or whatsapp

                             }else{
                                  shareShortMessageToAnyApp(generateRecordMessageToSend(data.getId(),data.getUserGivenDate(),data.getWagesOrDeposit(),data.getIsdeposited(),skillArr[0],data.getP1(),skillArr[1],data.getP2(),skillArr[2],data.getP3(),skillArr[3],data.getP4(),data.getRemarks()), context);//open any app
                             }
                         }else{
                             Toast.makeText(context, context.getResources().getString(R.string.no_phone_number), Toast.LENGTH_LONG).show();//snack-bar not using because its get hide
                              shareShortMessageToAnyApp(generateRecordMessageToSend(data.getId(),data.getUserGivenDate(),data.getWagesOrDeposit(),data.getIsdeposited(),skillArr[0],data.getP1(),skillArr[1],data.getP2(),skillArr[2],data.getP3(),skillArr[3],data.getP4(),data.getRemarks()), context);//open any app
                         }
                     }catch (Exception x) {
                         x.printStackTrace();
                     }
                 }
            }break;
        }
    }
    public static String generateRecordMessageToSend(String id, String date, int wagesOrDeposit, boolean isDeposited, String skill1, short p1, String skill2, short p2, String skill3, short p3, String skill4, short p4, String remarks){
        StringBuilder sb=new StringBuilder();
        sb.append("ID: ").append(id).append("\n")
                .append("DATE: ").append(date).append("\n");

        if(skill1!=null){
            sb.append(skill1).append(": ").append(p1).append("  ");
        }
        if(skill2!=null){
            sb.append(skill2).append(": ").append(p2).append("  ");
            if(skill3!=null){
                sb.append(skill3).append(": ").append(p3).append("  ");
                if(skill4!=null){
                    sb.append(skill4).append(": ").append(p4);
                }
            }
        }
        sb.append("\n");
        if(isDeposited){
            sb.append("DEPOSIT: ").append(MyUtility.convertToIndianNumberSystem(wagesOrDeposit)).append("\n");
        }else{
            sb.append("WAGES: ").append(MyUtility.convertToIndianNumberSystem(wagesOrDeposit)).append("\n");
        }
        sb.append("REMARKS: ").append(remarks);
        return sb.toString();
    }
    public static boolean shareShortMessageToAnyApp(String message,Context context){
        if(message==null) {
            return false;
        }
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, message);
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//If we don't add the chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) line to set the FLAG_ACTIVITY_NEW_TASK flag, the behavior of the app when launching the chooser intent may depend on the context in which the sendMessageToAnyApp method is called.If the method is called from an activity that is already the root of a task, launching the chooser without the FLAG_ACTIVITY_NEW_TASK flag will simply add the chosen activity to the current task stack. This can lead to unexpected back stack behavior and may not be desirable if the user is expected to return to the same activity after sharing the message.On the other hand, if the method is called from an activity that is not the root of a task, launching the chooser without the FLAG_ACTIVITY_NEW_TASK flag will create a new task for the chooser and clear the previous task. This can also be unexpected and disruptive to the user's workflow.Therefore, setting the FLAG_ACTIVITY_NEW_TASK flag ensures consistent behavior regardless of the context in which the method is called, and is generally a good practice when launching chooser intents from an app
            context.startActivity(Intent.createChooser(shareIntent, context.getResources().getString(R.string.share_message_using)));//startActivity launch activity without expecting any result back SO we don't need any result back so using start activity
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
    public static  boolean shareMessageDirectlyToWhatsApp(String message,String indianWhatsappNumber,Context context){
        if(message==null || indianWhatsappNumber==null){
            return false;
        }
        try {
            if (isInternetConnected(context)){//WE CAN SEND LARGE TEXT MESSAGE USING WHATSAPP
                if (isApplicationInstalled("com.whatsapp",context)) {//package name
                    indianWhatsappNumber = "91"+indianWhatsappNumber; // Add country code prefix for Indian numbers
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=" +indianWhatsappNumber+"&text="+message));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//If we don't add the chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) line to set the FLAG_ACTIVITY_NEW_TASK flag, the behavior of the app when launching the chooser intent may depend on the context in which the sendMessageToAnyApp method is called.If the method is called from an activity that is already the root of a task, launching the chooser without the FLAG_ACTIVITY_NEW_TASK flag will simply add the chosen activity to the current task stack. This can lead to unexpected back stack behavior and may not be desirable if the user is expected to return to the same activity after sharing the message.On the other hand, if the method is called from an activity that is not the root of a task, launching the chooser without the FLAG_ACTIVITY_NEW_TASK flag will create a new task for the chooser and clear the previous task. This can also be unexpected and disruptive to the user's workflow.Therefore, setting the FLAG_ACTIVITY_NEW_TASK flag ensures consistent behavior regardless of the context in which the method is called, and is generally a good practice when launching chooser intents from an app
                   context.startActivity(intent);//startActivity launch activity without expecting any result back we don't need any result back so using startActivity WITHOUT CHOOSER BECAUSE it will directly open whatsapp.//Including Intent.FLAG_ACTIVITY_NEW_TASK is necessary when you're trying to start an activity from a context that is not an activity
                    return true;
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
            return false;
        }
        return false;
    }
    public static boolean isApplicationInstalled(String packageName,Context context){
        try {
            PackageManager packageManager = context.getPackageManager();//in manifest <query>.. </query> permission added
            packageManager.getPackageInfo(packageName,PackageManager.GET_ACTIVITIES);//if this getPackageInfo() throws exception that means not installed else installed.PackageManager.GET_ACTIVITIES is a flag that can be passed as an argument to the getPackageInfo() method of the PackageManager class in Android. This flag is used to indicate that the PackageInfo object returned should contain information about all the activities defined in the package.In the context of checking if WhatsApp is installed, using the GET_ACTIVITIES flag ensures that the method returns the information about the activities in the WhatsApp package, which is necessary for determining if WhatsApp is installed on the device or not. Without this flag, the getPackageInfo() method would only return basic information about the package, which may not be sufficient to determine if the app is installed.
            return true;

//        or this code will list all app installed in device and IF FOUND return true
//        PackageManager packageManager = getPackageManager();
//        List<PackageInfo> list = packageManager.getInstalledPackages(PackageManager.GET_META_DATA);
//        for (PackageInfo p : list) {
//            if (packageName.equals(p.packageName)) {
//                return true;
//            }
//        }

        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
            return false;
        } catch(Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
    public static boolean isInternetConnected(Context context) {//permission required in manifest file for accessing ConnectivityManager permission is ACCESS_NETWORK_STATE
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnected());
        //note:this method only checks for the availability of an active network connection and does not verify if the connection can actually access the internet. It is possible to have an active network connection but not be able to access the internet due to network issues or other reasons.
    }
    public static boolean sendMessageToContact(String id, String message,Context context){
        if(id==null || message==null || context==null){
            return false;
        }
        try{
            String phoneNumber=MyUtility.getActiveOrBothPhoneNumber(id,context,true);
            if(phoneNumber!=null){
                if(checkPermissionForSMS(context)){   //send an SMS using an intent
                    Intent intent=new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms",phoneNumber,null));//The first parameter specifies the protocol ("sms"), the second parameter specifies the recipient's phone number.URI can be used to launch the SMS app with a pre-filled recipient phone number.
                    intent.putExtra("sms_body",message);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//Including Intent.FLAG_ACTIVITY_NEW_TASK is necessary when you're trying to start an activity from a context that is not an activity
                    context.startActivity(intent);
                    return true;
                }else{
                    Toast.makeText(context, "SMS PERMISSION REQUIRED", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.SEND_SMS}, 31);
                    return false;
                }
            }else{
                Toast.makeText(context, context.getResources().getString(R.string.no_phone_number), Toast.LENGTH_LONG).show();
                return false;
            }
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
    public static boolean checkPermissionForSMS(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }
    public static boolean deleteFolderAllFiles(String folderName,boolean trueForExternalFileDirAndFalseForCacheFileDir,Context context){
        try{
            if(MyUtility.checkPermissionForReadAndWriteToExternalStorage(context)){//checking permission
                File folder;
                if(trueForExternalFileDirAndFalseForCacheFileDir){
                    folder= new File(context.getExternalFilesDir(null) + "/" + folderName);//File folder = new File( externalFileDir + "/acBookPDF");   //https://stackoverflow.com/questions/65125446/cannot-resolve-method-getexternalfilesdir
                }else{
                    folder=context.getExternalCacheDir();//getting cache directory to delete all files
                }

                if (folder.exists() && folder.isDirectory()) {//if folder exist and if it is directory then delete all file present in this folder

                    File[] listOfFiles = folder.listFiles();//getting all files present in folder
                    if (listOfFiles != null && listOfFiles.length > 0) {
                        for (File file : listOfFiles){

                            if (file.isFile()) {//file.isFile() is a method call on the File object, specifically the isFile() method. This method returns true if the File object refers to a regular file and false if it refers to a directory, a symbolic link, or if the file doesn't exist.
                                if (!file.delete()) {// File deleted successfully
                                    return false; // Failed to delete the file
                                }
                            }
                        }
                    }
                }
            }else{
                Toast.makeText(context, "READ,WRITE EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 20);
                return false;
            }
            return true;
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }
    }
    public static boolean shareFileToAnyApp(File pdfOrTextFile, String mimeType, String title,Context context){// ActivityResultLauncher<Intent> sharePdfLauncher
        // if(pdfOrTextFile==null || sharePdfLauncher==null){//sharePdfLauncher is launcher of intent and get result after successful operation completed
        if(pdfOrTextFile==null ){//sharePdfLauncher is launcher of intent and get result after successful operation completed
            return false;
        }
        try {
            //this code is used when sharePdfLauncher
//            Intent intent = new Intent(Intent.ACTION_SEND);
//            intent.setType(mimeType);
//           // Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", new File(pdfOrTextFile));//**to access file uri FileProvider.getUriForFile() is compulsory from if your target sdk version is 24 or greater otherwise cannot access
//
//            Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", pdfOrTextFile);//**to access file uri FileProvider.getUriForFile() is compulsory from if your target sdk version is 24 or greater otherwise cannot access
//            intent.putExtra(Intent.EXTRA_STREAM, uri);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//If we don't add the chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) line to set the FLAG_ACTIVITY_NEW_TASK flag, the behavior of the app when launching the chooser intent may depend on the context in which the sendMessageToAnyApp method is called.If the method is called from an activity that is already the root of a task, launching the chooser without the FLAG_ACTIVITY_NEW_TASK flag will simply add the chosen activity to the current task stack. This can lead to unexpected back stack behavior and may not be desirable if the user is expected to return to the same activity after sharing the message.On the other hand, if the method is called from an activity that is not the root of a task, launching the chooser without the FLAG_ACTIVITY_NEW_TASK flag will create a new task for the chooser and clear the previous task. This can also be unexpected and disruptive to the user's workflow.Therefore, setting the FLAG_ACTIVITY_NEW_TASK flag ensures consistent behavior regardless of the context in which the method is called, and is generally a good practice when launching chooser intents from an app
//            sharePdfLauncher.launch(Intent.createChooser(intent,title));//Intent.createChooser creates dialog to choose app to share data


            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(mimeType);
            // Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", new File(pdfOrTextFile));//**to access file uri FileProvider.getUriForFile() is compulsory from if your target sdk version is 24 or greater otherwise cannot access.T
            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", pdfOrTextFile);//**to access file uri FileProvider.getUriForFile() is compulsory from if your target sdk version is 24 or greater otherwise cannot access.this method is used to share a file with another app using a content URI
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            Intent chooser = Intent.createChooser(intent, title);
            chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
           context.startActivity(chooser);// Start the chooser dialog

            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    public static int[] getRateArray(String id,Context context){
        Database db=Database.getInstance(context);
        try(Cursor cursor =db.getData("SELECT  "+Database.COL_32_R1+","+Database.COL_33_R2+","+Database.COL_34_R3+","+Database.COL_35_R4+"  FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE "+Database.COL_31_ID+"= '" + id +"'")){
            cursor.moveToFirst();
            return new int[]{cursor.getInt(0),cursor.getInt(1),cursor.getInt(2),cursor.getInt(3)};
        }catch (Exception x){
            x.printStackTrace();
            return new int[4];//if error return array with 0 value
        }
    }
    public static void p1_p2_p3_p4_Change_Tracker(int correctInput[],int [] cursorRate, EditText inputP1, EditText inputP2, EditText inputP3, EditText inputP4, TextView runtimeSuggestionAmountToGive) {
        String p1, p2, p3, p4;
        p1 = inputP1.getText().toString().trim();
        try {
            //all 15 combination
            //only p1
            if (correctInput[0] == 1 && correctInput[1] != 1 && correctInput[2] != 1 && correctInput[3] != 1) {
                runtimeSuggestionAmountToGive.setText(String.valueOf(cursorRate[0] * Integer.parseInt(p1)));//this will execute only when correctInput[] is 1
            }
            //only p1 p2
            else if (correctInput[0] == 1 && correctInput[1] == 1 && correctInput[2] != 1 && correctInput[3] != 1) {
                p2 = inputP2.getText().toString().trim();
                runtimeSuggestionAmountToGive.setText(String.valueOf((cursorRate[0] * Integer.parseInt(p1)) + (cursorRate[1] * Integer.parseInt(p2))));
            }
            //only p1 p2,p3
            else if (correctInput[0] == 1 && correctInput[1] == 1 && correctInput[2] == 1 && correctInput[3] != 1) {
                p2 = inputP2.getText().toString().trim();
                p3 = inputP3.getText().toString().trim();
                runtimeSuggestionAmountToGive.setText(String.valueOf((cursorRate[0] * Integer.parseInt(p1)) + (cursorRate[1] * Integer.parseInt(p2)) + (cursorRate[2] * Integer.parseInt(p3))));
            }
            //only p1 p2,p3,p4
            else if (correctInput[0] == 1 && correctInput[1] == 1 && correctInput[2] == 1 && correctInput[3] == 1) {
                p2 = inputP2.getText().toString().trim();
                p3 = inputP3.getText().toString().trim();
                p4 = inputP4.getText().toString().trim();
                runtimeSuggestionAmountToGive.setText(String.valueOf((cursorRate[0] * Integer.parseInt(p1)) + (cursorRate[1] * Integer.parseInt(p2)) + (cursorRate[2] * Integer.parseInt(p3)) + (cursorRate[3] * Integer.parseInt(p4))));
            }
            //only p1 p3 p4
            else if (correctInput[0] == 1 && correctInput[1] != 1 && correctInput[2] == 1 && correctInput[3] == 1) {
                p3 = inputP3.getText().toString().trim();
                p4 = inputP4.getText().toString().trim();
                runtimeSuggestionAmountToGive.setText(String.valueOf((cursorRate[0] * Integer.parseInt(p1)) + (cursorRate[2] * Integer.parseInt(p3)) + (cursorRate[3] * Integer.parseInt(p4))));
            }
            //only p1 p2 p4
            else if (correctInput[0] == 1 && correctInput[1] == 1 && correctInput[2] != 1 && correctInput[3] == 1) {
                p2 = inputP2.getText().toString().trim();
                p4 = inputP4.getText().toString().trim();
                runtimeSuggestionAmountToGive.setText(String.valueOf((cursorRate[0] * Integer.parseInt(p1)) + (cursorRate[1] * Integer.parseInt(p2)) + (cursorRate[3] * Integer.parseInt(p4))));
            }
            //only p2 p3 p4
            else if (correctInput[0] != 1 && correctInput[1] == 1 && correctInput[2] == 1 && correctInput[3] == 1) {
                p2 = inputP2.getText().toString().trim();
                p3 = inputP3.getText().toString().trim();
                p4 = inputP4.getText().toString().trim();
                runtimeSuggestionAmountToGive.setText(String.valueOf((cursorRate[1] * Integer.parseInt(p2)) + (cursorRate[2] * Integer.parseInt(p3)) + (cursorRate[3] * Integer.parseInt(p4))));
            }
            //only p1 P4
            else if (correctInput[0] == 1 && correctInput[1] != 1 && correctInput[2] != 1 && correctInput[3] == 1) {
                p4 = inputP4.getText().toString().trim();
                runtimeSuggestionAmountToGive.setText(String.valueOf((cursorRate[0] * Integer.parseInt(p1)) + (cursorRate[3] * Integer.parseInt(p4))));
            }
            //only p1 P3
            else if (correctInput[0] == 1 && correctInput[1] != 1 && correctInput[2] == 1 && correctInput[3] != 1) {
                p3 = inputP3.getText().toString().trim();
                runtimeSuggestionAmountToGive.setText(String.valueOf((cursorRate[0] * Integer.parseInt(p1)) + (cursorRate[2] * Integer.parseInt(p3))));
            }
            //Only p3,p4
            else if (correctInput[0] != 1 && correctInput[1] != 1 && correctInput[2] == 1 && correctInput[3] == 1) {
                p3 = inputP3.getText().toString().trim();
                p4 = inputP4.getText().toString().trim();
                runtimeSuggestionAmountToGive.setText(String.valueOf((cursorRate[2] * Integer.parseInt(p3)) + (cursorRate[3] * Integer.parseInt(p4))));
            }
            //Only p2,p4
            else if (correctInput[0] != 1 && correctInput[1] == 1 && correctInput[2] != 1 && correctInput[3] == 1) {
                p2 = inputP2.getText().toString().trim();
                p4 = inputP4.getText().toString().trim();
                runtimeSuggestionAmountToGive.setText(String.valueOf((cursorRate[1] * Integer.parseInt(p2)) + (cursorRate[3] * Integer.parseInt(p4))));
            }
            //Only p2,p3
            else if (correctInput[0] != 1 && correctInput[1] == 1 && correctInput[2] == 1 && correctInput[3] != 1) {
                p2 = inputP2.getText().toString().trim();
                p3 = inputP3.getText().toString().trim();
                runtimeSuggestionAmountToGive.setText(String.valueOf((cursorRate[1] * Integer.parseInt(p2)) + (cursorRate[2] * Integer.parseInt(p3))));
            }
            //only p2
            else if (correctInput[0] != 1 && correctInput[1] == 1 && correctInput[2] != 1 && correctInput[3] != 1) {
                p2 = inputP2.getText().toString().trim();
                runtimeSuggestionAmountToGive.setText(String.valueOf(cursorRate[1] * Integer.parseInt(p2)));
            }
            //only p3
            else if (correctInput[0] != 1 && correctInput[1] != 1 && correctInput[2] == 1 && correctInput[3] != 1) {
                p3 = inputP3.getText().toString().trim();
                runtimeSuggestionAmountToGive.setText(String.valueOf(cursorRate[2] * Integer.parseInt(p3)));
            }
            //only p4
            else if (correctInput[0] != 1 && correctInput[1] != 1 && correctInput[2] != 1 && correctInput[3] == 1) {
                p4 = inputP4.getText().toString().trim();
                runtimeSuggestionAmountToGive.setText(String.valueOf(cursorRate[3] * Integer.parseInt(p4)));
            }
            //if any wrong data then this will execute
            if (correctInput[0] == 2 || correctInput[1] == 2 || correctInput[2] == 2 || correctInput[3] == 2) {
                runtimeSuggestionAmountToGive.setText("0");
            }
        }catch (Exception x){
            x.printStackTrace();
            runtimeSuggestionAmountToGive.setText("0");
        }
    }
    public static boolean copyTextToClipBoard(String message,Context context) {
        if(message==null){
            return false;
        }
        try{
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label",message);
            clipboard.setPrimaryClip(clip);
            return true;
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }
    }
    public static String[] getPersonDetailsForRunningPDFInvoice(String id,Context context) {
       // try (Database db=new Database(context);
        try (Database db=Database.getInstance(context);
             Cursor cursor1 = db.getData("SELECT " + Database.COL_2_NAME +" FROM " + Database.TABLE_NAME1 + " WHERE "+Database.COL_1_ID+"='" + id + "'")){
            if (cursor1 != null){
                cursor1.moveToFirst();

                int pdfSequenceNo=MyUtility.getPdfSequence(id,context);
                if(pdfSequenceNo != -1){//if -1 means error
                    pdfSequenceNo = pdfSequenceNo+1;
                }
//                else {
//                    pdfSequenceNo=-1;//if errro
//                }
//                if (cursor2 != null) {
//                    cursor2.moveToFirst();
//                    pdfSequenceNo =(cursor2.getInt(0) + 1);//pdf sequence in db is updated and since it is for future invoice number so for now increasing manually
//                } else {
//                    pdfSequenceNo = -1;
//                }
                return new String[]{"NAME: "+((cursor1.getString(0)!=null)?cursor1.getString(0):""),"ID: "+id,"RUNNING  INVOICE NO. "+pdfSequenceNo,"CREATED ON: "+MyUtility.get12hrCurrentTimeAndDate()};
            }else{
                return new String[]{"[NULL NO DATA IN CURSOR]",id,"[NULL NO DATA IN CURSOR]","[NULL NO DATA IN CURSOR]"};//no value present in db
            }
        }catch (Exception ex){
            ex.printStackTrace();
             return new String[]{"ERROR",id,"ERROR","ERROR"};//to avoid error
        }
    }
    public static String[] getTotalOfWagesAndWorkingDaysFromDbBasedOnIndicator(byte indicator,int[] arrayOfTotalWagesDepositRateAccordingToIndicator,Context context) {// when no data and if error errorDetection will be set to true
        try{//getTotalOfWagesAndWorkingDaysFromDbBasedOnIndicator should be use after all wages displayed
            switch (indicator) {
                case 1: return new String[]{"+",MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[0]), arrayOfTotalWagesDepositRateAccordingToIndicator[1]+"" ,context.getResources().getString(R.string.star_total_star)};

                case 2: return new String[]{"+", MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[0]), arrayOfTotalWagesDepositRateAccordingToIndicator[1]+"",arrayOfTotalWagesDepositRateAccordingToIndicator[2]+"" ,context.getResources().getString(R.string.star_total_star)};

                case 3: return new String[]{"+", MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[0]), arrayOfTotalWagesDepositRateAccordingToIndicator[1]+"",arrayOfTotalWagesDepositRateAccordingToIndicator[2]+"",arrayOfTotalWagesDepositRateAccordingToIndicator[3]+"" ,context.getResources().getString(R.string.star_total_star)};

                case 4: return new String[]{"+", MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[0]), arrayOfTotalWagesDepositRateAccordingToIndicator[1]+"",arrayOfTotalWagesDepositRateAccordingToIndicator[2]+"",arrayOfTotalWagesDepositRateAccordingToIndicator[3]+"",arrayOfTotalWagesDepositRateAccordingToIndicator[4]+"" ,context.getResources().getString(R.string.star_total_star)};
            }
            return new String[]{"wrong indicator"};//this code will not execute due to return in switch block just using to avoid error
        }catch (Exception ex){
            ex.printStackTrace();
            // errorDetection[0]=true;//indicate error has occur
            return new String[]{"error occurred"};//to avoid error
        }
    }
    public static String getOtherDetails(String id,Context context){//which ever data is not present that column data is not included
        try(Database db=  Database.getInstance(context);
            Cursor cursor1 = db.getData("SELECT " +Database.COL_6_AADHAAR_NUMBER +","+Database.COL_17_LOCATION+","+Database.COL_18_RELIGION+ " FROM " + Database.TABLE_NAME1 + " WHERE "+Database.COL_1_ID+"='" + id + "'");
            Cursor cursor2 = db.getData("SELECT " +Database.COL_392_LEAVINGDATE+","+Database.COL_398_RETURNINGDATE+","+Database.COL_397_TOTAL_WORKED_DAYS+","+Database.COL_391_STAR +","+Database.COL_32_R1+","+Database.COL_33_R2+","+Database.COL_34_R3+","+Database.COL_35_R4+","+Database.COL_393_PERSON_REMARKS+" FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE "+Database.COL_1_ID+"='" + id + "'")){
            String skills[]=db.getAllSkill(id);

            StringBuilder sb=new StringBuilder();
            if(cursor1 != null && cursor1.moveToFirst()){
                sb.append(!TextUtils.isEmpty(cursor1.getString(0)) ?("AADHAAR NO: " + cursor1.getString(0)+"\n") : "")//!TextUtils.isEmpty() checks for null and ""
                        .append(!TextUtils.isEmpty(cursor1.getString(1)) ? ("LOCATION: " + cursor1.getString(1)+" , ") : "")
                        .append(!TextUtils.isEmpty(cursor1.getString(2)) ? ("RELIGION: " + cursor1.getString(2)+"\n") :"");
            }

            if(cursor2 != null && cursor2.moveToFirst()){
                sb.append(!TextUtils.isEmpty(cursor2.getString(0)) ? ("LEAVING DATE: " + cursor2.getString(0)+" , ") : "")
                        .append(!TextUtils.isEmpty(cursor2.getString(1)) ? ("RETURN DATE: " + cursor2.getString(1)+"\n") : "")
                        .append(!TextUtils.isEmpty(cursor2.getString(2)) ? ("TOTAL WORKED DAYS: " + cursor2.getString(2)+" , ") : "")
                        .append(!TextUtils.isEmpty(cursor2.getString(3)) ? ("STAR: " + cursor2.getString(3)+"\n\n") : "");

            }
            switch (MyUtility.get_indicator(context,id)){
                case 1:{
                    sb.append(skills[0]).append(":RATE "+(!TextUtils.isEmpty(cursor2.getString(4))?cursor2.getString(4):"0")).append("\n");
                }break;
                case 2:{
                    sb.append(skills[0]).append(":RATE "+(!TextUtils.isEmpty(cursor2.getString(4))?cursor2.getString(4):"0")).append(" , "+skills[1]).append(":RATE "+(!TextUtils.isEmpty(cursor2.getString(5))?cursor2.getString(5):"0")).append("\n");
                }break;
                case 3:{
                    sb.append(skills[0]).append(":RATE "+(!TextUtils.isEmpty(cursor2.getString(4))?cursor2.getString(4):"0")).append(" , "+skills[1]).append(":RATE "+(!TextUtils.isEmpty(cursor2.getString(5))?cursor2.getString(5):"0")).append(" , "+skills[2]).append(":RATE "+(!TextUtils.isEmpty(cursor2.getString(6))?cursor2.getString(6):"0")).append("\n");
                }break;
                case 4:{
                    sb.append(skills[0]).append(":RATE "+(!TextUtils.isEmpty(cursor2.getString(4))?cursor2.getString(4):"0")).append(" , "+skills[1]).append(":RATE "+(!TextUtils.isEmpty(cursor2.getString(5))?cursor2.getString(5):"0")).append(" , "+skills[2]).append(":RATE "+(!TextUtils.isEmpty(cursor2.getString(6))?cursor2.getString(6):"0")).append(" , "+skills[3]).append(":RATE "+(!TextUtils.isEmpty(cursor2.getString(7))?cursor2.getString(7):"0")).append("\n");
                }break;
            }
            sb.append(!TextUtils.isEmpty(cursor2.getString(8)) ? ("REMARKS: " + cursor2.getString(8)+"\n\n") : "");
            return sb.toString();

        }catch (Exception x){
            x.printStackTrace();
            return "error";
        }
    }
}
