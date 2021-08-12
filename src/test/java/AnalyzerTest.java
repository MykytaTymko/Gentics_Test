import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

public class AnalyzerTest {

    @Test
    public void getConnection() throws SQLException, IOException, ClassNotFoundException {
        Analyzer.getConnection();
    }

    @Test
    public void getAllDateFromTable() {
        String tableName = "domains";
        Analyzer analyzer = new Analyzer();
        for (String oneData : analyzer.getAllDateFromTable(tableName)){
            System.out.println(oneData);
        }
    }

    @Test
    public void getLinksByDomainName() {
        String domainName = "java.com";
        Analyzer analyzer = new Analyzer();
        for (String oneLink : analyzer.getAllLinksWithConcreteDomain(domainName)){
            System.out.println(oneLink);
        }
    }
}
