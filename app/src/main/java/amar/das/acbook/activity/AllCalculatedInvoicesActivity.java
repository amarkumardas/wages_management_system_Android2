package amar.das.acbook.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;

import amar.das.acbook.R;
import amar.das.acbook.ui.search.SearchFragment;

public class AllCalculatedInvoicesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_calculated_invoices);

    }
    public void invoice_layout_go_back(View view){
        finish();//first destroy current activity then go back
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.insert_detailsof_l_m_g, new SearchFragment()).commit();
    }
}