package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import com.pontoeletronico.api.domain.entity.empresa.IdentificacaoFuncionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IdentificacaoFuncionarioRepository extends JpaRepository<IdentificacaoFuncionario, UUID> {

    @Query(value = "SELECT empresa_id FROM identificacao_funcionario JOIN users ON users.id = identificacao_funcionario.funcionario_id WHERE users.ativo = true AND users.data_desativacao IS NULL AND funcionario_id = :funcionarioId LIMIT 1", nativeQuery = true)
    Optional<UUID> findEmpresaIdByFuncionarioIdAndAtivoTrue(@Param("funcionarioId") UUID funcionarioId);
    
    @Query(value = "SELECT if_.id, if_.funcionario_id, if_.empresa_id, if_.nome_completo, if_.primeiro_nome, if_.ultimo_nome, if_.cpf, if_.codigo_ponto, if_.data_nascimento, if_.updated_at FROM identificacao_funcionario if_ JOIN users u ON u.id = if_.funcionario_id WHERE u.ativo = true AND u.data_desativacao IS NULL AND if_.funcionario_id = :funcionarioId LIMIT 1", nativeQuery = true)
    Optional<IdentificacaoFuncionario> findByFuncionarioIdAndAtivoTrue(@Param("funcionarioId") UUID funcionarioId);
    

    @Query(value = "SELECT if_.id, if_.funcionario_id, if_.empresa_id, if_.nome_completo, if_.primeiro_nome, if_.ultimo_nome, if_.cpf, if_.codigo_ponto, if_.data_nascimento, if_.updated_at FROM identificacao_funcionario if_ JOIN users u ON u.id = if_.funcionario_id WHERE u.ativo = true AND u.data_desativacao IS NULL AND if_.funcionario_id = :funcionarioId LIMIT 1", nativeQuery = true)
    Optional<IdentificacaoFuncionario> findFirstByFuncionarioIdAndAtivoTrue(@Param("funcionarioId") UUID funcionarioId);
  
    @Query(value = "SELECT funcionario_id FROM identificacao_funcionario JOIN users ON users.id = identificacao_funcionario.funcionario_id WHERE users.ativo = true AND users.data_desativacao IS NULL AND empresa_id = :empresaId AND codigo_ponto = :codigoPonto LIMIT 1", nativeQuery = true)
    Optional<UUID> findFuncionarioIdByEmpresaIdAndCodigoPontoAndAtivoTrue(@Param("empresaId") UUID empresaId, @Param("codigoPonto") Integer codigoPonto);
  
    @Query(value = "SELECT if_.id, if_.funcionario_id, if_.empresa_id, if_.nome_completo, if_.primeiro_nome, if_.ultimo_nome, if_.cpf, if_.codigo_ponto, if_.data_nascimento, if_.updated_at FROM identificacao_funcionario if_ JOIN users u ON u.id = if_.funcionario_id WHERE u.ativo = true AND u.data_desativacao IS NULL AND if_.empresa_id = :empresaId AND if_.funcionario_id = :funcionarioId LIMIT 1", nativeQuery = true)
    Optional<IdentificacaoFuncionario> findByEmpresaIdAndFuncionarioIdAndAtivoTrue(@Param("empresaId") UUID empresaId, @Param("funcionarioId") UUID funcionarioId);

    
    
    
    List<IdentificacaoFuncionario> findByEmpresaIdOrderByNomeCompletoAsc(UUID empresaId);


    @Query(value = "SELECT 1 FROM identificacao_funcionario WHERE cpf = :cpf LIMIT 1", nativeQuery = true)
    Optional<Integer> existsByCpf(@Param("cpf") String cpf);

    @Query(value = "SELECT 1 FROM identificacao_funcionario WHERE cpf = :cpf AND funcionario_id != :excludeFuncionarioId LIMIT 1", nativeQuery = true)
    Optional<Integer> existsByCpfAndFuncionarioIdNot(@Param("cpf") String cpf, @Param("excludeFuncionarioId") UUID excludeFuncionarioId);

    @Query(value = "SELECT cpf FROM identificacao_funcionario WHERE funcionario_id = :funcionarioId LIMIT 1", nativeQuery = true)
    Optional<String> findCpfByFuncionarioId(@Param("funcionarioId") UUID funcionarioId);

    @Query(value = """
            SELECT COALESCE(MAX(codigo_ponto), 0) + 1
            FROM identificacao_funcionario
            WHERE empresa_id = :empresaId
            """, nativeQuery = true)
    Integer nextCodigoPonto(@Param("empresaId") UUID empresaId);

    @Modifying
    @Query(value = """
            INSERT INTO identificacao_funcionario (id, funcionario_id, empresa_id, nome_completo, primeiro_nome, ultimo_nome, cpf, codigo_ponto, data_nascimento, updated_at)
            VALUES (:id, :funcionarioId, :empresaId, :nomeCompleto, :primeiroNome, :ultimoNome, :cpf, :codigoPonto, :dataNascimento, :updatedAt)
            """, nativeQuery = true)
    void insert(@Param("id") UUID id, @Param("funcionarioId") UUID funcionarioId, @Param("empresaId") UUID empresaId,
                @Param("nomeCompleto") String nomeCompleto, @Param("primeiroNome") String primeiroNome, @Param("ultimoNome") String ultimoNome,
                @Param("cpf") String cpf, @Param("codigoPonto") Integer codigoPonto,
                @Param("dataNascimento") LocalDate dataNascimento, @Param("updatedAt") LocalDateTime updatedAt);

    @Modifying
    @Query(value = """
            UPDATE identificacao_funcionario SET nome_completo = :nomeCompleto, primeiro_nome = :primeiroNome, ultimo_nome = :ultimoNome, cpf = :cpf, data_nascimento = :dataNascimento, updated_at = :updatedAt
            WHERE funcionario_id = :funcionarioId
            """, nativeQuery = true)
    int updateByFuncionarioId(@Param("funcionarioId") UUID funcionarioId, @Param("nomeCompleto") String nomeCompleto,
                              @Param("primeiroNome") String primeiroNome, @Param("ultimoNome") String ultimoNome,
                              @Param("cpf") String cpf, @Param("dataNascimento") LocalDate dataNascimento, @Param("updatedAt") LocalDateTime updatedAt);

    @Query(value = """
            SELECT
                u.id                               AS usuarioId,
                if_.nome_completo                  AS nomeCompleto,
                if_.primeiro_nome                 AS "primeiroNome",
                if_.ultimo_nome                   AS "ultimoNome",
                u.username                         AS username,
                emails.emails                      AS emails,
                tels.telefones                     AS telefones

            FROM users u

            INNER JOIN identificacao_funcionario if_
                    ON if_.funcionario_id = u.id

            LEFT JOIN LATERAL (
                SELECT COALESCE(json_agg(uc.valor)::text, '[]') AS emails
                FROM user_credential uc
                JOIN tipo_credential tc ON tc.id = uc.tipo_credencial_id AND tc.descricao = 'EMAIL'
                WHERE uc.usuario_id = u.id
            ) emails ON true

            LEFT JOIN LATERAL (
                SELECT COALESCE(json_agg(json_build_object('codigoPais', ut.codigo_pais, 'ddd', ut.ddd, 'numero', ut.numero))::text, '[]') AS telefones
                FROM usuario_telefone ut
                WHERE ut.usuario_id = u.id
            ) tels ON true

            WHERE if_.empresa_id = :empresaId
              AND u.ativo = true
              AND u.data_desativacao IS NULL
              AND unaccent(LOWER(if_.nome_completo)) LIKE unaccent(LOWER('%' || REPLACE(TRIM(COALESCE(:nome, '')), ' ', '%') || '%'))
            ORDER BY if_.nome_completo ASC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<FuncionarioListagemProjection> findFuncionariosByEmpresaId(@Param("empresaId") UUID empresaId, @Param("nome") String nome, @Param("limit") int limit, @Param("offset") int offset);

    @Query(value = """
            SELECT COUNT(*)
            FROM users u
            INNER JOIN identificacao_funcionario if_ ON if_.funcionario_id = u.id
            WHERE if_.empresa_id = :empresaId
              AND u.ativo = true
              AND u.data_desativacao IS NULL
              AND unaccent(LOWER(if_.nome_completo)) LIKE unaccent(LOWER('%' || REPLACE(TRIM(COALESCE(:nome, '')), ' ', '%') || '%'))
            """, nativeQuery = true)
    long countFuncionariosByEmpresaId(@Param("empresaId") UUID empresaId, @Param("nome") String nome);
}
