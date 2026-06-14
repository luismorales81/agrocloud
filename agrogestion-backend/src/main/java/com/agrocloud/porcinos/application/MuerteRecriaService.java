package com.agrocloud.porcinos.application;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.application.EmpresaContextService;
import com.agrocloud.porcinos.domain.Recria;
import com.agrocloud.porcinos.infrastructure.RecriaRepository;
import com.agrocloud.porcinos.domain.CausaMortalidadPorcino;
import com.agrocloud.porcinos.domain.MuerteRecria;
import com.agrocloud.porcinos.infrastructure.MuerteRecriaRepository;
import com.agrocloud.porcinos.infrastructure.CausaMortalidadPorcinoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MuerteRecriaService {

    @Autowired
    private MuerteRecriaRepository muerteRecriaRepository;

    @Autowired
    private RecriaRepository recriaRepository;

    @Autowired
    private EmpresaContextService empresaContextService;

    @Autowired
    private CausaMortalidadPorcinoRepository causaMortalidadRepository;

    @Autowired
    private RecriaStockService recriaStockService;

    @Transactional
    public MuerteRecria registrarMuerte(Long recriaId, MuerteRecria muerteData, User user) {
        Optional<Recria> recriaOpt = recriaRepository.findByIdAndActivoTrue(recriaId);
        if (recriaOpt.isEmpty()) {
            throw new IllegalArgumentException("Recría no encontrada o inactiva");
        }

        Recria recria = recriaOpt.get();

        // Validar que la fecha no sea futura
        if (muerteData.getFecha().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de muerte no puede ser posterior a la fecha actual");
        }

        // Validar cantidad
        if (muerteData.getCantidad() == null || muerteData.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }

        // Validar que la recría pertenezca a la empresa del usuario
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty() || !recria.getEmpresa().getId().equals(empresaActiva.get().getId())) {
            throw new IllegalArgumentException("No tiene permisos para registrar la muerte en esta recría");
        }

        // VALIDACIÓN: Verificar que la causa provenga del catálogo configurable
        CausaMortalidadPorcino.EtapaMortalidad etapaCatalogo = CausaMortalidadPorcino.EtapaMortalidad.RECRIA;
        
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

        // REGLA DE NEGOCIO: disponibles = cantidadAnimales - Σ(muertes); validar que no quede stock negativo
        int animalesDisponibles = recriaStockService.animalesDisponibles(recria);
        int nuevaCantidadDespuesMuerte = animalesDisponibles - muerteData.getCantidad();

        if (nuevaCantidadDespuesMuerte < 0) {
            throw new IllegalArgumentException(
                String.format("No se puede registrar la muerte: quedarían %d animales (stock negativo). " +
                    "Animales disponibles: %d, cantidad a registrar: %d",
                    nuevaCantidadDespuesMuerte, animalesDisponibles, muerteData.getCantidad()));
        }

        // Configurar la muerte
        muerteData.setRecria(recria);
        muerteData.setEmpresa(empresaActiva.get());
        muerteData.setUsuario(user);
        muerteData.setActivo(true);

        // Spec: las muertes no modifican cantidadAnimales; solo se registran. Disponibles = cantidadAnimales - Σ(muertes).

        return muerteRecriaRepository.save(muerteData);
    }

    public List<MuerteRecria> obtenerMuertesPorRecria(Long recriaId, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }

        Optional<Recria> recria = recriaRepository.findById(recriaId);
        if (recria.isEmpty() || !recria.get().getEmpresa().getId().equals(empresaActiva.get().getId())) {
            return List.of();
        }

        return muerteRecriaRepository.findByRecriaAndActivoTrue(recria.get());
    }

    public List<MuerteRecria> obtenerTodasLasMuertes(User user, LocalDate fechaDesde, LocalDate fechaHasta) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }

        if (fechaDesde != null && fechaHasta != null) {
            return muerteRecriaRepository.findByEmpresaAndRangoFechas(empresaActiva.get(), fechaDesde, fechaHasta);
        }

        return muerteRecriaRepository.findByEmpresaAndActivoTrue(empresaActiva.get());
    }
}



