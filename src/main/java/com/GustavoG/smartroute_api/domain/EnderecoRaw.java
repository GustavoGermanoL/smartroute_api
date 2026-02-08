package com.GustavoG.smartroute_api.domain;

public record EnderecoRaw(
    String idPacote,      // Ex: 46401684881
    String logradouro,    // Ex: Avenida Comendador Camillo Júlio
    String numero,        // Ex: 2655
    String complemento,   // O que sobrar entre numero e cidade (Bairro entra aqui)
    String cep,           // Ex: 18086000
    String tipo           // C (Comercial) ou R (Residencial)
) {

}
