import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};

        String fileNameCSV = "data.csv";
        String fileNameXML = "data.xml";

        List<Employee> listCSV = parseCSV(columnMapping, fileNameCSV);
        String jsonCSV = listToJson(listCSV);
        writeString(jsonCSV, "data.json");

        List<Employee> listXML = parseXML(columnMapping, fileNameXML);
        String jsonXML = listToJson(listXML);
        writeString(jsonXML, "data2.json");

        String json = readString("data2.json");
        List<Employee> listJSON = jsonToList(json);

        listJSON.forEach(System.out::println);
    }

    private static List<Employee> parseCSV(String[] colMapping, String fileName) {
        List<Employee> list = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(colMapping);

            CsvToBean<Employee> scv = new CsvToBeanBuilder<Employee>(reader)
                    .withMappingStrategy(strategy)
                    .build();

            list = scv.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    private static List<Employee> parseXML(String[] colMapping, String fileName) {
        List<Employee> list = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(fileName));
            Node root = doc.getDocumentElement();
            NodeList nodeList = root.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (Node.ELEMENT_NODE == node.getNodeType()) {
                    Element element = (Element) node;
                    long id = Long.parseLong(element.getElementsByTagName(colMapping[0]).item(0).getTextContent());
                    String firstName = element.getElementsByTagName(colMapping[1]).item(0).getTextContent();
                    String lastName = element.getElementsByTagName(colMapping[2]).item(0).getTextContent();
                    String country = element.getElementsByTagName(colMapping[3]).item(0).getTextContent();
                    int age = Integer.parseInt(element.getElementsByTagName(colMapping[4]).item(0).getTextContent());

                    Employee employee = new Employee(id, firstName, lastName, country, age);
                    list.add(employee);
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        return list;
    }

    private static String listToJson(List<Employee> list) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Type listType = new TypeToken<ArrayList<Employee>>() {}.getType();
        return gson.toJson(list, listType);
    }

    private static void writeString(String text, String fileName) {
        try (FileWriter writer = new FileWriter(fileName, false)) {
            writer.write(text);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String readString(String fileName) {
        JSONParser parser = new JSONParser();
        String json = new String();
        try {
            Object obj = parser.parse(new FileReader(fileName));
            JSONArray jsonArray = (JSONArray) obj;
            json = jsonArray.toJSONString();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return json;
    }

    private static List<Employee> jsonToList(String json) {
        List<Employee> list = new ArrayList<>();
        JSONParser jsonParser = new JSONParser();
        try {
            JSONArray jsonArray = (JSONArray) jsonParser.parse(json);
            for (Object jsonObject : jsonArray) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();
                Employee employee = gson.fromJson(jsonObject.toString(), Employee.class);
                list.add(employee);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return list;
    }
}
