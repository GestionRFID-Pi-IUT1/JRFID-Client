package net.jrfid.client;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.google.gson.Gson;
import com.pi4j.io.gpio.*;
import jline.console.ConsoleReader;
import net.jrfid.JRFIDConfig;
import net.jrfid.database.Connections;
import net.jrfid.system.SerialCom;
import net.jrfid.utils.log.JRFIDClientLogger;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JRFIDClient {

    public static JRFIDClient client;

    //
    private final File configFile = new File("jrfidclient-config.json");

    private BasicDataSource connectionPool;
    private Connections connections;

    private ConsoleReader consoleReader;
    private Logger logger;


    private JRFIDConfig config;

    private ScheduledExecutorService executor;

    /** Raspberry GPIO **/
    public GpioController controller;
    public GpioPinDigitalOutput buzzer;

    /** Raspberry Serial Port **/
    public SerialCom serialCom;


    public JRFIDClient() throws UnknownHostException {
        client = this;
        this.logger = new JRFIDClientLogger(this);

        this.executor = Executors.newScheduledThreadPool(32);

        //RASPBERRY Config
        this.controller = GpioFactory.getInstance();
        this.buzzer = controller.provisionDigitalOutputPin(RaspiPin.GPIO_24, "Buzzer", PinState.LOW);

        log(Level.INFO,"---------------JRFIDClient---------------");
        log(Level.INFO,"-----------------------------------------");

        //CONFIG
        if (!checkConfigFile())
            return;

        Gson gson = new Gson();
        try {
            config = gson.fromJson(new String(Files.readAllBytes(configFile.toPath())), JRFIDConfig.class);
        } catch (IOException e) {
            log(Level.INFO,"Erreur de configuration .json");
            log(Level.INFO,"JRFIDClient va s'arrêter.");
        }

        try {
            consoleReader = new ConsoleReader();
        } catch (IOException e) {
            e.printStackTrace();
        }

        log(Level.INFO,"------------------------------------------");
        log(Level.INFO,"SQL ID -------------------------");
        log(Level.INFO,"HOST: " + config.getSqlHost());
        log(Level.INFO,"DATABASE: " + config.getSqlDatabase());
        log(Level.INFO,"USER: " + config.getSqlUser());
        log(Level.INFO,"PASSWORD: " + config.getSqlPassword());
        log(Level.INFO,"------------------------------------------");

        this.initPool();

        consoleReader.setExpandEvents(false);

        mysqlVerification();


        EnumerateComPorts();
        serialCom = new SerialCom("ttyS0", 9600);

        //Son de lancement PIN24
        buzzer.pulse(500);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log(Level.INFO,"Fermeture de JRFIDClient..");
            log(Level.INFO,"Aurevoir!");
            log(Level.INFO,"Deconnexion de la liaison Serial Port réussie.");
            serialCom.closeSerialPort();
        }));
    }

    private void initPool(){
        log(Level.INFO,"INFO: MYSQL Tentative de connexion en cours...");
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
            log(Level.INFO,"Impossible de trouver \"" + configFile.getName() + "\"");
            try {
                log(Level.INFO,"Création du fichier config \"" + configFile.getName() + "\".");
                if (configFile.createNewFile()) {
                    config = new JRFIDConfig();
                    config.setDebug(false);
                    config.setSqlHost("SQL-HOST");
                    config.setSqlDatabase("SQL-DATABASE");
                    config.setSqlUser("SQL-USER");
                    config.setSqlPassword("SQL-PASSWORD");


                    Gson gson = new Gson();
                    Files.write(configFile.toPath(), gson.toJson(config, config.getClass()).getBytes());

                    log(Level.INFO,"Fichier configuration créé.");
                    log(Level.INFO,"-------------------------------");
                    log(Level.INFO,"S'il vous plaît, complétez le fichier");
                    log(Level.INFO,"-------------------------------");
                    log(Level.INFO,"###############################");
                    log(Level.INFO,"###############################");
                    log(Level.INFO,"###############################");
                    log(Level.INFO,"Version instable en cours, veuillez redémarrer.");
                    log(Level.INFO,"###############################");
                    log(Level.INFO,"###############################");
                    log(Level.INFO,"###############################");
                } else {
                    log(Level.INFO,"Impossible de créer le fichier \"" + configFile.getName() + "\".");
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            log(Level.INFO,"Vous devriez shutdown le serveur.");
            return false;
        }
        log(Level.INFO,"Fichier configuration trouvé !");
        return true;
    }


    public void mysqlVerification(){
        boolean[] has = {false};
        connections.query("SELECT password FROM system WHERE password = 'raspberry'", (ResultSet rs) -> {
            try {
                if(rs.next()) has[0] = true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        log(Level.INFO,"Connexion avec succès: "+has[0]);
    }

    private void EnumerateComPorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        log(Level.INFO,"Ports disponible: ");
        for (SerialPort port : ports) {
            log(Level.INFO,"- "+port.getSystemPortName());
        }
    }

    public ScheduledExecutorService getExecutor() {
        return executor;
    }
    public ConsoleReader getConsoleReader() { return consoleReader; }

    public Logger getLogger() { return logger; }

    public void log(Level level, String message) { logger.log(level, message); }

    public Connections getConnections() { return connections; }

}
