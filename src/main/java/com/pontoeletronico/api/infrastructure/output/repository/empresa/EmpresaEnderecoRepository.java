package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import com.pontoeletronico.api.domain.entity.empresa.EmpresaEndereco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface EmpresaEnderecoRepository extends JpaRepository<EmpresaEndereco, UUID> {

    Optional<EmpresaEndereco> findByEmpresaId(UUID empresaId);

    @Modifying
    @Query(value = """
            INSERT INTO empresa_endereco (id, empresa_id, rua, numero, complemento, bairro, cidade, uf, cep, updated_at)
            VALUES (:id, :empresaId, :rua, :numero, :complemento, :bairro, :cidade, :uf, :cep, :updatedAt)
            """, nativeQuery = true)
    void insert(@Param("id") UUID id, @Param("empresaId") UUID empresaId, @Param("rua") String rua,
                @Param("numero") String numero, @Param("complemento") String complemento, @Param("bairro") String bairro,
                @Param("cidade") String cidade, @Param("uf") String uf, @Param("cep") String cep,
                @Param("updatedAt") LocalDateTime updatedAt);

    @Modifying
    @Query(value = """
            UPDATE empresa_endereco SET rua = :rua, numero = :numero, complemento = :complemento, bairro = :bairro,
            cidade = :cidade, uf = :uf, cep = :cep, updated_at = :updatedAt
            WHERE empresa_id = :empresaId
            """, nativeQuery = true)
    int updateByEmpresaId(@Param("empresaId") UUID empresaId, @Param("rua") String rua, @Param("numero") String numero,
                          @Param("complemento") String complemento, @Param("bairro") String bairro,
                          @Param("cidade") String cidade, @Param("uf") String uf, @Param("cep") String cep,
                          @Param("updatedAt") LocalDateTime updatedAt);
}
