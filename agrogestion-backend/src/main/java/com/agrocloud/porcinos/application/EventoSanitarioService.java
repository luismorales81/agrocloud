package com.agrocloud.porcinos.application;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.application.EmpresaContextService;
import com.agrocloud.core.application.port.LoteParaPorcinosQuery;

import com.agrocloud.core.inventory.domain.Insumo;
import com.agrocloud.porcinos.domain.EventoSanitario;
import com.agrocloud.porcinos.domain.Madre;
import com.agrocloud.porcinos.domain.Padrillo;
import com.agrocloud.porcinos.domain.Parto;
import com.agrocloud.porcinos.domain.Recria;
import com.agrocloud.porcinos.domain.TipoEventoSanitario;
import com.agrocloud.porcinos.infrastructure.EventoSanitarioRepository;
import com.agrocloud.porcinos.infrastructure.MadreRepository;
import com.agrocloud.porcinos.infrastructure.PadrilloRepository;
import com.agrocloud.porcinos.infrastructure.RecriaRepository;
import com.agrocloud.porcinos.infrastructure.TipoEventoSanitarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar eventos sanitarios
 */
@Service
public class EventoSanitarioService {

    @Autowired
    private EventoSanitarioRepository eventoSanitarioRepository;

    @Autowired
    private TipoEventoSanitarioRepository tipoEventoSanitarioRepository;

    @Autowired
    private MadreRepository madreRepository;

    @Autowired
    private PadrilloRepository padrilloRepository;

    @Autowired
    private RecriaRepository recriaRepository;

    @Autowired
    private EmpresaContextService empresaContextService;

    @Autowired
    private InventarioPorcinoService inventarioPorcinoService;

    @Autowired
    private com.agrocloud.porcinos.infrastructure.PartoRepository partoRepository;

    @Autowired
    private LoteParaPorcinosQuery loteParaPorcinosQuery;

    /**
     * Obtener eventos sanitarios por empresa
     */
    public List<EventoSanitario> obtenerEventosSanitarios(User user) {
        Optional<Empresa> empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaOpt.isEmpty()) {
            return List.of();
        }
        List<EventoSanitario> eventos = eventoSanitarioRepository.findByEmpresaAndActivoTrue(empresaOpt.get());
        // Poblar información adicional (entidadNombre)
        eventos.forEach(this::poblarInformacionEntidad);
        return eventos;
    }

    /**
     * Obtener eventos sanitarios por entidad (Madre, Padrillo o Lote)
     */
    public List<EventoSanitario> obtenerEventosPorEntidad(
            EventoSanitario.TipoEntidad tipoEntidad, Long entidadId, User user) {
        Optional<Empresa> empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaOpt.isEmpty()) {
            return List.of();
        }
        List<EventoSanitario> eventos = eventoSanitarioRepository.findByEmpresaAndTipoEntidadAndEntidadIdAndActivoTrue(
            empresaOpt.get(), tipoEntidad, entidadId);
        // Poblar información adicional (entidadNombre)
        eventos.forEach(this::poblarInformacionEntidad);
        return eventos;
    }

    /**
     * Obtener eventos sanitarios por rango de fechas
     */
    public List<EventoSanitario> obtenerEventosPorRangoFechas(
            LocalDate fechaInicio, LocalDate fechaFin, User user) {
        Optional<Empresa> empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaOpt.isEmpty()) {
            return List.of();
        }
        List<EventoSanitario> eventos = eventoSanitarioRepository.findByEmpresaAndFechaBetweenAndActivoTrue(
            empresaOpt.get(), fechaInicio, fechaFin);
        // Poblar información adicional (entidadNombre)
        eventos.forEach(this::poblarInformacionEntidad);
        return eventos;
    }

    /**
     * Crear o actualizar evento sanitario
     */
    @Transactional
    public EventoSanitario guardarEventoSanitario(EventoSanitario evento, User user) {
        Optional<Empresa> empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no tiene empresa activa");
        }

        Empresa empresa = empresaOpt.get();
        evento.setEmpresa(empresa);
        evento.setUsuario(user);

        // Validar tipo de evento sanitario
        if (evento.getTipoEventoSanitario() == null || evento.getTipoEventoSanitario().getId() == null) {
            throw new IllegalArgumentException("El tipo de evento sanitario es obligatorio");
        }

        Optional<TipoEventoSanitario> tipoOpt = tipoEventoSanitarioRepository
            .findByIdAndEmpresaAndActivoTrue(evento.getTipoEventoSanitario().getId(), empresa);
        if (tipoOpt.isEmpty()) {
            throw new IllegalArgumentException("Tipo de evento sanitario no encontrado");
        }
        evento.setTipoEventoSanitario(tipoOpt.get());

        // Validar entidad existe
        validarEntidadExiste(evento.getTipoEntidad(), evento.getEntidadId(), empresa);

        // Validar fecha de retiro si es requerida
        if (tipoOpt.get().getRequiereFechaRetiro() && evento.getFechaRetiro() == null) {
            throw new IllegalArgumentException("Este tipo de evento sanitario requiere fecha de retiro");
        }

        // Calcular fecha de retiro automáticamente si tiene días por defecto
        if (evento.getFechaRetiro() == null && tipoOpt.get().getDiasRetiroDefecto() != null) {
            evento.setFechaRetiro(evento.getFecha().plusDays(tipoOpt.get().getDiasRetiroDefecto()));
        }

        // Validar lote de medicamento si es requerido
        if (tipoOpt.get().getRequiereLoteMedicamento() && 
            (evento.getLoteMedicamento() == null || evento.getLoteMedicamento().trim().isEmpty())) {
            throw new IllegalArgumentException("Este tipo de evento sanitario requiere lote de medicamento");
        }

        // Validar y asociar insumo si está presente (referencia por id; validación de stock vía core.inventory)
        if (evento.getInsumo() != null && evento.getInsumo().getId() != null) {
            Long insumoId = evento.getInsumo().getId();
            Insumo ref = new Insumo();
            ref.setId(insumoId);
            evento.setInsumo(ref);
            if (evento.getDosis() != null) {
                inventarioPorcinoService.validarStockDisponible(empresa.getId(), insumoId, evento.getDosis());
            }
        }

        // Guardar evento sanitario
        EventoSanitario eventoSaved = eventoSanitarioRepository.save(evento);

        // Descontar stock después de guardar (para tener el ID del evento)
        if (eventoSaved.getInsumo() != null && eventoSaved.getInsumo().getId() != null && eventoSaved.getDosis() != null) {
            inventarioPorcinoService.descontarStockEventoSanitario(
                eventoSaved.getInsumo().getId(),
                eventoSaved.getDosis(),
                eventoSaved,
                user);
        }

        return eventoSaved;
    }

    /**
     * Validar que la entidad existe
     */
    private void validarEntidadExiste(EventoSanitario.TipoEntidad tipoEntidad, Long entidadId, Empresa empresa) {
        switch (tipoEntidad) {
            case MADRE:
                Optional<Madre> madre = madreRepository.findByIdAndEmpresa(entidadId, empresa);
                if (madre.isEmpty() || !madre.get().getActivo()) {
                    throw new IllegalArgumentException("Madre no encontrada o inactiva");
                }
                break;
            case PADRILLO:
                Optional<Padrillo> padrillo = padrilloRepository.findByIdAndEmpresa(entidadId, empresa);
                if (padrillo.isEmpty() || !padrillo.get().getActivo()) {
                    throw new IllegalArgumentException("Padrillo no encontrado o inactivo");
                }
                break;
            case LOTE:
                Optional<Recria> recria = recriaRepository.findByIdAndEmpresa(entidadId, empresa);
                if (recria.isEmpty() || !recria.get().getActivo()) {
                    throw new IllegalArgumentException("Lote no encontrado o inactivo");
                }
                break;
        }
    }

    /**
     * Obtener evento sanitario por ID
     */
    public Optional<EventoSanitario> obtenerEventoSanitarioPorId(Long id, User user) {
        Optional<Empresa> empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaOpt.isEmpty()) {
            return Optional.empty();
        }
        Optional<EventoSanitario> eventoOpt = eventoSanitarioRepository.findByIdAndEmpresaAndActivoTrue(id, empresaOpt.get());
        if (eventoOpt.isPresent()) {
            poblarInformacionEntidad(eventoOpt.get());
            return eventoOpt;
        }
        return Optional.empty();
    }

    /**
     * Eliminar (desactivar) evento sanitario
     */
    @Transactional
    public void eliminarEventoSanitario(Long id, User user) {
        Optional<EventoSanitario> eventoOpt = obtenerEventoSanitarioPorId(id, user);
        if (eventoOpt.isEmpty()) {
            throw new IllegalArgumentException("Evento sanitario no encontrado");
        }
        EventoSanitario evento = eventoOpt.get();
        
        // Restaurar stock si había insumo asociado y el evento estaba activo
        if (evento.getActivo() && evento.getInsumo() != null && evento.getInsumo().getId() != null && evento.getDosis() != null) {
            inventarioPorcinoService.restaurarStockEventoSanitario(
                evento.getInsumo().getId(),
                evento.getDosis(),
                evento,
                user);
        }
        
        evento.setActivo(false);
        eventoSanitarioRepository.save(evento);
    }

    /**
     * Marcar retiro como cumplido
     */
    @Transactional
    public void marcarRetiroCumplido(Long id, User user) {
        Optional<EventoSanitario> eventoOpt = obtenerEventoSanitarioPorId(id, user);
        if (eventoOpt.isEmpty()) {
            throw new IllegalArgumentException("Evento sanitario no encontrado");
        }
        EventoSanitario evento = eventoOpt.get();
        evento.setRetiroCumplido(true);
        eventoSanitarioRepository.save(evento);
    }

    /**
     * Obtener retiros vencidos
     */
    public List<EventoSanitario> obtenerRetirosVencidos(User user) {
        Optional<Empresa> empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaOpt.isEmpty()) {
            return List.of();
        }
        List<EventoSanitario> eventos = eventoSanitarioRepository.findRetirosVencidos(empresaOpt.get(), LocalDate.now());
        // Poblar información adicional (entidadNombre)
        eventos.forEach(this::poblarInformacionEntidad);
        return eventos;
    }

    /**
     * Obtener retiros próximos a vencer (próximos 7 días)
     */
    public List<EventoSanitario> obtenerRetirosProximosAVencer(User user) {
        Optional<Empresa> empresaOpt = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaOpt.isEmpty()) {
            return List.of();
        }
        LocalDate hoy = LocalDate.now();
        LocalDate en7Dias = hoy.plusDays(7);
        List<EventoSanitario> eventos = eventoSanitarioRepository.findRetirosProximosAVencer(empresaOpt.get(), hoy, en7Dias);
        // Poblar información adicional (entidadNombre)
        eventos.forEach(this::poblarInformacionEntidad);
        return eventos;
    }

    /**
     * Poblar información adicional de la entidad (entidadNombre)
     */
    private void poblarInformacionEntidad(EventoSanitario evento) {
        switch (evento.getTipoEntidad()) {
            case MADRE:
                Optional<Madre> madreOpt = madreRepository.findByIdAndActivoTrue(evento.getEntidadId());
                if (madreOpt.isPresent()) {
                    evento.setEntidadNombre(madreOpt.get().getIdentificacion());
                }
                break;
            case PADRILLO:
                Optional<Padrillo> padrilloOpt = padrilloRepository.findByIdAndActivoTrue(evento.getEntidadId());
                if (padrilloOpt.isPresent()) {
                    evento.setEntidadNombre(padrilloOpt.get().getIdentificacion());
                }
                break;
            case LOTE:
                // Para lotes (recrías), obtener el nombre del lote que ya tiene el formato "NombreMadre - FechaNacimiento"
                // Usar query con JOIN FETCH para cargar el lote
                Optional<Recria> recriaOpt = recriaRepository.findByIdAndActivoTrue(evento.getEntidadId());
                if (recriaOpt.isPresent()) {
                    Recria recria = recriaOpt.get();
                    // Obtener el nombre del lote asociado (ya está cargado con JOIN FETCH)
                    if (recria.getLoteId() != null) {
                        String nom = recria.getLoteNombre();
                        if (nom == null) {
                            nom = loteParaPorcinosQuery.obtenerPorId(recria.getLoteId()).map(dto -> dto.nombre()).orElse(null);
                        }
                        if (nom != null) {
                            evento.setEntidadNombre(nom);
                        } else {
                            buscarNombreLoteDesdeParto(recria, evento);
                        }
                    } else {
                        buscarNombreLoteDesdeParto(recria, evento);
                    }
                }
                break;
        }
    }

    /**
     * Buscar el nombre del lote desde el parto asociado
     */
    private void buscarNombreLoteDesdeParto(Recria recria, EventoSanitario evento) {
        List<Parto> partos = partoRepository.findByEmpresaAndActivoTrue(recria.getEmpresa());
        Optional<Parto> partoOpt = partos.stream()
            .filter(p -> p.getFechaInicio() != null && 
                        p.getFechaInicio().toLocalDate().equals(recria.getFechaIngreso()))
            .findFirst();
        if (partoOpt.isPresent() && partoOpt.get().getMadre() != null) {
            String nombreMadre = partoOpt.get().getMadre().getIdentificacion();
            String fechaNacimiento = recria.getFechaIngreso().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            evento.setEntidadNombre(nombreMadre + " - " + fechaNacimiento);
        } else {
            evento.setEntidadNombre("Lote ID: " + recria.getId());
        }
    }
}


















