package com.techlab.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.techlab.backend.pedido.Pedido;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByUsuarioId(Long usuarioId);

}