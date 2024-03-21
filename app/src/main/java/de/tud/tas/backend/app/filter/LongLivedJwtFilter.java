/****************************************************************************************
 *  TUD TAS Backend for the assistance system developed as part of the VerDatAs project
 *  Copyright (C) 2022-2024 TU Dresden (Sebastian Kucharski)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ***************************************************************************************/
package de.tud.tas.backend.app.filter;

import de.tud.tas.backend.app.model.User;
import de.tud.tas.backend.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RequiredArgsConstructor
public class LongLivedJwtFilter extends GenericFilterBean {
    private final UserService userService;
    private final JwtDecoder jwtDecoder;

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(LongLivedJwtFilter.class);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String encodedJwt = this.resolveToken(httpServletRequest);
        if (StringUtils.hasText(encodedJwt)) {
            Jwt jwt = jwtDecoder.decode(encodedJwt);
            if (jwt.getExpiresAt() != null) {
                // Nothing to do here since it is not a long-lived token
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
            if (jwt.getExpiresAt() == null && jwt.getClaims().get("lltid") == null) {
                ((HttpServletResponse) servletResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                log.warn("Attempt to authenticate with long-lived token without corresponding token ID!");
                return;
            }

            String longLivedTokenId = String.valueOf(jwt.getClaims().get("lltid"));
            List<String> validTokenStrings = getValidLongLivedTokens().stream().map(UUID::toString).toList();
            if (!validTokenStrings.contains(longLivedTokenId)) {
                ((HttpServletResponse) servletResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                log.warn("Attempt to authenticate with a revoked long-lived token!");
                return;
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private String resolveToken(HttpServletRequest request) {

        String bearerToken = request.getHeader(AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private List<UUID> getValidLongLivedTokens() {
        return userService.getAllUsers().stream().map(User::getLongLivedTokenId).filter(Objects::nonNull).toList();
    }
}
