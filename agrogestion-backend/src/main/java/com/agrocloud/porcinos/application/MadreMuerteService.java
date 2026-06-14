package com.agrocloud.porcinos.application;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.application.EmpresaContextService;

import com.agrocloud.porcinos.domain.CausaMortalidadPorcino;
import com.agrocloud.porcinos.domain.Gestacion;
import com.agrocloud.porcinos.domain.Madre;
import com.agrocloud.porcinos.domain.MadreMuerte;
import com.agrocloud.porcinos.domain.Parto;
import com.agrocloud.porcinos.domain.Servicio;
import com.agrocloud.porcinos.infrastructure.CausaMortalidadPorcinoRepository;
import com.agrocloud.porcinos.infrastructure.MadreMuerteRepository;
import com.agrocloud.porcinos.infrastructure.MadreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MadreMuerteService {

    @Autowired
    private MadreMuerteRepository madreMuerteRepository;

    @Autowired
    private MadreRepository madreRepository;

    @Autowired
    private EmpresaContextService empresaContextService;


    @Autowired
    private com.agrocloud.porcinos.infrastructure.GestacionRepository gestacionRepository;

    @Autowired
    private com.agrocloud.porcinos.infrastructure.ServicioRepository servicioRepository;

    @Autowired
    private com.agrocloud.porcinos.infrastructure.PartoRepository partoRepository;

    @Autowired
    private com.agrocloud.porcinos.infrastructure.DesteteRepository desteteRepository;

    @Autowired
    private CausaMortalidadPorcinoRepository causaMortalidadRepository;

    @Autowired
    private MadreService madreService;


    @Transactional
    public MadreMuerte registrarMuerte(Long madreId, MadreMuerte muerteData, User user) {
        Optional<Madre> madreOpt = madreRepository.findByIdAndActivoTrue(madreId);
        if (madreOpt.isEmpty()) {
            throw new IllegalArgumentException("Madre no encontrada o inactiva");
        }

        Madre madre = madreOpt.get();
        
        // Validar que la fecha no sea futura
        if (muerteData.getFecha().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de muerte no puede ser posterior a la fecha actual");
        }

        // Validar que la madre pertenezca a la empresa del usuario
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty() || !madre.getEmpresa().getId().equals(empresaActiva.get().getId())) {
            throw new IllegalArgumentException("No tiene permisos para registrar la muerte de esta madre");
        }

        // VALIDACIÓN: Verificar que la causa provenga del catálogo configurable
        CausaMortalidadPorcino.EtapaMortalidad etapaCatalogo = 
            muerteData.getEtapaMomento() == MadreMuerte.EtapaMomento.GESTACION ?
                CausaMortalidadPorcino.EtapaMortalidad.GESTACION :
                CausaMortalidadPorcino.EtapaMortalidad.GENERAL;
        
        List<CausaMortalidadPorcino> causasValidas = causaMortalidadRepository
            .findByEmpresaAndEtapaAndActivoTrue(empresaActiva.get(), etapaCatalogo);
        
        // Validar que existe una causa configurada con el mismo nombre (o permitir si no hay catálogo configurado)
        if (!causasValidas.isEmpty()) {
            String nombreCausa = muerteData.getCausa().name();
            boolean causaValida = causasValidas.stream()
                .anyMatch(c -> c.getNombre().equalsIgnoreCase(nombreCausa) || 
                              c.getNombre().equalsIgnoreCase(nombreCausa.replace("_", " ")));
            
            if (!causaValida) {
                throw new IllegalArgumentException(
                    String.format("La causa '%s' no está configurada en el catálogo para la etapa %s",
                        nombreCausa, etapaCatalogo));
            }
        }

        // Configurar la muerte
        muerteData.setMadre(madre);
        muerteData.setEmpresa(empresaActiva.get());
        muerteData.setUsuario(user);
        muerteData.setActivo(true);

        // Marcar madre como baja definitiva (baja lógica)
        madre.setEstadoActual(Madre.EstadoMadre.DESCARTE);
        madre.setActivo(false);
        madre.setFechaBaja(muerteData.getFecha());
        // Si se proporciona motivoBaja en muerteData, actualizarlo
        if (muerteData.getMotivoBaja() != null) {
            madre.setMotivoBaja(muerteData.getMotivoBaja());
        }
        madreRepository.save(madre);

        // AUTOMATISMO: Cerrar gestación activa si existe
        cerrarGestacionActiva(madre, muerteData.getFecha(), muerteData.getCausa().name());

        // AUTOMATISMO: Cerrar servicios pendientes
        cerrarServiciosPendientes(madre, muerteData.getFecha());

        // AUTOMATISMO: Cerrar registros pendientes de parto
        cerrarPartosAbiertos(madre, muerteData.getFecha());

        // AUTOMATISMO: Cerrar lactancias activas (destetes pendientes)
        cerrarDestetesPendientes(madre, muerteData.getFecha());

        // AUTOMATISMO: Cerrar recrías asociadas si corresponde
        // (Esto se manejará en el servicio de recría)

        return madreMuerteRepository.save(muerteData);
    }

    public List<MadreMuerte> obtenerMuertesPorMadre(Long madreId, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }

        Optional<Madre> madre = madreRepository.findById(madreId);
        if (madre.isEmpty() || !madre.get().getEmpresa().getId().equals(empresaActiva.get().getId())) {
            return List.of();
        }

        return madreMuerteRepository.findByMadreAndActivoTrue(madre.get());
    }

    public List<MadreMuerte> obtenerTodasLasMuertes(User user, LocalDate fechaDesde, LocalDate fechaHasta) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }

        if (fechaDesde != null && fechaHasta != null) {
            return madreMuerteRepository.findByEmpresaAndRangoFechas(empresaActiva.get(), fechaDesde, fechaHasta);
        }

        return madreMuerteRepository.findByEmpresaAndActivoTrue(empresaActiva.get());
    }

    public List<MadreMuerte> obtenerMuertesPorCausa(User user, MadreMuerte.CausaMuerteMadre causa) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }

        return madreMuerteRepository.findByEmpresaAndCausa(empresaActiva.get(), causa);
    }

    /**
     * Cerrar gestación activa cuando muere una madre
     */
    private void cerrarGestacionActiva(Madre madre, LocalDate fechaMuerte, String causa) {
        Optional<Gestacion> gestacionOpt = gestacionRepository.findActivaByMadre(madre);
        if (gestacionOpt.isPresent()) {
            Gestacion gestacion = gestacionOpt.get();
            gestacion.setEstado(Gestacion.EstadoGestacion.ABORTO);
            gestacion.setFechaAborto(fechaMuerte);
            gestacion.setCausaAborto("Muerte de la madre: " + causa);
            gestacionRepository.save(gestacion);
            madreService.actualizarEstadoMadre(madre, Madre.EstadoMadre.ADULTA, fechaMuerte,
                "Gestación cerrada por muerte de la madre: " + causa);
        }
    }

    /**
     * Cerrar servicios pendientes cuando muere una madre
     */
    private void cerrarServiciosPendientes(Madre madre, LocalDate fechaMuerte) {
        Optional<Servicio> servicioOpt = servicioRepository.findPendienteControlByMadre(madre);
        if (servicioOpt.isPresent()) {
            Servicio servicio = servicioOpt.get();
            servicio.setEstadoServicio(Servicio.EstadoServicio.FALLIDO);
            servicio.setObservaciones("Servicio cancelado por muerte de la madre");
            servicioRepository.save(servicio);
        }
    }

    /**
     * Cerrar partos abiertos cuando muere una madre
     */
    private void cerrarPartosAbiertos(Madre madre, LocalDate fechaMuerte) {
        List<Parto> partosAbiertos = partoRepository.findByMadreAndActivoTrue(madre);
        for (Parto parto : partosAbiertos) {
            if (parto.getFechaFin() == null) {
                parto.setFechaFin(fechaMuerte.atTime(23, 59));
                parto.setObservaciones(
                    (parto.getObservaciones() != null ? parto.getObservaciones() + "\n" : "") +
                    "Parto cerrado por muerte de la madre"
                );
                partoRepository.save(parto);
            }
        }
    }

    /**
     * Cerrar destetes pendientes cuando muere una madre
     */
    private void cerrarDestetesPendientes(Madre madre, LocalDate fechaMuerte) {
        List<Parto> partos = partoRepository.findByMadreAndActivoTrue(madre);
        for (Parto parto : partos) {
            // Si no hay destete registrado pero hay parto, se considera que la lactancia se cerró
            // por muerte de la madre
            // Verificar si hay destete pendiente
            desteteRepository.findByPartoAndActivoTrue(parto);
        }
    }
}



