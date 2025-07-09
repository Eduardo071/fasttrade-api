package com.fasttrade.api.controller;

import com.fasttrade.api.model.dto.ExchangeOptionDTO;
import com.fasttrade.api.model.dto.TradeIntentionRequestDTO;
import com.fasttrade.api.model.dto.TradeIntentionResponseDTO;
import com.fasttrade.api.service.TradeIntentionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trade-intention")
@Tag(name = "Intenção de Troca", description = "Criação e consulta de intenções de troca de moedas")
public class TradeIntentionController {
    @Autowired
    private TradeIntentionService tradeIntentionService;

    @PostMapping("/create")
    @Operation(summary = "Criar intenção de troca", description = "Cria uma nova intenção e tenta encontrar um match.")
    public ResponseEntity<TradeIntentionResponseDTO> createTradeIntention(@RequestHeader("Authorization") String bearerToken, @RequestBody TradeIntentionRequestDTO trade) {
        String token = bearerToken.replace("Bearer ", "");
        return ResponseEntity.ok(tradeIntentionService.createTradeIntention(trade, token));
    }

    @PostMapping("/trade-values")
    @Operation(summary = "Valores de troca possíveis", description = "Retorna opções válidas para a troca entre duas moedas.")
    public ResponseEntity<List<ExchangeOptionDTO>> getTradeValues(@RequestHeader("Authorization") String bearerToken, @RequestBody TradeIntentionRequestDTO trade) {
        String token = bearerToken.replace("Bearer ", "");
        return ResponseEntity.ok(tradeIntentionService.getTradeValues(trade, token));
    }
}
