package com.vivo.services.epm.config;

public interface IConfigurationManager {
    ContentValuesList getContentValuesList(String str, String str2);

    StringList getStringList(String str, String str2);

    Switch getSwitch(String str, String str2);

    boolean registerContentValuesListObserver(ContentValuesList contentValuesList, IConfigChangeCallback iConfigChangeCallback);

    boolean registerStringListObserver(StringList stringList, IConfigChangeCallback iConfigChangeCallback);

    boolean registerSwitchObserver(Switch switchR, IConfigChangeCallback iConfigChangeCallback);

    void unregisterContentValuesListObserver(ContentValuesList contentValuesList, IConfigChangeCallback iConfigChangeCallback);

    void unregisterStringListObserver(StringList stringList, IConfigChangeCallback iConfigChangeCallback);

    void unregisterSwitchObserver(Switch switchR, IConfigChangeCallback iConfigChangeCallback);
}
