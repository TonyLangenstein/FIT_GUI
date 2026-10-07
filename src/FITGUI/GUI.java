package FITGUI;

import java.util.prefs.Preferences;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.io.FileOutputStream;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.*;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import org.json.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.apache.logging.log4j.Level;
import javax.swing.JFileChooser;
import java.nio.file.Paths;
import java.awt.Desktop;
import java.net.URI;
import java.util.prefs.Preferences;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;


/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
/**

 @author Michael P. LeMire & Michael E LeMire
 */
public class GUI extends javax.swing.JFrame
   {
    // This block executes the instant the JVM loads the class, 
    // guaranteeing it runs BEFORE Log4j2 starts looking for configuration files.
    static {
        System.setProperty("app.name", "FIT_GUI");
        System.setProperty(
            "log4j2.contextSelector", 
            "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector"
        );
        
        // Optional: You can also set your unique identifier right here if you want
        System.setProperty("app.identifier", "FIT_GUI");

        // Explicitly forces Log4j2 to load your exact XML file location
        System.setProperty("log4j2.configurationFile", "log4j2.xml");
    }
    //Initialize the preferences node tied to your class package
    private final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    private JFrame tablePopupWindow;
    private javax.swing.JFrame popupFrame;
    

   // Font constants.
   final java.awt.Font HEADER_FONT = new java.awt.Font("Tahoma", 1, 12);
   final java.awt.Font POSITION_NAME_FONT = new java.awt.Font("Tahoma", 1, 14);
   final java.awt.Font STATUS_FONT = new java.awt.Font("Tahoma", 0, 14);
   final java.awt.Font CONTRACTS_FONT = new java.awt.Font("Tahoma", 0, 14);
   final java.awt.Font PRICE_FONT = new java.awt.Font("Tahoma", 0, 14);
   final java.awt.Font ARROW_FONT = new java.awt.Font("Tahoma", 1, 24);
   final java.awt.Font BUTTON_FONT = new java.awt.Font("Tahoma", 0, 14);
   final java.awt.Font PROFIT_FONT = new java.awt.Font("Tahoma", 0, 14);
   final java.awt.Font EXIT_TIME_FONT = new java.awt.Font("Tahoma", 0, 14);

   // Position panel component/widget name constants.  Note: They must be unique,
   // and they must not contain spaces.
   final String POSITION_NAME = "position";
   final String ACCOUNT_NAME = "account";
   final String SYMBOL_NAME = "symbol";
   final String STRIKE_NAME = "strike";
   final String STATUS_NAME = "status";
   final String CONTRACTS_NAME = "contracts";
   final String ARROW_NAME = "arrow";
   final String PROGRESS_NAME = "progress";
   final String MIN_MOVE_NAME = "min_move";
   final String AVE_MOVE_NAME = "ave_move";
   final String HIGH_WATER_UP_MOVE_NAME = "high_water_up_move";
   final String HIGH_WATER_DOWN_MOVE_NAME = "high_water_down_move";
   final String PAID_PRICE_NAME = "paid_price";
   final String POSITION_AMOUNT_NAME = "position_amount";
   final String CURRENT_LOCK_CHECKBOX_NAME = "current_lock_checkbox";
   final String CURRENT_PRICE_NAME = "current_price";
   final String CURRENT_PERCENTAGE_NAME = "current_percentage";
   final String CURRENT_AMOUNT_NAME = "current_amount";
   final String CURRENT_SELL_ALL_BUTTON_NAME = "current_sell_all_button";
   final String TARGET_PRICE_NAME = "target_price";
   final String TARGET_PERCENTAGE_NAME = "target_percentage";
   final String TARGET_AMOUNT_NAME = "target_amount";
   final String TARGET_SELL_ALL_BUTTON_NAME = "target_sell_all_button";
   final String TARGET_SELL_THREE_QUARTER_BUTTON_NAME = "target_sell_three_quarter_button";
   final String TARGET_SELL_HALF_BUTTON_NAME = "target_sell_half_button";
//   final String TARGET_SELL_CALLS_BUTTON_NAME = "target_sell_calls_button";
//   final String TARGET_SELL_PUTS_BUTTON_NAME = "target_sell_puts_button";
//   final String CANCEL_BUTTON_NAME = "cancel_button";

   private static boolean autoLogin = false;    //  -L arg
   private static boolean autoProcess = false;  //  -P arg
   private static boolean autoGUIOnly = false;  //  -G arg
   private static boolean autoShutDown = false; //  -S arg
   private static boolean loadingPrefs = false;

   // Variable used for dealing with when to start and stop processing and auto shut down of program.
   private static LocalTime openTime = LocalTime.now();
   private static LocalTime startTime = LocalTime.now();
   private static LocalTime endTime = LocalTime.now();
   private static LocalTime shutDownTime = LocalTime.now().plusHours(2);

   private static boolean keepGoing = true;
   private static final int lastColumn = 18;

   private static double maxPriceDefault = 1;
   private static double tradeAmountDefault = 5000;
   private static double buyWiggleDefault = 0.02;
   private static double minMoveDefault = 1.0;
   private static double aveMoveDefault = 2.0;
   private static double EOWMinDefault = 1.0;
   private static double percentGoalDefault = 35;
   private static int chaseWaitTimeDefault = 5;
   private static double chaseAmountDefault = 0.02;
   private static double EOWAveDefault = 2.0;
   private static int minutesToRunDefault = 0;
   private static String justGetInDefault = "N";
   private static String blueLineDefault = "Y";
   private static int numberOfStrikesDefault = 0;
   
   public static final String accountNameDefault = "";
   public static final String accountNumberDefault = "";
   public static final String maxTradesDefault = "";
   public static final String maxFundsDefault = "";
   public static final String brokerNameDefault = "";
   private static boolean prevKillOpenStraddleOrdersSelected = false;
   private static boolean initialLoadComplete = false;
   
   private static Log4J2AsyncLogger LogData = new Log4J2AsyncLogger();

   private JSONObject logout = new JSONObject();

   // Define a variable that contains the time that the stock market opens.  It will be used
   // to compute the Min & Max exit times.  The data values that we input will be an integer
   // that represents the number of minutes after the market opens.
   LocalTime Open_Time;

   DateFormat dateFormat = new SimpleDateFormat("M/d/YY");
   Date date = new Date();
   Calendar cal = Calendar.getInstance();
   String dateString;

   String accessToken;
   String refreshToken;

   public static final String TOKENS_FILENAME = "SchwabTokens.txt";
   public static final String PREFERENCES_FILENAME = "Preferences.txt";
   
   public boolean TradeResultsLoaded = false;

   // The number of Position panels.  It is incremented every time a new Position panel is created.
   Integer numPositionPanels = 0;

   // The maximum amount the Stock Price has moved Up and Down.
   
   Double maxHighWaterMoveUp = 0.0;
   Double maxHighWaterMoveDown = 0.0;

   // The maximum amount the Stock Price has moved Up and Down.
   
   int ProgressBarXCoordinate = 0;   // This is the same for every Progress Bar.
   int ProgressBarMaximum = 0;       // This will get updated every time thru the Update routine.
                                     // It is needed to position the High Water marks.

   // This contains all of the Position panels and widgets.
   List<JPanel> PositionPanelList = new ArrayList<JPanel>();

   // This defines the PositionPanelList index table.  It is difficult to extract the
   // Account, Symbol, and Strike from the PositionPanelList Components, so we will create
   // a corresponding index table entry for each Panel.  This makes it easier to find a
   // Position's panel.
   //
   // 8/4/2019 - I added the Target values to this table.  They need to be retained
   //            somewhere, and rather than define another similar table, I decided
   //            to add them here.
   class IndexTable
      {

      Integer Index;
      String Account;
      String Symbol;
      String Strike;

      // These values allow us to enter the price, percentage, or amount we want
      // to sell the Position for.  When a value is modified, the other two values
      // will automatically get updated.
      //
      Double TargetPrice;
      Double TargetPercentage;
      Double TargetAmount;

      // This value defines if the Current "Locked" Price.  We were having troubles with the
      // current price changing immediately prior to pressing the Sell button.  That was bad,
      // especially when we were the price changed from a nice profit to a loss.  So, we are
      // now going to be able to lock the current price prior to pressing the Sell button.
      // A value of zero means the price is NOT locked.  A non-zero price means it IS locked.
      Double CurrentLockedPrice = 0.0;
      }

   IndexTable[] PositionPanelListIndex = new IndexTable[100];   // Default to a fixed size.

   // This defines a Position.  I do NOT need the Account, Symbol, and Strike.  I can
   // create a function to extract them from the key.  However, this means we must
   // keep the current key format.  If you already have the three separate values,
   // then you should be able to populate them quickly.  I have include them here
   // in case we decide to change the key format.
   //
   // The number of Filled and Total contracts need to be initialized to zero.
   //
   // I am going to use the average Buy and Sell prices to determine if we have
   // actually bought and sold any contracts.  Initialize them to zero.  I will
   // enable the Sell buttons when the FilledContracts and the AveBuyPrice are
   // both non-zero.  I will disable the sell button when the FilledContracts is
   // zero and the AveSellPrice is non-zero.
   //
   // I assume we will have an array of these Position objects for each Account.
   //
   // I also assume that a Position will NOT get created until at least one contract
   // has been filled for a Buy order.
   //
   // I also assume that Positions will NOT get removed from the list.  Once a Position
   // is closed (sold), the number of contracts will show zero in the panel (Index.e. 0/25).
   //
   class Position
      {

      // The unique identifier in the format "AccountNumber_StockSymbol_StrikePrice" (Index.e. 865707835_STZ_197.5).
      String Identifier;

      String AccountNumber;          // 865707835
      String StockSymbol;            // STZ
      String StrikePrice;            // 197.5

      String AccountName;            // (Index.e. Brokerage, Roth, IRA, etc.) from the Preferences file.

      Double StockPriceOpen;         // The stock price when the market opened.
      Double StockPriceCurrent;      // The current stock price.

      Double MinMove;                // The minimum move from the setup table.
      Double AveMove;                // The maximum move from the setup table.

// 1/12/24      Integer MinExitTime;           // The minimum exit time from the setup table.
// 1/12/24      Integer MaxExitTime;           // The maximum exit time from the setup table.

      Double Percentage;             // The percentage profit goal from the setup table.
      //
      // Note: The value in the setup table is an integer (i.e. 25).  Store the value
      //       as a float (i.e. 0.25).  This will make the math easier, and then we will
      //       just have to multiply the value by 100 when we refresh the display widget.

      Integer FilledBuyContracts;    // The number of filled Buy contracts.
      Integer FilledSellContracts;   // The number of filled Sell contracts.
      Integer TotalContracts;        // The total number of contracts.

      Double AveStraddleBuyPrice;    // The average price we paid for the filled buy contracts.
      Double AveStraddleSellPrice;   // The average price we got for the filled sell contracts.

      Double CurrentCallsPrice;      // The current calls mark price.
      Double CurrentPutsPrice;       // The current puts mark price.
      Double CurrentStraddlePrice;   // The current straddle mark price = (CurrentCallsPrice + CurrentPutsPrice).
      Double CurrentLockedPrice;     // The current locked straddle price.

      String Expiration;            // The expiration date.  Tony needs this.

      Double High;                   // The High value of the day.
      Double Low;                    // The Low value of the .

      }

   // This initializes the table to all blanks.
   // Arrays.fill(PositionPanelListIndex, " ");

   // This defines the Swing Timer that is triggered once per second in the main GUI thread.
   // It invokes an action handler routine that updates the Monitor tab widgets.
   private Timer timer;

   // This defines the Position Action type.
   enum PositionActionType
      {
      SellStraddle,
      SellHalf,
      SellCalls,
      SellPuts,
      Cancel
      }

   // This defines the account numbers and names.  The values are set when the
   // preferences file is loaded.  Setting the values once is faster and safer
   // because the widget values can manually be modified after we start running.
   class Account_ID
      {

      String Name;
      String Number;
      }

   Account_ID[] Account_IDs = new Account_ID[9];

   // This defines the profit values.
   Double Account_Profit = 0.0;
   Double Total_Profit = 0.0;

   // This defines the currency formatter.
   NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);

   // This boolean is used to display the Position header.  I want to create it
   // only once, and I want to do it inside the Create_Position_Panel routine because
   // that routine keeps track of the order & width of each widget.  The boolean
   // is initialized to false, and then set to true when the first position is
   // created.
   boolean Position_Header_Created = false;

   
   private void Initialize_Account_Names()
      {
      }
      
   private void closeDownOtherGUIs() {
        try {
            // Send message to other processes to close down
            GUIData.setShutDown(true);
            // give it 1 sec to shut down
            Utility.hybridPrecisionWait(1000);
        } catch (Exception ex){
            LogData.LogThis(Level.INFO, "You need to manually close the FIT_Orders! \n");
        }
   }

     /**
     * Spawns a sub-application with environment-safe low-latency JVM flags.
     * Automatically adjusts parameters based on whether running on Java 23 or 25+.
     * 
     * @param subAppName The exact file name of the JAR to run (e.g., "FIT_Orders.jar")
     * @param socketPort The dynamic port number required by the application
     */
    public static void launchSubApp(String subAppName, int socketPort) {
        try {
            // 1. Detect the runtime major version (e.g., 23 or 25)
            int majorVersion = Runtime.version().feature();
            System.out.println("[INFO] Preparing sub-app launcher for Java " + majorVersion);

            // 2. Initialize the command array list
            List<String> command = new ArrayList<>();
            command.add("java");
            
            // 3. Inject standard diagnostic parameters
            command.add("-Djdk.traceVirtualThreadLocals=true");
            
            // These were placed directly into the java applications.
            // Force Log4j2 to use the external XML file next to the sub-app jar
//            command.add("-Dlog4j2.configurationFile=./log4j2.xml");            
            // Force Log4j2 to use the high-performance Async LMAX Disruptor engine
//            command.add("-Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
            
            // Generate a unique file identifier name based on the target application name
            String identifier = subAppName.replace(".jar", "");
            command.add("-Dapp.identifier=" + identifier);

            // 4. Inject strict version-specific parameters dynamically
            if (majorVersion == 23) {
                // Java 23 requires the old pinned threads tracing flag
                command.add("-Djdk.tracePinnedThreads=full");
            } else if (majorVersion >= 24) {
                // Java 24/25 dropped pinned tracing but enforces strict native access checking
                command.add("--illegal-native-access=deny");
            }

            // 5. Append the specific execution target and its operational parameters
            command.add("-jar");
            command.add(subAppName);
            command.add("-p");
            command.add(Integer.toString(socketPort));

            // 6. Build and execute the process with inherited streams
            ProcessBuilder pb = new ProcessBuilder(command);
            
            // Redirects standard outputs and errors directly to your main batch script window
            pb.inheritIO(); 
            
            Process process = pb.start();
            LogData.LogThis(Level.INFO, "[SUCCESS] Successfully spawned: " + subAppName + " on PID " + process.pid());

        } catch (Exception e) {
            LogData.LogThis(Level.ERROR, "[ERROR] Fatal exception occurred while launching sub-app: " + subAppName);
            System.out.println("[ERROR] Fatal exception occurred while launching sub-app: " + subAppName);
            e.printStackTrace();
        }
    }

    public void saveWindowPositions() {
        // Save Main Window bounds
        prefs.putInt("main_x", this.getX());
        prefs.putInt("main_y", this.getY());
        prefs.putInt("main_w", this.getWidth());
        prefs.putInt("main_h", this.getHeight());

        // Save Text Popup bounds
        if (popupFrame != null) {
            prefs.putInt("popup1_x", popupFrame.getX());
            prefs.putInt("popup1_y", popupFrame.getY());
            prefs.putInt("popup1_w", popupFrame.getWidth());
            prefs.putInt("popup1_h", popupFrame.getHeight());
        }

        // Save Table Popup bounds
        if (tablePopupWindow != null) {
            prefs.putInt("table_x", tablePopupWindow.getX());
            prefs.putInt("table_y", tablePopupWindow.getY());
            prefs.putInt("table_w", tablePopupWindow.getWidth());
            prefs.putInt("table_h", tablePopupWindow.getHeight());
        }

        // Mark that preferences have been saved at least once
        prefs.putBoolean("initialized", true);
    }    
    
    public void loadWindowPositions() {
        boolean isInitialized = prefs.getBoolean("initialized", false);

        if (!isInitialized) {
            // FIRST RUN: Do NOT resize or reposition. 
            // Let the NetBeans designer sizes take effect, but ensure the table is detached from the tab.
            if (Monitor_Panel.getParent() == Tabs_Panel) {
                Tabs_Panel.remove(Monitor_Panel);
                Tabs_Panel.revalidate();
                Tabs_Panel.repaint();
                if (tablePopupWindow != null) {
                    tablePopupWindow.add(new JScrollPane(Symbol_Data_Table));
                    tablePopupWindow.setVisible(true);
                }
            }
            if (popupFrame != null) {
//                popupFrame.setVisible(true);
            }
            return; 
        }

        // SUBSEQUENT RUNS: Load saved bounds for all 3 windows
        this.setBounds(
            prefs.getInt("main_x", this.getX()),
            prefs.getInt("main_y", this.getY()),
            prefs.getInt("main_w", this.getWidth()),
            prefs.getInt("main_h", this.getHeight())
        );

        if (popupFrame != null) {
            popupFrame.setBounds(
                prefs.getInt("popup1_x", popupFrame.getX()),
                prefs.getInt("popup1_y", popupFrame.getY()),
                prefs.getInt("popup1_w", popupFrame.getWidth()),
                prefs.getInt("popup1_h", popupFrame.getHeight())
            );
//            popupFrame.setVisible(true);
        }

        if (tablePopupWindow != null) {
            // Ensure table panel is detached from main tab if not already done
            if (Monitor_Panel.getParent() == Tabs_Panel) {
                Tabs_Panel.remove(Monitor_Panel);
                Tabs_Panel.revalidate();
                Tabs_Panel.repaint();
                tablePopupWindow.add(new JScrollPane(Symbol_Data_Table));
            }

            tablePopupWindow.setBounds(
                prefs.getInt("table_x", tablePopupWindow.getX()),
                prefs.getInt("table_y", tablePopupWindow.getY()),
                prefs.getInt("table_w", tablePopupWindow.getWidth()),
                prefs.getInt("table_h", tablePopupWindow.getHeight())
            );
            tablePopupWindow.setVisible(true);
        }
    }

    public void resizeWindowToFitPanel(javax.swing.JPanel targetPanel) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            this.setLocation(5,5);
            // Ensure components are fully laid out
            this.validate();

            // Get the absolute screen locations of both the frame and the panel
            java.awt.Point frameLocation = this.getLocationOnScreen();
            java.awt.Point panelLocation = targetPanel.getLocationOnScreen();

            // Calculate the panel's bottom edge relative to the top of the frame
            int panelBottomYRelativeToFrame = (panelLocation.y - frameLocation.y) + targetPanel.getHeight();

            // Add frame decorations (title bar height) + a comfortable buffer (e.g., 25 pixels)
            java.awt.Insets insets = this.getInsets();
            int scrollbarBuffer = 0; // 25; 

            int desiredHeight = panelBottomYRelativeToFrame + insets.top + scrollbarBuffer - 20;

            // Apply the new size
            this.setSize(this.getWidth(), desiredHeight);
            this.remove(Symbol_Data_Scroll_Pane);
            this.setResizable(false);
        });
    }    

    public void popOutTabPanel(javax.swing.JPanel panelToPopOut, String windowTitle) {
        // 1. Remove the panel from the JTabbedPane
        Tabs_Panel.remove(panelToPopOut);

        // Refresh the tabbed pane so it visually updates
        Tabs_Panel.revalidate();
        Tabs_Panel.repaint();

        // 2. Create the standalone popup JFrame
        popupFrame = new javax.swing.JFrame(windowTitle);
        popupFrame.setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);

        // 3. Add the panel to the new frame
        popupFrame.getContentPane().add(panelToPopOut);

        // 4. Set size (match the panel's current size or set a default)
        popupFrame.setSize(1400, 400);

        // 5. Position it nicely relative to your main window
        java.awt.Point mainLoc = this.getLocation();
        int pixelOffset = 40;
        popupFrame.setLocation(mainLoc.x + pixelOffset, mainLoc.y + pixelOffset);

        // 7. Make it visible
//        popupFrame.setVisible(true);
    }    
    
    public void popOutTableWindow() {
        Tabs_Panel.remove(Monitor_Panel);

        // 1. Wrap your existing JTable in a JScrollPane 
        JScrollPane scrollPane = new JScrollPane(Symbol_Data_Table);

        // 2. Create the separate window
        tablePopupWindow = new JFrame("FIT GUI Table Data");
        tablePopupWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        tablePopupWindow.setSize(1260, 450);

        // 3. Add the scroll pane
        tablePopupWindow.add(scrollPane);

        // 4. Force main window layout validation so it reports its ACTUAL current height
        this.validate();
        this.revalidate();

        // Ensure coordinate calculation runs after any pending resize events on the Event Dispatch Thread
        javax.swing.SwingUtilities.invokeLater(() -> {
            java.awt.Point mainLocation = this.getLocation();
            int mainHeight = this.getHeight();

            int tableX = mainLocation.x;                  // Left-aligned with main window
            int tableY = mainLocation.y + mainHeight + 10;  // Below resized main window

            tablePopupWindow.setLocation(tableX, tableY);
            tablePopupWindow.setVisible(true);
        });
    }

    public void updateTableWindowTitle(String newTitle) {
        if (tablePopupWindow != null && tablePopupWindow.isVisible()) {
            SwingUtilities.invokeLater(() -> {
                tablePopupWindow.setTitle(newTitle);
            });
        }
    }

    /**
    Creates new form OptionsTradingGUI
    */
   public GUI() {
        initComponents();
        LogData.LogThis(Level.INFO, "FIT_GUI starting up ...");

        try {
           File file = new File("FIT_GUI.jar");
           Path filePath = file.toPath();
           BasicFileAttributes attributes = null;
           attributes = Files.readAttributes(filePath, BasicFileAttributes.class);
           String buildDateTime = attributes.creationTime().toString();
           String buildDate = buildDateTime.substring(0,10) + " " + buildDateTime.substring(11, 19);  // YYYY-MM-DDThh:mm:ss[.s+]Z
           this.setTitle("FIT GUI Options Trading Platform - Build Date: " + buildDate);
        } catch (IOException exception) {
            System.out.println("Exception handled when trying to get file " + "attributes: " + exception.getMessage());
        }
      
        // Set the GUI size to use the full screen.
        Toolkit tk = Toolkit.getDefaultToolkit();
        int Width = (int) tk.getScreenSize().getWidth();
        int Height = (int) tk.getScreenSize().getHeight();
        //     this.setSize(Width, Height);
        //LogData.LogThis(Level.INFO, "Width  = " + Width);
        //LogData.LogThis(Level.INFO, "Height = " + Height);

        //dateString = dateFormat.format(date);
        //DateTextField.setText(dateString);
  //      Load_Config( );  // This just doesn't work.  Unable to load config file outside this module.  Must pull it all into this module.
        LoadPreferences(false);
      
        // Update the Monitor tab Account ID labels.  If an account name exists, then
        // set it, otherwise make the widgets invisible.
        for (int i = 0; i < 5; i++)
            {

            switch (i)
                {
                case 0:

                    if (Account_IDs[i] == null)
                        {
                        Account_1_Profit_Label.setVisible(false);
                        Account_1_Profit_TextField.setVisible(false);
                        }
                    else
                        {
                        Account_1_Profit_Label.setText(Account_IDs[i].Name);
                        }

                    break;

                case 1:

                    if (Account_IDs[i] == null)
                        {
                        Account_2_Profit_Label.setVisible(false);
                        Account_2_Profit_TextField.setVisible(false);
                        }
                    else
                        {
                        Account_2_Profit_Label.setText(Account_IDs[i].Name);
                        }

                    break;

                case 2:

                    if (Account_IDs[i] == null)
                        {
                        Account_3_Profit_Label.setVisible(false);
                        Account_3_Profit_TextField.setVisible(false);
                        }
                    else
                        {
                        Account_3_Profit_Label.setText(Account_IDs[i].Name);
                        }

                    break;

                case 3:

                    if (Account_IDs[i] == null)
                        {
                        Account_4_Profit_Label.setVisible(false);
                        Account_4_Profit_TextField.setVisible(false);
                        }
                    else
                        {
                        Account_4_Profit_Label.setText(Account_IDs[i].Name);
                        }

                    break;

                case 4:

                    if (Account_IDs[i] == null)
                        {
                        Account_5_Profit_Label.setVisible(false);
                        Account_5_Profit_TextField.setVisible(false);
                        }
                    else
                        {
                        Account_5_Profit_Label.setText(Account_IDs[i].Name);
                        }

                    break;

                        }

                        }

        ClearTable( true );

        // Setup Accounts_Table columns (except symbolLabel column) with default values to make life easier for entry;
        for (int ccc = 0; ccc < Accounts_Table.getRowCount(); ccc++)
           {
           for (int ddd = 0; ddd < Accounts_Table.getColumnCount(); ddd++)
              {
              if (Accounts_Table.getValueAt(ccc, ddd) == null)
                 {  // Don't overwrite anything that is already set somehow
                 switch (ddd)
                    {
                    case GUIData.accountNameColumn:
                       Accounts_Table.setValueAt(accountNameDefault, ccc, GUIData.accountNameColumn);
                       break;
                    case GUIData.accountNumberColumn:
                       Accounts_Table.setValueAt(accountNumberDefault, ccc, GUIData.accountNumberColumn);
                       break;
                    case GUIData.maxTradesColumn:
                       Accounts_Table.setValueAt(maxTradesDefault, ccc, GUIData.maxTradesColumn);
                       break;
                    case GUIData.maxFundsColumn:
                       Accounts_Table.setValueAt(maxFundsDefault, ccc, GUIData.maxFundsColumn);
                       break;
                    case GUIData.brokerNameColumn:
                       Accounts_Table.setValueAt(brokerNameDefault, ccc, GUIData.brokerNameColumn);
                       break;
                    default:
                       Accounts_Table.setValueAt("", ccc, ddd);
                       break;
                    }  // Switch
                 }   // If
              }  // for ddd
           } // for ccc

        Load_Date_Text_Field.setText(new SimpleDateFormat("M/d").format(new Date()));

        // Deal with processing buttons            
        Schwab_Login_Finish_Button.setEnabled(false);
        Kill_Open_Straddle_Orders_Button.setEnabled(false);
        Start_Order_Processing_Button.setEnabled(false);
        GUI_Only_Processing_Button.setEnabled(false);
        Quotes_Only_Processing_Button.setEnabled(false);
        GoogleGenDayData.setEnabled(false);
        SchwabAPI.LoadToken();        
        LogData.LogThis( Level.INFO, " Done with loadtoken");
        SchwabAPI.setAppKey(AppKey_Text_Field.getText());
        LogData.LogThis( Level.INFO, " Done with appkey");
        SchwabAPI.setSecret(String.valueOf(Secret_Field.getPassword()));
        LogData.LogThis( Level.INFO, " Done with setsecret");
        TT_API.setEnvironment(TT_API.production_URL);
        LogData.LogThis( Level.INFO, " Done with production URL");
        if (TT_API.LoadTokens() == true) {
            LogData.LogThis( Level.INFO, " LoadTokens = true");
            if (TT_API.getAccessToken().equalsIgnoreCase("") == true) {
                LogData.LogThis( Level.INFO, " Something went wrong");
                // Something went wrong.
            } else {
                TT_Access_Token_Text_Field.setText(TT_API.getAccessToken());
                TT_Refresh_Token_Text_Field.setText(TT_API.getRefreshToken());
                ClientSecret_Text_Field.setText(TT_API.getClientSecret());
                LogData.LogThis( Level.INFO, " Done with Tasty Access Token");
                TT_Login_Button.setEnabled(false);
                LogData.LogThis( Level.INFO, " login enabled");
                updateGUIs();
                LogData.LogThis( Level.INFO, " Done with update GUIs");
            }
        }
        LoadTable(false);
        LogData.LogThis( Level.INFO, " Done with loadtable");
        GUIData.StartServerSockets( Results_Text_Area );
        LogData.LogThis( Level.INFO, " Done with setserversockets");

        this.setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                saveWindowPositions();
                System.exit(0); // Or your graceful shutdown routine
            }
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                // Apply saved positions AFTER the windows are fully visible to the OS
                loadWindowPositions();

                // Bring the window that triggered the event to the front
                ((javax.swing.JFrame) e.getSource()).toFront();            }
        });
    
        // Adding in this listener here so can close out the orders GUI when this window is closed.
        addWindowListener(new java.awt.event.WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                  // Send a Shut down message to the orders GUI.
                  closeDownOtherGUIs();
              }
        });
        LogData.LogThis( Level.INFO, " Done with addwindowlistener");
        String currentDirectoryPath = System.getProperty("user.dir");
        LogData.LogThis(Level.INFO, "Current Directory Path = " + currentDirectoryPath);
        File currentDirectory = new File(currentDirectoryPath);
        try  {
            while (GUIData.getOrdersSocketInitDone() == false) {
                try {
                    Utility.hybridPrecisionWait(1000);
                } catch (Exception e) {                    
                    this.Results_Text_Area.append( "Waiting for Orders Socket Init!!\n" );
                    LogData.LogThis(Level.INFO, "Waiting for Orders Socket Init! \n");
                }
            }
            // There is the possibility that ports were not available for FIT Orders and Quotes processes to connect.
            //  So to account for that we will instantiate those processes with the port number used as an argument into the application.
            // Setup the command to kick off the orders process
            // 8/3/26 - replace with below   ProcessBuilder pb = new ProcessBuilder("java","-jar", "FIT_Orders.jar", "-p", Integer.toString(Config.SchwabOrdersSocketNumber));
            launchSubApp( "FIT_Orders.jar", Config.SchwabOrdersSocketNumber);
        } catch (Exception e) {
            this.Results_Text_Area.append( "You need to manually start the FIT_Orders!!\n" );
            LogData.LogThis(Level.INFO, "You need to manually start the FIT_Orders! \n");
        }
       //Create the Swing Timer to run once per every a second.
        timer = new Timer(1000, new ActionListener()
           {
           public void actionPerformed(ActionEvent evt) {
                  try {
                      if (Kill_Open_Orders_Check_Box.isSelected() != prevKillOpenStraddleOrdersSelected) {
                          // Need to let orders processing know current status.  
                          // User could uncheck this and not save preferences.  
                          // So forcing the save here causing the notification.
                          SavePreferences();
                          prevKillOpenStraddleOrdersSelected = Kill_Open_Orders_Check_Box.isSelected();
                      }
                      DateTimeFormatter dtfOpenTime = DateTimeFormatter.ofPattern("HH:mm");
                      LocalTime theOpenTime = LocalTime.parse( OpenTimeTextField.getText(), dtfOpenTime );
                      if ( LocalTime.now().isAfter( theOpenTime ) ) {
                              Update_Monitor_Tab();
                      }
                      if (TT_API.checkTimeToUpdateTokens() == true) {
                          // update AT for TT
                          TT_Access_Token_Text_Field.setText(TT_API.getAccessToken());
                      } 
                      if (SchwabAPI.checkTimeToUpdateTokens() == true) {
                         // update AT for Schwab
                         Access_Token_Text_Field.setText(SchwabAPI.getAccessToken());
                      }  
                      // Check for auto shut down here
                      if (autoShutDown == true) {
                          if ( LocalTime.now().isAfter( shutDownTime ) ) {
                              closeDownOtherGUIs();
                              System.exit( 0 );
                          }
                      }
                  } catch (Exception ex) {
                      Utility.dumpExceptionInfo(ex, "FIT_GUI Timer");
                      ex.printStackTrace();
                  }
              }
           });      
        // Start the Swing Timer.
        timer.start();
        LogData.LogThis(Level.INFO, "FIT_GUI has initialized");
        if (autoLogin == true) {
            ActionEvent evt = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "LoginButton");
            this.Schwab_Login_Start_ButtonActionPerformed(evt);
        }
        if (autoProcess == true) {
            ActionEvent evt = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "StartProcessingButton");
            this.Start_Order_Processing_ButtonActionPerformed(evt);
        }
        if (autoGUIOnly == true) {
            ActionEvent evt = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "StartProcessingButton");
            this.GUI_Only_Processing_ButtonActionPerformed(evt);
        }
        Auto_Shut_Down_Check_Box.setSelected(autoShutDown);
        resizeWindowToFitPanel(Login_Panel);
        popOutTableWindow();
        popOutTabPanel(Monitor_Panel, "Positions ... ");
        loadWindowPositions();
        initialLoadComplete = true;
    }

// SOMEDAY I WILL UNDERSTAND WHY THIS WON"T LET ME DO THIS.   
    private void Load_Config( ) {

        File file = new File(Config.configFileName);
        try {
            // Read the file.
            Scanner inputStream = new Scanner( file );

            // Loop until the end of the file.
            while (inputStream.hasNext())            {
                String data = inputStream.nextLine();     // Read the next line.
                String[] values = data.split(",");        // Get the values from the line.
                Config.Set_Value(values[0], values[1]);
            }
            inputStream.close();
        } catch (Exception ex) {
           Results_Text_Area.append("FYI ... No Config File processed ...   \n");
           Results_Text_Area.append("FYI ... Default values will be used for all config values.  \n");
           Utility.dumpExceptionInfo(ex);
           ex.printStackTrace();
            
        }
    }

   private String getSpeed()
      {
      int j = jComboBoxQuoteSpeed.getSelectedIndex();
      String Speed = "";
      switch (j)
         {
         case 0:
            Speed = "1";
            break;
         case 1:
            Speed = "1.5";
            break;
         case 2:
            Speed = "3";
            break;
         default:
            Speed = "5";
            break;
         }  // Switch
      return Speed;
      }

   private String getFileName( String currentValue ) {
       String returnValue = currentValue;
       
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            returnValue = selectedFile.getAbsolutePath();
        }
        return returnValue;
   }

   // Save the program preferences to a file
    private void SavePreferences() {
        if (!loadingPrefs) {
      try
         {
         // Save the Access and Refresh tokens to a file so we can load then later.
         // This will allow us to bypass the login process ... providing the refresh
         // token has not expired.
         PrintWriter tokensWriter = new PrintWriter(PREFERENCES_FILENAME);
         tokensWriter.println("TT User Data=" + TT_Refresh_Token_Text_Field.getText() + " + " + String.valueOf(ClientSecret_Text_Field.getPassword()) );
         tokensWriter.println("Schwab User ID=" + AppKey_Text_Field.getText() + " + " + String.valueOf(Secret_Field.getPassword()));
         tokensWriter.println("Schwab RedirectURI=" + Redirect_URI_TextField.getText());
         tokensWriter.println("Max Price=" + Double.toString(maxPriceDefault));
         tokensWriter.println("Trade Amount=" + Double.toString(tradeAmountDefault));
         tokensWriter.println("Buy Wiggle=" + Double.toString(buyWiggleDefault));
         tokensWriter.println("Min Move=" + Double.toString(minMoveDefault));
         tokensWriter.println("Ave Move=" + Double.toString(aveMoveDefault));
         tokensWriter.println("EOW Min=" + Double.toString(EOWMinDefault));
         tokensWriter.println("EOW Ave=" + Double.toString(EOWAveDefault));
         tokensWriter.println("Percent Goal=" + Double.toString(percentGoalDefault));
         tokensWriter.println("Chase Amount=" + Double.toString(chaseAmountDefault));
         tokensWriter.println("Chase Wait Time=" + Integer.toString(chaseWaitTimeDefault));
         tokensWriter.println("Open Time=" + OpenTimeTextField.getText());
         tokensWriter.println("Quote Speed=" + getSpeed());
         tokensWriter.println("Strikes=" + Number_Of_Strikes_Text_Field.getText());
         tokensWriter.println("Duration=" + Capture_Duration_Text_Field.getText());
         tokensWriter.println("Minutes B4 Open=" + StartMinutesB4Open.getText());
         tokensWriter.println("Kill Open=" + Boolean.toString(Kill_Open_Orders_Check_Box.isSelected()));
         tokensWriter.println("Order Speed=" + OrderProcessingSpeed.getText() );
         tokensWriter.println("Log More=" + Boolean.toString(Log_More_Data_Check_Box.isSelected()));
         tokensWriter.println("Initial Sell 2 orders=" + Boolean.toString(Initial_Sell_2_Orders_Check_Box.isSelected()));
         tokensWriter.println("Google Spreadsheet Title=" + Google_Spreadsheet_Name_Label.getText() );
         tokensWriter.println("Google Spreadsheet ID=" + Google_Spreadsheet_ID_Text_Field.getText() );
         tokensWriter.println("Download Tab Name=" + Download_Tab_Name_Text_Field.getText() );
         tokensWriter.println("Gen Prices Tab Name=" + Gen_Prices_Tab_Name_Text_Field.getText() );
         for (int abc = 0; abc < countAccountsEntered(GUIData.allBrokers); abc++)
            {
            tokensWriter.println("Account" + Integer.toString(abc+1) + "=" 
               + Accounts_Table.getValueAt(abc, GUIData.accountNameColumn).toString() + ","
               + Accounts_Table.getValueAt(abc, GUIData.accountNumberColumn).toString() + ","
               + Accounts_Table.getValueAt(abc, GUIData.maxTradesColumn).toString() + ","
               + Accounts_Table.getValueAt(abc, GUIData.maxFundsColumn).toString() + ","
               + Accounts_Table.getValueAt(abc, GUIData.brokerNameColumn).toString());
            }
         tokensWriter.close();
         }
      catch (Exception ex)
         {
         Utility.dumpExceptionInfo(ex);
         ex.printStackTrace();
         }
      try {
           while ( !GUIData.LockOrderSocketData() ) { Utility.hybridPrecisionWait(10);}
           GUIData.SetOrdersSocketData( GUIData.LoadPrefs );
        } catch (Exception e) {
            Utility.dumpExceptionInfo(e);
            e.printStackTrace();
      }
        } else {
            // Prefs are loading so skip the save command for now.
      }
    }

   // load the program preferences from a file
   private void LoadPreferences(boolean getName)
      {
          loadingPrefs = true;
      try
         {
         String fileNameToUse = PREFERENCES_FILENAME;
         if (getName == true) {
             fileNameToUse = getFileName( PREFERENCES_FILENAME );
         }
         File PrefsFile = new File(fileNameToUse);
         if (PrefsFile.exists())
            {
            Scanner inputStream = new Scanner(PrefsFile);  // Read the file.
            Integer accountRow = 0;
            Integer row = 0;
            while (inputStream.hasNext()) {
                String data = inputStream.nextLine();     // Read the next line.
                String[] values = data.split("=");        // Get the values from the line.

                LogData.LogThis(Level.INFO, "Row = " + Integer.toString(row+1) + ", data = " + data);
                switch (values[0]) {
                        case "TT User Data":
                         String[] split2 = values[1].split("\\ \\+\\ ");
                         if (split2.length > 1) {
                            TT_Refresh_Token_Text_Field.setText(split2[0]);
                            TT_API.setRefreshToken(split2[0]);
                            ClientSecret_Text_Field.setText(split2[1]);
                            TT_API.setClientSecret(split2[1]);
                         }
                         break;
                    case "Schwab User ID":
                        String[] split3 = values[1].split("\\ \\+\\ ");
                        AppKey_Text_Field.setText(split3[0]);
                        SchwabAPI.setAppKey(split3[0]);
                        Secret_Field.setText(split3[1]);
                        SchwabAPI.setSecret(split3[1]);
                        break;
                    case "Schwab RedirectURI":
                        Redirect_URI_TextField.setText(values[1]);
                        SchwabAPI.setRedirectURI(values[1]);
                        break;
                    case "Max Price":
                        maxPriceDefault = Double.parseDouble(values[1]);
                        break;
                    case "Trade Amount":
                        tradeAmountDefault = Double.parseDouble(values[1]);
                        break;
                    case "Buy Wiggle":
                        buyWiggleDefault = Double.parseDouble(values[1]);
                        break;
                    case "Min Move":
                        minMoveDefault = Double.parseDouble(values[1]);
                        break;
                    case "Ave Move":
                        aveMoveDefault = Double.parseDouble(values[1]);
                        break;
                    case "EOW Min":
                        EOWMinDefault = Double.parseDouble(values[1]);
                        break;
                    case "EOW Ave":
                        EOWAveDefault = Double.parseDouble(values[1]);
                        break;
                    case "Percent Goal":
                        percentGoalDefault = Double.parseDouble(values[1]);
                        break;
                    case "Chase Amount":
                        chaseAmountDefault = Double.parseDouble(values[1]);
                        break;
                    case "Chase Wait Time":
                        chaseWaitTimeDefault = Integer.parseInt(values[1]);
                        break;
                    case "Open Time":
                        Open_Time = LocalTime.parse(values[1]);
                        OpenTimeTextField.setText(values[1]);
                        break;
                    case "Quote Speed":
                        this.jComboBoxQuoteSpeed.setSelectedItem(values[1]);
                        break;
                    case "Strikes":
                        Number_Of_Strikes_Text_Field.setText(values[1]);
                        break;
                    case "Duration":
                        Capture_Duration_Text_Field.setText(values[1]);
                        break;
                    case "Minutes B4 Open":
                        StartMinutesB4Open.setText(values[1]);
                        break;
                    case "Kill Open":
                        Kill_Open_Orders_Check_Box.setSelected(values[1].equalsIgnoreCase("true"));
                        prevKillOpenStraddleOrdersSelected = Kill_Open_Orders_Check_Box.isSelected();
                        break;
                    case "Order Speed":
                        OrderProcessingSpeed.setText(values[1]);
                        break;
                    case "Log More":
                        Log_More_Data_Check_Box.setSelected(values[1].equalsIgnoreCase("true"));
                        break;
                    case "Initial Sell 2 orders":
                        Initial_Sell_2_Orders_Check_Box.setSelected(values[1].equalsIgnoreCase("true"));
                        break;
                    case "Google Spreadsheet Title":
                        Google_Spreadsheet_Name_Label.setText( values[1] );
                        break;
                    case "Google Spreadsheet ID":
                        GUIData.setGoogleSpreadsheetID( values[1] );
                        Google_Spreadsheet_ID_Text_Field.setText( values[1] );
                        // Update the spreadsheet name only if not already read in from preferences.  
                        //  Want to make sure the Title is first.
                        if (initialLoadComplete) {
                            if ( Google_Spreadsheet_Name_Label.getText().contains("Spreadsheet Name")) {
                                Google_Spreadsheet_Name_Label.setText( GoogleOperations.getSpreadsheetTitle( ) );
                            }
                        }
                        break;
                    case "Download Tab Name":
                        Download_Tab_Name_Text_Field.setText( values[1] );
                        break;
                    case "Gen Prices Tab Name":
                        Gen_Prices_Tab_Name_Text_Field.setText( values[1] );
                        break;
                    // Possible future preferences
                    case "Minutes To Run This Row":
                    case "Just Get In This Row":
                    case "Blue Line This Row":
                    case "Number Of Strikes This Row":
                    default:
                        // Let's see if this is for Accounts and if so process else there is something wrong
                        if (values[0].contains("Account")) {
                            String dataToUse = data;
                            int splitStart = 1;
                            if (values[0].contains("Accounts")) {
                                // Old format   "Accounts,<name>,<acct num>,<# trades>,<$ amt>,<broker>"                                
                                // values to use set above if statement, just need which account it is
                            } else {
                                // New format  "Account<number>=<name>,<acct num>,<# trades>,<$ amt>,<broker>"
                                dataToUse = values[1];
                                splitStart = 0;
                                int accountNumber = Integer.parseInt( values[0].substring(7) );
                                // Set the accountRow accordingly
                                accountRow = accountNumber - 1;
                            }
                            String[] AcctInfo = dataToUse.split(",");
                            // Save a local copy of the account values.
                            Account_IDs[accountRow] = new Account_ID();
                            Account_IDs[accountRow].Name = AcctInfo[splitStart];
                            Account_IDs[accountRow].Number = AcctInfo[splitStart+1];

                            // Update the account values in the table widget.
                            Accounts_Table.setValueAt(AcctInfo[splitStart], accountRow, GUIData.accountNameColumn);
                            Accounts_Table.setValueAt(AcctInfo[splitStart+1], accountRow, GUIData.accountNumberColumn);
                            Accounts_Table.setValueAt(AcctInfo[splitStart+2], accountRow, GUIData.maxTradesColumn);
                            Accounts_Table.setValueAt(AcctInfo[splitStart+3], accountRow, GUIData.maxFundsColumn);
                            Accounts_Table.setValueAt(AcctInfo[splitStart+4], accountRow, GUIData.brokerNameColumn);
                            accountRow++;
                        } else {
                           LogData.LogThis(Level.INFO, "Preferences line = " + data + " is not usable!");
                        }
                        break;
                }
            }
            inputStream.close();
            }
            loadingPrefs = false;
         }
      catch (Exception ex)
         {
         Utility.dumpExceptionInfo(ex);
         ex.printStackTrace();
         }

      // TBD Below for Mike  10/8/19
      // Save the Account names and numbers.
      //            Account_ID_1.Name = values[2];
      //            Account_ID_1.Number = values[3];
      //            Account_ID_2.Name = values[4];
      //            Account_ID_2.Number = values[5];
      //            Account_IDs[0] = Account_ID_1;
      //            Account_IDs[1] = Account_ID_2;
      }

   // This function returns an array of strings.  Each string represents an
   // Account number (i.e. 492395315).
   public String[][] Get_Account_Numbers()
      {
      return GUIData.getAccounts();
      }

   // This function determines if an Position Panel exists for the given identifier.
   public boolean Exists(String Position_ID)
      {
      boolean Found = false;

      for (int i = 0; i < numPositionPanels; i++)
         {
         IndexTable index = PositionPanelListIndex[i];

         if (Position_ID.equals(PositionPanelListIndex[i].Account + "_" + PositionPanelListIndex[i].Symbol + "_" + PositionPanelListIndex[i].Strike))
            {
            Found = true;
            break;
            }
         }
      return Found;
      }

   // This function returns the Account Name for the given Account Number.
   public String Get_Account_Name(String Account_Number)
      {
      if (Account_Number.equals(Account_IDs[0].Number))
         {
         return Account_IDs[0].Name;
         }
      else if (Account_Number.equals(Account_IDs[1].Number))
         {
         return Account_IDs[1].Name;
         }
      else if (Account_Number.equals(Account_IDs[2].Number))
         {
         return Account_IDs[2].Name;
         }
      else if (Account_Number.equals(Account_IDs[3].Number))
         {
         return Account_IDs[3].Name;
         }
      else if (Account_Number.equals(Account_IDs[4].Number))
         {
         return Account_IDs[4].Name;
         }
/*
      else if (Account_Number.equals(Account_IDs[5].Number))
         {
         return Account_IDs[5].Name;
         }
      else if (Account_Number.equals(Account_IDs[6].Number))
         {
         return Account_IDs[6].Name;
         }
      else if (Account_Number.equals(Account_IDs[7].Number))
         {
         return Account_IDs[7].Name;
         }
      else if (Account_Number.equals(Account_IDs[8].Number))
         {
         return Account_IDs[8].Name;
         }
*/
      else
         {
         LogData.LogThis(Level.INFO, "Invalid Account_Number = " + Account_Number);
         return "";
         }
      }

   // This function returns the Position object for the given inputs.
   synchronized public Position Get_Position(String Account_Number, String Stock_Symbol, String Strike_Price)
      {
      // Get the Positions Data from Tony for the current Account.
//      String[] PositionsStrings = TT_PositionsData.GetPositionsData(Account_Number);
      String[] PositionsStrings = PositionsData.getPositions(Account_Number);

      if (PositionsStrings == null)
         {
         return null;
         }
      else
         {
         Position position = new Position();

         for (int i = 0; i < PositionsStrings.length; i++)
            {
            String[] PositionValues = PositionsStrings[i].split(";");

            String OrderKey = PositionValues[0];
            String Symbol = PositionValues[2];
            String Strike = PositionValues[3];

            if (Symbol.equals(Stock_Symbol) && Strike.equals(Strike_Price))
               {
               String BuyAmount = PositionValues[4];
               String ContractsFilledBought = PositionValues[5];
               String ContractsTotal = PositionValues[6];
               String StockPriceOpen = PositionValues[7];
               String StockPriceCurrent = PositionValues[8];
               String StraddleMark = PositionValues[9];
               String CallsCurrentPrice = PositionValues[10];
               String PutsCurrentPrice = PositionValues[11];
               String SellAmount = PositionValues[12];
               String ContractsFilledSold = PositionValues[13];
               String Expiration = PositionValues[14];
               String High = PositionValues[15];
               String Low = PositionValues[16];

               // Get the unique Identifier.
               position.Identifier = OrderKey;

               position.AccountNumber = Account_Number;
               position.StockSymbol = Stock_Symbol;
               position.StrikePrice = Strike_Price;

               // Get the Account Name from the Account ID table.
               position.AccountName = Get_Account_Name(position.AccountNumber);

               // Get the Opening & Current stock prices.
               position.StockPriceOpen = Double.valueOf(StockPriceOpen);
               position.StockPriceCurrent = Double.valueOf(StockPriceCurrent);

               // Get the Min Move, Ave Move, Min Time, Max Time, and Percentage
               // values for the current stock from the Setup table.
               //
               String SetupValuesString = Get_Setup_Values(position.StockSymbol);

               String[] SetupValues = SetupValuesString.split(",");

               position.MinMove = Double.valueOf(SetupValues[0]);
               position.AveMove = Double.valueOf(SetupValues[1]);

//  1/12/24               position.MinExitTime = Integer.valueOf(SetupValues[2]);
//  1/12/24               position.MaxExitTime = Integer.valueOf(SetupValues[3]);

               position.Percentage = (Double.valueOf(SetupValues[4]) / 100.0);

               // Get the number of Filled & Total contracts.
               position.FilledBuyContracts = Integer.valueOf(ContractsFilledBought);
               position.FilledSellContracts = Integer.valueOf(ContractsFilledSold);
               position.TotalContracts = Integer.valueOf(ContractsTotal);

               // Get the average prices we paid & got for the straddle.
               position.AveStraddleBuyPrice = (Double.valueOf(BuyAmount) / Double.valueOf(ContractsFilledBought)) / 100.0;
               position.AveStraddleSellPrice = (Double.valueOf(SellAmount) / Double.valueOf(ContractsFilledSold)) / 100.0;

               // Get the current mark prices for the Calls & Puts and for the straddle.
               position.CurrentCallsPrice = Double.valueOf(CallsCurrentPrice);
               position.CurrentPutsPrice = Double.valueOf(PutsCurrentPrice);

               position.CurrentStraddlePrice = position.CurrentCallsPrice + position.CurrentPutsPrice;

               // Get the Expiration date.
               //
               position.Expiration = Expiration;

               position.High = Double.valueOf(High);
               position.Low = Double.valueOf(Low);

//  TBD - To be removed, with improvements in program will try and send updates every 1 second now.               
//               position = updateQuoteItems( position );

               break;

               }
            }

         return position;
         }
      }

   // This function returns all of the Position objects for the given Account Number.
   synchronized public Position[] Get_Positions(String Account_Number)
      {
      LogData.LogThis(Level.INFO, "Get_Positions - Account_Number = " + Account_Number);

      // Get the Positions Data from Tony for the current Account.
//      String[] PositionsStrings = TT_PositionsData.GetPositionsData(Account_Number);
      String[] PositionsStrings = PositionsData.getPositions(Account_Number);

      if (PositionsStrings == null)
         {
         return null;
         }
      else
         {
         LogData.LogThis(Level.INFO, "Printing Strings Returned from StreamingData for Positions...");
         for (int cntr = 0; cntr < PositionsStrings.length; cntr++)
            {
            LogData.LogThis(Level.INFO, PositionsStrings[cntr]);
            }
         LogData.LogThis(Level.INFO, "No More Strings");

//         // Begin - Initialize test data.
//         Position[] Positions = new Position[1];
//         
//         for (int i = 0; i < 1; i++)
//            {
//            Positions[i] = new Position();
//            
//            Positions[i].Identifier = "NTAP";
//            Positions[i].AccountNumber = "492395315";
//            Positions[i].StockSymbol = "NTAP";
//            Positions[i].StrikePrice = "99";
//            Positions[i].AccountName = "Roth";
//            Positions[i].StockPriceOpen = 25.0;
//            Positions[i].StockPriceCurrent = 26.55;
//            Positions[i].MinMove = 1.25;
//            Positions[i].AveMove = 2.1;
//            Positions[i].MinExitTime = 5;
//            Positions[i].MaxExitTime = 30;
//            Positions[i].Percentage = 0.25;
//            Positions[i].FilledBuyContracts = 50;
//            Positions[i].FilledSellContracts = 0;
//            Positions[i].TotalContracts = 50;
//            Positions[i].AveStraddleBuyPrice = 0.75;
//            Positions[i].AveStraddleSellPrice = 0.0;
//            Positions[i].CurrentCallsPrice = 0.88;
//            Positions[i].CurrentPutsPrice = 0.02;
//            Positions[i].CurrentStraddlePrice = Positions[i].CurrentCallsPrice + Positions[i].CurrentPutsPrice;
//            Positions[i].Expiration = "31JAN20";
//
//            }
//         
//         // End - Initialize test data.
         
         // Create the array of Position objects.
         Position[] Positions = new Position[PositionsStrings.length];

         // Convert each Position string to a Position object.
         for (int i = 0; i < PositionsStrings.length; i++)
            {
            String[] PositionValues = PositionsStrings[i].split(";");

            // 0 = <Order_Key>;
            // 1 = <account number>;
            // 2 = <symbol>;
            // 3 = <strike>;
            // 4 = <buy currentAmount>;
            // 5 = <contracts filled - bought>;
            // 6 = <contracts total>;
            // 7 = <stock price open>;
            // 8 = <stock price current>;
            // 9 = <straddle mark>;
            // 10 = <calls current price>;
            // 11 = <puts current price>;
            // 12 = <sell currentAmount>;
            // 13 = <contracts filled - sold>
            // 14 = <expiration>
            // 15 = <high>
            // 16 = <low>
            //
            String OrderKey = PositionValues[0];
            String AccountNumber = PositionValues[1];
            String Symbol = PositionValues[2];
            String Strike = PositionValues[3];
            String BuyAmount = PositionValues[4];
            String ContractsFilledBought = PositionValues[5];
            String ContractsTotal = PositionValues[6];
            String StockPriceOpen = PositionValues[7];
            String StockPriceCurrent = PositionValues[8];
            String StraddleMark = PositionValues[9];
            String CallsCurrentPrice = PositionValues[10];
            String PutsCurrentPrice = PositionValues[11];
            String SellAmount = PositionValues[12];
            String ContractsFilledSold = PositionValues[13];
            String Expiration = PositionValues[14];
            String High = PositionValues[15];
            String Low = PositionValues[16];

            Positions[i] = new Position();

            // Get the unique Identifier.
            Positions[i].Identifier = OrderKey;

            // Get the individual Identifier values.
            String[] IdentifierValues = Positions[i].Identifier.split("_");

            Positions[i].AccountNumber = IdentifierValues[0];
            Positions[i].StockSymbol = IdentifierValues[1];
            Positions[i].StrikePrice = IdentifierValues[2];

            LogData.LogThis(Level.INFO, "Positions[i].AccountNumber = " + Positions[i].AccountNumber);
            LogData.LogThis(Level.INFO, "Positions[i].StockSymbol   = " + Positions[i].StockSymbol);
            LogData.LogThis(Level.INFO, "Positions[i].StrikePrice   = " + Positions[i].StrikePrice);

            // Get the Account Name from the Account ID table.
            Positions[i].AccountName = Get_Account_Name(Positions[i].AccountNumber);

            LogData.LogThis(Level.INFO, "Positions[i].AccountName = " + Positions[i].AccountName);

            // Get the Opening & Current stock prices.
            Positions[i].StockPriceOpen = Double.valueOf(StockPriceOpen);
            Positions[i].StockPriceCurrent = Double.valueOf(StockPriceCurrent);

            LogData.LogThis(Level.INFO, "Positions[i].StockPriceOpen    = " + String.valueOf(Positions[i].StockPriceOpen));
            LogData.LogThis(Level.INFO, "Positions[i].StockPriceCurrent = " + String.valueOf(Positions[i].StockPriceCurrent));

            // Get the Min Move, Ave Move, Min Time, Max Time, and Percentage
            // values for the current stock from the Setup table.
            //
            String SetupValuesString = Get_Setup_Values(Positions[i].StockSymbol);

            String[] SetupValues = SetupValuesString.split(",");

            Positions[i].MinMove = Double.valueOf(SetupValues[0]);
            Positions[i].AveMove = Double.valueOf(SetupValues[1]);

//  1/12/24          Positions[i].MinExitTime = Integer.valueOf(SetupValues[2]);
//  1/12/24          Positions[i].MaxExitTime = Integer.valueOf(SetupValues[3]);

            Positions[i].Percentage = (Double.valueOf(SetupValues[4]) / 100.0);

            LogData.LogThis(Level.INFO, "Positions[i].MinMove      = " + String.valueOf(Positions[i].MinMove));
            LogData.LogThis(Level.INFO, "Positions[i].AveMove      = " + String.valueOf(Positions[i].AveMove));
// 1/12/24            LogData.LogThis(Level.INFO, "Positions[i].MinExitTime  = " + String.valueOf(Positions[i].MinExitTime));
// 1/12/24            LogData.LogThis(Level.INFO, "Positions[i].MaxExitTime  = " + String.valueOf(Positions[i].MaxExitTime));
            LogData.LogThis(Level.INFO, "Positions[i].Percentage   = " + String.valueOf(Positions[i].Percentage));

            // Get the number of Filled & Total contracts.
            Positions[i].FilledBuyContracts = Integer.valueOf(ContractsFilledBought);
            Positions[i].FilledSellContracts = Integer.valueOf(ContractsFilledSold);
            Positions[i].TotalContracts = Integer.valueOf(ContractsTotal);

            LogData.LogThis(Level.INFO, "Positions[i].FilledBuyContracts  = " + String.valueOf(Positions[i].FilledBuyContracts));
            LogData.LogThis(Level.INFO, "Positions[i].FilledSoldContracts = " + String.valueOf(Positions[i].FilledSellContracts));
            LogData.LogThis(Level.INFO, "Positions[i].TotalContracts      = " + String.valueOf(Positions[i].TotalContracts));

            // Get the average prices we paid & got for the straddle.
            Positions[i].AveStraddleBuyPrice = (Double.valueOf(BuyAmount) / Double.valueOf(ContractsFilledBought)) / 100.0;
            Positions[i].AveStraddleSellPrice = (Double.valueOf(SellAmount) / Double.valueOf(ContractsFilledSold)) / 100.0;

            LogData.LogThis(Level.INFO, "Positions[i].AveStraddleBuyPrice  = " + String.valueOf(Positions[i].AveStraddleBuyPrice));
            LogData.LogThis(Level.INFO, "Positions[i].AveStraddleSellPrice = " + String.valueOf(Positions[i].AveStraddleSellPrice));

            // Get the current mark prices for the Calls & Puts and for the straddle.
            Positions[i].CurrentCallsPrice = Double.valueOf(CallsCurrentPrice);
            Positions[i].CurrentPutsPrice = Double.valueOf(PutsCurrentPrice);
            //Positions[i].CurrentCallsPrice = 0.70;
            //Positions[i].CurrentPutsPrice = 0.27;
            Positions[i].CurrentStraddlePrice = Positions[i].CurrentCallsPrice + Positions[i].CurrentPutsPrice;

            LogData.LogThis(Level.INFO, "Positions[i].CurrentCallsPrice    = " + String.valueOf(Positions[i].CurrentCallsPrice));
            LogData.LogThis(Level.INFO, "Positions[i].CurrentPutsPrice     = " + String.valueOf(Positions[i].CurrentPutsPrice));
            LogData.LogThis(Level.INFO, "Positions[i].CurrentStraddlePrice = " + String.valueOf(Positions[i].CurrentStraddlePrice));

            // Get the Expiration date.
            //
            Positions[i].Expiration = Expiration;

            LogData.LogThis(Level.INFO, "Positions[i].Expiration           = " + Positions[i].Expiration);

            Positions[i].High = Double.valueOf(High);
            Positions[i].Low = Double.valueOf(Low);

//  TBD - To be removed, with improvements in program will try and send updates every 1 second now.               
//            Positions[i] = updateQuoteItems( Positions[i] );
            }

         return Positions;

         }
      }

   

   // The actual position data that is deliver from the FIT_Orders program comes at an rate that 
   //   isn't guaranteed to be 1 second or less.  So the quote items for the position can be stale.
   //   Since the GUI runs at a faster rate than the position status updates, we will assume the
   //   position is still valid and update the quote values to the most current from the TT_Quotes
   //   program.
   // Specifcally the values that will be updated are:  
   //       CurrentCallsPrice, 
   //       CurrentPutsPrice, 
   //       CurrentStraddlePrice, 
   //       StockPriceCurrent, 
   //       High,
   //       Low.
/*   
   synchronized public Position updateQuoteItems( Position currentValues ) {
       Position newValues = currentValues;
       
        // Build a string to identify the option symbol at Schwab or Tasty
        String[] OrderKeyValues = newValues.Identifier.split("_");  // 0 = AcctNum, 1 = Symbol, 2 = Strike
        String callLegSymbol = OrderKeyValues[1] + "_" + newValues.Expiration + "C" + OrderKeyValues[2];
        
        // Get updated stock price
        // Not going to update the open price here as the current update rate from FIT_Orders works fast enough.
        String stockPrice = StreamingQuotesData.getStockPrice(OrderKeyValues[1]);
        newValues.StockPriceCurrent = Double.parseDouble(stockPrice);
                
        // Get updated straddle, call, put option mark prices
        String[] optionPrices = StreamingQuotesData.getStraddleMarkPrices(callLegSymbol, OrderKeyValues[1]);
        //  There is a bug in the Quotes side where the straddle mark price isn't updating.  The Call and Put prices do
        //      so going to use them instead.
        // newValues.CurrentStraddlePrice = Double.parseDouble(optionPrices[0]);
        newValues.CurrentCallsPrice = Double.parseDouble(optionPrices[1]);
        newValues.CurrentPutsPrice = Double.parseDouble(optionPrices[2]);
        newValues.CurrentStraddlePrice = newValues.CurrentCallsPrice + newValues.CurrentPutsPrice;
        
        // Update the High and Low values if the currentStockPrice has exceeded 
        //      the previously reported threshhold.
        if (newValues.High < newValues.StockPriceCurrent) {
            newValues.High = newValues.StockPriceCurrent;
        } else {
            if (newValues.Low > newValues.StockPriceCurrent) {
                newValues.Low = newValues.StockPriceCurrent;
            }
        }
       
       return newValues;
   }
*/
   // This procedure creates, initializes, and adds a Position panel to the GUI.
   // It also adds a new IndexTable to the PositionPanelListIndex table.
   //
   // Some of the widgets in the Position panel do not need their value udpated
   // every second (i.e. Account, Symbol, Strike, and Min/Max Exit Times).  So, we
   // only need to set those values here.  All of the other values will be set in
   // the Update_Position_Panel routine.
   //
   public void Create_Position_Panel(Position Position)
      {
      final int gapWidth = 5;         // The gap width between widgets.

      final int accountWidth = 75;     // Brokerage
      final int symbolWidth = 55;      // CMCSA
      final int strikeWidth = 65;      // 1234.56
      final int statusWidth = 50;      // Selling
      final int contractsWidth = 65;   // 100/120

      final int arrowWidth = 20;
      final int progressWidth = 250;

      final int paidTextFieldWidth = 50;

      final int amountTextFieldWidth = 60;

      final int currentLockCheckboxWidth = 20;
      final int currentPriceWidth = 50;
      final int currentPercentWidth = 50;
      final int currentAmountWidth = 50;
      final int currentSellAllButtonWidth = 55;
//      final int currentSellHalfButtonWidth = 55;
//      final int currentSellCallsButtonWidth = 95;
//      final int currentSellPutsButtonWidth = 95;

      final int targetPriceWidth = 70;
      final int targetPercentWidth = 70;
      final int targetAmountWidth = 70;
      final int targetSellAllButtonWidth = 55;
      final int targetSellThreeQuarterButtonWidth = 55;
      final int targetSellHalfButtonWidth = 55;
//      final int targetSellCallsButtonWidth = 95;
//      final int targetSellPutsButtonWidth = 95;

//      final int cancelButtonWidth = 75;

      final int moveLinesWidth = 3;
      final int moveLinesHeight = 30;
      final int highWaterMoveLinesHeight = 5;

      int X;

      // Create a line border with the specified color and width
      Border border = BorderFactory.createLineBorder(Color.BLACK, 1);   // TBD - remove ???

      //
      //------------------------------------------------------------------------
      //
      // If this is the first panel, then create the header widgets.
      if (!Position_Header_Created)
         {
         String[] labels =
            {
            "Account",
            "Symbol",
            "Strike",
            "Status",
            "Contracts",
            "Progress",
            "Paid",
            "Amount",
            "Sell Current",
            "Sell Target"
            };

         int[] labelWidths =
            {
            accountWidth,
            symbolWidth,
            strikeWidth,
            statusWidth,
            contractsWidth,
            arrowWidth + gapWidth + progressWidth,
            paidTextFieldWidth,
            amountTextFieldWidth,
            currentLockCheckboxWidth + gapWidth + currentPriceWidth + gapWidth + currentPercentWidth + gapWidth + currentPriceWidth + gapWidth + currentSellAllButtonWidth,
            targetPriceWidth + gapWidth + targetPercentWidth + gapWidth + targetPriceWidth + gapWidth + targetSellAllButtonWidth + gapWidth + targetSellThreeQuarterButtonWidth + gapWidth + targetSellHalfButtonWidth,
            };

         // Initialize the gap width.
         X = 3 * gapWidth;

         for (int i = 0; i < labels.length; i++)
            {
            javax.swing.JLabel headerLabel = new javax.swing.JLabel(labels[i]);
            headerLabel.setFont(HEADER_FONT);
            headerLabel.setBounds(X, 70, labelWidths[i], 20);  // X, Y, Width, Height
            headerLabel.setHorizontalAlignment(JLabel.CENTER);
            headerLabel.setOpaque(true);
            headerLabel.setBackground(new java.awt.Color(204, 204, 255));
            headerLabel.setBorder(border);

            Monitor_Panel.add(headerLabel);

            X = X + labelWidths[i] + gapWidth;

            // Add extra space as needed.
            if (labels[i].equals("Contracts"))
               {
               //X = X + gapWidth;
               }
            else if (labels[i].equals("Progress"))
               {
               //X = X + gapWidth;
               }

            }
         }   // End of create the header widgets.

      //
      //------------------------------------------------------------------------
      //
      // Create a new panel and set its attributes.
      javax.swing.JPanel panel = new javax.swing.JPanel();

      panel.setName(POSITION_NAME + String.valueOf(numPositionPanels + 1));
      //panel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
      panel.setBorder(border);

      // Alternate the background color.   TBD - I think it reads better if they are all white.
      //if (Index % 2 == 0)
         {
         panel.setBackground(Color.WHITE);
         }

      // Set the Layout Manager to null so we can place the widgets at specific locations.
      panel.setLayout(null);

      // Reset X to the left side of the Panel.
      X = gapWidth;

      // Add the Account Name widget to the panel.
      javax.swing.JLabel account = new javax.swing.JLabel(Position.AccountName);

      account.setName(ACCOUNT_NAME);
      account.setFont(POSITION_NAME_FONT);
      account.setBounds(X, 8, accountWidth, 20);  // X, Y, Width, Height
      account.setHorizontalAlignment(JLabel.CENTER);

      panel.add(account);

      X = X + accountWidth + gapWidth;

      // Add the Stock Symbol to the panel.
      javax.swing.JLabel symbolLabel = new javax.swing.JLabel(Position.StockSymbol);

      symbolLabel.setName(SYMBOL_NAME);
      symbolLabel.setFont(POSITION_NAME_FONT);
      symbolLabel.setBounds(X, 8, symbolWidth, 20);  // X, Y, Width, Height
      symbolLabel.setHorizontalAlignment(JLabel.CENTER);

      panel.add(symbolLabel);

      X = X + symbolWidth + gapWidth;

      // Add the Strike Price to the panel.
      javax.swing.JLabel strikeLabel = new javax.swing.JLabel(Position.StrikePrice);

      strikeLabel.setName(STRIKE_NAME);
      strikeLabel.setFont(POSITION_NAME_FONT);
      strikeLabel.setBounds(X, 8, strikeWidth, 20);  // X, Y, Width, Height
      strikeLabel.setHorizontalAlignment(JLabel.CENTER);

      panel.add(strikeLabel);

      X = X + strikeWidth + gapWidth;

      // Add the Status to the panel.
      javax.swing.JLabel statusLabel = new javax.swing.JLabel();

      statusLabel.setName(STATUS_NAME);
      statusLabel.setFont(STATUS_FONT);
      statusLabel.setBounds(X, 8, statusWidth, 20);  // X, Y, Width, Height
      statusLabel.setHorizontalAlignment(JLabel.CENTER);

      panel.add(statusLabel);

      X = X + statusWidth + gapWidth;

      // Add the Contracts to the panel.  Note: When we are adding a panel, we should
      // not be selling anything yet, so just use the number of filled buy contracts.
      // Display X/N for a partial fill, and just N for a full fill.
      String Contracts;

      if (Position.FilledBuyContracts.equals(Position.TotalContracts))
         {
         Contracts = String.valueOf(Position.TotalContracts);
         }
      else
         {
         Contracts = String.valueOf(Position.FilledBuyContracts) + "/" + String.valueOf(Position.TotalContracts);
         }

      //javax.swing.JLabel contractsLabel = new javax.swing.JLabel(Contracts);
      javax.swing.JLabel contractsLabel = new javax.swing.JLabel();

      contractsLabel.setName(CONTRACTS_NAME);
      contractsLabel.setFont(CONTRACTS_FONT);
      contractsLabel.setBounds(X, 9, contractsWidth, 20);  // X, Y, Width, Height
      contractsLabel.setHorizontalAlignment(JLabel.CENTER);

      panel.add(contractsLabel);

      X = X + contractsWidth + gapWidth;

      // Add the Progress Arrow to the panel.
      javax.swing.JLabel arrow = new javax.swing.JLabel();

      arrow.setName(ARROW_NAME);
      arrow.setFont(ARROW_FONT);
      arrow.setBounds(X, -15, arrowWidth, 60);  // X, Y, Width, Height
      arrow.setHorizontalAlignment(JLabel.CENTER);
      arrow.setVerticalAlignment(JLabel.CENTER);

      panel.add(arrow);

      X = X + arrowWidth + gapWidth;

      // Add the Progress Bar to the panel.
      javax.swing.JProgressBar progress = new javax.swing.JProgressBar();

      progress.setName(PROGRESS_NAME);
      progress.setBounds(X, 10, progressWidth, 20); // X, Y, Width, Height
      progress.setString("");                       // This gets rid of the % text in the bar.
      progress.setStringPainted(true);              // This allows us to change the colors.

      double dblMin = Position.MinMove;
      double dblAve = Position.AveMove;

      double dblMax = (dblAve / 0.95);

      // Note: Every ProgressBar's displayed length needs to be the same. So, the value needs to be scaled
      //       so that we can see a decent move to the blue min line, while still displaying the red ave line.
      //       To do this, I am setting the progress bar's Max value to (AveMove / 0.95).  This will always
      //       place the red Ave line at 95% of the progress bar.  The blue line will move depending on the
      //       spread between the Min & Ave moves.  If the spread is small, then the blue Min line will be
      //       drawn further to the right.  If the spread is large, then the blue Min line will be drawn
      //       further to the left.  For example: If the Min = 2.00 and the Ave = 4.00, line is drawn near
      //       the middle of the bar at 47.5%.  If the Min is 2.50 and the Ave is 15.00, it's drawn at 16%.
      //
      //       I chose this implementation because I always want to see both lines.  However, the most important
      //       line for me is the blue line.  This is typically when I would choose to exit.  The also works
      //       well considering we seldom have large spreads that will draw the line so far to the left that
      //       it would be essentially worthless.
      //
      // Multiply the value by 100 to save the precision that would be lost converting the float to an integer.
      progress.setMaximum((int) (dblMax * 100.0));

      // Add the Min and Ave lines.
      //
      // Note: These must be drawn before the progress bar so that they are visible.
      //       I expected them to be drawn last, but that doesn't work. I am not sure
      //       why at this point.
      javax.swing.JPanel minPanel = new javax.swing.JPanel();
      javax.swing.JPanel avePanel = new javax.swing.JPanel();

      // The Min Move line is drawn in different locations depending on the Min - Ave spread.
      double dblMinPercent = dblMin / dblMax;

      minPanel.setName(MIN_MOVE_NAME);
      minPanel.setSize(moveLinesWidth, moveLinesHeight);
      minPanel.setLocation(X + (int) ((double) progressWidth * dblMinPercent), 5);
      minPanel.setBackground(Color.BLUE);

      // The Ave Move line is always drawn at 95% of the bar.
      avePanel.setName(AVE_MOVE_NAME);
      avePanel.setSize(moveLinesWidth, moveLinesHeight);
      avePanel.setLocation(X + (int) ((double) progressWidth * 0.95), 5);
      avePanel.setBackground(Color.RED);

      // There are two High Water lines.  The one on Top of the progress bar shows the
      // maximum amount the Stock Price has moved up.  The one on the Bottom shows the
      // maximum amount the Stock Price has moved down.  When they are created, they
      // will be drawn at the beginning of the progress bar.  They will will get moved
      // when new maximum moves occur during the Position Panel updates.
      
      javax.swing.JPanel highWaterUpPanel = new javax.swing.JPanel();
      javax.swing.JPanel highWaterDownPanel = new javax.swing.JPanel();

      highWaterUpPanel.setName(HIGH_WATER_UP_MOVE_NAME);
      highWaterUpPanel.setSize(moveLinesWidth, highWaterMoveLinesHeight);
      highWaterUpPanel.setLocation(X, 5);
      highWaterUpPanel.setBackground(Color.BLACK);

      highWaterDownPanel.setName(HIGH_WATER_DOWN_MOVE_NAME);
      highWaterDownPanel.setSize(moveLinesWidth, highWaterMoveLinesHeight);
      highWaterDownPanel.setLocation(X, 30);
      highWaterDownPanel.setBackground(Color.BLACK);

      panel.add(progress);
      panel.add(minPanel);
      panel.add(avePanel);
      panel.add(highWaterUpPanel);
      panel.add(highWaterDownPanel);

      // Save the initial X coordinate so we can use it to position the High Water
      // marks correctly.
      ProgressBarXCoordinate = X;
      
      X = X + progressWidth + gapWidth;

      // Add the Amount Paid to the panel.
      javax.swing.JTextField paidTextField = new javax.swing.JTextField();

      paidTextField.setName(PAID_PRICE_NAME);
      paidTextField.setFont(PRICE_FONT);
      paidTextField.setBounds(X, 10, paidTextFieldWidth, 20);  // X, Y, Width, Height
      paidTextField.setHorizontalAlignment(JLabel.CENTER);

      panel.add(paidTextField);

      X = X + paidTextFieldWidth + gapWidth;

      // Add the Position Amount to the panel.
      javax.swing.JTextField AmountTextField = new javax.swing.JTextField();

      AmountTextField.setName(POSITION_AMOUNT_NAME);
      AmountTextField.setFont(PRICE_FONT);
      AmountTextField.setBounds(X, 10, amountTextFieldWidth, 20);  // X, Y, Width, Height
      AmountTextField.setHorizontalAlignment(JLabel.CENTER);

      panel.add(AmountTextField);

      X = X + amountTextFieldWidth + gapWidth;

      // Add the Current Lock checkbox to the panel.
      javax.swing.JCheckBox currentLockCheckbox = new javax.swing.JCheckBox();
      
      currentLockCheckbox.setName(CURRENT_LOCK_CHECKBOX_NAME + " " + String.valueOf(numPositionPanels + 1));
      currentLockCheckbox.setBounds(X, 10, currentLockCheckboxWidth, 20);  // X, Y, Width, Height

      currentLockCheckbox.addActionListener(new java.awt.event.ActionListener()
         {
         public void actionPerformed(java.awt.event.ActionEvent evt)
            {
            Position_Current_Lock_Checkbox_Handler(evt);
            }
         });

      panel.add(currentLockCheckbox);

      X = X + currentLockCheckboxWidth + gapWidth;

      // Add the Current/Sold Straddle Price to the panel.
      javax.swing.JTextField currentPrice = new javax.swing.JTextField();

      currentPrice.setName(CURRENT_PRICE_NAME);
      currentPrice.setFont(PRICE_FONT);
      currentPrice.setBounds(X, 10, currentPriceWidth, 20);  // X, Y, Width, Height
      currentPrice.setHorizontalAlignment(JLabel.CENTER);

      panel.add(currentPrice);

      X = X + currentPriceWidth + gapWidth;

      // Add the Current Profit Percentage to the panel.
      javax.swing.JTextField currentPercent = new javax.swing.JTextField();

      currentPercent.setName(CURRENT_PERCENTAGE_NAME);
      currentPercent.setFont(PROFIT_FONT);
      currentPercent.setBounds(X, 10, currentPercentWidth, 20);  // X, Y, Width, Height
      currentPercent.setHorizontalAlignment(JLabel.CENTER);

      panel.add(currentPercent);

      X = X + currentPercentWidth + gapWidth;

      // Add the Current Profit Amount to the panel.
      javax.swing.JTextField currentAmount = new javax.swing.JTextField();

      currentAmount.setName(CURRENT_AMOUNT_NAME);
      currentAmount.setFont(PROFIT_FONT);
      currentAmount.setBounds(X, 10, currentAmountWidth, 20);  // X, Y, Width, Height
      currentAmount.setHorizontalAlignment(JLabel.CENTER);

      panel.add(currentAmount);

      X = X + currentAmountWidth + gapWidth;

      // Add the Current Sell All button to the panel.
      javax.swing.JButton currentSellAllButton = new javax.swing.JButton();

      currentSellAllButton.setName(CURRENT_SELL_ALL_BUTTON_NAME + " " + String.valueOf(numPositionPanels + 1));
      currentSellAllButton.setFont(BUTTON_FONT);
      currentSellAllButton.setBounds(X, 6, currentSellAllButtonWidth, 28);  // X, Y, Width, Height
      currentSellAllButton.setHorizontalAlignment(JButton.CENTER);
      currentSellAllButton.setText("All");

      currentSellAllButton.addActionListener(new java.awt.event.ActionListener()
         {
         public void actionPerformed(java.awt.event.ActionEvent evt)
            {
            Position_Button_Handler(evt);
            }
         });

      panel.add(currentSellAllButton);

      X = X + currentSellAllButtonWidth + gapWidth;

      // Add the Target Straddle Price to the panel.
      javax.swing.JTextField targetPrice = new javax.swing.JTextField();

      targetPrice.setName(TARGET_PRICE_NAME + " " + String.valueOf(numPositionPanels + 1));
      targetPrice.setFont(PRICE_FONT);
      targetPrice.setBounds(X, 6, targetPriceWidth, 28);  // X, Y, Width, Height
      targetPrice.setHorizontalAlignment(JTextField.CENTER);

      // Set the initial value here.  The value will get updated if the number of filled
      // buy contracts increases.  This could happen if this initial position is a partial
      // fill.  If we don't initialize it until all the contracts are filled, then we won't
      // be able to sell at the target price if we never get a total fill.
      //
      // The value will also get updated every time the Target Percentage or Target Amount
      // values change.  We change those asynchronously through the GUI.
      targetPrice.setText(currencyFormatter.format(Position.AveStraddleBuyPrice * (1.0 + Position.Percentage)));

      // The initial value will always be profitable, so set the color accordinly.
      targetPrice.setForeground(Color.BLACK);
      targetPrice.setBackground(Color.GREEN);

      targetPrice.addActionListener(new ActionListener()
         {
         public void actionPerformed(ActionEvent e)
            {
            Target_Price_Handler(e);
            }
         });

      panel.add(targetPrice);

      X = X + targetPriceWidth + gapWidth;

      // Add the Target Profit Percentage to the panel.
      javax.swing.JTextField targetPercent = new javax.swing.JTextField();

      targetPercent.setName(TARGET_PERCENTAGE_NAME + " " + String.valueOf(numPositionPanels + 1));
      targetPercent.setFont(PRICE_FONT);
      targetPercent.setBounds(X, 6, targetPercentWidth, 28);  // X, Y, Width, Height
      targetPercent.setHorizontalAlignment(JTextField.CENTER);

      targetPercent.setText(String.valueOf(Position.Percentage * 100.0));

      targetPercent.setForeground(Color.BLACK);
      targetPercent.setBackground(Color.GREEN);

      targetPercent.addActionListener(new ActionListener()
         {
         public void actionPerformed(ActionEvent e)
            {
            Target_Percentage_Handler(e);
            }
         });

      panel.add(targetPercent);

      X = X + targetPercentWidth + gapWidth;

      // Add the Target Profit Amount to the panel.
      javax.swing.JTextField targetAmount = new javax.swing.JTextField();

      targetAmount.setName(TARGET_AMOUNT_NAME + " " + String.valueOf(numPositionPanels + 1));
      targetAmount.setFont(PRICE_FONT);
      targetAmount.setBounds(X, 6, targetAmountWidth, 28);  // X, Y, Width, Height
      targetAmount.setHorizontalAlignment(JTextField.CENTER);

      targetAmount.setText(String.valueOf(Position.AveStraddleBuyPrice * Position.Percentage * Position.FilledBuyContracts * 100.0));

      targetAmount.setForeground(Color.BLACK);
      targetAmount.setBackground(Color.GREEN);

      targetAmount.addActionListener(new ActionListener()
         {
         public void actionPerformed(ActionEvent e)
            {
            Target_Amount_Handler(e);
            }
         });

      panel.add(targetAmount);

      X = X + targetAmountWidth + gapWidth;

      // Add the Target Sell All button to the panel.
      javax.swing.JButton targetSellButton = new javax.swing.JButton();

      targetSellButton.setName(TARGET_SELL_ALL_BUTTON_NAME + " " + String.valueOf(numPositionPanels + 1));
      targetSellButton.setFont(BUTTON_FONT);
      targetSellButton.setBounds(X, 6, targetSellAllButtonWidth, 28);  // X, Y, Width, Height
      targetSellButton.setHorizontalAlignment(JButton.CENTER);
      targetSellButton.setText("All");

      targetSellButton.addActionListener(new java.awt.event.ActionListener()
         {
         public void actionPerformed(java.awt.event.ActionEvent evt)
            {
            Position_Button_Handler(evt);
            }
         });

      panel.add(targetSellButton);

      X = X + targetSellAllButtonWidth + gapWidth;
 
      // Add the Target Sell 3/4 button to the panel.
      javax.swing.JButton targetSellThreeQuarterButton = new javax.swing.JButton();

      targetSellThreeQuarterButton.setName(TARGET_SELL_THREE_QUARTER_BUTTON_NAME + " " + String.valueOf(numPositionPanels + 1));
      targetSellThreeQuarterButton.setFont(BUTTON_FONT);
      targetSellThreeQuarterButton.setBounds(X, 6, targetSellThreeQuarterButtonWidth, 28); // X, Y, Width, Height
      targetSellThreeQuarterButton.setHorizontalAlignment(JButton.CENTER);
      targetSellThreeQuarterButton.setText("3/4");

      targetSellThreeQuarterButton.addActionListener(new java.awt.event.ActionListener()
         {
         public void actionPerformed(java.awt.event.ActionEvent evt)
            {
            Position_Button_Handler(evt);
            }
         });

      panel.add(targetSellThreeQuarterButton);

      X = X + targetSellThreeQuarterButtonWidth + gapWidth;

      // Add the Target Sell Half button to the panel.
      javax.swing.JButton targetSellHalfButton = new javax.swing.JButton();

      targetSellHalfButton.setName(TARGET_SELL_HALF_BUTTON_NAME + " " + String.valueOf(numPositionPanels + 1));
      targetSellHalfButton.setFont(BUTTON_FONT);
      targetSellHalfButton.setBounds(X, 6, targetSellHalfButtonWidth, 28); // X, Y, Width, Height
      targetSellHalfButton.setHorizontalAlignment(JButton.CENTER);
      targetSellHalfButton.setText("1/2");

      targetSellHalfButton.addActionListener(new java.awt.event.ActionListener()
         {
         public void actionPerformed(java.awt.event.ActionEvent evt)
            {
            Position_Button_Handler(evt);
            }
         });

      panel.add(targetSellHalfButton);

      X = X + targetSellHalfButtonWidth + gapWidth;

//       // Add the Target Sell Calls button to the panel.
//       javax.swing.JButton targetSellCallsButton = new javax.swing.JButton();
//
//       targetSellCallsButton.setName(TARGET_SELL_CALLS_BUTTON_NAME + String.valueOf(numPositionPanels + 1));
//       targetSellCallsButton.setFont(BUTTON_FONT);
//       targetSellCallsButton.setBounds(X, 6, targetSellCallsButtonWidth, 28); // X, Y, Width, Height
//       targetSellCallsButton.setHorizontalAlignment(JButton.CENTER);
//       targetSellCallsButton.setText("C: " + currencyFormatter.format(Position.CurrentCallsPrice));
//
//       targetSellCallsButton.addActionListener(new java.awt.event.ActionListener()
//       {
//       public void actionPerformed(java.awt.event.ActionEvent evt)
//       {
//       Position_Button_Handler(evt);
//       }
//       });
//
//       panel.add(targetSellCallsButton);
//
//       X = X + targetSellCallsButtonWidth + gapWidth;
//
//       // Add the Target Sell Puts button to the panel.
//       javax.swing.JButton targetSellPutsButton = new javax.swing.JButton();
//
//       targetSellPutsButton.setName(TARGET_SELL_PUTS_BUTTON_NAME + String.valueOf(numPositionPanels + 1));
//       targetSellPutsButton.setFont(BUTTON_FONT);
//       targetSellPutsButton.setBounds(X, 6, targetSellPutsButtonWidth, 28); // X, Y, Width, Height
//       targetSellPutsButton.setHorizontalAlignment(JButton.CENTER);
//       targetSellPutsButton.setText("P: " + currencyFormatter.format(Position.CurrentPutsPrice));
//
//       targetSellPutsButton.addActionListener(new java.awt.event.ActionListener()
//       {
//       public void actionPerformed(java.awt.event.ActionEvent evt)
//       {
//       Position_Button_Handler(evt);
//       }
//       });
//
//       panel.add(targetSellPutsButton);
//
//       X = X + targetSellPutsButtonWidth + gapWidth;
//
//       // Add the Cancel button to the panel.
//       javax.swing.JButton cancelButton = new javax.swing.JButton();
//
//       cancelButton.setName("cancelButton " + String.valueOf(numPositionPanels + 1));
//       cancelButton.setFont(BUTTON_FONT);
//       cancelButton.setBounds(X, 6, cancelButtonWidth, 28); // X, Y, Width, Height
//       cancelButton.setHorizontalAlignment(JButton.CENTER);
//       cancelButton.setText("Cancel");
//
//       cancelButton.addActionListener(new java.awt.event.ActionListener()
//       {
//       public void actionPerformed(java.awt.event.ActionEvent evt)
//       {
//       Position_Button_Handler(evt);
//       }
//       });
//
//       panel.add(cancelButton);
//
//       X = X + cancelButtonWidth + gapWidth;

      // Set the panel dimensions.
      // panel.setBounds(10, 10 + Index * 40, X, 40);  // X, Y, Width, Height
      panel.setBounds(10, 100 + numPositionPanels * 40, X, 40);  // X, Y, Width, Height

      // Add the panel to the list.
      PositionPanelList.add(panel);

      // Add the panel to the main panel.
      Monitor_Panel.add(PositionPanelList.get(numPositionPanels));

      // Revalidate the panel so everything becomes visible.
      PositionPanelList.get(numPositionPanels).revalidate();

      // Add the new IndexTable to the PositionPanelListIndex table.
      PositionPanelListIndex[numPositionPanels] = new IndexTable();

      PositionPanelListIndex[numPositionPanels].Index = numPositionPanels;
      PositionPanelListIndex[numPositionPanels].Account = Position.AccountNumber;
      PositionPanelListIndex[numPositionPanels].Symbol = Position.StockSymbol;
      PositionPanelListIndex[numPositionPanels].Strike = Position.StrikePrice;

      // Save the initial Target values.
      PositionPanelListIndex[numPositionPanels].TargetPrice = Position.AveStraddleBuyPrice * (1.0 + Position.Percentage);
      PositionPanelListIndex[numPositionPanels].TargetPercentage = Position.Percentage;
      PositionPanelListIndex[numPositionPanels].TargetAmount = (PositionPanelListIndex[numPositionPanels].TargetPrice - Position.AveStraddleBuyPrice) * Position.FilledBuyContracts * 100.0;

      // Increment the number of panels.
      numPositionPanels = numPositionPanels + 1;

      // Repaint the main panel so everything becomes visible.
      Monitor_Panel.repaint();

      }

   // This procedure gets the panel index for the given Position.
   public Integer Get_Position_Panel_Index(Position Position)
      {
      // Loop through the Position Panels until you find the given Position.

      int Index = 0;

      for (int i = 0; i < numPositionPanels; i++)
         {
         String Account = PositionPanelListIndex[i].Account;
         String Symbol = PositionPanelListIndex[i].Symbol;
         String Strike = PositionPanelListIndex[i].Strike;

         String Identifier = Account + "_" + Symbol + "_" + Strike;

         if (Position.Identifier.equalsIgnoreCase(Identifier))
            {
            Index = PositionPanelListIndex[i].Index;

            break;
            }
         }
      return Index;

      }

   //---------------------------------------------------------------------------
   //
   // This procedure updates the given Position's panel.
   public Boolean Sold(Position Position)
      {
      return ((Position.FilledSellContracts > 0) && (Position.FilledSellContracts.equals(Position.TotalContracts)));
     }

   //---------------------------------------------------------------------------
   //
   // This procedure updates the given Position's panel.
   public void Update_Position_Panel(Position Position)
      {
      // Get the panel index.
      Integer Index = Get_Position_Panel_Index(Position);

      // Get the panel.
      JPanel panel = PositionPanelList.get(Index);

      // Get the panel components, and then update each of them.
      Component[] components = panel.getComponents();
      for (Component component : components)
         {
         String componentName = component.getName();

         // Strip off the Position IndexTable suffix (if there is one).
         int spaceIndex = componentName.indexOf(" ");

         if (spaceIndex > 0)
            {
            String strIndex = componentName.substring(spaceIndex + 1);
            componentName = componentName.substring(0, spaceIndex);
            }

         // If the position is Sold, either make the panel invisible, or just grey out the panel.
         if (Sold (Position))
             {
             if (Show_Sold_Positions_Checkbox.isSelected())
                {
                panel.setVisible(true);
                panel.setBackground(Color.LIGHT_GRAY);
                }
             else
                {
                panel.setVisible(false);
                }
             }
         
         // Update the component (i.e. the widget).
         if (componentName.equals(STATUS_NAME))
            {
            String tempLabel;

            // Tony is sending -1 to indicate we haven't started selling anything yet.
            if (Position.FilledSellContracts.equals(-1))
               {
               if (Position.FilledBuyContracts < Position.TotalContracts)
                  {
                  tempLabel = "Buying";
                  }
               else
                  {
                  tempLabel = "Bought";
                  }
               }
            else
               {
               if (Position.FilledSellContracts < Position.TotalContracts)
                  {
                  tempLabel = "Selling";
                  }
               else
                  {
                  tempLabel = "Sold";
                  }
               }
            ((JLabel) component).setText(String.valueOf(tempLabel));
            }

         else if (componentName.equals(CONTRACTS_NAME))
            {
            if (Position.AveStraddleSellPrice > 0.0)
               {
               // We have sold, or we are selling.
               if (Position.FilledSellContracts.equals(Position.TotalContracts))
                  {
                  ((JLabel) component).setText(String.valueOf(Position.TotalContracts));
                  }
               else
                  {
                  ((JLabel) component).setText(String.valueOf(Position.FilledSellContracts) + " / " + String.valueOf(Position.TotalContracts));
                  }
               }
            else
               {
               // We have bought, or we are buying.
               if (Position.FilledBuyContracts.equals(Position.TotalContracts))
                  {
                  ((JLabel) component).setText(String.valueOf(Position.TotalContracts));
                  }
               else
                  {
                  ((JLabel) component).setText(String.valueOf(Position.FilledBuyContracts) + " / " + String.valueOf(Position.TotalContracts));
                  }
               }
            }

         else if ((componentName.equals(ARROW_NAME)) && (! Sold (Position)))
            {
            String strOpenPrice = String.valueOf(Position.StockPriceOpen);
            String strCurrPrice = String.valueOf(Position.StockPriceCurrent);

            double dblOpenPrice = Double.parseDouble(strOpenPrice);
            double dblCurrPrice = Double.parseDouble(strCurrPrice);

            if (dblCurrPrice < dblOpenPrice)
               {
               ((JLabel) component).setText("\u2193");          // Unicode down arrow value.
               ((JLabel) component).setForeground(Color.RED);
               }
            else
               {
               ((JLabel) component).setText("\u2191");          // Unicode up arrow value.
               ((JLabel) component).setForeground(Color.GREEN);
               }
            }

         else if ((componentName.equals(PROGRESS_NAME)))
            {
            String strOpenPrice = String.valueOf(Position.StockPriceOpen);
            String strCurrPrice = String.valueOf(Position.StockPriceCurrent);

            double dblOpenPrice = Double.parseDouble(strOpenPrice);
            double dblCurrPrice = Double.parseDouble(strCurrPrice);

            double dblMove = Math.abs(dblOpenPrice - dblCurrPrice);

            String strMin = String.valueOf(Position.MinMove);
            String strAve = String.valueOf(Position.AveMove);

            double dblMin = Double.parseDouble(strMin);
            double dblAve = Double.parseDouble(strAve);

            // Calculate the new scaled value.  Remember, we multiplied the
            // Max value by 100 to save the precision.  Also, we need to make
            // sure we don't try to set a value greater than the Max.
            JProgressBar progress = ((JProgressBar) component);

            int intMax = progress.getMaximum();
            int intMove = (int) (dblMove * 100.0);

            if (! Sold (Position))
              {
              
              ((JProgressBar) component).setValue(Math.min(intMove, intMax));

              // Set the color.
              if (dblMove >= dblAve)
                {
                ((JProgressBar) component).setForeground(Color.RED);
                }
              else if (dblMove >= dblMin)
                {
                ((JProgressBar) component).setForeground(Color.BLUE);
                }
              else
                {
                ((JProgressBar) component).setForeground(Color.LIGHT_GRAY);
                }
            
              }
            
            // Save the maximum value, so we can use it next to update the High Water
            // marks ... which are a different Component ... a Panel.
            
            ProgressBarMaximum = intMax;            
            }

         else if ((componentName.equals(HIGH_WATER_UP_MOVE_NAME)))
            {
            double dblHigh = Position.High;
            double dblOpen = Position.StockPriceOpen;
            
            if (dblHigh > dblOpen)   // Up                    
              {
              JPanel tempPanel = ((JPanel) component);

              int intMax = ProgressBarMaximum;                
              int intMove = (int) ((dblHigh - dblOpen) * 100.0);
              intMove = Math.min (intMove, intMax);
              double dblPercent = (double) intMove / (double) intMax;

              int intOffset = 0;
              if (dblPercent >= 1.0)
                {
                intOffset = -5;
                }
              
              ((JPanel) component).setLocation(ProgressBarXCoordinate + (int) (250 * dblPercent) + intOffset, 5);  // TBD hardoded with for now.

              }
              
            }

         else if ((componentName.equals(HIGH_WATER_DOWN_MOVE_NAME)))
            {
            double dblLow = Position.Low;
            double dblOpen = Position.StockPriceOpen;
            
            if (dblLow < dblOpen)   // Down                    
              {
              JPanel tempPanel = ((JPanel) component);

              int intMax = ProgressBarMaximum;               
              int intMove = (int) ((dblOpen - dblLow) * 100.0);
              intMove = Math.min (intMove, intMax);
              double dblPercent = (double) intMove / (double) intMax;

              int intOffset = 0;
              if (dblPercent >= 1.0)
                {
                intOffset = -5;
                }

              ((JPanel) component).setLocation(ProgressBarXCoordinate + (int) (250 * dblPercent) + intOffset, 30);
                     
              }
              
            }

         else if (componentName.equals(PAID_PRICE_NAME))
            {
            ((JTextField) component).setText(currencyFormatter.format(Position.AveStraddleBuyPrice));
            
            // If we did not pay more than the minimum move, then highlight the background so we
            // can see that we need to give this stock a chance to move for bigger profits.
            
            if (Position.MinMove > Position.AveStraddleBuyPrice)
                {
                ((JTextField) component).setBackground(Color.GREEN);
                }
            else
                {
                ((JTextField) component).setBackground(Color.WHITE);
                }         
            }

         else if (componentName.equals(POSITION_AMOUNT_NAME))
            {
            ((JTextField) component).setText(String.format("$%.0f%n", Position.TotalContracts * 100.0 * Position.AveStraddleBuyPrice));
            }

         else if (componentName.equals(CURRENT_PRICE_NAME))
            {
            // If the Position has been totally sold, then display the sold price.
            // Otherwise, display the current price.  Note:  It makes sense to display
            // the current price during a partial sell so that we can cancel/replace
            // the order again with another Sell button push.
            if (Sold (Position))
               //if (Position.FilledSellContracts.equals(Position.TotalContracts))
               {
               ((JTextField) component).setText(currencyFormatter.format(Position.AveStraddleSellPrice));
               }
            else
               {
               // If the current price is locked, then display the locked price.  Otherwise
               // display the current (i.e. dynamic) price.
                   
               if (PositionPanelListIndex[Index].CurrentLockedPrice > 0.0)
                   {
                   ((JTextField) component).setText(currencyFormatter.format(PositionPanelListIndex[Index].CurrentLockedPrice));
                   }
               else
                   {
                   ((JTextField) component).setText(currencyFormatter.format(Position.CurrentStraddlePrice));                  
                   }
                }
            }
         
         else if (componentName.equals(CURRENT_PERCENTAGE_NAME))
            {
            Integer FilledBuyContracts = Position.FilledBuyContracts;
            Integer FilledSellContracts = Position.FilledSellContracts;
            Integer TotalContracts = Position.TotalContracts;

            double AveStraddleBuyPrice = Position.AveStraddleBuyPrice;
            double AveStraddleSellPrice = Position.AveStraddleSellPrice;
            double CurrentStraddlePrice = Position.CurrentStraddlePrice;

            double FilledProfit;
            double UnfilledProfit;
            double ProfitPerContract;

            double Percentage;
            
            // If the current price is locked, then use the locked price.  Otherwise
            // use the current (i.e. dynamic) price.
                   
            if (PositionPanelListIndex[Index].CurrentLockedPrice > 0.0)
                {
                CurrentStraddlePrice = PositionPanelListIndex[Index].CurrentLockedPrice;
                }

            // If the average Sell price is greater than zero, then we have sold
            // at least one contract.  Otherwise, we have bought at least one contract.
            if (Position.AveStraddleSellPrice > 0.0)
               {
               if (FilledSellContracts.equals(TotalContracts))
                  {
                  // We have sold everything.
                  ProfitPerContract = AveStraddleSellPrice - AveStraddleBuyPrice;
                  }

               else
                  {
                  // We have sold at least one contract.
                  FilledProfit = FilledSellContracts * (AveStraddleSellPrice - AveStraddleBuyPrice);
                  UnfilledProfit = (TotalContracts - FilledBuyContracts) * (CurrentStraddlePrice - AveStraddleBuyPrice);

                  ProfitPerContract = (FilledProfit + UnfilledProfit) / TotalContracts;
                  }
               }

            else

               // We have not sold anything yet, so only focus on the contract(s)
               // we have bought.
               {
               ProfitPerContract = CurrentStraddlePrice - AveStraddleBuyPrice;
               }

            // Compute the percentage.
            Percentage = (ProfitPerContract / AveStraddleBuyPrice) * 100.0;

            // Update the value and the color.
            if (Percentage < 0.0)
               {
               ((JTextField) component).setForeground(Color.RED);
               ((JTextField) component).setBackground(Color.WHITE);
               }
            else if (Percentage > 0.0)
               {
               ((JTextField) component).setForeground(Color.BLACK);
               ((JTextField) component).setBackground(Color.GREEN);
               }

            ((JTextField) component).setText(String.format("%.0f%%", Math.abs(Percentage)));
            }

         else if (componentName.equals(CURRENT_AMOUNT_NAME))
            {
            Integer FilledBuyContracts = Position.FilledBuyContracts;
            Integer FilledSellContracts = Position.FilledSellContracts;
            Integer TotalContracts = Position.TotalContracts;

            double AveStraddleBuyPrice = Position.AveStraddleBuyPrice;
            double AveStraddleSellPrice = Position.AveStraddleSellPrice;
            double CurrentStraddlePrice = Position.CurrentStraddlePrice;

            double FilledProfit;
            double UnfilledProfit;
            double TotalProfit;
            
            // If the current price is locked, then use the locked price.  Otherwise
            // use the current (i.e. dynamic) price.
                   
            if (PositionPanelListIndex[Index].CurrentLockedPrice > 0.0)
                {
                CurrentStraddlePrice = PositionPanelListIndex[Index].CurrentLockedPrice;
                }

            // If the average Sell price is greater than zero, then we have sold
            // at least one contract.  Otherwise, we have bought at least one contract.
            if (Position.AveStraddleSellPrice > 0.0)
               {
               if (FilledSellContracts.equals(TotalContracts))
                  {
                  // We have sold everything.
                  TotalProfit = TotalContracts * (AveStraddleSellPrice - AveStraddleBuyPrice);
                  }

               else
                  {
                  // We have sold at least one contract.
                  FilledProfit = FilledSellContracts * (AveStraddleSellPrice - AveStraddleBuyPrice);
                  UnfilledProfit = (TotalContracts - FilledSellContracts) * (CurrentStraddlePrice - AveStraddleBuyPrice);

                  TotalProfit = FilledProfit + UnfilledProfit;
                  }
               }

            else

               // We have not sold anything yet, so only focus on the contract(s)
               // we have bought.
               {
               TotalProfit = FilledBuyContracts * (CurrentStraddlePrice - AveStraddleBuyPrice);
               }

            // Update the value and the color.
            if (TotalProfit < 0.0)
               {
               ((JTextField) component).setForeground(Color.RED);
               ((JTextField) component).setBackground(Color.WHITE);
               }
            else if (TotalProfit > 0.0)
               {
               ((JTextField) component).setForeground(Color.BLACK);
               ((JTextField) component).setBackground(Color.GREEN);
               }

            ((JTextField) component).setText(String.format("$%.0f%n", Math.abs(TotalProfit * 100.0)));

            // Update the Account Profit value.
            Account_Profit = Account_Profit + (TotalProfit * 100.0);
            }

         else if (componentName.equals(CURRENT_SELL_ALL_BUTTON_NAME))
            {
            // If we have sold all the contracts, then disable the button.
            ((JButton) component).setEnabled(!Position.FilledSellContracts.equals(Position.TotalContracts));
            }

         else if (componentName.equals(TARGET_PRICE_NAME))
            {
            // If we have sold all the contracts, then disable further entries.
            ((javax.swing.JTextField) component).setEnabled(!Position.FilledSellContracts.equals(Position.TotalContracts));
            }

         else if (componentName.equals(TARGET_PERCENTAGE_NAME))
            {
            // If we have sold all the contracts, then disable further entries.
            ((javax.swing.JTextField) component).setEnabled(!Position.FilledSellContracts.equals(Position.TotalContracts));
            }

         else if (componentName.equals(TARGET_AMOUNT_NAME))
            {
            // If we have sold all the contracts, then disable further entries.
            ((javax.swing.JTextField) component).setEnabled(!Position.FilledSellContracts.equals(Position.TotalContracts));
            }

         else if (componentName.equals(TARGET_SELL_ALL_BUTTON_NAME))
            {
            // If we have sold all the contracts, then disable the button.
            ((JButton) component).setEnabled(!Position.FilledSellContracts.equals(Position.TotalContracts));
            }

         else if (componentName.equals(TARGET_SELL_THREE_QUARTER_BUTTON_NAME))
          {
          // If we have sold all the contracts, then disable the button.
            ((JButton) component).setEnabled(!Position.FilledSellContracts.equals(Position.TotalContracts));
          }

          else if (componentName.equals(TARGET_SELL_HALF_BUTTON_NAME))
            {
            // If we have sold all the contracts, then disable the button.
            ((JButton) component).setEnabled(!Position.FilledSellContracts.equals(Position.TotalContracts));
            }

//          else if (componentName.equals(TARGET_SELL_CALLS_BUTTON_NAME))
//          {
//          // If we have sold all the contracts, then disable the button.
//          ((JButton) component).setEnabled(!Position.FilledSellContracts.equals(Position.TotalContracts));
//          }
//
//          else if (componentName.equals(TARGET_SELL_PUTS_BUTTON_NAME))
//          {
//          // If we have sold all the contracts, then disable the button.
//          ((JButton) component).setEnabled(!Position.FilledSellContracts.equals(Position.TotalContracts));
//          }

         /*

          else if (componentName.equals("sellCallsButton"))
          {
          // If we have sold all the contracts, then disable the button.
          // *** TBD - We need to keep track if all the Calls have been sold.
          ((JButton)
          component).setEnabled(!Position.FilledSellContracts.equals(Position.TotalContracts));

          double dblCallsCurrentPrice = Position.CurrentCallsPrice;
          ((JButton) component).setText("Sell Calls: " +
          String.format("$%.2f%n", dblCallsCurrentPrice));
          //((JTextField) component).setText(String.format("$%.0f%n",
          Math.abs(Amount)));
          }

          else if (componentName.equals("sellPutsButton"))
          {
          // If we have sold all the contracts, then disable the button.
          // *** TBD - We need to keep track if all the Puts have been sold.
          ((JButton)
          component).setEnabled(!Position.FilledSellContracts.equals(Position.TotalContracts));

          double dblPutsCurrentPrice = Position.CurrentPutsPrice;
          ((JButton) component).setText("Sell Puts: " + String.format("$%.2f%n",
          dblPutsCurrentPrice));
          }

          else if (componentName.equals("cancelButton"))
          {
          // If we have sold all the contracts, then disable the button.
          ((JButton)
          component).setEnabled(!Position.FilledSellContracts.equals(Position.TotalContracts));
          }

          */
         else
            {
            //LogData.LogThis(Level.INFO, "ERROR: Update_ButtonActionPerformed - Bad component name = " + componentName);
            }

         }
      
      // Repaint the panel so the updates become visible.
      panel.repaint();

      }

   /**
    *****************************************************************************
    */
   // This function determines if we are logged in to TDA.
   public boolean Logged_In()
      {
      //return (GUIData.getOrderProcessing());
      //return (Load_Date_Text_Field.getText().length() > 0);
      return (TradeResultsLoaded);  
      }

   /**
    *****************************************************************************
    */
   public void Update_Monitor_Tab()
      {
      // Don't try to update the tab until we are logged in.
      //if (Logged_In())
      if (true)
         {
             // Clear the Total Profit value.
         Total_Profit = 0.0;

         // Get the Account numbers.
         String[][] Account_Numbers = Get_Account_Numbers();

         // Loop through the Accounts.
         if (Account_Numbers != null)
            {
            for (int i = 0; i < Account_Numbers.length; i++)
               {
               String Account_Number = Account_Numbers[i][0];
               String Account_Name = Get_Account_Name(Account_Number);

               // Get the Position objects for the current Account.
               Position[] Positions = Get_Positions(Account_Number);

               if (Positions != null)
                  {
                  if (!popupFrame.isVisible()) {
                      popupFrame.setVisible(true);
                  }
                  // Clear the Account Profit value.
                  Account_Profit = 0.0;

                  // Loop through the Positions.
                  for (int j = 0; j < Positions.length; j++)
                     {
                     // If a panel exists for the current Position, then just update the
                     // panel.  Otherwise, create a new panel and then update the panel.
                     if (Exists(Positions[j].Identifier))
                        {
                        LogData.LogThis(Level.INFO, "Update_Position_Panel");
                        Update_Position_Panel(Positions[j]);
                        }
                     else
                        {
                        LogData.LogThis(Level.INFO, "Create_Position_Panel");
                        Create_Position_Panel(Positions[j]);
                        Update_Position_Panel(Positions[j]);
                        }
                     }

                  // Update the Account Profit label.
                  if (Account_Name.equals(Account_IDs[0].Name))
                     {
                     Account_1_Profit_TextField.setText(String.format("$%.0f%n", Account_Profit));

                     // Update the color.
                     if (Account_Profit < 0.0)
                        {
                        Account_1_Profit_TextField.setForeground(Color.RED);
                        Account_1_Profit_TextField.setBackground(Color.WHITE);
                        }
                     else if (Account_Profit > 0.0)
                        {
                        Account_1_Profit_TextField.setForeground(Color.BLACK);
                        Account_1_Profit_TextField.setBackground(Color.GREEN);
                        }
                     }
                  
                  else if (Account_Name.equals(Account_IDs[1].Name))
                     {
                     Account_2_Profit_TextField.setText(String.format("$%.0f%n", Account_Profit));

                     // Update the color.
                     if (Account_Profit < 0.0)
                        {
                        Account_2_Profit_TextField.setForeground(Color.RED);
                        Account_2_Profit_TextField.setBackground(Color.WHITE);
                        }
                     else if (Account_Profit > 0.0)
                        {
                        Account_2_Profit_TextField.setForeground(Color.BLACK);
                        Account_2_Profit_TextField.setBackground(Color.GREEN);
                        }
                     }
                  
                  else
                     {
                     Account_3_Profit_TextField.setText(String.format("$%.0f%n", Account_Profit));

                     // Update the color.
                     if (Account_Profit < 0.0)
                        {
                        Account_3_Profit_TextField.setForeground(Color.RED);
                        Account_3_Profit_TextField.setBackground(Color.WHITE);
                        }
                     else if (Account_Profit > 0.0)
                        {
                        Account_3_Profit_TextField.setForeground(Color.BLACK);
                        Account_3_Profit_TextField.setBackground(Color.GREEN);
                        }
                     }

                  Total_Profit = Total_Profit + Account_Profit;
                  }

               // Update the Total Profit label.
               Total_Profit_TextField.setText(String.format("$%.0f%n", Total_Profit));

               // Update the color.
               if (Total_Profit < 0.0)
                  {
                  Total_Profit_TextField.setForeground(Color.RED);
                  Total_Profit_TextField.setBackground(Color.WHITE);
                  }
               else if (Total_Profit > 0.0)
                  {
                  Total_Profit_TextField.setForeground(Color.BLACK);
                  Total_Profit_TextField.setBackground(Color.GREEN);
                  }
               }
            }
         }
      }

   /**
    *****************************************************************************
    */
   public String Get_Setup_Values(String StockSymbol)
      {
      // Loop through the Setup table until you find the given Stock Symbol.

      boolean Found = false;
      int Index = 0;

      for (int i = 0; i < countSymbolsEntered(); i++)
         {
         String TableSymbol = Symbol_Data_Table.getValueAt(i, 0).toString();

         if (TableSymbol.equalsIgnoreCase(StockSymbol))
            {
            Found = true;
            Index = i;

            break;
            }
         }

      if (Found)
         {
         // Return a comma delimited string containing the Min & Ave moves, the
         // Min & Max exit times, and the Percentage goal.
         return Symbol_Data_Table.getValueAt(Index, GUIData.minMoveColumn).toString()
            + "," + Symbol_Data_Table.getValueAt(Index, GUIData.aveMoveColumn).toString()
            + "," + Symbol_Data_Table.getValueAt(Index, GUIData.EOWMinColumn).toString()
            + "," + Symbol_Data_Table.getValueAt(Index, GUIData.EOWAveColumn).toString()
            + "," + Symbol_Data_Table.getValueAt(Index, GUIData.percentGoalColumn).toString();
         }
      else
         {
         return "0.0,0.0,0,0,0.0";
         }
      }

   /**
    This method is called from within the constructor to
    initialize the form.
    WARNING: Do NOT modify this code. The content of this method is
    always regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Main_Scrol_lPane = new javax.swing.JScrollPane();
        Tabs_Panel = new javax.swing.JTabbedPane();
        PES_Panel = new javax.swing.JPanel();
        Results_Scroll_Pane = new javax.swing.JScrollPane();
        Results_Text_Area = new javax.swing.JTextArea();
        Account_Setup_Panel = new javax.swing.JPanel();
        AppKey_Label = new javax.swing.JLabel();
        AppKey_Text_Field = new javax.swing.JTextField();
        Redirect_URI_Label = new javax.swing.JLabel();
        Redirect_URI_TextField = new javax.swing.JTextField();
        Load_Account_Info_Button = new javax.swing.JButton();
        Save_Account_Info_Button = new javax.swing.JButton();
        Max_Funds_Label1 = new javax.swing.JLabel();
        OpenTimeTextField = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        Accounts_Table = new javax.swing.JTable();
        Quote_Speed_Label = new javax.swing.JLabel();
        jComboBoxQuoteSpeed = new javax.swing.JComboBox<>();
        Number_Of_Strikes_Label = new javax.swing.JLabel();
        Number_Of_Strikes_Text_Field = new javax.swing.JTextField();
        Capture_Duration_Label = new javax.swing.JLabel();
        Capture_Duration_Text_Field = new javax.swing.JTextField();
        Wait_Duration_Label = new javax.swing.JLabel();
        StartMinutesB4Open = new javax.swing.JTextField();
        OrderCheckSpeed = new javax.swing.JLabel();
        OrderProcessingSpeed = new javax.swing.JTextField();
        Kill_Open_Orders_Check_Box = new javax.swing.JCheckBox();
        GUI_Only_Number_Of_Strikes_Label = new javax.swing.JLabel();
        GUI_Only_Number_Of_Strikes_Text_Field = new javax.swing.JTextField();
        Log_More_Data_Check_Box = new javax.swing.JCheckBox();
        Initial_Sell_2_Orders_Check_Box = new javax.swing.JCheckBox();
        Secret_Label = new javax.swing.JLabel();
        Secret_Field = new javax.swing.JPasswordField();
        Version_Label = new javax.swing.JLabel();
        Google_Spreadsheet_ID_Label = new javax.swing.JLabel();
        GoogleDateData = new javax.swing.JButton();
        LV_Log_File_Label1 = new javax.swing.JLabel();
        Download_Tab_Name_Text_Field = new javax.swing.JTextField();
        GoogleGenDayData = new javax.swing.JButton();
        LV_Log_File_Label2 = new javax.swing.JLabel();
        Gen_Prices_Tab_Name_Text_Field = new javax.swing.JTextField();
        Google_Spreadsheet_Name_Label = new javax.swing.JLabel();
        Google_Spreadsheet_ID_Text_Field = new javax.swing.JTextField();
        Login_Panel = new javax.swing.JPanel();
        TT_Login_Button = new javax.swing.JButton();
        TT_Refresh_Token_Label = new javax.swing.JLabel();
        TT_Refresh_Token_Text_Field = new javax.swing.JTextField();
        ClientSecret_Label = new javax.swing.JLabel();
        ClientSecret_Text_Field = new javax.swing.JPasswordField();
        TT_Access_Token_Label = new javax.swing.JLabel();
        TT_Access_Token_Text_Field = new javax.swing.JTextField();
        Symbol_Data_Scroll_Pane = new javax.swing.JScrollPane();
        Symbol_Data_Table = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        Schwab_Login_Start_Button = new javax.swing.JButton();
        Schwab_Login_Finish_Button = new javax.swing.JButton();
        Paste_URL_Text_Field = new javax.swing.JTextField();
        Access_Token_Label = new javax.swing.JLabel();
        Access_Token_Text_Field = new javax.swing.JTextField();
        Refresh_Token_Label = new javax.swing.JLabel();
        Refresh_Token_Text_Field = new javax.swing.JTextField();
        Load_Date_Text_Field = new javax.swing.JTextField();
        Load_Date_Label = new javax.swing.JLabel();
        Desired_Dollars_Lable = new javax.swing.JLabel();
        Desired_Dollars_Text_Field = new javax.swing.JTextField();
        OHLCButton = new javax.swing.JButton();
        GoogleLowValues = new javax.swing.JButton();
        LV_Log_File_Name = new javax.swing.JTextField();
        SelectLogFile = new javax.swing.JButton();
        LV_Log_File_Label = new javax.swing.JLabel();
        Start_Order_Processing_Button = new javax.swing.JButton();
        Kill_Open_Straddle_Orders_Button = new javax.swing.JButton();
        GUI_Only_Processing_Button = new javax.swing.JButton();
        Quotes_Only_Processing_Button = new javax.swing.JButton();
        Save_Table_Button = new javax.swing.JButton();
        Load_Table_Button = new javax.swing.JButton();
        Auto_Shut_Down_Check_Box = new javax.swing.JCheckBox();
        TestWeeklyPrep = new javax.swing.JButton();
        Monitor_Panel = new javax.swing.JPanel();
        Profit_Panel = new javax.swing.JPanel();
        Account_1_Profit_Label = new javax.swing.JLabel();
        Account_1_Profit_TextField = new javax.swing.JTextField();
        Account_2_Profit_Label = new javax.swing.JLabel();
        Account_2_Profit_TextField = new javax.swing.JTextField();
        Total_Profit_Label = new javax.swing.JLabel();
        Total_Profit_TextField = new javax.swing.JTextField();
        Account_3_Profit_Label = new javax.swing.JLabel();
        Account_3_Profit_TextField = new javax.swing.JTextField();
        Account_4_Profit_Label = new javax.swing.JLabel();
        Account_4_Profit_TextField = new javax.swing.JTextField();
        Account_5_Profit_Label = new javax.swing.JLabel();
        Account_5_Profit_TextField = new javax.swing.JTextField();
        Show_Sold_Positions_Checkbox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Options Trading Platform");
        setLocation(new java.awt.Point(0, 0));
        setName("MainFrame"); // NOI18N
        setSize(new java.awt.Dimension(2000, 900));
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });
        getContentPane().setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        Main_Scrol_lPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        Main_Scrol_lPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        Main_Scrol_lPane.setAlignmentX(0.0F);
        Main_Scrol_lPane.setAlignmentY(0.0F);
        Main_Scrol_lPane.setName("MainScrollPanel"); // NOI18N
        Main_Scrol_lPane.setPreferredSize(new java.awt.Dimension(1920, 1080));

        Tabs_Panel.setAutoscrolls(true);
        Tabs_Panel.setName("MainTabbedPanel"); // NOI18N
        Tabs_Panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                Tabs_PanelComponentHidden(evt);
            }
        });

        PES_Panel.setAutoscrolls(true);

        Results_Text_Area.setColumns(20);
        Results_Text_Area.setRows(5);
        Results_Scroll_Pane.setViewportView(Results_Text_Area);

        Account_Setup_Panel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        AppKey_Label.setText("App Key");

        AppKey_Text_Field.setText("cU8rPtgdUYZt2PvhBUZ6XqDyT1E5ekec");
        AppKey_Text_Field.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        AppKey_Text_Field.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AppKey_Text_FieldActionPerformed(evt);
            }
        });

        Redirect_URI_Label.setText("Redirect URI");

        Redirect_URI_TextField.setText("https://127.0.0.1");

        Load_Account_Info_Button.setText("Load Prf");
        Load_Account_Info_Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Load_Account_Info_ButtonActionPerformed(evt);
            }
        });

        Save_Account_Info_Button.setText("Save Prf");
        Save_Account_Info_Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Save_Account_Info_ButtonActionPerformed(evt);
            }
        });

        Max_Funds_Label1.setText("Open Time");

        OpenTimeTextField.setText("08:30");
        OpenTimeTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OpenTimeTextFieldActionPerformed(evt);
            }
        });

        Accounts_Table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Account Name", "Account Number", "Max Trades", "Max Funds", "Broker"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        Accounts_Table.setColumnSelectionAllowed(true);
        Accounts_Table.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(Accounts_Table);
        Accounts_Table.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (Accounts_Table.getColumnModel().getColumnCount() > 0) {
            Accounts_Table.getColumnModel().getColumn(0).setResizable(false);
            Accounts_Table.getColumnModel().getColumn(0).setPreferredWidth(125);
            Accounts_Table.getColumnModel().getColumn(1).setResizable(false);
            Accounts_Table.getColumnModel().getColumn(1).setPreferredWidth(125);
            Accounts_Table.getColumnModel().getColumn(2).setResizable(false);
            Accounts_Table.getColumnModel().getColumn(2).setPreferredWidth(75);
            Accounts_Table.getColumnModel().getColumn(3).setResizable(false);
            Accounts_Table.getColumnModel().getColumn(3).setPreferredWidth(75);
            Accounts_Table.getColumnModel().getColumn(4).setResizable(false);
            Accounts_Table.getColumnModel().getColumn(4).setPreferredWidth(75);
        }

        Quote_Speed_Label.setText("Quote Speed (1, 1.5, 3, or 5)");

        jComboBoxQuoteSpeed.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "1.5", "3", "5" }));

        Number_Of_Strikes_Label.setText("Processing Strikes per Symbol");

        Number_Of_Strikes_Text_Field.setText("4");
        Number_Of_Strikes_Text_Field.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Number_Of_Strikes_Text_FieldActionPerformed(evt);
            }
        });

        Capture_Duration_Label.setText("Process Time After Open (min) ");

        Capture_Duration_Text_Field.setText("4");
        Capture_Duration_Text_Field.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Capture_Duration_Text_FieldActionPerformed(evt);
            }
        });

        Wait_Duration_Label.setText("Start processing minutes b4 open");

        StartMinutesB4Open.setText("2");
        StartMinutesB4Open.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StartMinutesB4OpenActionPerformed(evt);
            }
        });

        OrderCheckSpeed.setText("Check 4 Orders Every X Seconds");

        OrderProcessingSpeed.setText("1");
        OrderProcessingSpeed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OrderProcessingSpeedActionPerformed(evt);
            }
        });

        Kill_Open_Orders_Check_Box.setText("Kill Open Straddle Orders after processing");
        Kill_Open_Orders_Check_Box.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Kill_Open_Orders_Check_BoxActionPerformed(evt);
            }
        });

        GUI_Only_Number_Of_Strikes_Label.setText("GUI Only Strikes per Symbol");

        GUI_Only_Number_Of_Strikes_Text_Field.setText("10");
        GUI_Only_Number_Of_Strikes_Text_Field.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GUI_Only_Number_Of_Strikes_Text_FieldActionPerformed(evt);
            }
        });

        Log_More_Data_Check_Box.setText("Log more data");
        Log_More_Data_Check_Box.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Log_More_Data_Check_BoxActionPerformed(evt);
            }
        });

        Initial_Sell_2_Orders_Check_Box.setText("Initial sell 3/4 order & 1/4 orders");
        Initial_Sell_2_Orders_Check_Box.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Initial_Sell_2_Orders_Check_BoxActionPerformed(evt);
            }
        });

        Secret_Label.setText("Secret");

        Secret_Field.setText("jPasswordField1");

        Version_Label.setFont(new java.awt.Font("Times New Roman", 1, 36)); // NOI18N
        Version_Label.setText("FIT GUI");

        Google_Spreadsheet_ID_Label.setText("Google Spreadsheet ID");

        GoogleDateData.setBackground(new java.awt.Color(102, 102, 255));
        GoogleDateData.setText("Google: Gen Table File");
        GoogleDateData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GoogleDateDataActionPerformed(evt);
            }
        });

        LV_Log_File_Label1.setText("Sheet: ");

        Download_Tab_Name_Text_Field.setText("New GUI Download");
        Download_Tab_Name_Text_Field.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Download_Tab_Name_Text_FieldActionPerformed(evt);
            }
        });

        GoogleGenDayData.setBackground(new java.awt.Color(102, 102, 255));
        GoogleGenDayData.setText("Google: Gen Prices");
        GoogleGenDayData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GoogleGenDayDataActionPerformed(evt);
            }
        });

        LV_Log_File_Label2.setText("Sheet: ");

        Gen_Prices_Tab_Name_Text_Field.setText("Current Q");
        Gen_Prices_Tab_Name_Text_Field.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Gen_Prices_Tab_Name_Text_FieldActionPerformed(evt);
            }
        });

        Google_Spreadsheet_Name_Label.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        Google_Spreadsheet_Name_Label.setForeground(new java.awt.Color(0, 0, 255));
        Google_Spreadsheet_Name_Label.setText("Spreadsheet Name: Morning Straddles");

        Google_Spreadsheet_ID_Text_Field.setText("Paste URL here");
        Google_Spreadsheet_ID_Text_Field.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Google_Spreadsheet_ID_Text_FieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout Account_Setup_PanelLayout = new javax.swing.GroupLayout(Account_Setup_Panel);
        Account_Setup_Panel.setLayout(Account_Setup_PanelLayout);
        Account_Setup_PanelLayout.setHorizontalGroup(
            Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Account_Setup_PanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(AppKey_Label, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(Redirect_URI_Label, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(AppKey_Text_Field, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addComponent(Redirect_URI_TextField, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE))
                    .addGroup(Account_Setup_PanelLayout.createSequentialGroup()
                        .addComponent(Load_Account_Info_Button)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Save_Account_Info_Button))
                    .addComponent(Secret_Label, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Secret_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(Account_Setup_PanelLayout.createSequentialGroup()
                        .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(Log_More_Data_Check_Box)
                            .addComponent(Initial_Sell_2_Orders_Check_Box))
                        .addGap(41, 41, 41)
                        .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(Number_Of_Strikes_Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(GUI_Only_Number_Of_Strikes_Label, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(GUI_Only_Number_Of_Strikes_Text_Field, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(Number_Of_Strikes_Text_Field, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(11, 11, 11))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 420, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(Account_Setup_PanelLayout.createSequentialGroup()
                        .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(Kill_Open_Orders_Check_Box)
                            .addGroup(Account_Setup_PanelLayout.createSequentialGroup()
                                .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(Capture_Duration_Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(OrderCheckSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(OrderProcessingSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(Capture_Duration_Text_Field, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(Account_Setup_PanelLayout.createSequentialGroup()
                                .addComponent(Quote_Speed_Label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBoxQuoteSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(Account_Setup_PanelLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(GoogleDateData)
                                    .addComponent(GoogleGenDayData))
                                .addGap(6, 6, 6)
                                .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(Account_Setup_PanelLayout.createSequentialGroup()
                                        .addGap(2, 2, 2)
                                        .addComponent(LV_Log_File_Label2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(Gen_Prices_Tab_Name_Text_Field, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(Account_Setup_PanelLayout.createSequentialGroup()
                                        .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(Account_Setup_PanelLayout.createSequentialGroup()
                                                .addGap(44, 44, 44)
                                                .addComponent(Download_Tab_Name_Text_Field, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(LV_Log_File_Label1))
                                        .addGap(0, 0, Short.MAX_VALUE))))
                            .addGroup(Account_Setup_PanelLayout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(Google_Spreadsheet_ID_Text_Field, javax.swing.GroupLayout.PREFERRED_SIZE, 397, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())))
                    .addGroup(Account_Setup_PanelLayout.createSequentialGroup()
                        .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(Account_Setup_PanelLayout.createSequentialGroup()
                                .addComponent(Wait_Duration_Label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(StartMinutesB4Open, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(Account_Setup_PanelLayout.createSequentialGroup()
                                .addComponent(Max_Funds_Label1, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(OpenTimeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(41, 41, 41)
                        .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(Account_Setup_PanelLayout.createSequentialGroup()
                                .addComponent(Version_Label, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Google_Spreadsheet_Name_Label, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(Google_Spreadsheet_ID_Label, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())))
        );
        Account_Setup_PanelLayout.setVerticalGroup(
            Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Account_Setup_PanelLayout.createSequentialGroup()
                .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(Account_Setup_PanelLayout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(Account_Setup_PanelLayout.createSequentialGroup()
                                .addGap(104, 104, 104)
                                .addComponent(Redirect_URI_TextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(Account_Setup_PanelLayout.createSequentialGroup()
                                .addComponent(AppKey_Label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(AppKey_Text_Field, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Secret_Label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(Secret_Field, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Redirect_URI_Label)
                                .addGap(31, 31, 31)))
                        .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(Load_Account_Info_Button)
                            .addComponent(Save_Account_Info_Button)))
                    .addGroup(Account_Setup_PanelLayout.createSequentialGroup()
                        .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(Max_Funds_Label1)
                            .addComponent(OpenTimeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(Wait_Duration_Label)
                            .addComponent(StartMinutesB4Open, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(Quote_Speed_Label)
                            .addComponent(jComboBoxQuoteSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(Capture_Duration_Label)
                            .addComponent(Capture_Duration_Text_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(OrderProcessingSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(OrderCheckSpeed))
                        .addGap(2, 2, 2)
                        .addComponent(Kill_Open_Orders_Check_Box))
                    .addGroup(Account_Setup_PanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(Google_Spreadsheet_Name_Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(Version_Label, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Google_Spreadsheet_ID_Label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Google_Spreadsheet_ID_Text_Field, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(22, 22, 22)
                        .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(GoogleDateData)
                            .addComponent(LV_Log_File_Label1)
                            .addComponent(Download_Tab_Name_Text_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(GoogleGenDayData)
                            .addComponent(LV_Log_File_Label2)
                            .addComponent(Gen_Prices_Tab_Name_Text_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, Account_Setup_PanelLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(Account_Setup_PanelLayout.createSequentialGroup()
                        .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(Number_Of_Strikes_Label)
                            .addComponent(Log_More_Data_Check_Box))
                        .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(Initial_Sell_2_Orders_Check_Box)
                            .addComponent(GUI_Only_Number_Of_Strikes_Label)))
                    .addGroup(Account_Setup_PanelLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addGroup(Account_Setup_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(GUI_Only_Number_Of_Strikes_Text_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(Account_Setup_PanelLayout.createSequentialGroup()
                                .addComponent(Number_Of_Strikes_Text_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(20, 20, 20)))))
                .addGap(10, 10, 10))
        );

        Login_Panel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        TT_Login_Button.setBackground(new java.awt.Color(0, 255, 51));
        TT_Login_Button.setText("TT Login");
        TT_Login_Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TT_Login_ButtonActionPerformed(evt);
            }
        });

        TT_Refresh_Token_Label.setText("Refresh Token = ");

        TT_Refresh_Token_Text_Field.setText("Refresh Token");
        TT_Refresh_Token_Text_Field.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TT_Refresh_Token_Text_FieldActionPerformed(evt);
            }
        });

        ClientSecret_Label.setText("Client Secret =");

        ClientSecret_Text_Field.setText("jPasswordField1");
        ClientSecret_Text_Field.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClientSecret_Text_FieldActionPerformed(evt);
            }
        });

        TT_Access_Token_Label.setText("Access Token = ");

        TT_Access_Token_Text_Field.setText("Access Token");
        TT_Access_Token_Text_Field.setEnabled(false);
        TT_Access_Token_Text_Field.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TT_Access_Token_Text_FieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout Login_PanelLayout = new javax.swing.GroupLayout(Login_Panel);
        Login_Panel.setLayout(Login_PanelLayout);
        Login_PanelLayout.setHorizontalGroup(
            Login_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Login_PanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(Login_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(Login_PanelLayout.createSequentialGroup()
                        .addComponent(TT_Login_Button)
                        .addGap(24, 24, 24)
                        .addComponent(ClientSecret_Label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(ClientSecret_Text_Field, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(Login_PanelLayout.createSequentialGroup()
                        .addComponent(TT_Access_Token_Label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(TT_Access_Token_Text_Field, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(TT_Refresh_Token_Label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(TT_Refresh_Token_Text_Field, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        Login_PanelLayout.setVerticalGroup(
            Login_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Login_PanelLayout.createSequentialGroup()
                .addGroup(Login_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TT_Login_Button)
                    .addComponent(ClientSecret_Label)
                    .addComponent(ClientSecret_Text_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(Login_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(Login_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(TT_Access_Token_Label)
                        .addComponent(TT_Access_Token_Text_Field, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(Login_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(TT_Refresh_Token_Label)
                        .addComponent(TT_Refresh_Token_Text_Field, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        Symbol_Data_Table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Symbol", "Max Price", "Buy Wiggle", "Min Move", "Ave Move", "EOW Min", "EOW Ave", "% Goal", "Chase Time", "Chase Wiggle", "Acct 1 $", "Acct 2 $", "Acct 3 $", "Acct 4 $", "Acct 5 $", "Min to Run", "Just Get In", "Blue Line", "# Strikes"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Integer.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        Symbol_Data_Table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        Symbol_Data_Table.setColumnSelectionAllowed(true);
        Symbol_Data_Table.getTableHeader().setReorderingAllowed(false);
        Symbol_Data_Scroll_Pane.setViewportView(Symbol_Data_Table);
        Symbol_Data_Table.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        if (Symbol_Data_Table.getColumnModel().getColumnCount() > 0) {
            Symbol_Data_Table.getColumnModel().getColumn(0).setResizable(false);
            Symbol_Data_Table.getColumnModel().getColumn(0).setPreferredWidth(50);
            Symbol_Data_Table.getColumnModel().getColumn(1).setResizable(false);
            Symbol_Data_Table.getColumnModel().getColumn(1).setPreferredWidth(65);
            Symbol_Data_Table.getColumnModel().getColumn(2).setResizable(false);
            Symbol_Data_Table.getColumnModel().getColumn(2).setPreferredWidth(70);
            Symbol_Data_Table.getColumnModel().getColumn(3).setResizable(false);
            Symbol_Data_Table.getColumnModel().getColumn(3).setPreferredWidth(65);
            Symbol_Data_Table.getColumnModel().getColumn(4).setResizable(false);
            Symbol_Data_Table.getColumnModel().getColumn(4).setPreferredWidth(65);
            Symbol_Data_Table.getColumnModel().getColumn(5).setResizable(false);
            Symbol_Data_Table.getColumnModel().getColumn(5).setPreferredWidth(60);
            Symbol_Data_Table.getColumnModel().getColumn(6).setResizable(false);
            Symbol_Data_Table.getColumnModel().getColumn(6).setPreferredWidth(60);
            Symbol_Data_Table.getColumnModel().getColumn(7).setResizable(false);
            Symbol_Data_Table.getColumnModel().getColumn(7).setPreferredWidth(60);
            Symbol_Data_Table.getColumnModel().getColumn(8).setResizable(false);
            Symbol_Data_Table.getColumnModel().getColumn(8).setPreferredWidth(70);
            Symbol_Data_Table.getColumnModel().getColumn(9).setResizable(false);
            Symbol_Data_Table.getColumnModel().getColumn(9).setPreferredWidth(80);
            Symbol_Data_Table.getColumnModel().getColumn(10).setResizable(false);
            Symbol_Data_Table.getColumnModel().getColumn(10).setPreferredWidth(65);
            Symbol_Data_Table.getColumnModel().getColumn(11).setResizable(false);
            Symbol_Data_Table.getColumnModel().getColumn(11).setPreferredWidth(65);
            Symbol_Data_Table.getColumnModel().getColumn(12).setResizable(false);
            Symbol_Data_Table.getColumnModel().getColumn(12).setPreferredWidth(65);
            Symbol_Data_Table.getColumnModel().getColumn(13).setResizable(false);
            Symbol_Data_Table.getColumnModel().getColumn(13).setPreferredWidth(65);
            Symbol_Data_Table.getColumnModel().getColumn(14).setResizable(false);
            Symbol_Data_Table.getColumnModel().getColumn(14).setPreferredWidth(65);
            Symbol_Data_Table.getColumnModel().getColumn(15).setResizable(false);
            Symbol_Data_Table.getColumnModel().getColumn(15).setPreferredWidth(70);
            Symbol_Data_Table.getColumnModel().getColumn(16).setResizable(false);
            Symbol_Data_Table.getColumnModel().getColumn(16).setPreferredWidth(65);
            Symbol_Data_Table.getColumnModel().getColumn(17).setResizable(false);
            Symbol_Data_Table.getColumnModel().getColumn(17).setPreferredWidth(60);
            Symbol_Data_Table.getColumnModel().getColumn(18).setResizable(false);
            Symbol_Data_Table.getColumnModel().getColumn(18).setPreferredWidth(60);
        }

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        Schwab_Login_Start_Button.setBackground(new java.awt.Color(0, 255, 51));
        Schwab_Login_Start_Button.setText("Login Start");
        Schwab_Login_Start_Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Schwab_Login_Start_ButtonActionPerformed(evt);
            }
        });

        Schwab_Login_Finish_Button.setBackground(new java.awt.Color(0, 255, 51));
        Schwab_Login_Finish_Button.setText("Login Finish");
        Schwab_Login_Finish_Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Schwab_Login_Finish_ButtonActionPerformed(evt);
            }
        });

        Paste_URL_Text_Field.setText("Paste URL here");
        Paste_URL_Text_Field.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Paste_URL_Text_FieldActionPerformed(evt);
            }
        });

        Access_Token_Label.setText("AccessToken = ");

        Access_Token_Text_Field.setText("Access Token");
        Access_Token_Text_Field.setEnabled(false);
        Access_Token_Text_Field.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Access_Token_Text_FieldActionPerformed(evt);
            }
        });

        Refresh_Token_Label.setText("Refresh Token = ");

        Refresh_Token_Text_Field.setText("Refresh Token");
        Refresh_Token_Text_Field.setEnabled(false);
        Refresh_Token_Text_Field.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Refresh_Token_Text_FieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(Schwab_Login_Finish_Button)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Access_Token_Label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Access_Token_Text_Field, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(Refresh_Token_Label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Refresh_Token_Text_Field, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(Schwab_Login_Start_Button)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Paste_URL_Text_Field, javax.swing.GroupLayout.PREFERRED_SIZE, 397, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(14, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Schwab_Login_Start_Button)
                    .addComponent(Paste_URL_Text_Field, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Schwab_Login_Finish_Button)
                    .addComponent(Access_Token_Label)
                    .addComponent(Access_Token_Text_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Refresh_Token_Label)
                    .addComponent(Refresh_Token_Text_Field, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        Load_Date_Text_Field.setText("12/31");
        Load_Date_Text_Field.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Load_Date_Text_FieldActionPerformed(evt);
            }
        });

        Load_Date_Label.setText("Date to load into table (MM/DD): ");

        Desired_Dollars_Lable.setText("Desired Dollars");

        Desired_Dollars_Text_Field.setText("45000.00");
        Desired_Dollars_Text_Field.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Desired_Dollars_Text_FieldActionPerformed(evt);
            }
        });

        OHLCButton.setText("Gen Quarterly Data");
        OHLCButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OHLCButtonActionPerformed(evt);
            }
        });

        GoogleLowValues.setBackground(new java.awt.Color(102, 102, 255));
        GoogleLowValues.setText("2) Google: Get Low Values");
        GoogleLowValues.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GoogleLowValuesActionPerformed(evt);
            }
        });

        LV_Log_File_Name.setText("PES_2020_...0021.log");
        LV_Log_File_Name.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LV_Log_File_NameActionPerformed(evt);
            }
        });

        SelectLogFile.setBackground(new java.awt.Color(102, 102, 255));
        SelectLogFile.setText("1) Select Log File ...");
        SelectLogFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SelectLogFileActionPerformed(evt);
            }
        });

        LV_Log_File_Label.setText("Log File Name:");

        Start_Order_Processing_Button.setBackground(new java.awt.Color(0, 255, 51));
        Start_Order_Processing_Button.setText("Start Order Processing");
        Start_Order_Processing_Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Start_Order_Processing_ButtonActionPerformed(evt);
            }
        });

        Kill_Open_Straddle_Orders_Button.setBackground(new java.awt.Color(0, 255, 51));
        Kill_Open_Straddle_Orders_Button.setText("Kill Open Straddle Orders");
        Kill_Open_Straddle_Orders_Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Kill_Open_Straddle_Orders_ButtonActionPerformed(evt);
            }
        });

        GUI_Only_Processing_Button.setBackground(new java.awt.Color(0, 255, 51));
        GUI_Only_Processing_Button.setText("GUI Only Processing");
        GUI_Only_Processing_Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GUI_Only_Processing_ButtonActionPerformed(evt);
            }
        });

        Quotes_Only_Processing_Button.setBackground(new java.awt.Color(102, 255, 102));
        Quotes_Only_Processing_Button.setText("Quotes Only Processing");
        Quotes_Only_Processing_Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Quotes_Only_Processing_ButtonActionPerformed(evt);
            }
        });

        Save_Table_Button.setText("Save Table");
        Save_Table_Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Save_Table_ButtonActionPerformed(evt);
            }
        });

        Load_Table_Button.setText("Load Table");
        Load_Table_Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Load_Table_ButtonActionPerformed(evt);
            }
        });

        Auto_Shut_Down_Check_Box.setText("Auto Shut Down");
        Auto_Shut_Down_Check_Box.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Auto_Shut_Down_Check_BoxActionPerformed(evt);
            }
        });

        TestWeeklyPrep.setBackground(new java.awt.Color(102, 102, 255));
        TestWeeklyPrep.setText("Google Week Prep");
        TestWeeklyPrep.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TestWeeklyPrepActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PES_PanelLayout = new javax.swing.GroupLayout(PES_Panel);
        PES_Panel.setLayout(PES_PanelLayout);
        PES_PanelLayout.setHorizontalGroup(
            PES_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PES_PanelLayout.createSequentialGroup()
                .addGroup(PES_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Symbol_Data_Scroll_Pane, javax.swing.GroupLayout.PREFERRED_SIZE, 1369, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(PES_PanelLayout.createSequentialGroup()
                        .addGroup(PES_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(PES_PanelLayout.createSequentialGroup()
                                .addGroup(PES_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(Login_Panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(PES_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(PES_PanelLayout.createSequentialGroup()
                                        .addComponent(Start_Order_Processing_Button)
                                        .addGap(31, 31, 31)
                                        .addComponent(Save_Table_Button))
                                    .addGroup(PES_PanelLayout.createSequentialGroup()
                                        .addComponent(GUI_Only_Processing_Button)
                                        .addGap(41, 41, 41)
                                        .addComponent(Load_Table_Button))
                                    .addGroup(PES_PanelLayout.createSequentialGroup()
                                        .addComponent(Kill_Open_Straddle_Orders_Button)
                                        .addGap(18, 18, 18)
                                        .addComponent(OHLCButton))
                                    .addGroup(PES_PanelLayout.createSequentialGroup()
                                        .addComponent(Quotes_Only_Processing_Button)
                                        .addGap(22, 22, 22)
                                        .addComponent(Auto_Shut_Down_Check_Box)))
                                .addGap(18, 18, 18)
                                .addGroup(PES_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(TestWeeklyPrep)
                                    .addGroup(PES_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(PES_PanelLayout.createSequentialGroup()
                                            .addComponent(LV_Log_File_Label)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(SelectLogFile)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(GoogleLowValues))
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, PES_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(LV_Log_File_Name, javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, PES_PanelLayout.createSequentialGroup()
                                                .addComponent(Load_Date_Text_Field, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(Load_Date_Label)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(Desired_Dollars_Lable)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(Desired_Dollars_Text_Field, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                            .addComponent(Account_Setup_Panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Results_Scroll_Pane, javax.swing.GroupLayout.PREFERRED_SIZE, 427, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(1663, Short.MAX_VALUE))
        );
        PES_PanelLayout.setVerticalGroup(
            PES_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PES_PanelLayout.createSequentialGroup()
                .addGroup(PES_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(PES_PanelLayout.createSequentialGroup()
                        .addComponent(Account_Setup_Panel, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addGroup(PES_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(PES_PanelLayout.createSequentialGroup()
                                .addGroup(PES_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(LV_Log_File_Label)
                                    .addComponent(SelectLogFile, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(GoogleLowValues)
                                    .addComponent(Start_Order_Processing_Button)
                                    .addComponent(Save_Table_Button))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(PES_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(LV_Log_File_Name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(GUI_Only_Processing_Button)
                                    .addComponent(Load_Table_Button))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(PES_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(Load_Date_Text_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(Load_Date_Label)
                                    .addComponent(Desired_Dollars_Text_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(Desired_Dollars_Lable)
                                    .addComponent(Kill_Open_Straddle_Orders_Button)
                                    .addComponent(OHLCButton))
                                .addGap(5, 5, 5)
                                .addGroup(PES_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(Quotes_Only_Processing_Button)
                                    .addComponent(Auto_Shut_Down_Check_Box)
                                    .addComponent(TestWeeklyPrep)))
                            .addGroup(PES_PanelLayout.createSequentialGroup()
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Login_Panel, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(Results_Scroll_Pane))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Symbol_Data_Scroll_Pane, javax.swing.GroupLayout.PREFERRED_SIZE, 475, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(3845, Short.MAX_VALUE))
        );

        Tabs_Panel.addTab("FIT", PES_Panel);

        Monitor_Panel.setAutoscrolls(true);

        Profit_Panel.setBackground(new java.awt.Color(204, 204, 204));
        Profit_Panel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        Profit_Panel.setEnabled(false);
        Profit_Panel.setPreferredSize(new java.awt.Dimension(1155, 40));

        Account_1_Profit_Label.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        Account_1_Profit_Label.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        Account_1_Profit_Label.setText("Account 1:");
        Account_1_Profit_Label.setAutoscrolls(true);
        Account_1_Profit_Label.setPreferredSize(new java.awt.Dimension(80, 20));

        Account_1_Profit_TextField.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        Account_1_Profit_TextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        Account_1_Profit_TextField.setText("$0");
        Account_1_Profit_TextField.setPreferredSize(new java.awt.Dimension(80, 20));
        Account_1_Profit_TextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Account_1_Profit_TextFieldActionPerformed(evt);
            }
        });

        Account_2_Profit_Label.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        Account_2_Profit_Label.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        Account_2_Profit_Label.setText("Account 2:");
        Account_2_Profit_Label.setAutoscrolls(true);
        Account_2_Profit_Label.setPreferredSize(new java.awt.Dimension(80, 20));

        Account_2_Profit_TextField.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        Account_2_Profit_TextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        Account_2_Profit_TextField.setText("$0");
        Account_2_Profit_TextField.setPreferredSize(new java.awt.Dimension(80, 20));

        Total_Profit_Label.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        Total_Profit_Label.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        Total_Profit_Label.setText("Total:");
        Total_Profit_Label.setAutoscrolls(true);
        Total_Profit_Label.setPreferredSize(new java.awt.Dimension(80, 20));

        Total_Profit_TextField.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        Total_Profit_TextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        Total_Profit_TextField.setText("$0");
        Total_Profit_TextField.setPreferredSize(new java.awt.Dimension(80, 20));

        Account_3_Profit_Label.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        Account_3_Profit_Label.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        Account_3_Profit_Label.setText("Account 3:");
        Account_3_Profit_Label.setAutoscrolls(true);
        Account_3_Profit_Label.setPreferredSize(new java.awt.Dimension(80, 20));

        Account_3_Profit_TextField.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        Account_3_Profit_TextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        Account_3_Profit_TextField.setText("$0");
        Account_3_Profit_TextField.setPreferredSize(new java.awt.Dimension(80, 20));

        Account_4_Profit_Label.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        Account_4_Profit_Label.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        Account_4_Profit_Label.setText("Account 4:");
        Account_4_Profit_Label.setAutoscrolls(true);
        Account_4_Profit_Label.setPreferredSize(new java.awt.Dimension(80, 20));

        Account_4_Profit_TextField.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        Account_4_Profit_TextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        Account_4_Profit_TextField.setText("$0");
        Account_4_Profit_TextField.setPreferredSize(new java.awt.Dimension(80, 20));

        Account_5_Profit_Label.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        Account_5_Profit_Label.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        Account_5_Profit_Label.setText("Account 5:");
        Account_5_Profit_Label.setAutoscrolls(true);
        Account_5_Profit_Label.setPreferredSize(new java.awt.Dimension(80, 20));

        Account_5_Profit_TextField.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        Account_5_Profit_TextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        Account_5_Profit_TextField.setText("$0");
        Account_5_Profit_TextField.setPreferredSize(new java.awt.Dimension(80, 20));

        javax.swing.GroupLayout Profit_PanelLayout = new javax.swing.GroupLayout(Profit_Panel);
        Profit_Panel.setLayout(Profit_PanelLayout);
        Profit_PanelLayout.setHorizontalGroup(
            Profit_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, Profit_PanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(Account_1_Profit_Label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(Account_1_Profit_TextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(Account_2_Profit_Label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(Account_2_Profit_TextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(Account_3_Profit_Label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(Account_3_Profit_TextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(Account_4_Profit_Label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(Account_4_Profit_TextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(Account_5_Profit_Label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(Account_5_Profit_TextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 61, Short.MAX_VALUE)
                .addComponent(Total_Profit_Label, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Total_Profit_TextField, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
        );
        Profit_PanelLayout.setVerticalGroup(
            Profit_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, Profit_PanelLayout.createSequentialGroup()
                .addContainerGap(12, Short.MAX_VALUE)
                .addGroup(Profit_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(Profit_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(Account_5_Profit_Label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(Account_5_Profit_TextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(Profit_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(Account_4_Profit_Label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(Account_4_Profit_TextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(Profit_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(Account_3_Profit_Label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(Account_3_Profit_TextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(Profit_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(Account_1_Profit_Label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(Account_1_Profit_TextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(Account_2_Profit_Label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(Account_2_Profit_TextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(Total_Profit_Label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(Total_Profit_TextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        Show_Sold_Positions_Checkbox.setText("Show Sold Positions");
        Show_Sold_Positions_Checkbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Show_Sold_Positions_CheckboxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout Monitor_PanelLayout = new javax.swing.GroupLayout(Monitor_Panel);
        Monitor_Panel.setLayout(Monitor_PanelLayout);
        Monitor_PanelLayout.setHorizontalGroup(
            Monitor_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Monitor_PanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(Profit_Panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Show_Sold_Positions_Checkbox)
                .addContainerGap(2073, Short.MAX_VALUE))
        );
        Monitor_PanelLayout.setVerticalGroup(
            Monitor_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Monitor_PanelLayout.createSequentialGroup()
                .addGroup(Monitor_PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(Monitor_PanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(Profit_Panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(Monitor_PanelLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(Show_Sold_Positions_Checkbox)))
                .addContainerGap(4592, Short.MAX_VALUE))
        );

        Tabs_Panel.addTab("Monitor", Monitor_Panel);

        Main_Scrol_lPane.setViewportView(Tabs_Panel);
        Tabs_Panel.getAccessibleContext().setAccessibleName("Login");

        getContentPane().add(Main_Scrol_lPane);

        setSize(new java.awt.Dimension(1843, 883));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void ClearTable( boolean FirstTime) {
      // Setup jTableSymbolData columns (except symbolLabel column) with default values to make life easier for entry;
      for (int ccc = 0; ccc < Symbol_Data_Table.getRowCount(); ccc++)
         {
         for (int ddd = 0; ddd < Symbol_Data_Table.getColumnCount(); ddd++)
            {
            if ( (Symbol_Data_Table.getValueAt(ccc, ddd) == null) || !FirstTime )
               {  // Don't overwrite anything that is already set somehow
               switch (ddd)
                  {
                  case GUIData.symbolColumn:  // Do nothing, we want this to be null unless user enters a symbolLabel
                      if ( !FirstTime ) {
                         Symbol_Data_Table.setValueAt(null, ccc, GUIData.symbolColumn);  // Column 0 is the symbol.  Set it to null so things work right.
                      }
                     break;
                  case GUIData.maxPriceColumn:
                     Symbol_Data_Table.setValueAt(maxPriceDefault, ccc, GUIData.maxPriceColumn);
                     break;
                  case GUIData.buyWiggleColumn:
                     Symbol_Data_Table.setValueAt(buyWiggleDefault, ccc, GUIData.buyWiggleColumn);
                     break;
                  case GUIData.minMoveColumn:
                     Symbol_Data_Table.setValueAt(minMoveDefault, ccc, GUIData.minMoveColumn);
                     break;
                  case GUIData.aveMoveColumn:
                     Symbol_Data_Table.setValueAt(aveMoveDefault, ccc, GUIData.aveMoveColumn);
                     break;
                  case GUIData.EOWMinColumn:
                     Symbol_Data_Table.setValueAt(EOWMinDefault, ccc, GUIData.EOWMinColumn);
                     break;
                  case GUIData.EOWAveColumn:
                     Symbol_Data_Table.setValueAt(EOWAveDefault, ccc, GUIData.EOWAveColumn);
                     break;
                  case GUIData.percentGoalColumn:
                     Symbol_Data_Table.setValueAt(percentGoalDefault, ccc, GUIData.percentGoalColumn);
                     break;
                  case GUIData.chaseWaitTimeColumn:
                     Symbol_Data_Table.setValueAt(chaseWaitTimeDefault, ccc, GUIData.chaseWaitTimeColumn);
                     break;
                  case GUIData.chaseAmountColumn:
                     Symbol_Data_Table.setValueAt(chaseAmountDefault, ccc, GUIData.chaseAmountColumn);
                     break;
                  case GUIData.tradeAmount1Column:
                     Symbol_Data_Table.setValueAt(tradeAmountDefault, ccc, GUIData.tradeAmount1Column);
                     break;
                  case GUIData.tradeAmount2Column:
                     Symbol_Data_Table.setValueAt(tradeAmountDefault, ccc, GUIData.tradeAmount2Column);
                     break;
                  case GUIData.tradeAmount3Column:
                     Symbol_Data_Table.setValueAt(tradeAmountDefault, ccc, GUIData.tradeAmount3Column);
                     break;
                  case GUIData.tradeAmount4Column:
                     Symbol_Data_Table.setValueAt(tradeAmountDefault, ccc, GUIData.tradeAmount4Column);
                     break;
                  case GUIData.tradeAmount5Column:
                     Symbol_Data_Table.setValueAt(tradeAmountDefault, ccc, GUIData.tradeAmount5Column);
                     break;
                  case GUIData.minutesToRunColumn:
                     Symbol_Data_Table.setValueAt(minutesToRunDefault, ccc, GUIData.minutesToRunColumn);
                     break;
                  case GUIData.justGetInColumn:
                     Symbol_Data_Table.setValueAt(justGetInDefault, ccc, GUIData.justGetInColumn);
                     break;
                  case GUIData.blueLineColumn:
                     Symbol_Data_Table.setValueAt(blueLineDefault, ccc, GUIData.blueLineColumn);
                     break;
                  case GUIData.numberOfStrikesColumn:
                     Symbol_Data_Table.setValueAt(numberOfStrikesDefault, ccc, GUIData.numberOfStrikesColumn);
                     break;
                  default:
                     Symbol_Data_Table.setValueAt(0, ccc, ddd);
                     break;
                  }  // Switch
               }   // If
            }  // for ddd
         } // for ccc
    }

    private void LoadTable(boolean getName) {
      try
         {
         ClearTable( false );
         String fileNameToUse = Config.TABLEDATA_FILENAME;
         if (getName == true) {
             fileNameToUse = getFileName( Config.TABLEDATA_FILENAME );
         }
         File tableFile = new File(fileNameToUse);
         Scanner inputStream = new Scanner(tableFile);  // Read the file.
         Integer row = 0;
         while (inputStream.hasNext())
            {
            String data = inputStream.nextLine();     // Read the next line.
            String[] values = data.split(",");        // Get the values from the line.

            for (int col = 0; col <= lastColumn; col++)
               {
               // Need to convert each column to appropriate type before writing into the table
               // Strings
               if ((col == GUIData.symbolColumn)
                  || (col == GUIData.blueLineColumn) 
                  || (col == GUIData.justGetInColumn))
                  {
                      try {
                          if (values[col].length() > 1) {
                            if (col == GUIData.blueLineColumn) {
                                Symbol_Data_Table.setValueAt(blueLineDefault, row, col);
                            } else {
                                if (col == GUIData.justGetInColumn) {
                                    Symbol_Data_Table.setValueAt(justGetInDefault, row, col);
                                } else {
                                    Symbol_Data_Table.setValueAt(values[col], row, col);
                                }
                            }
                          } else {
                              Symbol_Data_Table.setValueAt(values[col], row, col);
                          }
                      } catch (Exception ex) {
                          // This must be an older table file version so use default values
                          if (col == GUIData.blueLineColumn) {
                              Symbol_Data_Table.setValueAt(blueLineDefault, row, col);
                          } else {
                              if (col == GUIData.justGetInColumn) {
                                  Symbol_Data_Table.setValueAt(justGetInDefault, row, col);
                              }
                          }
                      }
                  }
               else
                  {
                  // 1/12/24 Min Wait now is EOW Min Move and Ave Wait is now EOW Ave Move
//                  if ((col == GUIData.minWaitColumn)
//                     || (col == GUIData.chaseWaitTimeColumn)
//                     || (col == GUIData.maxWaitColumn))
                  // Integers
                  if ((col == GUIData.chaseWaitTimeColumn)
                     || (col == GUIData.numberOfStrikesColumn) 
                     || (col == GUIData.minutesToRunColumn))
                     {
                         try {
                             Symbol_Data_Table.setValueAt(Integer.parseInt(values[col]), row, col);
                         } catch (Exception ex) {
                             // This must be an older table file version so use default values
                             if (col == GUIData.numberOfStrikesColumn) {
                                  Symbol_Data_Table.setValueAt(numberOfStrikesDefault, row, col);
                             } else {
                                  if (col == GUIData.minutesToRunColumn) {
                                      Symbol_Data_Table.setValueAt(minutesToRunDefault, row, col);
                                  }
                            }
                         }
                     }
                  else
                     // Doubles
                     {
                     Symbol_Data_Table.setValueAt(Double.parseDouble(values[col]), row, col);
                     }
                  }
               }
            row++;
            }

            // Close the file.
            inputStream.close();
         }
      catch (Exception ex)
         {
         Utility.dumpExceptionInfo(ex);
         ex.printStackTrace();
         }
    }
    
   private void SaveTable() {
      try
         {
         PrintWriter tableWriter = new PrintWriter(Config.TABLEDATA_FILENAME);
         // Loop thru the rows and columns and create a CSV file
         for (int row = 0; row < countSymbolsEntered(); row++)
            {
            for (int col = 0; col <= lastColumn; col++)
               {
               tableWriter.print(Symbol_Data_Table.getValueAt(row, col).toString() + ",");
               }
            tableWriter.println("EOL");
            }
         tableWriter.close();
         }
      catch (Exception ex)
         {
         Utility.dumpExceptionInfo(ex);
         ex.printStackTrace();
         }
      try {
           while ( !GUIData.LockOrderSocketData() ) { Utility.hybridPrecisionWait(10);}
           GUIData.SetOrdersSocketData( GUIData.LoadTable );
        } catch (Exception e) {
            Utility.dumpExceptionInfo(e);
            e.printStackTrace();
        }
   }
   
    private void updateGUIs(){
        boolean processingEnabled = ((TT_API.getAccessToken().equalsIgnoreCase("") == false) && (SchwabAPI.getAccessToken().equalsIgnoreCase("") == false));
        // Update all the tokens
        TT_Access_Token_Text_Field.setText(TT_API.getAccessToken());
        Access_Token_Text_Field.setText( SchwabAPI.getAccessToken() );
        Refresh_Token_Text_Field.setText( SchwabAPI.getRefreshToken() );      
        if (TT_API.getAccessToken().equalsIgnoreCase("") == false) {
            Quotes_Only_Processing_Button.setEnabled(true);
        }
        // Deal with processing buttons
        if (processingEnabled == true) {
            Kill_Open_Straddle_Orders_Button.setEnabled(true);
            Start_Order_Processing_Button.setEnabled(true);
            GUI_Only_Processing_Button.setEnabled(true);
            GoogleGenDayData.setEnabled(true);
        }
        TT_API.SaveTokens();
        SchwabAPI.SaveToken();
        // Send the data over to the orders gui
        try {
            if (SchwabAPI.getAccessToken().equalsIgnoreCase("") == false) {
                while ( !GUIData.LockOrderSocketData() ) { Utility.hybridPrecisionWait(10);}
                GUIData.SetOrdersSocketData( GUIData.TokenUpdate + "," + SchwabAPI.getRefreshToken() + "," + SchwabAPI.getAccessToken() + ", SCHWAB" );
            }
        } catch (Exception e) {
              Utility.dumpExceptionInfo(e, "Failed to send token data to orders GUI");
              e.printStackTrace();
        }
   }

    //  This will get existing position before we start order processing.  This then stores that data in GUIData.
    //  This is done so that we can avoid placing a straddle order over top of an existing option position for a particular stock.
    private JSONArray getExistingPositions( JSONArray allAcctInfo, String[][] acctNums, String whichBroker ) {
        JSONArray results = new JSONArray();
        try {
            JSONArray allAccts = SchwabAPI.getAccounts(true);
            for ( int a = 0; a < acctNums.length; a++) {
                if ( ( whichBroker.equalsIgnoreCase( GUIData.allBrokers ) ) ||
                     ( acctNums[a][1].equalsIgnoreCase( whichBroker ) ) ) {
                    JSONObject response = SchwabAPI.getAccountInfo(acctNums[a][0], allAccts);
                    JSONObject acctInfo = new JSONObject(response.getJSONObject("securitiesAccount").toString());

                    // If there is a position, then find the corresponding sell order.   If it isn't there then place one.
                    if( acctInfo.has("positions") ){  
                        if( acctInfo.isNull("positions") ){  
                            // Nothing to do - no positions
                        }  else { // For each position record the Symbol, Strike, AcctNumber as string in a JSONObject and add that to JSONArray.                
                            // Loop thru each position here
                            JSONArray AllPositions = new JSONArray( acctInfo.getJSONArray("positions").toString() );
                            for ( int PositionIndex = 0; PositionIndex < AllPositions.length(); PositionIndex++ ) {
                                // Get the details about the position needed
                                JSONObject aPosition = new JSONObject( AllPositions.getJSONObject(PositionIndex).toString() );
                                // These will be listed as Calls and Puts.  So I will have to pair them up before looking at the order strategies
                                JSONObject instrument = new JSONObject( aPosition.getJSONObject("instrument").toString() );
                                if ( instrument.getString("assetType").equalsIgnoreCase("OPTION") ) {
                                    JSONObject info = new JSONObject();
                                    info.put("symbol", instrument.getString("underlyingSymbol") );
                                    info.put("accountNum", acctNums[a]);
                                    info.put("optionSymbol", instrument.getString("symbol"));                                
                                    results.put(info);
                                } // if AssetType = OPTION
                            } // OrderIndex loop
                        } // Else            
                    }   // Has positions     
                }
            }             
        } catch (Exception ex) {
          Results_Text_Area.append("FAILED to get existing positions!!!!!");
          Utility.dumpExceptionInfo(ex);
          ex.printStackTrace();
        }
        return results;
    }    
    
   private void StartProcessing(boolean GUIOnly)
      {
      GoogleGenDayData.setEnabled(false);
      GUIData.setOpenTime( OpenTimeTextField.getText() );
      GUIData.setDefaultRunTime( Integer.parseInt(Capture_Duration_Text_Field.getText()) );
      if (GUIData.getOrderProcessing())
         {
         LogData.LogThis(Level.INFO, "Orders already processing.");
         Results_Text_Area.append(Utility.getTimeStamp() + "\n \n ORDERS ALREADY PROCESSING. \n \n");
         }
      else
         {             
         GUIData.setOrderProcessing(true);   // It is set to true here, but in reality we are only doing quote processing.  So perhaps remove.
         this.setTitle(this.getTitle() + " - Processing Orders Run ...");

         int j = jComboBoxQuoteSpeed.getSelectedIndex();
         String Speed = getSpeed();                 // TBD
         GUIData.setQuoteSpeed(Speed);        // TBD
         GUIData.setStrikesPerSymbol(Integer.parseInt(Number_Of_Strikes_Text_Field.getText()));

         String[] table = new String[countSymbolsEntered()];
         int captureTime = Integer.parseInt(Capture_Duration_Text_Field.getText().toString());

         // Kick off the quote, positions, orders processing thread
         // This doesn't account for the same symbolLabel entered on multiple lines.  That is an issue when quote processing is done.
         //      However, this has been addressed with Streaming - it builds a iist of symbols and removes duplicates.
         //     We also need to determine how long to run, there is the global setting of minutes.
         for (int i = 0; i < countSymbolsEntered(); i++)
            {
                table[i] = Symbol_Data_Table.getValueAt(i, GUIData.symbolColumn).toString().toUpperCase();
                if ( Integer.parseInt( Symbol_Data_Table.getValueAt(i, GUIData.minutesToRunColumn).toString() ) > captureTime ) {
                    captureTime = Integer.parseInt( Symbol_Data_Table.getValueAt(i, GUIData.minutesToRunColumn).toString() );
                }
            }
         int acctCount = countAccountsEntered(GUIData.allBrokers);
         String[][] accts = new String[acctCount][2];   // This will hold the accts number and broker name.
         double[] funds = new double[acctCount];
         int[] trades = new int[acctCount];
         for (int abc = 0; abc < acctCount; abc++)
            {
            accts[abc][0] = Accounts_Table.getValueAt(abc, GUIData.accountNumberColumn).toString();
            accts[abc][1] = Accounts_Table.getValueAt(abc, GUIData.brokerNameColumn).toString();
            funds[abc] = Double.parseDouble(Accounts_Table.getValueAt(abc, GUIData.maxFundsColumn).toString());
            trades[abc] = Integer.parseInt(Accounts_Table.getValueAt(abc, GUIData.maxTradesColumn).toString());
            }
         GUIData.setAccounts(accts);
         GUIData.setOrderProcessingSpeed( this.OrderProcessingSpeed.getText() );

         // Now figure out the actual wait duration.
         startTime = LocalTime.now();
         DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
         try {
            openTime = LocalTime.parse( OpenTimeTextField.getText(), dtf );
            if (LocalTime.now().isAfter(openTime)) {
                startTime = LocalTime.now();   // Since starting during market hours let it have 1 min for startup activitiy.
                endTime = startTime.plusMinutes( Integer.parseInt(Capture_Duration_Text_Field.getText() ) );
            } else {
                startTime = openTime.minusMinutes( Integer.parseInt( StartMinutesB4Open.getText() ) );
                endTime = openTime.plusMinutes( Integer.parseInt(Capture_Duration_Text_Field.getText() ) );
            }
            shutDownTime = endTime.plusMinutes( 3 );    // Use 3 minutes passed for things to shut down 
         } catch (Exception ex) {
            Utility. dumpExceptionInfo(ex, Results_Text_Area, " Exception 2 CheckForTradesStreamingThread.  Keep on processing.");
         }
// tbd On orders side
         // Last activity here is to set any open positions before starting processing so we can avoid conflicts.
         GUIData.setPositionsBeforeRunning( getExistingPositions( SchwabAPI.getAccounts(true), accts, GUIData.charlesSchwab ) );        // TBD

            // Let the orders client know to start processing
            try {
               while ( !GUIData.LockOrderSocketData() ) { Thread.sleep(10);}
               GUIData.SetOrdersSocketData( GUIData.StartProcessing );
            } catch (Exception e) {
                Utility.dumpExceptionInfo(e);
                e.printStackTrace();
            }  

         }
      this.Start_Order_Processing_Button.setEnabled(false);
      this.GUI_Only_Processing_Button.setEnabled(false);
      this.Quotes_Only_Processing_Button.setEnabled(false);
      // Not able to be pressed once processing has begun.  Save Table works still to get the data over to the other applications for last minute changes to existing rows.
      Load_Table_Button.setEnabled(false);
      }
    
   private void StartGUIProcessing()
      {
      GoogleGenDayData.setEnabled(false);
      GUIData.setOpenTime( OpenTimeTextField.getText() );
      if (GUIData.getOrderProcessing())
         {
         LogData.LogThis(Level.INFO, "Already processing.");
         Results_Text_Area.append(Utility.getTimeStamp() + "\n \n ALREADY PROCESSING. \n \n");
         }
      else
         {             
         GUIData.setOrderProcessing(true);   // It is set to true here, but in reality we are only doing quote processing.  So perhaps remove.
         this.setTitle(this.getTitle() + " - GUI Only processing Run ...");
         int j = jComboBoxQuoteSpeed.getSelectedIndex();
         String Speed = getSpeed();                 // TBD
         GUIData.setQuoteSpeed(Speed);        // TBD
         GUIData.setStrikesPerSymbol(Integer.parseInt(GUI_Only_Number_Of_Strikes_Text_Field.getText()));

         String[] table = new String[countSymbolsEntered()];

         // Kick off the quote, positions, orders processing thread
         // This doesn't account for the same symbolLabel entered on multiple lines.  That is an issue when quote processing is done.
         //      However, this has been addressed with Streaming - it builds a iist of symbols and removes duplicates.
         for (int i = 0; i < countSymbolsEntered(); i++)
            {
            table[i] = Symbol_Data_Table.getValueAt(i, GUIData.symbolColumn).toString().toUpperCase();
            }
         int acctCount = countAccountsEntered(GUIData.allBrokers);
         String[][] accts = new String[acctCount][2];
         for (int abc = 0; abc < acctCount; abc++)
            {
            accts[abc][0] = Accounts_Table.getValueAt(abc, GUIData.accountNumberColumn).toString();
            accts[abc][1] = Accounts_Table.getValueAt(abc, GUIData.brokerNameColumn).toString();
            }
         GUIData.setAccounts(accts);
         GUIData.setOrderProcessingSpeed( this.OrderProcessingSpeed.getText() );

            // Let the orders client know to start processing
            try {
               while ( !GUIData.LockOrderSocketData() ) { Thread.sleep(10);}
               GUIData.SetOrdersSocketData( GUIData.StartGUIOnlyProcessing );
            } catch (Exception e) {
                Utility.dumpExceptionInfo(e);
                e.printStackTrace();
            }  
         }
      this.GUI_Only_Processing_Button.setEnabled(false);
      this.Start_Order_Processing_Button.setEnabled(false);
      this.Quotes_Only_Processing_Button.setEnabled(false);
      // Not able to be pressed once processing has begun.  Save Table works still to get the data over to the other applications for last minute changes to existing rows.
      Load_Table_Button.setEnabled(false);
      }
   
    
    private void StartQuoteProcessing() {
        if (GUIData.getOrdersSocketInitDone() == false) {
           LogData.LogThis(Level.INFO, "Quotes application not running!");
           Results_Text_Area.append(Utility.getTimeStamp() + "\n \n Quotes application not running! \n \n");
        } else {
            GoogleGenDayData.setEnabled(false);
            GUIData.setOpenTime( OpenTimeTextField.getText() );
            if (GUIData.getOrderProcessing())
               {
               LogData.LogThis(Level.INFO, "Already processing.");
               Results_Text_Area.append(Utility.getTimeStamp() + "\n \n ALREADY PROCESSING. \n \n");
               }
            else
               {             
               GUIData.setOrderProcessing(true);   // It is set to true here, but in reality we are only doing quote processing.  So perhaps remove.
               this.setTitle(this.getTitle() + " - Quote Only processing Run ...");
               int j = jComboBoxQuoteSpeed.getSelectedIndex();
               String Speed = getSpeed();                 
               GUIData.setQuoteSpeed(Speed);        
               GUIData.setStrikesPerSymbol(Integer.parseInt(GUI_Only_Number_Of_Strikes_Text_Field.getText()));

               String[] table = new String[countSymbolsEntered()];
               // This doesn't account for the same symbolLabel entered on multiple lines.  That is an issue when quote processing is done.
               //      However, this has been addressed with Streaming - it builds a iist of symbols and removes duplicates.
               for (int i = 0; i < countSymbolsEntered(); i++)
                  {
                  table[i] = Symbol_Data_Table.getValueAt(i, GUIData.symbolColumn).toString().toUpperCase();
                  }
               int captureTime = Integer.parseInt(Capture_Duration_Text_Field.getText().toString());

               // We also need to determine how long to run, there is the global setting of minutes.
               for (int i = 0; i < countSymbolsEntered(); i++)
                  {
                      table[i] = Symbol_Data_Table.getValueAt(i, GUIData.symbolColumn).toString().toUpperCase();
                      if ( Integer.parseInt( Symbol_Data_Table.getValueAt(i, GUIData.minutesToRunColumn).toString() ) > captureTime ) {
                          captureTime = Integer.parseInt( Symbol_Data_Table.getValueAt(i, GUIData.minutesToRunColumn).toString() );
                      }
                  }
               GUIData.setOrderProcessingSpeed( this.OrderProcessingSpeed.getText() );
                autoShutDown = true;
                this.Auto_Shut_Down_Check_Box.setSelected(true);

                // Now figure out the actual wait duration.
                startTime = LocalTime.now();
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
                try {
                   openTime = LocalTime.parse( OpenTimeTextField.getText(), dtf );
                   if (LocalTime.now().isAfter(openTime)) {
                       startTime = LocalTime.now();   // Since starting during market hours let it have 1 min for startup activitiy.
                       endTime = startTime.plusMinutes( Integer.parseInt(Capture_Duration_Text_Field.getText() ) );
                   } else {
                       startTime = openTime.minusMinutes( Integer.parseInt( StartMinutesB4Open.getText() ) );
                       endTime = openTime.plusMinutes( Integer.parseInt(Capture_Duration_Text_Field.getText() ) );
                   }
                   shutDownTime = endTime.plusSeconds( 90 );    // Use 90 seconds and then shut things down 
                   LogData.LogThis(Level.INFO, "Shut down time = " + shutDownTime.toString() );
                   Results_Text_Area.append(Utility.getTimeStamp() + "\n \n Shut down time = " + shutDownTime.toString() + " \n \n");
                } catch (Exception ex) {
                   Utility. dumpExceptionInfo(ex, Results_Text_Area, " Exception 2 CheckForTradesStreamingThread.  Keep on processing.");
                }

                // Let the orders client know to start processing
                try {
                   while ( !GUIData.LockOrderSocketData() ) { Thread.sleep(10);}
                   GUIData.SetOrdersSocketData( GUIData.StartQuotesOnlyProcessing );
                } catch (Exception e) {
                    Utility.dumpExceptionInfo(e);
                    e.printStackTrace();
                }  
               }
            this.GUI_Only_Processing_Button.setEnabled(false);
            this.Start_Order_Processing_Button.setEnabled(false);
            this.Quotes_Only_Processing_Button.setEnabled(false);
            // Not able to be pressed once processing has begun.  Save Table works still to get the data over to the other applications for last minute changes to existing rows.
            Load_Table_Button.setEnabled(false);
            Schwab_Login_Start_Button.setEnabled(false);
            Schwab_Login_Finish_Button.setEnabled(false);
        }
    }
   
    private void LogTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LogTestActionPerformed

      ClassLoader cl = ClassLoader.getSystemClassLoader();

      URL[] urls = ((URLClassLoader) cl).getURLs();

      for (URL url : urls)
         {
         LogData.LogThis(Level.INFO, url.getFile());
         }

      // TODO add your handling code here:
      Log4J2AsyncLogger log4J2AsyncLogger = new Log4J2AsyncLogger();
      log4J2AsyncLogger.performSomeTask();
      log4J2AsyncLogger.LogThis(Level.ERROR, "Testing out ERROR");
      log4J2AsyncLogger.performSomeTask();
    }//GEN-LAST:event_LogTestActionPerformed

    
    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_formKeyPressed

    private void Tabs_PanelComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_Tabs_PanelComponentHidden
        // TODO add your handling code here:
    }//GEN-LAST:event_Tabs_PanelComponentHidden

    private void Show_Sold_Positions_CheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Show_Sold_Positions_CheckboxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Show_Sold_Positions_CheckboxActionPerformed

    private void Account_1_Profit_TextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Account_1_Profit_TextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Account_1_Profit_TextFieldActionPerformed

    private void TestWeeklyPrepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TestWeeklyPrepActionPerformed
        WeeklyPrep.GoogleWeeklyPrep(Results_Text_Area, "Weekly E's", "Current Q", "Last 8 Q's", Load_Date_Text_Field.getText());
    }//GEN-LAST:event_TestWeeklyPrepActionPerformed

    private void Auto_Shut_Down_Check_BoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Auto_Shut_Down_Check_BoxActionPerformed
        // TODO add your handling code here:
        autoShutDown = this.Auto_Shut_Down_Check_Box.isSelected();
    }//GEN-LAST:event_Auto_Shut_Down_Check_BoxActionPerformed

    private void Load_Table_ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Load_Table_ButtonActionPerformed
        LoadTable(true);
        try {

            while ( !GUIData.LockOrderSocketData() ) { Utility.hybridPrecisionWait(10);}
            GUIData.SetOrdersSocketData( GUIData.LoadTable );
        } catch (Exception e) {
            Utility.dumpExceptionInfo(e);
            e.printStackTrace();
        }
    }//GEN-LAST:event_Load_Table_ButtonActionPerformed

    private void Save_Table_ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Save_Table_ButtonActionPerformed
        SaveTable();
    }//GEN-LAST:event_Save_Table_ButtonActionPerformed

    private void Quotes_Only_Processing_ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Quotes_Only_Processing_ButtonActionPerformed
        StartQuoteProcessing();
    }//GEN-LAST:event_Quotes_Only_Processing_ButtonActionPerformed

    private void GUI_Only_Processing_ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GUI_Only_Processing_ButtonActionPerformed
        // TODO add your handling code here:
        StartGUIProcessing();
    }//GEN-LAST:event_GUI_Only_Processing_ButtonActionPerformed

    private void Kill_Open_Straddle_Orders_ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Kill_Open_Straddle_Orders_ButtonActionPerformed
        try {
            while ( !GUIData.LockOrderSocketData() ) { Utility.hybridPrecisionWait(10);}
            GUIData.SetOrdersSocketData( GUIData.KillOpenStraddleOrdersButtonPress );
        } catch (Exception e) {
            Utility.dumpExceptionInfo(e);
            e.printStackTrace();
        }
    }//GEN-LAST:event_Kill_Open_Straddle_Orders_ButtonActionPerformed

    private void Start_Order_Processing_ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Start_Order_Processing_ButtonActionPerformed
        if ( (TT_API.getAccessToken().equalsIgnoreCase("") == false) && (SchwabAPI.getAccessToken().equalsIgnoreCase("") == false) ) {
            StartProcessing(false);
        } else {
            Results_Text_Area.append("\n == Both logins have not been completed!!! == \n");

        }
    }//GEN-LAST:event_Start_Order_Processing_ButtonActionPerformed

    private void SelectLogFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SelectLogFileActionPerformed
        // TODO add your handling code here:
        LV_Log_File_Name.setText( getFileName( LV_Log_File_Name.getText() ) );
    }//GEN-LAST:event_SelectLogFileActionPerformed

/*    private void Extract_Low_Value_ButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                         
        // TODO add your handling code here:
        GetLowValues.getLowValues(Results_Text_Area, LV_Log_File_Name.getText(), LV_DayGen_File_Name.getText(), "Mod_" + LV_DayGen_File_Name.getText());
    }                                                        
*/
    private void LV_Log_File_NameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LV_Log_File_NameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_LV_Log_File_NameActionPerformed

    private void GoogleLowValuesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GoogleLowValuesActionPerformed
        GetLowValues.GoogleGetLowValues( Results_Text_Area, LV_Log_File_Name.getText(),
            Gen_Prices_Tab_Name_Text_Field.getText(), Load_Date_Text_Field.getText() );
    }//GEN-LAST:event_GoogleLowValuesActionPerformed

    private void OHLCButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OHLCButtonActionPerformed
        OHLC.GenerateOHLCData(Results_Text_Area);
    }//GEN-LAST:event_OHLCButtonActionPerformed

/*    private void GenDayDataButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                 
        // TODO add your handling code here:
        OHLC.GenDailyData( Results_Text_Area, Load_Date_Text_Field.getText(), Double.parseDouble( Desired_Dollars_Text_Field.getText() ) );        
    }                                                
*/
    private void Desired_Dollars_Text_FieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Desired_Dollars_Text_FieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Desired_Dollars_Text_FieldActionPerformed

    private void Load_Date_Text_FieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Load_Date_Text_FieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Load_Date_Text_FieldActionPerformed

    private void Refresh_Token_Text_FieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Refresh_Token_Text_FieldActionPerformed
        // TODO add your handling code here:
        Refresh_Token_Text_Field.setText(SchwabAPI.getRefreshToken());
    }//GEN-LAST:event_Refresh_Token_Text_FieldActionPerformed

    private void Access_Token_Text_FieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Access_Token_Text_FieldActionPerformed
        // TODO add your handling code here:
        Access_Token_Text_Field.setText(SchwabAPI.getAccessToken());
    }//GEN-LAST:event_Access_Token_Text_FieldActionPerformed

    private void Paste_URL_Text_FieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Paste_URL_Text_FieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Paste_URL_Text_FieldActionPerformed

    private void Schwab_Login_Finish_ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Schwab_Login_Finish_ButtonActionPerformed
        try
        {

            // Refresh Token.  It is only needed every 7 days.  Access Tokens can be generated from it.  Access Tokens are good for 30 minutes.
            // Get the text in the text box that was pasted by the user.
            String ToDecode = Paste_URL_Text_Field.getText();

            // Once you have the URL we need to parse it out.  The URL will look something like this:
            //  "https://127.0.0.1/?code=C0.b2F1dGgyLmJkYy5zY2h3YWIuY29t.auQNYBx7kWwhywlCKXgk7ntGxb9hw8mQuECObI1SPb4%40&session=65a07489-b1fe-4d0e-bd3f-fc12e78d97ec"
            String[] output = ToDecode.split("code=");
            // Now must Decode the code from inside the URL
            // Now split out the session part
            String[] justCode = output[1].split("&session=");
            // URL Decode the authorization_code
            String AuthCode = URLDecoder.decode(justCode[0], "UTF-8");
            SchwabAPI.getTokens("authorization_code", SchwabAPI.getRefreshToken(), AuthCode, AppKey_Text_Field.getText(), Redirect_URI_TextField.getText());
            Access_Token_Text_Field.setText(SchwabAPI.getAccessToken());
            Refresh_Token_Text_Field.setText(SchwabAPI.getRefreshToken());
            if (SchwabAPI.getAccessToken().equalsIgnoreCase("") == false) {
                Kill_Open_Straddle_Orders_Button.setEnabled(true);
                Start_Order_Processing_Button.setEnabled(true);
                GUI_Only_Processing_Button.setEnabled(true);
                Quotes_Only_Processing_Button.setEnabled(true);
                GoogleGenDayData.setEnabled(true);
            }
            Schwab_Login_Finish_Button.setEnabled(false);
        }
        catch (UnsupportedEncodingException ex)
        {
            Utility.dumpExceptionInfo(ex);
            ex.printStackTrace();
        }
    }//GEN-LAST:event_Schwab_Login_Finish_ButtonActionPerformed

    private void Schwab_Login_Start_ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Schwab_Login_Start_ButtonActionPerformed
        // READ IN DATA FROM FILE AND SET REFRESH TOKEN IF VALID
        // IF need new Refresh Token then do this normal stuff below
        if (SchwabAPI.LoadToken() == false)
        {  // Only need to do this if the refresh token has expired
            Pop_Up_Window_To_Login();
        }
        else
        {
            // Automatically do a different version of Login Finish using the refresh token to get the AccessToken
            SchwabAPI.setAppKey(AppKey_Text_Field.getText());
            SchwabAPI.setSecret(String.valueOf(Secret_Field.getPassword()));
            if ( SchwabAPI.updateTokens() == true ) {
                Access_Token_Text_Field.setText(SchwabAPI.getAccessToken());
                Refresh_Token_Text_Field.setText(SchwabAPI.getRefreshToken());
                if (TT_API.getAccessToken().equalsIgnoreCase("") == false) {
                    Kill_Open_Straddle_Orders_Button.setEnabled(true);
                    Start_Order_Processing_Button.setEnabled(true);
                    GUI_Only_Processing_Button.setEnabled(true);
                    Quotes_Only_Processing_Button.setEnabled(true);
                    GoogleGenDayData.setEnabled(true);
                }
                Schwab_Login_Start_Button.setEnabled(false);
                Schwab_Login_Finish_Button.setEnabled(false);
            } else {
                Pop_Up_Window_To_Login();
            }
        }
    }//GEN-LAST:event_Schwab_Login_Start_ButtonActionPerformed

    private void TT_Access_Token_Text_FieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TT_Access_Token_Text_FieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TT_Access_Token_Text_FieldActionPerformed

    private void ClientSecret_Text_FieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClientSecret_Text_FieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ClientSecret_Text_FieldActionPerformed

    private void TT_Refresh_Token_Text_FieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TT_Refresh_Token_Text_FieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TT_Refresh_Token_Text_FieldActionPerformed

    private void TT_Login_ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TT_Login_ButtonActionPerformed
        // Working on the new Oauth changes for TT
        //   Client ID	0b4d2af6-03a4-418a-ae44-ee4ead99f983
        TT_API.setRefreshToken("eyJhbGciOiJFZERTQSIsInR5cCI6InJ0K2p3dCIsImtpZCI6IkZqVTdUT25qVEQ2WnVySlg2cVlwWmVPbzBDQzQ5TnIzR1pUN1E4MTc0cUkiLCJqa3UiOiJodHRwczovL2ludGVyaW9yLWFwaS5jaDIudGFzdHl3b3Jrcy5jb20vb2F1dGgvandrcyJ9.eyJpc3MiOiJodHRwczovL2FwaS50YXN0eXRyYWRlLmNvbSIsInN1YiI6IlUwMDAwMzY0ODIyIiwiaWF0IjoxNzU4MTU4NjQ0LCJhdWQiOiIwYjRkMmFmNi0wM2E0LTQxOGEtYWU0NC1lZTRlYWQ5OWY5ODMiLCJncmFudF9pZCI6IkdlY2VmMzc3YS05ZDNlLTQ1MTEtYWExYy0zYzVjYjUxOWE0MDIiLCJzY29wZSI6InJlYWQgdHJhZGUgb3BlbmlkIn0.5yaz2nf82BmRXjs2oDsfcggVA1Du_PO2oQjDZsHF_neUi6N66ImP3M_VNOqP2O0M0pcxIWNLYY-WoiuyICArCQ");
        TT_API.setClientSecret("ef64f19e28e377f6ce83bf85dd0e452d24a3e804");
        TT_API.generateAccessToken();
        if (TT_API.getAccessToken().equalsIgnoreCase("") == true) {
            // Something went wrong.
        } else {
            TT_Login_Button.setEnabled(false);
            updateGUIs();
        }
    }//GEN-LAST:event_TT_Login_ButtonActionPerformed

    private void Google_Spreadsheet_ID_Text_FieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Google_Spreadsheet_ID_Text_FieldActionPerformed
        // Only update things if the ID changed
        if ( !GUIData.getGoogleSpreadsheetID().equalsIgnoreCase( Google_Spreadsheet_ID_Text_Field.getText() ) ){
            GUIData.setGoogleSpreadsheetID( Google_Spreadsheet_ID_Text_Field.getText() );
            Google_Spreadsheet_Name_Label.setText( GoogleOperations.getSpreadsheetTitle() );
        }
        SavePreferences();
    }//GEN-LAST:event_Google_Spreadsheet_ID_Text_FieldActionPerformed

    private void Gen_Prices_Tab_Name_Text_FieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Gen_Prices_Tab_Name_Text_FieldActionPerformed
        SavePreferences();
    }//GEN-LAST:event_Gen_Prices_Tab_Name_Text_FieldActionPerformed

/*    private void SelectSourceFileActionPerformed(java.awt.event.ActionEvent evt) {                                                 
        // TODO add your handling code here:
        LV_DayGen_File_Name.setText( getFileName( LV_DayGen_File_Name.getText() ) );
    }                                                
*/
    private void GoogleGenDayDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GoogleGenDayDataActionPerformed
        // TODO add your handling code here:
        OHLC.GenDailyDataGoogle(Results_Text_Area, Load_Date_Text_Field.getText(), Double.parseDouble( Desired_Dollars_Text_Field.getText() ), Gen_Prices_Tab_Name_Text_Field.getText());
    }//GEN-LAST:event_GoogleGenDayDataActionPerformed

    private void Download_Tab_Name_Text_FieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Download_Tab_Name_Text_FieldActionPerformed
        SavePreferences();
    }//GEN-LAST:event_Download_Tab_Name_Text_FieldActionPerformed

    private void GoogleDateDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GoogleDateDataActionPerformed
        // This procedure loads the Straddle Data from google sheet directly.
        // Since we got this far we will likely succeed, so clear the table.
        ClearTable(false);
        OHLC.GenTableFIle( Symbol_Data_Table, Results_Text_Area, Load_Date_Text_Field.getText(), Download_Tab_Name_Text_Field.getText() );
        SaveTable();
    }//GEN-LAST:event_GoogleDateDataActionPerformed

    private void Initial_Sell_2_Orders_Check_BoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Initial_Sell_2_Orders_Check_BoxActionPerformed
        SavePreferences();
    }//GEN-LAST:event_Initial_Sell_2_Orders_Check_BoxActionPerformed

    private void Log_More_Data_Check_BoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Log_More_Data_Check_BoxActionPerformed
        // TODO add your handling code here:
        if (Log_More_Data_Check_Box.isSelected()) {
            LogData.SetLogLevel( Log4J2AsyncLogger.LogLevelOptions.Full );
            GUIData.setLogMoreData(true);
        }  // This reduces the amount of data being logged.
        else {
            LogData.SetLogLevel( Log4J2AsyncLogger.LogLevelOptions.Critical );
            GUIData.setLogMoreData(false);
        }
        SavePreferences();
    }//GEN-LAST:event_Log_More_Data_Check_BoxActionPerformed

    private void GUI_Only_Number_Of_Strikes_Text_FieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GUI_Only_Number_Of_Strikes_Text_FieldActionPerformed
        SavePreferences();
    }//GEN-LAST:event_GUI_Only_Number_Of_Strikes_Text_FieldActionPerformed

    private void Kill_Open_Orders_Check_BoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Kill_Open_Orders_Check_BoxActionPerformed
        SavePreferences();
    }//GEN-LAST:event_Kill_Open_Orders_Check_BoxActionPerformed

    private void OrderProcessingSpeedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OrderProcessingSpeedActionPerformed
        SavePreferences();
    }//GEN-LAST:event_OrderProcessingSpeedActionPerformed

    private void StartMinutesB4OpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StartMinutesB4OpenActionPerformed
        SavePreferences();
    }//GEN-LAST:event_StartMinutesB4OpenActionPerformed

    private void Capture_Duration_Text_FieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Capture_Duration_Text_FieldActionPerformed
        SavePreferences();
    }//GEN-LAST:event_Capture_Duration_Text_FieldActionPerformed

    private void Number_Of_Strikes_Text_FieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Number_Of_Strikes_Text_FieldActionPerformed
        SavePreferences();
    }//GEN-LAST:event_Number_Of_Strikes_Text_FieldActionPerformed

    private void OpenTimeTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OpenTimeTextFieldActionPerformed
        SavePreferences();
    }//GEN-LAST:event_OpenTimeTextFieldActionPerformed

    private void Save_Account_Info_ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Save_Account_Info_ButtonActionPerformed
        SavePreferences();
    }//GEN-LAST:event_Save_Account_Info_ButtonActionPerformed

    private void Load_Account_Info_ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Load_Account_Info_ButtonActionPerformed
        LoadPreferences(true);
    }//GEN-LAST:event_Load_Account_Info_ButtonActionPerformed

    private void AppKey_Text_FieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AppKey_Text_FieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_AppKey_Text_FieldActionPerformed

    private void jComboBoxQuoteSpeedActionPerformed(java.awt.event.ActionEvent evt) {                                                    
        SavePreferences();
    }                                                   

    private void Pop_Up_Window_To_Login() {
        // https://api.schwabapi.com/v1/oauth/authorize?client_id={CONSUMER _KEY}&redirect_uri={APP_CALLBACK_URL}
        try {
            String url = SchwabAPI.getBaseURL("Auth") + "/oauth/authorize?client_id=" + AppKey_Text_Field.getText() + "&redirect_uri=" + Redirect_URI_TextField.getText();
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                this.Schwab_Login_Start_Button.setEnabled(false);
                this.Schwab_Login_Finish_Button.setEnabled(true);
            }

            //  I think what has to happen here is a GET to the URI provided to TDA.  That should give JSON data with the code needed.
            //  The trick is waiting for the login to complete and then moving forward.
            //  I also downloaded java code to do all this OAuth stuff.  Need to study it and see how it may work for us.
            // I think the next thing is to try using OAuth2 Code that I downloaded.]
        } catch (Exception ex) {
            Utility.dumpExceptionInfo(ex);
            ex.printStackTrace();
        }       
    }

    synchronized private void Position_Current_Lock_Checkbox_Handler(java.awt.event.ActionEvent evt)
    {
      // This routine handles all of the Position current lock checkboxes on the Position panel.
      //
      // Note: Every checkbox has a unique name that contains a Position Index suffix (Index.e. "current_lock_checkbox 1").
        
      // Get the checkbox from the event.
      javax.swing.JCheckBox checkbox = (javax.swing.JCheckBox) evt.getSource();

      // Get the checkbox name.
      String strName = checkbox.getName();

      LogData.LogThis(Level.INFO, "*** Position_Current_Lock_Checkbox_Handler - strName = " + strName);

      // Extract the Position Index from the name.
      int spaceIndex = strName.indexOf(" ");

      String strIndex = strName.substring(spaceIndex + 1);
      int index = Integer.valueOf(strIndex) - 1;

      // Process the action.  Save the value in the Index Table so the other widgets
      // can easily reference it.
      
      if (checkbox.isSelected())
          {

          // Look up the Stock Symbol and the Account Number.
          String AccountNumber = PositionPanelListIndex[index].Account;
          String StockSymbol = PositionPanelListIndex[index].Symbol;
          String StrikePrice = PositionPanelListIndex[index].Strike;

          String OrderKey = AccountNumber + "_" + StockSymbol + "_" + StrikePrice;

          Position position = Get_Position(AccountNumber, StockSymbol, StrikePrice);

          PositionPanelListIndex[index].CurrentLockedPrice = position.CurrentStraddlePrice;               
          }
      else
          {
          PositionPanelListIndex[index].CurrentLockedPrice = 0.0;
          }
       
    }
            
   synchronized private void Position_Button_Handler(java.awt.event.ActionEvent evt)
      {
      // This routine handles all of the Position buttons on the Position panel.
      //
      // Note: Every button has a unique name that contains an Action prefix and
      //       a Position Index suffix (i.e. "targetSellButton 1", "cancelButton 3").

      // Get the button from the event.
      javax.swing.JButton btn = (javax.swing.JButton) evt.getSource();

      // Get the button name.
      String strButtonName = btn.getName();

      // Extract the Action and the Position Index from the button name.
      int spaceIndex = strButtonName.indexOf(" ");

      String strIndex = strButtonName.substring(spaceIndex + 1);
      int index = Integer.valueOf(strIndex) - 1;

      strButtonName = strButtonName.substring(0, spaceIndex);

      // Look up the Stock Symbol and the Account Number.
      String AccountNumber = PositionPanelListIndex[index].Account;
      String StockSymbol = PositionPanelListIndex[index].Symbol;
      String StrikePrice = PositionPanelListIndex[index].Strike;

      String OrderKey = AccountNumber + "_" + StockSymbol + "_" + StrikePrice;

      Position position = Get_Position(AccountNumber, StockSymbol, StrikePrice);

      LogData.LogThis(Level.INFO, "*** Position_Button_Handler - strButtonName = " + strButtonName);

      // Process the action.
      // Note: Strings are not supported in switch statements for this Java version.
      if (strButtonName.equals(CURRENT_SELL_ALL_BUTTON_NAME))
         {
         Double CurrentStraddlePrice;
         
         // If the current price is locked, then use the locked price.  Otherwise
         // use the current (i.e. dynamic) price.
                   
         if (PositionPanelListIndex[index].CurrentLockedPrice > 0.0)
            {
            CurrentStraddlePrice = PositionPanelListIndex[index].CurrentLockedPrice;
            }
         else
            {
            CurrentStraddlePrice = position.CurrentStraddlePrice;
            }

         LogData.LogThis(Level.INFO, "*** Placing Sell order for Current = " + CurrentStraddlePrice);
         String contracts = "";
         try {
             // I want to test getting the right contract size for sell orders when there is partial orders, but don't want to kill the 
             //     ability to sell orders from the GUI for all by introducing an exception.  So temporarily putting this in place.
             if ( position.FilledSellContracts > 0 ) {
                contracts = String.valueOf(position.FilledBuyContracts - position.FilledSellContracts);                                 
             } else {
                contracts = String.valueOf(position.FilledBuyContracts);                                 
             }
         } catch (Exception e) {
            contracts = String.valueOf(position.FilledBuyContracts);                    
         }      
         try {
           // Process_Order_Action(String OrderKey, OrderActionType OrderAction, Double Price, String expiration, String contracts)
           String sellOrder = 
                   GUIData.PlaceSellOrder + GUIData.SocketStringSeparator +
                   OrderKey + GUIData.SocketStringSeparator +
                   PositionsData.OrderActionType.SellStraddle.toString() + GUIData.SocketStringSeparator +
                   Double.toString( position.CurrentStraddlePrice ) + GUIData.SocketStringSeparator +
                   position.Expiration + GUIData.SocketStringSeparator +
                   contracts;                    
           while ( !GUIData.LockOrderSocketData() ) { Utility.hybridPrecisionWait(10);}
           GUIData.SetOrdersSocketData( sellOrder );
         } catch (Exception e) {
            Utility.dumpExceptionInfo(e);
            e.printStackTrace();
         }      
         }
      
      else if (strButtonName.equals(TARGET_SELL_ALL_BUTTON_NAME))
         {
         LogData.LogThis(Level.INFO, "");
         LogData.LogThis(Level.INFO, "*** Placing Target Sell All order");
         LogData.LogThis(Level.INFO, "***");
         LogData.LogThis(Level.INFO, "***    OrderKey   = " + OrderKey);
         LogData.LogThis(Level.INFO, "***    Enum       = PositionsData.OrderActionType.SellStraddle");
         LogData.LogThis(Level.INFO, "***    Price      = " + PositionPanelListIndex[index].TargetPrice);
         LogData.LogThis(Level.INFO, "***    Expiration = " + position.Expiration);
         LogData.LogThis(Level.INFO, "***    Contracts  = " + String.valueOf(position.FilledBuyContracts));
         LogData.LogThis(Level.INFO, "");

         String contracts = "";
         try {
             // I want to test getting the right contract size for sell orders when there are partial orders, but don't want to kill 
             // the ability to sell orders from the GUI for all by introducing an exception.  So temporarily putting this in place.
             if ( position.FilledSellContracts > 0 ) {
                contracts = String.valueOf(position.FilledBuyContracts - position.FilledSellContracts);                                 
             } else {
                contracts = String.valueOf(position.FilledBuyContracts);                                 
             }
         } catch (Exception e) {
            contracts = String.valueOf(position.FilledBuyContracts);                    
         }      
         try {
           // Process_Order_Action(String OrderKey, OrderActionType OrderAction, Double Price, String expiration, String contracts)
           String sellOrder = 
                   GUIData.PlaceSellOrder + GUIData.SocketStringSeparator +
                   OrderKey + GUIData.SocketStringSeparator +
                   PositionsData.OrderActionType.SellStraddle.toString() + GUIData.SocketStringSeparator +
                   Double.toString( PositionPanelListIndex[index].TargetPrice ) + GUIData.SocketStringSeparator +
                   position.Expiration + GUIData.SocketStringSeparator +
                   contracts;                    
           while ( !GUIData.LockOrderSocketData() ) { Utility.hybridPrecisionWait(10);}
           GUIData.SetOrdersSocketData( sellOrder );
         } catch (Exception e) {
            Utility.dumpExceptionInfo(e);
            e.printStackTrace();
         }
         }
      
      else if (strButtonName.equals(TARGET_SELL_THREE_QUARTER_BUTTON_NAME))
         {
            LogData.LogThis(Level.INFO, "");
            LogData.LogThis(Level.INFO, "*** Placing Target Sell 3/4 order");
            LogData.LogThis(Level.INFO, "***");
            LogData.LogThis(Level.INFO, "***    OrderKey   = " + OrderKey);
            LogData.LogThis(Level.INFO, "***    Enum       = PositionsData.OrderActionType.SellStraddle");
            LogData.LogThis(Level.INFO, "***    Price      = " + PositionPanelListIndex[index].TargetPrice);
            LogData.LogThis(Level.INFO, "***    Expiration = " + position.Expiration);
            LogData.LogThis(Level.INFO, "***    Contracts  = " + String.valueOf(position.FilledBuyContracts));
            LogData.LogThis(Level.INFO, "");

            String contracts = "";
            try {
             // I want to test getting the right contract size for sell orders when there are partial orders, but don't want to kill 
             // the ability to sell orders from the GUI for all by introducing an exception.  So temporarily putting this in place.
             if ( position.FilledSellContracts > 0 ) {
                contracts = String.valueOf(Math.round((position.FilledBuyContracts - position.FilledSellContracts) * 0.75));                                 
             } else {
                contracts = String.valueOf(Math.round(position.FilledBuyContracts * 0.75));                                 
             }
         } catch (Exception e) {
            contracts = String.valueOf(Math.round(position.FilledBuyContracts * 0.75));                    
         }      
         try {
           // Process_Order_Action(String OrderKey, OrderActionType OrderAction, Double Price, String expiration, String contracts)
           String sellOrder = 
                   GUIData.PlaceSellOrder + GUIData.SocketStringSeparator +
                   OrderKey + GUIData.SocketStringSeparator +
                   PositionsData.OrderActionType.SellStraddle.toString() + GUIData.SocketStringSeparator +
                   Double.toString( PositionPanelListIndex[index].TargetPrice ) + GUIData.SocketStringSeparator +
                   position.Expiration + GUIData.SocketStringSeparator +
                   contracts;                    
           while ( !GUIData.LockOrderSocketData() ) { Utility.hybridPrecisionWait(10);}
           GUIData.SetOrdersSocketData( sellOrder );
            } catch (Exception e) {
            Utility.dumpExceptionInfo(e);
            e.printStackTrace();
            }      
         }
      
      else if (strButtonName.equals(TARGET_SELL_HALF_BUTTON_NAME))
         {
         LogData.LogThis(Level.INFO, "");
         LogData.LogThis(Level.INFO, "*** Placing Target Sell 1/2 order");
         LogData.LogThis(Level.INFO, "***");
         LogData.LogThis(Level.INFO, "***    OrderKey   = " + OrderKey);
         LogData.LogThis(Level.INFO, "***    Enum       = PositionsData.OrderActionType.SellStraddle");
         LogData.LogThis(Level.INFO, "***    Price      = " + PositionPanelListIndex[index].TargetPrice);
         LogData.LogThis(Level.INFO, "***    Expiration = " + position.Expiration);
         LogData.LogThis(Level.INFO, "***    Contracts  = " + String.valueOf(position.FilledBuyContracts));
         LogData.LogThis(Level.INFO, "");

         String contracts = "";
            try {
             // I want to test getting the right contract size for sell orders when there are partial orders, but don't want to kill 
             // the ability to sell orders from the GUI for all by introducing an exception.  So temporarily putting this in place.
             if ( position.FilledSellContracts > 0 ) {
                contracts = String.valueOf(Math.round((position.FilledBuyContracts - position.FilledSellContracts) * 0.5));                                 
             } else {
                contracts = String.valueOf(Math.round(position.FilledBuyContracts * 0.5));                                 
             }
         } catch (Exception e) {
            contracts = String.valueOf(Math.round(position.FilledBuyContracts * 0.5));                    
         }      
         try {
              // Process_Order_Action(String OrderKey, OrderActionType OrderAction, Double Price, String expiration, String contracts)
              String sellOrder = 
                      GUIData.PlaceSellOrder + GUIData.SocketStringSeparator +
                      OrderKey + GUIData.SocketStringSeparator +
                      PositionsData.OrderActionType.SellStraddle.toString() + GUIData.SocketStringSeparator +
                      Double.toString( PositionPanelListIndex[index].TargetPrice ) + GUIData.SocketStringSeparator +
                      position.Expiration + GUIData.SocketStringSeparator +
                      contracts;                    
              while ( !GUIData.LockOrderSocketData() ) { Utility.hybridPrecisionWait(10);}
              GUIData.SetOrdersSocketData( sellOrder );
            } catch (Exception e) {
               Utility.dumpExceptionInfo(e);
               e.printStackTrace();
            }
       }

/*

       else if (strButtonName.equals("sellHalfButton"))
       {
       StreamingData.Process_Order_Action(OrderKey,
       StreamingData.OrderActionType.SellHalf);
       }

       else if (strButtonName.equals("sellCallsButton"))
       {
       StreamingData.Process_Order_Action(OrderKey,
       StreamingData.OrderActionType.SellCalls);
       }
       else if (strButtonName.equals("sellPutsButton"))
       {
       StreamingData.Process_Order_Action(OrderKey,
       StreamingData.OrderActionType.SellPuts);
       }
       else if (strButtonName.equals("cancelButton"))
       {
       StreamingData.Process_Order_Action(OrderKey,
       StreamingData.OrderActionType.Cancel);
       }

       */
      else
         {
         LogData.LogThis(Level.INFO, "Invalid OrderType = " + strButtonName);
         }
      }

   // This routine handles a change to the Target Price.
   private void Target_Price_Handler(ActionEvent e)
      {
      // Get the widget from the event.
      javax.swing.JTextField widget = (javax.swing.JTextField) e.getSource();

      // Get the widget name.
      String strName = widget.getName();

      // Extract the Position Index from the widget name.
      int spaceIndex = strName.indexOf(" ");

      String strIndex = strName.substring(spaceIndex + 1);
      int index = Integer.valueOf(strIndex) - 1;

      // Update the value.
      Double value = Double.valueOf(widget.getText());

      PositionPanelListIndex[index].TargetPrice = value;

      // Update the other values.
      Position position = Get_Position(PositionPanelListIndex[index].Account, PositionPanelListIndex[index].Symbol, PositionPanelListIndex[index].Strike);

      PositionPanelListIndex[index].TargetPercentage = ((PositionPanelListIndex[index].TargetPrice - position.AveStraddleBuyPrice) / position.AveStraddleBuyPrice);
      PositionPanelListIndex[index].TargetAmount = (PositionPanelListIndex[index].TargetPrice - position.AveStraddleBuyPrice) * position.FilledBuyContracts * 100;

      // The Java Currency Formatter cannot handle negative numbers easily, so use colors
      // to indicate positive & negative numbers.
      if (PositionPanelListIndex[index].TargetPrice < position.AveStraddleBuyPrice)
         {
         widget.setForeground(Color.RED);
         widget.setBackground(Color.WHITE);
         }
      else
         {
         widget.setForeground(Color.BLACK);
         widget.setBackground(Color.GREEN);
         }

      // Refresh the displayed value so it is in the currency format.  This is needed
      // if/when we enter a value like 2.5 instead of $2.50.
      widget.setText(currencyFormatter.format(value));

      // Update the other widgets.  This has to be done here because using the periodic
      // update routine prevents proper data entry ... it keeps overwriting the value
      // while we are trying to handle this input.
      //
      // Get the panel.
      JPanel panel = PositionPanelList.get(index);

      // Get the panel components, and then update each of them.
      Component[] components = panel.getComponents();
      for (Component component : components)
         {
         String componentName = component.getName();

         // Strip off the Position IndexTable suffix (if there is one).
         spaceIndex = componentName.indexOf(" ");

         if (spaceIndex > 0)
            {
            strIndex = componentName.substring(spaceIndex + 1);
            componentName = componentName.substring(0, spaceIndex);
            }

         // Update the component (i.e. the widget).
         if (componentName.equals(TARGET_PERCENTAGE_NAME))
            {
            ((javax.swing.JTextField) component).setText(String.format("%.0f%%", Math.abs(PositionPanelListIndex[index].TargetPercentage * 100.0)));

            if (PositionPanelListIndex[index].TargetPrice < position.AveStraddleBuyPrice)
               {
               ((javax.swing.JTextField) component).setForeground(Color.RED);
               ((javax.swing.JTextField) component).setBackground(Color.WHITE);
               }
            else
               {
               ((javax.swing.JTextField) component).setForeground(Color.BLACK);
               ((javax.swing.JTextField) component).setBackground(Color.GREEN);
               }
            }

         else if (componentName.equals(TARGET_AMOUNT_NAME))
            {
            ((javax.swing.JTextField) component).setText(String.format("$%.0f%n", Math.abs(PositionPanelListIndex[index].TargetAmount)));

            if (PositionPanelListIndex[index].TargetPrice < position.AveStraddleBuyPrice)
               {
               ((javax.swing.JTextField) component).setForeground(Color.RED);
               ((javax.swing.JTextField) component).setBackground(Color.WHITE);
               }
            else
               {
               ((javax.swing.JTextField) component).setForeground(Color.BLACK);
               ((javax.swing.JTextField) component).setBackground(Color.GREEN);
               }
            }
         }

      }

   // This routine handles a change to the Target Percentage.
   private void Target_Percentage_Handler(ActionEvent e)
      {
      // Get the widget from the event.
      javax.swing.JTextField widget = (javax.swing.JTextField) e.getSource();

      // Get the widget name.
      String strName = widget.getName();

      // Extract the Position Index from the widget name.
      int spaceIndex = strName.indexOf(" ");

      String strIndex = strName.substring(spaceIndex + 1);
      int index = Integer.valueOf(strIndex) - 1;

      // Update the value.
      Double value = Double.valueOf(widget.getText());

      PositionPanelListIndex[index].TargetPercentage = value;

      // Update the other values.
      Position position = Get_Position(PositionPanelListIndex[index].Account, PositionPanelListIndex[index].Symbol, PositionPanelListIndex[index].Strike);

      PositionPanelListIndex[index].TargetPrice = position.AveStraddleBuyPrice * (1.0 + (PositionPanelListIndex[index].TargetPercentage / 100.0));
      PositionPanelListIndex[index].TargetAmount = (PositionPanelListIndex[index].TargetPrice - position.AveStraddleBuyPrice) * position.FilledBuyContracts * 100;

      // Use colors to indicate positive & negative numbers.
      if (PositionPanelListIndex[index].TargetPrice < position.AveStraddleBuyPrice)
         {
         widget.setForeground(Color.RED);
         widget.setBackground(Color.WHITE);
         }
      else
         {
         widget.setForeground(Color.BLACK);
         widget.setBackground(Color.GREEN);
         }

      // Refresh the displayed value.
      widget.setText(String.format("%.0f%%", Math.abs(value)));

      // Update the other widgets.  This has to be done here because using the periodic
      // update routine prevents proper data entry ... it keeps overwriting the value
      // while we are trying to handle this input.
      //
      // Get the panel.
      JPanel panel = PositionPanelList.get(index);

      // Get the panel components, and then update each of them.
      Component[] components = panel.getComponents();
      for (Component component : components)
         {
         String componentName = component.getName();

         // Strip off the Position IndexTable suffix (if there is one).
         spaceIndex = componentName.indexOf(" ");

         if (spaceIndex > 0)
            {
            strIndex = componentName.substring(spaceIndex + 1);
            componentName = componentName.substring(0, spaceIndex);
            }

         // Update the component (i.e. the widget).
         if (componentName.equals(TARGET_PRICE_NAME))
            {
            ((javax.swing.JTextField) component).setText(currencyFormatter.format(Math.abs(PositionPanelListIndex[index].TargetPrice)));

            if (PositionPanelListIndex[index].TargetPrice < position.AveStraddleBuyPrice)
               {
               ((javax.swing.JTextField) component).setForeground(Color.RED);
               ((javax.swing.JTextField) component).setBackground(Color.WHITE);
               }
            else
               {
               ((javax.swing.JTextField) component).setForeground(Color.BLACK);
               ((javax.swing.JTextField) component).setBackground(Color.GREEN);
               }
            }

         else if (componentName.equals(TARGET_AMOUNT_NAME))
            {
            ((javax.swing.JTextField) component).setText(String.format("$%.0f%n", Math.abs(PositionPanelListIndex[index].TargetAmount)));

            if (PositionPanelListIndex[index].TargetPrice < position.AveStraddleBuyPrice)
               {
               ((javax.swing.JTextField) component).setForeground(Color.RED);
               ((javax.swing.JTextField) component).setBackground(Color.WHITE);
               }
            else
               {
               ((javax.swing.JTextField) component).setForeground(Color.BLACK);
               ((javax.swing.JTextField) component).setBackground(Color.GREEN);
               }
            }
         }
      }

   // This routine handles a change to the Target Amount.
   private void Target_Amount_Handler(ActionEvent e)
      {
      // Get the widget from the event.
      javax.swing.JTextField widget = (javax.swing.JTextField) e.getSource();

      // Get the widget name.
      String strName = widget.getName();

      // Extract the Position Index from the widget name.
      int spaceIndex = strName.indexOf(" ");

      String strIndex = strName.substring(spaceIndex + 1);
      int index = Integer.valueOf(strIndex) - 1;

      // Update the value.
      Double value = Double.valueOf(widget.getText());

      PositionPanelListIndex[index].TargetAmount = value;

      // Update the other values.
      Position position = Get_Position(PositionPanelListIndex[index].Account, PositionPanelListIndex[index].Symbol, PositionPanelListIndex[index].Strike);

      PositionPanelListIndex[index].TargetPrice = position.AveStraddleBuyPrice + (PositionPanelListIndex[index].TargetAmount / position.FilledBuyContracts / 100.0);
      PositionPanelListIndex[index].TargetPercentage = ((PositionPanelListIndex[index].TargetPrice - position.AveStraddleBuyPrice) / position.AveStraddleBuyPrice);

      // Use colors to indicate positive & negative numbers.
      if (PositionPanelListIndex[index].TargetPrice < position.AveStraddleBuyPrice)
         {
         widget.setForeground(Color.RED);
         widget.setBackground(Color.WHITE);
         }
      else
         {
         widget.setForeground(Color.BLACK);
         widget.setBackground(Color.GREEN);
         }

      // Refresh the displayed value.
      widget.setText(String.format("$%.0f%n", Math.abs(value)));

      // Update the other widgets.  This has to be done here because using the periodic
      // update routine prevents proper data entry ... it keeps overwriting the value
      // while we are trying to handle this input.
      //
      // Get the panel.
      JPanel panel = PositionPanelList.get(index);

      // Get the panel components, and then update each of them.
      Component[] components = panel.getComponents();
      for (Component component : components)
         {
         String componentName = component.getName();

         // Strip off the Position IndexTable suffix (if there is one).
         spaceIndex = componentName.indexOf(" ");

         if (spaceIndex > 0)
            {
            strIndex = componentName.substring(spaceIndex + 1);
            componentName = componentName.substring(0, spaceIndex);
            }

         // Update the component (i.e. the widget).
         if (componentName.equals(TARGET_PRICE_NAME))
            {
            ((javax.swing.JTextField) component).setText(currencyFormatter.format(Math.abs(PositionPanelListIndex[index].TargetPrice)));

            if (PositionPanelListIndex[index].TargetPrice < position.AveStraddleBuyPrice)
               {
               ((javax.swing.JTextField) component).setForeground(Color.RED);
               ((javax.swing.JTextField) component).setBackground(Color.WHITE);
               }
            else
               {
               ((javax.swing.JTextField) component).setForeground(Color.BLACK);
               ((javax.swing.JTextField) component).setBackground(Color.GREEN);
               }
            }

         else if (componentName.equals(TARGET_PERCENTAGE_NAME))
            {
            ((javax.swing.JTextField) component).setText(String.format("%.0f%%", Math.abs(PositionPanelListIndex[index].TargetPercentage * 100.0)));

            if (PositionPanelListIndex[index].TargetPrice < position.AveStraddleBuyPrice)
               {
               ((javax.swing.JTextField) component).setForeground(Color.RED);
               ((javax.swing.JTextField) component).setBackground(Color.WHITE);
               }
            else
               {
               ((javax.swing.JTextField) component).setForeground(Color.BLACK);
               ((javax.swing.JTextField) component).setBackground(Color.GREEN);
               }
            }

         }
      }

   // Get the actual number of symbols entered in the Symbol Table
   // Assumes no gaps in rows for data entered
   // Assumes symbols are in column 0
   private int countSymbolsEntered()
      {
      int rowCount = 0;
      for (int ccc = 0; ccc < Symbol_Data_Table.getRowCount(); ccc++)
         {
         if (Symbol_Data_Table.getValueAt(ccc, GUIData.symbolColumn) != null)
            {
            rowCount = ccc + 1;
            }
         }
      return rowCount;
      }

    // Get the actual number of accounts entered in the Accounts Table
    // Assumes no gaps in rows for data entered
    // Assumes Names are in column 0
    private int countAccountsEntered(String broker) {
        int rowCount = 0;
        for (int ccc = 0; ccc < Accounts_Table.getRowCount(); ccc++) {
            boolean aMatch = false;
            if (!broker.equalsIgnoreCase(GUIData.allBrokers)) {
                        aMatch = (Accounts_Table.getValueAt(ccc, GUIData.accountNameColumn) != null) &&
                                 (Accounts_Table.getValueAt(ccc, GUIData.brokerNameColumn).toString().equalsIgnoreCase(broker));
            } else {
                aMatch = (Accounts_Table.getValueAt(ccc, GUIData.accountNameColumn) != null);
            }
            if (aMatch == true){
                if (!Accounts_Table.getValueAt(ccc, GUIData.accountNameColumn).toString().equalsIgnoreCase(accountNameDefault)) {
                   rowCount = ccc + 1;
               }
            }
        }
        return rowCount;
    }

   public static void main(String args[])
      {

        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-L")) {
                autoLogin = true;
            } else {
                if (args[i].equalsIgnoreCase("-P")) {
                    autoLogin = true;
                    autoProcess = true;
                } else {
                    if (args[i].equalsIgnoreCase("-G")) {
                        autoLogin = true;
                        autoGUIOnly = true;
                    } else {
                        if (args[i].equalsIgnoreCase("-S")) {
                            autoLogin = true;
                            autoProcess = true;
                            autoShutDown = true;
                        } else {
                            // Print out message on parameters
                            // TBD
                        }
                    }
                }
            }
        }
      /*
       Create and display the form
       */
      java.awt.EventQueue.invokeLater(new Runnable()
         {
         public void run()
            {
            new GUI().setVisible(true);
            }
         });
      }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Access_Token_Label;
    private javax.swing.JTextField Access_Token_Text_Field;
    private javax.swing.JLabel Account_1_Profit_Label;
    private javax.swing.JTextField Account_1_Profit_TextField;
    private javax.swing.JLabel Account_2_Profit_Label;
    private javax.swing.JTextField Account_2_Profit_TextField;
    private javax.swing.JLabel Account_3_Profit_Label;
    private javax.swing.JTextField Account_3_Profit_TextField;
    private javax.swing.JLabel Account_4_Profit_Label;
    private javax.swing.JTextField Account_4_Profit_TextField;
    private javax.swing.JLabel Account_5_Profit_Label;
    private javax.swing.JTextField Account_5_Profit_TextField;
    private javax.swing.JPanel Account_Setup_Panel;
    private javax.swing.JTable Accounts_Table;
    private javax.swing.JLabel AppKey_Label;
    private javax.swing.JTextField AppKey_Text_Field;
    private javax.swing.JCheckBox Auto_Shut_Down_Check_Box;
    private javax.swing.JLabel Capture_Duration_Label;
    private javax.swing.JTextField Capture_Duration_Text_Field;
    private javax.swing.JLabel ClientSecret_Label;
    private javax.swing.JPasswordField ClientSecret_Text_Field;
    private javax.swing.JLabel Desired_Dollars_Lable;
    private javax.swing.JTextField Desired_Dollars_Text_Field;
    private javax.swing.JTextField Download_Tab_Name_Text_Field;
    private javax.swing.JLabel GUI_Only_Number_Of_Strikes_Label;
    private javax.swing.JTextField GUI_Only_Number_Of_Strikes_Text_Field;
    private javax.swing.JButton GUI_Only_Processing_Button;
    private javax.swing.JTextField Gen_Prices_Tab_Name_Text_Field;
    private javax.swing.JButton GoogleDateData;
    private javax.swing.JButton GoogleGenDayData;
    private javax.swing.JButton GoogleLowValues;
    private javax.swing.JLabel Google_Spreadsheet_ID_Label;
    private javax.swing.JTextField Google_Spreadsheet_ID_Text_Field;
    private javax.swing.JLabel Google_Spreadsheet_Name_Label;
    private javax.swing.JCheckBox Initial_Sell_2_Orders_Check_Box;
    private javax.swing.JCheckBox Kill_Open_Orders_Check_Box;
    private javax.swing.JButton Kill_Open_Straddle_Orders_Button;
    private javax.swing.JLabel LV_Log_File_Label;
    private javax.swing.JLabel LV_Log_File_Label1;
    private javax.swing.JLabel LV_Log_File_Label2;
    private javax.swing.JTextField LV_Log_File_Name;
    private javax.swing.JButton Load_Account_Info_Button;
    private javax.swing.JLabel Load_Date_Label;
    private javax.swing.JTextField Load_Date_Text_Field;
    private javax.swing.JButton Load_Table_Button;
    private javax.swing.JCheckBox Log_More_Data_Check_Box;
    private javax.swing.JPanel Login_Panel;
    private javax.swing.JScrollPane Main_Scrol_lPane;
    private javax.swing.JLabel Max_Funds_Label1;
    private javax.swing.JPanel Monitor_Panel;
    private javax.swing.JLabel Number_Of_Strikes_Label;
    private javax.swing.JTextField Number_Of_Strikes_Text_Field;
    private javax.swing.JButton OHLCButton;
    private javax.swing.JTextField OpenTimeTextField;
    private javax.swing.JLabel OrderCheckSpeed;
    private javax.swing.JTextField OrderProcessingSpeed;
    private javax.swing.JPanel PES_Panel;
    private javax.swing.JTextField Paste_URL_Text_Field;
    private javax.swing.JPanel Profit_Panel;
    private javax.swing.JLabel Quote_Speed_Label;
    private javax.swing.JButton Quotes_Only_Processing_Button;
    private javax.swing.JLabel Redirect_URI_Label;
    private javax.swing.JTextField Redirect_URI_TextField;
    private javax.swing.JLabel Refresh_Token_Label;
    private javax.swing.JTextField Refresh_Token_Text_Field;
    private javax.swing.JScrollPane Results_Scroll_Pane;
    private javax.swing.JTextArea Results_Text_Area;
    private javax.swing.JButton Save_Account_Info_Button;
    private javax.swing.JButton Save_Table_Button;
    private javax.swing.JButton Schwab_Login_Finish_Button;
    private javax.swing.JButton Schwab_Login_Start_Button;
    private javax.swing.JPasswordField Secret_Field;
    private javax.swing.JLabel Secret_Label;
    private javax.swing.JButton SelectLogFile;
    private javax.swing.JCheckBox Show_Sold_Positions_Checkbox;
    private javax.swing.JTextField StartMinutesB4Open;
    private javax.swing.JButton Start_Order_Processing_Button;
    private javax.swing.JScrollPane Symbol_Data_Scroll_Pane;
    private javax.swing.JTable Symbol_Data_Table;
    private javax.swing.JLabel TT_Access_Token_Label;
    private javax.swing.JTextField TT_Access_Token_Text_Field;
    private javax.swing.JButton TT_Login_Button;
    private javax.swing.JLabel TT_Refresh_Token_Label;
    private javax.swing.JTextField TT_Refresh_Token_Text_Field;
    private javax.swing.JTabbedPane Tabs_Panel;
    private javax.swing.JButton TestWeeklyPrep;
    private javax.swing.JLabel Total_Profit_Label;
    private javax.swing.JTextField Total_Profit_TextField;
    private javax.swing.JLabel Version_Label;
    private javax.swing.JLabel Wait_Duration_Label;
    private javax.swing.JComboBox<String> jComboBoxQuoteSpeed;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

   private void resetForeground(Object get, Color BLUE)
      {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
      }
   }
