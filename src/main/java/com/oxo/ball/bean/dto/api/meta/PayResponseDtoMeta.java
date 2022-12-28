package com.oxo.ball.bean.dto.api.meta;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayResponseDtoMeta {
//    {"msg":"success",
// "code":200,
// "data":{"url":"https://metapay888.com/metaPay/api/payCongo?orderId=1220162266122016"}}
    private String msg;
    private Integer code;
    private Map<String,String> data;
}
