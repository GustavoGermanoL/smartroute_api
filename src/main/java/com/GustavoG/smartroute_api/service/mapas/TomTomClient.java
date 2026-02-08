package com.GustavoG.smartroute_api.service.mapas;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "tomtomClient", url = "${tomtom.host}")
public interface TomTomClient {

    // Mantemos o antigo caso precise
    @GetMapping("/search/2/geocode/{query}.json")
    String buscarEndereco(@PathVariable("query") String query, @RequestParam("key") String apiKey, @RequestParam("limit") int limit, @RequestParam("countrySet") String countrySet);

    // NOVO: Busca Estruturada (Muito mais preciso)
    // Doc: https://developer.tomtom.com/search-api/documentation/structured-geocode
    @GetMapping("/search/2/structuredGeocode.json")
    String buscarEnderecoEstruturado(
        @RequestParam("key") String apiKey,
        @RequestParam("countryCode") String countryCode, // "BR"
        @RequestParam("limit") int limit,
        @RequestParam("streetNumber") String numero,
        @RequestParam("streetName") String rua,
        @RequestParam("municipality") String cidade,
        @RequestParam("postalCode") String cep // Opcional, mas ajuda muito
    );
}