

package formattedfields;

import javax.swing.JFormattedTextField;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.text.ParseException;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;

/**
 *
 * @author Mark Pendergast
 * Copyright Mark Pendergast 
 */
public class FormattedPhoneField  extends JFormattedTextField implements Serializable{
    private PropertyChangeSupport propertySupport;

    public static final String PHONEFORMAT = "(###) ###-####";
    private DefaultFormatterFactory dff;

   /**
    *  Constructor - creates and sets up formatters for phone numbers
    * @throws java.text.ParseException
    */
    public FormattedPhoneField() throws ParseException
    {
      propertySupport = new PropertyChangeSupport(this);
      MaskFormatter mf = new MaskFormatter(PHONEFORMAT);
      mf.setValueClass(String.class);
      mf.setPlaceholderCharacter('_');  
      setInputVerifier(new formattedfields.verifiers.MaskVerifier(PHONEFORMAT));
      dff = new DefaultFormatterFactory(mf);
      setFormatterFactory(dff);
    }

}
