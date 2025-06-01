package com.example.twitterclone

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class TwitterCloneApplication {

	static void main(String[] args) {
		SpringApplication.run(TwitterCloneApplication, args)
	}

}
