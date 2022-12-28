package com.oxo.ball.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Combination {
    public static void main(String[] args) {
//        String str = "0123456789abcdefghijklmnopqrstuvwxyz";
//        List<String> cr = getCombinationResult(3, stringFilter(str));
//        for(String s: cr){
//            System.out.println(s);
//        }
//        System.out.println(cr.size());

        String[] str = {"1","2","3","4"};
        int total = 2;
        List<String> list = Arrays.asList(str);
        recursive("", total, list);
    }

    /**
     * 对字符串中元素进行重排序
     * 此外还可以在该方法对元素进行去重等
     * @param str  原字符串
     * @return  目标字符串
     */
    public static String stringFilter(String str){
        char[] c = str.toCharArray();
        Arrays.sort(c);
        return new String(c);
    }

    /**
     * 得到组合结果
     * @param num   从N个数中选取num个数
     * @param str   包含Ng个元素的字符串
     * @return  组合结果
     */
    public static List<String> getCombinationResult(int num, String str) {
        List<String> result = new ArrayList<String>();
        if (num == 1) {
            for (char c : str.toCharArray()) {
                result.add(String.valueOf(c));
            }
            return result;
        }
        if (num >= str.length()) {
            result.add(str);
            return result;
        }
        int strlen = str.length();
        for (int i = 0; i < (strlen - num + 1); i++) {
            //从i+1处直至字符串末尾
            List<String> cr = getCombinationResult(num - 1, str.substring(i + 1));
            //得到上面被去掉的字符，进行组合
            char c = str.charAt(i);
            for (String s : cr) {
                result.add(c + s);
            }
        }
        return result;
    }

    /**
     *
     * @param prefix 拼接结果前缀
     * @param total 需要从N个数中取total个数
     * @param list  含有N个数的集合
     */
    public static void recursive(String prefix,int total, List<String> list) {
        //总的要循环多少次
        for(int i=0;i<list.size();i++){
            LinkedList<String> tempList = new LinkedList<String>(list);
            //复制一份list，不能对原list进行操作
            String remove = tempList.remove(i);
            //从集合中移出某个元素,防止出现重复数字
            String s = prefix+remove;
            //拼接结果
            if(total == 1){
                //当从N个数中取1个数时
                System.out.println(s);
                //直接输出结果
            }else{
                int temp = total-1;
                //每到这里就得减一层
                recursive(s, temp, tempList);
                //这里所传的值都是新值
            }
        }
    }
}
