package amar.das.acbook.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteBlobTooBigException;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import amar.das.acbook.R;
import amar.das.acbook.databinding.ActivityPdfViewerBinding;
import amar.das.acbook.Database;

import amar.das.acbook.pdfgenerator.MakePdf;
import amar.das.acbook.progressdialog.ProgressDialogHelper;
import amar.das.acbook.textfilegenerator.TextFile;
import amar.das.acbook.utility.MyUtility;

public class PdfViewerOperationActivity extends AppCompatActivity {
    ActivityPdfViewerBinding binding;
    byte whichPdfIndicatorChangesDynamically;
    String fromIntentPersonId;
    public static String pdfFolderName="acBookPDF";
    String currentInvoiceFileName="currentInvoice";

    String invoiceFileName="invoice";
    //ActivityResultLauncher<Intent> sharePdfLauncher;
    //String[] absolutePathArrayToDelete=new String[4];//index 0 may contain path of pdf1 or 2 and index 1 may contain path of pdf3 , and both string array index may contain pdf1orpdf2 and pdf3.INDEX 2 contain  the path of image.index 3 is for text file to delete

    /*pdf1 and pdf2 are view by take directly bytes from db we don't create file in device to view where as current invoiceorpdf are view by creating file in device then viewing
    * and for sharing pdf1 and pdf2 files is created in device then share also for invoiceorpdf file is created in device then share */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0); //we have used overridePendingTransition(), it is used to remove activity create animation while re-creating activity.
        binding = ActivityPdfViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ProgressDialogHelper progressBar = new ProgressDialogHelper( this);

//        sharePdfLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),activityResult ->{//ActivityResultCallback it will execute when return from other intent
//            for (String path:absolutePathArrayToDelete){
//                if(path!=null){
//                   if(!MyUtility.deletePdfOrRecordingUsingPathFromDevice(path)){
//                       Log.d(this.getClass().getSimpleName(),"failed to delete file from device");
//                   }
//                }
//            }
//        });

        whichPdfIndicatorChangesDynamically = getIntent().getByteExtra("pdf1_or_2_or_3_for_blank_4",(byte) 4);//4 is default value to display blank pdf
        fromIntentPersonId = getIntent().getStringExtra("ID");

        boolean largeFileSizeIndicat[]={false};//by default no error
        if(onlyViewPdf(whichPdfIndicatorChangesDynamically,fromIntentPersonId,largeFileSizeIndicat)) {//by default it will show pdf according to whichPdfIndicatorChangesDynamically
            changeButtonColorBackgroundAsSelected(whichPdfIndicatorChangesDynamically);//set by default button as selected
        }else if (largeFileSizeIndicat[0]){
            displayDialogMessage("LARGE FILE SIZE", "CAN'T VIEW");
        }

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();//StrictMode is a developer tool which detects things you might be doing by accident and brings them to your attention so you can fix them.
        StrictMode.setVmPolicy(builder.build());

        binding.gobackPdfViewer.setOnClickListener(view -> {
            finish();//destroy current activity
            Intent intent=new Intent( PdfViewerOperationActivity.this,IndividualPersonDetailActivity.class);
            intent.putExtra("ID",fromIntentPersonId);
            startActivity(intent);// go back to previous Activity with updated activity so passing id to get particular person detail refresh
        });
        binding.downloadPdfBtn.setOnClickListener(view -> {
            if(whichPdfIndicatorChangesDynamically ==(byte)1 || whichPdfIndicatorChangesDynamically == (byte)2){
                boolean largeFileSizeIndicator[]={false};//by default no error
                //directly taking pdfByte SO THAT we don't need to create extra file and deleted
                if(downloadPdfUsingAbsPathOrByte(null,getPdfByteFromDb(whichPdfIndicatorChangesDynamically, fromIntentPersonId,largeFileSizeIndicator),fromIntentPersonId)){
                    displayDialogMessage("DOWNLOADED","INVOICE\nID: "+fromIntentPersonId+"\nIN DOWNLOAD FOLDER");
                }else{
                    Toast.makeText(this, "SOMETHING WENT WRONG CANNOT DOWNLOAD", Toast.LENGTH_LONG).show();
                }
            }else if(whichPdfIndicatorChangesDynamically ==(byte) 3){
//                absolutePathArrayToDelete[1]= createCurrentInvoiceAndReturnFile(fromIntentPersonId).getAbsolutePath();
//                if(downloadPdfUsingAbsPathOrByte(absolutePathArrayToDelete[1],null,fromIntentPersonId)){
//                    displayDialogMessage("DOWNLOADED","CURRENT INVOICE\nID: "+fromIntentPersonId+"\nIN DOWNLOAD FOLDER");
//                }else{
//                    Toast.makeText(this, "SOMETHING WENT WRONG CANNOT DOWNLOAD", Toast.LENGTH_LONG).show();
//                }
//                if(MyUtility.deletePdfOrRecordingUsingPathFromDevice(absolutePathArrayToDelete[1])){//manually delete the generated file from app private storage because this file is downloaded and stored in download folder so deleting it otherwise same file will be twice.it will be deleted when error occurred or not
//                    absolutePathArrayToDelete[1]=null; //after file deleted set null
//                }

                String pdfAbsolutePath=createCurrentInvoiceAndReturnFile(fromIntentPersonId).getAbsolutePath();
                if(downloadPdfUsingAbsPathOrByte(pdfAbsolutePath,null,fromIntentPersonId)){
                    displayDialogMessage("DOWNLOADED","CURRENT INVOICE\nID: "+fromIntentPersonId+"\nIN DOWNLOAD FOLDER");
                }else{
                    Toast.makeText(this, "SOMETHING WENT WRONG CANNOT DOWNLOAD", Toast.LENGTH_LONG).show();
                }
                //below operation not perform because on destroy this created file in acbookPdf folder will be deleted from acbookPdf folder

//                if(MyUtility.deletePdfOrRecordingUsingPathFromDevice(absolutePathArrayToDelete[1])){//manually delete the generated file from app private storage because this file is downloaded and stored in download folder so deleting it otherwise same file will be twice.it will be deleted when error occurred or not
//                    absolutePathArrayToDelete[1]=null; //after file deleted set null
//                 }

                }else//when whichPdfIndicatorChangesDynamically is 4
                displayDialogMessage("PLEASE SELECT","INVOICE TO DOWNLOAD");
        });

        binding.sharePdfBtn.setOnClickListener(view -> {
            ExecutorService backgroundTask = Executors.newSingleThreadExecutor();//Executors.newSingleThreadExecutor() creates a thread pool with a single thread. This means that only one task can be executed at a time. If there are more than one task waiting to be executed, the remaining tasks will be queued until the current task is finished.
            backgroundTask.execute(() -> {
                //pre execute
                if(whichPdfIndicatorChangesDynamically==3) {//3 is referring to current invoice.Progress bar is needed because it take time to create.Where as 1 and 2 don't take time so no need of progress bar
                    runOnUiThread(() -> progressBar.showProgressBar());
                }
                //background task
                try {
                    File pdfFile=null;

                    if(whichPdfIndicatorChangesDynamically == (byte) 1 || whichPdfIndicatorChangesDynamically == (byte) 2){//1 or 2
                        boolean largeFileSizeIndicator[]={false};
                        pdfFile= convertBytesToFileForSharingAndReturnFile(getPdfByteFromDb(whichPdfIndicatorChangesDynamically, fromIntentPersonId,largeFileSizeIndicator),invoiceFileName, fromIntentPersonId);
                       // absolutePathArrayToDelete[0]=pdfFile.getAbsolutePath();
                    }else if(whichPdfIndicatorChangesDynamically == (byte) 3){
                        pdfFile= createCurrentInvoiceAndReturnFile(fromIntentPersonId);
                       // absolutePathArrayToDelete[1]=pdfFile.getAbsolutePath();
                    }
                    /*note:using whatsapp we cannot send pdf directly to whatsapp phone number like message for that we required approval so not using that feature*/
                    if(whichPdfIndicatorChangesDynamically <=(byte)3){// this will only execute when value is 1,2 or 3
                        if (!shareFileToAnyApp(pdfFile,"application/pdf","ID "+fromIntentPersonId+" SHARE PDF USING")) {//open intent to share
                            runOnUiThread(() -> displayDialogMessage("CANNOT","SHARE PDF"));
                        }
                    }else if(whichPdfIndicatorChangesDynamically==(byte)4){//4 represent blank
                        runOnUiThread(() -> displayDialogMessage("PLEASE SELECT","INVOICE TO SHARE"));
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }

                runOnUiThread(() -> progressBar.hideProgressBar()); //post execute

            });backgroundTask.shutdown();//when all task completed then only shutdown
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
                    boolean errorIndicator=false;
                    switch (itemName) {
                        case "A/C": {
                            if (!openCustomAlertDialogToShareTextToAnyAppOrDirectlyToWhatsApp(getAccountDetailsFromDb(fromIntentPersonId,getIdNamePhone(fromIntentPersonId)), getResources().getString(R.string.enter_amount), fromIntentPersonId, true)) { //getAccountDetailsFromDb()if this method return null then alertdialog will return false
                                errorIndicator=true;
                            }
                        }break;
                        case "PHONE NUMBER": {
                            String message = activePhoneNumberToShare(fromIntentPersonId);//if there is no phone number then return null also when exception
                            if (message != null) {//if no phone number then don't open dialog because its useless
                                if (!openCustomAlertDialogToShareTextToAnyAppOrDirectlyToWhatsApp(message, getResources().getString(R.string.enter_amount), fromIntentPersonId, true)) {
                                    errorIndicator=true;
                                 }
                            } else {
                                displayDialogMessage("",getResources().getString(R.string.no_phone_number));
                             }
                        }break;
                        case "CURRENT INVOICE":{//here thread is not used because it takes more time to load when data is more but usually data is less so it will take 1 sec to load data.so not used thread because it will take more time and extra code
                            MyUtility.snackBar(view,getResources().getString(R.string.please_wait_a_few_seconds));
                             if (!openAlertDialogToShareTextToAnyAppOrDirectlyToWhatsApp(getMessageForCurrentInvoice(fromIntentPersonId,true),currentInvoiceFileName, fromIntentPersonId, true)) {  //getMessageForCurrentInvoice()if this method return null then alertdialog will return false
                                errorIndicator=true;
                             }
                        }break;
                        case "IMAGE": {
                            if (!shareImageAndMessageToAnyApp(getIdNamePhone(fromIntentPersonId),fromIntentPersonId)) {
                                errorIndicator=true;
                            }
                        }break;
                        case "SHARE ALL": {
                            ExecutorService backgroundTask = Executors.newSingleThreadExecutor();//Executors.newSingleThreadExecutor() creates a thread pool with a single thread. This means that only one task can be executed at a time. If there are more than one task waiting to be executed, the remaining tasks will be queued until the current task is finished.
                             backgroundTask.execute(() -> {
                                 //pre execute
                                 runOnUiThread(() -> progressBar.showProgressBar());

                                 //background execute
                                 if (!shareAllData(fromIntentPersonId)) {
                                    runOnUiThread(() -> displayDialogMessage("SOMETHING","WENT WRONG"));
                                 }
                                 //post execute
                                 runOnUiThread(() -> progressBar.hideProgressBar());
                             });
                        }break;
                    }
                    if(errorIndicator){//IF ANY ERROR display dialogbox
                        displayDialogMessage("SOMETHING","WENT WRONG");
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
            boolean largeFileSizeIndicator[]={false};//by default no error
            if(onlyViewPdf((byte)1,fromIntentPersonId,largeFileSizeIndicator)){
                changeButtonColorBackgroundAsSelected((byte)1);//WHEN PDF VIEWED THEN SET BUTTON AS SELECTED
                whichPdfIndicatorChangesDynamically = (byte)1;//this will be updated when SUCCESSFULLY pdf is displayed and  user click pdf1 for share and download
            } else if (largeFileSizeIndicator[0]) {//if true
                displayDialogMessage("LARGE FILE SIZE", "CAN'T VIEW");
            }else{
                displayDialogMessage("NO PREVIOUS INVOICE", "IT WILL BE AVAILABLE WHEN AGAIN CALCULATION IS DONE");
            }
        });

        binding.pdf2Btn.setOnClickListener(view -> {
            boolean largeFileSizeIndicator[]={false};//by default no error
            if(onlyViewPdf((byte)2,fromIntentPersonId,largeFileSizeIndicator)){
                changeButtonColorBackgroundAsSelected((byte)2);
                whichPdfIndicatorChangesDynamically = (byte)2;//this will be updated when SUCCESSFULLY pdf is displayed and  user click pdf1 for share and download
            } else if (largeFileSizeIndicator[0]) {//if true
               // Toast.makeText(this, "LARGE FILE SIZE CAN'T VIEW", Toast.LENGTH_LONG).show();
                displayDialogMessage("LARGE FILE SIZE", "CAN'T VIEW");
            } else {
                displayDialogMessage("NO PREVIOUS INVOICE", "IT WILL BE AVAILABLE WHEN CALCULATION IS DONE");
            }
            });

        binding.currentPdfBtn.setOnClickListener(view -> {
            ExecutorService backgroundTask = Executors.newSingleThreadExecutor();//Executors.newSingleThreadExecutor() creates a thread pool with a single thread. This means that only one task can be executed at a time. If there are more than one task waiting to be executed, the remaining tasks will be queued until the current task is finished.
            backgroundTask.execute(() -> {
                runOnUiThread(() -> progressBar.showProgressBar());//pre execute

                //execute background
                boolean largeFileSizeIndicator[]={false};//by default no error
                if(onlyViewPdf((byte)3,fromIntentPersonId,largeFileSizeIndicator)){
                    changeButtonColorBackgroundAsSelected((byte)3);
                    whichPdfIndicatorChangesDynamically = (byte)3;//this will be updated when SUCCESSFULLY pdf is displayed and  user click pdf1 for share and download
                } else if (largeFileSizeIndicator[0]) {
                    runOnUiThread(() ->displayDialogMessage("LARGE FILE SIZE", "CAN'T VIEW"));
                } else{
                    runOnUiThread(() ->displayDialogMessage("ERROR OCCURRED", "WHILE DISPLAYING INVOICE"));
                }

                runOnUiThread(() -> progressBar.hideProgressBar()); //post execute

            });backgroundTask.shutdown();//when all task completed then only shutdown

        });
    }
    private boolean downloadPdfUsingAbsPathOrByte(String absolutePath, byte[] pdfByte,String id) {
        try{
            if(id==null){
                return false;
            }
            if(pdfByte!=null){
               return downloadPdfUsingByteInDownloadFolder("DOWNLOADED"+ MyUtility.generateUniqueFileNameByTakingDateTime(id,invoiceFileName)+".pdf",pdfByte);
            }

            if(absolutePath!= null){
                byte[] convertedBytes = Files.readAllBytes(Paths.get(absolutePath));// This code uses the Files.readAllBytes() method from the java.nio.file package to read all the bytes from the file specified by the absolute path into a byte array. This method is more concise and efficient .
                return downloadPdfUsingByteInDownloadFolder("DOWNLOADED"+MyUtility.generateUniqueFileNameByTakingDateTime(id,currentInvoiceFileName)+".pdf",convertedBytes);
            }
            return false;
        }catch(Exception x){
            x.printStackTrace();
            return false;
        }
    }
    private boolean downloadPdfUsingByteInDownloadFolder(String filename, byte[] pdfContent) {//this method to save it to the download folder.
        if(filename==null || pdfContent==null){
            return false;
        }
        try {
          if(MyUtility.checkPermissionForReadAndWriteToExternalStorage(getApplicationContext())){
               File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
               File pdfFile = new File(downloadsFolder, filename);

               FileOutputStream outputStream = new FileOutputStream(pdfFile);
               outputStream.write(pdfContent);
               outputStream.close();
            return true;
        }else{
            Toast.makeText(PdfViewerOperationActivity.this, "EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(PdfViewerOperationActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 42);
            return false;
            }
        }catch (IOException e) {
            e.printStackTrace();
             return false;
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }
    }
    private boolean shareAllData(String id) {
        StringBuilder sb=new StringBuilder();
        try{
            String[] message=getPersonDetailsForCurrentInvoice(id);//id,name,invoice number,date
            sb.append(message[1]).append("\n");
            sb.append(message[0]).append("\n");//setting first id value
            sb.append(message[2]).append("\n");
            sb.append(message[3]).append("\n");

            String phoneNumber=MyUtility.getActivePhoneNumbersFromDb(id,getApplicationContext());
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

            sb.append(getAllSumAndDepositAndWagesDetails(id));//all wages and deposit data
            sb.append("------------FINISH--------------");
            return shareLargeDataAsTextFileToAnyApp(id, TextFile.allDataTextFileName,sb.toString(),"text/plain","ID "+id+" SHARE TEXT FILE USING");//sharing all data excluding image

        }catch (Exception x){
            x.printStackTrace();
            return false;
        }
    }
    private String getOtherDetails(String id){
        try(Database db=new Database(getBaseContext());
            Cursor cursor = db.getData("SELECT " +Database.COL_6_AADHAAR_NUMBER + " FROM " + Database.TABLE_NAME1 + " WHERE "+Database.COL_1_ID+"='" + id + "'")){
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
    private boolean shareImageAndMessageToAnyApp(String message, String id) {
        //if(message==null|| id==null || sharePdfLauncher ==null){
        if(message==null|| id==null){
            return false;
        }
        try(Database db=Database.getInstance(getBaseContext());
            Cursor cursor = db.getData("SELECT " +Database.COL_10_IMAGE + " FROM " + Database.TABLE_NAME1 + " WHERE "+Database.COL_1_ID+"='" + id + "'")){

            if(cursor != null){
                cursor.moveToFirst();
            }else return false;

            byte[] image=cursor.getBlob(0);
            if (image!=null) {
                if(MyUtility.checkPermissionForReadAndWriteToExternalStorage(getBaseContext())) {
                    //this code will be used when launcher is used
//                    Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length); //for resizing image -Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 65, 62, false);//image size
//                   //Why do we need to Save the image to external storage? When sharing an image with other apps in Android, you need to provide a file URI that points to the location of the image on the device's storage. If you don't save the image to external storage, you won't be able to share it with other apps.In addition, apps are not allowed to share files directly from their internal storage with other apps. This is a security measure implemented by Android to prevent apps from accessing each other's data without explicit user permission.
//                    File file = new File(getExternalCacheDir(), "image.jpg");//creating file in cache directory file name cache path.image.jpg.getExternalCacheDir() is a method in Android's Context class that returns a File object representing the external storage directory specific to your app for storing cache files. This directory is automatically created for your app and is private to your app, meaning that other apps cannot access its contents.Cache files are temporary files that are used to improve the performance of your app. By storing files that your app frequently uses in the cache directory, you can avoid repeatedly reading or downloading those files from a remote source, which can slow down your app's performance.The getExternalCacheDir() method returns a File object that represents the path to your app's external cache directory, which you can use to save cache files or other temporary files that your app needs to access quickly. For example, when sharing an image, you can save the image to this directory before sharing it with other apps.
//                    FileOutputStream outputStream = new FileOutputStream(file);
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);//image quality
//                    outputStream.flush();
//                    outputStream.close();
//                    //In Android 12, you cannot use Uri.fromFile() to get the URI for a file. Instead, you should use FileProvider.getUriForFile() to get the URI for the file.
//                    Uri fileUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);//**to access file uri FileProvider.getUriForFile() is compulsory from if your target sdk version is 24 or greater otherwise cannot access
//
//                    Intent shareIntent = new Intent(Intent.ACTION_SEND);//sharing
//                    shareIntent.setType("image/*");//No, there is no need to add flags to the intent. The intent created is simply used to share the text file, and the file is deleted after sharing. Adding flags to the intent would not have any impact on sharing the file.
//                    shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);//EXTRA_STREAM FOR SHARE LARGE DATA like image ,text
//                    shareIntent.putExtra(Intent.EXTRA_TEXT,message);
//                    sharePdfLauncher.launch(Intent.createChooser(shareIntent, getResources().getString(R.string.share_image_using)));//Intent.createChooser creates dialog to choose app to share data and after shared pdf launcher will execute to delete the image
//                    absolutePathArrayToDelete[2] = file.getAbsolutePath();//storing absolute path to delete the image


                    Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length); //for resizing image -Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 65, 62, false);//image size
                    //Why do we need to Save the image to external storage? When sharing an image with other apps in Android, you need to provide a file URI that points to the location of the image on the device's storage. If you don't save the image to external storage, you won't be able to share it with other apps.In addition, apps are not allowed to share files directly from their internal storage with other apps. This is a security measure implemented by Android to prevent apps from accessing each other's data without explicit user permission.
                    File file = new File(getExternalCacheDir(), "image.jpg");//creating file in cache directory file name cache path.image.jpg.getExternalCacheDir() is a method in Android's Context class that returns a File object representing the external storage directory specific to your app for storing cache files. This directory is automatically created for your app and is private to your app, meaning that other apps cannot access its contents.Cache files are temporary files that are used to improve the performance of your app. By storing files that your app frequently uses in the cache directory, you can avoid repeatedly reading or downloading those files from a remote source, which can slow down your app's performance.The getExternalCacheDir() method returns a File object that represents the path to your app's external cache directory, which you can use to save cache files or other temporary files that your app needs to access quickly. For example, when sharing an image, you can save the image to this directory before sharing it with other apps.
                    FileOutputStream outputStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);//image quality
                    outputStream.flush();
                    outputStream.close();
                    //In Android 12, you cannot use Uri.fromFile() to get the URI for a file. Instead, you should use FileProvider.getUriForFile() to get the URI for the file.This method is used to share a file with another app using a content URI
                    Uri fileUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);//**to access file uri FileProvider.getUriForFile() is compulsory from if your target sdk version is 24 or greater otherwise cannot access

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);//sharing
                    shareIntent.setType("image/*");//No, there is no need to add flags to the intent. The intent created is simply used to share the text file, and the file is deleted after sharing. Adding flags to the intent would not have any impact on sharing the file.
                    shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);//EXTRA_STREAM FOR SHARE LARGE DATA like image ,text
                    shareIntent.putExtra(Intent.EXTRA_TEXT,message);

                    Intent chooser=Intent.createChooser(shareIntent, getResources().getString(R.string.share_image_using));//Intent.createChooser creates dialog to choose app to share data
                    chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(chooser);//start chooser dialog

                }else{
                    Toast.makeText(PdfViewerOperationActivity.this, "EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(PdfViewerOperationActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 41);
                    return false;
                }
            }else{
                Toast.makeText(PdfViewerOperationActivity.this, "NO IMAGE", Toast.LENGTH_LONG).show();
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
            if(MyUtility.getActivePhoneNumbersFromDb(id,getApplicationContext())!= null) {//checking phoneNumber is there or not
                return getIdNamePhone(id);
            }else{
                return null;
            }
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
    public boolean openAlertDialogToShareTextToAnyAppOrDirectlyToWhatsApp(String message,String fileName,String id, boolean defaultTrueForOpenAnyAppAndFalseForWhatsApp) {
        if(message==null || id==null){
            return false;
        }
        boolean[] success= {true};
        try{
            AlertDialog.Builder dialogBuilder = new  AlertDialog.Builder(this);
            dialogBuilder.setCancelable(true);
            dialogBuilder.setTitle(getResources().getString(R.string.message));
            dialogBuilder.setMessage(message);

            dialogBuilder.setNegativeButton(getResources().getString(R.string.send_only_total_to_contact), (dialogInterface, i) -> {
                success[0]= sendMessageToContact(id,getMessageOnlyInvoiceDetailsAndTotalWagesAndDeposit(id));//sending only total wages and deposit due to long text cannot send as sms
                      if(!success[0]){//if no contact then send full txt file message to any app
                          success[0]= shareLargeDataAsTextFileToAnyApp(id,fileName,message,"text/plain","ID "+id+" SHARE TEXT FILE USING");
                      }
                dialogInterface.dismiss();
            });
            dialogBuilder.setPositiveButtonIcon(AppCompatResources.getDrawable(getBaseContext(),R.drawable.baseline_whatsapp_24));
            dialogBuilder.setPositiveButton( "", (dialogInterface, i) -> {
                if(defaultTrueForOpenAnyAppAndFalseForWhatsApp) {
                    success[0]= shareLargeDataAsTextFileToAnyApp(id,fileName,message,"text/plain","ID "+id+" SHARE TEXT FILE USING");

                }else{
                    success[0]= sendMessageDirectToWhatsAppOrAnyApp(id,fileName,message,"text/plain","ID "+id+" SHARE TEXT FILE USING");
                }
                dialogInterface.dismiss();
            });

              dialogBuilder.create().show();
         }catch (Exception x){
            x.printStackTrace();
            return false;
        }
        return success[0];
    }
    private boolean sendMessageToContact(String id, String message){
        if(id==null || message==null){
            return false;
        }
        try{
            return sendSMSUsingIntentToPhoneNumber(message,id);
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }
    }
    private boolean sendMessageDirectToWhatsAppOrAnyApp(String id,String fileName,String message,String mimeType,String title) {//disadvantage-when phone number has no whatsapp then cant send message.
        //if(id==null|| message==null|| mimeType==null||title==null||sharePdfLauncher==null){
       if(id==null|| message==null|| mimeType==null||title==null ){
           return false;
       }
        try{
            String activePhone=MyUtility.getActivePhoneNumbersFromDb(id,getApplicationContext());//for opening whatsapp we have to check phone number is available or not
            if(activePhone!=null) {
                if(!shareMessageDirectlyToWhatsApp(message,activePhone)){//if fail
                     return shareLargeDataAsTextFileToAnyApp(id,fileName,message,mimeType,title);
                }
            }else{//if no phone number then share text to any app
                return shareLargeDataAsTextFileToAnyApp(id,fileName,message,mimeType,title);
            }
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
            String phoneNumber=MyUtility.getActivePhoneNumbersFromDb(id,getBaseContext());
            if(phoneNumber!=null){
                if(checkPermissionForSMS()){   //send an SMS using an intent
                    Intent intent=new Intent(Intent.ACTION_VIEW,Uri.fromParts("sms",phoneNumber,null));//The first parameter specifies the protocol ("sms"), the second parameter specifies the recipient's phone number.URI can be used to launch the SMS app with a pre-filled recipient phone number.
                    intent.putExtra("sms_body",message);//here adding flag not required
                    startActivity(intent);
                    return true;
                }else{
                    Toast.makeText(PdfViewerOperationActivity.this, "SMS PERMISSION REQUIRED", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(PdfViewerOperationActivity.this, new String[]{Manifest.permission.SEND_SMS}, 31);
                    return false;
                }
            }else{
                Toast.makeText(this, getResources().getString(R.string.no_phone_number), Toast.LENGTH_LONG).show();
                return false;
            }
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
    private String getMessageForCurrentInvoice(String id,boolean trueForAllAndFalseForOnlyPersonDetails){//return null when exception
        StringBuilder sb=new StringBuilder();
        try{
            String[] message=getPersonDetailsForCurrentInvoice(id);
            sb.append(message[1]).append("\n");
            sb.append(message[0]).append("\n");//setting first id value
            sb.append(message[2]).append("\n");
            sb.append(message[3]).append("\n");

            if(trueForAllAndFalseForOnlyPersonDetails) {//if true then send all details including wages and deposit
                sb.append(getAllSumAndDepositAndWagesDetails(id));
                sb.append("------------FINISH--------------");
            }

            return sb.toString();
        }catch (Exception x){
            x.printStackTrace();
            return null;
        }
    }
    private String getMessageOnlyInvoiceDetailsAndTotalWagesAndDeposit(String id){
        if(id==null){
            return "id null";
        }
                StringBuilder sb=new StringBuilder();
                sb.append(getMessageForCurrentInvoice(id,false));//if this method getMessageForCurrentInvoice() return null then string builder will append null instead of throwing NULL POINTER EXCEPTION
                sb.append(MyUtility.getMessageOnlyTotalWagesAndDeposit(id,getBaseContext()));

            return sb.toString();
    }
    private String getAllSumAndDepositAndWagesDetails(String id) {
        StringBuilder sb=new StringBuilder();
        try{
            byte indicator=MyUtility.get_indicator(getBaseContext(),id);
            boolean[] errorDetection={false};//when ever exception occur it will be updated to true in method so it indicate error occurred or not
            String[] header = MyUtility.getWagesHeadersFromDbBasedOnIndicator(getBaseContext(),id, indicator, errorDetection);//THIS SHOULD BE TOP at arrayOfTotalWagesDepositRateBasedOnIndicator   TO AVOID INDEX EXCEPTION
            String[][] recyclerViewWagesData = MyUtility.getAllWagesDetailsFromDbBasedOnIndicator(getBaseContext(),id, indicator, errorDetection);//it amy return null   when no data
            String[][] recyclerViewDepositData = MyUtility.getAllDepositFromDb(getBaseContext(),id, errorDetection);//it amy return null   when no data
            int[] arrayOfTotalWagesDepositRateBasedOnIndicator= MyUtility.getSumOfTotalWagesDepositRateDaysWorkedBasedOnIndicator(getBaseContext(),id,indicator,errorDetection);//if error cause errorDetection will be set true

            if(!errorDetection[0]){//if(errorDetection[0]==false){

                sb.append("-----------").append(getResources().getString(R.string.total_sum)).append("-----------");
                sb.append(MyUtility.getTotalWagesDepositAndWorkingAccordingToIndicator(indicator,header,arrayOfTotalWagesDepositRateBasedOnIndicator,recyclerViewDepositData!=null));


                if (recyclerViewDepositData != null) {//null means data not present so don't add deposit in text
                    int rowLength=recyclerViewDepositData.length;
                    int columnLength=recyclerViewDepositData[0].length;
                    sb.append("\n-------------------------------");

                    sb.append("\n").append(getResources().getString(R.string.total_no_of_deposit_entries)).append(" ").append(rowLength).append("\n\n");
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

                    if (recyclerViewDepositData == null) {
                        sb.append("\n-------------------------------");//add space when deposit is null for better readability.because space is not added when recyclerViewDepositData is null
                    }else{
                        sb.append("-------------------------------");
                    }

                    sb.append("\n").append(getResources().getString(R.string.total_no_of_wages_entries)).append(" ").append(rowLength).append("\n\n");

                    for (int row = 0; row < rowLength; row++) {
                        sb.append(row + 1).append("-> ");
                        for (int col = 0; col < columnLength; col++){

                            if((columnLength-1)!=col) sb.append(recyclerViewWagesData[row][col]).append("  "); else sb.append("\n").append(recyclerViewWagesData[row][col]);

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
//    private String getTotalWagesDepositAndWorkingAccordingToIndicator(byte indicator, String[] headerBasedOnIndicator, int[] arrayOfTotalWagesDepositRateAccordingToIndicator, boolean isDepositPresent) {
//    try{
//        /*
//         * return message like:
//         * TOTAL WAGES: 23,000
//         * TOTAL M: 59
//         * TOTAL DEPOSIT: 55,000*/
//        StringBuilder sb=new StringBuilder();
//        switch (indicator) {//based on indicator generate message
//                case 1:{sb.append("\nTOTAL ").append(headerBasedOnIndicator[1]).append(": ").append(MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[0])).append("\nTOTAL ").append(headerBasedOnIndicator[2]).append(": ").append(arrayOfTotalWagesDepositRateAccordingToIndicator[1]);}break;
//                case 2:{sb.append("\nTOTAL ").append(headerBasedOnIndicator[1]).append(": ").append(MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[0])).append("\nTOTAL ").append(headerBasedOnIndicator[2]).append(": ").append(arrayOfTotalWagesDepositRateAccordingToIndicator[1]).append("\nTOTAL ").append(headerBasedOnIndicator[3]).append(": ").append(arrayOfTotalWagesDepositRateAccordingToIndicator[2]);}break;
//                case 3:{sb.append("\nTOTAL ").append(headerBasedOnIndicator[1]).append(": ").append(MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[0])).append("\nTOTAL ").append(headerBasedOnIndicator[2]).append(": ").append(arrayOfTotalWagesDepositRateAccordingToIndicator[1]).append("\nTOTAL ").append(headerBasedOnIndicator[3]).append(": ").append(arrayOfTotalWagesDepositRateAccordingToIndicator[2]).append("\nTOTAL ").append(headerBasedOnIndicator[4]).append(": ").append(arrayOfTotalWagesDepositRateAccordingToIndicator[3]);}break;
//                case 4:{sb.append("\nTOTAL ").append(headerBasedOnIndicator[1]).append(": ").append(MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[0])).append("\nTOTAL ").append(headerBasedOnIndicator[2]).append(": ").append(arrayOfTotalWagesDepositRateAccordingToIndicator[1]).append("\nTOTAL ").append(headerBasedOnIndicator[3]).append(": ").append(arrayOfTotalWagesDepositRateAccordingToIndicator[2]).append("\nTOTAL ").append(headerBasedOnIndicator[4]).append(": ").append(arrayOfTotalWagesDepositRateAccordingToIndicator[3]).append("\nTOTAL ").append(headerBasedOnIndicator[5]).append(": ").append(arrayOfTotalWagesDepositRateAccordingToIndicator[4]);}break;
//                }
//
//        if(isDepositPresent){//if deposit present then only add
//            sb.append("\nTOTAL DEPOSIT: ").append(MyUtility.convertToIndianNumberSystem(arrayOfTotalWagesDepositRateAccordingToIndicator[indicator + 1]));//[indicator + 1] it is index of deposit
//        }
//        return sb.toString();
//
//    }catch (Exception x){
//        x.printStackTrace();
//        return "ERROR OCCURRED";
//    }
//    }
    private boolean openCustomAlertDialogToShareTextToAnyAppOrDirectlyToWhatsApp(String message, String input1Header,String id,boolean defaultTrueForOpenAnyAppAndFalseForWhatsApp) {//true is for Open anyApp
        if(message==null || input1Header==null){
            return false;
        }
        try {
            AlertDialog.Builder customDialogBuilder = new AlertDialog.Builder(PdfViewerOperationActivity.this);
            LayoutInflater inflater = LayoutInflater.from(PdfViewerOperationActivity.this);
            View myView = inflater.inflate(R.layout.take_two_input_from_user_layout, null);//myView contain all layout view ids
            customDialogBuilder.setView(myView);//set custom layout to alert dialog
            customDialogBuilder.setCancelable(false);//if user touch to other place then dialog will not be close
            AlertDialog dialog = customDialogBuilder.create();//customDialogBuilder variable cannot be use in inner class so creating another final variable  to use in inner class
            //ids
            TextView showMessage_tV = myView.findViewById(R.id.message_of_dialog), input1_tV = myView.findViewById(R.id.input1_header_dialog);
            EditText integerInput_Et = myView.findViewById(R.id.input1_edit_text_dialog), remarks_Et = myView.findViewById(R.id.remarks_dialog);
            Button share = myView.findViewById(R.id.share_button_dialog);
            showSoftKeyboardByForced();//open keyboard automatically by programmatically

            showMessage_tV.setText(message);//set text
            input1_tV.setText(input1Header);
            myView.findViewById(R.id.cancel_btn_dialog).setOnClickListener(view1 -> dialog.dismiss());

            share.setOnClickListener(view12 -> {//SHARE
                    String remarks = "", amount = "";
                    if (!TextUtils.isEmpty(integerInput_Et.getText().toString().trim())) {//textUtils checks for both null and empty string
                        amount = "\n\n" + getResources().getString(R.string.amount_rs) + " " + MyUtility.convertToIndianNumberSystem(Integer.parseInt(integerInput_Et.getText().toString().trim()));
                    }
                    if (!TextUtils.isEmpty(remarks_Et.getText().toString().trim())) {
                        remarks = "\n" + getResources().getString(R.string.remarks) + ": " + remarks_Et.getText().toString().trim();
                    }
                    if(defaultTrueForOpenAnyAppAndFalseForWhatsApp){
                        shareShortMessageToAnyApp(MyUtility.get12hrCurrentTimeAndDate() + "\n" + message + amount + remarks);
                    }else{//execute only when boolean value is false
                        String activePhone= MyUtility.getActivePhoneNumbersFromDb(id,getApplicationContext());//for opening whatsapp we have to check phone number is available or not
                        if(activePhone!=null) {
                            shareMessageDirectlyToWhatsApp(MyUtility.get12hrCurrentTimeAndDate() + "\n" + message + amount + remarks,activePhone);
                        }else{
                            Toast.makeText(this, getResources().getString(R.string.no_phone_number), Toast.LENGTH_LONG).show();
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
                     Toast.makeText(PdfViewerOperationActivity.this,  getResources().getString(R.string.message_copied), Toast.LENGTH_LONG).show();
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
        try (Database db=new Database(getApplicationContext());
             Cursor cursor = db.getData("SELECT "+Database.COL_3_BANKAC+" , "+Database.COL_4_IFSCCODE+" , "+Database.COL_5_BANKNAME+" , "+Database.COL_9_ACCOUNT_HOLDER_NAME+ " FROM " + Database.TABLE_NAME1 + " WHERE "+Database.COL_1_ID+"='" + id + "'"))
        {
            StringBuilder sb=new StringBuilder(idNamePhone);
            if (cursor != null) {//which ever phone is available that phone will be send
                cursor.moveToFirst();

                if(!cursor.getString(2).isEmpty()){//isEmpty() checks for empty string only and not for null
                    sb.append("\nBANK NAME: ").append(cursor.getString(2)).append("\n");
                }else{
                    sb.append("\nBANK NAME: null\n");
                }
                if(!cursor.getString(3).isEmpty()){
                    sb.append("\nA/C HOLDER NAME: ").append(cursor.getString(3)).append("\n");
                }else{
                    sb.append("A/C HOLDER NAME: null\n");
                }

                if(!cursor.getString(0).isEmpty()){
                    sb.append("\nA/C: ").append(convertToReadableNumber(cursor.getString(0))).append("\n\n");
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
        try (Database db=new Database(getApplicationContext());
             Cursor cursor = db.getData("SELECT " +Database.COL_2_NAME+ " FROM " + Database.TABLE_NAME1 + " WHERE "+Database.COL_1_ID+"='" + id + "'"))
        {
            StringBuilder sb=new StringBuilder();
            if (cursor != null) {//which ever phone is available that phone will be send
                cursor.moveToFirst();

                sb.append("ID: ").append(id).append("\n");
                if (!cursor.getString(0).isEmpty()){
                    sb.append("NAME: ").append(cursor.getString(0)).append("\n");
                }else{
                    sb.append("NAME: null\n");
                }

                String activePhoneNumber=MyUtility.getActivePhoneNumbersFromDb(id,getApplicationContext());
                if(activePhoneNumber != null) {
                    sb.append("PHONE: ").append(activePhoneNumber);
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
        return new String(arr).trim();
    }
    public boolean shareMessageDirectlyToWhatsApp(String message,String indianWhatsappNumber){
  if(message==null || indianWhatsappNumber==null){
      return false;
  }
  try {
      if (isInternetConnected(this)){//WE CAN SEND LARGE TEXT MESSAGE USING WHATSAPP
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
    public boolean shareLargeDataAsTextFileToAnyApp(String id,String fileName,String message,String mimeType,String title){
        //if(id==null|| message==null|| mimeType==null||title==null||sharePdfLauncher==null){
        if(id==null|| message==null|| mimeType==null||title==null){
            return false;
        }
        try { // create a file to store the data
            if(MyUtility.checkPermissionForReadAndWriteToExternalStorage(getApplicationContext())) {
                File file = new File(getExternalCacheDir(), MyUtility.generateUniqueFileNameByTakingDateTime(id,fileName) + ".txt");//creating txt file in cache directory file name  getExternalCacheDir() is a method in Android's Context class that returns a File object representing the external storage directory specific to your app for storing cache files. This directory is automatically created for your app and is private to your app, meaning that other apps cannot access its contents.Cache files are temporary files that are used to improve the performance of your app. By storing files that your app frequently uses in the cache directory, you can avoid repeatedly reading or downloading those files from a remote source, which can slow down your app's performance.The getExternalCacheDir() method returns a File object that represents the path to your app's external cache directory, which you can use to save cache files or other temporary files that your app needs to access quickly. For example, when sharing an image, you can save the image to this directory before sharing it with other apps.
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(message.getBytes());
                outputStream.close();
              // if (!shareFileToAnyApp(file.getAbsolutePath(), mimeType, title, sharePdfLauncher)) {//open intent to share
                if (!shareFileToAnyApp(file , mimeType, title)) {//open intent to share
                    Toast.makeText(this, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show();
                    return false;
                }
                //absolutePathArrayToDelete[3] = file.getAbsolutePath();//storing absolute path to delete the image
                return true;
            }else{
                Toast.makeText(PdfViewerOperationActivity.this, "EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(PdfViewerOperationActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 41);
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean shareShortMessageToAnyApp(String message){
      if(message==null) {
        return false;
       }
        try {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, message);
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//If we don't add the chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) line to set the FLAG_ACTIVITY_NEW_TASK flag, the behavior of the app when launching the chooser intent may depend on the context in which the sendMessageToAnyApp method is called.If the method is called from an activity that is already the root of a task, launching the chooser without the FLAG_ACTIVITY_NEW_TASK flag will simply add the chosen activity to the current task stack. This can lead to unexpected back stack behavior and may not be desirable if the user is expected to return to the same activity after sharing the message.On the other hand, if the method is called from an activity that is not the root of a task, launching the chooser without the FLAG_ACTIVITY_NEW_TASK flag will create a new task for the chooser and clear the previous task. This can also be unexpected and disruptive to the user's workflow.Therefore, setting the FLAG_ACTIVITY_NEW_TASK flag ensures consistent behavior regardless of the context in which the method is called, and is generally a good practice when launching chooser intents from an app
                startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share_message_using)));//startActivity launch activity without expecting any result back SO we don't need any result back so using start activity
                return true;
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
}
    public boolean shareFileToAnyApp(File pdfOrTextFile, String mimeType, String title){// ActivityResultLauncher<Intent> sharePdfLauncher
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
            Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", pdfOrTextFile);//**to access file uri FileProvider.getUriForFile() is compulsory from if your target sdk version is 24 or greater otherwise cannot access.this method is used to share a file with another app using a content URI
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            Intent chooser = Intent.createChooser(intent, title);
            chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(chooser);// Start the chooser dialog

            return true;
        }catch (Exception e){
            e.printStackTrace();
             return false;
        }
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
        return (activeNetwork != null && activeNetwork.isConnected());
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
    private boolean onlyViewPdf(byte whichPdfIndicator1or2,String id,boolean largeFileSizeIndicator[]) {
       if(!(whichPdfIndicator1or2 >=1 && whichPdfIndicator1or2 <=3)){//if indicator is other then 1 or 2 or 3 then show wrong pdf indicator.if indicator is 4 that means blank pdf and indicator 3 is for current pdf
             return false;
        }
        try{
          if(whichPdfIndicator1or2!=3){//1 or 2

              byte[] pdfInByte=getPdfByteFromDb(whichPdfIndicator1or2,id,largeFileSizeIndicator);
              if(pdfInByte!=null){
                   binding.pdfView.fromBytes(pdfInByte).load();//if this getPdfByteFromDb method return null then dialog message will be displayed cause error even if try catch block is there so checking null present or not
              }else return false;

          }else {//display current pdf ie.3
             if(!createAndDisplayCurrentInvoice(id)) return false;
          }
        }catch (Exception e){
            e.printStackTrace();
             return false;
        }
        return true;
    }
    public byte[] getPdfByteFromDb(byte whichPdfIndicator,String id,boolean largeFileSizeIndicator[]){//return null when no data or error
        if(!(whichPdfIndicator >=1 && whichPdfIndicator <=2)){//if indicator is other then 1 or 2 then show wrong pdf indicator
             return null;
        }
        Cursor cursor=null;
        try(Database db = new Database(getBaseContext())) {
            switch (whichPdfIndicator) {
                case 1: {
                    cursor = db.getData("SELECT " + Database.COL_394_INVOICE1 + " FROM " + Database.TABLE_NAME3 + " WHERE " + Database.COL_31_ID + "= '" + id + "'");
                    cursor.moveToFirst();
                    if (cursor.getBlob(0) != null) {
                        return cursor.getBlob(0);
                    }
                }
                break;
                case 2: {
                    cursor = db.getData("SELECT " + Database.COL_395_INVOICE2 + " FROM " + Database.TABLE_NAME3 + " WHERE " + Database.COL_31_ID + "= '" + id + "'");
                    cursor.moveToFirst();
                    if (cursor.getBlob(0) != null) {
                        return cursor.getBlob(0);
                    }
                }
                break;
            }
        }catch(SQLiteBlobTooBigException x){ //Toast.makeText(this, "LARGE FILE SIZE CAN'T VIEW", Toast.LENGTH_LONG).show();
            largeFileSizeIndicator[0]=true;
            return null;
        }catch (Exception e){
             e.printStackTrace(); // Log.d(this.getClass().getSimpleName(),"exception occurred in method "+Thread.currentThread().getStackTrace()[2].getMethodName());
            return null;
        }finally {
            if(cursor!=null){
                cursor.close();
            }
        }
    return null;//if no switch case match then it will return null
}
    public boolean createAndDisplayCurrentInvoice(String id){

//     absolutePathArrayToDelete[1]= createCurrentInvoiceAndReturnFile(id).getAbsolutePath();//when user view this pdf then if user close the app then created file should be deleted so updating absolutePathPdfToDelete to delete on destroy
//            try {
//                if (absolutePathArrayToDelete[1] != null) {
//                     binding.pdfView.fromFile(new File(absolutePathArrayToDelete[1])).load();
//                    return true;
//                }else return false;
//
//            }catch (Exception e){
//                e.printStackTrace();
//                 return false;
//            }

        try {//optimised code
            File pdfFile= createCurrentInvoiceAndReturnFile(id);//when user view this pdf then if user close the app then created file should be deleted so updating absolutePathPdfToDelete to delete on destroy
            if (pdfFile != null) {
               // absolutePathArrayToDelete[1]=pdfFile.getAbsolutePath();//update to delete
                binding.pdfView.fromFile(pdfFile).load();
                return true;
            }else return false;

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    public File createCurrentInvoiceAndReturnFile(String id) {//return null when error
        try {
            byte indicator =  MyUtility.get_indicator(getBaseContext(),id);
            boolean[] errorDetection = {false};//when ever exception occur it will be updated to true in method so it indicate error occurred or not
            File pdfFile = null;
            MakePdf makePdf = new MakePdf();
            if (!makePdf.createPage1(MakePdf.defaultPageWidth, MakePdf.defaultPageHeight, 1))
                return null;//created page 1
            float[] columnWidth = getColumnWidthBasedOnIndicator(indicator, errorDetection);
            String[] headerAccordingToIndicator = MyUtility.getWagesHeadersFromDbBasedOnIndicator(getBaseContext(),id, indicator, errorDetection);//THIS SHOULD BE TOP at arrayOfTotalWagesDepositRateAccordingToIndicator   TO AVOID INDEX EXCEPTION
            String[][] recyclerViewWagesData = MyUtility.getAllWagesDetailsFromDbBasedOnIndicator(getBaseContext(),id, indicator, errorDetection);//it amy return null   when no data
            String[][] recyclerViewDepositData = MyUtility.getAllDepositFromDb(getBaseContext(),id, errorDetection);//it amy return null   when no data

            if (!makePdf.writeSentenceWithoutLines(new String[]{""}, new float[]{100f}, false, (byte) 0, (byte) 0))
                return null;//just for space

            if (!makePdf.writeSentenceWithoutLines(getPersonDetailsForCurrentInvoice(id), new float[]{40f, 10f, 20f, 30f}, true, (byte) 0, (byte) 0))
                return null;//name,id,date,future invoice number

            if (errorDetection[0] == false) {

                if (recyclerViewDepositData != null) {//null means data not present
                    if (!makePdf.makeTable(new String[]{"DATE", "DEPOSIT", "REMARKS"}, recyclerViewDepositData, new float[]{12f, 12f, 76f}, 9, true))
                        return null;
                }

                if (recyclerViewWagesData != null) {//null means data not present
                    if (!makePdf.makeTable(headerAccordingToIndicator, recyclerViewWagesData, columnWidth, 9, false))
                        return null;
                } else {//create dummy header with no data
                    if (!makePdf.makeTable(headerAccordingToIndicator, new String[][]{{}}, columnWidth, 9, false))
                        return null;
                }

                if (!addRowsAndWriteToPDF(indicator, makePdf, 38)) return null;

                if (!makePdf.createdPageFinish2())
                    return null;//after finish page we cannot write to it

                //while creating current pdf then its file name should be same because if user click on current PDF button repeatedly then many file will be create in device which is useless.so to avoid that file name is kept same so that whenever user click current pdf button then new file will be replaced with old file so it is necessary to keep same file name.if file name is unique then many file will be created in device
                pdfFile = makePdf.createFileToSavePdfDocumentAndReturnFile(getExternalFilesDir(null).toString(), "id" + id +currentInvoiceFileName);//we have to return filename  view pdf using file path
                if (!makePdf.closeDocumentLastOperation4()) return null;
            }
            return pdfFile;
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
    public boolean addRowsAndWriteToPDF(byte indicator, MakePdf makePdf, int numberOfRows) {
        switch(indicator){
            case 1:{for (int i = 0; i < numberOfRows; i++) {
                    if(!makePdf.singleCustomRow(new String[]{"","","",""}, new float[]{12f, 12f, 5f, 71f},0,0,0,0,true, (byte) 0, (byte) 0))return false;}
                   }break;
            case 2:{for (int i = 0; i < numberOfRows; i++) {
                     if(!makePdf.singleCustomRow(new String[]{"","","","",""},new float[]{12f, 12f, 5f, 5f, 66f},0,0,0,0,true, (byte) 0, (byte) 0))return false;}
                   }break;
            case 3:{for (int i = 0; i < numberOfRows; i++) {
                     if(!makePdf.singleCustomRow(new String[]{"","","","","",""}, new float[]{12f, 12f, 5f, 5f, 5f, 61f},0,0,0,0,true, (byte) 0, (byte) 0))return false;}
                   }break;
            case 4:{for (int i = 0; i < numberOfRows; i++) {
                     if(!makePdf.singleCustomRow(new String[]{"","","","","","",""},new float[]{12f, 12f, 5f, 5f, 5f, 5f, 56f},0,0,0,0,true, (byte) 0, (byte) 0))return false;}
                   }break;
        }
        return true;
    }
    public String[] getPersonDetailsForCurrentInvoice(String id) {
        try (Database db=new Database(getBaseContext());
             Cursor cursor1 = db.getData("SELECT " + Database.COL_2_NAME +" FROM " + Database.TABLE_NAME1 + " WHERE "+Database.COL_1_ID+"='" + id + "'");
             //Cursor cursor2 = db.getData("SELECT " + Database.COL_396_PDFSEQUENCE + " FROM " + Database.TABLE_NAME3 + " WHERE "+Database.COL_31_ID+"= '" + id + "'")
              ){
            if (cursor1 != null){
                cursor1.moveToFirst();

                int pdfSequenceNo=MyUtility.getPdfSequence(id,getBaseContext());
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
    public void displayDialogMessage(String title, String message) {
        AlertDialog.Builder showDataFromDataBase=new AlertDialog.Builder(PdfViewerOperationActivity.this);
        showDataFromDataBase.setCancelable(true);
        showDataFromDataBase.setTitle(title);
        showDataFromDataBase.setMessage(message);
        showDataFromDataBase.setPositiveButton(getResources().getString(R.string.ok), (dialogInterface, i) -> dialogInterface.dismiss());
        showDataFromDataBase.create().show();
    }
    private File convertBytesToFileForSharingAndReturnFile(byte [] pdfByte, String fileName, String id){//return null when error. Converts the array of bytes into a File
        if(pdfByte==null || id==null){//to prevent file create in device when pdfByte is null
            return null;
        }
        try {
            File folder = new File(getExternalFilesDir(null) + "/"+ PdfViewerOperationActivity.pdfFolderName+"");//create directory
            if (!folder.exists()) {//if folder not exist then create folder
                folder.mkdir();//File createNewFile() method returns true if new file is created and false if file already exists.
            }
            File file = new File(getExternalFilesDir(null) + "/"+ PdfViewerOperationActivity.pdfFolderName+"/" + MyUtility.generateUniqueFileNameByTakingDateTime(id,fileName) + ".pdf");//path of pdf file where it is saved in device and file is created

                FileOutputStream fileOutputStream = new FileOutputStream(file.getAbsolutePath());
                fileOutputStream.write(pdfByte);
                fileOutputStream.close();
               // return file.getAbsolutePath(); //get absoluteFile path
                return file;
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
                 return null;
            } catch (Exception ex) {
                ex.printStackTrace();
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
//    private boolean deletePdfFromDevice(String pdfPath){
//        if(pdfPath != null){
//            try {
//                File filePath = new File(pdfPath);//file to be delete
//                if (filePath.exists()) {//checks file is present in device  or not
//                    return filePath.delete();//only this can return false
//                }
//            }catch (Exception ex) {
//                ex.printStackTrace();
//                Log.d(this.getClass().getSimpleName(), "exception occurred in method " + Thread.currentThread().getStackTrace()[2].getMethodName());
//                return false;
//            }
//        }
//        return true;//if user deleted file from device ie. file not exist in device so return true
//    }
//    private String genersateUniqueFileNameByTakingDateTime(String id,String fileName) {//file name will always be unique
//        try {
//            final Calendar current = Calendar.getInstance();//to get current date and time
//            Date d = Calendar.getInstance().getTime();//To get time
//            SimpleDateFormat sdf = new SimpleDateFormat("hhmmssa");//a stands for is AM or PM.example which make file unique 091659am which is unique
//            return "id" + id + "date" + current.get(Calendar.DAY_OF_MONTH) + "_" + (current.get(Calendar.MONTH) + 1) + "_" + current.get(Calendar.YEAR)+fileName+ "At" + sdf.format(d);
//        }catch (Exception x){
//            x.printStackTrace();
//            return "error";
//        }
//    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
//        for (String path:absolutePathArrayToDelete){
//            if(path!=null){
//                if(!MyUtility.deletePdfOrRecordingUsingPathFromDevice(path)){
//                    Log.d(this.getClass().getSimpleName(),"failed to delete file from device");
//                }
//            }
//        }
       if(!deleteFolderAllFiles(pdfFolderName,true)){//delete external file
           Toast.makeText(this, "FAILED TO DELETE FILE FROM DEVICE", Toast.LENGTH_LONG).show();
        }
        if(!deleteFolderAllFiles(null,false)){//delete cache file
            Toast.makeText(this, "FAILED TO DELETE FILE FROM DEVICE", Toast.LENGTH_LONG).show();
        }
    }

    private boolean deleteFolderAllFiles(String folderName,boolean trueForExternalFileDirAndFalseForCacheFileDir){
        try{
            if(MyUtility.checkPermissionForReadAndWriteToExternalStorage(getBaseContext())){//checking permission
                File folder;
                if(trueForExternalFileDirAndFalseForCacheFileDir){
                    folder= new File(getExternalFilesDir(null) + "/" + folderName);//File folder = new File( externalFileDir + "/acBookPDF");   //https://stackoverflow.com/questions/65125446/cannot-resolve-method-getexternalfilesdir
                }else{
                   folder=getExternalCacheDir();//getting cache directory to delete all files
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
                Toast.makeText(PdfViewerOperationActivity.this, "READ,WRITE EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(PdfViewerOperationActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 20);
                return false;
            }
            return true;
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();//destroy current activity
        Intent intent=new Intent( PdfViewerOperationActivity.this,IndividualPersonDetailActivity.class);
        intent.putExtra("ID",fromIntentPersonId);
        startActivity(intent);// go back to previous Activity with updated activity so passing id to get particular person detail refresh
    }
    private boolean checkPermissionForSMS() {
        return ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
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