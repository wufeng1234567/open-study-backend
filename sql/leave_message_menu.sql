-- 留言管理菜单
-- 格式必须与 sys_menu 表结构完全一致

-- 1. 留言管理菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES ('留言管理', 0, 5, 'message', 'message/index', '', '', 1, 0, 'C', '0', '0', 'system:message:list', 'chat-dot-round', 'admin', NOW());

-- 2. 留言新增按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES ('留言新增', 2111, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:message:add', '#', 'admin', NOW());

-- 3. 留言修改按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES ('留言修改', 2111, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:message:edit', '#', 'admin', NOW());

-- 4. 留言删除按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES ('留言删除', 2111, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:message:remove', '#', 'admin', NOW());

-- 5. 为 admin 和 common 角色分配权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2111 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2111);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2112 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2112);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2113 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2113);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 2114 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2114);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, 2111 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 2 AND menu_id = 2111);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, 2112 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 2 AND menu_id = 2112);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, 2113 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 2 AND menu_id = 2113);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, 2114 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 2 AND menu_id = 2114);
