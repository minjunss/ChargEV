package ChargEV.ChargEV.chargingStation.service;

import ChargEV.ChargEV.chargingStation.domain.ChargingStation;
import ChargEV.ChargEV.chargingStation.feignClient.GongGongClient;
import ChargEV.ChargEV.chargingStation.repository.ChargingStationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static ChargEV.ChargEV.chargingStation.service.TestConstants.MOCK_JSON_RESPONSE_EMPTY;
import static ChargEV.ChargEV.chargingStation.service.TestConstants.createMockJsonResponse;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
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
}