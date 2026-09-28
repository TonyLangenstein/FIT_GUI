/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PESGUI;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log4J2AsyncLogger {

    public static enum LogLevelOptions
      {
      Full,
      Critical
      }          
    private static LogLevelOptions LogLevel = LogLevelOptions.Full;
    
    private static Logger logger = LogManager.getLogger();
    
    public void performSomeTask(){
               logger.debug("This is a debug message.");
               logger.info("This is an info message.");
               logger.warn("This is a warn message.");
               logger.error("This is an error message.");
               logger.fatal("This is a fatal message.");
     }

    public void SetLogLevel( LogLevelOptions value ) {
        LogLevel = value;
    }
    
    public LogLevelOptions GetLogLevel() {
        return LogLevel;
    }
    
    public void LogThis( Level Level, String Message){
        switch (LogLevel) {
            case Critical:
                if ( Level.equals(Level.ERROR) || Level.equals(Level.FATAL) ) {
                    logger.log( Level, Message );
                }
                    // else do nothing
                break;
            default:
                logger.log( Level, Message );
                break;
        }
     }
}
