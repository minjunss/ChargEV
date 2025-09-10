
package ChargEV.ChargEV.chargingStation.controller;

import ChargEV.ChargEV.chargingStation.service.ChargingStationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verify;

import ChargEV.ChargEV.chargingStation.config.FeignClientConfig;
import ChargEV.ChargEV.chargingStation.config.QueryDslConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@WebMvcTest(controllers = ChargingStationController.class)
public class ChargingStationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChargingStationService chargingStationService;

    @Test
    @DisplayName("충전소 정보 업데이트 API 호출 테스트")
    void testFetch() throws Exception {
        // when & then
        mockMvc.perform(get("/api/chargingStation/fetch"))
                .andExpect(status().isOk());

        verify(chargingStationService).updateChargingStation();
    }
}
