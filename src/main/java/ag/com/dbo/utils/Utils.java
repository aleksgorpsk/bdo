package ag.com.dbo.utils;

import ag.com.dbo.controllers.queue.QueueStatus;
import ag.com.dbo.models.management.StepInstance;
import ag.com.dbo.models.management.StepStatus;
import ag.com.dbo.models.queue.QueueStorage;
import ag.com.dbo.repositories.management.StepInstanceRepository;
import ag.com.dbo.repositories.queue.QueueStorageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.util.Map;

import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;

public class Utils {

    public static ObjectMapper getObjectMapper() {
        return JsonMapper.builder()
                .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
                .build();
    }

    public static Map<String, Object> getMap(String jsonString) throws JsonProcessingException {
        return getObjectMapper().readValue(jsonString, new TypeReference<>() {
        });
    }

    public static String objectToString(Object input) throws JsonProcessingException {
        return getObjectMapper().writeValueAsString(input);
    }


    public static void saveError(StepInstance si, StepInstanceRepository stepInstanceRepository, Throwable e, String message) {
        String errorTrace = getStackTrace(e);
        si.addLog(OffsetDateTime.now() + errorTrace);
        String error = OffsetDateTime.now() + ": " + message + ": " + e.getMessage();
        si.addLog(error);
        si.setStatus(StepStatus.Failed.name());
        stepInstanceRepository.saveAndFlush(si);

    }

    public static void saveError(QueueStorage queue, QueueStorageRepository queueStorageRepository, Throwable e, String message) {
        String errorTrace = getStackTrace(e);
        queue.addLog(OffsetDateTime.now() + errorTrace);
        String error = OffsetDateTime.now() + ": " + message + ": " + e.getMessage();
        queue.addLog(error);
        queue.setStatus(QueueStatus.FAIL.name());
        queueStorageRepository.saveAndFlush(queue);
    }
}