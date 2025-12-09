package org.openpickles.policy.engine.service;

import org.openpickles.policy.engine.model.Group;
import org.openpickles.policy.engine.model.Role;
import org.openpickles.policy.engine.model.User;
import org.openpickles.policy.engine.repository.GroupRepository;
import org.openpickles.policy.engine.repository.RoleRepository;
import org.openpickles.policy.engine.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private GroupRepository groupRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new org.openpickles.policy.engine.exception.FunctionalException(
                        "User not found with id: " + id, "FUNC_005"));
        user.setUsername(userDetails.getUsername());
        user.setRoles(userDetails.getRoles());
        user.setGroups(userDetails.getGroups());
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public Role createRole(Role role) {
        return roleRepository.save(role);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Group createGroup(Group group) {
        return groupRepository.save(group);
    }

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }
}
