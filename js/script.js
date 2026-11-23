document.addEventListener("DOMContentLoaded", () => {

  const API_URL = "http://localhost:8080/api";

  // Cache en memoria
  let productosGlobales = [];
  let categoriasGlobales = [];

  // Instancias de modales de Bootstrap
  const productoModalEl = document.getElementById("productoModal");
  const productoModal = productoModalEl ? new bootstrap.Modal(productoModalEl) : null;

  const confirmarEliminarModalEl = document.getElementById("confirmarEliminarModal");
  const confirmarEliminarModal = confirmarEliminarModalEl ? new bootstrap.Modal(confirmarEliminarModalEl) : null;

  const categoriaModalEl = document.getElementById("categoriaModal");
  const categoriaModal = categoriaModalEl ? new bootstrap.Modal(categoriaModalEl) : null;

  const confirmarEliminarCategoriaModalEl = document.getElementById("confirmarEliminarCategoriaModal");
  const confirmarEliminarCategoriaModal = confirmarEliminarCategoriaModalEl ? new bootstrap.Modal(confirmarEliminarCategoriaModalEl) : null;

  const historialPedidosModalEl = document.getElementById("historialPedidosModal");
  const historialPedidosModal = historialPedidosModalEl ? new bootstrap.Modal(historialPedidosModalEl) : null;

  let idProductoAEliminar = null;
  let idCategoriaAEliminar = null;

  // =====================================================
  // FORMATEO MONETARIO (es-AR)
  // =====================================================
  function formatearMoneda(valor) {
    const num = Number(valor) || 0;
    return `$ ${new Intl.NumberFormat("es-AR", {
      minimumFractionDigits: 0,
      maximumFractionDigits: 2
    }).format(num)}`;
  }

  // =====================================================
  // CARGA INICIAL
  // =====================================================
  cargarCategoriasDesdeAPI();
  cargarProductosDesdeAPI();

  async function cargarCategoriasDesdeAPI() {
    try {
      const response = await fetch(`${API_URL}/categorias`);
      if (response.ok) {
        categoriasGlobales = await response.json();
      }
    } catch (error) {
      console.error("Error al obtener categorías:", error);
    }
  }

  async function poblarSelectCategorias(categoriaSeleccionada = "") {
    const select = document.getElementById("prod-categoria");
    if (!select) return;

    select.innerHTML = '<option value="">Cargando categorías...</option>';

    try {
      const response = await fetch(`${API_URL}/categorias`);
      if (response.ok) {
        categoriasGlobales = await response.json();
      }
    } catch (e) {
      console.error("Error al refrescar categorías para el select:", e);
    }

    select.innerHTML = '<option value="">Seleccione una categoría...</option>';

    let categoriaEncontrada = false;

    if (Array.isArray(categoriasGlobales)) {
      categoriasGlobales.forEach((cat) => {
        const isSelected =
          categoriaSeleccionada &&
          cat.nombre.trim().toLowerCase() === categoriaSeleccionada.trim().toLowerCase();

        if (isSelected) categoriaEncontrada = true;

        select.innerHTML += `
          <option value="${cat.nombre}" ${isSelected ? "selected" : ""}>
            ${cat.nombre}
          </option>
        `;
      });
    }

    // Preservar compatibilidad con productos que tengan categorías existentes que no estén en la lista
    if (categoriaSeleccionada && !categoriaEncontrada) {
      select.innerHTML += `
        <option value="${categoriaSeleccionada}" selected>
          ${categoriaSeleccionada} (Actual)
        </option>
      `;
    }
  }

  // =====================================================
  // PRODUCTOS (TIENDA PÚBLICA)
  // =====================================================

  async function cargarProductosDesdeAPI() {
    try {
      const response = await fetch(`${API_URL}/productos`);

      if (!response.ok) {
        throw new Error("API de productos no disponible");
      }

      const productos = await response.json();

      if (!Array.isArray(productos)) {
        throw new Error("Formato inesperado de la API");
      }

      productosGlobales = productos;
      cargarProductos(productos);

    } catch (error) {
      console.error("Error al obtener productos:", error);
    }
  }

  function cargarProductos(productos) {
    const productosContainer = document.getElementById("productos-container");
    if (!productosContainer) return;

    productosContainer.innerHTML = "";

    productos.forEach((producto) => {
      const descripcion = producto.descripcion || "Sin descripción";
      const shortDescription = descripcion.split(" ").slice(0, 5).join(" ") + "...";

      const imagenProducto =
        producto.id === 1 && !producto.imagenUrl
          ? "img/notebook-gamer.jpg"
          : producto.id === 3 && !producto.imagenUrl
            ? "img/mouse-gamer.jpg"
            : producto.imagenUrl || (producto.id === 1 ? "img/notebook-gamer.jpg" : producto.id === 3 ? "img/mouse-gamer.jpg" : "https://dummyimage.com/300x200/e2e8f0/0f172a.png&text=TechLab");

      const nombreEscapado = (producto.nombre || "").replace(/'/g, "\\'");
      const imagenEscapada = (imagenProducto || "").replace(/'/g, "\\'");

      productosContainer.innerHTML += `
        <div class="card">
          <img
            src="${imagenProducto}"
            class="card-img-top"
            alt="${producto.nombre}"
            onerror="this.src='https://dummyimage.com/300x200/e2e8f0/0f172a.png&text=TechLab'"
          >

          <div class="card-body">
            <h5 class="card-title">
              ${producto.nombre}
            </h5>

            <p class="card-text short-description">
              ${shortDescription}
            </p>

            <p class="card-text full-description" style="display:none;">
              ${descripcion}
            </p>

            <button
              class="btn btn-link"
              onclick="toggleDescription(this)"
            >
              Ver descripción
            </button>

            <p class="card-text fw-bold fs-5 text-primary mb-3">
              ${formatearMoneda(producto.precio)}
            </p>

            ${producto.stock > 0
          ? `
              <button
                class="btn btn-primary"
                onclick="addToCart(
                  ${producto.id},
                  '${imagenEscapada}',
                  '${nombreEscapado}',
                  ${producto.precio},
                  ${producto.stock},
                  this
                )"
              >
                Agregar al carrito
              </button>
            `
          : `
              <button
                class="btn btn-secondary"
                disabled
              >
                Sin stock
              </button>
            `
        }

          </div>
        </div>
      `;
    });
  }

  // =====================================================
  // DESCRIPCIÓN ACORDEÓN
  // =====================================================
  window.toggleDescription = function (button) {
    const shortDescription = button.previousElementSibling;
    const fullDescription = shortDescription.previousElementSibling;

    if (fullDescription.style.display === "none") {
      fullDescription.style.display = "block";
      shortDescription.style.display = "none";
      button.textContent = "Ocultar descripción";
    } else {
      fullDescription.style.display = "none";
      shortDescription.style.display = "block";
      button.textContent = "Ver descripción";
    }
  };

  // =====================================================
  // CARRITO
  // =====================================================

  window.addToCart = function (id, image, title, price, stock, button) {
    let cart = JSON.parse(localStorage.getItem("cart")) || [];

    const existingProduct = cart.find((product) => product.id === id);
    const cantidadActualEnCarrito = existingProduct ? existingProduct.quantity : 0;

    if (cantidadActualEnCarrito >= stock) {
      alert(`No podés agregar más unidades. El stock disponible de "${title}" es ${stock}.`);
      return;
    }

    if (existingProduct) {
      existingProduct.quantity++;
      existingProduct.image = image;
      existingProduct.stockMax = stock;
    } else {
      cart.push({
        id,
        image,
        title,
        price,
        stockMax: stock,
        quantity: 1
      });
    }

    localStorage.setItem("cart", JSON.stringify(cart));
    updateCartUI();

    if (button) {
      const originalText = button.textContent;
      button.textContent = "Agregado";
      button.disabled = true;

      setTimeout(() => {
        button.textContent = originalText;
        button.disabled = false;
      }, 1000);
    }
  };

  function updateCartUI() {
    const cart = JSON.parse(localStorage.getItem("cart")) || [];
    const carritoItems = document.getElementById("carrito-items");
    if (!carritoItems) return;

    carritoItems.innerHTML = "";
    let total = 0;

    const btnComprar = document.getElementById("realizar-compra");

    if (cart.length === 0) {
      carritoItems.innerHTML = '<li class="text-center text-muted w-100">Tu carrito está vacío</li>';
      if (btnComprar) btnComprar.disabled = true;
    } else {
      if (btnComprar) btnComprar.disabled = false;

      cart.forEach((item) => {
        const subtotal = item.price * item.quantity;

        carritoItems.innerHTML += `
          <li class="cart-item">
            <img
              src="${item.image}"
              alt="${item.title}"
              onerror="this.src='https://dummyimage.com/150x150/e2e8f0/0f172a.png&text=TechLab'"
            >

            <div class="card-body">
              <h6 class="card-title">
                ${item.title}
              </h6>

              <p class="card-text">
                Cantidad: <strong>${item.quantity}</strong>
              </p>

              <p class="card-text">
                Precio unitario: ${formatearMoneda(item.price)}
              </p>

              <p class="card-text">
                Subtotal: <strong>${formatearMoneda(subtotal)}</strong>
              </p>

              <button
                class="btn btn-sm btn-danger mt-1"
                onclick="removeFromCart(${item.id})"
              >
                Eliminar
              </button>
            </div>
          </li>
        `;

        total += subtotal;
      });
    }

    const totalEl = document.getElementById("carrito-total");
    if (totalEl) {
      totalEl.textContent = formatearMoneda(total);
    }

    const cantidadTotal = cart.reduce((sum, item) => sum + item.quantity, 0);

    const counterSidebar = document.getElementById("cart-counter");
    if (counterSidebar) counterSidebar.textContent = cantidadTotal;

    const counterNav = document.getElementById("cart-counter-nav");
    if (counterNav) counterNav.textContent = cantidadTotal;
  }

  window.removeFromCart = function (id) {
    let cart = JSON.parse(localStorage.getItem("cart")) || [];
    cart = cart.filter((item) => item.id !== id);
    localStorage.setItem("cart", JSON.stringify(cart));
    updateCartUI();
  };

  const btnVaciar = document.getElementById("vaciar-carrito");
  if (btnVaciar) {
    btnVaciar.addEventListener("click", () => {
      localStorage.removeItem("cart");
      updateCartUI();
    });
  }

  // Manejo de apertura/cierre de carrito en móvil y tablet
  const openCartBtn = document.getElementById("open-cart");
  const closeCartBtn = document.getElementById("close-cart");
  const cartSidebar = document.getElementById("cart-sidebar");
  const cartOverlay = document.getElementById("cart-overlay");

  function abrirCarritoMobile(e) {
    if (e) e.preventDefault();
    if (cartSidebar) cartSidebar.classList.remove("hidden");
    if (cartOverlay) cartOverlay.classList.add("active");
  }

  function cerrarCarritoMobile() {
    if (cartSidebar && window.innerWidth <= 768) {
      cartSidebar.classList.add("hidden");
    }
    if (cartOverlay) cartOverlay.classList.remove("active");
  }

  if (openCartBtn) {
    openCartBtn.addEventListener("click", abrirCarritoMobile);
  }
  if (closeCartBtn) {
    closeCartBtn.addEventListener("click", cerrarCarritoMobile);
  }
  if (cartOverlay) {
    cartOverlay.addEventListener("click", cerrarCarritoMobile);
  }

  // =====================================================
  // REALIZAR COMPRA
  // =====================================================

  const btnRealizarCompra = document.getElementById("realizar-compra");
  if (btnRealizarCompra) {
    btnRealizarCompra.addEventListener("click", async () => {
      const cart = JSON.parse(localStorage.getItem("cart")) || [];

      if (cart.length === 0) {
        alert("Tu carrito está vacío");
        return;
      }

      const pedido = {
        usuario: {
          id: 1
        },
        detalles: cart.map((item) => ({
          cantidad: item.quantity,
          producto: {
            id: item.id
          }
        }))
      };

      try {
        const response = await fetch(`${API_URL}/pedidos`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify(pedido)
        });

        if (!response.ok) {
          let mensaje = "No se pudo crear el pedido";
          try {
            const errorBackend = await response.json();
            if (errorBackend.mensaje) {
              mensaje = errorBackend.mensaje;
            }
          } catch (error) {
            console.error(error);
          }
          throw new Error(mensaje);
        }

        const pedidoCreado = await response.json();

        const modalTotal = document.getElementById("modal-total");
        if (modalTotal) {
          modalTotal.textContent = formatearMoneda(pedidoCreado.total);
        }

        const modalCompra = new bootstrap.Modal(document.getElementById("compraExitosaModal"));
        modalCompra.show();

        localStorage.removeItem("cart");
        updateCartUI();

        cargarProductosDesdeAPI();

      } catch (error) {
        console.error("Error al realizar la compra:", error);
        alert(error.message);
      }
    });
  }

  // =====================================================
  // ADMINISTRACIÓN GENERAL
  // =====================================================

  const adminContenido = document.getElementById("admin-contenido");

  // -----------------------------------------------------
  // ADMIN - PRODUCTOS (CRUD COMPLETO)
  // -----------------------------------------------------

  const btnAdminProductos = document.getElementById("admin-productos");
  if (btnAdminProductos) {
    btnAdminProductos.addEventListener("click", renderizarAdminProductos);
  }

  async function renderizarAdminProductos() {
    mostrarCargando();

    try {
      const response = await fetch(`${API_URL}/productos`);

      if (!response.ok) {
        throw new Error("No se pudieron obtener los productos");
      }

      const productos = await response.json();
      productosGlobales = productos;

      let html = `
        <div class="card shadow-sm">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
              <h3 class="mb-0">Gestión de Productos</h3>
              <button class="btn btn-primary" id="btn-nuevo-producto">
                + Nuevo producto
              </button>
            </div>

            <div class="table-responsive">
              <table class="table table-striped table-bordered align-middle">
                <thead class="table-dark">
                  <tr>
                    <th>ID</th>
                    <th>Nombre</th>
                    <th>Categoría</th>
                    <th>Precio</th>
                    <th>Stock</th>
                    <th>Stock Mín.</th>
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
      `;

      if (productos.length === 0) {
        html += `
          <tr>
            <td colspan="7" class="text-center text-muted py-4">No hay productos registrados.</td>
          </tr>
        `;
      } else {
        productos.forEach((producto) => {
          const stockMin = producto.stockMinimo != null ? producto.stockMinimo : 5;
          const badgeStock = producto.stock === 0
            ? '<span class="badge bg-secondary">0 (Sin stock)</span>'
            : producto.stock <= stockMin
              ? `<span class="badge bg-warning text-dark">${producto.stock} (Bajo)</span>`
              : `<span class="badge bg-success">${producto.stock}</span>`;

          const nombreEscapado = (producto.nombre || "").replace(/'/g, "\\'");

          html += `
            <tr>
              <td><strong>#${producto.id}</strong></td>
              <td>${producto.nombre}</td>
              <td><span class="badge bg-light text-dark border">${producto.categoria}</span></td>
              <td><strong>${formatearMoneda(producto.precio)}</strong></td>
              <td>${badgeStock}</td>
              <td>${stockMin}</td>
              <td>
                <div class="btn-group btn-group-sm">
                  <button
                    class="btn btn-outline-primary"
                    onclick="abrirModalEditarProducto(${producto.id})"
                  >
                    Editar
                  </button>
                  <button
                    class="btn btn-outline-danger"
                    onclick="abrirModalEliminarProducto(${producto.id}, '${nombreEscapado}')"
                  >
                    Eliminar
                  </button>
                </div>
              </td>
            </tr>
          `;
        });
      }

      html += `
                </tbody>
              </table>
            </div>
          </div>
        </div>
      `;

      adminContenido.innerHTML = html;

      const btnNuevo = document.getElementById("btn-nuevo-producto");
      if (btnNuevo) {
        btnNuevo.addEventListener("click", abrirModalNuevoProducto);
      }

    } catch (error) {
      mostrarError(error.message);
    }
  }

  // Funciones de Apertura de Modales de Producto
  window.abrirModalNuevoProducto = async function () {
    const form = document.getElementById("form-producto");
    if (!form) return;
    form.reset();

    document.getElementById("prod-id").value = "";
    document.getElementById("productoModalLabel").textContent = "Nuevo Producto";
    document.getElementById("prod-stock-minimo").value = "5";

    const alerta = document.getElementById("modal-producto-alerta");
    if (alerta) alerta.classList.add("d-none");

    // Poblamos el selector de categorías desde /api/categorias
    await poblarSelectCategorias();

    if (productoModal) productoModal.show();
  };

  window.abrirModalEditarProducto = async function (id) {
    try {
      const response = await fetch(`${API_URL}/productos/${id}`);
      if (!response.ok) throw new Error("No se pudo cargar el producto para editar");

      const prod = await response.json();

      document.getElementById("prod-id").value = prod.id;
      document.getElementById("prod-nombre").value = prod.nombre || "";
      document.getElementById("prod-descripcion").value = prod.descripcion || "";
      document.getElementById("prod-precio").value = prod.precio != null ? prod.precio : "";
      document.getElementById("prod-imagen").value = prod.imagenUrl || "";
      document.getElementById("prod-stock").value = prod.stock != null ? prod.stock : 0;
      document.getElementById("prod-stock-minimo").value = prod.stockMinimo != null ? prod.stockMinimo : 5;

      document.getElementById("productoModalLabel").textContent = `Editar Producto #${prod.id}`;

      const alerta = document.getElementById("modal-producto-alerta");
      if (alerta) alerta.classList.add("d-none");

      // Poblamos el selector seleccionando la categoría actual
      await poblarSelectCategorias(prod.categoria);

      if (productoModal) productoModal.show();

    } catch (err) {
      alert(err.message);
    }
  };

  // Submit de Formulario Guardar Producto (Crear / Editar)
  const formProducto = document.getElementById("form-producto");
  if (formProducto) {
    formProducto.addEventListener("submit", async (e) => {
      e.preventDefault();

      const id = document.getElementById("prod-id").value;
      const nombre = document.getElementById("prod-nombre").value.trim();
      const categoria = document.getElementById("prod-categoria").value.trim();
      const descripcion = document.getElementById("prod-descripcion").value.trim();
      const precio = parseFloat(document.getElementById("prod-precio").value);
      const imagenUrl = document.getElementById("prod-imagen").value.trim();
      const stock = parseInt(document.getElementById("prod-stock").value, 10);
      const stockMinimo = parseInt(document.getElementById("prod-stock-minimo").value, 10);

      const alerta = document.getElementById("modal-producto-alerta");

      if (!nombre || !categoria) {
        alerta.textContent = "El nombre y la categoría son obligatorios.";
        alerta.classList.remove("d-none");
        return;
      }

      if (isNaN(precio) || precio <= 0) {
        alerta.textContent = "El precio debe ser un número mayor a 0.";
        alerta.classList.remove("d-none");
        return;
      }

      if (isNaN(stock) || stock < 0) {
        alerta.textContent = "El stock debe ser mayor o igual a 0.";
        alerta.classList.remove("d-none");
        return;
      }

      if (isNaN(stockMinimo) || stockMinimo < 0) {
        alerta.textContent = "El stock mínimo debe ser mayor o igual a 0.";
        alerta.classList.remove("d-none");
        return;
      }

      alerta.classList.add("d-none");

      const productoPayload = {
        nombre,
        categoria,
        descripcion,
        precio,
        imagenUrl,
        stock,
        stockMinimo
      };

      try {
        const url = id ? `${API_URL}/productos/${id}` : `${API_URL}/productos`;
        const method = id ? "PUT" : "POST";

        const response = await fetch(url, {
          method,
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify(productoPayload)
        });

        if (!response.ok) {
          let errorMsg = "Error al guardar el producto";
          try {
            const errorJson = await response.json();
            if (errorJson.mensaje) errorMsg = errorJson.mensaje;
          } catch (parseErr) {
            console.error(parseErr);
          }
          throw new Error(errorMsg);
        }

        if (productoModal) productoModal.hide();

        alert(id ? "Producto modificado con éxito." : "Producto creado con éxito.");

        renderizarAdminProductos();
        cargarProductosDesdeAPI();

      } catch (err) {
        alerta.textContent = err.message;
        alerta.classList.remove("d-none");
      }
    });
  }

  // Eliminar Producto
  window.abrirModalEliminarProducto = function (id, nombre) {
    idProductoAEliminar = id;
    const nombreEl = document.getElementById("eliminar-producto-nombre");
    if (nombreEl) nombreEl.textContent = `ID #${id}: ${nombre}`;
    if (confirmarEliminarModal) confirmarEliminarModal.show();
  };

  const btnConfirmarEliminar = document.getElementById("btn-confirmar-eliminar");
  if (btnConfirmarEliminar) {
    btnConfirmarEliminar.addEventListener("click", async () => {
      if (!idProductoAEliminar) return;

      try {
        const response = await fetch(`${API_URL}/productos/${idProductoAEliminar}`, {
          method: "DELETE"
        });

        if (!response.ok) {
          let errorMsg = "No se pudo eliminar el producto.";
          try {
            const errorJson = await response.json();
            if (errorJson.mensaje) errorMsg = errorJson.mensaje;
          } catch (e) {
            console.error(e);
          }
          throw new Error(errorMsg);
        }

        if (confirmarEliminarModal) confirmarEliminarModal.hide();
        idProductoAEliminar = null;

        alert("Producto eliminado correctamente.");

        renderizarAdminProductos();
        cargarProductosDesdeAPI();

      } catch (err) {
        if (confirmarEliminarModal) confirmarEliminarModal.hide();
        alert(err.message);
      }
    });
  }

  // -----------------------------------------------------
  // ADMIN - CATEGORÍAS (CRUD COMPLETO)
  // -----------------------------------------------------

  const btnAdminCategorias = document.getElementById("admin-categorias");
  if (btnAdminCategorias) {
    btnAdminCategorias.addEventListener("click", renderizarAdminCategorias);
  }

  async function renderizarAdminCategorias() {
    mostrarCargando();

    try {
      const response = await fetch(`${API_URL}/categorias`);

      if (!response.ok) {
        throw new Error("No se pudieron obtener las categorías");
      }

      const categorias = await response.json();
      categoriasGlobales = categorias;

      let html = `
        <div class="card shadow-sm">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
              <h3 class="mb-0">Gestión de Categorías</h3>
              <button class="btn btn-info text-white" id="btn-nueva-categoria">
                + Nueva categoría
              </button>
            </div>

            <div class="table-responsive">
              <table class="table table-striped table-bordered align-middle">
                <thead class="table-dark">
                  <tr>
                    <th style="width: 120px;">ID</th>
                    <th>Nombre</th>
                    <th style="width: 200px;">Acciones</th>
                  </tr>
                </thead>
                <tbody>
      `;

      if (categorias.length === 0) {
        html += `
          <tr>
            <td colspan="3" class="text-center text-muted py-4">No hay categorías registradas.</td>
          </tr>
        `;
      } else {
        categorias.forEach((cat) => {
          const nombreEscapado = (cat.nombre || "").replace(/'/g, "\\'");

          html += `
            <tr>
              <td><strong>#${cat.id}</strong></td>
              <td><span class="badge bg-light text-dark border fs-6">${cat.nombre}</span></td>
              <td>
                <div class="btn-group btn-group-sm">
                  <button
                    class="btn btn-outline-primary"
                    onclick="abrirModalEditarCategoria(${cat.id})"
                  >
                    Editar
                  </button>
                  <button
                    class="btn btn-outline-danger"
                    onclick="abrirModalEliminarCategoria(${cat.id}, '${nombreEscapado}')"
                  >
                    Eliminar
                  </button>
                </div>
              </td>
            </tr>
          `;
        });
      }

      html += `
                </tbody>
              </table>
            </div>
          </div>
        </div>
      `;

      adminContenido.innerHTML = html;

      const btnNueva = document.getElementById("btn-nueva-categoria");
      if (btnNueva) {
        btnNueva.addEventListener("click", abrirModalNuevoCategoria);
      }

    } catch (error) {
      mostrarError(error.message);
    }
  }

  window.abrirModalNuevoCategoria = function () {
    const form = document.getElementById("form-categoria");
    if (!form) return;
    form.reset();

    document.getElementById("cat-id").value = "";
    document.getElementById("categoriaModalLabel").textContent = "Nueva Categoría";

    const alerta = document.getElementById("modal-categoria-alerta");
    if (alerta) alerta.classList.add("d-none");

    if (categoriaModal) categoriaModal.show();
  };

  window.abrirModalEditarCategoria = async function (id) {
    try {
      const response = await fetch(`${API_URL}/categorias/${id}`);
      if (!response.ok) throw new Error("No se pudo cargar la categoría para editar");

      const cat = await response.json();

      document.getElementById("cat-id").value = cat.id;
      document.getElementById("cat-nombre").value = cat.nombre || "";
      document.getElementById("categoriaModalLabel").textContent = `Editar Categoría #${cat.id}`;

      const alerta = document.getElementById("modal-categoria-alerta");
      if (alerta) alerta.classList.add("d-none");

      if (categoriaModal) categoriaModal.show();

    } catch (err) {
      alert(err.message);
    }
  };

  // Submit de Formulario Guardar Categoría (Crear / Editar)
  const formCategoria = document.getElementById("form-categoria");
  if (formCategoria) {
    formCategoria.addEventListener("submit", async (e) => {
      e.preventDefault();

      const id = document.getElementById("cat-id").value;
      const nombre = document.getElementById("cat-nombre").value.trim();
      const alerta = document.getElementById("modal-categoria-alerta");

      if (!nombre) {
        alerta.textContent = "El nombre de la categoría es obligatorio.";
        alerta.classList.remove("d-none");
        return;
      }

      alerta.classList.add("d-none");

      try {
        const url = id ? `${API_URL}/categorias/${id}` : `${API_URL}/categorias`;
        const method = id ? "PUT" : "POST";

        const response = await fetch(url, {
          method,
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify({ nombre })
        });

        if (!response.ok) {
          let errorMsg = "Error al guardar la categoría";
          try {
            const errorJson = await response.json();
            if (errorJson.mensaje) errorMsg = errorJson.mensaje;
          } catch (e) {
            console.error(e);
          }
          throw new Error(errorMsg);
        }

        if (categoriaModal) categoriaModal.hide();

        alert(id ? "Categoría modificada con éxito." : "Categoría creada con éxito.");

        renderizarAdminCategorias();
        cargarCategoriasDesdeAPI();
        cargarProductosDesdeAPI();

      } catch (err) {
        alerta.textContent = err.message;
        alerta.classList.remove("d-none");
      }
    });
  }

  // Eliminar Categoría
  window.abrirModalEliminarCategoria = function (id, nombre) {
    idCategoriaAEliminar = id;
    const nombreEl = document.getElementById("eliminar-categoria-nombre");
    if (nombreEl) nombreEl.textContent = `ID #${id}: ${nombre}`;
    if (confirmarEliminarCategoriaModal) confirmarEliminarCategoriaModal.show();
  };

  const btnConfirmarEliminarCategoria = document.getElementById("btn-confirmar-eliminar-categoria");
  if (btnConfirmarEliminarCategoria) {
    btnConfirmarEliminarCategoria.addEventListener("click", async () => {
      if (!idCategoriaAEliminar) return;

      try {
        const response = await fetch(`${API_URL}/categorias/${idCategoriaAEliminar}`, {
          method: "DELETE"
        });

        if (!response.ok) {
          let errorMsg = "No se pudo eliminar la categoría.";
          try {
            const errorJson = await response.json();
            if (errorJson.mensaje) errorMsg = errorJson.mensaje;
          } catch (e) {
            console.error(e);
          }
          throw new Error(errorMsg);
        }

        if (confirmarEliminarCategoriaModal) confirmarEliminarCategoriaModal.hide();
        idCategoriaAEliminar = null;

        alert("Categoría eliminada correctamente.");

        renderizarAdminCategorias();
        cargarCategoriasDesdeAPI();

      } catch (err) {
        if (confirmarEliminarCategoriaModal) confirmarEliminarCategoriaModal.hide();
        alert(err.message);
      }
    });
  }

  // -----------------------------------------------------
  // ADMIN - STOCK
  // -----------------------------------------------------

  const btnAdminStock = document.getElementById("admin-stock");
  if (btnAdminStock) {
    btnAdminStock.addEventListener("click", async () => {
      mostrarCargando();

      try {
        const response = await fetch(`${API_URL}/productos`);

        if (!response.ok) {
          throw new Error("No se pudo consultar el stock");
        }

        const productos = await response.json();

        let html = `
          <div class="card shadow-sm">
            <div class="card-body">
              <h3 class="mb-4">Control de Stock</h3>

              <div class="table-responsive">
                <table class="table table-bordered align-middle">
                  <thead class="table-dark">
                    <tr>
                      <th>Producto</th>
                      <th>Stock actual</th>
                      <th>Stock mínimo</th>
                      <th>Estado</th>
                    </tr>
                  </thead>
                  <tbody>
        `;

        productos.forEach((producto) => {
          const stockMinimo = producto.stockMinimo != null ? producto.stockMinimo : 5;
          const stockActual = producto.stock != null ? producto.stock : 0;

          let badgeEstado = "";
          if (stockActual === 0) {
            badgeEstado = '<span class="badge bg-secondary">Sin stock</span>';
          } else if (stockActual <= stockMinimo) {
            badgeEstado = '<span class="badge bg-warning text-dark">Stock bajo</span>';
          } else {
            badgeEstado = '<span class="badge bg-success">Disponible</span>';
          }

          html += `
            <tr>
              <td><strong>${producto.nombre}</strong></td>
              <td>${stockActual}</td>
              <td>${stockMinimo}</td>
              <td>${badgeEstado}</td>
            </tr>
          `;
        });

        html += `
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        `;

        adminContenido.innerHTML = html;

      } catch (error) {
        mostrarError(error.message);
      }
    });
  }

  // -----------------------------------------------------
  // ADMIN - PEDIDOS (FLUJO COMPLETO DE ESTADOS)
  // -----------------------------------------------------

  const btnAdminPedidos = document.getElementById("admin-pedidos");
  if (btnAdminPedidos) {
    btnAdminPedidos.addEventListener("click", renderizarAdminPedidos);
  }

  async function renderizarAdminPedidos() {
    mostrarCargando();

    try {
      const response = await fetch(`${API_URL}/pedidos`);

      if (!response.ok) {
        throw new Error("No se pudieron obtener los pedidos");
      }

      const pedidos = await response.json();

      let html = `
        <div class="card shadow-sm">
          <div class="card-body">
            <h3 class="mb-4">Pedidos</h3>

            <div class="table-responsive">
              <table class="table table-striped table-bordered align-middle">
                <thead class="table-dark">
                  <tr>
                    <th>ID</th>
                    <th>Fecha</th>
                    <th>Usuario</th>
                    <th>Total</th>
                    <th>Estado</th>
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
      `;

      if (pedidos.length === 0) {
        html += `
          <tr>
            <td colspan="6" class="text-center text-muted py-4">No se registran pedidos aún.</td>
          </tr>
        `;
      } else {
        pedidos.forEach((pedido) => {
          const usuario = pedido.usuario ? pedido.usuario.nombre : "Sin usuario";
          const fecha = pedido.fecha ? new Date(pedido.fecha).toLocaleString("es-AR") : "-";

          let badgeEstado = "";
          switch (pedido.estado) {
            case "PENDIENTE":
              badgeEstado = '<span class="badge bg-warning text-dark">PENDIENTE</span>';
              break;
            case "CONFIRMADO":
              badgeEstado = '<span class="badge bg-primary">CONFIRMADO</span>';
              break;
            case "ENVIADO":
              badgeEstado = '<span class="badge bg-info text-dark">ENVIADO</span>';
              break;
            case "ENTREGADO":
              badgeEstado = '<span class="badge bg-success">ENTREGADO</span>';
              break;
            case "CANCELADO":
              badgeEstado = '<span class="badge bg-danger">CANCELADO</span>';
              break;
            default:
              badgeEstado = `<span class="badge bg-secondary">${pedido.estado}</span>`;
          }

          let botonesAccion = "-";
          if (pedido.estado === "PENDIENTE") {
            botonesAccion = `
              <div class="btn-group btn-group-sm">
                <button
                  class="btn btn-success"
                  onclick="cambiarEstadoPedido(${pedido.id}, 'CONFIRMADO')"
                >
                  Confirmar
                </button>
                <button
                  class="btn btn-outline-danger"
                  onclick="cambiarEstadoPedido(${pedido.id}, 'CANCELADO')"
                >
                  Cancelar
                </button>
              </div>
            `;
          } else if (pedido.estado === "CONFIRMADO") {
            botonesAccion = `
              <div class="btn-group btn-group-sm">
                <button
                  class="btn btn-primary"
                  onclick="cambiarEstadoPedido(${pedido.id}, 'ENVIADO')"
                >
                  Enviar
                </button>
                <button
                  class="btn btn-outline-danger"
                  onclick="cambiarEstadoPedido(${pedido.id}, 'CANCELADO')"
                >
                  Cancelar
                </button>
              </div>
            `;
          } else if (pedido.estado === "ENVIADO") {
            botonesAccion = `
              <button
                class="btn btn-sm btn-dark"
                onclick="cambiarEstadoPedido(${pedido.id}, 'ENTREGADO')"
              >
                Entregar
              </button>
            `;
          } else if (pedido.estado === "ENTREGADO") {
            botonesAccion = '<span class="text-success small fw-bold">✓ Completado</span>';
          } else if (pedido.estado === "CANCELADO") {
            botonesAccion = '<span class="text-danger small fw-bold">✕ Cancelado</span>';
          }

          html += `
            <tr>
              <td><strong>#${pedido.id}</strong></td>
              <td>${fecha}</td>
              <td>${usuario}</td>
              <td><strong>${formatearMoneda(pedido.total)}</strong></td>
              <td>${badgeEstado}</td>
              <td>${botonesAccion}</td>
            </tr>
          `;
        });
      }

      html += `
                </tbody>
              </table>
            </div>
          </div>
        </div>
      `;

      adminContenido.innerHTML = html;

    } catch (error) {
      mostrarError(error.message);
    }
  }

  window.cambiarEstadoPedido = async function (id, nuevoEstado) {
    try {
      const response = await fetch(`${API_URL}/pedidos/${id}/estado`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(nuevoEstado)
      });

      if (!response.ok) {
        let mensaje = "No se pudo actualizar el estado del pedido";
        try {
          const errorBackend = await response.json();
          if (errorBackend.mensaje) {
            mensaje = errorBackend.mensaje;
          }
        } catch (error) {
          console.error(error);
        }
        throw new Error(mensaje);
      }

      renderizarAdminPedidos();
      cargarProductosDesdeAPI();

    } catch (error) {
      console.error("Error al actualizar pedido:", error);
      alert(error.message);
    }
  };

  window.confirmarPedido = function (id) {
    return cambiarEstadoPedido(id, "CONFIRMADO");
  };

  // -----------------------------------------------------
  // ADMIN - USUARIOS (CON HISTORIAL)
  // -----------------------------------------------------

  const btnAdminUsuarios = document.getElementById("admin-usuarios");
  if (btnAdminUsuarios) {
    btnAdminUsuarios.addEventListener("click", async () => {
      mostrarCargando();

      try {
        const response = await fetch(`${API_URL}/usuarios`);

        if (!response.ok) {
          throw new Error("No se pudieron obtener los usuarios");
        }

        const usuarios = await response.json();

        let html = `
          <div class="card shadow-sm">
            <div class="card-body">
              <h3 class="mb-4">Usuarios</h3>

              <div class="table-responsive">
                <table class="table table-striped table-bordered align-middle">
                  <thead class="table-dark">
                    <tr>
                      <th>ID</th>
                      <th>Nombre</th>
                      <th>Email</th>
                      <th>Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
        `;

        if (usuarios.length === 0) {
          html += `
            <tr>
              <td colspan="4" class="text-center text-muted py-4">No hay usuarios registrados.</td>
            </tr>
          `;
        } else {
          usuarios.forEach((usuario) => {
            const nombreEscapado = (usuario.nombre || "").replace(/'/g, "\\'");
            const emailEscapado = (usuario.email || "").replace(/'/g, "\\'");

            html += `
              <tr>
                <td><strong>#${usuario.id}</strong></td>
                <td>${usuario.nombre}</td>
                <td>${usuario.email}</td>
                <td>
                  <button
                    class="btn btn-sm btn-outline-primary"
                    onclick="verHistorialUsuario(${usuario.id}, '${nombreEscapado}', '${emailEscapado}')"
                  >
                    Ver historial
                  </button>
                </td>
              </tr>
            `;
          });
        }

        html += `
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        `;

        adminContenido.innerHTML = html;

      } catch (error) {
        mostrarError(error.message);
      }
    });
  }

  // Ver Historial de Pedidos de Usuario
  window.verHistorialUsuario = async function (id, nombre, email) {
    try {
      const infoEl = document.getElementById("historial-usuario-info");
      const countEl = document.getElementById("historial-pedidos-count");
      const contenidoEl = document.getElementById("historial-pedidos-contenido");

      if (infoEl) infoEl.textContent = `Usuario: ${nombre} (${email})`;
      if (contenidoEl) contenidoEl.innerHTML = '<div class="text-center py-4"><div class="spinner-border text-primary" role="status"></div></div>';

      if (historialPedidosModal) historialPedidosModal.show();

      const response = await fetch(`${API_URL}/usuarios/${id}/pedidos`);
      if (!response.ok) throw new Error("No se pudo obtener el historial de pedidos del usuario");

      const pedidos = await response.json();

      if (countEl) countEl.textContent = `${pedidos.length} pedido(s)`;

      if (!pedidos || pedidos.length === 0) {
        contenidoEl.innerHTML = `
          <div class="alert alert-info text-center my-3" role="alert">
            Este usuario no tiene pedidos registrados hasta el momento.
          </div>
        `;
        return;
      }

      let historialHtml = `
        <div class="table-responsive">
          <table class="table table-bordered align-middle">
            <thead class="table-light">
              <tr>
                <th>Pedido</th>
                <th>Fecha</th>
                <th>Estado</th>
                <th>Total</th>
                <th>Detalles de Productos</th>
              </tr>
            </thead>
            <tbody>
      `;

      pedidos.forEach((p) => {
        const fecha = p.fecha ? new Date(p.fecha).toLocaleString("es-AR") : "-";

        let badge = "";
        switch (p.estado) {
          case "PENDIENTE": badge = '<span class="badge bg-warning text-dark">PENDIENTE</span>'; break;
          case "CONFIRMADO": badge = '<span class="badge bg-primary">CONFIRMADO</span>'; break;
          case "ENVIADO": badge = '<span class="badge bg-info text-dark">ENVIADO</span>'; break;
          case "ENTREGADO": badge = '<span class="badge bg-success">ENTREGADO</span>'; break;
          case "CANCELADO": badge = '<span class="badge bg-danger">CANCELADO</span>'; break;
          default: badge = `<span class="badge bg-secondary">${p.estado}</span>`;
        }

        let productosList = '<ul class="list-unstyled mb-0 small">';
        if (p.detalles && p.detalles.length > 0) {
          p.detalles.forEach((d) => {
            const nomProd = d.producto ? d.producto.nombre : "Producto";
            const unit = d.precioUnitario != null ? formatearMoneda(d.precioUnitario) : "-";
            productosList += `<li>• <strong>${d.cantidad}x</strong> ${nomProd} <span class="text-muted">(${unit} c/u)</span></li>`;
          });
        } else {
          productosList += '<li class="text-muted">Sin detalles</li>';
        }
        productosList += '</ul>';

        historialHtml += `
          <tr>
            <td><strong>#${p.id}</strong></td>
            <td><small>${fecha}</small></td>
            <td>${badge}</td>
            <td><strong>${formatearMoneda(p.total)}</strong></td>
            <td>${productosList}</td>
          </tr>
        `;
      });

      historialHtml += `
            </tbody>
          </table>
        </div>
      `;

      contenidoEl.innerHTML = historialHtml;

    } catch (err) {
      alert(err.message);
    }
  };

  // =====================================================
  // FEEDBACK DE CARGA Y ERROR
  // =====================================================

  function mostrarCargando() {
    adminContenido.innerHTML = `
      <div class="text-center p-5">
        <div class="spinner-border" role="status"></div>
        <p class="mt-3">Cargando información...</p>
      </div>
    `;
  }

  function mostrarError(mensaje) {
    adminContenido.innerHTML = `
      <div class="alert alert-danger" role="alert">
        <strong>Error:</strong> ${mensaje}
      </div>
    `;
  }

  // =====================================================
  // INICIALIZACIÓN
  // =====================================================

  updateCartUI();

});
