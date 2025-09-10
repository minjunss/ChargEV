package ChargEV.ChargEV.chargingStation.controller;

import ChargEV.ChargEV.chargingStation.config.FeignClientConfig;
import ChargEV.ChargEV.chargingStation.config.QueryDslConfig;
import ChargEV.ChargEV.chargingStation.dto.ChargingStationByRangeReqDto;
import ChargEV.ChargEV.chargingStation.dto.ChargingStationDetailResDto;
import ChargEV.ChargEV.chargingStation.dto.ChargingStationResDto;
import ChargEV.ChargEV.chargingStation.service.ChargingStationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ChargingStationController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {QueryDslConfig.class, FeignClientConfig.class})
        })
public class ChargingStationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChargingStationService chargingStationService;

    @Autowired
    private Gson gson;

    @Test
    @DisplayName("충전소 정보 업데이트 API 호출 테스트")
    void testFetch() throws Exception {
        // when & then
        mockMvc.perform(get("/api/chargingStation/fetch"))
                .andExpect(status().isOk());

        verify(chargingStationService).updateChargingStation();
    }

    @Test
    @DisplayName("범위 내 충전소 조회 API 테스트")
    void testRange() throws Exception {
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

        when(chargingStationService.getChargingStationsByRange(any())).thenReturn(expectedResponse);

        // when & then
        mockMvc.perform(post("/api/chargingStation/range")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(expectedResponse.size()))
                .andExpect(jsonPath("$[0].name").value("강남 충전소"))
                .andExpect(jsonPath("$[0].statId").value("ST123"))
                .andExpect(jsonPath("$[0].address").value("서울시 강남구"));

        verify(chargingStationService, times(1)).getChargingStationsByRange(any());
    }

    @Test
    @DisplayName("충전소 상세 정보 조회 API 테스트")
    void testGetChargingStationDetail() throws Exception {
        // given
        String statId = "ST001";
        List<ChargingStationDetailResDto> responseDtos = Collections.singletonList(
                ChargingStationDetailResDto.builder()
                        .name("충전소1")
                        .statId("ST001")
                        .chargerType("01")
                        .output("50")
                        .method("단독")
                        .kind("B0")
                        .kindDetail("B001")
                        .address("주소1")
                        .location("위치1")
                        .note("노트1")
                        .limitYn("N")
                        .limitDetail("")
                        .useTime("24시간")
                        .updatedDate("20250101000000")
                        .stat("2")
                        .chargerId("01")
                        .build()
        );

        given(chargingStationService.getDetail(statId)).willReturn(responseDtos);

        // when & then
        mockMvc.perform(get("/api/chargingStation/detail")
                        .param("statId", statId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].statId").value("ST001"))
                .andExpect(jsonPath("$[0].chargerId").value("01"));
    }
}