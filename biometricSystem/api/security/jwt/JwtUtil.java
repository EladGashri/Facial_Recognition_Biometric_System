package com.biometricsystem.api.security.jwt;
import com.biometricsystem.api.employee.EmployeeDetails;
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

    public static final String SECRET_KEY="TAU Faculty of Engineering";
    public static final int EXPIRATION_TIME=1000*60*60*10; //10 hours in milliseconds
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
        //currently claims is empty. can pass anything i want to include in the JWT payload
        Map<String, Object> claims=new HashMap<>();
        return createToken(claims,employeeDetails.getUsername());
    }

    public String createToken(Map<String,Object> claims, String subject){
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis())).
                setExpiration(new Date(System.currentTimeMillis()+EXPIRATION_TIME)).
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