package servicios.motor.servicios.repositorio.recargacelularuruguay;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import servicios.dto.business.moneda.Importe;
import servicios.dto.business.moneda.Moneda;
import servicios.motor.modelo.CasoUso;
import servicios.motor.modelo.DefinicionLote;
import servicios.motor.modelo.Estado;
import servicios.motor.modelo.Servicio;
import servicios.motor.modelo.TipoCasoUso;
import servicios.motor.modelo.Transicion;
import servicios.motor.modelo.accion.EsperarTicketCerrado;
import servicios.motor.modelo.accion.Imprimir;
import servicios.motor.modelo.accion.ItemResumen;
import servicios.motor.modelo.accion.MostrarResumen;
import servicios.motor.modelo.accion.SeleccionarValor;
import servicios.motor.modelo.accion.SolicitarDinero;
import servicios.motor.modelo.accion.SolicitarString;
import servicios.motor.modelo.accion.item.AgregarItemServicioTributa;
import servicios.motor.modelo.accion.item.ConfirmarItem;
import servicios.motor.modelo.accion.mensaje.EnviarMensajeExterno;
import servicios.motor.modelo.accion.mensaje.EnviarMensajeExterno.EstrategiaErrorComunicacion;
import servicios.motor.modelo.accion.operacion.IniciarTicketFiscal;
import servicios.motor.modelo.accion.pinpad.LimpiarMensajePrompt;
import servicios.motor.modelo.accion.pinpad.MostrarMensajePrompt;
import servicios.motor.modelo.expresion.ExpresionBeanShell;
import servicios.motor.modelo.expresion.ExpresionCampoMapContexto;
import servicios.motor.modelo.expresion.ExpresionLiteral;
import servicios.motor.modelo.expresion.ExpresionTokenReferencia;
import servicios.motor.modelo.expresion.ExpresionVariableContexto;
import servicios.motor.modelo.expresion.Restricciones;
import servicios.motor.servicios.repositorio.ServicioFactory;

public class ModeloRecargaCelularesUruguayFactory implements ServicioFactory {

	private List<Map<String, Object>> crearCompanias() {
		List<Map<String, Object>> valores = new LinkedList<Map<String, Object>>();

		Map<String, Object> claro = new LinkedHashMap<String, Object>();
		claro.put("id", "claro");
		claro.put("etiqueta", "Claro");
		claro.put("codigoArticulo", "REC CEL CLARO  ");
		claro.put("estrategiaReserva", EstrategiaErrorComunicacion.REVERSA);
		claro.put("estrategiaRecarga", EstrategiaErrorComunicacion.OFFLINE);
		claro.put("recargaLoteable", false);
		valores.add(claro);

		Map<String, Object> movistar = new LinkedHashMap<String, Object>();
		movistar.put("id", "movistar");
		movistar.put("etiqueta", "Movistar");
		movistar.put("codigoArticulo", "REC CEL MOVISTA");
		movistar.put("estrategiaReserva", EstrategiaErrorComunicacion.NINGUNA);
		movistar.put("estrategiaRecarga", EstrategiaErrorComunicacion.REVERSA);
		movistar.put("recargaLoteable", true);
		valores.add(movistar);

		return valores;
	}

	private Estado getEstadoServicio() {
		SeleccionarValor<Map<String, Object>> solicitarServicio = new SeleccionarValor<Map<String, Object>>();
		solicitarServicio.setDefinicionVariableContexto("servicio");
		solicitarServicio.setMensaje("Servicio");

		solicitarServicio.setValores(new ExpresionLiteral<Object>(
				crearCompanias()));
		solicitarServicio.setSelectorEtiqueta(new ExpresionBeanShell<String>(
				"v{\"etiqueta\"}"));
		return new Estado("seleccionServicio", solicitarServicio);
	}

	private Estado getEstadoNumero() {
		SolicitarString solicitarNumero = new SolicitarString();
		solicitarNumero.setDefinicionVariableContexto("numero");
		solicitarNumero.setMensaje("Numero");
		Restricciones.add(solicitarNumero, "v.matches(\"\\\\d{9}\")",
				"El número debe contener 9 dígitos");
		Restricciones.add(solicitarNumero, "v.startsWith(\"09\")",
				"El número debe comenzar con 09");
		return new Estado("ingresoNumero", solicitarNumero);
	}

	private Estado getEstadoImporteClaro() {
		SolicitarDinero solicitarImporte = new SolicitarDinero();
		solicitarImporte.setDefinicionVariableContexto("importe");
		solicitarImporte.setMensaje("Importe");
		Restricciones
				.add(solicitarImporte,
						"v.valor.compareTo(java.math.BigDecimal.valueOf(21)) >= 0"
								+ " && v.valor.compareTo(java.math.BigDecimal.valueOf(50)) <= 0"
								+ " || v.valor.compareTo(java.math.BigDecimal.valueOf(100)) == 0"
								+ " || v.valor.compareTo(java.math.BigDecimal.valueOf(150)) == 0"
								+ " || v.valor.compareTo(java.math.BigDecimal.valueOf(200)) == 0"
								+ " || v.valor.compareTo(java.math.BigDecimal.valueOf(300)) == 0"
								+ " || v.valor.compareTo(java.math.BigDecimal.valueOf(500)) == 0",
						"El importe debe estar entre 21 y 50 (inclusive) o ser 100, 150, 200, 300 o 500 para Claro.");
		return new Estado("ingresoImporte", solicitarImporte);
	}

	private Estado getEstadoImporteMovistar() {
		SolicitarDinero solicitarImporte = new SolicitarDinero();
		solicitarImporte.setDefinicionVariableContexto("importe");
		solicitarImporte.setMensaje("Importe");
		Restricciones
				.add(solicitarImporte,
						"v.valor.compareTo(java.math.BigDecimal.valueOf(20)) >= 0"
								+ " && v.valor.compareTo(java.math.BigDecimal.valueOf(1000)) <= 0",
						"El importe debe ser mayor o igual a 20 y menor o igual a 1000 para Movistar.");
		return new Estado("ingresoImporte", solicitarImporte);
	}

	private Estado getEstadoMostrarNumeroEnPinpad() {
		MostrarMensajePrompt mostrarMensajePrompt = new MostrarMensajePrompt(
				new ExpresionVariableContexto<String>("numero"));
		return new Estado("mostrarNumeroEnPinpad", mostrarMensajePrompt);
	}

	private Estado getEstadoResumen() {
		List<ItemResumen> itemsResumen = new ArrayList<ItemResumen>(3);
		itemsResumen.add(new ItemResumen(new ExpresionLiteral<String>(
				"Servicio"), new ExpresionCampoMapContexto<String>("servicio",
				"etiqueta")));
		itemsResumen.add(new ItemResumen("Número", "numero"));
		itemsResumen.add(new ItemResumen("Importe", "importe"));
		MostrarResumen mostrarResumen = new MostrarResumen();
		mostrarResumen.setLineas(new ExpresionLiteral<List<ItemResumen>>(
				itemsResumen));
		return new Estado("resumen", mostrarResumen);
	}

	private Estado getEstadoLimpiarNumeroEnPinpad() {
		LimpiarMensajePrompt limpiarMensajePrompt = new LimpiarMensajePrompt();
		return new Estado("limpiarNumeroPinpad", limpiarMensajePrompt);
	}

	private Estado getEstadoIniciarOperacion() {
		IniciarTicketFiscal initTicketFiscal = new IniciarTicketFiscal();
		initTicketFiscal.setMoneda(new ExpresionBeanShell<Moneda>(
				"c.v(\"importe\"){\"tipoMoneda\"}"));
		return new Estado("iniciarOperacion", initTicketFiscal);
	}
	
	private Estado getEstadoReserva() {
		EnviarMensajeExterno enviarMensajeExternoReserva = new EnviarMensajeExterno();
		enviarMensajeExternoReserva
				.setDefinicionVariableContextoResultado("respuestaReserva");
		enviarMensajeExternoReserva.setOperacion("reserva");
		enviarMensajeExternoReserva
				.setEstrategiaErrorComunicacion(new ExpresionBeanShell<EstrategiaErrorComunicacion>(
						"c.v(\"servicio\"){\"estrategiaReserva\"}"));

		enviarMensajeExternoReserva.addCampo("servicio",
				new ExpresionBeanShell<String>("c.v(\"servicio\"){\"id\"}"));
		enviarMensajeExternoReserva
				.addCampo(new ExpresionVariableContexto<String>("numero"));
		enviarMensajeExternoReserva
				.addCampo(new ExpresionVariableContexto<String>("importe"));

		return new Estado("enviarMensajeExternoReserva",
				enviarMensajeExternoReserva);
	}

	private Estado getEstadoAgregarItem() {
		AgregarItemServicioTributa agregarItem = new AgregarItemServicioTributa();
		agregarItem
				.setImporte(new ExpresionVariableContexto<Importe>("importe"));
		agregarItem.setCodigoArticulo(new ExpresionBeanShell<String>(
				"c.v(\"servicio\"){\"codigoArticulo\"}"));
		// agregarItem.setDescripcion(new ExpresionBeanShell<String>(
		// "\"Recarga \" + c.v(\"servicio\"){\"etiqueta\"}"));
		return new Estado("agregarItem", agregarItem);
	}

	private Estado getEstadoTicketCerrado() {
		return new Estado("esperarCierreTicket", new EsperarTicketCerrado());
	}

	private Estado getEstadoRecarga() {
		EnviarMensajeExterno enviarMensajeExternoRecarga = new EnviarMensajeExterno();
		enviarMensajeExternoRecarga
				.setDefinicionVariableContextoResultado("respuestaRecarga");
		enviarMensajeExternoRecarga.setOperacion("recarga");
		enviarMensajeExternoRecarga
				.setEstrategiaErrorComunicacion(new ExpresionBeanShell<EstrategiaErrorComunicacion>(
						"c.v(\"servicio\"){\"estrategiaRecarga\"}"));
		enviarMensajeExternoRecarga
				.setLoteable(new ExpresionBeanShell<Boolean>(
						"c.v(\"servicio\"){\"recargaLoteable\"}"));
		enviarMensajeExternoRecarga.addCampo("servicio",
				new ExpresionBeanShell<String>("c.v(\"servicio\"){\"id\"}"));
		enviarMensajeExternoRecarga
				.addCampo(new ExpresionVariableContexto<String>("numero"));
		enviarMensajeExternoRecarga
				.addCampo(new ExpresionVariableContexto<String>("importe"));
		enviarMensajeExternoRecarga.addCampo("numeroOperacion",
				new ExpresionBeanShell<Long>(
						"c.v(\"respuestaReserva\"){\"numeroOperacion\"}"));

		enviarMensajeExternoRecarga.addCampo("numeroTicket",

				new ExpresionBeanShell<Integer>("c.v(\"operacionVO\").numero"));
		return new Estado("confirmarRecarga", enviarMensajeExternoRecarga);
	}

	private Estado getEstadoImprimir() {
		Imprimir imprimir = new Imprimir("/servicios/motor/servicios/repositorio/recargacelularuruguay/template/recargaCelular.vm");
		imprimir.addParametro("numeroOperacionReservaHexa", new ExpresionBeanShell<String>("((c.v(\"respuestaReserva\"){\"numeroOperacion\"})!=null)? Integer.toHexString((c.v(\"respuestaReserva\"){\"numeroOperacion\"}).intValue()) : null"));
		imprimir.addParametro("numeroOperacionRecargaHexa", new ExpresionBeanShell<String>("((c.v(\"respuestaRecarga\"){\"numeroOperacion\"})!=null)? Integer.toHexString((c.v(\"respuestaRecarga\"){\"numeroOperacion\"}).intValue()) : null"));
		return new Estado("imprimirComprobante", imprimir);
	}

	private Estado getEstadoConfirmarItem() {
		ExpresionTokenReferencia expresionTokenReferencia = new ExpresionTokenReferencia();
		expresionTokenReferencia.add(ExpresionTokenReferencia.PRODUCTO,
				new ExpresionCampoMapContexto<Object>("servicio", "etiqueta"));
		expresionTokenReferencia.add("TEL",
				new ExpresionVariableContexto<Object>("numero"));		
		expresionTokenReferencia.add(ExpresionTokenReferencia.NUMERO_OPERACION,	new ExpresionBeanShell<Object>(
				"offline = c.v(\"respuestaRecarga\"){\"offline\"};\n"
						+ "return (offline != null && offline)? "
						+ "Integer.toHexString(c.v(\"respuestaReserva\"){\"numeroOperacion\"}.intValue()) : "
						+ "Integer.toHexString(c.v(\"respuestaRecarga\"){\"numeroOperacion\"}.intValue());"));
		expresionTokenReferencia
				.add(ExpresionTokenReferencia.AUTORIZACION,
						new ExpresionBeanShell<Object>(
								"offline = c.v(\"respuestaRecarga\"){\"offline\"};\n"
										+ "return (offline != null && offline)? \"OFF\" : \"ON\""));
		return new Estado("confirmarItem", new ConfirmarItem(
				expresionTokenReferencia));
	}

	private CasoUso getCasoUsoRecarga() {

		// Creación de estados
		Estado estadoServicio = getEstadoServicio();
		Estado estadoNumero = getEstadoNumero();
		Estado estadoImporteMovistar = getEstadoImporteMovistar();
		Estado estadoImporteClaro = getEstadoImporteClaro();
		//Estado estadoImporteAntel = getEstadoImporteAntel();
		Estado estadoMostrarNumeroEnPinpad = getEstadoMostrarNumeroEnPinpad();
		Estado estadoResumen = getEstadoResumen();
		Estado estadoLimpiarNumeroEnPinpad = getEstadoLimpiarNumeroEnPinpad();
		Estado estadoIniciarOperacion = getEstadoIniciarOperacion();
        Estado estadoReserva = getEstadoReserva();
		Estado estadoAgregarItem = getEstadoAgregarItem();
		Estado estadoEsperarTicketCerrado = getEstadoTicketCerrado();
		Estado estadoRecarga = getEstadoRecarga();
		Estado estadoImprimirComprobante = getEstadoImprimir();
		Estado estadoConfirmarItem = getEstadoConfirmarItem();

		// Armado de transiciones
		estadoServicio.add(new Transicion(estadoNumero));

		estadoNumero.add(new Transicion(new ExpresionBeanShell<Boolean>(
				"c.v(\"servicio\"){\"id\"}.equals(\"movistar\")"),
				estadoImporteMovistar));
		estadoNumero.add(new Transicion(new ExpresionBeanShell<Boolean>(
				"c.v(\"servicio\"){\"id\"}.equals(\"claro\")"),
				estadoImporteClaro));

		estadoImporteMovistar.add(new Transicion(estadoMostrarNumeroEnPinpad));
		estadoImporteClaro.add(new Transicion(estadoMostrarNumeroEnPinpad));

		estadoMostrarNumeroEnPinpad.add(new Transicion(estadoResumen));

		estadoResumen.add(new Transicion(estadoLimpiarNumeroEnPinpad));
		estadoLimpiarNumeroEnPinpad.add(new Transicion(estadoIniciarOperacion));

        estadoIniciarOperacion.add(new Transicion(estadoReserva));
		estadoReserva.add(new Transicion(estadoAgregarItem));

		estadoAgregarItem.add(new Transicion(estadoEsperarTicketCerrado));
		estadoEsperarTicketCerrado.add(new Transicion(estadoRecarga));
		estadoRecarga.add(new Transicion(estadoImprimirComprobante));
		estadoImprimirComprobante.add(new Transicion(estadoConfirmarItem));

		CasoUso casoUso = new CasoUso();
		casoUso.setId("recarga");
		casoUso.setNombre("Recarga de celulares");
		casoUso.setVersion("0.0.1");
		casoUso.setTipoCasoUso(TipoCasoUso.ITEM);
		casoUso.setEstadoInicial(estadoServicio);

		return casoUso;
	}

	private DefinicionLote getDefinicionLote() {
		DefinicionLote definicionLote = new DefinicionLote();
		definicionLote.setId("movistar");
		definicionLote.setEtiqueta("Movistar");
		definicionLote.setCondicion(new ExpresionBeanShell<Boolean>(
				"e.datos{\"servicio\"}.equals(\"movistar\")"));
		// Las expresiones trabajan sobre el mensaje serializado por lo que ya
		// no hay tipos compuestos
		// Por lo tanto, el importe se trata como un map
		definicionLote.setImporte(new ExpresionBeanShell<BigDecimal>(
				"e.datos{\"importe\"}{\"valor\"}"));
		return definicionLote;
	}

	@Override
	public Servicio crear() {
		Servicio servicio = new Servicio("recargaCelularesUruguay", 9003);
		servicio.setNombre("Recarga de Celulares");
		servicio.setVersion("0.0.2");
		servicio.setCasosUso(Collections.singletonList(getCasoUsoRecarga()));
		servicio.setDefinicionLotes(Collections
				.singletonList(getDefinicionLote()));
		return servicio;
	}

}
