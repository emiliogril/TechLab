package com.techlab.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techlab.backend.categoria.Categoria;
import com.techlab.backend.exception.CategoriaNoEncontradaException;
import com.techlab.backend.producto.Producto;
import com.techlab.backend.repository.CategoriaRepository;
import com.techlab.backend.repository.ProductoRepository;

import jakarta.annotation.PostConstruct;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    @PostConstruct
    public void inicializarCategoriasPorDefecto() {
        // Aseguramos que existan las categorías base del proyecto si la tabla está vacía
        if (categoriaRepository.count() == 0) {
            categoriaRepository.save(new Categoria("Tecnología"));
            categoriaRepository.save(new Categoria("Periféricos"));
        }
    }

    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll();
    }

    public Categoria buscarCategoriaPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNoEncontradaException(
                        "Categoría con ID " + id + " no encontrada"));
    }

    public Categoria buscarCategoriaPorNombre(String nombre) {
        return categoriaRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new CategoriaNoEncontradaException(
                        "Categoría con nombre '" + nombre + "' no encontrada"));
    }

    @Transactional
    public Categoria guardarCategoria(Categoria categoria) {
        if (categoria.getNombre() == null || categoria.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío");
        }

        String nombreNormalizado = categoria.getNombre().trim();

        if (categoriaRepository.existsByNombreIgnoreCase(nombreNormalizado)) {
            throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + nombreNormalizado);
        }

        categoria.setNombre(nombreNormalizado);
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria actualizarCategoria(Long id, Categoria categoriaActualizada) {
        if (categoriaActualizada.getNombre() == null || categoriaActualizada.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío");
        }

        Categoria existente = buscarCategoriaPorId(id);
        String nuevoNombre = categoriaActualizada.getNombre().trim();
        String nombreAnterior = existente.getNombre();

        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(nuevoNombre, id)) {
            throw new IllegalArgumentException("Ya existe otra categoría con el nombre: " + nuevoNombre);
        }

        existente.setNombre(nuevoNombre);
        Categoria guardada = categoriaRepository.save(existente);

        // Si el nombre cambió, actualizamos los productos vinculados para mantener coherencia
        if (!nombreAnterior.equalsIgnoreCase(nuevoNombre)) {
            List<Producto> productos = productoRepository.findByCategoriaIgnoreCase(nombreAnterior);
            for (Producto producto : productos) {
                producto.setCategoria(nuevoNombre);
                productoRepository.save(producto);
            }
        }

        return guardada;
    }

    @Transactional
    public void eliminarCategoria(Long id) {
        Categoria categoria = buscarCategoriaPorId(id);

        // Validar si la categoría está en uso por algún producto
        List<Producto> productos = productoRepository.findByCategoriaIgnoreCase(categoria.getNombre());
        if (!productos.isEmpty()) {
            throw new IllegalStateException(
                    "No se puede eliminar la categoría '" + categoria.getNombre()
                            + "' porque está siendo utilizada por " + productos.size() + " producto(s).");
        }

        categoriaRepository.delete(categoria);
    }
}
