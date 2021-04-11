package model;

import org.bson.Document;

public class Stock {
    private final String companyName;
    private int count;
    private int price;

    public Stock(Document document) {
        this(document.getString("companyName"), document.getInteger("count"), document.getInteger("price"));
    }

    public Stock(String companyName, int count, int price) {
        this.companyName = companyName;
        this.count = count;
        this.price = price;
    }

    public Document toDocument() {
        return new Document()
                .append("companyName", companyName)
                .append("count", count)
                .append("price", price);
    }

    public String getCompanyName() {
        return companyName;
    }

    public int getCount() {
        return count;
    }

    public int getPrice() {
        return price;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Stock changePrice(int newStockPrice) {
        return new Stock(companyName, count, newStockPrice);
    }

    public Stock add(int stocksCount) {
        return new Stock(companyName, count + stocksCount, price);
    }

    public Stock minus(int stocksCount) {
        if (count < stocksCount) {
            return this;
        }
        return new Stock(companyName, count - stocksCount, price);
    }
}
