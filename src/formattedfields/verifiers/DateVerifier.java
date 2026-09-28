package formattedfields.verifiers;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.InputVerifier;

/**
 *
 * @author Mark Pendergast
 * Copyright Mark Pendergast
 */
public class DateVerifier extends InputVerifier {
  private static final Color INVALID_COLOR = Color.red;
  private static final Color VALID_COLOR = Color.black;
    
  SimpleDateFormat sdf = null; // formatted used to check date formats
  /**
   * Default constructor
   */
 public DateVerifier()
 {
  sdf = new SimpleDateFormat();
 }
 /**
  * Constructor that accepts a mask
  * 
  * @param mask SimpleDateFormat mask
  */
         
 public DateVerifier(String mask)
 {
   sdf = new SimpleDateFormat(mask);
 }
 /**
  * Check the contents to see if its a valid date
  * 
  * @param jc JComponent (the date field)
  * @return true if valid date, false if not
  */
 public boolean verify(javax.swing.JComponent jc)
 {
   formattedfields.FormattedDateField fdf = (formattedfields.FormattedDateField)jc;
   try{
      Date d = sdf.parse(fdf.getText());  // note this allows months > 12, days > 31
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
