package amar.das.acbook.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    String[] absolutePathArrayToDelete =new String[3];//index 0 may contain path of pdf1 or 2 and index 1 may contain path of pdf3 , and both string array index may contain pdf1orpdf2 and pdf3.INDEX 2 contain  the path of image

    /*pdf1 and pdf2 are view by take directly bytes from db we don't create file in device to view where as current invoiceorpdf are view by creating file in device then viewing
    * and for sharing pdf1 and pdf2 files is created in device then share also for invoiceorpdf file is created in device then share */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0); //we have used overridePendingTransition(), it is used to remove activity create animation while re-creating activity.
        binding = ActivityPdfViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharePdfLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),activityResult -> {//ActivityResultCallback it will execute when return from other intent
            // if(activityResult!=null && activityResult.getResultCode()==RESULT_OK){//not using result-code because there can two path in one result-code so it will fail to delete both path so not using result code
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
            if(absolutePathArrayToDelete[2] !=null){//whether image is shared or not the created file in device should be deleted
                if(deletePdfFromDevice(absolutePathArrayToDelete[2])){
                    absolutePathArrayToDelete[2] = null;//after deleting set absolutePathPdfToDelete to null so that on destroy it will not delete the deleted file again which is useless
                }else{
                    Log.d(this.getClass().getSimpleName(),"failed to delete file from device");
                }
            }
        });

        whichPdfIndicatorChangesDynamically = getIntent().getByteExtra("pdf1_or_2_or_3_for_blank_4",(byte) 0);
        fromIntentPersonId = getIntent().getStringExtra("ID");
        if(onlyViewPdf(whichPdfIndicatorChangesDynamically,fromIntentPersonId)) {//by default it will show pdf according to whichPdfIndicatorChangesDynamically
            changeButtonColorBackgroundAsSelected(whichPdfIndicatorChangesDynamically);//set by default button as selected
        }
//        else{//this statement not needed because if user don't need to display pdf then it should not give this message but log will be displayed message as wrong pdf indicator
//            displayDialogMessage("ERROR OCCURRED", "ERROR OCCURRED WHILE DISPLAYING INVOICE");
//        }

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
                //directly taking pdfByte SO THAT we don't need to create extra file and deleted
                if(downloadPdfUsingAbsPathOrByte(null,getPdfByteFromDb(whichPdfIndicatorChangesDynamically, fromIntentPersonId),fromIntentPersonId)){
                    displayDialogMessage("DOWNLOADED","INVOICE 1\nID: "+fromIntentPersonId);
                }else{
                    Toast.makeText(this, "SOMETHING WENT WRONG CANNOT DOWNLOAD", Toast.LENGTH_LONG).show();
                }

            }else if(whichPdfIndicatorChangesDynamically == (byte)2){

                if(downloadPdfUsingAbsPathOrByte(null,getPdfByteFromDb(whichPdfIndicatorChangesDynamically, fromIntentPersonId),fromIntentPersonId)){
                    displayDialogMessage("DOWNLOADED","INVOICE 2\nID: "+fromIntentPersonId);
                }else{
                    Toast.makeText(this, "SOMETHING WENT WRONG CANNOT DOWNLOAD", Toast.LENGTH_LONG).show();
                }

            }else if(whichPdfIndicatorChangesDynamically ==(byte) 3){
                absolutePathArrayToDelete[1]=createCurrentInvoiceAndReturnAbsolutePath(fromIntentPersonId);
                if(downloadPdfUsingAbsPathOrByte(absolutePathArrayToDelete[1],null,fromIntentPersonId)){
                    displayDialogMessage("DOWNLOADED","CURRENT INVOICE\nID: "+fromIntentPersonId);

                }else{
                    Toast.makeText(this, "SOMETHING WENT WRONG CANNOT DOWNLOAD", Toast.LENGTH_LONG).show();
                }
                if(deletePdfFromDevice(absolutePathArrayToDelete[1])) {//manually delete the generated file from app private storage because this file is downloaded and stored in download folder so deleting it otherwise same file will be twice.it will be deleted when error occurred or not
                    absolutePathArrayToDelete[1]=null; //after file deleted set null
                }
            }else//when whichPdfIndicatorChangesDynamically is 4
                displayDialogMessage("PLEASE SELECT","INVOICE TO DOWNLOAD");
        });
        binding.sharePdfBtn.setOnClickListener(view -> {
            try {
                String absolutePathPdf;
                if (whichPdfIndicatorChangesDynamically != (byte) 3){//1 or 2
                    absolutePathArrayToDelete[0]=absolutePathPdf = convertBytesToFileForSharingAndReturnAbsolutePath(getPdfByteFromDb(whichPdfIndicatorChangesDynamically, fromIntentPersonId), fromIntentPersonId);
                }else{//for current pdf ie.3
                    absolutePathArrayToDelete[1]= absolutePathPdf = createCurrentInvoiceAndReturnAbsolutePath(fromIntentPersonId);

                }
                /**note:using whatsapp we cannot send pdf directly to whatsapp for that we required approval so not using that feature*/
                if(whichPdfIndicatorChangesDynamically!=(byte)4){//4 represent blank
                    if (!sharePdfToAnyApp(absolutePathPdf, sharePdfLauncher)) {//open intent to share
                        Toast.makeText(this, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show();
                    }
                }else{
                    displayDialogMessage("PLEASE SELECT","INVOICE TO SHARE");
                }
            }catch (Exception e){
                e.printStackTrace();
                Log.d(this.getClass().getSimpleName(),"exception occurred in method "+Thread.currentThread().getStackTrace()[2].getMethodName());
            }
        });
        //-----------TEXT BUTTON-----------------------------------------
        String[] shareOptions = getResources().getStringArray(R.array.sharingOptions);
        ArrayAdapter<String> shareOptionsAdapter = new ArrayAdapter<>( this, android.R.layout.simple_list_item_1, shareOptions);
        binding.textShare.setAdapter(shareOptionsAdapter);
        binding.textShare.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                try {
                    String itemName = adapterView.getItemAtPosition(position).toString();
                    switch (itemName) {
                        case "A/C": {
                            if (!openCustomAlertDialogToShareTextToAnyAppOrDirectlyToWhatsApp(getAccountDetailsFromDb(fromIntentPersonId,getIdNamePhone(fromIntentPersonId)), getResources().getString(R.string.enter_amount), fromIntentPersonId, true)) { //getAccountDetailsFromDb()if this method return null then alertdialog will return false
                                Toast.makeText(PdfViewerOperation.this, "SOMETHING WENT WRONG", Toast.LENGTH_LONG).show();
                            }
                        }
                        break;
                        case "PHONE NUMBER": {
                            String message = activePhoneNumberToShare(fromIntentPersonId);//if there is no phone number then return null also when exception
                            if (message != null) {//if no phone number then don't open dialog because its useless
                                if (!openCustomAlertDialogToShareTextToAnyAppOrDirectlyToWhatsApp(message, getResources().getString(R.string.enter_amount), fromIntentPersonId, true)) {
                                    Toast.makeText(PdfViewerOperation.this, "SOMETHING WENT WRONG", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(PdfViewerOperation.this, "NO PHONE NUMBER", Toast.LENGTH_LONG).show();
                            }
                        }
                        break;
                        case "CURRENT INVOICE": {
                            if (!openAlertDialogToShareTextToAnyAppOrDirectlyToWhatsApp(getMessageForCurrentInvoice(fromIntentPersonId), fromIntentPersonId, false)) {  //getMessageForCurrentInvoice()if this method return null then alertdialog will return false
                                Toast.makeText(PdfViewerOperation.this, "SOMETHING WENT WRONG", Toast.LENGTH_LONG).show();
                            }
                        }
                        break;
                        case "IMAGE": {
                            if (!shareImageToAnyApp(getIdNamePhone(fromIntentPersonId),fromIntentPersonId,sharePdfLauncher)) {
                                Toast.makeText(PdfViewerOperation.this, "SOMETHING WENT WRONG", Toast.LENGTH_LONG).show();
                            }
                        }
                        break;
                        case "SHARE ALL": {
                            if (!shareAllData(fromIntentPersonId,sharePdfLauncher)) {
                                Toast.makeText(PdfViewerOperation.this, "SOMETHING WENT WRONG", Toast.LENGTH_LONG).show();
                            }
                        }
                        break;
                    }
                    //after selecting second time any option data is not shown so 0 is set so that when second time click it will show data
                    //int initialposition = holder.spinnerdescAudioIcon.getSelectedItemPosition();
                    binding.textShare.setSelection(0, false);//clearing auto selected or if we remove this line then only one time we would be able to select audio and remarks which we don't want
                }catch (Exception x){
                    x.printStackTrace();
                }
                }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView){}
        });

        binding.pdf1Btn.setOnClickListener(view -> {
            if(onlyViewPdf((byte)1,fromIntentPersonId)){
                changeButtonColorBackgroundAsSelected((byte)1);//WHEN PDF VIEWED THEN SET BUTTON AS SELECTED
                whichPdfIndicatorChangesDynamically = (byte)1;//this will be updated when SUCCESSFULLY pdf is displayed and  user click pdf1 for share and download
            }else{
                displayDialogMessage("NO PREVIOUS INVOICE", "IT WILL BE AVAILABLE WHEN AGAIN CALCULATION IS DONE");
            }
        });

        binding.pdf2Btn.setOnClickListener(view -> {
            if(onlyViewPdf((byte)2,fromIntentPersonId)){
                changeButtonColorBackgroundAsSelected((byte)2);
                whichPdfIndicatorChangesDynamically = (byte)2;//this will be updated when SUCCESSFULLY pdf is displayed and  user click pdf1 for share and download
            }else {
                displayDialogMessage("NO PREVIOUS INVOICE", "IT WILL BE AVAILABLE WHEN CALCULATION IS DONE");
            }
            });

        binding.currentPdfBtn.setOnClickListener(view -> {
            if(onlyViewPdf((byte)3,fromIntentPersonId)){
                changeButtonColorBackgroundAsSelected((byte)3);
                whichPdfIndicatorChangesDynamically = (byte)3;//this will be updated when SUCCESSFULLY pdf is displayed and  user click pdf1 for share and download
            }else{
                displayDialogMessage("ERROR OCCURRED", "ERROR OCCURRED WHILE DISPLAYING INVOICE");
            }
        });
    }
    private boolean downloadPdfUsingAbsPathOrByte(String absolutePath, byte[] pdfByte,String id) {
        try{
            if(id==null){
                return false;
            }
            if(pdfByte!=null){
               return downloadPdfUsingByte("DOWNLOADED"+ generateUniqueFileName(id)+".pdf",pdfByte);
            }

            if(absolutePath!= null){
                byte[] convertedBytes = Files.readAllBytes(Paths.get(absolutePath));// This code uses the Files.readAllBytes() method from the java.nio.file package to read all the bytes from the file specified by the absolute path into a byte array. This method is more concise and efficient .
                return downloadPdfUsingByte("DOWNLOADED"+ generateUniqueFileName(id)+".pdf",convertedBytes);
            }
            return false;
        }catch(Exception x){
            x.printStackTrace();
            return false;
        }
    }
    private boolean downloadPdfUsingByte(String filename, byte[] pdfContent) {//this method to save it to the download folder.
        if(filename==null || pdfContent==null){
            return false;
        }
        try {
          if(checkPermissionForInternalAndExternal()){
               File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
               File pdfFile = new File(downloadsFolder, filename);

               FileOutputStream outputStream = new FileOutputStream(pdfFile);
               outputStream.write(pdfContent);
               outputStream.close();
            return true;
        }else{
            Toast.makeText(PdfViewerOperation.this, "EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(PdfViewerOperation.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 42);
            return false;
            }
        }catch (IOException e) {
            e.printStackTrace();
            Log.d(this.getClass().getSimpleName(),"io exception"+Thread.currentThread().getStackTrace()[2].getMethodName());
            return false;
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }
    }
    private boolean shareAllData(String id,ActivityResultLauncher<Intent> sharePdfLauncher) {
        StringBuilder sb=new StringBuilder();
        try{
            String[] message=getPersonDetailsForCurrentInvoice(id);//id,name,invoicenumber,date
            sb.append(message[1]).append("\n");
            sb.append(message[0]).append("\n");//setting first id value
            sb.append(message[2]).append("\n");
            sb.append(message[3]).append("\n");

            String phoneNumber=getActivePhoneNumbersFromDb(id);
            if(phoneNumber!=null){
                sb.append("PHONE: ").append(phoneNumber).append("\n");//phone number
            }else{
                sb.append("PHONE: null").append("\n");
            }

            sb.append(getOtherDetails(id)).append("\n");//other details like aadhaar

            String accountDetails=getAccountDetailsFromDb(id,"");
            if(accountDetails!=null){
                sb.append(accountDetails).append("\n");//account details
            }else{
              sb.append("ACCOUNT DETAILS: null") .append("\n");
            }

            sb.append(getAllDepositAndWagesDetailsAsText(id)).append("\n");//all wages and deposit data

            return  shareImageToAnyApp(sb.toString(),id,sharePdfLauncher);//sharing all data including image

        }catch (Exception x){
            x.printStackTrace();
            return false;
        }
    }
    private String getOtherDetails(String id) {
        try(PersonRecordDatabase db=new PersonRecordDatabase(getApplicationContext());
            Cursor cursor = db.getData("SELECT " +db.COL_6_AADHAAR_NUMBER + " FROM " + db.TABLE_NAME1 + " WHERE ID='" + id + "'")){
            if(cursor != null){
                cursor.moveToFirst();
            }else return "null";

         StringBuilder sb=new StringBuilder();

            if(!cursor.getString(0).isEmpty()){
                sb.append("AADHAAR NO: ").append(cursor.getString(0));
            }else{
                sb.append("AADHAAR NO: null");
            }
            return sb.toString();

        }catch (Exception x){
            x.printStackTrace();
            return "error";
        }
    }
    private boolean shareImageToAnyApp(String message, String id, ActivityResultLauncher<Intent> sharePdfLauncher) {
        if(message==null|| id==null || sharePdfLauncher ==null){
            return false;
        }
        try(PersonRecordDatabase db=new PersonRecordDatabase(getApplicationContext());
             Cursor cursor = db.getData("SELECT " +db.COL_10_IMAGE + " FROM " + db.TABLE_NAME1 + " WHERE ID='" + id + "'")){

            if(cursor != null){
                cursor.moveToFirst();
            }else return false;

            byte[] image=cursor.getBlob(0);
            if (image!=null) {
                if(checkPermissionForInternalAndExternal()) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length); //for resizing image -Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 65, 62, false);//image size

                   //Why do we need to Save the image to external storage? When sharing an image with other apps in Android, you need to provide a file URI that points to the location of the image on the device's storage. If you don't save the image to external storage, you won't be able to share it with other apps.In addition, apps are not allowed to share files directly from their internal storage with other apps. This is a security measure implemented by Android to prevent apps from accessing each other's data without explicit user permission.
                    File file = new File(getExternalCacheDir(), "image.jpg");//creating file in cache directory file name cache path.image.jpg.getExternalCacheDir() is a method in Android's Context class that returns a File object representing the external storage directory specific to your app for storing cache files. This directory is automatically created for your app and is private to your app, meaning that other apps cannot access its contents.Cache files are temporary files that are used to improve the performance of your app. By storing files that your app frequently uses in the cache directory, you can avoid repeatedly reading or downloading those files from a remote source, which can slow down your app's performance.The getExternalCacheDir() method returns a File object that represents the path to your app's external cache directory, which you can use to save cache files or other temporary files that your app needs to access quickly. For example, when sharing an image, you can save the image to this directory before sharing it with other apps.
                    FileOutputStream outputStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);//image quality
                    outputStream.flush();
                    outputStream.close();
                    //In Android 12, you cannot use Uri.fromFile() to get the URI for a file. Instead, you should use FileProvider.getUriForFile() to get the URI for the file.
                    Uri fileUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);//**to access file uri FileProvider.getUriForFile() is compulsory from if your target sdk version is 24 or greater otherwise cannot access

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);//sharing
                    shareIntent.setType("image/*");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, message);
                    sharePdfLauncher.launch(Intent.createChooser(shareIntent, "SHARE IMAGE USING"));//Intent.createChooser creates dialog to choose app to share data and after shared pdf launcher will execute to delete the image

                    absolutePathArrayToDelete[2] = file.getAbsolutePath();//storing absolute path to delete the image
                }else{
                    Toast.makeText(PdfViewerOperation.this, "EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(PdfViewerOperation.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 41);
                    return false;
                }
            }else{
                Toast.makeText(PdfViewerOperation.this, "NO IMAGE", Toast.LENGTH_LONG).show();
                return false;
            }
           return true;
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }
    }
    private String activePhoneNumberToShare(String id){//return null when exception or return null when no phone number
        try{
            if(getActivePhoneNumbersFromDb(id)!= null) {//checking phoneNumber is there or not
                return getIdNamePhone(id);
            }else{
                return null;
            }
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
    public boolean openAlertDialogToShareTextToAnyAppOrDirectlyToWhatsApp(String message, String id, boolean defaultTrueForOpenAnyAppAndFalseForWhatsApp) {
        if(message==null){
            return false;
        }
        try{
            AlertDialog.Builder dialogBuilder = new  AlertDialog.Builder(this);
            dialogBuilder.setCancelable(true);
            dialogBuilder.setTitle(getResources().getString(R.string.message));
            dialogBuilder.setMessage(message);

            dialogBuilder.setNegativeButton(getResources().getString(R.string.send_to_contact), (dialogInterface, i) -> {
                           if(!sendSMSUsingIntentToPhoneNumber(message,id)){//if failed then copy message
                               if(copyTextToClipBoard(message)){
                                   Toast.makeText(this, getResources().getString(R.string.message_copied), Toast.LENGTH_LONG).show();
                                }
                           }
                           dialogInterface.dismiss();
            });
            dialogBuilder.setPositiveButton(getResources().getString(R.string.share_to_whatsapp), (dialogInterface, i) -> {
                if(defaultTrueForOpenAnyAppAndFalseForWhatsApp) {
                    shareMessageToAnyApp(message);
                }else{
                    String activePhone=getActivePhoneNumbersFromDb(id);//for opening whatsapp we have to check phone number is available or not
                    if(activePhone!=null) {
                        if(!shareMessageDirectlyToWhatsApp(message,activePhone)){
                            shareMessageToAnyApp(message); //if no internet or error in opening whatsapp then execute this
                        }
                    }else{//if no phone number then share text to any app
                        shareMessageToAnyApp(message);
                    }
                }
                dialogInterface.dismiss();
            });
    //dialogBuilder.setPositiveButtonIcon(getDrawable(R.drawable.green_color_bg));
            dialogBuilder.create().show();
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }
        return true;
    }
    public boolean copyTextToClipBoard(String message) {
        if(message==null){
            return false;
        }
        try{
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label",message);
            clipboard.setPrimaryClip(clip);
            return true;
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }
    }
    private boolean sendSMSUsingIntentToPhoneNumber(String message, String id) {
        if(message==null && id==null) {
            return false;
        }
        try{
            String phoneNumber=getActivePhoneNumbersFromDb(id);
            if(phoneNumber!=null){
                if(checkPermissionForSMS()){   //send an SMS using an intent
                    Intent intent=new Intent(Intent.ACTION_VIEW,Uri.fromParts("sms",phoneNumber,null));//The first parameter specifies the protocol ("sms"), the second parameter specifies the recipient's phone number.URI can be used to launch the SMS app with a pre-filled recipient phone number.
                    intent.putExtra("sms_body",message);//here adding flag not required
                    startActivity(intent);
                    return true;
                }else{
                    Toast.makeText(PdfViewerOperation.this, "SMS PERMISSION REQUIRED", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(PdfViewerOperation.this, new String[]{Manifest.permission.SEND_SMS}, 31);
                    return false;
                }
            }else{
                Toast.makeText(this, "NO PHONE NUMBER", Toast.LENGTH_LONG).show();
                return false;
            }
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
    private String getMessageForCurrentInvoice(String id){//return null when exception
        StringBuilder sb=new StringBuilder();
        try{
            String[] message=getPersonDetailsForCurrentInvoice(id);
            sb.append(message[1]).append("\n");
            sb.append(message[0]).append("\n");//setting first id value
            sb.append(message[2]).append("\n");
            sb.append(message[3]).append("\n");

           sb.append(getAllDepositAndWagesDetailsAsText(id));

        }catch (Exception x){
            x.printStackTrace();
            return null;
        }
        return sb.toString();
    }
    private String getAllDepositAndWagesDetailsAsText(String id) {
        StringBuilder sb=new StringBuilder();
        try{
            byte indicator=(byte)get_indicator(id);
            boolean[] errorDetection={false};//when ever exception occur it will be updated to true in method so it indicate error occured or not
            String[] header = getWagesHeadersFromDbBasedOnIndicator(id, indicator, errorDetection);//THIS SHOULD BE TOP at arrayOfTotalWagesDepositRateBasedOnIndicator   TO AVOID INDEX EXCEPTION
            String[][] recyclerViewWagesdata = getAllWagesDetailsFromDbBasedOnIndicator(id, indicator, errorDetection);//it amy return null   when no data
            String[][] recyclerViewDepositdata = getAllDepositFromDb(id, errorDetection);//it amy return null   when no data
            int[] arrayOfTotalWagesDepositRateBasedOnIndicator= getSumOfTotalWagesDepositRateDaysWorkedBasedOnIndicator(id,indicator,errorDetection);//if error cause errorDetection will be set true

            if(errorDetection[0]==false){

                sb.append(getTotalWagesDepositAndWorkingAccordingToIndicator(indicator,header,arrayOfTotalWagesDepositRateBasedOnIndicator,recyclerViewDepositdata!=null));

                if (recyclerViewDepositdata != null) {//null means data not present so dont add deposit in text
                    sb.append("\n\n=====DEPOSIT=====\n");
                    int rowLength=recyclerViewDepositdata.length;
                    int columnLength=recyclerViewDepositdata[0].length;
                    for (int row = 0; row < rowLength; row++) {
                        sb.append("--> ");
                        for (int col = 0; col < columnLength; col++) {

                            if((columnLength-1)!=col)
                                sb.append(recyclerViewDepositdata[row][col]).append("  ");
                            else sb.append("\n").append(recyclerViewDepositdata[row][col]);//GIVING SPACE TO EACH COLUMN AND AT LAST COLUMN GIVING NEXT-LINE TO SHOW REMARKS
                        }
                        sb.append("\n\n");
                    }
                }
                if (recyclerViewWagesdata != null) {//null means data not present
                    if (recyclerViewDepositdata == null) {
                        sb.append("\n\n=====WAGES=====\n");//add double space when deposit is null for better readability
                    }else sb.append("\n=====WAGES=====\n");

                    int rowLength=recyclerViewWagesdata.length;
                    int columnLength=recyclerViewWagesdata[0].length;
                    for (int row = 0; row < rowLength; row++) {
                        sb.append("--> ");
                        for (int col = 0; col < columnLength; col++){

                            if((columnLength-1)!=col) sb.append(recyclerViewWagesdata[row][col]).append("  "); else sb.append("\n").append(recyclerViewWagesdata[row][col]);

                        }
                        sb.append("\n\n");
                    }
                }
            }
            return sb.toString();
        }catch (Exception x){
            x.printStackTrace();
            return "error";
        }
    }
    private String getTotalWagesDepositAndWorkingAccordingToIndicator(byte indicator,String[] headerBasedOnIndicator, int[] arrayOfTotalWagesDepositRateAccordingToIndicator,boolean isDepositPresent) {
    try{
        /**
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
            sb.append("\nTOTAL DEPOSIT: ").append(MyUtility.convertToIndianNumberSystem( arrayOfTotalWagesDepositRateAccordingToIndicator[indicator + 1]));//[indicator + 1] it is index of deposit
        }
        return sb.toString();

    }catch (Exception x){
        x.printStackTrace();
        return "ERROR OCCURRED";
    }
    }
    public int[] getSumOfTotalWagesDepositRateDaysWorkedBasedOnIndicator(String id, byte indicator, boolean[] errorDetection) {//important method return arr with value but if error return arr with 0 value and errorDetection set to true;.it index value is sensitive.according to indicator it store date in particular index
        Cursor sumDepositWagesCursor =null,rateCursor=null;//return data in format [wages,p1,p2,p3,p4,deposit,r1,r2,r3,r4]
        try(PersonRecordDatabase db = new PersonRecordDatabase(this)){
            switch(indicator){
                case 1:{sumDepositWagesCursor=db.getData("SELECT SUM("+db.COL_26_WAGES+"),SUM("+db.COL_28_P1+"), SUM("+db.COL_27_DEPOSIT+") FROM "+db.TABLE_NAME2+" WHERE ID= '"+id +"'");
                    rateCursor=db.getData("SELECT  "+db.COL_32_R1+" FROM " + db.TABLE_NAME3 + " WHERE ID= '" + id +"'");
                }break;
                case 2:{sumDepositWagesCursor=db.getData("SELECT SUM("+db.COL_26_WAGES+"),SUM("+db.COL_28_P1+"),SUM("+db.COL_29_P2+"), SUM("+db.COL_27_DEPOSIT+") FROM "+db.TABLE_NAME2+" WHERE ID= '"+id +"'");
                    rateCursor=db.getData("SELECT  "+db.COL_32_R1+", "+db.COL_33_R2+" FROM "+ db.TABLE_NAME3 +" WHERE ID= '" + id +"'");
                }break;
                case 3:{sumDepositWagesCursor=db.getData("SELECT SUM("+db.COL_26_WAGES+"),SUM("+db.COL_28_P1+"),SUM("+db.COL_29_P2+"),SUM("+db.COL_291_P3+"), SUM("+db.COL_27_DEPOSIT+") FROM "+db.TABLE_NAME2+" WHERE ID= '"+id +"'");
                    rateCursor=db.getData("SELECT  "+db.COL_32_R1+", "+db.COL_33_R2+", "+db.COL_34_R3+" FROM "+ db.TABLE_NAME3 +" WHERE ID= '" + id +"'");
                }break;
                case 4:{sumDepositWagesCursor=db.getData("SELECT SUM("+db.COL_26_WAGES+"),SUM("+db.COL_28_P1+"),SUM("+db.COL_29_P2+"),SUM("+db.COL_291_P3+"),SUM("+db.COL_292_P4+"), SUM("+db.COL_27_DEPOSIT+")  FROM "+db.TABLE_NAME2+" WHERE ID= '"+id +"'");
                    rateCursor=db.getData("SELECT  "+db.COL_32_R1+", "+db.COL_33_R2+", "+db.COL_34_R3+", "+db.COL_35_R4+" FROM "+ db.TABLE_NAME3 +" WHERE ID= '" + id +"'");
                }break;
            }
            int[] arr=new int[2*(indicator+1)];//size will change according to indicator to get exact size.like indicator 1 need 4 space in array so formula is [2*(indicator+1)]
            int col=0;
            if (sumDepositWagesCursor !=null && sumDepositWagesCursor.getCount()!=0) {
                sumDepositWagesCursor.moveToFirst();
                for (int i = 0; i < sumDepositWagesCursor.getColumnCount(); i++) {//retrieving data from cursor
                    arr[col++]=sumDepositWagesCursor.getInt(i);
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
            System.out.println("error occurred in getArrayDataOfTotalWagesDepositRateDaysWorked method**************************");
            errorDetection[0]=true;//indicate error has occur
            return new int[2*(indicator+1)];//if exception occur 0 value will be return
        }finally {//since there is return statement in try and catch block so finally needed
            if(sumDepositWagesCursor!=null&& rateCursor !=null) {
                sumDepositWagesCursor.close();
                rateCursor.close();
            }
        }
    }
    private boolean openCustomAlertDialogToShareTextToAnyAppOrDirectlyToWhatsApp(String message, String input1Header,String id,boolean defaultTrueForOpenAnyAppAndFalseForWhatsApp) {//true is for Open anyApp
        if(message==null || input1Header==null){
            return false;
        }
        try {
            AlertDialog.Builder customDialogBuilder = new AlertDialog.Builder(PdfViewerOperation.this);
            LayoutInflater inflater = LayoutInflater.from(PdfViewerOperation.this);
            View myView = inflater.inflate(R.layout.take_two_input_from_user_layout, null);//myView contain all layout view ids
            customDialogBuilder.setView(myView);//set custom layout to alert dialog
            customDialogBuilder.setCancelable(false);//if user touch to other place then dialog will not be close
            AlertDialog dialog = customDialogBuilder.create();//customDialogBuilder varialble cannot be use in inner class so creating another final varialbe  to use in inner class
            //ids
            TextView showMessage_tV = myView.findViewById(R.id.message_of_dialog), input1_tV = myView.findViewById(R.id.input1_header_dialog);
            EditText integerInput_Et = myView.findViewById(R.id.input1_edit_text_dialog), remarks_Et = myView.findViewById(R.id.remarks_dialog);
            Button share = myView.findViewById(R.id.share_button_dialog);
            showSoftKeyboardByForced();//open keyboard automatically by programatically

            showMessage_tV.setText(message);//set text
            input1_tV.setText(input1Header);
            myView.findViewById(R.id.cancel_btn_dialog).setOnClickListener(view1 -> dialog.dismiss());

            share.setOnClickListener(view12 -> {//SHARE
                    String remarks = "", amount = "";
                    if (!TextUtils.isEmpty(integerInput_Et.getText().toString().trim())) {//textutils checks for both null and empty string
                        amount = "\n\n" + getResources().getString(R.string.amount_rs) + " " + MyUtility.convertToIndianNumberSystem((long) Integer.parseInt(integerInput_Et.getText().toString().trim()));
                    }
                    if (!TextUtils.isEmpty(remarks_Et.getText().toString().trim())) {
                        remarks = "\n" + getResources().getString(R.string.remarks) + ": " + remarks_Et.getText().toString().trim();
                    }
                    if(defaultTrueForOpenAnyAppAndFalseForWhatsApp){
                        shareMessageToAnyApp(MyUtility.get12hrCurrentTimeAndDate() + "\n" + message + amount + remarks);
                    }else{//execute only when boolean value is false
                        String activePhone= getActivePhoneNumbersFromDb(id);//for opening whatsapp we have to check phone number is available or not
                        if(activePhone!=null) {
                            shareMessageDirectlyToWhatsApp(MyUtility.get12hrCurrentTimeAndDate() + "\n" + message + amount + remarks,activePhone);
                        }else{
                            Toast.makeText(this, "NO PHONE NUMBER", Toast.LENGTH_LONG).show();
                        }
                    }
                    dialog.dismiss();//after send close dialog
            });
            integerInput_Et.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    String userInput = integerInput_Et.getText().toString().trim();
                    if (!userInput.matches("[0-9]+")) {//space or , or - is restricted"[.]?[0-9]+[.]?[0-9]*"
                        integerInput_Et.setTextColor(Color.RED);
                        share.setVisibility(View.GONE);
                    } else {
                        share.setVisibility(View.VISIBLE);
                        integerInput_Et.setTextColor(Color.BLACK);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            showMessage_tV.setOnClickListener(view -> {
                if(copyTextToClipBoard(message)){
                     Toast.makeText(PdfViewerOperation.this,  getResources().getString(R.string.message_copied), Toast.LENGTH_LONG).show();
                }
            });
            dialog.show();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public String getAccountDetailsFromDb(String id,String idNamePhone) {//return null when error
        try (PersonRecordDatabase db=new PersonRecordDatabase(getApplicationContext());
             Cursor cursor = db.getData("SELECT "+db.COL_3_BANKAC+" , "+db.COL_4_IFSCCODE+" , "+db.COL_5_BANKNAME+" , "+db.COL_9_ACCOUNT_HOLDER_NAME+ " FROM " + db.TABLE_NAME1 + " WHERE ID='" + id + "'"))
        {
            StringBuilder sb=new StringBuilder(idNamePhone);
            if (cursor != null) {//which ever phone is available that phone will be send
                cursor.moveToFirst();

                if(!cursor.getString(2).isEmpty()){//isempty() checks for empty string only and not for null
                    sb.append("\nBANK NAME: ").append(cursor.getString(2)+"\n");
                }else{
                    sb.append("\nBANK NAME: null\n");
                }
                if(!cursor.getString(3).isEmpty()){
                    sb.append("\nA/C HOLDER NAME: ").append(cursor.getString(3)+"\n");
                }else{
                    sb.append("A/C HOLDER NAME: null\n");
                }

                if(!cursor.getString(0).isEmpty()){
                    sb.append("\nA/C: ").append(convertToReadableNumber(cursor.getString(0))+"\n\n");
                }else{
                    sb.append("A/C: null\n");
                }
                
                if(!cursor.getString(1).isEmpty()){
                    sb.append("IFSC CODE: ").append(cursor.getString(1));
                }else{
                    sb.append("IFSC CODE: null");
                }
            }
            return sb.toString();
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
    public String getIdNamePhone(String id) {
        try (PersonRecordDatabase db=new PersonRecordDatabase(getApplicationContext());
             Cursor cursor = db.getData("SELECT " +db.COL_2_NAME+ " FROM " + db.TABLE_NAME1 + " WHERE ID='" + id + "'"))
        {
            StringBuilder sb=new StringBuilder();
            if (cursor != null) {//which ever phone is available that phone will be send
                cursor.moveToFirst();


                sb.append("ID: "+id+"\n");
                if (!cursor.getString(0).isEmpty()){
                    sb.append("NAME: "+cursor.getString(0)+"\n");
                }else{
                    sb.append("NAME: null\n");
                }

                String activePhoneNumber=getActivePhoneNumbersFromDb(id);
                if(activePhoneNumber != null) {
                    sb.append("PHONE: " +activePhoneNumber);
                }else{
                    sb.append("PHONE: null");
                }
            }
            return sb.toString().trim();
        }catch(Exception ex){
            ex.printStackTrace();
            return "error";
        }
    }
    public String convertToReadableNumber(String number){//In this optimized code, we create a char array arr with an initial capacity equal to the length of the input string str, plus an extra capacity for the spaces that will be inserted (i.e., str.length() + str.length() / 4). We then loop through each character of the input string str, appending each character to arr using arr[j++] = str.charAt(i).After every fourth character (except for the last character in the string), we append a space character to arr using arr[j++] = ' '. We use an integer variable j to keep track of the next index in arr where a character should be inserted.
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
        return new String(arr);
    }
    public boolean shareMessageDirectlyToWhatsApp(String message,String indianWhatsappNumber){
  if(message==null || indianWhatsappNumber==null){
      return false;
  }
  try {
      if (isInternetConnected(this)){
          if (isApplicationInstalled("com.whatsapp")) {//package name
              indianWhatsappNumber = "91"+indianWhatsappNumber; // Add country code prefix for Indian numbers
              Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=" +indianWhatsappNumber+"&text="+message));
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
    private String getActivePhoneNumbersFromDb(String id){//if no data return null..it return first phone number if first not available then send second phone
        try (PersonRecordDatabase db=new PersonRecordDatabase(getApplicationContext());
             Cursor cursor = db.getData("SELECT " +db.COL_7_ACTIVE_PHONE1+" , "+db.COL_11_ACTIVE_PHONE2 + " FROM " + db.TABLE_NAME1 + " WHERE ID='" + id + "'"))
        {
            if (cursor != null) {//which ever phone is available that phone will be send
                cursor.moveToFirst();
                   if (cursor.getString(0).length() == 10){
                       return cursor.getString(0);
                    }

                if (cursor.getString(1).length() == 10){
                    return cursor.getString(1);
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
             if(!createAndDisplayCurrentInvoiceAndUpdateArrayToDelete(id)) return false;
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
 public boolean createAndDisplayCurrentInvoiceAndUpdateArrayToDelete(String id){

     absolutePathArrayToDelete[1]=createCurrentInvoiceAndReturnAbsolutePath(id);//when user view this pdf then if user close the app then created file should be deleted so updating absolutePathPdfToDelete to delete on destroy
            try {
                if (absolutePathArrayToDelete[1] != null) {
                     binding.pdfView.fromFile(new File(absolutePathArrayToDelete[1])).load();
                    return true;
                }else return false;

            }catch (Exception e){
                e.printStackTrace();
                Log.d(this.getClass().getSimpleName(),"exception occurred in method "+Thread.currentThread().getStackTrace()[2].getMethodName());
                return false;
            }
    }
 public String createCurrentInvoiceAndReturnAbsolutePath(String id) {//return null when error
        try {
            byte indicator = (byte) get_indicator(id);
            boolean[] errorDetection = {false};//when ever exception occur it will be updated to true in method so it indicate error occured or not
            String absoluteFilePath = null;
            MakePdf makePdf = new MakePdf();
            if (!makePdf.createPage1(MakePdf.defaultPageWidth, MakePdf.defaultPageHeight, 1))
                return null;//created page 1
            float[] columnWidth = getColumnWidthBasedOnIndicator(indicator, errorDetection);
            String[] headerAccordingToIndicator = getWagesHeadersFromDbBasedOnIndicator(id, indicator, errorDetection);//THIS SHOULD BE TOP at arrayOfTotalWagesDepositRateAccordingToIndicator   TO AVOID INDEX EXCEPTION
            String[][] recyclerViewWagesdata = getAllWagesDetailsFromDbBasedOnIndicator(id, indicator, errorDetection);//it amy return null   when no data
            String[][] recyclerViewDepositdata = getAllDepositFromDb(id, errorDetection);//it amy return null   when no data

            if (!makePdf.writeSentenceWithoutLines(new String[]{""}, new float[]{100f}, false, (byte) 0, (byte) 0))
                return null;//just for space

            if (!makePdf.writeSentenceWithoutLines(getPersonDetailsForCurrentInvoice(id), new float[]{40f, 10f, 20f, 30f}, true, (byte) 0, (byte) 0))
                return null;//name,id,date,future invoice number

            if (errorDetection[0] == false) {

                if (recyclerViewDepositdata != null) {//null means data not present
                    if (!makePdf.makeTable(new String[]{"DATE", "DEPOSIT", "REMARKS"}, recyclerViewDepositdata, new float[]{12f, 12f, 76f}, 9, true))
                        return null;
                }

                if (recyclerViewWagesdata != null) {//null means data not present
                    if (!makePdf.makeTable(headerAccordingToIndicator, recyclerViewWagesdata, columnWidth, 9, false))
                        return null;
                } else {//create dummy header with no data
                    if (!makePdf.makeTable(headerAccordingToIndicator, new String[][]{{}}, columnWidth, 9, false))
                        return null;
                }

                if (!addRowsAndWriteToPDF(indicator, makePdf, 38)) return null;

                if (!makePdf.createdPageFinish2())
                    return null;//after finish page we cannot write to it

                //while creating current pdf then its file name should be same because if user click on current PDF button repeatedly then many file will be create in device which is useless.so to avoid that file name is kept same so that whenever user click current pdf button then new file will be replaced with old file so it is necessary to keep same file name.if file name is unique then many file will be created in device
                absoluteFilePath = makePdf.createFileToSavePdfDocumentAndReturnFileAbsolutePath3(getExternalFilesDir(null).toString(), "id" + id + "currentInvoice");//we have to return filename  view pdf using file path
                if (!makePdf.closeDocumentLastOperation4()) return null;
            }
            return absoluteFilePath;
        }catch (Exception x){
            x.printStackTrace();
            return null;
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
    public String[] getPersonDetailsForCurrentInvoice(String id) {
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
    public void displayDialogMessage(String title, String message) {
        AlertDialog.Builder showDataFromDataBase=new AlertDialog.Builder(PdfViewerOperation.this);
        showDataFromDataBase.setCancelable(true);
        showDataFromDataBase.setTitle(title);
        showDataFromDataBase.setMessage(message);
        showDataFromDataBase.setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss());
        showDataFromDataBase.create().show();
    }
    private String convertBytesToFileForSharingAndReturnAbsolutePath(byte [] pdfByte, String id){//return null when error. Converts the array of bytes into a File
        if(pdfByte==null || id==null){//to prevent file create in device when pdfByte is null
            return null;
        }
        try {
            File folder = new File(getExternalFilesDir(null) + "/acBookPDF");//create directory
            if (!folder.exists()) {//if folder not exist then create folder
                folder.mkdir();//File createNewFile() method returns true if new file is created and false if file already exists.
            }
            File file = new File(getExternalFilesDir(null) + "/acBookPDF/" + generateUniqueFileName(id) + ".pdf");//path of pdf file where it is saved in device and file is created


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
    private String generateUniqueFileName(String id) {//file name will always be unique
        try {
            final Calendar current = Calendar.getInstance();//to get current date and time
            Date d = Calendar.getInstance().getTime();//To get time
            SimpleDateFormat sdf = new SimpleDateFormat("hhmmssa");//a stands for is AM or PM.example which make file unique 091659am which is unique
            return "id" + id + "date" + current.get(Calendar.DAY_OF_MONTH) + "_" + (current.get(Calendar.MONTH) + 1) + "_" + current.get(Calendar.YEAR) + "at" + sdf.format(d);
        }catch (Exception x){
            x.printStackTrace();
            return "error";
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(absolutePathArrayToDelete[0] !=null){//manually delete pdf1 or pdf2 index 0
             if(!deletePdfFromDevice(absolutePathArrayToDelete[0])){
                Log.d(this.getClass().getSimpleName(),"failed to delete pdf file from device on destroy method");
            }
        }
        if(absolutePathArrayToDelete[1] !=null){//manually delete currentinvoiceorpdfindex 1
            if(!deletePdfFromDevice(absolutePathArrayToDelete[1])){
                Log.d(this.getClass().getSimpleName(),"failed to delete pdf file from device on destroy method");
            }
        }
        if(absolutePathArrayToDelete[2] !=null){//manually delete image index 2
            if(!deletePdfFromDevice(absolutePathArrayToDelete[2])){
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
    private boolean checkPermissionForSMS() {//checking for permission of mic and external storage
        if((ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS)==PackageManager.PERMISSION_GRANTED)) {
            return true;
        }else
            return false;
    }
    private boolean checkPermissionForInternalAndExternal() {//checking for permission of mic and external storage
        if((ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED)) {
            return true;
        }else
            return false;
    }
    public void showSoftKeyboardByForced() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);//working
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }catch (Exception e){
            e.printStackTrace();
        }


//       public void showSoftKeyboardByForced(View searchView ) {//code link https://developer.android.com/training/keyboard-input/visibility#java
//        if (searchView.requestFocus()) {
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT);
//        }


 //                       InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);//open keyboard
//                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);

//                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);//to close keyboard
//                        imm.hideSoftInputFromWindow(view.getWindowToken(),0);
    }
}