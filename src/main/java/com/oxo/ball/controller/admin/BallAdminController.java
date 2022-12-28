package com.oxo.ball.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallGroup;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dto.req.SysUserEditRequest;
import com.oxo.ball.bean.dto.req.SysUserInsertRequest;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.interceptor.MainOper;
import com.oxo.ball.interceptor.SubOper;
import com.oxo.ball.service.IBasePlayerService;
import com.oxo.ball.service.admin.BallAdminService;
import com.oxo.ball.service.admin.BallGroupService;
import com.oxo.ball.utils.ResponseMessageUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/ball/admin")
@MainOper("系统管理-用户管理")
public class BallAdminController {
    @Resource
    BallAdminService ballAdminService;
    @Resource
    private BallGroupService ballGroupService;
    @Autowired
    IBasePlayerService basePlayerService;

    @PostMapping
    public Object index(BallAdmin query, @RequestParam(defaultValue = "1")Integer pageNo, @RequestParam(defaultValue = "20") Integer pageSize){
        SearchResponse<BallAdmin> search = ballAdminService.search(query, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }
    @GetMapping()
    public Object listAll(){
        List<BallGroup> list = ballGroupService.findAll();
        return BaseResponse.successWithData(list);
    }

    @PostMapping("add")
    @SubOper("添加用户")
    public Object add(@RequestBody SysUserInsertRequest sysUserRequest){
        BallAdmin insert = new BallAdmin();
        try {
            if(!StringUtils.isBlank(sysUserRequest.getPlayerName())){
                //绑定代理账号
                String[] split = sysUserRequest.getPlayerName().split(",");
                for(String item:split){
                    BallPlayer byUsername = basePlayerService.findByUsername(item);
                    if(byUsername==null){
                        return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                                ResponseMessageUtil.responseMessage("", "e3"));

                    }
                }
            }
            insert = ballAdminService.insert(sysUserRequest);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        BaseResponse res = (insert.getId() != null ? BaseResponse.successWithData(insert) : BaseResponse.failedWithMsg("add error"));
        res.setRemark("添加用户"+insert.getUsername());
        return res;
    }
    @GetMapping("edit/{id}")
    public Object edit(@PathVariable Long id){
        BallAdmin byId = ballAdminService.findById(id);
        BaseResponse<BallAdmin> ballAdminBaseResponse = new BaseResponse<>(byId);
        return ballAdminBaseResponse;
    }
    @PostMapping("edit")
    @SubOper("修改用户")
    public Object editSave(@Validated @RequestBody SysUserEditRequest sysUserEditRequest){
        BallAdmin byId = ballAdminService.findById(sysUserEditRequest.getId());
        if(byId.getUsername().equals("admin")){
            return BaseResponse.failedWithMsg("can't edit super admin");
        }
        if(!StringUtils.isBlank(sysUserEditRequest.getPlayerName())){
            //绑定代理账号
            String[] split = sysUserEditRequest.getPlayerName().split(",");
            for(String item:split){
                BallPlayer byUsername = basePlayerService.findByUsername(item);
                if(byUsername==null){
                    return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                            ResponseMessageUtil.responseMessage("", "e3"));
                }
            }
        }
        Boolean aBoolean = ballAdminService.edit(sysUserEditRequest);
        BaseResponse response = (aBoolean ? BaseResponse.SUCCESS : BaseResponse.failedWithMsg("edit error"));
        response.setRemark("edit"+sysUserEditRequest.getId());
        return response;
    }
    @GetMapping("edit")
    @SubOper("修改用户")
    public Object resetGoogle(Long id){
        SysUserEditRequest edit = new SysUserEditRequest();
        edit.setId(id);
        edit.setGoogleCode("");
        Boolean aBoolean = ballAdminService.edit(edit);
        BaseResponse response = (aBoolean ? BaseResponse.SUCCESS : BaseResponse.failedWithMsg("reset failed"));
        response.setRemark("reset "+id+"'s google verification code");
        return response;
    }
    @GetMapping("del")
    @SubOper("删除用户")
    public Object del(@RequestParam("id") Long id){
        BallAdmin byId = ballAdminService.findById(id);
        if(byId.getUsername().equals("admin")){
            return BaseResponse.failedWithMsg("can't edit super admin");
        }
        Boolean delete = ballAdminService.delete(id);
        BaseResponse res = delete ? BaseResponse.SUCCESS : BaseResponse.failedWithMsg("del error");
        res.setRemark("del user "+id);
        return res;
    }
}
