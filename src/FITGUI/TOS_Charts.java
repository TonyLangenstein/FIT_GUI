/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FITGUI;

import static FITGUI.OHLC.getYear;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.List;

/**
 *  "NT"
 * @author thela
 */
public class TOS_Charts {


   private static String normalizeDateFormat( String thisDate ) {
        String updatedDate = thisDate;
        if (updatedDate.length() <= 5) {
            // Append the year
            updatedDate = updatedDate.concat("/" + getYear());
        }
        String firstChar = updatedDate.substring(0, 1);
        if (firstChar.equalsIgnoreCase("0")) {
            updatedDate = updatedDate.substring(1);
        }
        return updatedDate;
    }

public static void Google_Gen_TOS_Study_Code ( String theDate, String sheetName ) {
    try {
         String DailyChartCode = "ChartCode_" + new SimpleDateFormat("MM_dd_yyyy").format(new Date()) + ".txt";
         PrintWriter tokensWriter = new PrintWriter( DailyChartCode );

         // Read the data from Google.
         BatchGetValuesResponse GUIDownload = GoogleOperations.GetSheet( sheetName );
         List<ValueRange> VRs = GUIDownload.getValueRanges();
         ValueRange VR = VRs.get(0);
         List<List<Object>> values = VR.getValues();
        /*
        def Today = 20190726;
        def WhichOne = 
            if GetSymbol() == "NTR" then 1 else 
            if GetSymbol() == "BYND" then 2 else 
            if GetSymbol() == "CRSP" then 3 else 
            4;
        */         
//         tokensWriter.println("def Today  = " + new SimpleDateFormat("YYYYMMdd").format(new Date()) + ";");
         LocalDate today = LocalDate.now();
         String year = Integer.toString(today.getYear());
         String[] parts = theDate.split("/");  //  mm/dd
         if ( parts[0].length() == 1 ) { parts[0] = "0" + parts[0]; }
         if ( parts[1].length() == 1 ) { parts[1] = "0" + parts[1]; }
         tokensWriter.println("input Today  = " + year + parts[0] + parts[1] + ";");
         tokensWriter.println(" ");
         tokensWriter.println("def WhichOne = ");
         
//         String fileName = Config.TF_Input_File_Name;
//         File file = new File(fileName);
         String dateInFile = "";
         Integer rowCnt = 1;
         String previousSymbol = "";

         // Now loop thru the values which are rows and process them.
         for( int rowIndex = 0; rowIndex < values.size(); rowIndex++) {
            List<Object> row = values.get(rowIndex);

            // If the values are for the current date, then insert them into the file.
            dateInFile = normalizeDateFormat( row.get( Config.TF_Date_Column ).toString() );   // Date is 0 in the list    
         
            if (theDate.equals(dateInFile)) {
                if ( !row.get( Config.TF_Symbol_Column ).toString().equalsIgnoreCase( previousSymbol ) ) {
                    tokensWriter.println("  if GetSymbol() == \"" + row.get( Config.TF_Symbol_Column ).toString() + "\" then " + Integer.toString(rowCnt) + " else");
                    rowCnt++;
                    previousSymbol = row.get( Config.TF_Symbol_Column ).toString();
                }  // Let's not put in an entry for repeat symbols.  We only need one entry.  Assumes they are consecutive.
            }
         }
         tokensWriter.println( "  " + Integer.toString( rowCnt ) + ";");
         tokensWriter.println( "  ");
         

        /*
        def Min1 = 1;
        def Ave1 = 2;
        def EM1 = 3;
        def Cl1 = 50.83;
        def ProgAmt1 = 2.1;
        def MaxAmt1 = 2.0;
        def GreenLine1 = -1;
        def RedLine1 = -1;

        def Min2 = 5;
        def Ave2 = 10;
        def EM2 = 15;
        def Cl2 = 234.9;
        def ProgAmt2 = 8;
        def MaxAmt2 = 4;
        def GreenLine2 = -1;
        def RedLine2 = -1;
         
         etc.         
         */
         rowCnt = 1;
         previousSymbol = "";

         // Now loop thru the values which are rows and process them.
         for( int rowIndex = 0; rowIndex < values.size(); rowIndex++) {
            List<Object> row = values.get(rowIndex);

            // If the values are for the current date, then insert them into the table.
            dateInFile = normalizeDateFormat( row.get( Config.TF_Date_Column ).toString() );   // Date is 0 in the list,  Symbol = 1 in the list
            if (theDate.equals(dateInFile))               {
                if ( !row.get( Config.TF_Symbol_Column ).toString().equalsIgnoreCase( previousSymbol ) ) {
                    tokensWriter.println("  def Min" + Integer.toString(rowCnt)+ " = " + row.get( Config.TF_MinMove_Column ).toString() + ";");
                    tokensWriter.println("  def Ave" + Integer.toString(rowCnt)+ " = " + row.get( Config.TF_AveMove_Column ).toString() + ";");
                    tokensWriter.println("  def EM" + Integer.toString(rowCnt)+ " = " + row.get( Config.TF_ExpMove_Column ).toString() + ";");         
                    tokensWriter.println("  def Cl" + Integer.toString(rowCnt)+ " = " + row.get( Config.TF_Close_Column ).toString() + ";");         
                    tokensWriter.println("  def ProgAmt" + Integer.toString(rowCnt)+ " = " + row.get(Config.TF_MaxPrice_Column ).toString() + ";");    // Set Program to Max
                    tokensWriter.println("  def MaxAmt" + Integer.toString(rowCnt)+ " = " + row.get( Config.TF_MaxPrice_Column ).toString() + ";");
                    // 1/12/24 Min Wait is now Min EOW value.
                    if ( row.get( Config.TF_EOWMin_Column ).toString().equalsIgnoreCase("5") ) {  // Min Wait
                        tokensWriter.println("  def GreenLine" + Integer.toString(rowCnt)+ " = " + row.get( Config.TF_MinMove_Column ).toString() + ";");
                    } else {
                        tokensWriter.println("  def GreenLine" + Integer.toString(rowCnt)+ " = " + row.get( Config.TF_EOWMin_Column ).toString() + ";");  // Min Wait
                    }
                    // 1/12/24  Max Wait is now Ave EOW value.
                    if ( row.get( Config.TF_EOWAve_Column ).toString().equalsIgnoreCase("30") ) {  // Max Wait
                        tokensWriter.println("  def RedLine" + Integer.toString(rowCnt)+ " = " + row.get( Config.TF_AveMove_Column ).toString() + ";");
                    } else {
                        tokensWriter.println("  def RedLine" + Integer.toString(rowCnt)+ " = " + row.get( Config.TF_EOWAve_Column ).toString() + ";");
                    }
                    tokensWriter.println( "  ");
                    rowCnt++;
                    previousSymbol = row.get( Config.TF_Symbol_Column ).toString();
                }
            }
         }
        tokensWriter.println("  def Min" + Integer.toString(rowCnt)+ " = 0.5;");
        tokensWriter.println("  def Ave" + Integer.toString(rowCnt)+ " = 1.0;");
        tokensWriter.println("  def EM" + Integer.toString(rowCnt)+ " = 1.5;");         
        tokensWriter.println("  def Cl" + Integer.toString(rowCnt)+ " = 50;");         
        tokensWriter.println("  def ProgAmt" + Integer.toString(rowCnt)+ " = 0.0;");    // Set Program to Max
        tokensWriter.println("  def MaxAmt" + Integer.toString(rowCnt)+ " = 0.0;");
        tokensWriter.println("  def GreenLine" + Integer.toString(rowCnt)+ " = -1;");
        tokensWriter.println("  def RedLine" + Integer.toString(rowCnt)+ " = -1;");
        tokensWriter.println( "  ");

        /*
        def Min = 
            if WhichOne == 1 then Min1 else 
            if WhichOne == 2 then Min2 else 
            Min2;
        def Ave = if GetSymbol() == "NTR" then Ave1 else Ave2;
        def EM = if GetSymbol() == "NTR" then EM1 else EM2;
        def Cl = if GetSymbol() == "NTR" then Cl1 else Cl2;
        def ProgAmt = if GetSymbol() == "NTR" then ProgAmt1 else ProgAmt2;
        def MaxAmt = if GetSymbol() == "NTR" then MaxAmt1 else MaxAmt2;
        def GreenLine = if GetSymbol() == "NTR" then GreenLine1 else GreenLine2;
        def RedLine = if GetSymbol() == "NTR" then RedLine1 else RedLine2;
         */
        tokensWriter.println("def Min =");
        for (int ddd = 1; ddd < rowCnt; ddd++) {
            tokensWriter.println(  "  if WhichOne == " + Integer.toString(ddd) + " then Min" + Integer.toString(ddd) + " else" );    
        }
        tokensWriter.println(  "  Min" + Integer.toString(rowCnt) + ";" );    
        tokensWriter.println( " " );
         
        tokensWriter.println("def Ave =");
        for (int ddd = 1; ddd < rowCnt; ddd++) {
            tokensWriter.println(  "  if WhichOne == " + Integer.toString(ddd) + " then Ave" + Integer.toString(ddd) + " else" );    
        }
        tokensWriter.println(  "  Ave" + Integer.toString(rowCnt) + ";" );    
        tokensWriter.println( " " );

        tokensWriter.println("def EM =");
        for (int ddd = 1; ddd < rowCnt; ddd++) {
            tokensWriter.println(  "  if WhichOne == " + Integer.toString(ddd) + " then EM" + Integer.toString(ddd) + " else" );    
        }
        tokensWriter.println(  "  EM" + Integer.toString(rowCnt) + ";" );    
        tokensWriter.println( " " );

        tokensWriter.println("def Cl =");
        for (int ddd = 1; ddd < rowCnt; ddd++) {
            tokensWriter.println(  "  if WhichOne == " + Integer.toString(ddd) + " then Cl" + Integer.toString(ddd) + " else" );    
        }
        tokensWriter.println(  "  Cl" + Integer.toString(rowCnt) + ";" );    
        tokensWriter.println( " " );

        tokensWriter.println("def ProgAmt =");
        for (int ddd = 1; ddd < rowCnt; ddd++) {
            tokensWriter.println(  "  if WhichOne == " + Integer.toString(ddd) + " then ProgAmt" + Integer.toString(ddd) + " else" );    
        }
        tokensWriter.println(  "  ProgAmt" + Integer.toString(rowCnt) + ";" );    
        tokensWriter.println( " " );

        tokensWriter.println("def MaxAmt =");
        for (int ddd = 1; ddd < rowCnt; ddd++) {
            tokensWriter.println(  "  if WhichOne == " + Integer.toString(ddd) + " then MaxAmt" + Integer.toString(ddd) + " else" );    
        }
        tokensWriter.println(  "  MaxAmt" + Integer.toString(rowCnt) + ";" );    
        tokensWriter.println( " " );

        tokensWriter.println("def GreenLine =");
        for (int ddd = 1; ddd < rowCnt; ddd++) {
            tokensWriter.println(  "  if WhichOne == " + Integer.toString(ddd) + " then GreenLine" + Integer.toString(ddd) + " else" );    
        }
        tokensWriter.println(  "  GreenLine" + Integer.toString(rowCnt) + ";" );    
        tokensWriter.println( " " );

        tokensWriter.println("def RedLine =");
        for (int ddd = 1; ddd < rowCnt; ddd++) {
            tokensWriter.println(  "  if WhichOne == " + Integer.toString(ddd) + " then RedLine" + Integer.toString(ddd) + " else" );    
        }
        tokensWriter.println(  "  RedLine" + Integer.toString(rowCnt) + ";" );            
        tokensWriter.println( " " );

        // Close the newly created file
         tokensWriter.close();
    } catch (FileNotFoundException ex) {
         Utility.dumpExceptionInfo(ex);
         ex.printStackTrace();
    } catch (Exception ex) {
         Utility.dumpExceptionInfo(ex);
         ex.printStackTrace();
    }    
}
    
public static void Gen_TOS_Study_Code (  String theDate ) {
    try {
         String DailyChartCode = "ChartCode_" + new SimpleDateFormat("MM_dd_yyyy").format(new Date()) + ".txt";
         PrintWriter tokensWriter = new PrintWriter( DailyChartCode );

        /*
        def Today = 20190726;
        def WhichOne = 
            if GetSymbol() == "NTR" then 1 else 
            if GetSymbol() == "BYND" then 2 else 
            if GetSymbol() == "CRSP" then 3 else 
            4;
        */         
//         tokensWriter.println("def Today  = " + new SimpleDateFormat("YYYYMMdd").format(new Date()) + ";");
         LocalDate today = LocalDate.now();
         String year = Integer.toString(today.getYear());
         String[] parts = theDate.split("/");  //  mm/dd
         if ( parts[0].length() == 1 ) { parts[0] = "0" + parts[0]; }
         if ( parts[1].length() == 1 ) { parts[1] = "0" + parts[1]; }
         tokensWriter.println("input Today  = " + year + parts[0] + parts[1] + ";");
         tokensWriter.println(" ");
         tokensWriter.println("def WhichOne = ");
         
         String fileName = Config.TF_Input_File_Name;
         File file = new File(fileName);
         String dateInFile = "";

         // Read the file.
         Scanner inputStream = new Scanner(file);
         String previousSymbol = "";
         
         // Loop until the end of the file.
         Integer row = 1;
         while (inputStream.hasNext())            {
            String data = inputStream.nextLine();     // Read the next line.
            String[] values = data.split("\\t");        // Get the values from the line.

            // If the values are for the current date, then insert them into the table.
            dateInFile = normalizeDateFormat( values[ Config.TF_Date_Column ] );   // Date is 0 in the list,  Symbol = 1 in the list
            if (theDate.equals(dateInFile))               {

                if ( !values[ Config.TF_Symbol_Column ].equalsIgnoreCase( previousSymbol ) ) {
                    tokensWriter.println("  if GetSymbol() == \"" + values[1] + "\" then " + Integer.toString(row) + " else");
                    row++;
                    previousSymbol = values[ Config.TF_Symbol_Column ];
                }  // Let's not put in an entry for repeat symbols.  We only need one entry.  Assumes they are consecutive.
            }
         }
         tokensWriter.println( "  " + Integer.toString( row ) + ";");
         tokensWriter.println( "  ");

         // Close the file.
         inputStream.close();

        /*
        def Min1 = 1;
        def Ave1 = 2;
        def EM1 = 3;
        def Cl1 = 50.83;
        def ProgAmt1 = 2.1;
        def MaxAmt1 = 2.0;
        def GreenLine1 = -1;
        def RedLine1 = -1;

        def Min2 = 5;
        def Ave2 = 10;
        def EM2 = 15;
        def Cl2 = 234.9;
        def ProgAmt2 = 8;
        def MaxAmt2 = 4;
        def GreenLine2 = -1;
        def RedLine2 = -1;
         
         etc.         
         */
         inputStream = new Scanner(file);

         // Loop until the end of the file.
         row = 1;
         previousSymbol = "";
         while (inputStream.hasNext())  {
            String data = inputStream.nextLine();     // Read the next line.
            String[] values = data.split("\\t");        // Get the values from the line.

            // If the values are for the current date, then insert them into the table.
            dateInFile = values[0];   // Date is 0 in the list,  Symbol = 1 in the list
            if (theDate.equals(dateInFile))               {
                if ( !values[ Config.TF_Symbol_Column ].equalsIgnoreCase( previousSymbol ) ) {
                    tokensWriter.println("  def Min" + Integer.toString(row)+ " = " + values[ Config.TF_MinMove_Column ] + ";");
                    tokensWriter.println("  def Ave" + Integer.toString(row)+ " = " + values[ Config.TF_AveMove_Column ] + ";");
                    tokensWriter.println("  def EM" + Integer.toString(row)+ " = " + values[ Config.TF_ExpMove_Column ] + ";");         
                    tokensWriter.println("  def Cl" + Integer.toString(row)+ " = " + values[ Config.TF_Close_Column ] + ";");         
                    tokensWriter.println("  def ProgAmt" + Integer.toString(row)+ " = " + values[ Config.TF_MaxPrice_Column ] + ";");    // Set Program to Max
                    tokensWriter.println("  def MaxAmt" + Integer.toString(row)+ " = " + values[ Config.TF_MaxPrice_Column ] + ";");
                    // 1/12/24  Min Wait is now Min EOW value.
                    if ( values[ Config.TF_EOWMin_Column ].equalsIgnoreCase("5") ) {  // Min Wait
                        tokensWriter.println("  def GreenLine" + Integer.toString(row)+ " = " + values[ Config.TF_MinMove_Column ] + ";");
                    } else {
                        tokensWriter.println("  def GreenLine" + Integer.toString(row)+ " = " + values[ Config.TF_EOWMin_Column ] + ";");  // Min Wait
                    }
                    // 1/12/24  Ave Wait is now Ave EOW value.
                    if ( values[ Config.TF_EOWAve_Column ].equalsIgnoreCase("30") ) {  // Max Wait
                        tokensWriter.println("  def RedLine" + Integer.toString(row)+ " = " + values[ Config.TF_AveMove_Column ] + ";");
                    } else {
                        tokensWriter.println("  def RedLine" + Integer.toString(row)+ " = " + values[ Config.TF_EOWAve_Column ] + ";");
                    }
                    tokensWriter.println( "  ");
                    row++;
                    previousSymbol = values[ Config.TF_Symbol_Column ];
                }
            }
         }
        tokensWriter.println("  def Min" + Integer.toString(row)+ " = 0.5;");
        tokensWriter.println("  def Ave" + Integer.toString(row)+ " = 1.0;");
        tokensWriter.println("  def EM" + Integer.toString(row)+ " = 1.5;");         
        tokensWriter.println("  def Cl" + Integer.toString(row)+ " = 50;");         
        tokensWriter.println("  def ProgAmt" + Integer.toString(row)+ " = 0.0;");    // Set Program to Max
        tokensWriter.println("  def MaxAmt" + Integer.toString(row)+ " = 0.0;");
        tokensWriter.println("  def GreenLine" + Integer.toString(row)+ " = -1;");
        tokensWriter.println("  def RedLine" + Integer.toString(row)+ " = -1;");
        tokensWriter.println( "  ");

         // Close the file.
         inputStream.close();
        
         
        /*
        def Min = 
            if WhichOne == 1 then Min1 else 
            if WhichOne == 2 then Min2 else 
            Min2;
        def Ave = if GetSymbol() == "NTR" then Ave1 else Ave2;
        def EM = if GetSymbol() == "NTR" then EM1 else EM2;
        def Cl = if GetSymbol() == "NTR" then Cl1 else Cl2;
        def ProgAmt = if GetSymbol() == "NTR" then ProgAmt1 else ProgAmt2;
        def MaxAmt = if GetSymbol() == "NTR" then MaxAmt1 else MaxAmt2;
        def GreenLine = if GetSymbol() == "NTR" then GreenLine1 else GreenLine2;
        def RedLine = if GetSymbol() == "NTR" then RedLine1 else RedLine2;
         */
        tokensWriter.println("def Min =");
        for (int ddd = 1; ddd < row; ddd++) {
            tokensWriter.println(  "  if WhichOne == " + Integer.toString(ddd) + " then Min" + Integer.toString(ddd) + " else" );    
        }
        tokensWriter.println(  "  Min" + Integer.toString(row) + ";" );    
        tokensWriter.println( " " );
         
        tokensWriter.println("def Ave =");
        for (int ddd = 1; ddd < row; ddd++) {
            tokensWriter.println(  "  if WhichOne == " + Integer.toString(ddd) + " then Ave" + Integer.toString(ddd) + " else" );    
        }
        tokensWriter.println(  "  Ave" + Integer.toString(row) + ";" );    
        tokensWriter.println( " " );

        tokensWriter.println("def EM =");
        for (int ddd = 1; ddd < row; ddd++) {
            tokensWriter.println(  "  if WhichOne == " + Integer.toString(ddd) + " then EM" + Integer.toString(ddd) + " else" );    
        }
        tokensWriter.println(  "  EM" + Integer.toString(row) + ";" );    
        tokensWriter.println( " " );

        tokensWriter.println("def Cl =");
        for (int ddd = 1; ddd < row; ddd++) {
            tokensWriter.println(  "  if WhichOne == " + Integer.toString(ddd) + " then Cl" + Integer.toString(ddd) + " else" );    
        }
        tokensWriter.println(  "  Cl" + Integer.toString(row) + ";" );    
        tokensWriter.println( " " );

        tokensWriter.println("def ProgAmt =");
        for (int ddd = 1; ddd < row; ddd++) {
            tokensWriter.println(  "  if WhichOne == " + Integer.toString(ddd) + " then ProgAmt" + Integer.toString(ddd) + " else" );    
        }
        tokensWriter.println(  "  ProgAmt" + Integer.toString(row) + ";" );    
        tokensWriter.println( " " );

        tokensWriter.println("def MaxAmt =");
        for (int ddd = 1; ddd < row; ddd++) {
            tokensWriter.println(  "  if WhichOne == " + Integer.toString(ddd) + " then MaxAmt" + Integer.toString(ddd) + " else" );    
        }
        tokensWriter.println(  "  MaxAmt" + Integer.toString(row) + ";" );    
        tokensWriter.println( " " );

        tokensWriter.println("def GreenLine =");
        for (int ddd = 1; ddd < row; ddd++) {
            tokensWriter.println(  "  if WhichOne == " + Integer.toString(ddd) + " then GreenLine" + Integer.toString(ddd) + " else" );    
        }
        tokensWriter.println(  "  GreenLine" + Integer.toString(row) + ";" );    
        tokensWriter.println( " " );

        tokensWriter.println("def RedLine =");
        for (int ddd = 1; ddd < row; ddd++) {
            tokensWriter.println(  "  if WhichOne == " + Integer.toString(ddd) + " then RedLine" + Integer.toString(ddd) + " else" );    
        }
        tokensWriter.println(  "  RedLine" + Integer.toString(row) + ";" );            
        tokensWriter.println( " " );

        // Close the newly created file
         tokensWriter.close();
    } catch (FileNotFoundException ex) {
         Utility.dumpExceptionInfo(ex);
         ex.printStackTrace();
    } catch (Exception ex) {
         Utility.dumpExceptionInfo(ex);
         ex.printStackTrace();
    }    
/*


#  ---------------------------------------------------------------------------
#
#    Paste new code each trading day above this line from the generated code
#    in the FIT program.  This will enable symbol aware charts for the possible
#    plays for the day.
#
#  ---------------------------------------------------------------------------


# can we display at the top some Squeeze information
script In_Squeeze {
    def BB = BollingerBands().UpperBand;
    def KC = KeltnerChannels().Upper_Band;
#    AddLabel(yes, "BB=" + BB + ": KC=" + KC, Color.BLUE);
    plot IS = if BB < KC then 1 else 0;
#    AddLabel(yes, "Squeeze = " + IS, Color.BLUE);
}
AddLabel(yes, "Squeeze = " + if In_Squeeze() then "YES" else "no" , if In_Squeeze() then Color.RED else Color.WHITE);

# def ShowLines = (GetYYYYMMDD() >= Today - 1) and  (GetYYYYMMDD() < Today + 1);

def CT = SecondsFromTime(0930);
# 1m Chart normal, also do 5m and 15m Chart
def Bars = if GetAggregationPeriod() == AggregationPeriod.MIN then CT / 60 else if GetAggregationPeriod() == AggregationPeriod.FIVE_MIN then CT / 300 else if GetAggregationPeriod() == AggregationPeriod.FIFTEEN_MIN then CT / 900 else 0;
#AddLabel(yes, "CT =  " + CT + ";  Bars = " + Bars, Color.RED);

# 23400 Seconds total in 1 trading day
def ShowLines = CT >= 0 and CT <= 23400 and GetYYYYMMDD() == Today;
def ShowLabel = 0;  #ShowLines and Bars == 1;
#def ShowLabel = ShowLines and (GetYYYYMMDD() != GetYYYYMMDD()[1]);

def Op = AbsValue(GetValue(open, Bars));
#AddLabel(yes, "Op =  " + Op, Color.Yellow);

AddLabel(yes, "Min =  " + Min + ";  Ave = " + Ave, Color.GRAY);
# AddLabel(yes, "  Max $$ = " + MaxAmt, Color.WHITE);
AddLabel(yes, "   Program $$ = " + ProgAmt + ";  Max $$ = " + MaxAmt, Color.WHITE);

def tLen = 40;                  # This is the extra seconds to form a single line on the chart.
def GreenStart = GreenLine * 60;
def ShowGreen = if ShowLines then CT >= GreenStart and CT <= GreenStart + tLen else no;
def redStart = RedLine * 60;
def ShowRed = if ShowLines then CT >= redStart and CT <= redStart + tLen else no;
AddVerticalLine(ShowGreen, "ENTER ??", Color.GREEN, Curve.SHORT_DASH);
AddVerticalLine(ShowRed, "GET OUT !!", Color.RED, Curve.SHORT_DASH);

plot EMLine = if ShowLines then Cl + EM else Double.NaN;
EMLine.SetDefaultColor(color = Color.GREEN);
EMLine.SetPaintingStrategy(PaintingStrategy.LINE);
EMLine.SetLineWeight(2);
AddChartBubble(ShowLabel, Cl + EM, "EM  " + EM , Color.GREEN, yes);
AddLabel(ShowLines and GetValue(close, 0) >= (Cl + EM), "----- ABOVE EM -----", Color.GREEN);


plot NEMLine = if ShowLines then Cl - EM else Double.NaN;
NEMLine.SetDefaultColor(color = Color.GREEN);
NEMLine.SetPaintingStrategy(PaintingStrategy.LINE);
NEMLine.SetLineWeight(2);
AddChartBubble(ShowLabel, Cl - EM, "--EM --" + EM , Color.GREEN, yes);
AddLabel(ShowLines and GetValue(close, 0) <= (Cl - EM), "===== BELOW EM =====", Color.GREEN);

plot EM15Line = if ShowLines then Cl + 1.5 * EM else Double.NaN;
EM15Line.SetDefaultColor(color = Color.GREEN);
EM15Line.SetPaintingStrategy(PaintingStrategy.LINE);
EM15Line.SetLineWeight(4);
AddChartBubble(ShowLabel, Cl + 1.5 * EM, "1.5 EM  " + 1.5 * EM , Color.GREEN, yes);

plot NEM15Line = if ShowLines then Cl - 1.5 * EM else Double.NaN;
NEM15Line.SetDefaultColor(color = Color.GREEN);
NEM15Line.SetPaintingStrategy(PaintingStrategy.LINE);
NEM15Line.SetLineWeight(4);
AddChartBubble(ShowLabel, Cl - 1.5 * EM, "--1.5 EM --" + 1.5 * EM , Color.GREEN, yes);

plot MinLine = if ShowLines then Op + Min else Double.NaN;
MinLine.SetDefaultColor( Color.BLUE );
MinLine.SetPaintingStrategy(PaintingStrategy.LINE);
MinLine.SetLineWeight(2);
AddChartBubble(ShowLabel, Op + Min, "Min  " + Min , Color.BLUE, yes);
AddLabel(ShowLines and (GetValue(close, 0) >= (Op + Min)), "===== ABOVE MIN =====", Color.BLUE);

plot NMinLine = if ShowLines then Op - Min else Double.NaN;
NMinLine.SetDefaultColor( Color.BLUE );
NMinLine.SetPaintingStrategy(PaintingStrategy.LINE);
NMinLine.SetLineWeight(2);
AddChartBubble(ShowLabel, Op - Min, "-- Min --" + Min , Color.BLUE, yes);
AddLabel(ShowLines and GetValue(close, 0) <= (Op - Min), "----- BELOW MIN -----", Color.BLUE);

plot AveLine = if ShowLines then Op + Ave else Double.NaN;
AveLine.SetDefaultColor(color = Color.RED);
AveLine.SetPaintingStrategy(PaintingStrategy.LINE);
AveLine.SetLineWeight(2);
AddChartBubble(ShowLabel, Op + Ave, "Ave  " + Ave , Color.RED, yes);
AddLabel(ShowLines and GetValue(close, 0) >= (Op + Ave), "===== ABOVE AVE =====", Color.RED);

plot NAveLine = if ShowLines then Op - Ave else Double.NaN;
NAveLine.SetDefaultColor(color = Color.RED);
NAveLine.SetPaintingStrategy(PaintingStrategy.LINE);
NAveLine.SetLineWeight(2);
AddChartBubble(ShowLabel, Op - Ave, "--Ave --" + Ave , Color.RED, yes);
AddLabel(ShowLines and GetValue(close, 0) <= (Op - Ave), "----- BELOW AVE -----", Color.RED);    
    */    
}    

}
