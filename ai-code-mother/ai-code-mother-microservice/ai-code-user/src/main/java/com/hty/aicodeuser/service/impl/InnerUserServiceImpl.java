package com.hty.aicodeuser.service.impl;

import com.hty.aicodemother.innerservice.InnerUserService;
import com.hty.aicodemother.model.entity.User;
import com.hty.aicodemother.model.vo.UserVO;
import com.hty.aicodeuser.service.UserService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;


import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 内部服务实现类
 */
@DubboService//能将实现类识别到
public class InnerUserServiceImpl implements InnerUserService {

    @Resource
    private UserService userService;

    @Override
    public List<User> listByIds(Collection<? extends Serializable> ids) {
        return userService.listByIds(ids);
    }

    @Override
    public User getById(Serializable id) {
        return userService.getById(id);
    }

    @Override
    public UserVO getUserVO(User user) {
        return userService.getUserVO(user);
    }
}
