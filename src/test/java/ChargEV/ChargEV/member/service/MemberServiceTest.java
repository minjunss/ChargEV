package ChargEV.ChargEV.member.service;

import ChargEV.ChargEV.feignClient.GoogleClient;
import ChargEV.ChargEV.member.domain.Member;
import ChargEV.ChargEV.member.dto.SignUpRequest;
import ChargEV.ChargEV.member.dto.TokenResponse;
import ChargEV.ChargEV.member.dto.UserInfoResponse;
import ChargEV.ChargEV.member.repository.MemberRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.json.webtoken.JsonWebSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GoogleClient googleClient;

    @Mock
    private GoogleIdTokenVerifier googleIdTokenVerifier;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(memberService, "GOOGLE_CLIENT_ID", "test-client-id");
        ReflectionTestUtils.setField(memberService, "GOOGLE_CLIENT_SECRET", "test-client-secret");
        ReflectionTestUtils.setField(memberService, "GOOGLE_REDIRECT_URI", "http://localhost/redirect");
        // init() 메소드에서 googleIdTokenVerifier가 다시 생성되므로, Mock 객체로 다시 설정합니다.
        ReflectionTestUtils.setField(memberService, "googleIdTokenVerifier", googleIdTokenVerifier);
    }

    @Test
    @DisplayName("기존 회원 로그인 테스트")
    void signIn_existingUser() throws GeneralSecurityException, IOException {
        // given
        String code = "test_code";
        String idTokenString = "test_id_token";
        String email = "test@example.com";

        TokenResponse tokenResponse = new TokenResponse();
        ReflectionTestUtils.setField(tokenResponse, "id_token", idTokenString);

        GoogleIdToken idToken = mock(GoogleIdToken.class);
        GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);

        when(googleClient.getToken(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(tokenResponse);
        when(googleIdTokenVerifier.verify(idTokenString)).thenReturn(idToken);
        when(idToken.getPayload()).thenReturn(payload);
        when(payload.getEmail()).thenReturn(email);

        Member existingMember = Member.builder().email(email).nickName("testuser").carNum("123가4567").build();
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(existingMember));

        // when
        UserInfoResponse userInfoResponse = memberService.signIn(code);

        // then
        assertThat(userInfoResponse.getEmail()).isEqualTo(email);
        assertThat(userInfoResponse.getCarNum()).isEqualTo("123가4567");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    @DisplayName("신규 회원 로그인 테스트")
    void signIn_newUser() throws GeneralSecurityException, IOException {
        // given
        String code = "test_code";
        String idTokenString = "test_id_token";
        String email = "new@example.com";

        TokenResponse tokenResponse = new TokenResponse();
        ReflectionTestUtils.setField(tokenResponse, "id_token", idTokenString);

        GoogleIdToken idToken = mock(GoogleIdToken.class);
        GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);

        when(googleClient.getToken(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(tokenResponse);
        when(googleIdTokenVerifier.verify(idTokenString)).thenReturn(idToken);
        when(idToken.getPayload()).thenReturn(payload);
        when(payload.getEmail()).thenReturn(email);
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when
        UserInfoResponse userInfoResponse = memberService.signIn(code);

        // then
        assertThat(userInfoResponse.getEmail()).isEqualTo(email);
        assertThat(userInfoResponse.getCarNum()).isNull();
        assertThat(userInfoResponse.getNickName()).isNull();
    }

    @Test
    @DisplayName("회원가입 성공 테스트")
    void signUp_success() {
        // given
        SignUpRequest signUpRequest = new SignUpRequest("new@example.com", "newuser", "123가1234");
        when(memberRepository.findByEmail(signUpRequest.getEmail())).thenReturn(Optional.empty());

        // when
        memberService.signUp(signUpRequest);

        // then
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 회원가입 시 예외 발생 테스트")
    void signUp_emailAlreadyExists() {
        // given
        SignUpRequest signUpRequest = new SignUpRequest("existing@example.com", "user", "111가1111");
        Member existingMember = Member.builder().email(signUpRequest.getEmail()).build();
        when(memberRepository.findByEmail(signUpRequest.getEmail())).thenReturn(Optional.of(existingMember));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            memberService.signUp(signUpRequest);
        });
    }
}
