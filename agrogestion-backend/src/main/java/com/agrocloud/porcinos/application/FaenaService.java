package com.agrocloud.porcinos.application;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.application.EmpresaContextService;

import com.agrocloud.porcinos.domain.Faena;
import com.agrocloud.porcinos.domain.Recria;
import com.agrocloud.porcinos.infrastructure.FaenaRepository;
import com.agrocloud.porcinos.infrastructure.RecriaRepository;
import com.agrocloud.porcinos.infrastructure.MuerteRecriaRepository;
import com.agrocloud.porcinos.domain.DatosEconomicosPorcino;
import com.agrocloud.porcinos.infrastructure.DatosEconomicosPorcinoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar faenas
 * Incluye validaciones completas, cálculo de merma en transporte y automatismos
 * 
 * @deprecated Este servicio ha sido unificado con VentaPorcinoService.
 *            Para registrar faenas, usar VentaPorcinoService.registrarVenta() 
 *            con tipo = TipoVenta.FAENA.
 *            Este servicio se mantiene por compatibilidad temporal.
 * 
 * @see VentaPorcinoService
 */
@Deprecated
@Service
@SuppressWarnings("deprecation")
public class FaenaService {

    @Autowired
    private FaenaRepository faenaRepository;

    @Autowired
    private RecriaRepository recriaRepository;

    @Autowired
    private MuerteRecriaRepository muerteRecriaRepository;

    @Autowired
    private DatosEconomicosPorcinoRepository datosEconomicosRepository;

    @Autowired
    private EmpresaContextService empresaContextService;

    /** Escritura deshabilitada: usar VentaPorcinoService.registrarVenta con tipo FAENA. */
    @Transactional
    public Faena registrarFaena(Long recriaId, Faena faenaData, User user) {
        throw new UnsupportedOperationException("La faena debe registrarse vía VentaPorcino tipo FAENA");
    }

    public List<Faena> obtenerFaenasPorRecria(Long recriaId, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }

        Optional<Recria> recria = recriaRepository.findById(recriaId);
        if (recria.isEmpty() || !recria.get().getEmpresa().getId().equals(empresaActiva.get().getId())) {
            return List.of();
        }

        return faenaRepository.findByRecria(recria.get());
    }

    public List<Faena> obtenerTodasLasFaenas(User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }
        return faenaRepository.findByEmpresa(empresaActiva.get());
    }

    public BigDecimal obtenerIngresosTotales(java.time.LocalDate fechaDesde, java.time.LocalDate fechaHasta, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = faenaRepository.sumIngresosByRangoFechas(empresaActiva.get(), fechaDesde, fechaHasta);
        return total != null ? total : BigDecimal.ZERO;
    }
}



