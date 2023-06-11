package com.christo.moneyplant.models.transaction;

import java.util.Date;

public class TransactionInfo {
    public String email;
    public String bin_uid;
    public String waste_type;
    public float weight;
    public String uid;
    public float credits;
    public Date timestamp;

    public TransactionInfo(String email, String bin_uid, String waste_type, float weight, String uid, float credits, Date timestamp) {
        this.email = email;
        this.bin_uid = bin_uid;
        this.waste_type = waste_type;
        this.weight = weight;
        this.uid = uid;
        this.credits = credits;
        this.timestamp = timestamp;
    }

    public String getEmail() {
        return email;
    }

    public String getBin_uid() {
        return bin_uid;
    }

    public String getWaste_type() {
        return waste_type;
    }

    public float getWeight() {
        return weight;
    }

    public String getUid() {
        return uid;
    }

    public float getCredits() {
        return credits;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

}
