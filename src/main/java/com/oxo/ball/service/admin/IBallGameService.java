package com.oxo.ball.service.admin;

import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallGame;
import com.baomidou.mybatisplus.extension.service.IService;
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
public interface IBallGameService extends IService<BallGame> {

    SearchResponse<BallGame> search(BallGame query, Integer pageNo, Integer pageSize);
    SearchResponse<BallGame> searchOnBet(BallGame query, Integer pageNo, Integer pageSize);
    Boolean insert(BallGame ballGame);
    BaseResponse edit(BallGame ballGame);
    Boolean deleteById(Long id);
    void whenGameStart();
    void autoSetHot();

    Boolean editStatus(BallGame ballGame);
    Boolean editStatusTop(BallGame ballGame);
    Boolean editStatusHot(BallGame ballGame);
    Boolean editStatusEven(BallGame ballGame);

    BaseResponse recount(BallGame ballGame, BallAdmin currentUser);

    BaseResponse rollback(BallGame ballGame, BallAdmin currentUser);

    List<BallGame> statisReport(ReportDataRequest reportDataRequest);

    SearchResponse<BallGame> statisReport(ReportGameRequest reportGameRequest);

    BaseResponse handOpen(BallGame ballGame);

    Boolean uploadGameLogo(String key,String url, MultipartFile file);

    BaseResponse add(BallGame ballGame);

    BaseResponse cancel(BallGame ballGame, BallAdmin admin);

}
