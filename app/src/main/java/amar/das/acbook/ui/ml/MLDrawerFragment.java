package amar.das.acbook.ui.ml;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.concurrent.atomic.AtomicInteger;

import amar.das.acbook.Database;

import amar.das.acbook.activity.FindActivity;
import amar.das.acbook.activity.RegisterPersonDetailsActivity;
import amar.das.acbook.R;
import amar.das.acbook.activity.ManualBackupActivity;
import amar.das.acbook.adapters.FragmentAdapter;
import amar.das.acbook.databinding.FragmentMlTabBinding;
import amar.das.acbook.fragments.BusinessInfoBottomSheetFragment;
import amar.das.acbook.globalenum.GlobalConstants;
import amar.das.acbook.sharedpreferences.SharedPreferencesHelper;
import amar.das.acbook.utility.MyUtility;

public class MLDrawerFragment extends Fragment {
    private  FragmentMlTabBinding binding ;
    AlertDialog dialog;//to close when destroy
    //private String[] titles=new String[]{getContext().getResources().getString(R.string.mestre),getResources().getString(R.string.laber),getResources().getString(R.string.inactive)};//to set on pager Ddont work
   // private String[] titles=new String[]{getString(R.string.mestre),getString(R.string.laber),getString(R.string.inactive)};//dont work
    private final String[] titles=new String[]{"M","L","INACTIVE"};//to set on pager
    //important
    //to store image in db we have to convert Bitmap to bytearray
    //to set in imageview we have to get from db as Blob known as large byte and convert it to Bitmap then set in imageview
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMlTabBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        //takeAllAppPermissionAtOnce();
        //for drawer toggle
        ActionBarDrawerToggle  drawerToggle=new ActionBarDrawerToggle(getActivity(),binding.drawerLayout,R.string.open,R.string.close);
        binding.drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        initialiseHeader();

        binding.navigationDrawer.getHeaderView(0).findViewById(R.id.edit_header_drawer).setOnClickListener(view -> {//first getting view then finding id.//View headerView = binding.navigationDrawer.getHeaderView(0);
            binding.drawerLayout.closeDrawer(GravityCompat.START);//to close drawer
            BusinessInfoBottomSheetFragment businessInfoBottomSheetFragment=new BusinessInfoBottomSheetFragment();//open dialog
            businessInfoBottomSheetFragment.show(requireActivity().getSupportFragmentManager(),businessInfoBottomSheetFragment.getTag());
        });

        binding.navigationDrawer.getHeaderView(0).findViewById(R.id.header_layout).setOnClickListener(view -> {
            if(MyUtility.copyTextToClipBoard(getBusinessDetails(),getContext())){
                MyUtility.snackBar(view,getResources().getString(R.string.message_copied));
            }
        });

        binding.verticleMenu.setOnClickListener(view -> {
           binding.drawerLayout.openDrawer(GravityCompat.START);//to open drawer
        });
        binding.backupBtn.setOnClickListener(view -> {
            //finish() no need to finish the current activity
            Intent intent = new Intent(getContext(),ManualBackupActivity.class);
            startActivity(intent);
        });

        binding.navigationDrawer.setNavigationItemSelectedListener(item -> {
//            ProgressDialogHelper progressBar = new ProgressDialogHelper( getContext());
            if(item.getItemId() == R.id.backup_manually){
                //finish() no need to finish the current activity
                Intent intent = new Intent(getContext(), ManualBackupActivity.class);
                startActivity(intent);
            }else if(item.getItemId() == R.id.inactive_setting) {
                  openDialogSetting(getContext(),true);
            }
            //  binding.drawerLayout.closeDrawer(GravityCompat.START);//to close drawer
            return true;
        });

        binding.searchClickTv.setOnClickListener(view -> {
            Intent intent=new Intent(getContext(),FindActivity.class);
            startActivity(intent);
        });

        binding.viewPager2.setAdapter(new FragmentAdapter(getActivity()));
        new TabLayoutMediator(binding.tabLayout, binding.viewPager2,((tab,position)-> tab.setText(titles[position]))).attach();//set text to page according to position

//        binding.verticleMenu.setOnClickListener(view -> {
//            PopupMenu popup=new PopupMenu(getContext(),binding.verticleMenu);
//            popup.inflate(R.menu.popuo_menu);
//
//            popup.setOnMenuItemClickListener(item -> {
//                switch(item.getItemId()){
//                    case R.id.insert_new:{
//                        Intent intent = new Intent(getContext(), InsertPersonDetailsActivity.class);
//                        startActivity(intent);
//                        break;
//                    }
//                    case R.id.all_invoice:{
//                        Intent intent = new Intent(getContext(), BackupCalculatedInvoicesActivity.class);
//                        startActivity(intent);
//                        break;
//                    }
//                }
//                return true;
//            });
//            popup.show();
//        });
        binding.addPerson.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), RegisterPersonDetailsActivity.class);
            startActivity(intent);
        });
        return root;
    }
    private void openDialogSetting(Context context,boolean setCancellable) {
        AlertDialog.Builder myCustomDialog=new AlertDialog.Builder(context);
        LayoutInflater inflater=LayoutInflater.from(context);
        View myView=inflater.inflate(R.layout.dialog_inactive_setting,null);//myView contain all layout view ids

        myCustomDialog.setView(myView);//set custom layout to alert dialog
        myCustomDialog.setCancelable(setCancellable);//if false user touch to other place then dialog will not be close

        dialog=myCustomDialog.create();//myCustomDialog variable cannot be use in inner class so creating another final variable  to use in inner class
        RadioGroup radioGroups=myView.findViewById(R.id.inactive_setting_radiogp);
        Button save=myView.findViewById(R.id.save_btn_inactive_setting);
        Button cancel=myView.findViewById(R.id.cancel_btn_inactive_setting);
        setUserSelectedInactiveRadioButton(myView);
        //Multiple threads (parts of your program running concurrently) can access and update the value stored in an AtomicInteger without causing data corruption. This is crucial when dealing with shared resources in multithreaded applications.
        AtomicInteger days= new AtomicInteger(SharedPreferencesHelper.getInt(context, SharedPreferencesHelper.Keys.INACTIVE_DAYS.name(),Integer.parseInt(GlobalConstants.TWO_WEEKS_DEFAULT.getValue())));

        radioGroups.setOnCheckedChangeListener((radioGroup, checkedIdOfRadioBtn) -> {
            if(checkedIdOfRadioBtn == R.id.two_weeks_rb){
                days.set(Integer.parseInt(GlobalConstants.TWO_WEEKS_DEFAULT.getValue()));
            }else if (checkedIdOfRadioBtn == R.id.three_weeks_rb) {
                days.set(Integer.parseInt(GlobalConstants.THREE_WEEKS.getValue()));
            }else if(checkedIdOfRadioBtn == R.id.one_month_rb) {
                days.set(Integer.parseInt(GlobalConstants.ONE_MONTH.getValue()));
            }
        });

        cancel.setOnClickListener(view12 -> {dialog.dismiss();});
        save.setOnClickListener(view1 -> {

            SharedPreferencesHelper.setInt(context,SharedPreferencesHelper.Keys.INACTIVE_DAYS.name(),days.get());
            dialog.dismiss();//after user click save btn then close the dialog

        });
        dialog.show();
    }
    private void setUserSelectedInactiveRadioButton(View myView){
        RadioButton twoWeek=myView.findViewById(R.id.two_weeks_rb);
        RadioButton threeWeek=myView.findViewById(R.id.three_weeks_rb);
        RadioButton oneMonth=myView.findViewById(R.id.one_month_rb);
        int days=SharedPreferencesHelper.getInt(myView.getContext(),SharedPreferencesHelper.Keys.INACTIVE_DAYS.name(),Integer.parseInt(GlobalConstants.TWO_WEEKS_DEFAULT.getValue()));
        if(days == Integer.parseInt(GlobalConstants.TWO_WEEKS_DEFAULT.getValue())){//default value   for making inactive
            twoWeek.setChecked(true);
        }else if(days == Integer.parseInt(GlobalConstants.THREE_WEEKS.getValue())){
            threeWeek.setChecked(true);
        }else if(days == Integer.parseInt(GlobalConstants.ONE_MONTH.getValue())){
            oneMonth.setChecked(true);
        }
    }
    private String  getBusinessDetails() {
        StringBuilder text=new StringBuilder();
        if(SharedPreferencesHelper.getString(getContext(),SharedPreferencesHelper.Keys.BUSINESS_NAME.name(),"").equals("")){//if there is no business name
            text.append(getResources().getString(R.string.business_name)+":\n");
        }else {
            text.append(SharedPreferencesHelper.getString(getContext(), SharedPreferencesHelper.Keys.BUSINESS_NAME.name(), "")+"\n");
        }
        text.append(getResources().getString(R.string.whatsapp)+": "+SharedPreferencesHelper.getString(getContext(),SharedPreferencesHelper.Keys.WHATSAPP_NUMBER.name(),"")+" , "+SharedPreferencesHelper.getString(getContext(),SharedPreferencesHelper.Keys.PHONE_NUMBER.name(),"")+"\n")
                .append(getResources().getString(R.string.email)+": "+SharedPreferencesHelper.getString(getContext(),SharedPreferencesHelper.Keys.EMAIL.name(),"")+"\n")
                .append(getResources().getString(R.string.gst)+": "+SharedPreferencesHelper.getString(getContext(),SharedPreferencesHelper.Keys.GST_NUMBER.name(),"")+"\n")
                .append(getResources().getString(R.string.address)+": "+SharedPreferencesHelper.getString(getContext(),SharedPreferencesHelper.Keys.ADDRESS.name(),""));
        return text.toString();
    }
    private void initialiseHeader(){
        TextView businessName= binding.navigationDrawer.getHeaderView(0).findViewById(R.id.business_name_tv);
        if(SharedPreferencesHelper.getString(getContext(),SharedPreferencesHelper.Keys.BUSINESS_NAME.name(),"").equals("")){
            businessName.setText(getResources().getString(R.string.business_name)+": "+GlobalConstants.DEFAULT_BUSINESS_NAME.getValue());
        }else {
            businessName.setText(SharedPreferencesHelper.getString(getContext(), SharedPreferencesHelper.Keys.BUSINESS_NAME.name(),""));
        }

        TextView phone= binding.navigationDrawer.getHeaderView(0).findViewById(R.id.phone_and_whatsapp_tv);
        phone.setText(getWhatsappOrPhoneNumber(SharedPreferencesHelper.getString(getContext(),SharedPreferencesHelper.Keys.WHATSAPP_NUMBER.name(),""),SharedPreferencesHelper.getString(getContext(),SharedPreferencesHelper.Keys.PHONE_NUMBER.name(),"")));

        TextView email= binding.navigationDrawer.getHeaderView(0).findViewById(R.id.email_tv);
        email.setText(getResources().getString(R.string.email)+": "+SharedPreferencesHelper.getString(getContext(),SharedPreferencesHelper.Keys.EMAIL.name(),""));

        TextView gstIn= binding.navigationDrawer.getHeaderView(0).findViewById(R.id.gstin_tv);
        gstIn.setText(getResources().getString(R.string.gst)+": "+SharedPreferencesHelper.getString(getContext(),SharedPreferencesHelper.Keys.GST_NUMBER.name(),""));

        TextView businessAddress= binding.navigationDrawer.getHeaderView(0).findViewById(R.id.business_address_tv);
        businessAddress.setText(getResources().getString(R.string.address)+": "+SharedPreferencesHelper.getString(getContext(),SharedPreferencesHelper.Keys.ADDRESS.name(),""));
    }
    private String getWhatsappOrPhoneNumber(String whatsappNo, String phoneNo) {
        StringBuilder sb = new StringBuilder(getResources().getString(R.string.whatsapp)+": ");

        if (!TextUtils.isEmpty(whatsappNo)) {
            sb.append(whatsappNo);

            if (!TextUtils.isEmpty(phoneNo)) {
                sb.append(" , ").append(phoneNo);
            }
        } else if (!TextUtils.isEmpty(phoneNo)) {
            sb.append(phoneNo);
        }

        return sb.toString();
    }
    private void takeAllAppPermissionAtOnce() {
//        //Taking multiple permission at once by user https://www.youtube.com/watch?v=y0gX4FD3nxk or  https://www.youtube.com/watch?v=y0gX4FD3nxk
//        //CHECKING ALL PERMISSION IS GRANTED OR NOT
//        if((getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
//                (getContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
//                (getContext().checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) &&
//                (getContext().checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) &&
//                (getContext().checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED)){
//
//        }else{//if user not granted permission then request for permission
//            Toast.makeText(getContext(), "ALL PERMISSION REQUIRED PLEASE ENABLE PERMISSION", Toast.LENGTH_LONG).show();
//            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.RECORD_AUDIO,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                    Manifest.permission.READ_EXTERNAL_STORAGE,
//                    Manifest.permission.CAMERA,
//                    Manifest.permission.SEND_SMS}, 80);
//        }
//        if(!(getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)){
//            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},80);
//        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(dialog != null){
            dialog.dismiss();
        }
//        if(!MyUtility.deleteFolderAndSubFolderAllFiles(GlobalConstants.PDF_FOLDER_NAME.getValue(),true,getContext())){//delete external file
//            Toast.makeText(getContext(), "FAILED TO DELETE FILE FROM DEVICE", Toast.LENGTH_LONG).show();
//        }
//        if(!MyUtility.deleteFolderAndSubFolderAllFiles(null,false,getContext())){//delete cache file
//            Toast.makeText(getContext(), "FAILED TO DELETE FILE FROM DEVICE", Toast.LENGTH_LONG).show();
//        }
        binding = null;
        Database.closeDatabase();
    }
}

