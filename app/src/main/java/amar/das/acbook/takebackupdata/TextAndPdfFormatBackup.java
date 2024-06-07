package amar.das.acbook.takebackupdata;

import android.content.Context;
import android.graphics.Color;

import java.io.File;

import amar.das.acbook.Database;
import amar.das.acbook.R;
import amar.das.acbook.activity.PdfViewerOperationActivity;
import amar.das.acbook.pdfgenerator.MakePdf;
import amar.das.acbook.utility.BackupDataUtility;
import amar.das.acbook.utility.MyUtility;

public class TextAndPdfFormatBackup {
    private Context context;
    public TextAndPdfFormatBackup(Context context) {
        this.context = context;
    }
    public String singleBackupTextFile( ){//if error return null
        StringBuilder sb=new StringBuilder();
        String personIds[];
        byte numberOfFiles=4;
        String createInfoAndAdvanceAndBalanceData=null;
        for (byte fileIndicator = 0; fileIndicator < numberOfFiles; fileIndicator++){

            if((personIds=BackupDataUtility.getPersonIdsAccordingToFileIndicator(fileIndicator,context)) == null) return null;//means error

            createInfoAndAdvanceAndBalanceData=createdInfoAndTotalAdvanceAndBalanceForSingleBackupTextFile(personIds.length,fileIndicator).toString();

            if(createInfoAndAdvanceAndBalanceData != null){
                sb.append(createInfoAndAdvanceAndBalanceData);
            }else return  null;//means error

            for (String id:personIds){//loop

                if((sb=getDataOfId(id,sb)) == null) return  null;//this method fetch data according to id weather it is active or inactive
            }
        }
        return sb.toString();//if everything goes fine
    }
    private StringBuilder createdInfoAndTotalAdvanceAndBalanceForSingleBackupTextFile(int numberOfPerson,byte fileIndicator){
        StringBuilder sb=new StringBuilder();
        String[] array=null;
        switch (fileIndicator) {
            case 0:{
                array=BackupDataUtility.getActiveSkillMLGCreatedInfo(numberOfPerson,context);
                sb.append(array[0]).append("\n").append(array[1]).append("\n\n");
                sb.append(BackupDataUtility.getTotalActiveMLGAdvanceAndBalanceInfo(context)[0]).append("\n-----------------------------\n\n");
                return sb;
            }
            case 1: {
                array=BackupDataUtility.getInActiveSkillCreatedInfo(numberOfPerson,context.getString(R.string.mestre));
                sb.append(array[0]).append("\n").append(array[1]).append("\n\n");
                sb.append(BackupDataUtility.getTotalInActiveMOrLOrGAdvanceAndBalanceInfo(context,context.getString(R.string.mestre))[0]).append("\n-----------------------------\n\n");
                return sb;
            }
            case 2: {
                array=BackupDataUtility.getInActiveSkillCreatedInfo(numberOfPerson,context.getString(R.string.laber));
                sb.append(array[0]).append("\n").append(array[1]).append("\n\n");
                sb.append(BackupDataUtility.getTotalInActiveMOrLOrGAdvanceAndBalanceInfo(context,context.getString(R.string.laber))[0]).append("\n-----------------------------\n\n");
                return sb;
            }
            case 3: {
                array=BackupDataUtility.getInActiveSkillCreatedInfo(numberOfPerson,context.getString(R.string.women_laber));
                sb.append(array[0]).append("\n").append(array[1]).append("\n\n");
                sb.append(BackupDataUtility.getTotalInActiveMOrLOrGAdvanceAndBalanceInfo(context,context.getString(R.string.women_laber))[0]).append("\n-----------------------------\n\n");
                return sb;
            }
            default:return null;
        }
    }
    public File singleBackupPdfFile(String backupPdfFileName){//if error return null
        MakePdf makePdf = new MakePdf();
        if (!makePdf.createPage1(MakePdf.defaultPageWidth, MakePdf.defaultPageHeight, 1)) return null;//created page 1

        String personIds[];
        byte numberOfFiles=4;
        for (byte fileIndicator = 0; fileIndicator < numberOfFiles; fileIndicator++) {

           if((personIds= createdInfoAndTotalAdvanceAndBalanceForSingleBackupPdfFile(makePdf,fileIndicator)) == null) return null;//means error

            for (String id:personIds){//loop
                if(!createPDFOfId(id,makePdf)) return null;//createPDFOfSkillAccordingToId() method create pdf whether it is active or inactive
            }
        }

        if (!makePdf.createdPageFinish2()) return null;//after finish page we cannot write to it

        File pdfFile = makePdf.createPdfFileInExternalStorageAndReturnFile(context.getExternalFilesDir(null).toString(),backupPdfFileName);//we have to return filename  view pdf using file path
        if(pdfFile == null) return null;//means error

        if (!makePdf.closeDocumentLastOperation4()) return null;

        return pdfFile;//if everything goes fine
    }
    private String[] createdInfoAndTotalAdvanceAndBalanceForSingleBackupPdfFile(MakePdf makePdf, byte fileIndicator) {//if error return null
        Database db=Database.getInstance(context);
        String personIds[]=null;
        String createInfo[]=null;
        String totalAdvanceAndBalance[]=null;

        if(fileIndicator==0){//for active mlg
            personIds=db.getIdOfActiveMLG(); if(personIds==null) return  null;//if error
            createInfo=BackupDataUtility.getActiveSkillMLGCreatedInfo(personIds.length,context);
            totalAdvanceAndBalance=BackupDataUtility.getTotalActiveMLGAdvanceAndBalanceInfo(context);
        }else if (fileIndicator==1) {//for inactive skill m
            personIds=db.getIdOfInActiveMOrLOrG(context.getString(R.string.mestre)); if(personIds==null) return  null;//if error
            createInfo=BackupDataUtility.getInActiveSkillCreatedInfo(personIds.length,context.getString(R.string.mestre));
            totalAdvanceAndBalance=BackupDataUtility.getTotalInActiveMOrLOrGAdvanceAndBalanceInfo(context,context.getString(R.string.mestre));
        }else if (fileIndicator==2) {//for inactive skill l
            personIds=db.getIdOfInActiveMOrLOrG(context.getString(R.string.laber)); if(personIds==null) return  null;//if error
            createInfo=BackupDataUtility.getInActiveSkillCreatedInfo(personIds.length,context.getString(R.string.laber));
            totalAdvanceAndBalance=BackupDataUtility.getTotalInActiveMOrLOrGAdvanceAndBalanceInfo(context,context.getString(R.string.laber));
        }else if (fileIndicator==3) {//for inactive skill g
            personIds=db.getIdOfInActiveMOrLOrG(context.getString(R.string.women_laber)); if(personIds==null) return  null;//if error
            createInfo=BackupDataUtility.getInActiveSkillCreatedInfo(personIds.length,context.getString(R.string.women_laber));
            totalAdvanceAndBalance=BackupDataUtility.getTotalInActiveMOrLOrGAdvanceAndBalanceInfo(context,context.getString(R.string.women_laber));
        }

        if (!makePdf.writeSentenceWithoutLines(new String[]{""}, new float[]{100f}, true, (byte) 0, (byte) 0,true)) return null;//just for space

        if(!makePdf.singleCustomRow(createInfo, new float[]{30f,70f},Color.rgb(10, 178, 21),Color.rgb(10, 178, 21), 0, 0, true, (byte) 0, (byte) 0)) return null;
        //if (!makePdf.writeSentenceWithoutLines(createInfo, new float[]{30f,70f}, true, (byte) 0, (byte) 0,true)) return null;

        if (!makePdf.writeSentenceWithoutLines(totalAdvanceAndBalance, new float[]{100f}, false, (byte) 0, (byte) 0,true)) return null;//just for space
        if (!makePdf.writeSentenceWithoutLines(new String[]{""}, new float[]{100f}, true, (byte) 0, (byte) 0,true)) return null;//just for space
        return personIds;
    }
    public String backupInActiveMOrLOrGDataInTextFormat(String skillType){
        Database db=Database.getInstance(context);
        String[] personIds =db.getIdOfInActiveMOrLOrG(skillType); if(personIds==null) return  null;//if error

        //this.numberOfPerson=personIds.length;
        StringBuilder sb=new StringBuilder();
        String[] array=BackupDataUtility.getInActiveSkillCreatedInfo(personIds.length,skillType);
        sb.append(array[0]).append("\n").append(array[1]).append("\n\n");

        //sb.append("CREATED ON: ").append(MyUtility.get12hrCurrentTimeAndDate()).append("\nBACKUP OF ").append(personIds.length).append(" INACTIVE PEOPLE SKILLED IN ( ").append(skillType).append(" ). SORTED  ACCORDING TO ID.\n\n");
        sb.append(BackupDataUtility.getTotalInActiveMOrLOrGAdvanceAndBalanceInfo(context,skillType)[0]).append("\n-----------------------------\n\n");

        for (String id:personIds){//loop
            sb= getDataOfId(id,sb);
            if(sb==null) return null;
        }
        Database.closeDatabase();
        return sb.toString();//if everything goes fine
    }
    public File backupInActiveMOrLOrGDataInPDFFormat(String backupPdfFileName,String skillType){//if error return null
        Database db=Database.getInstance(context);
        String personIds[]=db.getIdOfInActiveMOrLOrG(skillType); if(personIds==null) return  null;//if error

        MakePdf makePdf = new MakePdf();
        if (!makePdf.createPage1(MakePdf.defaultPageWidth, MakePdf.defaultPageHeight, 1)) return null;//created page 1
        if (!makePdf.writeSentenceWithoutLines(new String[]{""}, new float[]{100f}, true, (byte) 0, (byte) 0,true)) return null;//just for space

        if(!makePdf.singleCustomRow(BackupDataUtility.getInActiveSkillCreatedInfo(personIds.length,skillType),new float[]{30f,70f},Color.rgb(10, 178, 21),Color.rgb(10, 178, 21), 0, 0, true, (byte) 0, (byte) 0)) return null;
        //if (!makePdf.writeSentenceWithoutLines(BackupDataUtility.getInActiveSkillCreatedInfo(personIds.length,skillType), new float[]{30f,70f}, true, (byte) 0, (byte) 0,true)) return null;

        if (!makePdf.writeSentenceWithoutLines(BackupDataUtility.getTotalInActiveMOrLOrGAdvanceAndBalanceInfo(context,skillType), new float[]{100f}, false, (byte) 0, (byte) 0,true)) return null;//just for space
        if (!makePdf.writeSentenceWithoutLines(new String[]{""}, new float[]{100f}, true, (byte) 0, (byte) 0,true)) return null;//just for space

        for (String id:personIds){//loop
            if(!createPDFOfId(id,makePdf)) return null;//this createPDFOfSkillAccordingToId() method create pdf whether it is active or inactive
        }

        if (!makePdf.createdPageFinish2()) return null;//after finish page we cannot write to it

        File pdfFile = makePdf.createPdfFileInExternalStorageAndReturnFile(context.getExternalFilesDir(null).toString(),backupPdfFileName);//we have to return filename  view pdf using file path
        if(pdfFile == null) return null;//means error

        if (!makePdf.closeDocumentLastOperation4()) return null;

        Database.closeDatabase();
        return pdfFile;//if everything goes fine
    }
    public File backupActiveMLGDataInPDFFormat(String backupPdfFileName){//if error return null
        Database db=Database.getInstance(context);
        String personIds[]=db.getIdOfActiveMLG(); if(personIds==null) return  null;//if error

        MakePdf makePdf = new MakePdf();
        if (!makePdf.createPage1(MakePdf.defaultPageWidth, MakePdf.defaultPageHeight, 1)) return null;//created page 1
        if (!makePdf.writeSentenceWithoutLines(new String[]{""}, new float[]{100f}, true, (byte) 0, (byte) 0,true)) return null;//just for space

        if(!makePdf.singleCustomRow(BackupDataUtility.getActiveSkillMLGCreatedInfo(personIds.length,context),new float[]{30f,70f},Color.rgb(10, 178, 21),Color.rgb(10, 178, 21), 0, 0, true, (byte) 0, (byte) 0)) return null;
        //if (!makePdf.writeSentenceWithoutLines(BackupDataUtility.getActiveSkillCreatedInfo(personIds.length,context), new float[]{30f,70f}, true, (byte) 0, (byte) 0,true)) return null;

        if (!makePdf.writeSentenceWithoutLines(BackupDataUtility.getTotalActiveMLGAdvanceAndBalanceInfo(context), new float[]{100f}, false, (byte) 0, (byte) 0,true)) return null;//just for space
        if (!makePdf.writeSentenceWithoutLines(new String[]{""}, new float[]{100f}, true, (byte) 0, (byte) 0,true)) return null;//just for space

        for (String id:personIds){//loop
            if(!createPDFOfId(id,makePdf)) return null;//this createPDFOfSkillAccordingToId() method create pdf whether it is active or inactive
        }

        if (!makePdf.createdPageFinish2()) return null;//after finish page we cannot write to it

        File pdfFile = makePdf.createPdfFileInExternalStorageAndReturnFile(context.getExternalFilesDir(null).toString(),backupPdfFileName);//we have to return filename  view pdf using file path
        if(pdfFile == null) return null;//means error

        if (!makePdf.closeDocumentLastOperation4()) return null;

        Database.closeDatabase();
        return pdfFile;//if everything goes fine
    }
    public String backupActiveMLGDataInTextFormat(){//if error return null
        Database db=Database.getInstance(context);
        String personIds[]=db.getIdOfActiveMLG(); if(personIds==null) return  null;//if error

        StringBuilder sb=new StringBuilder();
        String[] array=BackupDataUtility.getActiveSkillMLGCreatedInfo(personIds.length,context);
        sb.append(array[0]).append("\n").append(array[1]).append("\n\n");

//        array=BackupDataUtility.getTotalActiveAdvanceAndBalanceInfo(context);
//        for (String str:array) {
//            sb.append(str);
//        }
        sb.append(BackupDataUtility.getTotalActiveMLGAdvanceAndBalanceInfo(context)[0]).append("\n-----------------------------\n\n");

        for (String id:personIds){//loop
            if((sb= getDataOfId(id,sb)) == null) return null;//this method fetch data according to id weather it is active or inactive
            //if(sb==null) return null;
        }
         Database.closeDatabase();
        return sb.toString();//if everything goes fine
    }
    private boolean createPDFOfId(String id, MakePdf makePdf){////this createPDFOfSkillAccordingToId() method create pdf whether it is active or inactive
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
            if(!makePdf.writeSentenceWithoutLines(new String[]{BackupDataUtility.getPhoneAccountOtherDetailsIfDataIsNotNull(id,context)},new float[]{100f}, false, (byte) 0, (byte) 0,true)) return false;//name,id,date,future invoice number,created date

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
    private StringBuilder getDataOfId(String id, StringBuilder sb) {//this method fetch data according to id weather it is active or inactive
        try{
            String personDetails[]=MyUtility.getPersonDetailsForRunningPDFInvoice(id,context);//id ,name,invoice number
            sb.append(personDetails[1]).append(" , ").append(personDetails[0]).append(" , ").append(personDetails[2]).append("\n")
              .append(BackupDataUtility.getPhoneAccountOtherDetailsIfDataIsNotNull(id,context))//other details like aadhaar,location,religion,total worked days etc
              .append(PdfViewerOperationActivity.getAllSumAndDepositAndWagesDetails(id,context))//all wages and deposit data
              .append("------------FINISH--------------\n\n");
            return sb;
        }catch (Exception x){
            x.printStackTrace();
            return null;
        }
    }
}
