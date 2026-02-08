package com.GustavoG.smartroute_api.service.parser;

import com.GustavoG.smartroute_api.domain.EnderecoRaw;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MercadoLivrePdfService {

    // ESTRATÉGIA "BLINDADA" (ANTIGULOSA):
    // 1. (?<id>46\d{9})       -> Pega o ID
    // 2. (?<miolo>            -> Começa a pegar o conteúdo do meio
    //    (?:(?!46\d{9}).)*?   -> (O SEGREDO) Pega qualquer caractere, MAS PÁRA se ver outro ID.
    //                            Isso impede que o ID 1 "coma" o ID 2 se eles estiverem empilhados.
    //    )
    // 3. Sorocaba             -> Âncora de fim
    // 4. (?<cep>\d{8})        -> CEP
    // 5. (?<tipo>[CR])        -> Tipo
    private static final String REGEX_BLOCO = 
        "(?<id>46\\d{9})(?<miolo>(?:(?!46\\d{9}).)*?)Sorocaba\\s+(?<cep>\\d{8})\\s+(?<tipo>[CR])";

    // Regex auxiliar para separar número (aceita 1 a 5 digitos ou S/N)
    private static final Pattern REGEX_NUMERO = Pattern.compile("(\\s\\d{1,5}|\\sS/N|\\sSN)");

    public List<EnderecoRaw> extrairEnderecos(MultipartFile arquivo) throws IOException {
        try (PDDocument documento = Loader.loadPDF(arquivo.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            
            // MUDANÇA 1: Trocamos para false.
            // true = tenta recriar o visual (gera colunas separadas e causa o problema da pág 1).
            // false = lê na ordem de fluxo (linha a linha), aproximando o ID do Endereço.
            stripper.setSortByPosition(false); 
            
            String texto = stripper.getText(documento);
            // Removemos quebras de linha para tratar tudo como um fluxo contínuo
            String textoLinear = texto.replace("\n", " ").replace("\r", " ");
            
            return processarTexto(textoLinear);
        }
    }

    private List<EnderecoRaw> processarTexto(String texto) {
        List<EnderecoRaw> enderecos = new ArrayList<>();
        Pattern pattern = Pattern.compile(REGEX_BLOCO);
        Matcher matcher = pattern.matcher(texto);

        while (matcher.find()) {
            String id = matcher.group("id");
            String mioloSujo = matcher.group("miolo").trim();
            String cep = matcher.group("cep");
            String tipo = matcher.group("tipo");

            EnderecoTratado tratado = separarEnderecoENumero(mioloSujo);

            // Filtro de segurança: Se o 'miolo' ficou vazio (o ID estava sozinho), ignoramos.
            if (!tratado.rua.isEmpty()) {
                enderecos.add(new EnderecoRaw(
                    id, 
                    tratado.rua, 
                    tratado.numero, 
                    tratado.complemento, 
                    cep, 
                    tipo
                ));
            }
        }
        return enderecos;
    }

    private record EnderecoTratado(String rua, String numero, String complemento) {}

    private EnderecoTratado separarEnderecoENumero(String textoCompleto) {
        Matcher matcherNumero = REGEX_NUMERO.matcher(textoCompleto);

        if (matcherNumero.find()) {
            int inicioNumero = matcherNumero.start();
            int fimNumero = matcherNumero.end();

            String rua = textoCompleto.substring(0, inicioNumero).trim();
            String numero = matcherNumero.group().trim();
            String complemento = textoCompleto.substring(fimNumero).trim();

            if (rua.contains(",")) {
                String[] partes = rua.split(",");
                rua = partes[partes.length - 1].trim();
            }
            // Limpeza extra para remover palavras chaves que o PDFBox pode ter colado
            rua = rua.replaceAll("^(Cliente|Endereço)\\s*", "");

            return new EnderecoTratado(rua, numero, complemento);
        }

        return new EnderecoTratado(textoCompleto, "S/N", "");
    }
}