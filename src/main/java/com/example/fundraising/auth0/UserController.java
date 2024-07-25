package com.example.fundraising.auth0;

import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class UserController {

    public String getStatus(String job, String token){
        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = template.exchange(
                "https://dev-yujsewbo20nj6zio.eu.auth0.com/api/v2/jobs/"+job,
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            JSONObject json = new JSONObject(response.getBody());
            return json.getString("status");
        }
        throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT);
    }

    public String getLocation(String job, String token){
        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = template.exchange(
                "https://dev-yujsewbo20nj6zio.eu.auth0.com/api/v2/jobs/"+job,
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            JSONObject json = new JSONObject(response.getBody());
            return json.getString("location");
        }
        throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT);
    }

    @GetMapping("/user/{userid}/mail")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String getUserEmail(@PathVariable String userid){
        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + Auth.getToken());
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = template.exchange(
                    "https://dev-yujsewbo20nj6zio.eu.auth0.com/api/v2/users/"+userid,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode().equals(HttpStatus.OK)) {
                JSONObject json = new JSONObject(response.getBody());
                return json.getString("email");
            }
        }catch (HttpClientErrorException exception){
            System.out.println(exception);
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT);
        }
        throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT);
    }

    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String getUsers() {
        String body = "{\"format\": \"csv\", \"fields\": [{\"name\":\"user_id\"}, {\"name\": \"email\"}, { \"name\": \"identities[0].connection\", \"export_as\": \"provider\" },{\"name\":\"user_metadata.bedrijf\", \"export_as\": \"bedrijf\"},{\"name\":\"user_metadata.voornaam\", \"export_as\": \"voornaam\"},{\"name\":\"user_metadata.familienaam\", \"export_as\": \"familienaam\"},{\"name\":\"user_metadata.straatEnHuisnummer\", \"export_as\": \"straatEnHuisnummer\"},{\"name\":\"user_metadata.postcode\", \"export_as\": \"postcode\"},{\"name\":\"user_metadata.stad\", \"export_as\": \"stad\"},{\"name\":\"user_metadata.land\", \"export_as\": \"land\"},{\"name\":\"user_metadata.bedrijfsnaam\", \"export_as\": \"bedrijfsnaam\"},{\"name\":\"user_metadata.ondernemingsnummer\", \"export_as\": \"ondernemingsnummer\"},{\"name\":\"user_metadata.rijksregisternummer\", \"export_as\": \"rijksregisternummer\"}]}";
        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        String token = Auth.getToken();
        headers.add("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = template.exchange(
                "https://dev-yujsewbo20nj6zio.eu.auth0.com/api/v2/jobs/users-exports",
                HttpMethod.POST,
                requestEntity,
                String.class
        );
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            JSONObject json = new JSONObject(response.getBody());
            String job = json.getString("id");
            String status = json.getString("status");
            while (status.equals("pending")){
                try{
                    Thread.sleep(1000);
                }catch (InterruptedException e) {
                    throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT);
                }
                status = getStatus(job, token);
            }
            if(status.equals("completed")){
                return getLocation(job, token);
            }
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT);
        }
        else{
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT);
        }

    }
}
