package com.cst438.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @Value("${frontend.post.login.url}")
    String redirect_url;

    @GetMapping("/user")
    public String user(@AuthenticationPrincipal OAuth2User principal) {
        return "redirect:" + redirect_url;
    }
}