package amar.das.acbook.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import amar.das.acbook.PersonRecordDatabase;
import amar.das.acbook.R;
import amar.das.acbook.pdfgenerator.MakePdf;
import amar.das.acbook.utility.MyUtility;

public class FinalPdfViewer extends AppCompatActivity {
    PDFView pdfView;
    Button pdf1, pdf2, downloadPdf, sharePdf,currentPdf;
    File absolutePathPdf;
    byte whichPdfIndicator;
    String fromIntentPersonId;
    byte [] bytepdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0); //we have used overridePendingTransition(), it is used to remove activity create animation while re-creating activity.
        setContentView(R.layout.activity_pdf_viewer);
        initializeIds();
        whichPdfIndicator = getIntent().getByteExtra("pdf1orpdf2",(byte) 0);
        fromIntentPersonId = getIntent().getStringExtra("ID");
        onlyViewPdf(whichPdfIndicator,fromIntentPersonId);


        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();//StrictMode is a developer tool which detects things you might be doing by accident and brings them to your attention so you can fix them.
        StrictMode.setVmPolicy(builder.build());

        downloadPdf.setOnClickListener(view -> {
            if(whichPdfIndicator ==(byte)1){

            } else if(whichPdfIndicator == (byte)2){

            }else if(whichPdfIndicator ==(byte) 3){

            } else
                Toast.makeText(this, "WRONG PDF INDICATOR", Toast.LENGTH_LONG).show();
        });
        sharePdf.setOnClickListener(view -> {
            if (whichPdfIndicator == (byte)1) {

            }else if (whichPdfIndicator ==(byte) 2) {
                String absolutePathPdf=convertBytesToFileForSharingAndReturnAbsolutePath(getPdfByteFromDb((byte) 2,fromIntentPersonId),fromIntentPersonId);
                     if(absolutePathPdf!=null){
                         Intent intentShare = new Intent(Intent.ACTION_SEND);
                         intentShare.setType("application/pdf");
                         Uri uri= FileProvider.getUriForFile(this,getApplicationContext().getPackageName()+".provider", new File(absolutePathPdf));//**to access file uri FileProvider.getUriForFile() is compulsory from if your target sdk version is 24 or greater otherwise cannot access
                         intentShare.putExtra(Intent.EXTRA_STREAM, uri);
                         startActivity(Intent.createChooser(intentShare, "Share the file ..."));
                     }else {
                         Toast.makeText(this, "FILE CANNOT BE SHARED", Toast.LENGTH_SHORT).show();
                     }
                     //deletePdfFromDevice(absolutePathPdf);
            }
            else if (whichPdfIndicator == (byte)3) {

            }else
                Toast.makeText(this, "WRONG PDF INDICATOR", Toast.LENGTH_LONG).show();
        });
        pdf1.setOnClickListener(view -> {
            whichPdfIndicator = (byte)1;//this will be updated when user click pdf1 for share and download
            if(!onlyViewPdf(whichPdfIndicator,fromIntentPersonId)){
                Toast.makeText(this, "CANNOT VIEW INVOICE1 ERROR OCCURRED", Toast.LENGTH_SHORT).show();
            }
        });
        pdf2.setOnClickListener(view -> {
            whichPdfIndicator =(byte)2;//this will be updated when user click pdf2 for share and download
            if(!onlyViewPdf(whichPdfIndicator,fromIntentPersonId)){
                Toast.makeText(this, "CANNOT VIEW INVOICE2 ERROR OCCURRED", Toast.LENGTH_SHORT).show();
            }
        });
        currentPdf.setOnClickListener(view -> {
            whichPdfIndicator = (byte)3;//this will be updated when user click current pdf for share and download
            if(!onlyViewPdf(whichPdfIndicator,fromIntentPersonId)){
                Toast.makeText(this, "CANNOT VIEW CURRENT INVOICE ERROR OCCURRED", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private boolean onlyViewPdf(byte whichPdfIndicator1or2,String id) {
       if(!(whichPdfIndicator1or2 >=1 && whichPdfIndicator1or2 <=3)){//if indicator is other then 1 or 2 or 3 then show wrong pdf indicator
           Log.d("FinalPdfViewer","wrong pdf indicator");
            return false;
        }
        try{
          if(whichPdfIndicator1or2!=3){
              pdfView.fromBytes(getPdfByteFromDb(whichPdfIndicator1or2,id)).load();//if this getPdfByteFromDb method return null then dialog message will be displayed and not cause error due to tyr catch block
          }else {//display current pdf ie.3
             if(!displayCurrentInvoice(id)) return false;
          }
        }catch (Exception e){
            e.printStackTrace();
            Log.d("FinalPdfViewer","cannot view pdf");
            return false;
        }
        return true;
}
public byte[] getPdfByteFromDb(byte whichPdfIndicator,String id){//return null when no data
        if(!(whichPdfIndicator >=1 && whichPdfIndicator <=2)){//if indicator is other then 1 or 2 then show wrong pdf indicator
            Log.d("FinalPdfViewer","wrong pdf indicator");
            return null;
        }
        Cursor cursor=null;
        try(PersonRecordDatabase db = new PersonRecordDatabase(getApplicationContext())) {
        switch(whichPdfIndicator){
            case 1:{
                cursor = db.getData("SELECT " + PersonRecordDatabase.COL_394_INVOICE1 + " FROM " + db.TABLE_NAME3 + " WHERE ID= '" + id + "'");
                cursor.moveToFirst();
                if (cursor.getBlob(0) != null) {
                    return cursor.getBlob(0);
                }else{
                    displDialogMessage("NO PREVIOUS INVOICE", "IT WILL BE AVAILABLE WHEN AGAIN CALCULATION IS DONE");
                }
            }
            case 2:{
                cursor = db.getData("SELECT " + PersonRecordDatabase.COL_395_INVOICE2 + " FROM " + db.TABLE_NAME3 + " WHERE ID= '" + id + "'");
                cursor.moveToFirst();
                if (cursor.getBlob(0) != null) {
                   return cursor.getBlob(0);
                }else{
                    displDialogMessage("NO PREVIOUS INVOICE", "IT WILL BE AVAILABLE WHEN AGAIN CALCULATION IS DONE");
                }
            }
        }
        }catch (Exception e){
             e.printStackTrace();
            return null;
        }finally {
            if(cursor!=null){
                cursor.close();
            }
        }
    return null;//if no switch case match then it will return null
}
    public boolean displayCurrentInvoice(String id) throws IOException {
            String currentInvoiceAbsolutePath;
            currentInvoiceAbsolutePath= createCurrentInvoiceAndReturnAbsolutePath(id);
            if(currentInvoiceAbsolutePath!=null){
                //pdfView.fromBytes(Files.readAllBytes(Paths.get(currentInvoiceAbsolutePath))).load();//viewing file from Absolute path
                pdfView.fromFile(new File(currentInvoiceAbsolutePath)).load();

                return true;
            }else{
                return false;
            }
    }

    public String createCurrentInvoiceAndReturnAbsolutePath(String id) {//return null when error
        byte indicator=(byte)get_indicator(id);
        boolean[] errorDetection={false};//when ever exception occur it will be updated to true in method so it indicate error occured or not
        String absoluteFilePath;
        MakePdf makePdf=new MakePdf();
        if(!makePdf.createPage1(MakePdf.defaultPageWidth, MakePdf.defaultPageHeight, 1)) return null;//created page 1

        String[] headerAccordingToIndicator = getWagesHeadersFromDbBasedOnIndicator(id, indicator, errorDetection);//THIS SHOULD BE TOP at arrayOfTotalWagesDepositRateAccordingToIndicator   TO AVOID INDEX EXCEPTION
        String[][] recyclerViewWagesdata = getAllWagesDetailsFromDbBasedOnIndicator(id, indicator, errorDetection);//it amy return null   when no data
        String[][] recyclerViewDepositdata = getAllDepositFromDb(id, errorDetection);//it amy return null   when no data

        if(!makePdf.writeSentenceWithoutLines(new String[]{""},new float[]{100f},false, (byte) 0, (byte) 0)) return null;//just for space

        if(!makePdf.writeSentenceWithoutLines(getPersonDetails(id),new float[]{40f,10f,20f,30f},true, (byte) 0, (byte) 0)) return null;//name,id,date,future invoice number

        if(errorDetection[0]==false) {
            switch (indicator) {
                case 1: {
                    if (recyclerViewWagesdata != null) {//null means data not present
                        if(!makePdf.makeTable(headerAccordingToIndicator, recyclerViewWagesdata, new float[]{12f, 12f, 5f, 71f}, 9, false))return null;
                    }
                    if (recyclerViewDepositdata != null) {//null means data not present
                        if (!makePdf.makeTable(new String[]{"DATE", "DEPOSIT", "REMARKS"}, recyclerViewDepositdata, new float[]{12f, 12f, 76f}, 9, true))return null;
                    }
                }break;
                case 2: {
                    if (recyclerViewWagesdata != null) {//null means data not present
                        if(!makePdf.makeTable(headerAccordingToIndicator, recyclerViewWagesdata, new float[]{12f, 12f, 5f, 5f, 66f}, 9, false)) return null;
                    }
                    if (recyclerViewDepositdata != null) {
                        if (!makePdf.makeTable(new String[]{"DATE", "DEPOSIT", "REMARKS"}, recyclerViewDepositdata, new float[]{12f, 12f, 76f}, 9, true)) return null;
                    }
                }break;
                case 3: {
                    if (recyclerViewWagesdata != null) {//null means data not present
                        if (!makePdf.makeTable(headerAccordingToIndicator, recyclerViewWagesdata, new float[]{12f, 12f, 5f, 5f, 5f, 61f}, 9, false)) return null;
                    }
                    if (recyclerViewDepositdata != null) {
                        if (!makePdf.makeTable(new String[]{"DATE", "DEPOSIT", "REMARKS"}, recyclerViewDepositdata, new float[]{12f, 12f, 76f}, 9, true)) return null;
                    }
                }break;
                case 4: {
                    if (recyclerViewWagesdata != null) {//null means data not present
                        if (!makePdf.makeTable(headerAccordingToIndicator, recyclerViewWagesdata, new float[]{12f, 12f, 5f, 5f, 5f, 5f, 56f}, 9, false)) return null;
                    }
                    if (recyclerViewDepositdata != null) {
                        if (!makePdf.makeTable(new String[]{"DATE", "DEPOSIT", "REMARKS"}, recyclerViewDepositdata, new float[]{12f, 12f, 76f}, 9, true)) return null;
                    }
                }break;
            }//switch
            if(!addRowsAndWriteToPDF(indicator,makePdf,38))return null;

            if(!makePdf.createdPageFinish2())return null;//after finish page we cannot write to it
            //while creating current pdf then its file name should be same because if user click on currentpdf button repeatedly then  many file will be create in device which is useless.so to avoid that file name is kept same so that whenever user click current pdf button then new file will be replaced with old file so it is necessary to keep same file name.if file name is unique then many file will be created in device
            absoluteFilePath = makePdf.createFileToSavePdfDocumentAndReturnFileAbsolutePath3(getExternalFilesDir(null).toString(), "id"+id+"currentInvoice");//we have to return filename  view pdf using file path
            if(!makePdf.closeDocumentLastOperation4())return null;
        }else return null;

        if(absoluteFilePath!=null){
            return absoluteFilePath;
        }else return null;
    }

    public boolean addRowsAndWriteToPDF(byte indicator, MakePdf makePdf, int numberOfrows) {
        switch(indicator){
            case 1:{for (int i = 0; i < numberOfrows; i++) {
                    if(!makePdf.singleCustomRow(new String[]{"","","",""}, new float[]{12f, 12f, 5f, 71f},0,0,0,0,true, (byte) 0, (byte) 0))return false;}
                   }break;
            case 2:{for (int i = 0; i < numberOfrows; i++) {
                     if(!makePdf.singleCustomRow(new String[]{"","","","",""},new float[]{12f, 12f, 5f, 5f, 66f},0,0,0,0,true, (byte) 0, (byte) 0))return false;}
                   }break;
            case 3:{for (int i = 0; i < numberOfrows; i++) {
                     if(!makePdf.singleCustomRow(new String[]{"","","","","",""}, new float[]{12f, 12f, 5f, 5f, 5f, 61f},0,0,0,0,true, (byte) 0, (byte) 0))return false;}
                   }break;
            case 4:{for (int i = 0; i < numberOfrows; i++) {
                     if(!makePdf.singleCustomRow(new String[]{"","","","","","",""},new float[]{12f, 12f, 5f, 5f, 5f, 5f, 56f},0,0,0,0,true, (byte) 0, (byte) 0))return false;}
                   }break;
        }
        return true;
    }
    public String[] getPersonDetails(String id) {
        try (PersonRecordDatabase db=new PersonRecordDatabase(getApplicationContext());
             Cursor cursor1 = db.getData("SELECT " + db.COL_2_NAME +" FROM " + db.TABLE_NAME1 + " WHERE ID='" + id + "'");
             Cursor cursor2 = db.getData("SELECT " + db.COL_396_PDFSEQUENCE + " FROM " + db.TABLE_NAME3 + " WHERE ID= '" + id + "'")){
            if (cursor1 != null){
                cursor1.moveToFirst();
                int pdfSequenceNo;
                if (cursor2 != null) {
                    cursor2.moveToFirst();
                    pdfSequenceNo =(cursor2.getInt(0) + 1);//pdf sequence in db is updated and since it is for future invoice number so for now increasing manually
                } else {
                    pdfSequenceNo = -1;
                }
                return new String[]{"NAME: "+cursor1.getString(0),"ID: "+id,"FUTURE  INVOICE NO. "+pdfSequenceNo,"CREATED ON: "+MyUtility.get12hrCurrentTimeAndDate()};
            }else{
                return new String[]{"[NULL NO DATA IN CURSOR]",id,"[NULL NO DATA IN CURSOR]","[NULL NO DATA IN CURSOR]"};//no value present in db
            }
        }catch (Exception ex){
            ex.printStackTrace();
            Log.d("Final_Pdf_Viewer","EXCEPTION failed to fetch person details");
            return new String[]{"ERROR",id,"ERROR","ERROR"};//to avoid error
        }
    }
    public String[] getWagesHeadersFromDbBasedOnIndicator(String id, byte indicator, boolean[] errorDetection ) {//  if error errorDetection will be set to true
        Cursor cursor2=null;//returnOnlySkill will return only string of array
        try(PersonRecordDatabase db = new PersonRecordDatabase(this);
            Cursor cursor1=db.getData("SELECT "+db.COL_8_SKILL+" FROM " +db.TABLE_NAME1+ " WHERE ID= '" + id +"'"))
        {
            cursor1.moveToFirst();
            switch (indicator) {
                case 1: {return new String[]{"DATE", "WAGES", cursor1.getString(0), "REMARKS"};}
                case 2: {
                    cursor2 = db.getData("SELECT " + db.COL_36_SKILL1 + " FROM " + db.TABLE_NAME3 + " WHERE ID='" + id + "'");
                    cursor2.moveToFirst();
                    return new String[]{"DATE", "WAGES", cursor1.getString(0), cursor2.getString(0), "REMARKS"};
                }
                case 3: { cursor2 = db.getData("SELECT " + db.COL_36_SKILL1 + " ," + db.COL_37_SKILL2 + " FROM " + db.TABLE_NAME3 + " WHERE ID='" + id + "'");
                    cursor2.moveToFirst();
                    return new String[]{"DATE", "WAGES", cursor1.getString(0), cursor2.getString(0), cursor2.getString(1), "REMARKS"};
                }
                case 4: { cursor2 = db.getData("SELECT " + db.COL_36_SKILL1 + " ," + db.COL_37_SKILL2 + " ," + db.COL_38_SKILL3 + " FROM " + db.TABLE_NAME3 + " WHERE ID='" + id + "'");
                    cursor2.moveToFirst();
                    return new String[]{"DATE", "WAGES", cursor1.getString(0), cursor2.getString(0), cursor2.getString(1), cursor2.getString(2), "REMARKS"};
                }
            }
            return new String[]{"no indicator", "no indicator", "no indicator", "no indicator"};//this statement will not execute due to retrun statement in switch just to remove error used
        }catch (Exception ex){
            ex.printStackTrace();
            Log.d("Final_Pdf_Viewer","EXCEPTION failed to fetch WagesHeadersFromDb");
            errorDetection[0]=true;
            return new String[]{"error occurred"};//to avoid error
        }finally {//since there is return statement in try and catch block so finally needed
            if(cursor2!=null) {
                cursor2.close();
            }
        }
    }
    public String[][] getAllWagesDetailsFromDbBasedOnIndicator(String id, byte indicator, boolean[] errorDetection) {//return null when no data and if error errorDetection will be set to true
        try(PersonRecordDatabase db = new PersonRecordDatabase(this)){
            Cursor wagesCursor = null;
            switch(indicator){
                case 1:wagesCursor=db.getData("SELECT "+db.COL_22_DATE+" ,"+db.COL_26_WAGES+" ,"+db.COL_28_P1+" ,"+db.COL_25_DESCRIPTION+" FROM " + db.TABLE_NAME2 + " WHERE ID='" + id + "'" + " AND "+db.COL_293_ISDEPOSITED+"='0'");break;
                case 2:wagesCursor=db.getData("SELECT "+db.COL_22_DATE+" ,"+db.COL_26_WAGES+" ,"+db.COL_28_P1+" ,"+db.COL_29_P2+" ,"+db.COL_25_DESCRIPTION+" FROM " + db.TABLE_NAME2 + " WHERE ID='" + id + "'" + " AND "+db.COL_293_ISDEPOSITED+"='0'");break;
                case 3:wagesCursor=db.getData("SELECT "+db.COL_22_DATE+" ,"+db.COL_26_WAGES+" ,"+db.COL_28_P1+" ,"+db.COL_29_P2+" ,"+db.COL_291_P3+" ,"+db.COL_25_DESCRIPTION+" FROM " + db.TABLE_NAME2 + " WHERE ID='" + id + "'" + " AND "+db.COL_293_ISDEPOSITED+"='0'");break;
                case 4:wagesCursor=db.getData("SELECT "+db.COL_22_DATE+" ,"+db.COL_26_WAGES+" ,"+db.COL_28_P1+" ,"+db.COL_29_P2+" ,"+db.COL_291_P3+" ,"+db.COL_292_P4+" ,"+db.COL_25_DESCRIPTION+" FROM " + db.TABLE_NAME2 + " WHERE ID='" + id + "'" + " AND "+db.COL_293_ISDEPOSITED+"='0'");break;
            }
            String recyclerViewWagesdata[][]=null;
            if(wagesCursor!=null&&wagesCursor.getCount()!= 0) {
                recyclerViewWagesdata = new String[wagesCursor.getCount()][wagesCursor.getColumnCount()];
                int row = 0;
                while (wagesCursor.moveToNext()) {
                    for (int col = 0; col < wagesCursor.getColumnCount(); col++) {
                        if(col !=1) {
                            recyclerViewWagesdata[row][col] = wagesCursor.getString(col);//storing all data in 2d string
                        }else{//when col is 1 then convert wages to number system
                            recyclerViewWagesdata[row][col]= MyUtility.convertToIndianNumberSystem(wagesCursor.getLong(col));
                        }
                    }
                    row++;
                }
            }
            if(wagesCursor!=null) wagesCursor.close();

            return recyclerViewWagesdata;//when no data return null
        }catch (Exception ex){
            ex.printStackTrace();
            System.out.println("error occurred in getAllWagesData method**************************");
            errorDetection[0]=true;
            return new String[][]{{"error occurred"}};//to avoid error
        }
    }
    public String[][] getAllDepositFromDb(String id, boolean[] errorDetection) {//return null when no data and if error errorDetection will be set to true
        try(PersonRecordDatabase db = new PersonRecordDatabase(this);
            Cursor depositCursor=db.getData("SELECT "+db.COL_22_DATE+" ,"+db.COL_27_DEPOSIT+" ,"+db.COL_25_DESCRIPTION+" FROM " + db.TABLE_NAME2 + " WHERE ID='" + id + "'" + " AND "+db.COL_293_ISDEPOSITED+"='1'"))
        {
            String recyclerViewDepositdata[][]=null;
            if(depositCursor!= null&&depositCursor.getCount()!=0){
                recyclerViewDepositdata= new String[depositCursor.getCount()][depositCursor.getColumnCount()];
                int row = 0;
                while (depositCursor.moveToNext()) {
                    for (int col = 0; col < depositCursor.getColumnCount(); col++) {
                        if(col!=1) {
                            recyclerViewDepositdata[row][col] = depositCursor.getString(col);//storing all data in 2d string
                        }else{
                            recyclerViewDepositdata[row][col] = MyUtility.convertToIndianNumberSystem(depositCursor.getLong(col));//if column is 1 then convert to indian number system
                        }
                    }
                    row++;
                }
            }
            return recyclerViewDepositdata;//when no data return null
        }catch (Exception ex){
            ex.printStackTrace();
            System.out.println("error occurred in getAllDepositFromDb method**************************");
            errorDetection[0]=true;
            return new String[][]{{"error occurred"}};//to avoid error
        }
    }
    private int get_indicator(String PersonId) {
        try(PersonRecordDatabase db=new PersonRecordDatabase(getApplicationContext());
            Cursor cursor = db.getData("SELECT INDICATOR FROM " + db.TABLE_NAME3 + " WHERE ID= '" + PersonId + "'")) {//for sure it will return type or skill
            if (cursor != null) {
                cursor.moveToFirst();
                if (cursor.getString(0) == null) {
                    return 1;
                } else
                    return Integer.parseInt(cursor.getString(0));
            } else
                Toast.makeText(this, "NO DATA IN CURSOR", Toast.LENGTH_SHORT).show();
        }catch(Exception ex){
            ex.printStackTrace();
            System.out.println("error occurred in get_indicator method********************");
            return 1;
        }
        return 1;//by default 1
    }
    public void displDialogMessage(String title, String message) {
        AlertDialog.Builder showDataFromDataBase=new AlertDialog.Builder(FinalPdfViewer.this);
        showDataFromDataBase.setCancelable(false);
        showDataFromDataBase.setTitle(title);
        showDataFromDataBase.setMessage(message);
        showDataFromDataBase.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {//REFRESHING
                dialogInterface.dismiss();
            }
        });
        showDataFromDataBase.create().show();
    }
    private String convertBytesToFileForSharingAndReturnAbsolutePath(byte [] pdfByte, String id){//return null when error Converts the array of bytes into a File
        File folder = new File(getExternalFilesDir(null) + "/acBookPDF");//create directory
        if (!folder.exists()) {//if folder not exist then create folder
            folder.mkdir();//File createNewFile() method returns true if new file is created and false if file already exists.
        }
       File file=new File(getExternalFilesDir(null) + "/acBookPDF/" +generateFileName(id)+ ".pdf");//path of pdf file where it is saved in device and file is created

        try{
            FileOutputStream fileOutputStream = new FileOutputStream(file.getAbsolutePath());
            fileOutputStream.write(pdfByte);
            fileOutputStream.close();
        }catch(FileNotFoundException ex){
            ex.printStackTrace();
            Log.d("FinalPdfViewer","file not found exception");
            return null;
        }catch(Exception ex){
            ex.printStackTrace();
            Log.d("FinalPdfViewer","exception while creating file in device");
            return null;
        }

        return file.getAbsolutePath();//getansoluteFile()

        /*What is difference in file.getansolutepath() and file.getansoluteFile() in java

         In Java, both file.getAbsolutePath() and file.getAbsoluteFile() are used to get the absolute path of a file. However, they return different types of objects.
         The getAbsolutePath() method returns a String object representing the absolute path of the file, whereas the getAbsoluteFile() method returns a File object representing the same.
         Here's an example to illustrate the difference:
         File file = new File("myFile.txt"); String absolutePath = file.getAbsolutePath(); File absoluteFile = file.getAbsoluteFile(); System.out.println("Absolute path: " + absolutePath); System.out.println("Absolute file: " + absoluteFile);
         The output of the above code would be something like this:
         Absolute path: /Users/username/myFile.txt Absolute file: /Users/username/myFile.txt
         As you can see, both methods return the same value in this case, but the first one returns a String while the second one returns a File object. Depending on your use case, you may prefer one over the other.*/

    }
    private boolean deletePdfFromDevice(String pdfPath) {
        try {
            File filePath = new File(pdfPath);//file to be delete
            if (filePath.exists()) {//checks file is present in device  or not
                return filePath.delete();//only this can return false
            }
        }catch (Exception ex){
            ex.printStackTrace();
            Log.d("FinalPdfViewer","failed to delete pdf from device");
            return false;
        }
        return true;//if user deleted file from device then also code will work so passing true
    }
    private String generateFileName(String ID) {
        final Calendar current=Calendar.getInstance();//to get current date and time
        Date d=Calendar.getInstance().getTime();//To get time
        SimpleDateFormat sdf=new SimpleDateFormat("hhmma");//a stands for is AM or PM
        return "id"+ID+"date"+current.get(Calendar.DAY_OF_MONTH)+"_"+(current.get(Calendar.MONTH)+1)+"_"+current.get(Calendar.YEAR)+"at"+sdf.format(d);
    }
    private void initializeIds() {
        pdfView=findViewById(R.id.pdfView);
        pdf1=findViewById(R.id.pdf1_btn);
        pdf2=findViewById(R.id.pdf2_btn);
        downloadPdf=findViewById(R.id.download_pdf_btn);
        sharePdf=findViewById(R.id.share_pdf_btn);
        currentPdf=findViewById(R.id.current_pdf_btn);
    }

}