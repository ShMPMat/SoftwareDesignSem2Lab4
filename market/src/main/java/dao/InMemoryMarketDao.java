package dao;

import com.mongodb.rx.client.*;
import model.MarketException;
import model.Stock;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class InMemoryMarketDao implements MarketDao {
    private final List<Stock> stocks = new ArrayList<>();

    public InMemoryMarketDao() {
    }

    @Override
    public Observable<Success> addCompany(String name, int stocksCount, int stocksPrice) {
        stocks.add(new Stock(name, stocksCount, stocksPrice));
        return Observable.just(Success.SUCCESS);
    }

    @Override
    public Observable<Stock> getCompanies() {
        return Observable.from(stocks);
    }

    @Override
    public Observable<Success> addStocks(String companyName, int stocksCount) {
        return stocks.stream()
                .filter(s -> s.getCompanyName().equals(companyName))
                .findFirst()
                .map(s -> {
                    s.setCount(s.getCount() + stocksCount);
                    return Observable.just(Success.SUCCESS);
                })
                .orElse(Observable.error(new MarketException("Company '" + companyName + "' doesn't exists")));
    }

    @Override
    public Observable<Stock> getStocksInfo(String companyName) {
        return Observable.from(
                stocks.stream()
                        .filter(s -> s.getCompanyName().equals(companyName))
                        .collect(Collectors.toList())
        );
    }

    @Override
    public Observable<Success> buyStocks(String companyName, int count) {
        return stocks.stream()
                .filter(s -> s.getCompanyName().equals(companyName))
                .findFirst()
                .map(s -> {
                    if (s.getCount() < count) {
                        Observable.error(new MarketException("Company '" + companyName + "' doesn't have " + count + "stocks"));
                    }
                    s.setCount(s.getCount() - count);
                    return Observable.just(Success.SUCCESS);
                })
                .orElse(Observable.error(new MarketException("Company '" + companyName + "' doesn't exists")));
    }

    @Override
    public Observable<Success> changeStocksPrice(String companyName, int newPrice) {
        return stocks.stream()
                .filter(s -> s.getCompanyName().equals(companyName))
                .findFirst()
                .map(s -> {
                    s.setPrice(newPrice);
                    return Observable.just(Success.SUCCESS);
                })
                .orElse(Observable.error(new MarketException("Company '" + companyName + "' doesn't exists")));
    }
}
