-- V4: Datos de contacto y unicidad
--   * Event: correo opcional del organizador (validado con @Email en el DTO de entrada)
--   * Venue: el nombre del lugar no se puede repetir (respaldo en BD de la regla DuplicateResourceException)

ALTER TABLE events ADD COLUMN email_organizador VARCHAR(150);

ALTER TABLE venues ADD CONSTRAINT uk_venues_nombre UNIQUE (nombre);
