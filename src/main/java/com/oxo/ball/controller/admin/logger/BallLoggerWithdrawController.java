package com.oxo.ball.controller.admin.logger;

import com.oxo.ball.bean.dao.*;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.service.admin.*;
import com.oxo.ball.utils.PoiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/ball/finance/out")
public class BallLoggerWithdrawController {
    @Autowired
    IBallLoggerWithdrawalService loggerWithdrawalService;
    @Autowired
    BallAdminService ballAdminService;
    @Autowired
    IBallPlayerService playerService;
    @Autowired
    private IBallPayBehalfService payBehalfService;
    @Autowired
    PoiUtil poiUtil;
    @Autowired
    IBallConfigWfailService configWfailService;
    @Autowired
    IBallSystemConfigService systemConfigService;
    @PostMapping
    public Object index(BallLoggerWithdrawal query,
                        @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize){
        //查询列表
        if (query.getAction()==0){
                SearchResponse<BallLoggerWithdrawal> search = loggerWithdrawalService.search2(query, pageNo, pageSize);
                return BaseResponse.successWithData(search);

        }if (query.getAction()==1){
            //查系统配置
            return BaseResponse.successWithData(systemConfigService.getSystemConfig());
        }if (query.getAction()==2){
            //修改系统配置
            systemConfigService.edit(BallSystemConfig.builder()
                    .id(query.getSysId())
                    .autoCheck(query.getAutoCheck())
                    .autoCheckTime(query.getAutoCheckTime())
                    .build());
            return BaseResponse.SUCCESS;
        }else {
            return BaseResponse.SUCCESS;
        }
    }
    @DeleteMapping
    public Object indexSum(BallLoggerWithdrawal query){
        BallLoggerWithdrawal sum = loggerWithdrawalService.searchStatis(query);
        if(sum==null){
            return BaseResponse.SUCCESS;
        }
        sum.setPlayerName("总计");
        return BaseResponse.successWithData(sum);
    }
//    @GetMapping
//    public Object indexAll(String bankCode,Integer type){
//        if(type==null){
//            return BaseResponse.successWithData(payBehalfService.findByAll());
//        }
//        List<BallPayBehalf> byAll = payBehalfService.findByBankCode(bankCode, type);
//        return BaseResponse.successWithData(byAll);
//    }
    @GetMapping
    public Object indexAll(Long id){
        if(id==null){
            return BaseResponse.successWithData(payBehalfService.findByAll());
        }
        List<BallPayBehalf> byAll = payBehalfService.findByAreaCode(id);
        return BaseResponse.successWithData(byAll);
    }
    @PutMapping
    public Object indexAllConfigWfail(){
        List<BallConfigWfail> byAll = configWfailService.findByAll();
        return BaseResponse.successWithData(byAll);
    }
    @PostMapping("check")
    public Object check(@RequestBody BallLoggerWithdrawal query, HttpServletRequest request){
        BallAdmin admin = ballAdminService.getCurrentUser(request.getHeader("token"));
        BaseResponse response = loggerWithdrawalService.check(query,admin);
        return response;
    }
    @GetMapping("check")
    public Object getPlayerCard(@RequestParam("pid") Long id){
        BallLoggerWithdrawal loggerWithdrawal = loggerWithdrawalService.findById(id);
        BaseResponse response = playerService.getPlayerCards(loggerWithdrawal);
        return response;
    }
    @PostMapping("export")
    public Object exportExcel(@RequestBody BallLoggerWithdrawal query){
        BaseResponse res = poiUtil.exportWithdrawal(query);
        return res;
    }
}
