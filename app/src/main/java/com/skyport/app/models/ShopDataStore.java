package com.skyport.app.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ShopDataStore {

    public static class Shop implements Serializable {
        public String id;
        public String name;
        public String location;
        public String rating;
        public String discount; // like "20% OFF"
        public int imageRes;
        
        public Shop(String id, String name, String location, String rating, String discount, int imageRes) {
            this.id = id; this.name = name; this.location = location; 
            this.rating = rating; this.discount = discount; this.imageRes = imageRes;
        }
    }

    public static class ShopItem implements Serializable {
        public String id;
        public String name;
        public String price;
        public int imageRes;

        public ShopItem(String id, String name, String price, int imageRes) {
            this.id = id; this.name = name; this.price = price; this.imageRes = imageRes;
        }
    }

    public static class OrderItem implements Serializable {
        public String shopName;
        public String itemName;
        public String price;
        public String date;
        public String status;
        public int imageRes;

        public OrderItem(String shopName, String itemName, String price, String date, String status, int imageRes) {
            this.shopName = shopName; this.itemName = itemName; this.price = price;
            this.date = date; this.status = status; this.imageRes = imageRes;
        }
    }

    public static List<Shop> getPopularShops() {
        List<Shop> list = new ArrayList<>();
        list.add(new Shop("1", "Starbucks", "Delhi Airport T3", "4.8", null, com.skyport.app.R.drawable.img_shop_placeholder));
        list.add(new Shop("2", "McDonald's", "Terminal 3", "4.5", null, com.skyport.app.R.drawable.img_shop_placeholder));
        list.add(new Shop("3", "KFC", "Delhi Airport", "4.2", null, com.skyport.app.R.drawable.img_shop_placeholder));
        return list;
    }

    public static List<Shop> getRestaurantRecommendations() {
        List<Shop> list = new ArrayList<>();
        list.add(new Shop("4", "Haldiram's", "Terminal 3 Departure", "4.6", null, com.skyport.app.R.drawable.img_shop_placeholder));
        list.add(new Shop("5", "Subway", "Delhi Airport T3", "4.3", null, com.skyport.app.R.drawable.img_shop_placeholder));
        return list;
    }

    public static List<Shop> getSpecialPromotions() {
        List<Shop> list = new ArrayList<>();
        list.add(new Shop("6", "Burger King", "Combo Meals", "4.4", "20% OFF", com.skyport.app.R.drawable.img_food_placeholder));
        list.add(new Shop("7", "Costa Coffee", "All Beverages", "4.7", "Buy 1 Get 1", com.skyport.app.R.drawable.img_food_placeholder));
        return list;
    }

    public static List<ShopItem> getShopItems(String shopId) {
        List<ShopItem> list = new ArrayList<>();
        list.add(new ShopItem("i1", "Classic Cheeseburger", "₹ 250", com.skyport.app.R.drawable.img_food_placeholder));
        list.add(new ShopItem("i2", "Large Fries", "₹ 150", com.skyport.app.R.drawable.img_food_placeholder));
        list.add(new ShopItem("i3", "Cold Coffee", "₹ 180", com.skyport.app.R.drawable.img_food_placeholder));
        list.add(new ShopItem("i4", "Chocolate Brownie", "₹ 120", com.skyport.app.R.drawable.img_food_placeholder));
        return list;
    }

    public static List<OrderItem> getPastOrders() {
        List<OrderItem> list = new ArrayList<>();
        list.add(new OrderItem("Starbucks", "Cold Coffee & Wrap", "₹ 450", "15 Mar 2026", "Finished", com.skyport.app.R.drawable.img_shop_placeholder));
        list.add(new OrderItem("McDonald's", "McVeggie Meal", "₹ 380", "12 Mar 2026", "Finished", com.skyport.app.R.drawable.img_shop_placeholder));
        list.add(new OrderItem("Haldiram's", "Raj Kachori", "₹ 150", "01 Feb 2026", "Finished", com.skyport.app.R.drawable.img_shop_placeholder));
        return list;
    }
}
