package com.techlab.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techlab.backend.exception.PedidoNoEncontradoException;
import com.techlab.backend.exception.ProductoNoEncontradoException;
import com.techlab.backend.exception.StockInsuficienteException;
import com.techlab.backend.exception.UsuarioNoEncontradoException;
import com.techlab.backend.pedido.DetallePedido;
import com.techlab.backend.pedido.EstadoPedido;
import com.techlab.backend.pedido.Pedido;
import com.techlab.backend.producto.Producto;
import com.techlab.backend.repository.PedidoRepository;
import com.techlab.backend.repository.ProductoRepository;
import com.techlab.backend.repository.UsuarioRepository;
import com.techlab.backend.usuario.Usuario;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public PedidoService(
            PedidoRepository pedidoRepository,
            ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository) {

        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    public Pedido buscarPedidoPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNoEncontradoException(
                        "Pedido con ID " + id + " no encontrado"));
    }

    @Transactional
    public Pedido actualizarEstado(Long id, EstadoPedido nuevoEstado) {

        Pedido pedido = buscarPedidoPorId(id);

        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new IllegalStateException("Un pedido cancelado no puede cambiar de estado");
        }

        if (pedido.getEstado() == EstadoPedido.ENTREGADO) {
            throw new IllegalStateException("Un pedido entregado ya está en estado final y no puede cambiar de estado");
        }

        if (nuevoEstado == pedido.getEstado()) {
            return pedido;
        }

        // Transiciones desde PENDIENTE
        if (pedido.getEstado() == EstadoPedido.PENDIENTE) {
            if (nuevoEstado != EstadoPedido.CONFIRMADO && nuevoEstado != EstadoPedido.CANCELADO) {
                throw new IllegalStateException(
                        "Un pedido pendiente solo puede confirmarse o cancelarse");
            }

            // Al pasar a CONFIRMADO, descontamos stock
            if (nuevoEstado == EstadoPedido.CONFIRMADO) {
                for (DetallePedido detalle : pedido.getDetalles()) {
                    Producto producto = productoRepository
                            .findById(detalle.getProducto().getId())
                            .orElseThrow(() -> new ProductoNoEncontradoException(
                                    "Producto no encontrado"));

                    if (producto.getStock() < detalle.getCantidad()) {
                        throw new StockInsuficienteException(
                                "Stock insuficiente para el producto: " + producto.getNombre());
                    }

                    producto.setStock(producto.getStock() - detalle.getCantidad());
                    productoRepository.save(producto);
                }
            }
        }

        // Transiciones desde CONFIRMADO
        else if (pedido.getEstado() == EstadoPedido.CONFIRMADO) {
            if (nuevoEstado != EstadoPedido.ENVIADO && nuevoEstado != EstadoPedido.CANCELADO) {
                throw new IllegalStateException(
                        "Un pedido confirmado solo puede ser enviado o cancelado");
            }

            // Si estaba CONFIRMADO y pasa a CANCELADO, devolvemos las unidades al stock
            if (nuevoEstado == EstadoPedido.CANCELADO) {
                for (DetallePedido detalle : pedido.getDetalles()) {
                    Producto producto = productoRepository
                            .findById(detalle.getProducto().getId())
                            .orElseThrow(() -> new ProductoNoEncontradoException(
                                    "Producto no encontrado"));

                    producto.setStock(producto.getStock() + detalle.getCantidad());
                    productoRepository.save(producto);
                }
            }
        }

        // Transiciones desde ENVIADO
        else if (pedido.getEstado() == EstadoPedido.ENVIADO) {
            if (nuevoEstado != EstadoPedido.ENTREGADO) {
                throw new IllegalStateException(
                        "Un pedido enviado solo puede pasar a entregado");
            }
        }

        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido guardarPedido(Pedido pedido) {

        if (pedido.getUsuario() == null || pedido.getUsuario().getId() == null) {
            throw new IllegalArgumentException("Debe indicarse un usuario válido para el pedido");
        }

        Usuario usuario = usuarioRepository
                .findById(pedido.getUsuario().getId())
                .orElseThrow(() -> new UsuarioNoEncontradoException(
                        "Usuario con ID " + pedido.getUsuario().getId() + " no encontrado"));

        pedido.setUsuario(usuario);

        if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe contener al menos un detalle");
        }

        double total = 0;

        for (DetallePedido detalle : pedido.getDetalles()) {
            if (detalle.getProducto() == null || detalle.getProducto().getId() == null) {
                throw new IllegalArgumentException("Cada detalle debe tener un producto válido asociado");
            }

            Producto producto = productoRepository
                    .findById(detalle.getProducto().getId())
                    .orElseThrow(() -> new ProductoNoEncontradoException(
                            "Producto con ID " + detalle.getProducto().getId() + " no encontrado"));

            if (producto.getStock() < detalle.getCantidad()) {
                throw new StockInsuficienteException(
                        "Stock insuficiente para el producto: " + producto.getNombre());
            }

            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setProducto(producto);
            detalle.setPedido(pedido);

            total += producto.getPrecio() * detalle.getCantidad();
        }

        pedido.setTotal(total);

        return pedidoRepository.save(pedido);
    }

    public List<Pedido> listarPedidosPorUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId);
    }
}
