package amar.das.acbook.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashSet;

import amar.das.acbook.ImageResizer;
import amar.das.acbook.Database;
import amar.das.acbook.R;

import amar.das.acbook.ui.search.SearchFragment;
import amar.das.acbook.utility.MyUtility;


public class InsertDataActivity extends AppCompatActivity {
    Button add;
  EditText name,account, acHolderName, ifscCode, aadhaarCard,phone, fatherName;
  AutoCompleteTextView bankName_autoComplete, location_autoComplete, religion_autoComplete;
  Database personDb;
  RadioGroup radioGroup;
  RadioButton  laberRadio,womenRadio,mestreRadio;
  String skill,fromIntentPersonId;
  String[] indianBank;//to store array
    HashSet<String> religionHashSet,locationHashSet;
    //********************for camera and galary***********************
    private static final int GalleryPick = 1;
    private static final int CAMERA_REQUEST = 100;
    private static final int STORAGE_REQUEST = 200;
    private static final int IMAGEPICK_GALLERY_REQUEST = 300;
    private static final int IMAGE_PICKCAMERA_REQUEST = 400;
    String cameraPermission[];
    String storagePermission[];
    ImageView imageView;
    //****************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0); //we have used overridePendingTransition(), it is used to remove activity create animation while re-creating activity.
        setContentView(R.layout.activity_insert_data);


        //database created
        personDb=new Database(this);
        //set ids
        add=findViewById(R.id.add_button);
        name=findViewById(R.id.name_et);
        account=findViewById(R.id.accountno_et);
        acHolderName =findViewById(R.id.acholder_et);
        ifscCode =findViewById(R.id.ifsccode_et);
        bankName_autoComplete =findViewById(R.id.bankname_autocomplte_tv);
        location_autoComplete=findViewById(R.id.location_autoComplete_tv);
        religion_autoComplete=findViewById(R.id.religion_autoComplete_tv);
        aadhaarCard =findViewById(R.id.aadharcard_et);
        phone=findViewById(R.id.phonenumber_et);
        fatherName =findViewById(R.id.fathername_et);

        //radio button
        radioGroup=findViewById(R.id.skill_radiogp);
        laberRadio =findViewById(R.id.laber);//required when updating
        womenRadio=findViewById(R.id.women_laber);//required when updating
        mestreRadio=findViewById(R.id.mestre);//required when updating
        laberRadio.setChecked(true);//by default laber will be checked other wise person wont be able to find.But this default will not work while updating because manually setting checked radio
        skill="L";//skill default value otherwise null will be set as default so its important
        radioGroup.setOnCheckedChangeListener((radioGroup, checkedIdOfRadioBtn) -> {
            switch(checkedIdOfRadioBtn){
                case R.id.mestre:{
                    skill=getResources().getString(R.string.mestre);//updating skill variable
                    break;
                }
                case R.id.laber:{
                    skill=getResources().getString(R.string.laber);
                    break;
                }
                case R.id.women_laber:{
                    skill=getResources().getString(R.string.women_laber);
                    break;
                }
            }
        });

        indianBank =getResources().getStringArray(R.array.indian_bank_names); //get bank names
        ArrayAdapter<String> bankAdapter=new ArrayAdapter<>(InsertDataActivity.this, android.R.layout.simple_list_item_1, indianBank);
        bankName_autoComplete.setAdapter(bankAdapter);

        religionHashSet=new HashSet<>(Arrays.asList(MyUtility.getReligionFromDb(getBaseContext()))); //hashset is taken to insert only unique data in table
        ArrayAdapter<String> religionAdapter=new ArrayAdapter<>(InsertDataActivity.this, android.R.layout.simple_list_item_1, religionHashSet.toArray(new String[religionHashSet.size()]));
        religion_autoComplete.setAdapter(religionAdapter);

        locationHashSet=new HashSet<>(Arrays.asList(MyUtility.getLocationFromDb(getBaseContext())));//hashset is taken to insert only unique data in table
        ArrayAdapter<String> locationAdapter=new ArrayAdapter<>(InsertDataActivity.this, android.R.layout.simple_list_item_1, locationHashSet.toArray(new String[locationHashSet.size()]));
        location_autoComplete.setAdapter(locationAdapter);

        //For camera and galary*********************************************************************************
        imageView = findViewById(R.id.imageview);

        // allowing permissions of gallery and camera
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        // After clicking on text we will have
        // to choose whether to
        // select image from camera and gallery
        imageView.setOnClickListener(view -> showImagePicDialog());


        if(getIntent().hasExtra("ID")){//after getting all the ids setting all data according to id
            fromIntentPersonId=getIntent().getStringExtra("ID");//getting id from intent
            //retrieving data from db
            Cursor cursor1=personDb.getData("SELECT "+Database.COL_2_NAME+" , "+Database.COL_3_BANKAC+" , "+Database.COL_4_IFSCCODE+"" + " , "+Database.COL_5_BANKNAME+" , "+Database.COL_6_AADHAAR_NUMBER+" , "+Database.COL_7_ACTIVE_PHONE1+"," + ""+Database.COL_8_SKILL1 +","+Database.COL_9_ACCOUNT_HOLDER_NAME+","+Database.COL_10_IMAGE+","+Database.COL_11_ACTIVE_PHONE2+","+Database.COL_17_LOCATION+","+Database.COL_18_RELIGION+" FROM "+Database.TABLE_NAME1 +" WHERE "+Database.COL_1_ID+"='"+fromIntentPersonId+"'");

            if(cursor1 != null) {
                cursor1.moveToFirst();
                name.setText(cursor1.getString(0));
                account.setText(cursor1.getString(1));
                ifscCode.setText(cursor1.getString(2));
                bankName_autoComplete.setText(cursor1.getString(3));
                location_autoComplete.setText(cursor1.getString(10));
                religion_autoComplete.setText(cursor1.getString(11));
                aadhaarCard.setText(cursor1.getString(4));
                phone.setText(cursor1.getString(5));
                skill=cursor1.getString(6);//skill is variable

                 //radio button should be checked according to data
                 if(skill.equals(getResources().getString(R.string.laber)))
                    laberRadio.setChecked(true);
                 else if(skill.equals(getResources().getString(R.string.mestre)))
                     mestreRadio.setChecked(true);
                 else//skill.equals("G")
                     womenRadio.setChecked(true);

                fatherName.setText(cursor1.getString(7));

                byte[] image=cursor1.getBlob(8);//getting image from db as blob and storing in byte type Blop is large type
                //getting bytearray image from DB and converting  to bitmap to set in imageview
                Bitmap bitmap= BitmapFactory.decodeByteArray(image,0, image.length);
                imageView.setImageBitmap(bitmap);
                acHolderName.setText(cursor1.getString(9));
                add.setBackgroundResource(R.drawable.green_color_bg);
                add.setText(getResources().getString(R.string.save));
                cursor1.close();
            }else
                Toast.makeText(this, "cursor is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void showImagePicDialog() {
        String []options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image From");
        builder.setCancelable(true);
        builder.setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss());
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                if (!checkCameraPermission()) {
                    requestCameraPermission();
                } else {
                    pickFromGallery();
                }
            } else if (which == 1) {
                if (!checkStoragePermission()) {
                    requestStoragePermission();
                } else {
                    pickFromGallery();
                }
            }
        });
        builder.create().show();
    }

    // checking storage permissions
    @NonNull
    private Boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        Toast.makeText(this, ""+result, Toast.LENGTH_SHORT).show();
        return result;
    }

    // Requesting  gallery permission
    private void requestStoragePermission() {
        requestPermissions(storagePermission, STORAGE_REQUEST);
    }

    // checking camera permissions
    @NonNull
    private Boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        Toast.makeText(this, ""+result1+result, Toast.LENGTH_SHORT).show();
        return result && result1;
    }

    // Requesting camera permission
    private void requestCameraPermission() {
        requestPermissions(cameraPermission, CAMERA_REQUEST);
    }

    // Requesting camera and gallery
    // permission if not given
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST: {
                if (grantResults.length > 0) {
                    boolean camera_accepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageaccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (camera_accepted && writeStorageaccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Please Enable Camera and Storage Permissions", Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST: {
                if (grantResults.length > 0) {
                    boolean writeStorageaccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageaccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Please Enable Storage Permissions", Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
        }
    }
    // Here we will pick image from gallery or camera
    private void pickFromGallery() {
        CropImage.activity().start(InsertDataActivity.this);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Toast.makeText(this, "CROP OUT", Toast.LENGTH_SHORT).show();
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                imageView.setImageURI(resultUri);
                Toast.makeText(this, "crop if", Toast.LENGTH_SHORT).show();
            }else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
                Toast.makeText(this, "Failed to crop image", Toast.LENGTH_SHORT).show();
        }
    }
    //action while clicking insert button
    public void insert_click(View view) {
        add.setVisibility(View.GONE);//so that user do not enter again add buttion while data is  inserting in database beacuse if user do again then it will overload

        String personName=name.getText().toString().toUpperCase().trim();//taking all value in uppercase
        String personAccount=account.getText().toString().trim();
        String personPhoneNumber2= acHolderName.getText().toString().toUpperCase().trim();
        String personIfscCode= ifscCode.getText().toString().toUpperCase().trim();
        String personAadhaar= aadhaarCard.getText().toString().trim();
        String personActivePhoneNo2=phone.getText().toString().trim();
        String personAccountHolderName= fatherName.getText().toString().toUpperCase().trim();
        String personSkill=skill;
        String personBankName= bankName_autoComplete.getText().toString().trim();//autocomplete Text view
        String location=location_autoComplete.getText().toString().toUpperCase().trim();//autocomplete Text view
        String religion=religion_autoComplete.getText().toString().toUpperCase().trim();//autocomplete Text view

        //to store in db we have to convert imageview to Bitmap and Bitmap to bytearray and to retrieve it from db we have to convert bytearray to Bitmap to set in imageview
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();//A Drawable that wraps a bitmap and can be tiled, stretched..
        Bitmap fullSizeBitmapImage = drawable.getBitmap();//converted imageview to bitmap
        Bitmap reduceSize= ImageResizer.reduceBitmapSize(fullSizeBitmapImage,46000);//resizing image to store in db
        byte[] imageStore= convertBitmapToByteArray(reduceSize);//convertImageViewToByteArray(reduceSize); reduceSize contain image so sending to convert to byte array to store in database

            AlertDialog.Builder detailsReview = new AlertDialog.Builder(this);
            detailsReview.setCancelable(false);
            detailsReview.setTitle("REVIEW DETAILS");// Html tags video- https://www.youtube.com/watch?v=98BD6IjQQkE
            detailsReview.setMessage(HtmlCompat.fromHtml("Name-" +"<b>"+ personName+"</b>"+"<br>"+"<br>"+
                    "Active Phone No.1---  " +"<b>"+ personActivePhoneNo2 +"</b>" +"<br>"+"<br>"+
                    "Phone No.2------------- " +"<b>"+personPhoneNumber2+"</b>" +"<br>"+"<br>"+
                    "Location-- " +"<b>"+location+"</b>" +"<br>"+"<br>"+
                    "Religion-- " +"<b>"+religion+"</b>" +"<br>"+"<br>"+
                    "Aadhaar Card No.- " +"<b>"+ personAadhaar+"</b>"  +"<br>"+"<br>"+
                    "Bank Name-" +"<b>"+ personBankName+"</b>"+"<br>" +"<br>"+
                    "Account No.--" +"<b>"+ personAccount +"</b>" +"<br>"+"<br>"+
                    "IFSC Code---  " +"<b>"+ personIfscCode+"</b>"  +"<br>"+"<br>"+
                    "A/C Holder Name-" +"<b>"+ personAccountHolderName+"</b>" +"<br>"+"<br>"+
                    "Person Skill- " +"<b>"+ personSkill+"</b>"  +"<br>",HtmlCompat.FROM_HTML_MODE_LEGACY));

            detailsReview.setNegativeButton(getResources().getString(R.string.cancel), (dialogInterface, i) -> {
                dialogInterface.dismiss();
                add.setVisibility(View.VISIBLE);
            });
            detailsReview.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();

                    boolean  success;
                    //update
                    if(getIntent().hasExtra("ID")){//will execute only when updating

                        if(!MyUtility.updateLocationReligionToTableIf(locationHashSet,location,religionHashSet,religion,getBaseContext())){//UPDATING location and religion table
                            Toast.makeText(InsertDataActivity.this, "NOT UPDATED", Toast.LENGTH_LONG).show();
                        }
                        success=personDb.updateDataTable1(personName, personAccount, personIfscCode, personBankName, personAadhaar, personActivePhoneNo2, personSkill, personAccountHolderName, imageStore, personPhoneNumber2,fromIntentPersonId,location,religion);
                        if(success){//if it is updated then show successfully message
                            Toast.makeText(InsertDataActivity.this, "ID- "+fromIntentPersonId+" "+getResources().getString(R.string.updated_successfully), Toast.LENGTH_SHORT).show();

                            //whenever user update its name,bank account,etc theN IF that account is inactive then that account will become active that is its latest date is updated to current date
//                            final Calendar current=Calendar.getInstance();//to get current date
//                            String currentDate =current.get(Calendar.DAY_OF_MONTH)+"-"+(current.get(Calendar.MONTH)+1)+"-"+current.get(Calendar.YEAR);
//                            personDb.updateTable("UPDATE " + personDb.TABLE_NAME1 + " SET  LATESTDATE='" + currentDate + "'" +" WHERE ID='" + fromIntentPersonId + "'");


                            //after success then go to previous activity automatically and destroy current activity so that when pressing back user should not get same activity this is done by finish();
                            Intent in=new Intent(getBaseContext(),IndividualPersonDetailActivity.class);//completed then go back
                            in.putExtra("ID",fromIntentPersonId);//after going back to this IndividualPersonDetailActivity then it require ID so putExtra is used
                            startActivity(in);
                            finish();//destroy current activity

                        }else
                            Toast.makeText(InsertDataActivity.this, "DATA NOT UPDATED", Toast.LENGTH_LONG).show();

                    }else {//this will execute only when adding new person

                        //for (int k = 1; k <= 10; k++) {
                            //inserting data to sqlite database
                        if(!MyUtility.updateLocationReligionToTableIf(locationHashSet,location,religionHashSet,religion,getBaseContext())){//UPDATING location and religion table
                            Toast.makeText(InsertDataActivity.this, "NOT INSERTED", Toast.LENGTH_LONG).show();
                        }
                        success = personDb.insertDataTable1(personName, personAccount, personIfscCode, personBankName, personAadhaar, personActivePhoneNo2, personSkill, personAccountHolderName, imageStore, personPhoneNumber2,location,religion);
                        //}

                        if (success) {//checking for duplicate
                            Cursor result = personDb.getId(personName, personAccount, personIfscCode, personBankName, personAadhaar, personActivePhoneNo2, personSkill, personAccountHolderName, personPhoneNumber2,location,religion);
                            StringBuilder buffer;//because it is not synchronized and efficient then string buffer and no need to lock and unlock
                            String holdLastId="";

                            if(result.getCount() == 1 || result.getCount() > 1){//no duplicate
                                buffer=new StringBuilder( );

                                if(result.moveToFirst() && result.getCount()==1) {//ONLY 1 DATE NO DUPLICATE

                                    insertDataToTable3(result.getString(0));//update R1,R2,R3,R4 TO 0

                                    buffer.append("\n" + "NEW PERSON ID- ").append(result.getString(0));
                                    displayResult("CREATED SUCCESSFULLY", buffer.toString());
                                    add.setVisibility(View.VISIBLE);
                                }

                                if(result.getCount() > 1){//this will be true when user all details is same to others means DUPLICATE
                                    buffer.append("Matching ").append(result.getCount()).append(" Person with same Details-").append("\n");
                                    result.moveToPrevious();//it help to start from first otherwise 1 item is not displayed
                                    while(result.moveToNext()){
                                        holdLastId=""+result.getString(0);//to display new added person ids comes at last when loop
                                        buffer.append("\nPerson ID- ").append(result.getString(0));
                                    }
                                    //update R1,R2,R3,R4 TO 0
                                    insertDataToTable3(holdLastId);//hold last id variable has newly added id.If this insertDataToTable3(hold last id);  method is placed in while loop then all matching duplicate then have to  execute which is useless and produce exception

                                    displayResult("Successfully Added New Person ID- "+holdLastId,buffer.toString());
                                    add.setVisibility(View.VISIBLE);
                                }
                                result.close();//closing cursor
                            }
                            else
                                displayResult("Data is Inserted but Query not returned any ID","\n"+"result.getCount()= "+result.getCount());

                            // eraseAllDataAfterInsertingFromLayout();//should be here because control does not wait for its execution
                        }
                        else
                            displayResult("Data FAILED to Insert","\n"+"Number of column maybe different in DataBase");
                    }
                }
                private void displayResult(String title, String message) {
                    AlertDialog.Builder showDataFromDataBase = new AlertDialog.Builder(InsertDataActivity.this);
                    showDataFromDataBase.setCancelable(false);
                    showDataFromDataBase.setTitle(title);
                    showDataFromDataBase.setMessage(message);
                    showDataFromDataBase.setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss());
                    showDataFromDataBase.create().show();
                }
                private void eraseAllDataAfterInsertingFromLayout() {
                    name.setText("");
                    account.setText("");
                    acHolderName.setText("");
                    ifscCode.setText("");
                    bankName_autoComplete.setText("");
                    aadhaarCard.setText("");
                    phone.setText("");
                    fatherName.setText("");
                    imageView.setImageResource(R.drawable.defaultprofileimage);
                }
            });
       detailsReview.create().show();
    }
    private void insertDataToTable3(String id) {
        boolean bool=personDb.insertDataTable3( id,0,0,0,0,null,null,null,null);
        if(!bool)
            Toast.makeText(this, "Not Inserted to table 3", Toast.LENGTH_LONG).show();
    }
    private byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();//converted bitmap to byte array
    }
    public void go_back(View view) {
         //from activity to activity
        if(getIntent().hasExtra("ID")){//execute when it is called from other activity with ID intent
            Intent in=new Intent(getBaseContext(),IndividualPersonDetailActivity.class); //go back
            in.putExtra("ID",fromIntentPersonId);//after going back to this IndividualPersonDetailActivity then it require ID so putExtra is used
            startActivity(in);
            finish();//destroy current activity
        }else {//go from activity to fragment
            finish();//first destroy current activity then go back
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.insert_detailsof_l_m_g, new SearchFragment()).commit();
        }
    }
}