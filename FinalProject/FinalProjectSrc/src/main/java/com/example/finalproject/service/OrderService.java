package com.example.finalproject.service;

import com.example.finalproject.dao.OrderDao;
import com.example.finalproject.model.Order;
import com.example.finalproject.model.OrderItem;

import java.util.List;

public class OrderService {
    private final OrderDao dao = new OrderDao();

    private OrderDao orderDao = new OrderDao();





    public void placeOrder(Order order) throws Exception {
        dao.saveOrder(order);
    }
    public List<Order> filterOrders(String keyword, String status) {
        return dao.filterOrders(keyword, status);
    }



    public List<Order> getAllOrders() {
        return dao.findAll();
    }

    public void markDelivered(int id) {
        dao.updateStatus(id, "DELIVERED");
    }

    public void deleteOrder(int id) {
        dao.delete(id);
    }
}
