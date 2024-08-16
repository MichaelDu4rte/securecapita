package com.michael.securecapita.repository.implementation;

import com.michael.securecapita.domain.User;
import com.michael.securecapita.exception.ApiException;
import com.michael.securecapita.repository.RoleRepository;
import com.michael.securecapita.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.michael.securecapita.enumeration.RoleType.*;
import static com.michael.securecapita.enumeration.VerificationType.ACCOUNT;
import static com.michael.securecapita.query.UserQuery.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository<User> {

    private final NamedParameterJdbcTemplate jdbc;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public User create(User user) {
        // Check email is unique
        if(getEmailCount(user.getEmail().trim().toLowerCase()) > 0) throw new ApiException("Email already in use. Please use a different email and try again.");

        // Save User
        try {
            KeyHolder holder = new GeneratedKeyHolder();
            SqlParameterSource parameters = getSqlParameterSource(user);
            jdbc.update(INSERT_USER_QUERY, parameters, holder);
            user.setId(Objects.requireNonNull(Objects.requireNonNull(holder.getKey())).longValue());

            // Add role user
            roleRepository.addRoleUser(user.getId(), ROLE_USER.name());

            // Send verification URL
            String verificationUrl = getVerificationUrl(UUID.randomUUID().toString(), ACCOUNT.getType());

            // Save Url DATABASE
            jdbc.update(INSERT_ACCOUT_VERIFICATION_URL_QUERY, Map.of("userId", user.getId(), "url", verificationUrl));

            // Send Email to user with verification URL
            // emailService.sendVerificationUrl(user.getFirstName(), user.getEmail(), verificationUrl, ACCOUNT);
            user.setEnabled(false);
            user.setNotLocked(true);

            // return created user
            return user;


        } catch (EmptyResultDataAccessException exception) {
            throw new ApiException("No role found by name: " + ROLE_USER.name());

        } catch (Exception exception){
            throw new ApiException("An error occurred. Please try again.");

        }

    }

    @Override
    public Collection<User> list(int page, int pageSize) {
        return null;
    }

    @Override
    public User get(Long id) {
        return null;
    }

    @Override
    public User update(User data) {
        return null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }

    private Integer getEmailCount(String email) {
        return jdbc.queryForObject(COUNT_USER_EMAIL_QUERY, Map.of("email",email), Integer.class);
    }

    private SqlParameterSource getSqlParameterSource(User user) {
        return new MapSqlParameterSource()
                .addValue("firstName", user.getFirstName())
                .addValue("lastname", user.getLastName())
                .addValue("email", user.getEmail())
                .addValue("password", bCryptPasswordEncoder.encode(user.getPassword()));
    }

    private String getVerificationUrl(String key, String type) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/verify/" + type + "/" + key).toUriString();
    }
}
