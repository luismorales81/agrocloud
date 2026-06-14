package com.agrocloud.porcinos.application;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.application.EmpresaContextService;
import com.agrocloud.core.application.port.CrearLotePorcinoPort;

import com.agrocloud.porcinos.domain.Destete;
import com.agrocloud.porcinos.domain.Madre;
import com.agrocloud.porcinos.domain.Parto;
import com.agrocloud.porcinos.domain.Recria;
import com.agrocloud.porcinos.infrastructure.DesteteRepository;
import com.agrocloud.porcinos.infrastructure.PartoRepository;
import com.agrocloud.porcinos.infrastructure.MadreRepository;
import com.agrocloud.porcinos.infrastructure.RecriaRepository;
import com.agrocloud.porcinos.infrastructure.HistorialEstadoMadreRepository;
import com.agrocloud.porcinos.domain.ParametrosProductivosPorcino;
import com.agrocloud.porcinos.infrastructure.ParametrosProductivosPorcinoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar destetes
 * Incluye validaciones de pesos y automatismos para cerrar lactancia y crear recría
 */
@Service
public class DesteteService {

    @Autowired
    private DesteteRepository desteteRepository;

    @Autowired
    private PartoRepository partoRepository;

    @Autowired
    private MadreRepository madreRepository;

    @Autowired
    private RecriaRepository recriaRepository;


    @Autowired
    private ParametrosProductivosPorcinoRepository parametrosProductivosRepository;

    @Autowired
    private EmpresaContextService empresaContextService;

    @Autowired
    private CrearLotePorcinoPort crearLotePorcinoPort;

    @Autowired
    private MadreService madreService;

    /**
     * Registrar un destete con validaciones completas
     */
    @Transactional
    public Destete registrarDestete(Long partoId, Destete desteteData, User user) {
        Optional<Parto> partoOpt = partoRepository.findByIdAndActivoTrue(partoId);
        if (partoOpt.isEmpty()) {
            throw new IllegalArgumentException("Parto no encontrado o inactivo");
        }

        Parto parto = partoOpt.get();
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty() || !parto.getEmpresa().getId().equals(empresaActiva.get().getId())) {
            throw new IllegalArgumentException("No tiene permisos para registrar destete en este parto");
        }

        // Validar que no haya un destete previo
        Optional<Destete> desteteExistente = desteteRepository.findByPartoAndActivoTrue(parto);
        if (desteteExistente.isPresent()) {
            throw new IllegalArgumentException("Ya existe un destete registrado para este parto");
        }

        // Validar que la cantidad destetada no exceda los nacidos vivos
        if (desteteData.getCantidadDestetados() > parto.getNacidosVivos()) {
            throw new IllegalArgumentException(
                String.format("La cantidad destetada (%d) no puede ser mayor a los nacidos vivos (%d)",
                    desteteData.getCantidadDestetados(), parto.getNacidosVivos()));
        }

        // VALIDAR PESOS AL DESTETE usando parámetros productivos
        validarPesoDestete(desteteData.getPesoPromedioDestete(), empresaActiva.get());

        // Calcular días de lactancia si no se proporciona
        if (desteteData.getDiasLactancia() == null) {
            // Si el parto está cerrado, calcular desde fechaInicio hasta fechaFin del parto
            // Si el parto está abierto, calcular hasta fechaDestete
            LocalDate fechaFinReferencia = parto.getFechaFin() != null 
                ? parto.getFechaFin().toLocalDate() 
                : desteteData.getFechaDestete();
            long dias = ChronoUnit.DAYS.between(parto.getFechaInicio().toLocalDate(), fechaFinReferencia);
            desteteData.setDiasLactancia((int) dias);
        }
        
        // AUTOMATISMO / VALIDACIÓN: parto.fechaFin y fechaDestete
        if (parto.getFechaFin() == null && desteteData.getFechaDestete() != null) {
            parto.setFechaFin(desteteData.getFechaDestete().atTime(parto.getFechaInicio().getHour(), 
                                                                   parto.getFechaInicio().getMinute()));
            partoRepository.save(parto);
        } else if (parto.getFechaFin() != null && desteteData.getFechaDestete() != null) {
            if (parto.getFechaFin().toLocalDate().isBefore(desteteData.getFechaDestete())) {
                throw new IllegalArgumentException(
                    "La fecha de fin del parto no puede ser anterior a la fecha del destete");
            }
        }

        desteteData.setParto(parto);
        desteteData.setEmpresa(empresaActiva.get());
        desteteData.setUsuario(user);
        desteteData.setActivo(true);

        Destete desteteGuardado = desteteRepository.save(desteteData);

        // AUTOMATISMO: Cerrar la lactancia de la madre
        Madre madre = parto.getMadre();
        madreService.actualizarEstadoMadre(madre, Madre.EstadoMadre.ADULTA, 
                             desteteData.getFechaDestete(), 
                             "Destete registrado: " + desteteData.getCantidadDestetados() + " lechones");

        // AUTOMATISMO: Crear recría automáticamente desde el destete
        // La recría se crea cuando los lechones son separados de la madre (destete)
        crearRecriaDesdeDestete(parto, desteteGuardado, user);

        return desteteGuardado;
    }

    /**
     * Validar peso al destete según parámetros configurados
     */
    private void validarPesoDestete(BigDecimal pesoPromedio, Empresa empresa) {
        if (pesoPromedio == null) {
            throw new IllegalArgumentException("El peso promedio al destete es obligatorio");
        }

        // Validar que el peso sea positivo
        if (pesoPromedio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El peso promedio al destete debe ser mayor a cero");
        }

        Optional<ParametrosProductivosPorcino> parametrosOpt = 
            parametrosProductivosRepository.findByEmpresa(empresa);

        if (parametrosOpt.isPresent()) {
            ParametrosProductivosPorcino parametros = parametrosOpt.get();
            
            // Usar pesoDesteteObjetivo de la configuración si está disponible
            if (parametros.getPesoDesteteObjetivo() != null) {
                BigDecimal pesoObjetivo = parametros.getPesoDesteteObjetivo();
                // Validar que el peso esté dentro de un rango razonable (±50% del objetivo)
                BigDecimal pesoMinimo = pesoObjetivo.multiply(BigDecimal.valueOf(0.5));
                BigDecimal pesoMaximo = pesoObjetivo.multiply(BigDecimal.valueOf(1.5));
                
                if (pesoPromedio.compareTo(pesoMinimo) < 0) {
                    throw new IllegalArgumentException(
                        String.format("El peso promedio al destete (%.2f kg) es muy bajo. Peso objetivo configurado: %.2f kg",
                            pesoPromedio, pesoObjetivo));
                }
                
                if (pesoPromedio.compareTo(pesoMaximo) > 0) {
                    throw new IllegalArgumentException(
                        String.format("El peso promedio al destete (%.2f kg) es muy alto. Peso objetivo configurado: %.2f kg",
                            pesoPromedio, pesoObjetivo));
                }
            } else {
                // Fallback a rangos básicos si no hay parámetro configurado
                if (pesoPromedio.compareTo(BigDecimal.valueOf(3)) < 0) {
                    throw new IllegalArgumentException("El peso promedio al destete es muy bajo (mínimo esperado: 3 kg)");
                }

                if (pesoPromedio.compareTo(BigDecimal.valueOf(12)) > 0) {
                    throw new IllegalArgumentException("El peso promedio al destete es muy alto (máximo esperado: 12 kg)");
                }
            }
        }
    }

    /**
     * Crear recría automáticamente desde el destete
     * La recría se crea cuando los lechones son separados de la madre (destete)
     * Fecha de ingreso a recría = fecha de destete (cuando se separan de la madre)
     */
    private void crearRecriaDesdeDestete(Parto parto, Destete destete, User user) {
        // Obtener o crear un lote automáticamente para la recría
        // El nombre del lote debe ser: "NombreMadre - FechaNacimiento" (fecha del parto, no del destete)
        LocalDate fechaNacimiento = parto.getFechaInicio().toLocalDate();
        Madre madre = parto.getMadre();
        String nombreSugerido = madre.getIdentificacion() + " - " + fechaNacimiento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Long loteId = crearLotePorcinoPort.crearOObtenerLotePorcino(parto.getEmpresa().getId(), user.getId(), nombreSugerido);

        Recria nuevaRecria = new Recria();
        nuevaRecria.setLoteId(loteId);
        nuevaRecria.setFechaIngreso(destete.getFechaDestete()); // Fecha de ingreso = fecha de destete
        nuevaRecria.setCantidadAnimales(destete.getCantidadDestetados()); // Cantidad destetados, no nacidos vivos
        BigDecimal pesoDestete = destete.getPesoPromedioDestete();
        nuevaRecria.setPesoPromedio(pesoDestete); // Peso actual = peso al destete hasta primera pesada
        nuevaRecria.setPesoInicialKg(pesoDestete); // Peso inicial (no se sobrescribe con pesadas)
        nuevaRecria.setFechaInicioEtapa(destete.getFechaDestete()); // Inicio de etapa F1
        nuevaRecria.setEtapa(Recria.EtapaRecria.F1);
        // Origen explícito: esta recría proviene de un destete interno
        nuevaRecria.setOrigen(Recria.OrigenRecria.DESTETE);
        nuevaRecria.setEmpresa(parto.getEmpresa());
        nuevaRecria.setUsuario(user);
        nuevaRecria.setActivo(true);
        
        // Determinar sexo según el parto (por ahora usar HEMBRA por defecto, puede mejorarse)
        nuevaRecria.setSexo(Recria.Sexo.HEMBRA);
        
        recriaRepository.save(nuevaRecria);
    }

    public Optional<Destete> obtenerDestetePorParto(Long partoId, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return Optional.empty();
        }

        Optional<Parto> parto = partoRepository.findById(partoId);
        if (parto.isEmpty() || !parto.get().getEmpresa().getId().equals(empresaActiva.get().getId())) {
            return Optional.empty();
        }

        return desteteRepository.findByPartoAndActivoTrue(parto.get());
    }

    public List<Destete> obtenerTodosLosDestetes(User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }

        return desteteRepository.findByEmpresaAndActivoTrue(empresaActiva.get());
    }

    public Optional<Destete> obtenerDestetePorId(Long id, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return Optional.empty();
        }

        Optional<Destete> destete = desteteRepository.findByIdWithRelations(id);
        if (destete.isEmpty() || !destete.get().getActivo() || 
            !destete.get().getEmpresa().getId().equals(empresaActiva.get().getId())) {
            return Optional.empty();
        }

        return destete;
    }
}
