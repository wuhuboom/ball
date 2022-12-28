package com.oxo.ball.controller.admin;

import com.oxo.ball.bean.dao.BallIpWhite;
import com.oxo.ball.bean.dao.BallApiConfig;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.service.admin.IBallIpWhiteService;
import com.oxo.ball.service.admin.IBallApiConfigService;
import com.oxo.ball.utils.BigDecimalUtil;
import com.oxo.ball.utils.ResponseMessageUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 系统配置 前端控制器
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@RestController
@RequestMapping("/ball/api_config")
public class BallApiConfigController {

    @Resource
    IBallApiConfigService systemConfigService;

    @GetMapping()
    public Object getConfig(){
        return BaseResponse.successWithData(systemConfigService.getApiConfig());
    }

    @PostMapping("edit")
    public Object edit(@RequestBody BallApiConfig systemConfig){
        if(!systemConfig.getSmsMessage().contains("{0}")){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e4"));

        }
        Boolean aBoolean = systemConfigService.edit(systemConfig);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("error");
    }
}
