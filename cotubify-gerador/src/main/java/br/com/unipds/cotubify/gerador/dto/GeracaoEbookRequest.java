package br.com.unipds.cotubify.gerador.dto;

public record GeracaoEbookRequest(
        Long pedidoId,
        String nomeCliente,
        String cpfCliente,
        Long ebookId,
        String tituloEbook,
        String urlRepositorioGit
) {}
