package amar.das.acbook.ui.ml;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayoutMediator;

import amar.das.acbook.activity.FindActivity;
import amar.das.acbook.activity.InsertPersonDetailsActivity;
import amar.das.acbook.R;
import amar.das.acbook.adapters.FragmentAdapter;
import amar.das.acbook.databinding.FragmentMlTabBinding;
import amar.das.acbook.fragments.BusinessInfoBottomSheetFragment;
import amar.das.acbook.sharedpreferences.SharedPreferencesHelper;
import amar.das.acbook.utility.MyUtility;


public class MLFragment extends Fragment  {
    private FragmentMlTabBinding binding ;
    //private String[] titles=new String[]{getContext().getResources().getString(R.string.mestre),getResources().getString(R.string.laber),getResources().getString(R.string.inactive)};//to set on pager Ddont work
   // private String[] titles=new String[]{getString(R.string.mestre),getString(R.string.laber),getString(R.string.inactive)};//dont work
    private String[] titles=new String[]{"M","L","INACTIVE"};//to set on pager
    //important
    //to store image in db we have to convert Bitmap to bytearray
    //to set in imageview we have to get from db as Blob known as large byte and convert it to Bitmap then set in imageview
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMlTabBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ActionBarDrawerToggle  drawerToggle=new ActionBarDrawerToggle(getActivity(),binding.drawerLayout,R.string.open,R.string.close);
        binding.drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        initialiseHeader();

        binding.navigationDrawer.getHeaderView(0).findViewById(R.id.edit_header_drawer).setOnClickListener(view -> {//first getting view then finding id.//View headerView = binding.navigationDrawer.getHeaderView(0);
            binding.drawerLayout.closeDrawer(GravityCompat.START);//to close drawer
            BusinessInfoBottomSheetFragment businessInfoBottomSheetFragment=new BusinessInfoBottomSheetFragment();
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
        binding.navigationDrawer.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.backup_active_mlg:{
                    Toast.makeText(getContext(), "clicked", Toast.LENGTH_SHORT).show();
                }break;
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START);//to close drawer
            return true;});

        takeAllAppPermissionAtOnce();
        binding.searchClickTv.setOnClickListener(view -> {
            Intent intent=new Intent(getContext(),FindActivity.class);
            startActivity(intent);

        });

        binding.viewPager2.setAdapter(new FragmentAdapter(getActivity()));
        new TabLayoutMediator(binding.tabLayout, binding.viewPager2,((tab,position)-> tab.setText(titles[position]))).attach();//set text to page according to position
//        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {//when tab select or swipe it will perform operation so we are getting position
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//
//            }
//        });


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
            Intent intent = new Intent(getContext(), InsertPersonDetailsActivity.class);
            startActivity(intent);
        });
        return root;
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
            businessName.setText(getResources().getString(R.string.business_name)+":");
        }else {
            businessName.setText(SharedPreferencesHelper.getString(getContext(), SharedPreferencesHelper.Keys.BUSINESS_NAME.name(), ""));
        }

        TextView phone= binding.navigationDrawer.getHeaderView(0).findViewById(R.id.phone_and_whatsapp_tv);
        phone.setText(getResources().getString(R.string.whatsapp)+": "+SharedPreferencesHelper.getString(getContext(),SharedPreferencesHelper.Keys.WHATSAPP_NUMBER.name(),"")+" , "+SharedPreferencesHelper.getString(getContext(),SharedPreferencesHelper.Keys.PHONE_NUMBER.name(),""));

        TextView email= binding.navigationDrawer.getHeaderView(0).findViewById(R.id.email_tv);
        email.setText(getResources().getString(R.string.email)+": "+SharedPreferencesHelper.getString(getContext(),SharedPreferencesHelper.Keys.EMAIL.name(),""));

        TextView gstIn= binding.navigationDrawer.getHeaderView(0).findViewById(R.id.gstin_tv);
        gstIn.setText(getResources().getString(R.string.gst)+": "+SharedPreferencesHelper.getString(getContext(),SharedPreferencesHelper.Keys.GST_NUMBER.name(),""));

        TextView businessAddress= binding.navigationDrawer.getHeaderView(0).findViewById(R.id.business_address_tv);
        businessAddress.setText(getResources().getString(R.string.address)+": "+SharedPreferencesHelper.getString(getContext(),SharedPreferencesHelper.Keys.ADDRESS.name(),""));
    }

    private void takeAllAppPermissionAtOnce() {
        //Taking multiple permission at once by user https://www.youtube.com/watch?v=y0gX4FD3nxk or  https://www.youtube.com/watch?v=y0gX4FD3nxk
        //CHECKING ALL PERMISSION IS GRANTED OR NOT
        if((getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (getContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (getContext().checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) &&
                (getContext().checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) &&
                (getContext().checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED)){

        }else{//if user not granted permission then request for permission
            Toast.makeText(getContext(), "ALL PERMISSION REQUIRED ENABLED PERMISSION", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.SEND_SMS}, 80);
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

