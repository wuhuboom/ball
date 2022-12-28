package com.oxo.ball.utils.cha;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("all")
public class SignUtil {

    public static String sortData(Map<String, ?> sourceMap) {
        //log.info("sortData sourceMap:" + sourceMap);
        String returnStr = sortData(sourceMap, "&");
        //log.info("sortData returnStr:" + returnStr);
        return returnStr;
    }

    public static String sortData(Map<String, ?> sourceMap, String link) {
        //log.info("start sortData method()");
        if (StringUtils.isEmpty(link)) {
            link = "&";
        }
        Map<String, Object> sortedMap = new TreeMap<String, Object>();
        sortedMap.putAll(sourceMap);
        Set<Entry<String, Object>> entrySet = sortedMap.entrySet();
        StringBuffer sbf = new StringBuffer();
        for (Entry<String, Object> entry : entrySet) {
            if (null != entry.getValue() && StringUtils.isNotEmpty(entry.getValue().toString())) {
                sbf.append(entry.getKey()).append("=").append(entry.getValue()).append(link);
            }
        }
        String returnStr = sbf.toString();
        if (returnStr.endsWith(link)) {
            returnStr = returnStr.substring(0, returnStr.length() - 1);
        }
        return returnStr;
    }

    /**
     * 灏嗚姹傚瓧绗︿覆瑙ｆ瀽涓篗ap
     * @param strParams  璇锋眰瀛楃涓�    key1=value1&key2=value2....&keyN=valueN
     * @return
     */
    public static Map parseParams(String strParams) {
        Map<String, String> map = new HashMap();
        if (!strParams.equals(""))
        {
          String[] list = strParams.split("&");
          for (int i = 0; i < list.length; i++)
          {
              String tmp = list[i];
            map.put(tmp.substring(0, tmp.indexOf("=")), tmp.substring(tmp.indexOf("=") + 1));
          }
        }
        return map;
    }    
}
