package com.oxo.ball.ws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse<T> {
    public static int DEEP_TYPE_R = 0;
    public static int DEEP_TYPE_W = 1;
    public static int DEEP_TYPE_T_2 = 2;
    public static int DEEP_TYPE_T_3 = 3;
    public static int DEEP_TYPE_T_4 = 4;
    public static int DEEP_TYPE_T_5 = 5;
    /**
     * 0.充值
     * 1.提现
     * 2.待办数增加
     */
    private Integer type;
    /**
     * 数据
     */
    private String data;

}
