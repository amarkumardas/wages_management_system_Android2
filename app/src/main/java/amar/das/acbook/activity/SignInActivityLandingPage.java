package amar.das.acbook.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import amar.das.acbook.Database;
import amar.das.acbook.R;
import amar.das.acbook.databinding.ActivitySigninWithGoogleBinding;
import amar.das.acbook.progressdialog.ProgressDialogHelper;
import amar.das.acbook.sharedpreferences.KeyValueTable;
import amar.das.acbook.sharedpreferences.SharedPreferencesHelper;
import amar.das.acbook.utility.BackupDataUtility;
import amar.das.acbook.utility.MyUtility;

public class SignInActivityLandingPage extends AppCompatActivity {
  ActivitySigninWithGoogleBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if(isUserSignIn()){//if user already signing
//            GoogleIdOptionsUtil.goToNavigationActivity(this);
//        }

        EdgeToEdge.enable(this);//The enableEdgeToEdge() method allows your Android app to display content using the full width and height of the screen. It makes system bars transparent, adjusts system icon colors, and ensures an edge-to-edge layout
//      setContentView(R.layout.activity_signin_with_google);
        binding = ActivitySigninWithGoogleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signin_layout),(v, insets) -> {// This code snippet adjusts the padding of a view based on the system bars’ insets, ensuring proper layout and avoiding content overlap with system UI elements
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        binding.newUserButton.setOnClickListener(view ->{
            SharedPreferencesHelper.setBoolean(this,SharedPreferencesHelper.Keys.SIGNIN_SKIP_TRUE.name(),true);
            finishAndGoToNavigationActivity();
        });

    ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (!ManualBackupActivity.isExternalStorageAvailable() || ManualBackupActivity.isExternalStorageReadOnly()) {// Check if available and not read only.In summary, these methods act as safeguards for your application's interaction with external storage. By using them effectively, you can prevent errors, improve user experience, and gracefully handle situations where external storage isn't available or writable.
                            Toast.makeText(this, "Storage not available or read only", Toast.LENGTH_LONG).show();//Log.e(TAG, "Storage not available or read only");
                            return ;
                        }

                        startDataRestoreProcess(result);

                    }else{
                        Toast.makeText(this, "No result from activity", Toast.LENGTH_LONG).show();
                    }
                });
    binding.findBackupButton.setOnClickListener(view -> {//we will able to select file which are created by this app only
        //System File Picker: No explicit permission request needed in Android 10 and above.The system file picker in Android is a built-in UI component that allows users to browse and select files from their device's storage or supported cloud storage providers (like Google Drive if the app is installed).
        //Storage Access Framework
        Intent chooseFileIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);//The ACTION_OPEN_DOCUMENT intent action allows users to select a specific document or file to open which is created by this app in android 10+.
        chooseFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFileIntent.setType("application/x-sqlite3"); //We set the file type filter using setType("application/x-sqlite3") to allow selecting only SQLite database files (.sqlite3).
        filePickerLauncher.launch(chooseFileIntent);
    });

    }
    private void startDataRestoreProcess(ActivityResult result) {//if data is not restored then cant go to navigation activity
        ExecutorService backgroundTask = Executors.newSingleThreadExecutor();//Executors.newSingleThreadExecutor() creates a thread pool with a single thread. This means that only one task can be executed at a time. If there are more than one task waiting to be executed, the remaining tasks will be queued until the current task is finished.
        backgroundTask.execute(() -> {
            ProgressDialogHelper progressBar = new ProgressDialogHelper(this);

            if (!MyUtility.checkPermissionForReadAndWriteToExternalStorage(this)) {
                this.runOnUiThread(() -> Toast.makeText(this, "EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show());
                //to read and write own app specific directory from minsdk 29 to 33+ we don't require READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE due to scope storage after android 10
                // ActivityCompat.requestPermissions((Activity)getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 41);
                return;
            }

            Uri uri = result.getData().getData(); // User selected a file

            if(!isSpaceAvailableInDeviceToRestoreBackupFile(uri)){//checking space available in device to restore
                this.runOnUiThread(() -> Toast.makeText(this, "NO SPACE AVAILABLE IN PHONE", Toast.LENGTH_LONG).show());
                return;
            }

            this.runOnUiThread(() -> progressBar.showProgressBar());

            Database database = Database.getInstance(getBaseContext());
            if (database.restoreDatabaseMethod1(uri)){

                if(checkAllDataRestoredSuccessfully()){
                    this.runOnUiThread(() -> Toast.makeText(this, getString(R.string.data_restored_successfully), Toast.LENGTH_LONG).show());
                    SharedPreferencesHelper.setBoolean(this, SharedPreferencesHelper.Keys.SIGNIN_SKIP_TRUE.name(), true);//user will move to navigation activity
                    SharedPreferencesHelper.setString(this, SharedPreferencesHelper.Keys.DATA_RESTORE_INFO.name(),"*"+getString(R.string.you_restored_data_successfully)+" on "+MyUtility.get12hrCurrentTimeAndDateForLastBackup());
                    finishAndGoToNavigationActivity();
                }else{
                    SharedPreferencesHelper.setString(this, SharedPreferencesHelper.Keys.DATA_RESTORE_INFO.name(),"*"+getString(R.string.your_data_was_not_fully_restored)+" on "+MyUtility.get12hrCurrentTimeAndDateForLastBackup());
                    this.runOnUiThread(() -> Toast.makeText(this, getString(R.string.your_data_was_not_fully_restored), Toast.LENGTH_LONG).show());
                    this.runOnUiThread(() -> Toast.makeText(this, getString(R.string.try_again), Toast.LENGTH_LONG).show());
                }

            }else {//try once more to restore with different method

                if (database.restoreDatabaseMethod2(uri)){

                    if(checkAllDataRestoredSuccessfully()){
                        this.runOnUiThread(() -> Toast.makeText(this, getString(R.string.data_restored_successfully), Toast.LENGTH_LONG).show());
                        SharedPreferencesHelper.setBoolean(this, SharedPreferencesHelper.Keys.SIGNIN_SKIP_TRUE.name(), true);//user will move to navigation activity
                        SharedPreferencesHelper.setString(this, SharedPreferencesHelper.Keys.DATA_RESTORE_INFO.name(),"*"+getString(R.string.you_restored_data_successfully)+" on "+MyUtility.get12hrCurrentTimeAndDateForLastBackup());
                        finishAndGoToNavigationActivity();
                    }else{
                        SharedPreferencesHelper.setString(this, SharedPreferencesHelper.Keys.DATA_RESTORE_INFO.name(),"*"+getString(R.string.your_data_was_not_fully_restored)+" on "+MyUtility.get12hrCurrentTimeAndDateForLastBackup());
                        this.runOnUiThread(() -> Toast.makeText(this, getString(R.string.your_data_was_not_fully_restored), Toast.LENGTH_LONG).show());
                        this.runOnUiThread(() -> Toast.makeText(this, getString(R.string.try_again), Toast.LENGTH_LONG).show());
                    }

                }else{
                    this.runOnUiThread(() -> Toast.makeText(this, getString(R.string.data_restore_failed), Toast.LENGTH_LONG).show());
                }
            }

            this.runOnUiThread(() -> progressBar.hideProgressBar());
        });backgroundTask.shutdown();//when all task completed then only shutdown
    }

    private boolean checkAllDataRestoredSuccessfully() {
        Database db=Database.getInstance(getBaseContext());
       String keywordValue=db.getValueFromKeyValueTable(KeyValueTable.TOTAL_CHECK_ROWS.name());//this keyword TOTAL_CHECK_ROWS contain total rows after subtracting
        if(keywordValue==null) return false;//means there was some error and this keyword TOTAL_CHECK_ROWS cannot contain null it will contain minus value or 0 or positive number
       String countOfTotalRowsFrom4TableAfterSubtraction=db.getTotalNumberOf4TablesRowsAfterSubtraction();//counting total no. of rows from 4 tables
       return  keywordValue.equals(countOfTotalRowsFrom4TableAfterSubtraction);//if true that means all data is fully backed up
    }

    private boolean isSpaceAvailableInDeviceToRestoreBackupFile(Uri uri) {//if device space is greater return true
        return  BackupDataUtility.checkDeviceInternalStorageAvailabilityInMB(getBaseContext()) > getFileSizeInMBUsingContentResolverOfBackupFile(uri,this);
    }

    private void finishAndGoToNavigationActivity() {
        this.finish();
        Intent intent = new Intent(this, NavigationActivity.class);
        this.startActivity(intent);
    }
    public long getFileSizeInMBUsingContentResolverOfBackupFile(Uri uri, Context context) {
        if (uri == null) {
            return -1; // Handle null URI
        }

        try (Cursor cursor = context.getContentResolver().query(
                uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1) {
                    long fileSizeInBytes = cursor.getLong(sizeIndex);
                    // Convert bytes to megabytes (ensure no integer overflow)
                    if (fileSizeInBytes == 0) {
                        return 0; // Handle zero-byte files
                    }
                    return fileSizeInBytes / (1024 * 1024);
                }
            }
        } catch (Exception e) {
            Log.e("ERROR", "Error getting file size", e);
        }

        return -1; // Indicate size unavailable
    }
//    private boolean isUserSignIn() {
   //     return SharedPreferencesHelper.getString(this,SharedPreferencesHelper.Keys.GOOGLE_SIGNIN_EMAIL.name(),null) != null;
//    }
   @Override
    protected void onDestroy() {
     super.onDestroy();
      Database.closeDatabase();
     }
}