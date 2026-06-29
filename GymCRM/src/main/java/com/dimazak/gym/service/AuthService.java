package com.dimazak.gym.service;

import com.dimazak.gym.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(String username, String password);

    void logout(String token);

    void changePassword(String username, String oldPassword, String newPassword);
}