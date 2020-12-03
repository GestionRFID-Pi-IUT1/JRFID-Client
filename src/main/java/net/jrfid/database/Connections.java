package net.jrfid.database;

import net.jrfid.client.JRFIDClient;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;

public class Connections {

    private BasicDataSource connectionPool;

    public Connections(BasicDataSource connectionPool) {
        this.connectionPool = connectionPool;
    }

    public Connection getMySQL() throws SQLException {
        return connectionPool.getConnection();
    }

     /*=========================
        Méthode pour sql pool
     ==========================*/

    public void update(String qry) {
        try (Connection c = getMySQL();
             PreparedStatement state = c.prepareStatement(qry)){
            state.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Object query(String qry, Function<ResultSet, Object> consumer) {
        try (Connection c = getMySQL();
             PreparedStatement state = c.prepareStatement(qry);
             ResultSet resultSet = state.executeQuery()){
            return consumer.apply(resultSet);
        } catch (SQLException e) {
            throw new IllegalStateException("Some things went wrong..." + e.getMessage());
        }
    }

    public void query(String qry, Consumer<ResultSet> consumer) {
        try (Connection c = getMySQL();
             PreparedStatement state = c.prepareStatement(qry);
             ResultSet resultSet = state.executeQuery()){
            consumer.accept(resultSet);
        } catch (SQLException e) {
            throw new IllegalStateException("Some things went wrong..." + e.getMessage());
        }
    }
}