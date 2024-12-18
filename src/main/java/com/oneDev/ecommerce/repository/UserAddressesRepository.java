package com.oneDev.ecommerce.repository;

import com.oneDev.ecommerce.entity.UserAddresses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAddressesRepository extends JpaRepository<UserAddresses, Long> {
    List<UserAddresses> findByUserId(Long userId);
    Optional<UserAddresses> findByUserIdAndIsDefaultTrue(Long userId);

    @Modifying
    @Query(value = """
        UPDATE user_addresses
        SET is_default = false
        WHERE user_id = :userId
""", nativeQuery = true)
    void resetUserDefaultAddresses( @Param("userId") Long userId);

    @Modifying
    @Query(value = """
        UPDATE user_addresses
        SET is_default = true
        WHERE user_address_id = :addressId
""", nativeQuery = true)
    void setDefaultAddresses(@Param("addressId") Long addressId);
}
