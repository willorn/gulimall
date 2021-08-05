package com.atguigu.gulimall.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //使用queryWrapper的方式来进行筛选，筛选出剩下的值就可以交给categoryEntities
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);

        //服务调用了下层级的方法
        List<CategoryEntity> level1Menus = categoryEntities
                .stream().filter(categoryEntity -> {
                    return categoryEntity.getParentCid() == 0;
                })
                .map((menu) -> {
                    //方法用于映射每个元素到对应的结果，以下代码片段使用 map 输出了元素对应的目录层级
                    //拿到子菜单
                    menu.setChlidren(getChildrens(menu, categoryEntities));
                    return menu;
                })
                .sorted((menu1, menu2) -> {
                    //对两级目录做排序
                    return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
                })
                .collect(Collectors.toList());
//        System.out.println(level1Menus);
        return level1Menus;
    }

    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {
        //filter说的是我要什么。
        List<CategoryEntity> collect = all
                .stream()
                .filter(categoryEntity -> {
                    return categoryEntity.getParentCid() == root.getCatId();
                })
                .map(categoryEntity -> {
                    categoryEntity.setChlidren(getChildrens(categoryEntity, all));
                    return categoryEntity;
                })
                .sorted((A, B) -> {
                    return (A.getSort() == null ? 0 : A.getSort()) - (B.getSort() == null ? 0 : B.getSort());
                })
                .collect(Collectors.toList());
        return collect;
    }
}