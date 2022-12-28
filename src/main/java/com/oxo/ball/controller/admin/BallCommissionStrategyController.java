package com.oxo.ball.controller.admin;

import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallCommissionStrategy;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.service.admin.BallAdminService;
import com.oxo.ball.service.admin.IBallCommissionStrategyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 反佣策略 前端控制器
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@RestController
@RequestMapping("/ball/tactics/back")
public class BallCommissionStrategyController {
    @Resource
    IBallCommissionStrategyService commissionStrategyService;
    @Autowired
    BallAdminService adminService;
    @PostMapping
    public Object index(BallCommissionStrategy query,
                        @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize){
        SearchResponse<BallCommissionStrategy> search = commissionStrategyService.search(query, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }
    @PostMapping("add")
    public Object add(@RequestBody BallCommissionStrategy sysUserRequest, HttpServletRequest request){
        BallCommissionStrategy insert = new BallCommissionStrategy();
        BallAdmin currentUser = adminService.getCurrentUser(request.getHeader("token"));
        sysUserRequest.setOperUser(currentUser.getUsername());
        try {
            insert = commissionStrategyService.insert(sysUserRequest);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return insert.getId()!=null?BaseResponse.successWithData(insert):BaseResponse.failedWithMsg("add error");
    }
    @PostMapping("edit")
    public Object editSave(@RequestBody BallCommissionStrategy ballPlayer){
        Boolean aBoolean = commissionStrategyService.edit(ballPlayer);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("edit error");
    }
    @PostMapping("status")
    public Object status(@RequestBody BallCommissionStrategy ballPlayer){
        Boolean aBoolean = commissionStrategyService.status(ballPlayer);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("edit error");
    }
    @GetMapping("del")
    public Object del(@RequestParam("id") Long id){
        BallCommissionStrategy byId = commissionStrategyService.findById(id);
        Boolean delete = commissionStrategyService.delete(byId);
        return delete?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("del error");
    }
}
