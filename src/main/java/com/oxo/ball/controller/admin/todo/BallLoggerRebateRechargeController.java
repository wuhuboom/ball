package com.oxo.ball.controller.admin.todo;

import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallLoggerBack;
import com.oxo.ball.bean.dao.BallLoggerRebate;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.service.admin.BallAdminService;
import com.oxo.ball.service.admin.IBallLoggerBackService;
import com.oxo.ball.service.admin.IBallLoggerRebateService;
import com.oxo.ball.utils.PoiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 日志表 前端控制器
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@RestController
@RequestMapping("/ball/todo/recharge")
public class BallLoggerRebateRechargeController {
    @Resource
    IBallLoggerRebateService loggerRebateService;
    @Autowired
    private BallAdminService adminService;
    @Autowired
    private PoiUtil poiUtil;

    @PostMapping
    public Object index(BallLoggerRebate loggerRebate, @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize,
                        HttpServletRequest request){
        BallAdmin currentUser = adminService.getCurrentUser(request.getHeader("token"));
//        if(pageSize==-1){
//            BaseResponse res = poiUtil.exportRebateRecharge(loggerRebate,currentUser);
//            return res;
//        }
        SearchResponse<BallLoggerRebate> search = loggerRebateService.searchRecharge(loggerRebate, pageNo, pageSize,currentUser);
        return BaseResponse.successWithData(search);
    }
    @DeleteMapping
    public Object export(BallLoggerRebate loggerRebate,
                        HttpServletRequest request){
        BallAdmin currentUser = adminService.getCurrentUser(request.getHeader("token"));
        BaseResponse res = poiUtil.exportRebateRecharge(loggerRebate,currentUser);
        return res;
    }

    @GetMapping()
    public Object doSettlement(BallLoggerRebate loggerRebate){
        BaseResponse response = loggerRebateService.settlement(loggerRebate);
        return response;
    }

    @PutMapping
    public Object indexTotal(BallLoggerRebate loggerRebate, @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize,
                        HttpServletRequest request){
        BallAdmin currentUser = adminService.getCurrentUser(request.getHeader("token"));
        loggerRebate.setStatus(1);
        SearchResponse<BallLoggerRebate> search = loggerRebateService.searchRecharge(loggerRebate, pageNo, pageSize,currentUser);
        return BaseResponse.successWithData(search);
    }
}
