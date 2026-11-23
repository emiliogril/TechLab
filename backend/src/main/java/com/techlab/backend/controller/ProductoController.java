package com.techlab.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.techlab.backend.producto.Producto;
import com.techlab.backend.service.ProductoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = {
        "http://127.0.0.1:5500",
        "http://localhost:5500"
})
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<Producto> listarProductos() {
        return productoService.listarProductos();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Producto guardarProducto(@Valid @RequestBody Producto producto) {
        return productoService.guardarProducto(producto);
    }

    @GetMapping("/{id}")
    public Producto buscarProductoPorId(@PathVariable Long id) {
        return productoService.buscarProductoPorId(id);
    }

    @GetMapping("/buscar")
    public List<Producto> buscarProductoPorNombre(@RequestParam String nombre) {
        return productoService.buscarProductoPorNombre(nombre);
    }

    @GetMapping("/categoria/{categoria}")
    public List<Producto> buscarProductoPorCategoria(@PathVariable String categoria) {
        return productoService.buscarProductoPorCategoria(categoria);
    }

    @GetMapping("/stock-bajo")
    public List<Producto> listarProductosConStockBajo() {
        return productoService.listarProductosConStockBajo();
    }

    @DeleteMapping("/{id}")
    public void eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
    }

    @PutMapping("/{id}")
    public Producto actualizarProducto(@PathVariable Long id, @Valid @RequestBody Producto producto) {
        return productoService.actualizarProducto(id, producto);
    }
}
