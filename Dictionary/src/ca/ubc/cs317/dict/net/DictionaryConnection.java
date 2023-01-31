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
    private boolean debug = true;


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
        try{socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Receive the initial welcome message
        String welcomeMessage = in.readLine();
        if (welcomeMessage == null|| welcomeMessage.startsWith("5")||welcomeMessage.startsWith("4")) {
            throw new DictConnectionException();
        }
        System.out.println(welcomeMessage);}

        catch(Exception e){
            System.out.println("Connection with specified port number went wrong." + e.getMessage());
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
        Set<String> matchset = new LinkedHashSet<>();

        // TODO Add your code here
        try{
            out.println("MATCH " + database.getName() + " " + strategy.getName() + " " + "\"" + word + "\"");
            System.out.println("MATCH " + database.getName() + " " + strategy.getName() + " " + "\"" + word + "\"");


            Status response = readStatus(in);
            System.out.println(response.getStatusCode());
//            switch (response.getStatusCode()) {
//                case 250:
                    String line;
                    while (!(line = in.readLine()).startsWith(".")) {
                        //if(!(line.contains("250")||line.contains("111"))){
                            String[] parts = splitAtoms(line);
                            String match = parts[1];
                            matchset.add(match);
                        }//}
//                case 550:
//                    throw new DictConnectionException("Invalid database");
//                case 551:
//                    throw new DictConnectionException("Invalid strategy");
//                case 552:
//                    break;
//            }



            if(debug){
            Set<String> temp= matchset;
            if (!temp.isEmpty()) {
                // Converting the above Map to an array
                String arr[] = new String[temp.size()];
                arr = temp.toArray(arr);

                // Accessing the first element by passing 0
                // as an argument which by default
                // accesses and prints out first element
                System.out.println("First element: " + arr[0]);
                System.out.println("2nd element: " + arr[1]);
                System.out.println("3rd element: " + arr[2]);
                System.out.println("4rd element: " + arr[3]);
                System.out.println("5rd element: " + arr[4]);
                System.out.println("6rd element: " + arr[5]);
            }}


            if(false){
            System.out.println("Matches for keyword '" + word + "':");
            for (String match : matchset) {
                System.out.println(match);
            }}

        }catch (Exception e){
            throw new DictConnectionException("Failed to get matches");
        }

        return matchset;
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
//            out.println("SHOW STRATEGIES");
//            Status response = readStatus(input);
            String line;
            while (!(line = in.readLine()).startsWith(".")) {
                if(!(line.contains("250")||line.contains("111"))){
                String[] parts = splitAtoms(line);

                set.add(new MatchingStrategy(parts[0], parts[1]));
                }
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
