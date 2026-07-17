-- ============================================================
--  GESTOR DE ALMACÉN DE PRENDAS DE ROPA
--  Base de datos: almacen_ropa
-- ============================================================

CREATE DATABASE IF NOT EXISTS almacen_ropa
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE almacen_ropa;

-- ------------------------------------------------------------
-- TABLA: usuarios
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100)                        NOT NULL,
    usuario     VARCHAR(50)  UNIQUE                 NOT NULL,
    contrasena  VARCHAR(255)                        NOT NULL,   -- SHA-256 hex
    rol         ENUM('ADMINISTRADOR','ALMACENISTA','DESPACHADOR') NOT NULL,
    activo      TINYINT(1)   DEFAULT 1,
    creado_en   DATETIME     DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
-- TABLA: categorias
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categorias (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(80)  UNIQUE NOT NULL,
    descripcion VARCHAR(255)
);

-- ------------------------------------------------------------
-- TABLA: productos  (prendas de ropa)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS productos (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    codigo          VARCHAR(30)  UNIQUE NOT NULL,
    nombre          VARCHAR(120) NOT NULL,
    descripcion     VARCHAR(255),
    categoria_id    INT          NOT NULL,
    talla           ENUM('XS','S','M','L','XL','XXL','UNICA') NOT NULL,
    color           VARCHAR(50),
    precio_unitario DECIMAL(10,2) NOT NULL,
    stock_actual    INT          NOT NULL DEFAULT 0,
    stock_minimo    INT          NOT NULL DEFAULT 5,
    activo          TINYINT(1)   DEFAULT 1,
    creado_en       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prod_cat FOREIGN KEY (categoria_id) REFERENCES categorias(id)
);

-- ------------------------------------------------------------
-- TABLA: solicitudes  (altas / bajas de inventario)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS solicitudes (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    tipo            ENUM('ALTA','BAJA')             NOT NULL,
    producto_id     INT                             NOT NULL,
    cantidad        INT                             NOT NULL CHECK (cantidad > 0),
    motivo          VARCHAR(255),
    estado          ENUM('PENDIENTE','APROBADA','RECHAZADA') DEFAULT 'PENDIENTE',
    solicitante_id  INT                             NOT NULL,
    aprobador_id    INT,
    observacion     VARCHAR(255),
    creado_en       DATETIME DEFAULT CURRENT_TIMESTAMP,
    resuelto_en     DATETIME,
    CONSTRAINT fk_sol_prod FOREIGN KEY (producto_id)    REFERENCES productos(id),
    CONSTRAINT fk_sol_solic FOREIGN KEY (solicitante_id) REFERENCES usuarios(id),
    CONSTRAINT fk_sol_apro  FOREIGN KEY (aprobador_id)   REFERENCES usuarios(id)
);

-- ------------------------------------------------------------
-- TABLA: despachos
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS despachos (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    folio           VARCHAR(30)  UNIQUE NOT NULL,
    producto_id     INT                             NOT NULL,
    cantidad        INT                             NOT NULL CHECK (cantidad > 0),
    destinatario    VARCHAR(120) NOT NULL,
    estado          ENUM('DESPACHADO','SIN_STOCK')  NOT NULL,
    despachador_id  INT                             NOT NULL,
    observacion     VARCHAR(255),
    creado_en       DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_des_prod  FOREIGN KEY (producto_id)    REFERENCES productos(id),
    CONSTRAINT fk_des_desp  FOREIGN KEY (despachador_id) REFERENCES usuarios(id)
);

-- ------------------------------------------------------------
-- TABLA: historial_inventario  (auditoría de movimientos)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS historial_inventario (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    producto_id     INT NOT NULL,
    tipo_movimiento ENUM('ALTA','BAJA','DESPACHO') NOT NULL,
    cantidad        INT NOT NULL,
    stock_anterior  INT NOT NULL,
    stock_nuevo     INT NOT NULL,
    referencia_id   INT,          -- id de solicitud o despacho
    usuario_id      INT NOT NULL,
    fecha           DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hist_prod FOREIGN KEY (producto_id) REFERENCES productos(id),
    CONSTRAINT fk_hist_user FOREIGN KEY (usuario_id)  REFERENCES usuarios(id)
);

-- ============================================================
--  DATOS INICIALES
-- ============================================================

-- Usuarios (contraseña: "admin123" / "almacen123" / "despacho123"  → SHA-256)
INSERT INTO usuarios (nombre, usuario, contrasena, rol) VALUES
('Administrador General', 'admin',
 '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMINISTRADOR'),
('Carlos Almacén',       'almacenista',
 '7439033334ff7f5d2d164c47fe54b5819c0f7cd1225ecbc3bbac5bc80206b01f', 'ALMACENISTA'),
('Luis Despacho',        'despachador',
 '861c7061d0b37a55c93270f41a412d33ed9600a5309a1b75b4f58147680e1c82', 'DESPACHADOR');

-- Categorías
INSERT INTO categorias (nombre, descripcion) VALUES
('Camisas',   'Camisas y blusas de todo tipo'),
('Pantalones','Pantalones de vestir, casual y deportivo'),
('Vestidos',  'Vestidos formales e informales'),
('Abrigos',   'Abrigos, chamarras y chaquetas'),
('Accesorios','Cinturones, gorras, bufandas');

-- Productos de ejemplo
INSERT INTO productos (codigo,nombre,descripcion,categoria_id,talla,color,precio_unitario,stock_actual,stock_minimo) VALUES
('CAM-001','Camisa Oxford Blanca',  'Camisa de algodón manga larga',1,'M','Blanco', 299.00,20,5),
('CAM-002','Camisa Polo Azul',      'Polo de algodón piqué',       1,'L','Azul',   189.00,15,5),
('PAN-001','Pantalón Casual Negro', 'Pantalón de mezclilla',       2,'32','Negro', 450.00,10,3),
('PAN-002','Pantalón Deportivo Gris','Jogger de algodón',          2,'M','Gris',   220.00, 8,3),
('VES-001','Vestido Floral Verano', 'Vestido ligero estampado',    3,'S','Multicolor',350.00,6,2),
('ABR-001','Chamarra Denim Azul',   'Chamarra vaquera unisex',     4,'L','Azul',   580.00,4,2),
('ACC-001','Gorra Visera Negra',    'Gorra ajustable 100% algodón',5,'UNICA','Negro',120.00,25,5);
