package com.rra.taxhandbook.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rra.taxhandbook.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUserCode(String userCode);
	Optional<User> findByEmail(String email);
	Optional<User> findByInviteToken(String inviteToken);
	Optional<User> findByPasswordResetToken(String passwordResetToken);
	List<User> findByStatusOrderByCreatedAtDesc(String status);
	long countByRole_Id(Long roleId);
	long countByStatusIgnoreCase(String status);

	@Query("""
		select u
		from User u
		where (:status is null or upper(u.status) = upper(:status))
		and (
			:search is null
			or lower(u.fullName) like lower(concat('%', :search, '%'))
			or lower(u.email) like lower(concat('%', :search, '%'))
			or lower(u.userCode) like lower(concat('%', :search, '%'))
		)
		order by u.createdAt desc
	""")
	List<User> findForAdminList(@Param("status") String status, @Param("search") String search);
}
