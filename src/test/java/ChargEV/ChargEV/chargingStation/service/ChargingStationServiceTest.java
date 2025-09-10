package ChargEV.ChargEV.chargingStation.service;

import ChargEV.ChargEV.config.QueryDslConfig;
import ChargEV.ChargEV.chargingStation.domain.ChargingStation;
import ChargEV.ChargEV.chargingStation.dto.ChargingStationByRangeReqDto;
import ChargEV.ChargEV.chargingStation.dto.ChargingStationDetailResDto;
import ChargEV.ChargEV.chargingStation.dto.ChargingStationResDto;
import ChargEV.ChargEV.feignClient.GongGongClient;
import ChargEV.ChargEV.chargingStation.repository.ChargingStationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static ChargEV.ChargEV.chargingStation.service.TestConstants.MOCK_JSON_RESPONSE_EMPTY;
import static ChargEV.ChargEV.chargingStation.service.TestConstants.createMockJsonResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import({QueryDslConfig.class})
public class ChargingStationServiceTest {

    @Autowired
    private ChargingStationService chargingStationService;

    @MockBean
    private ChargingStationRepository chargingStationRepository;

    @MockBean
    private GongGongClient gongGongClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(chargingStationService, "serviceCode", "testServiceCode");
    }

    @Test
    @Transactional
    @DisplayName("새로운 충전소 정보 저장 테스트")
    void testUpdateChargingStation_NewStations() {
        // given
        String statId = "ST123";
        String chargerId = "01";
        String newStationJson = createMockJsonResponse(statId, chargerId, "20250101000000");

        when(gongGongClient.getChargerInfo(anyString(), anyInt(), anyInt(), anyInt(), anyString()))
                .thenReturn(newStationJson) // 첫 페이지는 데이터 있음
                .thenReturn(MOCK_JSON_RESPONSE_EMPTY); // 두 번째 페이지는 데이터 없음

        when(chargingStationRepository.findAll()).thenReturn(Collections.emptyList());

        // when
        chargingStationService.updateChargingStation();

        // then
        verify(chargingStationRepository, times(1)).findAll();
        verify(chargingStationRepository, times(2)).saveAll(anyList());
    }

    @Test
    @Transactional
    @DisplayName("기존 충전소 정보 업데이트 테스트")
    void testUpdateChargingStation_UpdateExistingStations() {
        // given
        String statId = "ST123";
        String chargerId = "01";

        ChargingStation existingStation = ChargingStation.builder()
                .statId(statId)
                .chargerId(chargerId)
                .updatedDate("20240101000000") // 이전 업데이트 날짜
                .build();

        String updatedStationJson = createMockJsonResponse(statId, chargerId, "20250101000000"); // 최신 업데이트 날짜

        when(gongGongClient.getChargerInfo(anyString(), anyInt(), anyInt(), anyInt(), anyString()))
                .thenReturn(updatedStationJson)
                .thenReturn(MOCK_JSON_RESPONSE_EMPTY);

        when(chargingStationRepository.findAll()).thenReturn(Collections.singletonList(existingStation));

        // when
        chargingStationService.updateChargingStation();

        // then
        verify(chargingStationRepository, times(1)).findAll();
        verify(chargingStationRepository, times(2)).saveAll(anyList());
    }

    @Test
    @Transactional
    @DisplayName("업데이트가 필요 없는 경우 테스트 (기존 정보가 최신)")
    void testUpdateChargingStation_NoUpdateNeeded() {
        // given
        String statId = "ST123";
        String chargerId = "01";

        ChargingStation existingStation = ChargingStation.builder()
                .statId(statId)
                .chargerId(chargerId)
                .updatedDate("20250101000000") // 최신 날짜
                .build();

        String oldStationJson = createMockJsonResponse(statId, chargerId, "20240101000000"); // 이전 날짜

        when(gongGongClient.getChargerInfo(anyString(), anyInt(), anyInt(), anyInt(), anyString()))
                .thenReturn(oldStationJson)
                .thenReturn(MOCK_JSON_RESPONSE_EMPTY);

        when(chargingStationRepository.findAll()).thenReturn(Collections.singletonList(existingStation));

        // when
        chargingStationService.updateChargingStation();

        // then
        verify(chargingStationRepository, times(1)).findAll();
        verify(chargingStationRepository, times(2)).saveAll(anyList());
    }

    @Test
    @DisplayName("범위 내 충전소 조회 서비스 테스트")
    void testGetChargingStationsByRange() {
        // given
        ChargingStationByRangeReqDto reqDto = ChargingStationByRangeReqDto.builder()
                .minLatitude(37.5)
                .maxLatitude(37.6)
                .minLongitude(127.0)
                .maxLongitude(127.5)
                .chargerTypes(List.of("01", "04"))
                .build();

        ChargingStationResDto expectedStation = new ChargingStationResDto(
                "강남 충전소",
                "ST123",
                "서울시 강남구",
                "2층 주차장",
                37.55,
                "충전기 2대",
                "N",
                "",
                127.45,
                "24시간",
                "2025-09-10 12:00",
                "AVAILABLE"
        );

        List<ChargingStationResDto> expectedResponse = Collections.singletonList(expectedStation);
        when(chargingStationRepository.findByCoordinates(reqDto)).thenReturn(expectedResponse);

        // when
        List<ChargingStationResDto> actualResponse = chargingStationService.getChargingStationsByRange(reqDto);

        // then
        assertThat(actualResponse).isNotNull()
                .hasSize(1)
                .containsExactlyElementsOf(expectedResponse);

        verify(chargingStationRepository, times(1)).findByCoordinates(reqDto);
    }

    @Test
    @DisplayName("충전소 상세 조회 서비스 테스트")
    void testGetDetail() {
        // given
        String statId = "ST123";
        ChargingStationDetailResDto expectedDetail = ChargingStationDetailResDto.builder()
                .statId(statId)
                .name("강남 충전소")
                .address("서울시 강남구")
                .chargerType("01")
                .chargerId("CH001")
                .build();

        List<ChargingStationDetailResDto> expectedResponse = Collections.singletonList(expectedDetail);
        when(chargingStationRepository.findDetailByStatId(statId)).thenReturn(expectedResponse);

        // when
        List<ChargingStationDetailResDto> actualResponse = chargingStationService.getDetail(statId);

        // then
        assertThat(actualResponse).isNotNull()
                .hasSize(1)
                .containsExactlyElementsOf(expectedResponse);

        verify(chargingStationRepository, times(1)).findDetailByStatId(statId);
    }
}
