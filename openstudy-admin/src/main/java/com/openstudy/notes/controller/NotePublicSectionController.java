package com.openstudy.notes.controller;

import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import com.openstudy.notes.domain.NotePublicSection;
import com.openstudy.notes.mapper.NotePublicSectionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公开笔记分区接口 - 无需登录认证
 */
@RestController
@RequestMapping("/notes/note/publicSection")
public class NotePublicSectionController extends BaseController
{
    @Autowired
    private NotePublicSectionMapper notePublicSectionMapper;

    /**
     * 获取所有公开分区列表，无需登录
     */
    @GetMapping("/list")
    public AjaxResult list()
    {
        List<NotePublicSection> list = notePublicSectionMapper.selectAll();
        return success(list);
    }
}
