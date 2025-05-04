package ru.itmo.ivt.apitestgenplugin.model.userconfig;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Binding {
    @JsonProperty(required = true)
    private List<String> name;
    @JsonProperty(value = "create", required = true)
    private Endpoint createEndpoint;
    @JsonProperty(value = "delete")
    private Endpoint deleteEndpoint;
}