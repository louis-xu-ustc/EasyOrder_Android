package edu.cmu.EasyOrder_Android;

import java.util.ArrayList;

/**
 * Created by yunpengx on 7/9/17.
 */

public class Order {
    public String userName;
    public Boolean ifNotify;
    public int totalPrice;
    ArrayList<Dish> dishList;

    public Order() {
        dishList = new ArrayList<>();
        ifNotify = false;
        totalPrice = 0;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Boolean getIfNofity() {
        return this.ifNotify;
    }

    public void setIfNotify(Boolean ifNotify) {
        this.ifNotify = ifNotify;
    }

    public int getTotalPrice() {
        if (totalPrice == 0) {
            calcTotalPrice();
        }
        return this.totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public ArrayList<Dish> getDishList() {
        return this.dishList;
    }

    public void setDishList(ArrayList<Dish> orderList) {
        this.dishList = orderList;
    }

    private void calcTotalPrice() {
        int total = 0;
        for (Dish dish : dishList) {
            total += dish.getPrice() * dish.getQuantity();
        }
        totalPrice = total;
    }

    public void addDish(Dish dish) {
        this.dishList.add(dish);
    }
}
