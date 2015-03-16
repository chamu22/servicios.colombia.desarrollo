package commons.servicios.modelo.test;

import org.junit.Test;

import bsh.EvalError;
import bsh.Interpreter;

public class ExpresionBeanShellStringFormatTest {

	@Test
	public void test() throws EvalError {
		Interpreter interpreter = new Interpreter();
		String e = (String) interpreter.eval("String.format(\"%02d\",new Object[]{1})");
		
		System.out.print(e);
		
//		Object[ ] o = new Object[]{1};
	}

}
