# Pruebas obligatorias multi-tenant

## Aislamiento

1. Crear Tenant A.
2. Crear Tenant B.
3. Crear un producto para Tenant A.
4. Confirmar que Tenant B no puede consultarlo.
5. Intentar acceder al producto usando su ID.
6. El backend debe responder 404 o acceso denegado.

## Gym

1. Crear cliente.
2. Crear plan mensual.
3. Asignar membresía.
4. Registrar pago.
5. Comprobar fecha de vencimiento.
6. Renovar membresía.
7. Consultar historial.
8. Vender un producto en el POS.
9. Confirmar descuento de inventario.
10. Confirmar separación entre tenants.
