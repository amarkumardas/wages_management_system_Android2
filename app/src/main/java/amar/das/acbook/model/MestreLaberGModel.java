package amar.das.acbook.model;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class MestreLaberGModel implements Serializable,Comparable<MestreLaberGModel>  {
    private static final long serialVersionUID = 42L;
    public int getAdvanceAmount() {
        return advanceAmount;
    }

    public void setAdvanceAmount(int advanceAmount) {
        this.advanceAmount = advanceAmount;
    }

    public int getBalanceAmount() {
        return balanceAmount;
    }

    public void setBalanceAmount(int balanceAmount) {
        this.balanceAmount = balanceAmount;
    }

    private int advanceAmount,balanceAmount;
    private String name;
    private String id;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    private String time;
    public String getLatestDate() {
        return latestDate;
    }

    public void setLatestDate(String latestDate) {
        this.latestDate = latestDate;
    }

    private String latestDate;
    private String imagePath;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
                //default constructed is created here

    public String getImagePath() {
        return imagePath;
    }
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public int compareTo(MestreLaberGModel obj1) {//natural sorting latest date desc
        DateFormat f = new SimpleDateFormat("dd-MM-yyyy");
        try {
            return f.parse(obj1.getLatestDate()).compareTo(f.parse(this.getLatestDate()));//will return 0 if two date is same
        }catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }//sort data by taking latest date in desc order.so that search would be easy.






























//    public static Comparator<MestreLaberGModel> s =new Comparator<MestreLaberGModel>() {
//        @Override
//        public int compare(MestreLaberGModel a, MestreLaberGModel b) {
//           // return a.getAdvanceAmount()-b.getAdvanceAmount();
//             return -b.getTime().compareTo(a.getTime());
//           // return 1;
//        }
//    };
}
