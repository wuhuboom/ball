package com.oxo.ball.bean.dto.req.admin;

import lombok.Data;

@Data
public class QueryActivePlayerRequest {
    private String username;
    private Integer level;
    private String regbegin;
    private String regend;

    /**
     * 0 按充值查
     */
    private Integer recharge;

    /**
     * 时间区间,充值未指定,只需要查充值过,投注次数未指定
     */
    private String rbegin;
    private String rend;

    /**
     * 查活跃
     */
    private Integer bet;
    private String bbegin;
    private String bend;
    /**
     * 指定天数投注次数
     */
    private Integer betCount;

    private int pageNo=1;
    private int pageSize=20;
}
