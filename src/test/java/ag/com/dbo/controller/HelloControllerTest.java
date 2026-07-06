package ag.com.dbo.controller;


import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import ag.com.dbo.controllers.QueueController;
import ag.com.dbo.models.management.statuses.QueueInfo;
import ag.com.dbo.services.queue.MultithreadExecutor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RunWith(MockitoJUnitRunner.class) //@AutoConfigureMockMvc
public class HelloControllerTest {


    private MockMvc mockMvc;

    @Mock
    MultithreadExecutor multithreadExecutor; // Mock the dependent layer



    @InjectMocks
    private QueueController queueController; // Inject mock into controller


    @Before
    public void setUp() {
        // Initialize MockMvc in Standalone Mode
        this.mockMvc = MockMvcBuilders.standaloneSetup(queueController).build();
    }

    @Test
    public void getHello() throws Exception {
        QueueInfo info = new QueueInfo(10,3);
        when(multithreadExecutor.getFreeSpots()).thenReturn(info);

        // When & Then
        mockMvc.perform(get("/queue/info", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Asserts HTTP 200
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.freeSlots").value(10)) // Validates JSON fields
                .andExpect(jsonPath("$.busy").value(3));

    }
}