package dao;

import com.mongodb.rx.client.Success;
import http.MarketHttpClient;
import org.junit.*;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class IntegrationTest {
    @ClassRule
    public static GenericContainer marketContainer = new FixedHostPortGenericContainer("market:1.0-SNAPSHOT")
            .withFixedExposedPort(8080, 8080)
            .withExposedPorts(8080);

    private AccountDao dao;

    private final static String companyName = "test";
    private final static int stocksPrice = 42;
    private final long userId = 1;

    @Before
    public void startCompany() throws Exception {
        marketContainer.start();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/add_company?name=" + companyName + "&stocks_count=1000&stocks_price=" + stocksPrice))
                .GET()
                .build();

        HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Before
    public void createUser() {
        dao = new InMemoryAccountDao(new MarketHttpClient());
        dao.addUser(userId);
        dao.addMoney(userId, 1000);
    }

    @After
    public void stopContainer() {
        marketContainer.stop();
    }

    @Test
    public void buyStocksTest() {
        assertThat(dao.buyStocks(userId, companyName, 10).toBlocking().single())
                .isEqualTo(Success.SUCCESS);
    }

    @Test
    public void sellStocksTest() {
        dao.buyStocks(userId, companyName, 10);
        assertThat(dao.sellStocks(userId, companyName, 10).toBlocking().single())
                .isEqualTo(Success.SUCCESS);
    }
    @Test
    public void notEnoughStocksUserErrorTest() {
        assertThatThrownBy(() -> dao.sellStocks(userId, companyName, 1).toBlocking().single())
                .withFailMessage("No stocks for 'test'")
                .isInstanceOf(RuntimeException.class);

        dao.buyStocks(userId, companyName, 10);
        assertThatThrownBy(() -> dao.sellStocks(userId, companyName, 11).toBlocking().single())
                .withFailMessage("Not enough stocks")
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    public void noCompanyErrorTest() {
        assertThatThrownBy(() -> dao.buyStocks(userId, "nope", 10).toBlocking().single())
                .withFailMessage("Company 'nope' doesn't exists")
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    public void notEnoughStocksErrorTest() {
        assertThatThrownBy(() -> dao.buyStocks(userId, companyName, 2000).toBlocking().single())
                .withFailMessage("Not enough stocks in market")
                .isInstanceOf(RuntimeException.class);
    }

    @Test()
    public void notEnoughMoneyTest() {
        assertThatThrownBy(() -> dao.buyStocks(userId, companyName, 150).toBlocking().single())
                .withFailMessage("Not enough money")
                .isInstanceOf(RuntimeException.class);
    }
}
