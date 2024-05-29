package amar.das.acbook.activity;

import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
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
    String defaultBackupEachFileFormat= GlobalConstants.USER_SELECTED_PDF_FORMAT.getValue();
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

        setBackupEachFileUserSelectedFileFormat();
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

            if(SharedPreferencesHelper.getBoolean(this,SharedPreferencesHelper.Keys.FOR_SHARE_TRUE_AND_FOR_DOWNLOAD_FALSE.name(),true)){//CHECKING FOR DOWNLOAD OR FOR SHARE

              String userSelectedFileFormat= SharedPreferencesHelper.getString(this,SharedPreferencesHelper.Keys.BACKUP_EACH_FILE_USER_SELECTED_FORMAT.name(),defaultBackupEachFileFormat); // default file format will be use if user not selected any radio button
                if(userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_PDF_FORMAT.getValue())){
                    activeSkillMLGPdfFormat(progressBar);

                }else if (userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_TEXT_FORMAT.getValue())) {
                    activeSkillMLGTextFormat(progressBar);

                } else if (userSelectedFileFormat.equals(GlobalConstants.USER_SELECTED_EXCEL_FORMAT.getValue())) {
                    activeSkillMLGExcelFormat(progressBar);
                }
            }else{//for download
                
            }
        });
    }
    private void activeSkillMLGExcelFormat(ProgressDialogHelper progressBar) {
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

                if (!MyUtility.shareFileToAnyApp(excelFile,"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "BACKUP ACTIVE SKILL MLG EXCEL FILE",this)) {//open intent to share
                    this.runOnUiThread(() -> Toast.makeText(this,"CANNOT SHARE FILE", Toast.LENGTH_LONG).show());
                }
            }else{
                this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
            }
            this.runOnUiThread(() -> progressBar.hideProgressBar());
        });backgroundTask.shutdown();//when all task completed then only shutdown
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
    private void activeSkillMLGPdfFormat(ProgressDialogHelper progressBar) {
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
            if(!dataBackup.backupActiveMLGDataInPDFFormat(GlobalConstants.BACKUP_ACTIVE_MLG_PDF_FILE_NAME.getValue())){
                this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
            }
            this.runOnUiThread(() -> progressBar.hideProgressBar());
        });backgroundTask.shutdown();//when all task completed then only shutdown
    }
    private void activeSkillMLGTextFormat(ProgressDialogHelper progressBar) {
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
            if(!dataBackup.backupActiveMLGDataInTextFormat(GlobalConstants.BACKUP_ACTIVE_MLG_TEXT_FILE_NAME.getValue())){
                this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
            }
            this.runOnUiThread(() -> progressBar.hideProgressBar());
        });backgroundTask.shutdown();//when all task completed then only shutdown
    }
    private static boolean isExternalStorageReadOnly() {//Checks if Storage is READ-ONLY
        String externalStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(externalStorageState);
    }
    private static boolean isExternalStorageAvailable() {//Checks if Storage is Available
        String externalStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(externalStorageState);
    }
}