import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
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
        String grabFlashcards = "SELECT * FROM flashcards"; 

        try {
            DBConnectionWhitaker.getDBConnection();
            connection = DBConnectionWhitaker.connection;

            PreparedStatement preparedStatement = connection.prepareStatement(grabFlashcards);
            ResultSet resultSet = preparedStatement.executeQuery();

            JSONArray flashcardsArray = new JSONArray(); 

            while (resultSet.next()) {
            	int cardID = resultSet.getInt("cardID");
                String question = resultSet.getString("Question");
                String answer = resultSet.getString("Answer");
                JSONObject flashcard = new JSONObject();
                flashcard.put("cardID", cardID);
                flashcard.put("Question", question);
                flashcard.put("Answer", answer);

                flashcardsArray.put(flashcard); 
            }


            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("success", true);
            jsonResponse.put("flashcards", flashcardsArray);

            out.print(jsonResponse.toString());
            out.flush();
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.err.println("Database error: " + e.getMessage());
        } catch (JSONException e) { 
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

            Connection connection = null;
            String addCardStatement = "INSERT INTO flashcards (Question, Answer) VALUES (?, ?)";

            try {
                DBConnectionWhitaker.getDBConnection();
                connection = DBConnectionWhitaker.connection;

                PreparedStatement preparedStatement = connection.prepareStatement(addCardStatement, Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setString(1, question);
                preparedStatement.setString(2, answer);

                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows == 0) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    System.err.println("Failed to insert flashcard.");
                    return;
                }

                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int cardID = generatedKeys.getInt(1);
                    JSONObject jsonResponse = new JSONObject();
                    jsonResponse.put("success", true);
                    jsonResponse.put("cardID", cardID);
                    out.println(jsonResponse.toString());
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    System.err.println("Failed to retrieve the generated cardID.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                System.err.println("Database error: " + e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.err.println("Error parsing JSON data: " + e.getMessage());
        }
    }
}

