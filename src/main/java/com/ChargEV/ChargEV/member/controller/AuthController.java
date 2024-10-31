package com.ChargEV.ChargEV.member.controller;

import com.ChargEV.ChargEV.member.dto.SignUpRequest;
import com.ChargEV.ChargEV.member.dto.UserInfoResponse;
import com.ChargEV.ChargEV.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @Operation(summary = "로그인 엔드포인트", description = "OAuth2 code를 받아 로그인")
    @GetMapping("/signin")
    public ResponseEntity<UserInfoResponse> login(@Parameter(description = "OAuth2 인증코드") @RequestParam String code,
                                                  HttpServletResponse response) throws IOException {
        UserInfoResponse userInfoResponse = memberService.signIn(code);
        if(userInfoResponse.getCarNum() == null) {
            response.sendRedirect("/signup?email=" + URLEncoder.encode(userInfoResponse.getEmail(), StandardCharsets.UTF_8));
        }
        return ResponseEntity.ok(userInfoResponse);
    }

    @Operation(summary = "회원가입 엔드포인트", description = "사용자의 이메일, 닉네임 및 차량 번호를 포함한 정보를 수신하여 회원가입")
    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "회원가입 요청정보", required = true)
                                         @Valid @RequestBody SignUpRequest signUpRequest) {
        memberService.signUp(signUpRequest);
        // 리다이렉트할 URL을 클라이언트로 보냄
        return ResponseEntity.ok("/login");
    }

}
