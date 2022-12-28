package com.oxo.ball.bean.dto.req.player;

import lombok.Data;
import org.apache.ibatis.annotations.Param;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class PlayerBindPhoneRequest {
    @NotEmpty(message = "areaCodeIsEmpty")
    private String areaCode;
    @NotEmpty(message = "phoneIsEmpty")
    private String phone;
    @NotEmpty(message = "smsCodeIsEmpty")
    private String code;
}
