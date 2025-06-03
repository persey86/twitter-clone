package com.example.twitterclone.response

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TupleConstructor

@ToString
@EqualsAndHashCode
@TupleConstructor
class UserRequestDto {
    String username
    String password
    String bio

    UserRequestDto() {
    }
}
