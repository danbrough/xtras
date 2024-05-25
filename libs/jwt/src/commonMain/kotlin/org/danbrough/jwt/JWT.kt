package org.danbrough.jwt

expect class JWTEncode

expect fun <R> jwtEncode(block: JWTEncode.()->R): R


expect class JWTDecode

expect fun <R> jwtDecode(token:String,alg: JwtAlg,secret:UByteArray,block: JWTDecode.()->R): R



