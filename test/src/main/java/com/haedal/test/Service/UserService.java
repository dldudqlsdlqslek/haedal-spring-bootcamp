package com.haedal.test.Service;


import com.haedal.test.Domain.User;
import com.haedal.test.Repository.FollowRepository;
import com.haedal.test.Repository.PostRepository;
import com.haedal.test.Repository.UserRepository;
import com.haedal.test.RequestDTO.UserUpdateRequestDto;
import com.haedal.test.ResponseDTO.UserDetailResponseDto;
import com.haedal.test.ResponseDTO.UserSimpleResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final PostRepository postRepository;
    private final FollowRepository followRepository;

    @Autowired
    public UserService(FollowRepository followRepository,UserRepository userRepository, ImageService imageService, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.imageService = imageService;
        this.postRepository = postRepository;
        this.followRepository = followRepository;
    }

    public UserSimpleResponseDto saveUser(User newUser) {
        // 중복 회원 검증
        if (userRepository.existsByUsername(newUser.getUsername())) {
            throw new IllegalStateException("중복되는 username입니다.");
        }

        userRepository.save(newUser);
        return convertUserToSimpleDto(newUser, newUser);
    }

    public UserSimpleResponseDto convertUserToSimpleDto(User currentUser, User targetUser) {
        String imageUrl = targetUser.getImageUrl();
        String imageData = imageService.encodeImageToBase64(System.getProperty("user.dir") + "/src/main/resources/static/" + imageUrl);

        return new UserSimpleResponseDto(
                currentUser.getId(),
                currentUser.getUsername(),
                currentUser.getName(),
                imageData,
                followRepository.existsByFollowerAndFollowing(currentUser, targetUser)
        );
    }

    public List<UserSimpleResponseDto> getAllUsers(User currentUser) {
        List<User> users = userRepository.findAll();
        users.remove(currentUser);
        return users.stream().map(user -> convertUserToSimpleDto(currentUser, user)).toList();
    }


    public List<UserSimpleResponseDto> getUserByUsername(User currentUser, String username) {
        List<UserSimpleResponseDto> user = new ArrayList<>();
        User targetUser = userRepository.findByUsername(username).orElse(null);
        if (targetUser != null) {
            UserSimpleResponseDto userSimpleResponseDto = convertUserToSimpleDto(currentUser, targetUser);
            user.add(userSimpleResponseDto);
        }

        return user;
    }


    public UserDetailResponseDto updateUser(User currentUser, UserUpdateRequestDto userUpdateRequestDto) {
        if (userUpdateRequestDto.getUsername() != null) {
            currentUser.setUsername(userUpdateRequestDto.getUsername());
        }
        if (userUpdateRequestDto.getPassword() != null) {
            currentUser.setPassword(userUpdateRequestDto.getPassword());
        }
        if (userUpdateRequestDto.getName() != null) {
            currentUser.setName(userUpdateRequestDto.getName());
        }
        if (userUpdateRequestDto.getBio() != null) {
            currentUser.setBio(userUpdateRequestDto.getBio());
        }

        userRepository.save(currentUser);

        return convertUserToDetailDto(currentUser, currentUser);
    }


    public UserDetailResponseDto getUserDetail(User currentUser, Long targetUserId) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        return convertUserToDetailDto(currentUser, targetUser);
    }

    public UserDetailResponseDto convertUserToDetailDto(User currentUser, User targetUser) {
        String imageUrl = targetUser.getImageUrl();
        String imageData = imageService.encodeImageToBase64(System.getProperty("user.dir") + "/src/main/resources/static/" + imageUrl);


        return new UserDetailResponseDto(
                targetUser.getId(),
                targetUser.getUsername(),
                targetUser.getName(),
                imageData,
                followRepository.existsByFollowerAndFollowing(currentUser, targetUser),
                targetUser.getBio(),
                targetUser.getJoinedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm")),
                postRepository.countByUser(targetUser),
                followRepository.countByFollowing(targetUser),
                followRepository.countByFollower(targetUser)
        );
    }

}