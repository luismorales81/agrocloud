package com.agrocloud.porcinos.application;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.application.EmpresaContextService;

import com.agrocloud.porcinos.domain.CausaMortalidadPorcino;
import com.agrocloud.porcinos.domain.Madre;
import com.agrocloud.porcinos.domain.MuerteLechon;
import com.agrocloud.porcinos.domain.Parto;
import com.agrocloud.porcinos.infrastructure.CausaMortalidadPorcinoRepository;
import com.agrocloud.porcinos.infrastructure.MadreRepository;
import com.agrocloud.porcinos.infrastructure.MuerteLechonRepository;
import com.agrocloud.porcinos.infrastructure.PartoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MuerteLechonService {

    @Autowired
    private MuerteLechonRepository muerteLechonRepository;

    @Autowired
    private PartoRepository partoRepository;

    @Autowired
    private EmpresaContextService empresaContextService;

    @Autowired
    private MadreRepository madreRepository;

    @Autowired
    private CausaMortalidadPorcinoRepository causaMortalidadRepository;

    @Transactional
    public MuerteLechon registrarMuerte(Long partoId, MuerteLechon muerteData, User user) {
        Optional<Parto> partoOpt = partoRepository.findByIdAndActivoTrue(partoId);
        if (partoOpt.isEmpty()) {
            throw new IllegalArgumentException("Parto no encontrado o inactivo");
        }

        Parto parto = partoOpt.get();

        // Validar que la fecha no sea futura
        if (muerteData.getFecha().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de muerte no puede ser posterior a la fecha actual");
        }

        // Validar cantidad
        if (muerteData.getCantidad() == null || muerteData.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }

        // Validar que el parto pertenezca a la empresa del usuario
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty() || !parto.getEmpresa().getId().equals(empresaActiva.get().getId())) {
            throw new IllegalArgumentException("No tiene permisos para registrar la muerte en este parto");
        }

        // VALIDACIÓN: Verificar que la causa provenga del catálogo configurable
        CausaMortalidadPorcino.EtapaMortalidad etapaCatalogo = 
            muerteData.getEtapa() == MuerteLechon.EtapaLechon.LACTANCIA ?
                CausaMortalidadPorcino.EtapaMortalidad.LACTANCIA :
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
        muerteData.setParto(parto);
        muerteData.setMadre(parto.getMadre());
        muerteData.setEmpresa(empresaActiva.get());
        muerteData.setUsuario(user);
        muerteData.setActivo(true);

        // Actualizar nacidos vivos del parto (restar la cantidad de muertes)
        int nuevasMuertes = muerteData.getCantidad();
        int nacidosVivosActuales = parto.getNacidosVivos() != null ? parto.getNacidosVivos() : 0;
        
        // Calcular total de muertes registradas para este parto
        int totalMuertesRegistradas = muerteLechonRepository.sumCantidadByParto(parto) != null 
            ? muerteLechonRepository.sumCantidadByParto(parto) 
            : 0;
        
        // Validar que no se exceda el número de nacidos vivos
        if (totalMuertesRegistradas + nuevasMuertes > nacidosVivosActuales) {
            throw new IllegalArgumentException(
                String.format("La cantidad de muertes (%d) excede el número de nacidos vivos del parto (%d)",
                    totalMuertesRegistradas + nuevasMuertes, nacidosVivosActuales));
        }

        MuerteLechon muerteGuardada = muerteLechonRepository.save(muerteData);

        return muerteGuardada;
    }

    public List<MuerteLechon> obtenerMuertesPorParto(Long partoId, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }

        Optional<Parto> parto = partoRepository.findById(partoId);
        if (parto.isEmpty() || !parto.get().getEmpresa().getId().equals(empresaActiva.get().getId())) {
            return List.of();
        }

        return muerteLechonRepository.findByPartoAndActivoTrue(parto.get());
    }

    public List<MuerteLechon> obtenerMuertesPorMadre(Long madreId, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }

        Optional<Madre> madre = madreRepository.findById(madreId);
        if (madre.isEmpty() || !madre.get().getEmpresa().getId().equals(empresaActiva.get().getId())) {
            return List.of();
        }

        return muerteLechonRepository.findByMadreAndActivoTrue(madre.get());
    }

    public List<MuerteLechon> obtenerTodasLasMuertes(User user, LocalDate fechaDesde, LocalDate fechaHasta) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }

        if (fechaDesde != null && fechaHasta != null) {
            return muerteLechonRepository.findByEmpresaAndRangoFechas(empresaActiva.get(), fechaDesde, fechaHasta);
        }

        return muerteLechonRepository.findByEmpresaAndActivoTrue(empresaActiva.get());
    }
}

