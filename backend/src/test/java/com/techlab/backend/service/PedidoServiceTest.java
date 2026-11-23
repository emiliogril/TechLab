package com.techlab.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.techlab.backend.exception.StockInsuficienteException;
import com.techlab.backend.pedido.DetallePedido;
import com.techlab.backend.pedido.EstadoPedido;
import com.techlab.backend.pedido.Pedido;
import com.techlab.backend.producto.Producto;
import com.techlab.backend.repository.PedidoRepository;
import com.techlab.backend.repository.ProductoRepository;
import com.techlab.backend.repository.UsuarioRepository;
import com.techlab.backend.usuario.Usuario;

@ExtendWith(MockitoExtension.class)
public class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private PedidoService pedidoService;

    private Usuario usuario;
    private Producto producto;
    private Pedido pedido;
    private DetallePedido detalle;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("Emilio", "emilio@techlab.com");
        usuario.setId(1L);

        producto = new Producto("Mouse Gamer", "Mouse RGB", "Perifericos", 15000.0, "", 10);
        producto.setId(1L);

        pedido = new Pedido();
        pedido.setId(1L);
        pedido.setUsuario(usuario);
        pedido.setEstado(EstadoPedido.PENDIENTE);

        detalle = new DetallePedido();
        detalle.setProducto(producto);
        detalle.setCantidad(2);

        List<DetallePedido> detalles = new ArrayList<>();
        detalles.add(detalle);
        pedido.setDetalles(detalles);
    }

    @Test
    void testGuardarPedidoValidaStockDisponibleSinDescontar() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pedido guardado = pedidoService.guardarPedido(pedido);

        assertNotNull(guardado);
        assertEquals(30000.0, guardado.getTotal());
        assertEquals(EstadoPedido.PENDIENTE, guardado.getEstado());
        // El stock NO debe descontarse al crear el pedido
        assertEquals(10, producto.getStock());
    }

    @Test
    void testGuardarPedidoLanzaExcepcionSiStockInsuficiente() {
        producto.setStock(1);
        detalle.setCantidad(5);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        assertThrows(StockInsuficienteException.class, () -> {
            pedidoService.guardarPedido(pedido);
        });
    }

    @Test
    void testConfirmarPedidoDescuentaStock() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pedido confirmado = pedidoService.actualizarEstado(1L, EstadoPedido.CONFIRMADO);

        assertEquals(EstadoPedido.CONFIRMADO, confirmado.getEstado());
        // Stock inicial 10 - 2 cantidad = 8
        assertEquals(8, producto.getStock());
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    void testCancelarPedidoConfirmadoDevuelveStock() {
        pedido.setEstado(EstadoPedido.CONFIRMADO);
        producto.setStock(8); // Ya había descontado

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pedido cancelado = pedidoService.actualizarEstado(1L, EstadoPedido.CANCELADO);

        assertEquals(EstadoPedido.CANCELADO, cancelado.getEstado());
        // Stock 8 + 2 devueltas = 10
        assertEquals(10, producto.getStock());
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    void testTransicionInvalidaDePendienteAEntregadoLanzaExcepcion() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThrows(IllegalStateException.class, () -> {
            pedidoService.actualizarEstado(1L, EstadoPedido.ENTREGADO);
        });
    }

    @Test
    void testPedidoCanceladoNoPuedeCambiarDeEstado() {
        pedido.setEstado(EstadoPedido.CANCELADO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThrows(IllegalStateException.class, () -> {
            pedidoService.actualizarEstado(1L, EstadoPedido.CONFIRMADO);
        });
    }

    @Test
    void testPedidoEntregadoNoPuedeCambiarDeEstado() {
        pedido.setEstado(EstadoPedido.ENTREGADO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThrows(IllegalStateException.class, () -> {
            pedidoService.actualizarEstado(1L, EstadoPedido.CANCELADO);
        });
    }

    @Test
    void testProductoConStock18Permite18YRechaza19() {
        Producto prodStock18 = new Producto("Monitor Gamer", "144Hz", "Tecnología", 250000.0, "", 18);
        prodStock18.setId(2L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(prodStock18));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Pedido con cantidad = 18 (límite exacto de stock disponible)
        Pedido pedidoLimite = new Pedido();
        pedidoLimite.setUsuario(usuario);
        DetallePedido detalle18 = new DetallePedido();
        detalle18.setProducto(prodStock18);
        detalle18.setCantidad(18);
        pedidoLimite.setDetalles(List.of(detalle18));

        Pedido guardado = pedidoService.guardarPedido(pedidoLimite);
        assertNotNull(guardado);
        assertEquals(18 * 250000.0, guardado.getTotal());
        assertEquals(18, prodStock18.getStock()); // No descontado en guardado

        // Intento con cantidad = 19 (supera stock 18)
        Pedido pedidoExcedido = new Pedido();
        pedidoExcedido.setUsuario(usuario);
        DetallePedido detalle19 = new DetallePedido();
        detalle19.setProducto(prodStock18);
        detalle19.setCantidad(19);
        pedidoExcedido.setDetalles(List.of(detalle19));

        assertThrows(StockInsuficienteException.class, () -> {
            pedidoService.guardarPedido(pedidoExcedido);
        });
    }
}
