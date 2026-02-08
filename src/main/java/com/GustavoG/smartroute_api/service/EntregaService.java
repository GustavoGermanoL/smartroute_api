package com.GustavoG.smartroute_api.service;

import com.GustavoG.smartroute_api.domain.EnderecoRaw;
import com.GustavoG.smartroute_api.domain.Entrega;
import com.GustavoG.smartroute_api.domain.StatusEntrega;
import com.GustavoG.smartroute_api.repository.EntregaRepository;
import com.GustavoG.smartroute_api.service.mapas.GeocodingService;
import com.GustavoG.smartroute_api.service.parser.MercadoLivrePdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor // Cria construtor automático para as injeções
public class EntregaService {

    private final MercadoLivrePdfService pdfService;
    private final GeocodingService geocodingService;
    private final EntregaRepository entregaRepository;

    public String processarArquivo(MultipartFile arquivo) throws IOException {
        // 1. Extrair do PDF
        List<EnderecoRaw> enderecosRaw = pdfService.extrairEnderecos(arquivo);
        int salvos = 0;
        int erros = 0;

        // 2. Para cada endereço...
        for (EnderecoRaw raw : enderecosRaw) {
            
            // Evita duplicatas (Se já importou hoje, pula)
            if (entregaRepository.existsByCodigoPacote(raw.idPacote())) {
                continue;
            }

            Entrega novaEntrega = new Entrega();
            novaEntrega.setCodigoPacote(raw.idPacote());
            novaEntrega.setLogradouro(raw.logradouro());
            novaEntrega.setNumero(raw.numero());
            novaEntrega.setCep(raw.cep());
            novaEntrega.setCidade("Sorocaba");

            // 3. Geocodificar com TomTom
            double[] coords = geocodingService.obterCoordenadas(raw);
            
            if (coords != null) {
                novaEntrega.setLatitude(coords[0]);
                novaEntrega.setLongitude(coords[1]);
                novaEntrega.setStatus(StatusEntrega.PENDENTE);
            } else {
                novaEntrega.setStatus(StatusEntrega.FALHA_GEOCODING);
                erros++;
            }

            // 4. Salvar no Banco
            entregaRepository.save(novaEntrega);
            salvos++;
            
            // Delayzinho para não espancar a API da TomTom (boa prática)
            try { Thread.sleep(200); } catch (InterruptedException e) {}
        }

        return String.format("Processamento finalizado. Salvos: %d. Falhas de Geo: %d", salvos, erros);
    }
}