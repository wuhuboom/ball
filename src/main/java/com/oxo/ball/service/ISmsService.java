package com.oxo.ball.service;

import com.oxo.ball.bean.dao.BallApp;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.bean.dto.resp.admin.PlayerSmsCodeDTO;

import java.util.List;

public interface ISmsService {
    BaseResponse sendSms(String username, String phone, String code, String key);

    List<String> smsList();
}
