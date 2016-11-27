#java -classpath ./;../lib/mysql-connector-java-5.1.27-bin.jar MyMovieDBManage --clear
#java -classpath ./;../lib/mysql-connector-java-5.1.27-bin.jar MyMovieDBManage --show Actors
#java -classpath ./;../lib/mysql-connector-java-5.1.27-bin.jar MyMovieDBManage --show Movies -condition "movieTitle='hello world'"
#java -classpath ./;../lib/mysql-connector-java-5.1.27-bin.jar MyMovieDBManage --insert Movies null,'hello world',,,,,null,./1.jpg 
#java -classpath ./;../lib/mysql-connector-java-5.1.27-bin.jar MyMovieDBManage --update Actors year_of_birth=1990, county='uk' -condition "actorID < 2 and actorID=1"
#java -classpath ./;../lib/mysql-connector-java-5.1.27-bin.jar MyMovieDBManage --update poster "./1.jpg" -condition 1

java -classpath ./;../lib/mysql-connector-java-5.1.27-bin.jar MyMovieDBManage --show Movies -condition "movieTitle='Mission: Impossible - Ghost Protocol'"
java -classpath ./;../lib/mysql-connector-java-5.1.27-bin.jar MyMovieDBManage --delete Movies -condition "movieID = 31"
java -classpath ./;../lib/mysql-connector-java-5.1.27-bin.jar MyMovieDBManage --insert Movies 31,'Mission: Impossible - Ghost Protocol','PG-13','Action Thriller',2011,7.3,22,./1.jpg

java -classpath ./;../lib/mysql-connector-java-5.1.27-bin.jar MyMovieDBManage --insert Starin 12,31
java -classpath ./;../lib/mysql-connector-java-5.1.27-bin.jar MyMovieDBManage --insert Starin 11,31
java -classpath ./;../lib/mysql-connector-java-5.1.27-bin.jar MyMovieDBManage --insert Starin 26,31
java -classpath ./;../lib/mysql-connector-java-5.1.27-bin.jar MyMovieDBManage --show Starin -condition "movieID=31"

java -classpath ./;../lib/mysql-connector-java-5.1.27-bin.jar MyMovieDBManage --show Movies -condition "movieID=31"
java -classpath ./;../lib/mysql-connector-java-5.1.27-bin.jar MyMovieDB movie "Mission: Impossible - Ghost Protocol"

java -classpath ./;../lib/mysql-connector-java-5.1.27-bin.jar MyMovieDBManage --update poster "./31.jpg" -condition 31
java -classpath ./;../lib/mysql-connector-java-5.1.27-bin.jar MyMovieDB movie "Mission: Impossible - Ghost Protocol"
pause