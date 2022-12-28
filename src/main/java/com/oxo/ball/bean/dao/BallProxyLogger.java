package com.oxo.ball.bean.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 代理每日统计
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("ball_proxy_logger")
@JsonIgnoreProperties(ignoreUnknown = true)
public class BallProxyLogger {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long playerId;
    /**
     * 统计日期
     */
    private String ymd;
    /**
     * 统计日期时间戳
     */
    private Long ymdStamp;
    /**
     * 账号名
     */
    private String playerName;
    /**
     * 账号类型
     */
    private Integer playerType;
    /**
     * 层级
     */
    private Integer level;
    /**
     * 直属下级数
     */
    private Integer subCount;
    /**
     * 总下级数
     */
    private Integer subAllCount;
    /**
     * 新增注册
     */
    private Integer registCount;
    /**
     * 下级返利
     */
    private Long rebateCount;
    /**
     * 充值人数
     */
    private Integer payCount;
    /**
     * 首充人数
     */
    private Integer firstPayCount;
    /**
     * 提现人数
     */
    private Integer withdrawalCount;
    /**
     * 线上充值金额
     */
    private Long payCountOnline;
    /**
     * 线下充值金额
     */
    private Long payCountOffline;
    /**
     * 人工充值金额
     */
    private Long payCountHands;
    /**
     * 线上提现
     */
    private Long withdrawalCountOnline;
    /**
     * 线下提现
     */
    private Long withdrawalCountOffline;
    /**
     * 人工提现
     */
    private Long withdrawalCountHands;
    /**
     * 下注人数
     */
    private Integer betCountPlayer;
    /**
     * 下注次数
     */
    private Integer betCount;
    /**
     * 充提差不包人工
     */
    private Long inOut;
    /**
     * 充提差包人工
     */
    private Long inOutAll;

    @TableField(exist = false)
    private String begin;
    @TableField(exist = false)
    private String end;
    @TableField(exist = false)
    private Integer self;
    /**
     * 是否统计代理线
     */
    @TableField(exist = false)
    private Integer proxyLine;


}
