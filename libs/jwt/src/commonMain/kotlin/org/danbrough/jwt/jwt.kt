package org.danbrough.jwt


internal val log = klog.logger("JWT")


expect class JWTScope

expect fun <R> JWTScope.encode(block: JWTEncode.()->R): R

expect fun <R> jwt(block: JWTScope.()->R): R

@OptIn(ExperimentalUnsignedTypes::class)
expect fun <R> JWTScope.decode(token:String, alg: JwtAlg, secret:UByteArray, block: JWTDecode.()->R): R


