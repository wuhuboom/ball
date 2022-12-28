package com.oxo.ball.bean.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 代付管理
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ball_pay_behalf")
public class BallPayBehalf extends BaseDAO {
    /**
     * 下单成功
     * 处理中
     * 已代付
     * 未代付
     * 已取消
     */
    public static final String[] BEHALF_STATUS = new String[]{"下单成功","处理中","已代付","未代付","已取消"};

    private static final long serialVersionUID = 1L;
    /**
     * 代付名称
     */
    private String name;

//    private String country;
    private Long countryId;
    /**
     * 代付类型 1印度 2加纳 3本地代付
     */
    private Integer payType;
    /**
     * 1.印度 2加纳 3本地 4印度fast 5印度UPI 6印度wow 7印度allPay 8加纳tznpay
     */
    private Integer payType2;
    /**
     * 关联银行
     */
    private Long areaId;
    /**
     * 汇率
     */
    private String rate;
    private Integer status;
    /**
     * 发起代付地址
     */
    private String serverUrl;
    /**
     * 代付回调地址
     */
    private String localCallback;
    /**
     * 备注
     */
    private String remark;
    /**
     * 回调地址用于自查
     */
    private String callbackPath;
    /**
     * 后台商户号
     */
    private String merchantNo;
    /**
     * 代付查询地址
     */
    private String queryUrl;
    /**
     * 代付密钥
     */
    private String paymentKey;
    /**
     * IFSC,印度网银代付必填
     */
    private String bankIfsc;
    /**
     * 分行代码
     */
    private String bankSub;
    /**
     * 账号类型,收款账号类型,BRL货币必填
     */
    private String accountType;
    private String accountAttach;
    private String documentType;
    private String documentNo;
    private String mobileNo;
    private String whiteIp;

}
