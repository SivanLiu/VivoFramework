package com.vivo.services.vivodevice;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VivoDevice {
    private Map<String, PropertyData> propertyMap = new HashMap();

    public PropertyData getPropertyByName(String name) {
        return (PropertyData) this.propertyMap.get(name);
    }

    public VivoDevice setPropertyByName(String name, PropertyData data) {
        if (this.propertyMap.get(name) == null) {
            this.propertyMap.put(name, data);
            return this;
        }
        throw new RuntimeException("The property named " + name + " has been existed.");
    }

    public Set<String> getPropertyNameSet() {
        return this.propertyMap.keySet();
    }
}
