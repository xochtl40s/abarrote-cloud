package com.abarrote.abarroteapi.dto;

import java.util.List;

public class VentaRequest {

    private List<ItemVentaRequest> items;

    public List<ItemVentaRequest> getItems() {
        return items;
    }

    public void setItems(List<ItemVentaRequest> items) {
        this.items = items;
    }
}
