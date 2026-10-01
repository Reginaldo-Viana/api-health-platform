package com.apihealth.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class TargetRequest {

    @NotBlank
    @Size(max = 120)
    private String name;

    @NotBlank
    @Size(max = 1000)
    @Pattern(regexp = "https?://.+", message = "A URL deve começar com http:// ou https://")
    private String url;

    @NotBlank
    @Pattern(regexp = "(?i)GET|HEAD", message = "O método deve ser GET ou HEAD")
    private String method = "GET";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}