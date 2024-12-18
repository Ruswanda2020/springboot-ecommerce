package com.oneDev.ecommerce.service.impl;

import com.oneDev.ecommerce.entity.UserAddresses;
import com.oneDev.ecommerce.enumaration.ExceptionType;
import com.oneDev.ecommerce.exception.ApplicationException;
import com.oneDev.ecommerce.model.request.UserAddressRequest;
import com.oneDev.ecommerce.model.response.UserAddressResponse;
import com.oneDev.ecommerce.repository.UserAddressesRepository;
import com.oneDev.ecommerce.service.UserAddressService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAddressServiceImpl implements UserAddressService {

    private final UserAddressesRepository userAddressesRepository;

    @Override @Transactional
    public UserAddressResponse createUserAddress(Long userId, UserAddressRequest userAddressRequest) {

        UserAddresses newAddress = UserAddresses.builder()
                .userId(userId)
                .addressName(userAddressRequest.getAddressName())
                .state(userAddressRequest.getState())
                .city(userAddressRequest.getCity())
                .postalCode(userAddressRequest.getPostalCode())
                .country(userAddressRequest.getCountry())
                .streetAddress(userAddressRequest.getStreetAddress())
                .isDefault(userAddressRequest.isDefault())
                .build();

        // periksa apakah sudah ada alamat default yang sebelumnya terdaftar untuk pengguna ini.
        if (userAddressRequest.isDefault()){
            Optional<UserAddresses> existingUserAddresses = userAddressesRepository
                    .findByUserIdAndIsDefaultTrue(userId);

            // Jika ada alamat default yang sudah terdaftar, ubah status default alamat tersebut menjadi false.
            existingUserAddresses.ifPresent(address -> {
                address.setIsDefault(false);
                userAddressesRepository.save(address);
            });
        }

        // simpan alamat baru ke dalam database.
        UserAddresses savedUserAddress = userAddressesRepository.save(newAddress);
        return UserAddressResponse.from(savedUserAddress);
    }

    @Override
    public List<UserAddressResponse> findUserAddressByUserId(Long userId) {
        List<UserAddresses> userAddressesList = userAddressesRepository.findByUserId(userId);
        // Mengonversi daftar entitas UserAddresses ke dalam bentuk respons.
        return userAddressesList.stream()
                .map(UserAddressResponse::from)
                .collect(Collectors.toList());
    }

    @Override @Transactional
    public UserAddressResponse updateUserAddress(Long userAddressId, UserAddressRequest userAddressRequest) {

        // Mencari alamat berdasarkan ID untuk memastikan data ada.
        UserAddresses existingAddress = userAddressesRepository.findById(userAddressId)
                .orElseThrow(() -> new ApplicationException(
                        ExceptionType.RESOURCE_NOT_FOUND, "User address not found with id: " + userAddressId));

        // Membuat objek baru UserAddresses dengan data yang diperbarui.

        UserAddresses userAddresses = UserAddresses.builder()
                .userAddressId(existingAddress.getUserAddressId())
                .userId(existingAddress.getUserId())
                .addressName(userAddressRequest.getAddressName())
                .state(userAddressRequest.getState())
                .city(userAddressRequest.getCity())
                .postalCode(userAddressRequest.getPostalCode())
                .country(userAddressRequest.getCountry())
                .streetAddress(userAddressRequest.getStreetAddress())
                .isDefault(userAddressRequest.isDefault())
                .build();

        // Jika alamat yang diperbarui ditandai sebagai default, cari dan ubah default sebelumnya.
        if (userAddressRequest.isDefault() && !existingAddress.getIsDefault()){
            Optional<UserAddresses> existingUserAddresses = userAddressesRepository
                    .findByUserIdAndIsDefaultTrue(existingAddress.getUserId());

            // Jika ditemukan, ubah status default alamat sebelumnya menjadi false.
            existingUserAddresses.ifPresent(address -> {
                address.setIsDefault(false);
                userAddressesRepository.save(address);
            });
        }

        // Simpan data alamat yang diperbarui ke dalam database dan kembalikan respons.
        UserAddresses savedUserAddress = userAddressesRepository.save(userAddresses);
        return UserAddressResponse.from(userAddresses);
    }

    @Override
    public UserAddressResponse findById(Long userAddressId) {

        UserAddresses userAddresses = userAddressesRepository.findById(userAddressId)
                    .orElseThrow(() -> new ApplicationException(
                            ExceptionType.RESOURCE_NOT_FOUND, "User address not found with id: " + userAddressId));
        return UserAddressResponse.from(userAddresses);
    }

    @Override
    public void deleteUserAddressById(Long userAddressId) {

        UserAddresses existingAddress = userAddressesRepository.findById(userAddressId)
                .orElseThrow(() -> new ApplicationException(
                        ExceptionType.RESOURCE_NOT_FOUND, "User address not found with id: " + userAddressId));
        userAddressesRepository.delete(existingAddress);

        // Jika alamat yang dihapus adalah alamat default,
        // maka cari alamat lain milik pengguna yang sama untuk dijadikan default baru.
        if (existingAddress.getIsDefault()){
            List<UserAddresses> remainingAddresses = userAddressesRepository.findByUserId(existingAddress.getUserId());

            // Jika masih ada alamat yang tersisa, tetapkan alamat pertama sebagai default baru.
            if (!remainingAddresses.isEmpty()){
                UserAddresses newDefaultAddress = remainingAddresses.getFirst();
                userAddressesRepository.save(newDefaultAddress);
            }
        }

    }

    @Override @Transactional
    public UserAddressResponse setDefault(Long userId, Long userAddressId) {
        UserAddresses existingAddress = userAddressesRepository.findById(userAddressId)
                .orElseThrow(() -> new ApplicationException(
                        ExceptionType.RESOURCE_NOT_FOUND, "User address not found with id: " + userAddressId));

        // Memastikan alamat yang akan diatur default milik pengguna yang sesuai.
        if (!existingAddress.getUserId().equals(userId)){
            throw new ApplicationException(ExceptionType.FORBIDDEN, "Address doesn't belong to this user.");

        }

        // Mencari alamat default sebelumnya, jika ada ubah statusnya menjadi false.
        Optional<UserAddresses> existingUserAddresses = userAddressesRepository
                .findByUserIdAndIsDefaultTrue(existingAddress.getUserId());
        existingUserAddresses.ifPresent(address -> {
            address.setIsDefault(false);
            userAddressesRepository.save(address);
        });

        // Tetapkan alamat yang diminta sebagai default baru.
        existingAddress.setIsDefault(true);
        UserAddresses savedUserAddress = userAddressesRepository.save(existingAddress);
        return UserAddressResponse.from(savedUserAddress);
    }
}
