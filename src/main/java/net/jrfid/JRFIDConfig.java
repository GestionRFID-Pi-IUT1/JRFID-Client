package net.jrfid;

public class JRFIDConfig {


    private String sqlHost;
    private String sqlDatabase;
    private String sqlUser;
    private String sqlPassword;


    private boolean debug;

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getSqlDatabase() {
        return sqlDatabase;
    }

    public void setSqlDatabase(String sqlDatabase) {
        this.sqlDatabase = sqlDatabase;
    }

    public String getSqlUser() {
        return sqlUser;
    }

    public void setSqlUser(String sqlUser) {
        this.sqlUser = sqlUser;
    }

    public String getSqlPassword() {
        return sqlPassword;
    }

    public void setSqlPassword(String sqlPassword) {
        this.sqlPassword = sqlPassword;
    }

    public String getSqlHost() {
        return sqlHost;
    }

    public void setSqlHost(String sqlHost) {
        this.sqlHost = sqlHost;
    }
}