package com.b2b.admin;

import com.b2b.common.api.ApiException;
import com.b2b.domain.AdminUser;
import com.b2b.domain.AdminUserRepository;
import com.b2b.mall.auth.AuthValidators;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserManageService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserManageService(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> listUsers() {
        return adminUserRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(AdminUserResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public AdminUserResponse createUser(String username, String password) {
        String u = username == null ? "" : username.trim();
        if (!AuthValidators.isValidUsername(u)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "用户名须为 4～32 位字母、数字或下划线");
        }
        if (!AuthValidators.isValidPassword(password)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "密码至少 8 位且含大小写字母与数字");
        }
        if (adminUserRepository.existsByUsername(u)) {
            throw new ApiException(HttpStatus.CONFLICT, "用户名已存在");
        }
        AdminUser entity = new AdminUser(u, passwordEncoder.encode(password));
        entity.setEnabled(true);
        return AdminUserResponse.from(adminUserRepository.save(entity));
    }

    @Transactional
    public AdminUserResponse resetPassword(Long targetUserId, String newPassword) {
        if (!AuthValidators.isValidPassword(newPassword)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "密码至少 8 位且含大小写字母与数字");
        }
        AdminUser u =
                adminUserRepository
                        .findById(targetUserId)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "用户不存在"));
        u.setPasswordHash(passwordEncoder.encode(newPassword));
        return AdminUserResponse.from(adminUserRepository.save(u));
    }

    @Transactional
    public AdminUserResponse setEnabled(Long actorAdminId, Long targetUserId, boolean enabled) {
        AdminUser u =
                adminUserRepository
                        .findById(targetUserId)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "用户不存在"));
        if (!enabled && u.getId().equals(actorAdminId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "不能停用当前登录账号");
        }
        if (!enabled && u.isEnabled() && adminUserRepository.countByEnabledTrue() <= 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "至少保留一名可用后台账号");
        }
        u.setEnabled(enabled);
        return AdminUserResponse.from(adminUserRepository.save(u));
    }

    @Transactional
    public void deleteUser(Long actorAdminId, Long targetUserId) {
        if (targetUserId.equals(actorAdminId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "不能删除当前登录账号");
        }
        if (adminUserRepository.count() <= 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "至少保留一名后台账号");
        }
        AdminUser u =
                adminUserRepository
                        .findById(targetUserId)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "用户不存在"));
        adminUserRepository.delete(u);
    }
}
