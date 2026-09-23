package com.techlab.pedidos;

import java.util.List;

import com.techlab.productos.Cliente;

public class Pedido {

	private int id;
	private Cliente cliente;
	private List<LineaPedido> lineas;

	public Pedido(int id, Cliente cliente, List<LineaPedido> lineas) {
		super();
		this.id = id;
		this.cliente = cliente;
		this.lineas = lineas;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public List<LineaPedido> getLineas() {
		return lineas;
	}

	public void setLineas(List<LineaPedido> lineas) {
		this.lineas = lineas;
	}

	public double calcularTotal() {
		double total = 0;

		for (LineaPedido linea : lineas) {
			total += linea.calcularSubtotal();
		}

		return total;
	}

	@Override
	public String toString() {
		return "Pedido [id=" + id + ", cliente=" + cliente + ", lineas=" + lineas + "]";
	}
}
