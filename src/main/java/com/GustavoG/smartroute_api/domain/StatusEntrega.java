package com.GustavoG.smartroute_api.domain;

public enum StatusEntrega {
    PENDENTE,
    ROTEIRIZADO,
    ENTREGUE,
    FALHA_GEOCODING // Para sabermos quais deram erro na TomTom
}