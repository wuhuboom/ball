package com.oxo.ball.controller.admin;

import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallBank;
import com.oxo.ball.bean.dao.BallVirtualCurrency;
import com.oxo.ball.bean.dao.BallVirtualCurrency;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.service.admin.BallAdminService;
import com.oxo.ball.service.admin.IBallVirtualCurrencyService;
import com.oxo.ball.service.admin.IBallBankService;
import com.oxo.ball.service.admin.IBallVirtualCurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 银行卡 前端控制器
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@RestController
@RequestMapping("/ball/finance/usdt")
public class BallVirtualCurrencyController {
    @Autowired
    IBallVirtualCurrencyService virtualCurrencyService;
    @Autowired
    BallAdminService adminService;
    @PostMapping
    public Object index(BallVirtualCurrency query,
                        @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize){
        SearchResponse<BallVirtualCurrency> search = virtualCurrencyService.search(query, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }
    @PostMapping("edit")
    public Object editSave(@RequestBody BallVirtualCurrency ballBankCard,HttpServletRequest request){
        BallAdmin currentUser = adminService.getCurrentUser(request.getHeader("token"));
        ballBankCard.setOperUser(currentUser.getUsername());
        int aBoolean = virtualCurrencyService.edit(ballBankCard);
        return  aBoolean==0?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("fail");
    }
    @PostMapping("status")
    public Object status(@RequestBody BallVirtualCurrency ballBankCard,HttpServletRequest request){
        BallAdmin currentUser = adminService.getCurrentUser(request.getHeader("token"));
        ballBankCard.setOperUser(currentUser.getUsername());
        Boolean aBoolean = virtualCurrencyService.status(ballBankCard);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("edit error");
    }
    @PostMapping("check")
    public Object check(@RequestBody BallVirtualCurrency ballBankCard,HttpServletRequest request){
        BallAdmin currentUser = adminService.getCurrentUser(request.getHeader("token"));
        ballBankCard.setOperUser(currentUser.getUsername());
        Boolean aBoolean = virtualCurrencyService.check(ballBankCard);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("edit error");
    }
}
