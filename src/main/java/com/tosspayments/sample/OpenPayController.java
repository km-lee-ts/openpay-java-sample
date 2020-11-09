package com.tosspayments.sample;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Controller
public class OpenPayController {
    private static final Logger logger = LoggerFactory.getLogger(OpenPayController.class);

    // http 요청 및 json 처리를 위한 초기화 - 테스트 편의를 위해서 restTemplate이 exception 발생하지 않도록 설정함
    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    private void init() {
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) {
            }
        });
    }
    //

    private static final String TEST_API_KEY = "test_ak_ZORzdMaqN3wQd5k6ygr5AkYXQGwy";
    private static final String ACCESS_TOKEN_ENDPOINT = "https://api.tosspayments.com/v1/openpay/authorizations/access-token";

    @GetMapping("/")
    public String testIndex(Model model) {
        model.addAttribute("customerKey", getCustomerKeyFromSession());
        return "openpay_test";
    }

    @GetMapping("/callback_auth")
    public String authCallback(Model model, @RequestParam String code) throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(TEST_API_KEY, "");

        Map<String, String> payloadMap = new HashMap<>();
        payloadMap.put("code", code);

        String customerKey = getCustomerKeyFromSession();
        payloadMap.put("customerKey", customerKey);

        // optional
        String ci = getCustomerCI();
        payloadMap.put("ci", ci);
        //

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(payloadMap), headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(ACCESS_TOKEN_ENDPOINT, entity, JsonNode.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                String accessToken = response.getBody().get("accessToken").asText();

                model.addAttribute("message", "인증 되었습니다.");

                logger.info("access token: " + accessToken); // 브라우저에 노출되는 template에 출력하지 않아야 합니다.
            } else {
                model.addAttribute("message", response.getBody().get("message").asText());
            }
        } catch (RestClientResponseException e) {
            model.addAttribute("message", e.getMessage());
        }

        return "auth_callback";
    }

    private String getCustomerKeyFromSession() {
        // 실제 로그인 세션에서 사용자 key를 가져오는 로직 필요
        return "customer123";
    }

    private String getCustomerCI() {
        // 실제 사용자의 CI를 가져오는 로직 필요
        return "customer123-ci";
    }
}
