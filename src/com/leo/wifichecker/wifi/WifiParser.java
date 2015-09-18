package com.leo.wifichecker.wifi;

import java.util.ArrayList;
import java.util.List;

import com.leo.wifichecker.utils.ExecTerminal;
import com.leo.wifichecker.utils.LogEx;

/**
 * Created by luqingyuan on 15/9/7.
 */
public class WifiParser {
    private final static String WIFI_BLOCK_START = "network={";
    private final static String WIFI_BLOCK_END = "}";
    private boolean mIsRooted;
    private Thread mThread;
    private WifiInfoFetcher.InnerFetcherListener mListener;

    public WifiParser(WifiInfoFetcher.InnerFetcherListener lis) {
        this.mIsRooted = new ExecTerminal().checkSu();
        mListener = lis;
    }

    /**
     * 开始解析
     */
    public void startParse() {
        LogEx.enter();
        if(mThread == null) {
            mThread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    List<APInfo> infos = getWiFiPasswordList();
                    if(mListener != null) {
                        mListener.onWifiChanged(infos, WifiInfoFetcher.InnerFetcherListener.INFO_FROM_WIFI_PARSER);
                    }
                }
            };
        }
        if(!mThread.isAlive()) {
            mThread.run();
        }
        LogEx.leave();
    }

    private ArrayList<APInfo> getWiFiPasswordList() {
        final String[] shellCommands = new String[] {
                "cat /data/misc/wifi/wpa_supplicant.conf",
        };

        ArrayList<APInfo> l = new ArrayList<APInfo>();

        for (int i = 0; i < shellCommands.length; i++) {
            String result = execute(shellCommands[i]);
            if (result.trim().length() > 0) {
                l = parseWifiPasswords(l, result);
                return l;
            }
        }

        return l;
    }

    private String execute(String cmd) {
        final ExecTerminal et = new ExecTerminal();
        final ExecTerminal.ExecResult res;

        if (mIsRooted) {
            res = et.execSu(cmd);
        } else {
            res = et.exec(cmd);
        }

        return res.getStdOut();
    }

    private ArrayList<APInfo> parseWifiPasswords(ArrayList<APInfo> l, String wifiString) {
        final String passwordBlocks[] = wifiString.split("\n\n");

        if (wifiString.length() <= 0) {
            return l;
        }

        for (int i = 0; i < passwordBlocks.length; i++) {
            String block = passwordBlocks[i].trim();

            if (block.startsWith(WIFI_BLOCK_START) && block.endsWith(WIFI_BLOCK_END)) {

                APInfo netInfo = new APInfo();
                StringBuilder others = null;

                String blockLines[] = block.split("\n");

                for (int j = 0; j < blockLines.length; j++) {
                    String line = blockLines[j].trim();

                    if (line.startsWith("ssid=")) {
                        netInfo.mSSID = APInfo.stripLeadingAndTrailingQuotes(line.replace("ssid=", ""));
                    } else if (line.startsWith("bssid=")) {
                        netInfo.mBSSID = APInfo.stripLeadingAndTrailingQuotes(line.replace("bssid=", ""));
                    } else if (line.startsWith("psk=")) {
                        netInfo.mPassword = APInfo.stripLeadingAndTrailingQuotes(line.replace("psk=", ""));
                    } else if (line.startsWith("password=")) {
                        netInfo.mPassword = APInfo.stripLeadingAndTrailingQuotes(line.replace("password=", ""));
                    } else if (line.startsWith("key_mgmt=")) {
                        netInfo.setKeyMgmt(line.replace("key_mgmt=", ""));
                    } else if (line.startsWith("eap=")) {
                        netInfo.mEap = line.replace("eap=", "");
                    } else if (line.startsWith("identity=")) {
                        netInfo.mIdentity = APInfo.stripLeadingAndTrailingQuotes(line.replace("identity=", ""));
                    }  else if(!line.startsWith(WIFI_BLOCK_START)
                            && !line.startsWith(WIFI_BLOCK_END)){
                        if(others == null) {
                            others = new StringBuilder();
                        }
                        others.append(line);
                        others.append(" ");
                    }
                }

                if(others!=null) {
                    netInfo.mOtherSettings = others.toString();
                }
                l.add(netInfo);
            }
        }
        return l;
    }
}
