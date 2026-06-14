package com.agrocloud.porcinos.application;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Recordatorio;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.application.EmpresaContextService;
import com.agrocloud.core.application.RecordatorioService;

import com.agrocloud.porcinos.domain.Destete;
import com.agrocloud.porcinos.domain.Gestacion;
import com.agrocloud.porcinos.domain.Madre;
import com.agrocloud.porcinos.domain.Parto;
import com.agrocloud.porcinos.infrastructure.DesteteRepository;
import com.agrocloud.porcinos.infrastructure.GestacionRepository;
import com.agrocloud.porcinos.infrastructure.HistorialEstadoMadreRepository;
import com.agrocloud.porcinos.infrastructure.MadreRepository;
import com.agrocloud.porcinos.infrastructure.MuerteLechonRepository;
import com.agrocloud.porcinos.infrastructure.PartoRepository;
import com.agrocloud.porcinos.infrastructure.LechonNNRepository;
import com.agrocloud.porcinos.domain.ParametrosProductivosPorcino;
import com.agrocloud.porcinos.infrastructure.ParametrosProductivosPorcinoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar partos
 * Incluye validaciones completas y automatismos para actualizar estados
 * NOTA: La recría se crea automáticamente cuando se registra el destete (ver DesteteService)
 */
@Service
public class PartoService {

    @Autowired
    private PartoRepository partoRepository;

    @Autowired
    private MadreRepository madreRepository;

    @Autowired
    private GestacionRepository gestacionRepository;

    @Autowired
    @SuppressWarnings("unused") // Reservado para futuras implementaciones
    private LechonNNRepository lechonNNRepository;

    @Autowired
    @SuppressWarnings("unused") // Reservado para futuras implementaciones
    private MuerteLechonRepository muerteLechonRepository;

    @Autowired
    private HistorialEstadoMadreRepository historialEstadoMadreRepository;

    @Autowired
    private ParametrosProductivosPorcinoRepository parametrosProductivosRepository;

    @Autowired
    private DesteteRepository desteteRepository;

    @Autowired
    private EmpresaContextService empresaContextService;

    @Autowired
    private RecordatorioService recordatorioService;

    @Autowired
    private MadreService madreService;

    /**
     * Registrar un parto con validaciones completas y automatismos
     */
    @Transactional
    public Parto registrarParto(Long madreId, Parto partoData, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            throw new IllegalArgumentException("Usuario no tiene empresa activa");
        }

        // Validar y obtener la madre
        Optional<Madre> madreOpt = madreRepository.findByIdAndActivoTrue(madreId);
        if (madreOpt.isEmpty()) {
            throw new IllegalArgumentException("La madre no existe o está inactiva");
        }
        Madre madre = madreOpt.get();

        // Validar empresa
        if (!madre.getEmpresa().getId().equals(empresaActiva.get().getId())) {
            throw new IllegalArgumentException("La madre no pertenece a la empresa activa");
        }

        // VALIDACIÓN: La madre debe estar registrada como gestante
        if (madre.getEstadoActual() != Madre.EstadoMadre.GESTACION) {
            throw new IllegalArgumentException("La madre debe estar en estado GESTACION para registrar un parto");
        }

        // Validar que existe una gestación (activa o finalizada recientemente)
        // Primero intenta buscar una gestación activa
        Optional<Gestacion> gestacionOpt = gestacionRepository.findActivaByMadre(madre);
        
        // Si no hay gestación activa, busca la última gestación finalizada de la madre
        // (permite registrar parto aunque la gestación se haya finalizado manualmente)
        if (gestacionOpt.isEmpty()) {
            List<Gestacion> gestacionesMadre = gestacionRepository.findByMadreAndActivoTrue(madre);
            gestacionOpt = gestacionesMadre.stream()
                .filter(g -> g.getEstado() == Gestacion.EstadoGestacion.FINALIZADA)
                .max(java.util.Comparator.comparing(Gestacion::getFechaProbableParto));
        }
        
        if (gestacionOpt.isEmpty()) {
            throw new IllegalArgumentException("No existe una gestación para esta madre. Debe haber una gestación activa o finalizada para registrar el parto");
        }
        Gestacion gestacion = gestacionOpt.get();

        // Validar que no haya un parto abierto previo
        List<Parto> partosAbiertos = partoRepository.findByMadreAndActivoTrue(madre);
        for (Parto partoExistente : partosAbiertos) {
            if (partoExistente.getFechaFin() == null) {
                throw new IllegalArgumentException("Ya existe un parto abierto para esta madre");
            }
        }

        // VALIDACIONES DE DATOS DEL PARTO
        validarDatosParto(partoData);

        // Configurar datos del parto
        partoData.setMadre(madre);
        partoData.setEmpresa(empresaActiva.get());
        partoData.setUsuario(user);
        partoData.setActivo(true);
        
        if (partoData.getFechaInicio() == null) {
            partoData.setFechaInicio(LocalDateTime.now());
        }

        // Calcular total nacidos si no se proporciona
        if (partoData.getTotalNacidos() == null) {
            int total = (partoData.getNacidosVivos() != null ? partoData.getNacidosVivos() : 0) +
                       (partoData.getNacidosMuertos() != null ? partoData.getNacidosMuertos() : 0) +
                       (partoData.getMomias() != null ? partoData.getMomias() : 0);
            partoData.setTotalNacidos(total);
        }

        partoData.setGestacion(gestacion);
        Parto partoGuardado = partoRepository.save(partoData);

        // AUTOMATISMO: Crear registros de lechones nacidos muertos si corresponde
        if (partoData.getNacidosMuertos() != null && partoData.getNacidosMuertos() > 0) {
            crearLechonesNacidosMuertos(partoGuardado, partoData.getNacidosMuertos(), user);
        }

        // NOTA: NO se crea recría automáticamente después del parto
        // Los lechones están en LACTANCIA con la madre
        // La recría se creará automáticamente cuando se registre el DESTETE
        // Ver: DesteteService.crearRecriaDesdeDestete()

        // AUTOMATISMO: Actualizar estado de la madre a EN_LACTANCIA
        madreService.actualizarEstadoMadre(madre, Madre.EstadoMadre.LACTANCIA, 
                             partoData.getFechaInicio().toLocalDate(), 
                             "Parto registrado: " + partoData.getNacidosVivos() + " vivos");

        // AUTOMATISMO: Cerrar gestación (si no está ya finalizada)
        if (gestacion.getEstado() != Gestacion.EstadoGestacion.FINALIZADA) {
            gestacion.setEstado(Gestacion.EstadoGestacion.FINALIZADA);
            gestacionRepository.save(gestacion);
        }

        // AUTOMATISMO: Crear recordatorio para Destete
        crearRecordatorioDestete(partoGuardado, user, empresaActiva.get());

        return partoGuardado;
    }

    /**
     * Validar datos del parto
     */
    private void validarDatosParto(Parto parto) {
        int vivos = parto.getNacidosVivos() != null ? parto.getNacidosVivos() : 0;
        int muertos = parto.getNacidosMuertos() != null ? parto.getNacidosMuertos() : 0;
        int momias = parto.getMomias() != null ? parto.getMomias() : 0;
        int total = vivos + muertos + momias;

        if (total == 0) {
            throw new IllegalArgumentException("Debe registrar al menos un lechón (vivo, muerto o momia)");
        }

        if (parto.getTotalNacidos() != null && parto.getTotalNacidos() != total) {
            throw new IllegalArgumentException(
                String.format("El total de nacidos (%d) debe ser igual a vivos (%d) + muertos (%d) + momias (%d)",
                    parto.getTotalNacidos(), vivos, muertos, momias));
        }

        if (vivos < 0 || muertos < 0 || momias < 0) {
            throw new IllegalArgumentException("Los valores de vivos, muertos y momias no pueden ser negativos");
        }
    }

    /**
     * Crear registros de lechones nacidos muertos
     */
    private void crearLechonesNacidosMuertos(Parto parto, Integer cantidad, User user) {
        // Nota: LechonNN no tiene relación directa con Parto o Madre
        // Se registra como lechones NN (sin origen claro) en el lote correspondiente
        // Por ahora solo registramos la cantidad en las observaciones del parto
        // En un sistema completo, se podría crear un registro de MuerteLechon con etapa NACIMIENTO
        String observaciones = parto.getObservaciones() != null ? parto.getObservaciones() : "";
        observaciones += String.format("\nLechones nacidos muertos registrados: %d", cantidad);
        parto.setObservaciones(observaciones);
    }

    /**
     * Crear recordatorio automático para Destete
     */
    private void crearRecordatorioDestete(Parto parto, User user, Empresa empresa) {
        try {
            Optional<ParametrosProductivosPorcino> parametrosOpt = 
                parametrosProductivosRepository.findByEmpresa(empresa);
            
            int diasLactancia = parametrosOpt
                .map(p -> p.getDiasLactancia() != null ? p.getDiasLactancia() : 21)
                .orElse(21);

            LocalDate fechaDestete = parto.getFechaInicio().toLocalDate().plusDays(diasLactancia);

            Recordatorio recordatorio = new Recordatorio();
            recordatorio.setTitulo("Destete - " + parto.getMadre().getIdentificacion());
            
            // Formatear fecha del parto de forma legible
            java.time.LocalDate fechaParto = parto.getFechaInicio().toLocalDate();
            java.time.format.DateTimeFormatter formatter = 
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy", java.util.Locale.forLanguageTag("es-ES"));
            String fechaPartoFormateada = fechaParto.format(formatter);
            
            recordatorio.setDescripcion(String.format(
                "Destete programado. %d lechones nacidos vivos. Parto del %s",
                parto.getNacidosVivos(),
                fechaPartoFormateada));
            recordatorio.setFecha(fechaDestete);
            recordatorio.setTipo(Recordatorio.TipoRecordatorio.PARTO);
            recordatorio.setUsuario(user);
            recordatorio.setPartoId(parto.getId());
            recordatorio.setMadreId(parto.getMadre().getId());
            recordatorio.setActivo(true);
            recordatorio.setCompletado(false);

            recordatorioService.crearRecordatorio(recordatorio);
        } catch (Exception e) {
            // Log error pero no fallar el proceso principal
            System.err.println("Error creando recordatorio de destete: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Cerrar un parto
     */
    @Transactional
    public Parto cerrarParto(Long partoId, LocalDateTime fechaFin, User user) {
        Optional<Parto> partoOpt = partoRepository.findByIdAndActivoTrue(partoId);
        if (partoOpt.isEmpty()) {
            throw new IllegalArgumentException("Parto no encontrado o inactivo");
        }

        Parto parto = partoOpt.get();
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty() || !parto.getEmpresa().getId().equals(empresaActiva.get().getId())) {
            throw new IllegalArgumentException("No tiene permisos para cerrar este parto");
        }

        // VALIDACIÓN: Advertir si hay destete pendiente (pero permitir cerrar)
        Optional<Destete> desteteOpt = desteteRepository.findByPartoAndActivoTrue(parto);
        if (desteteOpt.isEmpty()) {
            // No hay destete registrado - esto es válido, pero se puede registrar después
            // El sistema ahora permite registrar destetes aunque el parto esté cerrado
            // Advertencia: se puede agregar un mensaje de advertencia si es necesario
        }

        // VALIDACIÓN: Verificar que la fecha de fin no sea anterior a la fecha de inicio
        LocalDateTime fechaFinFinal = fechaFin != null ? fechaFin : LocalDateTime.now();
        if (fechaFinFinal.isBefore(parto.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio del parto");
        }

        parto.setFechaFin(fechaFinFinal);
        return partoRepository.save(parto);
    }

    /**
     * Obtener partos por empresa
     */
    public List<Parto> obtenerPartosPorEmpresa(User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }
        return partoRepository.findByEmpresaAndActivoTrue(empresaActiva.get());
    }

    /**
     * Obtener parto por ID
     */
    public Optional<Parto> obtenerPartoPorId(Long id, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return Optional.empty();
        }

        Optional<Parto> parto = partoRepository.findByIdAndActivoTrue(id);
        if (parto.isPresent() && parto.get().getEmpresa().getId().equals(empresaActiva.get().getId())) {
            return parto;
        }
        return Optional.empty();
    }

    /**
     * Obtener partos por madre
     */
    @Transactional(readOnly = true)
    public List<Parto> obtenerPartosPorMadre(Long madreId, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }

        Optional<Madre> madreOpt = madreRepository.findByIdAndActivoTrue(madreId);
        if (madreOpt.isEmpty()) {
            return List.of();
        }

        Madre madre = madreOpt.get();
        // Validar que la madre pertenezca a la empresa activa
        if (!madre.getEmpresa().getId().equals(empresaActiva.get().getId())) {
            return List.of();
        }

        return partoRepository.findByMadreAndActivoTrue(madre);
    }
}
