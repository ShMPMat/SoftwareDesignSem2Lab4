package http;

import model.MarketException;

public interface MarketClient {
    void buyStocks(String companyName, int count) throws MarketException;

    void sellStocks(String companyName, int count) throws MarketException;

    int getStocksPrice(String companyName) throws MarketException;

    int getStocksCount(String companyName) throws MarketException;
}
