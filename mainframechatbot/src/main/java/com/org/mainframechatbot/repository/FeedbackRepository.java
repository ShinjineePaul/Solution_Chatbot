package com.org.mainframechatbot.repository;

import com.org.mainframechatbot.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    @Query("SELECT f FROM Feedback f WHERE f.positive = true ORDER BY f.createdAt DESC")
    List<Feedback> findAllPositiveOrderByCreatedAtDesc();

    List<Feedback> findByPositiveFalseOrderByCreatedAtDesc();
}