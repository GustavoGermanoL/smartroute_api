package com.GustavoG.smartroute_api.repository;

import com.GustavoG.smartroute_api.domain.Entrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EntregaRepository extends JpaRepository<Entrega, Long> {
    // Para verificar se já importamos esse pacote antes
    boolean existsByCodigoPacote(String codigoPacote);
}