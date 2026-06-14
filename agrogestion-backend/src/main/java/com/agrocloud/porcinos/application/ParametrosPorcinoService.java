package com.agrocloud.porcinos.application;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.application.EmpresaContextService;
import com.agrocloud.porcinos.domain.DatosEconomicosPorcino;
import com.agrocloud.porcinos.domain.ParametrosEstablecimientoPorcino;
import com.agrocloud.porcinos.domain.ParametrosProductivosPorcino;
import com.agrocloud.porcinos.infrastructure.DatosEconomicosPorcinoRepository;
import com.agrocloud.porcinos.infrastructure.ParametrosEstablecimientoPorcinoRepository;
import com.agrocloud.porcinos.infrastructure.ParametrosProductivosPorcinoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Servicio para gestionar parámetros del establecimiento, productivos y económicos
 */
@Service
public class ParametrosPorcinoService {

    @Autowired
    private ParametrosEstablecimientoPorcinoRepository parametrosEstablecimientoRepository;
    
    @Autowired
    private ParametrosProductivosPorcinoRepository parametrosProductivosRepository;
    
    @Autowired
    private DatosEconomicosPorcinoRepository datosEconomicosRepository;
    
    @Autowired
    private EmpresaContextService empresaContextService;

    // ============================================================================
    // PARÁMETROS DEL ESTABLECIMIENTO
    // ============================================================================
    
    @Transactional(readOnly = true)
    public Optional<ParametrosEstablecimientoPorcino> obtenerParametrosEstablecimiento(Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) return Optional.empty();
        return parametrosEstablecimientoRepository.findByEmpresa(empresa.get());
    }
    
    @Transactional
    public ParametrosEstablecimientoPorcino guardarParametrosEstablecimiento(
            ParametrosEstablecimientoPorcino parametros, Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) {
            throw new RuntimeException("Usuario no tiene empresa activa");
        }
        
        // Validaciones
        if (parametros.getNombreEstablecimiento() == null || parametros.getNombreEstablecimiento().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del establecimiento es obligatorio");
        }
        if (parametros.getNombreEstablecimiento().length() > 200) {
            throw new IllegalArgumentException("El nombre del establecimiento no puede exceder 200 caracteres");
        }
        if (parametros.getUnidadManejo() == null) {
            throw new IllegalArgumentException("La unidad de manejo es obligatoria");
        }
        
        // Validaciones numéricas
        if (parametros.getMaximoMadres() != null && parametros.getMaximoMadres() < 0) {
            throw new IllegalArgumentException("El máximo de madres no puede ser negativo");
        }
        if (parametros.getMaximoPadrillos() != null && parametros.getMaximoPadrillos() < 0) {
            throw new IllegalArgumentException("El máximo de padrillos no puede ser negativo");
        }
        if (parametros.getMaximaCapacidadRecriaEngorde() != null && parametros.getMaximaCapacidadRecriaEngorde() < 0) {
            throw new IllegalArgumentException("La capacidad máxima no puede ser negativa");
        }
        
        Optional<ParametrosEstablecimientoPorcino> existente = 
            parametrosEstablecimientoRepository.findByEmpresa(empresa.get());
        
        if (existente.isPresent()) {
            ParametrosEstablecimientoPorcino actual = existente.get();
            actual.setNombreEstablecimiento(parametros.getNombreEstablecimiento());
            actual.setProvincia(parametros.getProvincia());
            actual.setLocalidad(parametros.getLocalidad());
            actual.setRazonSocial(parametros.getRazonSocial());
            actual.setUnidadManejo(parametros.getUnidadManejo());
            actual.setMaximoMadres(parametros.getMaximoMadres());
            actual.setMaximoPadrillos(parametros.getMaximoPadrillos());
            actual.setMaximaCapacidadRecriaEngorde(parametros.getMaximaCapacidadRecriaEngorde());
            actual.setCategoriasHabilitadas(parametros.getCategoriasHabilitadas());
            actual.setCiclosProductivosPropios(parametros.getCiclosProductivosPropios());
            return parametrosEstablecimientoRepository.save(actual);
        } else {
            parametros.setEmpresa(empresa.get());
            return parametrosEstablecimientoRepository.save(parametros);
        }
    }

    // ============================================================================
    // PARÁMETROS PRODUCTIVOS
    // ============================================================================
    
    @Transactional(readOnly = true)
    public Optional<ParametrosProductivosPorcino> obtenerParametrosProductivos(Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) return Optional.empty();
        return parametrosProductivosRepository.findByEmpresa(empresa.get());
    }
    
    @Transactional
    public ParametrosProductivosPorcino guardarParametrosProductivos(
            ParametrosProductivosPorcino parametros, Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) {
            throw new RuntimeException("Usuario no tiene empresa activa");
        }
        
        // Validaciones de rangos
        validarRango(parametros.getDiasPromedioGestacion(), 100, 130, "Días promedio de gestación");
        validarRango(parametros.getDiasLactancia(), 14, 35, "Días de lactancia");
        validarRango(parametros.getDiasRecriaAntesEngorde(), 30, 120, "Días de recría antes del engorde");
        validarRango(parametros.getDiasEngorde(), 60, 180, "Días de engorde");
        validarRangoPositivo(parametros.getCantidadMaximaServiciosPadrilloDia(), "Cantidad máxima de servicios por día");
        validarRangoPositivo(parametros.getTiempoEsperaEntreServiciosHoras(), "Tiempo de espera entre servicios");
        validarRangoPorcentaje(parametros.getUmbralMortalidadLactanciaPorcentaje(), "Umbral de mortalidad en lactancia");
        validarRangoPorcentaje(parametros.getUmbralMortalidadRecriaPorcentaje(), "Umbral de mortalidad en recría");
        validarRangoPorcentaje(parametros.getPorcentajeMinimoPrenezAntesAdvertencia(), "Porcentaje mínimo de preñez");
        
        Optional<ParametrosProductivosPorcino> existente = 
            parametrosProductivosRepository.findByEmpresa(empresa.get());
        
        if (existente.isPresent()) {
            ParametrosProductivosPorcino actual = existente.get();
            // Actualizar todos los campos - Ciclo de producción
            actual.setDiasPromedioGestacion(parametros.getDiasPromedioGestacion());
            actual.setDiasLactancia(parametros.getDiasLactancia());
            actual.setDiasRecriaAntesEngorde(parametros.getDiasRecriaAntesEngorde());
            actual.setDiasEngorde(parametros.getDiasEngorde());
            
            // Parámetros reproductivos adicionales
            actual.setDiasToleranciaVencimientoGestacion(parametros.getDiasToleranciaVencimientoGestacion());
            actual.setDiasControlCelo(parametros.getDiasControlCelo());
            actual.setDiasEntreCelos(parametros.getDiasEntreCelos());
            
            // Servicios
            actual.setCantidadMaximaServiciosPadrilloDia(parametros.getCantidadMaximaServiciosPadrilloDia());
            actual.setTiempoEsperaEntreServiciosHoras(parametros.getTiempoEsperaEntreServiciosHoras());
            
            // Alertas
            actual.setDiasAntelacionAlertarPartos(parametros.getDiasAntelacionAlertarPartos());
            actual.setDiasAntelacionAlertarEcografias(parametros.getDiasAntelacionAlertarEcografias());
            actual.setDiasAntelacionAlertarDestetes(parametros.getDiasAntelacionAlertarDestetes());
            actual.setDiasAntelacionAlertarRevisionesSanitarias(parametros.getDiasAntelacionAlertarRevisionesSanitarias());
            
            // Umbrales
            actual.setUmbralMortalidadLactanciaPorcentaje(parametros.getUmbralMortalidadLactanciaPorcentaje());
            actual.setUmbralMortalidadRecriaPorcentaje(parametros.getUmbralMortalidadRecriaPorcentaje());
            actual.setPorcentajeMinimoPrenezAntesAdvertencia(parametros.getPorcentajeMinimoPrenezAntesAdvertencia());
            
            // Pesos estándar
            actual.setPesoPromedioNacimiento(parametros.getPesoPromedioNacimiento());
            actual.setPesoDesteteObjetivo(parametros.getPesoDesteteObjetivo());
            actual.setPesoVentaObjetivo(parametros.getPesoVentaObjetivo());
            
            // Índices productivos objetivo
            actual.setLechonesVivosPartoObjetivo(parametros.getLechonesVivosPartoObjetivo());
            actual.setLechonesDestetadosObjetivo(parametros.getLechonesDestetadosObjetivo());
            actual.setPartosMadreAnioObjetivo(parametros.getPartosMadreAnioObjetivo());
            
            return parametrosProductivosRepository.save(actual);
        } else {
            parametros.setEmpresa(empresa.get());
            return parametrosProductivosRepository.save(parametros);
        }
    }
    
    private void validarRango(Integer valor, int min, int max, String campo) {
        if (valor != null && (valor < min || valor > max)) {
            throw new IllegalArgumentException(String.format("%s debe estar entre %d y %d", campo, min, max));
        }
    }
    
    private void validarRangoPositivo(Integer valor, String campo) {
        if (valor != null && valor < 0) {
            throw new IllegalArgumentException(String.format("%s no puede ser negativo", campo));
        }
    }
    
    private void validarRangoPorcentaje(BigDecimal valor, String campo) {
        if (valor != null) {
            double valorDouble = valor.doubleValue();
            if (valorDouble < 0 || valorDouble > 100) {
                throw new IllegalArgumentException(String.format("%s debe estar entre 0 y 100", campo));
            }
        }
    }

    // ============================================================================
    // DATOS ECONÓMICOS
    // ============================================================================
    
    @Transactional(readOnly = true)
    public Optional<DatosEconomicosPorcino> obtenerDatosEconomicos(Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) return Optional.empty();
        return datosEconomicosRepository.findByEmpresa(empresa.get());
    }
    
    @Transactional
    public DatosEconomicosPorcino guardarDatosEconomicos(DatosEconomicosPorcino datos, Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) {
            throw new RuntimeException("Usuario no tiene empresa activa");
        }
        
        // Validaciones de valores monetarios y porcentajes
        validarValorNoNegativo(datos.getCostoMadreGestacionDia(), "Costo madre gestación/día");
        validarValorNoNegativo(datos.getCostoMadreLactanciaDia(), "Costo madre lactancia/día");
        validarValorNoNegativo(datos.getCostoLechon(), "Costo lechón");
        validarValorNoNegativo(datos.getCostoEngordeDia(), "Costo engorde/día");
        validarValorNoNegativo(datos.getCostoManoObraDia(), "Costo mano de obra/día");
        validarValorNoNegativo(datos.getPrecioVentaCerdoTerminadoKg(), "Precio venta cerdo terminado/kg");
        validarPorcentaje(datos.getPorcentajeMermaTransporte(), "Porcentaje merma en transporte");
        validarValorNoNegativo(datos.getKgMaizPorRacionEngorde(), "Kg maíz por ración de engorde");
        validarPorcentaje(datos.getPorcentajeMezclaAlimentoPropioBalanceado(), "Porcentaje mezcla alimento propio/balanceado");
        validarValorPositivo(datos.getIndiceConversionObjetivo(), "Índice de conversión objetivo");
        validarValorNoNegativo(datos.getPrecioManualCultivoKg(), "Precio manual cultivo/kg");
        
        Optional<DatosEconomicosPorcino> existente = 
            datosEconomicosRepository.findByEmpresa(empresa.get());
        
        if (existente.isPresent()) {
            DatosEconomicosPorcino actual = existente.get();
            // Actualizar todos los campos
            actual.setCostoMadreGestacionDia(datos.getCostoMadreGestacionDia());
            actual.setCostoMadreLactanciaDia(datos.getCostoMadreLactanciaDia());
            actual.setCostoLechon(datos.getCostoLechon());
            actual.setCostoEngordeDia(datos.getCostoEngordeDia());
            actual.setCostoManoObraDia(datos.getCostoManoObraDia());
            actual.setPrecioVentaCerdoTerminadoKg(datos.getPrecioVentaCerdoTerminadoKg());
            actual.setPorcentajeMermaTransporte(datos.getPorcentajeMermaTransporte());
            actual.setKgMaizPorRacionEngorde(datos.getKgMaizPorRacionEngorde());
            actual.setPorcentajeMezclaAlimentoPropioBalanceado(datos.getPorcentajeMezclaAlimentoPropioBalanceado());
            actual.setIndiceConversionObjetivo(datos.getIndiceConversionObjetivo());
            actual.setMetodoImputacionCostoCultivo(datos.getMetodoImputacionCostoCultivo());
            actual.setPrecioManualCultivoKg(datos.getPrecioManualCultivoKg());
            return datosEconomicosRepository.save(actual);
        } else {
            datos.setEmpresa(empresa.get());
            return datosEconomicosRepository.save(datos);
        }
    }
    
    private void validarValorNoNegativo(java.math.BigDecimal valor, String campo) {
        if (valor != null && valor.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(String.format("%s no puede ser negativo", campo));
        }
    }
    
    private void validarValorPositivo(java.math.BigDecimal valor, String campo) {
        if (valor != null && valor.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(String.format("%s debe ser mayor a cero", campo));
        }
    }
    
    private void validarPorcentaje(java.math.BigDecimal valor, String campo) {
        if (valor != null && (valor.compareTo(java.math.BigDecimal.ZERO) < 0 || valor.compareTo(java.math.BigDecimal.valueOf(100)) > 0)) {
            throw new IllegalArgumentException(String.format("%s debe estar entre 0 y 100", campo));
        }
    }
}

