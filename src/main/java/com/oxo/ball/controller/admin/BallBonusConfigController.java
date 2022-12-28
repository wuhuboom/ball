package com.oxo.ball.controller.admin;

import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallBonusConfig;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.service.admin.BallAdminService;
import com.oxo.ball.service.admin.IBallBonusConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 奖金策略 前端控制器
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@RestController
@RequestMapping("/ball/tactics/bonus")
public class BallBonusConfigController {
    @Resource
    IBallBonusConfigService bonusConfigService;
    @Autowired
    BallAdminService adminService;
    @PostMapping
    public Object index(BallBonusConfig query,
                        @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize){
        SearchResponse<BallBonusConfig> search = bonusConfigService.search(query, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }
    @PostMapping("add")
    public Object add(@RequestBody BallBonusConfig sysUserRequest, HttpServletRequest request){
        BallBonusConfig insert = new BallBonusConfig();
        BallAdmin currentUser = adminService.getCurrentUser(request.getHeader("token"));
        sysUserRequest.setOperUser(currentUser.getUsername());
        try {
            insert = bonusConfigService.insert(sysUserRequest);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return insert.getId()!=null?BaseResponse.successWithData(insert):BaseResponse.failedWithMsg("add error");
    }
    @PostMapping("edit")
    public Object editSave(@RequestBody BallBonusConfig bonusConfig){
        Boolean aBoolean = bonusConfigService.edit(bonusConfig);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("edit error");
    }
    @PostMapping("status")
    public Object status(@RequestBody BallBonusConfig bonusConfig){
        Boolean aBoolean = bonusConfigService.status(bonusConfig);
        return  aBoolean?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("edit error");
    }
    @GetMapping("del")
    public Object del(@RequestParam("id") Long id){
        BallBonusConfig byId = bonusConfigService.findById(id);
        Boolean delete = bonusConfigService.delete(byId);
        return delete?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("del error");
    }
}
