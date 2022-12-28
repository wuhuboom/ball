package com.oxo.ball.bean.dto.resp.admin;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.oxo.ball.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerSmsCodeDTO {
    private String username;
    private String phone;
    private String code;
    private Long sendTime;

    @Override
    public String toString() {
        try {
            return JsonUtil.toJson(this);
        } catch (JsonProcessingException e) {
        }
        return "{}";
    }
}
