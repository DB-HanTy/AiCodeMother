package com.hty.aicodemother.service.impl;

import com.hty.aicodemother.service.UserService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.hty.aicodemother.model.entity.User;
import com.hty.aicodemother.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
 * 用户 服务层实现。
 *
 * @author <a href="https://github.com/DB-platypuS">程序员普莱塔普斯</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService {

}
