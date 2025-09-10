package ChargEV.ChargEV.config;

import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }
    private static class CustomErrorDecoder implements ErrorDecoder {

        @Override
        public Exception decode(String methodKey, Response response) {
            //Custom Exception
            return FeignException.errorStatus(methodKey, response);
        }
    }
}
