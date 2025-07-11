package com.fasttrade.api.controller;

import com.fasttrade.api.model.dto.WalletDetailsDTO;
import com.fasttrade.api.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallet")
@Tag(name = "Carteira", description = "Consulta de saldo e moedas do usuário")
public class WalletController {
    @Autowired
    private WalletService walletService;

    @GetMapping("/details")
    @Operation(summary = "Detalhes da carteira", description = "Retorna os saldos em todas as moedas.")
    public ResponseEntity<WalletDetailsDTO> getWalletDetails(
            @RequestHeader("email") String email
    ) throws Exception {
        return ResponseEntity.ok(walletService.getWalletDetails(email));
    }
}
