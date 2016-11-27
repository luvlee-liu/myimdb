// Run using:
//            java -classpath .:/usr/local/mysql-connector-java/mysql-connector-java-5.1.18-bin.jar MyMovieDB

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class MyMovieDBManage {

	private final String TableMovies = "create table Movies("
			+ "movieID int NOT NULL AUTO_INCREMENT,"
			+ "movieTitle varchar(50) NOT NULL, INDEX USING HASH(movieTitle),"
			+ "rate varchar(5),"
			+ "genre varchar(50),"
			+ "year int, INDEX USING BTREE(year),"
			+ "rating numeric(2,1),"
			+ "directorID int,"
			+ "Poster BLOB,"
			+ "Primary key (movieID),"
			+ "Foreign key (directorID) references Directors(directorID) ON DELETE SET NULL ON UPDATE CASCADE) ENGINE=InnoDB";

	private final String TableActors = "create table Actors "
			+ "(actorID int NOT NULL AUTO_INCREMENT, "
			+ " actorName varchar(20) NOT NULL, INDEX USING HASH(actorName), "
			+ " year_of_birth int, " + " county varchar(50), "
			+ " awards varchar(50), " + " primary key(actorID)) ENGINE=InnoDB";

	private final String TableDirectors = "create table Directors "
			+ "(directorID int NOT NULL AUTO_INCREMENT, "
			+ " directorName varchar(20) NOT NULL, INDEX USING HASH(directorName), "
			+ " year_of_birth int, " + " county varchar(50), "
			+ " awards varchar(50), "
			+ " primary key(directorID)) ENGINE=InnoDB";

	private final String TableStarin = "create table Starin "
			+ "(actorID int NOT NULL, "
			+ " movieID int NOT NULL, "
			+ " primary key (actorID, movieID), "
			+ " foreign key (actorID) references Actors(actorID) ON DELETE CASCADE ON UPDATE CASCADE, "
			+ " foreign key (movieID) references Movies(movieID) ON DELETE CASCADE ON UPDATE CASCADE ) ENGINE=InnoDB";

	Connection cn;

	// currentResults holds current results from a search() so that other
	// methods can access them
	ResultSet currentResults;

	// currentItem holds the row number of an itme the user is looking at (so we
	// can use currentResults.absolute(currentItem) to go to it
	Integer currentItem;

	private void initialDB(String dbname, String userid, String password) {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			cn = DriverManager
					.getConnection(
							// "jdbc:mysql://cisc437.acad.cis.udel.edu:3306/"
							// + dbname, userid, password);
							// "jdbc:mysql://localhost:3306/" + dbname, userid, password);
							"jdbc:mysql://128.4.27.3:3306/" + dbname, "root",
							"xli2012");

			SendSQLUpdate("use " + dbname);
		} catch (Exception e) {
			System.out.println("connection failed: " + e);
		}

	}

	private ResultSet SendSQL(String sql) throws SQLException {
		System.out.println("#" + sql);
		Statement st = cn.createStatement();
		return st.executeQuery(sql);
	}

	private void SendSQLUpdate(String sql) {
		try {
			System.out.println("#" + sql);
			Statement st = cn.createStatement();

			System.out.printf("%d records affected\n", st.executeUpdate(sql));
		} catch (SQLException e) {
			System.out.println("SQL" + sql + "failed: " + e);
		}
	}

	private void SendSQL(String sql, boolean ShowRS) {
		try {
			System.out.println("#" + sql + ":");
			Statement st = cn.createStatement();
			if (ShowRS) {
				ResultSet rs = st.executeQuery(sql);
				ShowResultSet(rs);
				rs.close();
			} else
				st.executeUpdate(sql);
			st.close();
		} catch (SQLException e) {
			System.out.println("SQL" + sql + "failed: " + e);
		}

	}

	private void setPoster(String filename, String MovieID) {
		InputStream filecontent;
		
		String sql = "UPDATE Movies SET Poster=? where MovieID=" + MovieID;
		System.out.println(sql);
		try {
			filecontent = new FileInputStream(filename.replaceAll("^[ ]+", "").replaceAll("[ ]+$", ""));
			int size = filecontent.available();
			PreparedStatement ps = cn.prepareStatement(sql);
			ps.setBinaryStream(1, filecontent, size);
			System.out.println(ps.executeUpdate() + " records affected");
			filecontent.close();
		} catch (FileNotFoundException e) {
			System.out.println(filename + ": file not found");
		} catch (SQLException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(filename + ":read file fails");
		}
	}

	private void Insert() throws SQLException {
		StringBuilder sql = new StringBuilder();
		String terms[] = getOpt("sql").split(",");
		
		if (getOpt("table").equalsIgnoreCase("Movies")){
			
			if (terms.length>8){
				showHelp();
				return;
			}
			
			for (int i=0;i<7;i++){
				if (terms[i].equalsIgnoreCase("") || terms[i].equalsIgnoreCase(" "))
					sql.append("null,");
				else
					sql.append(terms[i]+",");
			}
			sql.append("null");
			String movieID = "";
			
			SendSQLUpdate("INSERT INTO " + getOpt("table") + " VALUES("
						+ sql + ")");
			if (terms[0].equals(null)){
				ResultSet rs =  SendSQL("SELECT LAST_INSERT_ID()");
				rs.first();
				movieID = rs.getString(1);
			}
			else
				movieID = terms[0];
			
			if (terms.length>7)
				if (!terms[7].equalsIgnoreCase("")
					&& !terms[7].equalsIgnoreCase(" ")
					&& !terms[7].equalsIgnoreCase("null"))
				setPoster(terms[7],movieID);
				
		}
		else{
			for (int i=0;i<terms.length-1;i++){
				if (terms[i].equalsIgnoreCase("") || terms[i].equalsIgnoreCase(" "))
					sql.append("null,");
				else
					sql.append(terms[i]+",");
			}
			if (terms[terms.length-1].equalsIgnoreCase("") || terms[terms.length-1].equalsIgnoreCase(" "))
				sql.append("null");
			else
				sql.append(terms[terms.length-1]);
			
			SendSQLUpdate("INSERT INTO " + getOpt("table") + " VALUES("
					+ sql + ")");
		}
	}

	private void Delete(String tabName, String condition) throws SQLException {
		SendSQLUpdate("Delete from " + tabName + " WHERE " + condition);
	}

	private void Update(String tabName, String sql, String condition) {
		if (tabName.equalsIgnoreCase("poster")){
			setPoster(getOpt("sql"),getOpt("condition"));
		}
		else
			SendSQLUpdate("UPDATE " + tabName + " SET " + sql + " WHERE " + condition);
	}

	private void ShowTable(String tabName) {
		SendSQL("SELECT * from " + tabName, true);
	}

	private void ShowTable(String tabName, String sql, String condition) {
		if (sql == null)
			SendSQL("SELECT * from " + tabName, true);
		else
			SendSQL("SELECT * from " + tabName + " WHERE " + condition, true);
	}

	private void executeCMD() {
		try {
			enumCMD cmd = enumCMD.valueOf(getOpt("cmd"));
			switch (cmd) {
			case insert:
				Insert();
				break;
			case show:
				ShowTable(getOpt("table"), getOpt("sql"), getOpt("condition"));
				break;
			case clear:
				CreateTalbes();
				break;
			case delete:
				Delete(getOpt("table"), getOpt("condition"));
				break;
			case update:
				Update(getOpt("table"), getOpt("sql"), getOpt("condition"));
				break;
			default:
				showHelp();
			}
		} catch (SQLException e) {
			System.out.println("excute command error: " + e);
		}
	}

	// main fuction
	public MyMovieDBManage(String dbname, String userid, String password,
			String[] args) {
		cn = null;
		currentResults = null;
		currentItem = null;

		// args parser
		{
			initialOpts();
			if (!parseArgs(args)) {
				return;
			}
		}

		try {
			initialDB(dbname, userid, password);
			System.out.println();
			// SendSQL("use " + dbname,false);
			// ShowResultSet(SendSQL("show tables "));

			// drop all former table

			executeCMD();
			// SendSQL("show tables ",true);

			// TODO for debug
			// InsertTable();
			// Insert(tabName,record);
			// Delete(tabName,record);
			// Alter(tabName,record);

			// TODO input parameter
			// if (Integer.parseInt(args[0]) == 2) QueryMovies("M2");
			System.out.println();
			cn.close();
		} catch (SQLException e) {
			System.out.println("Some other error: " + e);
		}
	}

	// debug
	/*private void InsertTable() {
		Insert("Directors", "directorName", "'D3'");
		ShowTable("Directors");
		SendSQL("INSERT INTO Directors(directorName) VALUES('D1')", false);
		SendSQL("INSERT INTO Directors(directorName) VALUES('D2')", false);
		SendSQL("INSERT INTO Movies(MovieTitle,directorID,year) VALUES('M1',NULL,'2012')",
				false);
		SendSQL("INSERT INTO Movies(MovieTitle,directorID,year) VALUES('M2','2','2009')",
				false);
		SendSQL("INSERT INTO Movies(MovieTitle,directorID,year) VALUES('M1',NULL,'2009')",
				false);
		SendSQL("INSERT INTO Actors(actorName) VALUES('A1')", false);
		SendSQL("INSERT INTO Actors(actorName) VALUES('A2')", false);
		SendSQL("INSERT INTO Actors(actorName) VALUES('D1')", false);
		SendSQL("INSERT INTO Starin VALUES(1,1)", false);
		SendSQL("INSERT INTO Starin VALUES(2,1)", false);
		SendSQL("INSERT INTO Starin VALUES(1,2)", false);

	}*/

	private void CreateTalbes() {

		try {
			Statement st = cn.createStatement();

			// drop all tables
			st.executeUpdate("drop table if exists Starin");

			st.executeUpdate("drop table if exists Actors");
			st.executeUpdate("drop table if exists Movies");
			st.executeUpdate("drop table if exists Directors");

			// create new tables
			st.execute(TableDirectors);
			st.execute(TableActors);
			st.execute(TableMovies);
			st.execute(TableStarin);

			st.close();
		} catch (SQLException e) {
			System.out.println("create failed: " + e);
		}

	}

	private void ShowResultSet(ResultSet rs) {
		try {

			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			if (columnCount>7)
				columnCount = 7;
			// show column labels
			for (int i = 1; i <= columnCount; i++) {
				System.out.print(rsmd.getColumnLabel(i) + " ");
			}
			System.out.println();

			// show table rows
			while (rs.next()) {
				for (int i = 1; i <= columnCount; i++) {
					System.out.print(rs.getString(i) + " ");
				}
				System.out.println();
			}
		} catch (SQLException e) {
			System.out.println("ShowResultSet: " + e);
		}
	}

	public static void main(String[] args) {
		String dbname = "yangc";
		String userid = "yangc";
		String password = "6110";

		MyMovieDBManage app = new MyMovieDBManage(dbname, userid, password,
				args);

	}

	// opts parser
	private Map<String, String> opts = new HashMap<String, String>();
	private Map<String, String> myargs = new HashMap<String, String>();

	private enum enumCMD {
		insert, delete, show, update, clear;
	}

	public boolean parseArgs(String[] args) {

		try {

			if (args[0].substring(0, 2).equalsIgnoreCase("--")
					&& hasOpt(args[0])) {
				setOpt("cmd", args[0].substring(2));
				if (args.length > 1)
					setOpt("table", args[1]);
				String sql = "";
				
				if (args.length > 2) {
					int i = 2;
					
					while(i < args.length){
						
						if (args[i].equalsIgnoreCase("-condition")) {
							setOpt("condition", args[i+1]);
							i=i+1;
						}
						/*if (args[i].equalsIgnoreCase("movieID")
								|| args[i].equalsIgnoreCase("actorID")
								|| args[i].equalsIgnoreCase("directorID")) {
							setOpt("condition",args[i] + "=" +args[i+1]);	
							i = i + 1;
						}*/
						else{
							sql = sql + args[i] + " ";
						}
						i = i + 1;
					}
					setOpt("sql", sql);

				}
				/*
				 System.out.println(getOpt("cmd"));
				 System.out.println(getOpt("table"));
				 System.out.println(getOpt("sql"));
				 String terms[] = sql.split(",");
				 for(int i=0; i< terms.length;i++){
					 if (terms[i].equalsIgnoreCase("") || terms[i].equalsIgnoreCase(" "))
						 System.out.println("NULL");
					 else
						 System.out.println(terms[i]);
				 }
				 System.out.println(getOpt("condition"));
				 */
				return true;
			} else {
				showHelp();
				return false;

			}
		} catch (ArrayIndexOutOfBoundsException e) {
			showHelp();
			return false;
		}

	}

	// set and get opts
	public String getOpt(String opt) {
		return myargs.get(opt);
	}

	public void setOpt(String optName, String arg) {
		myargs.put(optName, arg);
	}

	// //define opts
	public void defineOpt(String optName, String description) {
		opts.put(optName, description);
	}

	public boolean hasOpt(String opt) {
		if (opts.get(opt) == null)
			return false;
		else
			return true;
	}

	public void showHelp() {
		System.out
				.println("Database Project by Chao Yang & Chaoyu Chen\nTableName={Movies | Actors | Directors | Starin}");
		System.out.println("Movies(movieID,movieTitle,rate,genre,year,rating,directorID,Poster)");
		System.out.println("Actors(actorID,actorName,year_of_birth,county,awards)");
		System.out.println("Directors(directorID,directorName,year_of_birth,county,awards)");
		System.out.println("Starin(actorID,movieID)");
		for (String key : opts.keySet()) {
			System.out.println(opts.get(key));
		}
	}

	public void initialOpts() {
		opts.clear();
		defineOpt("--help", "--help show help information\n");
		defineOpt("--clear", "--clear delete all data! & re-create empty tables\n");
		defineOpt("--delete", "--delete TableName -condition \"conditions\"\n");
		
		defineOpt(
				"--insert",
				"--insert TableName value1,value2... \n");
		defineOpt("--update",
				"--update TableName Attr1=new_value1,Attr2=new_value2,... -condition \"conditions\"\n" +
				"--update poster \"FilePath\" -condition movieID\n");
		
		defineOpt("--show", "--show TableName  -condition \"conditions\"\n");

	}
}