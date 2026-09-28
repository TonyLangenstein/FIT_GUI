
package formattedfields.formatters;

import java.io.Serializable;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.DefaultFormatter;

/**
 * This class provides basic formatter services for email addresses. The stringToValue 
 * method uses a string pattern matcher to verify email format.  The valueToString simply 
 * returns the string.
 * 
 * @author Mark Pendergast
 * Copyright Mark Pendergast
 */
public class EmailFormatter extends DefaultFormatter implements Serializable {
/** pattern used for verifying email addresses */
 private static final String EMAILPATTERN = "^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
 /** precompiled version of the pattern */
 private static Pattern pattern = Pattern.compile(EMAILPATTERN,Pattern.CASE_INSENSITIVE);
/**
 *  Default constructor
 */
 public EmailFormatter() {
     
 }
/**
 * Converts the value object to a string
 * @param value current value of the JFormattedTextField
 * @return string representation
 */
    public String valueToString(Object value ) 
    {
        return value.toString();
    }
 /**
  * Converts the String to an object, verifying its contents. Throws a ParseException
  * if the string is not in valid email format.
  * 
  * @param text from the JFormattedTextField
  * @return 
  * @throws java.text.ParseException
  */
    public Object stringToValue(String text) throws ParseException
    {
       Matcher m = pattern.matcher(text);
       if(!m.matches())
       {
         throw new ParseException(text,0);        
       }
       else
         return text;
    }
}
