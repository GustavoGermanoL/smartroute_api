package com.GustavoG.smartroute_api.service;

import com.GustavoG.smartroute_api.domain.Entrega;
import com.GustavoG.smartroute_api.domain.StatusEntrega;
import com.GustavoG.smartroute_api.repository.EntregaRepository;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Solutions;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class OtimizacaoService {

    private final EntregaRepository entregaRepository;

    public String roteirizarEntregas() {
        // 1. Buscar entregas que ainda não foram roteirizadas (ou todas do dia)
        List<Entrega> entregas = entregaRepository.findAll(); 
        
        if (entregas.isEmpty()) return "Nenhuma entrega para roteirizar.";

        // 2. Configurar o Veículo (Sua Moto/Carro)
        VehicleType tipoVeiculo = VehicleTypeImpl.Builder.newInstance("moto")
                .addCapacityDimension(0, 200) // Cabe 200 pacotes
                .build();

        // PONTO DE PARTIDA: Defina a coordenada de onde você sai (Ex: Centro de Sorocaba)
        // Dica: Pegue a lat/long da sua casa no Google Maps e cole aqui
        Location deposito = Location.newInstance(-23.455121861863667, -47.4435298653351); 

        VehicleImpl veiculo = VehicleImpl.Builder.newInstance("minha-moto")
                .setStartLocation(deposito)
                .setType(tipoVeiculo)
                .build();

        // 3. Montar o Problema (Adicionar cada entrega como um "Serviço")
        VehicleRoutingProblem.Builder problemBuilder = VehicleRoutingProblem.Builder.newInstance();
        problemBuilder.addVehicle(veiculo);

        for (Entrega entrega : entregas) {
            // Só adiciona se tiver coordenadas válidas
            if (entrega.getLatitude() != null && entrega.getLongitude() != null) {
                Service service = Service.Builder.newInstance(String.valueOf(entrega.getId()))
                        .addSizeDimension(0, 1) // Cada pacote ocupa 1 espaço
                        .setLocation(Location.newInstance(entrega.getLatitude(), entrega.getLongitude()))
                        .build();
                problemBuilder.addJob(service);
            }
        }

        VehicleRoutingProblem problem = problemBuilder.build();

        // 4. Rodar o Algoritmo Mágico do Jsprit
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

        // 5. Salvar a ordem de volta no Banco
       int ordem = 1;
        for (VehicleRoute route : bestSolution.getRoutes()) {
            for (TourActivity activity : route.getActivities()) {
                
                // CORREÇÃO: Usamos TourActivity.JobActivity (Interface Interna)
                if (activity instanceof TourActivity.JobActivity) {
                    
                    // Cast correto
                    TourActivity.JobActivity jobActivity = (TourActivity.JobActivity) activity;
                    
                    // Agora conseguimos pegar o Job e o ID
                    String entregaIdStr = jobActivity.getJob().getId(); 
                    Long entregaId = Long.parseLong(entregaIdStr);

                    // Atualiza no banco
                    Entrega entregaAtualizada = entregaRepository.findById(entregaId).orElse(null);
                    if (entregaAtualizada != null) {
                        entregaAtualizada.setOrdemNaRota(ordem++);
                        entregaAtualizada.setStatus(StatusEntrega.ROTEIRIZADO);
                        entregaRepository.save(entregaAtualizada);
                    }
                }
            }
        }
        
        return "Roteirização concluída! Rota gerada com " + (ordem - 1) + " paradas.";
    }
}