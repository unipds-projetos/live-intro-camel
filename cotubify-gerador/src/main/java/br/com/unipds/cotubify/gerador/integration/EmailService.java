package br.com.unipds.cotubify.gerador.integration;

import br.com.unipds.cotubify.gerador.dto.GeracaoEbookRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmailService {

    @Value("${cotubify.s3.url-publica}")
    private String urlPublica;

    public String montarCorpoDoEmail(List<GeracaoEbookRequest> ebooks) {
        String linhas = ebooks.stream()
                .map(e -> """
                       <li><strong>%1$s</strong> —
                         <a href="%2$s/pedidos/%3$d/%4$d.pdf">PDF</a> |
                         <a href="%2$s/pedidos/%3$d/%4$d.epub">EPUB</a></li>
                       """.formatted(e.tituloEbook(),
                        urlPublica, e.pedidoId(), e.ebookId()))
                .collect(Collectors.joining());


        return """
               <h2>Olá, %s!</h2>

               <p>Os %d ebooks do seu pedido #%d estão prontos:</p>
               <ul>%s</ul>

               <p>Obrigado por comprar na Cotubify!</p>
               """.formatted(ebooks.getFirst().nomeCliente(), ebooks.size(),
                ebooks.getFirst().pedidoId(), linhas);

    }

}
