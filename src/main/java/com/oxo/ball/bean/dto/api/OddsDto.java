package com.oxo.ball.bean.dto.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.oxo.ball.utils.BigDecimalUtil;
import com.oxo.ball.utils.TimeUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomUtils;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OddsDto {
    private String home;
    private String away;
    private String odds;

    private static String[] ODDS_FIXED = new String[]{"0.3","0.4","0.5"};
    public static String getRandomOdd(String odds){
        double a = Double.parseDouble(odds);
        double randomNum = TimeUtil.getRandomNum(10, 20)/100.0;
        return BigDecimalUtil.format2(TimeUtil.getRandomNum(1,3)%2==0?a+a*randomNum:a-a*randomNum);
    }
    public static Map<String,OddsDto> full(){
        Map<String,OddsDto> oddsMap = new HashMap<>();
        oddsMap.put("0:0",OddsDto.builder()
                .home("0")
                .away("0")
                .odds("3.82")
                .build());
        oddsMap.put("0:1",OddsDto.builder()
                .home("0")
                .away("1")
                .odds("4.02")
                .build());
        oddsMap.put("0:2",OddsDto.builder()
                .home("0")
                .away("2")
                .odds("2.76")
                .build());
        oddsMap.put("0:3",OddsDto.builder()
                .home("0")
                .away("3")
                .odds("1.57")
                .build());
        oddsMap.put("*:4",OddsDto.builder()
                .home("*")
                .away("4")
                .odds(ODDS_FIXED[RandomUtils.nextInt(0,3)])
                .build());
        oddsMap.put("1:0",OddsDto.builder()
                .home("1")
                .away("0")
                .odds("4.79")
                .build());
        oddsMap.put("1:2",OddsDto.builder()
                .home("1")
                .away("2")
                .odds("3.49")
                .build());
        oddsMap.put("1:3",OddsDto.builder()
                .home("1")
                .away("3")
                .odds("1.83")
                .build());
        oddsMap.put("1:1",OddsDto.builder()
                .home("1")
                .away("1")
                .odds("5.58")
                .build());
        oddsMap.put("2:0",OddsDto.builder()
                .home("2")
                .away("0")
                .odds("3.77")
                .build());
        oddsMap.put("2:1",OddsDto.builder()
                .home("2")
                .away("1")
                .odds("4.09")
                .build());
        oddsMap.put("2:2",OddsDto.builder()
                .home("2")
                .away("2")
                .odds("2.75")
                .build());
        oddsMap.put("2:3",OddsDto.builder()
                .home("2")
                .away("3")
                .odds("1.38")
                .build());
        oddsMap.put("3:0",OddsDto.builder()
                .home("3")
                .away("0")
                .odds("2.31")
                .build());
        oddsMap.put("3:1",OddsDto.builder()
                .home("3")
                .away("1")
                .odds("2.37")
                .build());
        oddsMap.put("3:2",OddsDto.builder()
                .home("3")
                .away("2")
                .odds("1.56")
                .build());
        oddsMap.put("3:3",OddsDto.builder()
                .home("3")
                .away("3")
                .odds("0.89")
                .build());
        oddsMap.put("4:*",OddsDto.builder()
                .home("4")
                .away("*")
                .odds(ODDS_FIXED[RandomUtils.nextInt(0,3)])
                .build());
        return oddsMap;
    }
    public static Map<String,OddsDto> half(){
        Map<String,OddsDto> oddsMap = new HashMap<>();
        oddsMap.put("0:0",OddsDto.builder()
                .home("0")
                .away("0")
                .odds("13.63")
                .build());
        oddsMap.put("0:1",OddsDto.builder()
                .home("0")
                .away("1")
                .odds("7.74")
                .build());
        oddsMap.put("0:2",OddsDto.builder()
                .home("0")
                .away("2")
                .odds("2.74")
                .build());
        oddsMap.put("*:3",OddsDto.builder()
                .home("*")
                .away("3")
                .odds(ODDS_FIXED[RandomUtils.nextInt(0,3)])
                .build());
        oddsMap.put("1:0",OddsDto.builder()
                .home("1")
                .away("0")
                .odds("9.11")
                .build());
        oddsMap.put("1:1",OddsDto.builder()
                .home("1")
                .away("1")
                .odds("5.21")
                .build());
        oddsMap.put("1:2",OddsDto.builder()
                .home("1")
                .away("2")
                .odds("1.76")
                .build());
        oddsMap.put("2:0",OddsDto.builder()
                .home("2")
                .away("0")
                .odds("3.69")
                .build());
        oddsMap.put("2:1",OddsDto.builder()
                .home("2")
                .away("1")
                .odds("2.02")
                .build());
        oddsMap.put("2:2",OddsDto.builder()
                .home("2")
                .away("2")
                .odds("0.83")
                .build());
        oddsMap.put("3:*",OddsDto.builder()
                .home("3")
                .away("*")
                .odds(ODDS_FIXED[RandomUtils.nextInt(0,3)])
                .build());
        return oddsMap;
    }
}
