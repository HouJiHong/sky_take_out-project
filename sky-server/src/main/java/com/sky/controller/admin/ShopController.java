package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * 店铺状态,使用redis
 */
@RestController("adminShopController")//设置bean的默认名
@RequestMapping("/admin/shop")
@Api(tags = "管理端店铺相关接口")
@Slf4j
public class ShopController {
    //注入redis
    @Autowired
    private RedisTemplate redisTemplate;

    public static final String KEY ="SHOP_STATUS";


    /**
     * 设置店铺状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("设置店铺状态")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置店铺状态为：{}",status==1?"营业":"打烊");
        redisTemplate.opsForValue().set(KEY,status);
        return Result.success();
    }

    /**
     * 查询店铺状态
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("查询店铺状态")
    public Result<Integer> selectStatus(){
        Integer shopStatus = (Integer)redisTemplate.opsForValue().get(KEY);
        log.info("查询到店铺状态为：{}",shopStatus==1?"营业":"打烊");
        return Result.success(shopStatus);
    }
}
