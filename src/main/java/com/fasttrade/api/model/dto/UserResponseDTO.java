package com.fasttrade.api.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDTO extends UserAdditionalDataDTO {
    private String email;
    private String password;
    private String createdAt;
}
