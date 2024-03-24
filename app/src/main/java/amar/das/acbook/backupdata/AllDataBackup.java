package amar.das.acbook.backupdata;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.text.TextUtils;

import java.io.File;

import amar.das.acbook.Database;
import amar.das.acbook.R;
import amar.das.acbook.activity.PdfViewerOperationActivity;
import amar.das.acbook.globalenum.GlobalConstants;
import amar.das.acbook.pdfgenerator.MakePdf;
import amar.das.acbook.utility.MyUtility;

public class AllDataBackup{
    private Context context;
    private int numberOfPerson;
    private String backupFileName;
    public String createdAbsoluteFilePath;

    public AllDataBackup(Context context) {
        this.context = context;
    }

    public boolean backupMLGDataInPDFFormat(){
        Database db=Database.getInstance(context);
        String personIds[]=db.getIdOfActiveMLG(); if(personIds==null) return  false;//if error

        this.numberOfPerson=personIds.length;
        backupFileName =MyUtility.backupDateTime()+GlobalConstants.BACKUP_ACTIVE_MLG_PDF_NAME.getValue();

        MakePdf makePdf = new MakePdf();
        if (!makePdf.createPage1(MakePdf.defaultPageWidth, MakePdf.defaultPageHeight, 1)) return false;//created page 1
        if (!makePdf.writeSentenceWithoutLines(new String[]{""}, new float[]{100f}, false, (byte) 0, (byte) 0,true)) return false;//just for space
        if (!makePdf.writeSentenceWithoutLines(new String[]{"CREATED ON: "+MyUtility.get12hrCurrentTimeAndDate(),"BACKUP OF  "+numberOfPerson+"  ACTIVE  "+context.getResources().getString(R.string.mestre)+context.getResources().getString(R.string.laber)+context.getResources().getString(R.string.women_laber)+".  SORTED  ACCORDING TO  ID"}, new float[]{30f,70f}, true, (byte) 0, (byte) 0,true)) return false;
        if (!makePdf.writeSentenceWithoutLines(new String[]{""}, new float[]{100f}, false, (byte) 0, (byte) 0,true)) return false;//just for space


        for (String id:personIds){//loop
            if(!createActiveMLGInvoicePDF(id,makePdf)) return false;
        }

        if (!makePdf.createdPageFinish2()) return false;//after finish page we cannot write to it

        File pdfFile = makePdf.createFileToSavePdfDocumentAndReturnFile(context.getExternalFilesDir(null).toString(), backupFileName);//we have to return filename  view pdf using file path
        if(pdfFile == null) return false;//error

        createdAbsoluteFilePath=pdfFile.getAbsolutePath();//to delete this file from device
        if (!makePdf.closeDocumentLastOperation4()) return false;

        if (!MyUtility.shareFileToAnyApp(pdfFile,"application/pdf", GlobalConstants.BACKUP_ACTIVE_MLG_PDF_NAME.getValue()+" SHARE PDF USING",context)) return false;//open intent to share

        Database.closeDatabase();
        return true;//if everything goes fine
    }
    private boolean createActiveMLGInvoicePDF(String id, MakePdf makePdf){
        try {
            byte indicator =  MyUtility.get_indicator(context,id);
            boolean[] errorDetection = {false};//when ever exception occur in one place it will be updated to true in method.if no exception array will not be updated. so if any where error occur it will hold value true

            float[] columnWidth = PdfViewerOperationActivity.getColumnWidthBasedOnIndicator(indicator, errorDetection);
            int[] arrayOfTotalWagesDepositRateAccordingToIndicator= MyUtility.getSumOfTotalWagesDepositRateDaysWorkedBasedOnIndicator(context,id,indicator,errorDetection);//if error cause errorDetection will be set true
            String[] headerAccordingToIndicator = MyUtility.getWagesHeadersFromDbBasedOnIndicator(context,id, indicator, errorDetection);//THIS SHOULD BE TOP at arrayOfTotalWagesDepositRateAccordingToIndicator   TO AVOID INDEX EXCEPTION
            String[][] recyclerViewWagesData = MyUtility.getAllWagesDetailsFromDbBasedOnIndicator(context,id, indicator, errorDetection);//it  return null   when no data
            String[][] recyclerViewDepositData = MyUtility.getAllDepositFromDb(context,id, errorDetection);//it return null   when no data

            String personDetails[]=MyUtility.getPersonDetailsForRunningPDFInvoice(id,context);//id ,name,invoice number
            if(!makePdf.singleCustomRow(new String[]{personDetails[1],personDetails[0],personDetails[2]}, new float[]{10f, 66f, 24f},0,0,0,0,false,(byte)0,(byte)0)) return false;
            if(!makePdf.writeSentenceWithoutLines(new String[]{getPersonOtherDetails(id)},new float[]{100f}, false, (byte) 0, (byte) 0,true)) return false;//name,id,date,future invoice number,created date

            if (!errorDetection[0]){

                if(recyclerViewDepositData==null && recyclerViewWagesData==null ){//when no data
                    if(!makePdf.writeSentenceWithoutLines(new String[]{context.getResources().getString(R.string.no_data_present)},new float[]{100f}, false, (byte) 0, (byte) 0,true)) return false;//name,id,date,future invoice number,created date
                }

                if (recyclerViewDepositData != null) {//null means data not present
                    if(!makePdf.makeTable(new String[]{"DATE", "DEPOSIT", "REMARKS"}, recyclerViewDepositData, new float[]{10f, 12f, 78f}, 9, false)) return false;
                    if(!makePdf.singleCustomRow(new String[]{"+", MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[indicator + 1]),context.getResources().getString(R.string.star_total_width_star)}, Database.depositColumn(), 0, 0, 0, 0, true, (byte) 0, (byte) 0)) return false;//sub total
                }

                if (recyclerViewWagesData != null){//null means data not present
                    if(!makePdf.makeTable(headerAccordingToIndicator, recyclerViewWagesData, columnWidth, 9, false)) return false;
                    if(!makePdf.singleCustomRow(MyUtility.getTotalOfWagesAndWorkingDaysFromDbBasedOnIndicator(indicator,arrayOfTotalWagesDepositRateAccordingToIndicator,context), columnWidth, 0, Color.rgb(221, 133, 3), 0, 0, true, (byte) 0, (byte) 0)) return false;//sub total
                }

                 //adding empty row box for rough calculation
                //if(!makePdf.singleCustomRow(new String[]{""}, new float[]{100f} ,0,0,0,0,true, (byte) 0, (byte) 0))return false;
                if (!makePdf.writeSentenceWithoutLines(new String[]{""}, new float[]{100f}, true, (byte) 0, (byte) 0,true)) return false;//just for space
                if (!makePdf.writeSentenceWithoutLines(new String[]{""}, new float[]{100f}, false, (byte) 0, (byte) 0,true)) return false;//just for space
            }else return false; //if error occurred

        }catch (Exception x){
            x.printStackTrace();
            return false;
        }
        return true;
    }

    private String getPersonOtherDetails(String id) {
        StringBuilder sb=new StringBuilder();
        if(id=="92");
        String phoneNumber=MyUtility.getActiveOrBothPhoneNumber(id,context,false);
        if(!TextUtils.isEmpty(phoneNumber)){//checks for null or ""
            sb.append("PHONE: ").append(phoneNumber).append("\n");//phone number
        }
        String accountDetails= getAccountDetails(id);
        if(accountDetails!=null){
            sb.append(accountDetails).append("\n\n");//account details
        }
        sb.append(MyUtility.getOtherDetails(id,context));//other details like aadhaar,location,religion,total worked days etc
        return sb.toString();
    }
    private String getAccountDetails(String id) {//if error return null
       // try (Database db = new Database(context);
        try (Database db =Database.getInstance(context);
             Cursor cursor = db.getData("SELECT " + Database.COL_3_BANKAC + ", " + Database.COL_4_IFSCCODE + ", " + Database.COL_5_BANKNAME + ", " + Database.COL_9_ACCOUNT_HOLDER_NAME + " FROM " + Database.TABLE_NAME1 + " WHERE " + Database.COL_1_ID + "='" + id + "'")) {

            StringBuilder sb = new StringBuilder("\n");
            if (cursor != null && cursor.moveToFirst()) {
                //appear text if no data
//                sb.append("\nBANK NAME: ").append(cursor.getString(2) != null ? cursor.getString(2) : "").append("\n");
//                sb.append("A/C HOLDER NAME: ").append(cursor.getString(3) != null ? cursor.getString(3) : "").append("\n");
//                sb.append("A/C: ").append(cursor.getString(0) != null ? convertToReadableNumber(cursor.getString(0)) : "").append("\n");
//                sb.append("IFSC CODE: ").append(cursor.getString(1) != null ? cursor.getString(1) : "");

//                sb.append((cursor.getString(2) != null) ? "\nBANK NAME: "+cursor.getString(2) : "").append("\n");
//                sb.append((cursor.getString(3) != null) ? "A/C HOLDER NAME: "+cursor.getString(3) : "").append("\n");
//                sb.append((cursor.getString(0) != null) ? "A/C: "+convertToReadableNumber(cursor.getString(0)) : "").append("\n");
//                sb.append((cursor.getString(1) != null) ? "IFSC CODE: "+cursor.getString(1) : "");
//or
                if (!TextUtils.isEmpty(cursor.getString(2))){//TextUtils.isEmpty() checks for "" and null
                    sb.append("BANK NAME: ").append(cursor.getString(2)).append("\n");
                }
                if (!TextUtils.isEmpty(cursor.getString(3))) {
                    sb.append("A/C HOLDER NAME: ").append(cursor.getString(3)).append("\n");
                }
                if (!TextUtils.isEmpty(cursor.getString(0))) {
                    sb.append("A/C: ").append(convertToReadableNumber(cursor.getString(0))).append("\n");
                }
                if (!TextUtils.isEmpty(cursor.getString(1))) {
                    sb.append("IFSC CODE: ").append(cursor.getString(1));
                }
            }
            return sb.toString().trim();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    private String convertToReadableNumber(String number){//In this optimized code, we create a char array arr with an initial capacity equal to the length of the input string str, plus an extra capacity for the spaces that will be inserted (i.e., str.length() + str.length() / 4). We then loop through each character of the input string str, appending each character to arr using arr[j++] = str.charAt(i).After every fourth character (except for the last character in the string), we append a space character to arr using arr[j++] = ' '. We use an integer variable j to keep track of the next index in arr where a character should be inserted.
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
}
