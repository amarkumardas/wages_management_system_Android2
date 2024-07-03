package amar.das.acbook.adapters;

import android.content.Context;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.ArrayList;

import amar.das.acbook.Database;
import amar.das.acbook.activity.IndividualPersonDetailActivity;
import amar.das.acbook.globalenum.GlobalConstants;
import amar.das.acbook.model.MestreLaberGModel;
import amar.das.acbook.R;
import amar.das.acbook.sharedpreferences.SharedPreferencesHelper;
import amar.das.acbook.utility.MyUtility;

public class MestreLaberGAdapter extends RecyclerView.Adapter<MestreLaberGAdapter.ViewHolder> {
    Context context;
    ArrayList<MestreLaberGModel> arrayList;//because more operation is retrieving
    //final Calendar current=Calendar.getInstance();//to get current date
    // String currentDate =current.get(Calendar.DAY_OF_MONTH)+"-"+(current.get(Calendar.MONTH)+1)+"-"+current.get(Calendar.YEAR);
    //String []dateArray;
    byte daysToBecomeInactive= Byte.parseByte(GlobalConstants.TWO_WEEKS_DEFAULT.getValue());//default value if days inactive then make id inactive
//    LocalDate dbDate, todayDate = LocalDate.now();//current date; return 2022-05-01 added 0 automatically//for performance this variable is declare here
    LocalDate todayDate = LocalDate.now();//current date; return 2022-05-01 added 0 automatically//for performance this variable is declare here

    //String currentDateDBPattern =todayDate.getDayOfMonth()+"-"+ todayDate.getMonthValue()+"-"+ todayDate.getYear();//converted to 1-5-2022 remove 0.//for performance this variable is declare here
    Database db;

    public MestreLaberGAdapter(Context context, ArrayList<MestreLaberGModel> arrayList){
        this.context =context;
        this.arrayList=arrayList;
        this.daysToBecomeInactive=(byte)SharedPreferencesHelper.getInt(context,SharedPreferencesHelper.Keys.INACTIVE_DAYS.name(),daysToBecomeInactive);//if 1 months inactive then make id inactive
        db=Database.getInstance(this.context);
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
        View view=inflater.inflate(R.layout.single_person_profile,parent,false);
        return new ViewHolder(view);//constructor  public ViewHolder(@NonNull View itemView)
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {//to fill data on every view filed
        MestreLaberGModel data=arrayList.get(position);
//        byte[] image=data.getImagePath();//getting image from db
//        if(image!=null) {
//            //getting bytearray image from DB and converting  to bitmap to set in imageview
//            Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
//            holder.profileImg.setImageBitmap(bitmap);
//        }else{
//            holder.profileImg.setImageResource(R.drawable.defaultprofileimage);
//        }

        String imagePath=data.getImagePath();//getting image from db
        if(imagePath!=null){
            Bitmap bitmap = MyUtility.getBitmapFromPath(imagePath);//converting image path to bitmap
            if(bitmap != null){
                holder.profileImg.setImageBitmap(bitmap);
            }else{//default image will be shown
                holder.profileImg.setImageResource(R.drawable.defaultprofileimage);
            }
        }else{//else default image will be shown
            holder.profileImg.setImageResource(R.drawable.defaultprofileimage);
        }


        holder.name.setText(data.getName());
        if(data.getAdvanceAmount() > 0 ){//no need to give >= because wastage of time
            holder.amountAdvance.setText(""+data.getAdvanceAmount());
            holder.amountAdvance.setTextColor(Color.RED);
        }else if(data.getBalanceAmount() > 0 ){
            holder.amountAdvance.setText(""+data.getBalanceAmount());
            holder.amountAdvance.setTextColor(context.getColor(R.color.green));
        }else {
            holder.amountAdvance.setText("0");//if no advance or balance then set to zero
            holder.amountAdvance.setTextColor(context.getColor(R.color.green));
        }

        if(data.getLatestDate() !=null) {//for null pointer exception//https://www.youtube.com/watch?v=VmhcvoenUl0

             if (MyUtility.getDateFromSystemDateTime(data.getLatestDate()).equals(todayDate.toString())) {//if profile color is yellow that means on current day some data is entered
                holder.yellowBg.setBackgroundColor(context.getColor(R.color.yellow));
            }else {
                holder.yellowBg.setBackgroundColor(Color.WHITE);
            }

            //if user is not active for 1 month then it will become inactive based on latest date
//            dateArray = MyUtility.getDateFromSystemDateTime(data.getLatestDate()).split("-");//return 2022-05-01
//            dbDate = LocalDate.of(Integer.parseInt(dateArray[2]),Integer.parseInt(dateArray[1]),Integer.parseInt(dateArray[0]));//it convert to 2022-05-01 it add 0 automatically
            // making it active or inactive using latest date (2022-05-01,2022-05-01) 0 is added to date

           // if(MyUtility.daysBetweenDate(dbDate,todayDate) >= daysToBecomeInactive){//ChronoUnit.MONTHS it give total months.here dbDate is first and dbDate will always be lower then today date even if we miss to open app for long days
            if(MyUtility.daysBetweenDate(LocalDate.parse(MyUtility.getDateFromSystemDateTime(data.getLatestDate())),todayDate) >= daysToBecomeInactive){//LocalDate.parse method, which takes a date string in the format "yyyy-MM-dd" (year-month-day) and parses it into a LocalDate object.

                if(db.makeIdInActive(data.getId())){//if latest date is one month old
                    if(!db.updatePersonRemarks(data.getId(),getRemarksWhenIdBecomeInActive(MyUtility.getDateFromSystemDateTime(db.getOnlySystemDateTimeOfLastRowOfWages(data.getId()))))){
                        Toast.makeText(context, context.getResources().getString(R.string.failed_to_update_remarks), Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(context, context.getResources().getString(R.string.failed_to_make_id_inactive), Toast.LENGTH_LONG).show();
                }

            }else{
                if(db.isActiveOrInactive(data.getId())){//to make id inactive change to false if(!db.isActiveOrInactive(data.getId())){
                    db.updateTable("UPDATE " + Database.PERSON_REGISTERED_TABLE + " SET "+Database.COL_12_ACTIVE+"='" + GlobalConstants.ACTIVE_PEOPLE.getValue() +"' WHERE "+Database.COL_1_ID+"='" + data.getId() + "'");//here latest date is not updated because already it is updated during insertion and if we update latest date here manually then wrong output because everytime adapter will update latest date.latest date is updated only during insertion or updation and profile would never be inactive
                 }else{
                    //for testing make if condition false and next if comment it
//                    db.updateTable("UPDATE " + Database.PERSON_REGISTERED_TABLE + " SET "+Database.COL_15_LATESTDATE+"='2024-05-10 22:04:42'" + " WHERE "+Database.COL_1_ID+"='" + data.getId() + "'");//here latest date is not updated because already it it updated during insertion and if we update latest date here manually then wrong output because everytime adapter will update latest date.latest date is updated only during insertion or updation
//                    if(!db.makeIdInActive(data.getId())){
//                        Toast.makeText(context, "FAILED TO MAKE ID INACTIVE", Toast.LENGTH_LONG).show();
//                    }
                    if(!db.makeIdActiveAndUpdateRemarks(data.getId(),true)){
                        Toast.makeText(context, context.getResources().getString(R.string.failed_to_make_id_active), Toast.LENGTH_LONG).show();
                    }
                }
             }
        }else{
            holder.yellowBg.setBackgroundColor(Color.WHITE);
        }

        //****************leaving date updating if days is 0 between two date then update SET LEAVINGDATE="+null+
       if(!MyUtility.updateLeavingDate(data.getId(),context,todayDate)){//update leaving date
           Toast.makeText(context, "LEAVING DATE NOT UPDATED", Toast.LENGTH_LONG).show();
       }

        //**************************************************************************************************************
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, IndividualPersonDetailActivity.class);
            intent.putExtra("ID", data.getId());
            intent.putExtra("FromMesterLaberGAdapter", 1);
            context.startActivity(intent);
        });
    }
    private String getRemarksWhenIdBecomeInActive(String dateFromSystemDateTime) {//by taking last row of wages
        if(dateFromSystemDateTime == null) return null;

        LocalDate date=LocalDate.parse(dateFromSystemDateTime);//parsing date of last row
        StringBuilder sb=new StringBuilder();
        sb.append(context.getString(R.string.became_inactive_colon))
          .append(date.getDayOfMonth()+"-"+date.getMonthValue()+"-"+date.getYear());

        return sb.toString();//INACTIVE: 3-7-2024 (take last row DATE)
    }
    @Override
    public int getItemCount() {
        return arrayList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder{//this class will hold only references of view
        ImageView profileImg;
        TextView amountAdvance,name;
        LinearLayout yellowBg;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.names_tv);
            profileImg =itemView.findViewById(R.id.profile_img);
            amountAdvance =itemView.findViewById(R.id.advance_amount_tv);
            yellowBg =itemView.findViewById(R.id.yellow_layout);
        }
    }
}
    //***************For setting or showing different layout to recycler view***********************************************************************************
//    //This adapter will decide which person is active or not.Person will become inactive if its leaving duration is 1 month.if user enter any data in current date then that person become active.
//    Context context;
//    ArrayList<MestreLaberGModel> arrayList;//because more operation is retrieving
//    //final Calendar current=Calendar.getInstance();//to get current date
//    // String currentDate =current.get(Calendar.DAY_OF_MONTH)+"-"+(current.get(Calendar.MONTH)+1)+"-"+current.get(Calendar.YEAR);
//    String dateArray[];
//    //int d,m,y;
//    LocalDate dbDate, todayDate = LocalDate.now();//current date; return 2022-05-01
//    String currentDateDBPattern =""+ todayDate.getDayOfMonth()+"-"+ todayDate.getMonthValue()+"-"+ todayDate.getYear();//converted to 1-5-2022
//    PersonRecordDatabase db;
//
//    private static final int ITEM_VIEW=0;//TO DECIDE which view to show to user
//    private static final int ITEM_LOADING_VIEW=1;//TO DECIDE which view to show to user
//    public static boolean isLoading=true;
////    private OnLoadMore mOnLoadMore;
////    private boolean isLoading;
//
////    public void setOnLoadMore(OnLoadMore onLoadMore1){
////       mOnLoadMore=onLoadMore1;
////    }
////    public void setIsLoading(boolean param){
////        isLoading=param;
////    }
//
//    public MestreLaberGAdapter(Context context, ArrayList<MestreLaberGModel> arrayList,RecyclerView recyclerView){
//        this.contex=context;
//        this.arrayList=arrayList;
//        db=new PersonRecordDatabase(contex);//we cant give this at class level
//
//
//
//    }
//    @NonNull
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//
////        if(viewType==ITEM_VIEW) {
////              view = inflater.inflate(R.layout.single_person_profile, parent, false);
////            return new ViewHolder(view);//constructor  public ViewHolder(@NonNull View itemView)
////        }else if(viewType==ITEM_LOADING_VIEW){
////            view = inflater.inflate(R.layout.single_person_profile, parent, false);
////            return new ViewHolder(view);//constructor  public ViewHolder(@NonNull View itemView)
////        }
//        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
//        View view=null;
//        switch (viewType) {//return specific view based on viewType
//            case ITEM_VIEW: view = inflater.inflate(R.layout.single_person_profile, parent, false);
//                break;
//            case ITEM_LOADING_VIEW: {
//                view = inflater.inflate(R.layout.progress_bar_for_all, parent, false);
//                return new ProgressbarViewHoder(view);
//            }
//        }
//        return new ViewHolder(view);//constructor  public ViewHolder(@NonNull View itemView)
//    }
//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder hold, int position) {//to fill data on every view filed
//
//        MestreLaberGModel data = arrayList.get(position);
//
//        switch (getItemViewType(position)) {
//            case ITEM_VIEW: {
//                ViewHolder holder = (ViewHolder) hold;//typecasting
//
//                byte[] image = data.getPerson_img();//getting image from db
//                //getting bytearray image from DB and converting  to bitmap to set in imageview
//                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
//                holder.name.setText(data.getName());
//                holder.profileimg.setImageBitmap(bitmap);
//                if (data.getAdvanceAmount() > 0) {//no need to give >= because wastage of time
//                    holder.amountAdvance.setText("" + data.getAdvanceAmount());
//                    holder.amountAdvance.setTextColor(Color.RED);
//                } else if (data.getBalanceAmount() > 0) {
//                    holder.amountAdvance.setText("" + data.getBalanceAmount());
//                    holder.amountAdvance.setTextColor(contex.getColor(R.color.green));
//                } else {
//                    holder.amountAdvance.setText("0");//if no advance or balance then set to zero
//                    holder.amountAdvance.setTextColor(contex.getColor(R.color.green));
//                }
//                if (data.getLatestDate() != null) {//for null pointer exception//https://www.youtube.com/watch?v=VmhcvoenUl0
//                    if (data.getLatestDate().equals(currentDateDBPattern)) //if profile color is yellow that means on current day some data is entered
//                        holder.yellowBg.setBackgroundColor(contex.getColor(R.color.yellow));
//                    else
//                        holder.yellowBg.setBackgroundColor(Color.WHITE);
//
//                    //if user is not active for 1 month then it will become inactive based on latest date
//                    dateArray = data.getLatestDate().split("-");
////                d = Integer.parseInt(dateArray[0]);
////                m = Integer.parseInt(dateArray[1]);
////                y = Integer.parseInt(dateArray[2]);
//                    dbDate = LocalDate.of(Integer.parseInt(dateArray[2]), Integer.parseInt(dateArray[1]), Integer.parseInt(dateArray[0]));//it convert 2022-05-01 it add 0 automatically
//                    // making it active or inactive using latest date (2022-05-01,2022-05-01)
//                    if (ChronoUnit.MONTHS.between(dbDate, todayDate) >= 1) { //ChronoUnit.MONTHS it give total months.here dbDate is first and dbDate will always be lower then today date even if we miss to open app for long days
//                        db.updateTable("UPDATE " + db.TABLE_NAME1 + " SET ACTIVE='" + 0 + "'" + " WHERE ID='" + data.getId() + "'");//user has no permission to make it inactive it is automatically
//
//                    } else {
//                        db.updateTable("UPDATE " + db.TABLE_NAME1 + " SET ACTIVE='" + 1 + "'" + " WHERE ID='" + data.getId() + "'");
//
//                    }
//                } else
//                    holder.yellowBg.setBackgroundColor(Color.WHITE);
//
//                holder.profileimg.setOnClickListener(view -> {
//                    Intent intent = new Intent(contex, IndividualPersonDetailActivity.class);
//                    intent.putExtra("ID", data.getId());
//                    intent.putExtra("FromMesterLaberGAdapter", 1);
//                    contex.startActivity(intent);
//
//
//                    Cursor cursor5 = db.getData("SELECT LATESTDATE FROM " + db.TABLE_NAME1 + " WHERE ID='" + data.getId() + "'");
//                    cursor5.moveToFirst();
//                    Toast.makeText(contex, "showLatestdate" + cursor5.getString(0), Toast.LENGTH_SHORT).show();
//
//                    //************************leaving date updation if days is 0 between two date then update SET LEAVINGDATE="+null+
//                    Cursor cursor2 = db.getData("SELECT LEAVINGDATE FROM " + db.TABLE_NAME3 + " WHERE ID='" + data.getId() + "'");
//                    cursor2.moveToFirst();
//                    if (cursor2.getString(0) != null) {
//                        dateArray = cursor2.getString(0).split("-");
////                    d = Integer.parseInt(dateArray[0]);
////                    m = Integer.parseInt(dateArray[1]);
////                    y = Integer.parseInt(dateArray[2]);//dbDate is leaving date
//                        dbDate = LocalDate.of(Integer.parseInt(dateArray[2]), Integer.parseInt(dateArray[1]), Integer.parseInt(dateArray[0]));//it convert 2022-05-01it add 0 automatically
//                        //between (2022-05-01,2022-05-01) like
//                        if (ChronoUnit.DAYS.between(dbDate, todayDate) >= 0) {//if days between leaving date and today date is 0 then leaving date will set null automatically
//                            db.updateTable("UPDATE " + db.TABLE_NAME3 + " SET LEAVINGDATE=" + null + " WHERE ID='" + data.getId() + "'");
//                        }
//                    }
//                    cursor2.close();
//                });
//                break;
//            }//case 1
//            case ITEM_LOADING_VIEW: {
//                ProgressbarViewHoder progressbarViewHold = (ProgressbarViewHoder) hold;//typecasting
//                if(isLoading==true) {
//                    System.out.println(isLoading);
//                    // isLoading=true;
//                    // progressbarViewHold.progressBar.setIndeterminate(true);
//                    progressbarViewHold.progressBar.setVisibility(View.VISIBLE);
//                }else {
//                    progressbarViewHold.progressBar.setVisibility(View.GONE);
//                    isLoading=false;
//                }
//                System.out.println(isLoading);
//                break;
//            }
//        }//switch
//    }
//    @Override
//    public int getItemCount() {
//        return arrayList.size();//if arraylist is empty then size() method return 0
//    }
//    @Override
//    public int getItemViewType(int position) {//Return the view type of the item at <code>position</code> for the purposes of view recycling.
//        return (position==arrayList.size()-1)? ITEM_LOADING_VIEW:ITEM_VIEW;//position start from 0.SO IT OVERRIDE one data so null is inserted at last of arraylist to avoid loosing 1 data
//    }
//    public class ViewHolder extends RecyclerView.ViewHolder{//this class will hold only references of view
//        ImageView profileimg;
//        TextView amountAdvance,name;
//        LinearLayout yellowBg;
//        public ViewHolder(@NonNull View itemView) {
//            super(itemView);
//            name=itemView.findViewById(R.id.names_tv);
//            profileimg=itemView.findViewById(R.id.profile_img);
//            amountAdvance =itemView.findViewById(R.id.advance_amount_tv);
//            yellowBg =itemView.findViewById(R.id.yellow_layout);
//        }
//    }
//    //viewholder for progressbar
//    public class ProgressbarViewHoder extends RecyclerView.ViewHolder{
//        ProgressBar progressBar;
//        public ProgressbarViewHoder(@NonNull View itemView) {
//            super(itemView);
//            progressBar=itemView.findViewById(R.id.progress_bar);
//        }
//    }


