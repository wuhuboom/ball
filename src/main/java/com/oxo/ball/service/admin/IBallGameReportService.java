package com.oxo.ball.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallGame;
import com.oxo.ball.bean.dao.BallGameReport;
import com.oxo.ball.bean.dto.req.report.ReportDataRequest;
import com.oxo.ball.bean.dto.req.report.ReportGameRequest;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 游戏赛事 服务类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
public interface IBallGameReportService extends IService<BallGameReport> {
    SearchResponse<BallGameReport> search(BallGameReport query, Integer pageNo, Integer pageSize);
    Boolean insert(BallGameReport ballGameReport);
    void dayStatis();
}
