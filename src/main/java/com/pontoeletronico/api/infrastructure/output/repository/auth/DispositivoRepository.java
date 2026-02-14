package com.pontoeletronico.api.infrastructure.output.repository.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pontoeletronico.api.domain.entity.auth.Dispositivo;

import java.util.Optional;
import java.util.UUID;

public interface DispositivoRepository extends JpaRepository<Dispositivo, UUID> {

    @Query(value = "SELECT id FROM dispositivo WHERE usuario_id = :usuarioId AND ip_address IS NOT DISTINCT FROM :ipAddress AND user_agent IS NOT DISTINCT FROM :userAgent LIMIT 1", nativeQuery = true)
    Optional<UUID> findIdByUsuarioAndIpAndUserAgent(@Param("usuarioId") UUID usuarioId, @Param("ipAddress") String ipAddress, @Param("userAgent") String userAgent);

    @Modifying
    @Query(value = "INSERT INTO dispositivo (id, usuario_id, nome_dispositivo, sistema_operacional, versao_app, modelo_dispositivo, ip_address, user_agent) VALUES (:id, :usuarioId, NULL, NULL, NULL, NULL, :ipAddress, :userAgent)", nativeQuery = true)
    void insert(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId, @Param("ipAddress") String ipAddress, @Param("userAgent") String userAgent);
}
