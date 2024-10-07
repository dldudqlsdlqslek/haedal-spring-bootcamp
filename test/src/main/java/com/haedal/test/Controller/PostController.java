package com.haedal.test.Controller;

import com.haedal.test.Domain.Post;
import com.haedal.test.Domain.User;
import com.haedal.test.ResponseDTO.PostResponseDto;
import com.haedal.test.Service.AuthService;
import com.haedal.test.Service.ImageService;
import com.haedal.test.Service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
public class PostController {
    private final AuthService authService;
    private final ImageService imageService;
    private final PostService postService;

    @Autowired
    public PostController(AuthService authService, ImageService imageService, PostService postService) {
        this.authService = authService;
        this.imageService = imageService;
        this.postService = postService;
    }


    @PostMapping("/posts")
    public ResponseEntity<Void> createPost(@RequestParam("image") MultipartFile image, @RequestParam("content") String content,
                                           HttpServletRequest request) throws IOException {
        User currentUser = authService.getCurrentUser(request);
        String imageUrl = imageService.savePostImage(image);
        Post post = new Post(currentUser, content, imageUrl);
        postService.savePost(post);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/posts/user/{userId}")
    public ResponseEntity<List<PostResponseDto>> getPostsByUser(@PathVariable Long userId) {
        List<PostResponseDto> posts = postService.getPostsByUser(userId);
        return ResponseEntity.ok(posts);
    }
}