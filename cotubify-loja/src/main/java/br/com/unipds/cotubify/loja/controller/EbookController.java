package br.com.unipds.cotubify.loja.controller;

import br.com.unipds.cotubify.loja.dto.EbookResponseDTO;
import br.com.unipds.cotubify.loja.repository.EbookRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ebooks")
public class EbookController {

    private final EbookRepository ebookRepository;

    public EbookController(EbookRepository ebookRepository) {
        this.ebookRepository = ebookRepository;
    }

    @GetMapping
    public List<EbookResponseDTO> listAll() {
        return ebookRepository.findAllWithAuthors().stream()
                .map(EbookResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}