package amar.das.labourmistri.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;



import java.io.BufferedReader;
import java.io.File;

import java.io.FileReader;

import java.util.ArrayList;

import java.util.LinkedList;
import java.util.List;

import amar.das.labourmistri.R;

import amar.das.labourmistri.model.TextFileModel;
import amar.das.labourmistri.utility.MyUtility;

public class BackUpCalculatedTextFileAdapter extends RecyclerView.Adapter<BackUpCalculatedTextFileAdapter.ViewHolder> implements Filterable {
    Context context;
    LinkedList<TextFileModel> dataList;//LinkedList because more operation is deleting
    LinkedList<TextFileModel> backupData;//this data will not be changed
    boolean isActionModeEnable =false;
    boolean isSelectAll=false;

    private ActionMode actionMode;
    ArrayList<TextFileModel> userSelectedFiles =new ArrayList<>();

    private RecyclerViewListener listener;

    public interface RecyclerViewListener {//callback
          void updatedCount(int count);
          void hideSearchBar(boolean trueForHide);
    }
    public void RecyclerViewListener(RecyclerViewListener listener){//callback
        this.listener=listener;
    }
    public BackUpCalculatedTextFileAdapter(Context context, LinkedList<TextFileModel> list){
        this.context =context;
        this.dataList =list;
        backupData =new LinkedList<>(dataList);//storing object to backup for search view
    }
    @NonNull
    @Override
    public BackUpCalculatedTextFileAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
        View view=inflater.inflate(R.layout.text_file_single_row,parent,false);
        return new BackUpCalculatedTextFileAdapter.ViewHolder(view);//constructor  public ViewHolder(@NonNull View itemView)
    }

    @Override
    public void onBindViewHolder(@NonNull BackUpCalculatedTextFileAdapter.ViewHolder holder, int position) {//to fill data on every view filed
       TextFileModel data= dataList.get(position);
       holder.fileName.setText(data.getFileName());

//       holder.shareTextFileImg.setOnClickListener(view -> {
//           ArrayList<File> textFile=new ArrayList<>();
//           textFile.add(new File(data.getAbsolutePath()));
//           if(!shareMultipleFilesToAnyApp(textFile, "text/plain", "SHARE INVOICE USING")){
//               MyUtility.snackBar(holder.itemView,"CANNOT SHARE INVOICE");
//           }
//       });

       holder.itemView.setOnLongClickListener(view -> {//references https://www.youtube.com/watch?v=XhEhfmBJlLY , https://www.youtube.com/watch?v=vPLKNsQEAEc&t=696s
           if(!isActionModeEnable){
             ActionMode.Callback callback=new ActionMode.Callback() { //when action mode is not enable //initialize action mode
                  @Override
                   public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                       MenuInflater menuInflater=actionMode.getMenuInflater();
                       menuInflater.inflate(R.menu.contexual_action_mode_menu,menu);   //inflate menu
                       return true;
                   }
                   @Override
                   public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {//when it is prepare recycler view will create new view
                       isActionModeEnable =true;  //when action mode is prepare set isActionModeEnable true so that else statement will execute
                       userClickOnItem(holder);
                       return false;
                   }
                   @Override
                   public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {//when click on action mode item
                       int id= menuItem.getItemId();  //Get item id
                       if(id==R.id.menu_share){
                           if(userSelectedFiles.size() != 0) {
                               if(!shareMultipleFilesToAnyApp(getUserSelectedFiles(userSelectedFiles), "text/plain", "SHARE "+userSelectedFiles.size()+" INVOICE USING")){
                                   MyUtility.snackBar(view,"CANNOT SHARE INVOICE");
                               }
                           }else {
                               MyUtility.snackBar(view,"SELECT INVOICE");
                           }
                           //don't update the adapter here because when user return back after sharing file then in activity user can see selected file.so this can help to delete selected shared file
                       } else if (id == R.id.menu_delete) {
                           if(userSelectedFiles.size() > 0){
                               AlertDialog.Builder detailsReview = new AlertDialog.Builder(context);
                               detailsReview.setCancelable(true);
                               detailsReview.setTitle(context.getResources().getString(R.string.delete)+" PERMANENTLY");// Html tags video- https://www.youtube.com/watch?v=98BD6IjQQkE
                               detailsReview.setMessage(userSelectedFiles.size()+" INVOICE");

                               detailsReview.setNegativeButton(context.getResources().getString(R.string.cancel), (dialogInterface, i) -> {
                                   dialogInterface.dismiss();
                                   actionMode.finish();//FINISH ACTION MODE
                               });
                               detailsReview.setPositiveButton(context.getResources().getString(R.string.delete), (dialogInterface, i) -> {
                                   for (TextFileModel object: userSelectedFiles) {
                                       if(MyUtility.deletePdfOrRecordingUsingPathFromAppStorage(object.getAbsolutePath())){//deleting files
                                           dataList.remove(object);//remove selected object from the adapter one by one because when 1 file is not deleted then that file will not be removed from datalist
                                           backupData.remove(object);//backupData also contain object so removed
                                       }
                                   }
                                   MyUtility.snackBar(view, userSelectedFiles.size()+" DELETED");//this should be call here because when all files deleted then this line produce error
                                   userSelectedFiles.clear();//clear user selected files
                                   notifyDataSetChanged();//notify adapter
                                   dialogInterface.dismiss();
                                   listener.updatedCount(backupData.size());//callback sending this updated data activity
                                   actionMode.finish();//FINISH ACTION MODE
                               });
                               detailsReview.create().show();
                           }else if(userSelectedFiles.size() == 0){//when data list is empty visible text view
                               MyUtility.snackBar(view,"SELECT INVOICE");
                           }
                       } else if (id == R.id.menu_select_all) {
                           if(userSelectedFiles.size() == dataList.size()){//when user manually selected all item and then clicked on select all icon
                               isSelectAll=false;   //when all item selected
                               userSelectedFiles.clear();
                           }else{
                               isSelectAll=true; //when all item unselect
                               userSelectedFiles.clear(); //clear selected item
                               userSelectedFiles.addAll(dataList);
                           }

                           if(actionMode != null){
                               actionMode.setTitle(userSelectedFiles.size()+" SELECTED");//set text on view model
                           }
                           notifyDataSetChanged();//notify adapter
                       }
                       return true;
                   }
                   @Override
                   public void onDestroyActionMode(ActionMode actionMode) {//when action mode is destroy
                       listener.hideSearchBar(false);//callback sending this updated data activity
                       isActionModeEnable =false;
                       isSelectAll=false;
                       userSelectedFiles.clear(); //clear user selected list
                       notifyDataSetChanged();//Notify adapter
                   }
               };

               actionMode=((AppCompatActivity)view.getContext()).startActionMode(callback);//initialzing
               actionMode.setTitle(userSelectedFiles.size() + " SELECTED");//when at first user select item then due to action mode not initialize 1 selected is not shown as variable action mode is null so initializing here manually
              // holder.shareTextFileImg.setVisibility(View.GONE);//when at first user select item then hide share button

               listener.hideSearchBar(true);//callback sending this updated data activity
           }else{
               userClickOnItem(holder);//when action mode is already created.call this method as user has selected some item
           }
           return true;
       });

      holder.itemView.setOnClickListener(view -> {
        if(isActionModeEnable){
         // holder.shareTextFileImg.setVisibility(View.GONE);//when item view is clicked then hide share button
          userClickOnItem(holder); //when action mode is enable call this method to add user selected item

       }else{//when action mode is not enable then this will execute
            androidx.appcompat.app.AlertDialog.Builder myCustomDialog=new androidx.appcompat.app.AlertDialog.Builder( context);
            LayoutInflater inflater=LayoutInflater.from(context);

            View myView=inflater.inflate(R.layout.text_file_viewer,null);//myView contain all layout view ids
            myCustomDialog.setView(myView);//set custom layout to alert dialog
            myCustomDialog.setCancelable(true);//if user touch to other place then dialog will not be close

             final androidx.appcompat.app.AlertDialog customDialog=myCustomDialog.create();//myCustomDialog variable cannot be use in inner class so creating another final variable  to use in inner class
             TextView textFileViewerTv=myView.findViewById(R.id.textViewer);
            // WebView  textFileWebView=myView.findViewById(R.id.text_file_webView);
             TextView textFileNameTv=myView.findViewById(R.id.file_name);
             Button textFileShare= myView.findViewById(R.id.text_file_share);
             Button textFileCancel= myView.findViewById(R.id.text_file_cancel_btn);
             textFileCancel.setOnClickListener(view1 -> customDialog.dismiss());

            File file=new File(data.getAbsolutePath());
            textFileNameTv.setText(context.getResources().getString(R.string.calculated_invoice)+"\n"+data.getFileName());
            viewTextFile(file,textFileViewerTv);

            //textFileWebView.setVisibility(View.VISIBLE);
            // Handle URL loading within the WebView
           // textFileWebView.setWebViewClient(new WebViewClient());
            // Enable JavaScript (if needed)
//            textFileWebView.getSettings().setJavaScriptEnabled(true);
//            textFileWebView.loadData(convertFileToStringData(file), "text/html", "UTF-8");
//            textFileWebView.setWebChromeClient(new WebChromeClient(){
//                @Override
//                public void onProgressChanged(WebView view1,int newProgress){
//                    super.onProgressChanged(view1,newProgress);
//                }
//                @Override
//                public void onReceivedTitle(WebView view, String title) {
//                    super.onReceivedTitle(view, title);
//                }
//
//            });


             textFileShare.setOnClickListener(view12 -> {
                 ArrayList<File> textFile=new ArrayList<>();
                 textFile.add(file);
                 if(!shareMultipleFilesToAnyApp(textFile, "text/plain", "SHARE INVOICE USING")){
                     MyUtility.snackBar(view12,"CANNOT SHARE INVOICE");
                 }
             });
            customDialog.show();
        }
      });

//        if(isActionModeEnable){//this execute when select all button is click or when notifyDataSetIsChanget() is called. because this action created new view so new view is updated
//            holder.shareTextFileImg.setVisibility(View.GONE);
//        }else{
//            holder.shareTextFileImg.setVisibility(View.VISIBLE);
//        }

  if(isSelectAll){//when all value selected visible all check box image
      holder.ivCheckBox.setVisibility(View.VISIBLE);
      holder.itemView.setBackgroundColor(Color.LTGRAY);  //set background color
  }else{ //when all value unselected hide all check box image
      holder.ivCheckBox.setVisibility(View.GONE);
      holder.itemView.setBackgroundColor(Color.WHITE); //set background color
  }
    }
//    private String convertFileToStringData(File file) {//return null when error or if file not present
//        if (file.exists() && file.isFile()) {
//            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
//                StringBuilder stringBuilder = new StringBuilder();
//                String line;
//                while ((line = bufferedReader.readLine()) != null) {
//                    stringBuilder.append(line).append("\n");
//                }
//                 return stringBuilder.toString(); //Converted file content to a data
//            } catch (Exception e) {
//                e.printStackTrace();
//                return null;
//            }
//        }else{
//            return null;
//        }
//    }

    private boolean viewTextFile(File textFile,TextView textFileViewer){
        if (textFile.exists()){
            try (BufferedReader br = new BufferedReader(new FileReader(textFile))) {
                StringBuilder text = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    text.append(line).append('\n');
                }
                textFileViewer.setText(text.toString());
            }catch (Exception e) {
                e.printStackTrace();
                textFileViewer.setText("FAILED TO DISPLAY INVOICE");
                return false;
            }
        }else {
            textFileViewer.setText("INVOICE NOT FOUND");
            return false;
        }
        return true;
    }

    private ArrayList<File> getUserSelectedFiles(ArrayList<TextFileModel> userSelectFiles) {//if error return null
        try {
            ArrayList<File> result=new ArrayList<>();
            for (TextFileModel object: userSelectFiles) {
                File textFile=new File(object.getAbsolutePath());
                result.add(textFile);
            }
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public boolean shareMultipleFilesToAnyApp(List<File> files, String mimeType, String title) {
        if (files == null || files.isEmpty() || mimeType==null || mimeType.isEmpty()) {
            return false;
        }
        try {
            //error in android 12
//            ArrayList<Uri> uris = new ArrayList<>();
//
//            for (File file : files) {
//                Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
//                uris.add(uri);
//            }
//
//            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
//            intent.setType(mimeType);
//            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris); // for multiple files
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // add this line
//
//            Intent chooser = Intent.createChooser(intent, title);
//            chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(chooser);

            ShareCompat.IntentBuilder shareIntent = new ShareCompat.IntentBuilder(context)//ShareCompat.IntentBuilder is a helper for constructing sharing intents.
                    .setType(mimeType);
            Uri uri=null;
            for (File file : files) {
                try {
                    uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
                    shareIntent.addStream(uri);
                }catch (Exception x){
                    x.printStackTrace();
                    Toast.makeText(context, "Error in creating uri", Toast.LENGTH_SHORT).show();
                }
            }
            shareIntent.getIntent()
                    .setAction(Intent.ACTION_SEND_MULTIPLE)//is used for sharing multiple pieces of content..setAction(Intent.ACTION_SEND) is used for sharing a single piece of content.
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//rants temporary read permission to the recipient of the intent for the content URIs in the intent.

            Intent chooser = Intent.createChooser(shareIntent.getIntent(), title);
           //This code queries the package manager for activities that can handle the chooser intent.It then iterates over the list of ResolveInfo objects, each representing an app that can handle the intent.grantUriPermission grants temporary read and write permissions to each app for the last URI added to the intent.
            List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                //String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(resolveInfo.activityInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
           context.startActivity(chooser);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private void userClickOnItem(ViewHolder holder) {

        TextFileModel selectedObject=dataList.get(holder.getAdapterPosition()); //get selected item value

        if(holder.ivCheckBox.getVisibility() == View.GONE){//if check box is not visible means add item to userselected list
            holder.ivCheckBox.setVisibility(View.VISIBLE); //when item not selected //Visible checkbox image
            holder.itemView.setBackgroundColor(Color.LTGRAY);//set back ground coloritemView is one row of recycler view
            userSelectedFiles.add(selectedObject);//addding item to userSelected List
        }else{//when item de selected .hide check box image
           holder.ivCheckBox.setVisibility(View.GONE);
           holder.itemView.setBackgroundColor(Color.WHITE);
           userSelectedFiles.remove(selectedObject);//Remove value from select array list
        }
        if(actionMode != null) {
            actionMode.setTitle(userSelectedFiles.size() + " SELECTED"); //set text on action mode
        }
    }
    @Override
    public int getItemCount() {
        return dataList.size();
    }
    @Override
    public Filter getFilter() {
        return filter;//we have to create anonymous filter class
    }
    Filter filter=new Filter() {//it works background can be called as child thread
        @Override
        protected FilterResults performFiltering(CharSequence keyword){
          ArrayList<TextFileModel> filterData=new ArrayList<>();
            if(keyword.toString().isEmpty()){//if in search box no keywords then show all files
               filterData.addAll(backupData);
            }else {
                for (TextFileModel obj: backupData) {
                      if(obj.getFileName().toLowerCase().contains(keyword.toString().toLowerCase())){//if keyword matches then add it to filter data
                          filterData.add(obj);
                      }
                }
            }
            FilterResults filterResults=new FilterResults();
            filterResults.values=filterData;
            return filterResults;
        }

        @Override//view on main UI thread
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
//            dataList.clear();//clearing to show new filter search result
//            dataList.addAll( (ArrayList<TextFileModel>) filterResults.values);//typecasting to arraylist because
//            notifyDataSetChanged();//when background child thread searching is completed then this method will notify to main UI thread that search is completed

            dataList.clear(); // Clearing to show new filter search result
            ArrayList<TextFileModel> filteredList = (ArrayList<TextFileModel>) filterResults.values;
//            if (filteredList.isEmpty()) {//display message if not found
//                Toast.makeText(context,context.getResources().getString(R.string.not_found), Toast.LENGTH_SHORT).show();
//            }else {
//                dataList.addAll(filteredList);
//            }
            dataList.addAll(filteredList);

            notifyDataSetChanged();
        }};//anonymous filter class
    public class ViewHolder extends RecyclerView.ViewHolder{//this class will hold only references of view

        TextView fileName;
        ImageView ivCheckBox;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName =itemView.findViewById(R.id.text_file_name_tv);
            ivCheckBox=itemView.findViewById(R.id.iv_check_box);
        }
    }
}
