package dao;

import com.mongodb.client.model.Filters;
import com.mongodb.rx.client.*;
import model.MarketException;
import model.Stock;
import org.bson.Document;
import rx.Observable;
import rx.functions.Func2;

public class MongoMarketDao implements MarketDao {
    private final MongoCollection<Document> companies;

    public MongoMarketDao(String address) {
        MongoClient client = MongoClients.create(address);
        MongoDatabase database = client.getDatabase("market");
        this.companies = database.getCollection("companies");
    }

    @Override
    public Observable<Success> addCompany(String name, int stocksCount, int stocksPrice) {
        return companies
                .find(Filters.eq("companyName", name))
                .toObservable()
                .isEmpty()
                .flatMap(isEmpty -> {
                    if (isEmpty) {
                        return companies.insertOne(new Stock(name, stocksCount, stocksPrice).toDocument());
                    } else {
                        return Observable.error(new MarketException("Company '" + name + "' already exists"));
                    }
                });
    }

    @Override
    public Observable<Stock> getCompanies() {
        return companies.find().toObservable().map(Stock::new);
    }

    @Override
    public Observable<Success> addStocks(String companyName, int stocksCount) {
        return manageStocks(companyName, Stock::add, stocksCount);
    }

    @Override
    public Observable<Stock> getStocksInfo(String companyName) {
        return companies
                .find(Filters.eq("companyName", companyName))
                .toObservable()
                .map(Stock::new);
    }

    @Override
    public Observable<Success> buyStocks(String companyName, int count) {
        return manageStocks(companyName, Stock::minus, count);
    }

    @Override
    public Observable<Success> changeStocksPrice(String companyName, int newStocksPrice) {
        return manageStocks(companyName, Stock::changePrice, newStocksPrice);
    }

    private Observable<Success> manageStocks(String companyName, Func2<Stock, Integer, Stock> action, int parameter) {
        return companies
                .find(Filters.eq("companyName", companyName))
                .toObservable()
                .map(Stock::new)
                .defaultIfEmpty(null)
                .flatMap(company -> {
                    if (company == null) {
                        return Observable.error(new MarketException("Company with name '" + companyName + "' doesn't exists"));
                    } else {
                        return companies.replaceOne(
                                Filters.eq("companyName", companyName),
                                action.call(company, parameter).toDocument())
                                .map(doc -> Success.SUCCESS);
                    }
                });
    }
}
