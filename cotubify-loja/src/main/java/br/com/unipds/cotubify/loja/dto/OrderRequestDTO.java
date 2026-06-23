package br.com.unipds.cotubify.loja.dto;

import java.util.List;

public record OrderRequestDTO(
        String clientName,
        String cpf,
        String email,
        String address,
        List<OrderItemRequestDTO> items
) {}