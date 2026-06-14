package com.agrocloud.porcinos.application;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.application.EmpresaContextService;

import com.agrocloud.porcinos.domain.Gestacion;
import com.agrocloud.porcinos.domain.HistorialEstadoMadre;
import com.agrocloud.porcinos.domain.Madre;
import com.agrocloud.porcinos.domain.Parto;
import com.agrocloud.porcinos.domain.Servicio;
import com.agrocloud.porcinos.infrastructure.GestacionRepository;
import com.agrocloud.porcinos.infrastructure.HistorialEstadoMadreRepository;
import com.agrocloud.porcinos.infrastructure.MadreRepository;
import com.agrocloud.porcinos.infrastructure.PartoRepository;
import com.agrocloud.porcinos.infrastructure.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MadreService {

    @Autowired
    private MadreRepository madreRepository;

    @Autowired
    private EmpresaContextService empresaContextService;

    @Autowired
    private GestacionRepository gestacionRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private PartoRepository partoRepository;

    @Autowired
    private ConfiguracionPorcinoService configuracionPorcinoService;

    @Autowired
    private HistorialEstadoMadreRepository historialEstadoMadreRepository;

    public List<Madre> getMadresByUser(User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }
        List<Madre> madres = madreRepository.findByEmpresaAndActivoTrue(empresaActiva.get());
        // Recalcular estado según edad para todas las cerdas y guardar las que cambiaron
        List<Madre> madresActualizadas = new java.util.ArrayList<>();
        for (Madre madre : madres) {
            if (madre.getEstadoActual() != Madre.EstadoMadre.GESTACION && 
                madre.getEstadoActual() != Madre.EstadoMadre.LACTANCIA) {
                Madre.EstadoMadre estadoAnterior = madre.getEstadoActual();
                calcularEstadoSegunEdad(madre, user);
                // Guardar solo si el estado cambió
                if (madre.getEstadoActual() != estadoAnterior) {
                    madresActualizadas.add(madre);
                }
            }
            // Cargar gestación activa y último servicio para serialización
        }
        cargarInformacionAdicionalEnLote(madres);
        // Guardar todas las cerdas actualizadas en batch
        if (!madresActualizadas.isEmpty()) {
            madreRepository.saveAll(madresActualizadas);
        }
        return madres;
    }

    public List<Madre> getMadresByUserAndEstado(User user, Madre.EstadoMadre estado) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }
        List<Madre> madres = madreRepository.findByEmpresaAndEstadoOptional(empresaActiva.get(), estado);
        // Recalcular estado según edad para todas las cerdas y guardar las que cambiaron
        List<Madre> madresActualizadas = new java.util.ArrayList<>();
        for (Madre madre : madres) {
            if (madre.getEstadoActual() != Madre.EstadoMadre.GESTACION && 
                madre.getEstadoActual() != Madre.EstadoMadre.LACTANCIA) {
                Madre.EstadoMadre estadoAnterior = madre.getEstadoActual();
                calcularEstadoSegunEdad(madre, user);
                // Guardar solo si el estado cambió
                if (madre.getEstadoActual() != estadoAnterior) {
                    madresActualizadas.add(madre);
                }
            }
            // Cargar gestación activa y último servicio para serialización
        }
        cargarInformacionAdicionalEnLote(madres);
        // Guardar todas las cerdas actualizadas en batch
        if (!madresActualizadas.isEmpty()) {
            madreRepository.saveAll(madresActualizadas);
        }
        return madres;
    }

    public Optional<Madre> getMadreById(Long id, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return Optional.empty();
        }
        Optional<Madre> madre = madreRepository.findByIdAndActivoTrue(id);
        if (madre.isPresent() && madre.get().getEmpresa().getId().equals(empresaActiva.get().getId())) {
            Madre madreEncontrada = madre.get();
            // Recalcular y actualizar estado según edad si no está en gestación o lactancia
            if (madreEncontrada.getEstadoActual() != Madre.EstadoMadre.GESTACION && 
                madreEncontrada.getEstadoActual() != Madre.EstadoMadre.LACTANCIA) {
                Madre.EstadoMadre estadoAnterior = madreEncontrada.getEstadoActual();
                calcularEstadoSegunEdad(madreEncontrada, user);
                // Guardar solo si el estado cambió
                if (madreEncontrada.getEstadoActual() != estadoAnterior) {
                    madreRepository.save(madreEncontrada);
                }
            }
            // Cargar gestación activa y último servicio para serialización
            cargarInformacionAdicional(madreEncontrada);
            return Optional.of(madreEncontrada);
        }
        return Optional.empty();
    }

    @Transactional
    public Madre crearMadre(Madre madre, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            throw new IllegalArgumentException("Usuario no tiene empresa activa");
        }

        // VALIDACIÓN: Verificar que la identificación no esté vacía
        if (madre.getIdentificacion() == null || madre.getIdentificacion().trim().isEmpty()) {
            throw new IllegalArgumentException("El código de identificación de la cerda es obligatorio");
        }

        // VALIDACIÓN: Verificar que el código no esté duplicado en la misma empresa
        String codigoLimpio = madre.getIdentificacion().trim();
        Optional<Madre> madreExistente = madreRepository.findByIdentificacionAndEmpresaAndActivoTrue(
            codigoLimpio, empresaActiva.get());
        if (madreExistente.isPresent()) {
            throw new IllegalArgumentException(
                String.format("El código '%s' ya está siendo utilizado por otra cerda activa en esta empresa", codigoLimpio));
        }

        madre.setEmpresa(empresaActiva.get());
        madre.setUsuario(user);
        madre.setActivo(true); // Asegurar que esté activa al crear
        
        // Calcular estado inicial basado en la edad (solo si no tiene gestación/lactancia activa)
        calcularEstadoSegunEdad(madre, user);
        
        return madreRepository.save(madre);
    }

    @Transactional
    public Madre actualizarMadre(Long id, Madre madreData, User user) {
        Optional<Madre> madreOpt = getMadreById(id, user);
        if (madreOpt.isEmpty()) {
            throw new IllegalArgumentException("Madre no encontrada");
        }
        Madre madre = madreOpt.get();

        // VALIDACIÓN: Si se cambia la identificación, verificar que no esté duplicada
        if (madreData.getIdentificacion() != null && !madreData.getIdentificacion().equals(madre.getIdentificacion())) {
            String codigoLimpio = madreData.getIdentificacion().trim();
            if (codigoLimpio.isEmpty()) {
                throw new IllegalArgumentException("El código de identificación de la cerda no puede estar vacío");
            }
            Optional<Madre> madreConMismaIdentificacion = madreRepository.findByIdentificacionAndEmpresaAndActivoTrue(
                codigoLimpio, madre.getEmpresa());
            if (madreConMismaIdentificacion.isPresent() && !madreConMismaIdentificacion.get().getId().equals(madre.getId())) {
                throw new IllegalArgumentException(
                    String.format("El código '%s' ya está siendo utilizado por otra cerda activa en esta empresa", codigoLimpio));
            }
            madre.setIdentificacion(codigoLimpio);
        }

        if (madreData.getFechaNacimiento() != null) {
            madre.setFechaNacimiento(madreData.getFechaNacimiento());
            // Si se cambia la fecha de nacimiento, recalcular el estado según la edad
            // pero solo si no está en gestación o lactancia
            if (madre.getEstadoActual() != Madre.EstadoMadre.GESTACION && 
                madre.getEstadoActual() != Madre.EstadoMadre.LACTANCIA) {
                calcularEstadoSegunEdad(madre, user);
            }
        }
        if (madreData.getCantidadTetas() != null) madre.setCantidadTetas(madreData.getCantidadTetas());
        if (madreData.getOrigen() != null) madre.setOrigen(madreData.getOrigen());
        // Solo actualizar estado manualmente si no está en gestación o lactancia
        if (madreData.getEstadoActual() != null && 
            madre.getEstadoActual() != Madre.EstadoMadre.GESTACION && 
            madre.getEstadoActual() != Madre.EstadoMadre.LACTANCIA) {
            madre.setEstadoActual(madreData.getEstadoActual());
        }
        if (madreData.getFechaIngresoGranja() != null) madre.setFechaIngresoGranja(madreData.getFechaIngresoGranja());
        if (madreData.getObservaciones() != null) madre.setObservaciones(madreData.getObservaciones());
        return madreRepository.save(madre);
    }

    @Transactional
    public void eliminarMadre(Long id, User user) {
        Optional<Madre> madre = getMadreById(id, user);
        if (madre.isEmpty()) {
            throw new IllegalArgumentException("Madre no encontrada");
        }

        Madre madreToDelete = madre.get();

        // VALIDACIÓN DE INTEGRIDAD: No se puede eliminar una madre con gestaciones activas
        Optional<Gestacion> gestacionActiva = gestacionRepository.findActivaByMadre(madreToDelete);
        if (gestacionActiva.isPresent()) {
            throw new IllegalArgumentException(
                "No se puede eliminar una madre con gestación activa. Debe cerrar la gestación primero.");
        }

        // VALIDACIÓN DE INTEGRIDAD: No se puede eliminar una madre con servicios pendientes
        Optional<Servicio> servicioPendiente = servicioRepository.findPendienteControlByMadre(madreToDelete);
        if (servicioPendiente.isPresent()) {
            throw new IllegalArgumentException(
                "No se puede eliminar una madre con servicio pendiente de control. Debe cerrar el servicio primero.");
        }

        // VALIDACIÓN DE INTEGRIDAD: No se puede eliminar una madre con partos abiertos
        List<Parto> partosAbiertos = partoRepository.findByMadreAndActivoTrue(madreToDelete);
        for (Parto parto : partosAbiertos) {
            if (parto.getFechaFin() == null) {
                throw new IllegalArgumentException(
                    "No se puede eliminar una madre con parto abierto. Debe cerrar el parto primero.");
            }
        }

        // Baja lógica: marcar como inactiva en lugar de eliminar físicamente
        madreToDelete.setActivo(false);
        madreRepository.save(madreToDelete);
    }

    /**
     * Calcula el estado de la cerda basándose en su edad
     * Una cerda es CACHORRA hasta los DIAS_CACHORRA (por defecto 160 días)
     * Después de eso es ADULTA (a menos que esté en GESTACION o LACTANCIA)
     */
    private void calcularEstadoSegunEdad(Madre madre, User user) {
        if (madre.getFechaNacimiento() == null) {
            return; // No se puede calcular sin fecha de nacimiento
        }

        // Obtener días cachorra de la configuración (por defecto 160 días)
        int diasCachorra = configuracionPorcinoService.obtenerValorInteger("DIAS_CACHORRA", 160, user);
        
        // Calcular edad en días
        LocalDate fechaActual = LocalDate.now();
        long edadEnDias = ChronoUnit.DAYS.between(madre.getFechaNacimiento(), fechaActual);
        
        // Si la cerda tiene gestación o lactancia activa, no cambiar el estado
        if (madre.getEstadoActual() == Madre.EstadoMadre.GESTACION || 
            madre.getEstadoActual() == Madre.EstadoMadre.LACTANCIA) {
            return;
        }
        
        // Determinar estado según edad
        if (edadEnDias < diasCachorra) {
            // Menor a los días cachorra → CACHORRA
            madre.setEstadoActual(Madre.EstadoMadre.CACHORRA);
        } else {
            // Mayor o igual a los días cachorra → ADULTA
            madre.setEstadoActual(Madre.EstadoMadre.ADULTA);
        }
    }

    /**
     * Carga gestaciones activas y ultimos servicios en batch (2 consultas en lugar de N+1).
     */
    private void cargarInformacionAdicionalEnLote(List<Madre> madres) {
        if (madres == null || madres.isEmpty()) {
            return;
        }

        Map<Long, Gestacion> gestacionesActivas = gestacionRepository.findActivasByMadreIn(madres).stream()
                .collect(Collectors.toMap(g -> g.getMadre().getId(), g -> g, (actual, duplicado) -> actual));

        Map<Long, Servicio> ultimosServicios = new HashMap<>();
        for (Servicio servicio : servicioRepository.findByMadreInAndActivoTrue(madres)) {
            Long madreId = servicio.getMadre().getId();
            ultimosServicios.merge(madreId, servicio, (previo, actual) ->
                    actual.getFechaServicio().isAfter(previo.getFechaServicio()) ? actual : previo);
        }

        for (Madre madre : madres) {
            Gestacion gestacion = gestacionesActivas.get(madre.getId());
            if (gestacion != null) {
                gestacion.getFechaProbableParto();
                gestacion.getFechaInicio();
                gestacion.getId();
                madre.setGestacionActivaTransient(gestacion);
            }

            Servicio ultimoServicio = ultimosServicios.get(madre.getId());
            if (ultimoServicio != null) {
                ultimoServicio.getFechaServicio();
                ultimoServicio.getId();
                madre.setUltimoServicioTransient(ultimoServicio);
            }
        }
    }

    /**
     * Cargar información adicional de una madre (gestación activa y último servicio)
     * y asignarla a campos transitorios para serialización JSON
     */
    private void cargarInformacionAdicional(Madre madre) {
        cargarInformacionAdicionalEnLote(List.of(madre));
    }

    /**
     * Método centralizado para actualizar el estado de la madre y registrar en historial
     * Este método debe ser usado por todos los servicios que necesiten cambiar el estado de una madre
     * para mantener consistencia en el historial y evitar código duplicado
     */
    @Transactional
    public void actualizarEstadoMadre(Madre madre, Madre.EstadoMadre nuevoEstado, 
                                     LocalDate fechaCambio, String observacion) {
        // Cerrar estado anterior en historial
        List<HistorialEstadoMadre> historialesAbiertos = historialEstadoMadreRepository
            .findByMadreAndFechaFinIsNull(madre);
        
        for (HistorialEstadoMadre historial : historialesAbiertos) {
            if (historial.getEstado() == madre.getEstadoActual()) {
                historial.setFechaFin(fechaCambio.minusDays(1));
                historialEstadoMadreRepository.save(historial);
            }
        }

        // Crear nuevo registro en historial
        HistorialEstadoMadre nuevoHistorial = new HistorialEstadoMadre();
        nuevoHistorial.setMadre(madre);
        nuevoHistorial.setEstado(nuevoEstado);
        nuevoHistorial.setFechaInicio(fechaCambio);
        nuevoHistorial.setObservaciones(observacion);
        historialEstadoMadreRepository.save(nuevoHistorial);

        // Actualizar estado actual de la madre
        madre.setEstadoActual(nuevoEstado);
        madreRepository.save(madre);
    }
}

