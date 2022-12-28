package com.oxo.ball.service.admin;

import com.oxo.ball.bean.dao.BallPaymentManagement;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 支付管理 服务类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
public interface IBallPaymentManagementService extends IService<BallPaymentManagement> {
    SearchResponse<BallPaymentManagement> search(BallPaymentManagement query, Integer pageNo, Integer pageSize);
    BallPaymentManagement findById(Long id);
    List<BallPaymentManagement> findByAll(BallPlayer ballPlayer);
    List<BallPaymentManagement> findByAll();
    List<BallPaymentManagement> findByAllTrue();
    BallPaymentManagement insert(BallPaymentManagement ballVip, MultipartFile file);
    Boolean delete(Long id);
    Boolean edit(BallPaymentManagement ballVip, MultipartFile file);
    Boolean status(BallPaymentManagement ballVip);

    BallPaymentManagement findByCallback(String s);

    BallPaymentManagement findByName(String payName);

}
