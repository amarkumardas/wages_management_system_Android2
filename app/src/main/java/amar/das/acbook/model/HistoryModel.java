package amar.das.acbook.model;

public class HistoryModel {


    private  String systemTimeDate;
    private  String status;
    private String id;
    private String name;
    private String remarks;
    private String p1Skill;
    private String p2Skill;
    private String p3Skill;
    private String p4Skill;
    private Short p1Work;
    private Short p2Work;
    private Short p3Work;
    private Short p4Work;
    private String userDate;
    private Integer wagesOrDeposit;
    private Boolean isDeposit;
    private boolean isShared;
    private String  subtractedAdvanceOrBal;//String taken because we want null if data is not present in database
    public String getSystemTimeDate() {
        return systemTimeDate;
    }

    public void setSystemTimeDate(String systemTimeDate) {
        this.systemTimeDate = systemTimeDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getP1Skill() {
        return p1Skill;
    }

    public void setP1Skill(String p1Skill) {
        this.p1Skill = p1Skill;
    }

    public String getP2Skill() {
        return p2Skill;
    }

    public void setP2Skill(String p2Skill) {
        this.p2Skill = p2Skill;
    }

    public String getP3Skill() {
        return p3Skill;
    }

    public void setP3Skill(String p3Skill) {
        this.p3Skill = p3Skill;
    }

    public String getP4Skill() {
        return p4Skill;
    }

    public void setP4Skill(String p4Skill) {
        this.p4Skill = p4Skill;
    }

    public Short getP1Work() {
        return p1Work;
    }

    public void setP1Work(Short p1Work) {
        this.p1Work = p1Work;
    }

    public Short getP2Work() {
        return p2Work;
    }

    public void setP2Work(Short p2Work) {
        this.p2Work = p2Work;
    }

    public Short getP3Work() {
        return p3Work;
    }

    public void setP3Work(Short p3Work) {
        this.p3Work = p3Work;
    }

    public Short getP4Work() {
        return p4Work;
    }

    public void setP4Work(Short p4Work) {
        this.p4Work = p4Work;
    }

    public String getUserDate() {
        return userDate;
    }

    public void setUserDate(String userDate) {
        this.userDate = userDate;
    }

    public Integer getWagesOrDeposit() {
        return wagesOrDeposit;
    }

    public void setWagesOrDeposit(Integer wagesOrDeposit) {
        this.wagesOrDeposit = wagesOrDeposit;
    }

    public Boolean getIsDeposit() {
        return isDeposit;
    }

    public void setIsDeposit(Boolean deposit) {
        isDeposit = deposit;
    }

    public boolean isShared() {
        return isShared;
    }

    public void setShared(boolean shared) {
        isShared = shared;
    }

    public String getSubtractedAdvanceOrBal() {
        return subtractedAdvanceOrBal;
    }

    public void setSubtractedAdvanceOrBal(String subtractedAdvanceOrBal) {
        this.subtractedAdvanceOrBal = subtractedAdvanceOrBal;
    }
}
