// Script de inicialización - Sistema de Administración de Empleados
// Uso: mongosh --file baseDatos/init_db.js

use("empleadodb");

db.usuarios.deleteMany({});
db.empleados.deleteMany({});
db.calculos.deleteMany({});

db.usuarios.createIndex({ usuario: 1 }, { unique: true });
db.empleados.createIndex({ codigo: 1 }, { unique: true });
db.calculos.createIndex({ codigoEmpleado: 1 });

// Usuario administrador
db.usuarios.insertOne({
  usuario: "admin",
  password: "$2a$10$afFbZ9BVmheWorvJDQcSauuR1eVxWd6B.Kh39GrNjFJ99XU9.Zi1C",
  rol: "ADMIN",
  activo: true
});

// Los 7 empleados de prueba
db.empleados.insertMany([
  { codigo: "EMP-001", nombre: "María Fernanda López", documento: "01234567-8",
    cargo: "Gerente Administrativa", fechaIngreso: new Date("2020-03-15"),
    tipo: "ADMINISTRATIVO", activo: true, salarioBase: 1200.00 },
  { codigo: "EMP-002", nombre: "Carlos Alberto Ramírez", documento: "02345678-9",
    cargo: "Contador", fechaIngreso: new Date("2021-07-01"),
    tipo: "ADMINISTRATIVO", activo: true, salarioBase: 950.00 },
  { codigo: "EMP-003", nombre: "José Manuel Hernández", documento: "03456789-0",
    cargo: "Auxiliar de Bodega", fechaIngreso: new Date("2022-01-10"),
    tipo: "POR_HORA", activo: true, tarifaHora: 4.50, horasTrabajadas: 160 },
  { codigo: "EMP-004", nombre: "Ana Patricia Martínez", documento: "04567890-1",
    cargo: "Recepcionista", fechaIngreso: new Date("2023-05-22"),
    tipo: "POR_HORA", activo: true, tarifaHora: 5.25, horasTrabajadas: 120 },
  { codigo: "EMP-005", nombre: "Luis Enrique Castillo", documento: "05678901-2",
    cargo: "Vendedor", fechaIngreso: new Date("2021-11-08"),
    tipo: "POR_COMISION", activo: true, montoVentas: 8000.00, porcentajeComision: 5 },
  { codigo: "EMP-006", nombre: "Sofía Alejandra Rivera", documento: "06789012-3",
    cargo: "Ejecutiva de Ventas", fechaIngreso: new Date("2022-09-19"),
    tipo: "POR_COMISION", activo: true, montoVentas: 12500.00, porcentajeComision: 7.5 },
  { codigo: "EMP-007", nombre: "Roberto Carlos Menjívar", documento: "07890123-4",
    cargo: "Auxiliar", fechaIngreso: new Date("2019-02-04"),
    tipo: "POR_HORA", activo: false, tarifaHora: 4.00, horasTrabajadas: 0 }
]);

print("Base de datos inicializada correctamente.");