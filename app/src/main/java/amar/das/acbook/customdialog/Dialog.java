package amar.das.acbook.customdialog;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import amar.das.acbook.R;
import amar.das.acbook.activity.IndividualPersonDetailActivity;

public class Dialog {
    Context context;

    public Dialog(Context context){
        this.context=context;
    }
    public boolean openUpdateRatesDialog(boolean setCancelable){
        AlertDialog.Builder myCustomDialog=new AlertDialog.Builder( context);
        LayoutInflater inflater=LayoutInflater.from(context);

        View myView=inflater.inflate(R.layout.update_rates,null);//myView contain all layout view ids
        myCustomDialog.setView(myView);//set custom layout to alert dialog
        myCustomDialog.setCancelable(setCancelable);//if false user touch to other place then dialog will not be close

        final AlertDialog dialog=myCustomDialog.create();//myCustomDialog variable cannot be use in inner class so creating another final variable  to use in inner class
        TextView hardcodedP1Tv=myView.findViewById(R.id.hardcoded_p1_tv_rate);//don't remove
        EditText inputP1Et=myView.findViewById(R.id.input_p1_et_rate);
        TextView hardcodedP2Tv=myView.findViewById(R.id.hardcoded_p2_tv_rate);
        EditText inputP2Et=myView.findViewById(R.id.input_p2_et_rate);
        TextView hardcodedP3Tv=myView.findViewById(R.id.hardcoded_p3_tv_rate);
        EditText inputP3Et=myView.findViewById(R.id.input_p3_et_rate);
        TextView hardcodedP4Tv=myView.findViewById(R.id.hardcoded_p4_tv_rate);
        EditText inputP4Et=myView.findViewById(R.id.input_p4_et_rate);

        Button infoSave=myView.findViewById(R.id.save_btn_rate);
        Button  cancel=myView.findViewById(R.id.cancel_btn_rate);
        cancel.setOnClickListener(view12 -> dialog.dismiss());

        inputP1Et.setEnabled(false);
        inputP2Et.setEnabled(false);
        inputP3Et.setEnabled(false);
        inputP4Et.setEnabled(false);

        dialog.show();
        return true;
    }
}
