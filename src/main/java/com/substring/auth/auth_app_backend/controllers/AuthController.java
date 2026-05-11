package com.substring.auth.auth_app_backend.controllers;

import com.substring.auth.auth_app_backend.dtos.LoginRequest;
import com.substring.auth.auth_app_backend.dtos.RefreshTokenRequest;
import com.substring.auth.auth_app_backend.dtos.TokenResponse;
import com.substring.auth.auth_app_backend.dtos.Userdto;
import com.substring.auth.auth_app_backend.entities.RefreshToken;
import com.substring.auth.auth_app_backend.entities.User;
import com.substring.auth.auth_app_backend.repositories.RefreshTokenRepository;
import com.substring.auth.auth_app_backend.repositories.UserRepository;
import com.substring.auth.auth_app_backend.security.CookieService;
import com.substring.auth.auth_app_backend.security.JwtService;
import com.substring.auth.auth_app_backend.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
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
import java.util.Arrays;
import java.util.Optional;
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


    /*
      API for Login user here two kinds of followup will be
     -> Generate JWT token using User details
     -> Refresh token generated and save it to the databases
     and also store in the local browser using COOKIE
     */
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

        //generate access token to return the token response while log in hit
       TokenResponse tokenResponse = TokenResponse.of(accessToken,refreshToken, jwtService.getAccessTtlSeconds(),mapper.map(user,Userdto.class));
       return ResponseEntity.ok(tokenResponse);
    }

    // API for register a new user simply call the register method from the authService class
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

    /*
     API for Access and Refresh Token Renew
     - > * RefreshTokenRequest * DTO is not essential its added if somehow
         sent to this api to handle it, but usually it can be fetched
         from the browser cookie
    */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse>refreshToken(
            @RequestBody(required = false)RefreshTokenRequest body,
            HttpServletResponse response,
            HttpServletRequest request )
    {

        String refreshToken = readRefreshTokenFromRequest(body, request).orElseThrow(()-> new BadCredentialsException("Invalid refresh token"));

        // check if the token is valid or not
        if(!jwtService.isRefreshToken(refreshToken)){
            throw new BadCredentialsException("Invalid refresh token");
        }

        String jti = jwtService.getJti(refreshToken); // get the JTI from refresh token
        UUID userId = jwtService.getUserId(refreshToken); // get the User Id from refresh token

        RefreshToken storedRefreshToken = refreshTokenRepository.findByJti(jti)
                .orElseThrow(()-> new BadCredentialsException("Invalid refresh token type"));

        // check stored refresh token revoked, expired , token holder(user)
        if(storedRefreshToken.isRevoked())
        {
            throw new BadCredentialsException("Refresh Token Expired or Revoked");
        }
        if(storedRefreshToken.getExpiredAt().isBefore(Instant.now()))
        {
            throw new BadCredentialsException("Refresh Token Expired");
        }
        if(!storedRefreshToken.getUser().getId().equals(userId))
        {
            throw new BadCredentialsException("Refresh Token Doesn't Belong to this User");
        }

        // Refresh token rotate or changed
        storedRefreshToken.setRevoked(true);
        String newJti = UUID.randomUUID().toString();
        storedRefreshToken.setReplacedByToken(newJti);
        refreshTokenRepository.save(storedRefreshToken);

        User user = storedRefreshToken.getUser();

        var newRefreshTokenOb = RefreshToken.builder()
                .jti(newJti)
                .user(user)
                .createdAt(Instant.now())
                .expiredAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(newRefreshTokenOb);

        String newAccessToken =  jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user,newRefreshTokenOb.getJti());

        cookieService.attachRefreshCookie(response,newRefreshToken,(int)jwtService.getRefreshTtlSeconds());
        cookieService.addNoStoreHeaders(response);
        return ResponseEntity.ok(TokenResponse.of(newAccessToken,newRefreshToken,jwtService.getAccessTtlSeconds(),mapper.map(user,Userdto.class)))

    }



    //this method will read refresh token from request header or body
    private Optional<String> readRefreshTokenFromRequest(RefreshTokenRequest body, HttpServletRequest request){

        // prefer reading refresh token from cookie
        if(request.getCookies() != null){
           Optional<String> fromCookie =  Arrays.stream(request.getCookies())
                    .filter(c->cookieService.getRefreshTokenCookieName().equals(c.getName()))
                    .map(Cookie::getValue)
                    .filter(v->!v.isBlank())
                    .findFirst();

           if(fromCookie.isPresent()){
               return fromCookie;
           }
        }

        // if body has a refresh token
        if( body!=null && body.refreshToken() != null && !body.refreshToken().isBlank()){
            return Optional.of(body.refreshToken());
        }

        // custom header
        String refreshHeader  = request.getHeader("Refresh-Token");
        if(refreshHeader != null && !refreshHeader.isBlank()){
            return Optional.of(refreshHeader.trim());
        }

        // Authorization = Bearer <token>
        String authHeader = request.getHeader("Authorization");
        if(authHeader != null && !authHeader.regionMatches(true,0,"Bearer",0,authHeader.length())){
            String candidate = authHeader.substring(7).trim();
            if(!candidate.isEmpty()){
                try{
                    if(jwtService.isRefreshToken(candidate)){
                        return Optional.of(candidate);
                    }
                }
                catch(Exception ignored){

                }
            }
        }
        return Optional.empty();
    }
}
