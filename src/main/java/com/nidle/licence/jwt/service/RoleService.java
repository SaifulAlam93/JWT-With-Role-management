package com.nidle.licence.jwt.service;

import com.nidle.licence.jwt.dao.RoleDao;
import com.nidle.licence.jwt.entity.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    @Autowired
    private RoleDao roleDao;

    public Role createNewRole(Role role) {
        return roleDao.save(role);
    }
}
