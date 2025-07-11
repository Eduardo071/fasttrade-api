package com.fasttrade.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchResponseDTO {
    private String id;
    private String fromIntentionId;
    private String toIntentionId;
    private String status;
    private String createdAt;
}
