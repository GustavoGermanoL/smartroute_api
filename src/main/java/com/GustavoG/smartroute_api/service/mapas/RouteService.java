package com.GustavoG.smartroute_api.service.mapas;

import com.GustavoG.smartroute_api.domain.Entrega;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class RouteService {

    @Value("${ors.token}")
    private String orsToken;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final int CHUNK_SIZE = 40;

    public String gerarGeoJsonReal(List<Entrega> entregasOrdenadas) {
        try {
            ObjectNode featureCollection = mapper.createObjectNode();
            featureCollection.put("type", "FeatureCollection");
            ArrayNode features = featureCollection.putArray("features");

            // 1. Adiciona os pinos (marcadores)
            for (Entrega e : entregasOrdenadas) {
                adicionarPonto(features, e);
            }

            // 2. Prepara a lista única para a linha azul
            // CORREÇÃO AQUI: Era List<List<...>>, agora é List<Double[]>
            List<Double[]> todasCoordenadasDaLinha = new ArrayList<>();

            // Loop pelos pedaços da rota
            for (int i = 0; i < entregasOrdenadas.size(); i += (CHUNK_SIZE - 1)) {
                int fim = Math.min(i + CHUNK_SIZE, entregasOrdenadas.size());
                List<Entrega> lote = entregasOrdenadas.subList(i, fim);

                if (lote.size() < 2) continue;

                List<Double[]> trechoReal = buscarTrajetoNoOrs(lote);
                
                if (trechoReal != null) {
                    // AGORA FUNCIONA: Estamos adicionando pontos numa lista de pontos
                    todasCoordenadasDaLinha.addAll(trechoReal);
                }
                
                Thread.sleep(1000); // Pausa para não bloquear a API
            }

            // 3. Desenha a linha completa
            adicionarLinhaDoTrajeto(features, todasCoordenadasDaLinha);

            return mapper.writeValueAsString(featureCollection);

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Falha ao gerar rota visual: " + e.getMessage() + "\"}";
        }
    }

    // --- MÉTODOS AUXILIARES (Mantenha igual ao anterior, só conferindo) ---

    private List<Double[]> buscarTrajetoNoOrs(List<Entrega> lote) {
        try {
            ObjectNode body = mapper.createObjectNode();
            ArrayNode coordinates = body.putArray("coordinates");
            
            for (Entrega e : lote) {
                if (e.getLatitude() != null) {
                    ArrayNode point = coordinates.addArray();
                    point.add(e.getLongitude());
                    point.add(e.getLatitude());
                }
            }

            // URL para carro
            String url = "https://api.openrouteservice.org/v2/directions/driving-car/geojson";
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", orsToken); // Usa o token injetado
            headers.set("Content-Type", "application/json");

            org.springframework.http.HttpEntity<String> entity = 
                new org.springframework.http.HttpEntity<>(body.toString(), headers);

            String response = restTemplate.postForObject(url, entity, String.class);
            
            JsonNode root = mapper.readTree(response);
            // Proteção contra NullPointer se a API falhar
            if (!root.has("features") || root.path("features").isEmpty()) return null;

            JsonNode geometry = root.path("features").get(0).path("geometry");
            JsonNode coordsNode = geometry.path("coordinates");

            List<Double[]> listaCoords = new ArrayList<>();
            if (coordsNode.isArray()) {
                for (JsonNode coord : coordsNode) {
                    listaCoords.add(new Double[]{
                        coord.get(0).asDouble(), 
                        coord.get(1).asDouble()
                    });
                }
            }
            return listaCoords;
        } catch (Exception e) {
            System.err.println("Erro ORS: " + e.getMessage());
            return null;
        }
    }

    private void adicionarPonto(ArrayNode features, Entrega e) {
        if (e.getLatitude() == null) return;
        
        ObjectNode feature = features.addObject();
        feature.put("type", "Feature");
        
        ObjectNode properties = feature.putObject("properties");
        properties.put("ordem", e.getOrdemNaRota());
        properties.put("endereco", e.getLogradouro() + ", " + e.getNumero());
        // Se quiser cor diferente para o primeiro e último
        String cor = "#555555";
        if (e.getOrdemNaRota() == 1) cor = "#00FF00"; // Verde partida

        properties.put("marker-color", cor);
        properties.put("marker-symbol", String.valueOf(e.getOrdemNaRota()));

        ObjectNode geometry = feature.putObject("geometry");
        geometry.put("type", "Point");
        ArrayNode coords = geometry.putArray("coordinates");
        coords.add(e.getLongitude());
        coords.add(e.getLatitude());
    }

    private void adicionarLinhaDoTrajeto(ArrayNode features, List<Double[]> coordenadas) {
        if (coordenadas == null || coordenadas.isEmpty()) return;

        ObjectNode feature = features.addObject();
        feature.put("type", "Feature");
        
        ObjectNode properties = feature.putObject("properties");
        properties.put("stroke", "#0000FF"); // Azul
        properties.put("stroke-width", 5);
        properties.put("stroke-opacity", 0.7);

        ObjectNode geometry = feature.putObject("geometry");
        geometry.put("type", "LineString");
        ArrayNode coordsArray = geometry.putArray("coordinates");

        for (Double[] coord : coordenadas) {
            ArrayNode ponto = coordsArray.addArray();
            ponto.add(coord[0]); 
            ponto.add(coord[1]); 
        }
    }
}