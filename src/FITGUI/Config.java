/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FITGUI;

import java.io.File;
import java.util.Scanner;

/**
 *
 * @author thela
 */
public class Config {

/*
    NOTE:  UNABLE TO USE THIS FILE.  IT CAUSED OTHER FILE OPERATIONS TO FAIL.  Perhaps related to static declaration.  Had to skip the Load_Config() procedure call.
    */    
    public static int QuotesSocketNumber = 9977;
    public static int TTOrdersSocketNumber = 9988;
    public static int SchwabOrdersSocketNumber = 9999;
    
    public static int maxBufferSize = 45000;

    public static final String configFileName = "tony.csv";
    public static final String test = "1";
    
    // These are used to determine how much of a gap to use for trades in certain $ ranges.
    public static double under50Gap = 0.201;
    public static double under150Gap = 0.401;
    public static double over150Gap = 1.01;
    
    //  Table File Generation parameters with TF_ prefix
    public static String TF_Input_File_Name = "Morning Straddles - New GUI Download.TSV";  
    public static String TABLEDATA_FILENAME = "TableDate.txt";
    public static int TF_Date_Column = 0;
    public static int TF_Symbol_Column = 1;
    public static int TF_MinMove_Column = 6;
    public static int TF_AveMove_Column = 7;
    public static int TF_ExpMove_Column = 8;
    public static int TF_Close_Column = 9;
    public static int TF_MaxPrice_Column = 18;
    public static int TF_BuyWiggle_Column = 19;
    // Added 1/2/21  --------------------------
    public static int TF_MaxPrice1_Column = 52;
    public static int TF_Price1$$$_Column = 52;
    public static int TF_MaxPrice2_Column = 52;
    public static int TF_Price2$$$_Column = 52;
    public static int TF_MaxPrice3_Column = 52;
    public static int TF_Price3$$$_Column = 52;
    // ----------------------------------------
    public static int TF_EOWMin_Column = 52;
    public static int TF_EOWAve_Column = 53;
    public static int TF_PercentGoal_Column = 54;
    public static int TF_ChaseTime_Column = 55;
    public static int TF_ChaseAmount_Column = 56;
    public static int TF_TradeAmount1_Column = 57;
    public static int TF_TradeAmount2_Column = 58;
    public static int TF_TradeAmount3_Column = 59;
    public static int TF_TradeAmount4_Column = 60;
    public static int TF_TradeAmount5_Column = 61;
    public static int TF_MinutesToRun_Column = 62;
    public static int TF_JustGetIn_Column = 63;
    public static int TF_BlueLine_Column = 64;
    public static int TF_NumberOfStrikes_Column = 65;
    
    // Config parameters for OHLC generattion with OHLC_ prefix
    public static String OHLC_Input_File_Name = "Morning Straddles - GenQData.csv";
    public static String OHLC_Output_File_Name = "Updated_OHLC_Data.csv";
    public static int OHLC_Date_Column = 0;
    public static int OHLC_Symbol_Column = 1;
    public static int OHLC_DayOfWeek_Column = 2;
    public static int OHLC_NumberValidColumns = 3;
    
    // Config parameters for Daily Generation Data DG_ prefix
    public static String DG_Input_File_Name = "Morning Straddles - DayGenData.TSV";
    public static String DG_Output_File_Name = "Updated_Morning Straddles - DayGenData.CSV";
    public static int DG_Date_Column = 0;
    public static int DG_Symbol_Column = 1;
    public static int DG_DayOfWeek_Column = 2;
    public static int DG_OpenInterest_Column = 3;
    public static int DG_BidAsk_Column = 4;
    public static int DG_StartPrice_Column = 9;
    public static int DG_1SeriesLater_Column = 10;
    public static int DG_4SeriesLater_Column = 11;
    public static int DG_ThinkBack_Column = 12;
    public static int DG_BuyWiggle_Column = 19;
    public static int DG_ATMNightB4_Column = 20;
    public static int DG_IVNightB4_Column = 21;
    public static int DG_IV1SeriesLater_Column = 22;
    public static int DG_IV4SeriesLater_Column = 23;
    public static int DG_NumberValidColumns = 24;
    public static int DG_DayPlay = 26;
    public static int DG_PrevTradeEntries = 30;
    public static int DG_PrevTradeResults = 31;
    public static int DG_ActualEntry = 32;
    public static int DG_ActualExit = 33;
    public static int DG_Duration = 34;
    public static int DG_Profit = 35;
    public static String DG_OpenInterestStrikes = "11";
    public static String DG_BidAskStrikes = "3";
    public static double DG_InterestRate = 0.01;
    public static double DG_MinNumberContracts = 5.0;
    public static double DG_BidAskGreenPercent = 0.01;
    public static double DG_BidAskYellowPercent = 0.03;
    public static double DG_PercentOfD1 = 0.6;
    public static double DG_PercentOfD2 = 0.85;

    public static int LV_Low_Value_Column = 36;
    public static int LV_Low_Time_Column = 37;
    
    public static void Set_Value( String var, String value) {

        switch ( var ) {
            case "under50Gap":
                under50Gap = Double.parseDouble( value );
                break;
            case "under150Gap":
                under150Gap = Double.parseDouble( value );
                break;
            case "over150Gap":
                over150Gap = Double.parseDouble( value );
                break;
            case "TF_Input_File_Name":
                TF_Input_File_Name = value;
                break;
            case "TABLEDATA_FILENAME":
                TABLEDATA_FILENAME = value;
                break;
            case "TF_Date_Column":
                TF_Date_Column = Integer.parseInt( value );
                break;
            case "TF_Symbol_Column":
                TF_Symbol_Column = Integer.parseInt( value );
                break;
            case "TF_MaxPrice_Column":
                TF_MaxPrice_Column = Integer.parseInt( value );
                break;
            case "TF_MinMove_Column":
                TF_MinMove_Column = Integer.parseInt( value );
                break;
            case "TF_AveMove_Column":
                TF_AveMove_Column = Integer.parseInt( value );
                break;
            case "TF_ExpMove_Column":
                TF_ExpMove_Column = Integer.parseInt( value );
                break;
            case "TF_Close_Column":
                TF_Close_Column = Integer.parseInt( value );
                break;
            case "TF_BuyWiggle_Column":
                TF_BuyWiggle_Column = Integer.parseInt( value );
                break;
            case "TF_MinWait_Column":  // 1/12/24 This is now EOW Min Value
                TF_EOWMin_Column = Integer.parseInt( value );
                break;
            case "TF_MaxWait_Column":
                TF_EOWAve_Column = Integer.parseInt( value );
                break;
            case "TF_PercentGoal_Column":
                TF_PercentGoal_Column = Integer.parseInt( value );
                break;
            case "TF_ChaseTime_Column":
                TF_ChaseTime_Column = Integer.parseInt( value );
                break;
            case "TF_ChaseAmount_Column":
                TF_ChaseAmount_Column = Integer.parseInt( value );
                break;
            case "TF_TradeAmount1_Column":
                TF_TradeAmount1_Column = Integer.parseInt( value );
                break;
            case "TF_TradeAmount2_Column":
                TF_TradeAmount2_Column = Integer.parseInt( value );
                break;
            case "TF_TradeAmount3_Column":
                TF_TradeAmount3_Column = Integer.parseInt( value );
                break;
            case "TF_TradeAmount4_Column":
                TF_TradeAmount4_Column = Integer.parseInt( value );
                break;
            case "TF_TradeAmount5_Column":
                TF_TradeAmount5_Column = Integer.parseInt( value );
                break;
            case "TF_MinutesToRun_Column":
                TF_MinutesToRun_Column = Integer.parseInt( value );
                break;
            case "TF_JustGetIn_Column":
                TF_JustGetIn_Column = Integer.parseInt( value );
                break;
            case "TF_BlueLine_Column":
                TF_BlueLine_Column = Integer.parseInt( value );
                break;
            case "TF_NumberOfStrikes_Column":
                TF_NumberOfStrikes_Column = Integer.parseInt( value );
                break;
            case "OHLC_Input_File_Name":
                OHLC_Input_File_Name =  value;
                break;
            case "OHLC_Output_File_Name":
                OHLC_Output_File_Name = value;
                break;
            case "OHLC_Date_Column":
                OHLC_Date_Column = Integer.parseInt( value );
                break;
            case "OHLC_Symbol_Column":
                OHLC_Symbol_Column = Integer.parseInt( value );
                break;
            case "OHLC_DayOfWeek_Column":
                OHLC_DayOfWeek_Column = Integer.parseInt( value );
                break;
            case "OHLC_NumberValidColumns":
                OHLC_NumberValidColumns = Integer.parseInt( value );
                break;
            case "DG_Input_File_Name":
                DG_Input_File_Name =  value;
                break;
            case "DG_Output_File_Name":
                DG_Output_File_Name = value;
                break;
            case "DG_Date_Column":
                DG_Date_Column = Integer.parseInt( value );
                break;
            case "DG_Symbol_Column":
                DG_Symbol_Column = Integer.parseInt( value );
                break;
            case "DG_DayOfWeek_Column":
                DG_DayOfWeek_Column = Integer.parseInt( value );
                break;
            case "DG_OpenInterest_Column":
                DG_OpenInterest_Column = Integer.parseInt( value );
                break;
            case "DG_BidAsk_Column":
                DG_BidAsk_Column = Integer.parseInt( value );
                break;
            case "DG_StartPrice_Column":
                DG_StartPrice_Column = Integer.parseInt( value );
                break;
            case "DG_1SeriesLater_Column":
                DG_1SeriesLater_Column = Integer.parseInt( value );
                break;
            case "DG_4SeriesLater_Column":
                DG_4SeriesLater_Column = Integer.parseInt( value );
                break;
            case "DG_ThinkBack_Column":
                DG_ThinkBack_Column = Integer.parseInt( value );
                break;
            case "DG_BuyWiggle_Column":
                DG_BuyWiggle_Column = Integer.parseInt( value );
                break;
            case "DG_ATMNightB4_Column":
                DG_ATMNightB4_Column = Integer.parseInt( value );
                break;
            case "DG_IVNightB4_Column":
                DG_IVNightB4_Column = Integer.parseInt( value );
                break;
            case "DG_IV1SeriesLater_Column":
                DG_IV1SeriesLater_Column = Integer.parseInt( value );
                break;
            case "DG_IV4SeriesLater_Column":
                DG_IV4SeriesLater_Column = Integer.parseInt( value );
                break;
            case "DG_NumberValidColumns":
                DG_NumberValidColumns = Integer.parseInt( value );
                break;
            case "DG_OpenInterestStrikes":
                DG_OpenInterestStrikes =  value;
                break;
            case "DG_BidAskStrikes":
                DG_BidAskStrikes = value;
                break;
            case "DG_InterestRate":
                DG_InterestRate = Double.parseDouble( value );
                break;
            case "DG_MinNumberContracts":
                DG_MinNumberContracts = Double.parseDouble( value );
                break;
            case "DG_BidAskGreenPercent":
                DG_BidAskGreenPercent = Double.parseDouble( value );
                break;
            case "DG_BidAskYellowPercent":
                DG_BidAskYellowPercent = Double.parseDouble( value );
                break;
        }   
    }

    
    // DO NOT USE THE METHOD BELOW.  HERE FOR HISTORY REASONS
    public static void Load_Config( javax.swing.JTextArea Results_Text_Area ) {

        File file = new File(configFileName);
        try {
            // Read the file.
            Scanner inputStream = new Scanner( file );

            // Loop until the end of the file.
            while (inputStream.hasNext())            {
                String data = inputStream.nextLine();     // Read the next line.
                String[] values = data.split(",");        // Get the values from the line.
                // The expectation is that each line of the file contains a name and a value and that is all.
                switch ( values[0] ) {
                    case "under50Gap":
                        under50Gap = Double.parseDouble( values[1] );
                        break;
                    case "under150Gap":
                        under150Gap = Double.parseDouble( values[1] );
                        break;
                    case "over150Gap":
                        over150Gap = Double.parseDouble( values[1] );
                        break;
                    case "TF_Input_File_Name":
                        TF_Input_File_Name = values[1];
                        break;
                    case "TABLEDATA_FILENAME":
                        TABLEDATA_FILENAME = values[1];
                        break;
                    case "TF_Date_Column":
                        TF_Date_Column = Integer.parseInt( values[1] );
                        break;
                    case "TF_Symbol_Column":
                        TF_Symbol_Column = Integer.parseInt( values[1] );
                        break;
                    case "TF_MaxPrice_Column":
                        TF_MaxPrice_Column = Integer.parseInt( values[1] );
                        break;
                    case "TF_MinMove_Column":
                        TF_MinMove_Column = Integer.parseInt( values[1] );
                        break;
                    case "TF_AveMove_Column":
                        TF_AveMove_Column = Integer.parseInt( values[1] );
                        break;
                    case "TF_ExpMove_Column":
                        TF_ExpMove_Column = Integer.parseInt( values[1] );
                        break;
                    case "TF_Close_Column":
                        TF_Close_Column = Integer.parseInt( values[1] );
                        break;
                    case "TF_BuyWiggle_Column":
                        TF_BuyWiggle_Column = Integer.parseInt( values[1] );
                        break;
// 1/12/24                   case "TF_MinWait_Column":   // 1/12/24 Now EOW Min Value
                    case "TF_EOWMin_Column":   // 1/12/24 Now EOW Min Value
                        TF_EOWMin_Column = Integer.parseInt( values[1] );
                        break;
// 1/12/24                   case "TF_MaxWait_Column":
                    case "TF_EOWAve_Column":
                        TF_EOWAve_Column = Integer.parseInt( values[1] );
                        break;
                    case "TF_PercentGoal_Column":
                        TF_PercentGoal_Column = Integer.parseInt( values[1] );
                        break;
                    case "TF_ChaseTime_Column":
                        TF_ChaseTime_Column = Integer.parseInt( values[1] );
                        break;
                    case "TF_ChaseAmount_Column":
                        TF_ChaseAmount_Column = Integer.parseInt( values[1] );
                        break;
                    case "TF_TradeAmount1_Column":
                        TF_TradeAmount1_Column = Integer.parseInt( values[1] );
                        break;
                    case "TF_TradeAmount2_Column":
                        TF_TradeAmount2_Column = Integer.parseInt( values[1] );
                        break;
                    case "TF_TradeAmount3_Column":
                        TF_TradeAmount3_Column = Integer.parseInt( values[1] );
                        break;
                    case "TF_TradeAmount4_Column":
                        TF_TradeAmount4_Column = Integer.parseInt( values[1] );
                        break;
                    case "TF_TradeAmount5_Column":
                        TF_TradeAmount5_Column = Integer.parseInt( values[1] );
                        break;
                    case "TF_MinutesToRun_Column":
                        TF_MinutesToRun_Column = Integer.parseInt( values[1] );
                        break;
                    case "TF_JustGetIn_Column":
                        TF_JustGetIn_Column = Integer.parseInt( values[1] );
                        break;
                    case "TF_BlueLine_Column":
                        TF_BlueLine_Column = Integer.parseInt( values[1] );
                        break;
                    case "TF_NumberOfStrikes_Column":
                        TF_NumberOfStrikes_Column = Integer.parseInt( values[1] );
                        break;
                    case "OHLC_Input_File_Name":
                        OHLC_Input_File_Name =  values[1];
                        break;
                    case "OHLC_Output_File_Name":
                        OHLC_Output_File_Name = values[1];
                        break;
                    case "OHLC_Date_Column":
                        OHLC_Date_Column = Integer.parseInt( values[1] );
                        break;
                    case "OHLC_Symbol_Column":
                        OHLC_Symbol_Column = Integer.parseInt( values[1] );
                        break;
                    case "OHLC_DayOfWeek_Column":
                        OHLC_DayOfWeek_Column = Integer.parseInt( values[1] );
                        break;
                    case "OHLC_NumberValidColumns":
                        OHLC_NumberValidColumns = Integer.parseInt( values[1] );
                        break;
                    case "DG_Input_File_Name":
                        DG_Input_File_Name =  values[1];
                        break;
                    case "DG_Output_File_Name":
                        DG_Output_File_Name = values[1];
                        break;
                    case "DG_Date_Column":
                        DG_Date_Column = Integer.parseInt( values[1] );
                        break;
                    case "DG_Symbol_Column":
                        DG_Symbol_Column = Integer.parseInt( values[1] );
                        break;
                    case "DG_DayOfWeek_Column":
                        DG_DayOfWeek_Column = Integer.parseInt( values[1] );
                        break;
                    case "DG_OpenInterest_Column":
                        DG_OpenInterest_Column = Integer.parseInt( values[1] );
                        break;
                    case "DG_BidAsk_Column":
                        DG_BidAsk_Column = Integer.parseInt( values[1] );
                        break;
                    case "DG_StartPrice_Column":
                        DG_StartPrice_Column = Integer.parseInt( values[1] );
                        break;
                    case "DG_1SeriesLater_Column":
                        DG_1SeriesLater_Column = Integer.parseInt( values[1] );
                        break;
                    case "DG_4SeriesLater_Column":
                        DG_4SeriesLater_Column = Integer.parseInt( values[1] );
                        break;
                    case "DG_ThinkBack_Column":
                        DG_ThinkBack_Column = Integer.parseInt( values[1] );
                        break;
                    case "DG_BuyWiggle_Column":
                        DG_BuyWiggle_Column = Integer.parseInt( values[1] );
                        break;
                    case "DG_ATMNightB4_Column":
                        DG_ATMNightB4_Column = Integer.parseInt( values[1] );
                        break;
                    case "DG_IVNightB4_Column":
                        DG_IVNightB4_Column = Integer.parseInt( values[1] );
                        break;
                    case "DG_IV1SeriesLater_Column":
                        DG_IV1SeriesLater_Column = Integer.parseInt( values[1] );
                        break;
                    case "DG_IV4SeriesLater_Column":
                        DG_IV4SeriesLater_Column = Integer.parseInt( values[1] );
                        break;
                    case "DG_NumberValidColumns":
                        DG_NumberValidColumns = Integer.parseInt( values[1] );
                        break;
                    case "DG_OpenInterestStrikes":
                        DG_OpenInterestStrikes =  values[1];
                        break;
                    case "DG_BidAskStrikes":
                        DG_BidAskStrikes = values[1];
                        break;
                    case "DG_InterestRate":
                        DG_InterestRate = Double.parseDouble( values[1] );
                        break;
                    case "DG_MinNumberContracts":
                        DG_MinNumberContracts = Double.parseDouble( values[1] );
                        break;
                    case "DG_BidAskGreenPercent":
                        DG_BidAskGreenPercent = Double.parseDouble( values[1] );
                        break;
                    case "DG_BidAskYellowPercent":
                        DG_BidAskYellowPercent = Double.parseDouble( values[1] );
                        break;
                }   
            }
            inputStream.close();
        } catch (Exception ex) {
           Results_Text_Area.append("FYI ... No Config File processed ...   \n");
           Results_Text_Area.append("FYI ... Default values will be used for all config values.  \n");
           Utility.dumpExceptionInfo(ex);
           ex.printStackTrace();
            
        }
    }

}
