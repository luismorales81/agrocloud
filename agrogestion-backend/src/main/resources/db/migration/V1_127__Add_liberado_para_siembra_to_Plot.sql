-- Estado derivado: marcar lote liberado para nueva siembra (cosecha ya no vigente)
ALTER TABLE cultivo_lotes ADD COLUMN liberado_para_siembra BOOLEAN NOT NULL DEFAULT false;
