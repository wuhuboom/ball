package com.oxo.ball.controller.player;

import com.oxo.ball.auth.PlayerDisabledException;
import com.oxo.ball.auth.TokenInvalidedException;
import com.oxo.ball.bean.dao.*;
import com.oxo.ball.bean.dto.req.AuthEditPwdRequest;
import com.oxo.ball.bean.dto.req.player.*;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.config.SomeConfig;
import com.oxo.ball.service.IBasePlayerService;
import com.oxo.ball.service.admin.IBallBalanceChangeService;
import com.oxo.ball.service.admin.IBallLoggerBackService;
import com.oxo.ball.service.admin.IBallPaymentManagementService;
import com.oxo.ball.service.admin.IBallSystemConfigService;
import com.oxo.ball.service.player.AuthPlayerService;
import com.oxo.ball.service.player.IPlayerBetService;
import com.oxo.ball.service.player.IPlayerService;
import com.oxo.ball.utils.BigDecimalUtil;
import com.oxo.ball.utils.PasswordUtil;
import com.oxo.ball.utils.ResponseMessageUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.undertow.util.StatusCodes;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;


/**
 * <p>
 * 玩家账号 前端控制器
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@RestController
@RequestMapping("/player/v2")
@Api(tags = "玩家 - 手机验证绑定")
public class PlayerCenterController {

    @Resource
    IPlayerService playerService;
    @ApiOperation(
            value = "获取手机验证码-登录状态-输入手机号",
            notes = "用于绑定新手机号" ,
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone",value = "手机号",required = true)
    })
    @PostMapping("phone_code")
    public Object getPhoneCode(@Validated PlayerBindPhoneCodeRequest bindPhoneCodeRequest,HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        BaseResponse response  = playerService.getPhoneCode(bindPhoneCodeRequest,currentUser);
        return response;
    }
    @ApiOperation(
            value = "获取手机验证码-登录状态-不用输入手机号",
            notes = "直接发送验证码到绑定的手机号" ,
            httpMethod = "POST")
    @PostMapping("phone_code/online")
    public Object getPhoneCodeWithdrawal(HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        BaseResponse response  = playerService.getPhoneCode(currentUser);
        return response;
    }

    @ApiOperation(
            value = "换绑手机号",
            notes = "换绑手机号" ,
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "areaCode",value = "手机区号",required = true),
            @ApiImplicitParam(name = "phone",value = "手机号",required = true),
            @ApiImplicitParam(name = "code",value = "短信验证码",required = true)
    })
    @PostMapping("phone_bind")
    public Object bindNewPhone(@Validated PlayerBindPhoneRequest playerBindPhoneRequest,HttpServletRequest request) throws TokenInvalidedException, PlayerDisabledException {
        BallPlayer currentUser = playerService.getCurrentUser(request);
        if(currentUser.getAccountType()==1){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "testerCantBind"));
        }
        BaseResponse response = playerService.bindPhone(playerBindPhoneRequest,currentUser);
        return response;
    }

    @ApiOperation(
            value = "获取手机验证码-未登录状态",
            notes = "用于忘记密码" ,
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username",value = "用户名",required = true),
            @ApiImplicitParam(name = "phone",value = "手机号",required = true)
    })
    @PostMapping("phone_code/change_pwd")
    public Object getPhoneCodeOffline(@Validated PlayerChangePwdCodeRequest bindPhoneCodeRequest) {
        BaseResponse response  = playerService.getPhoneCode(bindPhoneCodeRequest);
        return response;
    }

    @ApiOperation(
            value = "获取手机验证码-未登录状态-用户名",
            notes = "用于新设备登录时" ,
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username",value = "用户名",required = true)
    })
    @PostMapping("phone_code/username")
    public Object getPhoneCodeByUsername(@Validated PlayerNewDevicesRequest newDevicesRequest) {
        BaseResponse response  = playerService.getPhoneCode(newDevicesRequest);
        return response;
    }

    @ApiOperation(
            value = "修改登录密码",
            notes = "未登录-使用绑定手机号重置登录密码",
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username",value = "用户名",required = true),
            @ApiImplicitParam(name = "phone",value = "手机号",required = true),
            @ApiImplicitParam(name = "newPwd",value = "新密码",required = true),
            @ApiImplicitParam(name = "twicePwd",value = "确认密码",required = true),
            @ApiImplicitParam(name = "code",value = "短信验证码",required = true)
    })
    @PostMapping("phone_change_pwd")
    public Object phoneChangePwd(@Validated PlayerPhoneChangePwdRequest phoneChangePwdRequest) {
        BaseResponse response = playerService.phoneChangePwd(phoneChangePwdRequest);
        return response;
    }
    @ApiOperation(
            value = "修改安全密码",
            notes = "登录状态-使用绑定手机号重置安全密码",
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "newPwd",value = "新密码",required = true),
            @ApiImplicitParam(name = "twicePwd",value = "确认密码",required = true),
            @ApiImplicitParam(name = "code",value = "短信验证码",required = true)
    })
    @PostMapping("phone_change_pwd_pay")
    public Object phoneChangePayPwd(@Validated PlayerPhoneChangePayPwdRequest phoneChangePwdRequest,HttpServletRequest request) throws PlayerDisabledException, TokenInvalidedException {
        BallPlayer player = playerService.getCurrentUser(request);
        BaseResponse response = playerService.phoneChangePwdPay(player,phoneChangePwdRequest);
        return response;
    }
    @ApiOperation(
            value = "修改安全密码",
            notes = "登录状态-使用原密码重置安全密码",
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "origin",value = "原密码",required = true),
            @ApiImplicitParam(name = "newPwd",value = "新密码",required = true),
            @ApiImplicitParam(name = "twicePwd",value = "确认密码",required = true)
    })
    @PostMapping("change_pwd_pay")
    public Object changePayPwd(@Validated PlayerChangePwdRequest changePwdRequest,HttpServletRequest request) throws PlayerDisabledException, TokenInvalidedException {
        BallPlayer player = playerService.getCurrentUser(request);
        BaseResponse response = playerService.changePwdPay(player,changePwdRequest);
        return response;
    }

    @ApiOperation(
            value = "修改登录密码",
            notes = "登录状态-使用验证码重置登录密码",
            httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "newPwd",value = "新密码",required = true),
            @ApiImplicitParam(name = "twicePwd",value = "确认密码",required = true),
            @ApiImplicitParam(name = "code",value = "短信验证码",required = true)
    })
    @PostMapping("change_pwd_online")
    public Object changePwdOnline(@Validated PlayerPhoneChangePayPwdRequest changePwdRequest,HttpServletRequest request) throws PlayerDisabledException, TokenInvalidedException {
        BallPlayer player = playerService.getCurrentUser(request);
        BaseResponse response = playerService.changePwd(player,changePwdRequest);
        return response;
    }

}
