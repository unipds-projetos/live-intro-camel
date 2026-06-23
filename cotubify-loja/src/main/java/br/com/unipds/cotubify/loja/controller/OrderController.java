package br.com.unipds.cotubify.loja.controller;

import br.com.unipds.cotubify.loja.dto.OrderRequestDTO;
import br.com.unipds.cotubify.loja.entity.Ebook;
import br.com.unipds.cotubify.loja.entity.Order;
import br.com.unipds.cotubify.loja.entity.OrderItem;
import br.com.unipds.cotubify.loja.repository.EbookRepository;
import br.com.unipds.cotubify.loja.repository.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final EbookRepository ebookRepository;

    public OrderController(OrderRepository orderRepository, EbookRepository ebookRepository) {
        this.orderRepository = orderRepository;
        this.ebookRepository = ebookRepository;
    }

    @PostMapping
    @Transactional
    @ResponseStatus(HttpStatus.CREATED)
    public void createOrder(@RequestBody OrderRequestDTO request) {
        Order order = new Order();
        order.setClientName(request.clientName());
        order.setCpf(request.cpf());
        order.setEmail(request.email());
        order.setAddress(request.address());
        order.setItems(new ArrayList<>());

        request.items().forEach(itemDto -> {
            Ebook ebook = ebookRepository.findById(itemDto.ebookId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Ebook não encontrado: " + itemDto.ebookId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setEbook(ebook);
            orderItem.setPurchasePrice(ebook.getPrice());

            BigDecimal discount = itemDto.discount() != null ? itemDto.discount() : BigDecimal.ZERO;
            orderItem.setDiscount(discount);

            order.addItem(orderItem);
        });

        orderRepository.save(order);
    }
}
