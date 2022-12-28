package com.oxo.ball.controller.admin;

import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallCommissionRecharge;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.service.admin.BallAdminService;
import com.oxo.ball.service.admin.IBallCommissionRechargeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 充值反佣策略 前端控制器
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@RestController
@RequestMapping("/ball/tactics/recharge")
public class BallCommissionRechargeController {
    @Resource
    IBallCommissionRechargeService commissionRechargeService;
    @GetMapping
    public Object index(){
        BallCommissionRecharge one = commissionRechargeService.findOne();
        return BaseResponse.successWithData(one);
    }
    @PostMapping("edit")
    public Object editSave(@RequestBody BallCommissionRecharge ballPlayer){
        BaseResponse aBoolean = commissionRechargeService.edit(ballPlayer);
        return  aBoolean;
    }
}
