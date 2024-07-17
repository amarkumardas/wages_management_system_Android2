package amar.das.acbook.ui.help;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import amar.das.acbook.R;
import amar.das.acbook.databinding.FragmentHelpTabBinding;
import amar.das.acbook.globalenum.GlobalConstants;
import amar.das.acbook.utility.MyUtility;

public class HelpFragment extends Fragment {
    private FragmentHelpTabBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHelpTabBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initialiseFields();

        binding.whatsappContact.setOnClickListener(view -> {
            if(!MyUtility.isInternetConnected(getContext())){
                Toast.makeText(getContext(), getString(R.string.please_turn_on_your_internet), Toast.LENGTH_LONG).show();
                return;
            }
            if(!MyUtility.shareMessageDirectlyToWhatsApp("",GlobalConstants.WHATSAPP_CONTACT.getValue(),getContext())){
                Toast.makeText(getContext(), getString(R.string.try_other_way_to_contact), Toast.LENGTH_LONG).show();
            }
        });

        binding.emailContact.setOnClickListener(view -> {
            if(!MyUtility.isInternetConnected(getContext())){
                Toast.makeText(getContext(), getString(R.string.please_turn_on_your_internet), Toast.LENGTH_LONG).show();
                return;
            }
            try {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain"); // Can be changed for different content types
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{GlobalConstants.EMAIL_CONTACT.getValue()});
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.please_help)+" App Version: "+getAppVersion());
                intent.putExtra(Intent.EXTRA_TEXT, "");
                startActivity(Intent.createChooser(intent, getString(R.string.choose_gmail_app)));
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(getContext(), getString(R.string.try_other_way_to_contact), Toast.LENGTH_LONG).show();            }
        });

        binding.instagramContact.setOnClickListener(view -> {
            if(!MyUtility.isInternetConnected(getContext())){
                Toast.makeText(getContext(), getString(R.string.please_turn_on_your_internet), Toast.LENGTH_LONG).show();
                return;
            }

            Uri uri =Uri.parse("https://www.instagram.com/" +GlobalConstants.INSTA_CONTACT.getValue());
            Intent instagram= new Intent(Intent.ACTION_VIEW, uri);
            instagram.setPackage("com.instagram.android");
            try {
                try {
                    startActivity(instagram);//direct open instagram if instagram app install

                }catch (ActivityNotFoundException e) {//occur if the Instagram app isn't installed on the device.
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));//Open in browser as fallback.This attempt creates a new Intent with just the Instagram profile URL (without the package specification).Launching this Intent might open the link in the user's default browser, which could then prompt them to open the Instagram app if available.

                }
            }catch (Exception x){
              x.printStackTrace();
              Toast.makeText(getContext(), getString(R.string.try_other_way_to_contact), Toast.LENGTH_LONG).show();
           }
        });

        return root;
    }

    private String getAppVersion() {
        try {
            PackageInfo pInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "null";
    }

    private void initialiseFields() {
        binding.whatsappTv.setText(GlobalConstants.WHATSAPP_CONTACT.getValue());
        binding.emailTv.setText(GlobalConstants.EMAIL_CONTACT.getValue());
        binding.instaTv.setText("@"+GlobalConstants.INSTA_CONTACT.getValue());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}