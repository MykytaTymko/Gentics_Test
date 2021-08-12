import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Class that do all tasks.
 */
public class Analyzer {

    /**
     * Name of our DB that contain domains.
     */
    private final String dbDomainName = "domains";

    /**
     * Name of our DB that contain links.
     */
    private final String dbLinksName = "links";

    /**
     * Default constructor.
     */
    public Analyzer() {
    }

    /**
     * Constructor that add list to DB.
     */
    public Analyzer(final List<URL> testListUrl) {
        addLinkToList(testListUrl);
    }

    /**
     * The method adds a link to the BD.
     *
     * @param listUrl - list of links that the user sends (in our case our test list).
     */
    protected void addLinkToList(final List<URL> listUrl) {
        for (URL url : listUrl) {
            workWithBd(url);
        }
    }

    /**
     * Method retrieves the domain name of the external reference.
     *
     * @param url - link.
     *
     * @return the domain name of the external reference.
     *
     * @throws URISyntaxException
     */
    public static String getDomainName(final URL url) throws URISyntaxException {
        URI uri = new URI(url.toString());
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    /**
     * The method adds objects to the database.
     *
     * @param url - link.
     */
    public void workWithBd(final URL url) {
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();

            Integer domainFromListId = containDbThisData(statement,  getDomainName(url), dbDomainName);
            Integer linkFromListId = containDbThisData(statement,  url.toString(), dbLinksName);

            if (domainFromListId == null) {
                int maxIdDomain = takeMaxIdValueFromTable(statement, dbDomainName);
                statement.executeUpdate(String.format("INSERT into %s(id, text) values(%d, '%s')",
                        dbDomainName, maxIdDomain + 1, getDomainName(url)));

                int maxIdLinks = takeMaxIdValueFromTable(statement, dbLinksName);
                statement.executeUpdate(String.format("INSERT into %s(id, text, domain_text, domain_id) values(%d, '%s', '%s', %d)",
                        dbLinksName, maxIdLinks + 1, url, getDomainName(url), maxIdDomain + 1));
            } else if (linkFromListId == null) {
                int maxIdLinks = takeMaxIdValueFromTable(statement, dbLinksName);
                statement.executeUpdate(String.format("INSERT into %s(id, text, domain_text, domain_id) values(%d, '%s', '%s', %d)",
                        dbLinksName, maxIdLinks + 1, url, getDomainName(url), domainFromListId));
            } else {
                System.out.printf("Link %s is also existed in DB.%n", url);
            }
        } catch (IOException | SQLException | ClassNotFoundException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * The method checks the existence of our domain or link in the database.
     *
     * @param statement           - statement that we use.
     * @param dataThatNeedToCheck - data that is checked for their presence in the database.
     * @param dbName              - name of DB in which we check for data availability.
     *
     * @return null if data not exist in our DB or id under which this entry exists.
     *
     * @throws SQLException
     */
    protected Integer containDbThisData(final Statement statement, final String dataThatNeedToCheck,
                                      final String dbName) throws SQLException {
        ResultSet resultSet = statement.executeQuery(String.format("select * from %s", dbName));
        while (resultSet.next()) {
            if (dataThatNeedToCheck.equals(resultSet.getString("text"))) {
                return resultSet.getInt("id");
            }
        }
        return null;
    }

    /**
     * The method gets a connection to the database.
     *
     * @return connection to DB.
     *
     * @throws SQLException
     * @throws IOException
     */
    public static Connection getConnection() throws SQLException, IOException, ClassNotFoundException {
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(Paths.get("src/main/resources/database.properties"))) {
            properties.load(in);
        }
        String url = properties.getProperty("url");
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * The method gets the maximum ID from the database.
     *
     * @param statement - our statement.
     * @param bdName    - the database in which we find the maximum id.
     *
     * @return - returns the maximum ID or zero if the table is still empty.
     *
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    protected Integer takeMaxIdValueFromTable(final Statement statement, final String bdName)
            throws SQLException, IOException, ClassNotFoundException {
        ResultSet resultSet = statement.executeQuery(
                String.format("select * from %s where id = (select max(id) from %s)", bdName, bdName));
        int maxCountDomain = 0;
        while (resultSet.next()) {
            maxCountDomain = resultSet.getInt("id");
        }
        return maxCountDomain;
    }

    /**
     * Method to get all date from concrete table.
     *
     * @param dbName - name of DB from which we want to take data.
     *
     * @return list of all dates from dbName table.
     */
    protected List<String> getAllDateFromTable(final String dbName) {
        List<String> listOfAllDate = new LinkedList<>();
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(String.format("select * from %s", dbName));
            while (resultSet.next()) {
                    listOfAllDate.add(resultSet.getString("text"));
            }
        } catch (IOException | SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return listOfAllDate;
    }

    /**
     * Method to get all date from concrete table.
     *
     * @param domain - name of domain which contain our return links.
     *               ! must write with domain name (for example - "google.com", "java.com").
     *
     * @return list of all links that contain concrete domain name.
     */
    protected List<String> getAllLinksWithConcreteDomain(final String domain) {
        List<String> listOfAllDate = new LinkedList<>();
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(String.format("select * from %s where domain_text = '%s'", dbLinksName, domain));
            while (resultSet.next()) {
                listOfAllDate.add(resultSet.getString("text"));
            }
        } catch (IOException | SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return listOfAllDate;
    }
}
