package com.oxo.ball.bean.dto.api.tnz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayParamBackDtoTnz {
    private Boolean success;
    private Integer code;
    private String message;
//    private String outTradeNo;
//    private String transNo;
//    private String link;
    /**
     * "outTradeNo":"TEST16",
     * "transNo":"49952825468686124611",
     * "link":"http://bang.okopays.com/#/0RcX4YNuX6"
     */
    private Map<String,Object> result;
}
