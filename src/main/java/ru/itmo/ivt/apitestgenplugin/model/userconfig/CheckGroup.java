package ru.itmo.ivt.apitestgenplugin.model.userconfig;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CheckGroup {
    @JsonProperty("check_correct_values")
    private boolean checkCorrectValues;

    @JsonProperty("check_incorrect_values")
    private boolean checkIncorrectValues;

    @JsonProperty("check_required_fields")
    private boolean checkRequiredFields;

    @JsonProperty("check_authentication")
    private boolean checkAuthentication;
}