package com.oneDev.ecommerce.utils;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

public class PageUtil {

    public static List<Sort.Order> parsSortOrderRequest(String[] sort) {
        List<Sort.Order> orders = new ArrayList<Sort.Order>();
        if (sort[0].contains(",")){
            for (String sortOrder : sort) {
                String[] _sort = sortOrder.split(",");
                orders.add(new Sort.Order(getSortDirection(_sort[1]), _sort[0]));
            }
        } else {
            orders.add(new Sort.Order(getSortDirection(sort[1]), sort[0]));
        }
        return orders;
    }

    private static Sort.Direction getSortDirection (String sort) {
        if (sort.equals("asc")) {
            return Sort.Direction.ASC;
        } else if (sort.equals("desc")) {
            return Sort.Direction.DESC;
        }
        return Sort.Direction.ASC;
    };
}
