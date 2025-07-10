package com.example.habitleague.location.model;

public enum LocationVerificationResult {
    VERIFIED,           // Ubicación verificada correctamente
    OUT_OF_RANGE,       // Fuera del rango de tolerancia
    FAILED,             // Error en la verificación
    SUSPICIOUS          // Actividad sospechosa detectada
} 