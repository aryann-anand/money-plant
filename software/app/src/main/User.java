package com.christo.moneyplant.models.user;

import java.io.Serializable;

public class User extends UserBase implements Serializable {

    private String name;
    private Float credit_balance;
    private String aadhaar_id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getCredit_balance() {
        return credit_balance;
    }

    public void setCredit_balance(Float credit_balance) {
        this.credit_balance = credit_balance;
    }

    public String getAadhaar_id() {
        return aadhaar_id;
    }

    public void setAadhaar_id(String aadhaar_id) {
        this.aadhaar_id = aadhaar_id;
    }

}
