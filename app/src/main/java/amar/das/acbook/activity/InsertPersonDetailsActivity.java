package amar.das.acbook.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import amar.das.acbook.ImageResizer;
import amar.das.acbook.Database;
import amar.das.acbook.R;

import amar.das.acbook.utility.MyUtility;


public class InsertPersonDetailsActivity extends AppCompatActivity {
  int [] correctInputArr =new int[10];
  Button add;
  EditText name,account, phone2, ifscCode, aadhaarNumber, activephone1, accountHolderName;
  AutoCompleteTextView bankName_autoComplete, location_autoComplete, religion_autoComplete;
  Database db;
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

        db=Database.getInstance(getBaseContext());
        add=findViewById(R.id.add_button);
        name=findViewById(R.id.name_et);
        account=findViewById(R.id.accountno_et);
        phone2 =findViewById(R.id.acholder_et);
        ifscCode =findViewById(R.id.ifsccode_et);
        bankName_autoComplete =findViewById(R.id.bankname_autocomplte_tv);
        location_autoComplete=findViewById(R.id.location_autoComplete_tv);
        religion_autoComplete=findViewById(R.id.religion_autoComplete_tv);
        aadhaarNumber =findViewById(R.id.aadharcard_et);
        activephone1 =findViewById(R.id.phonenumber_et);
        accountHolderName =findViewById(R.id.fathername_et);
        radioGroup=findViewById(R.id.skill_radiogp);
        laberRadio =findViewById(R.id.laber);//required when updating
        womenRadio=findViewById(R.id.women_laber);//required when updating
        mestreRadio=findViewById(R.id.mestre);//required when updating
       // laberRadio.setChecked(true);//by default laber will be checked other wise person wont be able to find.But this default will not work while updating because manually setting checked radio
        //skill=getResources().getString(R.string.laber);//skill default value otherwise null will be set as default so its important
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
        ArrayAdapter<String> bankAdapter=new ArrayAdapter<>(InsertPersonDetailsActivity.this, android.R.layout.simple_list_item_1, indianBank);
        bankName_autoComplete.setAdapter(bankAdapter);

        religionHashSet=new HashSet<>(Arrays.asList(MyUtility.getReligionFromDb(getBaseContext()))); //hashset is taken to insert only unique data in table
        ArrayAdapter<String> religionAdapter=new ArrayAdapter<>(InsertPersonDetailsActivity.this, android.R.layout.simple_list_item_1, religionHashSet.toArray(new String[religionHashSet.size()]));
        religion_autoComplete.setAdapter(religionAdapter);

        locationHashSet=new HashSet<>(Arrays.asList(MyUtility.getLocationFromDb(getBaseContext())));//hashset is taken to insert only unique data in table
        ArrayAdapter<String> locationAdapter=new ArrayAdapter<>(InsertPersonDetailsActivity.this, android.R.layout.simple_list_item_1, locationHashSet.toArray(new String[locationHashSet.size()]));
        location_autoComplete.setAdapter(locationAdapter);

        //For camera and galary*********************************************************************************
        imageView = findViewById(R.id.imageview);

        // allowing permissions of gallery and camera
        cameraPermission = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        // After clicking on text we will have
        // to choose whether to
        // select image from camera and gallery
        imageView.setOnClickListener(view -> showImagePicDialog());
        if(getIntent().hasExtra("ID")){//while updating. after getting all the ids setting all data according to id
            fromIntentPersonId=getIntent().getStringExtra("ID");//getting id from intent

            Cursor cursor= db.getData("SELECT "+Database.COL_2_NAME+" , "+Database.COL_3_BANKAC+" , "+Database.COL_4_IFSCCODE+"" + " , "+Database.COL_5_BANKNAME+" , "+Database.COL_6_AADHAAR_NUMBER+" , "+Database.COL_7_ACTIVE_PHONE1+"," + ""+Database.COL_8_MAINSKILL1 +","+Database.COL_9_ACCOUNT_HOLDER_NAME+","+Database.COL_10_IMAGE+","+Database.COL_11_ACTIVE_PHONE2+","+Database.COL_17_LOCATION+","+Database.COL_18_RELIGION+" FROM "+Database.TABLE_NAME1 +" WHERE "+Database.COL_1_ID+"='"+fromIntentPersonId+"'");
            if(cursor != null && cursor.moveToFirst()){
                name.setText(cursor.getString(0));
                account.setText(cursor.getString(1));
                ifscCode.setText(cursor.getString(2));
                bankName_autoComplete.setText(cursor.getString(3));
                aadhaarNumber.setText(cursor.getString(4));
                activephone1.setText(cursor.getString(5));
                skill=cursor.getString(6);//skill is variable
                accountHolderName.setText(cursor.getString(7));
                 if(skill.equals(getResources().getString(R.string.laber))) //radio button should be checked according to data
                    laberRadio.setChecked(true);
                 else if(skill.equals(getResources().getString(R.string.mestre)))
                     mestreRadio.setChecked(true);
                 else//skill.equals("G")
                     womenRadio.setChecked(true);
                byte[] image=cursor.getBlob(8);//getting image from db as blob and storing in byte type Blop is large type

                if(image!=null){
                    //getting bytearray image from DB and converting  to bitmap to set in imageview
                    Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                    imageView.setImageBitmap(bitmap);
                }//else default image will be shown
                phone2.setText(cursor.getString(9));
                location_autoComplete.setText(cursor.getString(10));
                religion_autoComplete.setText(cursor.getString(11));
                add.setText(getResources().getString(R.string.update));//change text to update
                cursor.close();
            }else
                Toast.makeText(this, "cursor is null", Toast.LENGTH_SHORT).show();
        }
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String userInput= name.getText().toString().trim();
                name.setTextColor(Color.BLACK);
                correctInputArr[0]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data

                if(db.isNameMatching(userInput)){
                    name.setError(getString(R.string.please_add_father_name_to_person_name));
                   // Toast.makeText(InsertPersonDetailsActivity.this, getString(R.string.please_add_father_name_to_person_name), Toast.LENGTH_LONG).show();
                }

                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(correctInputArr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    add.setText(getString(R.string.save));
                    add.setBackgroundResource(R.drawable.green_color_bg);
                    add.setEnabled(true);
                }
                if(!(userInput.matches("[a-zA-Z ]+") || TextUtils.isEmpty(userInput))){//alphabetic characters, and spaces and empty
                    name.setTextColor(Color.RED);
                    add.setText(getString(R.string.wrong_input));
                    add.setBackgroundResource(R.drawable.red_color_background);
                    add.setEnabled(false);
                    correctInputArr[0]=2;//means wrong data
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
        activephone1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String userInput= activephone1.getText().toString().trim();
                activephone1.setTextColor(Color.BLACK);
                correctInputArr[1]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data

                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(correctInputArr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    add.setText(getString(R.string.save));
                    add.setBackgroundResource(R.drawable.green_color_bg);
                    add.setEnabled(true);
                }
                boolean isMatching=db.isActivePhoneNumberMatching(userInput);
                if(isMatching){
                    activephone1.setError(getString(R.string.phone_number_already_exists));
                }

                if(isMatching || !(userInput.matches("[0-9]+") || userInput.isEmpty())){//only digits
                    if(!isMatching) {
                        activephone1.setTextColor(Color.RED);
                        add.setText(getString(R.string.wrong_input));
                        add.setBackgroundResource(R.drawable.red_color_background);
                    }
                    add.setEnabled(false);
                    correctInputArr[1]=2;//means wrong data
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
        phone2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String userInput= phone2.getText().toString().trim();
                phone2.setTextColor(Color.BLACK);
                correctInputArr[2]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data

                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(correctInputArr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    add.setText(getString(R.string.save));
                    add.setBackgroundResource(R.drawable.green_color_bg);
                    add.setEnabled(true);
                }

                if(!(userInput.matches("[0-9]+") || userInput.isEmpty())){//only digits
                    phone2.setTextColor(Color.RED);
                    add.setText(getString(R.string.wrong_input));
                    add.setBackgroundResource(R.drawable.red_color_background);
                    add.setEnabled(false);
                    correctInputArr[2]=2;//means wrong data
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
        location_autoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String userInput= location_autoComplete.getText().toString().trim();
                location_autoComplete.setTextColor(Color.BLACK);
                correctInputArr[3]=1;//means data is inserted correct.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data

                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(correctInputArr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    add.setText(getString(R.string.save));
                    add.setBackgroundResource(R.drawable.green_color_bg);
                    add.setEnabled(true);
                }

                if(!(userInput.matches("[a-zA-Z ]+") || userInput.isEmpty())){//alphabetic characters, and spaces
                    location_autoComplete.setTextColor(Color.RED);
                    add.setText(getString(R.string.wrong_input));
                    add.setBackgroundResource(R.drawable.red_color_background);
                    add.setEnabled(false);
                    correctInputArr[3]=2;//means wrong data
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
        religion_autoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String userInput= religion_autoComplete.getText().toString().trim();
                religion_autoComplete.setTextColor(Color.BLACK);
                correctInputArr[4]=1;//means data is inserted correct.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data

                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(correctInputArr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    add.setText(getString(R.string.save));
                    add.setBackgroundResource(R.drawable.green_color_bg);
                    add.setEnabled(true);
                }

                if(!(userInput.matches("[a-zA-Z ]+") || userInput.isEmpty())){//alphabetic characters, and spaces
                    religion_autoComplete.setTextColor(Color.RED);
                    add.setText(getString(R.string.wrong_input));
                    add.setBackgroundResource(R.drawable.red_color_background);
                    add.setEnabled(false);
                    correctInputArr[4]=2;//means wrong data
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
        aadhaarNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String string= aadhaarNumber.getText().toString().trim();
                aadhaarNumber.setTextColor(Color.BLACK);
                correctInputArr[5]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data

                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(correctInputArr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    add.setText(getString(R.string.save));
                    add.setBackgroundResource(R.drawable.green_color_bg);
                    add.setEnabled(true);
                }

                if(!(string.matches("[0-9]+")|| string.isEmpty())){//only digits
                    aadhaarNumber.setTextColor(Color.RED);
                    add.setText(getString(R.string.wrong_input));
                    add.setBackgroundResource(R.drawable.red_color_background);
                    add.setEnabled(false);
                    correctInputArr[5]=2;//means wrong data
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
        bankName_autoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String userInput= bankName_autoComplete.getText().toString().trim();
                bankName_autoComplete.setTextColor(Color.BLACK);
                correctInputArr[6]=1;//means data is inserted correct.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data

                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(correctInputArr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    add.setText(getString(R.string.save));
                    add.setBackgroundResource(R.drawable.green_color_bg);
                    add.setEnabled(true);
                }

                if(!(userInput.matches("[a-zA-Z ]+") || userInput.isEmpty())){//alphabetic characters, and spaces
                    bankName_autoComplete.setTextColor(Color.RED);
                    add.setText(getString(R.string.wrong_input));
                    add.setBackgroundResource(R.drawable.red_color_background);
                    add.setEnabled(false);
                    correctInputArr[6]=2;//means wrong data
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
        account.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String string= account.getText().toString().trim();
                account.setTextColor(Color.BLACK);
                correctInputArr[7]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data

                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(correctInputArr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    add.setText(getString(R.string.save));
                    add.setBackgroundResource(R.drawable.green_color_bg);
                    add.setEnabled(true);
                }

                if(!(string.matches("[0-9]+")|| string.isEmpty())){//only digits
                    account.setTextColor(Color.RED);
                    add.setText(getString(R.string.wrong_input));
                    add.setBackgroundResource(R.drawable.red_color_background);
                    add.setEnabled(false);
                    correctInputArr[7]=2;//means wrong data
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
        ifscCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String userInput= ifscCode.getText().toString().trim();
                ifscCode.setTextColor(Color.BLACK);
                correctInputArr[8]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data

                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(correctInputArr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    add.setText(getString(R.string.save));
                    add.setBackgroundResource(R.drawable.green_color_bg);
                    add.setEnabled(true);
                }

                if(!(userInput.matches("[a-zA-Z0-9]+") || userInput.isEmpty())){//alphabetic characters, and spaces
                    ifscCode.setTextColor(Color.RED);
                    add.setText(getString(R.string.wrong_input));
                    add.setBackgroundResource(R.drawable.red_color_background);
                    add.setEnabled(false);
                    correctInputArr[8]=2;//means wrong data
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
        accountHolderName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String userInput= accountHolderName.getText().toString().trim();
                accountHolderName.setTextColor(Color.BLACK);
                correctInputArr[9]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data

                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(correctInputArr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    add.setText(getString(R.string.save));
                    add.setBackgroundResource(R.drawable.green_color_bg);
                    add.setEnabled(true);
                }

                if(!(userInput.matches("[a-zA-Z ]+") || userInput.isEmpty())){//alphabetic characters, and spaces
                    accountHolderName.setTextColor(Color.RED);
                    add.setText(getString(R.string.wrong_input));
                    add.setBackgroundResource(R.drawable.red_color_background);
                    add.setEnabled(false);
                    correctInputArr[9]=2;//means wrong data
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
    }
    public void insert_click(View view) { //action while clicking insert button

        if(!checkCredentials(activephone1,phone2,aadhaarNumber,skill,name,view,ifscCode)) return;

        add.setEnabled(false);//so that user do not enter again add button while data is  inserting in database because if user do again then it will overload
        String personName = TextUtils.isEmpty(name.getText().toString().trim()) ? null : name.getText().toString().toUpperCase().trim();
        String personAccount = TextUtils.isEmpty(account.getText().toString().trim()) ? null : account.getText().toString().trim();
        String personPhoneNumber2 = TextUtils.isEmpty(phone2.getText().toString().trim()) ? null : phone2.getText().toString().trim();
        String personIfscCode = TextUtils.isEmpty(ifscCode.getText().toString().trim()) ? null : ifscCode.getText().toString().toUpperCase().trim();
        String personAadhaar = TextUtils.isEmpty(aadhaarNumber.getText().toString().trim()) ? null : aadhaarNumber.getText().toString().trim();
        String personActivePhoneNo2 = TextUtils.isEmpty(activephone1.getText().toString().trim()) ? null : activephone1.getText().toString().trim();
        String personAccountHolderName = TextUtils.isEmpty(accountHolderName.getText().toString().trim()) ? null : accountHolderName.getText().toString().toUpperCase().trim();
        String personSkill=TextUtils.isEmpty(skill)?null:skill;//checked before
        String personBankName = TextUtils.isEmpty(bankName_autoComplete.getText().toString().trim()) ? null : bankName_autoComplete.getText().toString().toUpperCase().trim();
        String location = TextUtils.isEmpty(location_autoComplete.getText().toString().trim()) ? null : location_autoComplete.getText().toString().toUpperCase().trim();
        String religion = TextUtils.isEmpty(religion_autoComplete.getText().toString().trim()) ? null : religion_autoComplete.getText().toString().toUpperCase().trim();
        byte[] imageStore=!isImageViewNull(imageView)?convertBitmapToByteArray(imageView):null;//convertImageViewToByteArray(reduceSize); reduceSize contain image so sending to convert to byte array to store in database

            AlertDialog.Builder detailsReview = new AlertDialog.Builder(this);
            detailsReview.setCancelable(false);
            detailsReview.setTitle("REVIEW DETAILS");// Html tags video- https://www.youtube.com/watch?v=98BD6IjQQkE
            detailsReview.setMessage(HtmlCompat.fromHtml("Name-" +"<b>"+ (personName!=null?personName:"")+"</b>"+"<br>"+"<br>"+
                    "Active Phone No.1---  " +"<b>"+ (personActivePhoneNo2!=null?personActivePhoneNo2:"") +"</b>" +"<br>"+"<br>"+
                    "Phone No.2------------- " +"<b>"+(personPhoneNumber2!=null?personPhoneNumber2:"")+"</b>" +"<br>"+"<br>"+
                    "Location-- " +"<b>"+(location!=null?location:"")+"</b>" +"<br>"+"<br>"+
                    "Religion-- " +"<b>"+(religion!=null?religion:"")+"</b>" +"<br>"+"<br>"+
                    "Aadhaar Card No.- " +"<b>"+ (personAadhaar!=null?personAadhaar:"")+"</b>"  +"<br>"+"<br>"+
                    "Bank Name-" +"<b>"+ (personBankName!=null?personBankName:"")+"</b>"+"<br>" +"<br>"+
                    "Account No.--" +"<b>"+ (personAccount!=null?personAccount:"") +"</b>" +"<br>"+"<br>"+
                    "IFSC Code---  " +"<b>"+ (personIfscCode!=null?personIfscCode:"")+"</b>"  +"<br>"+"<br>"+
                    "A/C Holder Name-" +"<b>"+ (personAccountHolderName!=null?personAccountHolderName:"")+"</b>" +"<br>"+"<br>"+
                    "Person Skill- " +"<b>"+ (personSkill!=null?personSkill:"")+"</b>"  +"<br>",HtmlCompat.FROM_HTML_MODE_LEGACY));

            detailsReview.setNegativeButton(getResources().getString(R.string.cancel), (dialogInterface, i) -> {
                dialogInterface.dismiss();
                add.setEnabled(true);
            });
            detailsReview.setPositiveButton(getResources().getString(R.string.ok),new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i){
                    dialogInterface.dismiss();

                    if(getIntent().hasExtra("ID")){//will execute only when updating
                        boolean success = false;
                        if (!MyUtility.updateLocationReligionToTableIf(locationHashSet, location, religionHashSet, religion, getBaseContext())) {//UPDATING location and religion table
                            Toast.makeText(InsertPersonDetailsActivity.this, "DATA NOT UPDATED", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (db.updatePersonSkillAndShiftData(personSkill, fromIntentPersonId)) {//if skill get updated then only all data will be updated its important
                            success = db.updateAllPersonDetails(personName, personAccount, personIfscCode, personBankName, personAadhaar, personActivePhoneNo2, personSkill, personAccountHolderName, imageStore, personPhoneNumber2, fromIntentPersonId, location, religion);
                        }
                        if(success){//if it is updated then show successfully message
                             Toast.makeText(InsertPersonDetailsActivity.this, "ID: " + fromIntentPersonId + " " + getResources().getString(R.string.updated_successfully), Toast.LENGTH_SHORT).show();

                            //whenever user update its name,bank account,etc theN IF that account is inactive then that account will become active that is its latest date is updated to current date
//                            final Calendar current=Calendar.getInstance();//to get current date
//                            String currentDate =current.get(Calendar.DAY_OF_MONTH)+"-"+(current.get(Calendar.MONTH)+1)+"-"+current.get(Calendar.YEAR);
//                            personDb.updateTable("UPDATE " + personDb.TABLE_NAME1 + " SET  LATESTDATE='" + currentDate + "'" +" WHERE ID='" + fromIntentPersonId + "'");

                            //after success then go to previous activity automatically and destroy current activity so that when pressing back user should not get same activity this is done by finish();
                            Intent intent = new Intent(getBaseContext(), IndividualPersonDetailActivity.class);//completed then go back
                            intent.putExtra("ID", fromIntentPersonId);//after going back to this IndividualPersonDetailActivity then it require ID so putExtra is used
                            startActivity(intent);
                            finish();//destroy current activity
                        }else
                            Toast.makeText(InsertPersonDetailsActivity.this, "DATA NOT UPDATED", Toast.LENGTH_LONG).show();
                    }else{
                      //this will execute only when adding new person
                      //  for (int k = 1; k <= 50; k++) {
                        String newelyCreatedId=null;
                        if (!MyUtility.updateLocationReligionToTableIf(locationHashSet, location, religionHashSet, religion, getBaseContext())) {//UPDATING location and religion table
                            Toast.makeText(InsertPersonDetailsActivity.this, "NOT INSERTED", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if((newelyCreatedId= db.insertDataToDetailsAndRateTable(personName, personAccount, personIfscCode, personBankName, personAadhaar, personActivePhoneNo2, personSkill, personAccountHolderName, imageStore, personPhoneNumber2, location, religion))==null){//if null means error
                            displayResult("FAILED", "TO ADD");
                            return;
                        }
                       if(newelyCreatedId !=null){//never be null because above code would return
                            checkForDuplicate();//checking for duplicate
                         }
                        skill=eraseAllDataAfterInsertingFromLayout();//should be here because control does not wait for its execution.skill = null;//this will update skill variable but inside eraseAllDataAfterInsertingFromLayout() method skill variable is not modified.In Java, anonymous inner classes and lambda expressions capture variables from their surrounding scope. If a variable is modified inside such a class or expression, it doesn't affect the original variable outside.To ensure changes to a variable inside an inner class or lambda expression reflect outside:Pass the variable as a parameter to the method.Return the modified variable from the method and assign it back outside.
                        add.setEnabled(true);//after all execution done
                    }
                }
                private void checkForDuplicate() {
                    Database db=Database.getInstance(getBaseContext());
                    Cursor result = db.getId(personName, personAccount, personIfscCode, personBankName, personAadhaar, personActivePhoneNo2, personSkill, personAccountHolderName, personPhoneNumber2, location, religion);
                    StringBuilder buffer;//because it is not synchronized and efficient then string buffer and no need to lock and unlock
                    String holdLastId = "";//ITS NEW PERSON ID

                    if (result !=null && result.getCount() == 1 || result.getCount() > 1){//no duplicate
                        buffer = new StringBuilder();

                        if (result.getCount() == 1 && result.moveToFirst()) {//ONLY 1 DATE NO DUPLICATE

                            buffer.append("\n" + "NEW PERSON ID: ").append(result.getString(0));
                            displayResult("CREATED SUCCESSFULLY", buffer.toString());
                        }

                        if (result.getCount() > 1) {//this will be true when details are DUPLICATE
                            buffer.append("MATCHING ").append(result.getCount()).append(" PERSON WITH SAME DETAILS:").append("\n");
                            while (result.moveToNext()) {
                                holdLastId = "" + result.getString(0);//to display new added person ids comes at last when loop
                                buffer.append("\nPERSON ID: ").append(result.getString(0));
                            }
                            displayResult("SUCCESSFULLY ADDED NEW PERSON ID: " + holdLastId, buffer.toString());
                        }
                        result.close();//closing cursor
                    }else
                        displayResult("SUCCESSFULLY ADDED NEW PERSON", personName+" BUT QUERY NOT RETURNED ANY DATA OF THAT PERSON");
                }
                private void displayResult(String title, String message) {
                    AlertDialog.Builder showDataFromDataBase = new AlertDialog.Builder(InsertPersonDetailsActivity.this);
                    showDataFromDataBase.setCancelable(false);
                    showDataFromDataBase.setTitle(title);
                    showDataFromDataBase.setMessage(message);
                    showDataFromDataBase.setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> dialogInterface.dismiss());
                    showDataFromDataBase.create().show();
                }
                private String eraseAllDataAfterInsertingFromLayout() {
                    try{
                        location_autoComplete.setText("");
                        religion_autoComplete.setText("");
                        name.setText("");
                        account.setText("");
                        phone2.setText("");
                        ifscCode.setText("");
                        bankName_autoComplete.setText("");
                        aadhaarNumber.setText("");
                        activephone1.setText("");
                        accountHolderName.setText("");
                        imageView.setImageDrawable(null);//to reset imageview
                        radioGroup.clearCheck();//clear all check
                        return null;//for skill return null//In Java, anonymous inner classes and lambda expressions capture variables from their surrounding scope. If a variable is modified inside such a class or expression, it doesn't affect the original variable outside.To ensure changes to a variable inside an inner class or lambda expression reflect outside:Pass the variable as a parameter to the method.Return the modified variable from the method and assign it back outside.
                    }catch (Exception x){
                        x.printStackTrace();
                        return null;
                    }
                }
            });
            detailsReview.create().show();
    }

    private boolean checkCredentials(EditText activePhone1, EditText phone2, EditText aadhaarNumber,String skill, EditText personName,View view,EditText ifscCode) {
        boolean isValid = true;

        if(TextUtils.isEmpty(skill)){//mandatory field
            MyUtility.snackBar(view,getString(R.string.please_select_skill));
            isValid = false;
        }
        if(TextUtils.isEmpty(personName.getText().toString().trim()) || !personName.getText().toString().trim().matches("[a-zA-Z ]+")){//mandatory field
            name.setError(getString(R.string.please_enter_name));
            isValid = false;
        }
        if (!TextUtils.isEmpty(activePhone1.getText().toString().trim()) && activePhone1.getText().toString().trim().length() != 10) {
            activePhone1.setError(getString(R.string.should_be_10_digits));
            isValid = false;
        }
        if (!TextUtils.isEmpty(phone2.getText().toString().trim()) && phone2.getText().toString().trim().length() != 10) {
            phone2.setError(getString(R.string.should_be_10_digits));
            isValid = false;
        }
        if (!TextUtils.isEmpty(aadhaarNumber.getText().toString().trim()) && aadhaarNumber.getText().toString().trim().length() != 12) {
            aadhaarNumber.setError(getString(R.string.should_be_12_digits));
            isValid = false;
        }
        if(!TextUtils.isEmpty(ifscCode.getText().toString().trim()) && ifscCode.getText().toString().trim().length() != 11){
            ifscCode.setError(getString(R.string.should_be_11_digits));
            isValid = false;
        }
        return isValid;
    }
//    private boolean checkSkillAndNameIsEmpty(View view, String skill, String name) {
//        if(TextUtils.isEmpty(skill)){
//            MyUtility.snackBar(view,getString(R.string.please_select_skill));
//            return false;
//        }
//        if(TextUtils.isEmpty(name) || name.matches("[a-zA-Z ]+")){
//            this.name.setError(getString(R.string.please_enter_name));//MyUtility.snackBar(view,getString(R.string.please_enter_name));
//           return  false;
//        }
//        return true;
//    }
    private boolean isImageViewNull(ImageView imageView) {//if getDrawable() returns non-null, it means an image has been set by user, and if it returns null, no image has been set.
        try {
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();//If imageView.getDrawable() returns null, it means that there is no image or drawable set to the ImageView. In such a case, attempting to cast null to BitmapDrawable would result in a NullPointerException.
            return drawable==null?true:false;
        } catch (Exception e) {
            e.printStackTrace();
           return false;
        }
    }

//    private void setDefaultRateToId(String id) {
//        if(!db.insertDataTable3( id,null,null,null,null,null,null,null,null))//default values
//            Toast.makeText(this, "Not Inserted to table 3", Toast.LENGTH_LONG).show();
//    }
    private byte[] convertBitmapToByteArray(ImageView imageView) {//convertImageViewToByteArray(reduceSize); reduceSize contain image so sending to convert to byte array to store in database
        if(imageView==null) return null;
        byte[] image=null;
        ByteArrayOutputStream byteArrayOutputStream=null;
        try {
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            Bitmap fullSizeBitmapImage = drawable.getBitmap();

            // Resize the bitmap efficiently
            Bitmap reducedSizeBitmap = ImageResizer.reduceBitmapSize(fullSizeBitmapImage, 46000);

            // Compress the resized bitmap into a byte array
            byteArrayOutputStream = new ByteArrayOutputStream();
            reducedSizeBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);

            // Return the byte array
            image = byteArrayOutputStream.toByteArray();

            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }catch (Exception x){
            x.printStackTrace();
            Toast.makeText(this, "image processing error", Toast.LENGTH_SHORT).show();
            return null;
        }
        return image;
    }
    public void go_back(View view){//when user press back arrow
         //from activity to activity
//        if(getIntent().hasExtra("ID")){//execute when it is called from other activity with ID intent
//            Intent intent=new Intent(getBaseContext(),IndividualPersonDetailActivity.class); //go back
//            intent.putExtra("ID",fromIntentPersonId);//after going back to this IndividualPersonDetailActivity then it require ID so putExtra is used
//            startActivity(intent);
//            finish();//destroy current activity
//        }else{//go from activity to fragment
//            finish();//first destroy current activity then go back
//            //fragment not getting refresh
////            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
////            transaction.replace(R.id.insert_detailsof_l_m_g, new MLFragment()).commit();
//
//            super.onBackPressed();
//        }
        refreshIndividualPersonDetailActivity(fromIntentPersonId);
    }
    @Override
    public void onBackPressed(){//when user press back button
        refreshIndividualPersonDetailActivity(fromIntentPersonId);
    }
    public void refreshIndividualPersonDetailActivity(String id){
        //from activity to activity
        if(getIntent().hasExtra("ID")){//execute when it is called from other activity with ID intent
            Intent intent=new Intent(getBaseContext(),IndividualPersonDetailActivity.class); //go back
            intent.putExtra("ID",id);//after going back to this IndividualPersonDetailActivity then it require ID so putExtra is used
            startActivity(intent);
            finish();//destroy current activity
        }else {
            finish();//destroy current activity
            super.onBackPressed();
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
        CropImage.activity().start(InsertPersonDetailsActivity.this);
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Database.closeDatabase();
    }
}