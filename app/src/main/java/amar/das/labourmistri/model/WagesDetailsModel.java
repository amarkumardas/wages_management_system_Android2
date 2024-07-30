package amar.das.labourmistri.model;

import java.io.Serializable;

public class WagesDetailsModel implements Serializable {
    private static final long serialVersionUID = 42345L;
    private String userGivenDate;

    public String getSystemDateAndTime() {
        return systemDateAndTime;
    }

    public void setSystemDateAndTime(String systemDateAndTime) {
        this.systemDateAndTime = systemDateAndTime;
    }

    private String systemDateAndTime;

    public boolean getIsdeposited() {
        return isdeposited;
    }

    public void setIsdeposited(boolean isdeposited) {
        this.isdeposited = isdeposited;
    }

    private boolean isdeposited;



    private String micPath;
    private String remarks;
    private String id;
    private short p1;
    private short p2;
    private short p3;
    private short p4;
    private int wagesOrDeposit;


//    public int getPdfSequence() {
//        return pdfSequence;
//    }
//
//    public void setPdfSequence(int pdfSequence) {
//        this.pdfSequence = pdfSequence;
//    }

    //private int pdfSequence;

    public  void setId(String id){
          this.id=id;
    }
    public String getId(){
        return  id;
    }
    public String getUserGivenDate() {
        return userGivenDate;
    }

    public void setUserGivenDate(String userGivenDate) {
        this.userGivenDate = userGivenDate;
    }

    public String getMicPath() {
        return micPath;
    }

    public void setMicPath(String micPath) {
        this.micPath = micPath;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public short getP1() {
        return p1;
    }

    public void setP1(short p1) {
        this.p1 = p1;
    }

    public short getP2() {
        return p2;
    }

    public void setP2(short p2) {
        this.p2 = p2;
    }

    public short getP3() {
        return p3;
    }

    public void setP3(short p3) {
        this.p3 = p3;
    }

    public short getP4() {
        return p4;
    }

    public void setP4(short p4) {
        this.p4 = p4;
    }

    public int getWagesOrDeposit() {
        return wagesOrDeposit;
    }

    public void setWagesOrDeposit(int wagesOrDeposit) {
        this.wagesOrDeposit = wagesOrDeposit;
    }

}
