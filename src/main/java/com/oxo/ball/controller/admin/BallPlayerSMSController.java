package com.oxo.ball.controller.admin;

import com.oxo.ball.bean.dao.BallApp;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.bean.dto.resp.admin.PlayerSmsCodeDTO;
import com.oxo.ball.service.ISmsService;
import com.oxo.ball.service.admin.IBallAppService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-24
 */
@RestController
@RequestMapping("/ball/player/sms")
public class BallPlayerSMSController {
    @Resource
    ISmsService smsService;
    @GetMapping
    public Object index(){
        List<String> search = smsService.smsList();
        return BaseResponse.successWithData(search);
    }
}
