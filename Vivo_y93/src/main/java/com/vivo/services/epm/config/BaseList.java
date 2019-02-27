package com.vivo.services.epm.config;

public class BaseList {
    public static final String STANDARD_LIST_ITEM_ATTR_VALUE = "value";
    public static final String STANDARD_LIST_ITEM_TAG = "item";
    public static final String STANDARD_LIST_TAG_ATTR_NAME = "name";
    protected String mConfigFilePath;
    protected String name;
    protected boolean uninitialized;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConfigFilePath() {
        return this.mConfigFilePath;
    }

    public void setConfigFilePath(String path) {
        this.mConfigFilePath = path;
    }

    public void setUninitialized(boolean uninitialized) {
        this.uninitialized = uninitialized;
    }

    public boolean isUninitialized() {
        return this.uninitialized;
    }
}
