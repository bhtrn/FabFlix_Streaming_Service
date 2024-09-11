import org.xml.sax.InputSource;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class MovieParser extends DefaultHandler {
    private Movie tempMovie;
    private String tempVal = "";
    private List<Movie> parsedFilms;
    private String director;

    //to maintain context

    public MovieParser() {
        parsedFilms = new ArrayList<>();
    }

    public void runExample() {
        parseDocument();
//        printData();
    }

    private void parseDocument() {
        try {
            // Specify the input file and encoding
            String xmlFilePath = "XMLparsingFiles/mains243.xml";
            String encoding = "ISO-8859-1";

            // Create input stream and reader with specified encoding
            InputStream inputStream = new FileInputStream(xmlFilePath);
            InputStreamReader reader = new InputStreamReader(inputStream, encoding);

            // Create input source with reader
            InputSource inputSource = new InputSource(reader);

            // Get a factory
            SAXParserFactory spf = SAXParserFactory.newInstance();

            // Get a new instance of parser
            javax.xml.parsers.SAXParser sp = spf.newSAXParser();

            // Parse the file with the specified input source
            sp.parse(inputSource, this);

            // Close the input stream and reader
            reader.close();
            inputStream.close();
        } catch (SAXException | ParserConfigurationException | IOException se) {
            se.printStackTrace();
        }
    }

    /**
     * Iterate through the list and print
     * the contents
     */
    private void printData() {

        System.out.println("No of Movies '" + parsedFilms.size() + "'.");

//        Iterator<Movie> it = parsedFilms.iterator();
//        while (it.hasNext()) {
//            System.out.println(it.next().toString());
//        }
    }

    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("film")) {
            tempMovie = new Movie();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
            tempVal += new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("t")) {
            tempMovie.setTitle(tempVal);
        } else if (qName.equalsIgnoreCase("fid")) {
            tempMovie.setId(tempVal);
        } else if (qName.equalsIgnoreCase("year")) {
            try{
                int year = Integer.parseInt(tempVal);
            }
            catch (Exception e){
                System.out.println("Inconsistency Occurred: Year value is: "+ tempVal);
                tempVal = "0";
            }
            finally {
                tempMovie.setYear(tempVal);
            }
            tempMovie.setYear(tempVal);
        } else if (qName.equalsIgnoreCase("dirname")) {
            director = tempVal;
        } else if (qName.equalsIgnoreCase("cat")) {
            tempMovie.setCategories(tempVal);
        } else if (qName.equalsIgnoreCase("film")) {
            tempMovie.setDirector(director);
            if (!tempMovie.getId().isEmpty()){
                parsedFilms.add(tempMovie);
            }
        }

    }

    public List<Movie> getParsedFilms() {
        return parsedFilms;
    }

    public static void main(String[] args) {
        MovieParser sp = new MovieParser();
        sp.runExample();
    }

}