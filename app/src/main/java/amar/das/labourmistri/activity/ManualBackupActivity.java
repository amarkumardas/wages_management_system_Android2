package amar.das.labourmistri.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import amar.das.labourmistri.Database;
import amar.das.labourmistri.R;
import amar.das.labourmistri.databinding.ActivityManualBackupBinding;
import amar.das.labourmistri.globalenum.GlobalConstants;
import amar.das.labourmistri.progressdialog.ProgressDialogHelper;
import amar.das.labourmistri.sharedpreferences.SharedPreferencesHelper;
import amar.das.labourmistri.takebackupdata.ExcelFormatBackup;
import amar.das.labourmistri.takebackupdata.TextAndPdfFormatBackup;
import amar.das.labourmistri.utility.BackupDataUtility;
import amar.das.labourmistri.utility.MyUtility;

public class ManualBackupActivity extends AppCompatActivity {
    ActivityManualBackupBinding binding;
    AlertDialog dialog;//to close when destroy
    String defaultBackupEachFileFormat = GlobalConstants.USER_SELECTED_PDF_FORMAT.getValue();//DEFAULT VALUE
    String defaultSingleBackupFileFormat = GlobalConstants.USER_SELECTED_PDF_FORMAT.getValue();//DEFAULT VALUE
    boolean forShareTrueForDownloadFalse=true;//default value
    boolean[] userLastBackUpEachFileIndicator =new boolean[4];//kept here so when this activity is in resume then oncreate will be called than this array need to be updated.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // EdgeToEdge.enable(this);//space where there is battery icon tower
        binding = ActivityManualBackupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        showBackupOrRestorationInfoToActivity();

        ProgressDialogHelper progressBar = new ProgressDialogHelper(this);

        setBackupEachFileUserSelectedFileFormat();//backup each file
        setSingleBackupFileUserSelectedFileFormat();//single file backup
        
        setLastSyncDateOfBackupEachFile();
        setLastSyncDateOfSingleBackupFile();
        setLastSyncDateOfDatabaseBackup();

        binding.backupEachFileRadiogroup.setOnCheckedChangeListener((radioGroup, checkedIdOfRadioBtn) -> {
            if(checkedIdOfRadioBtn == R.id.backup_each_file_pdf){
                SharedPreferencesHelper.setString(this,SharedPreferencesHelper.Keys.BACKUP_EACH_FILE_USER_SELECTED_FORMAT.name(), GlobalConstants.USER_SELECTED_PDF_FORMAT.getValue());
            } else if (checkedIdOfRadioBtn == R.id.backup_each_file_text) {
                SharedPreferencesHelper.setString(this,SharedPreferencesHelper.Keys.BACKUP_EACH_FILE_USER_SELECTED_FORMAT.name(), GlobalConstants.USER_SELECTED_TEXT_FORMAT.getValue());
            }else if (checkedIdOfRadioBtn == R.id.backup_each_file_excel) {
                SharedPreferencesHelper.setString(this,SharedPreferencesHelper.Keys.BACKUP_EACH_FILE_USER_SELECTED_FORMAT.name(), GlobalConstants.USER_SELECTED_EXCEL_FORMAT.getValue());
            }
        });
        binding.singleFileBackupRadiogroup.setOnCheckedChangeListener((radioGroup, checkedIdOfRadioBtn) -> {
            if(checkedIdOfRadioBtn == R.id.single_file_backup_pdf_file_rb){
                SharedPreferencesHelper.setString(this,SharedPreferencesHelper.Keys.SINGLE_BACKUP_FILE_USER_SELECTED_FORMAT.name(), GlobalConstants.USER_SELECTED_PDF_FORMAT.getValue());
            } else if (checkedIdOfRadioBtn == R.id.single_file_backup_text_file_rb) {
                SharedPreferencesHelper.setString(this,SharedPreferencesHelper.Keys.SINGLE_BACKUP_FILE_USER_SELECTED_FORMAT.name(), GlobalConstants.USER_SELECTED_TEXT_FORMAT.getValue());
            }else if (checkedIdOfRadioBtn == R.id.single_file_backup_excel_file_rb) {
                SharedPreferencesHelper.setString(this,SharedPreferencesHelper.Keys.SINGLE_BACKUP_FILE_USER_SELECTED_FORMAT.name(), GlobalConstants.USER_SELECTED_EXCEL_FORMAT.getValue());
            }
        });

        binding.singleFileBackupFile.setOnClickListener(view -> {
            if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {// Check if available and not read only.In summary, these methods act as safeguards for your application's interaction with external storage. By using them effectively, you can prevent errors, improve user experience, and gracefully handle situations where external storage isn't available or writable.
                Toast.makeText(this, "Storage not available or read only", Toast.LENGTH_LONG).show();//Log.e(TAG, "Storage not available or read only");
                return ;
            }
            forShareTrueForDownloadFalse =(SharedPreferencesHelper.getBoolean(this,SharedPreferencesHelper.Keys.BACKUP_FILE_FOR_SHARE_TRUE_OR_DOWNLOAD_FALSE.name(),forShareTrueForDownloadFalse))?true:false;//if user selected share then true else false for download.if user changes from share to download then dynamically value will be updated.so placing this here is important

            String userSelectedFileFormat= SharedPreferencesHelper.getString(this,SharedPreferencesHelper.Keys.SINGLE_BACKUP_FILE_USER_SELECTED_FORMAT.name(),defaultBackupEachFileFormat); // default file format will be use if user not selected any radio button
            if(userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_PDF_FORMAT.getValue())){
                singleBackupPdfFormat(progressBar,forShareTrueForDownloadFalse);

            }else if (userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_TEXT_FORMAT.getValue())) {
                singleBackupTextFormat(progressBar,forShareTrueForDownloadFalse);

            } else if (userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_EXCEL_FORMAT.getValue())) {
                singleBackupExcelFormat(progressBar,forShareTrueForDownloadFalse);
            }
        });
        binding.backupActiveSkillMlg.setOnClickListener(view -> {
            if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {// Check if available and not read only.In summary, these methods act as safeguards for your application's interaction with external storage. By using them effectively, you can prevent errors, improve user experience, and gracefully handle situations where external storage isn't available or writable.
                Toast.makeText(this, "Storage not available or read only", Toast.LENGTH_LONG).show();//Log.e(TAG, "Storage not available or read only");
                return ;
            }
            forShareTrueForDownloadFalse =(SharedPreferencesHelper.getBoolean(this,SharedPreferencesHelper.Keys.BACKUP_FILE_FOR_SHARE_TRUE_OR_DOWNLOAD_FALSE.name(),forShareTrueForDownloadFalse))?true:false;//if user selected share then true else false for download.if user changes from share to download then dynamically value will be updated.so placing this here is important

            String userSelectedFileFormat= SharedPreferencesHelper.getString(this,SharedPreferencesHelper.Keys.BACKUP_EACH_FILE_USER_SELECTED_FORMAT.name(),defaultBackupEachFileFormat); // default file format will be use if user not selected any radio button
            if(userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_PDF_FORMAT.getValue())){
                activeSkillMLGPdfFormat(progressBar,forShareTrueForDownloadFalse,userLastBackUpEachFileIndicator);

            }else if (userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_TEXT_FORMAT.getValue())) {
                activeSkillMLGTextFormat(progressBar,forShareTrueForDownloadFalse,userLastBackUpEachFileIndicator);

            } else if (userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_EXCEL_FORMAT.getValue())) {
                activeSkillMLGExcelFormat(progressBar,forShareTrueForDownloadFalse,userLastBackUpEachFileIndicator);
            }
        });
        binding.backupInactiveSkillM.setOnClickListener(view -> {

            if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {// Check if available and not read only.In summary, these methods act as safeguards for your application's interaction with external storage. By using them effectively, you can prevent errors, improve user experience, and gracefully handle situations where external storage isn't available or writable.
                Toast.makeText(this, "Storage not available or read only", Toast.LENGTH_LONG).show();//Log.e(TAG, "Storage not available or read only");
                return ;
            }
            forShareTrueForDownloadFalse=(SharedPreferencesHelper.getBoolean(this,SharedPreferencesHelper.Keys.BACKUP_FILE_FOR_SHARE_TRUE_OR_DOWNLOAD_FALSE.name(),forShareTrueForDownloadFalse))?true:false;//if user selected share then true else false for download.if user changes from share to download then dynamically value will be updated.so placing this here is important

            String userSelectedFileFormat= SharedPreferencesHelper.getString(this,SharedPreferencesHelper.Keys.BACKUP_EACH_FILE_USER_SELECTED_FORMAT.name(),defaultBackupEachFileFormat); // default file format will be use if user not selected any radio button
            if(userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_PDF_FORMAT.getValue())){
                inActiveSkillMPdfFormat(progressBar, forShareTrueForDownloadFalse,GlobalConstants.M_SKILL.getValue(),userLastBackUpEachFileIndicator);

            }else if (userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_TEXT_FORMAT.getValue())) {
                inActiveSkillMTextFormat(progressBar, forShareTrueForDownloadFalse,GlobalConstants.M_SKILL.getValue(),userLastBackUpEachFileIndicator);

            } else if (userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_EXCEL_FORMAT.getValue())) {
                inActiveSkillMExcelFormat(progressBar, forShareTrueForDownloadFalse,GlobalConstants.M_SKILL.getValue(),userLastBackUpEachFileIndicator);
            }
        });
        binding.backupInactiveSkillL.setOnClickListener(view -> {
            if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {// Check if available and not read only.In summary, these methods act as safeguards for your application's interaction with external storage. By using them effectively, you can prevent errors, improve user experience, and gracefully handle situations where external storage isn't available or writable.
                Toast.makeText(this, "Storage not available or read only", Toast.LENGTH_LONG).show();//Log.e(TAG, "Storage not available or read only");
                return ;
            }
            forShareTrueForDownloadFalse=(SharedPreferencesHelper.getBoolean(this,SharedPreferencesHelper.Keys.BACKUP_FILE_FOR_SHARE_TRUE_OR_DOWNLOAD_FALSE.name(),forShareTrueForDownloadFalse))?true:false;//if user selected share then true else false for download.if user changes from share to download then dynamically value will be updated.so placing this here is important

            String userSelectedFileFormat= SharedPreferencesHelper.getString(this,SharedPreferencesHelper.Keys.BACKUP_EACH_FILE_USER_SELECTED_FORMAT.name(),defaultBackupEachFileFormat); // default file format will be use if user not selected any radio button
            if(userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_PDF_FORMAT.getValue())){
                inActiveSkillLPdfFormat(progressBar,forShareTrueForDownloadFalse,GlobalConstants.L_SKILL.getValue(),userLastBackUpEachFileIndicator);

            }else if (userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_TEXT_FORMAT.getValue())) {
                inActiveSkillLTextFormat(progressBar,forShareTrueForDownloadFalse,GlobalConstants.L_SKILL.getValue(),userLastBackUpEachFileIndicator);

            } else if (userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_EXCEL_FORMAT.getValue())) {
                inActiveSkillLExcelFormat(progressBar,forShareTrueForDownloadFalse,GlobalConstants.L_SKILL.getValue(),userLastBackUpEachFileIndicator);
            }
        });
        binding.backupInactiveSkillG.setOnClickListener(view -> {
            if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {// Check if available and not read only.In summary, these methods act as safeguards for your application's interaction with external storage. By using them effectively, you can prevent errors, improve user experience, and gracefully handle situations where external storage isn't available or writable.
                Toast.makeText(this, "Storage not available or read only", Toast.LENGTH_LONG).show();//Log.e(TAG, "Storage not available or read only");
                return ;
            }
            forShareTrueForDownloadFalse=(SharedPreferencesHelper.getBoolean(this,SharedPreferencesHelper.Keys.BACKUP_FILE_FOR_SHARE_TRUE_OR_DOWNLOAD_FALSE.name(),forShareTrueForDownloadFalse))?true:false;//if user selected share then true else false for download.if user changes from share to download then dynamically value will be updated.so placing this here is important

            String userSelectedFileFormat= SharedPreferencesHelper.getString(this,SharedPreferencesHelper.Keys.BACKUP_EACH_FILE_USER_SELECTED_FORMAT.name(),defaultBackupEachFileFormat); // default file format will be use if user not selected any radio button
            if(userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_PDF_FORMAT.getValue())){
                inActiveSkillGPdfFormat(progressBar,forShareTrueForDownloadFalse,GlobalConstants.G_SKILL.getValue(),userLastBackUpEachFileIndicator);

            }else if (userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_TEXT_FORMAT.getValue())) {
                inActiveSkillGTextFormat(progressBar,forShareTrueForDownloadFalse,GlobalConstants.G_SKILL.getValue(),userLastBackUpEachFileIndicator);

            } else if (userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_EXCEL_FORMAT.getValue())) {
                inActiveSkillGExcelFormat(progressBar,forShareTrueForDownloadFalse,GlobalConstants.G_SKILL.getValue(),userLastBackUpEachFileIndicator);
            }
        });
        
        binding.backupEachFileTv.setOnClickListener(view -> {
            MyUtility.showDefaultDialog(getString(R.string._3_backup_each_file)+"?",getString(R.string.you_can_backup_your_aa_data_into_smaller_files_separately)+getString(R.string.newline_newline_this_file_is_useful_to_see_your_data_only),this,true);//it is activity so we have to pass this
        });
        binding.singleFileBackupTv.setOnClickListener(view -> {
            MyUtility.showDefaultDialog(getString(R.string._2_single_file_backup)+"?",getString(R.string.you_can_backup_your_all_data_in_a_single_file)+getString(R.string.newline_newline_this_file_is_useful_to_see_your_data_only),this,true);//it is activity so we have to pass this
        });
        binding.databaseBackupTv.setOnClickListener(view -> {
            MyUtility.showDefaultDialog(getString(R.string._1_database_backup)+"?",getString(R.string.you_can_backup_your_database),this,true);//it is activity so we have to pass this
        });
        binding.backupSettingForShareAndDownload.setOnClickListener(view -> {
            AlertDialog.Builder myCustomDialog=new AlertDialog.Builder(this);
            LayoutInflater inflater=LayoutInflater.from(this);
            View myView=inflater.inflate(R.layout.dialog_share_or_download_backup_file,null);//myView contain all layout view ids

            myCustomDialog.setView(myView);//set custom layout to alert dialog
            myCustomDialog.setCancelable(true);//if false user touch to other place then dialog will not be close

            dialog=myCustomDialog.create();//myCustomDialog variable cannot be use in inner class so creating another final variable  to use in inner class
            RadioGroup radioGroups=myView.findViewById(R.id.backup_setting_radiogp);
            Button save=myView.findViewById(R.id.save_btn_backup_setting);
            Button cancel=myView.findViewById(R.id.cancel_btn_backup_setting);
            setUserSelectedRadioButton(myView);

            boolean trueForShareFalseForDownload[]=new boolean[]{SharedPreferencesHelper.getBoolean(this,SharedPreferencesHelper.Keys.BACKUP_FILE_FOR_SHARE_TRUE_OR_DOWNLOAD_FALSE.name(),true)};//setting default value if user don't click radio button then default value will be used

            radioGroups.setOnCheckedChangeListener((radioGroup, checkedIdOfRadioBtn) -> {
                if(checkedIdOfRadioBtn == R.id.share_backup_file){
                    trueForShareFalseForDownload[0]=true;
                }else if (checkedIdOfRadioBtn == R.id.download_backup_file) {
                    trueForShareFalseForDownload[0]=false;
                }
            });

            cancel.setOnClickListener(view12 -> {dialog.dismiss();});
            save.setOnClickListener(view1 -> {
                if(trueForShareFalseForDownload[0]){
                    SharedPreferencesHelper.setBoolean(this,SharedPreferencesHelper.Keys.BACKUP_FILE_FOR_SHARE_TRUE_OR_DOWNLOAD_FALSE.name(),true);//true value is for sharing
                }else{
                    SharedPreferencesHelper.setBoolean(this,SharedPreferencesHelper.Keys.BACKUP_FILE_FOR_SHARE_TRUE_OR_DOWNLOAD_FALSE.name(),false);//true value is for download
                }
                dialog.dismiss();//after user click save btn then close the dialog
            });
            dialog.show();
        });

        binding.databaseBackupFile.setOnClickListener(view ->{
            Database.closeDatabase();//before backing up database close all the resources other storage issue will occur
            if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {// Check if available and not read only.In summary, these methods act as safeguards for your application's interaction with external storage. By using them effectively, you can prevent errors, improve user experience, and gracefully handle situations where external storage isn't available or writable.
                Toast.makeText(this, "Storage not available or read only", Toast.LENGTH_LONG).show();//Log.e(TAG, "Storage not available or read only");
                return ;
            }
            forShareTrueForDownloadFalse=(SharedPreferencesHelper.getBoolean(this,SharedPreferencesHelper.Keys.BACKUP_FILE_FOR_SHARE_TRUE_OR_DOWNLOAD_FALSE.name(),forShareTrueForDownloadFalse))?true:false;//if user selected share then true else false for download.if user changes from share to download then dynamically value will be updated.so placing this here is important
            dataBaseBackup(progressBar,forShareTrueForDownloadFalse);
        });
        
        binding.gobackBackupActivity.setOnClickListener(view -> {
            finish(); // This finishes the current activity
//            finish();//first destroy current activity then go back
//            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//            transaction.replace(R.id.manual_backup_layout,new MLDrawerFragment()).commit();
        });
    }
    private void showBackupOrRestorationInfoToActivity(){//this will show user haven't backup its data today info if user has not
        binding.restorationOrBackupLayout.setVisibility(View.VISIBLE);
        if(BackupDataUtility.didUserBackupDataToday(this,false)){//once user backup its data than this line will never execute.if three variable is null means user has initially download app so check for user has restored its data or not.if restored then show information else invisible and return

            if(SharedPreferencesHelper.getString(this,SharedPreferencesHelper.Keys.DATA_RESTORE_INFO.name(),null) != null){
//                binding.restorationOrBackupLayout.setVisibility(View.VISIBLE);
                binding.restorationOrBackupIcon.setBackgroundResource(R.drawable.ic_green_sharp_done_sharp_tick_20);
                binding.restorationOrBackupDateTv.setText(SharedPreferencesHelper.getString(this,SharedPreferencesHelper.Keys.DATA_RESTORE_INFO.name(),null));
            }else{
                binding.restorationOrBackupLayout.setVisibility(View.GONE);
            }

        }else{//if user has restored data and also user haven't backup its data today then backup needed information should be shown not restored information

            if(!BackupDataUtility.didUserBackupDataToday(this,true)){//high priority
//                binding.restorationOrBackupLayout.setVisibility(View.VISIBLE);
                binding.restorationOrBackupIcon.setBackgroundResource(R.drawable.baseline_warning_24);
                binding.restorationOrBackupDateTv.setText(getString(R.string.backup_needed));
            }else{
                if(SharedPreferencesHelper.getString(this,SharedPreferencesHelper.Keys.DATA_RESTORE_INFO.name(),null) != null){
//                    binding.restorationOrBackupLayout.setVisibility(View.VISIBLE);
                    binding.restorationOrBackupIcon.setBackgroundResource(R.drawable.ic_green_sharp_done_sharp_tick_20);
                    binding.restorationOrBackupDateTv.setText(SharedPreferencesHelper.getString(this,SharedPreferencesHelper.Keys.DATA_RESTORE_INFO.name(),null));
                }else{
                    binding.restorationOrBackupLayout.setVisibility(View.GONE);
                }
            }
        }
    }
    private void setUserSelectedRadioButton(View myView) {
        RadioButton shareRadio=myView.findViewById(R.id.share_backup_file);
        RadioButton downloadRadio=myView.findViewById(R.id.download_backup_file);

        if(SharedPreferencesHelper.getBoolean(this,SharedPreferencesHelper.Keys.BACKUP_FILE_FOR_SHARE_TRUE_OR_DOWNLOAD_FALSE.name(),forShareTrueForDownloadFalse)){//default value true for sharing
            shareRadio.setChecked(true);
        }else {
            downloadRadio.setChecked(true);
        }
    }

    private void dataBaseBackup(ProgressDialogHelper progressBar, boolean forShareTrueForDownloadFalse) {
        ExecutorService backgroundTask = Executors.newSingleThreadExecutor();//Executors.newSingleThreadExecutor() creates a thread pool with a single thread. This means that only one task can be executed at a time. If there are more than one task waiting to be executed, the remaining tasks will be queued until the current task is finished.
        backgroundTask.execute(() -> {
            if(!MyUtility.checkPermissionForReadAndWriteToExternalStorage(this)){
                this.runOnUiThread(() -> Toast.makeText(this, "EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show());
                //to read and write own app specific directory from minsdk 29 to 33+ we don't require READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE due to scope storage after android 10
                // ActivityCompat.requestPermissions((Activity)getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 41);
                return;
            }

            this.runOnUiThread(() -> progressBar.showProgressBar());

            Database db=Database.getInstance(getBaseContext());//opening new instance
            File dataBaseFile=null;

            if((dataBaseFile=db.databaseBackup(MyUtility.getDateTime12hrForBackupFile()+GlobalConstants.DATABASE_BACKUP_FILE_NAME.getValue())) !=null){
                if(forShareTrueForDownloadFalse){
                    if (MyUtility.shareFileToAnyApp(dataBaseFile, "application/x-sqlite3",getString(R.string.database_file_backup), this)) {//open intent to share
                        updateUserLastBackForDatabaseBackup();
                    }else {
                        this.runOnUiThread(() -> Toast.makeText(this, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show());
                    }
                }else{//download
                    if(download(dataBaseFile,this)){
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_saved_in_download_folder), Toast.LENGTH_LONG).show());
                         updateUserLastBackForDatabaseBackup();
                    }else{
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
                    }
                }
            }else{
                this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
            }
            this.runOnUiThread(() -> progressBar.hideProgressBar());
        });backgroundTask.shutdown();//when all task completed then only shutdown
    }
    private void singleBackupExcelFormat(ProgressDialogHelper progressBar, boolean forShareTrueForDownloadFalse) {
        ExecutorService backgroundTask = Executors.newSingleThreadExecutor();//Executors.newSingleThreadExecutor() creates a thread pool with a single thread. This means that only one task can be executed at a time. If there are more than one task waiting to be executed, the remaining tasks will be queued until the current task is finished.
        backgroundTask.execute(() -> {
            if(!MyUtility.checkPermissionForReadAndWriteToExternalStorage(this)) {
                this.runOnUiThread(() -> Toast.makeText(this, "EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show());
                //to read and write own app specific directory from minsdk 29 to 33+ we don't require READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE due to scope storage after android 10
                // ActivityCompat.requestPermissions((Activity)getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 41);
                return;
            }

            this.runOnUiThread(() -> progressBar.showProgressBar());

            ExcelFormatBackup excelObj=new ExcelFormatBackup(this);
            File excelFile=null;

            if((excelFile=excelObj.singleBackupExcelFile(MyUtility.getDateTime12hrForBackupFile()+GlobalConstants.SINGLE_BACKUP_EXCEL_FILE_NAME.getValue())) !=null){
                if(forShareTrueForDownloadFalse){
                    if (MyUtility.shareFileToAnyApp(excelFile, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", getString(R.string.single_excel_file_backup), this)) {//open intent to share
                        updateUserLastBackForSingleBackupFile();
                    }else{
                        this.runOnUiThread(() -> Toast.makeText(this, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show());
                    }
                }else{//download
                    if(download(excelFile,this)){
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_saved_in_download_folder), Toast.LENGTH_SHORT).show());
                        updateUserLastBackForSingleBackupFile();
                    }else{
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
                    }
                }
            }else{
                this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
            }
            this.runOnUiThread(() -> progressBar.hideProgressBar());
        });backgroundTask.shutdown();//when all task completed then only shutdown
    }
    private void singleBackupTextFormat(ProgressDialogHelper progressBar, boolean forShareTrueForDownloadFalse) {
        ExecutorService backgroundTask = Executors.newSingleThreadExecutor();//Executors.newSingleThreadExecutor() creates a thread pool with a single thread. This means that only one task can be executed at a time. If there are more than one task waiting to be executed, the remaining tasks will be queued until the current task is finished.
        backgroundTask.execute(() -> {
            if(!MyUtility.checkPermissionForReadAndWriteToExternalStorage(this)) {
                this.runOnUiThread(() -> Toast.makeText(this, "EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show());
                //to read and write own app specific directory from minsdk 29 to 33+ we don't require READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE due to scope storage after android 10
                // ActivityCompat.requestPermissions((Activity)getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 41);
                return;
            }

            this.runOnUiThread(() -> progressBar.showProgressBar());

            TextAndPdfFormatBackup dataBackup=new TextAndPdfFormatBackup(this);
            File textFile=convertTextToTextFile(dataBackup.singleBackupTextFile(),MyUtility.getDateTime12hrForBackupFile()+GlobalConstants.SINGLE_BACKUP_TEXT_FILE_NAME.getValue(),this);
            if(textFile != null){

                if(forShareTrueForDownloadFalse){
                    if(shareLargeDataAsTextFileToAnyApp(textFile ,getResources().getString(R.string.single_text_file_backup),this)){
                        updateUserLastBackForSingleBackupFile();
                    }else{
                        this.runOnUiThread(() -> Toast.makeText(this, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show());
                    }
                }else{//download
                    if(download(textFile,this)){
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_saved_in_download_folder), Toast.LENGTH_SHORT).show());
                        updateUserLastBackForSingleBackupFile();
                    }else{
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
                    }
                }
            }else{
                this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
            }
            this.runOnUiThread(() -> progressBar.hideProgressBar());
        });backgroundTask.shutdown();//when all task completed then only shutdown
    }
    private void singleBackupPdfFormat(ProgressDialogHelper progressBar, boolean forShareTrueForDownloadFalse) {
        ExecutorService backgroundTask = Executors.newSingleThreadExecutor();//Executors.newSingleThreadExecutor() creates a thread pool with a single thread. This means that only one task can be executed at a time. If there are more than one task waiting to be executed, the remaining tasks will be queued until the current task is finished.
        backgroundTask.execute(() -> {
            if(!MyUtility.checkPermissionForReadAndWriteToExternalStorage(this)) {
                this.runOnUiThread(() -> Toast.makeText(this, "EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show());
                //to read and write own app specific directory from minsdk 29 to 33+ we don't require READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE due to scope storage after android 10
                // ActivityCompat.requestPermissions((Activity)getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 41);
                return;
            }

            this.runOnUiThread(() -> progressBar.showProgressBar());

            TextAndPdfFormatBackup dataBackup=new TextAndPdfFormatBackup(this);
            File pdfFile=null;
            if((pdfFile=dataBackup.singleBackupPdfFile(MyUtility.getDateTime12hrForBackupFile()+GlobalConstants.SINGLE_BACKUP_PDF_FILE_NAME.getValue())) !=null){

                if(forShareTrueForDownloadFalse){//share
                    if (MyUtility.shareFileToAnyApp(pdfFile, "application/pdf",getString(R.string.single_pdf_file_backup), this)) {
                        updateUserLastBackForSingleBackupFile();
                    }else{
                        this.runOnUiThread(() -> Toast.makeText(this, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show());
                    }
                }else{//download
                    if(download(pdfFile,this)){
                        this.runOnUiThread(() -> Toast.makeText(this,getString(R.string.backup_saved_in_download_folder), Toast.LENGTH_SHORT).show());
                        updateUserLastBackForSingleBackupFile();
                    }else{
                        this.runOnUiThread(() -> Toast.makeText(this,getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
                    }
                }
            }else{
                this.runOnUiThread(() -> Toast.makeText(this, getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
            }

            this.runOnUiThread(() -> progressBar.hideProgressBar());
        });backgroundTask.shutdown();//when all task completed then only shutdown
    }
    private void setLastSyncDateOfDatabaseBackup() {
        String userBackupDate=SharedPreferencesHelper.getString(this,SharedPreferencesHelper.Keys.DATE_DATABASE_BACKUP.name(),null);
        if( userBackupDate != null ){

            checkIfUserBackupDataTodayThanMakeGreenTickVisibleAndChangeTextColorForBackupDatabase(userBackupDate);
            binding.databaseLastBackup.setText(userBackupDate);

        }else{
            binding.databaseLastBackup.setText(getString(R.string.last_backup_colon_hyphen));
            makeGreenTickInVisibleForDatabaseBackup();
        }
    }
    private void setLastSyncDateOfBackupEachFile() {
        String userBackupDate=SharedPreferencesHelper.getString(this,SharedPreferencesHelper.Keys.DATE_BACKUP_EACH_FILE.name(),null);
        if( userBackupDate != null ){

            checkIfUserBackupDataTodayThanMakeGreenTickVisibleAndChangeTextColorForEachBackupFile(userBackupDate);
            binding.backupEachFileUserLastBackup.setText(userBackupDate);

        }else{
            binding.backupEachFileUserLastBackup.setText(getString(R.string.last_backup_colon_hyphen));
            makeGreenTickInVisibleForBackupEachFile();
        }
    }
    private void setLastSyncDateOfSingleBackupFile() {
        String userBackupDate=SharedPreferencesHelper.getString(this,SharedPreferencesHelper.Keys.DATE_SINGLE_BACKUP_FILE.name(),null);
        if( userBackupDate != null ){

            checkIfUserBackupDataTodayThanMakeGreenTickVisibleAndChangeTextColorForSingleBackupFile(userBackupDate);
            binding.singleFileBackupUserLastBackup.setText(userBackupDate);

        }else{
            binding.singleFileBackupUserLastBackup.setText(getString(R.string.last_backup_colon_hyphen));
            makeGreenTickInVisibleForSingleBackupFile();
        }
    }
    private void checkIfUserBackupDataTodayThanMakeGreenTickVisibleAndChangeTextColorForEachBackupFile(String userBackupDate){//checking user has backup its data today or not.if date matches today date then make green tick visible else invisible
        if(MyUtility.getDateFromLastBackupDate(userBackupDate).equals(MyUtility.getDateFromLastBackupDate(MyUtility.get12hrCurrentTimeAndDateForLastBackup()))) {//checking by taking today date
            makeGreenTickVisibleForBackupEachFile();
            binding.backupEachFileUserLastBackup.setTextColor(getColor(R.color.dark_green));//if user has backup its data today then set text to green
        }else{
            makeGreenTickInVisibleForBackupEachFile();
        }
    }
    private void checkIfUserBackupDataTodayThanMakeGreenTickVisibleAndChangeTextColorForBackupDatabase(String userBackupDate){//checking user has backup its data today or not.if date matches today date then make green tick visible else invisible
        if(MyUtility.getDateFromLastBackupDate(userBackupDate).equals(MyUtility.getDateFromLastBackupDate(MyUtility.get12hrCurrentTimeAndDateForLastBackup()))) {//checking by taking today date
            makeGreenTickVisibleForDatabaseBackup();
            binding.databaseLastBackup.setTextColor(getColor(R.color.dark_green));//if user has backup its data today then set text to green
        }else{
            makeGreenTickInVisibleForDatabaseBackup();
        }
    }
    private void checkIfUserBackupDataTodayThanMakeGreenTickVisibleAndChangeTextColorForSingleBackupFile(String userBackupDate){//checking user has backup its data today or not.if date matches today date then make green tick visible else invisible
        if(MyUtility.getDateFromLastBackupDate(userBackupDate).equals(MyUtility.getDateFromLastBackupDate(MyUtility.get12hrCurrentTimeAndDateForLastBackup()))) {//checking by taking today date
            makeGreenTickVisibleForSingleBackupFile();
            binding.singleFileBackupUserLastBackup.setTextColor(getColor(R.color.dark_green));//if user has backup its data today then set text to green
        }else{
            makeGreenTickInVisibleForSingleBackupFile();
        }
    }
    private void makeGreenTickVisibleForBackupEachFile(){
        binding.backupActiveSkillMlgGreenTick.setVisibility(View.VISIBLE);
        binding.backupInactiveSkillGGreenTick.setVisibility(View.VISIBLE);
        binding.backupInactiveSkillLGreenTick.setVisibility(View.VISIBLE);
        binding.backupInactiveSkillMGreenTick.setVisibility(View.VISIBLE);
    }

    private void makeGreenTickVisibleForSingleBackupFile(){
        binding.singleFileBackupGreenTick.setVisibility(View.VISIBLE);
    }
    private void makeGreenTickInVisibleForBackupEachFile() {
        binding.backupActiveSkillMlgGreenTick.setVisibility(View.GONE);
        binding.backupInactiveSkillGGreenTick.setVisibility(View.GONE);
        binding.backupInactiveSkillLGreenTick.setVisibility(View.GONE);
        binding.backupInactiveSkillMGreenTick.setVisibility(View.GONE);
    }
    private void makeGreenTickInVisibleForDatabaseBackup() {
        binding.databaseBackupGreenTick.setVisibility(View.GONE);
    }
    private void makeGreenTickVisibleForDatabaseBackup(){
        binding.databaseBackupGreenTick.setVisibility(View.VISIBLE);
    }
    private void makeGreenTickInVisibleForSingleBackupFile() {
        binding.singleFileBackupGreenTick.setVisibility(View.GONE);
    }
    private void inActiveSkillGTextFormat(ProgressDialogHelper progressBar,boolean forShareTrueForDownloadFalse,String skillType,boolean[] lastBackUpEachFileIndicator) {
        ExecutorService backgroundTask = Executors.newSingleThreadExecutor();//Executors.newSingleThreadExecutor() creates a thread pool with a single thread. This means that only one task can be executed at a time. If there are more than one task waiting to be executed, the remaining tasks will be queued until the current task is finished.
        backgroundTask.execute(() -> {
            if(!MyUtility.checkPermissionForReadAndWriteToExternalStorage(this)) {
                this.runOnUiThread(() -> Toast.makeText(this, "EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show());
                //to read and write own app specific directory from minsdk 29 to 33+ we don't require READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE due to scope storage after android 10
                // ActivityCompat.requestPermissions((Activity)getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 41);
                return;
            }

            this.runOnUiThread(() -> progressBar.showProgressBar());

            TextAndPdfFormatBackup dataBackup=new TextAndPdfFormatBackup(this);
            File textFile=convertTextToTextFile(dataBackup.backupInActiveMOrLOrGDataInTextFormat(skillType),MyUtility.getDateTime12hrForBackupFile()+GlobalConstants.BACKUP_INACTIVE_G_TEXT_FILE_NAME.getValue(),this);
            if(textFile != null){

                if(forShareTrueForDownloadFalse){
                    if(shareLargeDataAsTextFileToAnyApp(textFile ,getResources().getString(R.string.backup_inactive_skill_g)+" TEXT FILE",this)){
                        updateLastBackEachFile(lastBackUpEachFileIndicator, (byte) 4);
                    }else{
                        this.runOnUiThread(() -> Toast.makeText(this, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show());
                    }
                }else{//download
                    if(download(textFile,this)){
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_saved_in_download_folder), Toast.LENGTH_SHORT).show());
                        updateLastBackEachFile(lastBackUpEachFileIndicator, (byte) 4);
                    }else{
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
                    }
                }
            }else{
                this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
            }
            this.runOnUiThread(() -> progressBar.hideProgressBar());
        });backgroundTask.shutdown();//when all task completed then only shutdown
    }
    private void inActiveSkillGExcelFormat(ProgressDialogHelper progressBar,boolean forShareTrueForDownloadFalse,String skillType,boolean[] lastBackUpEachFileIndicator) {
        ExecutorService backgroundTask = Executors.newSingleThreadExecutor();//Executors.newSingleThreadExecutor() creates a thread pool with a single thread. This means that only one task can be executed at a time. If there are more than one task waiting to be executed, the remaining tasks will be queued until the current task is finished.
        backgroundTask.execute(() -> {
            if(!MyUtility.checkPermissionForReadAndWriteToExternalStorage(this)) {
                this.runOnUiThread(() -> Toast.makeText(this, "EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show());
                //to read and write own app specific directory from minsdk 29 to 33+ we don't require READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE due to scope storage after android 10
                // ActivityCompat.requestPermissions((Activity)getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 41);
                return;
            }

            this.runOnUiThread(() -> progressBar.showProgressBar());

            ExcelFormatBackup excelObj=new ExcelFormatBackup(this);
            File excelFile=null;

            if((excelFile=excelObj.backupInActiveMOrLOrGDataInExcelFormat(skillType,MyUtility.getDateTime12hrForBackupFile()+GlobalConstants.BACKUP_INACTIVE_G_EXCEL_FILE_NAME.getValue())) !=null){
                if(forShareTrueForDownloadFalse){
                    if (MyUtility.shareFileToAnyApp(excelFile, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "BACKUP INACTIVE SKILL G EXCEL FILE", this)) {//open intent to share
                       updateLastBackEachFile(lastBackUpEachFileIndicator, (byte) 4);
                    }else {
                        this.runOnUiThread(() -> Toast.makeText(this, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show());
                    }
                }else{//download
                    if(download(excelFile,this)){
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_saved_in_download_folder), Toast.LENGTH_SHORT).show());
                        updateLastBackEachFile(lastBackUpEachFileIndicator, (byte) 4);
                    }else{
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
                    }
                }
            }else{
                this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
            }
            this.runOnUiThread(() -> progressBar.hideProgressBar());
        });backgroundTask.shutdown();//when all task completed then only shutdown
    }
    private void inActiveSkillGPdfFormat(ProgressDialogHelper progressBar,boolean forShareTrueForDownloadFalse,String skillType,boolean[] lastBackUpEachFileIndicator) {
        ExecutorService backgroundTask = Executors.newSingleThreadExecutor();//Executors.newSingleThreadExecutor() creates a thread pool with a single thread. This means that only one task can be executed at a time. If there are more than one task waiting to be executed, the remaining tasks will be queued until the current task is finished.
        backgroundTask.execute(() -> {
            if(!MyUtility.checkPermissionForReadAndWriteToExternalStorage(this)) {
                this.runOnUiThread(() -> Toast.makeText(this, "EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show());
                //to read and write own app specific directory from minsdk 29 to 33+ we don't require READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE due to scope storage after android 10
                // ActivityCompat.requestPermissions((Activity)getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 41);
                return;
            }

            this.runOnUiThread(() -> progressBar.showProgressBar());

            TextAndPdfFormatBackup dataBackup=new TextAndPdfFormatBackup(this);
            File pdfFile=null;
            if((pdfFile=dataBackup.backupInActiveMOrLOrGDataInPDFFormat(MyUtility.getDateTime12hrForBackupFile()+GlobalConstants.BACKUP_INACTIVE_G_PDF_FILE_NAME.getValue(),skillType)) !=null){

                if(forShareTrueForDownloadFalse) {//share
                    if (MyUtility.shareFileToAnyApp(pdfFile, "application/pdf", getResources().getString(R.string.backup_inactive_skill_g) + " PDF FILE", this)) {
                          updateLastBackEachFile(lastBackUpEachFileIndicator, (byte) 4);
                    }else{
                        this.runOnUiThread(() -> Toast.makeText(this, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show());
                    }
                }else{//download
                    if(download(pdfFile,this)){
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_saved_in_download_folder), Toast.LENGTH_SHORT).show());
                        updateLastBackEachFile(lastBackUpEachFileIndicator, (byte) 4);
                    }else{
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
                    }
                }
            }else{
                this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
            }

            this.runOnUiThread(() -> progressBar.hideProgressBar());
        });backgroundTask.shutdown();//when all task completed then only shutdown
    }
    private void inActiveSkillLExcelFormat(ProgressDialogHelper progressBar,boolean forShareTrueForDownloadFalse,String skillType,boolean[] lastBackUpEachFileIndicator) {
        ExecutorService backgroundTask = Executors.newSingleThreadExecutor();//Executors.newSingleThreadExecutor() creates a thread pool with a single thread. This means that only one task can be executed at a time. If there are more than one task waiting to be executed, the remaining tasks will be queued until the current task is finished.
        backgroundTask.execute(() -> {
         if(!MyUtility.checkPermissionForReadAndWriteToExternalStorage(this)) {
                this.runOnUiThread(() -> Toast.makeText(this, "EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show());
                //to read and write own app specific directory from minsdk 29 to 33+ we don't require READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE due to scope storage after android 10
                // ActivityCompat.requestPermissions((Activity)getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 41);
                return;
            }

            this.runOnUiThread(() -> progressBar.showProgressBar());

            ExcelFormatBackup excelObj=new ExcelFormatBackup(this);
            File excelFile=null;

            if((excelFile=excelObj.backupInActiveMOrLOrGDataInExcelFormat(skillType,MyUtility.getDateTime12hrForBackupFile()+GlobalConstants.BACKUP_INACTIVE_L_EXCEL_FILE_NAME.getValue())) !=null){
                if(forShareTrueForDownloadFalse){
                    if(MyUtility.shareFileToAnyApp(excelFile, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "BACKUP INACTIVE SKILL L EXCEL FILE", this)) {//open intent to share
                        updateLastBackEachFile(lastBackUpEachFileIndicator, (byte) 3);
                    }else{
                        this.runOnUiThread(() -> Toast.makeText(this, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show());
                    }
                }else{//download
                    if(download(excelFile,this)){
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_saved_in_download_folder), Toast.LENGTH_SHORT).show());
                        updateLastBackEachFile(lastBackUpEachFileIndicator, (byte) 3);
                    }else{
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
                    }
                }
            }else{
                this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
            }
            this.runOnUiThread(() -> progressBar.hideProgressBar());
        });backgroundTask.shutdown();//when all task completed then only shutdown
    }
    private void inActiveSkillLTextFormat(ProgressDialogHelper progressBar,boolean forShareTrueForDownloadFalse,String skillType,boolean[] lastBackUpEachFileIndicator) {
        ExecutorService backgroundTask = Executors.newSingleThreadExecutor();//Executors.newSingleThreadExecutor() creates a thread pool with a single thread. This means that only one task can be executed at a time. If there are more than one task waiting to be executed, the remaining tasks will be queued until the current task is finished.
        backgroundTask.execute(() -> {
            if(!MyUtility.checkPermissionForReadAndWriteToExternalStorage(this)) {
                this.runOnUiThread(() -> Toast.makeText(this, "EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show());
                //to read and write own app specific directory from minsdk 29 to 33+ we don't require READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE due to scope storage after android 10
                // ActivityCompat.requestPermissions((Activity)getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 41);
                return;
            }

            this.runOnUiThread(() -> progressBar.showProgressBar());

            TextAndPdfFormatBackup dataBackup=new TextAndPdfFormatBackup(this);
            File textFile=convertTextToTextFile(dataBackup.backupInActiveMOrLOrGDataInTextFormat(skillType),MyUtility.getDateTime12hrForBackupFile()+GlobalConstants.BACKUP_INACTIVE_L_TEXT_FILE_NAME.getValue(),this);
            if(textFile != null){

                if(forShareTrueForDownloadFalse){
                    if(shareLargeDataAsTextFileToAnyApp(textFile ,getResources().getString(R.string.backup_inactive_skill_l)+" TEXT FILE",this)){
                        updateLastBackEachFile(lastBackUpEachFileIndicator, (byte) 3);
                    }else{
                        this.runOnUiThread(() -> Toast.makeText(this, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show());
                    }
                }else{//download
                    if(download(textFile,this)){
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_saved_in_download_folder), Toast.LENGTH_SHORT).show());
                        updateLastBackEachFile(lastBackUpEachFileIndicator, (byte) 3);
                    }else{
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
                    }
                }
            }else{
                this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
            }
            this.runOnUiThread(() -> progressBar.hideProgressBar());
        });backgroundTask.shutdown();//when all task completed then only shutdown
    }
    private void inActiveSkillLPdfFormat(ProgressDialogHelper progressBar,boolean forShareTrueForDownloadFalse,String skillType,boolean lastBackUpEachFileIndicator[]) {
        ExecutorService backgroundTask = Executors.newSingleThreadExecutor();//Executors.newSingleThreadExecutor() creates a thread pool with a single thread. This means that only one task can be executed at a time. If there are more than one task waiting to be executed, the remaining tasks will be queued until the current task is finished.
        backgroundTask.execute(() -> {
            if(!MyUtility.checkPermissionForReadAndWriteToExternalStorage(this)) {
                this.runOnUiThread(() -> Toast.makeText(this, "EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show());
                //to read and write own app specific directory from minsdk 29 to 33+ we don't require READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE due to scope storage after android 10
                // ActivityCompat.requestPermissions((Activity)getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 41);
                return;
            }

            this.runOnUiThread(() -> progressBar.showProgressBar());

            TextAndPdfFormatBackup dataBackup=new TextAndPdfFormatBackup(this);
            File pdfFile=null;
            if((pdfFile=dataBackup.backupInActiveMOrLOrGDataInPDFFormat(MyUtility.getDateTime12hrForBackupFile()+GlobalConstants.BACKUP_INACTIVE_L_PDF_FILE_NAME.getValue(),skillType)) !=null){

                if(forShareTrueForDownloadFalse) {//share
                    if (MyUtility.shareFileToAnyApp(pdfFile, "application/pdf", getResources().getString(R.string.backup_inactive_skill_l) + " PDF FILE", this)) {
                        updateLastBackEachFile(lastBackUpEachFileIndicator, (byte) 3);
                    }else{
                        this.runOnUiThread(() -> Toast.makeText(this, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show());
                    }
                }else{//download
                    if(download(pdfFile,this)){
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_saved_in_download_folder), Toast.LENGTH_SHORT).show());
                        updateLastBackEachFile(lastBackUpEachFileIndicator, (byte) 3);
                    }else{
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
                    }
                }
            }else{
                this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
            }

            this.runOnUiThread(() -> progressBar.hideProgressBar());
        });backgroundTask.shutdown();//when all task completed then only shutdown
    }
    private void inActiveSkillMExcelFormat(ProgressDialogHelper progressBar,boolean forShareTrueForDownloadFalse,String skillType,boolean lastBackUpEachFileIndicator[]) {
        ExecutorService backgroundTask = Executors.newSingleThreadExecutor();//Executors.newSingleThreadExecutor() creates a thread pool with a single thread. This means that only one task can be executed at a time. If there are more than one task waiting to be executed, the remaining tasks will be queued until the current task is finished.
        backgroundTask.execute(() -> {
            if(!MyUtility.checkPermissionForReadAndWriteToExternalStorage(this)) {
                this.runOnUiThread(() -> Toast.makeText(this, "EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show());
                //to read and write own app specific directory from minsdk 29 to 33+ we don't require READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE due to scope storage after android 10
                // ActivityCompat.requestPermissions((Activity)getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 41);
                return;
            }

            this.runOnUiThread(() -> progressBar.showProgressBar());

            ExcelFormatBackup excelObj=new ExcelFormatBackup(this);
            File excelFile=null;

            if((excelFile=excelObj.backupInActiveMOrLOrGDataInExcelFormat(skillType,MyUtility.getDateTime12hrForBackupFile()+GlobalConstants.BACKUP_INACTIVE_M_EXCEL_FILE_NAME.getValue())) !=null){
                if(forShareTrueForDownloadFalse){
                    if (MyUtility.shareFileToAnyApp(excelFile, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "BACKUP INACTIVE SKILL M EXCEL FILE", this)) {//open intent to share
                        updateLastBackEachFile(lastBackUpEachFileIndicator, (byte) 2);
                    }else {
                        this.runOnUiThread(() -> Toast.makeText(this, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show());
                    }
                }else{//download
                    if(download(excelFile,this)){
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_saved_in_download_folder), Toast.LENGTH_SHORT).show());
                        updateLastBackEachFile(lastBackUpEachFileIndicator, (byte) 2);
                    }else{
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
                    }
                }
            }else{
                this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
            }
            this.runOnUiThread(() -> progressBar.hideProgressBar());
        });backgroundTask.shutdown();//when all task completed then only shutdown
    }
    private void activeSkillMLGExcelFormat(ProgressDialogHelper progressBar,boolean forShareTrueForDownloadFalse,boolean lastBackUpEachFileIndicator[]) {
        ExecutorService backgroundTask = Executors.newSingleThreadExecutor();//Executors.newSingleThreadExecutor() creates a thread pool with a single thread. This means that only one task can be executed at a time. If there are more than one task waiting to be executed, the remaining tasks will be queued until the current task is finished.
        backgroundTask.execute(() -> {
            if(!MyUtility.checkPermissionForReadAndWriteToExternalStorage(this)) {
                this.runOnUiThread(() -> Toast.makeText(this, "EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show());
                //to read and write own app specific directory from minsdk 29 to 33+ we don't require READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE due to scope storage after android 10
                // ActivityCompat.requestPermissions((Activity)getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 41);
                return;
            }

            this.runOnUiThread(() -> progressBar.showProgressBar());

            ExcelFormatBackup excelObj=new ExcelFormatBackup(this);
            File excelFile=null;

            if((excelFile=excelObj.backupActiveMLGDataInExcelFormat(MyUtility.getDateTime12hrForBackupFile()+GlobalConstants.BACKUP_ACTIVE_MLG_EXCEL_FILE_NAME.getValue())) !=null){
                if(forShareTrueForDownloadFalse){
                    if (MyUtility.shareFileToAnyApp(excelFile, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "BACKUP ACTIVE SKILL MLG EXCEL FILE", this)) {//open intent to share
                        updateLastBackEachFile(lastBackUpEachFileIndicator, (byte) 1);
                    }else{
                        this.runOnUiThread(() -> Toast.makeText(this, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show());
                    }
                }else{//download
                if(download(excelFile,this)){
                    this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_saved_in_download_folder), Toast.LENGTH_SHORT).show());
                    updateLastBackEachFile(lastBackUpEachFileIndicator, (byte) 1);
                }else{
                    this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
                }
            }
            }else{
                this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
            }
            this.runOnUiThread(() -> progressBar.hideProgressBar());
        });backgroundTask.shutdown();//when all task completed then only shutdown
    }
    private void inActiveSkillMPdfFormat(ProgressDialogHelper progressBar,boolean forShareTrueForDownloadFalse,String skillType,boolean lastBackUpEachFileIndicator[]) {
        ExecutorService backgroundTask = Executors.newSingleThreadExecutor();//Executors.newSingleThreadExecutor() creates a thread pool with a single thread. This means that only one task can be executed at a time. If there are more than one task waiting to be executed, the remaining tasks will be queued until the current task is finished.
        backgroundTask.execute(() -> {
            if(!MyUtility.checkPermissionForReadAndWriteToExternalStorage(this)) {
                this.runOnUiThread(() -> Toast.makeText(this, "EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show());
                //to read and write own app specific directory from minsdk 29 to 33+ we don't require READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE due to scope storage after android 10
                // ActivityCompat.requestPermissions((Activity)getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 41);
                return;
            }

            this.runOnUiThread(() -> progressBar.showProgressBar());

            TextAndPdfFormatBackup dataBackup=new TextAndPdfFormatBackup(this);
            File pdfFile=null;
            if((pdfFile=dataBackup.backupInActiveMOrLOrGDataInPDFFormat(MyUtility.getDateTime12hrForBackupFile()+GlobalConstants.BACKUP_INACTIVE_M_PDF_FILE_NAME.getValue(),skillType)) !=null){

                if(forShareTrueForDownloadFalse) {//share
                    if (MyUtility.shareFileToAnyApp(pdfFile, "application/pdf", getResources().getString(R.string.backup_inactive_skill_m) + " PDF FILE", this)) {
                        updateLastBackEachFile(lastBackUpEachFileIndicator, (byte) 2);
                    }else{
                        this.runOnUiThread(() -> Toast.makeText(this, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show());
                    }
                }else{//download
                    if(download(pdfFile,this)){
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_saved_in_download_folder), Toast.LENGTH_SHORT).show());
                        updateLastBackEachFile(lastBackUpEachFileIndicator, (byte) 2);
                    }else{
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
                    }
                }
            }else{
                this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
            }

            this.runOnUiThread(() -> progressBar.hideProgressBar());
        });backgroundTask.shutdown();//when all task completed then only shutdown
    }
    private void activeSkillMLGPdfFormat(ProgressDialogHelper progressBar,boolean forShareTrueForDownloadFalse,boolean lastBackUpEachFileIndicator[]) {
        ExecutorService backgroundTask = Executors.newSingleThreadExecutor();//Executors.newSingleThreadExecutor() creates a thread pool with a single thread. This means that only one task can be executed at a time. If there are more than one task waiting to be executed, the remaining tasks will be queued until the current task is finished.
        backgroundTask.execute(() -> {
            if(!MyUtility.checkPermissionForReadAndWriteToExternalStorage(this)) {
                this.runOnUiThread(() -> Toast.makeText(this, "EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show());
                //to read and write own app specific directory from minsdk 29 to 33+ we don't require READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE due to scope storage after android 10
                // ActivityCompat.requestPermissions((Activity)getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 41);
                return;
            }

            this.runOnUiThread(() -> progressBar.showProgressBar());

            TextAndPdfFormatBackup dataBackup=new TextAndPdfFormatBackup(this);
            File pdfFile=null;
            if((pdfFile=dataBackup.backupActiveMLGDataInPDFFormat(MyUtility.getDateTime12hrForBackupFile()+GlobalConstants.BACKUP_ACTIVE_MLG_PDF_FILE_NAME.getValue())) !=null){

                if(forShareTrueForDownloadFalse) {//share
                    if (MyUtility.shareFileToAnyApp(pdfFile, "application/pdf", getResources().getString(R.string.backup_active_skill_m_l_g) + " PDF FILE", this)) {
                        updateLastBackEachFile(lastBackUpEachFileIndicator, (byte) 1);
                    }else{
                        this.runOnUiThread(() -> Toast.makeText(this, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show());
                    }
                }else{//download
                    if(download(pdfFile,this)){
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_saved_in_download_folder), Toast.LENGTH_SHORT).show());
                        updateLastBackEachFile(lastBackUpEachFileIndicator, (byte) 1);
                    }else{
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
                    }
                }
            }else{
                this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
            }

            this.runOnUiThread(() -> progressBar.hideProgressBar());
        });backgroundTask.shutdown();//when all task completed then only shutdown
    }
    private void updateUserLastBackForSingleBackupFile() {
        SharedPreferencesHelper.setString(this,SharedPreferencesHelper.Keys.DATE_SINGLE_BACKUP_FILE.name(),MyUtility.get12hrCurrentTimeAndDateForLastBackup());//update value.   12 - 12 - 2024 (11:59:54 am)
        runOnUiThread(() -> {//is a method provided by the Activity class that allows you to safely update the user interface (UI) from a background thread.
            binding.singleFileBackupGreenTick.setVisibility(View.VISIBLE);//make green tick visible
            binding.singleFileBackupUserLastBackup.setText(SharedPreferencesHelper.getString(this,SharedPreferencesHelper.Keys.DATE_SINGLE_BACKUP_FILE.name(), null));
            binding.singleFileBackupUserLastBackup.setTextColor(getColor(R.color.dark_green));//set color to green
        });
    }
    private void updateUserLastBackForDatabaseBackup() {
        SharedPreferencesHelper.setString(this,SharedPreferencesHelper.Keys.DATE_DATABASE_BACKUP.name(),MyUtility.get12hrCurrentTimeAndDateForLastBackup());//update value.   12 - 12 - 2024 (11:59:54 am)
        runOnUiThread(() -> {//is a method provided by the Activity class that allows you to safely update the user interface (UI) from a background thread.
            binding.databaseBackupGreenTick.setVisibility(View.VISIBLE);//make green tick visible
            binding.databaseLastBackup.setText(SharedPreferencesHelper.getString(this,SharedPreferencesHelper.Keys.DATE_DATABASE_BACKUP.name(), null));
            binding.databaseLastBackup.setTextColor(getColor(R.color.dark_green));//set color to green
        });
    }
    private void updateLastBackEachFile(boolean[] lastBackUpEachFileIndicator, byte fileNumber) {
        if(fileNumber==1){
            lastBackUpEachFileIndicator[0]=true;//means file is backed up by user successfully by user
          runOnUiThread(() -> {//is a method provided by the Activity class that allows you to safely update the user interface (UI) from a background thread.
              binding.backupActiveSkillMlgGreenTick.setVisibility(View.VISIBLE);
          });
        }
        else if (fileNumber==2){
            lastBackUpEachFileIndicator[1]=true;
            runOnUiThread(() -> {//is a method provided by the Activity class that allows you to safely update the user interface (UI) from a background thread.
                binding.backupInactiveSkillMGreenTick.setVisibility(View.VISIBLE);
            });
        }
        else if (fileNumber==3){
            lastBackUpEachFileIndicator[2]=true;
            runOnUiThread(() -> {//is a method provided by the Activity class that allows you to safely update the user interface (UI) from a background thread.
                binding.backupInactiveSkillLGreenTick.setVisibility(View.VISIBLE);
            });
        }
        else if (fileNumber==4){
            lastBackUpEachFileIndicator[3]=true;
            runOnUiThread(() -> {//is a method provided by the Activity class that allows you to safely update the user interface (UI) from a background thread.
                binding.backupInactiveSkillGGreenTick.setVisibility(View.VISIBLE);
            });
        }

        if(lastBackUpEachFileIndicator[0] && lastBackUpEachFileIndicator[1] && lastBackUpEachFileIndicator[2] && lastBackUpEachFileIndicator[3]){//if all file is backed bu by user
          //(Last Backup: 24-05-2044 at 09:05 am)
            SharedPreferencesHelper.setString(this,SharedPreferencesHelper.Keys.DATE_BACKUP_EACH_FILE.name(),MyUtility.get12hrCurrentTimeAndDateForLastBackup());//update value.   12 - 12 - 2024 (11:59 am)
            runOnUiThread(() -> {//is a method provided by the Activity class that allows you to safely update the user interface (UI) from a background thread.
                binding.backupEachFileUserLastBackup.setText(SharedPreferencesHelper.getString(this, SharedPreferencesHelper.Keys.DATE_BACKUP_EACH_FILE.name(), null));
                binding.backupEachFileUserLastBackup.setTextColor(getColor(R.color.dark_green));//set color to green
            });
        }
    }
    private void inActiveSkillMTextFormat(ProgressDialogHelper progressBar, boolean forShareTrueForDownloadFalse,String skillType,boolean lastBackUpEachFileIndicator[]) {
        ExecutorService backgroundTask = Executors.newSingleThreadExecutor();//Executors.newSingleThreadExecutor() creates a thread pool with a single thread. This means that only one task can be executed at a time. If there are more than one task waiting to be executed, the remaining tasks will be queued until the current task is finished.
        backgroundTask.execute(() -> {
            if(!MyUtility.checkPermissionForReadAndWriteToExternalStorage(this)) {
                this.runOnUiThread(() -> Toast.makeText(this, "EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show());
                //to read and write own app specific directory from minsdk 29 to 33+ we don't require READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE due to scope storage after android 10
                // ActivityCompat.requestPermissions((Activity)getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 41);
                return;
            }

            this.runOnUiThread(() -> progressBar.showProgressBar());

            TextAndPdfFormatBackup dataBackup=new TextAndPdfFormatBackup(this);
            File textFile=convertTextToTextFile(dataBackup.backupInActiveMOrLOrGDataInTextFormat(skillType),MyUtility.getDateTime12hrForBackupFile()+GlobalConstants.BACKUP_INACTIVE_M_TEXT_FILE_NAME.getValue(),this);
            if(textFile != null){

                if(forShareTrueForDownloadFalse){
                    if(shareLargeDataAsTextFileToAnyApp(textFile ,getResources().getString(R.string.backup_inactive_skill_m)+" TEXT FILE",this)){
                       updateLastBackEachFile(lastBackUpEachFileIndicator, (byte) 2);
                    }else{
                        this.runOnUiThread(() -> Toast.makeText(this, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show());
                    }
                }else{//download
                    if(download(textFile,this)){
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_saved_in_download_folder), Toast.LENGTH_SHORT).show());
                        updateLastBackEachFile(lastBackUpEachFileIndicator, (byte) 2);
                    }else{
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
                    }
                }
            }else{
                this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
            }
            this.runOnUiThread(() -> progressBar.hideProgressBar());
        });backgroundTask.shutdown();//when all task completed then only shutdown
    }
    private void activeSkillMLGTextFormat(ProgressDialogHelper progressBar,boolean forShareTrueForDownloadFalse,boolean lastBackUpEachFileIndicator[]) {
        ExecutorService backgroundTask = Executors.newSingleThreadExecutor();//Executors.newSingleThreadExecutor() creates a thread pool with a single thread. This means that only one task can be executed at a time. If there are more than one task waiting to be executed, the remaining tasks will be queued until the current task is finished.
        backgroundTask.execute(() -> {
            if(!MyUtility.checkPermissionForReadAndWriteToExternalStorage(this)) {
                this.runOnUiThread(() -> Toast.makeText(this, "EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show());
                //to read and write own app specific directory from minsdk 29 to 33+ we don't require READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE due to scope storage after android 10
                // ActivityCompat.requestPermissions((Activity)getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 41);
                return;
            }

            this.runOnUiThread(() -> progressBar.showProgressBar());

            TextAndPdfFormatBackup dataBackup=new TextAndPdfFormatBackup(this);
            File textFile=convertTextToTextFile(dataBackup.backupActiveMLGDataInTextFormat(),MyUtility.getDateTime12hrForBackupFile()+GlobalConstants.BACKUP_ACTIVE_MLG_TEXT_FILE_NAME.getValue(),this);
            if(textFile != null){

               if(forShareTrueForDownloadFalse){
                     if(shareLargeDataAsTextFileToAnyApp(textFile ,getResources().getString(R.string.backup_active_skill_m_l_g)+" TEXT FILE",this)){
                         updateLastBackEachFile(lastBackUpEachFileIndicator, (byte) 1);
                     }else{
                         this.runOnUiThread(() -> Toast.makeText(this, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show());
                     }
                }else{//download
                   if(download(textFile,this)){
                       this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_saved_in_download_folder), Toast.LENGTH_SHORT).show());
                       updateLastBackEachFile(lastBackUpEachFileIndicator, (byte) 1);
                   }else{
                       this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
                   }
                }
            }else{
                this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
            }
            this.runOnUiThread(() -> progressBar.hideProgressBar());
        });backgroundTask.shutdown();//when all task completed then only shutdown
    }
    private File convertTextToTextFile(String textData,String fileName,Context context) {//if error return null
        if(textData==null || fileName==null) return null;
        try {
            File file = new File(context.getExternalCacheDir(), fileName + ".txt");//creating txt file in cache directory file name  getExternalCacheDir() is a method in Android's Context class that returns a File object representing the external storage directory specific to your app for storing cache files. This directory is automatically created for your app and is private to your app, meaning that other apps cannot access its contents.Cache files are temporary files that are used to improve the performance of your app. By storing files that your app frequently uses in the cache directory, you can avoid repeatedly reading or downloading those files from a remote source, which can slow down your app's performance.The getExternalCacheDir() method returns a File object that represents the path to your app's external cache directory, which you can use to save cache files or other temporary files that your app needs to access quickly. For example, when sharing an image, you can save the image to this directory before sharing it with other apps.
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(textData.getBytes());
            outputStream.close();
            return file;
        }catch (Exception x){
            x.printStackTrace();
            return null;
        }
    }
    private boolean download(File pdfFile, Context context){
        try {
            byte[] convertedBytes = Files.readAllBytes(Paths.get(pdfFile.getAbsolutePath()));// This code uses the Files.readAllBytes() method from the java.nio.file package to read all the bytes from the file specified by the absolute path into a byte array. This method is more concise and efficient .
            if(!MyUtility.downloadPdfUsingByteInDownloadFolder(pdfFile.getName(),convertedBytes, context)) return false;
        }catch (Exception x){
            x.printStackTrace();
            return false;
        }
        return true;
    }
    private void setBackupEachFileUserSelectedFileFormat() {//if user has not selected ant file format then default file format will be set
        String userSelectedFileFormat= SharedPreferencesHelper.getString(this,SharedPreferencesHelper.Keys.BACKUP_EACH_FILE_USER_SELECTED_FORMAT.name(),defaultBackupEachFileFormat); // default file format will be set if user not selected any radio button
        if(userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_PDF_FORMAT.getValue())){
            binding.backupEachFilePdf.setChecked(true);
        }else if (userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_TEXT_FORMAT.getValue())) {
            binding.backupEachFileText.setChecked(true);
        } else if (userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_EXCEL_FORMAT.getValue())) {
            binding.backupEachFileExcel.setChecked(true);
        }
    }
    private void setSingleBackupFileUserSelectedFileFormat() {//if user has not selected ant file format then default file format will be set
        String userSelectedFileFormat= SharedPreferencesHelper.getString(this,SharedPreferencesHelper.Keys.SINGLE_BACKUP_FILE_USER_SELECTED_FORMAT.name(),defaultSingleBackupFileFormat); // default file format will be set if user not selected any radio button
        if(userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_PDF_FORMAT.getValue())){
            binding.singleFileBackupPdfFileRb.setChecked(true);
        }else if (userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_TEXT_FORMAT.getValue())) {
            binding.singleFileBackupTextFileRb.setChecked(true);
        } else if (userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_EXCEL_FORMAT.getValue())) {
            binding.singleFileBackupExcelFileRb.setChecked(true);
        }
    }
    public static boolean isExternalStorageReadOnly() {//Checks if Storage is READ-ONLY
        String externalStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(externalStorageState);
    }
    public static boolean isExternalStorageAvailable() {//Checks if Storage is Available
        String externalStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(externalStorageState);
    }
    private boolean shareLargeDataAsTextFileToAnyApp(File textFile,String titleForSharing,Context context){//if error return null
        if(textFile==null|| context==null||titleForSharing==null){
            return false;
        }
            if (!MyUtility.shareFileToAnyApp(textFile , "text/plain", titleForSharing,context)) {//open intent to share
                //Toast.makeText(context, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show();
                Log.e("ERROR OCCURRED","CANNOT SHARE FILE");
                return false;
            }
            return true;
    }
    private void performOperationWhenDestroy() {
        if(dialog != null){
            dialog.dismiss();
        }
        if(!MyUtility.deleteFolderAndSubFolderAllFiles(GlobalConstants.PDF_FOLDER_NAME.getValue(),true,getBaseContext())){//delete external file
            Toast.makeText(this, "FAILED TO DELETE FILE FROM DEVICE", Toast.LENGTH_LONG).show();
        }
        if(!MyUtility.deleteFolderAndSubFolderAllFiles(null,false,getBaseContext())){//delete cache file.text file is stored in cache area
            Toast.makeText(this, "FAILED TO DELETE FILE FROM DEVICE", Toast.LENGTH_LONG).show();
        }
        if(!MyUtility.deleteFolderAndSubFolderAllFiles(GlobalConstants.EXCEL_FOLDER_NAME.getValue(),true,getBaseContext())){//delete external file
            Toast.makeText(this, "FAILED TO DELETE FILE FROM DEVICE", Toast.LENGTH_LONG).show();
        }
        if(!MyUtility.deleteFolderAndSubFolderAllFiles(GlobalConstants.DATABASE_FOLDER_NAME.getValue(),true,getBaseContext())){//delete external file
            Toast.makeText(this, "FAILED TO DELETE FILE FROM DEVICE", Toast.LENGTH_LONG).show();
        }
        Database.closeDatabase();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        performOperationWhenDestroy();
    }
}