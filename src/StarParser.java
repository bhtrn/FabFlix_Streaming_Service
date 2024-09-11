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

public class StarParser extends DefaultHandler {
    private Star tempStar;
    private String tempVal = "";
    private List<Star> stars;

    //to maintain context

    public StarParser() {
        stars = new ArrayList<>();
    }

    public void runExample() {
        parseDocument();
//        printData();
    }

    private void parseDocument() {
        try {
            // Specify the input file and encoding
            String xmlFilePath = "XMLparsingFiles/actors63.xml";
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

        System.out.println("No of Stars '" + stars.size() + "'.");

//        Iterator<Star> it = stars.iterator();
//        while (it.hasNext()) {
//            System.out.println(it.next().toString());
//        }
    }

    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("actor")) {
            tempStar = new Star();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
            tempVal += new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("stagename")) {
            tempStar.setName(tempVal);
        } else if (qName.equalsIgnoreCase("dob")) {
            try{
                if (tempVal.length() > 0) {
                    int year = Integer.parseInt(tempVal);
                }
                else{
                    tempVal = null;
                }
            }
            catch (Exception e){
                System.out.println("Inconsistency Occurred: Year value is: "+ tempVal);
                tempVal = null;
            }
            finally {
                tempStar.setBirthYear(tempVal);
            }
        } else if (qName.equalsIgnoreCase("actor")) {
            stars.add(tempStar);
        }

    }

    public List<Star> getStars() {
        return stars;
    }

    public static void main(String[] args) {
        StarParser sp = new StarParser();
        sp.runExample();
    }

}