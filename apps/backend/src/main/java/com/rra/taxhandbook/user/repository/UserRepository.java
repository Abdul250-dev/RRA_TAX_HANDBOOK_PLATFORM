package com.rra.taxhandbook.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rra.taxhandbook.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmployeeId(String employeeId);

	Optional<User> findByEmail(String email);
}
