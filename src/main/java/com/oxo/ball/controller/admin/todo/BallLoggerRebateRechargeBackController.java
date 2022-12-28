package com.oxo.ball.controller.admin.todo;

import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallLoggerRebate;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.service.admin.BallAdminService;
import com.oxo.ball.service.admin.IBallLoggerRebateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 充值返佣
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@RestController
@RequestMapping("/ball/todo/recharge/back")
public class BallLoggerRebateRechargeBackController {
    @Resource
    IBallLoggerRebateService loggerRebateService;
    @Autowired
    private BallAdminService adminService;

    @PostMapping
    public Object index(BallLoggerRebate loggerRebate, @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize,
                        HttpServletRequest request){
        BallAdmin currentUser = adminService.getCurrentUser(request.getHeader("token"));
        SearchResponse<BallLoggerRebate> search = loggerRebateService.search(loggerRebate, pageNo, pageSize,currentUser);
        return BaseResponse.successWithData(search);
    }

    @PutMapping
    public Object indexCount(BallLoggerRebate loggerRebate, @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize,
                        HttpServletRequest request){
        BallAdmin currentUser = adminService.getCurrentUser(request.getHeader("token"));
        loggerRebate.setStatus(1);
        SearchResponse<BallLoggerRebate> search = loggerRebateService.search(loggerRebate, pageNo, pageSize,currentUser);
        return BaseResponse.successWithData(search);
    }

    @GetMapping()
    public Object doSettlement(BallLoggerRebate loggerRebate){
        BaseResponse response = loggerRebateService.settlement(loggerRebate);
        return response;
    }
}
