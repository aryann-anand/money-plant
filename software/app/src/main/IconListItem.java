package com.christo.moneyplant.models.ui;

import android.graphics.Color;

import com.christo.moneyplant.R;

public class IconListItem {
    private String tag;
    private String description;
    private int imgId;
    private int iconColor;

    public IconListItem(String description, int imgId) {
        this.description = description;
        this.imgId = imgId;
    }

    public int getIconColor() {
        return iconColor;
    }

    public void setIconColor(int iconColor) {
        this.iconColor = iconColor;
    }

    public IconListItem(String description, int imgId, int iconColor) {
        this.description = description;
        this.imgId = imgId;
        this.iconColor = iconColor;
    }

    public IconListItem(String tag, String description, int imgId, int iconColor) {
        this.tag = tag;
        this.description = description;
        this.imgId = imgId;
        this.iconColor = iconColor;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public int getImgId() {
        return imgId;
    }
    public void setImgId(int imgId) {
        this.imgId = imgId;
    }
    public String getTag() {
        return tag;
    }
}
