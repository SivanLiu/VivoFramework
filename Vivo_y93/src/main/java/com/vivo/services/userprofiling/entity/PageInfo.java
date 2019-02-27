package com.vivo.services.userprofiling.entity;

import java.util.HashMap;
import java.util.Map;

public class PageInfo {
    private String infoType = "-1";
    private String pageName = "";
    private Map<String, String> pageProperties = new HashMap();
    private String pageType = "-1";

    public PageInfo(String pageName, String pageType, String infoType) {
        this.pageName = pageName;
        this.pageType = pageType;
        this.infoType = infoType;
    }

    public String getPageName() {
        return this.pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public String getPageType() {
        return this.pageType;
    }

    public void setPageType(String pageType) {
        this.pageType = pageType;
    }

    public String getInfoType() {
        return this.infoType;
    }

    public void setInfoType(String infoType) {
        this.infoType = infoType;
    }

    public void clearPageProperties() {
        if (this.pageProperties != null) {
            this.pageProperties.clear();
        }
    }

    public Map<String, String> getPageProperties() {
        return this.pageProperties;
    }

    public int getPagePropertiesSize() {
        if (this.pageProperties != null) {
            return this.pageProperties.size();
        }
        return 0;
    }

    public void addPageProperties(String key, String value) {
        if (this.pageProperties != null) {
            this.pageProperties.put(key, value);
        }
    }

    public String toString() {
        return "pageName = " + this.pageName + ", pageType = " + this.pageType + ", infoType = " + this.infoType + ", pageProperties = " + this.pageProperties;
    }
}
