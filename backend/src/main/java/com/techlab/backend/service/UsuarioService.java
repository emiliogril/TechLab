package com.techlab.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.techlab.backend.exception.UsuarioNoEncontradoException;
import com.techlab.backend.repository.UsuarioRepository;
import com.techlab.backend.usuario.Usuario;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(
                        "Usuario con ID " + id + " no encontrado"));
    }

    public Usuario guardarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
}
