package com.openstudy.courses.controller;

import com.openstudy.common.annotation.Log;
import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import com.openstudy.common.core.page.TableDataInfo;
import com.openstudy.common.enums.BusinessType;
import com.openstudy.common.utils.poi.ExcelUtil;
import com.openstudy.courses.domain.Courses;
import com.openstudy.courses.service.ICoursesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 课程信息Controller
 * 
 * @author ruoyi
 * @date 2025-10-23
 */
@RestController
@RequestMapping("/courses/courses")
public class CoursesController extends BaseController
{
    @Autowired
    private ICoursesService coursesService;

    /**
     * 查询课程信息列表
     */
    @PreAuthorize("@ss.hasPermi('courses:courses:list')")
    @GetMapping("/list")
    public TableDataInfo list(Courses courses)
    {
        startPage();
        List<Courses> list = coursesService.selectCoursesList(courses);
        return getDataTable(list);
    }

    /**
     * 导出课程信息列表
     */
    @PreAuthorize("@ss.hasPermi('courses:courses:export')")
    @Log(title = "课程信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Courses courses)
    {
        List<Courses> list = coursesService.selectCoursesList(courses);
        ExcelUtil<Courses> util = new ExcelUtil<Courses>(Courses.class);
        util.exportExcel(response, list, "课程信息数据");
    }

    /**
     * 获取课程信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('courses:courses:query')")
    @GetMapping(value = "/{courseId}")
    public AjaxResult getInfo(@PathVariable("courseId") Long courseId)
    {
        return success(coursesService.selectCoursesByCourseId(courseId));
    }

    /**
     * 新增课程信息
     */
    @PreAuthorize("@ss.hasPermi('courses:courses:add')")
    @Log(title = "课程信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Courses courses)
    {
        return toAjax(coursesService.insertCourses(courses));
    }

    /**
     * 修改课程信息
     */
    @PreAuthorize("@ss.hasPermi('courses:courses:edit')")
    @Log(title = "课程信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Courses courses)
    {
        return toAjax(coursesService.updateCourses(courses));
    }

    /**
     * 删除课程信息
     */
    @PreAuthorize("@ss.hasPermi('courses:courses:remove')")
    @Log(title = "课程信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{courseIds}")
    public AjaxResult remove(@PathVariable Long[] courseIds)
    {
        return toAjax(coursesService.deleteCoursesByCourseIds(courseIds));
    }
}
