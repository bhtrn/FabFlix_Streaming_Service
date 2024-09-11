# FabFlix_Streaming_Service
Web-Based application replicating modern streaming service

- # Connection Pooling
Included filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
      - _dashboardServlet
      -  _tableMetaDataServlet
      - AddMovieServlet
      -  AddStarServlet
      - LoginServlet
      -  MovieGenreServlet
      -  MovieListServlet
      -  MovieSuggestion
      -  PaymentServlet
      -  ShoppingCartServlet
      - SingleMovieServlet
      -  SingleStarServlet
      -  StarsServlet
    
Connection pooling is utilized by establishing our datasource immediately within all servlets. A connection
is built and closed off of this datasource. When a connection is built, it "grabs" a connection from the preset
connection pool from the data source. Once we're done grabbing data using the connection, the connecton is "closed"
and returned to the pool.
  
Single Star Servlet:
        - Inside my single-star servlet, information regarding our 
        database is initialized. This information is pulled from our 
        context.xml file. Given the XML file's information (such as 
        database credentials), a reference to our existing SQL database 
        data source is made. From this reference, we request existing 
        connections from the database by calling getConnection() on our 
        local datasource reference. By inputting this into a try 
        statement, the connection is automatically closed and returned to
        the connection pool once we exit the try statement's context.
        
Movie Suggestion Servlet:
        - Similar to the single-star servlet, a local reference to our
        database is initialized. Information is pulled from the context.xml
        file and a connection to our sql database is borrowed via a try-statement.
        Once done, the connection is returned (closed by the try statement) to the
        connection pool.


Master/Slave
Included filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.
      - _dashboardServlet
      -  _tableMetaDataServlet
      - AddMovieServlet
      -  AddStarServlet
      - LoginServlet
      -  MovieGenreServlet
      -  MovieListServlet
      -  MovieSuggestion
      -  PaymentServlet
      -  ShoppingCartServlet
      - SingleMovieServlet
      -  SingleStarServlet

Essentially, all write requests were sent to the master so that any changes made to the database is reflected in both 
    the master and slave. All read request were sent to the slave given no changes to the database needed to be made. 
    
