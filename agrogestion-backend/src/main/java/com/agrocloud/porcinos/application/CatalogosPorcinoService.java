package com.agrocloud.porcinos.application;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.application.EmpresaContextService;

import com.agrocloud.porcinos.domain.CausaMomificado;
import com.agrocloud.porcinos.domain.CausaMortalidadPorcino;
import com.agrocloud.porcinos.domain.CausaNacidoMuerto;
import com.agrocloud.porcinos.domain.EsquemaSanitarioPorcino;
import com.agrocloud.porcinos.domain.MotivoBajaPorcino;
import com.agrocloud.porcinos.domain.ProveedorGenetica;
import com.agrocloud.porcinos.domain.RazaPorcino;
import com.agrocloud.porcinos.domain.TipoEventoSanitario;
import com.agrocloud.porcinos.domain.TipoParto;
import com.agrocloud.porcinos.domain.TipoServicioPorcino;
import com.agrocloud.porcinos.domain.UbicacionInterna;
import com.agrocloud.porcinos.infrastructure.CausaMortalidadPorcinoRepository;
import com.agrocloud.porcinos.infrastructure.CausaNacidoMuertoRepository;
import com.agrocloud.porcinos.infrastructure.CausaMomificadoRepository;
import com.agrocloud.porcinos.infrastructure.EsquemaSanitarioPorcinoRepository;
import com.agrocloud.porcinos.infrastructure.MotivoBajaPorcinoRepository;
import com.agrocloud.porcinos.infrastructure.ProveedorGeneticaRepository;
import com.agrocloud.porcinos.infrastructure.RazaPorcinoRepository;
import com.agrocloud.porcinos.infrastructure.TipoEventoSanitarioRepository;
import com.agrocloud.porcinos.infrastructure.TipoPartoRepository;
import com.agrocloud.porcinos.infrastructure.TipoServicioPorcinoRepository;
import com.agrocloud.porcinos.infrastructure.UbicacionInternaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio unificado para gestionar todos los catálogos configurables del módulo porcino
 */
@Service
public class CatalogosPorcinoService {

    @Autowired
    private RazaPorcinoRepository razaRepository;
    
    @Autowired
    private TipoServicioPorcinoRepository tipoServicioRepository;
    
    @Autowired
    private CausaMortalidadPorcinoRepository causaMortalidadRepository;
    
    @Autowired
    private MotivoBajaPorcinoRepository motivoBajaRepository;
    
    @Autowired
    private EsquemaSanitarioPorcinoRepository esquemaSanitarioRepository;
    
    @Autowired
    private TipoEventoSanitarioRepository tipoEventoSanitarioRepository;
    
    @Autowired
    private ProveedorGeneticaRepository proveedorGeneticaRepository;
    
    @Autowired
    private TipoPartoRepository tipoPartoRepository;
    
    @Autowired
    private CausaNacidoMuertoRepository causaNacidoMuertoRepository;
    
    @Autowired
    private CausaMomificadoRepository causaMomificadoRepository;
    
    @Autowired
    private UbicacionInternaRepository ubicacionInternaRepository;
    
    @Autowired
    private EmpresaContextService empresaContextService;

    // ============================================================================
    // RAZAS
    // ============================================================================
    
    @Transactional(readOnly = true)
    public List<RazaPorcino> obtenerRazas(Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) return List.of();
        return razaRepository.findByEmpresaAndActivoTrue(empresa.get());
    }
    
    @Transactional(readOnly = true)
    public List<RazaPorcino> obtenerRazasPorTipo(Long userId, RazaPorcino.TipoRaza tipo) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) return List.of();
        return razaRepository.findByEmpresaAndTipoAndActivoTrue(empresa.get(), tipo);
    }
    
    @Transactional
    public RazaPorcino guardarRaza(RazaPorcino raza, Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) {
            throw new RuntimeException("Usuario no tiene empresa activa");
        }
        
        // Validaciones
        if (raza.getNombre() == null || raza.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la raza es obligatorio");
        }
        if (raza.getNombre().length() > 100) {
            throw new IllegalArgumentException("El nombre de la raza no puede exceder 100 caracteres");
        }
        if (raza.getTipo() == null) {
            throw new IllegalArgumentException("El tipo de raza es obligatorio");
        }
        
        // Verificar duplicados
        Optional<RazaPorcino> existente = razaRepository.findByEmpresaAndNombreAndActivoTrue(empresa.get(), raza.getNombre().trim());
        if (existente.isPresent() && (raza.getId() == null || !existente.get().getId().equals(raza.getId()))) {
            throw new IllegalArgumentException("Ya existe una raza con ese nombre");
        }
        
        raza.setEmpresa(empresa.get());
        raza.setActivo(true);
        return razaRepository.save(raza);
    }
    
    @Transactional
    public void eliminarRaza(Long id, Long userId) {
        Optional<RazaPorcino> raza = razaRepository.findById(id);
        if (raza.isPresent()) {
            raza.get().setActivo(false);
            razaRepository.save(raza.get());
        }
    }

    // ============================================================================
    // TIPOS DE ALIMENTO (ELIMINADO - REDUNDANTE)
    // ============================================================================
    // NOTA: TipoAlimentoPorcino fue eliminado porque es redundante con:
    // - Recetas: InsumoCompuesto (tipo RACION) asociado a etapas mediante RecetaAlimentacionPorEtapa
    // - Balanceados comerciales: Insumo (tabla cultivo_insumos)
    // - Granos propios: Cultivo + InventarioGrano
    // TipoAlimentoPorcino solo existía como catálogo sin integración funcional
    // en el sistema de consumo actual (ConsumoDiarioAutomatico)

    // ============================================================================
    // TIPOS DE SERVICIO
    // ============================================================================
    
    @Transactional(readOnly = true)
    public List<TipoServicioPorcino> obtenerTiposServicio(Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) return List.of();
        return tipoServicioRepository.findByEmpresaAndActivoTrue(empresa.get());
    }
    
    @Transactional
    public TipoServicioPorcino guardarTipoServicio(TipoServicioPorcino tipoServicio, Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) {
            throw new RuntimeException("Usuario no tiene empresa activa");
        }
        tipoServicio.setEmpresa(empresa.get());
        tipoServicio.setActivo(true);
        return tipoServicioRepository.save(tipoServicio);
    }
    
    @Transactional
    public void eliminarTipoServicio(Long id, Long userId) {
        Optional<TipoServicioPorcino> tipoServicio = tipoServicioRepository.findById(id);
        if (tipoServicio.isPresent()) {
            tipoServicio.get().setActivo(false);
            tipoServicioRepository.save(tipoServicio.get());
        }
    }

    // ============================================================================
    // CAUSAS DE MORTALIDAD
    // ============================================================================
    
    @Transactional(readOnly = true)
    public List<CausaMortalidadPorcino> obtenerCausasMortalidad(Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) return List.of();
        return causaMortalidadRepository.findByEmpresaAndActivoTrue(empresa.get());
    }
    
    @Transactional
    public CausaMortalidadPorcino guardarCausaMortalidad(CausaMortalidadPorcino causa, Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) {
            throw new RuntimeException("Usuario no tiene empresa activa");
        }
        causa.setEmpresa(empresa.get());
        causa.setActivo(true);
        return causaMortalidadRepository.save(causa);
    }
    
    @Transactional
    public void eliminarCausaMortalidad(Long id, Long userId) {
        Optional<CausaMortalidadPorcino> causa = causaMortalidadRepository.findById(id);
        if (causa.isPresent()) {
            causa.get().setActivo(false);
            causaMortalidadRepository.save(causa.get());
        }
    }

    // ============================================================================
    // MOTIVOS DE BAJA
    // ============================================================================
    
    @Transactional(readOnly = true)
    public List<MotivoBajaPorcino> obtenerMotivosBaja(Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) return List.of();
        return motivoBajaRepository.findByEmpresaAndActivoTrue(empresa.get());
    }
    
    @Transactional
    public MotivoBajaPorcino guardarMotivoBaja(MotivoBajaPorcino motivo, Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) {
            throw new RuntimeException("Usuario no tiene empresa activa");
        }
        motivo.setEmpresa(empresa.get());
        motivo.setActivo(true);
        return motivoBajaRepository.save(motivo);
    }
    
    @Transactional
    public void eliminarMotivoBaja(Long id, Long userId) {
        Optional<MotivoBajaPorcino> motivo = motivoBajaRepository.findById(id);
        if (motivo.isPresent()) {
            motivo.get().setActivo(false);
            motivoBajaRepository.save(motivo.get());
        }
    }

    // ============================================================================
    // ESQUEMAS SANITARIOS
    // ============================================================================
    
    @Transactional(readOnly = true)
    public List<EsquemaSanitarioPorcino> obtenerEsquemasSanitarios(Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) return List.of();
        return esquemaSanitarioRepository.findByEmpresaAndActivoTrue(empresa.get());
    }
    
    @Transactional
    public EsquemaSanitarioPorcino guardarEsquemaSanitario(EsquemaSanitarioPorcino esquema, Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) {
            throw new RuntimeException("Usuario no tiene empresa activa");
        }
        esquema.setEmpresa(empresa.get());
        esquema.setActivo(true);
        return esquemaSanitarioRepository.save(esquema);
    }
    
    @Transactional
    public void eliminarEsquemaSanitario(Long id, Long userId) {
        Optional<EsquemaSanitarioPorcino> esquema = esquemaSanitarioRepository.findById(id);
        if (esquema.isPresent()) {
            esquema.get().setActivo(false);
            esquemaSanitarioRepository.save(esquema.get());
        }
    }

    // ============================================================================
    // TIPOS DE EVENTO SANITARIO
    // ============================================================================
    
    @Transactional(readOnly = true)
    public List<TipoEventoSanitario> obtenerTiposEventoSanitario(Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) return List.of();
        return tipoEventoSanitarioRepository.findByEmpresaAndActivoTrue(empresa.get());
    }
    
    @Transactional(readOnly = true)
    public List<TipoEventoSanitario> obtenerTiposEventoSanitarioPorCategoria(
            Long userId, TipoEventoSanitario.CategoriaEvento categoria) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) return List.of();
        return tipoEventoSanitarioRepository.findByEmpresaAndCategoriaAndActivoTrue(empresa.get(), categoria);
    }
    
    @Transactional
    public TipoEventoSanitario guardarTipoEventoSanitario(TipoEventoSanitario tipo, Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) {
            throw new RuntimeException("Usuario no tiene empresa activa");
        }
        
        if (tipo.getNombre() == null || tipo.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        
        Optional<TipoEventoSanitario> existente = tipoEventoSanitarioRepository
            .findByEmpresaAndNombreAndActivoTrue(empresa.get(), tipo.getNombre().trim());
        if (existente.isPresent() && (tipo.getId() == null || !existente.get().getId().equals(tipo.getId()))) {
            throw new IllegalArgumentException("Ya existe un tipo de evento sanitario con ese nombre");
        }
        
        tipo.setEmpresa(empresa.get());
        tipo.setActivo(true);
        return tipoEventoSanitarioRepository.save(tipo);
    }
    
    @Transactional
    public void eliminarTipoEventoSanitario(Long id, Long userId) {
        Optional<TipoEventoSanitario> tipo = tipoEventoSanitarioRepository.findById(id);
        if (tipo.isPresent()) {
            tipo.get().setActivo(false);
            tipoEventoSanitarioRepository.save(tipo.get());
        }
    }

    // ============================================================================
    // PROVEEDORES DE GENÉTICA
    // ============================================================================
    
    @Transactional(readOnly = true)
    public List<ProveedorGenetica> obtenerProveedoresGenetica(Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) return List.of();
        return proveedorGeneticaRepository.findByEmpresaAndActivoTrue(empresa.get());
    }
    
    @Transactional(readOnly = true)
    public List<ProveedorGenetica> obtenerProveedoresGeneticaPorTipo(
            Long userId, ProveedorGenetica.TipoProveedor tipo) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) return List.of();
        return proveedorGeneticaRepository.findByEmpresaAndTipoAndActivoTrue(empresa.get(), tipo);
    }
    
    @Transactional
    public ProveedorGenetica guardarProveedorGenetica(ProveedorGenetica proveedor, Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) {
            throw new RuntimeException("Usuario no tiene empresa activa");
        }
        
        if (proveedor.getNombre() == null || proveedor.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        
        Optional<ProveedorGenetica> existente = proveedorGeneticaRepository
            .findByEmpresaAndNombreAndActivoTrue(empresa.get(), proveedor.getNombre().trim());
        if (existente.isPresent() && (proveedor.getId() == null || !existente.get().getId().equals(proveedor.getId()))) {
            throw new IllegalArgumentException("Ya existe un proveedor con ese nombre");
        }
        
        proveedor.setEmpresa(empresa.get());
        proveedor.setActivo(true);
        return proveedorGeneticaRepository.save(proveedor);
    }
    
    @Transactional
    public void eliminarProveedorGenetica(Long id, Long userId) {
        Optional<ProveedorGenetica> proveedor = proveedorGeneticaRepository.findById(id);
        if (proveedor.isPresent()) {
            proveedor.get().setActivo(false);
            proveedorGeneticaRepository.save(proveedor.get());
        }
    }

    // ============================================================================
    // TIPOS DE PARTO
    // ============================================================================
    
    @Transactional(readOnly = true)
    public List<TipoParto> obtenerTiposParto(Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) return List.of();
        return tipoPartoRepository.findByEmpresaAndActivoTrue(empresa.get());
    }
    
    @Transactional
    public TipoParto guardarTipoParto(TipoParto tipo, Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) {
            throw new RuntimeException("Usuario no tiene empresa activa");
        }
        
        if (tipo.getNombre() == null || tipo.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        
        Optional<TipoParto> existente = tipoPartoRepository
            .findByEmpresaAndNombreAndActivoTrue(empresa.get(), tipo.getNombre().trim());
        if (existente.isPresent() && (tipo.getId() == null || !existente.get().getId().equals(tipo.getId()))) {
            throw new IllegalArgumentException("Ya existe un tipo de parto con ese nombre");
        }
        
        tipo.setEmpresa(empresa.get());
        tipo.setActivo(true);
        return tipoPartoRepository.save(tipo);
    }
    
    @Transactional
    public void eliminarTipoParto(Long id, Long userId) {
        Optional<TipoParto> tipo = tipoPartoRepository.findById(id);
        if (tipo.isPresent()) {
            tipo.get().setActivo(false);
            tipoPartoRepository.save(tipo.get());
        }
    }

    // ============================================================================
    // CAUSAS DE NACIDOS MUERTOS
    // ============================================================================
    
    @Transactional(readOnly = true)
    public List<CausaNacidoMuerto> obtenerCausasNacidosMuertos(Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) return List.of();
        return causaNacidoMuertoRepository.findByEmpresaAndActivoTrue(empresa.get());
    }
    
    @Transactional
    public CausaNacidoMuerto guardarCausaNacidoMuerto(CausaNacidoMuerto causa, Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) {
            throw new RuntimeException("Usuario no tiene empresa activa");
        }
        
        if (causa.getNombre() == null || causa.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        
        Optional<CausaNacidoMuerto> existente = causaNacidoMuertoRepository
            .findByEmpresaAndNombreAndActivoTrue(empresa.get(), causa.getNombre().trim());
        if (existente.isPresent() && (causa.getId() == null || !existente.get().getId().equals(causa.getId()))) {
            throw new IllegalArgumentException("Ya existe una causa con ese nombre");
        }
        
        causa.setEmpresa(empresa.get());
        causa.setActivo(true);
        return causaNacidoMuertoRepository.save(causa);
    }
    
    @Transactional
    public void eliminarCausaNacidoMuerto(Long id, Long userId) {
        Optional<CausaNacidoMuerto> causa = causaNacidoMuertoRepository.findById(id);
        if (causa.isPresent()) {
            causa.get().setActivo(false);
            causaNacidoMuertoRepository.save(causa.get());
        }
    }

    // ============================================================================
    // CAUSAS DE MOMIFICADOS
    // ============================================================================
    
    @Transactional(readOnly = true)
    public List<CausaMomificado> obtenerCausasMomificados(Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) return List.of();
        return causaMomificadoRepository.findByEmpresaAndActivoTrue(empresa.get());
    }
    
    @Transactional
    public CausaMomificado guardarCausaMomificado(CausaMomificado causa, Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) {
            throw new RuntimeException("Usuario no tiene empresa activa");
        }
        
        if (causa.getNombre() == null || causa.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        
        Optional<CausaMomificado> existente = causaMomificadoRepository
            .findByEmpresaAndNombreAndActivoTrue(empresa.get(), causa.getNombre().trim());
        if (existente.isPresent() && (causa.getId() == null || !existente.get().getId().equals(causa.getId()))) {
            throw new IllegalArgumentException("Ya existe una causa con ese nombre");
        }
        
        causa.setEmpresa(empresa.get());
        causa.setActivo(true);
        return causaMomificadoRepository.save(causa);
    }
    
    @Transactional
    public void eliminarCausaMomificado(Long id, Long userId) {
        Optional<CausaMomificado> causa = causaMomificadoRepository.findById(id);
        if (causa.isPresent()) {
            causa.get().setActivo(false);
            causaMomificadoRepository.save(causa.get());
        }
    }

    // ============================================================================
    // UBICACIONES INTERNAS
    // ============================================================================
    
    @Transactional(readOnly = true)
    public List<UbicacionInterna> obtenerUbicacionesInternas(Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) return List.of();
        return ubicacionInternaRepository.findByEmpresaAndActivoTrue(empresa.get());
    }
    
    @Transactional(readOnly = true)
    public List<UbicacionInterna> obtenerUbicacionesPorNivel(
            Long userId, UbicacionInterna.NivelUbicacion nivel) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) return List.of();
        return ubicacionInternaRepository.findByEmpresaAndNivelAndActivoTrue(empresa.get(), nivel);
    }
    
    @Transactional(readOnly = true)
    public List<UbicacionInterna> obtenerUbicacionesHijas(Long ubicacionPadreId, Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) return List.of();
        
        Optional<UbicacionInterna> padre = ubicacionInternaRepository.findById(ubicacionPadreId);
        if (padre.isEmpty()) return List.of();
        
        // Verificar que el padre pertenezca a la empresa del usuario
        if (!padre.get().getEmpresa().getId().equals(empresa.get().getId())) {
            return List.of();
        }
        
        return ubicacionInternaRepository.findByUbicacionPadreAndActivoTrue(padre.get());
    }
    
    @Transactional
    public UbicacionInterna guardarUbicacionInterna(UbicacionInterna ubicacion, Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) {
            throw new RuntimeException("Usuario no tiene empresa activa");
        }
        
        if (ubicacion.getNombre() == null || ubicacion.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        
        if (ubicacion.getNivel() == null) {
            throw new IllegalArgumentException("El nivel es obligatorio");
        }
        
        // Si se recibió ubicacionPadreId pero no ubicacionPadre, cargar el padre
        if (ubicacion.getUbicacionPadre() == null && ubicacion.getUbicacionPadreId() != null) {
            UbicacionInterna padre = ubicacionInternaRepository.findById(ubicacion.getUbicacionPadreId())
                .orElseThrow(() -> new IllegalArgumentException("Ubicación padre no encontrada"));
            ubicacion.setUbicacionPadre(padre);
        }
        
        // Validar jerarquía: si tiene padre, el nivel debe ser el siguiente
        if (ubicacion.getUbicacionPadre() != null) {
            UbicacionInterna padre = ubicacion.getUbicacionPadre();
            
            // Verificar que el padre pertenezca a la empresa del usuario
            if (!padre.getEmpresa().getId().equals(empresa.get().getId())) {
                throw new IllegalArgumentException("La ubicación padre no pertenece a su empresa");
            }
            
            if (padre.getNivel() == UbicacionInterna.NivelUbicacion.GALPON && 
                ubicacion.getNivel() != UbicacionInterna.NivelUbicacion.SALA) {
                throw new IllegalArgumentException("Un galpón solo puede tener salas como hijas");
            }
            if (padre.getNivel() == UbicacionInterna.NivelUbicacion.SALA && 
                ubicacion.getNivel() != UbicacionInterna.NivelUbicacion.CORRAL) {
                throw new IllegalArgumentException("Una sala solo puede tener corrales como hijos");
            }
            if (padre.getNivel() == UbicacionInterna.NivelUbicacion.CORRAL) {
                throw new IllegalArgumentException("Un corral no puede tener ubicaciones hijas");
            }
        } else {
            // Si no tiene padre, debe ser GALPON
            if (ubicacion.getNivel() != UbicacionInterna.NivelUbicacion.GALPON) {
                throw new IllegalArgumentException("Solo los galpones pueden estar en el nivel raíz");
            }
        }
        
        ubicacion.setEmpresa(empresa.get());
        ubicacion.setActivo(true);
        return ubicacionInternaRepository.save(ubicacion);
    }
    
    @Transactional
    public void eliminarUbicacionInterna(Long id, Long userId) {
        Optional<Empresa> empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(userId);
        if (empresa.isEmpty()) {
            throw new RuntimeException("Usuario no tiene empresa activa");
        }
        
        Optional<UbicacionInterna> ubicacion = ubicacionInternaRepository.findById(id);
        if (ubicacion.isEmpty()) {
            throw new IllegalArgumentException("Ubicación no encontrada");
        }
        
        // Verificar que la ubicación pertenezca a la empresa del usuario
        if (!ubicacion.get().getEmpresa().getId().equals(empresa.get().getId())) {
            throw new IllegalArgumentException("La ubicación no pertenece a su empresa");
        }
        
        // Verificar que no tenga ubicaciones hijas activas
        List<UbicacionInterna> hijas = ubicacionInternaRepository
            .findByUbicacionPadreAndActivoTrue(ubicacion.get());
        if (!hijas.isEmpty()) {
            throw new IllegalArgumentException("No se puede eliminar una ubicación que tiene ubicaciones hijas activas. Elimine primero las ubicaciones hijas.");
        }
        
        ubicacion.get().setActivo(false);
        ubicacionInternaRepository.save(ubicacion.get());
    }
}

