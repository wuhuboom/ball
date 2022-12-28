package com.oxo.ball.controller.admin;

import com.oxo.ball.bean.dao.BallTimezone;
import com.oxo.ball.bean.dao.BallMenu;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.service.admin.BallMenuService;
import com.oxo.ball.service.admin.IBallTimezoneService;
import com.oxo.ball.utils.MapUtil;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/ball/timezone")
public class BallTimezoneController {
    @Resource
    IBallTimezoneService ballGroupService;

    @PostMapping
    public Object index(BallTimezone query, @RequestParam(defaultValue = "1")Integer pageNo, @RequestParam(defaultValue = "20") Integer pageSize){
        SearchResponse<BallTimezone> search = ballGroupService.search(query, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }

    @PostMapping("add")
    public Object add(@RequestBody BallTimezone sysUserRequest){
        BaseResponse insert = ballGroupService.insert(sysUserRequest);
        return insert;
    }

    @PostMapping("edit")
    public Object editSave(@Validated @RequestBody BallTimezone editBallTimezone){
        BaseResponse aBoolean = ballGroupService.edit(editBallTimezone);
        return aBoolean;
    }

    /**
     * @return
     */
    @GetMapping("status")
    public Object getRoleAuth(long id) {
        Boolean aBoolean = ballGroupService.statusOn(id);
        return BaseResponse.successWithData(MapUtil.newMap("sucmsg","e58"));
    }
    @GetMapping("del")
    public Object del(@RequestParam("id") Long id){
        Boolean delete = ballGroupService.delete(id);
        return delete?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("del error");
    }

}
