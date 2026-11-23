package com.techlab.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.techlab.backend.categoria.Categoria;
import com.techlab.backend.exception.CategoriaNoEncontradaException;
import com.techlab.backend.producto.Producto;
import com.techlab.backend.repository.CategoriaRepository;
import com.techlab.backend.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
public class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private Categoria categoria;

    @BeforeEach
    void setUp() {
        categoria = new Categoria("Tecnología");
        categoria.setId(1L);
    }

    @Test
    void testListarCategorias() {
        when(categoriaRepository.findAll()).thenReturn(List.of(categoria));

        List<Categoria> resultado = categoriaService.listarCategorias();
        assertEquals(1, resultado.size());
        assertEquals("Tecnología", resultado.get(0).getNombre());
    }

    @Test
    void testBuscarCategoriaPorIdExistente() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        Categoria resultado = categoriaService.buscarCategoriaPorId(1L);
        assertNotNull(resultado);
        assertEquals("Tecnología", resultado.getNombre());
    }

    @Test
    void testBuscarCategoriaPorIdInexistenteLanzaExcepcion() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CategoriaNoEncontradaException.class, () -> {
            categoriaService.buscarCategoriaPorId(99L);
        });
    }

    @Test
    void testGuardarCategoriaNueva() {
        when(categoriaRepository.existsByNombreIgnoreCase("Audio")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> {
            Categoria c = invocation.getArgument(0);
            c.setId(2L);
            return c;
        });

        Categoria guardada = categoriaService.guardarCategoria(new Categoria("Audio"));
        assertNotNull(guardada);
        assertEquals("Audio", guardada.getNombre());
        verify(categoriaRepository, times(1)).save(any(Categoria.class));
    }

    @Test
    void testGuardarCategoriaDuplicadaLanzaExcepcion() {
        when(categoriaRepository.existsByNombreIgnoreCase("Tecnología")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            categoriaService.guardarCategoria(new Categoria("Tecnología"));
        });
    }

    @Test
    void testGuardarCategoriaNombreVacioLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            categoriaService.guardarCategoria(new Categoria("   "));
        });
    }

    @Test
    void testActualizarCategoriaRenombraYActualizaProductos() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNombreIgnoreCaseAndIdNot("Tecnología Pro", 1L)).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Producto p = new Producto("Notebook", "Desc", "Tecnología", 1000.0, "", 5);
        List<Producto> productos = new ArrayList<>();
        productos.add(p);
        when(productoRepository.findByCategoriaIgnoreCase("Tecnología")).thenReturn(productos);

        Categoria actualizada = categoriaService.actualizarCategoria(1L, new Categoria("Tecnología Pro"));

        assertEquals("Tecnología Pro", actualizada.getNombre());
        assertEquals("Tecnología Pro", p.getCategoria());
        verify(productoRepository, times(1)).save(p);
    }

    @Test
    void testEliminarCategoriaSinProductosAsociadosPermiteBorrar() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.findByCategoriaIgnoreCase("Tecnología")).thenReturn(Collections.emptyList());

        categoriaService.eliminarCategoria(1L);
        verify(categoriaRepository, times(1)).delete(categoria);
    }

    @Test
    void testEliminarCategoriaConProductosAsociadosLanzaExcepcion() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        Producto p = new Producto("Notebook", "Desc", "Tecnología", 1000.0, "", 5);
        when(productoRepository.findByCategoriaIgnoreCase("Tecnología")).thenReturn(List.of(p));

        assertThrows(IllegalStateException.class, () -> {
            categoriaService.eliminarCategoria(1L);
        });
        verify(categoriaRepository, never()).delete(any());
    }
}
