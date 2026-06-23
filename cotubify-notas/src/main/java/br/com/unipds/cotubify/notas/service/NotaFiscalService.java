package br.com.unipds.cotubify.notas.service;

import br.com.unipds.cotubify.notas.dto.PedidoDTO;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;

@ApplicationScoped
public class NotaFiscalService {

    public String gerarNotaFiscal(PedidoDTO pedido) {
        BigDecimal total = pedido.itens().stream()
                .map(item -> item.precoCompra().subtract(item.desconto()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String xml = """
                <notaFiscal>
                  <pedidoId>%d</pedidoId>
                  <cliente>
                    <nome>%s</nome>
                    <cpf>%s</cpf>
                  </cliente>
                  <valorTotal>%s</valorTotal>
                </notaFiscal>
                """.formatted(pedido.id(), pedido.nomeCliente(), pedido.cpfCliente(), total);

        System.out.println(xml);

        return xml;
    }

}
