package com.oneDev.ecommerce.utils;

import com.oneDev.ecommerce.enumaration.OrderStatus;

import java.util.EnumMap;
import java.util.Set;

public class OrderStateTransaction {

    // Membuat konstanta VALID_TRANSACTIONS untuk menyimpan status pesanan dan status valid berikutnya
    private static final EnumMap<OrderStatus, Set<OrderStatus>> VALID_TRANSACTIONS = new EnumMap<>(OrderStatus.class);

    static {
        // Status PENDING dapat berubah ke PAID, CANCELLED, atau PAYMENT_FAILED
        VALID_TRANSACTIONS.put(OrderStatus.PENDING, Set.of(OrderStatus.PAID, OrderStatus.CANCELLED, OrderStatus.PAYMENT_FAILED));

        // Status PAID hanya dapat berubah ke SHIPPED
        VALID_TRANSACTIONS.put(OrderStatus.PAID, Set.of(OrderStatus.SHIPPED));

        // Status CANCELLED tidak memiliki status berikutnya (transisi berakhir)
        VALID_TRANSACTIONS.put(OrderStatus.CANCELLED, Set.of());

        // Status SHIPPED juga tidak memiliki status berikutnya
        VALID_TRANSACTIONS.put(OrderStatus.SHIPPED, Set.of());

        // Status PAYMENT_FAILED tidak memiliki status berikutnya
        VALID_TRANSACTIONS.put(OrderStatus.PAYMENT_FAILED, Set.of());
    }

    public static boolean isValidTransaction(OrderStatus currentStatus, OrderStatus newStatus) {
        // Mengambil daftar status berikutnya yang valid untuk status saat ini
        Set<OrderStatus> validNextStates = VALID_TRANSACTIONS.get(currentStatus);

        // Jika status saat ini tidak ada dalam VALID_TRANSACTIONS, transisi tidak valid
        if (validNextStates == null) {
            return false;
        }

        // Memeriksa apakah status baru termasuk dalam daftar status yang valid
        return validNextStates.contains(newStatus);
    }
}
