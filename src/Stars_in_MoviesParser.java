import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Stars_in_MoviesParser extends DefaultHandler {
    private Stars_in_Movies tempSim;
    private String tempVal = "";
    private List<Stars_in_Movies> sims;

    //to maintain context

    public Stars_in_MoviesParser() {
        sims = new ArrayList<>();
    }

    public void runExample() {
        parseDocument();
//        printData();
    }

    private void parseDocument() {
        try {
            // Specify the input file and encoding
            String xmlFilePath = "XMLparsingFiles/casts124.xml";
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

        System.out.println("No of Casts Members '" + sims.size() + "'.");

        Iterator<Stars_in_Movies> it = sims.iterator();
        while (it.hasNext()) {
            System.out.println(it.next().toString());
        }
    }

    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("f")) {
            tempSim = new Stars_in_Movies();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
            tempVal += new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("f")) {
            tempSim.setMovieId(tempVal);
        } else if (qName.equalsIgnoreCase("a")) {
            tempSim.setStarname(tempVal);
        } else if (qName.equalsIgnoreCase("m")) {
            sims.add(tempSim);
        }

    }

    public List<Stars_in_Movies> getSims() {
        return sims;
    }

    public static void main(String[] args) {
        Stars_in_MoviesParser sp = new Stars_in_MoviesParser();
        sp.runExample();
    }

}