package amar.das.labourmistri.model;

import java.io.Serializable;

public class SearchModel implements Serializable {
   private String name;
    private String id;
    private String account;
    private String aadhaar;
    private String skill;
    private String latestDate;

    public String getLatestDate() {
        return latestDate;
    }

    public void setLatestDate(String latestDate) {
        this.latestDate = latestDate;
    }

    public boolean isActive() {
        return activeOrInactive;
    }

    public void setActive(boolean activeOrInactive) {
        this.activeOrInactive = activeOrInactive;
    }

    private boolean activeOrInactive;
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    private String phoneNumber;
    private static final long serialVersionUID = 4234L;
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

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAadhaar() {
        return aadhaar;
    }

    public void setAadhaar(String aadhaar) {
        this.aadhaar = aadhaar;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

}
