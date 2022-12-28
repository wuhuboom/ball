package com.oxo.ball.config;

import com.oxo.ball.auth.*;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.auth.MultiFormSubmitException;
import com.oxo.ball.service.player.AuthPlayerService;
import com.oxo.ball.service.player.IPlayerService;
import com.oxo.ball.utils.ResponseMessageUtil;
import io.undertow.util.StatusCodes;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.oxo.ball.service.admin.AuthService.HAVE_NO_AUTH;
import static com.oxo.ball.service.admin.AuthService.TOKEN_INVALID;


/**
 * @author flooming
 */
@ControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public BaseResponse exceptionHandler(Exception e) {
        e.printStackTrace();
        return new BaseResponse(StatusCodes.INTERNAL_SERVER_ERROR, e.getMessage());
    }
    @ExceptionHandler(AuthException.class)
    @ResponseBody
    public BaseResponse exceptionHandler(AuthException e) {
        return new BaseResponse(HAVE_NO_AUTH, "unauthorized ");
    }
    @ExceptionHandler(PlayerEnabledException.class)
    @ResponseBody
    public BaseResponse exceptionHandler(PlayerEnabledException e) {
        return new BaseResponse(AuthPlayerService.PLAYER_INVALID, "Account disabled");
    }
    @ExceptionHandler(TokenInvalidedException.class)
    @ResponseBody
    public BaseResponse exceptionHandler(TokenInvalidedException e) {
        return new BaseResponse(TOKEN_INVALID, "Login failed or expired");
    }
    @ExceptionHandler(PlayerDisabledException.class)
    @ResponseBody
    public BaseResponse exceptionHandler(PlayerDisabledException e) {
        return new BaseResponse(IPlayerService.STATUS_DISABLED, "");
    }
    @ExceptionHandler(PlayerApiTooFastException.class)
    @ResponseBody
    public BaseResponse apiTooFastException(PlayerApiTooFastException e) {
        return new BaseResponse(IPlayerService.API_TOO_FAST, "");
    }
    @ExceptionHandler(CountryInvalidedException.class)
    @ResponseBody
    public BaseResponse countryInvalided(CountryInvalidedException e) {
        return new BaseResponse(IPlayerService.COUNTRY_INVALID, "");
    }
    @ExceptionHandler(MultiFormSubmitException.class)
    @ResponseBody
    public BaseResponse multiFormSubmit(MultiFormSubmitException e) {
        return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                ResponseMessageUtil.responseMessage("", "e1"));
    }
    @ExceptionHandler(BindException.class)
    @ResponseBody
    public BaseResponse bindExceptionHandler(BindException e) {
        BindingResult bindingResult = e.getBindingResult();
        List<ObjectError> allErrors = bindingResult.getAllErrors();
        List<Map<String,Object>> errorList = new ArrayList<>();
        for(ObjectError objectError:allErrors){
            if(objectError instanceof FieldError){
                FieldError error = (FieldError)objectError;
                Map<String,Object> data = new HashMap<>();
                data.put("name", error.getField());
                data.put("msgKey", error.getDefaultMessage());
                errorList.add(data);
            }
        }
        return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,errorList);
    }

}
