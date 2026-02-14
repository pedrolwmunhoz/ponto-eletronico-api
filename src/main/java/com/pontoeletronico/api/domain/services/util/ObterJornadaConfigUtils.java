package com.pontoeletronico.api.domain.services.util;

import com.pontoeletronico.api.infrastructure.output.repository.empresa.EmpresaJornadaConfigRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.JornadaFuncionarioConfigRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.TipoEscalaJornadaRepository;
import com.pontoeletronico.api.domain.services.bancohoras.record.JornadaConfig;
import com.pontoeletronico.api.domain.entity.empresa.TipoEscalaJornada;

import lombok.Data;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.Optional;
import java.time.Duration;

@Data 
@Component
public class ObterJornadaConfigUtils {


    private final EmpresaJornadaConfigRepository empresaJornadaConfigRepository;
    private final JornadaFuncionarioConfigRepository jornadaFuncionarioConfigRepository;
    private final TipoEscalaJornadaRepository tipoEscalaJornadaRepository;

    public JornadaConfig obterJornadaConfig(UUID empresaId, UUID funcionarioId) {
        
        var jornadaConfigEmpresa = empresaJornadaConfigRepository.findByEmpresaId(empresaId);
        var jornadaConfigFuncionario = jornadaFuncionarioConfigRepository.findByFuncionarioId(funcionarioId);
        
        if (jornadaConfigFuncionario.isPresent()) {
            var gravaGeoObrigatoria = false;
            var gravaPontoApenasEmGeofence = false;
            var permiteAjustePonto = false;

            if (jornadaConfigEmpresa.isPresent()) {
                gravaGeoObrigatoria = jornadaConfigFuncionario.get().getGravaGeoObrigatoria() ? 
                    jornadaConfigFuncionario.get().getGravaGeoObrigatoria() : 
                    jornadaConfigEmpresa.get().getGravaGeoObrigatoria();
                gravaPontoApenasEmGeofence = jornadaConfigEmpresa.get().getGravaPontoApenasEmGeofence();
                permiteAjustePonto = jornadaConfigEmpresa.get().getPermiteAjustePonto();
            }
            return new JornadaConfig(
                jornadaConfigFuncionario.get().getTempoDescansoEntreJornada(), 
                jornadaConfigFuncionario.get().getTipoEscalaJornadaId(), 
                jornadaConfigFuncionario.get().getCargaHorariaDiaria(), 
                gravaGeoObrigatoria, 
                gravaPontoApenasEmGeofence,
                permiteAjustePonto
            );
        }
        if (jornadaConfigEmpresa.isPresent()) {
            return new JornadaConfig(
                jornadaConfigEmpresa.get().getTempoDescansoEntreJornada(), 
                jornadaConfigEmpresa.get().getTipoEscalaJornadaId(), 
                jornadaConfigEmpresa.get().getCargaHorariaDiaria(), 
                jornadaConfigEmpresa.get().getGravaGeoObrigatoria(), 
                jornadaConfigEmpresa.get().getGravaPontoApenasEmGeofence(), 
                jornadaConfigEmpresa.get().getPermiteAjustePonto()
            );
        }

        Optional<TipoEscalaJornada> tipoEscala = tipoEscalaJornadaRepository.findByDescricaoAndAtivoTrue("5x2");
        JornadaConfig jornadaConfig = new JornadaConfig(
            Duration.ofHours(11), 
            tipoEscala.get().getId() != null ? tipoEscala.get().getId() : 1,
            Duration.ofHours(8), 
            false, 
            false,
            false
        );
        return jornadaConfig;
    }

}
