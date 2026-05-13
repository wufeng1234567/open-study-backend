package com.openstudy.courses.service;

import com.openstudy.courses.domain.Courses;

import java.util.List;

/**
 * 课程信息Service接口
 * 
 * @author ruoyi
 * @date 2025-10-23
 */
public interface ICoursesService 
{
    /**
     * 查询课程信息
     * 
     * @param courseId 课程信息主键
     * @return 课程信息
     */
    public Courses selectCoursesByCourseId(Long courseId);

    /**
     * 查询课程信息列表
     * 
     * @param courses 课程信息
     * @return 课程信息集合
     */
    public List<Courses> selectCoursesList(Courses courses);

    /**
     * 新增课程信息
     * 
     * @param courses 课程信息
     * @return 结果
     */
    public int insertCourses(Courses courses);

    /**
     * 修改课程信息
     * 
     * @param courses 课程信息
     * @return 结果
     */
    public int updateCourses(Courses courses);

    /**
     * 批量删除课程信息
     * 
     * @param courseIds 需要删除的课程信息主键集合
     * @return 结果
     */
    public int deleteCoursesByCourseIds(Long[] courseIds);

    /**
     * 删除课程信息信息
     * 
     * @param courseId 课程信息主键
     * @return 结果
     */
    public int deleteCoursesByCourseId(Long courseId);
}
