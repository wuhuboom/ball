package com.oxo.ball.bean.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.oxo.ball.bean.dao.BaseDAO;
import java.io.Serializable;

import lombok.*;

/**
 * <p>
 * 支付管理
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ball_payment_management")
@JsonIgnoreProperties(ignoreUnknown = true)
public class BallPaymentManagement extends BaseDAO {

    private static final long serialVersionUID = 1L;
    /**
     * 国家ID
     */
    private String country;
    /**
     * 支付渠道
     */
    private String name;
    /**
     * 支付类型
     * 1.usdt
     * 2.ID-印度
     * 3.加纳
     * 4.印度-fastpay
     * 5.印度-3
     * 6.印度-wow
     * 7.印度-allpay
     * 8.加纳-tznpay
     * 9.加纳-metapay
     */
    private Integer payType;
    /**
     * 货币符号
     */
    private String currencySymbol;
    /**
     * 支付类型
     * 1.线上
     * 2.线下
     */
    private Integer payTypeOnff;
    /**
     * 请求支付地址
     */
    private String ustdServer;
    /**
     * 回调地址
     */
    private String ustdCallback;
    /**
     * 回调查询地址,用于查询是哪个支付方式
     */
    private String ustdCallbackPath;
    /**
     * 前台是否显示 1显示 2不显示 3维护
     */
    private Integer frontDisplay;
    /**
     * 加密密钥
     */
    private String publicKey;

    /**
     * 解密密钥
     */
    private String privateKey;
    /**
     * 备注
     */
    private String remark;
    /**
     * 显示图片
     */
    private String img;

    /**
     * 快捷金额
     */
    private String fastMoney;
    /**
     * 充值区间
     */
    private String minMax;
    /**
     * 汇率,USTD->系统余额
     */
    private String rate;

    /**
     * 服务区号
     */
    private String areaCode;


    /**
     * 印度支付专用字段
     */
    /**
     * 商户号
     */
    private String merchantNo;
    /**
     * 支付代码
     */
    private String paymentCode;

    /**
     * 支付成功跳转地址
     */
    private String returnUrl;
    /**
     * 支付查询地址
     */
    private String queryUrl;
    /**
     * 支付密钥
     */
    private String paymentKey;


    /**
     * ================================加纳支付专用字段
     * 商品名
     */
    private String goodsName;

    /**
     * 0否1是
     */
    private Integer unhold;
    /**
     * 维护消息
     */
    private String unholdMessage;

    private Integer sort;

    private String whiteIp;
}
