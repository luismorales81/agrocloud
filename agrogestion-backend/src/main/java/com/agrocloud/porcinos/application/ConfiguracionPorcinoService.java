package com.agrocloud.porcinos.application;

import com.agrocloud.core.application.EmpresaContextService;
import com.agrocloud.porcinos.domain.ConfiguracionPorcino;
import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;
import com.agrocloud.porcinos.infrastructure.ConfiguracionPorcinoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ConfiguracionPorcinoService {

    @Autowired
    private ConfiguracionPorcinoRepository configuracionRepository;

    @Autowired
    private EmpresaContextService empresaContextService;

    /**
     * Obtener una configuración por clave. Si es una clave con valor por defecto conocido,
     * la crea de forma perezosa para la empresa del usuario.
     */
    @Transactional
    public Optional<ConfiguracionPorcino> obtenerConfiguracion(String clave, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return Optional.empty();
        }
        Empresa empresa = empresaActiva.get();
        Optional<ConfiguracionPorcino> existente = configuracionRepository.findByClaveAndEmpresaAndActivoTrue(clave, empresa);
        if (existente.isPresent()) {
            return existente;
        }
        if (configuracionRepository.existsByClaveAndActivoTrue(clave)) {
            return Optional.empty();
        }
        return asegurarConfiguracionPorDefecto(clave, empresa);
    }

    /**
     * Obtener valor de configuración como Integer
     */
    public Integer obtenerValorInteger(String clave, Integer valorPorDefecto, User user) {
        Optional<ConfiguracionPorcino> config = obtenerConfiguracion(clave, user);
        if (config.isPresent()) {
            Integer valor = config.get().getValorComoInteger();
            return valor != null ? valor : valorPorDefecto;
        }
        return valorPorDefecto;
    }

    /**
     * Obtener valor de configuración como Double
     */
    public Double obtenerValorDouble(String clave, Double valorPorDefecto, User user) {
        Optional<ConfiguracionPorcino> config = obtenerConfiguracion(clave, user);
        if (config.isPresent()) {
            Double valor = config.get().getValorComoDouble();
            return valor != null ? valor : valorPorDefecto;
        }
        return valorPorDefecto;
    }

    /**
     * Obtener valor de configuración como booleano (acepta true/1; resto false si existe registro).
     */
    public boolean obtenerValorBoolean(String clave, boolean valorPorDefecto, User user) {
        return obtenerConfiguracion(clave, user)
            .map(ConfiguracionPorcino::getValorComoBoolean)
            .orElse(valorPorDefecto);
    }

    /**
     * Obtener todas las configuraciones por categoría
     */
    @Transactional(readOnly = true)
    public List<ConfiguracionPorcino> obtenerPorCategoria(String categoria, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }
        return configuracionRepository.findByEmpresaAndCategoriaAndActivoTrue(empresaActiva.get(), categoria);
    }

    /**
     * Obtener todas las configuraciones
     */
    @Transactional(readOnly = true)
    public List<ConfiguracionPorcino> obtenerTodas(User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }
        return configuracionRepository.findByEmpresaAndActivoTrue(empresaActiva.get());
    }

    /**
     * Guardar o actualizar configuración
     */
    @Transactional
    public ConfiguracionPorcino guardarConfiguracion(String clave, String valor, ConfiguracionPorcino.TipoConfig tipo, 
                                                       String categoria, String descripcion, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            throw new RuntimeException("Usuario no tiene empresa activa");
        }

        Optional<ConfiguracionPorcino> existente = configuracionRepository.findByClaveAndEmpresaAndActivoTrue(clave, empresaActiva.get());
        
        if (existente.isPresent()) {
            ConfiguracionPorcino config = existente.get();
            config.setValor(valor);
            config.setTipo(tipo);
            config.setCategoria(categoria);
            config.setDescripcion(descripcion);
            return configuracionRepository.save(config);
        } else {
            ConfiguracionPorcino nuevaConfig = new ConfiguracionPorcino(clave, valor, tipo, categoria, empresaActiva.get());
            nuevaConfig.setDescripcion(descripcion);
            return configuracionRepository.save(nuevaConfig);
        }
    }

    /**
     * Inicializar configuraciones por defecto para una empresa
     */
    @Transactional
    public void inicializarConfiguracionesPorDefecto(Empresa empresa) {
        for (String clave : mapaConfiguracionesPorDefecto().keySet()) {
            asegurarConfiguracionPorDefecto(clave, empresa);
        }
    }

    private Optional<ConfiguracionPorcino> asegurarConfiguracionPorDefecto(String clave, Empresa empresa) {
        Map<String, Object> configData = mapaConfiguracionesPorDefecto().get(clave);
        if (configData == null) {
            return Optional.empty();
        }
        Optional<ConfiguracionPorcino> existente = configuracionRepository.findByClaveAndEmpresaAndActivoTrue(clave, empresa);
        if (existente.isPresent()) {
            return existente;
        }
        ConfiguracionPorcino config = new ConfiguracionPorcino(
                clave,
                (String) configData.get("valor"),
                (ConfiguracionPorcino.TipoConfig) configData.get("tipo"),
                (String) configData.get("categoria"),
                empresa
        );
        config.setDescripcion((String) configData.get("descripcion"));
        try {
            return Optional.of(configuracionRepository.save(config));
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            return configuracionRepository.findByClaveAndEmpresaAndActivoTrue(clave, empresa);
        }
    }

    private Map<String, Map<String, Object>> mapaConfiguracionesPorDefecto() {
        Map<String, Map<String, Object>> configs = new HashMap<>();
        configs.put("DIAS_CACHORRA", Map.of(
                "valor", "160",
                "tipo", ConfiguracionPorcino.TipoConfig.NUMERO,
                "categoria", "ETAPAS",
                "descripcion", "Días que una cachorra permanece antes de ser adulta"
        ));
        configs.put(CalendarioAlimentacionService.CLAVE_CONFIRMACION_CALENDARIO_SOLO_CON_RACION_REAL, Map.of(
                "valor", "false",
                "tipo", ConfiguracionPorcino.TipoConfig.BOOLEAN,
                "categoria", "ALIMENTACION",
                "descripcion", "Si está activo, no se puede confirmar un día del calendario de alimentación mientras quede algún consumo en modo estimado (sin kg real de ración)."
        ));
        return configs;
    }
}







