package com.rra.taxhandbook.user.repository;

import java.util.List;
import java.util.Optional;

import com.rra.taxhandbook.user.entity.User;

public interface UserRepository {

	List<User> findAll();

	Optional<User> findById(Long id);
}
