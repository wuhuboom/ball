package com.oxo.ball.controller.admin;

import com.oxo.ball.bean.dao.BallCountry;
import com.oxo.ball.bean.dao.BallDepositPolicy;
import com.oxo.ball.bean.dao.BallPaymentManagement;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.service.admin.IBallCountryService;
import com.oxo.ball.service.admin.IBallDepositPolicyService;
import com.oxo.ball.service.admin.IBallPaymentManagementService;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 存款策略 前端控制器
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@RestController
@RequestMapping("/ball/tactics/inout")
public class BallDepositPolicyController {
    @Resource
    IBallDepositPolicyService depositPolicyService;
    @Autowired
    IBallPaymentManagementService paymentManagementService;
    @Autowired
    IBallCountryService countryService;
    @PostMapping
    public Object index(BallDepositPolicy query,
                        @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize){
        SearchResponse<BallDepositPolicy> search = depositPolicyService.search(query, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }
    @GetMapping
    public Object getPays(){
        List<BallPaymentManagement> byAll = paymentManagementService.findByAll();
        return BaseResponse.successWithData(byAll);
    }
    @DeleteMapping
    public Object getCountry(){
        List<BallCountry> byAll = countryService.findAll();
        return BaseResponse.successWithData(byAll);
    }
    @PostMapping("add")
    public Object add(@RequestBody BallDepositPolicy sysUserRequest){
        BaseResponse insert = depositPolicyService.insert(sysUserRequest);
        return insert;
    }
    @PostMapping("edit")
    public Object editSave(@RequestBody BallDepositPolicy ballPlayer){
        BaseResponse aBoolean = depositPolicyService.edit(ballPlayer);
        return  aBoolean;
    }
    @PostMapping("status")
    public Object status(@RequestBody BallDepositPolicy ballPlayer){
        Boolean aBoolean = depositPolicyService.status(ballPlayer);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("edit error");
    }
    @GetMapping("del")
    public Object del(@RequestParam("id") Long id){
        Boolean delete = depositPolicyService.delete(id);
        return delete?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("del error");
    }
}
