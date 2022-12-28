package com.oxo.ball.controller.admin;

import com.google.zxing.WriterException;
import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallIpWhite;
import com.oxo.ball.bean.dao.BallMenu;
import com.oxo.ball.bean.dao.BallSystemConfig;
import com.oxo.ball.bean.dto.req.AuthEditPwdRequest;
import com.oxo.ball.bean.dto.req.AuthLoginRequest;
import com.oxo.ball.bean.dto.req.SysUserEditRequest;
import com.oxo.ball.bean.dto.resp.AuthLoginResponse;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.config.SomeConfig;
import com.oxo.ball.contant.LogsContant;
import com.oxo.ball.interceptor.MainOper;
import com.oxo.ball.interceptor.SubOper;
import com.oxo.ball.service.admin.*;
import com.oxo.ball.service.impl.admin.AuthServiceImpl;
import com.oxo.ball.utils.*;
import de.taimos.totp.TOTP;
import io.undertow.util.StatusCodes;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author flooming
 */
@RestController
@RequestMapping("/auth")
@MainOper("个人设置")
public class AuthController {
    private Logger logger = LoggerFactory.getLogger(LogsContant.API_LOG);

    @Autowired
    BallAdminService ballAdminService;
    @Resource
    BallMenuService ballMenuService;

    @Resource
    AuthService authService;
    @Resource
    RedisUtil redisUtil;
    @Autowired
    IBallIpWhiteService ipWhiteService;
    @Autowired
    SomeConfig someConfig;
    @Autowired
    IBallSystemConfigService systemConfigService;

    @PostMapping("/login")
    public BaseResponse login(@RequestBody AuthLoginRequest req,HttpServletRequest request) {
        String ipAddress = IpUtil.getIpAddress(request);
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        if(systemConfig.getOpenWhite()==1){
            List<BallIpWhite> all = ipWhiteService.findAll();
            boolean contains = all.contains(BallIpWhite.builder()
                    .ip(ipAddress)
                    .build());
            if(!contains){
                return BaseResponse.failedWithDataAndMsg("当前IP禁止登录！",ipAddress);
            }
        }
        BallAdmin ballAdmin = null;
        try {
            ballAdmin = ballAdminService.findByUsername(req.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ballAdmin == null || !ballAdmin.getPassword().equals(PasswordUtil.genPasswordMd5(req.getPassword()))) {
            return new BaseResponse(500, "账号或密码错误!");
        }
//        if(!sysUser.getRole().equals(req.getIsAdmin())){
//            return new BaseResponse(500,"不合法的账号，请联系管理员");
//        }

        AuthLoginResponse userBean = new AuthLoginResponse();
        userBean.setId(ballAdmin.getId());
        userBean.setUserName(ballAdmin.getUsername());
//        userBean.setToken(authService.buildToken(sysUser));
        if(systemConfig.getOpenGoogle()==1) {
            if (StringUtils.isBlank(ballAdmin.getGoogleCode())) {
                //没有绑定google验证
                String generateSecretKey = GoogleAuthenticationTool.generateSecretKey();
                String qrCodeString = GoogleAuthenticationTool.spawnScanQRString(ballAdmin.getUsername(), generateSecretKey, "oxo_ball");
                String qrCodeImageBase64 = null;
                try {
                    qrCodeImageBase64 = GoogleAuthenticationTool.createQRCode(qrCodeString, null, 128, 128);
                } catch (WriterException | IOException e) {
                    e.printStackTrace();
                }
                userBean.setGtokenKey(generateSecretKey);
                userBean.setGtokenQr(qrCodeImageBase64);
                return BaseResponse.successWithData(userBean);
            }
        }
        Object get = redisUtil.get(AuthServiceImpl.REDIS_AUTH_KEY+ballAdmin.getId().toString());
        if (get == null || StringUtils.isBlank(get.toString())) {
            userBean.setToken(authService.buildToken(ballAdmin));
        } else {
            userBean.setToken(get.toString());
        }
        if(systemConfig.getOpenGoogle()==1) {
//            String rightCode = GoogleAuthenticationTool.getTOTPCode(ballAdmin.getGoogleCode());
//        System.out.println(rightCode);
            //TODO 关闭GOOGOLE验证码
//            if (!rightCode.equals(req.getGoogleCode())) {
        if(!MyTOTP.validate(GoogleAuthenticationTool.getHexKey(ballAdmin.getGoogleCode()),req.getGoogleCode())){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "e2"));
            }
        }
        userBean.setGtokenKey("ok");
        //验证GOOGLE验证码
        return new BaseResponse<>(userBean);
    }

    @PostMapping("userinfo")
    public BaseResponse bindGoogleValid(@RequestBody AuthLoginRequest loginRequest) {
        BallAdmin ballAdmin = ballAdminService.findByUsername(loginRequest.getUsername());
        if (!StringUtils.isBlank(ballAdmin.getGoogleCode())) {
            return BaseResponse.failedWithMsg("The Google verification code has been bound. There is no need to bind it again");
        }
        String rightCode = GoogleAuthenticationTool.getTOTPCode(loginRequest.getGoogleKey());
//        System.out.println(rightCode);
        if (!rightCode.equals(loginRequest.getGoogleCode())) {
//        if(!MyTOTP.validate(GoogleAuthenticationTool.getHexKey(ballAdmin.getGoogleCode()),req.getGoogleCode())){
            return BaseResponse.failedWithMsg("Google verification code input error");
        }
        //验证码正确
        SysUserEditRequest edit = new SysUserEditRequest();
        edit.setId(ballAdmin.getId());
        edit.setGoogleCode(loginRequest.getGoogleKey());
        ballAdminService.edit(edit);
        AuthLoginResponse userBean = new AuthLoginResponse();
        userBean.setId(ballAdmin.getId());
        userBean.setUserName(ballAdmin.getUsername());
        Object get = redisUtil.get(AuthServiceImpl.REDIS_AUTH_KEY+ballAdmin.getId().toString());
        if (get == null || StringUtils.isBlank(get.toString())) {
            userBean.setToken(authService.buildToken(ballAdmin));
        } else {
            userBean.setToken(get.toString());
        }
        return new BaseResponse<>(userBean);
    }

    @GetMapping("userinfo")
    public BaseResponse getUserInfo(HttpServletRequest request) {
        BallAdmin systemUser = ballAdminService.getCurrentUser(request.getHeader("token"));
        List<BallMenu> byRole = ballMenuService.findByRole(systemUser.getRoleId());
        List<String> auths = new ArrayList<>();
        for (BallMenu auth : byRole) {
            auths.add(auth.getPath());
        }
        Map<String,Object> data = new HashMap<>();
        data.put("auths",auths);
        data.put("name",systemUser.getUsername());
        return BaseResponse.successWithData(data);
    }

    @PostMapping("/logout")
    public BaseResponse logout(HttpServletRequest request) {
        BallAdmin systemUser = ballAdminService.getCurrentUser(request.getHeader("token"));
        if (systemUser == null) {
            return new BaseResponse("未登录");
        }

//        authService.clearAuth(systemUser);

        return new BaseResponse(StatusCodes.OK, "注销成功");
    }

    @PostMapping("/editPwd")
    @SubOper("修改密码")
    public BaseResponse editPwd(@RequestBody AuthEditPwdRequest req, HttpServletRequest request) {
        BallAdmin systemUser = ballAdminService.getCurrentUser(request.getHeader("token"));
        if (systemUser == null) {
            throw new RuntimeException("system error");
        }

        if (!systemUser.getPassword().equals(PasswordUtil.genPasswordMd5(req.getOrigin()))) {
            throw new RuntimeException("原始密码不一致");
        }

        if (!req.getConfirmed().equals(req.getNewpwd())) {
            throw new RuntimeException("确认密码不一致");
        }

        if (!ballAdminService.editPwd(systemUser.getId(), PasswordUtil.genPasswordMd5(req.getConfirmed()))) {
            throw new RuntimeException("系统错误");
        }
        redisUtil.del(AuthServiceImpl.REDIS_AUTH_KEY+systemUser.getId().toString());
        return new BaseResponse(StatusCodes.OK, "修改成功");
    }
}
