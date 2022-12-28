package com.oxo.ball.controller.admin.logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallLoggerHandsup;
import com.oxo.ball.bean.dao.BallLoggerRecharge;
import com.oxo.ball.bean.dao.BallLoggerWithdrawal;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.service.admin.BallAdminService;
import com.oxo.ball.service.admin.IBallLoggerHandsupService;
import com.oxo.ball.service.admin.IBallLoggerRechargeService;
import com.oxo.ball.utils.PoiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/ball/finance")
public class BallLoggerRechargeController {
    @Autowired
    IBallLoggerRechargeService loggerRechargeService;
    @Autowired
    BallAdminService adminService;
    @Autowired
    private PoiUtil poiUtil;

    @PostMapping("online")
    public Object index(BallLoggerRecharge query,
                        @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize){
        SearchResponse<BallLoggerRecharge> search = loggerRechargeService.search(query, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }

    @PostMapping("online/up")
    public Object editSave(BallLoggerRecharge paymentManagement, HttpServletRequest request) throws JsonProcessingException {
        BallAdmin currentUser = adminService.getCurrentUser(request);
        paymentManagement.setOperUser(currentUser.getUsername());
        BaseResponse res = loggerRechargeService.editRe(paymentManagement);
        return  res;
    }

    @PostMapping("online/export")
    public Object exportExcelOnline(@RequestBody BallLoggerRecharge query){
        BaseResponse res = poiUtil.exportRecharge(query);
        return res;
    }
    @PostMapping("offline/export")
    public Object exportExcelOffline(@RequestBody BallLoggerRecharge query){
        BaseResponse res = poiUtil.exportRecharge(query);
        return res;
    }
}
