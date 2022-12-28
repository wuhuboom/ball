package com.oxo.ball.controller.admin;

import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallBankArea;
import com.oxo.ball.bean.dao.BallPayBehalf;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.service.admin.BallAdminService;
import com.oxo.ball.service.admin.IBallBankService;
import com.oxo.ball.service.admin.IBallCountryService;
import com.oxo.ball.service.admin.IBallPayBehalfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 代付管理 前端控制器
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@RestController
@RequestMapping("/ball/finance/behalf")
public class BallPayBehalfController {
    @Autowired
    IBallPayBehalfService paymentManagementService;
    @Autowired
    private BallAdminService ballAdminService;
    @Autowired
    IBallBankService bankService;
    @Autowired
    IBallCountryService countryService;

    @PostMapping
    public Object index(BallPayBehalf query,
                        @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize){
        SearchResponse<BallPayBehalf> search = paymentManagementService.search(query, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }
    @GetMapping
    public Object indexAll(){
        List<BallPayBehalf> byAll = paymentManagementService.findByAll();
        return BaseResponse.successWithData(byAll);
    }
    @PutMapping
    public Object indexAllBank(Integer payType){
//        List<BallBankArea> byAll = bankService.findByCode(payType);
        List<BallBankArea> byAll = bankService.findEnabled();
        return BaseResponse.successWithData(byAll);
    }
    @DeleteMapping
    public Object indexAllCountry(){
        return BaseResponse.successWithData(countryService.findAll());
    }
    @PostMapping("add")
    public Object add(BallPayBehalf paymentManagement,MultipartFile file){
        try {
            paymentManagement.setPayType(1);
            paymentManagementService.insert(paymentManagement,file);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return paymentManagement.getId()!=null?BaseResponse.successWithData(paymentManagement):BaseResponse.failedWithMsg("add error");
    }
    @PostMapping("edit")
    public Object editSave(BallPayBehalf paymentManagement, MultipartFile file){
        Boolean aBoolean = paymentManagementService.edit(paymentManagement,file);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("edit error");
    }
    @PostMapping("status")
    public Object status(@RequestBody BallPayBehalf paymentManagement){
        Boolean aBoolean = paymentManagementService.status(paymentManagement);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("edit error");
    }
    @GetMapping("del")
    public Object del(@RequestParam("id") Long id){
        Boolean delete = paymentManagementService.delete(id);
        return delete?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("del error");
    }
    @PostMapping("pay")
    public Object pay(@RequestParam("id") Long id, @RequestParam("wid") Long wid, HttpServletRequest request){
        BallAdmin admin = ballAdminService.getCurrentUser(request.getHeader("token"));
        BaseResponse res = paymentManagementService.pay(id,wid,admin);
        return res;
    }

}
