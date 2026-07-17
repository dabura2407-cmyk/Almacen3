# 🧥 Gestor de Almacén de Prendas de Ropa

Sistema de control de inventario de prendas de ropa con tres roles de usuario,
desarrollado en **Java** con base de datos **MySQL/MariaDB**.

---

## Estructura del Proyecto

```
AlmacenRopa/
├── sql/
│   └── schema.sql                          ← Script SQL completo (BD + datos de prueba)
└── src/main/java/almacen/
    ├── Main.java                           ← Punto de entrada
    ├── model/
    │   ├── Usuario.java
    │   ├── Producto.java
    │   ├── Solicitud.java
    │   └── Despacho.java
    ├── dao/
    │   ├── UsuarioDAO.java
    │   ├── ProductoDAO.java
    │   ├── SolicitudDAO.java
    │   ├── DespachoDAO.java
    │   └── HistorialDAO.java
    ├── service/
    │   └── AlmacenService.java             ← Lógica de negocio
    └── ui/
        ├── Consola.java                    ← Utilidades de consola (colores ANSI)
        ├── MenuAdministrador.java
        ├── MenuAlmacenista.java
        └── MenuDespachador.java
```

---

## Requisitos

| Herramienta       | Versión mínima |
|-------------------|----------------|
| Java JDK          | 17             |
| MySQL / MariaDB   | 8.0 / 10.5     |
| Conector JDBC     | mysql-connector-j-8.x.jar |

---

## Instalación

### 1. Base de datos

```sql
-- En tu cliente MySQL (Workbench, DBeaver, terminal, etc.)
SOURCE /ruta/al/proyecto/sql/schema.sql;
```

Esto crea la base de datos `almacen_ropa`, todas las tablas y datos de prueba.

### 2. Configurar la conexión

Edita `src/main/java/almacen/util/ConexionDB.java`:

```java
private static final String URL      = "jdbc:mysql://localhost:3306/almacen_ropa...";
private static final String USER     = "root";       // tu usuario MySQL
private static final String PASSWORD = "";           // tu contraseña MySQL
```

### 3. Compilar

```bash
# Desde la raíz del proyecto
javac -cp ".;lib/mysql-connector-j-8.x.x.jar" \
      -d out \
      src/main/java/almacen/**/*.java \
      src/main/java/almacen/*.java
```

> En Linux/Mac usa `:` en vez de `;` como separador del classpath.

### 4. Ejecutar

```bash
java -cp "out;lib/mysql-connector-j-8.x.x.jar" almacen.Main
```

---

## Credenciales de Prueba

| Usuario       | Contraseña    | Rol             |
|---------------|---------------|-----------------|
| `admin`       | `admin123`    | ADMINISTRADOR   |
| `almacenista` | `almacen123`  | ALMACENISTA     |
| `despachador` | `despacho123` | DESPACHADOR     |

> **Nota (corrección aplicada):** los hashes SHA-256 de `almacenista` y `despachador`
> en `sql/schema.sql` no correspondían originalmente a estas contraseñas. Ya fueron
> recalculados y corregidos; las tres cuentas funcionan tal como se documenta.

---

## Flujo de Trabajo

```
ALMACENISTA                    ADMINISTRADOR              DESPACHADOR
     │                               │                          │
     ├─ Crea solicitud ALTA/BAJA ──► │                          │
     │   (queda PENDIENTE)           ├─ Aprueba/Rechaza         │
     │                               │  (actualiza stock)       │
     │ ◄─ Estado actualizado ────────┤                          │
     │                               │                          │
     │                               │          Consulta ───────┤
     │                               │          inventario       │
     │                               │                          │
     │                               │          Registra ───────┤
     │                               │          despacho         │
     │                               │          (DESPACHADO o    │
     │                               │           SIN_STOCK)      │
```

---

## Roles y Permisos

### 👤 ADMINISTRADOR
- CRUD completo de productos y usuarios
- Consultar inventario
- **Aprobar o rechazar** solicitudes de alta/baja (actualiza stock automáticamente)
- Ver historial de solicitudes y despachos
- Ver alertas de stock bajo

### 📦 ALMACENISTA
- Consultar inventario
- **Solicitar ALTA** de inventario (requiere aprobación)
- **Solicitar BAJA** de inventario (requiere aprobación)
- Ver el estado de sus propias solicitudes

### 🚚 DESPACHADOR
- Consultar inventario
- **Registrar despacho**: si hay stock suficiente → `DESPACHADO` y descuenta stock
- Si no hay stock → registra aclaración `SIN_STOCK` con folio de evidencia
- Ver historial de sus propios despachos

---

## Tablas de la Base de Datos

| Tabla                  | Descripción                                      |
|------------------------|--------------------------------------------------|
| `usuarios`             | Cuentas del sistema con rol y contraseña (SHA-256) |
| `categorias`           | Categorías de prendas (Camisas, Pantalones, etc.) |
| `productos`            | Catálogo de prendas con stock                    |
| `solicitudes`          | Solicitudes de alta/baja pendientes de aprobación |
| `despachos`            | Registro de salidas de mercancía                 |
| `historial_inventario` | Auditoría de todos los movimientos de stock      |
