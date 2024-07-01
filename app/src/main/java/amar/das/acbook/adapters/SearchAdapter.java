package amar.das.acbook.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.ArrayList;

import amar.das.acbook.R;
import amar.das.acbook.activity.IndividualPersonDetailActivity;
import amar.das.acbook.globalenum.GlobalConstants;
import amar.das.acbook.model.SearchModel;
import amar.das.acbook.utility.MyUtility;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> implements Filterable {
    Context context;
    ArrayList<SearchModel> dataList;
    ArrayList<SearchModel> backup  ;
    public SearchAdapter(Context context, ArrayList<SearchModel> dataList) {
        this.context = context;
        this.dataList = dataList;
        backup=new ArrayList<>(dataList);//storing object to backup arraylist

    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.single_search_row,parent,false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
       SearchModel data= dataList.get(position);

       holder.id.setText("ID: "+data.getId());
       holder.name.setText(HtmlCompat.fromHtml("NAME: "+"<b>"+((data.getName()!=null)?data.getName():"")+"</b>",HtmlCompat.FROM_HTML_MODE_LEGACY));
       holder.acHolderName.setText(""+context.getString(R.string.skill)+": "+data.getSkill());

       if(data.isActive()){
           holder.inactiveOrActive.setText(context.getString(R.string.active));
           holder.inactiveOrActive.setBackgroundResource(R.drawable.green_color_bg);
       }else {
           holder.inactiveOrActive.setText(context.getString(R.string.inactive)+" FOR "+getInactiveDays(MyUtility.getDateFromSystemDateTime(data.getLatestDate())));
           holder.inactiveOrActive.setBackgroundResource(R.drawable.graycolor_bg);
       }

       //user may enter only account no or AADHAAR so if else is use separately
       //account and aadhaar length should be greater than 4 or 5 otherwise string out of bound exception because we r using this method (data.getAccount().length() - 4 or 5)so checking in if statement.we are viewing last 4 and 5 letters to user
        if(data.getAccount() !=null && data.getAccount().length()>4 ) {
            holder.account.setText(HtmlCompat.fromHtml("A/C: " +"<b>"+ data.getAccount().substring(data.getAccount().length() - 4)+"<b>",HtmlCompat.FROM_HTML_MODE_LEGACY));//getting last 4 letters
        }else {/*when data is not there in Db than set account  to - otherwise others people value is been showed in place of account To check just comment next line and see.Default null in database is not working only empty data is set if user don't enter data.*/
            holder.account.setText("A/C:    -");
          }

        if(data.getAadhaar()!=null && data.getAadhaar().length()>5){
            holder.aadhaar.setText(HtmlCompat.fromHtml("AADHAAR: " +"<b>"+ data.getAadhaar().substring(data.getAadhaar().length() - 5)+"<b>",HtmlCompat.FROM_HTML_MODE_LEGACY));//getting last 5 letters
        }else{/*when data is not there in Db than set  aadhaar to - otherwise others people value is been showed in place of aadhaar .Default null in database is not working only empty data is set if user don't enter data*/
            holder.aadhaar.setText("AADHAAR:    -");
        }

        if(data.getPhoneNumber() != null && data.getPhoneNumber().length()>6){
            holder.phoneNumber.setText(HtmlCompat.fromHtml("PHONE: " +"<b>"+ data.getPhoneNumber().substring(data.getPhoneNumber().length() - 6)+"<b>",HtmlCompat.FROM_HTML_MODE_LEGACY));//getting last 5 letters
        }else{/*when data is not there in Db than set  aadhaar to - otherwise others people value is been showed in place of aadhaar .Default null in database is not working only empty data is set if user don't enter data*/
            holder.phoneNumber.setText("PHONE:    -");
        }

        holder.phoneNumber.setOnClickListener(view ->{
            if(data.getPhoneNumber() != null && data.getPhoneNumber().length()>6) {
                MyUtility.snackBar(view,context.getString(R.string.last_6_digits_phone_number));
            }
        });
        holder.aadhaar.setOnClickListener(view -> {
            if(data.getAadhaar()!=null && data.getAadhaar().length()>5){
                MyUtility.snackBar(view, context.getString(R.string.last_5_digits_aadhaar_number));
            }
        });
        holder.account.setOnClickListener(view -> {
            if(data.getAccount() !=null && data.getAccount().length()>4) {
                MyUtility.snackBar(view, context.getString(R.string.last_4_digits_back_account_number));
            }
        });

        holder.itemView.setOnClickListener(view ->{
            Intent intent=new Intent(context, IndividualPersonDetailActivity.class);
            intent.putExtra("ID",data.getId());
            context.startActivity(intent);
            //((Activity)context).finish();//syntax to destroy activity from adapter
        });
    }
    private String getInactiveDays(String latestDate){
        if(latestDate== null) return "";

//        String[] dateArray = latestDate.split("-");
//        LocalDate dbDate = LocalDate.of(Integer.parseInt(dateArray[2]),Integer.parseInt(dateArray[1]),Integer.parseInt(dateArray[0]));//it convert 2022-5-1 to 2022-05-01 it add 0 automatically
        return convertDaysToPeriod(MyUtility.daysBetweenDate(LocalDate.parse(latestDate),LocalDate.now()));
    }
    private String convertDaysToPeriod(int days) {
        StringBuilder period = new StringBuilder();

        int years = days / 365;
        days %= 365; // Remaining days after calculating years

        if (years > 0) {
            period.append(years).append(" ").append(context.getString(R.string.year)).append(years > 1 ? context.getString(R.string.s)+" " : " "); // Add "s" for plural years
        }

        if (days >= 30) {
            int months = days / 30;
            days %= 30; // Remaining days after calculating months
 //            if (period.length() > 0) {
//                period.append(", "); //if there is year then Add comma and space if there's already a period
//            }
            period.append(months).append(" ").append(context.getString(R.string.month)).append(months > 1 ?  context.getString(R.string.s)+" " : " ");
        }

        if (days >= 7 && period.length() == 0) { // Only add weeks if no years or months
            int weeks = days / 7;
            days %= 7;
            period.append(weeks).append(" ").append(context.getString(R.string.week)).append(weeks > 1 ?  context.getString(R.string.s)+" " : " ");
        }

        if (days > 0) {// Handle remaining days (optional)
            period.append(days).append(" ").append(context.getString(R.string.day)).append(days > 1 ?  context.getString(R.string.s)+" " : "");
        }

        return period.toString().isEmpty() ? "0 "+context.getString(R.string.day) : period.toString(); // Handle 0 days case
    }

    @Override
    public int getItemCount() {
        return  dataList.size();
    }
    @Override
    public Filter getFilter() {
          return filter;//we have to create anonymous filter class
    }

    //it works background can be called as child thread
    Filter filter=new Filter() {//anonymous filter class
        @Override
        protected FilterResults performFiltering(CharSequence keyword) {//charSequence change to keyword
            ArrayList<SearchModel> filteredData=new ArrayList<>();//whatever data is filtered will be store here
             keyword=keyword.toString().replaceAll("[^a-zA-Z0-9\\s]", "");//[^a-zA-Z0-9\\s] it will remove all special character except whitespace,string will look like "a234 4 5"
              boolean keywordLength = keyword.toString().replaceAll(" ","").trim().length() >= 10;

            if(!keywordLength && keyword.toString().matches("[\\s*[0-9]+\\s*]+")){//\\s is whitespace and * is optional if user enter space between character then also work
               String str=keyword.toString().replaceAll(" ","");//removing all spaces with ""

                if (str.length() == 4) {//ac
                     for (SearchModel obj : backup){
                       //contains method checks like pattern match
                       // if (obj.getAccount().substring(obj.getAccount().length()-4).equals(str)) //not working while using only this statement no idea
                           if(obj.getAccount()!=null && obj.getAccount().contains(str)) {//if it matches then store in filter data ie. arraylist
                               if (obj.getAccount().substring(obj.getAccount().length()-4).equals(str))//boilerplate code but its working
                                   filteredData.add(obj);//equals method check  by content
                           }
                    }
                }else if (str.length() == 5) {//aadhaar
                    // if (obj.getAadhaar().substring(obj.getAadhaar().length()-5).equals(str))//not working while using only this statement no idea
                    for (SearchModel obj : backup) {
                        if (obj.getAadhaar()!=null && obj.getAadhaar().contains(str)) {//if it matches then store in filter data ie. arraylist
                            //here already length is checked so no exception obj.getAadhaar().length()-5
                            if (obj.getAadhaar().substring(obj.getAadhaar().length()-5).equals(str))//boilerplate code but its working
                               filteredData.add(obj);//equals method check  by content
                        }
                    }
                }else if (str.length() == 6) {//phone
                    // if (obj.getAadhaar().substring(obj.getAadhaar().length()-5).equals(str))//not working while using only this statement no idea
                    for (SearchModel obj : backup) {
                        if (obj.getPhoneNumber()!=null && obj.getPhoneNumber().contains(str)) {//if it matches then store in filter data ie. arraylist
                            //here already length is checked so no exception obj.getAadhaar().length()-5
                            if (obj.getPhoneNumber().substring(obj.getPhoneNumber().length()-6).equals(str))//boilerplate code but its working
                                filteredData.add(obj);//equals method check  by content
                        }
                    }
                }
            }else if(keyword.toString().matches("[\\s*[a-zA-Z]+\\s*]+")){//name

                 for (SearchModel obj : backup) {             //front and last spaces are removed using trim()
                   // if(obj.getName().startsWith(keyword.toString().toUpperCase().trim()))//if we enter A the it will show result whose first letter is A followed by other letters so we will get exact name
                    if(obj.getName()!=null && obj.getName().contains(keyword.toString().toUpperCase().trim()))//if we use this then name may be duplicate so using contain method so that it store when it match like patter match
                    {//if it matches then store in filter data ie arraylist
                        filteredData.add(obj);
                    }
                }
            }else if(!keywordLength && keyword.toString().matches("[\\s*[a-zA-Z][0-9]+\\s*]+")) {// with id user have to type any letter then id
                String str = keyword.toString().replaceAll("[\\s*[a-zA-Z]+\\s]", "");//replacing all spaces and alphabet with ""

                for (SearchModel obj : backup) {
                    //since id is not duplicate so filteredData should not contain duplicate like 23,123,123 here 23 is in all so
                    // equal method is used to check only content and then store
                    if (obj.getId()!=null && obj.getId().equals(str))//if it matches then store in filter data ie arraylist
                    {  //string equals( ) method is called because obj.getId() is string
                        filteredData.add(obj);
                        break;//once id found then break because id is not duplicate
                    }
                }
                //if length is greater than or equal to 10 and only number or space contain then only condition would be true
            }else if (keywordLength &&  keyword.toString().matches("[\\s*[0-9]+\\s*]+")){//\\s is whitespace and * is optional if user enter space between character then also work
                String str=keyword.toString().replaceAll(" ","");//removing all spaces with "".\\s is space

                checkFullPhoneAadhaarAccount(backup,str,filteredData);

            }
            //we have to return FilterResults so creating its object
            FilterResults  filterResults=new FilterResults();
            filterResults.values=filteredData;
            return filterResults;//this return will go in publishResults method
        }

        private void checkFullPhoneAadhaarAccount(ArrayList<SearchModel> backup, String str, ArrayList<SearchModel> filteredData) {
            for (SearchModel obj : backup) { //checking complete phone,aadhaar,account.taken separately if statement to add all matching data

                if(obj.getPhoneNumber()!=null && obj.getPhoneNumber().equals(str)){
                    filteredData.add(obj);
                }
                if(obj.getAadhaar()!=null && obj.getAadhaar().equals(str)) {
                    filteredData.add(obj);
                }
                if(obj.getAccount()!=null && obj.getAccount().equals(str)){
                    filteredData.add(obj);
                }
            }
        }

        @Override //view main UI thread
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
              dataList.clear();//clearing to show new filter search result
              dataList.addAll( (ArrayList<SearchModel>) filterResults.values);//typecasting to arraylist because
              notifyDataSetChanged();//when background child thread searching is completed then this method will notify to main UI thread that search is completed
//            dataList.clear(); // Clearing to show new filter search result
//            ArrayList<SearchModel> filteredList = (ArrayList<SearchModel>) filterResults.values;
//            if (filteredList.isEmpty()) {
//                Toast.makeText(context,context.getResources().getString(R.string.not_found), Toast.LENGTH_SHORT).show();
//            } else {
//                dataList.addAll(filteredList);
//            }
//            notifyDataSetChanged();
        }
    };//it is like statement so ; is necessary
    public class ViewHolder extends  RecyclerView.ViewHolder{
        TextView name,id, aadhaar,account, acHolderName,phoneNumber,inactiveOrActive;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
           name=itemView.findViewById(R.id.search_name_tv);
           id=itemView.findViewById(R.id.search_id_tv);
           aadhaar =itemView.findViewById(R.id.search_aadhar_tv);
           account=itemView.findViewById(R.id.search_ac_tv);
           acHolderName =itemView.findViewById(R.id.search_skill_tv);
           phoneNumber=itemView.findViewById(R.id.search_phone_tv);
           inactiveOrActive=itemView.findViewById(R.id.search_inactive_or_active_tv);
        }
    }
}
