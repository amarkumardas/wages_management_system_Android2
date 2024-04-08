package amar.das.acbook.backupdata;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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

    public AllDataBackup(Context context) {
        this.context = context;
    }
    public boolean backupInActiveMOrLOrGDataInTextFormat(String textFileName, String skillType){
        Database db=Database.getInstance(context);
        String[] personIds =db.getIdOfInActiveMOrLOrG(skillType); if(personIds==null) return  false;//if error

        this.numberOfPerson=personIds.length;
        backupFileName =MyUtility.backupDateTime()+textFileName;//GlobalConstants.BACKUP_INACTIVE_M_TEXT_FILE_NAME.getValue()
        StringBuilder sb=new StringBuilder();
        sb.append("CREATED ON: ").append(MyUtility.get12hrCurrentTimeAndDate()).append("\nBACKUP OF ").append(numberOfPerson).append(" INACTIVE PEOPLE SKILLED IN ( ").append(skillType).append(" ). SORTED  ACCORDING TO ID.\n\n");
        sb.append(getTotalInActiveAdvanceAndBalance(skillType)).append("\n-----------------------------\n\n");

        for (String id:personIds){//loop
            sb=createInActiveMOrLOrGInvoiceTextFile(id,sb);
            if(sb==null) return false;
        }

         if(!shareLargeDataAsTextFileToAnyApp(backupFileName,sb.toString(),"text/plain", "BACKUP INACTIVE SKILL "+skillType+" TEXT FILE",context)) return false;

        Database.closeDatabase();
        return true;//if everything goes fine
    }
    private String getTotalInActiveAdvanceAndBalance(String skillType) {
        Database db=Database.getInstance(context);
        String noRateIds=db.getIdOfSpecificSkillAndReturnNullIfRateIsProvidedOfActiveOrInactiveMLG(skillType,false);
        if(noRateIds!=null){//means no rate ids are there
            return "FOR CALCULATING TOTAL ADVANCE  AND  BALANCE  PLEASE  SET  RATE  TO  IDs: "+noRateIds;
        }

        try(Cursor cursor=db.getData("SELECT SUM("+Database.COL_13_ADVANCE+") , SUM("+Database.COL_14_BALANCE+") FROM "+Database.TABLE_NAME1+" WHERE "+Database.COL_8_MAINSKILL1 +"='"+skillType+"' AND "+Database.COL_12_ACTIVE+"='"+GlobalConstants.INACTIVE.getValue()+"'")){
            cursor.moveToFirst();
            StringBuilder sb = new StringBuilder();
            sb.append("BASED ON PREVIOUS CALCULATED RATE. TOTAL ADVANCE Rs: ")
                    .append(MyUtility.convertToIndianNumberSystem(cursor.getLong(0)))
                    .append(" , TOTAL BALANCE Rs: ")
                    .append(MyUtility.convertToIndianNumberSystem(cursor.getLong(1)));
            return sb.toString();
        }catch(Exception x){
            x.printStackTrace();
            return "error";
        }
    }
    private boolean shareLargeDataAsTextFileToAnyApp(String fileName,String message,String mimeType,String title,Context context){//if error return null
        if(message==null|| mimeType==null||title==null){
            return false;
        }
        try { // create a file to store the data
//            if(MyUtility.checkPermissionForReadAndWriteToExternalStorage(context)){
                File file = new File(context.getExternalCacheDir(), fileName + ".txt");//creating txt file in cache directory file name  getExternalCacheDir() is a method in Android's Context class that returns a File object representing the external storage directory specific to your app for storing cache files. This directory is automatically created for your app and is private to your app, meaning that other apps cannot access its contents.Cache files are temporary files that are used to improve the performance of your app. By storing files that your app frequently uses in the cache directory, you can avoid repeatedly reading or downloading those files from a remote source, which can slow down your app's performance.The getExternalCacheDir() method returns a File object that represents the path to your app's external cache directory, which you can use to save cache files or other temporary files that your app needs to access quickly. For example, when sharing an image, you can save the image to this directory before sharing it with other apps.
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(message.getBytes());
                outputStream.close();
                // if (!shareFileToAnyApp(file.getAbsolutePath(), mimeType, title, sharePdfLauncher)) {//open intent to share
                if (!MyUtility.shareFileToAnyApp(file , mimeType, title,context)) {//open intent to share
                    Toast.makeText(context, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show();
                    return false;
                }
                //absolutePathArrayToDelete[3] = file.getAbsolutePath();//storing absolute path to delete the image
               // return file.getAbsolutePath();
                return true;
//            }else{
//                Toast.makeText(context, "EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show();
//                ActivityCompat.requestPermissions((Activity)context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 41);
//                return false;
//            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean backupActiveMLGDataInPDFFormat(){
        Database db=Database.getInstance(context);
        String personIds[]=db.getIdOfActiveMLG(); if(personIds==null) return  false;//if error

        this.numberOfPerson=personIds.length;
        backupFileName =MyUtility.backupDateTime()+GlobalConstants.BACKUP_ACTIVE_MLG_PDF_FILE_NAME.getValue();

        MakePdf makePdf = new MakePdf();
        if (!makePdf.createPage1(MakePdf.defaultPageWidth, MakePdf.defaultPageHeight, 1)) return false;//created page 1
        if (!makePdf.writeSentenceWithoutLines(new String[]{""}, new float[]{100f}, true, (byte) 0, (byte) 0,true)) return false;//just for space
        if (!makePdf.writeSentenceWithoutLines(getPdfCreatedInfo(numberOfPerson), new float[]{30f,70f}, true, (byte) 0, (byte) 0,true)) return false;
        if (!makePdf.writeSentenceWithoutLines(getTotalActiveAdvanceAndBalanceInfo(), new float[]{100f}, true, (byte) 0, (byte) 0,true)) return false;//just for space
        if (!makePdf.writeSentenceWithoutLines(new String[]{""}, new float[]{100f}, true, (byte) 0, (byte) 0,true)) return false;//just for space

        for (String id:personIds){//loop
            if(!createActiveMLGInvoicePDF(id,makePdf)) return false;
        }

        if (!makePdf.createdPageFinish2()) return false;//after finish page we cannot write to it

        File pdfFile = makePdf.createFileToSavePdfDocumentAndReturnFile(context.getExternalFilesDir(null).toString(), backupFileName);//we have to return filename  view pdf using file path
        if(pdfFile == null) return false;//means error

        if (!makePdf.closeDocumentLastOperation4()) return false;

        if (!MyUtility.shareFileToAnyApp(pdfFile,"application/pdf", context.getString(R.string.backup_active_m_l_g)+" PDF FILE",context)) return false;//open intent to share

        Database.closeDatabase();
        return true;//if everything goes fine
    }
    public String[] getPdfCreatedInfo(int numberOfPerson) {
        String[] backupInfo = new String[2];
        StringBuilder sb1 = new StringBuilder();
        sb1.append("CREATED ON: ").append(MyUtility.get12hrCurrentTimeAndDate());
        backupInfo[0] = sb1.toString();

        StringBuilder sb2 = new StringBuilder();
        sb2.append("BACKUP OF ").append(numberOfPerson)
                .append(" ACTIVE  PEOPLE  SKILLED  IN ( ")
                .append(context.getResources().getString(R.string.mestre)).append(" ")
                .append(context.getResources().getString(R.string.laber)).append(" ")
                .append(context.getResources().getString(R.string.women_laber)).append(" ")
                .append("). SORTED ACCORDING TO  ID");
        backupInfo[1] = sb2.toString();
        return backupInfo;
    }
    public String[] getTotalActiveAdvanceAndBalanceInfo() {
        String[] ratesInfo = new String[1];
        Database db=Database.getInstance(context);
        String noRateIds=db.getIdsAndReturnNullIfRateIsProvidedOfActiveMLG();
        if(noRateIds!=null){//means no rate ids are there
             ratesInfo[0] ="FOR CALCULATING  TOTAL  ADVANCE  AND  BALANCE  PLEASE  SET  RATE  TO  IDs: "+noRateIds;
             return ratesInfo;
        }
        StringBuilder sb = new StringBuilder();
        try(Cursor cursor=db.getData("SELECT SUM("+Database.COL_13_ADVANCE+") , SUM("+Database.COL_14_BALANCE+") FROM "+Database.TABLE_NAME1+" WHERE ("+Database.COL_8_MAINSKILL1 +"='"+context.getResources().getString(R.string.mestre)+"' OR "+Database.COL_8_MAINSKILL1 +"='"+context.getResources().getString(R.string.laber)+"' OR "+Database.COL_8_MAINSKILL1 +"='"+context.getResources().getString(R.string.women_laber)+"') AND "+Database.COL_12_ACTIVE+"='"+ GlobalConstants.ACTIVE.getValue()+"'")){
            cursor.moveToFirst();
            sb.append("BASED ON PREVIOUS CALCULATED RATE. TOTAL ADVANCE  Rs: ")
                    .append(MyUtility.convertToIndianNumberSystem(cursor.getLong(0)))
                    .append(" , TOTAL BALANCE  Rs: ")
                    .append(MyUtility.convertToIndianNumberSystem(cursor.getLong(1)));

            ratesInfo[0] = sb.toString();
            return ratesInfo;
        }catch (Exception x){
            x.printStackTrace();
            return new String[]{"error"};
        }
    }
//    private String getTotalActiveMLGAdvanceAndBalance(boolean forAdvanceTrueForBalanceFalse) {
//        Database db=Database.getInstance(context);
//        Cursor cursor=null;
//        try{
//        if(forAdvanceTrueForBalanceFalse){
//            cursor=db.getData("SELECT SUM("+Database.COL_13_ADVANCE+") FROM "+Database.TABLE_NAME1+" WHERE ("+Database.COL_8_MAINSKILL1 +"='"+context.getResources().getString(R.string.mestre)+"' OR "+Database.COL_8_MAINSKILL1 +"='"+context.getResources().getString(R.string.laber)+"' OR "+Database.COL_8_MAINSKILL1 +"='"+context.getResources().getString(R.string.women_laber)+"') AND "+Database.COL_12_ACTIVE+"='"+ GlobalConstants.ACTIVE.getValue()+"'");
//            cursor.moveToFirst();
//            return MyUtility.convertToIndianNumberSystem(cursor.getLong(0));
//        }else{
//            cursor=db.getData("SELECT SUM("+Database.COL_14_BALANCE+") FROM "+Database.TABLE_NAME1+" WHERE ("+Database.COL_8_MAINSKILL1 +"='"+context.getResources().getString(R.string.mestre)+"' OR "+Database.COL_8_MAINSKILL1 +"='"+context.getResources().getString(R.string.laber)+"' OR "+Database.COL_8_MAINSKILL1 +"='"+context.getResources().getString(R.string.women_laber)+"') AND "+Database.COL_12_ACTIVE+"='"+ GlobalConstants.ACTIVE.getValue()+"'");
//            cursor.moveToFirst();
//            return MyUtility.convertToIndianNumberSystem(cursor.getLong(0));
//        }
//
//        }catch(Exception x){
//            x.printStackTrace();
//            return "error";
//        }finally {
//            if(cursor!=null)cursor.close();
//        }
//    }
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
            if(!makePdf.writeSentenceWithoutLines(new String[]{getPhoneAccountOtherDetailsIfDataIsNotNull(id)},new float[]{100f}, false, (byte) 0, (byte) 0,true)) return false;//name,id,date,future invoice number,created date

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
    private StringBuilder createInActiveMOrLOrGInvoiceTextFile(String id,StringBuilder sb) {
        try{
            String personDetails[]=MyUtility.getPersonDetailsForRunningPDFInvoice(id,context);//id ,name,invoice number
            sb.append(personDetails[1]).append(" , ").append(personDetails[0]).append(" , ").append(personDetails[2]).append("\n")
              .append(getPhoneAccountOtherDetailsIfDataIsNotNull(id))//other details like aadhaar,location,religion,total worked days etc
              .append(PdfViewerOperationActivity.getAllSumAndDepositAndWagesDetails(id,context))//all wages and deposit data
              .append("------------FINISH--------------\n\n");
            return sb;
        }catch (Exception x){
            x.printStackTrace();
            return null;
        }
    }
    private String getPhoneAccountOtherDetailsIfDataIsNotNull(String id) {
        StringBuilder sb=new StringBuilder();
        String phoneNumber=MyUtility.getActiveOrBothPhoneNumber(id,context,false);
        if(!TextUtils.isEmpty(phoneNumber)){//checks for null or ""
            sb.append("PHONE: ").append(phoneNumber).append("\n");//phone number
        }
        String accountDetails= getAccountDetailsIfDataIsNotNull(id);
        if(accountDetails!=null){
            sb.append(accountDetails).append("\n\n");//account details
        }
        sb.append(MyUtility.getOtherDetails(id,context));//other details like aadhaar,location,religion,total worked days etc
        return sb.toString();
    }
    private String getAccountDetailsIfDataIsNotNull(String id) {//if error return null
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
                    sb.append("A/C: ").append(MyUtility.convertToReadableNumber(cursor.getString(0))).append("\n");
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
}
