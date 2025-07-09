package com.fasttrade.api.controller;

import com.fasttrade.api.model.dto.CashoutRequestDTO;
import com.fasttrade.api.model.dto.CashoutResponseDTO;
import com.fasttrade.api.service.CashoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cashout")
@Tag(name = "Saque", description = "Registrar solicitações de saque para o Pix")
public class CashoutController {
    @Autowired
    private CashoutService cashoutService;

    @PostMapping("/register")
    @Operation(summary = "Solicitar saque", description = "Registra um pedido de saque em uma moeda específica.")
    public ResponseEntity<CashoutResponseDTO> postCashout(
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody CashoutRequestDTO cashoutRequestDTO
    ) throws Exception {
        String token = bearerToken.replace("Bearer ", "");
        return ResponseEntity.ok(cashoutService.postCashout(token, cashoutRequestDTO));
    }
}
