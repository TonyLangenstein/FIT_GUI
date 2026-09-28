
package formattedfields.verifiers;

import java.awt.Color;
import java.text.ParseException;
import javax.swing.InputVerifier;
import javax.swing.JFormattedTextField;
import javax.swing.text.MaskFormatter;

/**
 *
 * @author Mark Pendergast
 * Copyright Mark Pendergast 
 */
public class MaskVerifier extends InputVerifier{
 private static final Color INVALID_COLOR = Color.red;
 private static final Color VALID_COLOR = Color.black;
 private MaskFormatter mf = null;
/**
 * Constructor
 * 
 * @param mask MaskFormatter mask for parsing
 * @throws java.text.ParseException
 */ 
  public MaskVerifier(String mask) throws ParseException
  {
   mf = new MaskFormatter(mask);
  }
 /**
  * Test to see if the contents of the component match the mask
  * 
  * @param jc the component to test
  * @return return true if valid, false if not
  */
 public boolean verify(javax.swing.JComponent jc)
 {

   try{
      JFormattedTextField ftf = (JFormattedTextField)jc;
      String value = (String)mf.stringToValue(ftf.getText());
      jc.setForeground(VALID_COLOR); 
      return true;
   }
   catch(Exception e)
   {
    jc.setForeground(INVALID_COLOR);
    return false;
   }

 }
}