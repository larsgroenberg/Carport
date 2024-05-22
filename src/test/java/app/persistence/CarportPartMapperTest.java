package app.persistence;


import app.exceptions.DatabaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CarportPartMapperTest {
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
    void getDBParts() throws DatabaseException {
        assertTrue(!CarportPartMapper.getDBParts(connectionPool).isEmpty());
    }

    @Test
    void getPartById() throws DatabaseException {
        assertTrue(!CarportPartMapper.getPartById(1,connectionPool).getDBname().isEmpty());
    }


    @Test
    void getCompletePartsListByOrderId() throws DatabaseException {
        assertTrue(!CarportPartMapper.getCompletePartsListByOrderId(435,connectionPool).isEmpty());
    }
}