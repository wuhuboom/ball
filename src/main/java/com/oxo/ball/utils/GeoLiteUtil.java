package com.oxo.ball.utils;

import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.Map;

//Error:(4, 26) java: 无法访问com.maxmind.geoip2.DatabaseReader
//        错误的类文件: C:\Users\ASUS\.m2\repository\com\maxmind\geoip2\geoip2\3.0.1\geoip2-3.0.1.jar(com/maxmind/geoip2/DatabaseReader.class)
//        类文件具有错误的版本 55.0, 应为 52.0
//        请删除该文件或确保该文件位于正确的类路径子目录中。
public class GeoLiteUtil {
    static DatabaseReader countryReader;
    static String getIp(String ip){
        Country country = null;
        try {
            // 读取当前工程下的IP库文件
            File countryFile = new File("ipdb/GeoLite2-Country.mmdb");
//            File cityFile = new File("ipdb/GeoLite2-City.mmdb");

            // 读取IP库文件
            DatabaseReader countryReader = (new DatabaseReader.Builder(countryFile).withCache(new CHMCache())).build();
//            DatabaseReader cityReader = (new DatabaseReader.Builder(cityFile).withCache(new CHMCache())).build();
            CountryResponse countryResponse = countryReader.country(InetAddress.getByName(ip));
            country = countryResponse.getCountry();
//            CityResponse cityResponse = cityReader.city(InetAddress.getByName(ip));
//            City city = cityResponse.getCity();

            System.out.println("从country IP库读取国家结果： " + country);
//            System.out.println("从city IP库读取国家结果：" + cityResponse.getCountry());
//            System.out.println("从city IP库读取城市结果：" + city);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return country.getName();
    }
    public static String getIpAddr(String ip){
        Country country = null;
        try {
            // 读取IP库文件
            CountryResponse countryResponse = countryReader.country(InetAddress.getByName(ip));
            country = countryResponse.getCountry();
        } catch (Exception e) {
            country = new Country();
        }
        String address = country.getNames().get("zh-CN");
        return StringUtils.isBlank(address)?"":address;
    }
    static {
        File countryFile = new File("ipdb/GeoLite2-Country.mmdb");
        try {
            countryReader = (new DatabaseReader.Builder(countryFile).withCache(new CHMCache())).build();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        System.out.println(getIpAddr("182.137.106.201"));
    }
}
