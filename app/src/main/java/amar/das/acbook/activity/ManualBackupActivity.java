package amar.das.acbook.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import amar.das.acbook.R;
import amar.das.acbook.databinding.ActivityManualBackupBinding;
import amar.das.acbook.globalenum.GlobalConstants;
import amar.das.acbook.progressdialog.ProgressDialogHelper;
import amar.das.acbook.sharedpreferences.SharedPreferencesHelper;
import amar.das.acbook.takebackupdata.AllDataBackup;
import amar.das.acbook.utility.MyUtility;

public class ManualBackupActivity extends AppCompatActivity {
    ActivityManualBackupBinding binding;  
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
        binding.backupActiveSkillMlg.setOnClickListener(view -> {
            if(SharedPreferencesHelper.getBoolean(this,SharedPreferencesHelper.Keys.FOR_SHARE_TRUE_AND_FOR_DOWNLOAD_FALSE.name(),true)){//CHECKING FOR DOWNLOAD OR FOR SHARE

              byte userSelectedFileFormat= (byte) SharedPreferencesHelper.getInt(this,SharedPreferencesHelper.Keys.BACKUP_EACH_FILE_FORMAT.name(),Integer.parseInt(GlobalConstants.USER_SELECTED_PDF_FORMAT.getValue())); //default value is pdf format.checking for which file format
                if(userSelectedFileFormat==Integer.parseInt(GlobalConstants.USER_SELECTED_PDF_FORMAT.getValue())){

                   activeSkillMLGPdfFormat(progressBar);

                }else if (userSelectedFileFormat==Integer.parseInt(GlobalConstants.USER_SELECTED_TEXT_FORMAT.getValue())) {

                    activeSkillMLGTextFormat(progressBar);

                } else if (userSelectedFileFormat==Integer.parseInt(GlobalConstants.USER_SELECTED_EXCEL_FORMAT.getValue())) {

                }
            }else{//for download
                
            }
        });
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

            AllDataBackup dataBackup=new AllDataBackup(this);
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

            AllDataBackup dataBackup=new AllDataBackup(this);
            if(!dataBackup.backupActiveMLGDataInTextFormat(GlobalConstants.BACKUP_ACTIVE_MLG_TEXT_FILE_NAME.getValue())){
                this.runOnUiThread(() -> Toast.makeText(this, this.getString(R.string.backup_failed), Toast.LENGTH_LONG).show());
            }
            this.runOnUiThread(() -> progressBar.hideProgressBar());
        });backgroundTask.shutdown();//when all task completed then only shutdown
    }
}