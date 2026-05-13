package com.openstudy.courses.service.impl;

import com.openstudy.courses.domain.Courses;
import com.openstudy.courses.mapper.CoursesMapper;
import com.openstudy.courses.service.ICoursesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 课程信息Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-10-23
 */
@Service
public class CoursesServiceImpl implements ICoursesService 
{
    @Autowired
    private CoursesMapper coursesMapper;

    /**
     * 查询课程信息
     * 
     * @param courseId 课程信息主键
     * @return 课程信息
     */
    @Override
    public Courses selectCoursesByCourseId(Long courseId)
    {
        return coursesMapper.selectCoursesByCourseId(courseId);
    }

    /**
     * 查询课程信息列表
     * 
     * @param courses 课程信息
     * @return 课程信息
     */
    @Override
    public List<Courses> selectCoursesList(Courses courses)
    {
        return coursesMapper.selectCoursesList(courses);
    }

    /**
     * 新增课程信息
     * 
     * @param courses 课程信息
     * @return 结果
     */
    @Override
    public int insertCourses(Courses courses)
    {
        return coursesMapper.insertCourses(courses);
    }

    /**
     * 修改课程信息
     * 
     * @param courses 课程信息
     * @return 结果
     */
    @Override
    public int updateCourses(Courses courses)
    {
        return coursesMapper.updateCourses(courses);
    }

    /**
     * 批量删除课程信息
     * 
     * @param courseIds 需要删除的课程信息主键
     * @return 结果
     */
    @Override
    public int deleteCoursesByCourseIds(Long[] courseIds)
    {
        return coursesMapper.deleteCoursesByCourseIds(courseIds);
    }

    /**
     * 删除课程信息信息
     * 
     * @param courseId 课程信息主键
     * @return 结果
     */
    @Override
    public int deleteCoursesByCourseId(Long courseId)
    {
        return coursesMapper.deleteCoursesByCourseId(courseId);
    }
}
