package com.oxo.ball.controller.admin.logger;

import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallLoggerHandsup;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.service.admin.BallAdminService;
import com.oxo.ball.service.admin.IBallLoggerHandsupService;
import com.oxo.ball.service.admin.IBallLoggerHandsupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/ball/finance/inlog")
public class BallLoggerHandsupController {
    @Autowired
    IBallLoggerHandsupService loggerHandsupService;
    @PostMapping
    public Object index(BallLoggerHandsup query,
                        @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize){
        SearchResponse<BallLoggerHandsup> search = loggerHandsupService.search(query, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }
}
