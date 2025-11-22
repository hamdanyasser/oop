package com.example.finalproject.service;

import com.example.finalproject.dao.ReviewDao;
import com.example.finalproject.model.Review;
import java.sql.SQLException;
import java.util.List;

public class ReviewService {
    private final ReviewDao dao = new ReviewDao();

    public void addReview(Review r) throws SQLException {
        dao.saveReview(r);
    }

    public List<Review> getReviewsByProduct(int productId) {
        return dao.getReviewsByProduct(productId);
    }

    public double getAverageRating(int productId) {
        return dao.getAverageRating(productId);
    }
}
