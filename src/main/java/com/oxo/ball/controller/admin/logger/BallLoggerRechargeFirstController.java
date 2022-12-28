package com.oxo.ball.controller.admin.logger;

import com.oxo.ball.bean.dao.BallLoggerRecharge;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.service.admin.IBallLoggerRechargeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ball/log/first")
public class BallLoggerRechargeFirstController {
    @Autowired
    IBallLoggerRechargeService loggerRechargeService;

    @PostMapping
    public Object index(BallLoggerRecharge query,
                        @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize){
        query.setFirst(1);
        SearchResponse<BallLoggerRecharge> search = loggerRechargeService.search(query, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }
}
