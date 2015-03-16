package commons.servicios.modelo.test;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import servicios.motor.servicios.repositorio.ute.ModeloUteMidesFactory;
import bsh.BshClassManager;
import bsh.EvalError;
import bsh.Interpreter;
import bsh.NameSpace;
import bsh.UtilEvalError;

public class ExpresionBeanShellDigitoVerificadorUteTest {

	private Interpreter getInterpreter() throws EvalError {
		Interpreter interpreter = new Interpreter();
		BshClassManager bcm = BshClassManager.createClassManager();
		final Logger logger = LoggerFactory.getLogger(Interpreter.class);
		NameSpace nameSpace = new NameSpace(bcm, "global") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 13121L;

			@Override
			public void loadDefaultImports() {
				super.loadDefaultImports();
				importClass("java.math.BigDecimal");
			}

			@Override
			public void clear() {
				super.clear();
				try {
					setVariable("logger", logger, false);
					setVariable("c", "a", false);
				} catch (UtilEvalError e) {
					throw new RuntimeException(e);
				}
			}
		};

		interpreter.setNameSpace(nameSpace);
		interpreter.eval("clear()");
		return interpreter;
	}

	private boolean esValido(String referenciaPago) throws EvalError {
		Interpreter interpreter = getInterpreter();
		interpreter.set("v", referenciaPago);
		boolean valido = (Boolean) interpreter
				.eval(ModeloUteMidesFactory.EXPRESION_BEANSHELL_DIGITO_VERIFICADOR_MAS_DE_10_DIGITOS);
		return valido;
	}

	@Test
	public void cuentaValida() throws EvalError{
		Assert.assertTrue(esValido("6808701000"));
	}
	
	@Test
	public void importeTotalValidoEjemplo() throws EvalError {
		Assert.assertTrue(esValido("00118834225"));
	}

	@Test
	public void importeTotalNoValidoEjemploCambiado() throws EvalError {
		Assert.assertFalse(esValido("01118834225"));
	}

	@Test
	public void importeTotalValidoDocumentacion() throws EvalError {
		Assert.assertTrue(esValido("99996100006"));
	}

	@Test
	public void importeTotalNoValidoDocumentacionCambiado() throws EvalError {
		Assert.assertFalse(esValido("99996100106"));
	}

	@Test
	public void federico1() throws EvalError {
		Assert.assertTrue(esValido("61460110002614601799159"));
	}

	@Test
	public void federico2() throws EvalError {
		Assert.assertTrue(esValido("68087010002680878027914"));
	}

}
