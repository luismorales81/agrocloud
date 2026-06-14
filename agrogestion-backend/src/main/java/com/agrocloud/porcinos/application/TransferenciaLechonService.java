package com.agrocloud.porcinos.application;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.application.EmpresaContextService;
import com.agrocloud.model.dto.TransferenciaLechonDTO;

import com.agrocloud.porcinos.domain.Parto;
import com.agrocloud.porcinos.domain.TransferenciaLechon;
import com.agrocloud.porcinos.infrastructure.MadreRepository;
import com.agrocloud.porcinos.infrastructure.PartoRepository;
import com.agrocloud.porcinos.infrastructure.TransferenciaLechonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransferenciaLechonService {

    @Autowired
    private TransferenciaLechonRepository transferenciaRepository;

    @Autowired
    private PartoRepository partoRepository;

    @Autowired
    @SuppressWarnings("unused") // Reservado para futuras implementaciones
    private MadreRepository madreRepository;

    @Autowired
    private EmpresaContextService empresaContextService;

    @Transactional
    public TransferenciaLechon registrarTransferencia(Long partoOrigenId, Long partoDestinoId, 
                                                      Integer cantidad, String motivo, String observaciones, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            throw new RuntimeException("Usuario no tiene empresa activa");
        }

        Optional<Parto> partoOrigen = partoRepository.findByIdAndActivoTrue(partoOrigenId);
        Optional<Parto> partoDestino = partoRepository.findByIdAndActivoTrue(partoDestinoId);

        if (partoOrigen.isEmpty() || partoDestino.isEmpty()) {
            throw new RuntimeException("Uno o ambos partos no encontrados");
        }

        // Validar que haya suficientes lechones vivos en el parto origen
        if (partoOrigen.get().getNacidosVivos() < cantidad) {
            throw new RuntimeException("No hay suficientes lechones vivos en el parto origen");
        }

        // Actualizar nacidos vivos en ambos partos
        partoOrigen.get().setNacidosVivos(partoOrigen.get().getNacidosVivos() - cantidad);
        partoDestino.get().setNacidosVivos(partoDestino.get().getNacidosVivos() + cantidad);
        partoOrigen.get().setTotalNacidos(partoOrigen.get().getTotalNacidos() - cantidad);
        partoDestino.get().setTotalNacidos(partoDestino.get().getTotalNacidos() + cantidad);

        partoRepository.save(partoOrigen.get());
        partoRepository.save(partoDestino.get());

        TransferenciaLechon transferencia = new TransferenciaLechon();
        transferencia.setPartoOrigen(partoOrigen.get());
        transferencia.setMadreOrigen(partoOrigen.get().getMadre());
        transferencia.setPartoDestino(partoDestino.get());
        transferencia.setMadreDestino(partoDestino.get().getMadre());
        transferencia.setFechaTransferencia(java.time.LocalDate.now());
        transferencia.setCantidad(cantidad);
        transferencia.setMotivo(motivo);
        transferencia.setObservaciones(observaciones);
        transferencia.setEmpresa(empresaActiva.get());
        transferencia.setUsuario(user);

        return transferenciaRepository.save(transferencia);
    }

    @Transactional(readOnly = true)
    public List<TransferenciaLechonDTO> obtenerTransferenciasPorParto(Long partoId, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }

        Optional<Parto> parto = partoRepository.findById(partoId);
        if (parto.isEmpty() || !parto.get().getEmpresa().getId().equals(empresaActiva.get().getId())) {
            return List.of();
        }

        List<TransferenciaLechon> transferencias = transferenciaRepository.findByPartoOrigenWithRelations(parto.get());
        transferencias.addAll(transferenciaRepository.findByPartoDestinoWithRelations(parto.get()));
        
        return transferencias.stream()
                .map(TransferenciaLechonDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransferenciaLechonDTO> obtenerTodasLasTransferencias(User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }
        List<TransferenciaLechon> transferencias = transferenciaRepository.findByEmpresaWithRelations(empresaActiva.get());
        return transferencias.stream()
                .map(TransferenciaLechonDTO::new)
                .collect(Collectors.toList());
    }
}







