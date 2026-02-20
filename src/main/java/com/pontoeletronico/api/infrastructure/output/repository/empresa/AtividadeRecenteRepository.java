package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import com.pontoeletronico.api.domain.entity.registro.RegistroPonto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Consultas para atividades recentes (registros de ponto) da empresa.
 * Native query: últimos 4 registros com nome do funcionário.
 */
public interface AtividadeRecenteRepository extends Repository<RegistroPonto, UUID> {

    /**
     * Últimos N registros de ponto da empresa (por created_at DESC).
     * Size direto na query (LIMIT :size). Retorna [nome_completo, created_at] por linha.
     */
    @Query(value = """
            SELECT i.nome_completo, r.created_at
            FROM registro_ponto r
            INNER JOIN identificacao_funcionario i ON r.usuario_id = i.funcionario_id
            WHERE i.empresa_id = :empresaId
            ORDER BY r.created_at DESC
            LIMIT :size
            """, nativeQuery = true)
    List<Object[]> findByEmpresaIdOrderByCreatedAtDesc(@Param("empresaId") UUID empresaId, @Param("size") int size);
}
