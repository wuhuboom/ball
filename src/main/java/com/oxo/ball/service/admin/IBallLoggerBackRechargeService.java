//package com.oxo.ball.service.admin;
//
//import com.baomidou.mybatisplus.extension.service.IService;
//import com.oxo.ball.bean.dao.BallLoggerBackRecharge;
//import com.oxo.ball.bean.dao.BallPlayer;
//import com.oxo.ball.bean.dto.req.report.ReportDataRequest;
//import com.oxo.ball.bean.dto.resp.BaseResponse;
//import com.oxo.ball.bean.dto.resp.SearchResponse;
//
///**
// * <p>
// * 日志表 服务类
// * </p>
// *
// * @author oxo_jy
// * @since 2022-04-13
// */
//public interface IBallLoggerBackRechargeService extends IService<BallLoggerBackRecharge> {
//    SearchResponse<BallLoggerBackRecharge> search(BallLoggerBackRecharge queryParam, Integer pageNo, Integer pageSize);
//    BaseResponse statis(BallPlayer currPlayer);
//    BallLoggerBackRecharge insert(BallLoggerBackRecharge ballLoggerBack);
//    boolean edit(BallLoggerBackRecharge ballLoggerBack);
//    BaseResponse draw(BallLoggerBackRecharge id);
//    BallLoggerBackRecharge statis(ReportDataRequest reportDataRequest, int type);
//}
