package dao;

import com.mongodb.rx.client.Success;
import model.Stock;
import rx.Observable;

public interface MarketDao {
    Observable<Success> addCompany(String name, int stocksCount, int stocksPrice);

    Observable<Stock> getCompanies();

    Observable<Success> addStocks(String companyName, int stocksCount);

    Observable<Stock> getStocksInfo(String companyName);

    Observable<Success> buyStocks(String companyName, int count);

    Observable<Success> changeStocksPrice(String companyName, int newStocksPrice);
}
