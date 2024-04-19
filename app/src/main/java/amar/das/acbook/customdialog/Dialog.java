package amar.das.acbook.customdialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import amar.das.acbook.Database;
import amar.das.acbook.R;
import amar.das.acbook.activity.IndividualPersonDetailActivity;
import amar.das.acbook.utility.MyUtility;

public class Dialog {
    private Context context;
    private String id;

    public Dialog(Context context,String id){
        this.context=context;
        this.id=id;
    }
    public boolean openUpdateRatesDialogSaveAndRefresh(boolean setCancelable){
        AlertDialog.Builder myCustomDialog=new AlertDialog.Builder( context);
        LayoutInflater inflater=LayoutInflater.from(context);

        View myView=inflater.inflate(R.layout.dialog_update_rates,null);//myView contain all layout view ids
        myCustomDialog.setView(myView);//set custom layout to alert dialog
        myCustomDialog.setCancelable(setCancelable);//if false user touch to other place then dialog will not be close

        final AlertDialog dialog=myCustomDialog.create();//myCustomDialog variable cannot be use in inner class so creating another final variable  to use in inner class
        TextView hardcodedP1Tv=myView.findViewById(R.id.hardcoded_p1_tv_rate);//don't remove
        EditText inputP1Et=myView.findViewById(R.id.input_p1_et_rate);
        TextView hardcodedP2Tv=myView.findViewById(R.id.hardcoded_p2_tv_rate);
        EditText inputP2Et=myView.findViewById(R.id.input_p2_et_rate);
        TextView hardcodedP3Tv=myView.findViewById(R.id.hardcoded_p3_tv_rate);
        EditText inputP3Et=myView.findViewById(R.id.input_p3_et_rate);
        TextView hardcodedP4Tv=myView.findViewById(R.id.hardcoded_p4_tv_rate);
        EditText inputP4Et=myView.findViewById(R.id.input_p4_et_rate);

        Button infoSave=myView.findViewById(R.id.save_btn_rate);
        Button  cancel=myView.findViewById(R.id.cancel_btn_rate);
        cancel.setOnClickListener(view12 -> dialog.dismiss());


        int indicator=MyUtility.get_indicator(context,id);
        setRateComponentAccordingToId(hardcodedP1Tv,inputP1Et,hardcodedP2Tv,inputP2Et,hardcodedP3Tv,inputP3Et,hardcodedP4Tv,inputP4Et,infoSave,new int[indicator],new int[indicator],indicator,id);

        infoSave.setOnClickListener(view -> {
            int p1Rate=0,p2Rate=0,p3Rate=0,p4Rate=0;//taken this variable so that default value will be stored
            //rates--------------------------------
            if(!TextUtils.isEmpty(inputP1Et.getText().toString().trim())){//TextUtils.isEmpty() checks for null or ""
                p1Rate= Integer.parseInt(inputP1Et.getText().toString().trim());
            }
            if(!TextUtils.isEmpty(inputP2Et.getText().toString().trim())){//TextUtils.isEmpty() checks for null or ""
                p2Rate= Integer.parseInt(inputP2Et.getText().toString().trim());
            }
            if(!TextUtils.isEmpty(inputP3Et.getText().toString().trim())){//TextUtils.isEmpty() checks for null or ""
                p3Rate= Integer.parseInt(inputP3Et.getText().toString().trim());
            }
            if(!TextUtils.isEmpty(inputP4Et.getText().toString().trim())){//TextUtils.isEmpty() checks for null or ""
                p4Rate= Integer.parseInt(inputP4Et.getText().toString().trim());
            }

            dialog.dismiss();//dismiss current dialog because new dialog will be open to display result()

           try(Database db=Database.getInstance(context)){
               if(db.update_Rating_TABLE_NAME3(null, null ,null,null,(p1Rate!=0?String.valueOf(p1Rate):null),(p2Rate!=0?String.valueOf(p2Rate):null),(p3Rate!=0?String.valueOf(p3Rate):null),(p4Rate!=0?String.valueOf(p4Rate):null),id,indicator,true)){
                   //refresh
                   Intent intent=new Intent(context,IndividualPersonDetailActivity.class);
                   intent.putExtra("ID",id);
                   ((Activity) context).finish();//while going to other activity so destroy  this current activity so that while coming back we will see refresh activity
                   context.startActivity(intent);
               }else{
                   MyUtility.showDefaultDialog(context.getString(R.string.not_saved),context.getString(R.string.failed_to_update),context);
               }
           }catch (Exception x){
               x.printStackTrace();
               Toast.makeText(context, "error occurred", Toast.LENGTH_LONG).show();
           }

        });
        dialog.show();
        return true;
    }
    //set rate component according to indicator
    private void setRateComponentAccordingToId(TextView hardcodedP1Tv, EditText inputP1Rate, TextView hardcodedP2Tv, EditText inputP2Rate, TextView hardcodedP3Tv, EditText inputP3Rate, TextView hardcodedP4Tv, EditText inputP4Rate, Button saveButton, int checkCorrectionArray[], int userInputRateArray[], int indicator, String id) {
        try(Database db=new Database(context);
            Cursor rateCursor1 = db.getData("SELECT " + Database.COL_32_R1 + " FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE " + Database.COL_31_ID + "= '" + id + "'")){
            if(rateCursor1 != null) rateCursor1.moveToFirst();
            String mainSkill=db.getOnlyMainSkill(id);

            Cursor skillNRateCursor=null;
            if(indicator > 1) {//if indicator more than 1 then get  other skill and rate
                skillNRateCursor = db.getData("SELECT " + Database.COL_36_SKILL2 + "," + Database.COL_37_SKILL3 + "," + Database.COL_38_SKILL4 + " , "+ Database.COL_33_R2 + "," + Database.COL_34_R3 + "," + Database.COL_35_R4 + " FROM " + Database.TABLE_NAME_RATE_SKILL + " WHERE " + Database.COL_31_ID + "= '" + id + "'");
                if (skillNRateCursor != null) skillNRateCursor.moveToFirst();
            }
            //initially hide
            hardcodedP2Tv.setVisibility(View.GONE);
            inputP2Rate.setVisibility(View.GONE);
            hardcodedP3Tv.setVisibility(View.GONE);
            inputP3Rate.setVisibility(View.GONE);
            hardcodedP4Tv.setVisibility(View.GONE);
            inputP4Rate.setVisibility(View.GONE);

            switch (indicator){
                case 1: {
                    hardcodedP1Tv.setText(mainSkill);
                    inputP1Rate.setText(rateCursor1.getString(0));
                    rate1Et(inputP1Rate, saveButton, checkCorrectionArray, userInputRateArray);//input rate array is updated
                }
                break;
                case 2: {
                    hardcodedP2Tv.setVisibility(View.VISIBLE);
                    inputP2Rate.setVisibility(View.VISIBLE);

                    hardcodedP1Tv.setText(mainSkill);
                    inputP1Rate.setText(rateCursor1.getString(0));
                    rate1Et(inputP1Rate, saveButton, checkCorrectionArray, userInputRateArray);//input rate array is updated

                    hardcodedP2Tv.setText(skillNRateCursor.getString(0));
                    inputP2Rate.setText(skillNRateCursor.getString(3));
                    rate2Et(inputP2Rate, saveButton, checkCorrectionArray, userInputRateArray);//input rate array is updated
                }
                break;
                case 3: {
                    hardcodedP2Tv.setVisibility(View.VISIBLE);
                    inputP2Rate.setVisibility(View.VISIBLE);

                    hardcodedP3Tv.setVisibility(View.VISIBLE);
                    inputP3Rate.setVisibility(View.VISIBLE);

                    hardcodedP1Tv.setText(mainSkill);
                    inputP1Rate.setText(rateCursor1.getString(0));
                    rate1Et(inputP1Rate, saveButton, checkCorrectionArray, userInputRateArray);//input rate array is updated

                    hardcodedP2Tv.setText(skillNRateCursor.getString(0));
                    inputP2Rate.setText(skillNRateCursor.getString(3));
                    rate2Et(inputP2Rate, saveButton, checkCorrectionArray, userInputRateArray);//input rate array is updated

                    hardcodedP3Tv.setText(skillNRateCursor.getString(1));
                    inputP3Rate.setText(skillNRateCursor.getString(4));
                    rate3Et(inputP3Rate, saveButton, checkCorrectionArray, userInputRateArray);//input rate array is updated
                }
                break;
                case 4: {
                    hardcodedP2Tv.setVisibility(View.VISIBLE);
                    inputP2Rate.setVisibility(View.VISIBLE);

                    hardcodedP3Tv.setVisibility(View.VISIBLE);
                    inputP3Rate.setVisibility(View.VISIBLE);

                    hardcodedP4Tv.setVisibility(View.VISIBLE);
                    inputP4Rate.setVisibility(View.VISIBLE);

                    hardcodedP1Tv.setText(mainSkill);
                    inputP1Rate.setText(rateCursor1.getString(0));
                    rate1Et(inputP1Rate, saveButton, checkCorrectionArray, userInputRateArray);//input rate array is updated

                    hardcodedP2Tv.setText(skillNRateCursor.getString(0));
                    inputP2Rate.setText(skillNRateCursor.getString(3));
                    rate2Et(inputP2Rate, saveButton, checkCorrectionArray, userInputRateArray);//input rate array is updated

                    hardcodedP3Tv.setText(skillNRateCursor.getString(1));
                    inputP3Rate.setText(skillNRateCursor.getString(4));
                    rate3Et(inputP3Rate, saveButton, checkCorrectionArray, userInputRateArray);//input rate array is updated

                    hardcodedP4Tv.setText(skillNRateCursor.getString(2));
                    inputP4Rate.setText(skillNRateCursor.getString(5));
                    rate4Et(inputP4Rate, saveButton, checkCorrectionArray, userInputRateArray);//input rate array is updated
                }break;
            }
            if(skillNRateCursor != null) {
                skillNRateCursor.close();
            }
        }catch (Exception x){
            x.printStackTrace();
            Toast.makeText(context, "error occurred to show visibility", Toast.LENGTH_LONG).show();
        }
    }
    private void rate1Et(EditText inputP1Rate, Button  saveButton, int checkCorrectionArray[], int userInputRateArray[]) {
        inputP1Rate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String p1 =inputP1Rate.getText().toString().trim();
                inputP1Rate.setTextColor(context.getColor(R.color.black));
                checkCorrectionArray[0]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data
                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(checkCorrectionArray)) {//this is important if in field data is wrong then save button will not enabled until data is right.
                    saveButton.setVisibility(View.VISIBLE);
                }
                if (!(p1.matches("[0-9]+") || TextUtils.isEmpty(p1))) {//space or , or - is restricted"[.]?[0-9]+[.]?[0-9]*"
                    inputP1Rate.setTextColor(Color.RED);
                    saveButton.setVisibility(View.GONE);
                    checkCorrectionArray[0]=2;//means data is inserted wrong
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                try{
                    if(!TextUtils.isEmpty(inputP1Rate.getText().toString().trim())) {
                        userInputRateArray[0] = Integer.parseInt(inputP1Rate.getText().toString().trim());
                    }
                }catch(Exception e){
                    e.printStackTrace();
                    //Log.d(this.getClass().getSimpleName(), "method afterTextChanged: wrong input");
                }
            }
        });
    }
    private void rate2Et(EditText inputP2Rate, Button saveButton, int[] checkCorrectionArray, int[] userInputRateArray) {
        inputP2Rate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String p1 =inputP2Rate.getText().toString().trim();
                inputP2Rate.setTextColor(context.getColor(R.color.black));
                checkCorrectionArray[1]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data
                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(checkCorrectionArray)) {//this is important if in field data is wrong then save button will not enabled until data is right.
                    saveButton.setVisibility(View.VISIBLE);

                }
                if (!(p1.matches("[0-9]+") || TextUtils.isEmpty(p1))) {//space or , or - is restricted"[.]?[0-9]+[.]?[0-9]*"
                    inputP2Rate.setTextColor(Color.RED);
                    saveButton.setVisibility(View.GONE);
                    checkCorrectionArray[1]=2;//means data is inserted wrong
                    // Toast.makeText(IndividualPersonDetailActivity.this, "NOT ALLOWED(space  .  ,  -)\nPLEASE CORRECT", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                try{
                    if(!TextUtils.isEmpty(inputP2Rate.getText().toString().trim())) {
                        userInputRateArray[1] = Integer.parseInt(inputP2Rate.getText().toString().trim());
                    }

                }catch(Exception x){
                    x.printStackTrace();
                    //Log.d(this.getClass().getSimpleName(), "method afterTextChanged: wrong input");
                }
            }
        });
    }
    private void rate3Et(EditText inputP3Rate, Button saveButton, int[] checkCorrectionArray, int[] userInputRateArray) {
        inputP3Rate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String p1 =inputP3Rate.getText().toString().trim();
                inputP3Rate.setTextColor(context.getColor(R.color.black));
                checkCorrectionArray[2]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data
                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(checkCorrectionArray)) {//this is important if in field data is wrong then save button will not enabled until data is right.
                    saveButton.setVisibility(View.VISIBLE);
                }
                if (!(p1.matches("[0-9]+") || TextUtils.isEmpty(p1))) {//space or , or - is restricted"[.]?[0-9]+[.]?[0-9]*"
                    inputP3Rate.setTextColor(Color.RED);
                    saveButton.setVisibility(View.GONE);
                    checkCorrectionArray[2]=2;//means data is inserted wrong
                    //Toast.makeText(IndividualPersonDetailActivity.this, "NOT ALLOWED(space  .  ,  -)\nPLEASE CORRECT", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    if(!TextUtils.isEmpty(inputP3Rate.getText().toString().trim())) {
                        userInputRateArray[2] = Integer.parseInt(inputP3Rate.getText().toString().trim());
                    }
                }catch (Exception e){
                    e.printStackTrace();
                   // Log.d(this.getClass().getSimpleName(), "method afterTextChanged: wrong input");
                }
            }
        });
    }
    private void rate4Et(EditText inputP4Rate, Button saveButton, int[] checkCorrectionArray, int[] userInputRateArray) {
        inputP4Rate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String p1 =inputP4Rate.getText().toString().trim();
                inputP4Rate.setTextColor(context.getColor(R.color.black));
                checkCorrectionArray[3]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data
                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(checkCorrectionArray)) {//this is important if in field data is wrong then save button will not enabled until data is right.
                    saveButton.setVisibility(View.VISIBLE);
                }
                if (!(p1.matches("[0-9]+") || TextUtils.isEmpty(p1))) {//space or , or - is restricted"[.]?[0-9]+[.]?[0-9]*"
                    inputP4Rate.setTextColor(Color.RED);
                    saveButton.setVisibility(View.GONE);
                    checkCorrectionArray[3]=2;//means data is inserted wrong
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    if(!TextUtils.isEmpty(inputP4Rate.getText().toString().trim())) {
                        userInputRateArray[3] = Integer.parseInt(inputP4Rate.getText().toString().trim());
                    }
                }catch(Exception e){
                    e.printStackTrace();
                   //Log.d(this.getClass().getSimpleName(), "method afterTextChanged: wrong input");
                }
            }
        });
    }
}
