package amar.das.acbook.activity;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import amar.das.acbook.R;
import amar.das.acbook.databinding.ActivitySigninWithGoogleBinding;
import amar.das.acbook.googlesigninauthentication.GoogleIdOptionsUtil;

public class SignInWithGoogleActivity extends AppCompatActivity {
  ActivitySigninWithGoogleBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if(isUserSignIn()){//if user already signin
//            GoogleIdOptionsUtil.goToNavigationActivity(this);
//        }

        EdgeToEdge.enable(this);//The enableEdgeToEdge() method allows your Android app to display content using the full width and height of the screen. It makes system bars transparent, adjusts system icon colors, and ensures an edge-to-edge layout
//      setContentView(R.layout.activity_signin_with_google);
        binding = ActivitySigninWithGoogleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signin_layout),(v, insets) -> {// This code snippet adjusts the padding of a view based on the system bars’ insets, ensuring proper layout and avoiding content overlap with system UI elements
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    binding.newUserButton.setOnClickListener(view ->{
//        if(isUserSignIn()){//if user already signin
//             GoogleIdOptionsUtil.goToNavigationActivity(this);
//        }else {
        //we can place dialog box here to show privacy policy
            GoogleIdOptionsUtil googleIdOptionsUtil = new GoogleIdOptionsUtil(this);
            googleIdOptionsUtil.createGoogleIdOption();
        //}
    });
    binding.findBackupButton.setOnClickListener(view -> {

    });
    }
//    private boolean isUserSignIn() {
//        return SharedPreferencesHelper.getString(this,SharedPreferencesHelper.Keys.GOOGLE_SIGNIN_EMAIL.name(),null) != null;
//    }
}