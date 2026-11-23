package com.techlab.backend.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.techlab.backend.categoria.Categoria;
import com.techlab.backend.exception.CategoriaNoEncontradaException;
import com.techlab.backend.exception.GlobalExceptionHandler;
import com.techlab.backend.service.CategoriaService;

@ExtendWith(MockitoExtension.class)
public class CategoriaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CategoriaService categoriaService;

    @InjectMocks
    private CategoriaController categoriaController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoriaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testListarCategoriasRetorna200() throws Exception {
        Categoria cat = new Categoria("Tecnología");
        cat.setId(1L);
        when(categoriaService.listarCategorias()).thenReturn(List.of(cat));

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Tecnología"));
    }

    @Test
    void testBuscarPorIdInexistenteRetorna404() throws Exception {
        when(categoriaService.buscarCategoriaPorId(99L))
                .thenThrow(new CategoriaNoEncontradaException("Categoría no encontrada"));

        mockMvc.perform(get("/api/categorias/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testCrearCategoriaRetorna201() throws Exception {
        Categoria salida = new Categoria("Periféricos");
        salida.setId(2L);

        when(categoriaService.guardarCategoria(any(Categoria.class))).thenReturn(salida);

        mockMvc.perform(post("/api/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Periféricos\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.nombre").value("Periféricos"));
    }
}
