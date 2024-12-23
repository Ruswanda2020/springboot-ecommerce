package com.oneDev.ecommerce.service.impl;

import com.oneDev.ecommerce.entity.Product;
import com.oneDev.ecommerce.enumaration.ExceptionType;
import com.oneDev.ecommerce.exception.ApplicationException;
import com.oneDev.ecommerce.repository.ProductRepository;
import com.oneDev.ecommerce.service.InventoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;

    @Override @Transactional
    public boolean checkAndLockInventory(Map<Long, Integer> productQuantities) {
        // Iterasi untuk setiap pasangan productId dan quantity di map productQuantities.
        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Product product = productRepository.findByIdWithPessimisticLock(entry.getKey())
                    .orElseThrow(
                            ()-> new ApplicationException(ExceptionType.BAD_REQUEST,"Product with id " + entry.getKey() + " does not exist"));

            // Jika stok produk lebih sedikit dari yang diminta, kembalikan false.
            if (product.getStockQuantity() < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    @Override @Transactional
    public void decreaseProductQuantity(Map<Long, Integer> productQuantities) {
        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Product product = productRepository.findByIdWithPessimisticLock(entry.getKey())
                    .orElseThrow(
                            ()-> new ApplicationException(ExceptionType.BAD_REQUEST,"Product with id " + entry.getKey() + " is not found"));

            // Jika stok kurang dari permintaan, lempar exception.
            if (product.getStockQuantity() < entry.getValue()) {
                throw new ApplicationException(ExceptionType.BAD_REQUEST,"Insufficient inventory for product: " + entry.getKey());
            }

            // Kurangi stok produk dan simpan perubahan ke database.
            Integer newQuantity = product.getStockQuantity() - entry.getValue();
            product.setStockQuantity(newQuantity);
            productRepository.save(product);
        }

    }

    @Override @Transactional
    public void increaseProductQuantity(Map<Long, Integer> productQuantities) {
        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Product product = productRepository.findByIdWithPessimisticLock(entry.getKey())
                    .orElseThrow(
                            ()-> new ApplicationException(ExceptionType.BAD_REQUEST,"Product with id " + entry.getKey() + " is not found"));

            // Tambahkan stok produk dan simpan perubahan ke database.
            Integer newQuantity = product.getStockQuantity() + entry.getValue();
            product.setStockQuantity(newQuantity);
            productRepository.save(product);
        }


    }
}
