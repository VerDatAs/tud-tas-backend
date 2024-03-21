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

import de.tud.tas.backend.app.exceptions.UserNotFoundException;
import de.tud.tas.backend.app.model.User;
import de.tud.tas.backend.app.model.UserLanguage;
import de.tud.tas.backend.app.model.UserRole;
import de.tud.tas.backend.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    }

    @Override
    public User getUserByActorAccountName(String actorAccountName) {
        return userRepository.findByActorAccountName(actorAccountName).orElseThrow(UserNotFoundException::new);
    }

    @Override
    @Transactional
    public User getUserByActorAccountNameOrAddUser(String actorAccountName) {
        try {
            return getUserByActorAccountName(actorAccountName);
        } catch (UserNotFoundException e) {
            return addUser(actorAccountName, UserRole.STUDENT);
        }
    }

    @Override
    public User getUserByIdOrActorAccountName(String userIdOrActorAccountName) {
        UUID userId;
        try {
            userId = UUID.fromString(userIdOrActorAccountName);
        } catch (IllegalArgumentException e) {
            return getUserByActorAccountName(userIdOrActorAccountName);
        }

        return getUser(userId);
    }

    @Override
    @Transactional
    public User addUser(String actorAccountName, UserRole role) {
        return userRepository.save(
                new User(UUID.randomUUID(), actorAccountName, UserLanguage.DE, role));
    }

    @Override
    @Transactional
    public User addUser(String actorAccountName, UserRole role, String password) {
        User user = new User(UUID.randomUUID(), actorAccountName, UserLanguage.DE, role);
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(String actorAccountName) {
        userRepository.deleteByActorAccountName(actorAccountName);
    }

    @Override
    public void updateLongLivedTokenId(UUID userId, UUID longLivedTokenId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        user.setLongLivedTokenId(longLivedTokenId);
        userRepository.save(user);
    }

    @Override
    public void updateLastLoggedInLmsUrl(UUID userId, String lastLoggedInLmsUrl) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        user.setLastLoggedInLmsUrl(lastLoggedInLmsUrl);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserRole updateUserRole(UUID userId, UserRole role) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        user.setRole(role);
        userRepository.save(user);
        return user.getRole();
    }

    @Override
    @Transactional
    public UserLanguage updateUserLanguage(UUID userId, UserLanguage language) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        user.setLanguage(language);
        userRepository.save(user);
        return user.getLanguage();
    }

    @Override
    @Transactional
    public void updatePassword(User user, String rawPassword) {
        updatePassword(user.getId(), rawPassword);
    }

    @Override
    @Transactional
    public void updatePassword(UUID userId, String rawPassword) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
    }
}
