
package formattedfields.formatters;

import java.text.NumberFormat;
import java.text.ParseException;
import javax.swing.text.NumberFormatter;

/**
 * This class provides basic formatter services for percents. The code assumes
 * the string value to be displayed and edited by the user will be a double number, e.g. 
 * 5.3 for 5.3%, and that the value is to be in percentage format .053 for 5.3%.
 * The stringToValue method uses the super clases method to do the conversion, then divides by 100. 
 * The valueToString simply multiplies the number by 100, then class the supers 
 * valueTOString to do the conversion.
 * 
 *
 * @author Mark Pendergast
 * Copyright Mark Pendergast
 */
public class PercentFormatter extends NumberFormatter{
    
    public PercentFormatter()
    {
        
    }
    public PercentFormatter(NumberFormat format)
    {
     super(format); 
    }
    
 /**
  *  Multiply the value by 100, then convert to a string
  * 
  * @param o - current value of the object
  * @return string representation of the object multiplied by 100
  * @throws java.text.ParseException
  */  
    public String valueToString(Object value)throws ParseException
    {
        Number number = (Number)value;
        double d = number.doubleValue() * 100.0;
        number = Double.valueOf(d);
        return super.valueToString(number) + " %";
    }
    /**
     *  Convert the string to a number, then divide by 100.
     * @param s text representation of the value
     * @return the number divided by 100 
     * @throws java.text.ParseException
     */
  public Object stringToValue(String text) throws ParseException {
        text = text.replaceAll("%", "");  // discard any %
        text = text.trim(); // get rid of leading/trailing spaces
       
        Number number = (Number)super.stringToValue(text);
        double d = number.doubleValue() / 100.0;
        return Double.valueOf(d);
    }
  


}
