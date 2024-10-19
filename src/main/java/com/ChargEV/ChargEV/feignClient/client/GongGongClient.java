package com.ChargEV.ChargEV.feignClient.client;

import com.ChargEV.ChargEV.feignClient.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "GongGongClient", url = "http://apis.data.go.kr/B552584/EvCharger", configuration = FeignClientConfig.class)
public interface GongGongClient {
    @GetMapping("/getChargerInfo")
    String getChargerInfo(@RequestParam String ServiceKey,
                          @RequestParam int pageNo,
                          @RequestParam int numOfRows,
                          @RequestParam int zcode,
                          @RequestParam(defaultValue = "JSON") String dataType);
}
