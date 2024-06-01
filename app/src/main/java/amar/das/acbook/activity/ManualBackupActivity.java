package amar.das.acbook.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import amar.das.acbook.R;
import amar.das.acbook.databinding.ActivityManualBackupBinding;
import amar.das.acbook.globalenum.GlobalConstants;
import amar.das.acbook.progressdialog.ProgressDialogHelper;
import amar.das.acbook.sharedpreferences.SharedPreferencesHelper;
import amar.das.acbook.takebackupdata.ExcelFormatBackup;
import amar.das.acbook.takebackupdata.TextAndPdfFormatBackup;
import amar.das.acbook.utility.MyUtility;

public class ManualBackupActivity extends AppCompatActivity {
    ActivityManualBackupBinding binding;
    String defaultBackupEachFileFormat= GlobalConstants.USER_SELECTED_PDF_FORMAT.getValue();//DEFAULT VALUE
    boolean forShareTrueForDownloadFalse=true;//default value
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
        ProgressDialogHelper progressBar = new ProgressDialogHelper(this);
        boolean lastBackUpEachFileIndicator[]=new boolean[4];
        setBackupEachFileUserSelectedFileFormat();//backup each file
        setLastBackupOfEachFile();
        binding.backupEachFileRadiogroup.setOnCheckedChangeListener((radioGroup, checkedIdOfRadioBtn) -> {
            if(checkedIdOfRadioBtn == R.id.backup_each_file_pdf){
                SharedPreferencesHelper.setString(this,SharedPreferencesHelper.Keys.BACKUP_EACH_FILE_USER_SELECTED_FORMAT.name(), GlobalConstants.USER_SELECTED_PDF_FORMAT.getValue());
            } else if (checkedIdOfRadioBtn == R.id.backup_each_file_text) {
                SharedPreferencesHelper.setString(this,SharedPreferencesHelper.Keys.BACKUP_EACH_FILE_USER_SELECTED_FORMAT.name(), GlobalConstants.USER_SELECTED_TEXT_FORMAT.getValue());
            }else if (checkedIdOfRadioBtn == R.id.backup_each_file_excel) {
                SharedPreferencesHelper.setString(this,SharedPreferencesHelper.Keys.BACKUP_EACH_FILE_USER_SELECTED_FORMAT.name(), GlobalConstants.USER_SELECTED_EXCEL_FORMAT.getValue());
            }
        });
        binding.backupActiveSkillMlg.setOnClickListener(view -> {
            if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {// Check if available and not read only.In summary, these methods act as safeguards for your application's interaction with external storage. By using them effectively, you can prevent errors, improve user experience, and gracefully handle situations where external storage isn't available or writable.
                Toast.makeText(this, "Storage not available or read only", Toast.LENGTH_LONG).show();//Log.e(TAG, "Storage not available or read only");
                return ;
            }
            forShareTrueForDownloadFalse =(SharedPreferencesHelper.getBoolean(this,SharedPreferencesHelper.Keys.BACKUP_FILE_SHARE_OR_DOWNLOAD.name(),forShareTrueForDownloadFalse))?true:false;//if user selected share then true else false for download.if user changes from share to download then dynamically value will be updated.so placing this here is important

            String userSelectedFileFormat= SharedPreferencesHelper.getString(this,SharedPreferencesHelper.Keys.BACKUP_EACH_FILE_USER_SELECTED_FORMAT.name(),defaultBackupEachFileFormat); // default file format will be use if user not selected any radio button
            if(userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_PDF_FORMAT.getValue())){
                activeSkillMLGPdfFormat(progressBar,forShareTrueForDownloadFalse,lastBackUpEachFileIndicator);

            }else if (userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_TEXT_FORMAT.getValue())) {
                activeSkillMLGTextFormat(progressBar,forShareTrueForDownloadFalse,lastBackUpEachFileIndicator);

            } else if (userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_EXCEL_FORMAT.getValue())) {
                activeSkillMLGExcelFormat(progressBar,forShareTrueForDownloadFalse,lastBackUpEachFileIndicator);
            }
        });
        binding.backupInactiveSkillM.setOnClickListener(view -> {

            if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {// Check if available and not read only.In summary, these methods act as safeguards for your application's interaction with external storage. By using them effectively, you can prevent errors, improve user experience, and gracefully handle situations where external storage isn't available or writable.
                Toast.makeText(this, "Storage not available or read only", Toast.LENGTH_LONG).show();//Log.e(TAG, "Storage not available or read only");
                return ;
            }
            forShareTrueForDownloadFalse=(SharedPreferencesHelper.getBoolean(this,SharedPreferencesHelper.Keys.BACKUP_FILE_SHARE_OR_DOWNLOAD.name(),forShareTrueForDownloadFalse))?true:false;//if user selected share then true else false for download.if user changes from share to download then dynamically value will be updated.so placing this here is important

            String userSelectedFileFormat= SharedPreferencesHelper.getString(this,SharedPreferencesHelper.Keys.BACKUP_EACH_FILE_USER_SELECTED_FORMAT.name(),defaultBackupEachFileFormat); // default file format will be use if user not selected any radio button
            if(userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_PDF_FORMAT.getValue())){
                inActiveSkillMPdfFormat(progressBar, forShareTrueForDownloadFalse,getString(R.string.mestre));

            }else if (userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_TEXT_FORMAT.getValue())) {
                inActiveSkillMTextFormat(progressBar, forShareTrueForDownloadFalse,getString(R.string.mestre));

            } else if (userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_EXCEL_FORMAT.getValue())) {
                inActiveSkillMExcelFormat(progressBar, forShareTrueForDownloadFalse,getString(R.string.mestre));
            }
        });
        binding.backupInactiveSkillL.setOnClickListener(view -> {
            if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {// Check if available and not read only.In summary, these methods act as safeguards for your application's interaction with external storage. By using them effectively, you can prevent errors, improve user experience, and gracefully handle situations where external storage isn't available or writable.
                Toast.makeText(this, "Storage not available or read only", Toast.LENGTH_LONG).show();//Log.e(TAG, "Storage not available or read only");
                return ;
            }
            forShareTrueForDownloadFalse=(SharedPreferencesHelper.getBoolean(this,SharedPreferencesHelper.Keys.BACKUP_FILE_SHARE_OR_DOWNLOAD.name(),forShareTrueForDownloadFalse))?true:false;//if user selected share then true else false for download.if user changes from share to download then dynamically value will be updated.so placing this here is important

            String userSelectedFileFormat= SharedPreferencesHelper.getString(this,SharedPreferencesHelper.Keys.BACKUP_EACH_FILE_USER_SELECTED_FORMAT.name(),defaultBackupEachFileFormat); // default file format will be use if user not selected any radio button
            if(userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_PDF_FORMAT.getValue())){
                inActiveSkillLPdfFormat(progressBar,forShareTrueForDownloadFalse,getString(R.string.laber));

            }else if (userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_TEXT_FORMAT.getValue())) {
                inActiveSkillLTextFormat(progressBar,forShareTrueForDownloadFalse,getString(R.string.laber));

            } else if (userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_EXCEL_FORMAT.getValue())) {
                inActiveSkillLExcelFormat(progressBar,forShareTrueForDownloadFalse,getString(R.string.laber));
            }

        });
        binding.backupInactiveSkillG.setOnClickListener(view -> {
            if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {// Check if available and not read only.In summary, these methods act as safeguards for your application's interaction with external storage. By using them effectively, you can prevent errors, improve user experience, and gracefully handle situations where external storage isn't available or writable.
                Toast.makeText(this, "Storage not available or read only", Toast.LENGTH_LONG).show();//Log.e(TAG, "Storage not available or read only");
                return ;
            }
            forShareTrueForDownloadFalse=(SharedPreferencesHelper.getBoolean(this,SharedPreferencesHelper.Keys.BACKUP_FILE_SHARE_OR_DOWNLOAD.name(),forShareTrueForDownloadFalse))?true:false;//if user selected share then true else false for download.if user changes from share to download then dynamically value will be updated.so placing this here is important

            String userSelectedFileFormat= SharedPreferencesHelper.getString(this,SharedPreferencesHelper.Keys.BACKUP_EACH_FILE_USER_SELECTED_FORMAT.name(),defaultBackupEachFileFormat); // default file format will be use if user not selected any radio button
            if(userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_PDF_FORMAT.getValue())){
                inActiveSkillGPdfFormat(progressBar,forShareTrueForDownloadFalse,getString(R.string.women_laber));

            }else if (userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_TEXT_FORMAT.getValue())) {
                inActiveSkillGTextFormat(progressBar,forShareTrueForDownloadFalse,getString(R.string.women_laber));

            } else if (userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_EXCEL_FORMAT.getValue())) {
                inActiveSkillGExcelFormat(progressBar,forShareTrueForDownloadFalse,getString(R.string.women_laber));
            }
        });
        binding.backupEachFile.setOnClickListener(view -> {
            MyUtility.showDefaultDialog(getString(R.string._1_dot_backup_each_file),getString(R.string.you_can_backup_your_data_in_small_file_size_separately),this,true);//it is activity so we have to pass this
        });
        binding.gobackBackupActivity.setOnClickListener(view -> {
            finish(); // This finishes the current activity

//            finish();//first destroy current activity then go back
//            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//            transaction.replace(R.id.manual_backup_layout,new MLDrawerFragment()).commit();
        });
    }

    private void setLastBackupOfEachFile() {
        String backupDate=SharedPreferencesHelper.getString(this,SharedPreferencesHelper.Keys.LAST_BACKUP_EACH_FILE.name(),null);
        if( backupDate != null ){
            MyUtility.getBackupDateFormat(backupDate);//if date matches today date then make green tick visible else invisible
            binding.backupEachFileLastBackup.setText(getString(R.string.last_backup_colon)+" "+backupDate);
        }else{
            binding.backupEachFileLastBackup.setText("LAST BACKUP: -");
        }
    }
    private void inActiveSkillGTextFormat(ProgressDialogHelper progressBar,boolean forShareTrueForDownloadFalse,String skillType) {
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
            File textFile=convertTextToTextFile(dataBackup.backupInActiveMOrLOrGDataInTextFormat(skillType),MyUtility.backupDateTime()+GlobalConstants.BACKUP_INACTIVE_G_TEXT_FILE_NAME.getValue(),this);
            if(textFile != null){

                if(forShareTrueForDownloadFalse){
                    if(!shareLargeDataAsTextFileToAnyApp(textFile ,getResources().getString(R.string.backup_inactive_skill_g)+" TEXT FILE",this)){
                        this.runOnUiThread(() -> Toast.makeText(this, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show());
                    }
                }else{//download
                    if(download(textFile,this)){
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_saved_in_download_folder), Toast.LENGTH_SHORT).show());
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
    private void inActiveSkillGExcelFormat(ProgressDialogHelper progressBar,boolean forShareTrueForDownloadFalse,String skillType) {
        ExecutorService backgroundTask = Executors.newSingleThreadExecutor();//Executors.newSingleThreadExecutor() creates a thread pool with a single thread. This means that only one task can be executed at a time. If there are more than one task waiting to be executed, the remaining tasks will be queued until the current task is finished.
        backgroundTask.execute(() -> {
            if(!MyUtility.checkPermissionForReadAndWriteToExternalStorage(this)) {
                this.runOnUiThread(() -> Toast.makeText(this, "EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show());
                //to read and write own app specific directory from minsdk 29 to 33+ we don't require READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE due to scope storage after android 10
                // ActivityCompat.requestPermissions((Activity)getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 41);
                return;
            }

            this.runOnUiThread(() -> progressBar.showProgressBar());

            ExcelFormatBackup excelObj=new ExcelFormatBackup(this,GlobalConstants.BACKUP_INACTIVE_G_EXCEL_FILE_NAME.getValue());
            File excelFile=null;

            if((excelFile=excelObj.backupInActiveMOrLOrGDataInExcelFormat(skillType)) !=null){
                if(forShareTrueForDownloadFalse){
                    if (!MyUtility.shareFileToAnyApp(excelFile, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "BACKUP INACTIVE SKILL G EXCEL FILE", this)) {//open intent to share
                        this.runOnUiThread(() -> Toast.makeText(this, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show());
                    }
                }else{//download
                    if(download(excelFile,this)){
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_saved_in_download_folder), Toast.LENGTH_SHORT).show());
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
    private void inActiveSkillGPdfFormat(ProgressDialogHelper progressBar,boolean forShareTrueForDownloadFalse,String skillType) {
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
            if((pdfFile=dataBackup.backupInActiveMOrLOrGDataInPDFFormat(MyUtility.backupDateTime()+GlobalConstants.BACKUP_INACTIVE_G_PDF_FILE_NAME.getValue(),skillType)) !=null){

                if(forShareTrueForDownloadFalse) {//share
                    if (!MyUtility.shareFileToAnyApp(pdfFile, "application/pdf", getResources().getString(R.string.backup_inactive_skill_g) + " PDF FILE", this)) {
                        this.runOnUiThread(() -> Toast.makeText(this, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show());
                    }
                }else{//download
                    if(download(pdfFile,this)){
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_saved_in_download_folder), Toast.LENGTH_SHORT).show());
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
    private void inActiveSkillLExcelFormat(ProgressDialogHelper progressBar,boolean forShareTrueForDownloadFalse,String skillType) {
        ExecutorService backgroundTask = Executors.newSingleThreadExecutor();//Executors.newSingleThreadExecutor() creates a thread pool with a single thread. This means that only one task can be executed at a time. If there are more than one task waiting to be executed, the remaining tasks will be queued until the current task is finished.
        backgroundTask.execute(() -> {
         if(!MyUtility.checkPermissionForReadAndWriteToExternalStorage(this)) {
                this.runOnUiThread(() -> Toast.makeText(this, "EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show());
                //to read and write own app specific directory from minsdk 29 to 33+ we don't require READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE due to scope storage after android 10
                // ActivityCompat.requestPermissions((Activity)getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 41);
                return;
            }

            this.runOnUiThread(() -> progressBar.showProgressBar());

            ExcelFormatBackup excelObj=new ExcelFormatBackup(this,GlobalConstants.BACKUP_INACTIVE_L_EXCEL_FILE_NAME.getValue());
            File excelFile=null;

            if((excelFile=excelObj.backupInActiveMOrLOrGDataInExcelFormat(skillType)) !=null){
                if(forShareTrueForDownloadFalse){
                    if (!MyUtility.shareFileToAnyApp(excelFile, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "BACKUP INACTIVE SKILL L EXCEL FILE", this)) {//open intent to share
                        this.runOnUiThread(() -> Toast.makeText(this, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show());
                    }
                }else{//download
                    if(download(excelFile,this)){
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_saved_in_download_folder), Toast.LENGTH_SHORT).show());
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
    private void inActiveSkillLTextFormat(ProgressDialogHelper progressBar,boolean forShareTrueForDownloadFalse,String skillType) {
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
            File textFile=convertTextToTextFile(dataBackup.backupInActiveMOrLOrGDataInTextFormat(skillType),MyUtility.backupDateTime()+GlobalConstants.BACKUP_INACTIVE_L_TEXT_FILE_NAME.getValue(),this);
            if(textFile != null){

                if(forShareTrueForDownloadFalse){
                    if(!shareLargeDataAsTextFileToAnyApp(textFile ,getResources().getString(R.string.backup_inactive_skill_l)+" TEXT FILE",this)){
                        this.runOnUiThread(() -> Toast.makeText(this, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show());
                    }
                }else{//download
                    if(download(textFile,this)){
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_saved_in_download_folder), Toast.LENGTH_SHORT).show());
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
    private void inActiveSkillLPdfFormat(ProgressDialogHelper progressBar,boolean forShareTrueForDownloadFalse,String skillType) {
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
            if((pdfFile=dataBackup.backupInActiveMOrLOrGDataInPDFFormat(MyUtility.backupDateTime()+GlobalConstants.BACKUP_INACTIVE_L_PDF_FILE_NAME.getValue(),skillType)) !=null){

                if(forShareTrueForDownloadFalse) {//share
                    if (!MyUtility.shareFileToAnyApp(pdfFile, "application/pdf", getResources().getString(R.string.backup_inactive_skill_l) + " PDF FILE", this)) {
                        this.runOnUiThread(() -> Toast.makeText(this, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show());
                    }
                }else{//download
                    if(download(pdfFile,this)){
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_saved_in_download_folder), Toast.LENGTH_SHORT).show());
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
    private void inActiveSkillMExcelFormat(ProgressDialogHelper progressBar,boolean forShareTrueForDownloadFalse,String skillType) {
        ExecutorService backgroundTask = Executors.newSingleThreadExecutor();//Executors.newSingleThreadExecutor() creates a thread pool with a single thread. This means that only one task can be executed at a time. If there are more than one task waiting to be executed, the remaining tasks will be queued until the current task is finished.
        backgroundTask.execute(() -> {
            if(!MyUtility.checkPermissionForReadAndWriteToExternalStorage(this)) {
                this.runOnUiThread(() -> Toast.makeText(this, "EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show());
                //to read and write own app specific directory from minsdk 29 to 33+ we don't require READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE due to scope storage after android 10
                // ActivityCompat.requestPermissions((Activity)getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 41);
                return;
            }

            this.runOnUiThread(() -> progressBar.showProgressBar());

            ExcelFormatBackup excelObj=new ExcelFormatBackup(this,GlobalConstants.BACKUP_INACTIVE_M_EXCEL_FILE_NAME.getValue());
            File excelFile=null;

            if((excelFile=excelObj.backupInActiveMOrLOrGDataInExcelFormat(skillType)) !=null){
                if(forShareTrueForDownloadFalse){
                    if (!MyUtility.shareFileToAnyApp(excelFile, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "BACKUP INACTIVE SKILL M EXCEL FILE", this)) {//open intent to share
                        this.runOnUiThread(() -> Toast.makeText(this, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show());
                    }
                }else{//download
                    if(download(excelFile,this)){
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_saved_in_download_folder), Toast.LENGTH_SHORT).show());
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

            ExcelFormatBackup excelObj=new ExcelFormatBackup(this,GlobalConstants.BACKUP_ACTIVE_MLG_EXCEL_FILE_NAME.getValue());
            File excelFile=null;

            if((excelFile=excelObj.backupActiveMLGDataInExcelFormat()) !=null){
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
    private void inActiveSkillMPdfFormat(ProgressDialogHelper progressBar,boolean forShareTrueForDownloadFalse,String skillType) {
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
            if((pdfFile=dataBackup.backupInActiveMOrLOrGDataInPDFFormat(MyUtility.backupDateTime()+GlobalConstants.BACKUP_INACTIVE_M_PDF_FILE_NAME.getValue(),skillType)) !=null){

                if(forShareTrueForDownloadFalse) {//share
                    if (!MyUtility.shareFileToAnyApp(pdfFile, "application/pdf", getResources().getString(R.string.backup_inactive_skill_m) + " PDF FILE", this)) {
                        this.runOnUiThread(() -> Toast.makeText(this, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show());
                    }
                }else{//download
                    if(download(pdfFile,this)){
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_saved_in_download_folder), Toast.LENGTH_SHORT).show());
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
            if((pdfFile=dataBackup.backupActiveMLGDataInPDFFormat(MyUtility.backupDateTime()+GlobalConstants.BACKUP_ACTIVE_MLG_PDF_FILE_NAME.getValue())) !=null){

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

    private void updateLastBackEachFile(boolean[] lastBackUpEachFileIndicator, byte fileNumber) {
        if(fileNumber==1){
            lastBackUpEachFileIndicator[0]=true;//means file is backed up by user successfully by user
            binding.backupActiveSkillMlgGreenTick.setVisibility(View.VISIBLE);
        }
        else if (fileNumber==2){
            lastBackUpEachFileIndicator[1]=true;
            binding.backupInactiveSkillMGreenTick.setVisibility(View.VISIBLE);
        }
        else if (fileNumber==3){
            lastBackUpEachFileIndicator[2]=true;
            binding.backupInactiveSkillLGreenTick.setVisibility(View.VISIBLE);
        }
        else if (fileNumber==4){
            lastBackUpEachFileIndicator[3]=true;
            binding.backupInactiveSkillGGreenTick.setVisibility(View.VISIBLE);
        }

        if(lastBackUpEachFileIndicator[0] && lastBackUpEachFileIndicator[1] && lastBackUpEachFileIndicator[2] && lastBackUpEachFileIndicator[3]){//if all file is backed bu by user
          //(Last Backup: 24-05-2044 at 09:05 am)
            SharedPreferencesHelper.setString(this,SharedPreferencesHelper.Keys.LAST_BACKUP_EACH_FILE.name(),MyUtility.get12hrCurrentTimeAndDate());//update value.   12 - 12 - 2024 (11:59 am)
            binding.backupEachFileLastBackup.setText(getString(R.string.last_backup_colon)+" "+SharedPreferencesHelper.getString(this,SharedPreferencesHelper.Keys.LAST_BACKUP_EACH_FILE.name(),null));
            binding.backupEachFileLastBackup.setTextColor(getColor(R.color.green));//set color to green
        }
    }

    private void inActiveSkillMTextFormat(ProgressDialogHelper progressBar, boolean forShareTrueForDownloadFalse,String skillType) {
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
            File textFile=convertTextToTextFile(dataBackup.backupInActiveMOrLOrGDataInTextFormat(skillType),MyUtility.backupDateTime()+GlobalConstants.BACKUP_INACTIVE_M_TEXT_FILE_NAME.getValue(),this);
            if(textFile != null){

                if(forShareTrueForDownloadFalse){
                    if(!shareLargeDataAsTextFileToAnyApp(textFile ,getResources().getString(R.string.backup_inactive_skill_m)+" TEXT FILE",this)){
                        this.runOnUiThread(() -> Toast.makeText(this, "CANNOT SHARE FILE", Toast.LENGTH_LONG).show());
                    }
                }else{//download
                    if(download(textFile,this)){
                        this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_saved_in_download_folder), Toast.LENGTH_SHORT).show());
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
            File textFile=convertTextToTextFile(dataBackup.backupActiveMLGDataInTextFormat(),MyUtility.backupDateTime()+GlobalConstants.BACKUP_ACTIVE_MLG_TEXT_FILE_NAME.getValue(),this);
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
    private static boolean isExternalStorageReadOnly() {//Checks if Storage is READ-ONLY
        String externalStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(externalStorageState);
    }
    private static boolean isExternalStorageAvailable() {//Checks if Storage is Available
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
}