package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oxo.ball.bean.dao.BallPaymentManagement;
import com.oxo.ball.bean.dao.BallPlayer;
import com.oxo.ball.bean.dao.BallSystemConfig;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.config.SomeConfig;
import com.oxo.ball.mapper.BallPaymentManagementMapper;
import com.oxo.ball.service.admin.IBallPaymentManagementService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.service.admin.IBallSystemConfigService;
import com.oxo.ball.utils.UUIDUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>
 * 支付管理 服务实现类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Service
public class BallPaymentManagementServiceImpl extends ServiceImpl<BallPaymentManagementMapper, BallPaymentManagement> implements IBallPaymentManagementService {
    @Value("${static.file}")
    private String staticFile;
    @Autowired
    private IBallSystemConfigService systemConfigService;
    @Autowired
    SomeConfig someConfig;

    @Override
    public SearchResponse<BallPaymentManagement> search(BallPaymentManagement queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallPaymentManagement> response = new SearchResponse<>();
        Page<BallPaymentManagement> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallPaymentManagement> query = new QueryWrapper<>();
        if (queryParam.getFrontDisplay() != null) {
            query.eq("front_display", queryParam.getFrontDisplay());
        }
        if (queryParam.getPayType() != null) {
            query.eq("pay_type", queryParam.getPayType());
        }
        if (queryParam.getPayTypeOnff() != null) {
            query.eq("pay_type_onff", queryParam.getPayTypeOnff());
        }
        if (!StringUtils.isBlank(queryParam.getCountry())) {
            query.eq("country", queryParam.getCountry());
        }
        query.orderByDesc("sort");
        IPage<BallPaymentManagement> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    public BallPaymentManagement findById(Long id) {
        return getById(id);
    }

    @Override
    public List<BallPaymentManagement> findByAll(BallPlayer ballPlayer) {
        QueryWrapper<BallPaymentManagement> query = new QueryWrapper();
        query.eq("front_display", 1);
//        query.eq("country", ballPlayer.getLoginContry());
        query.orderByDesc("sort");
        //查询区号和手机号相同 ,或者没有填写区号的支付渠道，
        BallSystemConfig systemConfig = systemConfigService.getSystemConfig();
        //区号通道开关为开才按区号查询
        List<BallPaymentManagement> list = list(query);
        List<BallPaymentManagement> res = new CopyOnWriteArrayList<>(list);
        //
        if (systemConfig.getRechargeAreaSwtich() != null && systemConfig.getRechargeAreaSwtich() == 1) {
            for (BallPaymentManagement item : res) {
                //如果玩家没有设置区号,只返回未设置区号的通道
                if (StringUtils.isBlank(item.getAreaCode())) {
                    //没有填写区号则返回
                } else {
                    String[] split = item.getAreaCode().split(",");
                    List<String> strings = Arrays.asList(split);
                    if (!strings.contains(ballPlayer.getAreaCode())) {
                        res.remove(item);
                    }
                }
            }
        }
        return res;
    }

    @Override
    public List<BallPaymentManagement> findByAll() {
        QueryWrapper<BallPaymentManagement> query = new QueryWrapper();
        query.eq("front_display", 1);
        return list(query);
    }

    @Override
    public List<BallPaymentManagement> findByAllTrue() {
        QueryWrapper<BallPaymentManagement> query = new QueryWrapper();
        return list(query);
    }

    @Override
    public BallPaymentManagement insert(BallPaymentManagement paymentManagement, MultipartFile file) {
        paymentManagement.setCreatedAt(System.currentTimeMillis());
        if(!someConfig.getServerUrl().endsWith("/")){
            someConfig.setServerUrl(someConfig.getServerUrl()+"/");
        }
        setCallBackUrl(paymentManagement);
        uploadFile(paymentManagement, file);
        if (!StringUtils.isBlank(paymentManagement.getUstdCallback())) {
            paymentManagement.setUstdCallbackPath(UUIDUtil.getServletPath(paymentManagement.getUstdCallback()));
        }
        if (paymentManagement.getPayType() == 1) {
            paymentManagement.setPrivateKey(paymentManagement.getPrivateKey().replaceAll("\\s", ""));
            paymentManagement.setPublicKey(paymentManagement.getPublicKey().replaceAll("\\s", ""));
        }
        if (StringUtils.isBlank(paymentManagement.getReturnUrl()) || "null".equals(paymentManagement.getReturnUrl())) {
            paymentManagement.setReturnUrl("");
        }
        if (StringUtils.isBlank(paymentManagement.getQueryUrl()) || "null".equals(paymentManagement.getQueryUrl())) {
            paymentManagement.setQueryUrl("");
        }
//        1.usdt
//                * 2.ID-印度
//                * 3.加纳
//                * 4.印度-fastpay
        save(paymentManagement);
        return paymentManagement;
    }

    @Override
    public Boolean delete(Long id) {
        boolean b = removeById(id);
        return b;
    }

    @Override
    public Boolean edit(BallPaymentManagement paymentManagement, MultipartFile file) {
        setCallBackUrl(paymentManagement);
        uploadFile(paymentManagement, file);
        if (!StringUtils.isBlank(paymentManagement.getUstdCallback())) {
            //回调的serverPath,每个usdt对应1个处理path
            paymentManagement.setUstdCallbackPath(UUIDUtil.getServletPath(paymentManagement.getUstdCallback()));
        }
        if (paymentManagement.getPayType() != null && paymentManagement.getPayType() == 1) {
            paymentManagement.setPrivateKey(paymentManagement.getPrivateKey().replaceAll("\\s", ""));
            paymentManagement.setPublicKey(paymentManagement.getPublicKey().replaceAll("\\s", ""));
        }
        if (StringUtils.isBlank(paymentManagement.getReturnUrl()) || "null".equals(paymentManagement.getReturnUrl())) {
            paymentManagement.setReturnUrl("");
        }
        if (StringUtils.isBlank(paymentManagement.getQueryUrl()) || "null".equals(paymentManagement.getQueryUrl())) {
            paymentManagement.setQueryUrl("");
        }
        boolean b = updateById(paymentManagement);
        return b;
    }

    private void setCallBackUrl(BallPaymentManagement paymentManagement) {
        if(paymentManagement.getPayType()==null){
            return;
        }
        //TODO 新支付
        switch (paymentManagement.getPayType()) {
            case 1:
                paymentManagement.setUstdCallback(someConfig.getServerUrl()+"player/pay/callback/1");
                break;
            case 2:
                paymentManagement.setUstdCallback(someConfig.getServerUrl()+"player/pay/callback/2");
                break;
            case 3:
                paymentManagement.setUstdCallback(someConfig.getServerUrl()+"player/pay/callback/4");
                break;
            case 4:
                paymentManagement.setUstdCallback(someConfig.getServerUrl()+"player/pay/callback/6");
                break;
            case 5:
                paymentManagement.setUstdCallback(someConfig.getServerUrl()+"player/pay/callback/8");
                break;
            case 6:
                paymentManagement.setUstdCallback(someConfig.getServerUrl()+"player/pay/callback/10");
                break;
            case 7:
                paymentManagement.setUstdCallback(someConfig.getServerUrl()+"player/pay/callback/12");
                break;
            case 8:
                paymentManagement.setUstdCallback(someConfig.getServerUrl()+"player/pay/callback/14");
                break;
            case 9:
                paymentManagement.setUstdCallback(someConfig.getServerUrl()+"player/pay/callback/16");
                break;
            case 10:
                paymentManagement.setUstdCallback(someConfig.getServerUrl()+"player/pay/callback/18");
                break;
            case 11:
                paymentManagement.setUstdCallback(someConfig.getServerUrl()+"player/pay/callback/20");
                break;
            case 12:
                paymentManagement.setUstdCallback(someConfig.getServerUrl()+"player/pay/callback/22");
                break;
            case 13:
                paymentManagement.setUstdCallback(someConfig.getServerUrl()+"player/pay/callback/24");
                break;
            default:
                break;
        }
    }

    @Override
    public Boolean status(BallPaymentManagement ballVip) {
        BallPaymentManagement edit = BallPaymentManagement.builder()
                .frontDisplay(ballVip.getFrontDisplay())
                .build();
        edit.setId(ballVip.getId());
        edit.setUpdatedAt(System.currentTimeMillis());
        Boolean succ = edit(edit, null);
        return succ;
    }

    @Override
    public BallPaymentManagement findByCallback(String s) {
        QueryWrapper query = new QueryWrapper();
        query.eq("ustd_callback_path", s);
        query.eq("front_display", 1);
        return getOne(query);
    }

    @Override
    public BallPaymentManagement findByName(String payName) {
        QueryWrapper query = new QueryWrapper();
        query.eq("name", payName);
        return getOne(query);
    }

    private void uploadFile(BallPaymentManagement paymentManagement, MultipartFile file) {
        String rootPath = staticFile.substring(staticFile.indexOf(":") + 1);
        if (file != null && !file.isEmpty()) {
            String webpath = "payImg/";
            String fileRootPath = rootPath + webpath;
            File fileRoot = new File(fileRootPath);
            if (!fileRoot.exists()) {
                fileRoot.mkdirs();
            }
            String originalFilename = file.getOriginalFilename();
            //后缀
            String subfex = originalFilename.substring(originalFilename.lastIndexOf("."));
            String saveName = UUIDUtil.getUUID() + subfex;
            try {
                InputStream inputStream = file.getInputStream();
                String savePath = fileRootPath + saveName;
                FileOutputStream fos = new FileOutputStream(savePath);
                byte[] b = new byte[128];
                int len;
                while ((len = inputStream.read(b)) != -1) {
                    fos.write(b, 0, len);
                }
                fos.flush();
                fos.close();
                inputStream.close();
                paymentManagement.setImg(webpath + saveName);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
