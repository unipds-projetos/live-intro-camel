package br.com.unipds.cotubify.loja.dto;

import java.math.BigDecimal;

public record OrderItemRequestDTO(
        Long ebookId,
        BigDecimal discount
) {}
