package org.danbrough.jwt


internal val log = klog.logger("JWT")



expect fun <R> jwtEncode(block: JWTEncode.()->R): R




@OptIn(ExperimentalUnsignedTypes::class)
expect fun <R> jwtDecode(token:String, alg: JwtAlg, secret:UByteArray, block: JWTDecode.()->R): R


