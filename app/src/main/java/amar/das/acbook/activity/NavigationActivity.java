package amar.das.acbook.activity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import androidx.navigation.ui.NavigationUI;

import amar.das.acbook.R;
import amar.das.acbook.databinding.ActivityNavigationBinding;
import amar.das.acbook.googlesigninauthentication.GoogleIdOptionsUtil;
import amar.das.acbook.sharedpreferences.SharedPreferencesHelper;

public class NavigationActivity extends AppCompatActivity {
    private ActivityNavigationBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0); //we have used overridePendingTransition(), it is used to remove activity create animation while re-creating activity.
        binding = ActivityNavigationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
       /* AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();*/
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_navigation);
       // NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }
    @Override
    protected void onStart() {
        super.onStart();
        if(!isUserSignIn()){//if you want to remove signin feature then remove this signin code
         Intent intent = new Intent(this,SignInWithGoogleActivity.class);//unless user signin cant use the app
         startActivity(intent);
        }
    }
    private boolean isUserSignIn(){
        return SharedPreferencesHelper.getString(this,SharedPreferencesHelper.Keys.GOOGLE_SIGNIN_EMAIL.name(),null) != null;
    }
}