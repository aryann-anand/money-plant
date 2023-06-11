package com.christo.moneyplant.models.ws.response;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class BinInfo {

    private int state;
    private String uid;
    private String location;
    private HashMap<String, Float> waste_types;

    public BinInfo(int state, String uid, String location, HashMap<String, Float> waste_types) {
        this.state = state;
        this.uid = uid;
        this.location = location;
        this.waste_types = waste_types;
    }

    public int getState() {
        return state;
    }

    public String getUid() {
        return uid;
    }

    public String getLocation() {
        return location;
    }

    public Set<Map.Entry<String, Float>> getWaste_types() {
        return waste_types.entrySet();
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setWaste_types(HashMap<String, Float> waste_types) {
        this.waste_types = waste_types;
    }
}
