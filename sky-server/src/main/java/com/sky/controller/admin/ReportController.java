package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 数据统计报表
 */
@RestController
@RequestMapping("/admin/report")
@Api(tags = "数据统计报表接口")
@Slf4j
public class ReportController {
    @Autowired
    private ReportService reportService;
    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/turnoverStatistics")
    @ApiOperation("营业额统计")
    public Result<TurnoverReportVO> turnoverStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                                       @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("营业额统计开始时间：{}，结束时间：{}",begin,end);
        TurnoverReportVO turnoverStatistics = reportService.getTurnoverStatistics(begin,end);
        log.info("营业额统计结果：{}",turnoverStatistics);
        return Result.success(turnoverStatistics);
    }


    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/userStatistics")
    @ApiOperation("用户统计")
    public Result<UserReportVO> userStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                                           @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("用户统计开始时间：{}，结束时间：{}",begin,end);
        UserReportVO userStatistics = reportService.getUserStatistics(begin,end);
        log.info("用户统计，{}",userStatistics);
        return Result.success(userStatistics);
    }
}
