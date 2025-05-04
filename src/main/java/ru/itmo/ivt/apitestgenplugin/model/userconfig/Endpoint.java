package ru.itmo.ivt.apitestgenplugin.model.userconfig;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Endpoint {
    @JsonProperty(required = true)
    private String path;
    private String method;
    @JsonProperty(required = true)
    private String field;
}
