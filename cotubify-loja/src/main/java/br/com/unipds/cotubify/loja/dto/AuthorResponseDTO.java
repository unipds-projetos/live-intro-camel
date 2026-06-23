package br.com.unipds.cotubify.loja.dto;

import br.com.unipds.cotubify.loja.entity.Author;

public record AuthorResponseDTO(Long id, String name, String miniBio) {
    public static AuthorResponseDTO fromEntity(Author author) {
        return new AuthorResponseDTO(author.getId(), author.getName(), author.getMiniBio());
    }
}
