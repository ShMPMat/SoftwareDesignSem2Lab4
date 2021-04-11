package dao;

import com.mongodb.rx.client.Success;
import http.MarketClient;
import model.MarketException;
import model.Stock;
import model.User;
import rx.Observable;

import java.util.HashMap;
import java.util.Map;

public class InMemoryAccountDao implements AccountDao {
    private final MarketClient marketClient;
    private final Map<Long, User> users = new HashMap<>();

    public InMemoryAccountDao(MarketClient marketClient) {
        this.marketClient = marketClient;
    }

    @Override
    public Observable<Success> addUser(long id) {
        if (users.containsKey(id)) {
            return Observable.error(new MarketException("User with id = " + id + " already exists"));
        }
        users.put(id, new User(id, 0));
        return Observable.just(Success.SUCCESS);
    }

    @Override
    public Observable<Success> addMoney(long id, int count) {
        if (!users.containsKey(id)) {
            return Observable.error(new MarketException("User with id = " + id + " doesn't exist"));
        }
        users.get(id).addMoney(count);
        return Observable.just(Success.SUCCESS);
    }

    @Override
    public Observable<Stock> getUserStocksInfo(long id) {
        if (!users.containsKey(id)) {
            return Observable.error(new MarketException("User with id = " + id + " doesn't exist"));
        }
        return Observable.from(users.get(id).getStocks()).map(this::updateStocksPrice);
    }

    @Override
    public Observable<Integer> getAllMoney(long id) {
        if (!users.containsKey(id)) {
            return Observable.error(new MarketException("User with id = " + id + " doesn't exist"));
        }
        User user = users.get(id);
        return Observable.from(user.getStocks())
                .map(this::updateStocksPrice)
                .map(Stock::getPrice)
                .defaultIfEmpty(0)
                .reduce(Integer::sum)
                .map(x -> x + user.getMoney());
    }

    @Override
    public Observable<Success> buyStocks(long id, String companyName, int count) {
        if (!users.containsKey(id)) {
            return Observable.error(new MarketException("User with id = " + id + " doesn't exist"));
        }

        try {
            int price = marketClient.getStocksPrice(companyName);
            int availableCount = marketClient.getStocksCount(companyName);
            if (availableCount < count) {
                return Observable.error(new MarketException("Not enough stocks on market"));
            }
            users.get(id).buyStocks(companyName, price, count);
            marketClient.buyStocks(companyName, count);
            return Observable.just(Success.SUCCESS);
        } catch (MarketException e) {
            return Observable.error(e);
        }
    }

    @Override
    public Observable<Success> sellStocks(long id, String companyName, int count) {
        if (!users.containsKey(id)) {
            return Observable.error(new MarketException("User with id = " + id + " doesn't exist"));
        }

        try {
            int price = marketClient.getStocksPrice(companyName);
            users.get(id).sellStocks(companyName, price, count);
            marketClient.sellStocks(companyName, count);
            return Observable.just(Success.SUCCESS);
        } catch (MarketException e) {
            return Observable.error(e);
        }
    }

    private Stock updateStocksPrice(Stock stock) {
        try {
            return stock.changePrice(marketClient.getStocksPrice(stock.getCompanyName()));
        } catch (MarketException e) {
            e.printStackTrace();
            return null;
        }
    }
}
