package edu.cmu.EasyOrder_Android;

/**
 * Created by yunpengx on 7/8/17.
 */

public class Dish {
    private String name;
    private String image;
    private int price;
    private int quantity;
    private int rate;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return this.image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getPrice() {
        return this.price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getRate() {
        return this.rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public int getTotalPrice() {
        return this.price * this.quantity;
    }

    @Override
    public String toString() {
        return getName() + "\t\t\t" + getQuantity() + "\t\t\t" + "$" + getTotalPrice();
    }
}
