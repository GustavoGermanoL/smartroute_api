package com.GustavoG.smartroute_api.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "entregas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // Lombok para facilitar a criação
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true) // Não queremos duplicar o mesmo pacote
    private String codigoPacote; // Aquele ID 46... do ML

    private String logradouro;
    private String numero;
    private String bairro;
    private String cidade; // Vamos fixar "Sorocaba" por enquanto
    private String cep;
    
    // As coordenadas preciosas que conseguimos
    private Double latitude;
    private Double longitude;

    private Integer ordemNaRota;

    private Double distanciaAteAqui;
    
    // Status para controle
    @Enumerated(EnumType.STRING)
    private StatusEntrega status; // PENDENTE, ROTEIRIZADO, ENTREGUE
}