package br.com.unipds.cotubify.loja.integration;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class OrderRoute extends RouteBuilder  {

    @Override
    public void configure() throws Exception {
        from("direct:new-order")
                .log("Camel recebeu um Order de id: ${body.id}")
                .marshal().json(JsonLibrary.Jackson)
                .to("spring-rabbitmq:cotubify.orders?routingKey=notas&queues=notas.queue&autoDeclareProducer=true")
                .log("Mensagem produzida com sucesso para o RabbitMQ.");
    }

}
