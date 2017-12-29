package de.chrgroth.jsonstore.json.flexjson.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FlexjsonTestDataParent {

    private int id;
    private String value;

    private Date date;
    private LocalDateTime dateTime;

    // flexjson will serialize collections only in deep serialize mode, references
    // are always serialized so we choose a list here
    private List<FlexjsonTestDataChild> children;

    public FlexjsonTestDataParent() {
        this(0, null);
    }

    public FlexjsonTestDataParent(int id, String value) {
        this(id, value, null, null);
    }

    public FlexjsonTestDataParent(int id, String value, Date date, LocalDateTime dateTime) {
        this.id = id;
        this.value = value;
        this.date = date;
        this.dateTime = dateTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public void add(FlexjsonTestDataChild child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
    }

    public List<FlexjsonTestDataChild> getChildren() {
        return children;
    }

    public void setChildren(List<FlexjsonTestDataChild> children) {
        this.children = children;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FlexjsonTestDataParent other = (FlexjsonTestDataParent) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }
}
