package com.oxo.ball.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dao.BallAdmin;
import com.oxo.ball.bean.dao.BallPayBehalf;
import com.oxo.ball.bean.dto.api.behalfcha.PayBehalfNoticeDtoCHA;
import com.oxo.ball.bean.dto.api.fast.PayCallBackDtoFast;
import com.oxo.ball.bean.dto.api.in3.behalf.PayBehalfNoticeDto3;
import com.oxo.ball.bean.dto.api.inbehalf.PayBehalfNoticeDtoIN;
import com.oxo.ball.bean.dto.api.meta.PayCallBackDtoMeta;
import com.oxo.ball.bean.dto.api.mp.MpPayCallBack;
import com.oxo.ball.bean.dto.api.tnz.PayCallBackDtoTnz;
import com.oxo.ball.bean.dto.api.web.WebPayCallback;
import com.oxo.ball.bean.dto.api.xd.XdPayCallBack;
import com.oxo.ball.bean.dto.resp.BaseResponse;
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
public interface IBallPayBehalfService extends IService<BallPayBehalf> {
    SearchResponse<BallPayBehalf> search(BallPayBehalf query, Integer pageNo, Integer pageSize);

    BallPayBehalf findById(Long id);

    List<BallPayBehalf> findByType(Integer type);

    List<BallPayBehalf> findByAll();
    List<BallPayBehalf> findByAllTrue();

    BallPayBehalf insert(BallPayBehalf ballVip, MultipartFile file);

    Boolean delete(Long id);

    Boolean edit(BallPayBehalf ballVip, MultipartFile file);

    Boolean status(BallPayBehalf ballVip);

    List<BallPayBehalf> findByCallback(String s);

    void payCallBack(PayBehalfNoticeDtoIN payNotice);

    BaseResponse pay(Long id, Long wid, BallAdmin admin);

    void payCallBackCha(PayBehalfNoticeDtoCHA payNotice);

    List<BallPayBehalf> findByBankCode(String bankCode, Integer type);

    void payCallBackFast(PayCallBackDtoFast payNotice);

    void payCallBackIn3(PayBehalfNoticeDto3 payNotice);

    void payCallBackWow(PayBehalfNoticeDtoCHA payNotice);

    void payCallBackAllPay(PayBehalfNoticeDtoCHA payNotice);

    void payCallBackTNZ(PayCallBackDtoTnz payNotice);

    void payCallBackMETA(PayCallBackDtoMeta payNotice);

    void payCallBackMETAGG(PayCallBackDtoMeta payNotice);

    List<BallPayBehalf> findByAreaCode(Long id);

    void payCallBackWeb(WebPayCallback payNotice);

    void payCallBackXD(XdPayCallBack payNotice);

    void payCallBackMp(MpPayCallBack payNotice);
}
