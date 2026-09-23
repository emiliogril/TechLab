package com.techlab.main;

import java.util.ArrayList;

import java.util.List;

import java.util.Scanner;
import java.util.InputMismatchException;

import com.techlab.productos.Cliente;
import com.techlab.productos.Producto;
import com.techlab.pedidos.LineaPedido;
import com.techlab.pedidos.Pedido;
import com.techlab.excepciones.StockInsuficienteException;

public class Main {

	public static void main(String[] args) {

		Scanner scanner = new Scanner(System.in);

		// Productos disponibles
		Producto producto1 = new Producto(1, "Notebook", 1500.0, 10);
		Producto producto2 = new Producto(2, "Mouse", 50.0, 20);

		List<Producto> productos = new ArrayList<>();

		productos.add(producto1);
		productos.add(producto2);

		// Cliente
		Cliente cliente1 = new Cliente(1, "Emilio", "emiliogril@gmail.com");
		List<Pedido> pedidos = new ArrayList<>();

		int opcion = 0;
		while (opcion != 7) {

			System.out.println();
			System.out.println("=== TECHLAB ===");
			System.out.println("1. Ver productos");
			System.out.println("2. Realizar pedido");
			System.out.println("3. Agregar producto");
			System.out.println("4. Buscar / actualizar producto");
			System.out.println("5. Eliminar producto");
			System.out.println("6. Listar pedidos");
			System.out.println("7. Salir");
			System.out.print("Seleccione una opción: ");

			try {
				opcion = scanner.nextInt();
			} catch (InputMismatchException e) {
				System.out.println("Error: debe ingresar un número.");
				scanner.nextLine();
				continue;
			}

			switch (opcion) {

			case 1:
				System.out.println("=== PRODUCTOS DISPONIBLES ===");

				for (Producto producto : productos) {
					System.out.println(producto);
				}

				break;

			case 2:

				System.out.println("=== REALIZAR PEDIDO ===");

				System.out.print("¿Cuántos productos desea agregar al pedido?: ");

				int cantidadProductos;

				try {
					cantidadProductos = scanner.nextInt();
				} catch (InputMismatchException e) {
					System.out.println("Error: debe ingresar un número.");
					scanner.nextLine();
					break;
				}

				if (cantidadProductos <= 0) {
					System.out.println("La cantidad de productos debe ser mayor a 0.");
					break;
				}

				List<LineaPedido> lineas = new ArrayList<>();

				boolean pedidoValido = true;

				System.out.println("Productos disponibles:");

				for (Producto producto : productos) {
					System.out.println(producto.getId() + ". " + producto.getNombre() + " - $" + producto.getPrecio()
							+ " - Stock: " + producto.getStock());
				}

				for (int i = 0; i < cantidadProductos; i++) {

					System.out.println();
					System.out.println("Producto " + (i + 1) + " de " + cantidadProductos);

					System.out.print("Seleccione el ID del producto: ");

					int idProducto;

					try {
						idProducto = scanner.nextInt();
					} catch (InputMismatchException e) {
						System.out.println("Error: el ID debe ser un número.");
						scanner.nextLine();
						pedidoValido = false;
						break;
					}

					System.out.print("Ingrese la cantidad: ");

					int cantidad;

					try {
						cantidad = scanner.nextInt();
					} catch (InputMismatchException e) {
						System.out.println("Error: la cantidad debe ser un número.");
						scanner.nextLine();
						pedidoValido = false;
						break;
					}

					Producto productoSeleccionado = null;

					for (Producto producto : productos) {
						if (producto.getId() == idProducto) {
							productoSeleccionado = producto;
							break;
						}
					}

					if (productoSeleccionado == null) {
					    System.out.println("Producto inválido");
					    pedidoValido = false;
					    break;
					}

					boolean productoRepetido = false;

					for (LineaPedido linea : lineas) {
					    if (linea.getProducto().getId() == productoSeleccionado.getId()) {
					        productoRepetido = true;
					        break;
					    }
					}

					if (productoRepetido) {
					    System.out.println("El producto ya fue agregado al pedido.");
					    pedidoValido = false;
					    break;
					}

					if (cantidad <= 0) {
						System.out.println("La cantidad debe ser mayor a 0.");
						pedidoValido = false;
						break;
					}

					if (cantidad > productoSeleccionado.getStock()) {
						try {
							throw new StockInsuficienteException(
									"Stock insuficiente. Stock disponible: " + productoSeleccionado.getStock());
						} catch (StockInsuficienteException e) {
							System.out.println(e.getMessage());
							pedidoValido = false;
							break;
						}
					}

					LineaPedido linea1 = new LineaPedido(productoSeleccionado, cantidad);

					lineas.add(linea1);

				}

				if (!pedidoValido) {
					System.out.println("Pedido cancelado.");
					break;
				}

				System.out.print("¿Confirmar pedido? (S/N): ");
				String confirmar = scanner.next();

				if (!confirmar.equalsIgnoreCase("S")) {
					System.out.println("Pedido cancelado.");
					break;
				}

				Pedido pedido1 = new Pedido(pedidos.size() + 1, cliente1, lineas);

				pedidos.add(pedido1);

				for (LineaPedido linea : lineas) {
					linea.getProducto().reducirStock(linea.getCantidad());
				}

				System.out.println("=== PEDIDO CREADO ===");
				System.out.println("TOTAL DEL PEDIDO: $" + pedido1.calcularTotal());

				break;

			case 3:

				System.out.println("=== AGREGAR PRODUCTO ===");

				System.out.print("Ingrese ID: ");

				int nuevoId;

				try {
					nuevoId = scanner.nextInt();
				} catch (InputMismatchException e) {
					System.out.println("Error: el ID debe ser un número.");
					scanner.nextLine();
					break;
				}

				boolean idExiste = false;

				for (Producto producto : productos) {
					if (producto.getId() == nuevoId) {
						idExiste = true;
						break;
					}
				}

				if (idExiste) {
					System.out.println("Ya existe un producto con ese ID.");
					break;
				}

				scanner.nextLine();

				System.out.print("Ingrese nombre: ");
				String nuevoNombre = scanner.nextLine();

				System.out.print("Ingrese precio: ");

				double nuevoPrecio;

				try {
					nuevoPrecio = scanner.nextDouble();
				} catch (InputMismatchException e) {
					System.out.println("Error: el precio debe ser un número.");
					scanner.nextLine();
					break;
				}

				System.out.print("Ingrese stock: ");

				int nuevoStock;

				try {
					nuevoStock = scanner.nextInt();
				} catch (InputMismatchException e) {
					System.out.println("Error: el stock debe ser un número.");
					scanner.nextLine();
					break;
				}

				if (nuevoPrecio <= 0) {
					System.out.println("El precio debe ser mayor a 0.");
					break;
				}

				if (nuevoStock < 0) {
					System.out.println("El stock no puede ser negativo.");
					break;
				}

				Producto nuevoProducto = new Producto(nuevoId, nuevoNombre, nuevoPrecio, nuevoStock);

				productos.add(nuevoProducto);

				System.out.println("Producto agregado correctamente.");

				break;

			case 4:
				System.out.println("=== BUSCAR / ACTUALIZAR PRODUCTO ===");

				System.out.print("Ingrese el ID del producto: ");

				int idBuscar;

				try {
					idBuscar = scanner.nextInt();
				} catch (InputMismatchException e) {
					System.out.println("Error: el ID debe ser un número.");
					scanner.nextLine();
					break;
				}

				Producto productoEncontrado = null;

				for (Producto producto : productos) {
					if (producto.getId() == idBuscar) {
						productoEncontrado = producto;
						break;
					}
				}

				if (productoEncontrado == null) {
					System.out.println("Producto no encontrado.");
					break;
				}

				System.out.println("Producto encontrado:");
				System.out.println(productoEncontrado);

				System.out.print("Ingrese nuevo precio: ");

				double precioActualizado;

				try {
					precioActualizado = scanner.nextDouble();
				} catch (InputMismatchException e) {
					System.out.println("Error: el precio debe ser un número.");
					scanner.nextLine();
					break;
				}

				System.out.print("Ingrese nuevo stock: ");

				int stockActualizado;

				try {
					stockActualizado = scanner.nextInt();
				} catch (InputMismatchException e) {
					System.out.println("Error: el stock debe ser un número.");
					scanner.nextLine();
					break;
				}

				if (precioActualizado <= 0) {
					System.out.println("El precio debe ser mayor a 0.");
					break;
				}

				if (stockActualizado < 0) {
					System.out.println("El stock no puede ser negativo.");
					break;
				}

				productoEncontrado.setPrecio(precioActualizado);
				productoEncontrado.setStock(stockActualizado);

				System.out.println("Producto actualizado correctamente.");
				System.out.println(productoEncontrado);

				break;

			case 5:
				System.out.println("=== ELIMINAR PRODUCTO ===");

				System.out.print("Ingrese el ID del producto a eliminar: ");

				int idEliminar;

				try {
					idEliminar = scanner.nextInt();
				} catch (InputMismatchException e) {
					System.out.println("Error: el ID debe ser un número.");
					scanner.nextLine();
					break;
				}

				Producto productoEliminar = null;

				for (Producto producto : productos) {
					if (producto.getId() == idEliminar) {
						productoEliminar = producto;
						break;
					}
				}

				if (productoEliminar == null) {
					System.out.println("Producto no encontrado.");
					break;
				}

				productos.remove(productoEliminar);

				System.out.println("Producto eliminado correctamente.");

				break;

			case 6:
				System.out.println("=== PEDIDOS REALIZADOS ===");

				for (Pedido pedido : pedidos) {
					System.out.println(pedido);
					System.out.println("TOTAL: $" + pedido.calcularTotal());
				}

				break;

			case 7:
				System.out.println("Saliendo del sistema...");
				break;

			default:
				System.out.println("Opción inválida");
			}
		}
		scanner.close();
	}
}