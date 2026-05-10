package com.substring.auth.auth_app_backend.controllers;

import com.substring.auth.auth_app_backend.dtos.LoginRequest;
import com.substring.auth.auth_app_backend.dtos.TokenResponse;
import com.substring.auth.auth_app_backend.dtos.Userdto;
import com.substring.auth.auth_app_backend.entities.RefreshToken;
import com.substring.auth.auth_app_backend.entities.User;
import com.substring.auth.auth_app_backend.repositories.RefreshTokenRepository;
import com.substring.auth.auth_app_backend.repositories.UserRepository;
import com.substring.auth.auth_app_backend.security.CookieService;
import com.substring.auth.auth_app_backend.security.JwtService;
import com.substring.auth.auth_app_backend.services.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final ModelMapper mapper;
    private final CookieService cookieService;


    @PostMapping("/login")
    public ResponseEntity<TokenResponse>login(@RequestBody LoginRequest loginRequest, HttpServletResponse response){
       Authentication authenticate=  authenticate(loginRequest);
       User user = userRepository.findByEmail(loginRequest.email()).orElseThrow(()-> new BadCredentialsException("Invalid email or password"));

       // check if the user is Disabled
       if(!user.isEnabled()){
           throw new DisabledException("User is disabled");
       }

      // generate -> refresh token
      String jti = UUID.randomUUID().toString();
       var refreshTokenOb = RefreshToken.builder()
               .jti(jti)
               .user(user)
               .createdAt(Instant.now())
               .expiredAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
               .revoked(false)
               .build();

         // save the refresh token into database
         refreshTokenRepository.save(refreshTokenOb);

       // generate -> access token
       String accessToken = jwtService.generateAccessToken(user);
       String refreshToken = jwtService.generateRefreshToken(user,refreshTokenOb.getJti());

        // use cookie service to attach the token
        cookieService.attachRefreshCookie(response,refreshToken,(int)jwtService.getRefreshTtlSeconds());
        cookieService.addNoStoreHeaders(response);

       TokenResponse tokenResponse = TokenResponse.of(accessToken,refreshToken, jwtService.getAccessTtlSeconds(),mapper.map(user,Userdto.class));
       return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<Userdto>register(@RequestBody Userdto userdto){
        return ResponseEntity.status(HttpStatus.OK).body(authService.register(userdto));
    }

    // Private Function for Authentication
    private Authentication authenticate(LoginRequest loginRequest){
        try{
           return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(),loginRequest.password()));
        }
        catch (Exception e){
            throw new BadCredentialsException("Invalid email or password");
        }
    }

}
