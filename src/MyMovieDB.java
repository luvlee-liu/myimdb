// Run using:
//            java -classpath .:/usr/local/mysql-connector-java/mysql-connector-java-5.1.18-bin.jar MyMovieDB

import java.io.*;
import java.sql.*;

public class MyMovieDB {
	
	private final String TableMovies = 
			"create table Movies(" +
			"movieID int NOT NULL AUTO_INCREMENT," +
			"movieTitle varchar(50) NOT NULL, INDEX USING HASH(movieTitle)," +
			"rate varchar(5)," +
			"genre varchar(50)," +
			"year int, INDEX USING BTREE(year)," +
			"rating numeric(2,1)," +
			"directorID int," +
			"Poster BLOB," +
			"Primary key (movieID)," +
			"Foreign key (directorID) references Directors(directorID) on delete set NULL) ENGINE=InnoDB";
	
	private final String TableActors = 
			 "create table Actors " + 
			 "(actorID int NOT NULL AUTO_INCREMENT, " + 
			 " actorName varchar(20) NOT NULL, INDEX USING HASH(actorName), " + 
			 " year_of_birth int, " + 
			 " county varchar(15), " + 
			 " awards varchar(50), " + 
			 " primary key(actorID)) ENGINE=InnoDB";
			
	private final String TableDirectors = 
			 "create table Directors " + 
			 "(directorID int NOT NULL AUTO_INCREMENT, " + 
			 " directorName varchar(20) NOT NULL, INDEX USING HASH(directorName), " + 
			 " year_of_birth int, " + 
			 " county varchar(15), " + 
			 " awards varchar(50), " + 
			 " primary key(directorID)) ENGINE=InnoDB";
			
	private final String TableStarin = 
			 "create table Starin " + 
			 "(actorID int NOT NULL, " + 
			 " movieID int NOT NULL, " + 
			 " primary key (actorID, movieID), " + 
			 " foreign key (actorID) references Actors(actorID) ON DELETE CASCADE, " + 
			 " foreign key (movieID) references Movies(movieID) ON DELETE CASCADE ) ENGINE=InnoDB";
	
	Connection cn;

	// currentResults holds current results from a search() so that other
	// methods can access them
	ResultSet currentResults;

	// currentItem holds the row number of an itme the user is looking at (so we
	// can use currentResults.absolute(currentItem) to go to it
	Integer currentItem;
	private void initialDB(String dbname, String userid, String password){
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			cn = DriverManager.getConnection(
							//"jdbc:mysql://cisc437.acad.cis.udel.edu:3306/"
									//+ dbname, userid, password);
					// "jdbc:mysql://localhost:3306/" + dbname, userid, password);
					"jdbc:mysql://128.4.27.3:3306/" + dbname,"root","xli2012");
		} catch (Exception e) {
			System.out.println("connection failed: " + e);
		}
		
		SendSQL("use " + dbname,false);
	}
	
	private ResultSet SendSQL(String sql) throws SQLException{
		System.out.println("#"+sql);
		Statement st = cn.createStatement();
		
		return st.executeQuery(sql);
	}
	
	private void SendSQL(String sql, boolean ShowRS){
		try {
			System.out.println("#" + sql + ":");
			Statement st = cn.createStatement();
			if (ShowRS){
				ResultSet rs = st.executeQuery(sql);
				ShowResultSet(rs);
				rs.close();
			}
			else
				st.executeUpdate(sql);
			st.close();
		} catch (SQLException e) {
			System.out.println("SQL" + sql + "failed: " + e);
		}
		
	}
	
	private void displayHelp(){
		System.out.println("MyMovieDB by Chao Yang & Chaoyu Chen");
		System.out.println("Uasage:");
		System.out.println("MyMovieDB movie [Movie title] : query movie info by title");
		System.out.println("MyMovieDB person [Person Name] : query person info by Name");
		System.out.println("MyMovieDB year [year Begin] [year End] : query movie tilte from \"year Begin\" to \"year End\"");
		System.out.println("MyMovieDB year [year] : query movie tilte of year");
	}
	//main fuction
	public MyMovieDB(String dbname, String userid, String password, String[] args) {
		cn = null;
		currentResults = null;
		currentItem = null;

		if (args.length < 2){
			displayHelp();
			return;
		}
		try {
			
			initialDB(dbname,userid,password);
			System.out.println();
			if (args[0].equalsIgnoreCase("movie")) {
				String movieTitle = args[1];
				for (int i=2; i<args.length; i++){
					movieTitle = movieTitle + " " + args[i];
				}
				
				QueryMoviesByName(movieTitle);
			}
			else if (args[0].equalsIgnoreCase("person")){
				String personName = args[1];
				for (int i=2; i<args.length; i++){
					personName = personName + " " + args[i];
				}
				
				QueryPersonByName(personName);
			}
			else if (args[0].equalsIgnoreCase("year")){
				int yearBegin=9999,yearEnd=9999;
				if (args.length==2){
					yearBegin = Integer.parseInt(args[1]);
					yearEnd = yearBegin;
				}
				else if (args.length==3){
					yearBegin = Integer.parseInt(args[1]);
					yearEnd = Integer.parseInt(args[2]);;
				}
				else
					displayHelp();
				QueryMoviesByYears(yearBegin,yearEnd);
			}
			else
				displayHelp();
			
			System.out.println();
			//TODO for debug
			//InsertTable();
			
			//for (int mid = 1; mid < 32 ; mid++){
				//setPoster("./" + String.valueOf(mid) + ".jpg" , String.valueOf(mid));
			//}
			
			cn.close();
		} catch (SQLException e) {
			System.out.println("Some other error: " + e);
		} catch (ArrayIndexOutOfBoundsException e){
			displayHelp();
		}
	}

	private boolean getPoster(String MovieID) {

		try {
			
			String sql = "SELECT Poster FROM Movies WHERE MovieID=" + MovieID;

			PreparedStatement ps = cn.prepareStatement(sql);
			
			ResultSet rs = ps.executeQuery(sql);
			if(rs.next()){
				InputStream contentStream = rs.getBinaryStream("Poster");
				if (contentStream == null){
					return false;
				}
			    String newFilename = "./Poster_" + MovieID + ".jpg";
			    // storing the input stream in the file
			
			    OutputStream out=new FileOutputStream(newFilename);
			    byte buf[]=new byte[1024];
			    
			    int len;
			    while((len=contentStream.read(buf))>0){
			    	out.write(buf,0,len);
			    }
			    out.close();
			    return true;

			}
		} catch (FileNotFoundException e) {
			System.out.println("file not found");
		} catch (SQLException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println("read file fails");
		}
		return false;
	}
	
	private void setPoster(String filename,String MovieID) {
		InputStream filecontent;
		String sql = "UPDATE Movies SET Poster=? where MovieID=" + MovieID;
		try {
			filecontent = new FileInputStream(filename);
			int size = filecontent.available();
			PreparedStatement ps = cn.prepareStatement(sql);
			ps.setBinaryStream(1, filecontent, size);
			ps.executeUpdate();
			filecontent.close();
		} catch (FileNotFoundException e) {
			System.out.println("file not found");
		} catch (SQLException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println("read file fails");
		}
	}

	private void CreateTalbes(){
		
		try{
			Statement st = cn.createStatement();
			
			//drop all tables
			st.executeUpdate("drop table if exists Starin");
			
			st.executeUpdate("drop table if exists Actors");
			st.executeUpdate("drop table if exists Movies");
			st.executeUpdate("drop table if exists Directors");
			
			//create new tables
			st.execute(TableDirectors);
			st.execute(TableActors);
			st.execute(TableMovies);
			st.execute(TableStarin);
			
			st.close();
		} catch (SQLException e) {
			System.out.println("create failed: " + e);
		}
		
		
	}
	
	
	private void ShowResultSet(ResultSet rs){
		try{
			rs.beforeFirst();
			ResultSetMetaData rsmd = rs.getMetaData() ;   
			int columnCount = rsmd.getColumnCount();
			
			//show column labels
			for (int i = 1; i <= columnCount; i++){
				System.out.print(rsmd.getColumnLabel(i) + " ");
			}
			System.out.println();
			
			//show table rows
			while (rs.next()){
				for(int i = 1; i <= columnCount; i++){
					System.out.print( rs.getString(i) + " " );
				}
				System.out.println();
			}
		} catch (SQLException e){
			System.out.println("ShowResultSet: " + e);
		}	
	}
	
	private String getCol(ResultSet rs, String colname) throws SQLException{
		try{
			String value = rs.getString(colname);
			value.isEmpty();
			return value;
		}
		catch(NullPointerException e){
	
			return " ";
		}	
	}
	private String getCol(ResultSet rs, int colIndex) throws SQLException{
		try{
			String value = rs.getString(colIndex);
			value.isEmpty();
			return value;
		}
		catch(NullPointerException e){
			return " ";
		}	
	}
	
	private void displayPerson(ResultSet rs) throws SQLException{
		StringBuilder personInfo = new StringBuilder();
		rs.beforeFirst();
		while(rs.next()){
			personInfo.append("ID: " + getCol(rs,1));
			personInfo.append("\nName: " + getCol(rs,2));
			personInfo.append("\nBorn: " + getCol(rs,3) + " in " + getCol(rs,4));
			personInfo.append("\nAwards: " + getCol(rs,5));
		}
		System.out.println(personInfo);
	}
	
	
	private void displayMovies(ResultSet rs){
		try{
			if (!rs.first()){
				System.out.println("Empty result!");
				return;
			}
			
			StringBuilder movieInfo = new StringBuilder();
			int cur_ID = 0;
			//show table rows
			do{
				int movieID = Integer.parseInt(getCol(rs, "movieID"));
				if (movieID != cur_ID ){
					cur_ID = movieID;
					
					if (!rs.isFirst()){
						System.out.println(movieInfo);
					}
										
					movieInfo = new StringBuilder();
					movieInfo.append("\nID: " + movieID);
					movieInfo.append("\nTitle: " + getCol(rs, "movieTitle"));
					movieInfo.append("\nyear: " + getCol(rs, "year"));
					movieInfo.append("\ngenre: " + getCol(rs, "genre"));
					movieInfo.append("\nrating: " + getCol(rs, "rating"));
					movieInfo.append("\ndirectorName: " + getCol(rs, "directorName"));
					if (getPoster(String.valueOf(movieID)))
						movieInfo.append("\nPoster: ./Poster_" + movieID + ".jpg");
					movieInfo.append("\nactorName: " + getCol(rs, "actorName"));
						
				}
				else{ 
					movieInfo.append("; " + getCol(rs, "actorName"));

				}
			}while (rs.next());
			System.out.println(movieInfo);
			
		} catch (SQLException e){
			System.out.println("displayMovies: " + e);
		}	
	}
	

	private void QueryPersonByName(String Name){
		try {
			System.out.println("*Query by Name: " + Name);
			Statement st = cn.createStatement();
			ResultSet rs = st.executeQuery(
					"(select * from Actors"+
					" where actorName='" + Name + "')" +
					" union (select * from Directors" +
					" where directorName='" + Name + "')" 
					);
			
			//TODO debug display
			//ShowResultSet(rs);
			displayPerson(rs);
			rs.close();
			st.close();
			
		}catch (SQLException e) {
			System.out.println("Query failed: " + e);
		}		
	}
	
	private void QueryMoviesByYears(int year_begin, int year_end){
		try {
			System.out.println("*Query by Years( " + year_begin + "-" + year_end + ")");
			Statement st = cn.createStatement();
			ResultSet rs = st.executeQuery(
					"select year,movieTitle from Movies"+
					" where year>=" + year_begin + " and year<=" + year_end +
					" ORDER by year,movieID ASC"
					);
			
			//TODO debug display
			ShowResultSet(rs);
			
			rs.close();
			st.close();
			
		}catch (SQLException e) {
			System.out.println("Query failed: " + e);
		}
	}
	
	private void QueryMoviesByName(String MovieTitle){
		try {
			System.out.println("*Query by MovieTitle: " + MovieTitle);
			Statement st = cn.createStatement();
			ResultSet rs = st.executeQuery(
					//"select M.movieTitle, A.actorName , D.directorName from "+
					"select * from "+
					"( select * from Movies where movieTitle=\"" + MovieTitle + "\") M" +
					" natural LEFT OUTER join Starin" +
					" natural LEFT OUTER join (select actorID,actorName from Actors) A" +
					" natural LEFT OUTER join (select directorID, directorName from Directors) D" + 
					" ORDER by M.movieID ASC"
					);
			
			//TODO debug display
			//ShowResultSet(rs);
			displayMovies(rs);
				
			rs.close();
			st.close();
			
		}catch (SQLException e) {
			System.out.println("Query failed: " + e);
		}
		
	}
	
	public static void main(String[] args) {
		String dbname = "myimdb";
		String userid = "myimdb";
		String password = "myimdb";

		MyMovieDB app = new MyMovieDB(dbname, userid, password, args);

	}
}