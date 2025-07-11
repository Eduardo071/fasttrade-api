package com.fasttrade.api.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAdditionalDataDTO {
    private String fullName;
    private String mainCurrency;
    private String countryId;
    private String fcmToken;
}
