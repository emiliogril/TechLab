package com.techlab.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.techlab.backend.pedido.EstadoPedido;
import com.techlab.backend.pedido.Pedido;
import com.techlab.backend.service.PedidoService;

import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = {
        "http://127.0.0.1:5500",
        "http://localhost:5500"
})
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public List<Pedido> listarPedidos() {
        return pedidoService.listarPedidos();
    }

    @GetMapping("/{id}")
    public Pedido buscarPedidoPorId(@PathVariable Long id) {
        return pedidoService.buscarPedidoPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pedido guardarPedido(@Valid @RequestBody Pedido pedido) {
        return pedidoService.guardarPedido(pedido);
    }

    @PutMapping("/{id}/estado")
    public Pedido actualizarEstado(
            @PathVariable Long id,
            @RequestBody EstadoPedido nuevoEstado) {
        return pedidoService.actualizarEstado(id, nuevoEstado);
    }

}
