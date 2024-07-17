package amar.das.acbook.activity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import amar.das.acbook.R;
import amar.das.acbook.databinding.ActivityBackupCalculatedInvoicesBinding;
import amar.das.acbook.databinding.ActivityWebViewBinding;
import amar.das.acbook.utility.MyUtility;

public class WebViewActivity extends AppCompatActivity {
    public static final String PRIVACY_POLICY="1";
    public static final String TERMS_AND_CONDITIONS="2";
    ActivityWebViewBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0); //we have used overridePendingTransition(), it is used to remove activity create animation while re-creating activity.
        binding = ActivityWebViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.webView.setWebViewClient(new WebViewClient());
        WebSettings webSettings= binding.webView.getSettings();
        webSettings.setJavaScriptEnabled(true);//enabling javascript

        if (getIntent().hasExtra(PRIVACY_POLICY)) {
            if(MyUtility.isInternetConnected(this)){
                binding.webView.loadUrl("https://sites.google.com/view/labourmistriprivacypolicy");
            }else{
                Toast.makeText(this, getString(R.string.please_turn_on_your_internet), Toast.LENGTH_LONG).show();
            }

        }else if (getIntent().hasExtra(TERMS_AND_CONDITIONS)) {
            if(MyUtility.isInternetConnected(this)){
               binding.webView.loadUrl("https://sites.google.com/view/labourmistritermsandconditions");
            }else{
                Toast.makeText(this, getString(R.string.please_turn_on_your_internet), Toast.LENGTH_LONG).show();
            }
        }

        binding.gobackWebView.setOnClickListener(view -> {
            finish();//destroy current activity
        });

    }
}