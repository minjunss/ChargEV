package ChargEV.ChargEV.member.controller;

import ChargEV.ChargEV.chargingStation.dto.ChargingStationResDto;
import ChargEV.ChargEV.config.FeignClientConfig;
import ChargEV.ChargEV.config.QueryDslConfig;
import ChargEV.ChargEV.config.SecurityConfig;
import ChargEV.ChargEV.member.domain.Member;
import ChargEV.ChargEV.member.domain.UserPrincipal;
import ChargEV.ChargEV.member.dto.FavoriteResDto;
import ChargEV.ChargEV.member.service.FavoriteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FavoriteController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {QueryDslConfig.class, FeignClientConfig.class})
        })
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = true)
public class FavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FavoriteService favoriteService;

    private UserPrincipal testUserPrincipal;
    private Member testMember;

    @BeforeEach
    void setUp() {
        // 테스트용 member에 id를 명시적으로 넣어두면 검증에서 편합니다.
        testMember = Member.builder()
                .email("test@example.com")
                .nickName("testuser")
                .build();

        testUserPrincipal = new UserPrincipal(testMember);

    }

    private RequestPostProcessor withUser() {
        return org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(testUserPrincipal);
    }

    @Test
    @DisplayName("즐겨찾기 추가 - 성공")
    void addFavorite_success() throws Exception {
        String statId = "STAT123";

        mockMvc.perform(post("/api/favorites/{statId}", statId)
                        .with(withUser())
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(favoriteService, times(1)).addFavorite(testMember.getId(), statId);
    }

    @Test
    @DisplayName("즐겨찾기 추가 - 권한 없음")
    void addFavorite_unauthorized() throws Exception {
        String statId = "STAT123";

        mockMvc.perform(post("/api/favorites/{statId}", statId))
                .andExpect(status().isUnauthorized());

        verify(favoriteService, never()).addFavorite(anyLong(), anyString());
    }

    @Test
    @DisplayName("즐겨찾기 삭제 - 성공")
    void removeFavorite_success() throws Exception {
        String statId = "STAT123";

        mockMvc.perform(delete("/api/favorites/{statId}", statId)
                        .with(withUser())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(favoriteService, times(1)).removeFavorite(testMember.getId(), statId);
    }

    @Test
    @DisplayName("즐겨찾기 삭제 - 권한 없음")
    void removeFavorite_unauthorized() throws Exception {
        String statId = "STAT123";

        mockMvc.perform(delete("/api/favorites/{statId}", statId))
                .andExpect(status().isUnauthorized());

        verify(favoriteService, never()).removeFavorite(anyLong(), anyString());
    }

    @Test
    @DisplayName("즐겨찾기 목록 조회 - 성공")
    void getFavorites_success() throws Exception {
        ChargingStationResDto csDto = ChargingStationResDto.builder()
                .statId("STAT123")
                .name("Test Station")
                .address("Test Address")
                .latitude(37.0)
                .longitude(127.0)
                .build();
        FavoriteResDto favDto = FavoriteResDto.builder().id(1L).chargingStation(csDto).build();
        List<FavoriteResDto> expectedFavorites = Collections.singletonList(favDto);

        given(favoriteService.getFavorites(any())).willReturn(expectedFavorites);

        mockMvc.perform(get("/api/favorites")
                        .with(withUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].chargingStation.statId").value("STAT123"));

        verify(favoriteService, times(1)).getFavorites(testMember.getId());
    }

    @Test
    @DisplayName("즐겨찾기 목록 조회 - 권한 없음")
    void getFavorites_unauthorized() throws Exception {
        mockMvc.perform(get("/api/favorites"))
                .andExpect(status().isUnauthorized());

        verify(favoriteService, never()).getFavorites(anyLong());
    }

    @Test
    @DisplayName("즐겨찾기 여부 확인 - true")
    void checkFavorite_true() throws Exception {
        String statId = "STAT123";
        given(favoriteService.isFavorite(eq(testMember.getId()), eq(statId)))
                .willReturn(true);

        mockMvc.perform(get("/api/favorites/check/{statId}", statId)
                        .with(withUser()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(favoriteService, times(1)).isFavorite(testMember.getId(), statId);
    }

    @Test
    @DisplayName("즐겨찾기 여부 확인 - false")
    void checkFavorite_false() throws Exception {
        String statId = "STAT123";
        given(favoriteService.isFavorite(anyLong(), anyString())).willReturn(false);

        mockMvc.perform(get("/api/favorites/check/{statId}", statId)
                        .with(withUser()))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(favoriteService, times(1)).isFavorite(testMember.getId(), statId);
    }

    @Test
    @DisplayName("즐겨찾기 여부 확인 - 권한 없음")
    void checkFavorite_unauthorized() throws Exception {
        String statId = "STAT123";

        mockMvc.perform(get("/api/favorites/check/{statId}", statId))
                .andExpect(status().isUnauthorized());

        verify(favoriteService, never()).isFavorite(anyLong(), anyString());
    }
}
