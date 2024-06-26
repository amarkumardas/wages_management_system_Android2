package amar.das.acbook.adapters;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import amar.das.acbook.R;
import amar.das.acbook.activity.IndividualPersonDetailActivity;
import amar.das.acbook.model.MestreLaberGModel;
import amar.das.acbook.utility.MyUtility;


public class InactiveAdapter extends RecyclerView.Adapter<InactiveAdapter.ViewHolder> {
    Context context;
    ArrayList<MestreLaberGModel> arrayList;//because more operation is retrieving

    public InactiveAdapter(Context context, ArrayList<MestreLaberGModel> arrayList){
        this.context =context;
        this.arrayList=arrayList;
    }
    @NonNull
    @Override
    public InactiveAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
       // View view=inflater.inflate(R.layout.inactive_single_person_profile,parent,false);
        View view=inflater.inflate(R.layout.single_person_profile,parent,false);
        return new InactiveAdapter.ViewHolder(view);//constructor  public ViewHolder(@NonNull View itemView)
    }

    @Override
    public void onBindViewHolder(@NonNull InactiveAdapter.ViewHolder holder, int position) {//to fill data on every view filed
        MestreLaberGModel data=arrayList.get(position);

        holder.name.setText(data.getName());

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

        holder.itemView.setOnClickListener(view -> {
            Intent intent=new Intent(context, IndividualPersonDetailActivity.class);
            intent.putExtra("ID",data.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder{//this class will hold only references of view
        ImageView profileImg;
        TextView amountAdvance,name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImg =itemView.findViewById(R.id.profile_img);
            amountAdvance =itemView.findViewById(R.id.advance_amount_tv);
            name=itemView.findViewById(R.id.names_tv);
        }
    }

}
