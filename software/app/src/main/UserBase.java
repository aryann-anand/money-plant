package com.christo.moneyplant.models.user;

import java.io.Serializable;

public class UserBase implements Serializable {
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
