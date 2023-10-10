import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Servlet implementation class FlashcardJava
 */

@WebServlet("/FlashcardJava")
public class FlashcardCreation_and_Retrieval extends HttpServlet {
	private static final long serialVersionUID = 1L;

       
    public FlashcardCreation_and_Retrieval() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        Connection connection = null;
        String selectAllCardsQuery = "SELECT * FROM flashcards"; // Adjust your SQL query as needed

        try {
            DBConnectionWhitaker.getDBConnection();
            connection = DBConnectionWhitaker.connection;

            PreparedStatement preparedStatement = connection.prepareStatement(selectAllCardsQuery);
            ResultSet resultSet = preparedStatement.executeQuery();

            JSONArray flashcardsArray = new JSONArray(); // Create a JSON array to hold all flashcards

            while (resultSet.next()) {
            	int cardID = resultSet.getInt("cardID");
                String question = resultSet.getString("Question");
                String answer = resultSet.getString("Answer");

                // Create a JSON object for each flashcard
                JSONObject flashcardObject = new JSONObject();
                flashcardObject.put("cardID", cardID);
                flashcardObject.put("Question", question);
                flashcardObject.put("Answer", answer);

                flashcardsArray.put(flashcardObject); // Add the JSON object to the array
            }

            // Create a JSON response object
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("success", true);
            jsonResponse.put("flashcards", flashcardsArray);

            out.print(jsonResponse.toString());
            out.flush();
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.err.println("Database error: " + e.getMessage());
        } catch (JSONException e) { // Handle JSONException
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.err.println("JSON error: " + e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            JSONObject requestData = new JSONObject(request.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual));

            String question = requestData.getString("question");
            String answer = requestData.getString("answer");

            // Now you can use the question and answer variables in your servlet logic

            // For testing purposes, print the received data
            System.out.println("Received question: " + question);
            System.out.println("Received answer: " + answer);

            Connection connection = null;
            String addCardStatement = "INSERT INTO flashcards (cardID, Question, Answer) VALUES (default, ?, ?)";
            try {
                DBConnectionWhitaker.getDBConnection();
                connection = DBConnectionWhitaker.connection;

                System.out.println("SQL: " + addCardStatement);
                PreparedStatement preparedStatement = connection.prepareStatement(addCardStatement);
                preparedStatement.setString(1, question);
                preparedStatement.setString(2, answer);
                preparedStatement.execute();
                connection.close();

                // Send a success response to the client
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("success", true);
                out.println(jsonResponse.toString());

            } catch (SQLException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                // Handle database-related errors and provide an error response
                System.err.println("Database error: " + e.getMessage());

                // Send an error response (customize this based on your error handling)
                response.getWriter().write("{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            // Handle the exception and provide an appropriate error response
            System.err.println("Error parsing JSON data: " + e.getMessage());

            // Send an error response (customize this based on your error handling)
            response.getWriter().write("{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
        }
    }
}

