# Commerce Cloud: arquitectura multi-tenant

## Estrategia inicial

- Una aplicación Spring Boot.
- Una base de datos PostgreSQL.
- Un esquema compartido.
- Separación mediante tenant_id.
- Resolución del tenant desde el usuario autenticado.
- El navegador nunca será autoridad del tenant.
- Los repositorios deberán filtrar por tenant_id.
- Las operaciones de escritura deberán validar tenant_id.
- Las restricciones únicas deberán incluir tenant_id.

## Tipos de negocio iniciales

- ABARROTES
- GYM
- FARMACIA
- COFFEE_SHOP

## Módulos comunes

- Seguridad
- Usuarios
- Roles
- Sucursales
- Productos
- Categorías
- Inventario
- Ventas
- Caja
- Clientes
- Auditoría

## Módulos Gym

- Clientes
- Planes de membresía
- Membresías
- Pagos
- Vencimientos
- Accesos
