package com.GustavoG.smartroute_api.service.mapas;

import com.GustavoG.smartroute_api.domain.EnderecoRaw;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class GeocodingService {

    private final TomTomClient tomtomClient;
    private final String tomtomKey;
    private final ObjectMapper objectMapper;

    // Injeção de dependência via Construtor
    public GeocodingService(TomTomClient tomtomClient, 
                            @Value("${tomtom.key}") String tomtomKey,
                            ObjectMapper objectMapper) {
        this.tomtomClient = tomtomClient;
        this.tomtomKey = tomtomKey;
        this.objectMapper = objectMapper;
    }

   public double[] obterCoordenadas(EnderecoRaw endereco) {
        try {
            System.out.println("--- TOMTOM ESTRUTURADA ---");
            
            // Limpeza básica: Remove "Rua", "Av" se quiser, mas a TomTom lida bem com isso.
            // O importante é passar os campos separados.
            
            String jsonResposta = tomtomClient.buscarEnderecoEstruturado(
                tomtomKey,
                "BR",
                1,
                endereco.numero(),       // Ex: "155"
                endereco.logradouro(),   // Ex: "Avenida Itavuvu"
                "Sorocaba",              // Cidade Fixa (ou pegue do objeto se tiver)
                endereco.cep()           // Ex: "18000000"
            );

            JsonNode root = objectMapper.readTree(jsonResposta);
            JsonNode results = root.path("results");

            if (results.isArray() && results.size() > 0) {
                JsonNode melhorResultado = results.get(0);
                
                String tipoMatch = melhorResultado.path("type").asText();
                double score = melhorResultado.path("score").asDouble();
                String enderecoAchado = melhorResultado.path("address").path("freeformAddress").asText();
                
                System.out.println("Busca: " + endereco.logradouro() + ", " + endereco.numero());
                System.out.println("Achou: " + enderecoAchado);
                System.out.println("Qualidade: " + tipoMatch + " (Score: " + score + ")");
                
                JsonNode position = melhorResultado.path("position");
                return new double[]{position.path("lat").asDouble(), position.path("lon").asDouble()};
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}