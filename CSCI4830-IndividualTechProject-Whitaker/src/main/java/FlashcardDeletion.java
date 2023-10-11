import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;


@WebServlet("/FlashcardDeletion")
public class FlashcardDeletion extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public FlashcardDeletion() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        Connection connection = null;
        try {
            DBConnectionWhitaker.getDBConnection();
            connection = DBConnectionWhitaker.connection;
            JSONObject requestData = new JSONObject(request.getReader().lines().reduce("", String::concat));
            int cardID = requestData.getInt("cardID");
            System.out.println(cardID);
            String deleteCardQuery = "DELETE FROM flashcards WHERE cardID = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(deleteCardQuery);
            preparedStatement.setInt(1, cardID);
            int rowsAffected = preparedStatement.executeUpdate();

            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("success", rowsAffected > 0);
            out.print(jsonResponse.toString());

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
            out.flush();
        }
    }

}

