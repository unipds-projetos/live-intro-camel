package br.com.unipds.cotubify.notas.dto;

import java.math.BigDecimal;

public record ItemPedidoDTO(
        Long ebookId,
        String tituloEbook,
        BigDecimal precoCompra,
        BigDecimal desconto
) { }
