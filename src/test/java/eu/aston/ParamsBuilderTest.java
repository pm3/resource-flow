package eu.aston;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.aston.model.ResourceParam;
import eu.aston.utils.ExecProcess;
import eu.aston.utils.ParamsBuilder;

public class ParamsBuilderTest {

    @Test
    public void testBuildParams() throws IOException {
        // Vytvorenie mock objektov
        ObjectMapper objectMapper = new ObjectMapper();
        ExecProcess mockExecProcess = mock(ExecProcess.class);
        
        // Vytvorenie inštancie ParamsBuilder
        ParamsBuilder paramsBuilder = new ParamsBuilder(objectMapper, mockExecProcess);
        
        // Vytvorenie testovacích parametrov
        List<ResourceParam> testParams = Arrays.asList(
            new ResourceParam("param1", "value1", null, null),
            new ResourceParam("param2", null, "namespace/name/property1", null),
            new ResourceParam("param3", null, null, "namespace/name/property2")
        );
        
        // Nastavenie očakávaného správania pre mock objekty
        when(mockExecProcess.execBuilder(any(), any())).thenReturn("{ \"property1\": \"param2\", \"property2\": \"param3\" }");
        
        // Volanie testovanej metódy
        Map<String, String> result = paramsBuilder.build(testParams);
        
        // Overenie výsledkov
        assertEquals(3, result.size());
        assertEquals("value1", result.get("param1"));
        assertNotNull(result.get("param2"));
        assertNotNull(result.get("param3"));
        
        // Overenie, že metódy mock objektov boli volané
        verify(mockExecProcess, times(2)).execBuilder(any(), any());
    }


}