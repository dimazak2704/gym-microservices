package com.dimazak.gym.service.impl;

import com.dimazak.gym.dao.UserDao;
import com.dimazak.gym.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveUsers() {
        return userDao.countActiveUsers();
    }
}