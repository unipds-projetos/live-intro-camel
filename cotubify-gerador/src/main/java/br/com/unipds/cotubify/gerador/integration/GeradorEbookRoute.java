package br.com.unipds.cotubify.gerador.integration;

import br.com.unipds.cotubify.gerador.dto.GeracaoEbookRequest;
import br.com.unipds.cotubify.gerador.service.GeradorEbookService;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GeradorEbookRoute extends RouteBuilder {

    private final GeradorEbookService geradorEbookService;

    public GeradorEbookRoute(GeradorEbookService geradorEbookService) {
        this.geradorEbookService = geradorEbookService;
    }

    @Override
    public void configure() throws Exception {
        from("spring-rabbitmq:cotubify.orders?queues=gerador.queue&autoDeclare=false")
                .unmarshal().json(JsonLibrary.Jackson)

                .setHeader("pedidoId", simple("${body[id]}")) // correlation identifier
                .setHeader("nomeCliente", simple("${body[clientName]}")) // correlation identifier
                .setHeader("cpfCliente", simple("${body[cpf]}")) // correlation identifier
                .setHeader("emailCliente", simple("${body[email]}")) // correlation identifier

                .log("Pedido ${header.pedidoId} vai ser processado.")

                .split(simple("${body[items]}")).parallelProcessing()
                .log("Gerador recebeu pedido: ${body}")
                .process(exchange -> {
                    Message in = exchange.getIn();
                    Map<String, Object> item = in.getBody(Map.class);
                    Map<String, Object> ebook = (Map<String, Object>) item.get("ebook");
                    GeracaoEbookRequest geracaoEbookRequest = new GeracaoEbookRequest(
                            in.getHeader("pedidoId", Long.class),
                            in.getHeader("nomeCliente", String.class),
                            in.getHeader("cpfCliente", String.class),
                            ((Number) ebook.get("id")).longValue(),
                            ((String) ebook.get("title")),
                            ((String) ebook.get("gitRepoUrl"))
                    );
                    in.setBody(geracaoEbookRequest);
                })
                .bean(geradorEbookService, "gerarPdfEEpub")
                .end()
                .log("Pedido ${header.pedidoId} processado com sucesso. Todos os ebooks gerados.");
    }
}
