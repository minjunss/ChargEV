package ChargEV.ChargEV.member.controller;

import ChargEV.ChargEV.member.dto.SignUpRequest;
import ChargEV.ChargEV.member.dto.UserInfoResponse;
import ChargEV.ChargEV.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {
    private final MemberService memberService;

    @Operation(summary = "로그인 엔드포인트", description = "OAuth2 code를 받아 로그인 후 상태에 따라 리다이렉트")
    @GetMapping("/signin")
    public void login(@Parameter(description = "OAuth2 인증코드") @RequestParam String code,
                      HttpServletRequest request,
                      HttpServletResponse response) throws IOException {
        UserInfoResponse userInfoResponse = memberService.signIn(code);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("[AUTH_CONTROLLER] Authentication object after signIn: {}", auth);

        // SecurityContext를 HttpSession에 직접 저장
        SecurityContext context = SecurityContextHolder.getContext();
        HttpSession session = request.getSession(true); // true: 세션이 없으면 새로 생성
        session.setAttribute("SPRING_SECURITY_CONTEXT", context);
        log.info("Manually saved SecurityContext to session. Session ID: {}", session.getId());

        if(userInfoResponse.getCarNum() == null) {
            // 신규 회원이면 회원가입 페이지로
            response.sendRedirect("/signup?email=" + URLEncoder.encode(userInfoResponse.getEmail(), StandardCharsets.UTF_8));
        } else {
            // 기존 회원이면 홈 페이지로
            response.sendRedirect("/");
        }
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
