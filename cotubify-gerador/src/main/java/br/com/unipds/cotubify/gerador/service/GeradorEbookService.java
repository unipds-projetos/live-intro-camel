package br.com.unipds.cotubify.gerador.service;

import br.com.unipds.cotubify.gerador.dto.GeracaoEbookRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GeradorEbookService {

    private static final Logger log = LoggerFactory.getLogger(GeradorEbookService.class);

    public void gerarPdfEEpub(GeracaoEbookRequest request) {
        String info = """
                =====================================================
                ⚙️ GERANDO EBOOK
                📦 Pedido Ref: %d
                👤 Cliente : %s (%s)
                📚 Título: %s
                🔗 Clonando repositório de origem: %s
                =====================================================
                """.formatted(request.pedidoId(), request.nomeCliente(), request.cpfCliente(), request.tituloEbook(), request.urlRepositorioGit());

        System.out.println(info);

        try {
            // Simulando o tempo de compilação do PDF/EPUB (Asciidoctor, Pandoc, etc)
            Thread.sleep(2000);
            System.out.println("✅ Arquivos .pdf e .epub gerados com sucesso na pasta temporária!");

        } catch (InterruptedException e) {
            log.error("Erro durante a geração do ebook", e);
            Thread.currentThread().interrupt();
        }
    }
}
