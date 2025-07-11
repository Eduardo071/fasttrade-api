package com.fasttrade.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TradeNotificationData {
    private String userId1;
    private String userId2;
    private String title;
    private String body;
}
