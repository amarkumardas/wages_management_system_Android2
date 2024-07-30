package amar.das.labourmistri.progressdialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import amar.das.labourmistri.R;

public class ProgressDialogHelper {
    private AlertDialog progressDialog;
    private ProgressBar progressBar;

    private Context context;

    public ProgressDialogHelper(Context context) {
        this.context = context;
    }

    public void showProgressBar() {
        if (progressDialog == null) {
            // Inflate the layout
            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogLayout = inflater.inflate(R.layout.progress_bar_layout, null);

            // Find the ProgressBar in the layout
            progressBar = dialogLayout.findViewById(R.id.progressBar);

            // Create a dialog with the custom layout
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(dialogLayout);
            builder.setCancelable(false); // Set to true if you want the user to be able to cancel it

            // Show the dialog
            progressDialog = builder.create();
            progressDialog.show();
        }
    }

    public void hideProgressBar() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}

