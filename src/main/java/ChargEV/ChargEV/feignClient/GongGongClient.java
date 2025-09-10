package ChargEV.ChargEV.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "GongGongClinet", url = "http://apis.data.go.kr/B552584/EvCharger", configuration = FeignClientProperties.FeignClientConfiguration.class)
public interface GongGongClient {
    @GetMapping("/getChargerInfo")
    String getChargerInfo(@RequestParam String ServiceKey,
                          @RequestParam int pageNo,
                          @RequestParam int numOfRows,
                          @RequestParam int zcode,
                          @RequestParam(defaultValue = "JSON") String dataType);
}
