package com.pontoeletronico.api.domain.services.registro;

import com.pontoeletronico.api.domain.services.bancohoras.record.JornadaConfig;
import com.pontoeletronico.api.exception.RegistroPontoInvalidoException;
import com.pontoeletronico.api.infrastructure.input.dto.registro.RegistroMetadadosRequest;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.EmpresaJornadaConfigRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.JornadaFuncionarioConfigRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.TipoEscalaJornadaRepository;
import com.pontoeletronico.api.infrastructure.output.repository.usuario.UsuarioGeofenceRepository;

import lombok.Data;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@Data
public class RegistroPontoValidacaoService {

    private final EmpresaJornadaConfigRepository empresaJornadaConfigRepository;
    private final JornadaFuncionarioConfigRepository jornadaFuncionarioConfigRepository;
    private final TipoEscalaJornadaRepository tipoEscalaJornadaRepository;
    private final UsuarioGeofenceRepository usuarioGeofenceRepository;


    public JornadaConfig validar(UUID empresaId, UUID funcionarioId, RegistroMetadadosRequest metadados) {

        JornadaConfig jornadaConfig = null;
        var tipoEscala = tipoEscalaJornadaRepository.findByDescricaoAndAtivoTrue("5x2");
        var jornadaFunc = jornadaFuncionarioConfigRepository.findByFuncionarioId(funcionarioId);
        var jornadaEmpresa = empresaJornadaConfigRepository.findByEmpresaId(empresaId);

        boolean gravaGeoObrigatoria = jornadaEmpresa.get().getGravaPontoApenasEmGeofence();
        boolean gravaPontoApenasEmGeofence = jornadaEmpresa.get().getGravaPontoApenasEmGeofence();
        boolean permiteAjustePonto = jornadaEmpresa.get().getPermiteAjustePonto();

        if (jornadaFunc.isPresent()) {
            gravaGeoObrigatoria = jornadaFunc.get().getGravaGeoObrigatoria();
            jornadaConfig = new JornadaConfig(
                jornadaFunc.get().getTempoDescansoEntreJornada(), 
                jornadaFunc.get().getTipoEscalaJornadaId(), 
                jornadaFunc.get().getCargaHorariaDiaria(), 
                gravaGeoObrigatoria, 
                gravaPontoApenasEmGeofence,
                permiteAjustePonto
            );
        } else if (jornadaEmpresa.isPresent()) {
            jornadaConfig = new JornadaConfig(
                jornadaEmpresa.get().getTempoDescansoEntreJornada(), 
                jornadaEmpresa.get().getTipoEscalaJornadaId(), 
                jornadaEmpresa.get().getCargaHorariaDiaria(), 
                gravaGeoObrigatoria, 
                gravaPontoApenasEmGeofence,
                permiteAjustePonto
            );
        } else {
            jornadaConfig = new JornadaConfig(
                Duration.ofHours(11), 
                tipoEscala.get().getId(), 
                Duration.ofHours(8), 
                false, 
                false,
                false
            );
        }

        if (gravaGeoObrigatoria && (metadados != null && (metadados.geoLatitude() == null || metadados.geoLongitude() == null))) {
            throw new RegistroPontoInvalidoException("Geolocalização é obrigatória para registro de ponto");
        }

        if (gravaPontoApenasEmGeofence) {
            if (metadados != null && (metadados.geoLatitude() == null || metadados.geoLongitude() == null)) {
                throw new RegistroPontoInvalidoException("Geolocalização é obrigatória para registro dentro do geofence");
            }
            if (metadados != null && usuarioGeofenceRepository.existsFuncionarioDentroDeGeofence(funcionarioId, metadados.geoLatitude(), metadados.geoLongitude()).isPresent()) {
                throw new RegistroPontoInvalidoException("Registro de ponto permitido apenas dentro do geofence cadastrado");
            }
        }
        return jornadaConfig;
    }
}
