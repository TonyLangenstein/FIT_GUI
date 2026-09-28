
package formattedfields;

import formattedfields.formatters.PercentFormatter;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.text.DecimalFormat;
import javax.swing.JFormattedTextField;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

/**
 *
 * @author Mark Pendergast
 * Copyright Mark Pendergast
 */
public class FormattedPercentField extends JFormattedTextField implements Serializable{

    public static final String PROP_MINVALUE_PROPERTY = "minimum value";
    public static final String PROP_MAXVALUE_PROPERTY = "maximum value";
    public static final String PROP_DECIMALPLACES_PROPERTY = "decimal places";
    
    public static final String displayFormat = "#,###,##0.000000000000000";
    public static final String editFormat = "#.###############";
    private formattedfields.verifiers.PercentVerifier verifier;
    
     
    private double minValue = -Double.MAX_VALUE;
    private double maxValue = Double.MAX_VALUE;
    private int decimalPlaces = DEFAULT_DECIMALPLACES;
    private DefaultFormatterFactory dff;

    
    private static final int DEFAULT_DECIMALPLACES = 2;
    private static final int MAX_DECIMALPLACES = 15;
    private static final int POSITIONS_LEFT_OF_DECIMALDISPLAY = displayFormat.indexOf('.');
    private static final int POSITIONS_LEFT_OF_DECIMALEDIT = editFormat.indexOf('.');

    private PropertyChangeSupport propertySupport;

    public FormattedPercentField() {
       this(-Double.MAX_VALUE, Double.MAX_VALUE,0, DEFAULT_DECIMALPLACES);
    }

    public FormattedPercentField(int places) {
       this(-Double.MAX_VALUE, Double.MAX_VALUE,0, places);
    }

     public FormattedPercentField(double min, double max) throws IllegalArgumentException{
        this(min,max,0,DEFAULT_DECIMALPLACES);
    }
     
     public FormattedPercentField(double min, double max, double value, int places) throws IllegalArgumentException{
        setValue(Double.valueOf(value));
        minValue = min;
        maxValue = max;
        if(min > max)
            throw new IllegalArgumentException("min value cannot be greater than max value");
        propertySupport = new PropertyChangeSupport(this);
        verifier = new formattedfields.verifiers.PercentVerifier(min, max);
        setInputVerifier(verifier);
       
        NumberFormatter def = new NumberFormatter();
        def.setValueClass(Double.class);
        PercentFormatter disp = new PercentFormatter(new DecimalFormat(displayFormat.substring(0,POSITIONS_LEFT_OF_DECIMALDISPLAY+places+1)));
        disp.setValueClass(Double.class);
        PercentFormatter ed = new PercentFormatter(new DecimalFormat(editFormat.substring(0,POSITIONS_LEFT_OF_DECIMALEDIT+places+1)));
        ed.setValueClass(Double.class);
        dff = new DefaultFormatterFactory(def,disp,ed);
        setFormatterFactory(dff);  
    }
     
    public double getMinValue() {
        return minValue;
    }
    
    public double getMaxValue() {
        return maxValue;
    }
    
    public int getDecimalPlaces()
    {
      return decimalPlaces;
    }

    public void setMinValue(double value) throws IllegalArgumentException{
        if(value > maxValue)
               throw new IllegalArgumentException("min value cannot be greater than max value");
        double oldValue = minValue;
        minValue  = value;
        propertySupport.firePropertyChange(PROP_MINVALUE_PROPERTY, oldValue, minValue);
        verifier.setMinValue(value);
    }
    
    public void setMaxValue(double value) throws IllegalArgumentException {
        if(value < minValue)
               throw new IllegalArgumentException("max value cannot be less than min value");
        double oldValue = maxValue;
        maxValue  = value;
        propertySupport.firePropertyChange(PROP_MAXVALUE_PROPERTY, oldValue, maxValue);
        verifier.setMaxValue(value);
    }
    
    public void setDecimalPlaces(int value) throws IllegalArgumentException {
        if(value < 0 || value > MAX_DECIMALPLACES)
               throw new IllegalArgumentException("decimal places cannot be negative");
        int oldValue = decimalPlaces;
        decimalPlaces  = value;
        propertySupport.firePropertyChange(PROP_DECIMALPLACES_PROPERTY, oldValue, decimalPlaces);
        dff.setDisplayFormatter(new PercentFormatter((new DecimalFormat(displayFormat.substring(0,POSITIONS_LEFT_OF_DECIMALDISPLAY+value+1)))));
        dff.setEditFormatter(new PercentFormatter((new DecimalFormat(editFormat.substring(0,POSITIONS_LEFT_OF_DECIMALEDIT+value+1)))));
        setFormatterFactory(dff);
        repaint();
    }
}
