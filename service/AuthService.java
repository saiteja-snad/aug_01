package com.quickbite.service;

import com.quickbite.dto.request.LoginRequest;
import com.quickbite.dto.request.RegisterRequest;
import com.quickbite.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
