/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PESGUI;

/**
 *
 * @author thela
 */
public class BlackScholes {
 
public static double getCall(double stock, double strike, double interest, double timehorizon, double volatility) {
    double d1 	= (Math.log(stock / strike) 
            + (interest + (Math.pow(volatility, 2) / 2)) 
            * timehorizon)
            / (volatility * Math.sqrt(timehorizon));

    double d2 	= d1 - (volatility * Math.sqrt(timehorizon));
    return      (stock * Gaussian.cdf(d1))
            -   (strike * Math.exp(-interest * timehorizon)
            *   Gaussian.cdf(d2));
    }   

public static double getPut(double stock, double strike, double interest, double timehorizon, double volatility) {
    double d1 	= (Math.log(stock / strike) 
            + (interest + (Math.pow(volatility, 2) / 2)) 
            * timehorizon)
            / (volatility * Math.sqrt(timehorizon));

    double d2 	= d1 - (volatility * Math.sqrt(timehorizon));
    return  strike * Math.exp(-interest * timehorizon)
    * Gaussian.cdf(-d2)
    - stock * Gaussian.cdf(-d1);
    }
}
