package br.com.unipds.cotubify.gerador.controller;

import br.com.unipds.cotubify.gerador.dto.GeracaoEbookRequest;
import br.com.unipds.cotubify.gerador.service.GeradorEbookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gerador")
public class GeradorController {

    private final GeradorEbookService service;

    public GeradorController(GeradorEbookService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> acionarGeracaoManualmente(@RequestBody GeracaoEbookRequest request) {
        service.gerarPdfEEpub(request);

        return ResponseEntity.accepted().build();
    }
}
