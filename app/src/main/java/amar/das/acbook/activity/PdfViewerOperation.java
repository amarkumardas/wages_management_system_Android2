package amar.das.acbook.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import amar.das.acbook.R;
import amar.das.acbook.databinding.ActivityPdfViewerBinding;
import amar.das.acbook.PersonRecordDatabase;

import amar.das.acbook.pdfgenerator.MakePdf;
import amar.das.acbook.utility.MyUtility;

public class PdfViewerOperation extends AppCompatActivity {
     ActivityPdfViewerBinding binding;
    byte whichPdfIndicatorChangesDynamically;
    String fromIntentPersonId;
    ActivityResultLauncher<Intent> sharePdfLauncher;
    String[] absolutePathArrayToDelete =new String[2];//index 0 may contain path of pdf1 or 2 and index 2 may contain path of pdf3 , and both string array index may contain pdf1orpdf2 and pdf3

    /*pdf1 and pdf2 are view by take directly bytes from db we dont create file in device to view where as current invoiceorpdf are view by creating file in device then viewing
    * and for sharing pdf1 and pdf2 files is created in device then share also for invoiceorpdf file is created in device then share */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0); //we have used overridePendingTransition(), it is used to remove activity create animation while re-creating activity.
        binding = ActivityPdfViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        sharePdfLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),activityResult -> {//ActivityResultCallback it will execute when return from other intent
            // if(activityResult!=null && activityResult.getResultCode()==RESULT_OK){//means pdf is shared to other app
                  if(absolutePathArrayToDelete[0] !=null){//whether pdf is shared or not the created file in device should be deleted.this String array absolutePathPdfToDelete size is 2 both two space may contain path to delete so manually deleting index0 and 1
                       if(deletePdfFromDevice(absolutePathArrayToDelete[0])){
                           absolutePathArrayToDelete[0] = null;//after deleting set absolutePathPdfToDelete to null so that on destroy it will not delete the deleted file again which is useless
                        }else{
                           Log.d(this.getClass().getSimpleName(),"failed to delete file from device");
                       }

                  }
             if(absolutePathArrayToDelete[1] !=null){//whether pdf is shared or not the created file in device should be deleted
                if(deletePdfFromDevice(absolutePathArrayToDelete[1])){
                    absolutePathArrayToDelete[1] = null;//after deleting set absolutePathPdfToDelete to null so that on destroy it will not delete the deleted file again which is useless
                }else{
                    Log.d(this.getClass().getSimpleName(),"failed to delete file from device");
                }

             }
                });

        whichPdfIndicatorChangesDynamically = getIntent().getByteExtra("PDF1OR2OR3",(byte) 0);
        fromIntentPersonId = getIntent().getStringExtra("ID");
        if(onlyViewPdf(whichPdfIndicatorChangesDynamically,fromIntentPersonId)) {//by default it will show pdf according to whichPdfIndicatorChangesDynamically
            changeButtonColorBackgroundAsSelected(whichPdfIndicatorChangesDynamically);//set by default button as selected
        }else{
            displDialogMessage("ERROR OCCURRED", "ERROR OCCURRED WHILE DISPLAYING INVOICE");
        }

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();//StrictMode is a developer tool which detects things you might be doing by accident and brings them to your attention so you can fix them.
        StrictMode.setVmPolicy(builder.build());

        binding.gobackPdfViewer.setOnClickListener(view -> {
            finish();//destroy current activity
            Intent intent=new Intent( PdfViewerOperation.this,IndividualPersonDetailActivity.class);
            intent.putExtra("ID",fromIntentPersonId);
            startActivity(intent);// go back to previous Activity with updated activity so passing id to get particular person detail refresh
        });
        binding.downloadPdfBtn.setOnClickListener(view -> {
            if(whichPdfIndicatorChangesDynamically ==(byte)1){

            } else if(whichPdfIndicatorChangesDynamically == (byte)2){

            }else if(whichPdfIndicatorChangesDynamically ==(byte) 3){

            } else
                Toast.makeText(this, "WRONG PDF INDICATOR", Toast.LENGTH_LONG).show();
        });
        binding.sharePdfBtn.setOnClickListener(view -> {
            try {
                String absolutePathPdf;
                if (whichPdfIndicatorChangesDynamically != (byte) 3){//1 or 2
                    absolutePathArrayToDelete[0]=absolutePathPdf = convertBytesToFileForSharingAndReturnAbsolutePath(getPdfByteFromDb(whichPdfIndicatorChangesDynamically, fromIntentPersonId), fromIntentPersonId);
                }else{//for current pdf ie.3
                    absolutePathPdf = createCurrentInvoiceUpdatePathArrayAndReturnAbsolutePath(fromIntentPersonId);
                }

             /**note:using whatsapp we cannot send pdf directly to whatsapp for that we required approval so not using that feature*/
                if (!sharePdfToAnyApp(absolutePathPdf,sharePdfLauncher)){//open intent to share
                        Toast.makeText(this, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show();
                }
//                String phoneNumber= getActivePhoneNumbers(fromIntentPersonId);
//             shareMessageDirectlyUsingPhoneNumberToWhatsApp("amar kumar\n das\n bro",phoneNumber);
                // sendMessageToAnyApp("amar");

            }catch (Exception e){
                e.printStackTrace();
                Log.d(this.getClass().getSimpleName(),"exception occurred in method "+Thread.currentThread().getStackTrace()[2].getMethodName());
            }

        });
        binding.pdf1Btn.setOnClickListener(view -> {
            if(onlyViewPdf((byte)1,fromIntentPersonId)){
                changeButtonColorBackgroundAsSelected((byte)1);//WHEN PDF VIEWED THEN SET BUTTON AS SELECTED
                whichPdfIndicatorChangesDynamically = (byte)1;//this will be updated when SUCCESSFULLY pdf is displayed and  user click pdf1 for share and download
            }else{
                displDialogMessage("NO PREVIOUS INVOICE", "IT WILL BE AVAILABLE WHEN AGAIN CALCULATION IS DONE");
            }
        });

        binding.pdf2Btn.setOnClickListener(view -> {
            if(onlyViewPdf((byte)2,fromIntentPersonId)){
                changeButtonColorBackgroundAsSelected((byte)2);
                whichPdfIndicatorChangesDynamically = (byte)2;//this will be updated when SUCCESSFULLY pdf is displayed and  user click pdf1 for share and download
            }else {
                displDialogMessage("NO PREVIOUS INVOICE", "IT WILL BE AVAILABLE WHEN CALCULATION IS DONE");
            }
            });

        binding.currentPdfBtn.setOnClickListener(view -> {
            if(onlyViewPdf((byte)3,fromIntentPersonId)){
                changeButtonColorBackgroundAsSelected((byte)3);
                whichPdfIndicatorChangesDynamically = (byte)3;//this will be updated when SUCCESSFULLY pdf is displayed and  user click pdf1 for share and download
            }else{
                displDialogMessage("ERROR OCCURRED", "ERROR OCCURRED WHILE DISPLAYING INVOICE");
            }
        });
    }
public boolean shareMessageDirectlyUsingPhoneNumberToWhatsApp(String message,String indianWhatsappNumber){
  if(message==null || indianWhatsappNumber==null){
      return false;
  }
  try {
      if (isInternetConnected(this)){
          if (isApplicationInstalled("com.whatsapp")) {//package name
              indianWhatsappNumber = "91"+indianWhatsappNumber; // Add country code prefix for Indian numbers
              Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=" + indianWhatsappNumber+"&text="+message));
              intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//If we don't add the chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) line to set the FLAG_ACTIVITY_NEW_TASK flag, the behavior of the app when launching the chooser intent may depend on the context in which the sendMessageToAnyApp method is called.If the method is called from an activity that is already the root of a task, launching the chooser without the FLAG_ACTIVITY_NEW_TASK flag will simply add the chosen activity to the current task stack. This can lead to unexpected back stack behavior and may not be desirable if the user is expected to return to the same activity after sharing the message.On the other hand, if the method is called from an activity that is not the root of a task, launching the chooser without the FLAG_ACTIVITY_NEW_TASK flag will create a new task for the chooser and clear the previous task. This can also be unexpected and disruptive to the user's workflow.Therefore, setting the FLAG_ACTIVITY_NEW_TASK flag ensures consistent behavior regardless of the context in which the method is called, and is generally a good practice when launching chooser intents from an app
              startActivity(intent);//startActivity launch activity without expecting any result back we don't need any result back so using startActivity WITHOUT CHOOSER BECAUSE it will directly open whatsapp
              return true;
          }
      }
  }catch(Exception ex){
      ex.printStackTrace();
      return false;
  }
  return false;
}
public boolean shareMessageToAnyApp(String message){
      if(message==null) {
        return false;
       }
        try {
               Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, message);
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//If we don't add the chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) line to set the FLAG_ACTIVITY_NEW_TASK flag, the behavior of the app when launching the chooser intent may depend on the context in which the sendMessageToAnyApp method is called.If the method is called from an activity that is already the root of a task, launching the chooser without the FLAG_ACTIVITY_NEW_TASK flag will simply add the chosen activity to the current task stack. This can lead to unexpected back stack behavior and may not be desirable if the user is expected to return to the same activity after sharing the message.On the other hand, if the method is called from an activity that is not the root of a task, launching the chooser without the FLAG_ACTIVITY_NEW_TASK flag will create a new task for the chooser and clear the previous task. This can also be unexpected and disruptive to the user's workflow.Therefore, setting the FLAG_ACTIVITY_NEW_TASK flag ensures consistent behavior regardless of the context in which the method is called, and is generally a good practice when launching chooser intents from an app
                startActivity(Intent.createChooser(shareIntent, "Share message using"));//startActivity launch activity without expecting any result back SO we don't need any result back so using start activity
                return true;

        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
}
    public boolean sharePdfToAnyApp(String absolutePathPdf, ActivityResultLauncher<Intent> sharePdfLauncher){
        if(absolutePathPdf==null || sharePdfLauncher==null){//sharePdfLuncher is launcher of intent and get result after successful operation completed
            return false;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", new File(absolutePathPdf));//**to access file uri FileProvider.getUriForFile() is compulsory from if your target sdk version is 24 or greater otherwise cannot access
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//If we don't add the chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) line to set the FLAG_ACTIVITY_NEW_TASK flag, the behavior of the app when launching the chooser intent may depend on the context in which the sendMessageToAnyApp method is called.If the method is called from an activity that is already the root of a task, launching the chooser without the FLAG_ACTIVITY_NEW_TASK flag will simply add the chosen activity to the current task stack. This can lead to unexpected back stack behavior and may not be desirable if the user is expected to return to the same activity after sharing the message.On the other hand, if the method is called from an activity that is not the root of a task, launching the chooser without the FLAG_ACTIVITY_NEW_TASK flag will create a new task for the chooser and clear the previous task. This can also be unexpected and disruptive to the user's workflow.Therefore, setting the FLAG_ACTIVITY_NEW_TASK flag ensures consistent behavior regardless of the context in which the method is called, and is generally a good practice when launching chooser intents from an app
            sharePdfLauncher.launch(Intent.createChooser(intent, "Share PDF using"));//Intent.createChooser creates dialog to choose app to share data
            return true;
        }catch (Exception e){
            e.printStackTrace();
            Log.d(this.getClass().getSimpleName(),"exception occurred in method "+Thread.currentThread().getStackTrace()[2].getMethodName());
            return false;
        }
    }
    private String getActivePhoneNumbers(String id){//if no data return null
        try (PersonRecordDatabase db=new PersonRecordDatabase(getApplicationContext());
             Cursor cursor = db.getData("SELECT " +db.COL_7_PHONE + " FROM " + db.TABLE_NAME1 + " WHERE ID='" + id + "'"))
        {
            if (cursor != null) {
                cursor.moveToFirst();
                   if (cursor.getString(0).length() == 10){
                       return cursor.getString(0);
                    }
            }
        }catch(Exception ex){
                ex.printStackTrace();
                return null;
            }
        return null;
    }
    public boolean isApplicationInstalled(String packageName){
        try {
        PackageManager packageManager = getApplicationContext().getPackageManager();//in manifest <query>.. </query> permission added
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
      Log.d(this.getClass().getSimpleName(), "package name not found----------------------------------");
      return false;
    } catch(Exception ex){
        ex.printStackTrace();
        return false;
    }
    }
    public static boolean isInternetConnected(Context context) {//permission required in manifest file for accessing ConnectivityManager permission is ACCESS_NETWORK_STATE
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return true;
        } else {
            return false;
        }
        //note:this method only checks for the availability of an active network connection and does not verify if the connection can actually access the internet. It is possible to have an active network connection but not be able to access the internet due to network issues or other reasons.
    }
    public boolean changeButtonColorBackgroundAsSelected(byte buttonNumber) {
       try {
           switch (buttonNumber) {
               case 1: {
                   binding.pdf1Btn.setBackgroundResource(R.drawable.graycolor_bg);
                   binding.pdf2Btn.setBackgroundResource(R.drawable.white_detailsbg);
                   binding.currentPdfBtn.setBackgroundResource(R.drawable.white_detailsbg);
               }
               break;
               case 2: {
                   binding.pdf2Btn.setBackgroundResource(R.drawable.graycolor_bg);
                   binding.pdf1Btn.setBackgroundResource(R.drawable.white_detailsbg);
                   binding.currentPdfBtn.setBackgroundResource(R.drawable.white_detailsbg);
               }
               break;
               case 3: {
                   binding.currentPdfBtn.setBackgroundResource(R.drawable.graycolor_bg);
                   binding.pdf2Btn.setBackgroundResource(R.drawable.white_detailsbg);
                   binding.pdf1Btn.setBackgroundResource(R.drawable.white_detailsbg);
               }
               break;
           }
           return true;
       }catch (Exception e){
           e.printStackTrace();
           return false;
       }
    }
 private boolean onlyViewPdf(byte whichPdfIndicator1or2,String id) {
       if(!(whichPdfIndicator1or2 >=1 && whichPdfIndicator1or2 <=3)){//if indicator is other then 1 or 2 or 3 then show wrong pdf indicator
           Log.d(this.getClass().getSimpleName(),"wrong pdf indicator");
            return false;
        }
        try{
          if(whichPdfIndicator1or2!=3){//1 or 2

              byte[] pdfInByte=getPdfByteFromDb(whichPdfIndicator1or2,id);//if pdfbyte is null then display dialog message
              if(pdfInByte!=null){
                   binding.pdfView.fromBytes(pdfInByte).load();//if this getPdfByteFromDb method return null then dialog message will be displayed cause error even if try catch block is there so checking null present or not
              }else return false;
          }else {//display current pdf ie.3
             if(!createAndDisplayCurrentInvoice(id)) return false;
          }
        }catch (Exception e){
            e.printStackTrace();
            Log.d(this.getClass().getSimpleName(),"exception occurred in method "+Thread.currentThread().getStackTrace()[2].getMethodName());
            return false;
        }
        return true;
}
public byte[] getPdfByteFromDb(byte whichPdfIndicator,String id){//return null when no data
        if(!(whichPdfIndicator >=1 && whichPdfIndicator <=2)){//if indicator is other then 1 or 2 then show wrong pdf indicator
            Log.d(this.getClass().getSimpleName(),"wrong pdf indicator");
            return null;
        }
        Cursor cursor=null;
        try(PersonRecordDatabase db = new PersonRecordDatabase(getApplicationContext())) {
         switch(whichPdfIndicator){
            case 1:{
                cursor = db.getData("SELECT " + PersonRecordDatabase.COL_394_INVOICE1 + " FROM " + db.TABLE_NAME3 + " WHERE ID= '" + id + "'");
                cursor.moveToFirst();
                if (cursor.getBlob(0) != null){
                    return cursor.getBlob(0);
                }

            }break;
            case 2:{
                cursor = db.getData("SELECT " + PersonRecordDatabase.COL_395_INVOICE2 + " FROM " + db.TABLE_NAME3 + " WHERE ID= '" + id + "'");
                cursor.moveToFirst();
                if (cursor.getBlob(0) != null) {
                   return cursor.getBlob(0);
                }

            }break;
        }
        }catch (Exception e){
             e.printStackTrace();
            Log.d(this.getClass().getSimpleName(),"exception occurred in method "+Thread.currentThread().getStackTrace()[2].getMethodName());
            return null;
        }finally {
            if(cursor!=null){
                cursor.close();
            }
        }
    return null;//if no switch case match then it will return null
}
 public boolean createAndDisplayCurrentInvoice(String id){
            String currentInvoiceAbsolutePath;
           currentInvoiceAbsolutePath= createCurrentInvoiceUpdatePathArrayAndReturnAbsolutePath(id);//when user view this pdf then if user close the app then created file should be deleted so updating absolutePathPdfToDelete to delete on destroy
            try {
                if (currentInvoiceAbsolutePath != null) {
                    //pdfView.fromBytes(Files.readAllBytes(Paths.get(currentInvoiceAbsolutePath))).load();//viewing file from Absolute path
                     binding.pdfView.fromFile(new File(currentInvoiceAbsolutePath)).load();
                    return true;
                }else return false;

            }catch (Exception e){
                e.printStackTrace();
                Log.d(this.getClass().getSimpleName(),"exception occurred in method "+Thread.currentThread().getStackTrace()[2].getMethodName());
                return false;
            }
    }
 public String createCurrentInvoiceUpdatePathArrayAndReturnAbsolutePath(String id) {//return null when error
        byte indicator=(byte)get_indicator(id);
        boolean[] errorDetection={false};//when ever exception occur it will be updated to true in method so it indicate error occured or not
        String absoluteFilePath = null;
        MakePdf makePdf=new MakePdf();
        if(!makePdf.createPage1(MakePdf.defaultPageWidth, MakePdf.defaultPageHeight, 1)) return null;//created page 1
        float[] columnWidth=getColumnWidthBasedOnIndicator(indicator,errorDetection);
        String[] headerAccordingToIndicator = getWagesHeadersFromDbBasedOnIndicator(id, indicator, errorDetection);//THIS SHOULD BE TOP at arrayOfTotalWagesDepositRateAccordingToIndicator   TO AVOID INDEX EXCEPTION
        String[][] recyclerViewWagesdata = getAllWagesDetailsFromDbBasedOnIndicator(id, indicator, errorDetection);//it amy return null   when no data
        String[][] recyclerViewDepositdata = getAllDepositFromDb(id, errorDetection);//it amy return null   when no data

        if(!makePdf.writeSentenceWithoutLines(new String[]{""},new float[]{100f},false, (byte) 0, (byte) 0)) return null;//just for space

        if(!makePdf.writeSentenceWithoutLines(getPersonDetails(id),new float[]{40f,10f,20f,30f},true, (byte) 0, (byte) 0)) return null;//name,id,date,future invoice number

        if(errorDetection[0]==false){

            if (recyclerViewDepositdata != null) {//null means data not present
                if (!makePdf.makeTable(new String[]{"DATE", "DEPOSIT", "REMARKS"}, recyclerViewDepositdata, new float[]{12f, 12f, 76f}, 9, true))return null;
            }

            if (recyclerViewWagesdata != null) {//null means data not present
                if(!makePdf.makeTable(headerAccordingToIndicator, recyclerViewWagesdata, columnWidth, 9, false))return null;
            }else{//create dummy header with no data
                if(!makePdf.makeTable(headerAccordingToIndicator, new String[][]{{}}, columnWidth, 9, false))return null;
            }

            if(!addRowsAndWriteToPDF(indicator,makePdf,38))return null;

            if(!makePdf.createdPageFinish2())return null;//after finish page we cannot write to it

            //while creating current pdf then its file name should be same because if user click on currentpdf button repeatedly then  many file will be create in device which is useless.so to avoid that file name is kept same so that whenever user click current pdf button then new file will be replaced with old file so it is necessary to keep same file name.if file name is unique then many file will be created in device
            absoluteFilePath = makePdf.createFileToSavePdfDocumentAndReturnFileAbsolutePath3(getExternalFilesDir(null).toString(), "id"+id+"currentInvoice");//we have to return filename  view pdf using file path
            if(!makePdf.closeDocumentLastOperation4())return null;
        }

        if(absoluteFilePath!=null){
            return absolutePathArrayToDelete[1]=absoluteFilePath;//updating path array to delete when operation is completed
        }else return null;
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
            Log.d(this.getClass().getSimpleName(),"exception occurred in method "+Thread.currentThread().getStackTrace()[2].getMethodName());
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
            Log.d(this.getClass().getSimpleName(),"exception occurred in method "+Thread.currentThread().getStackTrace()[2].getMethodName());
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
            Log.d(this.getClass().getSimpleName(),"exception occurred in method "+Thread.currentThread().getStackTrace()[2].getMethodName());            errorDetection[0]=true;
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
            Log.d(this.getClass().getSimpleName(),"exception occurred in method "+Thread.currentThread().getStackTrace()[2].getMethodName());
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
            Log.d(this.getClass().getSimpleName(),"exception occurred in method "+Thread.currentThread().getStackTrace()[2].getMethodName());
            return 1;
        }
        return 1;//by default 1
    }
    public void displDialogMessage(String title, String message) {
        AlertDialog.Builder showDataFromDataBase=new AlertDialog.Builder(PdfViewerOperation.this);
        showDataFromDataBase.setCancelable(false);
        showDataFromDataBase.setTitle(title);
        showDataFromDataBase.setMessage(message);
        showDataFromDataBase.setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss());
        showDataFromDataBase.create().show();
    }
    private String convertBytesToFileForSharingAndReturnAbsolutePath(byte [] pdfByte, String id){//return null when error. Converts the array of bytes into a File
        if(pdfByte != null) {//to prevent file create in device when pdfByte is null
            File folder = new File(getExternalFilesDir(null) + "/acBookPDF");//create directory
            if (!folder.exists()) {//if folder not exist then create folder
                folder.mkdir();//File createNewFile() method returns true if new file is created and false if file already exists.
            }
            File file = new File(getExternalFilesDir(null) + "/acBookPDF/" + generateFileName(id) + ".pdf");//path of pdf file where it is saved in device and file is created

            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file.getAbsolutePath());
                fileOutputStream.write(pdfByte);
                fileOutputStream.close();
                return file.getAbsolutePath(); //get absoluteFile path
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
                Log.d(this.getClass().getSimpleName(), "FileNotFoundException");
                return null;
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.d(this.getClass().getSimpleName(), "exception occurred in method " + Thread.currentThread().getStackTrace()[2].getMethodName());
                return null;
            }
        }else return null;

        /*What is difference in file.getansolutepath() and file.getansoluteFile() in java

         In Java, both file.getAbsolutePath() and file.getAbsoluteFile() are used to get the absolute path of a file. However, they return different types of objects.
         The getAbsolutePath() method returns a String object representing the absolute path of the file, whereas the getAbsoluteFile() method returns a File object representing the same.
         Here's an example to illustrate the difference:
         File file = new File("myFile.txt"); String absolutePath = file.getAbsolutePath(); File absoluteFile = file.getAbsoluteFile(); System.out.println("Absolute path: " + absolutePath); System.out.println("Absolute file: " + absoluteFile);
         The output of the above code would be something like this:
         Absolute path: /Users/username/myFile.txt Absolute file: /Users/username/myFile.txt
         As you can see, both methods return the same value in this case, but the first one returns a String while the second one returns a File object. Depending on your use case, you may prefer one over the other.*/
    }
    private boolean deletePdfFromDevice(String pdfPath){
        if(pdfPath != null){
            try {
                File filePath = new File(pdfPath);//file to be delete
                if (filePath.exists()) {//checks file is present in device  or not
                    return filePath.delete();//only this can return false
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.d(this.getClass().getSimpleName(), "exception occurred in method " + Thread.currentThread().getStackTrace()[2].getMethodName());
                return false;
            }
        }
        return false;//if user deleted file from device then also code will work
    }
    private String generateFileName(String ID) {
        final Calendar current=Calendar.getInstance();//to get current date and time
        Date d=Calendar.getInstance().getTime();//To get time
        SimpleDateFormat sdf=new SimpleDateFormat("hhmma");//a stands for is AM or PM
        return "id"+ID+"date"+current.get(Calendar.DAY_OF_MONTH)+"_"+(current.get(Calendar.MONTH)+1)+"_"+current.get(Calendar.YEAR)+"at"+sdf.format(d);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(absolutePathArrayToDelete[0] !=null){//manually delete index 0
             if(!deletePdfFromDevice(absolutePathArrayToDelete[0])){
                Log.d(this.getClass().getSimpleName(),"failed to delete pdf file from device on destroy method");
            }

        }
        if(absolutePathArrayToDelete[1] !=null){//manually delete index 1
            if(!deletePdfFromDevice(absolutePathArrayToDelete[1])){
                Log.d(this.getClass().getSimpleName(),"failed to delete pdf file from device on destroy method");
            }

        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();//destroy current activity
        Intent intent=new Intent( PdfViewerOperation.this,IndividualPersonDetailActivity.class);
        intent.putExtra("ID",fromIntentPersonId);
        startActivity(intent);// go back to previous Activity with updated activity so passing id to get particular person detail refresh
    }
}