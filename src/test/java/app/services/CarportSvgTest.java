package app.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;



public class CarportSvgTest {

    private Context ctx;
    private CarportSvg carportSvg;

    int width = 500;
    int length = 700;
    int height = 250;
    double shedLength = 0;
    double shedWidth = 0;
    @BeforeEach
    public void setUp() {
        ctx = mock(Context.class);
        carportSvg = new CarportSvg(ctx, width, length,height,shedLength,shedWidth);
        //carportBuilder = new CarportBuilder(carportSvg); // Assuming CarportBuilder takes CarportSvg as a dependency
    }

    @Test
    public void testAddPolesWithoutShed() {
        // Given
         width = 500;
         length = 700;
         height = 250;
         shedLength = 0;
         shedWidth = 0;

        // When
        int result = carportSvg.addPoles(ctx, width, length, height, shedLength, shedWidth);
        // Then
        assertEquals(6, result);
        // When
        result = carportSvg.addPoles(ctx, width, length-300, height, shedLength, shedWidth);
        // Then
        assertEquals(4, result);

        result = carportSvg.addPoles(ctx, width, length+300, height, shedLength, shedWidth);
        // Then
        assertEquals(8, result);
    }

    @Test
    public void testAddPolesWithShed() {
        // Given
        width = 500;
        length = 700;
        height = 250;
        shedLength = 300;
        shedWidth = 500;

        // When
        int result = carportSvg.addPoles(ctx, width, length, height, shedLength, shedWidth);
        // Then
        assertEquals(8, result);


        // We still expect the result is 8 because if there's no pole at the start of the shed one should be added
        // When
        result = carportSvg.addPoles(ctx, width, length-300, height, shedLength, shedWidth);
        // Then
        assertEquals(8, result);


        // When
        result = carportSvg.addPoles(ctx, width, length, height, shedLength-300, shedWidth-500);
        // Then
        assertEquals(6, result);


        // When
        result = carportSvg.addPoles(ctx, width, length+300, height, shedLength, shedWidth);
        // Then
        assertEquals(10, result);
    }

    //Beams/Rem skulle gerne altid kun være 2
    @Test
    void addBeams() {
        assertEquals(2,carportSvg.addBeams(width,length));
    }




    //TODO: tilføj flere tests
}
