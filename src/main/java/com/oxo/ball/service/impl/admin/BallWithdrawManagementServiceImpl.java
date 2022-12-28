package com.oxo.ball.service.impl.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oxo.ball.bean.dao.BallPayBehalf;
import com.oxo.ball.bean.dao.BallWithdrawManagement;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.mapper.BallWithdrawManagementMapper;
import com.oxo.ball.service.admin.IBallWithdrawManagementService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxo.ball.utils.UUIDUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

/**
 * <p>
 * 提现方式 服务实现类
 * </p>
 *
 * @author oxo_jy
 * @since 2022-04-13
 */
@Service
public class BallWithdrawManagementServiceImpl extends ServiceImpl<BallWithdrawManagementMapper, BallWithdrawManagement> implements IBallWithdrawManagementService {

    @Value("${static.file}")
    private String staticFile;

    @Override
    public SearchResponse<BallWithdrawManagement> search(BallWithdrawManagement queryParam, Integer pageNo, Integer pageSize) {
        SearchResponse<BallWithdrawManagement> response = new SearchResponse<>();
        Page<BallWithdrawManagement> page = new Page<>(pageNo, pageSize);
        QueryWrapper<BallWithdrawManagement> query = new QueryWrapper<>();
        if(queryParam.getType()!=null){
            query.eq("type",queryParam.getType());
        }
//        if(!StringUtils.isBlank(queryParam.getLanguage())){
//            query.eq("language",queryParam.getLanguage());
//        }
        if(!StringUtils.isBlank(queryParam.getName())){
            query.eq("name",queryParam.getName());
        }
        if(!StringUtils.isBlank(queryParam.getCountry())){
            query.eq("country",queryParam.getCountry());
        }
        if(queryParam.getStatus()!=null){
            query.eq("status",queryParam.getStatus());
        }
        IPage<BallWithdrawManagement> pages = page(page, query);
        response.setPageNo(pages.getCurrent());
        response.setPageSize(pages.getSize());
        response.setTotalCount(pages.getTotal());
        response.setTotalPage(pages.getPages());
        response.setResults(pages.getRecords());
        return response;
    }

    @Override
    @CacheEvict(value = "ball_withdraw_management",key = "'all'")
    public void insert(BallWithdrawManagement ballSlideshowRequest, MultipartFile file) {
        if(file!=null){
            uploadFile(ballSlideshowRequest,file);
        }
        if(ballSlideshowRequest.getSort()==null){
            ballSlideshowRequest.setSort(0);
        }
        ballSlideshowRequest.setCreatedAt(System.currentTimeMillis());
        save(ballSlideshowRequest);
    }

    @Override
    @CacheEvict(value = "ball_withdraw_management",key = "'all'")
    public Boolean edit(BallWithdrawManagement withdrawManagement, MultipartFile file) {
        uploadFile(withdrawManagement,file);
        if(withdrawManagement.getSort()==null){
            withdrawManagement.setSort(0);
        }
        withdrawManagement.setUpdatedAt(System.currentTimeMillis());
        return updateById(withdrawManagement);
    }

    @Override
    @CacheEvict(value = "ball_withdraw_management",key = "'all'")
    public Boolean status(BallWithdrawManagement withdrawManagement) {
        BallWithdrawManagement edit = BallWithdrawManagement.builder()
                .status(withdrawManagement.getStatus()==1?2:1)
                .build();
        edit.setId(withdrawManagement.getId());
        return updateById(edit);
    }

    @Override
    @CacheEvict(value = "ball_withdraw_management",key = "'all'")
    public Boolean delete(Long id) {
        return removeById(id);
    }

    @Override
    @Cacheable(value = "ball_withdraw_management",key = "'all'",unless = "#result.empty")
    public List<BallWithdrawManagement> findAll() {
        QueryWrapper query = new QueryWrapper();
        query.eq("status",1);
        query.orderByAsc("sort");
        List list = list(query);
        return list;
    }

    @Override
    public BallWithdrawManagement findById(Long id) {
        return getById(id);
    }

    private void uploadFile(BallWithdrawManagement slideshow, MultipartFile file) {
        String rootPath = staticFile.substring(staticFile.indexOf(":")+1);
        if(file!=null && !file.isEmpty()){
            String webpath = "withdraw/";
            String fileRootPath = rootPath+webpath;
            File fileRoot = new File(fileRootPath);
            if(!fileRoot.exists()){
                fileRoot.mkdirs();
            }
            String originalFilename = file.getOriginalFilename();
            //后缀
            String subfex = originalFilename.substring(originalFilename.lastIndexOf("."));
            String saveName = UUIDUtil.getUUID()+subfex;
            try {
                InputStream inputStream = file.getInputStream();
                String savePath = fileRootPath+saveName;
                FileOutputStream fos = new FileOutputStream(savePath);
                byte[] b = new byte[128];
                int len;
                while((len = inputStream.read(b))!=-1){
                    fos.write(b,0,len);
                }
                fos.flush();
                fos.close();
                inputStream.close();
                slideshow.setIamgeUrl(webpath+saveName);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
}
