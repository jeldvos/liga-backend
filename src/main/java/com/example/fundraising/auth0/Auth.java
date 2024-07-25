package com.example.fundraising.auth0;

import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;


public final class Auth {

    public static String getToken() {
        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"client_id\":\"Zq5DCRtlGIfPKIexfEil8tfye4gZVLNJ\",\"client_secret\":\"ulWv1RmquT3ilK0BTp8HEocNo6zyP1-chLJLyp2DNVs_6mAvA3N4pJyU_4L50Zkm\",\"audience\":\"https://dev-yujsewbo20nj6zio.eu.auth0.com/api/v2/\",\"grant_type\":\"client_credentials\"}";
        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = template.exchange(
                "https://dev-yujsewbo20nj6zio.eu.auth0.com/oauth/token",
                HttpMethod.POST,
                requestEntity,
                String.class
        );
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            JSONObject json = new JSONObject(response.getBody());
            return json.getString("access_token");
        }
        throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT);
    }
}
