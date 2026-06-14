import React, { useCallback, useEffect, useState } from 'react';
import { useEmpresa } from '../../../../contexts/EmpresaContext';
import { usePermissions } from '../../../../hooks/usePermissions';
import {
  desactivarPlanRecria,
  listarPlanesRecria,
  type PlanRecriaResumen,
} from '../../services/planesRecriaGestionService';

const PlanesRecriaListScreen: React.FC = () => {
  const { empresaActiva } = useEmpresa();
  const { canEditRecria } = usePermissions();
  const [planes, setPlanes] = useState<PlanRecriaResumen[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const cargar = useCallback(async () => {
    if (!empresaActiva) return;
    setCargando(true);
    setError(null);
    try {
      const data = await listarPlanesRecria();
      setPlanes(data);
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Error al cargar planes');
    } finally {
      setCargando(false);
    }
  }, [empresaActiva]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const handleDesactivar = async (id: number, nombre: string) => {
    if (!canEditRecria) return;
    if (!window.confirm(`¿Desactivar el plan "${nombre}"? No se borra de la base; deja de mostrarse en listados activos.`)) {
      return;
    }
    try {
      await desactivarPlanRecria(id);
      await cargar();
    } catch (e: unknown) {
      alert(e instanceof Error ? e.message : 'Error');
    }
  };

  if (!empresaActiva) {
    return <p className="p-6 text-amber-700">Seleccioná una empresa.</p>;
  }

  return (
    <div className="p-6 max-w-5xl mx-auto">
      <div className="flex flex-wrap justify-between items-center gap-4 mb-6">
        <h1 className="text-xl font-semibold text-gray-800">Planes de recría</h1>
      </div>
      <p className="text-sm text-gray-600 mb-4">
        Alta y edición asistida por IA no están disponibles por el momento. Podés consultar planes existentes y desactivarlos.
      </p>

      {error && <p className="text-red-600 text-sm mb-4">{error}</p>}

      {cargando ? (
        <p className="text-gray-600">Cargando…</p>
      ) : planes.length === 0 ? (
        <p className="text-gray-600">No hay planes activos.</p>
      ) : (
        <div className="overflow-x-auto border rounded-lg">
          <table className="min-w-full text-sm">
            <thead className="bg-gray-100 text-left">
              <tr>
                <th className="p-3">Nombre</th>
                <th className="p-3">Propósito</th>
                <th className="p-3">Raza objetivo</th>
                <th className="p-3">Alta</th>
                <th className="p-3 w-48">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {planes.map((p) => (
                <tr key={p.id} className="border-t border-gray-200">
                  <td className="p-3 font-medium">{p.nombre}</td>
                  <td className="p-3">{p.proposito ?? '—'}</td>
                  <td className="p-3">{p.razaObjetivo ?? '—'}</td>
                  <td className="p-3 text-gray-600">{p.fechaCreacion ? new Date(p.fechaCreacion).toLocaleString() : '—'}</td>
                  <td className="p-3">
                    {canEditRecria && (
                      <button type="button" className="text-red-600 underline text-left" onClick={() => handleDesactivar(p.id, p.nombre)}>
                        Desactivar
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default PlanesRecriaListScreen;
