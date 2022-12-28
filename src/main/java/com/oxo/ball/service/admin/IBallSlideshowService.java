package com.oxo.ball.service.admin;

import com.oxo.ball.bean.dao.BallSlideshow;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 轮播图 服务类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
public interface IBallSlideshowService extends IService<BallSlideshow> {

    SearchResponse<BallSlideshow> search(BallSlideshow query, Integer pageNo, Integer pageSize);
    BallSlideshow insert(BallSlideshow slideshow, MultipartFile file);
    Boolean delete(Long id);
    Boolean edit(BallSlideshow slideshow,MultipartFile file);
    Boolean status(BallSlideshow slideshow);
}
