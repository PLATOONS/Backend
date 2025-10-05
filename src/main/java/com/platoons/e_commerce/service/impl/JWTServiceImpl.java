package com.platoons.e_commerce.service.impl;

import com.platoons.e_commerce.service.IJWTService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class JWTServiceImpl implements IJWTService {

    private final Environment env;

    public JWTServiceImpl(Environment env) {
        this.env = env;
    }

    @Override
    public void validateToken(String jwt) {
        if (jwt != null && jwt.startsWith("Bearer ")) {
            try {
                jwt = jwt.substring(7);

                String secret = env.getProperty("JWT_SECRET", "jxgEQeXHuPq8VdbyYFNkANdudQ53YUn4");
                SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

                Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(jwt).getPayload();
                String username = claims.get("username").toString();
                String authorities = claims.get("authorities").toString();

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        username, null, AuthorityUtils.commaSeparatedStringToAuthorityList(authorities));
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception ignored) {}
        }
    }

    @Override
    public String generateToken(Authentication authentication) {
        if(authentication == null)
            return null;

        String secret = env.getProperty("JWT_SECRET", "jxgEQeXHuPq8VdbyYFNkANdudQ53YUn4");
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts
                .builder()
                .issuer("JsnmCFjaN")
                .subject("JWT Token")
                .claim("username", authentication.getName())
                .claim("authorities", authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(",")))
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + 24*60*60*1000))
                .signWith(secretKey)
                .compact();
    }
}
