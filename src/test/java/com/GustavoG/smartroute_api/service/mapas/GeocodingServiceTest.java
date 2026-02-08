package com.GustavoG.smartroute_api.service.mapas;

import com.GustavoG.smartroute_api.domain.EnderecoRaw;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class GeocodingServiceTest {

    @Autowired
    private GeocodingService geocodingService;

    @Test
    public void deveEncontrarCoordenadasDeSorocaba() {
        // Cria um endereço fictício só para testar a API
        EnderecoRaw enderecoTeste = new EnderecoRaw(
            "123", 
            "Rua Vital de Mello", // Uma avenida famosa de Sorocaba
            "405", 
            "", 
            "18090055", 
            "C"
        );

        System.out.println("--- INICIANDO BUSCA NO OPENROUTESERVICE ---");
        double[] coordenadas = geocodingService.obterCoordenadas(enderecoTeste);

        if (coordenadas != null) {
            System.out.println("SUCESSO! Coordenadas encontradas:");
            System.out.println("Latitude: " + coordenadas[0]);
            System.out.println("Longitude: " + coordenadas[1]);
        } else {
            System.err.println("ERRO: Não conseguiu trazer as coordenadas. Verifique a chave.");
        }
    }
}