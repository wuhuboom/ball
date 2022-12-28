package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oxo.ball.bean.dao.BallLoggerLogin;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.mapper.BallLoggerMapper;
import com.oxo.ball.service.admin.IBallLoggerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 日志表 服务实现类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Service
public class BallLoggerServiceImpl extends ServiceImpl<BallLoggerMapper, BallLoggerLogin> implements IBallLoggerService {

    @Override
    public SearchResponse<BallLoggerLogin> search(BallLoggerLogin queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallLoggerLogin> response = new SearchResponse<>();
        Page<BallLoggerLogin> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallLoggerLogin> query = new QueryWrapper<>();
        if(!StringUtils.isBlank(queryParam.getIpAddr())){
            query.eq("ip_addr",queryParam.getIpAddr());
        }
        if(!StringUtils.isBlank(queryParam.getPlayerName())){
            query.eq("player_name",queryParam.getPlayerName());
        }
        query.orderByDesc("id");
        IPage<BallLoggerLogin> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public List<BallLoggerLogin> search(BallLoggerLogin queryParam) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("player_name",queryParam.getPlayerName());
        queryWrapper.select("ip_addr,GROUP_CONCAT(DISTINCT ip) ip,count(DISTINCT ip) id");
        queryWrapper.groupBy("ip_addr");
        List<BallLoggerLogin> list = list(queryWrapper);
        return list;
    }

    @Override
    public BallLoggerLogin insert(BallLoggerLogin loggerLogin) {
        save(loggerLogin);
        return loggerLogin;
    }

    @Override
    public BallLoggerLogin findPlayerLastLogin(BallPlayer ballPlayer) {
        QueryWrapper query = new QueryWrapper();
        query.eq("player_name",ballPlayer.getUsername());
        query.orderByDesc("id");
        Page<BallLoggerLogin> page = new Page<>(1, 1);
        IPage<BallLoggerLogin> pageList = page(page, query);
        List<BallLoggerLogin> records = pageList.getRecords();
        if(records==null||records.isEmpty()||records.get(0)==null){
            return null;
        }
        return records.get(0);
    }

    @Override
    public BallLoggerLogin search(String username, String ipAddr) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("player_name",username);
        queryWrapper.select("ip_addr,GROUP_CONCAT(DISTINCT ip) ip,count(DISTINCT ip) id");
        queryWrapper.eq("ip_addr",ipAddr);
        queryWrapper.groupBy("ip_addr");
        BallLoggerLogin list = getOne(queryWrapper);
        return list;
    }
}
