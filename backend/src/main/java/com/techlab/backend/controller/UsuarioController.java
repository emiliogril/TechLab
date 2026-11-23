package com.techlab.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.techlab.backend.pedido.Pedido;
import com.techlab.backend.service.PedidoService;
import com.techlab.backend.service.UsuarioService;
import com.techlab.backend.usuario.Usuario;

import jakarta.validation.Valid;

@CrossOrigin(origins = {
        "http://127.0.0.1:5500",
        "http://localhost:5500"
})
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PedidoService pedidoService;

    public UsuarioController(
            UsuarioService usuarioService,
            PedidoService pedidoService) {

        this.usuarioService = usuarioService;
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    @GetMapping("/{id}")
    public Usuario buscarUsuarioPorId(@PathVariable Long id) {
        return usuarioService.buscarUsuarioPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario guardarUsuario(@Valid @RequestBody Usuario usuario) {
        return usuarioService.guardarUsuario(usuario);
    }

    @GetMapping("/{id}/pedidos")
    public List<Pedido> listarPedidosPorUsuario(@PathVariable Long id) {
        usuarioService.buscarUsuarioPorId(id);
        return pedidoService.listarPedidosPorUsuario(id);
    }
}
