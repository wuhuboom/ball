package com.oxo.ball.bean.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.oxo.ball.bean.dao.BaseDAO;
import java.io.Serializable;

import com.oxo.ball.utils.BigDecimalUtil;
import lombok.*;

/**
 * <p>
 * 系统配置
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Data
@TableName("ball_system_config")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BallSystemConfig implements Serializable{

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 注册是否需要邀请码 1需要 0不需要
     */
    private Integer registerIfNeedVerificationCode;

    /**
     * 验证码 格式 1 纯数字 2串字母 3字母数字
     */
    private Integer verificationCodeLayout;

    /**
     * 密码连续错误的次数
     */
    private Integer passwordMaxErrorTimes;

    /**
     * 密码连续错误锁屏时间(秒)
     */
    private Integer passwordErrorLockTime;

    /**
     * 客服连接
     */
    private String serverUrl;
    /**
     * 临时客服
     */
    private String serverUrlTmp;

    /**
     * 会员最多绑卡数量
     */
    private Integer cardCanNeedNums;

    /**
     * 充值打码量转换比例
     */
    private Long rechargeCodeConversionRate;
    @TableField(exist = false)
    private String rechargeCodeConversionRateStr;

    /**
     * 用户打码设量设置阀值
     */
    private Long captchaThreshold;

    /**
     * 投注手续费率
     */
    private Integer betHandMoneyRate;
    @TableField(exist = false)
    private String betHandMoneyRateStr;
    /**
     * 快捷金额
     */
    private String fastMoney;

    /**
     * 提现usdt自动汇率
     * 1打开2关闭
     */
    private Integer withdrawUsdtAutomaticPer;

    /**
     * 保本扣除手续费
     * 1开2关
     */
    private Integer evenNeedHandMoney;

    /**
     * 最多可绑定usdt账号数量
     */
    private Integer maxUsdtAccountNums;

    /**
     * 最多可绑定pix账号数量
     */
    private Integer maxPixAccountNums;

    /**
     * 提现密码是否可以修改 1 可以 2不可以
     */
    private Integer withdrawPasswordCanUpdate;
    /**
     * 是否可以连续发起提现
     */
    private Integer canWithdrawContinuity;

    /**
     * 控制首页提现密码是否可以关闭
     */
    private Integer withdrawPasswordShowNeed;

    /**
     * 每日的提现上线次数
     */
    private Integer everydayWithdrawTimes;
    /**
     * 每日的提现免手续费
     */
    private Integer everydayWithdrawFree;
    /**
     * 提现手续费率
     */
    private Integer withdrawalRate;
    @TableField(exist = false)
    private String withdrawalRateStr;
    private Integer withdrawMax;
    private Integer withdrawMin;
    private Integer withdrawalRateMax;
    private Integer withdrawalRateMin;

    /**
     * usdt 提现手续费
     */
    private Integer usdtWithdrawalRate;
    @TableField(exist = false)
    private String usdtWithdrawalRateStr;
    /**
     * TODO 转usdt汇率
     */
    private String usdtWithdrawPer;

    @TableField(exist = false)
    private String usdtWithdrawPerStr;

    private Integer usdtWithdrawMax;
    private Integer usdtWithdrawMin;
    private Integer usdtWithdrawalRateMax;
    private Integer usdtWithdrawalRateMin;

    /**
     * 手机区号
     */
    private String phoneAreaCode;
    /**
     * 修改支付密码是否需要手机验证码
     */
    private Integer payPwdNpc;
    /**
     * 默认代理账号
     */
    private String defaultProxy;

    /**
     * 区号支付开关
     */
    private Integer rechargeAreaSwtich;
    /**
     * 手动充值上限
     */
    private Integer rechargeMax;

    /**
     * 全盘最高投注
     */
    private Integer playerBetMax;
    private Integer playerBetMin;

    /**
     *
     */
    private Integer openGoogle;
    private Integer openWhite;
    private Long version;

    /**
     * 新设备需要验证 0否1是
     */
    private Integer newDevices;
    /**
     * 线上支付是否自动上分 0否1是
     */
    private Integer autoUp;
    /**
     *线下支付是否自动上下分0否1是
     */
    private Integer autoUpOff;

    /**
     * 周返利开关 0关1开
     */
    private Integer switchRebate;
    private Integer rebateWeek;
    private String rebateTime;

    /**
     * 验证码次数/天
     */
    private Integer maxSms;
    /**
     * 首充优惠开关
     */
    private Integer switchRebateFirst;
    /**
     * 每次优惠开关
     */
    private Integer switchRebateEvery;

    /**
     * 未充值不能邀请0关1开
     */
    private Integer switchNoRecharge;

    /**
     * 限制拉充值次数
     */
    private Integer reMax;
    /**
     * 限制分钟时间内
     */
    private Integer reTime;

    /**
     * 检测会员等级的时间
     */
    private String checkLevelTime;

    /**
     * 是否关闭TG报警 0否1是
     */
    private Integer closeNotice;
    /**
     * 待办模式 0 权限模式  1权限指定代理模式
     */
    private Integer todoModel;
    /**
     * 赛事时长
     */
    private Integer gameFinishMin;
    /**
     * 统计类日期配置
     *  0.创建时间 1确认时间
     */
    private Integer statisTime;
    /**
     * VIP奖励时间
     */
    private String vipRewardTime;

    /**
     * 验证码频率/分钟
     */
    private Integer smsInterval;

    /**
     * 其它汇率
     */
    private String euroRate;
    /**
     * 银行卡开关 0关1开
     */
    private Integer bankListSwtich;


    /**
     * 是否自动审核 0否1是
     */
    private Integer autoCheck;
    /**
     * 超过N小时自动审核
     */
    private String autoCheckTime;

    /**
     * 提现间隔
     */
    private Integer wiInterval;
}
