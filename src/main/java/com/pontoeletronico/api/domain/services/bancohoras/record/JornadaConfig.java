package com.pontoeletronico.api.domain.services.bancohoras.record;

import java.time.Duration;

public record JornadaConfig(
    Duration tempoDescansoEntreJornada, 
    int escalaId, 
    Duration cargaDiaria, 
    boolean gravaGeoObrigatoria, 
    boolean gravaPontoApenasEmGeofence, 
    boolean permiteAjustePonto
) {}
