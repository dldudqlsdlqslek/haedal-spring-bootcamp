package com.haedal.test.Repository;


import com.haedal.test.Domain.Post;
import com.haedal.test.Domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Long countByUser(User user);
    List<Post> findByUser(User user);
}
