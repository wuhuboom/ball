package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.bean.dao.BallBank;
import com.oxo.ball.bean.dao.BallBankArea;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dao.BallSystemConfig;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.mapper.BallBankAreaMapper;
import com.oxo.ball.mapper.BallBankMapper;
import com.oxo.ball.service.admin.IBallBankService;
import com.oxo.ball.service.admin.IBallSystemConfigService;
import com.oxo.ball.utils.RedisUtil;
import com.oxo.ball.utils.ResponseMessageUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 银行卡 服务实现类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Service
public class BallBankServiceImpl extends ServiceImpl<BallBankMapper, BallBank> implements IBallBankService {

    @Autowired
    BallBankMapper ballBankMapper;
    @Autowired
    BallBankAreaMapper ballBankAreaMapper;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    IBallSystemConfigService systemConfigService;

    @Override
    public BaseResponse searchBank(BallBank queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallBank> response = new SearchResponse<>();
        Page<BallBank> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallBank> query = new QueryWrapper<>();
        query.eq("area_id",queryParam.getAreaId());
        IPage<BallBank> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return BaseResponse.successWithData(response);
    }

    @Override
    @Cacheable(value = "ball_bank_list", key = "#code", unless = "#result.empty")
    public List<BallBank> findAll(Integer code, List<BallBankArea> areaId) {
        List<Long> ids = new ArrayList<>();
        for(BallBankArea area:areaId){
            ids.add(area.getId());
        }
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.in("area_id",ids);
        queryWrapper.orderByAsc("bank_cname");
        queryWrapper.groupBy("bank_code");
        return list(queryWrapper);
    }

    @Override
    public List<BallBank> findAll() {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("status",1);
        List<BallBankArea> list = ballBankAreaMapper.selectList(queryWrapper);
        List<Long> ids = new ArrayList<>();
        for(BallBankArea item:list){
            ids.add(item.getId());
        }
        queryWrapper = new QueryWrapper();
        queryWrapper.in("id",ids);
        return list(queryWrapper);
    }

    @Override
    @Cacheable(value = "ball_bank_list", key = "#player.areaCode", unless = "#result.empty")
    public List<BallBank> findAll(BallPlayer player) {
        //查询所有银行大分类
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("status",1);
        List<BallBankArea> list1 = ballBankAreaMapper.selectList(queryWrapper);
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        List<Long> ids = new ArrayList<>();
        for(BallBankArea item:list1){
            if(!StringUtils.isBlank(item.getAreaCode())){
                String[] split = item.getAreaCode().split(",");
                List<String> strings = Arrays.asList(split);
                if(systemConfig.getBankListSwtich()==1){
                    if(strings.contains(player.getAreaCode())){
                        ids.add(item.getId());
                    }
                }else{
                    ids.add(item.getId());
                }
            }
        }
        queryWrapper = new QueryWrapper();
        if(ids.isEmpty()){
            ids.add(-100L);
        }
        queryWrapper.in("area_id",ids);
        List list = list(queryWrapper);
        return list;
    }

    @Override
    public BaseResponse insert(BallBank ballBank){
        if(StringUtils.isBlank(ballBank.getBankCname())){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e17"));
        }
        String[] split = ballBank.getBankCname().split("\n");
        List<BallBank> list = new ArrayList<>();
        for(String item:split){
            int i = item.indexOf(" ");
            list.add(BallBank.builder()
                    .bankCode(item.substring(0,i).trim())
                    .bankCname(item.substring(i+1).trim())
                    .areaId(ballBank.getAreaId())
                    .build());
        }
        int count = ballBankMapper.insertBatch(list);
        redisUtil.delKeys("ball_bank_list::*");
        return BaseResponse.SUCCESS;
    }

    @Override
    public BaseResponse edit(BallBank ballBank) {
        boolean b = updateById(ballBank);
        redisUtil.delKeys("ball_bank_list::*");
        return BaseResponse.SUCCESS;
    }

    @Override
    public BaseResponse del(long id) {
        boolean b = removeById(id);
        redisUtil.delKeys("ball_bank_list::*");
        return BaseResponse.SUCCESS;
    }

    @Override
    public BallBank findById(Long bankId) {
        return getById(bankId);
    }

    @Override
    public List<BallBank> findByName(String bankName) {
        QueryWrapper query = new QueryWrapper();
        query.eq("bank_cname",bankName);
        return list(query);
    }

    @Override
    public BaseResponse search(BallBankArea ballBankArea, Integer pageNo, Integer pageSize) {
        SearchResponse<BallBankArea> response = new SearchResponse<>();
        Page<BallBankArea> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallBankArea> query = new QueryWrapper<>();
        IPage<BallBankArea> pages = ballBankAreaMapper.selectPage(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return BaseResponse.successWithData(response);
    }

    @Override
    public BaseResponse insertArea(BallBankArea ballBankArea) {
        try {
            ballBankArea.setStatus(1);
            ballBankAreaMapper.insert(ballBankArea);
            redisUtil.delKeys("ball_bank_list::*");
        }catch (Exception ex){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e18"));
        }
        return BaseResponse.successWithData(ballBankArea);
    }

    @Override
    public BaseResponse editArea(BallBankArea ballBankArea) {
        try {
            int i = ballBankAreaMapper.updateById(ballBankArea);
            redisUtil.delKeys("ball_bank_list::*");
        }catch (Exception ex){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e18"));
        }
        redisUtil.del("ball_bank_enabled::one");
        return BaseResponse.SUCCESS;
    }

    @Override
    public BaseResponse delArea(long id) {
        int b = ballBankAreaMapper.deleteById(id);
        redisUtil.delKeys("ball_bank_list::*");
        redisUtil.del("ball_bank_enabled::one");
        return BaseResponse.SUCCESS;
    }

    @Override
    public BaseResponse statusArea(BallBankArea params) {
//        UpdateWrapper update = new UpdateWrapper();
//        ballBankAreaMapper.update(BallBankArea.builder()
//                .status(0)
//                .build(),update);
//        BallBankArea byId = ballBankAreaMapper.selectById(id);
//        update.eq("code",byId.getCode());
//        ballBankAreaMapper.update(BallBankArea.builder()
//                .status(1)
//                .build(),update);
//        redisUtil.del("ball_bank_enabled::one");
//        redisUtil.delKeys("ball_bank_list::*");
        ballBankAreaMapper.updateById(BallBankArea.builder()
                .id(params.getId())
                .status(params.getStatus()==1?2:1)
                .build());
        redisUtil.del("ball_bank_enabled::one");
        return BaseResponse.successWithMsg("edit success~");
    }

    @Override
    public BallBankArea findByAreaId(Long id) {
        return ballBankAreaMapper.selectById(id);
    }

    @Override
    @Cacheable(value = "ball_bank_enabled",key = "'one'")
    public List<BallBankArea> findEnabled() {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("status",1);
        return ballBankAreaMapper.selectList(queryWrapper);
    }

    @Override
    public List<BallBankArea> findByCode(Integer payType) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("code",payType);
        return ballBankAreaMapper.selectList(queryWrapper);
    }

    @Override
    public List<BallBank> findByName(Long areaId, String bankName) {
        QueryWrapper query = new QueryWrapper();
        query.eq("bank_cname",bankName);
        query.eq("area_id",areaId);
        return list(query);
    }
}
