package app.persistence;

import app.exceptions.DatabaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrdersMapperTest {
    ConnectionPool connectionPool;
    @BeforeEach
    public void setUp() {
        String DEFAULT_URL = "jdbc:postgresql://161.35.204.41:5432/%s?currentSchema=public";
        String DEFAULT_DB = "carport";
        String DEFAULT_USER = "postgres";
        String DEFAULT_PASSWORD = System.getenv("Default_Password");
        connectionPool = ConnectionPool.getInstance(DEFAULT_USER, DEFAULT_PASSWORD, DEFAULT_URL, DEFAULT_DB);
    }

    @Test
    void getOrderByEmail() throws DatabaseException {
        assertTrue(OrdersMapper.getOrderByEmail("larsgroenberg@gmail.com",connectionPool).getOrderId() != 0);
    }

    @Test
    void getAllOrders() throws DatabaseException {
        assertTrue(!OrdersMapper.getAllOrders(connectionPool).isEmpty());
    }
}