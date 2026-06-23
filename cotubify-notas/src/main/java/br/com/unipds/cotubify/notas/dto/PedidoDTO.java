package br.com.unipds.cotubify.notas.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PedidoDTO(
        Long id,
        String nomeCliente,
        String cpfCliente,
        String enderecoCliente,
        LocalDateTime dataCriacao,
        List<ItemPedidoDTO> itens
) { }
