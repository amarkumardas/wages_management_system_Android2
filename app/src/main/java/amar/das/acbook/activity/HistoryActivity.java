package amar.das.acbook.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.util.HashMap;

import amar.das.acbook.R;
import amar.das.acbook.databinding.ActivityHistoryBinding;
 ;

public class HistoryActivity extends AppCompatActivity {
    ActivityHistoryBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0); //we have used overridePendingTransition(), it is used to remove activity create animation while re-creating activity.
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

    }

    public static HashMap<String,String> stringToHashMap(String string) {//1.if key is there and value not there then get() method will return null.2.if key is not there but we try to retrieve value then get() method will alse return null.
        HashMap<String, String> hashMap = new HashMap<>();
        string = string.replaceAll("[{}]", "");// Remove curly brackets using a single replace call.eg {WAGES=5000, P1_SKILL=0, P1_WORK=6, NAME=AMAR KUMAR DAS}
        String[] keyValuePairs = string.split(",");
        for (String keyValuePair : keyValuePairs) {
            String[] keyValueArray = keyValuePair.split("=");
            if (keyValueArray.length == 2) {//Added a check to ensure that a valid key-value pair is added to the HashMap. This check verifies that there are exactly two parts when splitting by "=", avoiding potential ArrayIndexOutOfBoundsException errors.
                String key = keyValueArray[0].trim();
                String value = keyValueArray[1].trim();
                hashMap.put(key, value);
            }
        }
        return hashMap;
    }

}