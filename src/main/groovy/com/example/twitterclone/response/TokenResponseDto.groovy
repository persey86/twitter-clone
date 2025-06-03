package com.example.twitterclone.response

import com.fasterxml.jackson.annotation.JsonInclude
import groovy.transform.ToString
import groovy.transform.TupleConstructor
import groovy.transform.builder.Builder

@ToString
@TupleConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
class TokenResponseDto {
    String accessToken
    String refreshToken
}
