package br.com.unipds.cotubify.loja.dto;

import br.com.unipds.cotubify.loja.entity.Ebook;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

public record EbookResponseDTO(
        Long id,
        String title,
        String description,
        String imageUrl,
        BigDecimal price,
        Integer pageCount,
        String isbn,
        LocalDate publicationDate,
        Set<AuthorResponseDTO> authors
) {
    public static EbookResponseDTO fromEntity(Ebook ebook) {
        return new EbookResponseDTO(
                ebook.getId(),
                ebook.getTitle(),
                ebook.getDescription(),
                ebook.getImageUrl(),
                ebook.getPrice(),
                ebook.getPageCount(),
                ebook.getIsbn(),
                ebook.getPublicationDate(),
                ebook.getAuthors().stream()
                        .map(AuthorResponseDTO::fromEntity)
                        .collect(Collectors.toSet())
        );
    }
}
