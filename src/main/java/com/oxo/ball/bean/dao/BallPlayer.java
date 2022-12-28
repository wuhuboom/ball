package com.oxo.ball.bean.dao;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;

import java.sql.Blob;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.*;
import org.apache.commons.lang3.StringUtils;


/**
 * <p>
 * 玩家账号
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Data
@TableName("ball_player")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BallPlayer extends BaseDAO {

    private static final long serialVersionUID = 1L;
    private static String[] ACCOUNT_TYPE = {"测试号","正式号"};

    @Version
    private Long version;
    /**
     * 会员ID
     */
    private Long userId;
    /**
     * 0否 1是
     */
    private Integer proxyPlayer;
    /**
     * 是否在线 0否1是
     * 在线状态 20分钟内有操作就在线
     * 操作过滤器配合rediskey,过期则离线
     */
    private Integer statusOnline;
    /**
     * 层级,根据所处的树结构计算
     */
    private Integer vipRank;

//    old >>>
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    @JsonIgnore
    private String password;
    /**
     * 支付提现密码
     */
    @JsonIgnore
    private String payPassword;
    /**
     * 测试账号密码
     */
    private String passwordTest;
    /**
     * 钱包余额(2位小数/100)
     */
    private Long balance;

    /**
     * 投注冻结
     */
    private Long frozenBet;
    /**
     * 提现冻结
     */
    private Long frozenWithdrawal;
    /**
     * 邀请码(每个账户生成的时候就会生成唯一性)
     */
    private String invitationCode;

    /**
     * 直属上级(添加索引)
     */
    private Long superiorId;
    /**
     * 直属上级username
     */
    private String superiorName;

    /**
     * 状态1正常 2封禁
     */
    private Integer status;

    /**
     * 账号类型 1测试号 2正常号 3代理账号
     */
    private Integer accountType;
    /**
     * 最新的一次登录ip
     */
    private String theNewIp;
    private Long theNewLoginTime;

    /**
     * 上一次登录的ip
     */
    private String theLastIp;

    /**
     * 会员的级别
     */
    private Integer vipLevel;
    /**
     * 历史最高等级
     */
    private Integer vipLevelMax;


    /**
     * 累计中奖
     */
    private Long cumulativeWinning;

    /**
     * 推广收入
     */
    private Long promoteIncome;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;
    /**
     * 手机区号
     */
    private String areaCode;

    /**
     * 团队人数
     */
    private Integer groupSize;

    /**
     * 累计提现
     */
    private Long cumulativeReflect;
    /**
     * 提现次数
     */
    private Integer reflectTimes;
    /**
     * 最大提现金额
     */
    private Long maxReflect;
    /**
     * 首次提现金额
     */
    private Long firstReflect;

    /**
     * 累计充值
     */
    private Long cumulativeTopUp;
    /**
     * 累计优惠,充值优惠+充值返利
     */
    private Long cumulativeDiscount;
    /**
     * 累计盈利返利+投注返利
     */
    private Long cumulativeActivity;
    /**
     * 充值次数
     */
    private Integer topUpTimes;
    /**
     * 最大的充值金额
     */
    private Long maxTopUp;

    /**
     * 首次充值金额
     */
    private Long firstTopUp;
    /**
     * 次充
     */
    private Long secondTopUp;

    /**
     * 首次充值金额时间
     */
    private Long firstTopUpTime;
    /**
     * 次充时间
     */
    private Long secondTopUpTime;

    /**
     * 线上充值金额
     */
    private Long onLineTopUp;

    /**
     * 线下充值金额
     */
    private Long offlineTopUp;

    /**
     * 人工加款
     */
    private Long artificialAdd;

    /**
     * 人工减款
     */
    private Long artificialSubtract;

    /**
     * 累计打码量
     */
    private Long cumulativeQr;

    /**
     * 离下次提现所需要的打码量
     */
    private Long needQr;

    /**
     * 累计投注金额
     */
    private Long accumulativeBet;

    /**
     * 累计反水
     */
    private Long cumulativeBackWater;

    /**
     * 直属下级个数
     */
    private Integer directlySubordinateNum;

    /**
     * 备注
     */
    private String remark;

    /**
     * 层级关系树
     * _id_id_..._id_,
     */
    private String superTree;
    /**
     * 是否达到活跃天数
     * 连续N天投注
     */
    private Integer actived;

    /**
     * 达到目标的有效邀请人数
     */
    private Integer invitationCount;

//    private String loginIps;
    private String loginContry;

    @TableField(exist = false)
    private String editPwd;
    @TableField(exist = false)
    private String editPayPwd;

    @TableField(exist = false)
    private String ip;
    @TableField(exist = false)
    private String currRate;
    @TableField(exist = false)
    private String currencySymbol;
    @TableField(exist = false)
    private String currencyName;
    @TableField(exist = false)
    private Integer treeType;
    @TableField(exist = false)
    private Integer offlines;
    @TableField(exist = false)
    private Integer timeType;
    @TableField(exist = false)
    private String begin;
    @TableField(exist = false)
    private String end;
    @TableField(exist = false)
    private Integer qrmult;
    @TableField(exist = false)
    private String qrRemark;
    @TableField(exist = false)
    private String operUser;
    @TableField(exist = false)
    private Double dbalance;
    @TableField(exist = false)
    private Integer balanceType;
    @TableField(exist = false)
    private Integer bcount;
    @TableField(exist = false)
    @JsonIgnore
    private Set<String> betDays;

    public Integer getBetDaysString(){
        if(betDays==null){
            return 0;
        }
        return betDays.size();
    }
    public Integer getLevelStr(){
//        if(StringUtils.isBlank(superTree)){
//            return 0;
//        }
//        if("_".equals(superTree)){
//            return 1;
//        }
//        return superTree.split("_").length;
        return vipRank;
    }
    public static Integer getTreeCount(String superTree){
        if(StringUtils.isBlank(superTree)){
            return 1;
        }
        if("_".equals(superTree)){
            return 1;
        }
        return superTree.split("_").length;
    }
    public static String getAccountType(int accountType){
        return ACCOUNT_TYPE[accountType-1];
    }
}
