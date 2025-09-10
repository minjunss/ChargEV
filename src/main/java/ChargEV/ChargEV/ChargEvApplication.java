package ChargEV.ChargEV;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableFeignClients
public class ChargEvApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChargEvApplication.class, args);
	}

}
