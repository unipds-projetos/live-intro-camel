package br.com.unipds.cotubify.gerador.service;

import br.com.unipds.cotubify.gerador.dto.GeracaoEbookRequest;
import com.itextpdf.html2pdf.HtmlConverter;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubWriter;
import nl.siegmann.epublib.service.MediatypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class GeradorEbookService {

    private static final Logger log = LoggerFactory.getLogger(GeradorEbookService.class);

    public void gerarPdfEEpub(GeracaoEbookRequest request) throws IOException {
        String info = """
                =====================================================
                ⚙️ GERANDO EBOOK
                📦 Pedido Ref: %d
                👤 Cliente : %s (%s)
                📚 Título: %s
                🔗 Clonando repositório de origem: %s
                =====================================================
                """.formatted(request.pedidoId(), request.nomeCliente(), request.cpfCliente(), request.tituloEbook(), request.urlRepositorioGit());

        System.out.println(info);

        Path pasta = Path.of("target", "ebooks", String.valueOf(request.pedidoId()));
        Files.createDirectories(pasta);


        Path pdf = pasta.resolve(request.ebookId() + ".pdf");
        Path epub = pasta.resolve(request.ebookId() + ".epub");
        Files.write(pdf, gerarPdf(request));
        Files.write(epub, gerarEpub(request));


        System.out.printf("✅ Gerados: %s (%d bytes) e %s (%d bytes)\n",
                pdf, Files.size(pdf), epub, Files.size(epub));

    }

    private String html(GeracaoEbookRequest req) {
        return """
           <html xmlns="http://www.w3.org/1999/xhtml">
             <head><title>%s</title></head>
             <body>
               <h1>%s</h1>
               <p>Licenciado para <strong>%s</strong> — CPF %s</p>
               <p>Pedido #%d — Cotubify</p>
               <h2>Capítulo 1</h2>
               <p>Era uma vez um sistema que não conversava com os outros...</p>
             </body>
           </html>
           """.formatted(req.tituloEbook(), req.tituloEbook(),
                req.nomeCliente(), req.cpfCliente(), req.pedidoId());
    }

    public byte[] gerarPdf(GeracaoEbookRequest req) {
        var out = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(html(req), out);
        return out.toByteArray();
    }

    public byte[] gerarEpub(GeracaoEbookRequest req) throws IOException {
        var epub = new Book();
        epub.getMetadata().addTitle(req.tituloEbook());
        epub.getMetadata().addAuthor(new Author("Cotubify"));
        epub.addSection("Capítulo 1",
                new Resource(html(req).getBytes(UTF_8), MediatypeService.XHTML));
        var out = new ByteArrayOutputStream();
        new EpubWriter().write(epub, out);
        return out.toByteArray();
    }



}
