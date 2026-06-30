INSERT INTO usuario (nombre, username, password, rol, activo) 
VALUES ('Administrador Central', 'admin', '$2a$10$dXJ3w46N7P6Kwg3e.y3uCOfR1gM5XshvX4XpH6mI1X9GvWf7t76eK', 'ADMIN', true)
ON CONFLICT (username) DO NOTHING;
