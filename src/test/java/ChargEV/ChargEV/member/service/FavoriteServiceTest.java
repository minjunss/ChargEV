package ChargEV.ChargEV.member.service;

import ChargEV.ChargEV.chargingStation.domain.ChargingStation;
import ChargEV.ChargEV.chargingStation.dto.ChargingStationResDto;
import ChargEV.ChargEV.chargingStation.repository.ChargingStationRepository;
import ChargEV.ChargEV.member.domain.Favorite;
import ChargEV.ChargEV.member.domain.Member;
import ChargEV.ChargEV.member.dto.FavoriteResDto;
import ChargEV.ChargEV.member.repository.FavoriteRepository;
import ChargEV.ChargEV.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FavoriteServiceTest {

    @InjectMocks
    private FavoriteService favoriteService;

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ChargingStationRepository chargingStationRepository;

    private Member testMember;
    private ChargingStation testChargingStation;
    private Favorite testFavorite;

    @BeforeEach
    void setUp() {
        testMember = Member.builder().email("test@example.com").nickName("testuser").build();
        testChargingStation = ChargingStation.builder().statId("STAT123").name("Test Station").address("Test Address").build();
        testFavorite = Favorite.builder().member(testMember).chargingStation(testChargingStation).build();
    }

    @Test
    @DisplayName("즐겨찾기 추가 성공 테스트")
    void addFavorite_success() {
        // Given
        when(memberRepository.findById(any())).thenReturn(Optional.of(testMember));
        when(chargingStationRepository.findByStatId(anyString())).thenReturn(Collections.singletonList(testChargingStation));
        when(favoriteRepository.findByMemberIdAndChargingStationStatId(any(), anyString())).thenReturn(Optional.empty());

        // When
        favoriteService.addFavorite(testMember.getId(), testChargingStation.getStatId());

        // Then
        verify(favoriteRepository, times(1)).save(any(Favorite.class));
    }

    @Test
    @DisplayName("즐겨찾기 추가 실패 - 회원 없음")
    void addFavorite_memberNotFound() {
        // Given
        when(memberRepository.findById(any())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            favoriteService.addFavorite(testMember.getId(), testChargingStation.getStatId());
        }, "Member not found");
    }

    @Test
    @DisplayName("즐겨찾기 추가 실패 - 충전소 없음")
    void addFavorite_chargingStationNotFound() {
        // Given
        when(memberRepository.findById(any())).thenReturn(Optional.of(testMember));
        when(chargingStationRepository.findByStatId(anyString())).thenReturn(Collections.emptyList());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            favoriteService.addFavorite(testMember.getId(), testChargingStation.getStatId());
        }, "ChargingStation not found");
    }

    @Test
    @DisplayName("즐겨찾기 추가 실패 - 이미 즐겨찾기됨")
    void addFavorite_alreadyFavorited() {
        // Given
        when(memberRepository.findById(any())).thenReturn(Optional.of(testMember));
        when(chargingStationRepository.findByStatId(anyString())).thenReturn(Collections.singletonList(testChargingStation));
        when(favoriteRepository.findByMemberIdAndChargingStationStatId(any(), anyString())).thenReturn(Optional.of(testFavorite));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            favoriteService.addFavorite(testMember.getId(), testChargingStation.getStatId());
        }, "Already favorited");
    }

    @Test
    @DisplayName("즐겨찾기 삭제 성공 테스트")
    void removeFavorite_success() {
        // When
        favoriteService.removeFavorite(testMember.getId(), testChargingStation.getStatId());

        // Then
        verify(favoriteRepository, times(1)).deleteByMemberIdAndChargingStationStatId(testMember.getId(), testChargingStation.getStatId());
    }

    @Test
    @DisplayName("즐겨찾기 목록 조회 성공 테스트 - 즐겨찾기 있음")
    void getFavorites_successWithFavorites() {
        // Given
        List<Favorite> favorites = Arrays.asList(testFavorite);
        when(favoriteRepository.findByMemberId(any())).thenReturn(favorites);

        // When
        List<FavoriteResDto> result = favoriteService.getFavorites(testMember.getId());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(testFavorite.getId());
        assertThat(result.get(0).getChargingStation().getStatId()).isEqualTo(testChargingStation.getStatId());
        assertThat(result.get(0).getChargingStation().getName()).isEqualTo(testChargingStation.getName());
    }

    @Test
    @DisplayName("즐겨찾기 목록 조회 성공 테스트 - 즐겨찾기 없음")
    void getFavorites_successNoFavorites() {
        // Given
        when(favoriteRepository.findByMemberId(any())).thenReturn(Collections.emptyList());

        // When
        List<FavoriteResDto> result = favoriteService.getFavorites(testMember.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("즐겨찾기 여부 확인 테스트 - 즐겨찾기됨")
    void isFavorite_true() {
        // Given
        when(favoriteRepository.findByMemberIdAndChargingStationStatId(any(), anyString())).thenReturn(Optional.of(testFavorite));

        // When
        boolean result = favoriteService.isFavorite(testMember.getId(), testChargingStation.getStatId());

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("즐겨찾기 여부 확인 테스트 - 즐겨찾기 안됨")
    void isFavorite_false() {
        // Given
        when(favoriteRepository.findByMemberIdAndChargingStationStatId(any(), anyString())).thenReturn(Optional.empty());

        // When
        boolean result = favoriteService.isFavorite(testMember.getId(), testChargingStation.getStatId());

        // Then
        assertThat(result).isFalse();
    }
}
