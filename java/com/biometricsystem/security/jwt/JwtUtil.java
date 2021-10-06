package com.biometricsystem.security.jwt;
import com.biometricsystem.security.EmployeeDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Service
public class JwtUtil {

    public static final String SECRET_KEY="ergm0835tFD9v5tc9we89ERTYTU83";
    public static final long EXPIRATION_TIME=1000L*60*60*24; //1 day in milliseconds
    public static final String TOKEN_PREFIX="Bearer ";
    public static final String HEADER="Authorization";

    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractEXPIRATION(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    public Claims extractAllClaims(String token){
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        Claims claims=extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(EmployeeDetails employeeDetails){
        //currently claims is empty. can pass anything you want to include in the JWT payload
        Map<String, Object> claims=new HashMap<>();
        return createToken(claims,employeeDetails.getUsername(),EXPIRATION_TIME);
    }

    public String generateInfiniteToken(EmployeeDetails employeeDetails){
        long expirationTime=EXPIRATION_TIME*365*100; //100 years in milliseconds
        Map<String, Object> claims=new HashMap<>();
        return createToken(claims,employeeDetails.getUsername(),expirationTime);
    }

    public String createToken(Map<String,Object> claims, String employeeNumber, long expirationTime){
        return Jwts.builder().setClaims(claims).setSubject(employeeNumber).setIssuedAt(new Date(System.currentTimeMillis())).
                setExpiration(new Date(System.currentTimeMillis()+expirationTime)).
                signWith(SignatureAlgorithm.HS256,SECRET_KEY).compact();
    }

    public boolean isTokenExpired(String token){
        return extractEXPIRATION(token).before(new Date());
    }

    public boolean validToken(String token, EmployeeDetails employeeDetails){
        String username=extractUsername(token);
        return username.equals(employeeDetails.getUsername()) && !isTokenExpired(token);
    }

}