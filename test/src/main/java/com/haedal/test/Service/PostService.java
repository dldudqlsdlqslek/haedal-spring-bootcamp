package com.haedal.test.Service;


import com.haedal.test.Domain.Post;
import com.haedal.test.Domain.User;
import com.haedal.test.Repository.LikeRepository;
import com.haedal.test.Repository.PostRepository;
import com.haedal.test.Repository.UserRepository;
import com.haedal.test.ResponseDTO.PostResponseDto;
import com.haedal.test.ResponseDTO.UserSimpleResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ImageService imageService;
    private final LikeRepository likeRepository;


    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository, UserService userService, ImageService imageService, LikeRepository likeRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.imageService = imageService;
        this.likeRepository = likeRepository;
    }
    public void savePost(Post post){
        Post saved = postRepository.save(post);
    }

    public List<PostResponseDto> getPostsByUser(Long targetUserId) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        List<Post> posts = postRepository.findByUser(targetUser);
        posts.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));

        return posts.stream().map(post -> convertPostToDto(targetUser, post)).toList();
    }


    private PostResponseDto convertPostToDto(User currentUser, Post post) {
        User author = post.getUser();
        UserSimpleResponseDto userSimpleResponseDto = userService.convertUserToSimpleDto(currentUser, author);
        String imageUrl = post.getImageUrl();
        String imageData = imageService.encodeImageToBase64(System.getProperty("user.dir") + "/src/main/resources/static/" + imageUrl);

        return new PostResponseDto(
                post.getId(),
                userSimpleResponseDto,
                imageData,
                post.getContent(),
                likeRepository.countByPost(post),
                likeRepository.existsByUserAndPost(currentUser, post),
                post.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm"))
        );
    }


}
