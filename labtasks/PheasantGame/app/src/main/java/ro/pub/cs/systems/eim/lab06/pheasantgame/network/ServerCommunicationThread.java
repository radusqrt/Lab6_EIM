package ro.pub.cs.systems.eim.lab06.pheasantgame.network;

import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ro.pub.cs.systems.eim.lab06.pheasantgame.general.Constants;
import ro.pub.cs.systems.eim.lab06.pheasantgame.general.Utilities;

public class ServerCommunicationThread extends Thread {

    private Socket socket;
    private TextView serverHistoryTextView;

    private Random random = new Random();

    private String expectedWordPrefix = new String();

    public ServerCommunicationThread(Socket socket, TextView serverHistoryTextView) {
        if (socket != null) {
            this.socket = socket;
            Log.d(Constants.TAG, "[SERVER] Created communication thread with: " + socket.getInetAddress() + ":" + socket.getLocalPort());
        }
        this.serverHistoryTextView = serverHistoryTextView;
    }

    public void run() {
        try {
            if (socket == null) {
                return;
            }
            boolean isRunning = true;
            BufferedReader requestReader = Utilities.getReader(socket);
            PrintWriter responsePrintWriter = Utilities.getWriter(socket);

            while (isRunning) {
                // TODO exercise 7a
                String line = requestReader.readLine();
                Log.d(Constants.TAG, "[SERVER] Received: " + line);
                if (Constants.END_GAME.equals(line)) {
                    responsePrintWriter.print(Constants.END_GAME);
                    break;
                }

                if (Utilities.wordValidation(line) == true) {
                    Log.d(Constants.TAG, "[SERVER] Valid word: " + line);
                    if (expectedWordPrefix.length() == 2 && !expectedWordPrefix.equals(line.substring(0, 3))) {
                        responsePrintWriter.print(line);
                    } else {
                        String prefix = line.substring(line.length() - 2);
                        List<String> list = Utilities.getWordListStartingWith(prefix);
                        if (list == null || list.size() == 0) {
                            responsePrintWriter.print(Constants.END_GAME);
                        } else {
                            String response = list.get(random.nextInt() % list.size());
                            Log.d(Constants.TAG, "[SERVER] Response: " + response);
                            responsePrintWriter.print(response);
                            expectedWordPrefix = response.substring(response.length() - 2);
                        }
                    }
                } else {
                    Log.d(Constants.TAG, "[SERVER] Not a valid word: " + line);
                    responsePrintWriter.print(line);
                }
            }

            socket.close();
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
    }
}
