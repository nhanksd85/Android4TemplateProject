package bku.iot.dashboard;

import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Ultis {
    public static Map<String, String> getCpuInfoMap() {
        Map<String, String> map = new HashMap<String, String>();
        try {
            Scanner s = new Scanner(new File("/proc/cpuinfo"));
            while (s.hasNextLine()) {
                String[] vals = s.nextLine().split(": ");
                if (vals.length > 1) map.put(vals[0].trim(), vals[1].trim());
            }
        } catch (Exception e) {
            Log.e("getCpuInfoMap",Log.getStackTraceString(e));}
        return map;
    }

    public static String getCPUSerial(){

        String strData = getCpuInfoMap().toString();
        String[] splitData = strData.split(Pattern.quote(","));

        int i;
        for(i = 0; i<splitData.length; i++)
        {
            if (splitData[i].indexOf("Serial") >=0){
                String[] splitSerial = splitData[i].split(Pattern.quote("="));
                Log.d("TEST_IOT", splitSerial[1]);
                return splitSerial[1] + "ABC";
            }


        }
        return "123456789";
    }

    public static String hashCode(String hexCode) {
        String hash = "";
        final int length = 6;
        if (hexCode.isEmpty()) {
            Random random = new Random();
            int d = 0;
            for (int i = 0; i < length; i++) {
                d = random.nextInt(10);
                hash = hash + d;
            }
        } else {
//            long code = Long.parseLong(hexCode, 16);
//            hash = code + "000000";
//            hash = hash.substring(0, length);

            try {
                long code = Long.parseLong(hexCode, 16);
                hash = code + "000000";
                hash = hash.substring(0, length);
            }catch (Exception e){
                if(hexCode.length() > 10)
                    hexCode = hexCode.substring(0, 10);
                long code = Long.parseLong(hexCode.substring(0, hexCode.length() - 4), 16);
                //Log.d("TESTCODE","Code is: " + code);
                hash = code + "000000";
                hash = hash.substring(0, length);
            }
        }
        Log.d("ChatGPT", hash);
        return hash;
    }

}
