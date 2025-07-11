package com.fasttrade.api.controller;

import com.fasttrade.api.model.dto.UserAdditionalDataDTO;
import com.fasttrade.api.model.dto.UserResponseDTO;
import com.fasttrade.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user")
@Tag(name = "Usuário", description = "Endpoints para autenticação e dados do usuário")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @Operation(summary = "Login do usuário", description = "Realiza login e registra o token FCM.")
    public ResponseEntity<UserResponseDTO> login(
            @RequestBody UserResponseDTO data
    ) throws Exception {
        return ResponseEntity.ok(userService.authenticateAndGetUser(data));
    }

    @PostMapping("/register")
    @Operation(summary = "Registro de usuário", description = "Registra novo usuário e cria a carteira.")
    public ResponseEntity<UserResponseDTO> register(
            @RequestBody UserResponseDTO data
    ) throws Exception {
        return ResponseEntity.ok(userService.registerUser(data));
    }

    @PutMapping("/data-update")
    @Operation(summary = "Atualizar dados", description = "Atualiza dados do usuário autenticado.")
    public ResponseEntity<UserResponseDTO> updateUser(
            @RequestBody UserResponseDTO updateUserDTO
    ) throws Exception {
        return ResponseEntity.ok(userService.updateUser(updateUserDTO));
    }

}
