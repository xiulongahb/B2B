package com.b2b.mall.member;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "mall_member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 登录名，注册后唯一 */
    @Column(unique = true, length = 64)
    private String username;

    /** BCrypt 哈希，仅用户名密码登录使用 */
    @Column(name = "password_hash", length = 80)
    private String passwordHash;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberType memberType = MemberType.RETAIL;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    protected Member() {}

    /** 兼容旧数据：仅手机号 */
    public Member(String phone, MemberType memberType) {
        this.phone = phone;
        this.memberType = memberType;
    }

    public Member(String username, String passwordHash, String phone, MemberType memberType) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.memberType = memberType;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public MemberType getMemberType() {
        return memberType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }
}
