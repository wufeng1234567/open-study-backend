package com.openstudy.web.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.openstudy.common.core.controller.BaseController;
import com.openstudy.common.core.domain.AjaxResult;
import com.openstudy.common.core.domain.entity.SysUser;
import com.openstudy.common.core.domain.model.RegisterBody;
import com.openstudy.common.utils.StringUtils;
import com.openstudy.framework.web.service.SysRegisterService;
import com.openstudy.noteCategory.domain.NoteCategory;
import com.openstudy.noteCategory.service.INoteCategoryService;
import com.openstudy.system.service.ISysConfigService;
import com.openstudy.system.service.ISysUserService;

/**
 * 注册验证
 * 
 * @author ruoyi
 */
@RestController
public class SysRegisterController extends BaseController
{
    @Autowired
    private SysRegisterService registerService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private INoteCategoryService noteCategoryService;

    @PostMapping("/register")
    public AjaxResult register(@RequestBody RegisterBody user)
    {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser"))))
        {
            return error("当前系统没有开启注册功能！");
        }
        String msg = registerService.register(user);
        if (StringUtils.isEmpty(msg))
        {
            // 注册成功，获取用户ID并执行后续操作
            SysUser sysUser = userService.selectUserByUserName(user.getUsername());
            if (sysUser != null && sysUser.getUserId() != null)
            {
                Long userId = sysUser.getUserId();
                
                // 1. 分配普通用户角色（role_id=2）
                userService.insertUserRole(userId, new Long[]{2L});
                
                // 2. 创建默认笔记分类
                NoteCategory defaultCategory = new NoteCategory();
                defaultCategory.setUserId(userId);
                defaultCategory.setName("默认分类");
                defaultCategory.setOrderNum(0L);
                noteCategoryService.insertNoteCategory(defaultCategory);
            }
            
            return success();
        }
        return error(msg);
    }
}
