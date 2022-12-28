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

import java.util.List;

/**
 * <p>
 * 游戏赛事-报表
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("ball_game_report")
@JsonIgnoreProperties(ignoreUnknown = true)
public class BallGameReport {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;
    private String ymd;
    private Long ymdStamp;
    private Integer gameCount;
    private Integer betGameCount;
    private Integer notStart;
    private Integer betPlayerCount;
    private Integer betCounts;
    private Long betBalance;
    private Integer betBingoCount;
    private Long betBingoBalance;
    private Long betNotBingoBalance;
    private Long betHandMoney;
    private Long betWinLose;
    private String betWinLosePerPlayer;
    private String betWinLosePer;
    private Integer bbetPlayerCount;
    private Integer bbetCounts;
    private Long bbetBalance;
    private Integer bbetBingoCount;
    private Long bbetBingoBalance;
    private Long bbetNotBingoBalance;
    private Long bbetHandMoney;
    private Long bbetWinLose;
    private String bbetWinLosePerPlayer;
    private String bbetWinLosePer;

    @TableField(exist = false)
    private Integer timeType;
    @TableField(exist = false)
    private String begin;
    @TableField(exist = false)
    private String end;
}
