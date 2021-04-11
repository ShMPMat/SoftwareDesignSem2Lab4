import dao.InMemoryMarketDao;
import dao.MongoMarketDao;
import http.RxNettyMarketHttpServer;


public class Main {
    public static void main(String[] args) {
//        new RxNettyMarketHttpServer(new MongoMarketDao("mongodb://localhost:27017")).run();
        new RxNettyMarketHttpServer(new InMemoryMarketDao()).run();
    }
}
