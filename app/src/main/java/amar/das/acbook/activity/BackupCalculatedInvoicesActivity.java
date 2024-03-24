package amar.das.acbook.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import amar.das.acbook.R;
import amar.das.acbook.adapters.BackUpCalculatedTextFileAdapter;
import amar.das.acbook.databinding.ActivityBackupCalculatedInvoicesBinding;
import amar.das.acbook.globalenum.GlobalConstants;
import amar.das.acbook.model.TextFileModel;
import amar.das.acbook.textfilegenerator.TextFile;
import amar.das.acbook.ui.ml.MLDrawerFragment;
import amar.das.acbook.utility.MyUtility;

public class BackupCalculatedInvoicesActivity extends AppCompatActivity {
       ActivityBackupCalculatedInvoicesBinding binding;
      byte toDeleteOldInvoiceIndicator=100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0); //we have used overridePendingTransition(), it is used to remove activity create animation while re-creating activity.
        binding = ActivityBackupCalculatedInvoicesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ArrayList<String> allFilePathFromDevice=getAllAbsolutePathOfFilesFromFolderDevice(GlobalConstants.TEXT_FILE_FOLDER_NAME.getValue());
        if(allFilePathFromDevice != null){//if null means error

          if(allFilePathFromDevice.size()==0){
              binding.totalBackupMessage.setText(getResources().getString(R.string.no_backup_invoice_available));
          }
            binding.textFileTotalCount.setText("" + allFilePathFromDevice.size());
            LinkedList<TextFileModel> pathList=new LinkedList<>();

            File file=null;TextFileModel model=null;
            for (String absolutePath: allFilePathFromDevice){//if allFilePathFromDevice is null then gives nullpointer exception
                file=new File(absolutePath);
                model=new TextFileModel();
                model.setAbsolutePath(absolutePath);
                model.setFileName(file.getName().replace(".txt","").trim());//removing file extension
                pathList.add(model);
            }

            Collections.sort(pathList);//in ascending order

            BackUpCalculatedTextFileAdapter textFileAdapter=new BackUpCalculatedTextFileAdapter(this,pathList);
            binding.textFileRecyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            binding.textFileRecyclerview.setAdapter(textFileAdapter);
            binding.textFileRecyclerview.setHasFixedSize(true);
            textFileAdapter.RecyclerViewListener(new BackUpCalculatedTextFileAdapter.RecyclerViewListener() {//this is automatically triggered when in adapter class any event or click happen
                @Override
                public void updatedCount(int count) {
                    if(count == 0){
                        binding.totalBackupMessage.setText(getResources().getString(R.string.no_backup_invoice_available));
                    }
                    binding.textFileTotalCount.setText("" +count);
                }
                @Override
                public void hideSearchBar(boolean trueForHide) {
                  if(trueForHide){
                      binding.searchbarGoback.setVisibility(View.GONE);
                  }else{
                      binding.searchbarGoback.setVisibility(View.VISIBLE);
                  }
                }
            });
            binding.textFileSerachView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }
                @Override
                public boolean onQueryTextChange(String newText) {
                    binding.textFileRecyclerview.setAdapter(textFileAdapter);

                    textFileAdapter.getFilter().filter(newText);
                    return false;
                }
            });
            binding.textfileCalculatedHint.setOnClickListener(view -> {
                MyUtility.showResult(getResources().getString(R.string.backup_and_freeup_space_tips),getResources().getString(R.string.calculated_invoice_backup_hint),view.getContext());
            });

            if(allFilePathFromDevice.size() >= toDeleteOldInvoiceIndicator){
                MyUtility.snackBar(binding.getRoot().findFocus(),getResources().getString(R.string.backup_calculated_invoice_and_delete_invoice_to_free_up_space));
            }

        }else if(allFilePathFromDevice==null){
            binding.totalBackupMessage.setText("ERROR IN RETRIEVING FILE");
        }
    }
    public void invoice_layout_go_back(View view){
        finish();//first destroy current activity then go back
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.all_calculated_invoices_layout, new MLDrawerFragment()).commit();
    }
    private ArrayList<String> getAllAbsolutePathOfFilesFromFolderDevice(String folderName){//if no data return empty list ie 0 size, and if error return null
        try{
            ArrayList<String> absolutePathList=new ArrayList<>();
            if(MyUtility.checkPermissionForReadAndWriteToExternalStorage(getBaseContext())){//checking permission

                File  folder= new File(getExternalFilesDir(null) + "/" + folderName);//File folder = new File( externalFileDir + "/acBookPDF");   //https://stackoverflow.com/questions/65125446/cannot-resolve-method-getexternalfilesdir

                if (folder.exists() && folder.isDirectory()) {//if folder exist and if it is directory then delete all file present in this folder

                    File[] listOfFiles = folder.listFiles();//getting all files present in folder
                    if (listOfFiles != null ){
                        for (File file : listOfFiles){

                            if (file.isFile()) {//file.isFile() is a method call on the File object, specifically the isFile() method. This method returns true if the File object refers to a regular file and false if it refers to a directory, a symbolic link, or if the file doesn't exist.
                                absolutePathList.add(file.getAbsolutePath());
                            }
                        }
                    }
                }
            }else{
                Toast.makeText(BackupCalculatedInvoicesActivity.this, "READ,WRITE EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(BackupCalculatedInvoicesActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 20);
             }
            return absolutePathList;
        }catch (Exception x){
            x.printStackTrace();
            return null;
        }
    }
//    private int findFileSizeInMB(File file) {
//        long fileSizeInBytes = file.length();
//        return (int) (fileSizeInBytes / (1024 * 1024));
//    }
}