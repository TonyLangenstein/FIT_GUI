
package formattedfields.verifiers;

import javax.swing.text.JTextComponent;
/**
 *
 * @author mpenderg
 * Copyright Mark Pendergast
 */
public class PercentVerifier extends DoubleVerifier {
     
        
  /**
   * Creates an Verifier object that makes sure the text can be parsed into a double between MIN_VALUE and MAX_VALUE
   */            
        public PercentVerifier()
        {
            
        }
 /**
   * Creates an Verifier object that makes sure the text can be parsed into a double between min and max
   * @param min lowest valid value
   * @param max highest valid value
   * @throws java.lang.IllegalArgumentException
   */    

        public PercentVerifier(double min, double max) throws IllegalArgumentException
        {
          if(min  > max)
            throw new IllegalArgumentException("min value must be less than max value");
         minValue = min;
         maxValue = max;
        }
       
 /**
  * verifies the value in the component can be parsed to a double between minValue and maxValue
  * @param jc a JTextComponent
  * @return returns false if the value is not valid
  */   
       public boolean verify(javax.swing.JComponent jc)
        {
           
         try{
           String text = ((JTextComponent)jc).getText();
           text = text.replaceAll("%", "");  // discard any %
           text = text.trim(); // get rid of leading/trailing spaces
            
           double val = Double.parseDouble(text);
           if(val < minValue || val > maxValue)
           {
               jc.setForeground(INVALID_COLOR);
               return false;
           }
         }
         catch(Exception e)
         {
           jc.setForeground(INVALID_COLOR);  
           return false;  
         }
         jc.setForeground(VALID_COLOR);
         return true;        
        }
       
  
}
