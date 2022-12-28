package com.oxo.ball.bean.dto.queue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.oxo.ball.utils.JsonUtil;
import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NegativeOrZero;
import java.io.Serializable;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageQueueDTO implements Serializable {
    //登录
    public static final int TYPE_LOG_LOGIN = 1;
    //操作
    public static final int TYPE_LOG_OPER = 2;
    //下注
    public static final int TYPE_LOG_BET = 3;
    //下注返佣
    public static final int TYPE_LOG_BET_BACK = 4;
    //中奖返佣
    public static final int TYPE_LOG_BET_BINGO = 5;
    //充值返佣
    public static final int TYPE_LOG_RECHARGE = 6;
    //充值日志
    public static final int TYPE_LOG_RECHARGE_LOG = 7;
    //充值后是否升级
    public static final int TYPE_LOG_RECHARGE_UP = 8;
    //充值后是否触发上级奖金
    public static final int TYPE_RECHARGE_PARENT_BONUS = 9;
    //结算后真实账变
    public static final int TYPE_BET_OPEN = 10;
    public static final int TYPE_BET_BINGO_HANDSUP = 11;
    public static final int TYPE_RECHARGE_FIRST = 12;
    //玩家TG消息
    public static final int TYPE_PLAYER_TG_CHAT = 13;
    //下注返佣-补
    public static final int TYPE_LOG_BET_BACK2 = 14;
    private Integer type;
    private String data;

    @Override
    public String toString() {
        try {
            return JsonUtil.toJson(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "{}";

    }
}
