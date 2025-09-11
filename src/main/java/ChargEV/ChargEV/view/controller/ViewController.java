package ChargEV.ChargEV.view.controller;

import ChargEV.ChargEV.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ViewController {

    private final MemberRepository memberRepository;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${naver.map.client-id}")
    private String naverMapClientId;

    @GetMapping("/")
    public String home(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        model.addAttribute("naverMapClientId", naverMapClientId);

        // 인증된 사용자인지 확인 (익명 사용자가 아닌 경우)
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String userEmail = authentication.getName();
            memberRepository.findByEmail(userEmail).ifPresent(member -> {
                model.addAttribute("userNickname", member.getNickName());
            });
        }

        return "home";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("googleClientId", googleClientId);
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }
}
