package ru.itmo.ivt.apitestgenplugin.model.userconfig;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TestConfiguration {
    @JsonProperty("all")
    private CheckGroup all;

    @JsonProperty("post")
    private CheckGroup post;

    @JsonProperty("get")
    private CheckGroup get;

    @JsonProperty("delete")
    private CheckGroup delete;

    @JsonProperty("head")
    private CheckGroup head;

    @JsonProperty("put")
    private CheckGroup put;
}
