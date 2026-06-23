package br.com.unipds.cotubify.loja.repository;

import br.com.unipds.cotubify.loja.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}