package com.openstudy.carousel.service.impl;

import com.openstudy.carousel.domain.Carousel;
import com.openstudy.carousel.mapper.CarouselMapper;
import com.openstudy.carousel.service.ICarouselService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 轮播图管理Service业务层处理
 * 
 * @author liu
 * @date 2025-12-05
 */
@Service
public class CarouselServiceImpl implements ICarouselService 
{
    @Autowired
    private CarouselMapper carouselMapper;

    /**
     * 查询轮播图管理
     * 
     * @param id 轮播图管理主键
     * @return 轮播图管理
     */
    @Override
    public Carousel selectCarouselById(String id)
    {
        return carouselMapper.selectCarouselById(id);
    }

    /**
     * 查询轮播图管理列表
     * 
     * @param carousel 轮播图管理
     * @return 轮播图管理
     */
    @Override
    public List<Carousel> selectCarouselList(Carousel carousel)
    {
        return carouselMapper.selectCarouselList(carousel);
    }

    /**
     * 新增轮播图管理
     * 
     * @param carousel 轮播图管理
     * @return 结果
     */
    @Override
    public int insertCarousel(Carousel carousel)
    {
        return carouselMapper.insertCarousel(carousel);
    }

    /**
     * 修改轮播图管理
     * 
     * @param carousel 轮播图管理
     * @return 结果
     */
    @Override
    public int updateCarousel(Carousel carousel)
    {
        return carouselMapper.updateCarousel(carousel);
    }

    /**
     * 批量删除轮播图管理
     * 
     * @param ids 需要删除的轮播图管理主键
     * @return 结果
     */
    @Override
    public int deleteCarouselByIds(String[] ids)
    {
        return carouselMapper.deleteCarouselByIds(ids);
    }

    /**
     * 删除轮播图管理信息
     * 
     * @param id 轮播图管理主键
     * @return 结果
     */
    @Override
    public int deleteCarouselById(String id)
    {
        return carouselMapper.deleteCarouselById(id);
    }
}
