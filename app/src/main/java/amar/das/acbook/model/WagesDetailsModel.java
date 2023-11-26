package amar.das.acbook.model;

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

    public String getIsdeposited() {
        return isdeposited;
    }

    public void setIsdeposited(String isdeposited) {
        this.isdeposited = isdeposited;
    }

    private String isdeposited;



    private String micPath;
    private String remarks;
    private String id;
    private int p1;
    private int p2;
    private int p3;
    private int p4;
    private int wages;
    private int deposit;

    public int getPdfSequence() {
        return pdfSequence;
    }

    public void setPdfSequence(int pdfSequence) {
        this.pdfSequence = pdfSequence;
    }

    private int pdfSequence;

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

    public int getP1() {
        return p1;
    }

    public void setP1(int p1) {
        this.p1 = p1;
    }

    public int getP2() {
        return p2;
    }

    public void setP2(int p2) {
        this.p2 = p2;
    }

    public int getP3() {
        return p3;
    }

    public void setP3(int p3) {
        this.p3 = p3;
    }

    public int getP4() {
        return p4;
    }

    public void setP4(int p4) {
        this.p4 = p4;
    }

    public int getWages() {
        return wages;
    }

    public void setWages(int wages) {
        this.wages = wages;
    }

    public int getDeposit() {
        return deposit;
    }

    public void setDeposit(int deposit) {
        this.deposit = deposit;
    }
}
