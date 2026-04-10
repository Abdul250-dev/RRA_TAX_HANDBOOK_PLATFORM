package com.rra.taxhandbook.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rra.taxhandbook.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUserCode(String userCode);
	Optional<User> findByEmail(String email);
	Optional<User> findByInviteToken(String inviteToken);
	Optional<User> findByPasswordResetToken(String passwordResetToken);
	List<User> findAllByOrderByCreatedAtDesc();
	List<User> findByStatusOrderByCreatedAtDesc(String status);
	long countByRole_Id(Long roleId);
	long countByStatusIgnoreCase(String status);
}
