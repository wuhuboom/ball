package com.oxo.ball.utils;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.Closeable;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Set;

public class HttpUtil {
    /**
     * @param url-[请求地址] headMap-[请求头参数] paramMap-[请求参数]
     * @return String 返回结果
     * @Description post请求，请求体为JSON格式
     * @Author msx
     * @Date 2021-08-27 16:31
     */
    public static String doPost(String url, Map<String, String> header, String jsonParams) {
//        System.out.println(" = = 请求地址 = = > > > > > > " + url);
//        System.out.println(" = = 请求参数 = = > > > > > > " + jsonParams);
        //创建httpClient对象
        CloseableHttpClient httpClient = (CloseableHttpClient) wrapClient();
        CloseableHttpResponse response = null;
        String result = "";
        try {
            //创建http请求
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("Content-Type", "application/json");
            //创建请求内容
            StringEntity entity = new StringEntity(jsonParams, "utf-8");
            entity.setContentType("application/json");
            httpPost.setEntity(entity);
            // 设置请求头
            if (null != header && !header.isEmpty()) {
//                System.out.println(" = = 请求头 = = > > > > > > ");
                Set<Map.Entry<String, String>> entries = header.entrySet();
                for (Map.Entry<String, String> e : entries) {
//                    System.out.println(e.getKey() + ":" + e.getValue());
                    httpPost.setHeader(e.getKey(), e.getValue());
                }
            }
            response = httpClient.execute(httpPost);
            result = EntityUtils.toString(response.getEntity(), "utf-8");
//            System.out.println(" = = 请求返回 = = > > > > > > " + result);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //关闭资源
            closeResource(response, httpClient);
        }
        return result;
    }

    /**
     * @param url-[请求地址] headMap-[请求头参数] paramMap-[请求参数]
     * @return String 返回结果
     * @Description post请求，请求体为JSON格式
     * @Author msx
     * @Date 2021-08-27 16:31
     */
    public static String doGet(String url, Map<String, String> header) {
        //创建httpClient对象
        CloseableHttpClient httpClient = (CloseableHttpClient) wrapClient();
        CloseableHttpResponse response = null;
        String result = "";
        result = doGetCommon(url, header, httpClient, response, result, httpClient);
        return result;
    }
    public static String doGetProxy(String url, Map<String, String> header) {
        //创建httpClient对象
        CloseableHttpClient httpClient = (CloseableHttpClient) wrapClientProxy();
        CloseableHttpResponse response = null;
        String result = "";
        result = doGetCommon(url, header, httpClient, response, result, httpClient);
        return result;
    }

    private static String doGetCommon(String url, Map<String, String> header, CloseableHttpClient httpClient, CloseableHttpResponse response, String result, CloseableHttpClient httpClient2) {
        try {
            //创建http请求
            HttpGet httpGet = new HttpGet(url);
            // 设置请求头
            if (null != header && !header.isEmpty()) {
                Set<Map.Entry<String, String>> entries = header.entrySet();
                for (Map.Entry<String, String> e : entries) {
                    httpGet.setHeader(e.getKey(), e.getValue());
                }
            }
            response = httpClient.execute(httpGet);
            result = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (Exception e) {
        } finally {
            //关闭资源
            closeResource(response, httpClient2);
        }
        return result;
    }

    /**
     * @Description 关闭资源
     * @Author msx
     * @Date 2021/9/8 10:44
     */
    private static void closeResource(Closeable... resources) {
//        System.out.println("> > > > > > > > > > 关闭流资源 > > > > > > > > > >");
        try {
            for (Closeable resource : resources) {
                if (resource != null) {
                    resource.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //绕过证书
    public static HttpClient wrapClient() {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] arg0,
                                               String arg1) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] arg0,
                                               String arg1) throws CertificateException {
                }
            };
            ctx.init(null, new TrustManager[]{tm}, null);
            SSLConnectionSocketFactory ssf = new SSLConnectionSocketFactory(
                    ctx, NoopHostnameVerifier.INSTANCE);
            CloseableHttpClient httpclient = HttpClients.custom()
                    .setSSLSocketFactory(ssf).build();
            return httpclient;
        } catch (Exception e) {
            return HttpClients.createDefault();
        }
    }
    public static HttpClient wrapClientProxy() {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] arg0,
                                               String arg1) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] arg0,
                                               String arg1) throws CertificateException {
                }
            };
            ctx.init(null, new TrustManager[]{tm}, null);
            SSLConnectionSocketFactory ssf = new SSLConnectionSocketFactory(
                    ctx, NoopHostnameVerifier.INSTANCE);
            HttpHost proxy = new HttpHost("127.0.0.1", 7890);
            RequestConfig defaultRequestConfig = RequestConfig.custom()
                    .setConnectTimeout(3000).setSocketTimeout(3000)
                    .setProxy(proxy).build();
            CloseableHttpClient httpclient = HttpClients.custom()
                    .setDefaultRequestConfig(defaultRequestConfig)
                    .setSSLSocketFactory(ssf).build();

            return httpclient;
        } catch (Exception e) {
            return HttpClients.createDefault();
        }
    }

}
