package com.GustavoG.smartroute_api.controller;

import com.GustavoG.smartroute_api.domain.Entrega;
import com.GustavoG.smartroute_api.repository.EntregaRepository;
import com.GustavoG.smartroute_api.service.EntregaService;
import com.GustavoG.smartroute_api.service.OtimizacaoService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private OtimizacaoService otimizacaoService; // Injete o novo service

    @PostMapping("/roteirizar")
    public ResponseEntity<String> criarRota() {
        String resultado = otimizacaoService.roteirizarEntregas();
        return ResponseEntity.ok(resultado);
    }
    
    // Atualize o listarTodas para ordenar pela rota!
    @GetMapping
    public ResponseEntity<List<Entrega>> listarTodas() {
        // Ordena por ordemNaRota (os nulls vão pro final)
        List<Entrega> lista = entregaRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "ordemNaRota"));
        return ResponseEntity.ok(lista);
    }

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

 
}