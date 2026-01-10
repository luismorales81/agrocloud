package com.agrocloud.service;

import com.agrocloud.model.entity.*;
import com.agrocloud.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Servicio para inicializar datos por defecto del módulo Porcinos para una empresa
 */
@Service
public class InicializacionPorcinoService {

    @Autowired
    private RazaPorcinoRepository razaRepository;
    
    @Autowired
    private TipoAlimentoPorcinoRepository tipoAlimentoRepository;
    
    @Autowired
    private TipoServicioPorcinoRepository tipoServicioRepository;
    
    @Autowired
    private CausaMortalidadPorcinoRepository causaMortalidadRepository;
    
    @Autowired
    private MotivoBajaPorcinoRepository motivoBajaRepository;
    
    @Autowired
    private EsquemaSanitarioPorcinoRepository esquemaSanitarioRepository;
    
    @Autowired
    private TipoCorralPorcinoRepository tipoCorralRepository;
    
    @Autowired
    @SuppressWarnings("unused") // Reservado para futuras implementaciones
    private ParametrosEstablecimientoPorcinoRepository parametrosEstablecimientoRepository;
    
    @Autowired
    private ParametrosProductivosPorcinoRepository parametrosProductivosRepository;
    
    @Autowired
    private DatosEconomicosPorcinoRepository datosEconomicosRepository;

    /**
     * Inicializar todos los datos por defecto para una empresa
     */
    @Transactional
    public void inicializarDatosPorDefecto(Empresa empresa) {
        inicializarRazas(empresa);
        inicializarTiposAlimento(empresa);
        inicializarTiposServicio(empresa);
        inicializarCausasMortalidad(empresa);
        inicializarMotivosBaja(empresa);
        inicializarEsquemasSanitarios(empresa);
        inicializarTiposCorral(empresa);
        inicializarParametrosProductivos(empresa);
        inicializarDatosEconomicos(empresa);
    }

    private void inicializarRazas(Empresa empresa) {
        List<RazaPorcino> razasPorDefecto = Arrays.asList(
            crearRaza("Duroc", RazaPorcino.TipoRaza.PADRILLO, empresa),
            crearRaza("Landrace", RazaPorcino.TipoRaza.MADRE, empresa),
            crearRaza("Large White", RazaPorcino.TipoRaza.MADRE, empresa),
            crearRaza("Pietrain", RazaPorcino.TipoRaza.PADRILLO, empresa),
            crearRaza("Híbrido Duroc x Landrace", RazaPorcino.TipoRaza.HIBRIDO, empresa)
        );

        for (RazaPorcino raza : razasPorDefecto) {
            if (razaRepository.findByEmpresaAndNombreAndActivoTrue(empresa, raza.getNombre()).isEmpty()) {
                razaRepository.save(raza);
            }
        }
    }

    private RazaPorcino crearRaza(String nombre, RazaPorcino.TipoRaza tipo, Empresa empresa) {
        RazaPorcino raza = new RazaPorcino();
        raza.setNombre(nombre);
        raza.setTipo(tipo);
        raza.setEmpresa(empresa);
        raza.setActivo(true);
        return raza;
    }

    private void inicializarTiposAlimento(Empresa empresa) {
        // NOTA: Las categorías RACION_* fueron eliminadas porque están solapadas con InsumoCompuesto
        // Las recetas (raciones) ahora se gestionan completamente en InsumoCompuesto (tipo RACION)
        // que se asocia a etapas mediante RecetaAlimentacionPorEtapa
        List<TipoAlimentoPorcino> alimentosPorDefecto = Arrays.asList(
            crearTipoAlimento("Balanceado 18%", TipoAlimentoPorcino.CategoriaAlimento.BALANCEADO, BigDecimal.valueOf(18.0), empresa),
            crearTipoAlimento("Balanceado 21%", TipoAlimentoPorcino.CategoriaAlimento.BALANCEADO, BigDecimal.valueOf(21.0), empresa),
            crearTipoAlimento("Maíz Propio", TipoAlimentoPorcino.CategoriaAlimento.GRANO_PROPIO, null, empresa),
            crearTipoAlimento("Sorgo Propio", TipoAlimentoPorcino.CategoriaAlimento.GRANO_PROPIO, null, empresa)
            // Raciones eliminadas: ahora se gestionan como InsumoCompuesto tipo RACION
        );

        for (TipoAlimentoPorcino alimento : alimentosPorDefecto) {
            if (tipoAlimentoRepository.findByEmpresaAndNombreAndActivoTrue(empresa, alimento.getNombre()).isEmpty()) {
                tipoAlimentoRepository.save(alimento);
            }
        }
    }

    private TipoAlimentoPorcino crearTipoAlimento(String nombre, TipoAlimentoPorcino.CategoriaAlimento categoria, 
                                                   BigDecimal porcentajeProteina, Empresa empresa) {
        TipoAlimentoPorcino alimento = new TipoAlimentoPorcino();
        alimento.setNombre(nombre);
        alimento.setCategoria(categoria);
        alimento.setPorcentajeProteina(porcentajeProteina);
        alimento.setUnidadMedida("kg");
        alimento.setEmpresa(empresa);
        alimento.setActivo(true);
        return alimento;
    }

    private void inicializarTiposServicio(Empresa empresa) {
        List<TipoServicioPorcino> serviciosPorDefecto = Arrays.asList(
            crearTipoServicio("Monta Natural Directa", TipoServicioPorcino.TipoServicio.MONTA_NATURAL_DIRECTA, empresa),
            crearTipoServicio("IA Poscervical", TipoServicioPorcino.TipoServicio.IA_POSCERVICAL, empresa),
            crearTipoServicio("IA Tradicional", TipoServicioPorcino.TipoServicio.IA_TRADICIONAL, empresa),
            crearTipoServicio("Servicio Repetido", TipoServicioPorcino.TipoServicio.SERVICIO_REPETIDO, empresa)
        );

        for (TipoServicioPorcino servicio : serviciosPorDefecto) {
            if (tipoServicioRepository.findByEmpresaAndNombreAndActivoTrue(empresa, servicio.getNombre()).isEmpty()) {
                tipoServicioRepository.save(servicio);
            }
        }
    }

    private TipoServicioPorcino crearTipoServicio(String nombre, TipoServicioPorcino.TipoServicio tipo, Empresa empresa) {
        TipoServicioPorcino servicio = new TipoServicioPorcino();
        servicio.setNombre(nombre);
        servicio.setTipo(tipo);
        servicio.setEmpresa(empresa);
        servicio.setActivo(true);
        return servicio;
    }

    private void inicializarCausasMortalidad(Empresa empresa) {
        List<CausaMortalidadPorcino> causasPorDefecto = Arrays.asList(
            crearCausaMortalidad("Aplastamiento", CausaMortalidadPorcino.EtapaMortalidad.LACTANCIA, empresa),
            crearCausaMortalidad("Diarrea", CausaMortalidadPorcino.EtapaMortalidad.LACTANCIA, empresa),
            crearCausaMortalidad("Desnutrición", CausaMortalidadPorcino.EtapaMortalidad.LACTANCIA, empresa),
            crearCausaMortalidad("Problemas Respiratorios", CausaMortalidadPorcino.EtapaMortalidad.RECRIA, empresa),
            crearCausaMortalidad("Mortalidad Perinatal", CausaMortalidadPorcino.EtapaMortalidad.GESTACION, empresa),
            crearCausaMortalidad("Fallo Cardíaco", CausaMortalidadPorcino.EtapaMortalidad.GENERAL, empresa),
            crearCausaMortalidad("Causa Desconocida", CausaMortalidadPorcino.EtapaMortalidad.GENERAL, empresa)
        );

        for (CausaMortalidadPorcino causa : causasPorDefecto) {
            if (causaMortalidadRepository.findByEmpresaAndNombreAndEtapaAndActivoTrue(
                    empresa, causa.getNombre(), causa.getEtapa()).isEmpty()) {
                causaMortalidadRepository.save(causa);
            }
        }
    }

    private CausaMortalidadPorcino crearCausaMortalidad(String nombre, CausaMortalidadPorcino.EtapaMortalidad etapa, Empresa empresa) {
        CausaMortalidadPorcino causa = new CausaMortalidadPorcino();
        causa.setNombre(nombre);
        causa.setEtapa(etapa);
        causa.setEmpresa(empresa);
        causa.setActivo(true);
        return causa;
    }

    private void inicializarMotivosBaja(Empresa empresa) {
        List<MotivoBajaPorcino> motivosPorDefecto = Arrays.asList(
            crearMotivoBaja("Venta", MotivoBajaPorcino.TipoBaja.VENTA, empresa),
            crearMotivoBaja("Muerte", MotivoBajaPorcino.TipoBaja.MUERTE, empresa),
            crearMotivoBaja("Reemplazo", MotivoBajaPorcino.TipoBaja.REEMPLAZO, empresa),
            crearMotivoBaja("Problemas Sanitarios", MotivoBajaPorcino.TipoBaja.PROBLEMAS_SANITARIOS, empresa),
            crearMotivoBaja("Problemas Reproductivos", MotivoBajaPorcino.TipoBaja.PROBLEMAS_REPRODUCTIVOS, empresa)
        );

        for (MotivoBajaPorcino motivo : motivosPorDefecto) {
            if (motivoBajaRepository.findByEmpresaAndNombreAndActivoTrue(empresa, motivo.getNombre()).isEmpty()) {
                motivoBajaRepository.save(motivo);
            }
        }
    }

    private MotivoBajaPorcino crearMotivoBaja(String nombre, MotivoBajaPorcino.TipoBaja tipo, Empresa empresa) {
        MotivoBajaPorcino motivo = new MotivoBajaPorcino();
        motivo.setNombre(nombre);
        motivo.setTipo(tipo);
        motivo.setEmpresa(empresa);
        motivo.setActivo(true);
        return motivo;
    }

    private void inicializarEsquemasSanitarios(Empresa empresa) {
        // Los esquemas sanitarios son muy específicos por establecimiento
        // Por defecto solo creamos algunos básicos
        List<EsquemaSanitarioPorcino> esquemasPorDefecto = Arrays.asList(
            crearEsquemaSanitario("Vacunación Triple", EsquemaSanitarioPorcino.TipoEsquema.VACUNA, empresa),
            crearEsquemaSanitario("Desparasitación General", EsquemaSanitarioPorcino.TipoEsquema.DESPARASITACION, empresa)
        );

        for (EsquemaSanitarioPorcino esquema : esquemasPorDefecto) {
            esquemaSanitarioRepository.save(esquema);
        }
    }

    private EsquemaSanitarioPorcino crearEsquemaSanitario(String nombre, EsquemaSanitarioPorcino.TipoEsquema tipo, Empresa empresa) {
        EsquemaSanitarioPorcino esquema = new EsquemaSanitarioPorcino();
        esquema.setNombre(nombre);
        esquema.setTipo(tipo);
        esquema.setEmpresa(empresa);
        esquema.setActivo(true);
        return esquema;
    }

    private void inicializarTiposCorral(Empresa empresa) {
        List<TipoCorralPorcino> corralesPorDefecto = Arrays.asList(
            crearTipoCorral("Sala de Gestación", TipoCorralPorcino.TipoCorral.SALA_GESTACION, empresa),
            crearTipoCorral("Maternidad", TipoCorralPorcino.TipoCorral.MATERNIDAD, empresa),
            crearTipoCorral("Recría", TipoCorralPorcino.TipoCorral.RECRIA, empresa),
            crearTipoCorral("Engorde", TipoCorralPorcino.TipoCorral.ENGORDE, empresa),
            crearTipoCorral("Enfermería", TipoCorralPorcino.TipoCorral.ENFERMERIA, empresa)
        );

        for (TipoCorralPorcino corral : corralesPorDefecto) {
            if (tipoCorralRepository.findByEmpresaAndNombreAndActivoTrue(empresa, corral.getNombre()).isEmpty()) {
                tipoCorralRepository.save(corral);
            }
        }
    }

    private TipoCorralPorcino crearTipoCorral(String nombre, TipoCorralPorcino.TipoCorral tipo, Empresa empresa) {
        TipoCorralPorcino corral = new TipoCorralPorcino();
        corral.setNombre(nombre);
        corral.setTipo(tipo);
        corral.setEmpresa(empresa);
        corral.setActivo(true);
        return corral;
    }

    private void inicializarParametrosProductivos(Empresa empresa) {
        if (parametrosProductivosRepository.findByEmpresa(empresa).isEmpty()) {
            ParametrosProductivosPorcino parametros = new ParametrosProductivosPorcino(empresa);
            
            // Ciclo de producción
            parametros.setDiasPromedioGestacion(115);
            parametros.setDiasLactancia(21);
            parametros.setDiasRecriaAntesEngorde(60);
            parametros.setDiasEngorde(120);
            
            // Parámetros reproductivos adicionales
            parametros.setDiasToleranciaVencimientoGestacion(5);
            parametros.setDiasControlCelo(21);
            parametros.setDiasEntreCelos(21);
            parametros.setDiasPasajeMaternidad(7); // 7 días antes del parto
            
            // Servicios
            parametros.setCantidadMaximaServiciosPadrilloDia(3);
            parametros.setTiempoEsperaEntreServiciosHoras(12);
            
            // Alertas y recordatorios
            parametros.setDiasAntelacionAlertarPartos(7);
            parametros.setDiasAntelacionAlertarEcografias(3);
            parametros.setDiasAntelacionAlertarDestetes(2);
            parametros.setDiasAntelacionAlertarPasajeMaternidad(5);
            parametros.setDiasAntelacionAlertarRevisionesSanitarias(1);
            
            // Umbrales de mortalidad
            parametros.setUmbralMortalidadLactanciaPorcentaje(BigDecimal.valueOf(10.0));
            parametros.setUmbralMortalidadRecriaPorcentaje(BigDecimal.valueOf(5.0));
            parametros.setPorcentajeMinimoPrenezAntesAdvertencia(BigDecimal.valueOf(85.0));
            
            // Pesos estándar (en kg)
            parametros.setPesoPromedioNacimiento(BigDecimal.valueOf(1.5));
            parametros.setPesoDesteteObjetivo(BigDecimal.valueOf(7.0));
            parametros.setPesoVentaObjetivo(BigDecimal.valueOf(110.0));
            
            // Índices productivos objetivo
            parametros.setLechonesVivosPartoObjetivo(BigDecimal.valueOf(12.0));
            parametros.setLechonesDestetadosObjetivo(BigDecimal.valueOf(11.0));
            parametros.setPartosMadreAnioObjetivo(BigDecimal.valueOf(2.4));
            
            parametrosProductivosRepository.save(parametros);
        }
    }

    private void inicializarDatosEconomicos(Empresa empresa) {
        if (datosEconomicosRepository.findByEmpresa(empresa).isEmpty()) {
            DatosEconomicosPorcino datos = new DatosEconomicosPorcino(empresa);
            datos.setCostoMadreGestacionDia(BigDecimal.valueOf(15.0));
            datos.setCostoMadreLactanciaDia(BigDecimal.valueOf(25.0));
            datos.setCostoLechon(BigDecimal.valueOf(50.0));
            datos.setCostoEngordeDia(BigDecimal.valueOf(8.0));
            datos.setCostoManoObraDia(BigDecimal.valueOf(200.0));
            datos.setPrecioVentaCerdoTerminadoKg(BigDecimal.valueOf(180.0));
            datos.setPorcentajeMermaTransporte(BigDecimal.valueOf(2.0));
            datos.setKgMaizPorRacionEngorde(BigDecimal.valueOf(2.5));
            datos.setPorcentajeMezclaAlimentoPropioBalanceado(BigDecimal.valueOf(60.0));
            datos.setIndiceConversionObjetivo(BigDecimal.valueOf(3.2));
            datos.setMetodoImputacionCostoCultivo(DatosEconomicosPorcino.MetodoImputacionCosto.PROMEDIO_PONDERADO);
            datosEconomicosRepository.save(datos);
        }
    }
}

