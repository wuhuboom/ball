package com.oxo.ball.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallProxyLogger;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dto.req.player.WithdrawalLogRequest;
import com.oxo.ball.bean.dto.req.report.ReportDataRequest;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 代理统计日志表 服务类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
public interface IBallProxyLoggerService extends IService<BallProxyLogger> {
    SearchResponse<BallProxyLogger> search(BallProxyLogger queryParam, Integer pageNo, Integer pageSize);
    BallProxyLogger insert(BallProxyLogger proxyLogger);
    void statisEveryDay();

    BaseResponse statis(BallProxyLogger queryParam);
    BaseResponse statis2(BallProxyLogger queryParam) throws ParseException;
    BaseResponse statis2() throws ParseException;

    BaseResponse statis2_1(BallProxyLogger queryParam) throws ParseException;
}
