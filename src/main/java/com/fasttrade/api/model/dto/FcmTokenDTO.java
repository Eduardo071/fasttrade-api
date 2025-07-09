package com.fasttrade.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FcmTokenDTO {
    private String userId;
    private String fcmToken;
    private String updatedAt;
}
