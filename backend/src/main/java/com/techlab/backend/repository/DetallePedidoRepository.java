package com.techlab.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.techlab.backend.pedido.DetallePedido;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

}