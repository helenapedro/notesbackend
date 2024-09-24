package com.notesbackend.filter;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.notesbackend.service.MyUserDetailsService;
import com.notesbackend.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//the JWT filter processes incoming requests and validates the token.
@Component
public class JwtRequestFilter extends OncePerRequestFilter {
	@Autowired
	private JwtUtil jwtUtil;
	
	@Autowired
	private MyUserDetailsService userDetailsService;
	
	 @Override
	 protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
	         throws IOException, ServletException {
	
		     final String authorizationHeader = request.getHeader("Authorization");
	
	     String email = null;
	     String jwt = null;
	
	     if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
	         jwt = authorizationHeader.substring(7);
	         email = jwtUtil.extractEmail(jwt);
	     }
	
	     if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
	         UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);
	         if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
	             UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
	                     userDetails, null, userDetails.getAuthorities());
	             usernamePasswordAuthenticationToken
	                     .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
	             SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
	         }
	     }
	     chain.doFilter(request, response);
	 }
}
