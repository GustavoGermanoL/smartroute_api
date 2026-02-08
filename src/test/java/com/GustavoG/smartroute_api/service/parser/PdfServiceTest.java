package com.GustavoG.smartroute_api.service.parser;

import com.GustavoG.smartroute_api.domain.EnderecoRaw;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class PdfServiceTest {

    @Test
    public void deveLerPdfMercadoLivre() throws IOException {
        MercadoLivrePdfService service = new MercadoLivrePdfService();
        
        // Aponte para o seu arquivo PDF real baixado
        String caminhoArquivo = "C:\\Users\\gusta\\OneDrive\\Área de Trabalho\\Rota Teste.pdf"; 
        
        FileInputStream input = new FileInputStream(caminhoArquivo);
        MockMultipartFile pdfFake = new MockMultipartFile("file", "Rota Teste.pdf", "application/pdf", input);

        List<EnderecoRaw> resultado = service.extrairEnderecos(pdfFake);

        System.out.println("==========================================");
        System.out.println("ENTREGAS ENCONTRADAS: " + resultado.size());
        System.out.println("==========================================");
        
        for (EnderecoRaw end : resultado) {
            System.out.printf("[%s] %s | %s, %s | CEP: %s | Detalhes: %s%n", 
                end.tipo(), end.idPacote(), end.logradouro(), end.numero(), end.cep(), end.complemento());
        }
    }
}