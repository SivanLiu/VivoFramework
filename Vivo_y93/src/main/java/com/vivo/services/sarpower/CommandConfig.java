package com.vivo.services.sarpower;

import android.annotation.VivoHook;
import android.annotation.VivoHook.VivoHookType;
import android.os.SystemProperties;
import android.util.Slog;
import java.io.FileInputStream;
import java.util.HashMap;

@VivoHook(hookType = VivoHookType.NEW_CLASS)
public class CommandConfig {
    private static final String BOARD_VERSION = "/sys/devs_list/board_version";
    private static final String TAG = "SarCommandConfig";
    private static final boolean isOverseas = SystemProperties.get("ro.vivo.product.overseas", "no").equals("yes");
    private static final ConfigList[] mConfigs;
    private static final String mCountryCode = SystemProperties.get("ro.product.customize.bbk", "N");
    private static final String model = SystemProperties.get("ro.vivo.product.model").toLowerCase();
    public String[] mSarCommandsBody = new String[]{"AT+ERFTX=9,16,16,16,32,32,32"};
    public String[] mSarCommandsHead = new String[]{"AT+ERFTX=9,16,16,16,32,32,32"};
    public String[] mSarCommandsOnC2K = new String[]{"AT+ERFTX=4,8,0,8,16"};
    public String[] mSarCommandsOnC2KWhite = new String[]{"AT+ERFTX=4,8,0,8,16"};
    public String[] mSarCommandsWhiteBody = new String[]{"AT+ERFTX=9,16,16,16,32,32,32"};
    public String[] mSarCommandsWhiteHead = new String[]{"AT+ERFTX=9,16,16,16,32,32,32"};

    private static class ConfigList {
        private String[] commandsBody;
        private String[] commandsHead;
        private String[] commandsOnC2K;
        private String model;
        private String[] wcommandsBody;
        private String[] wcommandsHead;
        private String[] wcommandsOnC2K;

        /* synthetic */ ConfigList(String model, HashMap commands, HashMap wcommands, String[] commandsOnC2K, String[] wcommandsOnC2K, ConfigList -this5) {
            this(model, commands, wcommands, commandsOnC2K, wcommandsOnC2K);
        }

        private ConfigList(String model, String[] commands, String[] wcommands, String[] commandsOnC2K, String[] wcommandsOnC2K) {
            this.model = model;
            this.commandsHead = commands;
            this.wcommandsHead = wcommands;
            this.commandsOnC2K = commandsOnC2K;
            this.wcommandsOnC2K = wcommandsOnC2K;
        }

        private ConfigList(String model, HashMap<String, String[]> commands, HashMap<String, String[]> wcommands, String[] commandsOnC2K, String[] wcommandsOnC2K) {
            this.model = model;
            this.commandsHead = (String[]) commands.get("head");
            this.commandsBody = (String[]) commands.get("body");
            this.wcommandsHead = (String[]) wcommands.get("head");
            this.wcommandsBody = (String[]) wcommands.get("body");
            this.commandsOnC2K = commandsOnC2K;
            this.wcommandsOnC2K = wcommandsOnC2K;
        }
    }

    static {
        r7 = new ConfigList[27];
        r7[0] = new ConfigList("td1702", new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=9,8,8,8,16,16,16", "AT+ERFTX=13,7,8,16"});
                put("body", new String[0]);
            }
        }, new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=9,16,16,16,32,32,32", "AT+ERFTX=13,7,16,32"});
                put("body", new String[0]);
            }
        }, new String[]{""}, new String[]{""}, null);
        r7[1] = new ConfigList("pd1718", new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=10,2,4,4,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,0,0,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=10,3,1,4,0", "AT+ERFTX=10,3,3,12,0", "AT+ERFTX=10,3,7,12,0"});
                put("body", new String[0]);
            }
        }, new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=10,1,0,0,0,0,0,0,0,0,16,16,16,16,16,16,16,16,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0", "AT+ERFTX=10,2,8,8,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,0,0,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=10,3,1,8,0", "AT+ERFTX=10,3,3,16,0", "AT+ERFTX=10,3,7,16,0"});
                put("body", new String[0]);
            }
        }, new String[]{""}, new String[]{""}, null);
        r7[2] = new ConfigList("td1702f_ex", new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=10,2,4,4,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,0,0,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=10,3,1,4,0"});
                put("body", new String[]{""});
            }
        }, new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=10,1,0,0,0,0,0,0,0,0,24,24,24,24,24,24,24,24,28,28,28,28,28,28,28,28,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0", "AT+ERFTX=10,2,52,52,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,0,0,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=10,3,1,52,0", "AT+ERFTX=10,3,3,36,0", "AT+ERFTX=10,3,40,16,0", "AT+ERFTX=10,3,41,16,0"});
                put("body", new String[]{""});
            }
        }, new String[]{""}, new String[]{""}, null);
        r7[3] = new ConfigList("td1705", new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=10,1,0,0,0,0,0,0,0,0,24,24,24,24,24,24,24,24,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,24,24,24,24,24,24,24,24,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0", "AT+ERFTX=10,2,24,24,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,24,24,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=10,3,7,8,24"});
                put("body", new String[]{"AT+ERFTX=10,1,0,0,0,0,0,0,0,0,48,48,48,48,48,48,48,48,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,48,48,48,48,48,48,48,48,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0", "AT+ERFTX=10,2,48,48,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,48,48,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=10,3,7,16,48"});
            }
        }, new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=10,1,0,0,0,0,0,0,0,0,24,24,24,24,24,24,24,24,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,24,24,24,24,24,24,24,24,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0", "AT+ERFTX=10,2,24,24,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,24,24,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=10,3,7,8,24"});
                put("body", new String[]{"AT+ERFTX=10,1,0,0,0,0,0,0,0,0,48,48,48,48,48,48,48,48,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,48,48,48,48,48,48,48,48,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0", "AT+ERFTX=10,2,48,48,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,48,48,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=10,3,7,16,48"});
            }
        }, new String[]{""}, new String[]{""}, null);
        r7[4] = new ConfigList("pd1803", new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,24,24,24,24,24,24,24,24", "AT+ERFTX=10,2,0,0,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,32,32,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=10,3,1,0,32", "AT+ERFTX=10,3,3,0,8", "AT+ERFTX=10,3,7,0,16", "AT+ERFTX=10,3,34,0,24", "AT+ERFTX=10,3,39,0,12"});
                put("body", new String[0]);
            }
        }, new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,32,32,32,32,32,32,32,32,40,40,40,40,40,40,40,40", "AT+ERFTX=10,2,0,0,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,48,48,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=10,3,1,0,48", "AT+ERFTX=10,3,3,0,24", "AT+ERFTX=10,3,7,0,24", "AT+ERFTX=10,3,34,0,32", "AT+ERFTX=10,3,39,0,32", "AT+ERFTX=10,3,38,0,16", "AT+ERFTX=10,3,41,0,16"});
                put("body", new String[0]);
            }
        }, new String[]{""}, new String[]{""}, null);
        r7[5] = new ConfigList("pd1803f_ex", new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,24,24,24,24,24,24,24,24", "AT+ERFTX=10,2,0,0,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,32,32,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=10,3,1,0,32", "AT+ERFTX=10,3,3,0,8", "AT+ERFTX=10,3,7,0,16"});
                put("body", new String[]{"AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,32,32,32,32,32,32,32,32", "AT+ERFTX=10,2,0,0,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,28,28,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=10,3,1,0,28", "AT+ERFTX=10,3,7,0,12"});
            }
        }, new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,48,48,48,48,48,48,48,48,64,64,64,64,64,64,64,64", "AT+ERFTX=10,2,0,0,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,72,72,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=10,3,1,0,72", "AT+ERFTX=10,3,3,0,64", "AT+ERFTX=10,3,7,0,56", "AT+ERFTX=10,3,38,0,48", "AT+ERFTX=10,3,40,0,24", "AT+ERFTX=10,3,41,0,48"});
                put("body", new String[]{"AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,40,40,40,40,40,40,40,40", "AT+ERFTX=10,2,0,0,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,36,36,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=10,3,1,0,36", "AT+ERFTX=10,3,3,0,32", "AT+ERFTX=10,3,7,0,32"});
            }
        }, new String[]{""}, new String[]{""}, null);
        r7[6] = new ConfigList("pd1803bf_ex", new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,24,24,24,24,24,24,24,24", "AT+ERFTX=10,2,0,0,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,32,32,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=10,3,1,0,32", "AT+ERFTX=10,3,3,0,8", "AT+ERFTX=10,3,7,0,16"});
                put("body", new String[]{"AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,32,32,32,32,32,32,32,32", "AT+ERFTX=10,2,0,0,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,28,28,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=10,3,1,0,28", "AT+ERFTX=10,3,7,0,12"});
            }
        }, new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,48,48,48,48,48,48,48,48,64,64,64,64,64,64,64,64", "AT+ERFTX=10,2,0,0,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,72,72,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=10,3,1,0,72", "AT+ERFTX=10,3,3,0,64", "AT+ERFTX=10,3,7,0,56", "AT+ERFTX=10,3,38,0,48", "AT+ERFTX=10,3,40,0,24", "AT+ERFTX=10,3,41,0,48"});
                put("body", new String[]{"AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,40,40,40,40,40,40,40,40", "AT+ERFTX=10,2,0,0,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,36,36,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=10,3,1,0,36", "AT+ERFTX=10,3,3,0,32", "AT+ERFTX=10,3,7,0,32"});
            }
        }, new String[]{""}, new String[]{""}, null);
        r7[7] = new ConfigList("pd1801", new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=9,0,0,0,0,0,0"});
                put("body", new String[]{"AT+ERFTX=9,0,0,0,0,0,0"});
            }
        }, new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=10,2,0,0,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,14,14,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=10,3,1,0,12", "AT+ERFTX=10,3,3,0,4", "AT+ERFTX=10,3,7,0,12"});
                put("body", new String[]{"AT+ERFTX=9,0,0,0,0,0,0"});
            }
        }, new String[]{""}, new String[]{""}, null);
        r7[8] = new ConfigList("pd1732", new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=5,2,28,28,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,28", "AT+ERFTX=5,3,3,20", "AT+ERFTX=5,3,7,12"});
                put("body", new String[]{"AT+ERFTX=9,0,0,0,0,0,0"});
            }
        }, new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=5,2,36,36,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,36", "AT+ERFTX=5,3,3,32", "AT+ERFTX=5,3,7,16"});
                put("body", new String[]{"AT+ERFTX=9,0,0,0,0,0,0"});
            }
        }, new String[]{""}, new String[]{""}, null);
        r7[9] = new ConfigList("pd1732f_ex", new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=5,2,20,20,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,20", "AT+ERFTX=5,3,3,16", "AT+ERFTX=5,3,7,24"});
                put("body", new String[]{"AT+ERFTX=5,2,20,20,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,28", "AT+ERFTX=5,3,3,12", "AT+ERFTX=5,3,7,28", "AT+ERFTX=5,3,40,8"});
            }
        }, new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=5,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,80,80,80,80,80,80,80,80,104,104,104,104,104,104,104,104", "AT+ERFTX=5,2,72,72,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,72", "AT+ERFTX=5,3,3,56", "AT+ERFTX=5,3,5,16", "AT+ERFTX=5,3,7,64", "AT+ERFTX=5,3,38,48", "AT+ERFTX=5,3,40,72", "AT+ERFTX=5,3,41,48"});
                put("body", new String[]{"AT+ERFTX=5,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,80,80,80,80,80,80,80,80,104,104,104,104,104,104,104,104", "AT+ERFTX=5,2,72,72,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,72", "AT+ERFTX=5,3,3,56", "AT+ERFTX=5,3,5,16", "AT+ERFTX=5,3,7,64", "AT+ERFTX=5,3,38,48", "AT+ERFTX=5,3,40,72", "AT+ERFTX=5,3,41,48"});
            }
        }, new String[]{""}, new String[]{""}, null);
        r7[10] = new ConfigList("pd1732f_ex_RU", new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=5,2,8,8,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,8", "AT+ERFTX=5,3,3,16", "AT+ERFTX=5,3,7,24"});
                put("body", new String[]{"AT+ERFTX=5,2,16,16,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,16", "AT+ERFTX=5,3,3,16", "AT+ERFTX=5,3,7,28", "AT+ERFTX=5,3,40,8"});
            }
        }, new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=5,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,80,80,80,80,80,80,80,80,104,104,104,104,104,104,104,104", "AT+ERFTX=5,2,72,72,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,72", "AT+ERFTX=5,3,3,56", "AT+ERFTX=5,3,7,64", "AT+ERFTX=5,3,38,48", "AT+ERFTX=5,3,40,72", "AT+ERFTX=5,3,41,48"});
                put("body", new String[]{"AT+ERFTX=5,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,80,80,80,80,80,80,80,80,104,104,104,104,104,104,104,104", "AT+ERFTX=5,2,72,72,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,72", "AT+ERFTX=5,3,3,56", "AT+ERFTX=5,3,7,64", "AT+ERFTX=5,3,38,48", "AT+ERFTX=5,3,40,72", "AT+ERFTX=5,3,41,48"});
            }
        }, new String[]{""}, new String[]{""}, null);
        r7[11] = new ConfigList("pd1732f_ex_PH", new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=5,3,3,24", "AT+ERFTX=5,3,7,24"});
                put("body", new String[]{"AT+ERFTX=5,2,8,8,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,8", "AT+ERFTX=5,3,3,24", "AT+ERFTX=5,3,7,28", "AT+ERFTX=5,3,40,8"});
            }
        }, new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=5,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,80,80,80,80,80,80,80,80,104,104,104,104,104,104,104,104", "AT+ERFTX=5,2,72,72,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,72", "AT+ERFTX=5,3,3,56", "AT+ERFTX=5,3,5,16", "AT+ERFTX=5,3,7,64", "AT+ERFTX=5,3,38,48", "AT+ERFTX=5,3,40,72", "AT+ERFTX=5,3,41,48"});
                put("body", new String[]{"AT+ERFTX=5,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,80,80,80,80,80,80,80,80,104,104,104,104,104,104,104,104", "AT+ERFTX=5,2,72,72,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,72", "AT+ERFTX=5,3,3,56", "AT+ERFTX=5,3,5,16", "AT+ERFTX=5,3,7,64", "AT+ERFTX=5,3,38,48", "AT+ERFTX=5,3,40,72", "AT+ERFTX=5,3,41,48"});
            }
        }, new String[]{""}, new String[]{""}, null);
        r7[12] = new ConfigList("pd1732b", new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=5,2,20,20,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,20", "AT+ERFTX=5,3,3,16", "AT+ERFTX=5,3,7,24"});
                put("body", new String[]{"AT+ERFTX=9,0,0,0,0,0,0"});
            }
        }, new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=5,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,80,80,80,80,80,80,80,80,104,104,104,104,104,104,104,104", "AT+ERFTX=5,2,72,72,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,72", "AT+ERFTX=5,3,3,56", "AT+ERFTX=5,3,5,16", "AT+ERFTX=5,3,7,64", "AT+ERFTX=5,3,38,48", "AT+ERFTX=5,3,40,72", "AT+ERFTX=5,3,41,48"});
                put("body", new String[]{"AT+ERFTX=9,0,0,0,0,0,0"});
            }
        }, new String[]{""}, new String[]{""}, null);
        r7[13] = new ConfigList("pd1732bf_ex", new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=5,2,20,20,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,20", "AT+ERFTX=5,3,3,16", "AT+ERFTX=5,3,7,24"});
                put("body", new String[]{"AT+ERFTX=5,2,20,20,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,28", "AT+ERFTX=5,3,3,12", "AT+ERFTX=5,3,7,28", "AT+ERFTX=5,3,40,8"});
            }
        }, new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=5,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,80,80,80,80,80,80,80,80,104,104,104,104,104,104,104,104", "AT+ERFTX=5,2,72,72,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,72", "AT+ERFTX=5,3,3,56", "AT+ERFTX=5,3,5,16", "AT+ERFTX=5,3,7,64", "AT+ERFTX=5,3,38,48", "AT+ERFTX=5,3,40,72", "AT+ERFTX=5,3,41,48"});
                put("body", new String[]{"AT+ERFTX=5,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,80,80,80,80,80,80,80,80,104,104,104,104,104,104,104,104", "AT+ERFTX=5,2,72,72,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,72", "AT+ERFTX=5,3,3,56", "AT+ERFTX=5,3,5,16", "AT+ERFTX=5,3,7,64", "AT+ERFTX=5,3,38,48", "AT+ERFTX=5,3,40,72", "AT+ERFTX=5,3,41,48"});
            }
        }, new String[]{""}, new String[]{""}, null);
        r7[14] = new ConfigList("pd1732bf_ex_RU", new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=5,2,8,8,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,8", "AT+ERFTX=5,3,3,16", "AT+ERFTX=5,3,7,24"});
                put("body", new String[]{"AT+ERFTX=5,2,16,16,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,16", "AT+ERFTX=5,3,3,16", "AT+ERFTX=5,3,7,28", "AT+ERFTX=5,3,40,8"});
            }
        }, new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=5,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,80,80,80,80,80,80,80,80,104,104,104,104,104,104,104,104", "AT+ERFTX=5,2,72,72,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,72", "AT+ERFTX=5,3,3,56", "AT+ERFTX=5,3,7,64", "AT+ERFTX=5,3,38,48", "AT+ERFTX=5,3,40,72", "AT+ERFTX=5,3,41,48"});
                put("body", new String[]{"AT+ERFTX=5,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,80,80,80,80,80,80,80,80,104,104,104,104,104,104,104,104", "AT+ERFTX=5,2,72,72,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,72", "AT+ERFTX=5,3,3,56", "AT+ERFTX=5,3,7,64", "AT+ERFTX=5,3,38,48", "AT+ERFTX=5,3,40,72", "AT+ERFTX=5,3,41,48"});
            }
        }, new String[]{""}, new String[]{""}, null);
        r7[15] = new ConfigList("pd1732bf_ex_PH", new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=5,3,3,24", "AT+ERFTX=5,3,7,24"});
                put("body", new String[]{"AT+ERFTX=5,2,8,8,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,8", "AT+ERFTX=5,3,3,24", "AT+ERFTX=5,3,7,28", "AT+ERFTX=5,3,40,8"});
            }
        }, new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=5,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,80,80,80,80,80,80,80,80,104,104,104,104,104,104,104,104", "AT+ERFTX=5,2,72,72,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,72", "AT+ERFTX=5,3,3,56", "AT+ERFTX=5,3,5,16", "AT+ERFTX=5,3,7,64", "AT+ERFTX=5,3,38,48", "AT+ERFTX=5,3,40,72", "AT+ERFTX=5,3,41,48"});
                put("body", new String[]{"AT+ERFTX=5,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,80,80,80,80,80,80,80,80,104,104,104,104,104,104,104,104", "AT+ERFTX=5,2,72,72,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,72", "AT+ERFTX=5,3,3,56", "AT+ERFTX=5,3,5,16", "AT+ERFTX=5,3,7,64", "AT+ERFTX=5,3,38,48", "AT+ERFTX=5,3,40,72", "AT+ERFTX=5,3,41,48"});
            }
        }, new String[]{""}, new String[]{""}, null);
        r7[16] = new ConfigList("pd1732bf_ex_TW", new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=5,3,3,24", "AT+ERFTX=5,3,7,24"});
                put("body", new String[]{"AT+ERFTX=5,2,8,8,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,8", "AT+ERFTX=5,3,3,24", "AT+ERFTX=5,3,7,28", "AT+ERFTX=5,3,40,8"});
            }
        }, new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=5,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,80,80,80,80,80,80,80,80,104,104,104,104,104,104,104,104", "AT+ERFTX=5,2,72,72,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,72", "AT+ERFTX=5,3,3,56", "AT+ERFTX=5,3,5,16", "AT+ERFTX=5,3,7,64", "AT+ERFTX=5,3,38,48", "AT+ERFTX=5,3,40,72", "AT+ERFTX=5,3,41,48"});
                put("body", new String[]{"AT+ERFTX=5,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,80,80,80,80,80,80,80,80,104,104,104,104,104,104,104,104", "AT+ERFTX=5,2,72,72,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,72", "AT+ERFTX=5,3,3,56", "AT+ERFTX=5,3,5,16", "AT+ERFTX=5,3,7,64", "AT+ERFTX=5,3,38,48", "AT+ERFTX=5,3,40,72", "AT+ERFTX=5,3,41,48"});
            }
        }, new String[]{""}, new String[]{""}, null);
        r7[17] = new ConfigList("pd1732c", new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=5,2,20,20,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,20", "AT+ERFTX=5,3,3,16", "AT+ERFTX=5,3,7,24"});
                put("body", new String[]{"AT+ERFTX=9,0,0,0,0,0,0"});
            }
        }, new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=5,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,80,80,80,80,80,80,80,80,104,104,104,104,104,104,104,104", "AT+ERFTX=5,2,72,72,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,72", "AT+ERFTX=5,3,3,56", "AT+ERFTX=5,3,5,16", "AT+ERFTX=5,3,7,64", "AT+ERFTX=5,3,38,48", "AT+ERFTX=5,3,40,72", "AT+ERFTX=5,3,41,48"});
                put("body", new String[]{"AT+ERFTX=9,0,0,0,0,0,0"});
            }
        }, new String[]{""}, new String[]{""}, null);
        r7[18] = new ConfigList("pd1732cf_ex", new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=5,2,20,20,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,20", "AT+ERFTX=5,3,3,16", "AT+ERFTX=5,3,7,24"});
                put("body", new String[]{"AT+ERFTX=5,2,20,20,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,28", "AT+ERFTX=5,3,3,12", "AT+ERFTX=5,3,7,28", "AT+ERFTX=5,3,40,8"});
            }
        }, new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=3,1,0,0,0,0,0,0,0,0,0,24,24,24,0,24,24,24,80,80,80,80,80,80,80,80,104,104,104,104,104,104,104,104", "AT+ERFTX=5,2,72,72,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,72", "AT+ERFTX=5,3,3,56", "AT+ERFTX=5,3,5,16", "AT+ERFTX=5,3,7,64", "AT+ERFTX=5,3,38,48", "AT+ERFTX=5,3,40,72", "AT+ERFTX=5,3,41,48"});
                put("body", new String[]{"AT+ERFTX=3,1,0,0,0,0,0,0,0,0,0,24,24,24,0,24,24,24,80,80,80,80,80,80,80,80,104,104,104,104,104,104,104,104", "AT+ERFTX=5,2,72,72,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,72", "AT+ERFTX=5,3,3,56", "AT+ERFTX=5,3,5,16", "AT+ERFTX=5,3,7,64", "AT+ERFTX=5,3,38,48", "AT+ERFTX=5,3,40,72", "AT+ERFTX=5,3,41,48"});
            }
        }, new String[]{""}, new String[]{""}, null);
        r7[19] = new ConfigList("pd1732cf_ex_PH", new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=5,3,3,16", "AT+ERFTX=5,3,7,24"});
                put("body", new String[]{"AT+ERFTX=5,2,8,8,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,8", "AT+ERFTX=5,3,3,16", "AT+ERFTX=5,3,7,28", "AT+ERFTX=5,3,40,8"});
            }
        }, new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=3,1,0,0,0,0,0,0,0,0,0,24,24,24,0,24,24,24,80,80,80,80,80,80,80,80,104,104,104,104,104,104,104,104", "AT+ERFTX=5,2,72,72,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,72", "AT+ERFTX=5,3,3,56", "AT+ERFTX=5,3,5,16", "AT+ERFTX=5,3,7,64", "AT+ERFTX=5,3,38,48", "AT+ERFTX=5,3,40,72", "AT+ERFTX=5,3,41,48"});
                put("body", new String[]{"AT+ERFTX=3,1,0,0,0,0,0,0,0,0,0,24,24,24,0,24,24,24,80,80,80,80,80,80,80,80,104,104,104,104,104,104,104,104", "AT+ERFTX=5,2,72,72,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,72", "AT+ERFTX=5,3,3,56", "AT+ERFTX=5,3,5,16", "AT+ERFTX=5,3,7,64", "AT+ERFTX=5,3,38,48", "AT+ERFTX=5,3,40,72", "AT+ERFTX=5,3,41,48"});
            }
        }, new String[]{""}, new String[]{""}, null);
        r7[20] = new ConfigList("pd1732d", new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=5,2,20,20,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,20", "AT+ERFTX=5,3,3,16", "AT+ERFTX=5,3,7,24"});
                put("body", new String[]{"AT+ERFTX=9,0,0,0,0,0,0"});
            }
        }, new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=5,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,80,80,80,80,80,80,80,80,104,104,104,104,104,104,104,104", "AT+ERFTX=5,2,72,72,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,72", "AT+ERFTX=5,3,3,56", "AT+ERFTX=5,3,5,16", "AT+ERFTX=5,3,7,64", "AT+ERFTX=5,3,38,48", "AT+ERFTX=5,3,40,72", "AT+ERFTX=5,3,41,48"});
                put("body", new String[]{"AT+ERFTX=9,0,0,0,0,0,0"});
            }
        }, new String[]{""}, new String[]{""}, null);
        r7[21] = new ConfigList("pd1732df_ex", new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=5,2,20,20,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,20", "AT+ERFTX=5,3,3,16", "AT+ERFTX=5,3,7,24"});
                put("body", new String[]{"AT+ERFTX=5,2,20,20,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,28", "AT+ERFTX=5,3,3,12", "AT+ERFTX=5,3,7,28", "AT+ERFTX=5,3,40,8"});
            }
        }, new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=3,1,0,0,0,0,0,0,0,0,0,24,24,24,0,24,24,24,80,80,80,80,80,80,80,80,104,104,104,104,104,104,104,104", "AT+ERFTX=5,2,72,72,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,72", "AT+ERFTX=5,3,3,56", "AT+ERFTX=5,3,5,16", "AT+ERFTX=5,3,7,64", "AT+ERFTX=5,3,38,48", "AT+ERFTX=5,3,40,72", "AT+ERFTX=5,3,41,48"});
                put("body", new String[]{"AT+ERFTX=3,1,0,0,0,0,0,0,0,0,0,24,24,24,0,24,24,24,80,80,80,80,80,80,80,80,104,104,104,104,104,104,104,104", "AT+ERFTX=5,2,72,72,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,72", "AT+ERFTX=5,3,3,56", "AT+ERFTX=5,3,5,16", "AT+ERFTX=5,3,7,64", "AT+ERFTX=5,3,38,48", "AT+ERFTX=5,3,40,72", "AT+ERFTX=5,3,41,48"});
            }
        }, new String[]{""}, new String[]{""}, null);
        r7[22] = new ConfigList("pd1732df_ex_PH", new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=5,3,3,16", "AT+ERFTX=5,3,7,24"});
                put("body", new String[]{"AT+ERFTX=5,2,8,8,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,8", "AT+ERFTX=5,3,3,16", "AT+ERFTX=5,3,7,28", "AT+ERFTX=5,3,40,8"});
            }
        }, new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=3,1,0,0,0,0,0,0,0,0,0,24,24,24,0,24,24,24,80,80,80,80,80,80,80,80,104,104,104,104,104,104,104,104", "AT+ERFTX=5,2,72,72,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,72", "AT+ERFTX=5,3,3,56", "AT+ERFTX=5,3,5,16", "AT+ERFTX=5,3,7,64", "AT+ERFTX=5,3,38,48", "AT+ERFTX=5,3,40,72", "AT+ERFTX=5,3,41,48"});
                put("body", new String[]{"AT+ERFTX=3,1,0,0,0,0,0,0,0,0,0,24,24,24,0,24,24,24,80,80,80,80,80,80,80,80,104,104,104,104,104,104,104,104", "AT+ERFTX=5,2,72,72,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,", "AT+ERFTX=5,3,1,72", "AT+ERFTX=5,3,3,56", "AT+ERFTX=5,3,5,16", "AT+ERFTX=5,3,7,64", "AT+ERFTX=5,3,38,48", "AT+ERFTX=5,3,40,72", "AT+ERFTX=5,3,41,48"});
            }
        }, new String[]{""}, new String[]{""}, null);
        r7[23] = new ConfigList("pd1813", new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,7,7,7,0,0,0,0,0,16,16,16,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0", "AT+ERFTX=10,2,24,24,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,0,0,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=10,3,1,23,0", "AT+ERFTX=10,3,3,24,0", "AT+ERFTX=10,3,4,16,0", "AT+ERFTX=10,3,7,4,0", "AT+ERFTX=10,3,39,12,0", "AT+ERFTX=10,3,40,4,0"});
                put("body", new String[0]);
            }
        }, new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,15,15,15,0,0,0,0,0,24,24,24,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0", "AT+ERFTX=10,2,32,32,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,0,0,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=10,3,1,31,0", "AT+ERFTX=10,3,3,32,0", "AT+ERFTX=10,3,4,20,0", "AT+ERFTX=10,3,7,12,0", "AT+ERFTX=10,3,39,20,0", "AT+ERFTX=10,3,40,12,0"});
                put("body", new String[0]);
            }
        }, new String[]{""}, new String[]{""}, null);
        r7[24] = new ConfigList("pd1813f_ex", new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,7,7,7,0,0,0,0,0,24,24,24,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0", "AT+ERFTX=10,2,24,24,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,0,0,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=10,3,1,23,0", "AT+ERFTX=10,3,3,24,0", "AT+ERFTX=10,3,7,4,0", "AT+ERFTX=10,3,39,12,0", "AT+ERFTX=10,3,40,4,0"});
                put("body", new String[]{"AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,24,24,24,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0", "AT+ERFTX=10,2,20,20,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,0,0,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=10,3,1,22,0", "AT+ERFTX=10,3,3,19,0", "AT+ERFTX=10,3,39,10,0"});
            }
        }, new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=10,1,16,16,16,16,0,0,0,0,24,24,24,24,0,0,0,0,50,50,50,50,0,0,0,0,76,76,76,76,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0", "AT+ERFTX=10,2,65,65,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,0,0,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=10,3,1,68,0", "AT+ERFTX=10,3,3,72,0", "AT+ERFTX=10,3,5,4,0", "AT+ERFTX=10,3,7,64,0", "AT+ERFTX=10,3,8,8,0", "AT+ERFTX=10,3,38,44,0", "AT+ERFTX=10,3,39,55,0", "AT+ERFTX=10,3,40,48,0", "AT+ERFTX=10,3,41,48,0"});
                put("body", new String[]{"AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,32,32,32,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0", "AT+ERFTX=10,2,24,24,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,0,0,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=10,3,1,26,0", "AT+ERFTX=10,3,3,23,0", "AT+ERFTX=10,3,39,14,0"});
            }
        }, new String[]{""}, new String[]{""}, null);
        r7[25] = new ConfigList("pd1813bf_ex", new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,7,7,7,0,0,0,0,0,24,24,24,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0", "AT+ERFTX=10,2,24,24,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,0,0,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=10,3,1,23,0", "AT+ERFTX=10,3,3,24,0", "AT+ERFTX=10,3,7,4,0", "AT+ERFTX=10,3,39,12,0", "AT+ERFTX=10,3,40,4,0"});
                put("body", new String[]{"AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,24,24,24,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0", "AT+ERFTX=10,2,20,20,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,0,0,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=10,3,1,22,0", "AT+ERFTX=10,3,3,19,0", "AT+ERFTX=10,3,39,10,0"});
            }
        }, new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=10,1,16,16,16,16,0,0,0,0,24,24,24,24,0,0,0,0,50,50,50,50,0,0,0,0,76,76,76,76,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0", "AT+ERFTX=10,2,65,65,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,0,0,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=10,3,1,68,0", "AT+ERFTX=10,3,3,72,0", "AT+ERFTX=10,3,5,4,0", "AT+ERFTX=10,3,7,64,0", "AT+ERFTX=10,3,8,8,0", "AT+ERFTX=10,3,38,44,0", "AT+ERFTX=10,3,39,55,0", "AT+ERFTX=10,3,40,48,0", "AT+ERFTX=10,3,41,48,0"});
                put("body", new String[]{"AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,32,32,32,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0", "AT+ERFTX=10,2,24,24,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,0,0,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=10,3,1,26,0", "AT+ERFTX=10,3,3,23,0", "AT+ERFTX=10,3,39,14,0"});
            }
        }, new String[]{""}, new String[]{""}, null);
        r7[26] = new ConfigList("pd1818c", new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=5,2,4,4,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,0,0,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=5,3,7,20", "AT+ERFTX=5,3,38,4"});
                put("body", new String[0]);
            }
        }, new HashMap<String, String[]>() {
            {
                put("head", new String[]{"AT+ERFTX=5,2,8,8,0,0,,,,,0,0,,,,,0,0,,,,,,,,,,,,,,,,,,,,,,,,,0,0,0,0,,,,,0,0,,,,,0,0", "AT+ERFTX=5,3,1,4", "AT+ERFTX=5,3,7,24", "AT+ERFTX=5,3,38,8", "AT+ERFTX=5,3,41,4"});
                put("body", new String[0]);
            }
        }, new String[]{""}, new String[]{""}, null);
        mConfigs = r7;
    }

    private static String getModelWithCountryCode(String model) {
        String modelWithCountryCode = model;
        if (!isOverseas || mCountryCode.equals("N")) {
            return model;
        }
        if (model.equals("pd1732f_ex") && (mCountryCode.equals("PH") || mCountryCode.equals("RU"))) {
            modelWithCountryCode = model + "_" + mCountryCode;
        }
        if (model.equals("pd1732bf_ex") && (mCountryCode.equals("PH") || mCountryCode.equals("RU") || mCountryCode.equals("TW"))) {
            modelWithCountryCode = modelWithCountryCode + "_" + mCountryCode;
        }
        if (model.equals("pd1732cf_ex") && mCountryCode.equals("PH")) {
            modelWithCountryCode = modelWithCountryCode + "_" + mCountryCode;
        }
        if (model.equals("pd1732df_ex") && mCountryCode.equals("PH")) {
            modelWithCountryCode = modelWithCountryCode + "_" + mCountryCode;
        }
        return modelWithCountryCode;
    }

    private boolean isUnderFactoryMode() {
        return SystemProperties.get("persist.sys.factory.mode", "no").equals("yes");
    }

    private static boolean isPD1732D() {
        try {
            FileInputStream mInputStream = new FileInputStream(BOARD_VERSION);
            byte[] buf = new byte[100];
            int len = mInputStream.read(buf);
            String board_version = new String(buf, 0, len);
            Slog.e(TAG, "borad version: " + board_version + " len: " + len);
            char[] temp = board_version.toCharArray();
            mInputStream.close();
            if (temp[2] == '0') {
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    private static String getBoardVersionChanged(String model) {
        String tempModel = model;
        if (model.equals("pd1732") && isPD1732D()) {
            tempModel = "pd1732c";
        }
        if (model.equals("pd1732f_ex") && isPD1732D()) {
            return "pd1732cf_ex";
        }
        return tempModel;
    }

    private Object[] parseCommandByProject() {
        String[] specificConfigHead = new String[]{"AT+ERFTX=9,16,16,16,32,32,32"};
        String[] specificConfigBody = new String[]{"AT+ERFTX=9,16,16,16,32,32,32"};
        String[] wspecificConfigHead = new String[]{"AT+ERFTX=9,16,16,16,32,32,32"};
        String[] wspecificConfigBody = new String[]{"AT+ERFTX=9,16,16,16,32,32,32"};
        String[] specificConfigOnC2K = new String[]{"AT+ERFTX=4,8,0,8,16"};
        String[] wspecificConfigOnC2K = new String[]{"AT+ERFTX=4,8,0,8,16"};
        String mModel = model;
        Slog.e(TAG, "isOverseas = " + isOverseas + ", mCountryCode = " + mCountryCode + ", model = " + mModel);
        if (!isUnderFactoryMode()) {
            mModel = getModelWithCountryCode(model);
        }
        mModel = getBoardVersionChanged(mModel);
        Slog.e(TAG, "after modify  model = " + mModel);
        if (mConfigs == null) {
            Slog.e(TAG, "mConfigs is null! return default command config : " + specificConfigHead.toString());
        } else if (mConfigs.length > 0) {
            for (int i = 0; i < mConfigs.length; i++) {
                if (mModel.equals(mConfigs[i].model)) {
                    specificConfigHead = mConfigs[i].commandsHead;
                    specificConfigBody = mConfigs[i].commandsBody;
                    wspecificConfigHead = mConfigs[i].wcommandsHead;
                    wspecificConfigBody = mConfigs[i].wcommandsBody;
                    specificConfigOnC2K = mConfigs[i].commandsOnC2K;
                    wspecificConfigOnC2K = mConfigs[i].wcommandsOnC2K;
                    Slog.e(TAG, "get it, return project " + mModel + " 's specific command config : " + specificConfigHead.toString());
                    return new Object[]{specificConfigHead, specificConfigBody, wspecificConfigHead, wspecificConfigBody, specificConfigOnC2K, wspecificConfigOnC2K};
                }
            }
        } else {
            Slog.e(TAG, "mConfigs is empty, return default command config : " + specificConfigHead.toString());
        }
        return new Object[]{specificConfigHead, specificConfigBody, wspecificConfigHead, wspecificConfigBody, specificConfigOnC2K, wspecificConfigOnC2K};
    }

    public CommandConfig() {
        updateSarCommands();
    }

    public void updateSarCommands() {
        Object[] commandset = parseCommandByProject();
        this.mSarCommandsHead = (String[]) commandset[0];
        this.mSarCommandsBody = (String[]) commandset[1];
        this.mSarCommandsWhiteHead = (String[]) commandset[2];
        this.mSarCommandsWhiteBody = (String[]) commandset[3];
        this.mSarCommandsOnC2K = (String[]) commandset[4];
        this.mSarCommandsOnC2KWhite = (String[]) commandset[5];
    }
}
