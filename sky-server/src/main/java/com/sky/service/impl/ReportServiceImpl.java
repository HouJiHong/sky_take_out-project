package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //计算从begin到end的所有日期
        List<LocalDate> dataList = new ArrayList<>();
        dataList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dataList.add(begin);
        }
        //将列表转为字符串，用逗号分隔
        /*StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < dataList.size()-1; i++) {
            stringBuilder.append(dataList.get(i)).append(",");
        }
        stringBuilder.append(dataList.get(dataList.size()-1));
        String dateList = stringBuilder.toString();*/
        String join1 = StringUtils.join(dataList, ",");


        //获取营业额数据(状态为已完成)
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dataList){
            //select sum(amount) from orders where order_time >= beginTime and order_time < endTime and status = 5
            //计算时间 -使用LocalDateTime.of补充时间
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map hashMap = new HashMap<>();
            hashMap.put("begin",beginTime);
            hashMap.put("end",endTime);
            hashMap.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(hashMap);
            if(turnover == null){
                turnover = 0.0;
            }
            turnoverList.add(turnover);
        }
        //将列表转为字符串，用逗号分隔
        String join2 = StringUtils.join(turnoverList, ",");


        return TurnoverReportVO.builder()
                .dateList(join1)
                .turnoverList(join2)
                .build();
    }


    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //计算从begin到end的所有日期
        List<LocalDate> dataList = new ArrayList<>();
        dataList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dataList.add(begin);
        }
        //将列表转为字符串，用逗号分隔
        String join1 = StringUtils.join(dataList, ",");

        //获取新增用户数据
        List<Integer> newUserList = new ArrayList<>();
        //获取总用户数据
        List<Integer> totalUserList = new ArrayList<>();

        for(LocalDate date : dataList){
            //计算时间
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //先计算总用户select count(id) from user where create_time < endTime
            HashMap<String, Object> map = new HashMap<>();
            map.put("end",endTime);
            Integer TotalCount = userMapper.countByMap(map);


            //再计算新用户select count(id) from user where create_time > beginTime and create_time < endTime
            map.put("begin",beginTime);
            Integer newCount = userMapper.countByMap(map);

            totalUserList.add(TotalCount);
            newUserList.add(newCount);
        }
        String join2 = StringUtils.join(newUserList, ",");
        String join3 = StringUtils.join(totalUserList, ",");

        //返回结果
        return UserReportVO.builder()
                .dateList(join1)
                .newUserList(join2)
                .totalUserList(join3)
                .build();
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        //计算从begin到end的所有日期
        List<LocalDate> dataList = new ArrayList<>();
        dataList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dataList.add(begin);
        }
        //将列表转为字符串，用逗号分隔
        String join1 = StringUtils.join(dataList, ",");

        //每日订单数据
        List<Integer> totalOrderList = new ArrayList<>();
        //每日有效订单数据
        List<Integer> validOrderList = new ArrayList<>();
        //查询每日订单数和有效总数
        for(LocalDate date : dataList){
            //计算时间
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            HashMap<String, Object> map = new HashMap<>();
            //查询每天总订单数select count(id) from orders where order_time >= beginTime and order_time < endTime
            map.put("begin",beginTime);
            map.put("end",endTime);
            Integer totalOrderCount = orderMapper.countByMap(map);
            totalOrderList.add(totalOrderCount);
            //查询每天有效订单数select count(id) from orders where order_time >= beginTime and order_time < endTime and status = 5
            map.put("status",Orders.COMPLETED);
            Integer validOrderCount = orderMapper.countByMap(map);
            validOrderList.add(validOrderCount);
        }
        String join2 = StringUtils.join(totalOrderList, ",");
        String join3 = StringUtils.join(validOrderList, ",");

        //计算总订单数和有效订单数
        Integer total = totalOrderList.stream().reduce(Integer::sum).get();
        Integer valid = validOrderList.stream().reduce(Integer::sum).get();
        //计算订单完成率
        Double orderCompletionRate = 0.0;
        if(total!=0){
            orderCompletionRate = valid.doubleValue() / total;
        }

        return OrderReportVO.builder()
                .dateList(join1) //日期
                .orderCountList(join2) //每日订单数量
                .validOrderCountList(join3) //每日有效订单数量
                .totalOrderCount(total) //总订单数量
                .validOrderCount(valid) //有效订单数量
                .orderCompletionRate(orderCompletionRate) //订单完成率
                .build();
    }


    /**
     * 销量排名
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);


        //select name,sum(od.number) number from order_detail od left join orders o on o.id = od.order_id
        //where o.order_time >= beginTime
        // and o.order_time < endTime
        // and o.status = 5
        //group by od.name
        //order by number desc
        //limit 0,10

        //获取在时间区间内，订单状态为已完成的订单的商品销量排行
        List<GoodsSalesDTO> salesTop = orderMapper.getSalesTop(beginTime, endTime);

        List<String> name = new ArrayList<>();
        List<Integer> number = new ArrayList<>();
        for(GoodsSalesDTO dto : salesTop){
            name.add(dto.getName());
            number.add(dto.getNumber());
        }
        String join1 = StringUtils.join(name, ",");
        String join2 = StringUtils.join(number, ",");

        return SalesTop10ReportVO.builder()
                .nameList(join1)
                .numberList(join2)
                .build();
    }
}
