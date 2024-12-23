package com.oneDev.ecommerce.service;

import java.util.Map;

public interface InventoryService {
    boolean checkAndLockInventory(Map<Long, Integer> productQuantities);
    void decreaseProductQuantity(Map<Long, Integer> productQuantities);
    void increaseProductQuantity(Map<Long, Integer> productQuantities);

}
