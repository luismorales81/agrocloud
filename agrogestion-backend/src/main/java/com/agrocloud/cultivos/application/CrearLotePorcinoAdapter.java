package com.agrocloud.cultivos.application;

import com.agrocloud.core.application.port.CrearLotePorcinoPort;
import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.infrastructure.EmpresaRepository;
import com.agrocloud.core.infrastructure.UserRepository;
import com.agrocloud.cultivos.domain.Field;
import com.agrocloud.cultivos.domain.Plot;
import com.agrocloud.cultivos.infrastructure.FieldRepository;
import com.agrocloud.cultivos.infrastructure.PlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementación del puerto {@link CrearLotePorcinoPort} que crea o obtiene
 * un lote/corral porcino en el módulo Cultivos sin que Porcinos dependa de Plot/Field.
 */
@Service
public class CrearLotePorcinoAdapter implements CrearLotePorcinoPort {

    @Autowired
    private PlotRepository plotRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    @Lazy
    private EstadoLoteUpdater estadoLoteUpdater;

    @Override
    public Long crearOObtenerLotePorcino(Long empresaId, Long userId, String nombreSugerido) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

        List<Plot> lotesUsuario = plotRepository.findByUserIdAndActivoTrue(user.getId());
        Optional<Plot> loteExistente = lotesUsuario.stream()
                .filter(l -> nombreSugerido.equals(l.getNombre()))
                .findFirst();

        if (loteExistente.isPresent()) {
            return loteExistente.get().getId();
        }

        Field campo = obtenerOCrearCampoParaRecria(empresa, user);

        Plot nuevoLote = new Plot();
        nuevoLote.setNombre(nombreSugerido);
        nuevoLote.setUser(user);
        nuevoLote.setCampo(campo);
        nuevoLote.setAreaHectareas(java.math.BigDecimal.valueOf(1.0));
        nuevoLote.setActivo(true);
        nuevoLote.setTipoUso(Plot.TipoUsoLote.PORCINO);

        try {
            Plot guardado = plotRepository.save(nuevoLote);
            estadoLoteUpdater.recalcularEstado(guardado.getId());
            return guardado.getId();
        } catch (Exception e) {
            List<Plot> todosLotes = plotRepository.findByUserId(user.getId());
            if (!todosLotes.isEmpty()) {
                return todosLotes.get(0).getId();
            }
            throw new RuntimeException("No se pudo crear o encontrar un lote para la recría. Por favor, cree un lote primero.");
        }
    }

    private Field obtenerOCrearCampoParaRecria(Empresa empresa, User user) {
        List<Field> camposUsuario = fieldRepository.findByUserIdAndActivoTrue(user.getId());
        if (!camposUsuario.isEmpty()) {
            return camposUsuario.get(0);
        }
        Field nuevoCampo = new Field();
        nuevoCampo.setNombre("Campo Porcinos - " + empresa.getNombre());
        nuevoCampo.setUbicacion("Granja");
        nuevoCampo.setAreaHectareas(java.math.BigDecimal.valueOf(10.0));
        nuevoCampo.setUser(user);
        nuevoCampo.setEmpresa(empresa);
        nuevoCampo.setActivo(true);
        nuevoCampo.setEstado("ACTIVO");
        try {
            return fieldRepository.save(nuevoCampo);
        } catch (Exception e) {
            List<Field> todosCampos = fieldRepository.findByUserId(user.getId());
            if (!todosCampos.isEmpty()) {
                return todosCampos.get(0);
            }
            throw new RuntimeException("No se pudo crear o encontrar un campo para la recría. Por favor, cree un campo primero.");
        }
    }
}
