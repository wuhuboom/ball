package com.oxo.ball.utils;

import com.sun.org.apache.xpath.internal.operations.Bool;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UUIDUtil {

    public static String getUUID(){
        return UUID.randomUUID().toString().replace("-","");
    }

    public static  Pattern CHAR_ZH = Pattern.compile("[\u4E00-\u9FFF]");

    public static void main(String[] args) {
        System.out.println(Arrays.toString(getRandomIntArr(20)));
    }

    public static Boolean hasZhChar(String str){
        Matcher matcher = CHAR_ZH.matcher(str);
        return matcher.find();
    }
    public static int[] getRandomIntArr(int size){
        Set<Integer> sets = new HashSet<>();
        int arr[] = new int[size];
        int i=0;
        while (sets.size()<size){
            int randomNum = TimeUtil.getRandomNum(60, 3600);
            boolean add = sets.add(randomNum);
            if(add){
                arr[i++] = randomNum;
            }
        }
        Arrays.sort(arr);
        return  arr;
    }

    public static String urlDecode(String string){
        try {
            return URLDecoder.decode(string,"UTF8");
        } catch (UnsupportedEncodingException e) {
        }
        return "";
    }

    public static String getServletPath(String url){
        try {
            url = url.substring(url.indexOf("pay/callback")-1);
        }catch (Exception ex){
        }
        return url;
    }

    /**
     * 对字符串处理:将指定位置到指定位置的字符以星号代替
     *
     * @param content
     *            传入的字符串
     * @param begin
     *            开始位置
     * @param end
     *            结束位置
     * @return
     */
    public static String getStarString(String content, int begin, int end) {

        if (begin >= content.length() || begin < 0) {
            return content;
        }
        if (end >= content.length() || end < 0) {
            return content;
        }
        if (begin >= end) {
            return content;
        }
        String starStr = "";
        for (int i = begin; i < end; i++) {
            starStr = starStr + "*";
        }
        return content.substring(0, begin) + starStr + content.substring(end, content.length());
    }




    /**
     * 对字符加星号处理：除前面几位和后面几位外，其他的字符以星号代替
     *
     * @param content
     *            传入的字符串
     * @param frontNum
     *            保留前面字符的位数
     * @param endNum
     *            保留后面字符的位数
     * @return 带星号的字符串
     */

    public static String getStarString2(String content, int frontNum, int endNum) {

        if (frontNum >= content.length() || frontNum < 0) {
            return content;
        }
        if (endNum >= content.length() || endNum < 0) {
            return content;
        }
        if (frontNum + endNum >= content.length()) {
            return content;
        }
        String starStr = "";
        for (int i = 0; i < (content.length() - frontNum - endNum); i++) {
            starStr = starStr + "*";
        }
        return content.substring(0, frontNum) + starStr
                + content.substring(content.length() - endNum, content.length());
    }

    public static String getRandomUsername() {
        String val = "";
        //参数length，表示生成几位随机数
        for (int i = 0; i < TimeUtil.getRandomNum(5,12); i++) {
            String charOrNum =i==0?"char":TimeUtil.random.nextInt(2) % 2 == 0 ? "char" : "num";
            //输出字母还是数字
            if ("char" .equalsIgnoreCase(charOrNum)) {
                //输出是大写字母还是小写字母
                int temp = TimeUtil.random.nextInt(2) % 2 == 0 ? 65 : 97;
                val += (char) (TimeUtil.random.nextInt(26) + temp);
            } else if ("num" .equalsIgnoreCase(charOrNum)) {
                val += String.valueOf(TimeUtil.random.nextInt(10));
            }
        }
        return getStarString2(val,1,1);
    }

}
