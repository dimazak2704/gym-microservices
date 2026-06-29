package com.dimazak.gym.service;

import com.dimazak.gym.dao.UserDao;
import com.dimazak.gym.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserDao userDao;

    @InjectMocks private UserServiceImpl userService;

    @Test
    void countActiveUsers_shouldDelegateToDao() {
        when(userDao.countActiveUsers()).thenReturn(15L);
        assertEquals(15L, userService.countActiveUsers());
        verify(userDao).countActiveUsers();
    }

    @Test
    void countActiveUsers_shouldReturnZeroWhenNoneActive() {
        when(userDao.countActiveUsers()).thenReturn(0L);
        assertEquals(0L, userService.countActiveUsers());
    }
}