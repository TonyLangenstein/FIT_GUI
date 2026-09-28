/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package formattedfields;

import java.beans.PropertyChangeSupport;
import java.text.DecimalFormat;
import javax.swing.JFormattedTextField;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

/**
 * Provides a JFormattedTextField that accepts only doubles. Allows for the formats as a currency
 * and min/max values allowed.
 * 
 * @author Mark Pendergast
 * Copyright Mark Pendergast
 */
public class FormattedCurrencyField extends JFormattedTextField{
    
    public static final String PROP_MINVALUE_PROPERTY = "minimum value";
    public static final String PROP_MAXVALUE_PROPERTY = "maximum value";
      
    public static final String displayFormat = "$#,###,##0.00";
    public static final String editFormat = "#.##";
    
     
    private double minValue = -Double.MAX_VALUE;
    private double maxValue = Double.MAX_VALUE;
    private formattedfields.verifiers.DoubleVerifier verifier;
    
    private PropertyChangeSupport propertySupport;
/**
 *  Default constructor
 */
    public FormattedCurrencyField() {
       this(-Double.MAX_VALUE, Double.MAX_VALUE,0);
    }
/**
 *  Constructor that accepts an initial value
 * 
 * @param value initial value
 * @throws java.lang.IllegalArgumentException
 */
 public FormattedCurrencyField(double value) throws IllegalArgumentException{
        this(-Double.MAX_VALUE, Double.MAX_VALUE,value);
    }
     
 /**
  * Constructor that accepts a min and max value
  * @param min min valid value
  * @param max max valid value
  * @throws java.lang.IllegalArgumentException
  */   
   public FormattedCurrencyField(double min, double max) throws IllegalArgumentException{
        this(min,max,0);
    }
 
   /**
    *  Constructor that accepts min, max, and initial values
    * 
    * @param min min valid value
    * @param max max valid value
    * @param value initial value
    * @throws java.lang.IllegalArgumentException
    */  
     public FormattedCurrencyField(double min, double max, double value) throws IllegalArgumentException{
        propertySupport = new PropertyChangeSupport(this);

        setValue(Double.valueOf(value));
        minValue = min;
        maxValue = max;
        if(min > max)
            throw new IllegalArgumentException("min value cannot be greater than max value");
        // create verifier
        verifier = new formattedfields.verifiers.DoubleVerifier(min, max);
        setInputVerifier(verifier);
        // create formatters
        NumberFormatter def = new NumberFormatter();
        def.setValueClass(Double.class);
        NumberFormatter disp = new NumberFormatter((new DecimalFormat(displayFormat)));
        disp.setValueClass(Double.class);
        NumberFormatter ed = new NumberFormatter((new DecimalFormat(editFormat)));
        ed.setValueClass(Double.class);
        // setup formatter factory
        DefaultFormatterFactory dff = new DefaultFormatterFactory(def,disp,ed);
        setFormatterFactory(dff);      
    }
     /**
      *  accessor for minimum valid value
      * 
      * @return min valid value
      */
      public double getMinValue() {
        return minValue;
    }
    /**
     * accessor for maximum valid value
     * 
     * @return maximum valid value
     */
    public double getMaxValue() {
        return maxValue;
    }
    /**
     *  mutator for minimum valid value
     * 
     * @param value minimum valid value
     * @throws java.lang.IllegalArgumentException
     */
    public void setMinValue(double value) throws IllegalArgumentException{
        if(value > maxValue)
               throw new IllegalArgumentException("min value cannot be greater than max value");
        double oldValue = minValue;
        minValue  = value;
        propertySupport.firePropertyChange(PROP_MINVALUE_PROPERTY, oldValue, minValue);
        verifier.setMinValue(value);
    }
    /**
     *  mutator for maximum valid value
     * 
     * @param value maximum valid value
     * @throws java.lang.IllegalArgumentException
     */
    public void setMaxValue(double value) throws IllegalArgumentException {
        if(value < minValue)
               throw new IllegalArgumentException("max value cannot be less than min value");
        double oldValue = maxValue;
        maxValue  = value;
        propertySupport.firePropertyChange(PROP_MAXVALUE_PROPERTY, oldValue, maxValue);
        verifier.setMaxValue(value);
    }
   
  
}

