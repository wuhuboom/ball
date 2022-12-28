package com.oxo.ball.service.admin;

import com.oxo.ball.bean.dao.BallPayBehalf;
import com.oxo.ball.bean.dao.BallWithdrawManagement;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 提现方式 服务类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
public interface IBallWithdrawManagementService extends IService<BallWithdrawManagement> {

    SearchResponse<BallWithdrawManagement> search(BallWithdrawManagement query, Integer pageNo, Integer pageSize);

    void insert(BallWithdrawManagement ballSlideshowRequest, MultipartFile file);

    Boolean edit(BallWithdrawManagement withdrawManagement, MultipartFile file);

    Boolean status(BallWithdrawManagement withdrawManagement);

    Boolean delete(Long id);

    List<BallWithdrawManagement> findAll();
    BallWithdrawManagement findById(Long id);
}
