
package formattedfields;


import formattedfields.formatters.EmailFormatter;
import javax.swing.JFormattedTextField;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import javax.swing.text.DefaultFormatterFactory;


/**
 *
 * @author Mark Pendergast
 * Copyright Mark Pendergast
 */
public class FormattedEmailField  extends JFormattedTextField implements Serializable{
    
    private PropertyChangeSupport propertySupport;

   /**
    * Constructor - creates and sets up formatters for email address
    */
    public FormattedEmailField() 
    {  
      setValue("");
      propertySupport = new PropertyChangeSupport(this);     
      EmailFormatter ef = new EmailFormatter();  
      setInputVerifier(new formattedfields.verifiers.EmailVerifier());
      DefaultFormatterFactory dff = new DefaultFormatterFactory(ef);
      setFormatterFactory(dff);
    }

}