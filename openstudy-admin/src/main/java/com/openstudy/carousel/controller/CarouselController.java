package com.openstudy.carousel.controller;

import com.openstudy.carousel.domain.Carousel;
import com.openstudy.carousel.service.ICarouselService;
import com.openstudy.common.annotation.Log;
import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import com.openstudy.common.core.page.TableDataInfo;
import com.openstudy.common.enums.BusinessType;
import com.openstudy.common.utils.poi.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 轮播图管理Controller
 * 
 * @author liu
 * @date 2025-12-05
 */
@RestController
@RequestMapping("/carousel/carousel")
@PreAuthorize("@ss.hasRole('common')")
public class CarouselController extends BaseController
{
    @Autowired
    private ICarouselService carouselService;

    /**
     * 查询轮播图管理列表
     */
    @GetMapping("/list")
    public TableDataInfo list(Carousel carousel)
    {
        startPage();
        List<Carousel> list = carouselService.selectCarouselList(carousel);
        return getDataTable(list);
    }

    /**
     * 查询轮播图管理列表不分页
     */
    @GetMapping("/all")
    public AjaxResult all(Carousel carousel)
    {

        List<Carousel> list = carouselService.selectCarouselList(carousel);
        return success(list);
    }

    /**
     * 导出轮播图管理列表
     */
    @Log(title = "轮播图管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Carousel carousel)
    {
        List<Carousel> list = carouselService.selectCarouselList(carousel);
        ExcelUtil<Carousel> util = new ExcelUtil<Carousel>(Carousel.class);
        util.exportExcel(response, list, "轮播图管理数据");
    }

    /**
     * 获取轮播图管理详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(carouselService.selectCarouselById(id));
    }

    /**
     * 新增轮播图管理
     */
    @Log(title = "轮播图管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Carousel carousel)
    {
        return toAjax(carouselService.insertCarousel(carousel));
    }

    /**
     * 修改轮播图管理
     */
    @Log(title = "轮播图管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Carousel carousel)
    {
        return toAjax(carouselService.updateCarousel(carousel));
    }

    /**
     * 删除轮播图管理
     */
    @Log(title = "轮播图管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(carouselService.deleteCarouselByIds(ids));
    }
}
