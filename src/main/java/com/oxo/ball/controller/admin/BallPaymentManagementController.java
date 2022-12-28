package com.oxo.ball.controller.admin;

import com.oxo.ball.bean.dao.BallCountry;
import com.oxo.ball.bean.dao.BallPaymentManagement;
import com.oxo.ball.bean.dao.BallSystemConfig;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.service.admin.IBallCountryService;
import com.oxo.ball.service.admin.IBallPaymentManagementService;
import com.oxo.ball.service.admin.IBallSystemConfigService;
import com.oxo.ball.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 支付管理 前端控制器
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@RestController
@RequestMapping("/ball/finance/pay")
public class BallPaymentManagementController {
    @Autowired
    IBallPaymentManagementService paymentManagementService;
    @Autowired
    IBallCountryService countryService;
    @Autowired
    private IBallSystemConfigService systemConfigService;
    @Autowired
    private RedisUtil redisUtil;

    @PostMapping
    public Object index(BallPaymentManagement query,
                        @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize){
        SearchResponse<BallPaymentManagement> search = paymentManagementService.search(query, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }
    @GetMapping
    public Object indexAllCountry(){
        List<BallCountry> all = countryService.findAll();
        return BaseResponse.successWithData(all);
    }
    @PutMapping
    public Object getSysConfig(){
        return BaseResponse.successWithData(systemConfigService.getSystemConfig());
    }
    @PostMapping("add")
    public Object add(BallPaymentManagement paymentManagement, MultipartFile file){
        try {
            paymentManagementService.insert(paymentManagement,file);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return paymentManagement.getId()!=null?BaseResponse.successWithData(paymentManagement):BaseResponse.failedWithMsg("add error");
    }
    @PostMapping("edit")
    public Object editSave(BallPaymentManagement paymentManagement,MultipartFile file){
        Boolean aBoolean = paymentManagementService.edit(paymentManagement,file);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("edit error");
    }
    @GetMapping("edit")
    public Object editSaveSwitch(BallSystemConfig systemConfig){
        systemConfigService.edit(BallSystemConfig.builder()
                .id(systemConfig.getId())
                .rechargeAreaSwtich(systemConfig.getRechargeAreaSwtich())
                .build());
        redisUtil.delKeys("ball_bank_list::*");
        return BaseResponse.successWithMsg("edit success");
    }
    @PostMapping("status")
    public Object status(@RequestBody BallPaymentManagement paymentManagement){
        Boolean aBoolean = paymentManagementService.status(paymentManagement);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("edit error");
    }
    @GetMapping("del")
    public Object del(@RequestParam("id") Long id){
        Boolean delete = paymentManagementService.delete(id);
        return delete?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("del error");
    }
}
