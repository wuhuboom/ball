package com.oxo.ball.controller.admin;

import com.oxo.ball.bean.dao.*;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.service.admin.BallAdminService;
import com.oxo.ball.service.admin.IBallBankCardService;
import com.oxo.ball.service.admin.IBallBankService;
import com.oxo.ball.service.admin.IBallSystemConfigService;
import com.oxo.ball.utils.RedisUtil;
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
@RequestMapping("/ball/bank")
public class BallBankController {
    @Autowired
    IBallBankService bankService;
    @Autowired
    IBallSystemConfigService systemConfigService;
    @Autowired
    private RedisUtil redisUtil;

    @PostMapping
    public Object indexArea(BallBankArea query,
                            @RequestParam(defaultValue = "1")Integer pageNo,
                            @RequestParam(defaultValue = "20") Integer pageSize){
        BaseResponse search = bankService.search(query, pageNo, pageSize);
        return search;
    }
    @GetMapping
    public Object getSysConfig(){
        return BaseResponse.successWithData(systemConfigService.getSystemConfig());
    }
    @PutMapping
    public Object changeBankListSwtich(BallSystemConfig systemConfig){
        systemConfigService.edit(BallSystemConfig.builder()
                .id(systemConfig.getId())
                .bankListSwtich(systemConfig.getBankListSwtich())
                .build());
        redisUtil.delKeys("ball_bank_list::*");
        return BaseResponse.successWithMsg("edit success");
    }
    @PostMapping("add")
    public Object add(@RequestBody BallBankArea ballBankArea){
        BaseResponse insert = bankService.insertArea(ballBankArea);
        return insert;
    }
    @PostMapping("edit")
    public Object editSave(@RequestBody BallBankArea ballPlayer){
        BaseResponse aBoolean = bankService.editArea(ballPlayer);
        return  aBoolean;
    }
    @GetMapping("status")
    public Object status(BallBankArea params){
        BaseResponse aBoolean = bankService.statusArea(params);
        return aBoolean;
    }
    @GetMapping("del")
    public Object del(@RequestParam("id") Long id){
        BaseResponse delete = bankService.delArea(id);
        return delete;
    }

    @PostMapping("bank/add")
    public Object addBank(@RequestBody BallBank ballBank){
        BaseResponse insert = bankService.insert(ballBank);
        return insert;
    }
    @PostMapping("bank/edit")
    public Object editSaveBank(@RequestBody BallBank bank){
        BaseResponse aBoolean = bankService.edit(bank);
        return  aBoolean;
    }
    @GetMapping("bank/del")
    public Object delBank(@RequestParam("id") Long id){
        BaseResponse delete = bankService.del(id);
        return delete;
    }
    @PostMapping("bank")
    public Object index(BallBank query,
                            @RequestParam(defaultValue = "1")Integer pageNo,
                            @RequestParam(defaultValue = "20") Integer pageSize){
        BaseResponse search = bankService.searchBank(query, pageNo, pageSize);
        return search;
    }
}
