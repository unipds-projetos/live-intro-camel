package br.com.unipds.cotubify.notas.rest;

import br.com.unipds.cotubify.notas.dto.PedidoDTO;
import br.com.unipds.cotubify.notas.service.NotaFiscalService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/notas")
@Produces(MediaType.APPLICATION_XML)
@Consumes(MediaType.APPLICATION_JSON)
public class NotaFiscalResource {

    @Inject
    NotaFiscalService notaFiscalService;

    @POST
    public Response receberPedidoManualmente(PedidoDTO pedido) {
        String notaFiscalXml = notaFiscalService.gerarNotaFiscal(pedido);
        return Response.ok(notaFiscalXml).build();
    }
}
