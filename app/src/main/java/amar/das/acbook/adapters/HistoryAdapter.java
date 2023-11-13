package amar.das.acbook.adapters;

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


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import amar.das.acbook.Database;
import amar.das.acbook.R;

import amar.das.acbook.activity.IndividualPersonDetailActivity;
import amar.das.acbook.model.HistoryModel;
import amar.das.acbook.utility.MyUtility;


public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    Context context;
    ArrayList<HistoryModel> dataList;//because more operation is retrieving

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
        if (data.getIsDeposit()){
            holder.wagesOrDeposit.setTextColor(context.getColor(R.color.green));
        } else {
            holder.wagesOrDeposit.setTextColor(Color.BLACK);
        }
        holder.wagesOrDeposit.setText(data.getWagesOrDeposit()+ "");//we want to show 0 so no if condition

        holder.p1Skill.setText((data.getP1Skill()!=null)?data.getP1Skill():null);
        holder.p1Work.setText((data.getP1Work()!=0)?data.getP1Work()+"":null);
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
        if(getMicPathFromDb(data.getId(),data.getSystemTimeDate()) !=null) {//if only audio  is present then set min icon to green
            holder.spinnerRemarksAudioIcon.setBackgroundResource(R.drawable.ic_green_sharp_mic_20);
        }else {
            holder.spinnerRemarksAudioIcon.setBackgroundResource(R.drawable.black_sharp_mic_24);//if no audio  present then set mic icon to white
        }
            String[] audioAndRemarks = context.getResources().getStringArray(R.array.audioRemarksShare);
            holder.spinnerRemarksAudioIcon.setAdapter(new ArrayAdapter<>(context, android.R.layout.select_dialog_item, new String[]{audioAndRemarks[0],audioAndRemarks[1],audioAndRemarks[2]}));//adapter set
        holder.spinnerRemarksAudioIcon.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {//this will only execute when there is data
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                MyUtility.spinnerAudioRemarksShare(adapterView,pos,view,getMicPathFromDb(data.getId(),data.getSystemTimeDate()),data.getRemarks(),context);
                //after selecting second time remarks data is not shown so 0 is set so that when second time click it will show data
                // int initialPosition = holder.spinnerDescAudioIcon.getSelectedItemPosition();
                holder.spinnerRemarksAudioIcon.setSelection(0, false);//clearing auto selected or if we remove this line then only one time we would be able to select audio and remarks which we don't want
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
        //*************************************done audio and mic*********************************************************


        holder.share.setOnClickListener(view -> {

        });
        holder.itemView.setOnLongClickListener(view -> {
            Intent intent=new Intent(context, IndividualPersonDetailActivity.class);
            intent.putExtra("ID",data.getId());
            context.startActivity(intent);
            //((Activity)context).finish();//syntax to destroy activity from adapter
            return false;
        });
    }
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
       TextView name,p1Skill,p2Skill,p3Skill,p4Skill,p1Work,p2Work,p3Work,p4Work, userDate,wagesOrDeposit,share;
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
            share=itemView.findViewById(R.id.history_share_tv);

        }
    }
}
