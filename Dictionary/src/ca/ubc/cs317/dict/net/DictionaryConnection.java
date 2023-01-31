package ca.ubc.cs317.dict.net;

import ca.ubc.cs317.dict.model.Database;
import ca.ubc.cs317.dict.model.Definition;
import ca.ubc.cs317.dict.model.MatchingStrategy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

import static ca.ubc.cs317.dict.net.Status.readStatus;
import static ca.ubc.cs317.dict.net.DictStringParser.splitAtoms;

/**
 * Created by Jonatan on 2017-09-09.
 */
public class DictionaryConnection {

    private static final int DEFAULT_PORT = 2628;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean debug = false;


    /** Establishes a new connection with a DICT server using an explicit host and port number, and handles initial
     * welcome messages.
     *
     * @param host Name of the host where the DICT server is running
     * @param port Port number used by the DICT server
     * @throws DictConnectionException If the host does not exist, the connection can't be established, or the messages
     * don't match their expected value.
     */
    public DictionaryConnection(String host, int port) throws DictConnectionException {

        // TODO Add your code here
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Receive the initial welcome message
            String welcomeMessage = in.readLine();
            if (welcomeMessage == null || welcomeMessage.startsWith("5") || welcomeMessage.startsWith("4")) {
                throw new DictConnectionException();
            }
            System.out.println(welcomeMessage);


        } catch (Exception e) {
            System.out.println("Connection went wrong." + e.getMessage());
            throw new DictConnectionException();
        }

    }

    /** Establishes a new connection with a DICT server using an explicit host, with the default DICT port number, and
     * handles initial welcome messages.
     *
     * @param host Name of the host where the DICT server is running
     * @throws DictConnectionException If the host does not exist, the connection can't be established, or the messages
     * don't match their expected value.
     */
    public DictionaryConnection(String host) throws DictConnectionException {
        this(host, DEFAULT_PORT);
    }

    /** Sends the final QUIT message and closes the connection with the server. This function ignores any exception that
     * may happen while sending the message, receiving its reply, or closing the connection.
     *
     */
    public synchronized void close() {

        // TODO Add your code here
        try{
            out.println("QUIT");
        String goodbyeMessage = in.readLine();
        System.out.println(goodbyeMessage);

        // Close the socket connection
        in.close();
        out.close();
        socket.close();}
        catch(Exception e){
            System.out.println("Something went wrong when closing. Error:" + e.getMessage());
        }
    }

    /** Requests and retrieves all definitions for a specific word.
     *
     * @param word The word whose definition is to be retrieved.
     * @param database The database to be used to retrieve the definition. A special database may be specified,
     *                 indicating either that all regular databases should be used (database name '*'), or that only
     *                 definitions in the first database that has a definition for the word should be used
     *                 (database '!').
     * @return A collection of Definition objects containing all definitions returned by the server.
     * @throws DictConnectionException If the connection was interrupted or the messages don't match their expected value.
     */
    public synchronized Collection<Definition> getDefinitions(String word, Database database) throws DictConnectionException {
        Collection<Definition> set = new ArrayList<>();

        // TODO Add your code here
        out.println("DEFINE " + database.getName() + " " + "\"" + word + "\"");

        Status response = readStatus(in);
        System.out.println(response.getStatusCode());

        switch (response.getStatusCode()) {
            case 150:
                String[] initialResponse = splitAtoms(response.getDetails());
                int numDefinitions = Integer.parseInt(initialResponse[0]);
                for (int i = 0; i < numDefinitions; i++) {
                    Status current = readStatus(in);
                    if (current.getStatusCode() != 151) throw new DictConnectionException();
                    Definition df = new Definition(word, database.getName());
                    try {
                        String line;
                        while (!(line = in.readLine()).startsWith(".")) {
                            df.appendDefinition(line);
                        }
                    }catch (Exception e) {
                        throw new DictConnectionException();
                    }
                    set.add(df);
                }

                Status closing = readStatus(in);
                if (closing.getStatusCode() != 250) throw new DictConnectionException();
                break;

                /*try {
                    String line;
                    while (!(line = in.readLine()).startsWith(".")) {
                        String[] parts = splitAtoms(line);
                        String string = parts[0];
                        String databaseName = parts[1];

                        Definition def = new Definition(string, databaseName);
                        set.add(def);
                    }
                } catch(Exception e){
                    throw new DictConnectionException();
                }
                Status finalResponse = readStatus(in);
                if (finalResponse.getStatusCode() != 250) throw new DictConnectionException();
                break;*/
            case 550:
//                throw new DictConnectionException("Invalid database");
                break;

            case 552:
                // do nothing
                break;
            default:
                throw new DictConnectionException("INVALID RESPONSE");
        }

        return set;
    }

    /** Requests and retrieves a list of matches for a specific word pattern.
     *
     * @param word     The word whose definition is to be retrieved.
     * @param strategy The strategy to be used to retrieve the list of matches (e.g., prefix, exact).
     * @param database The database to be used to retrieve the definition. A special database may be specified,
     *                 indicating either that all regular databases should be used (database name '*'), or that only
     *                 matches in the first database that has a match for the word should be used (database '!').
     * @return A set of word matches returned by the server.
     * @throws DictConnectionException If the connection was interrupted or the messages don't match their expected value.
     */
    public synchronized Set<String> getMatchList(String word, MatchingStrategy strategy, Database database) throws DictConnectionException {
        Set<String> set = new LinkedHashSet<>();

        // TODO Add your code here

        out.println("MATCH " + database.getName() + " " + strategy.getName() + " " + "\"" + word + "\"");
        System.out.println("MATCH " + database.getName() + " " + strategy.getName() + " " + "\"" + word + "\"");

        Status response = readStatus(in);
        System.out.println("Match list first response: " + response.getStatusCode());

        switch (response.getStatusCode()) {
            case 152:
                try {
                    String line;

                    while (!(line = in.readLine()).startsWith(".")) {
                        //if(!(line.contains("250")||line.contains("111"))){
                        String[] parts = splitAtoms(line);
                        System.out.println(line);
                        String match = parts[1];
                        set.add(match);
                    }//}

                } catch (Exception e) {
                    throw new DictConnectionException("Failed to get matches");
                }
                Status finalResponse = readStatus(in);
                System.out.println("Match list final response: " + finalResponse.getStatusCode());
                if (finalResponse.getStatusCode() != 250) throw new DictConnectionException();
                break;

            case 550:
                break;
//                throw new DictConnectionException("Invalid database");
            case 551:
                break;
//                throw new DictConnectionException("Invalid strategy");
            case 552:
                break;
            default:
                throw new DictConnectionException("INVALID response");
        }

        //examine first 6 elements of set of matched words
        if (debug) {
            Set<String> temp = set;
            if (!temp.isEmpty()) {
                // Converting the above Map to an array
                String arr[] = new String[temp.size()];
                arr = temp.toArray(arr);

                System.out.println("First element: " + arr[0]);
                System.out.println("2nd element: " + arr[1]);
                System.out.println("3rd element: " + arr[2]);
                System.out.println("4th element: " + arr[3]);
                System.out.println("5th element: " + arr[4]);
                System.out.println("6th element: " + arr[5]);
            }
        }
            if(debug){
            System.out.println("Matches for '" + word + "':");
            for (String match : set) {
                System.out.println(match);
            }}

        return set;
    }

    /** Requests and retrieves a map of database name to an equivalent database object for all valid databases used in the server.
     *
     * @return A map of Database objects supported by the server.
     * @throws DictConnectionException If the connection was interrupted or the messages don't match their expected value.
     */
    public synchronized Map<String, Database> getDatabaseList() throws DictConnectionException {
        Map<String, Database> databaseMap = new HashMap<>();

        // TODO Add your code here
        try{
            out.println("SHOW DB");
            Status response = readStatus(in);

            switch (response.getStatusCode()) {
                case 110:
                    String line;
                    while (!(line = in.readLine()).startsWith(".")) {
                        //if(!(line.contains("110"))){
                            String[] parts = splitAtoms(line);
                            String name = parts[0];
                            String description = parts[1];
                            databaseMap.put(name, new Database(name, description));
                        //}
                    }

                    Status finalResponse = readStatus(in);
                    if (finalResponse.getStatusCode() != 250) throw new DictConnectionException();
                    break;
                case 554:
                    break;
                default:
                    throw new DictConnectionException();
            }
            // display the list of databases
            if(debug){
            System.out.println("Databases:");
            for (Map.Entry<String, Database> entry : databaseMap.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue().getDescription());
            }}
        }catch (Exception e){
            throw new DictConnectionException();
        }
        return databaseMap;
    }

    /** Requests and retrieves a list of all valid matching strategies supported by the server.
     *
     * @return A set of MatchingStrategy objects supported by the server.
     * @throws DictConnectionException If the connection was interrupted or the messages don't match their expected value.
     */
    public synchronized Set<MatchingStrategy> getStrategyList() throws DictConnectionException {
        Set<MatchingStrategy> set = new LinkedHashSet<>();
        // TODO Add your code here
        try {
            out.println("SHOW STRAT");

            Status response = readStatus(in);
            System.out.println(response.getStatusCode());

            switch (response.getStatusCode()) {
                case 111:

                    String line;
                    while (!(line = in.readLine()).startsWith(".")) {
                        //if (!(line.contains("250") || line.contains("111"))) {
                            String[] parts = splitAtoms(line);
                            set.add(new MatchingStrategy(parts[0], parts[1]));
                        //}
                    }
                    Status finish = readStatus(in);
                    if (finish.getStatusCode() != 250) throw new DictConnectionException();
                    break;
                case 555:
                    break;
            }

            // display the list of strategies
            if(debug){
            System.out.println("Strategies:");
            for (MatchingStrategy strategy : set) {
                System.out.println(strategy.getName() + "   " + strategy.getDescription());
            }}

            return set;
        } catch (Exception e) {
            throw new DictConnectionException();
        }
    }

    /** Requests and retrieves detailed information about the currently selected database.
     *
     * @return A string containing the information returned by the server in response to a "SHOW INFO <db>" command.
     * @throws DictConnectionException If the connection was interrupted or the messages don't match their expected value.
     */
    public synchronized String getDatabaseInfo(Database d) throws DictConnectionException {
	StringBuilder sb = new StringBuilder();

        // TODO Add your code here
        try {
            out.println("SHOW INFO " + d.getName());
            String line;
            while (!(line = in.readLine()).startsWith(".")) {
                sb.append(line);
                //System.out.println(line);
            }
        } catch(Exception e){
            throw new DictConnectionException();
        }
        return sb.toString();
    }
}
