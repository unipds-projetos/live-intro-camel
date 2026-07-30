package br.com.unipds.cotubify.notas.integration;

import br.com.unipds.cotubify.notas.dto.ItemPedidoDTO;
import br.com.unipds.cotubify.notas.dto.PedidoDTO;
import br.com.unipds.cotubify.notas.service.NotaFiscalService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class NotaFiscalRoute extends RouteBuilder  {

    @Inject
    NotaFiscalService notaFiscalService;

    @Override
    public void configure() throws Exception {
        from("amqp:queue:notas.queue")
                .log("Nova mensagem de pedido recebido do RabbitMQ")
                .unmarshal(new JacksonDataFormat(JsonNode.class))
                .process(exchange -> {
                    Message exchangeIn = exchange.getIn();
                    JsonNode jsonNode = exchangeIn.getBody(JsonNode.class);
                    List<ItemPedidoDTO> itens = new ArrayList<>();

                    JsonNode itemsNode = jsonNode.get("items");
                    for (JsonNode itemNode : itemsNode) {
                        JsonNode ebookNode = itemNode.get("ebook");
                        itens.add(new ItemPedidoDTO(
                                ebookNode.get("id").asLong(),
                                ebookNode.get("title").asText(),
                                new BigDecimal(itemNode.get("purchasePrice").asText()),
                                new BigDecimal(itemNode.get("discount").asText())
                        ));
                    }

                    PedidoDTO pedidoDTO = new PedidoDTO(
                            jsonNode.get("id").asLong(),
                            jsonNode.get("clientName").asText(),
                            jsonNode.get("cpf").asText(),
                            jsonNode.get("address").asText(),
                            LocalDateTime.now(),
                            itens
                    );
                    exchangeIn.setBody(pedidoDTO);
                })
                .bean(notaFiscalService, "gerarNotaFiscal");
    }

}
