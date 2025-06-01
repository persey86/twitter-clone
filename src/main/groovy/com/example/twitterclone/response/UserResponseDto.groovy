package com.example.twitterclone.response

import com.fasterxml.jackson.annotation.JsonInclude
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TupleConstructor
import groovy.transform.builder.Builder

@ToString
//@EqualsAndHashCode
@TupleConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
class UserResponseDto {
    String id
    String token
    String bio
    Set<String> following = [] as Set

    UserResponseDto() {}
}
