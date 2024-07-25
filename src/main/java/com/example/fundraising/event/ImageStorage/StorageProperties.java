package com.example.fundraising.event.ImageStorage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

@Component //dit er moeten bij zetten => anders error
@ConfigurationProperties("storage")
public class StorageProperties {

    private String location = Paths.get("images").toAbsolutePath().toString();

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
}
