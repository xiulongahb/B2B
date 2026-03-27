package com.b2b.security;

import java.io.IOException;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            Optional<JwtService.UnifiedPayload> parsed = jwtService.parseToken(token);
            if (parsed.isPresent()) {
                JwtService.UnifiedPayload p = parsed.get();
                UsernamePasswordAuthenticationToken auth = buildAuth(request, p);
                if (auth != null) {
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private static UsernamePasswordAuthenticationToken buildAuth(
            HttpServletRequest request, JwtService.UnifiedPayload p) {
        switch (p.getKind()) {
            case MALL_MEMBER:
                MallMemberPrincipal mp =
                        new MallMemberPrincipal(p.getId(), p.getPhone(), p.getUsername());
                UsernamePasswordAuthenticationToken a =
                        new UsernamePasswordAuthenticationToken(
                                mp, null, mp.getAuthorities());
                a.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                return a;
            case ADMIN:
                AdminPrincipal ap = new AdminPrincipal(p.getId(), p.getUsername());
                UsernamePasswordAuthenticationToken a2 =
                        new UsernamePasswordAuthenticationToken(
                                ap, null, ap.getAuthorities());
                a2.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                return a2;
            case SUPPLIER:
                SupplierPrincipal sp = new SupplierPrincipal(p.getId(), p.getUsername());
                UsernamePasswordAuthenticationToken a3 =
                        new UsernamePasswordAuthenticationToken(
                                sp, null, sp.getAuthorities());
                a3.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                return a3;
            default:
                return null;
        }
    }
}
