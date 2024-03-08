package amar.das.acbook.ui.ml;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayoutMediator;

import amar.das.acbook.activity.BackupCalculatedInvoicesActivity;
import amar.das.acbook.activity.FindActivity;
import amar.das.acbook.activity.InsertPersonDetailsActivity;
import amar.das.acbook.R;
import amar.das.acbook.adapters.FragmentAdapter;
import amar.das.acbook.databinding.FragmentMlTabBinding;


public class MLFragment extends Fragment  {
    ActionBarDrawerToggle drawerToggle;
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

        drawerToggle=new ActionBarDrawerToggle(getActivity(),binding.drawerLayout,R.string.open,R.string.close);
        binding.drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        binding.verticleMenu.setOnClickListener(view -> {
           binding.drawerLayout.openDrawer(GravityCompat.START);
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

