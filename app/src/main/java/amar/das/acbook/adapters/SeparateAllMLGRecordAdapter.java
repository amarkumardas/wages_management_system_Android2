package amar.das.acbook.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import amar.das.acbook.Database;
import amar.das.acbook.R;
import amar.das.acbook.activity.IndividualPersonDetailActivity;
import amar.das.acbook.globalenum.GlobalConstants;
import amar.das.acbook.model.MLGAllRecordModel;
import amar.das.acbook.utility.MyUtility;

public class SeparateAllMLGRecordAdapter extends RecyclerView.Adapter<SeparateAllMLGRecordAdapter.ViewHolder> {

    Context context;
    ArrayList<MLGAllRecordModel> arrayList;//because more operation is retrieving
    //for date***********************
    String []dateArray;
   // int d,m,y;
    LocalDate dbLatestDate, todayDate =LocalDate.now();
    Database db;
   //array lis has data name id and active
    public SeparateAllMLGRecordAdapter(Context context, ArrayList<MLGAllRecordModel> data){
        this.arrayList=data;
        this.context=context;
//        db=new Database(context);//we cant give this at class level
        db=Database.getInstance(context);//we cant give this at class level
    }

    @NonNull
    @Override
    public SeparateAllMLGRecordAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.l_m_g_singlerecord,parent,false);
         return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeparateAllMLGRecordAdapter.ViewHolder holder, int position) {
        MLGAllRecordModel data=arrayList.get(position);
        holder.name.setText(data.getName());
        holder.inactiveDuration.setText(GlobalConstants.ACTIVE.name());//when user is active then it show active
        if(!data.isActive()) { //if account is not active then view will be in red color which indicate inactive
           // holder.name.setTextColor(Color.RED);
           //if they are not active then only it will show months
            if(data.getLatestDate() !=null) {//https://www.youtube.com/watch?v=VmhcvoenUl0
                dateArray = data.getLatestDate().split("-");
//                d = Integer.parseInt(dateArray[0]);
//                m = Integer.parseInt(dateArray[1]);
//                y = Integer.parseInt(dateArray[2]);
                dbLatestDate = LocalDate.of(Integer.parseInt(dateArray[2]), Integer.parseInt(dateArray[1]), Integer.parseInt(dateArray[0]));//it convert 01.05.2022 it add 0 automatically
                holder.inactiveDuration.setText(""+ChronoUnit.MONTHS.between(dbLatestDate, todayDate)+" MONTHS");
            }
            holder.inactiveDuration.setTextColor(Color.RED);
        }else {
            holder.inactiveDuration.setTextColor(Color.BLACK);
          // holder.name.setTextColor(Color.BLACK);
        }

        holder.layout.setOnClickListener(view -> {//WE have to send id
            Intent intent=new Intent(context, IndividualPersonDetailActivity.class);
            intent.putExtra("ID",data.getId());
            context.startActivity(intent);
           // ((Activity)context).finish();//syntax to destroy activity from adapter
            //***********leaving date updating if days is 0 between two date then update SET LEAVINGDATE="+null+
            if(!MyUtility.updateLeavingDate(data.getId(),context,todayDate)){//update leaving date
                Toast.makeText(context, "LEAVING DATE NOT UPDATED", Toast.LENGTH_LONG).show();
            }

//            Cursor cursor2 = db.getData("SELECT "+Database.COL_392_LEAVINGDATE+" FROM " + Database.TABLE_NAME3 + " WHERE "+Database.COL_31_ID+"='" + data.getId() + "'");
//            cursor2.moveToFirst();
//            if(cursor2.getString(0) != null){
//                dateArray = cursor2.getString(0).split("-");
////                    d = Integer.parseInt(dateArray[0]);
////                    m = Integer.parseInt(dateArray[1]);
////                    y = Integer.parseInt(dateArray[2]);//dbDate is leaving date
//                dbLatestDate = LocalDate.of(Integer.parseInt(dateArray[2]),Integer.parseInt(dateArray[1]),Integer.parseInt(dateArray[0]));//it convert 01.05.2022 it add 0 automatically
//                if(ChronoUnit.DAYS.between(dbLatestDate, todayDate) >= 0){//if days between leaving date and today date is 0 then leaving date will set null automatically
//                    db.updateTable("UPDATE " + Database.TABLE_NAME3 + " SET "+Database.COL_392_LEAVINGDATE+"="+null+" WHERE "+Database.COL_31_ID+"='" + data.getId() + "'");
//                }
//            }
////                cursor2 = db.getData("SELECT LEAVINGDATE FROM " + db.TABLE_NAME3 + " WHERE ID='" + data.getId() + "'");
////                cursor2.moveToFirst();
////                if(cursor2.getString(0) != null){//https://www.youtube.com/watch?v=VmhcvoenUl0
////                    dateArray = cursor2.getString(0).split("-");
//////                    d = Integer.parseInt(dateArray[0]);
//////                    m = Integer.parseInt(dateArray[1]);
//////                    y = Integer.parseInt(dateArray[2]);//dbDate is leaving date
////                    dbLatestDate = LocalDate.of(Integer.parseInt(dateArray[2]),Integer.parseInt(dateArray[1]),Integer.parseInt(dateArray[0]));//it convert 01.05.2022 it add 0 automatically
////                    Toast.makeText(context, ""+ChronoUnit.DAYS.between(currentDate,dbLatestDate)+" DAYS LEFT TO LEAVE", Toast.LENGTH_SHORT).show();//HERE dbDate will always be higher then todayDate because user will leave in forward date so in method chronounit todayDate is written first and second dbDate to get right days
////                }
//            cursor2.close();
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, inactiveDuration;
        LinearLayout layout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.name_tv);
            inactiveDuration =itemView.findViewById(R.id.inactive_duration_tv);
            layout=itemView.findViewById(R.id.single_record_layout_of_inactive_and_active);
        }
    }
}
