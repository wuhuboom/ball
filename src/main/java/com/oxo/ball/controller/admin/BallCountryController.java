package com.oxo.ball.controller.admin;

import com.oxo.ball.bean.dao.BallCountry;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.service.admin.IBallCountryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/ball/country")
public class BallCountryController {
    @Resource
    IBallCountryService ballGroupService;

    @PostMapping
    public Object index(BallCountry query, @RequestParam(defaultValue = "1")Integer pageNo, @RequestParam(defaultValue = "20") Integer pageSize){
        SearchResponse<BallCountry> search = ballGroupService.search(query, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }

    @PostMapping("add")
    public Object add(@RequestBody BallCountry sysUserRequest){
        BaseResponse insert = ballGroupService.insert(sysUserRequest);
        return insert;
    }

    @PostMapping("edit")
    public Object editSave(@Validated @RequestBody BallCountry editBallCountry){
        BaseResponse aBoolean = ballGroupService.edit(editBallCountry);
        return aBoolean;
    }

    @GetMapping("del")
    public Object del(@RequestParam("id") Long id){
        Boolean delete = ballGroupService.delete(id);
        return delete?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("del error");
    }

}
