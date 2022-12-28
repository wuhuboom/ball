//package com.oxo.ball.controller.admin;
//
//import com.oxo.ball.bean.dao.BallAdmin;
//import com.oxo.ball.bean.dao.BallLoggerBack;
//import com.oxo.ball.bean.dao.BallLoggerBackRecharge;
//import com.oxo.ball.bean.dao.BallLoggerRebate;
//import com.oxo.ball.bean.dto.resp.BaseResponse;
//import com.oxo.ball.bean.dto.resp.SearchResponse;
//import com.oxo.ball.service.admin.BallAdminService;
//import com.oxo.ball.service.admin.IBallLoggerBackRechargeService;
//import com.oxo.ball.service.admin.IBallLoggerRebateService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.annotation.Resource;
//import javax.servlet.http.HttpServletRequest;
//
///**
// * <p>
// * 充值返佣结算
// * </p>
// *
// * @author oxo_jy
// * @since 2022-04-13
// */
//@RestController
//@RequestMapping("/ball/finance/rebate/back")
//public class BallLoggerRebateBackController {
//    @Resource
//    IBallLoggerBackRechargeService loggerBackRechargeService;
//    @Autowired
//    BallAdminService adminService;
//    @PostMapping
//    public Object index(BallLoggerBackRecharge loggerRebate, @RequestParam(defaultValue = "1")Integer pageNo,
//                        @RequestParam(defaultValue = "20") Integer pageSize){
//        SearchResponse<BallLoggerBackRecharge> search = loggerBackRechargeService.search(loggerRebate, pageNo, pageSize);
//        return BaseResponse.successWithData(search);
//    }
//
//    @PostMapping("do")
//    public Object doSettlement(BallLoggerBackRecharge loggerRebate, HttpServletRequest request){
//        BaseResponse draw = loggerBackRechargeService.draw(loggerRebate);
//        return draw;
//    }
//}
