package utility;

import com.example.valentina.virtuallifecoach.model.ExpiredGoal;
import com.example.valentina.virtuallifecoach.model.Goal;
import com.example.valentina.virtuallifecoach.model.GoalFeedback;
import com.example.valentina.virtuallifecoach.model.Measurement;
import com.example.valentina.virtuallifecoach.model.Person;
import com.example.valentina.virtuallifecoach.model.Recipe;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class ServicesUtility {
    public final static String baseURL = "http://10.196.201.231:10000/virtualcoach/";

    public static Person unmarshallPerson(String jsonResult) {
        try {
            return new ObjectMapper().readValue(jsonResult, Person.class);
        } catch (IOException e) {
            return null;
        }
    }

    public static Goal unmarshallGoal(String jsonResult) {
        try {
            return new ObjectMapper().readValue(jsonResult, Goal.class);
        } catch (IOException e) {
            return null;
        }
    }

    public static Recipe unmarshallRecipe(String jsonResult) {
        try {
            return new ObjectMapper().readValue(jsonResult, Recipe.class);
        } catch (IOException e) {
            return null;
        }
    }

    public static Measurement unmarshallMeasurement(String jsonResult) {
        try {
            return new ObjectMapper().readValue(jsonResult, Measurement.class);
        } catch (IOException e) {
            return null;
        }
    }

    public static GoalFeedback unmarshallGoalFeedback(String jsonResult) {
        try {
            return new ObjectMapper().readValue(jsonResult, GoalFeedback.class);
        } catch (IOException e) {
            return null;
        }
    }

    public static String unmarshallSuggestion(String jsonResult) {
        try {
            JsonNode tree = new ObjectMapper().readTree(jsonResult);
            return tree.path("generalComment").asText();
        } catch (IOException e) {
            return null;
        }
    }

    public static List<Goal> unmarshallListOfGoals(String jsonResult) {
        try {
            return new ObjectMapper().readValue(jsonResult, new TypeReference<List<Goal>>() {
            });
        } catch (IOException e) {
            return null;
        }
    }

    public static List<ExpiredGoal> unmarshallListOfExpiredGoals(String jsonResult) {
        try {
            return new ObjectMapper().readValue(jsonResult, new TypeReference<List<ExpiredGoal>>() {
            });
        } catch (IOException e) {
            return null;
        }
    }

    public static List<Measurement> unmarshallListOfMeasurements(String jsonResult) {
        try {
            return new ObjectMapper().readValue(jsonResult, new TypeReference<List<Measurement>>() {
            });
        } catch (IOException e) {
            return null;
        }
    }
}
