package com.vivo.customized.support;

import com.vivo.services.cust.VivoCustomManager;

abstract class BaseManager {
    protected VivoCustomManager custManager;

    public BaseManager() {
        this.custManager = null;
        this.custManager = new VivoCustomManager();
    }
}
