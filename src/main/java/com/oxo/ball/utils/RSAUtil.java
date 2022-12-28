package com.oxo.ball.utils;

import com.oxo.ball.bean.dto.api.PayParamDto;
import org.omg.CORBA.INTERNAL;
import springfox.documentation.spring.web.json.Json;

import javax.crypto.Cipher;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class RSAUtil {
    //
    /**
     * RSA公钥加密
     *
     * @param str    加密字符串
     * @param publicKey 公钥
     * @return 密文
     * @throws Exception 加密过程中的异常信息
     */
    public static String encrypt(String str, String publicKey) throws Exception {
        //base64编码的公钥
        byte[] decoded = Base64.getDecoder().decode(publicKey);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
        //RSA加密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        String outStr = Base64.getEncoder().encodeToString(cipher.doFinal(str.getBytes("UTF-8")));
        return outStr;
    }

    /**
     * RSA私钥解密
     *
     * @param str    加密字符串
     * @param privateKey 私钥
     * @return 明文
     * @throws Exception 解密过程中的异常信息
     */
    public static String decrypt(String str, String privateKey) throws Exception {
        //64位解码加密后的字符串
        byte[] inputByte = Base64.getDecoder().decode(str);
        //base64编码的私钥
        byte[] decoded = Base64.getDecoder().decode(privateKey);
        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
        //RSA解密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, priKey);
        String outStr = new String(cipher.doFinal(inputByte));
        return outStr;
    }
    public static Map<Integer, String> keyMap = new HashMap<>();

    static {
        keyMap.put(0, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoEixcgAPmLpHLEDh3P8e"+
                "GpxolNbGJoxbrNQU1kaRCTMiu5qTaLJsTb6SVh7J4yztLOSdvIwbC2YeyVW8fatx"+
                "3eQ4RX6/txdtm07ov1bmC9n6/caOeRz2Pq2ZOse3uFuSjpQbF/2oAv3E6zWq5tdH"+
                "wG89ZNj+igs5lme4S6Uy2OE2MsqV/kwGMdBcdTOld8ki3MTsoEeBg9+IoqRD6gqi"+
                "l9sZdoHf0ItVE85Rw2Gp1rMfeTUMW7W3SvKItB33978/PgVmUvKLwY9+xsvWmILB"+
                "ZgkIMjUZ9/98LsyhdOvElcWFkYX2f84PSIJ8roAJhUpGJnw05i1jykDM6wa3sZ05"+
                "1wIDAQAB");
        keyMap.put(1, "MIIEpAIBAAKCAQEAy3My0diBGfkjFzq5UVl0SeOLSg/Lcmvhl9hEuRr6B7O7KV7g" +
                "hYCQR2tJcfrZQ1ehqPVS1jskNkoXKXfcAzeEgQrcFLYMVwuHVh2mu8imUoKY7fke" +
                "arU6MtJHi2bpxownZLJzurbzbeeWiZWj05HzCZVPjfAhxkTdC+kuZBJfF0Fc6xrl" +
                "XgbDslsEyEyKIGku7G47ZRmtJjDiUk+Bec7f9uhTbSWWu4ZO57S4fuA5K1qXf0Tm" +
                "w6fEiM5DRkfYGsmO+2x6AmHGwVhFw37k/UEpur3bkajK9fk3s6xtJjmLet3y+g6j" +
                "cdpPMr+sZdMFxfGhIqu0xPy2mNNZmANJni3/PQIDAQABAoIBAA8LL6DQr4sqHuwi" +
                "zX00biLgjnYlgNevHnlJ5psBYaecJKTEfTmh7gk5565j7BjMrAmASmXI7b6N7/SD" +
                "BmO+gS/Bi9CEPZlaIuG9Q4zzI0lKmuBN4W/mgq0rW1r1eyfRSUBq6Z/O02U3EKyP" +
                "whNs4Vm+DqniLb0pbmbpESMZMKrZaqXSRMQHfwp7wx8W+3dPEQAs3pO1SwEpfL8S" +
                "bx/gBaRUi5iVIjG52G7daoCCGAPzyzhGtKNkc/Yff9dnI7hGwS2/zysrOychDSKJ" +
                "+/XGlQb9Zwp8F7bz84wm3BACTCCjzvGd5NxIZqXoeR8VZ9LWpJSlRIMIskExhlp/" +
                "hFfZtckCgYEA8Vc/m8frL879ioxSkbcaFYDuvS0FFbAZWh9z2XTJGgSWQlQ0Ms5k" +
                "U/668c7VXOlLrbUQrvH+aAp4uok+o+hlZNTbFJhRXs2igfIKpC4Ak8tYkbrsxmpJ" +
                "gP9/LScLlgTmrrFSo6ADJU0oPSpC95HwvwyIao9fpauhFYZtBf5s7o8CgYEA187D" +
                "YOuRbmHKbYLCXe37grEGerOz4WUpSWkaFmlwZ14T8i/U0ps/qRiiOOdfrEnjAT3L" +
                "JkZ1GL0fBLqN2KAkD7XjcOECcVYQMN/5qwEys9udmbgj+I0u5RSeE8mYB0RP06/q" +
                "Cg8NocxH7Z5STjd3JA1gZwl974uwcN1mkdduW3MCgYEAyUOXmlRowCAAtRA8s6Rd" +
                "Ll2tuznWKbYIDm54cHrCUt5MaNhMB6qzZJDkWk/BA5DTOfPsC9ln7l/9OqLGCG8A" +
                "T8xrP4ufIE6hHXk6gpySgq5sGGwolXeCAQARkRgkw2Em97yNTENfHDZyPkAGROwC" +
                "N3E+Oo+ClmjBF3BZb0w0j+UCgYA5Y46pc3uVMwQ14xP1DphXxOPINYmcYt572ytI" +
                "0nlFw8riGL4r04U2XoqlP0I9+tgXOGuRniL9lS1ugH3AIbX1R5VYKz4PDaf4l1c5" +
                "lnP5SGm8uy81pbXWzYjMEkwPgqcH0DwYuLATWtO16OhSTIWuXLBKNkf7L9aX7Qid" +
                "uABs6QKBgQCU6GhHoaIPAvVaMCm+sdT8MwH6ThH3a+kWqFj3AgkUBSv9UUhYQ+A6" +
                "Qey+AcRpKVP91z82noHucakJ/7EMTOnsnLK8mjrFak+L2GZWrPhk2bMZnF7vxGHu" +
                "FcVT4XWqDGY3GwdDTfX0WcJGtjyLJkD05ldO74jNYXVvIZvyN0abLw==");
        java.security.Security.addProvider(
                new org.bouncycastle.jce.provider.BouncyCastleProvider()
        );
    }

    public static PrivateKey getPrivateKey(String privateKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] decodedKey = org.apache.commons.codec.binary.Base64.decodeBase64(privateKey.getBytes());
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);

        PrivateKey key = keyFactory.generatePrivate(keySpec);
        Signature signature = Signature.getInstance("MD5withRSA");
        signature.initSign(key);
        signature.update("123".getBytes());
        System.out.println(new String(org.apache.commons.codec.binary.Base64.encodeBase64(signature.sign())));

        return keyFactory.generatePrivate(keySpec);
    }
    public static String sign(String data, String privateKeyStr) {
        try {
            PrivateKey privateKey = getPrivateKey(privateKeyStr);
            byte[] keyBytes = privateKey.getEncoded();
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey key = keyFactory.generatePrivate(keySpec);
            Signature signature = Signature.getInstance("MD5withRSA");
            signature.initSign(key);
            signature.update(data.getBytes());
            return new String(org.apache.commons.codec.binary.Base64.encodeBase64(signature.sign()));
        } catch (Exception e) {

        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        if(true){
            String string = sign("123","MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAK3/5b30bLvBVGYxJu20cIUA6KWt+uJhqWHMBECvriVa9ZdUPWaf3+o99N7tcO6WdAUKfqy80R9DxZyzYNhB2UUEjGIT16hbuu5xNowhz0fVzY1CMnqqtSrlcD5AepJDrlG2P/UsYHBz4rJZkN5vhtTjWgD2fBLiXSniDAgBnFT9AgMBAAECgYEAodDdw65QgLvm1bLYVS/9hHO36HzIWUgh0gV93vqCpdLVmwqIn6wG/TzpQXjoEbpx405DSDnA8aaBXJ2lbviuKOzkaLUjmi/8/GZGeM2U2/YSSFMjQncSttt2kxptoFA7CgZcfQ41POHTpEgxGuiDYotBtx5JdGizCOpnFSePwrECQQDUncM7P2201cmFn10ZuLELLqy1AkBJTWfqb9wO0+QyKNxCTisoFeExS3UE7UJAcnIrgeYYRAUYWoboKTZukQlzAkEA0YDzyVZ9ItMDm0kYd6QqlFOUtHRd9KVp+a9bfG4h418CFwXz+Y/8a+caiTiXFhp4u8H8Dft9sWHPBdk10OtLzwJBAJnO7xcDUafKKJjDqqQa2ejPAR2I3Vyp5IzSEw2e96vZp01IIyxCd8AjaCoCQTVCFZmnxP605kKx6no0pi3iOkUCQErqTMKjTc4FHNYLdldZ+eveU1+P+QUBnXqubQ16qF2Q3MN02oBUL5Q6KLAG72f1AVVKkaJCWvjyG8BgorDZYtsCQCR4Ya0ZsxeTAEYe3u0s5bcIsLvdjiFBVCGI74V7YGgsKtV6kH2wzW2Nke/Nc6a5Yvi6P5RjeZ81VNGtM0X/cHE=");
            System.out.println(string);
            return;
        }
//        genKeyPair();
        long temp = System.currentTimeMillis();
//        PayParamDto(username=aaa111, platformOrder=815145781, rechargeType=USDT, accountOrders=100.0, backUrl=http://45.76.146.183:10100/player/pay/callback/1)
        PayParamDto build = PayParamDto.builder()
                .username("aaa111")
                .platformOrder("815145781")
                .rechargeType("USDT")
                .accountOrders(100.0F)
                .backUrl("http://45.76.146.183:10100/player/pay/callback/1")
                .build();
        //生成公钥和私钥
        //加密字符串
//        System.out.println("公钥:" + keyMap.get(0));
//        System.out.println("私钥:" + keyMap.get(1));
//        System.out.println("生成密钥消耗时间:" + (System.currentTimeMillis() - temp) / 1000.0 + "秒");
//        //客户id + 授权时间 + 所用模块
//        String message = "{\"PlatformOrder\":\"2012553332254545254224252455\",\"RechargeAddress\":\"\",\"Username\":\"wing\",\"AccountOrders\":200,\"RechargeType\":\"USDT\",\"BackUrl\":\"https://123.com\"}";
//        System.out.println("原文:" + message);
//        temp = System.currentTimeMillis();
//        //通过原文，和公钥加密。
        String messageEn = encrypt(JsonUtil.toJson(build), keyMap.get(0));
        System.out.println("密文:" + messageEn);
//        System.out.println("加密消耗时间:" + (System.currentTimeMillis() - temp) / 1000.0 + "秒");
//        temp = System.currentTimeMillis();
//        //通过密文，和私钥解密。
//        messageEn = "FBeV//omznmzA/vTT8WA/Vn8mG7uiICHd2369rL8sP9OV22y7o/1rSK/cXl0ditVUVMzJOxp2F1zvbRglP2X31bVxqDB8vaohTwOXXDZsG8T2KQM0UWmSBGq594ey9Xqie/3cvoNfo2V9oH3OBYhh0I3YDP83sc95lE1b5MvYQnvOAx3Ox1Nm7sYfhhPtYEGXY48NOUnC+tkAGmmtPcX+QjgpM0/Yii/vNr14uIqXYVqBkFPUVNwW64Ipfz1cvVtof0Bi+1Qss0eai6EeAuuQjSbd7DDdnHOBo7EGcSbLlyfsI9JZJeVdVcoV57XCOPpV5gCtdqgPhYWeVISp7COhg==";
//        String messageDe = decrypt(messageEn, keyMap.get(1));
//        System.out.println("解密:" + messageDe);
//        System.out.println("解密消耗时间:" + (System.currentTimeMillis() - temp) / 1000.0 + "秒");

    }

}
