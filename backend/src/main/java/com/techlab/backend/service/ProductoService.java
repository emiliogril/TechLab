package com.techlab.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.techlab.backend.categoria.Categoria;
import com.techlab.backend.exception.ProductoNoEncontradoException;
import com.techlab.backend.producto.Producto;
import com.techlab.backend.repository.CategoriaRepository;
import com.techlab.backend.repository.ProductoRepository;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    public Producto guardarProducto(Producto producto) {
        if (producto.getStockMinimo() == null) {
            producto.setStockMinimo(5);
        }

        // Asegurar que la categoría quede sincronizada en el catálogo de categorías
        if (producto.getCategoria() != null && !producto.getCategoria().trim().isEmpty()) {
            String cat = producto.getCategoria().trim();
            if (!categoriaRepository.existsByNombreIgnoreCase(cat)) {
                categoriaRepository.save(new Categoria(cat));
            }
            producto.setCategoria(cat);
        }

        return productoRepository.save(producto);
    }

    public Producto buscarProductoPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException(
                        "Producto con ID " + id + " no encontrado"));
    }

    public List<Producto> buscarProductoPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public List<Producto> buscarProductoPorCategoria(String categoria) {
        return productoRepository.findByCategoriaIgnoreCase(categoria);
    }

    public List<Producto> listarProductosConStockBajo() {
        return productoRepository.findByStockLessThanEqual(5);
    }

    public void eliminarProducto(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException(
                        "Producto con ID " + id + " no encontrado"));

        productoRepository.delete(producto);
    }

    public Producto actualizarProducto(Long id, Producto productoActualizado) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException(
                        "Producto con ID " + id + " no encontrado"));

        producto.setNombre(productoActualizado.getNombre());
        producto.setDescripcion(productoActualizado.getDescripcion());
        producto.setPrecio(productoActualizado.getPrecio());

        if (productoActualizado.getCategoria() != null && !productoActualizado.getCategoria().trim().isEmpty()) {
            String cat = productoActualizado.getCategoria().trim();
            if (!categoriaRepository.existsByNombreIgnoreCase(cat)) {
                categoriaRepository.save(new Categoria(cat));
            }
            producto.setCategoria(cat);
        }

        producto.setImagenUrl(productoActualizado.getImagenUrl());
        producto.setStock(productoActualizado.getStock());
        if (productoActualizado.getStockMinimo() != null) {
            producto.setStockMinimo(productoActualizado.getStockMinimo());
        }

        return productoRepository.save(producto);
    }
}
