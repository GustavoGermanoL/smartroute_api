package com.GustavoG.smartroute_api.controller;

import com.GustavoG.smartroute_api.domain.Entrega;
import com.GustavoG.smartroute_api.domain.StatusEntrega;
import com.GustavoG.smartroute_api.repository.EntregaRepository;
import com.GustavoG.smartroute_api.service.EntregaService;
import com.GustavoG.smartroute_api.service.OtimizacaoService;
import com.GustavoG.smartroute_api.service.mapas.RouteService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/entregas")
@RequiredArgsConstructor
public class EntregaController {

    private final EntregaService entregaService;
    private final EntregaRepository entregaRepository;

    @Autowired
    private OtimizacaoService otimizacaoService; // Injete o novo service

    @Autowired
    private RouteService routeService;


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

    @GetMapping("/visualizar-rota")
    public ResponseEntity<String> getRotaVisual() {
        // Pega as entregas JÁ ordenadas (Passo importante!)
        List<Entrega> entregas = entregaRepository.findAll(Sort.by("ordemNaRota"));
        
        // Remove quem não foi roteirizado ainda
        entregas.removeIf(e -> e.getOrdemNaRota() == null);

        String geoJson = routeService.gerarGeoJsonReal(entregas);
        return ResponseEntity.ok(geoJson);
    }

    @DeleteMapping
    public ResponseEntity<String> limparBanco() {
        entregaRepository.deleteAll(); // Apaga tudo da tabela entregas
        return ResponseEntity.ok("Banco de dados limpo! Pode subir nova rota.");
    }

    // Atualizar latitude/longitude manualmente
    @PatchMapping("/{id}/coordenada")
    public ResponseEntity<Entrega> atualizarCoordenada(
            @PathVariable Long id, 
            @RequestBody Map<String, Double> novaPosicao) {
        
        Entrega entrega = entregaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entrega não encontrada"));
        
        entrega.setLatitude(novaPosicao.get("lat"));
        entrega.setLongitude(novaPosicao.get("lng"));
        entrega.setStatus(StatusEntrega.PENDENTE); // Marca como pendente para re-roteirizar
        
        return ResponseEntity.ok(entregaRepository.save(entrega));
    }
 
}