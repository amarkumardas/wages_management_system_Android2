package amar.das.labourmistri.model;

import java.io.Serializable;

public class MLGAllRecordModel implements Serializable {
    private static final long serialVersionUID = 423L;
   private String name, latestDate,id;
          private boolean activeOrInactive;

    public boolean isActive() {
        return activeOrInactive;
    }

    public void setActive(boolean activeOrInactive) {
        this.activeOrInactive = activeOrInactive;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLatestDate() {
        return latestDate;
    }

    public void setLatestDate(String latestDate) {
        this.latestDate = latestDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
