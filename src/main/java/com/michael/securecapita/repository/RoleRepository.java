package com.michael.securecapita.repository;

import com.michael.securecapita.domain.Role;
import com.michael.securecapita.domain.User;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Repository;

import java.util.Collection;

public interface RoleRepository<T extends Role> {

    /* Basic CRUD Operations */
    T create(T data);
    Collection<T> list(int page, int pageSize);
    T get(Long id);
    T update(T data);
    Boolean delete(Long id);

    /* More Complex Operations */

    void addRoleUser(Long userId, String roleName);
}
