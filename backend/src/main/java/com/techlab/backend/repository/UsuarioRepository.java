package com.techlab.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.techlab.backend.usuario.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}