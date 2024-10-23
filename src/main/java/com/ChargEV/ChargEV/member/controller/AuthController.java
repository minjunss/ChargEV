package com.ChargEV.ChargEV.member.controller;

import com.ChargEV.ChargEV.member.dto.SignUpRequest;
import com.ChargEV.ChargEV.member.dto.UserInfoResponse;
import com.ChargEV.ChargEV.member.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final MemberService memberService;

    @GetMapping("/signin")
    public ResponseEntity<UserInfoResponse> login(@RequestParam String code, HttpServletResponse response) throws IOException {
        UserInfoResponse userInfoResponse = memberService.signIn(code);
        if(userInfoResponse.getCarNum() == null) {
            response.sendRedirect("/signup?email=" + URLEncoder.encode(userInfoResponse.getEmail(), StandardCharsets.UTF_8));
        }
        return ResponseEntity.ok(userInfoResponse);
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        memberService.signUp(signUpRequest);
        // 리다이렉트할 URL을 클라이언트로 보냄
        return ResponseEntity.ok("/login");
    }

}
