package com.openstudy.courses.domain;

import com.openstudy.common.annotation.Excel;
import com.openstudy.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 课程信息对象 courses
 * 
 * @author ruoyi
 * @date 2025-10-23
 */
public class Courses extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    private Long courseId;

    /** 课程代码 */
    @Excel(name = "课程代码")
    private String courseCode;

    /** 课程名称 */
    @Excel(name = "课程名称")
    private String courseName;

    /** 课程描述 */
    @Excel(name = "课程描述")
    private String description;

    /** 学分 */
    @Excel(name = "学分")
    private Long credits;

    /** 所属院系 */
    @Excel(name = "所属院系")
    private String department;

    /** 授课教师 */
    @Excel(name = "授课教师")
    private String instructor;

    /** 学期 */
    @Excel(name = "学期")
    private String semester;

    /** 年份 */
    @Excel(name = "年份")
    private Long year;

    /** 最大学生数 */
    @Excel(name = "最大学生数")
    private Long maxStudents;

    /** 创建时间 */
    private Date createdAt;

    public void setCourseId(Long courseId) 
    {
        this.courseId = courseId;
    }

    public Long getCourseId() 
    {
        return courseId;
    }

    public void setCourseCode(String courseCode) 
    {
        this.courseCode = courseCode;
    }

    public String getCourseCode() 
    {
        return courseCode;
    }

    public void setCourseName(String courseName) 
    {
        this.courseName = courseName;
    }

    public String getCourseName() 
    {
        return courseName;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }

    public void setCredits(Long credits) 
    {
        this.credits = credits;
    }

    public Long getCredits() 
    {
        return credits;
    }

    public void setDepartment(String department) 
    {
        this.department = department;
    }

    public String getDepartment() 
    {
        return department;
    }

    public void setInstructor(String instructor) 
    {
        this.instructor = instructor;
    }

    public String getInstructor() 
    {
        return instructor;
    }

    public void setSemester(String semester) 
    {
        this.semester = semester;
    }

    public String getSemester() 
    {
        return semester;
    }

    public void setYear(Long year) 
    {
        this.year = year;
    }

    public Long getYear() 
    {
        return year;
    }

    public void setMaxStudents(Long maxStudents) 
    {
        this.maxStudents = maxStudents;
    }

    public Long getMaxStudents() 
    {
        return maxStudents;
    }

    public void setCreatedAt(Date createdAt) 
    {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() 
    {
        return createdAt;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("courseId", getCourseId())
            .append("courseCode", getCourseCode())
            .append("courseName", getCourseName())
            .append("description", getDescription())
            .append("credits", getCredits())
            .append("department", getDepartment())
            .append("instructor", getInstructor())
            .append("semester", getSemester())
            .append("year", getYear())
            .append("maxStudents", getMaxStudents())
            .append("createdAt", getCreatedAt())
            .toString();
    }
}
