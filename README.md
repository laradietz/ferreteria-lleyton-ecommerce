# Ferretería Lleyton — API de E-commerce

API REST completa de e-commerce construida con Java 21 + Spring Boot, pensada
para la tienda online de **Ferretería Lleyton**, con una tienda web completa
en español incluida (servida por el propio backend).

## Dominio

```
Usuarios (con rol ADMIN o CUSTOMER)
    ↓
Categorías
    ↓
Productos
    ↓
Carrito
    ↓
Pedidos
    ↓
Pagos
    ↓
Envíos
    ↓
Notificaciones
```

## Tecnologías

- Java 21
- Spring Boot 3 (Web, Data JPA, Validation, Security)
- Spring Security + JWT (login/registro con autenticación stateless)
- PostgreSQL
- Maven
- Docker / Docker Compose
- Swagger / OpenAPI (springdoc-openapi)
- JUnit 5 + Mockito (tests unitarios de servicios)
- Frontend propio (HTML/CSS/JS) servido como recurso estático, en español

## Cómo correrlo

### Opción 1: con Docker (recomendado, no requiere instalar nada más)

```bash
docker compose up --build
```

Esto levanta dos contenedores: la base PostgreSQL y la API. Al arrancar, la
API crea automáticamente:
- un usuario **admin** (`admin` / `admin123`)
- un catálogo de ejemplo de la ferretería (categorías y ~24 productos)

La tienda queda disponible en `http://localhost:8080`.
Swagger UI (documentación técnica): `http://localhost:8080/swagger-ui.html`

### Opción 2: local, con tu propio PostgreSQL

1. Creá una base `ecommerce` en tu PostgreSQL local.
2. Ajustá `src/main/resources/application.properties` si tu usuario/contraseña
   no son `postgres`/`postgres`.
3. Corré:
   ```bash
   mvn spring-boot:run
   ```
   (Hibernate crea las tablas solo, con `ddl-auto=update` — no hace falta
   ningún script SQL manual.)

## Usar la tienda

Andá a `http://localhost:8080` en el navegador. Vas a ver la pantalla de
login/registro de Ferretería Lleyton.

- **Como administrador**: `admin` / `admin123` — podés gestionar productos,
  categorías, y todos los pedidos (cambiar estado, cargar datos de envío).
- **Como cliente**: registrate con tu propio usuario — podés navegar el
  catálogo, armar tu carrito, generar pedidos, pagarlos (pago simulado) y
  ver tus notificaciones.

## Roles y permisos

| Acción                                    | CUSTOMER | ADMIN |
|--------------------------------------------|:--------:|:-----:|
| Ver productos/categorías                   |    ✔     |   ✔   |
| Crear/editar productos/categorías           |          |   ✔   |
| Gestionar su propio carrito                 |    ✔     |       |
| Crear pedidos                               |    ✔     |       |
| Ver/cancelar sus propios pedidos            |    ✔     |       |
| Ver todos los pedidos                       |          |   ✔   |
| Cambiar estado de un pedido / cargar envío  |          |   ✔   |
| Pagar un pedido propio                      |    ✔     |       |

Un cliente **no puede** ver, pagar ni cancelar el pedido de otro cliente
(verificado del lado del servidor, no solo ocultado en la interfaz).

## Tests

```bash
mvn test
```

Tests unitarios con JUnit 5 + Mockito sobre `ProductService` y `CartService`
(validaciones de stock, SKU duplicado, etc.), sin necesidad de levantar base
de datos ni contexto de Spring.

## Notas de diseño

- Los controllers **nunca** devuelven entidades de JPA directamente, siempre
  DTOs armados a mano en los servicios — evita por completo los problemas de
  serialización de relaciones "lazy" de Hibernate.
- Los enums (`OrderStatus`, `PaymentStatus`, etc.) se guardan como texto
  simple (`VARCHAR`), no como tipos `ENUM` nativos de Postgres, para que
  coincidan siempre con el mapeo de Hibernate.
- Cada operación sobre un pedido, pago o envío verifica que le pertenezca al
  usuario autenticado (o que sea ADMIN) antes de devolver o modificar nada.
- El pago está **simulado** (no hay integración real con Mercado Pago,
  Stripe, etc.) — queda aprobado automáticamente si el monto es mayor a cero.
  Mercado Pago aparece como una opción más dentro del pago simulado.

## WhatsApp

- Hay un botón flotante (💬, abajo a la derecha) visible en toda la tienda
  para que cualquier visitante consulte por WhatsApp.
- Al generar un pedido, aparece un botón "Avisar por WhatsApp" que abre un
  chat con el pedido ya redactado.
- **Antes de usarlo en serio**, cambiá el número de la ferretería: abrí
  `src/main/resources/static/index.html`, buscá la constante
  `WHATSAPP_NUMBER` (cerca del principio del `<script>`) y reemplazala por
  el número real, con código de país y área, sin `+` ni espacios
  (ej. `5491112345678`).
- Esto usa el link "click to chat" de WhatsApp (gratis, sin cuenta de
  desarrollador). Para enviar mensajes 100% automáticos sin que el cliente
  tenga que apretar nada, se necesitaría la API oficial de WhatsApp Business
  (de pago, requiere aprobación de Meta) — fuera del alcance de este proyecto.

## Fotos de productos

- Desde el panel de administración (Productos → Nuevo/Editar), se puede
  subir una foto real (JPG/PNG/WEBP/GIF, hasta 5 MB) — no hace falta pegar
  una URL externa.
- Las fotos se guardan en una carpeta `uploads/` en el servidor (fuera del
  `.jar`) y se sirven en `/uploads/<archivo>`. Con Docker, esa carpeta queda
  en un volumen (`lleyton_uploads`) para no perderse al reiniciar el
  contenedor.

## Qué aprendí

- Separar completamente las entidades JPA de lo que devuelve la API (DTOs
  armados a mano en los servicios) para no depender de cómo Hibernate
  serializa relaciones lazy.
- Verificar la propiedad de un recurso (pedido, pago, envío) del lado del
  servidor en cada operación, no solo mostrar/ocultar botones en el frontend.
- Diseñar un pago simulado de forma explícita y documentada, en vez de dejar
  ambigüedad sobre qué es real y qué no en un proyecto de portfolio.

## Próximas mejoras

- Reemplazar `ddl-auto=update` por migraciones versionadas con Flyway.
- Agregar tests de integración con Testcontainers (hoy los tests unitarios
  cubren `ProductService` y `CartService`, pero no el flujo completo contra
  una base real).
- Integrar un proveedor de pagos real (Mercado Pago) detrás de la misma
  interfaz que hoy usa el pago simulado.
