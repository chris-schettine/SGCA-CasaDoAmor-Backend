ALTER TABLE acompanhantes
  ADD COLUMN deleted_at DATETIME NULL AFTER updated_at,
  ADD COLUMN deleted_by VARCHAR(255) NULL AFTER deleted_at;
