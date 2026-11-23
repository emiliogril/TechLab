# TechLab E-Commerce

Trabajo Final Integrador del curso Java Spring Boot + MySQL + JavaScript.  
Sistema de e-commerce completo con catálogo de productos, carrito de compras con persistencia local, panel de administración con operaciones CRUD para productos y categorías, control de stock y seguimiento del ciclo de vida de pedidos.

---

## 🚀 Tecnologías Utilizadas

### Backend
- **Java 25**
- **Spring Boot 4.2.0**
- **Maven Wrapper**
- **Spring Web (REST API)**
- **Spring Data JPA / Hibernate**
- **Jakarta Bean Validation**
- **MySQL Connector/J**

### Frontend
- **HTML5 semántico**
- **CSS3 personalizado**
- **Bootstrap 5.3**
- **JavaScript Moderno (ES6+)**
- **Fetch API**
- **localStorage** para persistencia del carrito

### Base de Datos
- **MySQL 8+** (Base de datos: `techlab_ecommerce`)

---

## 📋 Requisitos del Sistema

- **JDK 25** instalado y configurado en el `PATH`.
- **MySQL Server** en ejecución en `localhost:3306`.
- **Navegador web moderno** (Chrome, Firefox, Edge).
- Extensión **Live Server** para Visual Studio Code (o cualquier servidor web estático en el puerto 5500).

---

## ⚙️ Configuración y Puesta en Marcha

### 1. Base de Datos
Asegurate de crear la base de datos en MySQL antes de ejecutar la aplicación:

```sql
CREATE DATABASE IF NOT EXISTS techlab_ecommerce CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Hibernate creará y actualizará automáticamente las tablas (`productos`, `pedidos`, `detalles_pedido`, `usuarios`, `categorias`) gracias a `spring.jpa.hibernate.ddl-auto=update`.

### 2. Variable de Entorno
La aplicación utiliza la variable de entorno `DB_PASSWORD` para conectarse a MySQL de forma segura.

En Windows PowerShell:
```powershell
$env:DB_PASSWORD="tu_contraseña_aqui"
```

En CMD:
```cmd
set DB_PASSWORD=tu_contraseña_aqui
```

### 3. Iniciar el Backend
Desde la raíz del proyecto, ingresá a la carpeta `backend` y ejecutá el script de Maven Wrapper:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

El backend iniciará en:
**http://localhost:8080**  
(API base: `http://localhost:8080/api`)

### 4. Iniciar el Frontend
Abrí la raíz del proyecto (`C:\01.programacion\techlab-ecommerce`) con VS Code y lanzá `index.html` utilizando la extensión **Live Server** en el puerto `5500`.

La aplicación estará accesible en:
**http://127.0.0.1:5500** o **http://localhost:5500**

---

## 🛒 Funcionalidades Principales

### 1. Tienda y Catálogo
- Consulta dinámica de productos desde el backend (`GET /api/productos`).
- Presentación monetaria en formato argentino (`Intl.NumberFormat("es-AR")`, ej: `$ 1.600.000`).
- Detección de disponibilidad: productos con stock disponible muestran el botón **Agregar al carrito**; los productos con stock 0 muestran **Sin stock** deshabilitado.
- Límite de compra: el frontend y el backend impiden agregar o pedir cantidades que superen el stock disponible.

### 2. Carrito de Compras
- Gestión de cantidades, subtotal y total en tiempo real.
- Almacenamiento local mediante `localStorage`.
- Realización de pedido asociada al usuario (`POST /api/pedidos`), generando un pedido en estado `PENDIENTE` sin descontar stock hasta su confirmación.
- Modal de confirmación de compra y vaciado automático del carrito.

### 3. Panel de Administración
El panel de control cuenta con 5 módulos temáticos:

- **📦 Gestión de Productos (CRUD Completo):**
  - Alta de nuevo producto mediante modal interactivo con validaciones (nombre, categoría mediante selector desplegable cargado dinámicamente de `/api/categorias`, precio > 0, stock >= 0, stock mínimo >= 0).
  - Modificación de productos existentes (`PUT /api/productos/{id}`).
  - Eliminación con confirmación modal (`DELETE /api/productos/{id}`).
  - Actualización automática de la tabla administrativa y del catálogo de la tienda.

- **🏷️ Gestión de Categorías (CRUD Completo):**
  - Gestión independiente de categorías (`/api/categorias`).
  - Alta mediante modal con validación de nombre no vacío y prevención de nombres duplicados.
  - Modificación con actualización en cascada sobre los productos que la utilicen.
  - Eliminación con validación de integridad referencial: no permite borrar categorías asignadas a productos activos.

- **📊 Control de Stock:**
  - Visualización del stock actual y stock mínimo de cada producto.
  - Distintivos visuales dinámicos: *Disponible* (verde), *Stock bajo* (amarillo) y *Sin stock* (gris/rojo).

- **🛒 Seguimiento de Pedidos (Ciclo de Vida):**
  - Flujo controlado de transiciones:
    `PENDIENTE` ➔ `CONFIRMADO` ➔ `ENVIADO` ➔ `ENTREGADO`
  - Cancelación controlada (`CANCELADO`).
  - **Descuento de stock transaccional:** el stock se descuenta exactamente una vez al pasar a `CONFIRMADO`. Si el stock es insuficiente, se rechaza la operación con HTTP 400.
  - **Devolución de stock:** si un pedido `CONFIRMADO` es cancelado, las unidades regresan automáticamente al stock.
  - El backend valida y restringe transiciones incoherentes (ej. de pendiente a entregado, o modificar un pedido cancelado/entregado).

- **👤 Usuarios e Historial:**
  - Consulta de usuarios registrados en el sistema (`GET /api/usuarios`).
  - Botón **Ver historial** (`GET /api/usuarios/{id}/pedidos`) para visualizar en modal todos los pedidos realizados por un cliente, con detalle de productos, cantidades, precios unitarios y estado.

---

## 🛡️ Manejo Global de Excepciones (REST)

El backend implementa un `GlobalExceptionHandler` con `@RestControllerAdvice` que captura los errores previsibles y devuelve respuestas JSON limpias y homogéneas:

| Situación | Código HTTP | Mensaje |
|-----------|-------------|---------|
| Recurso inexistente (Producto / Categoría / Pedido / Usuario) | `404 Not Found` | Recurso con ID X no encontrado |
| Categoría duplicada o nombre vacío | `400 Bad Request` | Ya existe una categoría con el nombre: X |
| Intento de borrar categoría en uso | `400 Bad Request` | No se puede eliminar la categoría X porque está siendo utilizada |
| Stock insuficiente para confirmar o comprar | `400 Bad Request` | Stock insuficiente para el producto: X |
| Transición inválida de estado | `400 Bad Request` | Un pedido cancelado/entregado no puede cambiar de estado |
| Violación de validación de formulario | `400 Bad Request` | Error en el campo 'X': descripción |
| Integridad referencial (ej: eliminar producto con pedidos) | `400 Bad Request` | No se puede completar la operación por registros asociados |
| Errores no controlados | `500 Internal Error`| Ocurrió un error interno en el servidor (sin stack trace) |

---

## 👥 Estructura del Código

```text
backend/src/main/java/com/techlab/backend/
├── categoria/
│   └── Categoria.java
├── controller/
│   ├── CategoriaController.java
│   ├── PedidoController.java
│   ├── ProductoController.java
│   └── UsuarioController.java
├── exception/
│   ├── CategoriaNoEncontradaException.java
│   ├── GlobalExceptionHandler.java
│   ├── PedidoNoEncontradoException.java
│   ├── ProductoNoEncontradoException.java
│   ├── StockInsuficienteException.java
│   └── UsuarioNoEncontradoException.java
├── pedido/
│   ├── DetallePedido.java
│   ├── EstadoPedido.java
│   └── Pedido.java
├── producto/
│   └── Producto.java
├── repository/
│   ├── CategoriaRepository.java
│   ├── DetallePedidoRepository.java
│   ├── PedidoRepository.java
│   ├── ProductoRepository.java
│   └── UsuarioRepository.java
├── service/
│   ├── CategoriaService.java
│   ├── PedidoService.java
│   ├── ProductoService.java
│   └── UsuarioService.java
├── usuario/
│   └── Usuario.java
└── BackendApplication.java
```
