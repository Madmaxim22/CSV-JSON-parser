package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
        String fileCsv = "data.csv";
        String fileXml = "data.xml";
        String fileJson1 = "data.json";
        String fileJson2 = "data2.json";
        List<Employee> list = parseCSV(fileCsv);
        String json = listToJson(list);
        writeString(json, fileJson1);
        List<Employee> list1 = parseXML(fileXml);
        String json1 = listToJson(list1);
        writeString(json1, fileJson2);
        String json2 = readStrings(fileJson2);
        List<Employee> list2 = jsonToList(json2);
        for (Employee emp : list2) {
            System.out.println(emp);
        }


    }

    private static List<Employee> jsonToList(String json2) {
        List<Employee> employees = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            JSONArray array = (JSONArray) parser.parse(json2);
            Gson gson = new Gson();
            for (Object o : array) {
                JSONObject object = (JSONObject) o;
                Employee employee = gson.fromJson(String.valueOf(object), Employee.class);
                employees.add(employee);
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return employees;
    }

    private static String readStrings(String s) {
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(s))) {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return stringBuilder.toString();
    }

    private static List<Employee> parseXML(String fileXml) {
        List<Employee> employees = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(fileXml));
            Node root = doc.getDocumentElement();
            NodeList nodeList = root.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (Node.ELEMENT_NODE == node.getNodeType()) {
                    Element element = (Element) node;
                    Employee employee = new Employee(
                            Integer.parseInt(element.getElementsByTagName("id").item(0).getTextContent()),
                            element.getElementsByTagName("firstName").item(0).getTextContent(),
                            element.getElementsByTagName("lastName").item(0).getTextContent(),
                            element.getElementsByTagName("country").item(0).getTextContent(),
                            Integer.parseInt(element.getElementsByTagName("age").item(0).getTextContent())
                    );
                    employees.add(employee);
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            ex.printStackTrace(System.out);
        }
        return employees;
    }

    private static void writeString(String json, String fileName) {
        try (FileWriter writer = new FileWriter(fileName, false)) {
            writer.write(json);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static <T> String listToJson(List<T> list) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting().create();
        Type listType = new TypeToken<List<T>>() {
        }.getType();
        return gson.toJson(list, listType);
    }

    private static List<Employee> parseCSV(String fileName) {
        List<Employee> employees = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy =
                    new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping("id", "firstName", "lastName", "country", "age");

            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(reader)
                    .withMappingStrategy(strategy)
                    .build();
            employees = csv.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return employees;
    }
}