package com.oxo.ball.bean.dto.req.player;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class GameFinishRequest {
    /**
     * 开赛时间
     * 0 全部 1今天 2最近七天
     */
    @NotNull(message = "startTimeIsNull")
    @Min(value = 0,message = "startTimeError")
    @Max(value = 2,message = "startTimeError")
    private Integer startTime;

    private String teamName="";
}
