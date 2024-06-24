package amar.das.acbook.adapters;

import android.Manifest;
import android.app.Activity;
import android.content.Context;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import amar.das.acbook.Database;
import amar.das.acbook.R;

import amar.das.acbook.activity.IndividualPersonDetailActivity;

import amar.das.acbook.model.HistoryModel;
import amar.das.acbook.ui.history.HistoryFragment;
import amar.das.acbook.utility.MyUtility;


public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    Context context;
    ArrayList<HistoryModel> dataList;//because more operation is retrieving
   // String updatedStatus="3";
    public HistoryAdapter(Context context, ArrayList<HistoryModel> arrayList){
        this.context =context;
        this.dataList =arrayList;
    }
    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
        View view=inflater.inflate(R.layout.history_single_row,parent,false);
        return new HistoryAdapter.ViewHolder(view);//constructor  public ViewHolder(@NonNull View itemView)
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryModel data= dataList.get(position);
        holder.name.setText(data.getName());
        holder.userDate.setText(data.getUserDate());
        holder.time.setText(MyUtility.getTime12hrFromSystemDateTime(data.getSystemTimeDate()));

        if(data.isShared()){
            holder.shareIcon.setBackgroundResource(R.drawable.ic_green_sharp_done_sharp_tick_20);
        }else{
            String phoneNumber=MyUtility.getActiveOrBothPhoneNumber(data.getId(),context,true);
            if(phoneNumber!=null){//if there is phone number
                holder.shareIcon.setBackgroundResource(R.drawable.baseline_whatsapp_24);
            }else{//if no phone number
                holder.shareIcon.setBackgroundResource(R.drawable.baseline_phone_disabled_24);
            }
        }

        if(data.getStatus().equals(HistoryFragment.previousRecordUpdated)){//if user update previous day amount then show the information so make it visible and 3 indicate updated previously
            holder.subtractedAmount.setVisibility(View.VISIBLE);//hide
            int subtractedAdvanceOrBal=(data.getSubtractedAdvanceOrBal() != null)? Integer.parseInt(data.getSubtractedAdvanceOrBal()) : 0;//0 will occur when user update previous record but changes remain same eg. if user update 1 to 1 then her no changes in vaLUE SO I have to set value to 0
            if(subtractedAdvanceOrBal <= 0){//means amount payment minus
                holder.subtractedAmount.setTextColor(context.getColor(R.color.black));
                holder.subtractedAmount.setText(context.getResources().getString(R.string.updated_payment)+" "+MyUtility.convertToIndianNumberSystem(Math.abs(Integer.parseInt(String.valueOf(subtractedAdvanceOrBal)))));
            }else if(subtractedAdvanceOrBal >= 0){//amount received
                holder.subtractedAmount.setText(context.getResources().getString(R.string.updated_amount_received)+" "+MyUtility.convertToIndianNumberSystem(Integer.parseInt(String.valueOf(subtractedAdvanceOrBal))));
                holder.subtractedAmount.setTextColor(context.getColor(R.color.green));
            }
        }else{
            holder.subtractedAmount.setVisibility(View.GONE);//hide
        }

        if (data.getIsDeposit()){
            holder.wagesOrDeposit.setTextColor(context.getColor(R.color.green));
        }else{
            holder.wagesOrDeposit.setTextColor(Color.BLACK);
        }

        if(!data.getStatus().equals(HistoryFragment.automaticInserted)){
            holder.wagesOrDeposit.setText(MyUtility.convertToIndianNumberSystem(data.getWagesOrDeposit()));//we want to show 0 so no if condition
        }else {
            holder.wagesOrDeposit.setTextColor(Color.BLACK);//calculate text should be in black to avoid confusion
            holder.wagesOrDeposit.setText(R.string.calculated);//we want to show 0 so no if condition
        }

        holder.p1Skill.setText((data.getP1Skill()!=null)?data.getP1Skill():null);
        holder.p1Work.setText((data.getP1Work()!=0)?data.getP1Work()+"":null);
        //setting to null so that it don't take default value
        holder.p2Skill.setText(null);
        holder.p2Work.setText(null);
        holder.p3Skill.setText(null);
        holder.p3Work.setText(null);
        holder.p4Skill.setText(null);
        holder.p4Work.setText(null);

        if(data.getP2Skill()!=null){
            holder.p2Skill.setText((data.getP2Skill()!=null)?data.getP2Skill():null);
            holder.p2Work.setText((data.getP2Work()!=0)?data.getP2Work()+"":null);

            if(data.getP3Skill()!=null){
                holder.p3Skill.setText((data.getP3Skill()!=null)?data.getP3Skill():null);
                holder.p3Work.setText((data.getP3Work()!=0)?data.getP3Work()+"":null);

                if (data.getP4Skill()!=null) {
                    holder.p4Skill.setText((data.getP4Skill()!=null)?data.getP4Skill():null);
                    holder.p4Work.setText((data.getP4Work()!=0)?data.getP4Work()+"":null);
                }
            }
        }
        //*************************************Audio and mic*********************************************************
        if(getMicPathFromDb(data.getId(),data.getSystemTimeDate()) !=null){//if only audio  is present then set min icon to green
            holder.spinnerRemarksAudioIcon.setBackgroundResource(R.drawable.ic_green_sharp_mic_20);
        }else{
            holder.spinnerRemarksAudioIcon.setBackgroundResource(R.drawable.black_sharp_mic_24);//if no audio  present then set mic icon to white
        }
            String[] audioAndRemarks = context.getResources().getStringArray(R.array.audioRemarksShare);
            holder.spinnerRemarksAudioIcon.setAdapter(new ArrayAdapter<>(context, android.R.layout.select_dialog_item, new String[]{audioAndRemarks[0],audioAndRemarks[1],audioAndRemarks[2]}));//adapter set
        holder.spinnerRemarksAudioIcon.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){//this will only execute when there is data
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                MyUtility.spinnerAudioRemarksShare(adapterView,pos,view,getMicPathFromDb(data.getId(),data.getSystemTimeDate()),data.getRemarks(),context,null);
                //after selecting second time remarks data is not shown so 0 is set so that when second time click it will show data
                // int initialPosition = holder.spinnerDescAudioIcon.getSelectedItemPosition();
                holder.spinnerRemarksAudioIcon.setSelection(0, false);//clearing auto selected or if we remove this line then only one time we would be able to select audio and remarks which we don't want
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
        //*************************************done audio and mic*********************************************************

        holder.shareIcon.setOnClickListener(view -> {
            if(!MyUtility.checkPermissionForReadAndWriteToExternalStorage(view.getContext())) {
                Toast.makeText(view.getContext(), "EXTERNAL STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show();
                //to read and write own app specific directory from minsdk 29 to 33+ we don't require READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE due to scope storage after android 10
                //ActivityCompat.requestPermissions((Activity) view.getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 41);
                return;
            }
           if(HistoryFragment.shareingToggle){//send to phone number if no then open any app
               //if(MyUtility.sendMessageToContact(data.getId(), generateRecordeMessageToSend(data),context)){//if false then open any app
               if(MyUtility.sendMessageToContact(data.getId(), MyUtility.generateRecordMessageToSend(data.getId(),data.getUserDate(),data.getWagesOrDeposit(),data.getIsDeposit(),data.getP1Skill(),data.getP1Work(),data.getP2Skill(),data.getP2Work(),data.getP3Skill(),data.getP3Work(),data.getP4Skill(),data.getP4Work(),data.getRemarks()),context)){
                   try(Database db=Database.getInstance(context)){//update table as shared.if data send to contact number or whatsapp
                        db.updateAsSharedToHistory(data.getId(),data.getSystemTimeDate() );
                   }catch (Exception x){
                       x.printStackTrace();
                   }
               }else{
                   MyUtility.shareShortMessageToAnyApp(MyUtility.generateRecordMessageToSend(data.getId(),data.getUserDate(),data.getWagesOrDeposit(),data.getIsDeposit(),data.getP1Skill(),data.getP1Work(),data.getP2Skill(),data.getP2Work(),data.getP3Skill(),data.getP3Work(),data.getP4Skill(),data.getP4Work(),data.getRemarks()),context);//open any app
               }
           }else{//send to whatsapp.if no contact then open any app
               String phoneNumber=MyUtility.getActiveOrBothPhoneNumber(data.getId(),context,true);
               if(phoneNumber!=null){
                   if (MyUtility.shareMessageDirectlyToWhatsApp(MyUtility.generateRecordMessageToSend(data.getId(),data.getUserDate(),data.getWagesOrDeposit(),data.getIsDeposit(),data.getP1Skill(),data.getP1Work(),data.getP2Skill(),data.getP2Work(),data.getP3Skill(),data.getP3Work(),data.getP4Skill(),data.getP4Work(),data.getRemarks()), phoneNumber, context)){//if false then open any app
                       try(Database db=Database.getInstance(context)){//update table as shared.if data send to contact number or whatsapp
                           db.updateAsSharedToHistory(data.getId(),data.getSystemTimeDate() );
                       }catch (Exception x){
                           x.printStackTrace();
                       }
                   }else{
                       MyUtility.shareShortMessageToAnyApp(MyUtility.generateRecordMessageToSend(data.getId(),data.getUserDate(),data.getWagesOrDeposit(),data.getIsDeposit(),data.getP1Skill(),data.getP1Work(),data.getP2Skill(),data.getP2Work(),data.getP3Skill(),data.getP3Work(),data.getP4Skill(),data.getP4Work(),data.getRemarks()),context);//open any app
                   }
               }else{
                   Toast.makeText(context,context.getResources().getString(R.string.no_phone_number), Toast.LENGTH_LONG).show();//snack-bar not using because its get hide
                   MyUtility.shareShortMessageToAnyApp(MyUtility.generateRecordMessageToSend(data.getId(),data.getUserDate(),data.getWagesOrDeposit(),data.getIsDeposit(),data.getP1Skill(),data.getP1Work(),data.getP2Skill(),data.getP2Work(),data.getP3Skill(),data.getP3Work(),data.getP4Skill(),data.getP4Work(),data.getRemarks()),context);//open any app
               }
           }
        });
        holder.itemView.setOnLongClickListener(view -> {
            Intent intent=new Intent(context, IndividualPersonDetailActivity.class);
            intent.putExtra("ID",data.getId());
            context.startActivity(intent);
            //((Activity)context).finish();//syntax to destroy activity from adapter
            return false;
        });
    }
//    public boolean shareShortMessageToAnyApp(String message){
//        if(message==null) {
//            return false;
//        }
//        try {
//            Intent shareIntent = new Intent(Intent.ACTION_SEND);
//            shareIntent.setType("text/plain");
//            shareIntent.putExtra(Intent.EXTRA_TEXT, message);
//            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//If we don't add the chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) line to set the FLAG_ACTIVITY_NEW_TASK flag, the behavior of the app when launching the chooser intent may depend on the context in which the sendMessageToAnyApp method is called.If the method is called from an activity that is already the root of a task, launching the chooser without the FLAG_ACTIVITY_NEW_TASK flag will simply add the chosen activity to the current task stack. This can lead to unexpected back stack behavior and may not be desirable if the user is expected to return to the same activity after sharing the message.On the other hand, if the method is called from an activity that is not the root of a task, launching the chooser without the FLAG_ACTIVITY_NEW_TASK flag will create a new task for the chooser and clear the previous task. This can also be unexpected and disruptive to the user's workflow.Therefore, setting the FLAG_ACTIVITY_NEW_TASK flag ensures consistent behavior regardless of the context in which the method is called, and is generally a good practice when launching chooser intents from an app
//            context.startActivity(Intent.createChooser(shareIntent, context.getResources().getString(R.string.share_message_using)));//startActivity launch activity without expecting any result back SO we don't need any result back so using start activity
//            return true;
//        }catch (Exception ex){
//            ex.printStackTrace();
//            return false;
//        }
//    }
    private String getMicPathFromDb(String id,String systemDateTime){//String id,String systemDateTime this two variables act as primary key
        try(Database db=Database.getInstance(context)){
         return db.getMicPath(id,systemDateTime);
        }catch (Exception x){
            x.printStackTrace();
            return null;
        }
    }
    @Override
    public int getItemCount() {
        return dataList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder{//this class will hold only references of view
       TextView name,p1Skill,p2Skill,p3Skill,p4Skill,p1Work,p2Work,p3Work,p4Work, userDate,wagesOrDeposit, shareIcon,subtractedAmount,time;
       Spinner spinnerRemarksAudioIcon;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name =itemView.findViewById(R.id.history_name_tv);
            p1Skill =itemView.findViewById(R.id.history_p1_skill_tv);
            p2Skill =itemView.findViewById(R.id.history_p2_skill_tv);
            p3Skill =itemView.findViewById(R.id.history_p3_skill_tv);
            p4Skill =itemView.findViewById(R.id.history_p4_skill_tv);
            p1Work =itemView.findViewById(R.id.history_p1_tv);
            p2Work =itemView.findViewById(R.id.history_p2_tv);
            p3Work =itemView.findViewById(R.id.history_p3_tv);
            p4Work =itemView.findViewById(R.id.history_p4_tv);
            userDate =itemView.findViewById(R.id.history_user_date_tv);
            wagesOrDeposit=itemView.findViewById(R.id.history_wages_or_deposit_tv);
            spinnerRemarksAudioIcon =itemView.findViewById(R.id.history_mic_spinner);
            shareIcon =itemView.findViewById(R.id.history_share_tv);
            subtractedAmount=itemView.findViewById(R.id.history_updated_amount_tv);
            time=itemView.findViewById(R.id.history_time);
        }
    }
}
