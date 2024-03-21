/****************************************************************************************
 *  TUD TAS Backend for the assistance system developed as part of the VerDatAs project
 *  Copyright (C) 2022-2024 TU Dresden (Robert Peine, Sebastian Kucharski)
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
package de.tud.tas.backend.app.service;

import de.tud.tas.backend.app.exceptions.PasswordMissingException;
import de.tud.tas.backend.app.exceptions.PasswordWrongException;
import de.tud.tas.backend.app.model.LongLivedToken;
import de.tud.tas.backend.app.model.User;
import de.tud.tas.backend.app.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserService userService;
    private final JwtEncoder jwtEncoder;
    private final Clock clock;
    private final PasswordEncoder passwordEncoder;

    private final String CLAIM_KEY_ROLES = "roles";
    @SuppressWarnings("FieldCanBeLocal")
    private final String CLAIM_KEY_LONG_LIVED_TOKE_ID = "lltid";

    @Override
    @Transactional
    public String login(String actorAccountName, String rawPassword) {
        User user = userService.getUserByActorAccountNameOrAddUser(actorAccountName);

        // FIXME: Check which roles should send a password
        if (user.getRole().equals(UserRole.ADMIN) && rawPassword == null) {
            throw new PasswordMissingException();
        }

        if (rawPassword != null) {
            if (user.getPassword() == null) {
                userService.updatePassword(user, rawPassword);
            } else if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
                throw new PasswordWrongException();
            }
        }

        return createJwt(user);
    }

    @Override
    @Transactional
    public String createJwt(User user) {
        return createJwt(user.getId().toString(), user.getRole());
    }

    @Override
    public String createJwt(String userId, UserRole role) {
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(clock.instant())
                .expiresAt(clock.instant().plus(Duration.ofHours(3)))
                .subject(userId)
                .claim(CLAIM_KEY_ROLES, List.of(role.toString()))
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    @Override
    public String createLongLivedJwt(User user, UUID longLivedTokenId) {
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(clock.instant())
                .subject(user.getId().toString())
                .claim(CLAIM_KEY_ROLES, List.of(user.getRole().toString()))
                .claim(CLAIM_KEY_LONG_LIVED_TOKE_ID, longLivedTokenId.toString())
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    @Override
    @Transactional
    public LongLivedToken createLongLivedTokenOrGetTokenId(UUID userId) {
        User user = userService.getUser(userId);

        if (user.getLongLivedTokenId() != null) {
            return new LongLivedToken(null, user.getLongLivedTokenId());
        }

        UUID longLivedTokenId = UUID.randomUUID();
        userService.updateLongLivedTokenId(user.getId(), longLivedTokenId);

        return new LongLivedToken(createLongLivedJwt(user, longLivedTokenId), longLivedTokenId);
    }
}
