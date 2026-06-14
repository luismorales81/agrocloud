package com.agrocloud.core.application;

import com.agrocloud.core.domain.SerieTareaRecurrenteCalendario;
import com.agrocloud.core.domain.TipoRepeticionTareaRecurrente;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Calcula fechas de ocurrencia de una serie dentro de un rango (sin persistir).
 */
public final class ExpansionOcurrenciasSerieTareaRecurrente {

    private ExpansionOcurrenciasSerieTareaRecurrente() {
    }

    public static List<LocalDate> expandir(
            SerieTareaRecurrenteCalendario serie,
            LocalDate desdeRango,
            LocalDate hastaRango) {
        LocalDate inicioSerie = serie.getFechaInicio();
        LocalDate finSerie = serie.getFechaFin();
        LocalDate hastaEfectivo = finSerie == null || finSerie.isAfter(hastaRango) ? hastaRango : finSerie;
        LocalDate limiteInferior = inicioSerie.isAfter(desdeRango) ? inicioSerie : desdeRango;
        if (limiteInferior.isAfter(hastaEfectivo)) {
            return List.of();
        }
        TipoRepeticionTareaRecurrente tipo = serie.getTipoRepeticion();
        return switch (tipo) {
            case DIARIA -> expandirDiaria(limiteInferior, hastaEfectivo);
            case SEMANAL -> expandirSemanal(inicioSerie, limiteInferior, hastaEfectivo);
            case MENSUAL -> expandirMensual(inicioSerie, limiteInferior, hastaEfectivo);
            case ANUAL -> expandirAnual(inicioSerie, limiteInferior, hastaEfectivo);
        };
    }

    private static List<LocalDate> expandirDiaria(LocalDate desde, LocalDate hasta) {
        List<LocalDate> fechas = new ArrayList<>();
        LocalDate d = desde;
        while (!d.isAfter(hasta)) {
            fechas.add(d);
            d = d.plusDays(1);
        }
        return fechas;
    }

    private static List<LocalDate> expandirSemanal(LocalDate ancla, LocalDate desde, LocalDate hasta) {
        List<LocalDate> fechas = new ArrayList<>();
        LocalDate d = ancla;
        while (d.isBefore(desde)) {
            d = d.plusWeeks(1);
        }
        while (!d.isAfter(hasta)) {
            fechas.add(d);
            d = d.plusWeeks(1);
        }
        return fechas;
    }

    private static List<LocalDate> expandirMensual(LocalDate ancla, LocalDate desde, LocalDate hasta) {
        int diaPreferido = ancla.getDayOfMonth();
        List<LocalDate> fechas = new ArrayList<>();
        YearMonth ym = YearMonth.from(ancla);
        YearMonth ymFin = YearMonth.from(hasta);
        while (!ym.isAfter(ymFin)) {
            LocalDate fecha = diaSeguroEnMes(ym, diaPreferido);
            if (!fecha.isBefore(ancla) && !fecha.isBefore(desde) && !fecha.isAfter(hasta)) {
                fechas.add(fecha);
            }
            ym = ym.plusMonths(1);
        }
        return fechas;
    }

    private static List<LocalDate> expandirAnual(LocalDate ancla, LocalDate desde, LocalDate hasta) {
        List<LocalDate> fechas = new ArrayList<>();
        MonthDay md = MonthDay.from(ancla);
        int y0 = ancla.getYear();
        int y1 = hasta.getYear();
        for (int y = y0; y <= y1; y++) {
            LocalDate fecha = md.atYear(y);
            if (md.getMonthValue() == 2 && md.getDayOfMonth() == 29 && !java.time.Year.isLeap(y)) {
                fecha = LocalDate.of(y, 2, 28);
            }
            if (fecha.isBefore(ancla)) {
                continue;
            }
            if (!fecha.isBefore(desde) && !fecha.isAfter(hasta)) {
                fechas.add(fecha);
            }
        }
        return fechas;
    }

    private static LocalDate diaSeguroEnMes(YearMonth ym, int diaPreferido) {
        int max = ym.lengthOfMonth();
        int dia = Math.min(diaPreferido, max);
        return ym.atDay(dia);
    }
}
