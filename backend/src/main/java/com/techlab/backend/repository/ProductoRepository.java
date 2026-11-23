package com.techlab.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techlab.backend.producto.Producto;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    List<Producto> findByCategoriaIgnoreCase(String categoria);

    List<Producto> findByStockLessThanEqual(Integer stock);

}