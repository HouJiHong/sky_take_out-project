package com.sky.service.impl;

import com.alibaba.druid.sql.visitor.functions.Length;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    /**
     * 新增菜品
     * @param dishDTO
     */
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);

        //向菜品表插入基本数据
        dishMapper.insert(dish);
        //获取主键
        Long id = dish.getId();

        //向口味表插入口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors !=null && flavors.size() >0){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(id);
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 删除菜品
     * @param ids
     */
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void deleteBatch(List<Long> ids) {
        //判断当前菜品是否可删--是否在售
        for(long id:ids){
            Dish dish = dishMapper.selectById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //是否被套餐关联
        List<Long> setmealIdsByDishIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if(setmealIdsByDishIds !=null && setmealIdsByDishIds.size()>0){
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }
        //删除菜品中的基本数据
        dishMapper.deleteByIds(ids);
        //删除菜品中的口味数据
        dishFlavorMapper.deleteBatch(ids);

    }

    /**
     * 更新菜品-查询回显
     * @param id
     * @return
     */
    @Override
    public DishVO selectByIdWithFlavor(Long id) {
        /*//法一：分两次查询，一次查菜品基础信息，第二次查口味集合，最后封装
        Dish dish = dishMapper.selectById(id);

        List<DishFlavor> flavors = dishFlavorMapper.selectById(id);
        //封装数据
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(flavors);
        return dishVO;*/

        //法二：两表连接查询，只需设置resultMap的封装
        DishVO dishVO = dishMapper.getInfo(id);
        return dishVO;
    }

    /**
     * 更新菜品-基本消息和口味信息
     * @param dishDTO
     */
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        //修改菜品的基本信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);

        dishMapper.update(dish);

        //修改菜品的口味
        dishFlavorMapper.deleteBatch(Arrays.asList(dishDTO.getId()));

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(!CollectionUtils.isEmpty(flavors)){
            flavors.forEach(flavor -> {
                flavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }
}
