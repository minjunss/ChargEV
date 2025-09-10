package ChargEV.ChargEV.member.service;

import ChargEV.ChargEV.feignClient.GoogleClient;
import ChargEV.ChargEV.member.domain.Member;
import ChargEV.ChargEV.member.domain.UserPrincipal;
import ChargEV.ChargEV.member.dto.SignUpRequest;
import ChargEV.ChargEV.member.dto.TokenResponse;
import ChargEV.ChargEV.member.dto.UserInfoResponse;
import ChargEV.ChargEV.member.repository.MemberRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
    private final MemberRepository memberRepository;
    private final GoogleClient googleClient;
    private GoogleIdTokenVerifier googleIdTokenVerifier;
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String GOOGLE_CLIENT_ID;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String GOOGLE_CLIENT_SECRET;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String GOOGLE_REDIRECT_URI;

    @PostConstruct
    public void init() {
        googleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
                .build();
        log.info("GoogleIdTokenVerifier initialized.");
    }

    public UserInfoResponse signIn(String code) {
        TokenResponse tokenResponse = googleClient.getToken(GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET, code, GOOGLE_REDIRECT_URI, "authorization_code");
        try {
            GoogleIdToken idToken = googleIdTokenVerifier.verify(tokenResponse.getId_token());

            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();

                Member foundMember = memberRepository.findByEmail(email).orElse(null);
                if (foundMember != null) {
                    UserPrincipal userPrincipal = new UserPrincipal(foundMember);
                    Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    return UserInfoResponse.builder()
                            .email(email)
                            .carNum(foundMember.getCarNum())
                            .nickName(foundMember.getNickName())
                            .build();

                } else {
                    return UserInfoResponse.builder()
                            .email(email)
                            .carNum(null)
                            .nickName(null)
                            .build();
                }
            } else {
                throw new IllegalArgumentException("Invalid ID token");
            }

        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void signUp(SignUpRequest signUpRequest) {
        Member foundMember = memberRepository.findByEmail(signUpRequest.getEmail()).orElse(null);
        if (foundMember != null) {
            throw new IllegalArgumentException("Member with this email already exists.");
        }
        memberRepository.save(SignUpRequest.toEntity(signUpRequest));
    }

}
