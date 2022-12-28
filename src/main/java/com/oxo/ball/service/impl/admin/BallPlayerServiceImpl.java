package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.oxo.ball.bean.dao.*;
import com.oxo.ball.bean.dto.model.RechargeRebateDto;
import com.oxo.ball.bean.dto.queue.MessageQueueDTO;
import com.oxo.ball.bean.dto.queue.PlayerChatMessage;
import com.oxo.ball.bean.dto.req.admin.PlayerRepairRecharge;
import com.oxo.ball.bean.dto.req.admin.PlayerRepairWithdrawal;
import com.oxo.ball.bean.dto.req.admin.QueryActivePlayerRequest;
import com.oxo.ball.bean.dto.req.report.ReportDataRequest;
import com.oxo.ball.bean.dto.req.report.ReportStandardRequest;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.bean.dto.resp.admin.ProxyStatis3Dto;
import com.oxo.ball.contant.LogsContant;
import com.oxo.ball.contant.RedisKeyContant;
import com.oxo.ball.mapper.BallPlayerMapper;
import com.oxo.ball.service.IMessageQueueService;
import com.oxo.ball.service.admin.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.service.impl.BasePlayerService;
import com.oxo.ball.service.player.IPlayerService;
import com.oxo.ball.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * <p>
 * 玩家账号 服务实现类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Service
public class BallPlayerServiceImpl extends ServiceImpl<BallPlayerMapper, BallPlayer> implements IBallPlayerService {
    private Logger apiLog = LoggerFactory.getLogger(LogsContant.API_LOG);

    @Resource
    BasePlayerService basePlayerService;
    @Autowired
    IPlayerService playerService;
    @Resource
    IBallBalanceChangeService ballBalanceChangeService;
    @Autowired
    IBallSystemConfigService systemConfigService;
    @Autowired
    IBallVipService vipService;
    @Autowired
    IBallLoggerHandsupService loggerHandsupService;
    @Autowired
    private IMessageQueueService messageQueueService;
    @Autowired
    IBallBankCardService bankCardService;
    @Autowired
    IBallVirtualCurrencyService virtualCurrencyService;
    @Autowired
    IBallSimCurrencyService simCurrencyService;
    @Autowired
    private IBallDepositPolicyService depositPolicyService;
    @Autowired
    IBallLoggerWithdrawalService loggerWithdrawalService;
    @Autowired
    IBallBetService ballBetService;
    @Autowired
    BallPlayerMapper ballPlayerMapper;
    @Autowired
    private IBallLoggerRechargeService loggerRechargeService;
    @Autowired
    IBallBankService bankService;
    @Autowired
    IBallPaymentManagementService paymentManagementService;
    @Autowired
    IBallWithdrawManagementService withdrawManagementService;
    @Autowired
    IBallPayBehalfService payBehalfService;
    @Autowired
    BallPlayerMapper mapper;
    @Autowired
    private IBallApiConfigService apiConfigService;
    @Autowired
    RedisUtil redisUtil;
    @Override
    public SearchResponse<BallPlayer> search(BallPlayer paramQuery, Integer pageNo, Integer pageSize) {
        SearchResponse<BallPlayer> response = new SearchResponse<>();

        Page<BallPlayer> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallPlayer> query = new QueryWrapper<>();
        //如果传入的用户ID或者用户名,然后判定查询条件
//        query.select("id","user_id","username","balance","the_last_ip","status_online","the_new_login_time","superior_name",
//                "invitation_code","directly_subordinate_num","group_size","account_type","vip_rank","vip_level","status","created_at");
        BallPlayer playerTree = null;
        if(paramQuery.getUserId()!=null){
            if(paramQuery.getTreeType()==null){
                query.eq("user_id",paramQuery.getUserId());
            }else{
                playerTree = basePlayerService.findByUserId(paramQuery.getUserId());
            }
        }
        if(!StringUtils.isBlank(paramQuery.getUsername())){
            if(paramQuery.getTreeType()==null) {
                query.eq("username",paramQuery.getUsername());
            }else{
                playerTree = basePlayerService.findByUsername(paramQuery.getUsername());
            }
        }
        if(!StringUtils.isBlank(paramQuery.getInvitationCode())){
            if(paramQuery.getTreeType()==null) {
                query.eq("invitation_code",paramQuery.getInvitationCode());
            }else{
                playerTree = basePlayerService.findByInvitationCode(paramQuery.getInvitationCode());
            }
        }
        if(paramQuery.getSuperiorId()!=null){
            query.eq("superior_id",paramQuery.getSuperiorId());
        }
        if(paramQuery.getTreeType()!=null){
            if(playerTree!=null){
                //要查直属下级,或者全部下级
                if(paramQuery.getTreeType()==1){
                    //查直属
                    query.eq("superior_id",playerTree.getId());
                }else if(paramQuery.getTreeType()==2){
                    //查全部下级
                    query.likeRight("super_tree",playerTree.getSuperTree()+playerTree.getId()+"\\_");
                }
            }else{
                query.eq("user_id",-100);
            }
        }
        if(!StringUtils.isBlank(paramQuery.getPhone())){
            query.eq("phone",paramQuery.getPhone());
        }
        if(!StringUtils.isBlank(paramQuery.getEmail())){
            query.eq("email",paramQuery.getEmail());
        }
        if(paramQuery.getAccountType()!=null){
            query.eq("account_type",paramQuery.getAccountType());
        }
        if(paramQuery.getStatusOnline()!=null){
            query.eq("status_online",paramQuery.getStatusOnline());
        }
        if(paramQuery.getVipRank()!=null){
            query.eq("vip_rank",paramQuery.getVipRank());
        }
        if(paramQuery.getVipLevel()!=null){
            query.eq("vip_level",paramQuery.getVipLevel());
        }
        if(paramQuery.getOfflines()!=null){
            //未登录天数,登录时间<当前时间-指定天数时间
            query.lt("the_new_login_time",System.currentTimeMillis()-TimeUtil.TIME_ONE_DAY*paramQuery.getOfflines());
        }
        if(!StringUtils.isBlank(paramQuery.getSuperTree())){
            //解密核对码，得到用户ID，然后按ID查询
            String encrypt = Base64Util.decrypt(paramQuery.getSuperTree());
            try {
                Long userId = Long.parseLong(encrypt);
                query.eq("user_id",userId);
            }catch (Exception ex){
                query.eq("user_id",-100);
            }
        }
        if(paramQuery.getVersion()!=null && paramQuery.getVersion()==-1){
            query.orderByDesc("group_size");
        }else{
            query.orderByDesc("id");
        }
        IPage<BallPlayer> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());

        return response;
    }

    @Override
    public SearchResponse<BallPlayer> searchlike(BallPlayer paramQuery, Integer pageNo, Integer pageSize) {
        SearchResponse<BallPlayer> response = new SearchResponse<>();

        Page<BallPlayer> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallPlayer> query = new QueryWrapper<>();
        //如果传入的用户ID或者用户名,然后判定查询条件
//        query.select("id","user_id","username","balance","the_last_ip","status_online","the_new_login_time","superior_name",
//                "invitation_code","directly_subordinate_num","group_size","account_type","vip_rank","vip_level","status","created_at");
        BallPlayer playerTree = null;
        if(paramQuery.getUserId()!=null){
            if(paramQuery.getTreeType()==null){
                query.eq("user_id",paramQuery.getUserId());
            }else{
                playerTree = basePlayerService.findByUserId(paramQuery.getUserId());
            }
        }
        if(!StringUtils.isBlank(paramQuery.getUsername())){
            if(paramQuery.getTreeType()==null) {
                query.like("username",paramQuery.getUsername());
            }else{
                playerTree = basePlayerService.findByUsername(paramQuery.getUsername());
            }
        }
        if(!StringUtils.isBlank(paramQuery.getInvitationCode())){
            if(paramQuery.getTreeType()==null) {
                query.eq("invitation_code",paramQuery.getInvitationCode());
            }else{
                playerTree = basePlayerService.findByInvitationCode(paramQuery.getInvitationCode());
            }
        }
        if(paramQuery.getSuperiorId()!=null){
            query.eq("superior_id",paramQuery.getSuperiorId());
        }
        if(paramQuery.getTreeType()!=null){
            if(playerTree!=null){
                //要查直属下级,或者全部下级
                if(paramQuery.getTreeType()==1){
                    //查直属
                    query.eq("superior_id",playerTree.getId());
                }else if(paramQuery.getTreeType()==2){
                    //查全部下级
                    query.likeRight("super_tree",playerTree.getSuperTree()+playerTree.getId()+"\\_");
                }
            }else{
                query.eq("user_id",-100);
            }
        }
        if(!StringUtils.isBlank(paramQuery.getPhone())){
            query.eq("phone",paramQuery.getPhone());
        }
        if(!StringUtils.isBlank(paramQuery.getEmail())){
            query.eq("email",paramQuery.getEmail());
        }
        if(paramQuery.getAccountType()!=null){
            query.eq("account_type",paramQuery.getAccountType());
        }
        if(paramQuery.getStatusOnline()!=null){
            query.eq("status_online",paramQuery.getStatusOnline());
        }
        if(paramQuery.getVipRank()!=null){
            query.eq("vip_rank",paramQuery.getVipRank());
        }
        if(paramQuery.getVipLevel()!=null){
            query.eq("vip_level",paramQuery.getVipLevel());
        }
        if(paramQuery.getOfflines()!=null){
            //未登录天数,登录时间<当前时间-指定天数时间
            query.lt("the_new_login_time",System.currentTimeMillis()-TimeUtil.TIME_ONE_DAY*paramQuery.getOfflines());
        }
        if(!StringUtils.isBlank(paramQuery.getSuperTree())){
            //解密核对码，得到用户ID，然后按ID查询
            String encrypt = Base64Util.decrypt(paramQuery.getSuperTree());
            try {
                Long userId = Long.parseLong(encrypt);
                query.eq("user_id",userId);
            }catch (Exception ex){
                query.eq("user_id",-100);
            }
        }
        if(paramQuery.getVersion()!=null && paramQuery.getVersion()==-1){
            query.orderByDesc("group_size");
        }else{
            query.orderByDesc("id");
        }
        IPage<BallPlayer> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());

        return response;
    }

    @Override
    public BaseResponse insert(BallPlayer registRequest) {
        Long parentPlayerId = 0L;
        String superTree = "_";
        String parentPlayerName = "";
        BallPlayer parentPlayer=null;
        //检查数据库里面是否有用户名
        BallPlayer ballPlayer = basePlayerService.findByUsername(registRequest.getUsername());
        if (ballPlayer != null) {
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("username", "name_exsit"));
        }
        String phone = null;
        if(!StringUtils.isBlank(registRequest.getAreaCode())&&!StringUtils.isBlank(registRequest.getPhone())){
            phone = registRequest.getAreaCode()+registRequest.getPhone();
            BallPlayer byPhone = basePlayerService.findByPhone(registRequest.getAreaCode(), phone);
            if(byPhone!=null){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("phone", "phone_exsit"));
            }
        }
        //创建测试不给创建邀请码
        String invitationCode = "";
        if(registRequest.getAccountType()==2){
            //是否有输入上级
            if(!"0".equals(registRequest.getSuperiorName()) && !StringUtils.isBlank(registRequest.getSuperiorName())) {
                //邀请码是否正确
                parentPlayer = basePlayerService.findByUsername(registRequest.getSuperiorName());
                if (parentPlayer == null || parentPlayer.getStatus()==2) {
                    return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                            ResponseMessageUtil.responseMessage("superiorName", "superiorName"));
                } else {
                    apiLog.info("admin-regist_parent:{}",parentPlayer);
                    parentPlayerName = parentPlayer.getUsername();
                    //邀请码正确,注册账号上级为邀请码关联账号
                    parentPlayerId = parentPlayer.getId();
                    //tree
                    superTree = (StringUtils.isBlank(parentPlayer.getSuperTree())?"_":parentPlayer.getSuperTree())+parentPlayer.getId()+"_";
                }
            }
            while (true){
                invitationCode = String.valueOf(TimeUtil.getRandomNum(1234567, 9876543));
                BallPlayer byInvitationCode = basePlayerService.findByInvitationCode(invitationCode);
                if(byInvitationCode==null){
                    break;
                }
            }
        }

        BallPlayer save = BallPlayer.builder()
                .version(1L)
                .username(registRequest.getUsername())
                .password(PasswordUtil.genPasswordMd5(registRequest.getEditPwd()))
                .invitationCode(invitationCode)
                //会员ID 10位数字,1+ymdd+hhmmss,12614125401
                .userId(basePlayerService.createUserId())
                .superiorId(parentPlayerId)
                .superTree(superTree)
                .vipRank(BallPlayer.getTreeCount(superTree))
                .accountType(registRequest.getAccountType())
                .status(1)
                .theNewIp("--")
                .statusOnline(0)
                .balance(0L)
                .superiorName(parentPlayerName)
                .areaCode(registRequest.getAreaCode())
                .email(registRequest.getEmail())
                .remark(registRequest.getRemark())
                .vipLevel(0)
                .build();
        if(!StringUtils.isEmpty(phone)){
            save.setPhone(phone);
        }

        MapUtil.setCreateTime(save);

        boolean res = save(save);
        if (res) {
            //如果保存成功，并且有上级，重新计算上级的团队和下属计数
            if(save.getSuperiorId()!=0){
                //上级直属下级+1,团队人数+1
                while (true){
                    BallPlayer parentPlayerEdit = BallPlayer.builder()
                            .version(parentPlayer.getVersion())
                            .directlySubordinateNum(parentPlayer.getDirectlySubordinateNum()+1)
                            .groupSize(parentPlayer.getGroupSize()+1)
                            .build();
                    parentPlayerEdit.setId(parentPlayerId);
                    boolean isSucc = basePlayerService.editAndClearCache(parentPlayerEdit, parentPlayer);
                    if(isSucc){
                        break;
                    }else{
                        parentPlayer = basePlayerService.findById(parentPlayerId);
                    }
                }
                //上级的上上上。。级团队人数+1
                String treePath = parentPlayer.getSuperTree();
                if(!StringUtils.isBlank(treePath)&&!treePath.equals("_")){
                    String ids = StringUtils.join(treePath.split("_"), ",").substring(1);
                    if(!StringUtils.isBlank(ids)){
                        basePlayerService.editMultGroupNum(ids,1);
                    }
                }
            }
        }
        return res ? BaseResponse.successWithMsg("ok") : BaseResponse.failedWithMsg("error");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse edit(BallPlayer ballPlayer) {
        BallPlayer byId = basePlayerService.findById(ballPlayer.getId());
        //修改了上级的话,原上级需要减团队人数和下级人数
        //新上级需要加团队人数和下级人数
        //先判定是否设置了上级,或者自己设置为顶级
        boolean hasOldParent = false;
        //如果是空和不变就不管,0和变化的时候再处理
        if(!StringUtils.isBlank(ballPlayer.getSuperiorName())&&!byId.getSuperiorName().equals(ballPlayer.getSuperiorName())){
            BallPlayer newParentPlayer = null;
            if("0".equals(ballPlayer.getSuperiorName())){
                ballPlayer.setSuperiorId(0L);
                ballPlayer.setSuperiorName("");
                if(byId.getSuperiorId()!=0){
                    //如果原来有上级
                    hasOldParent = true;
                }
            }else{
                newParentPlayer =  basePlayerService.findByUsername(ballPlayer.getSuperiorName());
                ballPlayer.setSuperiorId(newParentPlayer.getId());
                //新上级是本号下级就不给改
                if(newParentPlayer.getSuperTree().startsWith(byId.getSuperTree()+byId.getId()+"_")){
                    return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                            ResponseMessageUtil.responseMessage("", "e32"));
                }
                if(!ballPlayer.getSuperiorId().equals(byId.getSuperiorId())
                        && byId.getSuperiorId()!=0
                        ){
                    hasOldParent = true;
                }
            }
            //如果修改了上级将会影响所有上级的:团队人数
            //如果修改了上级将会影响自己和所有下级的:团队人数,层级,树结构
            //有旧上级
            String oldSuperTree = byId.getSuperTree()+byId.getId()+"_";
            if(hasOldParent){
                //原上级的直属下级 -1
                // 团队数-本号团队数
                BallPlayer parentPlayer = basePlayerService.findById(byId.getSuperiorId());
                while (true){
                    BallPlayer parentPlayerEdit = BallPlayer.builder()
                            .version(parentPlayer.getVersion())
                            .directlySubordinateNum(parentPlayer.getDirectlySubordinateNum()-1)
                            .groupSize(parentPlayer.getGroupSize()-1-byId.getGroupSize())
                            .build();
                    parentPlayerEdit.setId(parentPlayer.getId());
                    boolean isSucc = basePlayerService.editAndClearCache(parentPlayerEdit, parentPlayer);
                    if(isSucc){
                        break;
                    }else{
                        parentPlayer = basePlayerService.findById(byId.getSuperiorId());
                    }
                }
                //上级的上上上。。级团队人数-1-本号团队人数
                String treePath = parentPlayer.getSuperTree();
                if(!StringUtils.isBlank(treePath)&&!treePath.equals("_")){
                    String ids = StringUtils.join(treePath.split("_"), ",").substring(1);
                    if(!StringUtils.isBlank(ids)) {
                        basePlayerService.editMultGroupNum(ids, -1-byId.getGroupSize());
                    }
                }
            }
            //有新上级
            if(ballPlayer.getSuperiorId()>0){
                //新上级直属+1
                //新上级团队+自身团队
                while (true){
                    BallPlayer parentPlayerEdit = BallPlayer.builder()
                            .version(newParentPlayer.getVersion())
                            .directlySubordinateNum(newParentPlayer.getDirectlySubordinateNum()+1)
                            .groupSize(newParentPlayer.getGroupSize()+1+byId.getGroupSize())
                            .build();
                    parentPlayerEdit.setId(newParentPlayer.getId());
                    boolean isSucc = basePlayerService.editAndClearCache(parentPlayerEdit, newParentPlayer);
                    if(isSucc){
                        break;
                    }else{
                        newParentPlayer = basePlayerService.findById(byId.getSuperiorId());
                    }
                }
                //上级的上上上。。级团队人数+1+自身团队
                String treePath = newParentPlayer.getSuperTree();
                if(!StringUtils.isBlank(treePath)&&!treePath.equals("_")){
                    String ids = StringUtils.join(treePath.split("_"), ",").substring(1);
                    if(!StringUtils.isBlank(ids)) {
                        basePlayerService.editMultGroupNum(ids, 1+byId.getGroupSize());
                    }
                }
                ballPlayer.setSuperTree((StringUtils.isBlank(newParentPlayer.getSuperTree())?"_":newParentPlayer.getSuperTree())+newParentPlayer.getId()+"_");
                //本号的所有下级修改superTree
                editSuperTree(ballPlayer,oldSuperTree);
            }else{
                ballPlayer.setSuperTree("_");
                //没有上级也需要修改下级树
                editSuperTree(ballPlayer,oldSuperTree);
            }
        }
        BallPlayer edit = BallPlayer.builder()
                .areaCode(ballPlayer.getAreaCode())
                .email(ballPlayer.getEmail())
                .accountType(ballPlayer.getAccountType())
                .remark(ballPlayer.getRemark())
                .superiorName(ballPlayer.getSuperiorName())
                .superiorId(ballPlayer.getSuperiorId())
                .superTree(ballPlayer.getSuperTree())
                .vipRank(BallPlayer.getTreeCount(ballPlayer.getSuperTree()))
                .build();
        if(!StringUtils.isBlank(ballPlayer.getAreaCode())&&!StringUtils.isBlank(ballPlayer.getPhone())){
            edit.setPhone(ballPlayer.getAreaCode()+ballPlayer.getPhone());
        }
        edit.setId(ballPlayer.getId());
        boolean b = basePlayerService.editAndClearCache(edit, byId);
        return b?BaseResponse.SUCCESS:BaseResponse.failedWithMsg("error");
    }

    private void editSuperTree(BallPlayer ballPlayer, String oldSuperTree) {
        //id = 2,superTree = _1_
        //原号下级 _1_2_...
        //如果没有上级,下级应该是_2_...,where st like _1_2_%,rep (st,'_1_2_',_2_)

        //id = 4,superTree = _1_2_3_
        //原号下级 _1_2_3_4...
        //如果上级变为2,下级应该是1_2_4_...,where st like _1_2_3_4_%,rep (st,'_1_2_3_4_',_1_2_4_)

        //原下级tree = 原上级tree+_本号id_
        //新下级tree = 新tree+本号id_
        String newSuperTree = ballPlayer.getSuperTree()+ballPlayer.getId()+"_";
        //层级加减
        int oldRank = BallPlayer.getTreeCount(oldSuperTree);
        int newRank = BallPlayer.getTreeCount(newSuperTree);
        baseMapper.updateSuperTree(oldSuperTree,newSuperTree,newRank-oldRank);
    }

    @Override
    public Boolean editPwd(BallPlayer ballPlayer) {
        BallPlayer byId = basePlayerService.findById(ballPlayer.getId());
        BallPlayer edit = BallPlayer.builder()
                .password(PasswordUtil.genPasswordMd5(ballPlayer.getEditPwd()))
                .build();
        edit.setId(ballPlayer.getId());
        boolean b = basePlayerService.editAndClearCache(edit, byId);
        return b;
    }

    @Override
    public Boolean editPayPwd(BallPlayer ballPlayer) {
        BallPlayer byId = basePlayerService.findById(ballPlayer.getId());
        BallPlayer edit = BallPlayer.builder()
                .payPassword(PasswordUtil.genPasswordMd5(ballPlayer.getEditPayPwd()))
                .build();
        edit.setId(ballPlayer.getId());
        boolean b = basePlayerService.editAndClearCache(edit, byId);
        return b;
    }

    @Override
    public Boolean editStatus(BallPlayer ballPlayer) {
        BallPlayer byId = basePlayerService.findById(ballPlayer.getId());
        BallPlayer edit = BallPlayer.builder()
                .status(ballPlayer.getStatus()==1?2:1)
                .build();
        edit.setId(ballPlayer.getId());
        boolean b = basePlayerService.editAndClearCache(edit, byId);
        return b;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse editAddBalance(BallPlayer requestParam) throws JsonProcessingException {
        if(requestParam.getDbalance()==0){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e55"));
        }
        BallPlayer player = basePlayerService.findById(requestParam.getId());
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        if(systemConfig.getRechargeMax()!=null
                && systemConfig.getRechargeMax()!=0
                && requestParam.getDbalance()>systemConfig.getRechargeMax()){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e33"));
        }
        Long realMoney = Double.valueOf(BigDecimalUtil.mul(requestParam.getDbalance(),BigDecimalUtil.PLAYER_MONEY_UNIT)).longValue();
//        RechargeHanderDTO rechargeHander = rechargeHander(player, requestParam, realMoney, systemConfig);
        while(true){
            BallPlayer edit = BallPlayer.builder()
                    .version(player.getVersion())
                    .balance(player.getBalance()+realMoney)
                    .build();
            edit.setId(requestParam.getId());
            if(requestParam.getQrmult()==null||requestParam.getQrmult()==1){
                if(systemConfig.getRechargeCodeConversionRate()!=null&&systemConfig.getRechargeCodeConversionRate()>0){
                    //累计打码量,查询配置,是否需要*比例
                    Double mul = BigDecimalUtil.mul(realMoney, BigDecimalUtil.div(systemConfig.getRechargeCodeConversionRate(), 100));
                    edit.setCumulativeQr((player.getCumulativeQr()==null?0:player.getCumulativeQr())+mul.intValue());
                }else{
                    edit.setCumulativeQr((player.getCumulativeQr()==null?0:player.getCumulativeQr())+realMoney);
                }
            }else{
                edit.setCumulativeQr((player.getCumulativeQr()==null?0:player.getCumulativeQr())+realMoney*requestParam.getQrmult());
            }
            //插入账变
            int changeType = -1;
            if(realMoney>0){
                if(requestParam.getBalanceType()==1){
                    changeType = 6;
                    edit.setArtificialAdd(player.getArtificialAdd()==null?realMoney:player.getArtificialAdd()+realMoney);
                }else{
                    changeType = requestParam.getBalanceType();
                }
            }else{
                changeType = 8;
                edit.setArtificialSubtract(player.getArtificialSubtract()==null?realMoney:player.getArtificialSubtract()+realMoney);
            }
            boolean b = basePlayerService.editAndClearCache(edit, player);
            if(b){
                BallBalanceChange balanceChange = BallBalanceChange.builder()
                        .playerId(player.getId())
                        .accountType(player.getAccountType())
                        .userId(player.getUserId())
                        .parentId(player.getSuperiorId())
                        .username(player.getUsername())
                        .superTree(player.getSuperTree())
                        .initMoney(player.getBalance())
                        .changeMoney(realMoney)
                        .dnedMoney(edit.getBalance())
                        // key:admin_add
//                        .remark("管理员上分")
//                        .remark("admin_add")
                        .remark(requestParam.getQrRemark())
                        .createdAt(System.currentTimeMillis())
                        .balanceChangeType(changeType)
                        .build();
                ballBalanceChangeService.insert(balanceChange);
                //上分才计算奖励啥的
                if(changeType==6){
//                    if(rechargeHander.getDiscountQuota()>0){
//                        BallBalanceChange saveChangeDiscount = BallBalanceChange.builder()
//                                .playerId(player.getId())
//                                .accountType(player.getAccountType())
//                                .userId(player.getUserId())
//                                .parentId(player.getSuperiorId())
//                                .username(player.getUsername())
//                                .superTree(player.getSuperTree())
//                                .initMoney(rechargeHander.getEdit().getBalance()-rechargeHander.getDiscountQuota())
//                                .changeMoney(rechargeHander.getDiscountQuota())
//                                .dnedMoney(rechargeHander.getEdit().getBalance())
//                                .createdAt(System.currentTimeMillis())
//                                .balanceChangeType(19)
//                                .orderNo(0L)
//                                .build();
//                        ballBalanceChangeService.insert(saveChangeDiscount);
//                    }
                    //插入上下分记录,上分才添加上分记录
                    loggerHandsupService.insert(BallLoggerHandsup.builder()
                            .playerId(player.getId())
                            .accountType(player.getAccountType())
                            .username(player.getUsername())
                            .createdAt(TimeUtil.getNowTimeMill())
                            .userId(player.getUserId())
                            .money(realMoney)
                            .remark(requestParam.getQrRemark())
                            .qrmult(requestParam.getQrmult())
                            .operUser(requestParam.getOperUser())
                            .type(realMoney>0?1:0)
                            .superTree(player.getSuperTree())
                            .build());
                    //是否升级
                    BallPlayer byId = basePlayerService.findById(player.getId());
                    messageQueueService.putMessage(MessageQueueDTO.builder()
                            .type(MessageQueueDTO.TYPE_LOG_RECHARGE_UP)
                            .data(JsonUtil.toJson(player))
                            .build());
                    //充值返佣
//                    messageQueueService.putMessage(MessageQueueDTO.builder()
//                            .type(MessageQueueDTO.TYPE_LOG_RECHARGE)
//                            .data(JsonUtil.toJson(balanceChange))
//                            .build());
//                    if(rechargeHander.isFirst()){
//                        //首充是否触发上级奖金
//                        messageQueueService.putMessage(MessageQueueDTO.builder()
//                                .type(MessageQueueDTO.TYPE_RECHARGE_PARENT_BONUS)
//                                .data(JsonUtil.toJson(balanceChange))
//                                .build());
//                    }
                }
                return BaseResponse.successWithData(basePlayerService.findById(requestParam.getId()));
            }else{
                player = basePlayerService.findById(requestParam.getId());
//                rechargeHander(player,requestParam,realMoney,systemConfig);
            }
        }
    }

//    private RechargeHanderDTO rechargeHander(BallPlayer player,BallPlayer requestParame,long realMoney,BallSystemConfig systemConfig){
//        BallPlayer edit = BallPlayer.builder()
//                .version(player.getVersion())
//                .balance(player.getBalance()+realMoney)
//                .build();
//        edit.setId(requestParame.getId());
//        if(requestParame.getQrmult()==null||requestParame.getQrmult()==1){
//            if(systemConfig.getRechargeCodeConversionRate()!=null&&systemConfig.getRechargeCodeConversionRate()>0){
//                //累计打码量,查询配置,是否需要*比例
//                Double mul = BigDecimalUtil.mul(realMoney, BigDecimalUtil.div(systemConfig.getRechargeCodeConversionRate(), 100));
//                edit.setCumulativeQr((player.getCumulativeQr()==null?0:player.getCumulativeQr())+mul.intValue());
//            }else{
//                edit.setCumulativeQr((player.getCumulativeQr()==null?0:player.getCumulativeQr())+realMoney);
//            }
//        }else{
//            edit.setCumulativeQr((player.getCumulativeQr()==null?0:player.getCumulativeQr())+realMoney*requestParame.getQrmult());
//        }
//        //人工加款+累计充值
//        boolean isFirst = false;
//        Long discountQuota = 0L;
//        if(realMoney>0){
//            edit.setArtificialAdd(player.getArtificialAdd()==null?realMoney:player.getArtificialAdd()+realMoney);
//            edit.setCumulativeTopUp((player.getCumulativeTopUp() == null ? 0 : player.getCumulativeTopUp()) + realMoney);
//            //判定首充之类的
//            if (player.getFirstTopUp() == null || player.getFirstTopUp() == 0) {
//                //首充
//                edit.setFirstTopUp(realMoney);
//                edit.setFirstTopUpTime(TimeUtil.getNowTimeMill());
//                isFirst = true;
//            }
//            if (player.getMaxTopUp() == null || player.getMaxTopUp() == 0) {
//                // 最大充值金额
//                edit.setMaxTopUp(realMoney);
//            } else if (player.getMaxTopUp() < realMoney) {
//                // 本次比上次大
//                edit.setMaxTopUp(realMoney);
//            }
//            //次数+1
//            edit.setTopUpTimes(player.getTopUpTimes()==null?1:player.getTopUpTimes()+1);
//            //查询是否有充值优惠
//            BallDepositPolicy discount = depositPolicyService.findDiscount(realMoney,isFirst);
//            //充值优惠
//            discountQuota = rechargeDiscount(realMoney, edit, discountQuota, discount);
//        }
//        return RechargeHanderDTO.builder()
//                .edit(edit)
//                .discountQuota(discountQuota)
//                .first(isFirst)
//                .build();
//    }

    public static void rechargeDiscount(long realMoney, RechargeRebateDto discountQuota, BallPaymentManagement paymentManagement) {
        long discount = 0L;
        //优惠计算 金额*百分比
        Double rate = Double.valueOf(discountQuota.getRate());
        if(discountQuota.getRate()!=null&&rate>0){
            double div = BigDecimalUtil.div(rate, 100);
            Double disc = BigDecimalUtil.mul(realMoney, div);
            discount = disc.longValue();
        }
        if(!StringUtils.isBlank(discountQuota.getFixed())){
            //*汇率
            Double fixedd = BigDecimalUtil.mul(Double.parseDouble(discountQuota.getFixed()),Double.valueOf(paymentManagement.getRate()));
            Double res = BigDecimalUtil.mul(fixedd,BigDecimalUtil.PLAYER_MONEY_UNIT);
            discount+=res.longValue();
        }
        discountQuota.setDiscount(discount);
    }

    @Override
    public Boolean editCaptchaPass(BallPlayer ballPlayer) {
        BallPlayer byId = basePlayerService.findById(ballPlayer.getId());
        BallPlayer edit = BallPlayer.builder()
                .needQr(ballPlayer.getNeedQr()*BigDecimalUtil.PLAYER_MONEY_UNIT)
                .build();
        edit.setId(ballPlayer.getId());
        boolean b = basePlayerService.editAndClearCache(edit, byId);
        return b;
    }

    @Override
    public Boolean editLevel(BallPlayer ballPlayer) {
//        BallVip vip = vipService.findByLevel(ballPlayer.getVipLevel());
        BallPlayer byId = basePlayerService.findById(ballPlayer.getId());
        BallPlayer edit = BallPlayer.builder()
                .vipLevel(ballPlayer.getVipLevel())
                .build();
        edit.setId(ballPlayer.getId());
        boolean b = basePlayerService.editAndClearCache(edit, byId);
        return b;
    }

    @Override
    public BaseResponse info(BallPlayer ballPlayer) {
        return BaseResponse.successWithData(basePlayerService.findById(ballPlayer.getId()));
    }

    @Override
    public SearchResponse<BallPlayer> searchFinance(BallPlayer paramQuery, Integer pageNo, Integer pageSize) {
        SearchResponse<BallPlayer> response = new SearchResponse<>();

        Page<BallPlayer> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallPlayer> query = new QueryWrapper<>();

//        query.select("id","username","cumulative_reflect","reflect_times","first_reflect",
//                "cumulative_winning","accumulative_bet","cumulative_back_water",
//                "cumulative_qr","promote_income","need_qr");
        if(paramQuery.getUserId()!=null){
            query.eq("user_id",paramQuery.getUserId());
        }
        if(!StringUtils.isBlank(paramQuery.getUsername())){
            query.eq("username",paramQuery.getUsername());
        }
        if (paramQuery.getTimeType() != null) {
            switch (paramQuery.getTimeType()) {
                case 0:
                    query.ge("first_top_up_time", TimeUtil.getDayBegin().getTime());
                    query.le("first_top_up_time", TimeUtil.getDayEnd().getTime());
                    break;
                case 1:
                    query.ge("first_top_up_time", TimeUtil.getBeginDayOfYesterday().getTime());
                    query.le("first_top_up_time", TimeUtil.getEndDayOfYesterday().getTime());
                    break;
                case 2:
                    query.ge("first_top_up_time", TimeUtil.getDayBegin().getTime() - 3 * TimeUtil.TIME_ONE_DAY);
                    query.le("first_top_up_time", TimeUtil.getDayEnd().getTime());
                    break;
                case 3:
                    query.ge("first_top_up_time", TimeUtil.getBeginDayOfWeek().getTime());
                    query.le("first_top_up_time", TimeUtil.getDayEnd().getTime());
                    break;
                case 4:
                    query.ge("first_top_up_time", TimeUtil.getBeginDayOfLastWeek().getTime());
                    query.le("first_top_up_time", TimeUtil.getEndDayOfLastWeek().getTime());
                    break;
                case 5:
                    query.ge("first_top_up_time", TimeUtil.getBeginDayOfMonth().getTime());
                    query.le("first_top_up_time", TimeUtil.getDayEnd().getTime());
                    break;
                case 6:
                    query.ge("first_top_up_time", TimeUtil.getBeginDayOfLastMonth().getTime());
                    query.le("first_top_up_time", TimeUtil.getEndDayOfLastMonth().getTime());
                    break;
                case 7:
                    if(!StringUtils.isBlank(paramQuery.getBegin())){
                        try {
                            long timeStamp = TimeUtil.stringToTimeStamp(paramQuery.getBegin(), TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
                            query.ge("first_top_up_time", timeStamp);
                        } catch (ParseException e) {
                            try {
                                long timeStamp = TimeUtil.stringToTimeStamp(paramQuery.getBegin(), TimeUtil.TIME_YYYY_MM_DD);
                                query.ge("first_top_up_time", timeStamp);
                            } catch (ParseException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                    if(!StringUtils.isBlank(paramQuery.getEnd())){
                        try {
                            long timeStamp = TimeUtil.stringToTimeStamp(paramQuery.getEnd(), TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
                            query.le("first_top_up_time", timeStamp);
                        } catch (ParseException e) {
                            try {
                                long timeStamp = TimeUtil.stringToTimeStamp(paramQuery.getEnd(), TimeUtil.TIME_YYYY_MM_DD);
                                query.le("first_top_up_time", timeStamp+TimeUtil.TIME_ONE_DAY);
                            } catch (ParseException ex) {
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        IPage<BallPlayer> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());

        return response;
    }

    @Override
    public BallPlayer statisTotal() {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select("sum(balance) as balance,count(id) as id,sum(accumulative_bet) as accumulative_bet,sum(cumulative_winning) as cumulative_winning,sum(cumulative_discount) as cumulative_discount");
        queryWrapper.eq("account_type",2);
        List<BallPlayer> list = list(queryWrapper);
        return list.get(0);
    }

    @Override
    public BallPlayer statisTotal(ReportDataRequest reportDataRequest) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select("count(id) as id," +
                "count(case when account_type!=1 then 1 end) superior_id," +
                "count(case when the_new_login_time!=0 then 1 end) the_new_login_time");
        queryWrapper.eq("account_type",2);
        //时间条件
        queryByTime(queryWrapper,reportDataRequest.getTime(),reportDataRequest.getBegin(),reportDataRequest.getEnd());
        BallPlayer player=null;
        if(reportDataRequest.getUserId()!=null){
            player = basePlayerService.findByUserId(reportDataRequest.getUserId());
        }else if(!StringUtils.isBlank(reportDataRequest.getUsername())){
            player = basePlayerService.findByUsername(reportDataRequest.getUsername());
        }
        if(player!=null){
            queryWrapper.likeRight("super_tree",player.getSuperTree()+player.getId()+"\\_");
        }
        List<BallPlayer> list = list(queryWrapper);
        BallPlayer statisPlayer = list.get(0);
        QueryWrapper countTestPlayer = new QueryWrapper();
        countTestPlayer.eq("account_type",1);
        queryByTime(countTestPlayer,reportDataRequest.getTime(),reportDataRequest.getBegin(),reportDataRequest.getEnd());
        int count = count(countTestPlayer);
        statisPlayer.setAccountType(count);
        statisPlayer.setId(statisPlayer.getId()+count);

        //统计首充人数                 "count(case when first_top_up!=0 then 1 end) first_top_up," +
        QueryWrapper firstRecharge = new QueryWrapper();
        firstRecharge.eq("account_type",2);
        firstRecharge.gt("first_top_up",0);
        queryByTime(firstRecharge,"first_top_up_time",reportDataRequest.getTime(),reportDataRequest.getBegin(),reportDataRequest.getEnd());
        count = count(firstRecharge);
        statisPlayer.setFirstTopUp((long)count);
        return statisPlayer;
    }

    @Override
    public BaseResponse getPlayerCards(BallLoggerWithdrawal loggerWithdrawal) {
        Map<String,Object> data = new HashMap<>();
        List<BallBankCard> bank = new ArrayList<>();
        BallBankCard byPlayerId = bankCardService.findByPlayerId(loggerWithdrawal.getPlayerId());
        if(byPlayerId!=null){
            //查询银行属于是什么类型
            BallBank ballBank = BallBank.builder()
                    .build();
            List<BallBank> byName = bankService.findByName(byPlayerId.getBankName());
            if(!byName.isEmpty()){
                ballBank = byName.get(0);
                byPlayerId.setBackEncoding(ballBank.getBankCode());
                bank.add(byPlayerId);
            }
//            if(ballBank!=null&&ballBank.getId()!=null){
//                BallBankArea byAreaId = bankService.findByAreaId(ballBank.getAreaId());
//                //1 印度  2加纳
//                byPlayerId.setAreaType(byAreaId.getCode());
//                if(byAreaId.getCode()==2){
//                    byPlayerId.setBackEncoding(ballBank.getBankCode());
//                }
//                bank.add(byPlayerId);
//                data.put("bankType",byAreaId.getCode());
//            }else{
//                //只有印度可能没有指定银行
//                data.put("bankType",1);
//            }
        }
        data.put("bank",bank);
        BallVirtualCurrency byId = virtualCurrencyService.findById(loggerWithdrawal.getUsdtId());
        List<BallVirtualCurrency> virtual = new ArrayList<>();
        if(byId!=null){
            virtual.add(byId);
        }
        data.put("virtual",virtual);

        BallSimCurrency byPlayerId1 = simCurrencyService.findByPlayerId(loggerWithdrawal.getPlayerId());
        List<BallSimCurrency> sims = new ArrayList<>();
        if(byPlayerId1!=null){
            sims.add(byPlayerId1);
        }
        data.put("sim",sims);
        return BaseResponse.successWithData(data);
    }

    @Override
    public BaseResponse insertMult(BallPlayer ballPlayer) {
        List<BallPlayer> players = new ArrayList<>();
        for(int i=1;i<=ballPlayer.getVipRank();i++){
            String invitationCode = "";
            if(ballPlayer.getAccountType()==2){
                while (true){
                    invitationCode = String.valueOf(TimeUtil.getRandomNum(1234567, 9876543));
                    BallPlayer byInvitationCode = basePlayerService.findByInvitationCode(invitationCode);
                    if(byInvitationCode==null){
                        break;
                    }
                }
            }
            String pwd = UUIDUtil.getUUID().substring(6,12);
            BallPlayer build = BallPlayer.builder()
                    .username(ballPlayer.getUsername() + i)
                    .password(PasswordUtil.genPasswordMd5(pwd))
                    .passwordTest(pwd)
                    .accountType(ballPlayer.getAccountType())
                    .version(1L)
                    .invitationCode(invitationCode)
                    .userId(basePlayerService.createUserId())
                    .status(1)
                    .theNewIp("--")
                    .statusOnline(0)
                    .balance(0L)
                    .vipLevel(0)
                    .superiorId(-1L)
                    .superTree("0")
                    .vipRank(1)
                    .build();
            MapUtil.setCreateTime(build);
            try {
                save(build);
                if(build.getId()!=null){
                    players.add(build);
                }
            }catch (Exception e){}
        }
        return BaseResponse.successWithData(players);
    }

    @Override
    public Integer statisTotalRegist(Long id, Long begin, Long end) {
        QueryWrapper<BallPlayer> query = new QueryWrapper();
        query.select("id");
        query.eq("account_type",2);
        query.likeRight("super_tree","\\_"+id+"\\_");
        query.between("created_at",begin,end);
        Integer integer = baseMapper.selectCount(query);
        return  integer;
    }

    @Override
    public Integer statisFirstPayCount(Long id, Long begin, Long end) {
        QueryWrapper<BallPlayer> query = new QueryWrapper<>();
        query.eq("account_type",2);
        query.likeRight("super_tree","\\_"+id+"\\_");
        query.between("first_top_up_time",begin,end);
        query.select("count(id) as id");
        BallPlayer one = getOne(query);
        return one.getId().intValue();
    }

    @Override
    public Long statisFrozen() {
        //提现冻结
        long withdrawal = loggerWithdrawalService.statisTotalNot();
        //下注冻结
        return withdrawal;
    }

    @Override
    public List<BallPlayer> searchProxy(BallProxyLogger queryParam, BallPlayer proxyUser) {
        QueryWrapper<BallPlayer> query = new QueryWrapper();
        //层级不为空且不查代理线，只查指定层级的
//        if(queryParam.getLevel()!=null && queryParam.getProxyLine()==0){
//            query.lt("vip_rank",queryParam.getLevel()+2);
//        }
//        if(!StringUtils.isBlank(queryParam.getBegin())){
//            try {
//                query.gt("created_at",TimeUtil.stringToTimeStamp(queryParam.getBegin(),TimeUtil.TIME_YYYY_MM_DD));
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//        }
//        if(!StringUtils.isBlank(queryParam.getEnd())){
//            try {
//                query.lt("created_at",TimeUtil.stringToTimeStamp(queryParam.getEnd(),TimeUtil.TIME_YYYY_MM_DD)+TimeUtil.TIME_ONE_DAY);
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//        }
        //所有下级,代理线为指定
        query.likeRight("super_tree",proxyUser.getSuperTree()+proxyUser.getId()+"\\_");
        int pageNo = 1;
        List<BallPlayer> list = new ArrayList<>();
        while (true){
            IPage<BallPlayer> page = page(new Page<>(pageNo++, 1000), query);
            if(page.getRecords().isEmpty()){
                break;
            }
            list.addAll(page.getRecords());
        }
        return list;
    }

    @Override
    public SearchResponse<BallPlayer> queryActivePlayer(QueryActivePlayerRequest request, BallPlayer player) {
        Map<String,Object> queryParam = new HashMap<>();
        if(!StringUtils.isBlank(request.getRbegin())){
            try {
                long timeStamp = TimeUtil.stringToTimeStamp(request.getRbegin(), TimeUtil.TIME_YYYY_MM_DD);
                queryParam.put("begin",timeStamp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if(!StringUtils.isBlank(request.getRend())){
            try {
                long timeStamp = TimeUtil.stringToTimeStamp(request.getRend(), TimeUtil.TIME_YYYY_MM_DD);
                queryParam.put("end",timeStamp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if(request.getLevel()!=null){
            //查层级 ,相对于当前账号
            int begin = player.getVipRank()+1;
            int end = player.getVipRank()+request.getLevel();
            queryParam.put("beginRank",begin);
            queryParam.put("endRank",end);
        }
        queryParam.put("superTree",player.getSuperTree()+player.getId()+"\\_%");
        Page<BallPlayer> page = new Page<>(request.getPageNo(), request.getPageSize());
        IPage<BallPlayer> pages = ballPlayerMapper.listPage(page,queryParam);
        SearchResponse<BallPlayer> response = new SearchResponse<>();
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public SearchResponse<BallPlayer> queryActivePlayerByBet(QueryActivePlayerRequest request, BallPlayer player) {
        Map<String,Object> queryParam = new HashMap<>();
        if(!StringUtils.isBlank(request.getRbegin())){
            try {
                long timeStamp = TimeUtil.stringToTimeStamp(request.getRbegin(), TimeUtil.TIME_YYYY_MM_DD);
                queryParam.put("begin",timeStamp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if(!StringUtils.isBlank(request.getRend())){
            try {
                long timeStamp = TimeUtil.stringToTimeStamp(request.getRend(), TimeUtil.TIME_YYYY_MM_DD);
                queryParam.put("end",timeStamp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if(request.getLevel()!=null){
            //查层级 ,相对于当前账号
            int begin = player.getVipRank()+1;
            int end = player.getVipRank()+request.getLevel();
            queryParam.put("beginRank",begin);
            queryParam.put("endRank",end);
        }
        queryParam.put("superTree",player.getSuperTree()+player.getId()+"\\_%");
        //次数
        if(request.getBetCount()!=null){
            queryParam.put("betCount",request.getBetCount());
        }
        Page<BallPlayer> page = new Page<>(request.getPageNo(), request.getPageSize());
        IPage<BallPlayer> pages = ballPlayerMapper.listPageBet(page,queryParam);
        SearchResponse<BallPlayer> response = new SearchResponse<>();
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public List<BallPlayer> queryActivePlayerAll(QueryActivePlayerRequest request, BallPlayer player) {
        //1.查充值 2.查投注  3查提现
        QueryWrapper query = new QueryWrapper();
        if(!StringUtils.isBlank(request.getRegbegin())){
            try {
                long timeStamp = TimeUtil.stringToTimeStamp(request.getRegbegin(), TimeUtil.TIME_YYYY_MM_DD);
                query.gt("created_at",timeStamp);
            } catch (ParseException e) {
            }
        }
        if(!StringUtils.isBlank(request.getRegend())){
            try {
                long timeStamp = TimeUtil.stringToTimeStamp(request.getRegend(), TimeUtil.TIME_YYYY_MM_DD);
                query.lt("created_at",timeStamp+TimeUtil.TIME_ONE_DAY);
            } catch (ParseException e) {
            }
        }
        //1.先查下级
        if(request.getLevel()!=null){
            //查层级 ,相对于当前账号
            int begin = player.getVipRank()+1;
            int end = player.getVipRank()+request.getLevel();
            query.between("vip_rank",begin,end);
        }
        query.likeRight("super_tree",player.getSuperTree()+player.getId()+"\\_");
        int pageNo = 1;
        Map<Long,BallPlayer> dataMap = new ConcurrentHashMap<>();
        while (true){
            IPage page = new Page(pageNo++,1000);
            IPage<BallPlayer> pages = page(page, query);
            List<BallPlayer> records = pages.getRecords();
            if(records==null||records.isEmpty()||records.get(0)==null){
                break;
            }
            for(BallPlayer item:records){
                //提现
                item.setCumulativeReflect(0L);
                //充值
                item.setCumulativeTopUp(0L);
                //投注次数
                item.setAccumulativeBet(0L);
                item.setBcount(0);
                //投注天数
                item.setBetDays(new HashSet<>());
                item.setVipRank(item.getVipRank()-player.getVipRank());
                dataMap.put(item.getId(),item);
            }
        }
        //1.查充值

        final CountDownLatch countDownLatch = new CountDownLatch(3);
        ThreadPoolUtil.execSaki(() -> {
            //统计下级充值
            loggerRechargeService.search(request,player,dataMap);
            countDownLatch.countDown();
        });
        ThreadPoolUtil.execSaki(() -> {
            //统计下级提现+人工
            loggerWithdrawalService.search(request,player,dataMap);
            countDownLatch.countDown();
        });
        ThreadPoolUtil.execSaki(() -> {
            //统计下级投注
            ballBetService.search(request,player,dataMap);
            countDownLatch.countDown();
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
        }
        List<BallPlayer> responseList = new ArrayList<>();
        for(BallPlayer item:dataMap.values()){
            //充值过滤
            if(request.getRecharge()==1){
                if(item.getCumulativeTopUp()==0){
                    continue;
                }
            }
            //投注过滤
            if(request.getBet()==1){
                if(item.getBcount()==0){
                    continue;
                }
                if(request.getBetCount()!=null&&request.getBetCount()>item.getBcount()){
                    continue;
                }
            }
            responseList.add(item);
        }
        return responseList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse repairRecharge(PlayerRepairRecharge repairRecharge, BallAdmin currentUser) throws SQLException, JsonProcessingException {
        //增加账变
        BallPlayer player = basePlayerService.findById(repairRecharge.getPlayerId());
        if( player==null ){
            return BaseResponse.failedWithMsg("player not found~");
        }
        BallPaymentManagement paymentManagement = paymentManagementService.findById(repairRecharge.getPayId());
        if(paymentManagement==null){
            return BaseResponse.failedWithMsg("payment not found~");
        }
        //增加充值记录
        Double moneySysd = BigDecimalUtil.mul(Double.valueOf(repairRecharge.getMoney())
                ,BigDecimalUtil.PLAYER_MONEY_UNIT);
        long money = moneySysd.longValue();
        //汇率后金额
        Double moneyd = BigDecimalUtil.mul(money, Double.parseDouble(paymentManagement.getRate()));
        long moneySys = moneyd.longValue();
        BallLoggerRecharge save = BallLoggerRecharge.builder()
                .playerId(player.getId())
                .accountType(player.getAccountType())
                .username(player.getUsername())
                .userId(player.getUserId())
                .superTree(player.getSuperTree())
                .moneySys(moneySys)
                .payName(paymentManagement.getName())
                .type(paymentManagement.getPayTypeOnff())
                .payId(paymentManagement.getId())
                .status(2)
                .money(money)
                .moneyReal(money)
                .moneyDiscount(0L)
                .orderNo(Long.parseLong(TimeUtil.dateFormat(new Date(), TimeUtil.TIME_TAG_MM_DD_HH_MM_SS)) + loggerRechargeService.getDayOrderNo())
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .operUser(currentUser.getUsername())
                .remark(repairRecharge.getRemark())
                .build();
        String superTree = player.getSuperTree();
        if(superTree.equals("0")){
        }else if(superTree.equals("_")){
        }else {
            String[] split = superTree.split("_");
            BallPlayer superPlayer = basePlayerService.findById(Long.parseLong(split[1]));
            save.setTopUsername(superPlayer.getUsername());
            if (split.length == 2) {
                save.setFirstUsername(superPlayer.getUsername());
            } else if (split.length > 2) {
                BallPlayer firstPlayer = basePlayerService.findById(Long.parseLong(split[2]));
                save.setFirstUsername(firstPlayer.getUsername());
            }
        }
        BallLoggerRecharge insert = loggerRechargeService.insert(save);
        if(insert.getId()>0){
            insert.setMoneyParam(repairRecharge.getMoney());
            loggerRechargeService.editRe(insert);
        }
//        while (true){
//            BallPlayer edit = BallPlayer.builder()
//                    .version(player.getVersion())
//                    .balance(player.getBalance()+moneySys)
//                    .cumulativeQr((player.getCumulativeQr()==null?0:player.getCumulativeQr())+moneySys)
//                    .cumulativeTopUp((player.getCumulativeTopUp() == null ? 0 : player.getCumulativeTopUp()) + moneySys)
//                    .topUpTimes((player.getTopUpTimes()==null?0:player.getTopUpTimes())+1)
//                    .build();
//            edit.setId(player.getId());
//            if (player.getMaxTopUp() == null || player.getMaxTopUp() == 0) {
//                // 最大充值金额
//                edit.setMaxTopUp(moneySys);
//            } else if (player.getMaxTopUp() < moneySys) {
//                // 本次比上次大
//                edit.setMaxTopUp(moneySys);
//            }
//            boolean b = basePlayerService.editAndClearCache(edit, player);
//            if(b){
//                BallLoggerRecharge save = BallLoggerRecharge.builder()
//                        .playerId(player.getId())
//                        .accountType(player.getAccountType())
//                        .username(player.getUsername())
//                        .userId(player.getUserId())
//                        .superTree(player.getSuperTree())
//                        .moneySys(moneySys)
//                        .payName(paymentManagement.getName())
//                        .type(paymentManagement.getPayTypeOnff())
//                        .status(3)
//                        .money(money)
//                        .moneyReal(money)
//                        .moneyDiscount(0L)
//                        .orderNo(Long.parseLong(TimeUtil.dateFormat(new Date(), TimeUtil.TIME_TAG_MM_DD_HH_MM_SS)) + loggerRechargeService.getDayOrderNo())
//                        .createdAt(System.currentTimeMillis())
//                        .updatedAt(System.currentTimeMillis())
//                        .operUser(currentUser.getUsername())
//                        .remark(repairRecharge.getRemark())
//                        .build();
//                String superTree = player.getSuperTree();
//                if(superTree.equals("0")){
//                }else if(superTree.equals("_")){
//                }else {
//                    String[] split = superTree.split("_");
//                    BallPlayer superPlayer = basePlayerService.findById(Long.parseLong(split[1]));
//                    save.setTopUsername(superPlayer.getUsername());
//                    if (split.length == 2) {
//                        save.setFirstUsername(superPlayer.getUsername());
//                    } else if (split.length > 2) {
//                        BallPlayer firstPlayer = basePlayerService.findById(Long.parseLong(split[2]));
//                        save.setFirstUsername(firstPlayer.getUsername());
//                    }
//                }
//                BallLoggerRecharge insert = loggerRechargeService.insert(save);
//                if(insert.getId()>0){
//                    //账变
//                    BallBalanceChange change = BallBalanceChange.builder()
//                            .orderNo(save.getOrderNo())
//                            .username(player.getUsername())
//                            .playerId(player.getId())
//                            .accountType(player.getAccountType())
//                            .userId(player.getUserId())
//                            .superTree(player.getSuperTree())
//                            .parentId(player.getSuperiorId())
//                            .changeMoney(moneySys)
//                            .initMoney(player.getBalance())
//                            .dnedMoney(player.getBalance()+moneySys)
//                            .balanceChangeType(save.getType()==1?1:11)
//                            .frozenStatus(1)
//                            .createdAt(System.currentTimeMillis())
//                            .build();
//                    boolean insert1 = ballBalanceChangeService.insert(change);
//                    if(insert1){
//                        break;
//                    }else{
//                        throw new SQLException();
//                    }
//                }else{
//                    throw new SQLException();
//                }
//            }else{
//                player = basePlayerService.findById(repairRecharge.getPlayerId());
//            }
//        }
        return BaseResponse.successWithData(MapUtil.newMap("sucmsg","e65"));
    }

    @Override
    public BaseResponse repairWithdrawal(PlayerRepairWithdrawal repairWithdrawal, BallAdmin currentUser) throws SQLException {
        //增加账变
        BallPlayer player = basePlayerService.findById(repairWithdrawal.getPlayerId());
        if( player==null ){
            return BaseResponse.failedWithMsg("player not found~");
        }
        BallWithdrawManagement withdrawManagement = withdrawManagementService.findById(repairWithdrawal.getWiId());
        if(withdrawManagement==null){
            return BaseResponse.failedWithMsg("withdraw not found~");
        }
        if(repairWithdrawal.getBehalfId()==null){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e34"));
        }
        BallPayBehalf payBehalf = payBehalfService.findById(repairWithdrawal.getBehalfId());
        if(payBehalf==null){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e35"));
        }
        //增加提现记录
        Double moneySysd = BigDecimalUtil.mul(Double.valueOf(repairWithdrawal.getMoney()),BigDecimalUtil.PLAYER_MONEY_UNIT);
        long moneySys = moneySysd.longValue();
        //汇率后金额
        Double moneyd = BigDecimalUtil.div(moneySys, Double.parseDouble(withdrawManagement.getRate()));
        long money = moneyd.longValue();
        while (true){
            if(player.getBalance()-moneySys<0){
                return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                        ResponseMessageUtil.responseMessage("", "e57"));
            }
            BallPlayer edit = BallPlayer.builder()
                    .version(player.getVersion())
                    .balance(player.getBalance()-moneySys)
                    .cumulativeReflect((player.getCumulativeReflect() == null ? 0 : player.getCumulativeReflect()) + moneySys)
                    .reflectTimes((player.getReflectTimes()==null?0:player.getReflectTimes())+1)
                    .build();
            edit.setId(player.getId());
            if (player.getMaxReflect() == null || player.getMaxReflect() == 0) {
                // 最大充值金额
                edit.setMaxReflect(moneySys);
            } else if (player.getMaxReflect() < moneySys) {
                // 本次比上次大
                edit.setMaxReflect(moneySys);
            }
            boolean b = basePlayerService.editAndClearCache(edit, player);
            if(b){
                Double rate = Double.valueOf(withdrawManagement.getRate())*BigDecimalUtil.PLAYER_MONEY_UNIT;
                BallLoggerWithdrawal save = BallLoggerWithdrawal.builder()
                        .playerId(player.getId())
                        .accountType(player.getAccountType())
                        .playerName(player.getUsername())
                        .superTree(player.getSuperTree())
                        .status(4)
                        //1银行 2 usdt
                       .type(withdrawManagement.getType())
                        .money(moneySys)
                        .orderNo(Long.parseLong(TimeUtil.dateFormat(new Date(), TimeUtil.TIME_TAG_MM_DD_HH_MM_SS)) + loggerWithdrawalService.getDayOrderNo())
                        .createdAt(System.currentTimeMillis())
                        .updatedAt(System.currentTimeMillis())
                        .rate(0)
                        .usdtRate(rate.toString())
                        .usdtMoney(money)
                        .checker(currentUser.getUsername())
                        .oker(currentUser.getUsername())
                        .commission(0L)
                        .remark(repairWithdrawal.getRemark())
                        .behalfTime(System.currentTimeMillis())
                        .behalfId(payBehalf.getId())
                        .build();
                if(withdrawManagement.getType()==2){
                    BallVirtualCurrency virtualCurrency = virtualCurrencyService.findById(repairWithdrawal.getUsdtId());
                    save.setUsdtId(virtualCurrency.getId());
                }
                String superTree = player.getSuperTree();
                if(superTree.equals("0")){
                }else if(superTree.equals("_")){
                }else {
                    String[] split = superTree.split("_");
                    BallPlayer superPlayer = basePlayerService.findById(Long.parseLong(split[1]));
                    save.setTopUsername(superPlayer.getUsername());
                    if (split.length == 2) {
                        save.setFirstUsername(superPlayer.getUsername());
                    } else if (split.length > 2) {
                        BallPlayer firstPlayer = basePlayerService.findById(Long.parseLong(split[2]));
                        save.setFirstUsername(firstPlayer.getUsername());
                    }
                }
                //登录地区
                save.setIpAddr(player.getTheNewIp()+"|"+GeoLiteUtil.getIpAddr(player.getTheNewIp()));
                BallLoggerWithdrawal insert = loggerWithdrawalService.insert(save);
                if(insert.getId()>0){
                    //账变
                    BallBalanceChange change = BallBalanceChange.builder()
                            .orderNo(save.getOrderNo())
                            .username(player.getUsername())
                            .playerId(player.getId())
                            .accountType(player.getAccountType())
                            .userId(player.getUserId())
                            .superTree(player.getSuperTree())
                            .parentId(player.getSuperiorId())
                            .changeMoney(moneySys)
                            .initMoney(player.getBalance())
                            .dnedMoney(player.getBalance()-moneySys)
                            .balanceChangeType(2)
                            .frozenStatus(1)
                            .createdAt(System.currentTimeMillis())
                            .build();
                    boolean insert1 = ballBalanceChangeService.insert(change);
                    if(insert1){
                        break;
                    }else{
                        throw new SQLException();
                    }
                }else{
                    throw new SQLException();
                }
            }else{
                player = basePlayerService.findById(repairWithdrawal.getPlayerId());
            }
        }
        return BaseResponse.successWithData(MapUtil.newMap("sucmsg","e66"));
    }

    @Override
    public BaseResponse setPlayerToProxy(Long id) {
        BallPlayer player = basePlayerService.findById(id);
        BallPlayer edit = BallPlayer.builder()
                .proxyPlayer(player.getProxyPlayer()==0?1:0)
                .build();
        edit.setId(id);
        boolean b = basePlayerService.editAndClearCache(edit, player);
        return BaseResponse.successWithData(MapUtil.newMap("sucmsg","e58"));
    }

    @Override
    public List<BallPlayer> findProxys() {
        QueryWrapper query = new QueryWrapper();
        query.eq("proxy_player",1);
        return list(query);
    }

    @Override
    public List<BallPlayer> findProxys(BallProxyLogger queryParam) {
        QueryWrapper query = new QueryWrapper();
        query.eq("proxy_player",1);
        if(!StringUtils.isBlank(queryParam.getPlayerName())){
            query.eq("username",queryParam.getPlayerName());
        }
        return list(query);
    }

    @Override
    public List<BallPlayer> findSubThree(ReportStandardRequest reportStandardRequest,BallPlayer player) {
        QueryWrapper query = new QueryWrapper();
        query.likeRight("super_tree",player.getSuperTree()+player.getId()+"\\_");
        List<BallPlayer> players = new ArrayList<>();
        int pageNo = 1;
        while (true){
            IPage<BallPlayer> pages = page(new Page<>(pageNo++,500), query);
            List<BallPlayer> records = pages.getRecords();
            if(records==null||records.isEmpty()||records.get(0)==null){
                break;
            }
            players.addAll(records);
        }
        return players;
    }

    @Override
    public void statisFirst(BallProxyLogger queryParam, ProxyStatis3Dto total, ProxyStatis3Dto data) {
        QueryWrapper<BallPlayer> query = new QueryWrapper<>();
        if(!StringUtils.isEmpty(queryParam.getBegin())){
            try {
                query.gt("first_top_up_time",TimeUtil.stringToTimeStamp(queryParam.getBegin(),TimeUtil.TIME_YYYY_MM_DD));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if(!StringUtils.isEmpty(queryParam.getEnd())){
            try {
                query.lt("first_top_up_time",TimeUtil.stringToTimeStamp(queryParam.getEnd(),TimeUtil.TIME_YYYY_MM_DD)+TimeUtil.TIME_ONE_DAY);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
//        query.gt("first_top_up_time",0);
        query.likeRight("super_tree",queryParam.getPlayerName()+queryParam.getPlayerId()+"\\_");
        query.select("count(id) id");

        BallPlayer one = getOne(query);
        data.setRechargeFirst(one.getId()==null?0:one.getId().intValue());
        total.setRechargeFirst(total.getRechargeFirst()+data.getRechargeFirst());
    }

    @Override
    public void statisSubs(BallProxyLogger queryParam, ProxyStatis3Dto total, ProxyStatis3Dto data) {
        QueryWrapper<BallPlayer> query = new QueryWrapper<>();
        query.eq("super_tree",queryParam.getPlayerName()+queryParam.getPlayerId()+"_");
        if(!StringUtils.isEmpty(queryParam.getBegin())){
            try {
                query.gt("created_at",TimeUtil.stringToTimeStamp(queryParam.getBegin(),TimeUtil.TIME_YYYY_MM_DD));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if(!StringUtils.isEmpty(queryParam.getEnd())){
            try {
                query.lt("created_at",TimeUtil.stringToTimeStamp(queryParam.getEnd(),TimeUtil.TIME_YYYY_MM_DD)+TimeUtil.TIME_ONE_DAY);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        query.select("count(id) id");
        BallPlayer one = getOne(query);
        data.setSubCount(one.getId()==null?0:one.getId().intValue());
        total.setSubCount(total.getSubCount()+data.getSubCount());

        query = new QueryWrapper<>();
        //全部下级
        if(!StringUtils.isEmpty(queryParam.getBegin())){
            try {
                query.gt("created_at",TimeUtil.stringToTimeStamp(queryParam.getBegin(),TimeUtil.TIME_YYYY_MM_DD));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if(!StringUtils.isEmpty(queryParam.getEnd())){
            try {
                query.lt("created_at",TimeUtil.stringToTimeStamp(queryParam.getEnd(),TimeUtil.TIME_YYYY_MM_DD)+TimeUtil.TIME_ONE_DAY);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        query.select("count(id) id");
        query.likeRight("super_tree",queryParam.getPlayerName()+queryParam.getPlayerId()+"\\_");
        BallPlayer one1 = getOne(query);
        data.setSubCountAll(one1.getId()==null?0:one1.getId().intValue());
        total.setSubCountAll(total.getSubCountAll()+data.getSubCountAll());
    }

    @Override
    public void statis(BallProxyLogger queryParam, ProxyStatis3Dto total, ProxyStatis3Dto data, BallSystemConfig systemConfig) {
        QueryWrapper<BallPlayer> query = new QueryWrapper<>();
        query.likeRight("super_tree",queryParam.getPlayerName()+queryParam.getPlayerId()+"\\_");
        query.select("sum(cumulative_reflect) as cumulative_reflect,sum(cumulative_top_up) as cumulative_top_up");
        BallPlayer one1 = getOne(query);
        data.setRechargeTotal(one1.getCumulativeTopUp());
        data.setWithdrawalTotal(one1.getCumulativeReflect());
        total.setRechargeTotal(total.getRechargeTotal()+data.getRechargeTotal());
        total.setWithdrawalTotal(total.getWithdrawalTotal()+data.getWithdrawalTotal());
    }

    @Override
    public void dayReward() {
        int pageNo = 1;
        int pageSize = 500;
        List<BallVip> byAll = vipService.findByAll();
        Map<Integer,Double> vipMap = new HashMap<>();
        for(BallVip item:byAll){
            try {
                double dayReward = Double.parseDouble(item.getDayReward());
                if(dayReward>0){
                    vipMap.put(item.getLevel(),dayReward);
                }
            }catch (Exception ex){
            }
        }
        while (true){
            SearchResponse<BallPlayer> search = search(BallPlayer.builder()
                    .accountType(2)
                    .build(), pageNo++, pageSize);
            List<BallPlayer> results = search.getResults();
            if(results==null||results.isEmpty()||results.get(0)==null){
                break;
            }
            for(BallPlayer item:results){
                Double dayReward = vipMap.get(item.getVipLevel());
                if(dayReward==null){
                    continue;
                }
                //余额小于1
                if(item.getBalance()+item.getFrozenBet()<1){
                    continue;
                }
                ThreadPoolUtil.exec(new Runnable() {
                    @Override
                    public void run() {
                        BallPlayer player = item;
                        //计算VIP奖励
                        long balance = player.getBalance()+player.getFrozenBet();
                        Double reward = BigDecimalUtil.div(BigDecimalUtil.mul(balance,dayReward),100);
                        Long rewardLong = reward.longValue();
                        if(reward>0){
                            //发放奖励
                            while (true){
                                BallPlayer edit = BallPlayer.builder()
                                        .balance(player.getBalance() + rewardLong)
                                        .version(player.getVersion())
                                        .build();
                                edit.setId(player.getId());
                                boolean b = basePlayerService.editAndClearCache(edit, player);
                                if(b){
                                    BallBalanceChange build = BallBalanceChange.builder()
                                            .playerId(player.getId())
                                            .accountType(player.getAccountType())
                                            .userId(player.getUserId())
                                            .parentId(player.getSuperiorId())
                                            .username(player.getUsername())
                                            .superTree(player.getSuperTree())
                                            .createdAt(System.currentTimeMillis())
                                            .changeMoney(rewardLong)
                                            .initMoney(player.getBalance())
                                            .dnedMoney(player.getBalance()+rewardLong)
                                            .balanceChangeType(17)
                                            .remark("")
                                            .build();
                                    ballBalanceChangeService.insert(build);
                                    break;
                                }else{
                                    player = basePlayerService.findById(player.getId());
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public int standard(ReportStandardRequest reportStandardRequest) {
        return mapper.standard(reportStandardRequest);
    }
    @Override
    public int standardGrouop(ReportStandardRequest reportStandardRequest) {
        return mapper.standardGrouop(reportStandardRequest);
    }

    @Override
    public SearchResponse<BallPlayer> searchStandard(BallPlayer paramQuery, Integer pageNo, Integer pageSize) {
        SearchResponse<BallPlayer> response = new SearchResponse<>();

        Page<BallPlayer> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallPlayer> query = new QueryWrapper<>();
        query.eq("account_type",2);
        query.gt("group_size",0);
        //如果传入的用户ID或者用户名,然后判定查询条件
        BallPlayer playerTree = null;
        if(!StringUtils.isBlank(paramQuery.getUsername())){
            if(paramQuery.getTreeType()==null) {
                query.eq("username",paramQuery.getUsername());
            }else{
                playerTree = basePlayerService.findByUsername(paramQuery.getUsername());
            }
        }
        if(paramQuery.getTreeType()!=null){
            if(playerTree!=null){
                //要查直属下级,或者全部下级
                if(paramQuery.getTreeType()==1){
                    //查直属
                    query.eq("superior_id",playerTree.getId());
                }else if(paramQuery.getTreeType()==2){
                    //查全部下级
                    query.likeRight("super_tree",playerTree.getSuperTree()+playerTree.getId()+"\\_");
                }
            }else{
                query.eq("user_id",-100);
            }
        }
        query.orderByDesc("group_size");
        IPage<BallPlayer> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());

        return response;
    }

    @Override
    public BaseResponse sendMessageToPlayerChat(String minMax, Integer type,BallApiConfig apiConfig,boolean auto) throws IOException {
        String[] split = minMax.split("-");
        int min = 0;
        int max = 0;
        try {
            min = Integer.parseInt(split[0]);
            max = Integer.parseInt(split[1]);
        }catch (Exception e){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e36"));
        }
        if(type==null){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e37"));
        }
        //随机发送 奖励
        if(apiConfig==null){
            apiConfig = apiConfigService.getApiConfig();
        }
        if(UUIDUtil.hasZhChar(apiConfig.getFirstRecharge())
                || UUIDUtil.hasZhChar(apiConfig.getFirstRecharge2())
                ||UUIDUtil.hasZhChar(apiConfig.getFixedRecharge())
                || UUIDUtil.hasZhChar(apiConfig.getSecondRecharge())){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e38"));
        }
//        if(apiConfig.getAutoSend()==1 && !auto){
//            return BaseResponse.failedWithMsg("已配置了自动消息");
//        }
//        if(true){
//            System.out.println("发送消息："+ type);
//            return BaseResponse.SUCCESS;
//        }
        String format = null;
//        int recharge = TimeUtil.getRandomNum(min,max);
        //尾数0
        int recharge = TimeUtil.getRandomNum(min,max);
        if(recharge<10000){
            recharge = recharge/100*100;
        }else if(recharge<100000){
            recharge = recharge/1000*1000;
        } else if(recharge<1000000){
            recharge = recharge/10000*10000;
        }else{
            recharge = recharge/100000*100000;
        }
        if(recharge<5000){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e39"));
        }
        int parentRate = new int[]{3,5,10}[TimeUtil.getRandomNum(0,3)];
        switch (type){
            case 1:
            case 2:
                //上级,3 5 10
                //自己查1个吧
                SearchResponse<BallDepositPolicy> search = depositPolicyService.search(BallDepositPolicy.builder()
                        .depositPolicyType(1)
                        .build(),1,10);
                BallDepositPolicy ballDepositPolicy = search.getResults().get(TimeUtil.getRandomNum(0,search.getResults().size()));
                List<RechargeRebateDto> odds = JsonUtil.fromJsonToList(ballDepositPolicy.getRules(),RechargeRebateDto.class);
                for(RechargeRebateDto item:odds){
                    if(recharge>item.getMin()&&recharge<item.getMax()){
                        if(type==1){
                            format = MessageFormat.format(apiConfig.getFirstRecharge(),
                                    UUIDUtil.getRandomUsername(),
                                    String.valueOf(recharge),
                                    UUIDUtil.getRandomUsername(),
                                    String.valueOf(BigDecimalUtil.div(BigDecimalUtil.mul(recharge,Double.valueOf(item.getRate())),100)),
                                    String.valueOf(BigDecimalUtil.div(BigDecimalUtil.mul(recharge,parentRate),100)));
                        }else{
                            format = MessageFormat.format(apiConfig.getFirstRecharge2(),
                                    UUIDUtil.getRandomUsername(),
                                    String.valueOf(recharge),
                                    UUIDUtil.getRandomUsername(),
                                    String.valueOf(BigDecimalUtil.div(BigDecimalUtil.mul(recharge,Double.valueOf(item.getRate())),100)));
                        }
                    }
                }
                break;
            case 3:
                //自己查1个吧
                search = depositPolicyService.search(BallDepositPolicy.builder()
                        .depositPolicyType(3)
                        .build(),1,10);
                ballDepositPolicy = search.getResults().get(TimeUtil.getRandomNum(0,search.getResults().size()));
                odds = JsonUtil.fromJsonToList(ballDepositPolicy.getRules(),RechargeRebateDto.class);
                for(RechargeRebateDto item:odds) {
                    if (recharge > item.getMin() && recharge < item.getMax()) {
                        format = MessageFormat.format(apiConfig.getSecondRecharge(),
                                UUIDUtil.getRandomUsername(),
                                String.valueOf(BigDecimalUtil.div(BigDecimalUtil.mul(recharge,Double.valueOf(item.getRate())),100))
                                );
                    }
                }

                break;
            case 4:
                search = depositPolicyService.search(BallDepositPolicy.builder()
                        .depositPolicyType(4)
                        .build(),1,10);
                ballDepositPolicy = search.getResults().get(TimeUtil.getRandomNum(0,search.getResults().size()));
                odds = JsonUtil.fromJsonToList(ballDepositPolicy.getRules(),RechargeRebateDto.class);
                for(RechargeRebateDto item:odds) {
                    if (recharge > item.getMin() && recharge < item.getMax()) {
                        format = MessageFormat.format(apiConfig.getFixedRecharge(),
                                UUIDUtil.getRandomUsername(),
                                String.valueOf(BigDecimalUtil.div(BigDecimalUtil.mul(recharge,Double.valueOf(item.getRate())),100))
                        );
                    }
                }
                break;
            default:
                break;
        }
        if(!StringUtils.isEmpty(format)){
            messageQueueService.putMessage(MessageQueueDTO.builder()
                    .type(MessageQueueDTO.TYPE_PLAYER_TG_CHAT)
                    .data(format)
                    .build());
        }
        return BaseResponse.successWithData(MapUtil.newMap("sucmsg","e67"));
    }

    @Override
    public void checkPlayerMessage() {
        BallApiConfig apiConfig = apiConfigService.getApiConfig();
        if(apiConfig.getAutoSend()==1){
            //要发
            Object lpop = redisUtil.lGetIndex(RedisKeyContant.PLAYER_CHAT_MESSAGE,0);
            long curr = System.currentTimeMillis();
            if (lpop == null) {
                //没有消息了,马上创建消息队列
                int[] randomIntArr = UUIDUtil.getRandomIntArr(apiConfig.getHourPer());
                for(int item:randomIntArr){
                    redisUtil.rightSet(RedisKeyContant.PLAYER_CHAT_MESSAGE,PlayerChatMessage.builder()
                            .sendTime(curr+item*1000)
                            .build());
                }
                return;
            }else{
                PlayerChatMessage msg = (PlayerChatMessage)lpop;
                if(curr>msg.getSendTime()){
                    int type = apiConfig.getTypeSend();
                    if(type==0){
                        type = TimeUtil.getRandomNum(1,5);
                    }
                    try {
                        sendMessageToPlayerChat(apiConfig.getMinMax(),type,apiConfig,true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    redisUtil.lpop(RedisKeyContant.PLAYER_CHAT_MESSAGE);
                }
            }
        }
    }

    public static void queryByTime(QueryWrapper<?> query,int time,String begin,String end){
        switch (time){
            case 0:
                query.ge("created_at", TimeUtil.getDayBegin().getTime());
                query.le("created_at", TimeUtil.getDayEnd().getTime());
                break;
            case 1:
                query.ge("created_at", TimeUtil.getBeginDayOfYesterday().getTime());
                query.le("created_at", TimeUtil.getEndDayOfYesterday().getTime());
                break;
            case 2:
                query.ge("created_at", TimeUtil.getDayBegin().getTime()-3*TimeUtil.TIME_ONE_DAY);
                query.le("created_at", TimeUtil.getDayEnd().getTime());
                break;
            case 3:
                query.ge("created_at", TimeUtil.getBeginDayOfWeek().getTime());
                query.le("created_at", TimeUtil.getDayEnd().getTime());
                break;
            case 4:
                query.ge("created_at", TimeUtil.getBeginDayOfLastWeek().getTime());
                query.le("created_at", TimeUtil.getEndDayOfLastWeek().getTime());
                break;
            case 5:
                query.ge("created_at", TimeUtil.getBeginDayOfMonth().getTime());
                query.le("created_at", TimeUtil.getDayEnd().getTime());
                break;
            case 6:
                query.ge("created_at", TimeUtil.getBeginDayOfLastMonth().getTime());
                query.le("created_at", TimeUtil.getEndDayOfLastMonth().getTime());
                break;
            case 7:
                if(!StringUtils.isBlank(begin)){
                    try {
                        long timeStamp = TimeUtil.stringToTimeStamp(begin, TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
                        query.ge("created_at", timeStamp);
                    } catch (ParseException e) {
                        try {
                            long timeStamp = TimeUtil.stringToTimeStamp(begin, TimeUtil.TIME_YYYY_MM_DD);
                            query.ge("created_at", timeStamp);
                        } catch (ParseException e1) {
                        }
                    }
                }
                if(!StringUtils.isBlank(end)){
                    try {
                        long timeStamp = TimeUtil.stringToTimeStamp(end, TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
                        query.le("created_at", timeStamp);
                    } catch (ParseException e) {
                        try {
                            long timeStamp = TimeUtil.stringToTimeStamp(end, TimeUtil.TIME_YYYY_MM_DD);
                            query.le("created_at", timeStamp+TimeUtil.TIME_ONE_DAY);
                        } catch (ParseException e1) {
                        }
                    }
                }
                break;
            default:
                break;
        }
    }
    public static void queryByTime(QueryWrapper<?> query,String column,int time,String begin,String end){
        switch (time){
            case 0:
                query.ge(column, TimeUtil.getDayBegin().getTime());
                query.le(column, TimeUtil.getDayEnd().getTime());
                break;
            case 1:
                query.ge(column, TimeUtil.getBeginDayOfYesterday().getTime());
                query.le(column, TimeUtil.getEndDayOfYesterday().getTime());
                break;
            case 2:
                query.ge(column, TimeUtil.getDayBegin().getTime()-3*TimeUtil.TIME_ONE_DAY);
                query.le(column, TimeUtil.getDayEnd().getTime());
                break;
            case 3:
                query.ge(column, TimeUtil.getBeginDayOfWeek().getTime());
                query.le(column, TimeUtil.getDayEnd().getTime());
                break;
            case 4:
                query.ge(column, TimeUtil.getBeginDayOfLastWeek().getTime());
                query.le(column, TimeUtil.getEndDayOfLastWeek().getTime());
                break;
            case 5:
                query.ge(column, TimeUtil.getBeginDayOfMonth().getTime());
                query.le(column, TimeUtil.getDayEnd().getTime());
                break;
            case 6:
                query.ge(column, TimeUtil.getBeginDayOfLastMonth().getTime());
                query.le(column, TimeUtil.getEndDayOfLastMonth().getTime());
                break;
            case 7:
                if(!StringUtils.isBlank(begin)){
                    try {
                        long timeStamp = TimeUtil.stringToTimeStamp(begin, TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
                        query.ge(column, timeStamp);
                    } catch (ParseException e) {
                        try {
                            long timeStamp = TimeUtil.stringToTimeStamp(begin, TimeUtil.TIME_YYYY_MM_DD);
                            query.ge(column, timeStamp);
                        } catch (ParseException e1) {
                        }
                    }
                }
                if(!StringUtils.isBlank(end)){
                    try {
                        long timeStamp = TimeUtil.stringToTimeStamp(end, TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
                        query.le(column, timeStamp);
                    } catch (ParseException e) {
                        try {
                            long timeStamp = TimeUtil.stringToTimeStamp(end, TimeUtil.TIME_YYYY_MM_DD);
                            query.le(column, timeStamp+TimeUtil.TIME_ONE_DAY);
                        } catch (ParseException e1) {
                        }
                    }
                }
                break;
            default:
                break;
        }
    }
    public static void queryByTimeConf(QueryWrapper query, Integer time, String begin, String end, BallSystemConfig systemConfig) {
        String queryTime = systemConfig.getStatisTime()==0?"created_at":"updated_at";
        switch (time){
            case 0:
                query.ge(queryTime, TimeUtil.getDayBegin().getTime());
                query.le(queryTime, TimeUtil.getDayEnd().getTime());
                break;
            case 1:
                query.ge(queryTime, TimeUtil.getBeginDayOfYesterday().getTime());
                query.le(queryTime, TimeUtil.getEndDayOfYesterday().getTime());
                break;
            case 2:
                query.ge(queryTime, TimeUtil.getDayBegin().getTime()-3*TimeUtil.TIME_ONE_DAY);
                query.le(queryTime, TimeUtil.getDayEnd().getTime());
                break;
            case 3:
                query.ge(queryTime, TimeUtil.getBeginDayOfWeek().getTime());
                query.le(queryTime, TimeUtil.getDayEnd().getTime());
                break;
            case 4:
                query.ge(queryTime, TimeUtil.getBeginDayOfLastWeek().getTime());
                query.le(queryTime, TimeUtil.getEndDayOfLastWeek().getTime());
                break;
            case 5:
                query.ge(queryTime, TimeUtil.getBeginDayOfMonth().getTime());
                query.le(queryTime, TimeUtil.getDayEnd().getTime());
                break;
            case 6:
                query.ge(queryTime, TimeUtil.getBeginDayOfLastMonth().getTime());
                query.le(queryTime, TimeUtil.getEndDayOfLastMonth().getTime());
                break;
            case 7:
                if(!StringUtils.isBlank(begin)){
                    try {
                        long timeStamp = TimeUtil.stringToTimeStamp(begin, TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
                        query.ge(queryTime, timeStamp);
                    } catch (ParseException e) {
                        try {
                            long timeStamp = TimeUtil.stringToTimeStamp(begin, TimeUtil.TIME_YYYY_MM_DD);
                            query.ge(queryTime, timeStamp);
                        } catch (ParseException e1) {
                        }
                    }
                }
                if(!StringUtils.isBlank(end)){
                    try {
                        long timeStamp = TimeUtil.stringToTimeStamp(end, TimeUtil.TIME_YYYY_MM_DD_HH_MM_SS);
                        query.le(queryTime, timeStamp+TimeUtil.TIME_ONE_DAY);
                    } catch (ParseException e) {
                        try {
                            long timeStamp = TimeUtil.stringToTimeStamp(end, TimeUtil.TIME_YYYY_MM_DD);
                            query.le(queryTime, timeStamp+TimeUtil.TIME_ONE_DAY);
                        } catch (ParseException ex) {
                        }
                    }
                }
                break;
            default:
                break;
        }
    }
}
