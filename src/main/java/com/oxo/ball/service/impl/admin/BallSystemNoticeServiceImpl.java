package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dao.BallPlayerNotice;
import com.oxo.ball.bean.dao.BallSystemNotice;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.mapper.BallPlayerNoticeMapper;
import com.oxo.ball.mapper.BallSystemConfigMapper;
import com.oxo.ball.mapper.BallSystemNoticeMapper;
import com.oxo.ball.service.admin.IBallSystemNoticeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 系统公告 服务实现类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Service
public class BallSystemNoticeServiceImpl extends ServiceImpl<BallSystemNoticeMapper, BallSystemNotice> implements IBallSystemNoticeService {

    @Autowired
    BallPlayerNoticeMapper playerNoticeMapper;

    @Autowired
    BallSystemNoticeMapper systemNoticeMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    @Cacheable(value = "ball_player_notice", key = "#queryParam.playerId+''+#queryParam.language", unless = "#result.totalCount==0")
    public SearchResponse<BallSystemNotice> searchApp(BallSystemNotice queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallSystemNotice> response = new SearchResponse<>();
        Page<BallSystemNotice> page = new Page<>(pageNo, pageSize);

        IPage<BallSystemNotice> pages = systemNoticeMapper.listPage(page,queryParam);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }
    @Override
    public SearchResponse<BallSystemNotice> search(BallSystemNotice queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallSystemNotice> response = new SearchResponse<>();
        Page<BallSystemNotice> page = new Page<>(pageNo, pageSize);

        QueryWrapper<BallSystemNotice> query = new QueryWrapper<>();
        if(queryParam.getStatus()!=null){
            query.eq("status",queryParam.getStatus());
        }
        if(!StringUtils.isBlank(queryParam.getLanguage())){
            query.eq("language",queryParam.getLanguage());
        }
        IPage<BallSystemNotice> pages = page(page, query);
//        IPage<BallSystemNotice> pages = systemNoticeMapper.listPage(page,BallSystemNotice.builder()
//                .status(queryParam.getStatus())
//                .language(queryParam.getLanguage()==null?"en":queryParam.getLanguage())
//                .build());
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public BallSystemNotice insert(BallSystemNotice slideshow) {
        slideshow.setStatus(1);
        slideshow.setCreatedAt(System.currentTimeMillis());
        boolean save = save(slideshow);
        redisUtil.delKeys("ball_player_notice*");
        return slideshow;
    }

    @Override
    public Boolean delete(Long id) {
        redisUtil.delKeys("ball_player_notice*");
        return removeById(id);
    }

    @Override
    public Boolean edit(BallSystemNotice slideshow) {
        boolean b = updateById(slideshow);
        if(b){
            UpdateWrapper updateWrapper = new UpdateWrapper();
            updateWrapper.eq("notice_id",slideshow.getId());
            playerNoticeMapper.update(BallPlayerNotice.builder()
                    .status(0)
                    .build(),updateWrapper);
            redisUtil.delKeys("ball_player_notice*");
        }
        return b;
    }

    @Override
    public Boolean status(BallSystemNotice notice) {
        BallSystemNotice edit = BallSystemNotice.builder()
                .status(notice.getStatus())
                .build();
        edit.setId(notice.getId());
        boolean b = updateById(edit);
        if(b&&notice.getStatus()==1){
            UpdateWrapper updateWrapper = new UpdateWrapper();
            updateWrapper.eq("notice_id",notice.getId());
            playerNoticeMapper.update(BallPlayerNotice.builder()
                    .status(0)
                    .build(),updateWrapper);
        }
        redisUtil.delKeys("ball_player_notice*");
        return b;
    }

    @Override
    public BaseResponse setRead(Long noticeId, BallPlayer currentUser) {
        QueryWrapper query = new QueryWrapper();
        query.eq("player_id",currentUser.getId());
        query.eq("notice_id",noticeId);
        BallPlayerNotice ballPlayerNotice = playerNoticeMapper.selectOne(query);
        if(ballPlayerNotice==null){
            playerNoticeMapper.insert(BallPlayerNotice.builder()
                    .playerId(currentUser.getId())
                    .noticeId(noticeId)
                    .status(1)
                    .build());
        }else{
            UpdateWrapper updateWrapper = new UpdateWrapper();
            updateWrapper.eq("player_id",currentUser.getId());
            updateWrapper.eq("notice_id",noticeId);
            playerNoticeMapper.update(BallPlayerNotice.builder()
                    .status(1)
                    .build(),updateWrapper);
        }
        redisUtil.delKeys("ball_player_notice::"+currentUser.getId()+"*");
        return BaseResponse.SUCCESS;
    }
}
