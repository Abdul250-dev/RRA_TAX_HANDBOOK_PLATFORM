package com.rra.taxhandbook.role.repository;

import java.util.List;

import com.rra.taxhandbook.role.entity.Role;

public interface RoleRepository {

	List<Role> findAll();
}
