package com.oxo.ball.controller.admin.logger;

import com.oxo.ball.bean.dao.BallLoggerLogin;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.service.admin.IBallLoggerService;
import com.oxo.ball.utils.PoiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 日志表 前端控制器
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@RestController
@RequestMapping("/ball/log/login")
public class BallLoggerLoginController {
    @Resource
    IBallLoggerService loggerService;
    @Autowired
    PoiUtil poiUtil;
    @PostMapping
    public Object index(BallLoggerLogin query,
                        @RequestParam(defaultValue = "1")Integer pageNo,
                        @RequestParam(defaultValue = "20") Integer pageSize){
        SearchResponse<BallLoggerLogin> search = loggerService.search(query, pageNo, pageSize);
        return BaseResponse.successWithData(search);
    }
    @GetMapping
    public Object export(BallLoggerLogin query){
        BaseResponse res = poiUtil.exportLoginLog(query);
        return res;
    }
}
