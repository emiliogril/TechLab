package com.techlab.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.techlab.backend.exception.ProductoNoEncontradoException;
import com.techlab.backend.producto.Producto;
import com.techlab.backend.repository.CategoriaRepository;
import com.techlab.backend.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
public class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto producto;

    @BeforeEach
    void setUp() {
        producto = new Producto("Teclado Mecánico", "RGB Switch Blue", "Periféricos", 35000.0, "img/teclado.jpg", 12);
        producto.setId(1L);
        producto.setStockMinimo(5);
    }

    @Test
    void testBuscarProductoPorIdExistente() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        Producto resultado = productoService.buscarProductoPorId(1L);
        assertNotNull(resultado);
        assertEquals("Teclado Mecánico", resultado.getNombre());
    }

    @Test
    void testBuscarProductoPorIdInexistenteLanzaExcepcion() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductoNoEncontradoException.class, () -> {
            productoService.buscarProductoPorId(99L);
        });
    }

    @Test
    void testActualizarProducto() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(categoriaRepository.existsByNombreIgnoreCase(anyString())).thenReturn(true);

        Producto nuevo = new Producto("Teclado Mecánico V2", "RGB Switch Red", "Periféricos", 40000.0, "img/teclado2.jpg", 15);
        nuevo.setStockMinimo(6);

        Producto actualizado = productoService.actualizarProducto(1L, nuevo);

        assertEquals("Teclado Mecánico V2", actualizado.getNombre());
        assertEquals(40000.0, actualizado.getPrecio());
        assertEquals(15, actualizado.getStock());
        assertEquals(6, actualizado.getStockMinimo());
    }
}
