package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Updated
 */
@WebServlet("/Updated")
public class Updated extends HttpServlet {
private static final long serialVersionUID = 1L;
	private Connection conn = null;
	private Statement st = null;
	private ResultSet rs = null;
	private PreparedStatement ps = null;
       
	// method to connect to database
	private void connect() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://35.193.248.160/main?user=root&password=RandallWilliams");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    public Updated() {
        super();
        connect();
    }

	// handles get request - searches for a user by email
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
		response.setContentType("applicaton/json");
		try {
			ps = conn.prepareStatement("SELECT COUNT(*) AS count FROM posts;");
			rs = ps.executeQuery();
			if (rs.next()) {
				out.println("{");
				out.println("\"count\": " + rs.getString("count"));
				out.println("}");
			} else {
				out.println("null");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		out.flush();
	}
	
	
	// method to check if a user is in the database
	protected int findUser(String email) {
		try {
			ps = conn.prepareStatement("SELECT * FROM users WHERE email = ?;");
			ps.setString(1, email);
			rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getInt("user_id");
			} else {
				return -1;
			}
		} catch (SQLException e) {
			return -1;
		}
	}

	
	// Handles post request - checks if the user is already in the database or else creates a new user
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		out.print("{\"alreadyExists\": ");
		if (findUser(request.getParameter("email")) <= 0) {
			out.print("false, ");
			try {
				ps = conn.prepareStatement("INSERT INTO users (email, display_name) VALUES (?, ?);");
				ps.setString(1, request.getParameter("email"));
				ps.setString(2, request.getParameter("name"));
				ps.executeUpdate();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				out.println("Create User Failed.");
			}
		} else {
			out.print("true, ");
		}
		out.print("\"user_id\": " + findUser(request.getParameter("email")) + "}");
		out.flush();
	}

}
