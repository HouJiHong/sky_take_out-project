package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.service.impl.DishServiceImpl;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 菜品管理，由于用户端使用了redis缓存菜品，当管理端进行新增，修改，删除，
 * 起售/停售菜品，都需要对redis进行清理，保持数据一致性
 */
@RestController
@Slf4j
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @ApiOperation("新增菜品")
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品, {}",dishDTO);
        dishService.saveWithFlavor(dishDTO);

        // 清理缓存
        String key = "dish_" + dishDTO.getCategoryId();
        redisTemplate.delete(key);

        return Result.success();
    }

    /**
     * 分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("分页查询,{}",dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("删除菜品")
    public Result deleteByIds(@RequestParam List<Long> ids){
        log.info("删除菜品, {}",ids);
        dishService.deleteBatch(ids);

        // 清理缓存(删除全部dish_开头的key)
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

        return Result.success();
    }

    /**
     * 更新菜品-查询回显
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("更新菜品-查询回显")
    public Result<DishVO> selectById(@PathVariable Long id){
        log.info("更新菜品-查询回显,{}",id);
        DishVO dishVO = dishService.selectByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 更新菜品
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("更新菜品")
    public Result updateById(@RequestBody DishDTO dishDTO){
        log.info("更新菜品,{}",dishDTO);
        dishService.updateWithFlavor(dishDTO);

        // 清理缓存(删除全部dish_开头的key)
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

        return Result.success();
    }

    /**
     * 启用/禁用菜品
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用/禁用菜品")
    public Result<String> startOrStop(Integer status, Long id){
        log.info("启用/禁用菜品,{}",id);
        dishService.startOrStop(status,id);

        // 清理缓存(删除全部dish_开头的key)
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId){
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }
}
