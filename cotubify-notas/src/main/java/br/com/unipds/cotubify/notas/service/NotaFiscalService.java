package br.com.unipds.cotubify.notas.service;

import br.com.unipds.cotubify.notas.dto.PedidoDTO;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@ApplicationScoped
public class NotaFiscalService {

    public String gerarNotaFiscal(PedidoDTO pedido) {
        BigDecimal total = pedido.itens().stream()
                .map(item -> item.precoCompra().subtract(item.desconto()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String descricaoItens = pedido.itens().stream()
                .map(item -> "- %s (%d)".formatted(item.tituloEbook(), item.ebookId()) )
                .collect(Collectors.joining("\n    "));

        String xml = """
                <notaFiscal>
                  <pedidoId>%d</pedidoId>
                  <cliente>
                    <nome>%s</nome>
                    <cpf>%s</cpf>
                  </cliente>
                  <valorTotal>%s</valorTotal>
                  <descricao>
                    %s
                  </descricao>
                </notaFiscal>
                """.formatted(pedido.id(), pedido.nomeCliente(), pedido.cpfCliente(), total, descricaoItens);

        System.out.println(xml);

        return xml;
    }

}
