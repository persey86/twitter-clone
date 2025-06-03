package com.example.twitterclone.service

import com.example.twitterclone.model.Comment
import com.example.twitterclone.model.Post
import com.example.twitterclone.model.User
import com.example.twitterclone.repository.PostRepository
import com.example.twitterclone.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

import java.time.Instant

@Component
class DataLoader implements CommandLineRunner {
    private final PostRepository postRepository
    private final UserRepository userRepository
    private final PasswordEncoder passwordEncoder

    DataLoader(PostRepository postRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.postRepository = postRepository
        this.userRepository = userRepository
        this.passwordEncoder = passwordEncoder
    }

    @Override
    void run(String... args) throws Exception {
        userRepository.deleteAll()

        Set<String> followers = new HashSet<>()
        followers.add("111")
        followers.add("112")

        User user1 = new User()
        user1.setId("111")
        user1.setUsername("bio1_user")
        user1.setBio("bio1")
        user1.setPassword(passwordEncoder.encode("passw1"))
        user1.setFollowing(Collections.singleton("113"))

        User user2 = new User()
        user2.setId("112")
        user2.setUsername("bio2_user")
        user2.setBio("bio2")
        user2.setPassword(passwordEncoder.encode("passw2"))

        User user3 = new User()
        user3.setId("113")
        user3.setUsername("bio3_user")
        user3.setBio("bio3")
        user3.setPassword(passwordEncoder.encode("passw3"))
        user3.setFollowing(followers)

        userRepository.save(user1)
        userRepository.save(user2)
        userRepository.save(user3)

        postRepository.deleteAll()

        List<Comment> comments = new ArrayList<>()
        Comment comment1 = new Comment()
        comment1.setAuthorId("111")
        comment1.setText("Nice post!")

        Comment comment2 = new Comment()
        comment2.setAuthorId("112")
        comment2.setText("It would be better to type nothing!")

        comments.add(comment1)
        comments.add(comment2)

        Set<String> likes = new HashSet<>()
        likes.add("111")
        likes.add("112")

        Post post1 = new Post()
        post1.setContent("This is the first test post")
        post1.setAuthorId("113")
        post1.setCreatedAt(Instant.now().minusSeconds(14400))

        Post post2 = new Post()
        post2.setContent("This is the second test post")
        post2.setAuthorId("113")
        post2.setCreatedAt(Instant.now().minusSeconds(7200))
        post2.setComments(comments)
        post2.setLikes(likes)

        Post post3 = new Post()
        post3.setContent("This is the third test post")
        post3.setAuthorId("113")
        post3.setCreatedAt(Instant.now().minusSeconds(3600))
        post3.setLikes(likes)

        Post post4 = new Post()
        post4.setContent("This is the forth test post")
        post4.setAuthorId("111")
        post4.setCreatedAt(Instant.now().minusSeconds(1800))

        Post post5 = new Post()
        post5.setContent("This is the fifth test post")
        post5.setAuthorId("111")
        post5.setCreatedAt(Instant.now().minusSeconds(900))

        Post post6 = new Post()
        post6.setContent("This is the sixth test post")
        post6.setAuthorId("111")
        post6.setCreatedAt(Instant.now().minusSeconds(450))

        Post post7 = new Post()
        post7.setContent("This is the seventh test post")
        post7.setAuthorId("112")
        post7.setCreatedAt(Instant.now().minusSeconds(225))

        Post post8 = new Post()
        post8.setContent("This is the eights test post")
        post8.setAuthorId("112")
        post8.setCreatedAt(Instant.now().minusSeconds(200))

        Post post9 = new Post()
        post9.setContent("This is the ninth test post")
        post9.setAuthorId("112")
        post9.setCreatedAt(Instant.now().minusSeconds(100))

        Post post0 = new Post()
        post0.setContent("This is the tenth test post")
        post0.setAuthorId("111")
        post0.setCreatedAt(Instant.now().minusSeconds(28800))

        Post post = new Post()
        post.setContent("This is the zero test post")
        post.setAuthorId("111")
        post.setCreatedAt(Instant.now().minusSeconds(57600))

        Post post11 = new Post()
        post11.setContent("This is the eleventh test post")
        post11.setAuthorId("111")
        post11.setCreatedAt(Instant.now().minusSeconds(50))

        postRepository.save(post1)
        postRepository.save(post2)
        postRepository.save(post3)
        postRepository.save(post4)
        postRepository.save(post5)
        postRepository.save(post6)
        postRepository.save(post7)
        postRepository.save(post9)
        postRepository.save(post0)
        postRepository.save(post)
        postRepository.save(post11)

        System.out.println("Test data has been loaded into the Posts collection.")

    }
}
