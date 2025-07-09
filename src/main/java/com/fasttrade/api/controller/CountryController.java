package com.fasttrade.api.controller;

import com.fasttrade.api.model.dto.CountryDTO;
import com.fasttrade.api.service.CountryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/country")
@Tag(name = "Países", description = "Lista de países disponíveis para cadastro")
public class CountryController {
    @Autowired
    private CountryService countryService;

    @GetMapping("/all")
    @Operation(summary = "Listar países", description = "Retorna a lista de países disponíveis para seleção.")
    public ResponseEntity<List<CountryDTO>> getAllCountries() {
        return ResponseEntity.ok(countryService.getAllCountries());
    }
}
