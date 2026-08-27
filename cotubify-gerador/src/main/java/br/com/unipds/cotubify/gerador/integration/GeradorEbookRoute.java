package br.com.unipds.cotubify.gerador.integration;

import br.com.unipds.cotubify.gerador.dto.GeracaoEbookRequest;
import br.com.unipds.cotubify.gerador.service.GeradorEbookService;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws2.s3.AWS2S3Constants;
import org.apache.camel.model.ClaimCheckOperation;
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

                    in.setHeader("ebookId",     geracaoEbookRequest.ebookId());
                    in.setHeader("tituloEbook", geracaoEbookRequest.tituloEbook());

                })

                .log("Ebook ${header.ebookId} - ${header.tituloEbook}")

                .claimCheck(ClaimCheckOperation.Push)

                .bean(geradorEbookService, "gerarPdf")
                .setHeader(AWS2S3Constants.KEY, simple("pedidos/${header.pedidoId}/${header.ebookId}.pdf"))
                .setHeader(AWS2S3Constants.CONTENT_TYPE, constant("application/pdf"))
                .to("{{cotubify.s3.uri}}")
                .log("PDF no S3: ${header.CamelAwsS3Key}")

                .claimCheck(ClaimCheckOperation.Pop)

                .claimCheck(ClaimCheckOperation.Push)

                .bean(geradorEbookService, "gerarEpub")
                .setHeader(AWS2S3Constants.KEY, simple("pedidos/${header.pedidoId}/${header.ebookId}.epub"))
                .setHeader(AWS2S3Constants.CONTENT_TYPE, constant("application/epub+zip"))
                .to("{{cotubify.s3.uri}}")
                .log("EPUB no S3: ${header.CamelAwsS3Key}")

                .claimCheck(ClaimCheckOperation.Pop)

                .setHeader("To",      simple("${header.emailCliente}"))
                .setHeader("Subject", simple("Seu ebook \"${header.tituloEbook}\" está pronto!"))
                .setBody(simple("""
                   <h2>Olá, ${header.nomeCliente}!</h2>
                   <p>O ebook <strong>${header.tituloEbook}</strong> do pedido
                      <strong>#${header.pedidoId}</strong> está pronto para download:</p>
                   <ul>
                     <li><a href="{{cotubify.s3.url-publica}}/pedidos/${header.pedidoId}/${header.ebookId}.pdf">Baixar PDF</a></li>
                     <li><a href="{{cotubify.s3.url-publica}}/pedidos/${header.pedidoId}/${header.ebookId}.epub">Baixar EPUB</a></li>
                   </ul>
                   <p>Obrigado por comprar na Cotubify!</p>
                """))
                .to("{{cotubify.smtp.uri}}")

                .log("E-mail enviado para ${header.To}")

                .end()
                .log("Pedido ${header.pedidoId} processado com sucesso. Todos os ebooks gerados.");
    }
}
