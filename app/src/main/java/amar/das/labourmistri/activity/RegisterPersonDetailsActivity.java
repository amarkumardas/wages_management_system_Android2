package amar.das.labourmistri.activity;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.text.HtmlCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
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
import android.widget.Toast;
//import com.theartofdev.edmodo.cropper.CropImage;
import com.github.drjacky.imagepicker.ImagePicker;
import com.github.drjacky.imagepicker.constant.ImageProvider;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;

import amar.das.labourmistri.ImageResizer;
import amar.das.labourmistri.Database;
import amar.das.labourmistri.R;
import amar.das.labourmistri.globalenum.GlobalConstants;
import amar.das.labourmistri.utility.MyUtility;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

public class RegisterPersonDetailsActivity extends AppCompatActivity {
  //if you really want to write files, either make sure you're only writing to your app's designated storage directories, in which case you won't need any permissions at all, or if you really need to write to a directory your app doesn't own get that file manager permission from Google (how to get that permission)
  int [] correctInputArr =new int[10];
  Button add, deleteImageBtn;
  EditText name,account,phone2,ifscCode,aadhaarNumber,activePhone1,accountHolderName;
  AutoCompleteTextView bankName_autoComplete, location_autoComplete, religion_autoComplete;
  Database db;
  RadioGroup radioGroup;
  RadioButton  laberRadio,womenRadio,mestreRadio;
  String skill,fromIntentPersonId;
  String[] indianBank;//to store array
  HashSet<String> religionHashSet,locationHashSet;
  ImageView imageView;
  boolean isNewImageSet;//this variable will indicate image picker has picked new image or not
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //overridePendingTransition(0, 0); //we have used overridePendingTransition(), it is used to remove activity create animation while re-creating activity.
        setContentView(R.layout.activity_name_registration);

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
        activePhone1 =findViewById(R.id.phonenumber_et);
        accountHolderName =findViewById(R.id.fathername_et);
        radioGroup=findViewById(R.id.skill_radiogp);
        laberRadio =findViewById(R.id.laber_skill);//required when updating
        womenRadio=findViewById(R.id.women_laber_skill);//required when updating
        mestreRadio=findViewById(R.id.mestre_skill);//required when updating
        deleteImageBtn =findViewById(R.id.delete_image);
        //laberRadio.setChecked(true);//by default laber will be checked other wise person wont be able to find.But this default will not work while updating because manually setting checked radio
        //skill=getResources().getString(R.string.laber);//skill default value otherwise null will be set as default so its important

        radioGroup.setOnCheckedChangeListener((radioGroup, checkedIdOfRadioBtn) -> {
            if(checkedIdOfRadioBtn == R.id.mestre_skill){
                skill= GlobalConstants.M_SKILL.getValue();//updating skill variable
            } else if (checkedIdOfRadioBtn == R.id.laber_skill) {
                skill=GlobalConstants.L_SKILL.getValue();
            }else if (checkedIdOfRadioBtn == R.id.women_laber_skill) {
                skill=GlobalConstants.G_SKILL.getValue();
            }
        });

        indianBank =getResources().getStringArray(R.array.indian_bank_names); //get bank names
        ArrayAdapter<String> bankAdapter=new ArrayAdapter<>(RegisterPersonDetailsActivity.this, android.R.layout.simple_list_item_1, indianBank);
        bankName_autoComplete.setAdapter(bankAdapter);

        religionHashSet=new HashSet<>(Arrays.asList(MyUtility.getReligionFromDb(getBaseContext()))); //hashset is taken to insert only unique data in table
        ArrayAdapter<String> religionAdapter=new ArrayAdapter<>(RegisterPersonDetailsActivity.this, android.R.layout.simple_list_item_1, religionHashSet.toArray(new String[religionHashSet.size()]));
        religion_autoComplete.setAdapter(religionAdapter);

        locationHashSet=new HashSet<>(Arrays.asList(MyUtility.getLocationFromDb(getBaseContext())));//hashset is taken to insert only unique data in table
        ArrayAdapter<String> locationAdapter=new ArrayAdapter<>(RegisterPersonDetailsActivity.this, android.R.layout.simple_list_item_1, locationHashSet.toArray(new String[locationHashSet.size()]));
        location_autoComplete.setAdapter(locationAdapter);

        imageView = findViewById(R.id.imageview);

        ActivityResultLauncher<Intent> launcher= registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),(ActivityResult result)->{
                if(result.getResultCode()==RESULT_OK){
                        Uri uri=result.getData().getData();
                            imageView.setImageURI(uri);//set image to imageview
                            isNewImageSet=true;
//                        File file= ImagePicker.Companion.getFile(getIntent());
//                        if(file!=null && file.exists()) file.delete();//delete the crop image ie. is saved in device. but selected image is not deleted

//                        String multipleFilesPath = ImagePicker.MULTIPLE_FILES_PATH;
//                        System.out.println(multipleFilesPath+"--------------");
               //   MyUtility.deleteFolderAllFiles(ImagePicker.Companion.)
                  // ArrayList<Uri> uris=ImagePicker.Companion.getAllFilePath(result.getData());
                      // System.out.println(ImagePicker.Companion.getFilePath(result.getData()));

//                        for (Uri s:uris) {
//                            System.out.println(s!=null?s.toString():"");
//                        }

                        // Use the uri to load the image
                    }else if(result.getResultCode()== ImagePicker.RESULT_ERROR){
                        // Use ImagePicker.Companion.getError(result.getData()) to show an error
                        ImagePicker.Companion.getError(result.getData());
                        Toast.makeText(this, "IMAGE ERROR", Toast.LENGTH_LONG).show();
                    }
                });

        imageView.setOnClickListener(view -> {
            if(!checkCameraPermission()){
                requestCameraPermission();
                return;
            }
            if(!checkImageStoragePermission()){
                requestImageStoragePermission();
                return;
            }
            try {
                //noinspection unchecked
                ImagePicker.Companion.with(this)// Start building the configuration
                        .provider(ImageProvider.BOTH) //Or bothCameraGallery()
                        .setMultipleAllowed(false) //by default it is false
                        .crop(10f, 3f)
                        //.cropSquare()
                        .cropFreeStyle()//Let the user to resize crop bounds:
                        .maxResultSize(350, 350, true) //true: Keep Ratio.Set Max Width and Height of final image. When the user picks an image from the gallery or captures a new image using the camera, the library ensures that the resulting image does not exceed the specified resolution. This is useful for scenarios where you want to limit the image size for storage, display, or other purposes.
                        .setOutputFormat(Bitmap.CompressFormat.JPEG)//compress
                        .createIntentFromDialog(new Function1(){
                            public Object invoke(Object var1) {
                                this.invoke((Intent) var1);
                                return Unit.INSTANCE;
                            }
                            public void invoke(@NotNull Intent it) {
                                Intrinsics.checkNotNullParameter(it, "it");
                                launcher.launch(it);
                            }
                        });

            }catch(Exception x){
                x.printStackTrace();
                Toast.makeText(this, "Exception occurred Image picker", Toast.LENGTH_LONG).show();
                //provide alternative way to set image in imageview if first approach dont work
            }
        });
        deleteImageBtn.setOnClickListener(view -> {
            if(!isImageViewNull(imageView)){//checking imageview has image or not

                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setCancelable(true);//when dialog is visible and user click other part of screen then dialog will disappear when value is true
                dialog.setTitle(getString(R.string.delete_image));
                dialog.setMessage(getString(R.string.are_you_sure_questionmark));
                dialog.setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {

                    if(fromIntentPersonId != null){//this will execute when updating because while updating fromIntentPersonId would not be null
                        if(!deleteImageFromDatabaseAndDeviceAndSetImageViewToNull(imageView,fromIntentPersonId)){
                            MyUtility.snackBar(view,getString(R.string.failed_to_delete_image));
                        }
                    }else{
                        imageView.setImageDrawable(null);//Set the imageVIEW to null (removes any currently set image)
                    }

                    dialogInterface.dismiss();
                });
               dialog.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                   dialogInterface.dismiss();
               });

                dialog.create().show();

            }else{//if no image
                MyUtility.snackBar(view,getString(R.string.no_image_to_delete));
            }
        });
        if(getIntent().hasExtra("ID")){//while updating. after getting all the ids setting all data according to id
            fromIntentPersonId=getIntent().getStringExtra("ID");//getting id from intent

            Cursor cursor= db.getData("SELECT "+Database.COL_2_NAME+" , "+Database.COL_3_BANKAC+" , "+Database.COL_4_IFSCCODE+" , "+Database.COL_5_BANKNAME+" , "+Database.COL_6_AADHAAR_NUMBER+" , "+Database.COL_7_MAIN_ACTIVE_PHONE1 +","+Database.COL_8_MAINSKILL1 +","+Database.COL_9_ACCOUNT_HOLDER_NAME+","+Database.COL_10_IMAGE_PATH +","+Database.COL_11_ACTIVE_PHONE2+","+Database.COL_17_LOCATION+","+Database.COL_18_RELIGION+" FROM "+Database.PERSON_REGISTERED_TABLE +" WHERE "+Database.COL_1_ID+"='"+fromIntentPersonId+"'");
            if(cursor != null && cursor.moveToFirst()){
                name.setText(cursor.getString(0));
                account.setText(cursor.getString(1));
                ifscCode.setText(cursor.getString(2));
                bankName_autoComplete.setText(cursor.getString(3));
                aadhaarNumber.setText(cursor.getString(4));
                activePhone1.setText(cursor.getString(5));
                skill=cursor.getString(6);//skill is variable
                accountHolderName.setText(cursor.getString(7));
                 if(skill.equals(GlobalConstants.L_SKILL.getValue())) //radio button should be checked according to data
                    laberRadio.setChecked(true);
                 else if(skill.equals(GlobalConstants.M_SKILL.getValue()))
                     mestreRadio.setChecked(true);
                 else//skill.equals("G")
                     womenRadio.setChecked(true);
//                byte[] image=cursor.getBlob(8);//getting image from db as blob and storing in byte type Blop is large type
//
//                if(image!=null){
//                    //getting bytearray image from DB and converting  to bitmap to set in imageview
//                    Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
//                    imageView.setImageBitmap(bitmap);
//                }//else default image will be shown

                String imagePath=cursor.getString(8);//getting image from db

                if(imagePath!=null){
                    Bitmap bitmap = MyUtility.getBitmapFromPath(imagePath);//converting image path to bitmap
                    if(bitmap != null){
                    imageView.setImageBitmap(bitmap);
                    }//else default image will be shown

//                    else{//default image will be shown
//                        Toast.makeText(this, getString(R.string.no_image), Toast.LENGTH_LONG).show();
//                    }
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
        activePhone1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String userInput= activePhone1.getText().toString().trim();
                activePhone1.setTextColor(Color.BLACK);
                correctInputArr[1]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data

                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(correctInputArr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    add.setText(getString(R.string.save));
                    add.setBackgroundResource(R.drawable.green_color_bg);
                    add.setEnabled(true);
                }
                boolean isMatching=false;
                if((userInput.length()==10) && (isMatching=db.isActivePhoneNumberMatching(userInput))) activePhone1.setError(getString(R.string.phone_number_already_exists));

                if(isMatching || !(userInput.matches("[0-9]+") || userInput.isEmpty())){//only digits
                    if(!isMatching) {
                        activePhone1.setTextColor(Color.RED);
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
                String userInput=aadhaarNumber.getText().toString().trim();
                aadhaarNumber.setTextColor(Color.BLACK);
                correctInputArr[5]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data

                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(correctInputArr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    add.setText(getString(R.string.save));
                    add.setBackgroundResource(R.drawable.green_color_bg);
                    add.setEnabled(true);
                }
                boolean isMatching=false;
                 if((userInput.length()==12) && (isMatching=db.isAadhaarNumberMatching(userInput))){
                     aadhaarNumber.setError(getString(R.string.aadhaar_number_already_exists));
                 }

                if(isMatching || !(userInput.matches("[0-9]+") || userInput.isEmpty())){//only digits
                    if(!isMatching) {
                        aadhaarNumber.setTextColor(Color.RED);
                        add.setText(getString(R.string.wrong_input));
                        add.setBackgroundResource(R.drawable.red_color_background);
                    }
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
                String userInput= account.getText().toString().trim();
                account.setTextColor(Color.BLACK);
                correctInputArr[7]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data

                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(correctInputArr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    add.setText(getString(R.string.save));
                    add.setBackgroundResource(R.drawable.green_color_bg);
                    add.setEnabled(true);
                }
                boolean isMatching=false;
                if((userInput.length() > 9) && (isMatching=db.isAccountNumberMatching(userInput))){
                    account.setError(getString(R.string.account_number_already_exists));//after 9 digits only checking will start
                }

                if(isMatching || !(userInput.matches("[0-9]+") || userInput.isEmpty())){//only digits
                    if(!isMatching) {
                        account.setTextColor(Color.RED);
                        add.setText(getString(R.string.wrong_input));
                        add.setBackgroundResource(R.drawable.red_color_background);
                    }
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
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() { // Code to execute when back button is pressed
                refreshIndividualPersonDetailActivity(fromIntentPersonId);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);//add it to the OnBackPressedDispatcher using getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback).This ensures that your custom back button handling logic is invoked when the back button is pressed.
    }

    private boolean deleteImageFromDatabaseAndDeviceAndSetImageViewToNull(ImageView imageView, String id) {
        if(imageView==null || id==null) return false;
        Database db=Database.getInstance(getBaseContext());

        if(MyUtility.deletePdfOrRecordingUsingPathFromAppStorage(db.getImagePath(id))){//first delete that image from device
            if(db.setImageColumnToNull(id)){//than from database
                imageView.setImageDrawable(null);//Set the image to null (removes any currently set image)
                return true;
            }
        }

       return false;
    }

    public void insert_click(View view) { //action while clicking insert button

        if(!checkCredentials(activePhone1,phone2,aadhaarNumber,skill,name,view,ifscCode)) return;

        add.setEnabled(false);//so that user do not enter again add button while data is  inserting in database because if user do again then it will overload
        String personName = TextUtils.isEmpty(name.getText().toString().trim()) ? null : name.getText().toString().toUpperCase().trim();
        String personAccount = TextUtils.isEmpty(account.getText().toString().trim()) ? null : account.getText().toString().trim();
        String personPhoneNumber2 = TextUtils.isEmpty(phone2.getText().toString().trim()) ? null : phone2.getText().toString().trim();
        String personIfscCode = TextUtils.isEmpty(ifscCode.getText().toString().trim()) ? null : ifscCode.getText().toString().toUpperCase().trim();
        String personAadhaar = TextUtils.isEmpty(aadhaarNumber.getText().toString().trim()) ? null : aadhaarNumber.getText().toString().trim();
        String personActivePhoneNo1 = TextUtils.isEmpty(activePhone1.getText().toString().trim()) ? null : activePhone1.getText().toString().trim();
        String personAccountHolderName = TextUtils.isEmpty(accountHolderName.getText().toString().trim()) ? null : accountHolderName.getText().toString().toUpperCase().trim();
        String personSkill=TextUtils.isEmpty(skill)?null:skill;//checked before
        String personBankName = TextUtils.isEmpty(bankName_autoComplete.getText().toString().trim()) ? null : bankName_autoComplete.getText().toString().toUpperCase().trim();
        String location = TextUtils.isEmpty(location_autoComplete.getText().toString().trim()) ? null : location_autoComplete.getText().toString().toUpperCase().trim();
        String religion = TextUtils.isEmpty(religion_autoComplete.getText().toString().trim()) ? null : religion_autoComplete.getText().toString().toUpperCase().trim();

        //byte[] imageStore=!isImageViewNull(imageView)?convertBitmapToByteArray(imageView):null;//convertImageViewToByteArray(reduceSize); reduceSize contain image so sending to convert to byte array to store in database

        String imagePath=getCompressImagePath(fromIntentPersonId);//while inserting fromIntentPersonId would be null else non null

            AlertDialog.Builder detailsReview = new AlertDialog.Builder(this);
            detailsReview.setCancelable(false);
            detailsReview.setTitle("REVIEW DETAILS");// Html tags video- https://www.youtube.com/watch?v=98BD6IjQQkE
            detailsReview.setMessage(HtmlCompat.fromHtml("Name-" +"<b>"+ (personName!=null?personName:"")+"</b>"+"<br>"+"<br>"+
                    "Active Phone No.1---  " +"<b>"+ (personActivePhoneNo1!=null?personActivePhoneNo1:"") +"</b>" +"<br>"+"<br>"+
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
                        if (!MyUtility.updateLocationAndReligionToTableIfValueIsUnique(locationHashSet, location, religionHashSet, religion, getBaseContext())) {//UPDATING location and religion table
                            Toast.makeText(RegisterPersonDetailsActivity.this, "DATA NOT UPDATED", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (db.updatePersonSkillAndShiftData(personSkill, fromIntentPersonId)) {//if skill get updated then only all data will be updated its important
                            success = db.updateAllPersonDetails(personName, personAccount, personIfscCode, personBankName, personAadhaar, personActivePhoneNo1, personSkill, personAccountHolderName, imagePath, personPhoneNumber2, fromIntentPersonId, location, religion);
                        }
                        if(success){//if it is updated then show successfully message
                             Toast.makeText(RegisterPersonDetailsActivity.this, "ID: " + fromIntentPersonId + " " + getResources().getString(R.string.updated_successfully), Toast.LENGTH_SHORT).show();

                            //after success then go to previous activity automatically and destroy current activity so that when pressing back user should not get same activity this is done by finish();
                            Intent intent = new Intent(getBaseContext(), IndividualPersonDetailActivity.class);//completed then go back
                            intent.putExtra("ID", fromIntentPersonId);//after going back to this IndividualPersonDetailActivity then it require ID so putExtra is used
                            startActivity(intent);
                            finish();//destroy current activity
                        }else
                            Toast.makeText(RegisterPersonDetailsActivity.this, "DATA NOT UPDATED", Toast.LENGTH_LONG).show();
                    }else{//this will execute only when adding new person

                      //  for (int k = 1; k <= 50; k++) {
                        String newelyCreatedId=null;
                        if (!MyUtility.updateLocationAndReligionToTableIfValueIsUnique(locationHashSet, location, religionHashSet, religion, getBaseContext())) {//UPDATING location and religion table
                            Toast.makeText(RegisterPersonDetailsActivity.this, "NOT INSERTED", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if((newelyCreatedId= db.insertDataToPersonRegisteredAndRateSkillTable(personName, personAccount, personIfscCode, personBankName, personAadhaar, personActivePhoneNo1, personSkill, personAccountHolderName, imagePath, personPhoneNumber2, location, religion))==null){//if null means error
                            displayResult("FAILED", "TO REGISTER");
                            return;
                        }

                        if(!db.updatePersonRemarks(newelyCreatedId,getString(R.string.name_registered_colon)+LocalDate.now().getDayOfMonth()+"-"+LocalDate.now().getMonthValue()+"-"+LocalDate.now().getYear())) {//converted to 1-5-2022 remove 0.adding person remarks this line should be after aclling this method insertDataToPersonRegisteredAndRateSkillTable()
                            Toast.makeText(RegisterPersonDetailsActivity.this, "FAILED TO ADD REMARKS", Toast.LENGTH_LONG).show();
                        }

                       if(newelyCreatedId !=null){//never be null because above code would return
                            checkForDuplicate();//checking for duplicate
                         }
                        skill=eraseAllDataAfterInsertingFromLayout();//should be here because control does not wait for its execution.skill = null;//this will update skill variable but inside eraseAllDataAfterInsertingFromLayout() method skill variable is not modified.In Java, anonymous inner classes and lambda expressions capture variables from their surrounding scope. If a variable is modified inside such a class or expression, it doesn't affect the original variable outside.To ensure changes to a variable inside an inner class or lambda expression reflect outside:Pass the variable as a parameter to the method.Return the modified variable from the method and assign it back outside.
                        add.setEnabled(true);//after all execution done
                    }
                }
                private void checkForDuplicate() {//very rare to execute
                    Database db=Database.getInstance(getBaseContext());
                    Cursor result = db.getId(personName, personAccount, personIfscCode, personBankName, personAadhaar, personActivePhoneNo1, personSkill, personAccountHolderName, personPhoneNumber2, location, religion);
                    StringBuilder buffer;//because it is not synchronized and efficient then string buffer and no need to lock and unlock
                    String holdLastId = "";//ITS NEW PERSON ID

                    if (result !=null && result.getCount() == 1 || result.getCount() > 1){//no duplicate
                        buffer = new StringBuilder();

                        if (result.getCount() == 1 && result.moveToFirst()) {//ONLY 1 DATE NO DUPLICATE

                            buffer.append("\n" + "NEW PERSON ID: ").append(result.getString(0));
                            displayResult("REGISTERED SUCCESSFULLY", buffer.toString());
                        }

                        if (result.getCount() > 1) {//this will be true when details are DUPLICATE
                            buffer.append("MATCHING ").append(result.getCount()).append(" PERSON WITH SAME DETAILS:").append("\n");
                            while (result.moveToNext()) {
                                holdLastId = "" + result.getString(0);//to display new added person ids comes at last when loop
                                buffer.append("\nPERSON ID: ").append(result.getString(0));
                            }
                            displayResult("SUCCESSFULLY REGISTERED NEW PERSON ID: " + holdLastId, buffer.toString());
                        }
                        result.close();//closing cursor
                    }else
                        displayResult("SUCCESSFULLY REGISTERED NEW PERSON", personName+" BUT QUERY NOT RETURNED ANY DATA OF THAT PERSON");
                }
                private void displayResult(String title, String message) {
                    AlertDialog.Builder showDataFromDataBase = new AlertDialog.Builder(RegisterPersonDetailsActivity.this);
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
                        activePhone1.setText("");
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

    private String getCompressImagePath(String id) {
        String imagePath=null;
        if (id == null) {//if id is null means inserting so compress image and return file path to store in db
            imagePath= getCompressImageAbsolutePathAndIfFailedDeletePath(imageView);

        }else if(id != null){//if id is not null means user updating so if new image is set then delete previous image from device and compress new image
            Database db = Database.getInstance(getBaseContext());

            if(isNewImageSet) {//if new image is set then delete previous image from device
                MyUtility.deletePdfOrRecordingUsingPathFromAppStorage(db.getImagePath(id));//first delete that image from device
                imagePath= getCompressImageAbsolutePathAndIfFailedDeletePath(imageView);//compress new image to store in db
            }else{
                imagePath=db.getImagePath(id);//if no new image is set then get previous image path from db
            }
        }
        return imagePath;
    }

    private String getCompressImageAbsolutePathAndIfFailedDeletePath(ImageView imageView){//error return null
        File file=MyUtility.getImageUniqueFile(getExternalFilesDir(null).toString());//create file in device

        String imagePath=!isImageViewNull(imageView)? saveCompressImageInDeviceAndReturnAbsolutePath(imageView,file):null;
        if(imagePath==null){//delete the created file from device due to error occur or no image in imageview.imagePath would be null in two case 1.when imageView has no image and 2.this method saveImageInDeviceAndReturnAbsolutePath may give null when error occur
            MyUtility.deletePdfOrRecordingUsingPathFromAppStorage(file.getAbsolutePath());
        }
         return imagePath;
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
    private boolean isImageViewNull(ImageView imageView) {//if getDrawable() returns non-null, it means an image has been set by user, and if it returns null, no image has been set.
        try {
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();//If imageView.getDrawable() returns null, it means that there is no image or drawable set to the ImageView. In such a case, attempting to cast null to BitmapDrawable would result in a NullPointerException.
            return drawable==null;
        } catch (Exception e) {
            e.printStackTrace();
           return false;
        }
    }
    private String saveCompressImageInDeviceAndReturnAbsolutePath(ImageView imageView, File file) {
        if(imageView==null || file == null) return null;

        try(FileOutputStream out=new FileOutputStream(file)) {
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            Bitmap fullSizeBitmapImage = drawable.getBitmap();

            // Resize the bitmap efficiently
            Bitmap reducedSizeBitmap = ImageResizer.reduceBitmapSize(fullSizeBitmapImage, 46000);

            reducedSizeBitmap.compress(Bitmap.CompressFormat.JPEG, 50,out);//writing to device file
            return file.getAbsolutePath();

        }catch (Exception x){
            x.printStackTrace();
            Toast.makeText(this, "Failed To Save Image", Toast.LENGTH_LONG).show();
            return null;
        }
    }
    public void go_back_btn(View view){//when user press back arrow
        refreshIndividualPersonDetailActivity(fromIntentPersonId);
    }
    public void refreshIndividualPersonDetailActivity(String id){
        //from activity to activity
        if(getIntent().hasExtra("ID")){//execute when it is called from IndividualPersonDetailActivity with ID intent only
            Intent intent=new Intent(getBaseContext(),IndividualPersonDetailActivity.class); //go back
            intent.putExtra("ID",id);//after going back to this IndividualPersonDetailActivity then it require ID so putExtra is used
            startActivity(intent);
            finish();//destroy current activity
        }else {//if this activity is not called from IndividualPersonDetailActivity then execute this and go to navigation activity by refreshing so that user can see new register person name and photo.
            finish();//destroy current activity
            Intent intent = new Intent(this,NavigationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);//This line ensures that when you start the NavigationActivity using the intent, any existing activities on top of it will be cleared (if they belong to the same task), and the NavigationActivity will be brought to the foreground. If no task exists, a new task will be created for the NavigationActivity.
            startActivity(intent);
        }
    }
//    private void showImagePicDialog() {
//        String []options = {"CAMERA", "GALLERY"};
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("PICK IMAGE FROM");
//        builder.setCancelable(true);
//        builder.setNegativeButton(getResources().getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss());
//        builder.setItems(options, (dialog, which) -> {
//            if (which == 0) {
//                if (!checkCameraPermission()) {
//                    requestCameraPermission();
//                } else {
//                   // pickFromGallery();
//                }
//            } else if (which == 1) {
//                if (!checkImageStoragePermission()) {
//                    requestImageStoragePermission();
//                } else {
//                   // pickFromGallery();
//                }
//            }
//        });
//        builder.create().show();
//    }
    private boolean checkImageStoragePermission() {//we need permission to read other app files
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){//if it is android 13 or above
            return (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED);
        }

        // If it is Android 10,11,12 (R) or above required only READ_EXTERNAL_STORAGE permission only to read and write
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);//for below android 12 ie. sdk version 32
        //return ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);//for below android 12 ie. sdk version 32

        // If it is Android 10  required  READ_EXTERNAL_STORAGE or  WRITE_EXTERNAL_STORAGE permission when accessing other app files but since here we want to read only image so taking READ_EXTERNAL_STORAGE

        // return (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&  (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }
    private void requestImageStoragePermission() {
         Toast.makeText(this, "ENABLE MEDIA PERMISSION TO ACCESS IMAGE ", Toast.LENGTH_LONG).show();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {//if it is android 13 or above
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 311);
        }
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // If it is Android 11 (R) or above required only READ_EXTERNAL_STORAGE permission
//              ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},311);
//        }

        //If it is Android 10  required TO REQUEST READ_EXTERNAL_STORAGE or  WRITE_EXTERNAL_STORAGE permission when accessing other app files but since here we want to read only image so REQUESTING READ_EXTERNAL_STORAGE
        ActivityCompat.requestPermissions(this,new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE},311);

        //below android 13
         // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 311);
       // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE}, 311);
    }
    private boolean checkCameraPermission(){ //Requesting camera permission
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;//IT IS CAMERA PERMISSION not storage permission

//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){//if it is android 13 or above
//            //return (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
//            return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
//        }
//        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }
    private void requestCameraPermission() {
        //requestPermissions(cameraPermission, CAMERA_REQUEST);
       Toast.makeText(this, "ENABLE CAMERA PERMISSION TO TAKE PHOTO", Toast.LENGTH_LONG).show();
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 311);

        // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 311);
    }
    // Requesting camera and gallery
    // permission if not given
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case CAMERA_REQUEST: {
//                if (grantResults.length > 0) {
//                    boolean camera_accepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
//                    boolean writeStorageaccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
//                    if (camera_accepted && writeStorageaccepted) {
//                      //  pickFromGallery();
//                    } else {
//                        Toast.makeText(this, "Please Enable Camera and Storage Permissions", Toast.LENGTH_LONG).show();
//                    }
//                }
//            }
//            break;
//            case STORAGE_REQUEST: {
//                if (grantResults.length > 0) {
//                    boolean writeStorageaccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
//                    if (writeStorageaccepted) {
//                      //  pickFromGallery();
//                    } else {
//                        Toast.makeText(this, "Please Enable Storage Permissions", Toast.LENGTH_LONG).show();
//                    }
//                }
//            }
//            break;
//        }
//    }
    // Here we will pick image from gallery or camera
//       imageView.setOnClickListener(view -> {
//        ImagePicker.Companion.with(InsertPersonDetailsActivity.this)
//                .bothCameraGallery()
//                .crop()
//                .cropOval()
//                .maxResultSize(1080,1080,true)
//                .createIntent();
//
//    });
//    private void pickFromGallery() {
//      CropImage.activity().start(InsertPersonDetailsActivity.this);
//    }
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//
//            if (resultCode == RESULT_OK) {
//                Uri resultUri = result.getUri();
//                imageView.setImageURI(resultUri);
//
//            }else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
//                Toast.makeText(this, "Failed to crop image", Toast.LENGTH_SHORT).show();
//        }
//    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Database.closeDatabase();

        if(!MyUtility.deleteFolderAndSubFolderAllFiles("DCIM",true,getBaseContext())){//this folder created by image picker.delete external file
            Toast.makeText(this, "FAILED TO DELETE FILE FROM DEVICE", Toast.LENGTH_LONG).show();
        }
        if(!MyUtility.deleteFolderAndSubFolderAllFiles("Pictures",true,getBaseContext())){//this folder created by image picker.delete external file
            Toast.makeText(this, "FAILED TO DELETE FILE FROM DEVICE", Toast.LENGTH_LONG).show();
        }
    }
}