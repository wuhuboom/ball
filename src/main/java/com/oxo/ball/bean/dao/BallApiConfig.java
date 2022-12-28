package com.oxo.ball.bean.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * <p>
 * 系统配置
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Data
@TableName("ball_api_config")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BallApiConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 足球key
     */
    private String ballApiKey;
    private String smsServer;
    private String smsAppKey;
    private String smsSecretKey;
    private String smsMessage;
    private Integer smsUnhold;
    private String smsUnholdMessage;

    /**
     * 赛事报警
     */
    private String tgToken;
    private String tgChat;

    /**
     * 待办报警
     */
    private String todoToken;
    private String todoChat;

    /**
     * 会员群消息
     */
    private String playerToken;
    private String playerChat;
    //首充消息
    private String firstRecharge;
    //次充消息
    private String secondRecharge;
    //固定日消息
    private String fixedRecharge;
    // 首充上级无返
    private String firstRecharge2;
    // 开关
    private Integer autoSend;
    // 范围
    private String minMax;
    // 频率,小时
    private Integer hourPer;
    // 类型，0随机
    private Integer typeSend;
}