package amar.das.acbook.adapters;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import amar.das.acbook.R;

import amar.das.acbook.model.HistoryModel;


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
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder{//this class will hold only references of view
       TextView name,p1Skill,p2Skill,p3Skill,p4Skill,p1Work,p2Work,p3Work,p4Work,date,wagesOrDeposit;
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
            date =itemView.findViewById(R.id.history_date_tv);
            wagesOrDeposit=itemView.findViewById(R.id.history_wages_or_deposit_tv);
            spinnerRemarksAudioIcon=itemView.findViewById(R.id.history_mic_spinner);

        }
    }
}
