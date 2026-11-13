package it.pa.tributi.tributiservice.config;


import it.pa.tributi.tributiservice.repository.OperatoreRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final String PREFISSO = "Bearer ";

    private final JwtService jwtService;
    private final OperatoreRepository operatoreRepository;

    public JwtFilter(JwtService jwtService, OperatoreRepository operatoreRepository) {
        this.jwtService = jwtService;
        this.operatoreRepository = operatoreRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        estraiToken(request)
                .flatMap(jwtService::estraiCodiceFiscale)
                .ifPresent(this::autentica);

        chain.doFilter(request, response);
    }

    private Optional<String> estraiToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(PREFISSO)) {
            return Optional.empty();
        }
        return Optional.of(header.substring(PREFISSO.length()));
    }

    private void autentica(String codiceFiscale) {
        Ruolo ruolo = operatoreRepository.existsById(codiceFiscale)
                ? Ruolo.OPERATORE
                : Ruolo.CITTADINO;

        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + ruolo.name()));
        var auth = new UsernamePasswordAuthenticationToken(codiceFiscale, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}