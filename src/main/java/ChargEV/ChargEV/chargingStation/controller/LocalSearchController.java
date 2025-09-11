package ChargEV.ChargEV.chargingStation.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RestController
@RequestMapping("/api/localSearch")
public class LocalSearchController {

    @Value("${naver.search.client.id}")
    private String clientId;

    @Value("${naver.search.client.secret}")
    private String clientSecret;

    @GetMapping("/search")
    public ResponseEntity<String> search(@RequestParam("query") String query) {
        try {
            String apiURL = "https://openapi.naver.com/v1/search/local.json?query=" + URLEncoder.encode(query, "UTF-8") + "&display=5";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiURL))
                    .header("X-Naver-Client-Id", clientId)
                    .header("X-Naver-Client-Secret", clientSecret)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return ResponseEntity.status(response.statusCode()).body(response.body());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("API 호출 실패");
        }
    }
}
