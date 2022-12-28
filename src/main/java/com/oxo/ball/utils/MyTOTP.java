package com.oxo.ball.utils;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Implementation of MyTOTP: Time-based One-time Password Algorithm
 *
 * @author thoeger
 */
public final class MyTOTP {

    private MyTOTP() {
        // private utility class constructor
    }

    /**
     * @param key - secret credential key (HEX)
     * @return the OTP
     */
    public static String getOTP(String key) {
        return MyTOTP.getOTP(MyTOTP.getStep(), key);
    }

    /**
     * @param key - secret credential key (HEX)
     * @param otp - OTP to validate
     * @return valid?
     */
    public static boolean validate(final String key, final String otp) {
        return MyTOTP.validate(MyTOTP.getStep(), key, otp)
                ||MyTOTP.validate(MyTOTP.getStep(System.currentTimeMillis()-30000), key, otp)
                ||MyTOTP.validate(MyTOTP.getStep(System.currentTimeMillis()+30000), key, otp);
    }

    private static boolean validate(final long step, final String key, final String otp) {
        String code = MyTOTP.getOTP(step, key);
        return code.equals(otp);
    }

    private static long getStep() {
        // 30 seconds StepSize (ID MyTOTP)
        return System.currentTimeMillis() / 30000;
    }
    private static long getStep(Long time) {
        // 30 seconds StepSize (ID MyTOTP)
        return time / 30000;
    }

    private static String getOTP(final long step, final String key) {
        String steps = Long.toHexString(step).toUpperCase();
        while (steps.length() < 16) {
            steps = "0" + steps;
        }

        // Get the HEX in a Byte[]
        final byte[] msg = MyTOTP.hexStr2Bytes(steps);
        final byte[] k = MyTOTP.hexStr2Bytes(key);

        final byte[] hash = MyTOTP.hmac_sha1(k, msg);

        // put selected bytes into result int
        final int offset = hash[hash.length - 1] & 0xf;
        final int binary = ((hash[offset] & 0x7f) << 24) | ((hash[offset + 1] & 0xff) << 16) | ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);
        final int otp = binary % 1000000;

        String result = Integer.toString(otp);
        while (result.length() < 6) {
            result = "0" + result;
        }
        return result;
    }

    /**
     * This method converts HEX string to Byte[]
     *
     * @param hex the HEX string
     *
     * @return A byte array
     */
    private static byte[] hexStr2Bytes(final String hex) {
        // Adding one byte to get the right conversion
        // values starting with "0" can be converted
        final byte[] bArray = new BigInteger("10" + hex, 16).toByteArray();
        final byte[] ret = new byte[bArray.length - 1];

        // Copy all the REAL bytes, not the "first"
        System.arraycopy(bArray, 1, ret, 0, ret.length);
        return ret;
    }

    /**
     * This method uses the JCE to provide the crypto algorithm. HMAC computes a Hashed Message Authentication Code with the crypto hash
     * algorithm as a parameter.
     *
     * @param keyBytes the bytes to use for the HMAC key
     * @param text the message or text to be authenticated.
     */
    private static byte[] hmac_sha1(final byte[] keyBytes, final byte[] text) {
        try {
            final Mac hmac = Mac.getInstance("HmacSHA1");
            final SecretKeySpec macKey = new SecretKeySpec(keyBytes, "RAW");
            hmac.init(macKey);
            return hmac.doFinal(text);
        } catch (final GeneralSecurityException gse) {
            throw new UndeclaredThrowableException(gse);
        }
    }

}

