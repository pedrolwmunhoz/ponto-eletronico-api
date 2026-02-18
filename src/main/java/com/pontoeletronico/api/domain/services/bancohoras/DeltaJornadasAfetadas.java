package com.pontoeletronico.api.domain.services.bancohoras;

import java.time.Duration;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto que guarda o delta de registros e horas da jornada afetada.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeltaJornadasAfetadas {

    private int deltaRegistrosPontoAfetada;
    private Duration deltaHorasAfetada;
    LocalDate dataRefAfetada;

    private int deltaRegistrosPontoAnterior;
    private Duration deltaHorasAnterior;
    private LocalDate dataRefAnterior;

    private int deltaRegistrosPontoPosterior;
    private Duration deltaHorasPosterior;
    private LocalDate dataRefPosterior;
    
}
