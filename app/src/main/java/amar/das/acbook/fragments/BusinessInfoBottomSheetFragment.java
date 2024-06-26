package amar.das.acbook.fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import amar.das.acbook.R;
import amar.das.acbook.databinding.FragmentBusinessInfoBottomSheetBinding;
import amar.das.acbook.globalenum.GlobalConstants;
import amar.das.acbook.sharedpreferences.SharedPreferencesHelper;
import amar.das.acbook.utility.MyUtility;

public class BusinessInfoBottomSheetFragment extends BottomSheetDialogFragment {
    private FragmentBusinessInfoBottomSheetBinding binding;
    int [] correctInputArr =new int[5];
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBusinessInfoBottomSheetBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        initialiseFieldsWithValues();
        binding.businessNameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String userInput= binding.businessNameEt.getText().toString().trim();
                binding.businessNameEt.setTextColor(Color.BLACK);
                correctInputArr[0]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data

                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(correctInputArr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                   binding.businessInfoSaveButton.setBackgroundResource(R.drawable.green_color_bg);
                   binding.businessInfoSaveButton.setText(getString(R.string.save));
                   binding.businessInfoSaveButton.setEnabled(true);
                }

                if(!(userInput.matches("[a-zA-Z0-9 ]+") || userInput.isEmpty())){//alphabetic characters, digits, and spaces
                     binding.businessNameEt.setTextColor(Color.RED);
                    binding.businessInfoSaveButton.setText(getString(R.string.wrong_input));
                    binding.businessInfoSaveButton.setBackgroundResource(R.drawable.red_color_background);
                    binding.businessInfoSaveButton.setEnabled(false);
                    correctInputArr[0]=2;//means wrong data
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
        binding.whatsappNumberEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String string= binding.whatsappNumberEt.getText().toString().trim();
                binding.whatsappNumberEt.setTextColor(Color.BLACK);
                correctInputArr[1]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data

                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(correctInputArr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    binding.businessInfoSaveButton.setBackgroundResource(R.drawable.green_color_bg);
                    binding.businessInfoSaveButton.setText(getString(R.string.save));
                    binding.businessInfoSaveButton.setEnabled(true);
                }

                if(!(string.matches("[0-9]+")|| string.isEmpty())){//alphabetic characters, digits, and spaces
                    binding.whatsappNumberEt.setTextColor(Color.RED);
                    binding.businessInfoSaveButton.setText(getString(R.string.wrong_input));
                    binding.businessInfoSaveButton.setBackgroundResource(R.drawable.red_color_background);
                    binding.businessInfoSaveButton.setEnabled(false);
                    correctInputArr[1]=2;//means wrong data
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
        binding.phoneNumberEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String string= binding.phoneNumberEt.getText().toString().trim();
                binding.phoneNumberEt.setTextColor(Color.BLACK);
                correctInputArr[2]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data

                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(correctInputArr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    binding.businessInfoSaveButton.setBackgroundResource(R.drawable.green_color_bg);
                    binding.businessInfoSaveButton.setText(getString(R.string.save));
                    binding.businessInfoSaveButton.setEnabled(true);
                }

                if(!(string.matches("[0-9]+")|| string.isEmpty())){//only digits
                    binding.phoneNumberEt.setTextColor(Color.RED);
                    binding.businessInfoSaveButton.setText(getString(R.string.wrong_input));
                    binding.businessInfoSaveButton.setBackgroundResource(R.drawable.red_color_background);
                    binding.businessInfoSaveButton.setEnabled(false);
                    correctInputArr[2]=2;//means wrong data
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
        binding.emailEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String string= binding.emailEt.getText().toString().trim();
                binding.emailEt.setTextColor(Color.BLACK);
                correctInputArr[3]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data

                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(correctInputArr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    binding.businessInfoSaveButton.setBackgroundResource(R.drawable.green_color_bg);
                    binding.businessInfoSaveButton.setText(getString(R.string.save));
                    binding.businessInfoSaveButton.setEnabled(true);
                }

                if(!(string.matches("^[a-zA-Z0-9_+&*-]+(?:\\."+ "[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-z" + "A-Z]{2,7}$") || string.isEmpty())){
                    binding.emailEt.setTextColor(Color.RED);
                    binding.businessInfoSaveButton.setText(getString(R.string.wrong_input));
                    binding.businessInfoSaveButton.setBackgroundResource(R.drawable.red_color_background);
                    binding.businessInfoSaveButton.setEnabled(false);
                    correctInputArr[3]=2;//means wrong data
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
        binding.gstinEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String string= binding.gstinEt.getText().toString().trim();
                binding.gstinEt.setTextColor(Color.BLACK);
                correctInputArr[4]=1;//means data is inserted.This line should be here because when user enter wrong data and again enter right data then it should update array to 1 which indicate write data

                //this will check if other data is right or wrong
                if(!MyUtility.isEnterDataIsWrong(correctInputArr)) {//this is important if in field data is wrong then save button will not enabled until data is right.if save button is enabled with wrong data then if user has record audio then it will not be saved it will store null so to check right or wrong data this condition is important
                    binding.businessInfoSaveButton.setBackgroundResource(R.drawable.green_color_bg);
                    binding.businessInfoSaveButton.setText(getString(R.string.save));
                    binding.businessInfoSaveButton.setEnabled(true);
                }

                if(!(string.matches("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}[Z]{1}[0-9A-Z]{1}")|| string.isEmpty())){
                    binding.gstinEt.setTextColor(Color.RED);
                    binding.businessInfoSaveButton.setText(getString(R.string.wrong_input));
                    binding.businessInfoSaveButton.setBackgroundResource(R.drawable.red_color_background);
                    binding.businessInfoSaveButton.setEnabled(false);
                    correctInputArr[4]=2;//means wrong data
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });

        binding.businessInfoSaveButton.setOnClickListener(view1 -> {

            if(!checkCredentials(binding.whatsappNumberEt,binding.phoneNumberEt)) {return ;}

               SharedPreferencesHelper.setString(getContext(),SharedPreferencesHelper.Keys.BUSINESS_NAME.name(),binding.businessNameEt.getText().toString().toUpperCase().trim());

                SharedPreferencesHelper.setString(getContext(),SharedPreferencesHelper.Keys.WHATSAPP_NUMBER.name(),binding.whatsappNumberEt.getText().toString().trim());

                SharedPreferencesHelper.setString(getContext(),SharedPreferencesHelper.Keys.PHONE_NUMBER.name(),binding.phoneNumberEt.getText().toString().trim());

                SharedPreferencesHelper.setString(getContext(),SharedPreferencesHelper.Keys.EMAIL.name(),binding.emailEt.getText().toString().trim());

                SharedPreferencesHelper.setString(getContext(),SharedPreferencesHelper.Keys.GST_NUMBER.name(),binding.gstinEt.getText().toString().trim());

                SharedPreferencesHelper.setString(getContext(),SharedPreferencesHelper.Keys.ADDRESS.name(),binding.businessAddressEt.getText().toString().toUpperCase().trim());

                dismiss();//close bottom-sheet
        });
        return view;
    }
    private boolean checkCredentials(EditText whatsApp, EditText phoneNumber) {
        boolean isValid = true;
        if (!TextUtils.isEmpty(whatsApp.getText().toString().trim()) && whatsApp.getText().toString().trim().length() != 10) {
            whatsApp.setError(getString(R.string.should_be_10_digits));
            isValid = false;
        }
        if (!TextUtils.isEmpty(phoneNumber.getText().toString().trim()) && phoneNumber.getText().toString().trim().length() != 10) {
            phoneNumber.setError(getString(R.string.should_be_10_digits));
            isValid = false;
        }
        return isValid;
    }
    private void initialiseFieldsWithValues(){
        binding.businessNameEt.setText(SharedPreferencesHelper.getString(getContext(),SharedPreferencesHelper.Keys.BUSINESS_NAME.name(),GlobalConstants.DEFAULT_BUSINESS_NAME.getValue()));
        binding.whatsappNumberEt.setText(SharedPreferencesHelper.getString(getContext(),SharedPreferencesHelper.Keys.WHATSAPP_NUMBER.name(),""));
        binding.phoneNumberEt.setText(SharedPreferencesHelper.getString(getContext(),SharedPreferencesHelper.Keys.PHONE_NUMBER.name(),""));
        binding.emailEt.setText(SharedPreferencesHelper.getString(getContext(),SharedPreferencesHelper.Keys.EMAIL.name(),""));
        binding.gstinEt.setText(SharedPreferencesHelper.getString(getContext(),SharedPreferencesHelper.Keys.GST_NUMBER.name(),""));
        binding.businessAddressEt.setText(SharedPreferencesHelper.getString(getContext(),SharedPreferencesHelper.Keys.ADDRESS.name(),""));
    }
}