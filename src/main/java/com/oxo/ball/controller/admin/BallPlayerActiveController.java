package com.oxo.ball.controller.admin;

import com.oxo.ball.bean.dto.req.admin.QueryActivePlayerRequest;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.interceptor.MainOper;
import com.oxo.ball.service.admin.IBallPlayerActiveService;
import com.oxo.ball.service.admin.IBallPlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ball/player/active")
@MainOper("会员管理")
public class BallPlayerActiveController {
    @Autowired
    IBallPlayerActiveService playerActiveService;

    @PostMapping
    public Object index(QueryActivePlayerRequest queryActivePlayerRequest){
        return BaseResponse.successWithData(playerActiveService.queryActivePlayer(queryActivePlayerRequest));
    }
}
