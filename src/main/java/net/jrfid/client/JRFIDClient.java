package net.jrfid.client;

import com.google.gson.Gson;
import net.jrfid.JRFIDConfig;
import net.jrfid.database.Connections;
import org.apache.commons.dbcp2.BasicDataSource;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;

public class JRFIDClient {

    public static JRFIDClient client;

    //
    private final File configFile = new File("jrfidclient-config.json");

    private BasicDataSource connectionPool;
    private Connections connections;


    private JRFIDConfig config;

    private ScheduledExecutorService executor;


    public JRFIDClient() throws UnknownHostException {
        client = this;

        this.executor = Executors.newScheduledThreadPool(32);

        System.out.println("---------------JRFIDClient---------------");
        System.out.println("-----------------------------------------");

        //CONFIG
        if (!checkConfigFile())
            return;

        Gson gson = new Gson();
        try {
            config = gson.fromJson(new String(Files.readAllBytes(configFile.toPath())), JRFIDConfig.class);
        } catch (IOException e) {
            System.out.println("An error occurred.");
            System.out.println("JRFIDClient will stop.");
        }

        System.out.println("------------------------------------------");
        System.out.println("SQL CREDENTIALS -------------------------");
        System.out.println("HOST IS " + config.getSqlHost());
        System.out.println("DATABASE IS " + config.getSqlDatabase());
        System.out.println("USER IS " + config.getSqlUser());
        System.out.println("PASSWORD IS " + config.getSqlPassword());
        System.out.println("------------------------------------------");

        this.initPool();


        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            System.out.println("Fermeture de JRFIDClient..");
            System.out.println("Aurevoir!");
        }));
    }

    private void initPool(){
        System.out.println("INFO: MYSQL Tentative de connexion en cours...");
        connectionPool = new BasicDataSource();
        connectionPool.setDriverClassName("com.mysql.jdbc.Driver");
        connectionPool.setUsername(config.getSqlUser());
        connectionPool.setPassword(config.getSqlPassword());
        connectionPool.setUrl("jdbc:mysql://" + config.getSqlHost() + ":" + 3306 + "/" + config.getSqlDatabase() + "?autoReconnect=true");
        connectionPool.setInitialSize(1);
        connectionPool.setMaxTotal(10);
        connections = new Connections(connectionPool);
    }

    private boolean checkConfigFile() {
        if (!configFile.exists()) {
            System.out.println("Could not find \"" + configFile.getName() + "\"");
            try {
                System.out.println("Creating the \"" + configFile.getName() + "\" file.");
                if (configFile.createNewFile()) {
                    config = new JRFIDConfig();
                    config.setDebug(false);
                    config.setSqlHost("SQL-HOST");
                    config.setSqlDatabase("SQL-DATABASE");
                    config.setSqlUser("SQL-USER");
                    config.setSqlPassword("SQL-PASSWORD");


                    Gson gson = new Gson();
                    Files.write(configFile.toPath(), gson.toJson(config, config.getClass()).getBytes());

                    System.out.println("Credentials file created.");
                    System.out.println("-------------------------------");
                    System.out.println("PLEASE COMPLETE THE CONFIG FILE");
                    System.out.println("-------------------------------");
                    System.out.println("###############################");
                    System.out.println("###############################");
                    System.out.println("###############################");
                    System.out.println("UNSTABLE BEHAVIOUR INCOMING. PLEASE RELOAD THE SERVER.");
                    System.out.println("###############################");
                    System.out.println("###############################");
                    System.out.println("###############################");
                } else {
                    System.out.println("Unable to create the \"" + configFile.getName() + "\" file.");
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            System.out.println("You should shut down the server.");
            return false;
        }
        System.out.println("Credentials file found!");
        return true;
    }


    public Connections getConnections() { return connections; }

}
