package com.oxo.ball.bean.dto.req.player;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class SubPlayersRequest {
    /**
     *1.全部，2.3天未登录，3.7天未登录
     */
    @Range(min = 1,max = 3,message = "timeError")
    private Integer time;

    /**
     *
     */
    private String username;
}
