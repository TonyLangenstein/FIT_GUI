package FITGUI;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.Level;
import org.json.JSONArray;

/**
 * Remote Order Server Socket Thread handling multiplexed frames over Virtual Threads.
 */
public class OrderServerSocketThread implements Runnable {

    private static volatile boolean connected = false;
    private static volatile boolean waiting4Ack = false;
    private static javax.swing.JTextArea clientWindow;   
    private static DataInputStream is;
    private static DataOutputStream os;
    private static ServerSocket serverSocket = null;
    private static Socket clientSocket = null;
    private static final Log4J2AsyncLogger LogData = new Log4J2AsyncLogger();

    // Virtual Thread references for teardown/interrupt handling
    private static Thread readerThread;
    private static Thread writerThread;

    // Concurrent reassembly buffer for multi-part messages
    private static final ConcurrentHashMap<String, StringBuilder> reassemblyBuffers = new ConcurrentHashMap<>();
//    private static int msgCnt = 0;

    public OrderServerSocketThread(javax.swing.JTextArea cw) {
        clientWindow = cw;
    }

@Override
    public void run() { 
        // 1. Bind port and flag server ready
        InitSocket();

        if (serverSocket == null) {
            return;
        }

        // 2. Wait for incoming client connection on background virtual thread
        try {
            java.awt.EventQueue.invokeLater(() -> 
                clientWindow.append("Server listening on port " + Config.SchwabOrdersSocketNumber + ", waiting for client...\n")
            );

            // Blocks ONLY this background thread until client connects
            clientSocket = serverSocket.accept(); 

            is = new DataInputStream(clientSocket.getInputStream());
            os = new DataOutputStream(clientSocket.getOutputStream());

            connected = true;

            java.awt.EventQueue.invokeLater(() -> 
                clientWindow.append("Client connected successfully!\n")
            );

            // 3. Start worker Virtual Threads for active connection
            readerThread = Thread.ofVirtual().name("ServerReader").start(() -> {
                while (connected) ReadSocket();
            });

            writerThread = Thread.ofVirtual().name("ServerWriter").start(() -> {
                while (connected) WriteSocket();
            }); 

            readerThread.join();
            writerThread.join();

        } catch (Exception e) {
            LogData.LogThis(Level.ERROR, "Exception during connection acceptance or execution: " + e);
        } finally {
            CloseSocket();
        } 
    }
    /**
     * Reads incoming frames from network wire, reassembles multi-part chunks concurrently,
     * and processes completed payloads.
     */
    private static void ReadSocket() {
        try {
            String data = is.readUTF();
            LogData.LogThis(Level.INFO, "Got msg ... " + data + " ...\n");

            if (data.contains(GUIData.ClientAck)) {
                if (waiting4Ack) {
                    GUIData.UnlockOrderSocketData();
                    waiting4Ack = false;
                } else {
                    LogData.LogThis(Level.INFO, "Received unexpected Ack from client \n");
                }
                return;
            }

//            msgCnt++;
//            if (msgCnt > 60) {
//                java.awt.EventQueue.invokeLater(() -> clientWindow.append(".\n"));
//                msgCnt = 0;                        
//            } else {
//                java.awt.EventQueue.invokeLater(() -> clientWindow.append("."));
//            }

            // Limit split to 3 parts so inner separators in JSON are preserved
            if (data.startsWith("MSG-")) {
                String[] tokens = data.split(GUIData.SocketStringSeparator, 3);

                if (tokens.length >= 3) {
                    String messageId = tokens[0];
                    boolean isFinal = tokens[1].startsWith("1");
                    String chunkPayload = tokens[2];

                    StringBuilder buffer = reassemblyBuffers.computeIfAbsent(messageId, k -> new StringBuilder());
                    buffer.append(chunkPayload);

                    if (isFinal) {
                        String fullPayload = reassemblyBuffers.remove(messageId).toString();
                        processPayload(fullPayload);
                    }

                    sendServerAck();
                    return;
                }
            }

            processPayload(data);
            sendServerAck();

        } catch (java.io.EOFException | java.net.SocketException e) {
            LogData.LogThis(Level.INFO, "Client connection terminated: " + e.getMessage());
            handleDisconnect();
        } catch (Exception e) {
            LogData.LogThis(Level.ERROR, "Read Exception: " + e);
            java.awt.EventQueue.invokeLater(() -> 
                clientWindow.append("Exception in ReadSocket in Socket Thread!\n")
            );
            handleDisconnect();
        }
    }

    /**
     * Outbound message loop: Pulls data from GUIData and streams atomic multiplexed frames.
     */
    private static void WriteSocket() {
        try {
            if (!waiting4Ack && GUIData.OrdersDataReadyToSend()) {
                String msg = GUIData.GetOrdersSocketDataToSend();
                String messageId = "MSG-" + UUID.randomUUID().toString().substring(0, 8);

                // Multi-chunk outbound streaming loop for intermediate frames
                while (msg.length() >= Config.maxBufferSize) {
                    String currentChunk = msg.substring(0, Config.maxBufferSize - 1);
                    
                    String frame = messageId + GUIData.SocketStringSeparator + "0" 
                            + GUIData.SocketStringSeparator + currentChunk 
                            + GUIData.SocketStringSeparator + "MORE TO COME";

                    synchronized (os) {
                        os.writeUTF(frame);
                        os.flush();
                    }

                    LogData.LogThis(Level.INFO, "Sent chunk frame [" + messageId + "]: " + frame);
                    msg = msg.substring(Config.maxBufferSize - 1);
                }

                // Final frame write (isFinal = 1)
                String finalFrame = messageId + GUIData.SocketStringSeparator + "1" 
                        + GUIData.SocketStringSeparator + msg;

                synchronized (os) {
                    os.writeUTF(finalFrame);
                    os.flush();
                }

                GUIData.ClearOrdersSocketData();
                waiting4Ack = true;

                LogData.LogThis(Level.INFO, "Sent final frame [" + messageId + "]: " + finalFrame);
            } else {
                // Short yield when no data is queued
                Utility.hybridPrecisionWait(10);
            }
        } catch (Exception e) {
            LogData.LogThis(Level.ERROR, "Write Exception: " + e);
            java.awt.EventQueue.invokeLater(() -> 
                clientWindow.append("Exception in WriteSocket in Socket Thread!\n")
            );
        }
    }

    /**
     * Flags the thread as disconnected and interrupts the idle writer thread.
     */
    private static void handleDisconnect() {
        connected = false;
        waiting4Ack = false;
        if (writerThread != null && writerThread.isAlive()) {
            writerThread.interrupt(); // Wakes writer up from hybridPrecisionWait
        }
    }

    private static void sendServerAck() {
        try {
            synchronized (os) {
                os.writeUTF(GUIData.ServerAck);
                os.flush();
            }
        } catch (Exception e) {
            LogData.LogThis(Level.ERROR, "Failed to send ServerAck: " + e);
        }
    }

    private static void processPayload(String payload) {
        try {
            String[] parts = payload.split(GUIData.SocketStringSeparator, 2);

            if (parts.length < 2) {
                LogData.LogThis(Level.WARN, "Received raw payload without separator: " + payload);
                return;
            }

            String messageType = parts[0];
            String jsonContent = parts[1];

            if (!jsonContent.trim().startsWith("[")) {
                LogData.LogThis(Level.WARN, "Payload content is not a valid JSONArray: " + jsonContent);
                return;
            }

            if (messageType.contains(GUIData.OrderStatus) || payload.contains(GUIData.OrderStatus)) {
                JSONArray allOrders = new JSONArray(jsonContent);
                GUIData.setAllAcctsOrders(allOrders);
            } else if (messageType.contains(GUIData.PositionStatus) || payload.contains(GUIData.PositionStatus)) {
                JSONArray positionsArray = new JSONArray(jsonContent);
                PositionsData.setAllAcctPositions(positionsArray);
            } else if (messageType.contains(GUIData.QuoteUpdates) || payload.contains(GUIData.QuoteUpdates)) {
                JSONArray quotesArray = new JSONArray(jsonContent);
                GUIData.setSchwabSymbolQuotes(quotesArray);
            }
        } catch (Exception e) {
            LogData.LogThis(Level.ERROR, "Payload Processing Exception: " + e + " | Full Payload: " + payload);
        }
    }
    
    private static void InitSocket() {
        try {
            // 1. Attempt binding to the preferred configured port
            try {
                serverSocket = new ServerSocket(Config.SchwabOrdersSocketNumber);
            } catch (IOException e) {
                // 2. Port occupied: Ask the OS to assign the first available dynamic port (port 0)
                serverSocket = new ServerSocket(0);
                
                // 3. Update Config so the rest of the application knows the actual assigned port
                Config.SchwabOrdersSocketNumber = serverSocket.getLocalPort();
                
                String portMsg = "Orders port not free, dynamically assigned port = " + Config.SchwabOrdersSocketNumber + "\n";
                LogData.LogThis(Level.INFO, portMsg);
                java.awt.EventQueue.invokeLater(() -> clientWindow.append(portMsg));
            }

            // Signal to main application startup that the server socket is bound and listening
            GUIData.setOrdersSocketInitDone(true);

            String msg = "Server socket listener initialized on Port = " + Config.SchwabOrdersSocketNumber + "\n";
            java.awt.EventQueue.invokeLater(() -> clientWindow.append(msg));
            LogData.LogThis(Level.INFO, msg);

        } catch (Exception e) {
            connected = false;
            GUIData.setOrdersSocketInitDone(false);
            LogData.LogThis(Level.ERROR, "Exception in InitSocket: " + e);
            java.awt.EventQueue.invokeLater(() -> 
                clientWindow.append("Exception in InitSocket in Socket Thread!\n")
            );
        }
    }

    private static void CloseSocket() {
        try {
            connected = false;
            reassemblyBuffers.clear();

            // Flag socket state as torn down
            GUIData.setOrdersSocketInitDone(false);

            String msg = "The server socket connection is closed!\n";
            java.awt.EventQueue.invokeLater(() -> clientWindow.append(msg));
            LogData.LogThis(Level.INFO, msg);

            if (is != null) try { is.close(); } catch (IOException ignored) {}
            if (os != null) try { os.close(); } catch (IOException ignored) {}
            if (clientSocket != null && !clientSocket.isClosed()) try { clientSocket.close(); } catch (IOException ignored) {}
            if (serverSocket != null && !serverSocket.isClosed()) try { serverSocket.close(); } catch (IOException ignored) {}

        } catch (Exception e) {
            LogData.LogThis(Level.ERROR, "Exception in CloseSocket: " + e);
        }
    }
}