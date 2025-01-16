package com.oneDev.ecommerce.service;

import java.io.IOException;

public interface BulkReindexService {

    void reindexAllProducts() throws IOException;
}
