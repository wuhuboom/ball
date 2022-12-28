package com.oxo.ball.utils;

import com.oxo.ball.bean.dao.*;
import com.oxo.ball.bean.dto.req.report.ReportStandardRequest;
import com.oxo.ball.bean.dto.resp.BaseResponse;
import com.oxo.ball.bean.dto.resp.SearchResponse;
import com.oxo.ball.bean.dto.resp.report.ReportStandardDTO;
import com.oxo.ball.service.admin.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PoiUtil {

    private static int page_size = 500;

    @Value("${static.file}")
    private String staticFile;
    @Autowired
    IBallLoggerWithdrawalService loggerWithdrawalService;
    @Autowired
    IBallLoggerRechargeService loggerRechargeService;
    @Autowired
    IBallBetService betService;
    @Autowired
    IBallLoggerService loggerService;
    @Autowired
    IBallPayBehalfService payBehalfService;
    @Resource
    IBallLoggerRebateService loggerRebateService;
    @Autowired
    IBallReportService reportService;

    public BaseResponse exportLoginLog(BallLoggerLogin queryParam) {
        if(StringUtils.isBlank(queryParam.getPlayerName())){
            return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                    ResponseMessageUtil.responseMessage("", "e51"));
        }
        try {
            String rootPath = staticFile.substring(staticFile.indexOf(":")+1);
            String webpath = "export/";
            String fileRootPath = rootPath+webpath;
            File fileRoot = new File(fileRootPath);
            if(!fileRoot.exists()){
                fileRoot.mkdirs();
            }
            //MD5,如果文件同名,则直接返回
            SearchResponse<BallLoggerLogin> count = loggerService.search(queryParam, 1, 1);
            Long totalCount = count.getTotalCount();
            String s = PasswordUtil.genMd5("loginlog"+queryParam.toString()+totalCount);
            String fileName = s+".xls";
            File downloadFile = new File(fileRootPath+fileName);
            //如果条件一样,返回记录数一样,则不进行查库,直接返回已存在的文件
            if(downloadFile.exists()){
                return BaseResponse.successWithData(MapUtil.newMap("path",webpath+fileName));
            }

            String time = TimeUtil.dateFormat(new Date(), TimeUtil.TIME_YYYY_MM_DD);
            HSSFWorkbook wb = new HSSFWorkbook();
            // 根据页面index 获取sheet页
            HSSFSheet sheet = wb.createSheet(time);
            sheet.setDefaultColumnWidth(12);
            int i=0;
            int pageNo=1;
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("会员账号");
            row.createCell(1).setCellValue("顶级总代");
            row.createCell(2).setCellValue("登录IP");
            row.createCell(3).setCellValue("登录地区");
            row.createCell(4).setCellValue("登录日期");
            row.createCell(5).setCellValue("登录设备");
            while (true){
                SearchResponse<BallLoggerLogin> search = loggerService.search(queryParam, pageNo++, page_size);
                List<BallLoggerLogin> results = search.getResults();
                if(results==null||results.isEmpty()||results.get(0)==null){
                    break;
                }
                for(BallLoggerLogin item:results){
                    // 创建HSSFRow对象
                    row = sheet.createRow(++i);
                    // 创建HSSFCell对象 设置单元格的值
                    row.createCell(0).setCellValue(item.getPlayerName());
                    row.createCell(1).setCellValue(item.getSuperPlayerName());
                    row.createCell(2).setCellValue(item.getIp());
                    row.createCell(3).setCellValue(item.getIpAddr());
                    row.createCell(4).setCellValue(TimeUtil.dateFormat(new Date(item.getCreatedAt())));
                    row.createCell(5).setCellValue(item.getDevices());
                }
            }
            // 输出Excel文件
            FileOutputStream fos = new FileOutputStream(downloadFile);
            wb.write(fos);
            wb.close();
            return BaseResponse.successWithData(MapUtil.newMap("path",webpath+fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                ResponseMessageUtil.responseMessage("", "e52"));
    }
    public BaseResponse exportWithdrawal(BallLoggerWithdrawal queryParam) {
        try {
            String rootPath = staticFile.substring(staticFile.indexOf(":")+1);
            String webpath = "export/";
            String fileRootPath = rootPath+webpath;
            File fileRoot = new File(fileRootPath);
            if(!fileRoot.exists()){
                fileRoot.mkdirs();
            }
            List<BallPayBehalf> byAllTrue = payBehalfService.findByAllTrue();
            Map<Long,BallPayBehalf> ballPayBehalfMap = new HashMap<>();
            for(BallPayBehalf item:byAllTrue){
                ballPayBehalfMap.put(item.getId(),item);
            }
            //MD5,如果文件同名,则直接返回
            SearchResponse<BallLoggerWithdrawal> count = loggerWithdrawalService.search(queryParam, 1, 1);
            Long totalCount = count.getTotalCount();
            String s = PasswordUtil.genMd5(queryParam.toString()+totalCount);
            String fileName = s+".xls";
            File downloadFile = new File(fileRootPath+fileName);
            //如果条件一样,返回记录数一样,则不进行查库,直接返回已存在的文件
            if(downloadFile.exists()){
                return BaseResponse.successWithData(MapUtil.newMap("path",webpath+fileName));
            }

            String time = TimeUtil.dateFormat(new Date(), TimeUtil.TIME_YYYY_MM_DD);
            HSSFWorkbook wb = new HSSFWorkbook();
            // 根据页面index 获取sheet页
            HSSFSheet sheet = wb.createSheet(time);
            sheet.setDefaultColumnWidth(12);
            int i=0;
            int pageNo=1;
            BallLoggerWithdrawal total = BallLoggerWithdrawal.builder()
                    .playerName("总计")
                    .money(0L)
                    .commission(0L)
                    .usdtMoney(0L)
                    .build();
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("订单号");
            row.createCell(1).setCellValue("登录账号");
            row.createCell(2).setCellValue("顶级总代");
            row.createCell(3).setCellValue("一级代理");
            row.createCell(4).setCellValue("账号类型");
            row.createCell(5).setCellValue("提现方式");
            row.createCell(6).setCellValue("状态");
            row.createCell(7).setCellValue("代付单号");
            row.createCell(8).setCellValue("金额");
            row.createCell(9).setCellValue("手续费率(%)");
            row.createCell(10).setCellValue("手续费");
            row.createCell(11).setCellValue("实际金额");
            row.createCell(12).setCellValue("USDT汇率");
            row.createCell(13).setCellValue("USDT金额");
            row.createCell(14).setCellValue("审核人");
            row.createCell(15).setCellValue("确认人");
            row.createCell(16).setCellValue("创建时间");
            row.createCell(17).setCellValue("确认时间");
            row.createCell(18).setCellValue("下发代付");
            row.createCell(19).setCellValue("下发代付时间");
            row.createCell(20).setCellValue("备注");
            i++;
            while (true){
                SearchResponse<BallLoggerWithdrawal> search = loggerWithdrawalService.search(queryParam, pageNo++, page_size);
                List<BallLoggerWithdrawal> results = search.getResults();
                if(results==null||results.isEmpty()||results.get(0)==null){
                    break;
                }
                for(BallLoggerWithdrawal item:results){
                    if(item.getOrderNo()==null){
                        continue;
                    }
                    // 创建HSSFRow对象
                    row = sheet.createRow(++i);
                    // 创建HSSFCell对象 设置单元格的值
                    row.createCell(0).setCellValue(item.getOrderNo());
                    row.createCell(1).setCellValue(item.getPlayerName());
                    row.createCell(2).setCellValue(item.getTopUsername());
                    row.createCell(3).setCellValue(item.getFirstUsername());
                    row.createCell(4).setCellValue(BallPlayer.getAccountType(item.getAccountType()));
                    row.createCell(5).setCellValue(BallLoggerWithdrawal.getTypeString(item.getType()));
                    row.createCell(6).setCellValue(BallLoggerWithdrawal.getStatusString(item.getStatus()));
                    row.createCell(7).setCellValue(item.getBehalfNo());
                    row.createCell(8).setCellValue(item.getMoney()/100d);
                    row.createCell(9).setCellValue(item.getRate()/100d);
                    row.createCell(10).setCellValue(item.getCommission()/100d);
                    row.createCell(11).setCellValue((item.getMoney()-item.getCommission())/100d);
                    Double rate = Double.valueOf(item.getUsdtRate())/100d;
                    row.createCell(12).setCellValue(rate.toString());
                    row.createCell(13).setCellValue(item.getUsdtMoney()/100d);
                    row.createCell(14).setCellValue(item.getChecker());
                    row.createCell(15).setCellValue(item.getOker());
                    row.createCell(16).setCellValue(TimeUtil.dateFormat(new Date(item.getCreatedAt())));
                    row.createCell(17).setCellValue(item.getUpdatedAt()==0?"":TimeUtil.dateFormat(new Date(item.getUpdatedAt())));
                    BallPayBehalf behalf = ballPayBehalfMap.get(item.getBehalfId());
                    row.createCell(18).setCellValue(behalf==null?BallLoggerWithdrawal.getTypeString(item.getType()):behalf.getName());
                    row.createCell(19).setCellValue(item.getBehalfTime()==0?"":TimeUtil.dateFormat(new Date(item.getBehalfTime())));
                    row.createCell(20).setCellValue(item.getRemark());
                    total.setMoney(total.getMoney()+item.getMoney());
                    total.setCommission(total.getCommission()+item.getCommission());
                    total.setUsdtMoney(total.getUsdtMoney()+item.getUsdtMoney());
                }
            }
            row = sheet.createRow(1);
            row.createCell(0).setCellValue("");
            row.createCell(1).setCellValue(total.getPlayerName());
            row.createCell(2).setCellValue("");
            row.createCell(3).setCellValue("");
            row.createCell(4).setCellValue("");
            row.createCell(5).setCellValue("");
            row.createCell(6).setCellValue("");
            row.createCell(7).setCellValue("");
            row.createCell(8).setCellValue(total.getMoney()/100d);
            row.createCell(9).setCellValue("");
            row.createCell(10).setCellValue(total.getCommission()/100d);
            row.createCell(11).setCellValue((total.getMoney()-total.getCommission())/100d);
            row.createCell(12).setCellValue("");
            row.createCell(13).setCellValue(total.getUsdtMoney()/100d);
            row.createCell(14).setCellValue("");
            row.createCell(15).setCellValue("");
            row.createCell(16).setCellValue("");
            row.createCell(17).setCellValue("");
            row.createCell(18).setCellValue("");
            row.createCell(19).setCellValue("");
            row.createCell(20).setCellValue("");
            // 输出Excel文件
            FileOutputStream fos = new FileOutputStream(downloadFile);
            wb.write(fos);
            wb.close();
            return BaseResponse.successWithData(MapUtil.newMap("path",webpath+fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                ResponseMessageUtil.responseMessage("", "e52"));
    }

    public BaseResponse exportRecharge(BallLoggerRecharge queryParam) {
        try {
            String rootPath = staticFile.substring(staticFile.indexOf(":")+1);
            String webpath = "export/";
            String fileRootPath = rootPath+webpath;
            File fileRoot = new File(fileRootPath);
            if(!fileRoot.exists()){
                fileRoot.mkdirs();
            }
            //MD5,如果文件同名,则直接返回
            SearchResponse<BallLoggerRecharge> count = loggerRechargeService.search(queryParam, 1, 1);
            Long totalCount = count.getTotalCount();
            String s = PasswordUtil.genMd5(queryParam.toString()+totalCount);
            String fileName = s+".xls";
            File downloadFile = new File(fileRootPath+fileName);
            //如果条件一样,返回记录数一样,则不进行查库,直接返回已存在的文件
            if(downloadFile.exists()){
                return BaseResponse.successWithData(MapUtil.newMap("path",webpath+fileName));
            }

            String time = TimeUtil.dateFormat(new Date(), TimeUtil.TIME_YYYY_MM_DD);
            HSSFWorkbook wb = new HSSFWorkbook();
            // 根据页面index 获取sheet页
            HSSFSheet sheet = wb.createSheet(time);
            sheet.setDefaultColumnWidth(12);
            int i=0;
            int pageNo=1;
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("用户ID");
            row.createCell(1).setCellValue("登录账号");
            row.createCell(2).setCellValue("顶级总代");
            row.createCell(3).setCellValue("一级代理");
            row.createCell(4).setCellValue("拉起金额");
            row.createCell(5).setCellValue("实际金额");
            row.createCell(6).setCellValue("转系统金额");
            row.createCell(7).setCellValue("支付状态");
            row.createCell(8).setCellValue("订单号");
            row.createCell(9).setCellValue("支付方式");
            row.createCell(10).setCellValue("创建时间");
            row.createCell(11).setCellValue("更新时间");
            row.createCell(12).setCellValue("更新者");
            row.createCell(13).setCellValue("备注");
            while (true){
                SearchResponse<BallLoggerRecharge> search = loggerRechargeService.search(queryParam, pageNo++, page_size);
                List<BallLoggerRecharge> results = search.getResults();
                if(results==null||results.isEmpty()||results.get(0)==null){
                    break;
                }
                for(BallLoggerRecharge item:results){
                    // 创建HSSFRow对象
                    row = sheet.createRow(++i);
                    // 创建HSSFCell对象 设置单元格的值
                    row.createCell(0).setCellValue(item.getUserId().toString());
                    row.createCell(1).setCellValue(item.getUsername());
                    row.createCell(2).setCellValue(item.getTopUsername());
                    row.createCell(3).setCellValue(item.getFirstUsername());
                    row.createCell(4).setCellValue(item.getMoney()/100d);
                    row.createCell(5).setCellValue(item.getMoneyReal()==null?0:item.getMoneyReal()/100d);
                    row.createCell(6).setCellValue(item.getMoneySys()==null?0:item.getMoneySys()/100d);
                    row.createCell(7).setCellValue(BallLoggerRecharge.getStatusString(item.getStatus()));
                    row.createCell(8).setCellValue(item.getOrderNo());
                    row.createCell(9).setCellValue(item.getPayName());
                    row.createCell(10).setCellValue(TimeUtil.dateFormat(new Date(item.getCreatedAt())));
                    row.createCell(11).setCellValue(item.getUpdatedAt()==null||item.getUpdatedAt()==0?"":TimeUtil.dateFormat(new Date(item.getUpdatedAt())));
                    row.createCell(12).setCellValue(item.getOperUser());
                    row.createCell(13).setCellValue(item.getRemark());
                }
            }

            // 输出Excel文件
            FileOutputStream fos = new FileOutputStream(downloadFile);
            wb.write(fos);
            wb.close();
            return BaseResponse.successWithData(MapUtil.newMap("path",webpath+fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                ResponseMessageUtil.responseMessage("", "e52"));
    }

    public BaseResponse exportBet(BallBet queryParam) {
        try {
            String rootPath = staticFile.substring(staticFile.indexOf(":")+1);
            String webpath = "export/";
            String fileRootPath = rootPath+webpath;
            File fileRoot = new File(fileRootPath);
            if(!fileRoot.exists()){
                fileRoot.mkdirs();
            }
            //MD5,如果文件同名,则直接返回
            SearchResponse<BallBet> count = betService.search(queryParam, 1, 1);
            Long totalCount = count.getTotalCount();
            String s = PasswordUtil.genMd5(queryParam.toString()+totalCount);
            String fileName = s+".xls";
            File downloadFile = new File(fileRootPath+fileName);
            //如果条件一样,返回记录数一样,则不进行查库,直接返回已存在的文件
            if(downloadFile.exists()){
                return BaseResponse.successWithData(MapUtil.newMap("path",webpath+fileName));
            }

            String time = TimeUtil.dateFormat(new Date(), TimeUtil.TIME_YYYY_MM_DD);
            HSSFWorkbook wb = new HSSFWorkbook();
            // 根据页面index 获取sheet页
            HSSFSheet sheet = wb.createSheet(time);
            sheet.setDefaultColumnWidth(12);
            int i=0;
            int pageNo=1;
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("订单ID");
            row.createCell(1).setCellValue("用户ID");
            row.createCell(2).setCellValue("登录账号");
            row.createCell(3).setCellValue("顶级总代");
            row.createCell(4).setCellValue("一级代理");
            row.createCell(5).setCellValue("账号类型");
            row.createCell(6).setCellValue("赛事信息");
            row.createCell(7).setCellValue("投注时间");
            row.createCell(8).setCellValue("开赛时间");
            row.createCell(9).setCellValue("结算时间");
            row.createCell(10).setCellValue("下注信息");
            row.createCell(11).setCellValue("波胆");
            row.createCell(12).setCellValue("下注金额");
            row.createCell(13).setCellValue("手续费");
            row.createCell(14).setCellValue("中奖金额");
            row.createCell(15).setCellValue("保本状态");
            row.createCell(16).setCellValue("订单状态");
            row.createCell(17).setCellValue("开奖状态");
            row.createCell(18).setCellValue("结算状态");
            row.createCell(19).setCellValue("结算人");
            while (true){
                SearchResponse<BallBet> search = betService.search(queryParam, pageNo++, page_size);
                List<BallBet> results = search.getResults();
                if(results==null||results.isEmpty()||results.get(0)==null){
                    break;
                }
                for(BallBet item:results){
                    // 创建HSSFRow对象
                    row = sheet.createRow(++i);
                    // 创建HSSFCell对象 设置单元格的值
                    row.createCell(0).setCellValue(item.getOrderNo().toString());
                    row.createCell(1).setCellValue(item.getUserId().toString());
                    row.createCell(2).setCellValue(item.getUsername());
                    row.createCell(3).setCellValue(item.getTopUsername());
                    row.createCell(4).setCellValue(item.getFirstUsername());
                    row.createCell(5).setCellValue(BallPlayer.getAccountType(item.getAccountType()));
                    row.createCell(6).setCellValue(item.getGameInfo());
                    row.createCell(7).setCellValue(TimeUtil.dateFormat(new Date(item.getCreatedAt())));
                    row.createCell(8).setCellValue(TimeUtil.dateFormat(new Date(item.getStartTime())));
                    row.createCell(9).setCellValue(item.getSettlementTime()==0?"":TimeUtil.dateFormat(new Date(item.getSettlementTime())));
                    row.createCell(10).setCellValue(item.getRemark());
                    row.createCell(11).setCellValue(BallBet.getBetTypeString(item.getBetType()));
                    row.createCell(12).setCellValue(item.getBetMoney()/100d);
                    row.createCell(13).setCellValue(item.getHandMoney()/100d);
                    row.createCell(14).setCellValue(item.getWinningAmount()/100d);
                    row.createCell(15).setCellValue(BallBet.getEvenString(item.getEven()));
                    row.createCell(16).setCellValue(BallBet.getBetStatusString(item.getStatus()));
                    row.createCell(17).setCellValue(BallBet.getOpenStatusString(item.getStatusOpen()));
                    row.createCell(18).setCellValue(BallBet.getSettlementString(item.getStatusSettlement()));
                    row.createCell(19).setCellValue(item.getSettlememntPerson());
                }
            }

            // 输出Excel文件
            FileOutputStream fos = new FileOutputStream(downloadFile);
            wb.write(fos);
            wb.close();
            return BaseResponse.successWithData(MapUtil.newMap("path",webpath+fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                ResponseMessageUtil.responseMessage("", "e52"));
    }

    public BaseResponse exportRebateRecharge(BallLoggerRebate loggerRebate, BallAdmin currentUser) {
        try {
            String rootPath = staticFile.substring(staticFile.indexOf(":")+1);
            String webpath = "export/";
            String fileRootPath = rootPath+webpath;
            File fileRoot = new File(fileRootPath);
            if(!fileRoot.exists()){
                fileRoot.mkdirs();
            }
            //MD5,如果文件同名,则直接返回
            SearchResponse<BallLoggerRebate> count = loggerRebateService.searchRecharge(loggerRebate, 1, 1,currentUser);
            Long totalCount = count.getTotalCount();
            String s = PasswordUtil.genMd5(loggerRebate.toString()+totalCount);
            String fileName = s+".xls";
            File downloadFile = new File(fileRootPath+fileName);
            //如果条件一样,返回记录数一样,则不进行查库,直接返回已存在的文件
            if(downloadFile.exists()){
                return BaseResponse.successWithData(MapUtil.newMap("path",webpath+fileName));
            }

            String time = TimeUtil.dateFormat(new Date(), TimeUtil.TIME_YYYY_MM_DD);
            HSSFWorkbook wb = new HSSFWorkbook();
            // 根据页面index 获取sheet页
            HSSFSheet sheet = wb.createSheet(time);
            sheet.setDefaultColumnWidth(12);
            int i=0;
            int pageNo=1;
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("会员账号");
            row.createCell(1).setCellValue("顶级总代");
            row.createCell(2).setCellValue("一级代理");
            row.createCell(3).setCellValue("充值金额");
            row.createCell(4).setCellValue("汇率");
            row.createCell(5).setCellValue("转系统金额");
            row.createCell(6).setCellValue("类型");
            row.createCell(7).setCellValue("优惠金额");
            row.createCell(8).setCellValue("充值订单号");
            row.createCell(9).setCellValue("充值类型");
            row.createCell(10).setCellValue("结算状态");
            row.createCell(11).setCellValue("创建时间");
            i++;
            while (true){
                SearchResponse<BallLoggerRebate> search = loggerRebateService.searchRecharge(loggerRebate, pageNo++, page_size,currentUser);
                List<BallLoggerRebate> results = search.getResults();
                if(results==null||results.isEmpty()||results.get(0)==null){
                    break;
                }
                for(BallLoggerRebate item:results){
                    if(item.getOrderNo()==null){
                        continue;
                    }
                    // 创建HSSFRow对象
                    row = sheet.createRow(i++);
                    // 创建HSSFCell对象 设置单元格的值
                    row.createCell(0).setCellValue(item.getPlayerName());
                    row.createCell(1).setCellValue(item.getTopUsername());
                    row.createCell(2).setCellValue(item.getFirstUsername());
                    row.createCell(3).setCellValue(BigDecimalUtil.div(item.getMoneyUsdt(),BigDecimalUtil.PLAYER_MONEY_UNIT));
                    row.createCell(4).setCellValue(item.getRateUsdt());
                    row.createCell(5).setCellValue(item.getMoneyReal()/100d);
                    row.createCell(6).setCellValue(BallLoggerRebate.getTypeString(item.getType()));
                    row.createCell(7).setCellValue(item.getMoney()/100d);
                    row.createCell(8).setCellValue(item.getOrderNo());
                    row.createCell(9).setCellValue(item.getPayType()==1?"线上":"线下");
                    row.createCell(10).setCellValue(BallLoggerRebate.getStatusString(item.getStatus()));
                    row.createCell(11).setCellValue(TimeUtil.dateFormat(new Date(item.getCreatedAt())));
                }
            }
            // 输出Excel文件
            FileOutputStream fos = new FileOutputStream(downloadFile);
            wb.write(fos);
            fos.close();
            wb.close();
            return BaseResponse.successWithData(MapUtil.newMap("path",webpath+fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                ResponseMessageUtil.responseMessage("", "e52"));
    }

    public BaseResponse exportStandardExport(ReportStandardRequest reportStandardRequest) {
        try {
            String rootPath = staticFile.substring(staticFile.indexOf(":")+1);
            String webpath = "export/";
            String fileRootPath = rootPath+webpath;
            File fileRoot = new File(fileRootPath);
            if(!fileRoot.exists()){
                fileRoot.mkdirs();
            }
            //MD5,如果文件同名,则直接返回
            SearchResponse<ReportStandardDTO> searchResponse = reportService.standard3(reportStandardRequest, 1, 1);
            Long totalCount = searchResponse.getTotalCount();
            String key = searchResponse.toString()+totalCount;
            String s = PasswordUtil.genMd5(key);
            String fileName = s+".xls";
            File downloadFile = new File(fileRootPath+fileName);
            //如果条件一样,返回记录数一样,则不进行查库,直接返回已存在的文件
            if(downloadFile.exists()){
                return BaseResponse.successWithData(MapUtil.newMap("path",webpath+fileName));
            }

            String time = TimeUtil.dateFormat(new Date(), TimeUtil.TIME_YYYY_MM_DD);
            HSSFWorkbook wb = new HSSFWorkbook();
            // 根据页面index 获取sheet页
            HSSFSheet sheet = wb.createSheet(time);
            sheet.setDefaultColumnWidth(12);
            int i=0;
            int pageNo=1;
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("会员账号");
            row.createCell(1).setCellValue("顶级总代");
            row.createCell(2).setCellValue("上级代理");
            row.createCell(3).setCellValue("达标人数");
            row.createCell(4).setCellValue("未达标人数");
            row.createCell(5).setCellValue("团队人数");
            row.createCell(6).setCellValue("充值未达标");
            row.createCell(7).setCellValue("下注未达标");
            row.createCell(8).setCellValue("均未达标");
            i++;
            while (true){
                SearchResponse<ReportStandardDTO> search = reportService.standard3(reportStandardRequest, pageNo++, 10);
                List<ReportStandardDTO> results = search.getResults();
                if(results==null||results.isEmpty()||results.get(0)==null){
                    break;
                }
                for(ReportStandardDTO item:results){
                    // 创建HSSFRow对象
                    row = sheet.createRow(i++);
                    // 创建HSSFCell对象 设置单元格的值
                    row.createCell(0).setCellValue(item.getPlayerName());
                    row.createCell(1).setCellValue(item.getTopUser());
                    row.createCell(2).setCellValue(item.getParentUser());
                    row.createCell(3).setCellValue(String.valueOf(item.getAimBet()));
                    row.createCell(4).setCellValue(String.valueOf(item.getGroupCount()-item.getAimBet()));
                    row.createCell(5).setCellValue(String.valueOf(item.getGroupCount()));
                    row.createCell(6).setCellValue(String.valueOf(item.getGroupCount()-item.getAimRecharge()));
                    row.createCell(7).setCellValue(String.valueOf(item.getGroupCount()-item.getAimBet()));
                    row.createCell(8).setCellValue(String.valueOf(item.getGroupCount()-(item.getAimRecharge()>item.getAimBet()?item.getAimRecharge():item.getAimBet())));
                }
            }
            // 输出Excel文件
            FileOutputStream fos = new FileOutputStream(downloadFile);
            wb.write(fos);
            fos.close();
            wb.close();
            return BaseResponse.successWithData(MapUtil.newMap("path",webpath+fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BaseResponse.failedWithData(BaseResponse.FAIL_FORM_SUBMIT,
                ResponseMessageUtil.responseMessage("", "e52"));
    }
}
