package com.GustavoG.smartroute_api.controller;

import com.GustavoG.smartroute_api.domain.Entrega;
import com.GustavoG.smartroute_api.repository.EntregaRepository;
import com.GustavoG.smartroute_api.service.EntregaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/entregas")
@RequiredArgsConstructor
public class EntregaController {

    private final EntregaService entregaService;
    private final EntregaRepository entregaRepository;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadPdf(@RequestParam("file") MultipartFile file) {
        try {
            // Chama nosso serviço que lê, geocodifica e salva
            String resultado = entregaService.processarArquivo(file);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao processar arquivo: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Entrega>> listarTodas() {
        // Endpoint simples só para validar a persistência
        return ResponseEntity.ok(entregaRepository.findAll());
    }
}