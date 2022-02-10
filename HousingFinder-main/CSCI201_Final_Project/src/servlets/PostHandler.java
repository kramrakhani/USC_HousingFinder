package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Servlet implementation class PostHandler
 */
@WebServlet("/PostHandler")
public class PostHandler extends HttpServlet{
	/*
	 * Get requests with mode all do not require any extra fields
	 * 
	 * Get requests with mode user require the author_id field (string)
	 * 
	 * Get requests with mode date require the date field (string)
	 * 
	 * Get requests with mode locality require the locality field (string), the locality field should only contain the area of interest, for example "Orange county"
	 * 
	 * Get requests with mode ID require the post_id field (int)
	 * 
	 * Delete requests must have a post_ID (int)
	 * 
	 * may add future mode that filters by zipcode/displays all addresses a certain distance from a given address
	 * 
	 * can implement update feature in front-end by calling get on a post, deleting the post, and creating a new post with the updated parameters
	 * 
	 * */
	
	//TODO: Implement getByAddress feature, implement getByLocality using google API, implement User authentication when deleting or posting a post
	//output html error codes after catching an exception
	
	public volatile Boolean dataBaseUpdated = false;//will come in handy for multithreading later
	
	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	enum Types{all, user, date, locality, address, ID, search};

	private static final long serialVersionUID = 1L;
	Connection conn = null;
	Gson gson = new Gson();
	
	public PostHandler() throws ClassNotFoundException {//gets connection to database
		super();
		try {
			connectToDatabase();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
		}
	}
	
	private class Post{
		
		
		public int author_id;
		public int post_id;
		public String description;
		public String address;
		public String time;
		public String locality;
		public String author_email;
		public double price;
		public int rooms;
		
		Post (int auth, int post, String desc, String add, String tim, String l, String e, double p, int r){
			author_id = auth;
			post_id = post;
			description = desc;
			address = add;
			time = tim;
			locality = l;
			author_email = e;
			price = p;
			rooms = r;
		}
		
		Post (int auth, int post, String desc, String add, Date tim, String l, String e, double p, int r){
			author_id = auth;
			post_id = post;
			description = desc;
			address = add;
			time = dateFormat.format(tim);
			locality = l;
			author_email = e;
			price = p;
			rooms = r;
		}
		
	}
	
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		/*
		 * 
		 * 
		 * 
		 * Add a check to make sure user is logged in, and that the post they are deleting belongs to them
		 * 
		 * IMPORTANT:
		 * ASSUMES THAT AUTHOR ID IS PROVIDED IN THE REQUEST AND IS THE ID OF THE USER THAT IS DELETING A POST
		 * 
		 * 
		 * */
		JsonObject request = makeJson(req);
		if (request == null) return;
		try {
		Statement st = conn.createStatement();
		PreparedStatement ps = conn.prepareStatement("DELETE FROM posts WHERE post_id = ? AND author_id = ?");
		ps.setInt(1, request.get("post_id").getAsInt());
		ps.setString(2, request.get("author_id").getAsString());
		ps.execute();
		}
		catch (SQLException e) {
			System.out.println("SQL exception in trying to delete");
		}
		dataBaseUpdated = true;
	}
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
		JsonObject request = makeJson(req);
		if (request == null) return;
		Types type = getRequestType(request);
		ResultSet result = null;
		try {
		switch (type) {
		case all: {
			 result = getAllPosts();
			 break;
		}
		case user:{
			result = getByUser(request.get("author_id").getAsString());
			break;
		}
		case date:{
			result = getByDate(request.get("time").getAsString());
			break;
		}
		case locality:{
			/*
			 * 
			 * 
			 * implement google api for this mode to work
			 * 
			 * 
			 * 
			 * */
			result = getByLocality(request.get("locality").getAsInt());
			break;
		}
		case address:{
			/*
			 * 
			 * 
			 * unimplemented
			 * 
			 * 
			 * 
			 * */
			break;
		}
		case search:{
			result = getSearch(request.get("search").getAsString());
			break;
		}
		case ID:{
			result = getByID(request.get("post_id").getAsInt());
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + type);
		}
		writeSet(result, resp);
		}
		catch(SQLException e) {System.out.println("SQL Exception trying to get");}
		catch(ParseException e) {System.out.println("Date formatted incorrectly");}
	}
	
	
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
		/*
		 * 
		 * add a check to make sure user is logged in
		 * 
		 * make sure the user can only make a post with their author_id
		 * 
		 * 
		 * */
		
		try {
		PreparedStatement ps = conn.prepareStatement("INSERT INTO posts (address, description, author_id, time, locality_id, rooms, price) VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?)");
		System.out.println(req.getParameterNames());
		System.out.println(req.getParameter("author_id"));
		ps.setString(1, req.getParameter("address"));
		if (req.getParameter("description") != null) {
			ps.setString(2, req.getParameter("description"));
		} else {
			ps.setNull(2, java.sql.Types.VARCHAR);
		}
		ps.setInt(3,Integer.parseInt(req.getParameter("author_id")));
		if (req.getParameter("locality") != null) {
			ps.setInt(4, Integer.parseInt(req.getParameter("locality")));
		} else {
			ps.setNull(4, java.sql.Types.INTEGER);
		}
		if (req.getParameter("rooms") != null) {
			ps.setInt(5, Integer.parseInt(req.getParameter("rooms")));
		} else {
			ps.setNull(5, java.sql.Types.INTEGER);
		}
		if (req.getParameter("price") != null) {
			ps.setDouble(6, Double.parseDouble(req.getParameter("price")));
		} else {
			ps.setNull(6, java.sql.Types.DOUBLE);
		}
//		JsonObject j = makeJson(req);
//		if (j == null) {
//			System.out.println("request failed");
//			return;}
//		setParameters(ps, j);
		ps.execute();
		}
		catch(SQLException e) {System.out.println("SQL exception trying to add"); e.printStackTrace();}
//		catch(ParseException e) {System.out.println("Invalid Date format");}
		dataBaseUpdated = true;
	}
	
	
	
	//Utility functions below
	
	Types getRequestType(JsonObject request){
		String query = request.get("mode").getAsString().toLowerCase();
		switch (query.charAt(0)) {
		case 'a': {
			if (query.charAt(1) == 'l') return Types.all;
			else return Types.address;
		}
		case 'u':{
			return Types.user;
		}
		case 'd':{
			return Types.date;
		}
		case 'l':{
			return Types.locality;
		}
		case 'i':{
			return Types.ID;
		}
		case 's': {
			return Types.search;
		}
		default:
			return Types.all;
		}
	}
	
	void writeSet(ResultSet result, HttpServletResponse resp) throws SQLException, IOException{
		ArrayList<Post> arr = new ArrayList<Post>();
		while (result.next()) {
			int postid = result.getInt("post_id");
			double price = result.getDouble("price");
			int rooms = result.getInt("rooms");
			String postedBy = result.getString("email");
			String address = result.getString("address");
			String description = result.getString("description");
			int author_id = result.getInt("author_id");
			String locality = result.getString("locality");
			String time = result.getString("time");//can make this to get time as a date and not string
			arr.add(new Post(author_id, postid, description, address, time, locality, postedBy, price, rooms));
		}
	   PrintWriter print = new PrintWriter(resp.getWriter());
	   print.print(gson.toJson(arr));
	   print.flush();
	}
	
	ResultSet getAllPosts() throws SQLException{
		Statement st = conn.createStatement();
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM posts LEFT JOIN localities ON posts.locality_id = localities.locality_id LEFT JOIN users ON posts.author_id = users.user_id ORDER BY time DESC;");
		return ps.executeQuery();
	}
	
	ResultSet getByUser(String userID) throws SQLException{
		Statement st = conn.createStatement();
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM posts s WHERE s.author_id = ?");
		ps.setString(1, userID);
		return ps.executeQuery();
	} 
	
	ResultSet getByDate(String date) throws SQLException, ParseException{
		Statement st = conn.createStatement();
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM posts s WHERE s.time = ?");
		ps.setDate(1, new Date(dateFormat.parse(date).getTime()));
		return ps.executeQuery();
	} 
	
	ResultSet getByLocality(int locality) throws SQLException{
		Statement st = conn.createStatement();
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM posts LEFT JOIN localities ON posts.locality_id = localities.locality_id LEFT JOIN users ON posts.author_id = users.user_id WHERE localities.locality_id = ? ORDER BY time DESC;");
		ps.setInt(1, locality);
		ResultSet result = ps.executeQuery();
		/*
		 * 
		 * 
		 * 
		 * Implement google geocoding api here
		 * 
		 * 
		 * 
		 * 
		 * 
		 * */
		return result;
	} 
	
	ResultSet getByID(int ID) throws SQLException{
		Statement st = conn.createStatement();
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM posts LEFT JOIN localities ON posts.locality_id = localities.locality_id LEFT JOIN users ON posts.author_id = users.user_id WHERE post_id = ?");
		ps.setInt(1, ID);
		ResultSet result = ps.executeQuery();
		return result;
	}

	ResultSet getSearch(String search) throws SQLException{
		Statement st = conn.createStatement();
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM posts LEFT JOIN localities ON posts.locality_id = localities.locality_id LEFT JOIN users ON posts.author_id = users.user_id WHERE display_name LIKE ? OR email LIKE ? OR locality LIKE ? OR description LIKE ? OR address LIKE ? ORDER BY time DESC;");
		for (int i = 1; i < 6; i++) ps.setString(i, "%" + search + "%");
		ResultSet result = ps.executeQuery();
		return result;
	}

	
	String readReader(BufferedReader stream) throws IOException{
		String output = new String();
		while (stream.ready()) {
			output += stream.readLine();
		}
		return output;
	}
	
	void connectToDatabase() throws SQLException{
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO: handle exception
			System.out.println("Please check the JDBC driver and its build path");
		}
		this.conn = DriverManager.getConnection("jdbc:mysql://35.193.248.160/main?user=root&password=RandallWilliams");
		if(conn == null) System.out.println("Could not connect to database");
	}
//	void setParameters(PreparedStatement ps, JsonObject post) throws SQLException, ParseException{
//		try {
//		ps.setString(1, post.get("address").getAsString());//can use google API to sanitize address
//		ps.setString(2, post.get("description").getAsString());
//		ps.setInt(3, post.get("author_id").getAsInt());
//		ps.setInt(4, post.get("locality").getAsInt());
//		}
//		catch(NullPointerException e) {
//			System.out.println("The post request was missing a parameter");
//		}
//	}
	
	JsonObject makeJson(HttpServletRequest req) throws IOException{
		//String JSONString = readReader(req.getReader());
		
		//return gson.fromJson(JSONString, JsonObject.class);
		
System.out.print(gson.fromJson(gson.toJson(req.getParameterMap()), JsonObject.class));
		return gson.fromJson(gson.toJson(req.getParameterMap()), JsonObject.class);
	}
}
