package com.oxo.ball.bean.dto.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RechargeRebateDto {
    private Double min;
    private Double max;
    //百分比
    private String rate;
    //固定
    private String fixed;
    /**
     * 支付类型
     */
    @JsonIgnore
    private Long id;
    @JsonIgnore
    private Long startTime;
    @JsonIgnore
    private Long endTime;
    @JsonIgnore
    private Integer depositPolicyType;
    @JsonIgnore
    private Long payId;
    @JsonIgnore
    private Long discount;
    @JsonIgnore
    private boolean autoSettlement;

}
