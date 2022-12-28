package com.oxo.ball.controller.admin;

import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallWithdrawManagement;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.service.admin.BallAdminService;
import com.oxo.ball.service.admin.IBallCountryService;
import com.oxo.ball.service.admin.IBallWithdrawManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 提现方式 前端控制器
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@RestController
@RequestMapping("/ball/finance/withdraw")
public class BallWithdrawManagementController {
    @Autowired
    IBallWithdrawManagementService withdrawManagementService;
    @Autowired
    BallAdminService ballAdminService;
    @Autowired
    IBallCountryService countryService;
    @PostMapping
    public Object index(BallWithdrawManagement query,
                        @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize){
        SearchResponse<BallWithdrawManagement> search = withdrawManagementService.search(query, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }
    @GetMapping
    public Object indexCountry(){
        return BaseResponse.successWithData(countryService.findAll());
    }
    @PostMapping("add")
    public Object add(BallWithdrawManagement withdrawManagement, MultipartFile file, HttpServletRequest request){
        try {
            BallAdmin currentUser = ballAdminService.getCurrentUser(request.getHeader("token"));
            withdrawManagement.setOperUser(currentUser.getUsername());
            withdrawManagementService.insert(withdrawManagement,file);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return withdrawManagement.getId()!=null?BaseResponse.successWithData(withdrawManagement):BaseResponse.failedWithMsg("add error");
    }
    @PostMapping("edit")
    public Object editSave(BallWithdrawManagement withdrawManagement,MultipartFile file,HttpServletRequest request){
        Boolean aBoolean = withdrawManagementService.edit(withdrawManagement,file);
        BallAdmin currentUser = ballAdminService.getCurrentUser(request.getHeader("token"));
        withdrawManagement.setOperUser(currentUser.getUsername());
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("edit error");
    }
    @PostMapping("status")
    public Object status(@RequestBody BallWithdrawManagement withdrawManagement){
        Boolean aBoolean = withdrawManagementService.status(withdrawManagement);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("edit error");
    }
    @GetMapping("del")
    public Object del(@RequestParam("id") Long id){
        Boolean delete = withdrawManagementService.delete(id);
        return delete?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("del error");
    }
}
