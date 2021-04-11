package model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class User {
    private final long id;
    private int money;
    private final Map<String, Stock> stocks = new HashMap<>();

    public User(long id, int money) {
        this.id = id;
        this.money = money;
    }

    public long getId() {
        return id;
    }

    public int getMoney() {
        return money;
    }

    public void addMoney(int addition) {
        money += addition;
    }

    public Collection<Stock> getStocks() {
        return stocks.values();
    }

    public void buyStocks(String companyName, int price, int count) throws MarketException {
        if (price * count > money) {
            throw new MarketException("Not enough money");
        }
        Stock current = stocks.getOrDefault(companyName, new Stock(companyName, 0, 0));
        stocks.put(companyName, current.add(count));
        money -= price * count;
    }

    public void sellStocks(String companyName, int price, int count) throws MarketException {
        Stock current = stocks.get(companyName);
        if (current == null) {
            throw new MarketException("No stocks for '" + companyName + "'");
        } else if (current.getCount() < count) {
            throw new MarketException("Not enough stocks");
        }
        current.setCount(current.getCount() - count);
        money += price * count;
    }
}
