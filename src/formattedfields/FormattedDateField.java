
package formattedfields;

import java.awt.event.*;
import java.util.Date;
import java.util.GregorianCalendar;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.text.*;
import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;

/**
 *  Provides a class that allows the formatted entry of dates.
 * 
 * @author Mark Pendergast
 * Copyright Mark Pendergast
 */
public class FormattedDateField  extends JFormattedTextField implements ActionListener, Serializable{
    private PropertyChangeSupport propertySupport;
    public static final String PROP_TICKING_PROPERTY = "clock is active";

    public static final String MASKFORMAT = "##/##/#### ##:##:## UM";
    public static final String SIMPLEDATEFORMATMASK = "MM/dd/yyyy HH:mm:ss a";
    private SimpleDateFormat sdf = new SimpleDateFormat(SIMPLEDATEFORMATMASK);

    private static final int TIMERINTERVAL = 1000;
    private Timer timer = new Timer(TIMERINTERVAL,this);
    
    private boolean ticking = true;
   /**
    * Default constructor, uses current date as value
    * @throws java.text.ParseException
    */ 
    public FormattedDateField() throws ParseException
    {
      this(new Date());
    }
    /**
     *  Constructor that uses a GregorianCalendar object as initial value
     * @param cal initial value, GregorianCalendar object
     * @throws java.text.ParseException
     */
    public FormattedDateField(GregorianCalendar cal)  throws ParseException
    {
      this(cal.getTime());
    }
    /**
     * Constructor that accepts year/month/day for the initial value
     * @param year inital year
     * @param month inital month
     * @param day initial day
     * @throws java.text.ParseException
     */
    public FormattedDateField(int year, int month, int day)  throws ParseException
    {
       this(new GregorianCalendar(year, month, day).getTime());
    }
   /**
    * Constructor that accepts year/month/day/hour/min/sec as initial value
    * 
    * @param year inital year
    * @param month inital month (0-11)
    * @param day initial day (0-30)
    * @param hour inital hour (0-23)
    * @param min inital min (0-59)
    * @param sec inital second (0-59)
    * @throws java.text.ParseException
    */
   public FormattedDateField(int year, int month, int day, int hour, int min, int sec) throws ParseException
   {
      this(new GregorianCalendar(year, month, day, hour, min, sec).getTime());
       
   }    
   /**
    * Constructor that accepts initial value as a Date object. Sets up the formatters
    * 
    * @param date
    * @throws java.text.ParseException
    */
    public FormattedDateField(Date date) throws ParseException
    {
      
      setValue(sdf.format(date)); 
      propertySupport = new PropertyChangeSupport(this);
      setInputVerifier(new formattedfields.verifiers.DateVerifier(SIMPLEDATEFORMATMASK));
  
      MaskFormatter mf = new MaskFormatter(MASKFORMAT);
      mf.setValidCharacters("0123456789AP");
      mf.setPlaceholderCharacter('_');     
      mf.setValueClass(String.class);
      DefaultFormatterFactory dff = new DefaultFormatterFactory(mf);
      setFormatterFactory(dff);
      if(ticking)
          timer.start();
     }
    /**
     * retrieve the current value as a Date object
     * @return Current value as a Date object
     * @throws java.text.ParseException
     */
    public Date getDate() throws ParseException
    {
      return sdf.parse((String)getValue());
    }
    /**
     * Set the current value
     * @param date new current value
     */
    public void setDate(Date date)
    {
      setValue(sdf.format(date));
    }
    /**
     * get the current value as a GregorianCalendar object
     * @return current value
     * @throws java.text.ParseException
     */
    public GregorianCalendar getCalendar() throws ParseException
    {
     GregorianCalendar cal = new GregorianCalendar();   
     cal.setTime(getDate());
     return cal;
    }
    /**
     *  returns the value of the ticking field
     * @return true if the field is active, false if not
     */
    public boolean isTicking()
    {
     return ticking;
    }
    /**
     * turns the clock on/off, when on the field updates every second
     * @param value true for on, false for off
     */
    public void setTicking(boolean value)
    {
      boolean old = ticking;
      ticking = value;
      if(ticking)
          timer.start();
      else
          timer.stop();      
     propertySupport.firePropertyChange(PROP_TICKING_PROPERTY, old, value);
    }
    /**
     *  process the timer event, if the timer is on, update the current value by the time interval
     * @param evt
     */
    public void actionPerformed(ActionEvent evt)
    {
      if(evt.getSource() == timer)
      {
        if(isFocusOwner())  // don't mess with the time if editing
          return;
        
        try{
         Date value = getDate();         
         value.setTime(value.getTime()+TIMERINTERVAL);
         setDate(value);
        }
        catch(Exception e)
        {           
        }        
      }
    }
    
}
